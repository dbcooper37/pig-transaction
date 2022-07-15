package com.pig.recovery;

import com.pig.transaction.TransactionManager;
import com.pig.support.TransactionConfigurator;

import java.util.concurrent.locks.Lock;

public class RecoverConfiguration implements TransactionConfigurator {
    private TransactionManager transactionManager;
    private TransactionRepository transactionRepository;
    private RecoverFrequency recoverFrequency;
    private RecoveryLock recoveryLock = RecoveryLock.DEFAULT_LOCK;
    @Override
    public TransactionManager getTransactionManager() {
        return null;
    }

    @Override
    public TransactionRepository getTransactionRepository() {
        return null;
    }

    @Override
    public RecoverFrequency getRecoverFrequency() {
        return null;
    }

    @Override
    public Lock getRecoveryLock() {
        return null;
    }
}
