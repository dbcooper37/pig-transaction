package com.pig.support;

import com.pig.transaction.TransactionManager;
import com.pig.recovery.RecoverFrequency;

import java.util.concurrent.locks.Lock;

public interface TransactionConfigurator {

    TransactionManager getTransactionManager();
    TransactionRepository getTransactionRepository();
    RecoverFrequency getRecoverFrequency();
    Lock getRecoveryLock();
}
