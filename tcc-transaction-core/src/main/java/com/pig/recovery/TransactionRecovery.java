package com.pig.recovery;

import com.pig.Transaction;

import com.pig.api.TransactionStatus;
import com.pig.repository.SentinelTransactionRepository;
import com.pig.repository.TransactionRepository;
import com.pig.support.TransactionConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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
        if (transactionRepository instanceof SentinelTransactionRepository){

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
        transaction.setStatus(TransactionStatus.CONFIRMING);
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
            recoverErorrTransaction(transactionRepository, transaction);
            return null;
        }
    }
}
