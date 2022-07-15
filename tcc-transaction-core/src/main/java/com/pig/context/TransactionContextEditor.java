package com.pig.context;

import com.pig.api.TransactionContext;

import java.lang.reflect.Method;

public interface TransactionContextEditor {
    TransactionContext get(Object target, Method method, Object[] args);

    void set(TransactionContext transactionContext, Object target, Method method, Object[] args);

    default void clear(TransactionContext transactionContext, Object target, Method method, Object[] args) {

    }
}
