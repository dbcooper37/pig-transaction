package com.pig.transaction;

import com.pig.api.TransactionContext;
import com.pig.api.TransactionStatus;
import com.pig.common.TransactionType;
import com.pig.exception.CancellingException;
import com.pig.exception.ConfirmingException;
import com.pig.exception.NoExistedTransactionException;
import com.pig.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TransactionManager {
    static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class.getSimpleName());

    private static final ThreadLocal<Deque<Transaction>> CURRENT = new ThreadLocal<>();
    private final int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
    private final int threadQueueSize = 1024;

    private final ExecutorService asyncTerminatorExecutorService = new ThreadPoolExecutor(threadPoolSize,
            threadPoolSize, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(threadQueueSize), new ThreadPoolExecutor.AbortPolicy()
    );

    private ExecutorService asyncSaveExecutorService = new ThreadPoolExecutor(threadPoolSize,
            threadPoolSize, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(threadQueueSize * 2), new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private TransactionRepository transactionRepository;

    public TransactionManager() {
    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction begin(Object uniqueIdentity) {
        Transaction transaction = new Transaction(uniqueIdentity, TransactionType.ROOT);
        registerTransaction(transaction);
        return transaction;
    }

    public Transaction begin() {
        Transaction transaction = new Transaction(TransactionType.ROOT);
        registerTransaction(transaction);
        return transaction;
    }

    public Transaction propagationNewBegin(TransactionContext transactionContext) {
        Transaction transaction = new Transaction(transactionContext);
        registerTransaction(transaction);
        return transaction;
    }

    public Transaction propagationExistBegin(TransactionContext transactionContext) throws NoExistedTransactionException {
        Transaction transaction = transactionRepository.findByXid(transactionContext.getXid());
        if (transaction != null) {
            registerTransaction(transaction);
            return transaction;
        } else {
            throw new NoExistedTransactionException();
        }
    }

    public void enlistParticipant(Participant participant) {
        Transaction transaction = this.getCurrentTransaction();
        transaction.enlistParticipant(participant);
        if (transaction.getVersion() == 0L) {
            transactionRepository.create(transaction);
        } else {
            transactionRepository.update(transaction);
        }
    }

    public void commit(boolean asyncCommit) {
        final Transaction transaction = getCurrentTransaction();
        transaction.changeStatus(TransactionStatus.CONFIRMING);
        transactionRepository.update(transaction);
        if (asyncCommit) {
            try {
                long startTime = System.currentTimeMillis();
                asyncTerminatorExecutorService.submit(() -> commitTransaction(transaction));
                LOGGER.debug("async submit cost time: {}",(System.currentTimeMillis() - startTime));
            } catch (Throwable commitException) {
                LOGGER.warn("compensable transaction async submit confirm failed, recovery job will try to confirm later.", commitException.getCause());
            }
        } else {
            commitTransaction(transaction);
        }
    }

    public void rollback(boolean asyncRollback) {
        final Transaction transaction = getCurrentTransaction();
        transaction.changeStatus(TransactionStatus.CANCELLING);
        transactionRepository.update(transaction);
        if (asyncRollback) {
            try {
                asyncTerminatorExecutorService.submit(() -> rollbackTransaction(transaction));
            } catch (Throwable rollbackException) {
                LOGGER.warn("compensable transaction async rollback failed, recovery job will try to rollback later.", rollbackException);
                throw new CancellingException(rollbackException);
            }
        }
    }

    private void commitTransaction(Transaction transaction) {
        try {
            transaction.commit();
            transactionRepository.delete(transaction);
        } catch (Throwable commitException) {
            try {
                transactionRepository.update(transaction);
            } catch (Exception e) {
                // ignore the exception
            }
            LOGGER.warn("compensable transaction confirm failed, recovery job will try to confirm later.", commitException);
            throw new ConfirmingException(commitException);
        }
    }

    private void rollbackTransaction(Transaction transaction) {
        try {
            transaction.rollback();
            transactionRepository.delete(transaction);
        } catch (Throwable rollbackException) {

            LOGGER.info("try save updated transaction");
            //try save updated transaction
            try {
                transactionRepository.update(transaction);
            } catch (Exception e) {
                //ignore any exception here
            }

            LOGGER.warn("compensable transaction rollback failed, recovery job will try to rollback later.", rollbackException);
            throw new CancellingException(rollbackException);
        }
    }

    public Transaction getCurrentTransaction() {
        if (isTransactionActive()) {
            return CURRENT.get().peek();
        }
        return null;
    }

    public boolean isTransactionActive() {
        Deque<Transaction> transactions = CURRENT.get();
        return transactions != null && !transactions.isEmpty();
    }

    private void registerTransaction(Transaction transaction) {
        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<>());
        }
        CURRENT.get().push(transaction);
    }

    public void changeStatus(TransactionStatus status) {
        changeStatus(status);
    }
    public void changeStatus(TransactionStatus status, boolean asyncSave) {
        Transaction transaction = this.getCurrentTransaction();
        transaction.setStatus(status);

        if (asyncSave) {
            asyncSaveExecutorService.submit(new AsyncSaveTask(transaction));
        } else {
            transactionRepository.update(transaction);
        }
    }
    public void cleanAfterCompletion(Transaction transaction) {
        if (isTransactionActive() && transaction != null) {
            Transaction currentTransaction = getCurrentTransaction();
            if (currentTransaction == transaction) {
                CURRENT.get().pop();
                if (CURRENT.get().size() == 0) {
                    CURRENT.remove();
                }
            } else {
                throw new SystemException("Illegal transaction when clean after completion");
            }
        }
    }

    class AsyncSaveTask implements Runnable {
        private final Transaction transaction;

        public AsyncSaveTask(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        public void run() {
            try {
                if (transaction != null && transaction.getStatus().equals(TransactionStatus.TRY_SUCCESS)) {
                    Transaction foundTransaction = transactionRepository.findByXid(transaction.getXid());
                    if (foundTransaction != null && foundTransaction.getStatus().equals(TransactionStatus.TRYING)) {
                        transactionRepository.update(transaction);
                    }
                }
            } catch (Exception e) {
                // ignore the exception
            }
        }
    }
}
