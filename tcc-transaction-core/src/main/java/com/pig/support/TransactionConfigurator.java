package com.pig.support;

import com.pig.TransactionManager;
import com.pig.recovery.RecoverFrequency;
import com.pig.repository.TransactionRepository;

import java.util.concurrent.locks.Lock;

public interface TransactionConfigurator {

    TransactionManager getTransactionManager();
    TransactionRepository getTransactionRepository();
    RecoverFrequency getRecoverFrequency();
    Lock getRecoveryLock();
}
