package com.pig.context;

import com.pig.api.TransactionContext;

public class TransactionContextHolder {
    private static ThreadLocal<TransactionContext> transactionContextThreadLocal = new ThreadLocal<TransactionContext>();

    public static TransactionContext getCurrentTransactionContext() {
        return transactionContextThreadLocal.get();
    }

    public static void setCurrentTransactionContext(TransactionContext transactionContext) {
        transactionContextThreadLocal.set(transactionContext);
    }

    public static void clear() {
        transactionContextThreadLocal.remove();
    }
}
