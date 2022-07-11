package com.pig.recovery;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzRecoveryTask implements Job {
    public final static String RECOVERY_INSTANCE_KEY = "transactionKey";
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        TransactionRecovery transactionRecovery = (TransactionRecovery) jobExecutionContext.getMergedJobDataMap().get(RECOVERY_INSTANCE_KEY);
        transactionRecovery.startRecover();
    }
}
