package com.pig.api;

import java.lang.reflect.Method;

public interface TransactionContextEditor {
    public TransactionContext get(Object target, Method method, Object[] args);

    public void set(TransactionContext transactionContext, Object target, Method method, Object[] args);
}
