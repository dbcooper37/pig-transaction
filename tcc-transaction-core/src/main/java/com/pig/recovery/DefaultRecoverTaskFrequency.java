package com.pig.recovery;

public class DefaultRecoverTaskFrequency implements RecoverFrequency {
    public static final RecoverFrequency INSTANCE = new DefaultRecoverTaskFrequency();
    private int maxRetryCount = 30;
    private int recoverDuration = 30;
    private String cronExpression = "0/15 * * * * ?";
    private int fetchPageSize = 500;
    private int concurrentRecoveryThreadCount = Runtime.getRuntime().availableProcessors() * 2;

    @Override
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    @Override
    public int getFetchPageSize() {
        return fetchPageSize;
    }

    @Override
    public int getRecoverDuration() {
        return recoverDuration;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public int getConcurrentRecoveryThreadCount() {
        return concurrentRecoveryThreadCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setRecoverDuration(int recoverDuration) {
        this.recoverDuration = recoverDuration;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setFetchPageSize(int fetchPageSize) {
        this.fetchPageSize = fetchPageSize;
    }

    public void setConcurrentRecoveryThreadCount(int concurrentRecoveryThreadCount) {
        this.concurrentRecoveryThreadCount = concurrentRecoveryThreadCount;
    }
}
