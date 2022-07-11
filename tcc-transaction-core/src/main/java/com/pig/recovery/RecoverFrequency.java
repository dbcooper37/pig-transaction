package com.pig.recovery;

public interface RecoverFrequency {

    int getMaxRetryCount();

    int getFetchPageSize();

    int getRecoverDuration();

    String getCronExpression();

    int getConcurrentRecoveryThreadCount();
}
