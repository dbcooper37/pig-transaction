package com.pig.recovery;

import com.alibaba.fastjson.JSON;
import com.pig.Transaction;

import com.pig.TransactionOptimisticLockException;
import com.pig.api.TransactionStatus;
import com.pig.common.TransactionType;
import com.pig.repository.LocalStoreable;
import com.pig.repository.Page;
import com.pig.repository.SentinelTransactionRepository;
import com.pig.repository.TransactionRepository;
import com.pig.support.TransactionConfigurator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.pig.api.TransactionStatus.CANCELLING;
import static com.pig.api.TransactionStatus.CONFIRMING;


public class TransactionRecovery {
    public static final int CONCURRENT_RECOVERY_TIMEOUT = 60;
    public static final int MAX_ERROR_COUNT_SHREDHOLD = 15;

    static final Logger logger = LoggerFactory.getLogger(TransactionRecovery.class.getSimpleName());

    static volatile ExecutorService recoveryExecutorService = null;

    private TransactionConfigurator transactionConfigurator;

    private AtomicInteger triggerMaxRetryPrintCount = new AtomicInteger();

    private AtomicInteger recoveryFailedPrintCount = new AtomicInteger();

    private volatile int logMaxPrintCount = MAX_ERROR_COUNT_SHREDHOLD;

    private Lock logSync = new ReentrantLock();

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }

    public void startRecover() {
        ensureRecoveryInitialized();
        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        if (transactionRepository instanceof SentinelTransactionRepository) {
            SentinelTransactionRepository sentinelTransactionRepository = (SentinelTransactionRepository) transactionRepository;
            if (!sentinelTransactionRepository.getSentinelController().degrade()) {
                startRecover(sentinelTransactionRepository.getWorkTransactionRepository());
            } else {
                startRecover(sentinelTransactionRepository.getDegradedTransactionRepository());
            }
        } else {
            startRecover(transactionRepository);
        }
    }

    private void startRecover(TransactionRepository transactionRepository) {
        Lock recoveryLock = transactionRepository instanceof LocalStoreable ? RecoveryLock.DEFAULT_LOCK : transactionConfigurator.getRecoveryLock();
        if (recoveryLock.tryLock()) {
            try {
                String offset = null;
                int totalCount = 0;
                do {
                    Page<Transaction> page = loadErrorTransactionByPage(transactionRepository, offset);
                    if (page.getData().size() > 0) {
                        concurrentRecoveryErrorTransactions(transactionRepository, page.getData());
                        offset = page.getNextOffset();
                        totalCount += page.getData().size();
                    } else {
                        break;
                    }
                } while (true);

                logger.debug(String.format("total recovery count %d from repository:%s", totalCount, transactionRepository.getClass().getName()
                ));
            } catch (Throwable e) {
                logger.error(String.format("recovery failed from repository: %s", transactionRepository.getClass().getName()), e);
            } finally {
                recoveryLock.unlock();
            }
        }
    }

    private Page<Transaction> loadErrorTransactionByPage(TransactionRepository transactionRepository, String offset) {
        long currentTimeInMillis = Instant.now().toEpochMilli();
        RecoverFrequency recoveryFrequency = transactionConfigurator.getRecoverFrequency();
        return transactionRepository.findAllUnmodifiedSince(new Date(currentTimeInMillis - recoveryFrequency.getRecoverDuration() * 1000L), offset, recoveryFrequency.getFetchPageSize());
    }

    private void concurrentRecoveryErrorTransactions(TransactionRepository transactionRepository, List<Transaction> transactions) throws InterruptedException, ExecutionException {
        initLogStatistics();
        List<RecoverTask> tasks = new ArrayList<>();
        for (Transaction transaction : transactions) {
            tasks.add(new RecoverTask(transactionRepository, transaction));
        }
        List<Future<Void>> futures = recoveryExecutorService.invokeAll(tasks, CONCURRENT_RECOVERY_TIMEOUT, TimeUnit.SECONDS);
        for (Future future : futures) {
            future.get();
        }

    }

    private void recoverErrorTransaction(TransactionRepository transactionRepository, Transaction transaction) {
        if (transaction.getRetriedCount() > transactionConfigurator.getRecoverFrequency().getMaxRetryCount()) {
            logSync.lock();
            try {
                if (triggerMaxRetryPrintCount.get() < logMaxPrintCount) {
                    logger.error("recover failed with max retry count, will not try again. txid %s | status %s | retried count %d | transaction content %s",
                            transaction.getXid(),
                            transaction.getStatus().getId(),
                            transaction.getRetriedCount(),
                            JSON.toJSONString(transaction)
                    );
                    triggerMaxRetryPrintCount.incrementAndGet();
                } else if (triggerMaxRetryPrintCount.get() == logMaxPrintCount) {
                    logger.error("To many transaction's retried count max the MaxRetryCount during one page transaction recover process, will not print error again!");
                }
            } finally {
                logSync.unlock();
            }
            return;
        }
        try {
            if (transaction.getTransactionType().equals(TransactionType.ROOT)) {
                switch (transaction.getStatus()) {
                    case CONFIRMING:
                        commitTransaction(transactionRepository, transaction);
                        break;
                    case CANCELLING:
                        rollbackTransaction(transactionRepository, transaction);
                        break;
                    default:
                        break;
                }
            } else {
                switch (transaction.getStatus()) {
                    case CONFIRMING:
                        commitTransaction(transactionRepository, transaction);
                        break;
                    case CANCELLING:
                    case TRY_FAILED:
                        rollbackTransaction(transactionRepository, transaction);
                        break;
                    case TRY_SUCCESS:
                        if (transactionRepository.getRootDomain() == null) {
                            break;
                        }

                        Transaction rootTransaction = transactionRepository.findByRootXid(transaction.getRootXid());
                        if (rootTransaction == null) {
                            rollbackTransaction(transactionRepository, transaction);
                        } else {
                            switch (rootTransaction.getStatus()) {
                                case CONFIRMING:
                                    commitTransaction(transactionRepository, transaction);
                                    break;
                                case CANCELLING:
                                    rollbackTransaction(transactionRepository, transaction);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;

                    default:
                        break;
                }
            }
        } catch (Throwable throwable) {
            if (throwable instanceof TransactionOptimisticLockException || ExceptionUtils.getRootCause(throwable) instanceof TransactionOptimisticLockException) {
                logger.warn(String.format("" +
                                "optimisticLockException happened while recover txid %s | status %s | retried count %d",
                        transaction.getXid(),
                        transaction.getStatus(),
                        transaction.getRetriedCount()
                ));
            } else {
                logSync.lock();
                try {
                    if (recoveryFailedPrintCount.get() < logMaxPrintCount) {
                        logger.error(String.format("recover failed, txid %d | status %s | retried count %d | transaction content %s",
                                transaction.getXid(),
                                transaction.getStatus(),
                                transaction.getRetriedCount(),
                                JSON.toJSONString(transaction)
                        ));
                    } else if (recoveryFailedPrintCount.get() == logMaxPrintCount) {
                        logger.error("Too many transaction' recover error during one page transactions recover process. will not print errors again!");
                    }
                } finally {
                    logSync.unlock();
                }
            }
        }
    }

    private void rollbackTransaction(TransactionRepository transactionRepository, Transaction transaction) {
        transaction.setRetriedCount(transaction.getRetriedCount() + 1);
        transaction.setStatus(TransactionStatus.CANCELLING);
        transactionRepository.update(transaction);
        transaction.rollback();
        transactionRepository.delete(transaction);
    }

    private void commitTransaction(TransactionRepository transactionRepository, Transaction transaction) {
        transaction.setRetriedCount(transaction.getRetriedCount());
        transaction.setStatus(CONFIRMING);
        transactionRepository.update(transaction);
        transaction.commit();
        transactionRepository.delete(transaction);
    }

    private void ensureRecoveryInitialized() {
        if (recoveryExecutorService == null) {
            synchronized (TransactionRecovery.class) {
                if (recoveryExecutorService == null) {
                    recoveryExecutorService = Executors.newFixedThreadPool(transactionConfigurator.getRecoverFrequency().getConcurrentRecoveryThreadCount());
                    logMaxPrintCount = Math.min(transactionConfigurator.getRecoverFrequency().getFetchPageSize() / 2, MAX_ERROR_COUNT_SHREDHOLD);
                }
            }
        }
    }

    private void initLogStatistics() {
        triggerMaxRetryPrintCount.set(0);
        recoveryFailedPrintCount.set(0);
    }

    class RecoverTask implements Callable<Void> {
        TransactionRepository transactionRepository;
        Transaction transaction;

        public RecoverTask(TransactionRepository transactionRepository, Transaction transaction) {
            this.transactionRepository = transactionRepository;
            this.transaction = transaction;
        }

        @Override
        public Void call() throws Exception {
            recoverErrorTransaction(transactionRepository, transaction);
            return null;
        }
    }
}
