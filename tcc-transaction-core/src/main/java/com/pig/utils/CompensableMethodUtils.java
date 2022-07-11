package com.pig.utils;

import com.pig.api.Compensable;
import com.pig.api.Propagation;
import com.pig.api.TransactionContext;
import com.pig.common.ParticipantRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public class CompensableMethodUtils {

    private CompensableMethodUtils() {
        throw new UnsupportedOperationException();
    }

    public static Method getCompensableMethod(ProceedingJoinPoint proceedingJoinPoint) {
        Method method = ((MethodSignature) (proceedingJoinPoint.getSignature())).getMethod();
        if (method.getAnnotation(Compensable.class) == null) {
            try {
                method = proceedingJoinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());

            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return method;
    }

    public static ParticipantRole calcualateMethodType(Propagation propagation, boolean isTransactionActive, TransactionContext transactionContext) {
        if ((propagation.equals(Propagation.REQUIRED) && !isTransactionActive && transactionContext == null) || propagation.equals(Propagation.REQUIRES_NEW)) {
            return ParticipantRole.ROOT;
        } else if ((propagation.equals(Propagation.REQUIRED) || propagation.equals(Propagation.MANDATORY)) || !isTransactionActive && transactionContext != null) {
            return ParticipantRole.PROVIDER;
        } else {
            return ParticipantRole.NORMAL;
        }
    }

    public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {
        int position = -1;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(com.pig.api.TransactionContext.class)) {
                position = 1;
                break;
            }
        }
        return position;
    }
}
