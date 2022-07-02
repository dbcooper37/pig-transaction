package com.pig.api;

import java.lang.reflect.Method;

public class ParameterTransactionContextEditor implements TransactionContextEditor {

    public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {
        int position = -1;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(com.pig.api.TransactionContext.class)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static boolean hasTransactionContextParameter(Class<?>[] parameterTypes) {
        return getTransactionContextParamPosition(parameterTypes) >= 0;
    }

    public static TransactionContext getTransactionContextFromArgs(Object[] args) {
        TransactionContext transactionContext = null;
        for (Object object : args) {
            if (object != null && com.pig.api.TransactionContext.class.isAssignableFrom(object.getClass())) {
                transactionContext = (com.pig.api.TransactionContext) object;
            }
        }
        return transactionContext;
    }

    @Override
    public TransactionContext get(Object target, Method method, Object[] args) {
        int position = getTransactionContextParamPosition(method.getParameterTypes());
        if (position > 0) {
            return (TransactionContext) args[position];
        } else {
            throw new RuntimeException("No TransactionContext parameter exist while get TransactionContext with ParameterTransactionContextEditor!");

        }
    }

    @Override
    public void set(TransactionContext transactionContext, Object target, Method method, Object[] args) {
        int position = getTransactionContextParamPosition(method.getParameterTypes());
        if (position >= 0) {
            args[position] = transactionContext;
        } else {
            throw new RuntimeException("No TransactionContext parameter exist while set TransactionContext with ParameterTransactionContextEditor!");
        }
    }
}
