package com.pig;

import com.pig.api.TransactionContext;
import com.pig.api.TransactionContextEditor;
import com.pig.support.FactoryBuilder;
import com.pig.utils.StringUtils;

import java.lang.reflect.Method;

public class Terminator {

    public Terminator(){}

    public static Object invoke(TransactionContext transactionContext, InvocationContext invocationContext,
                              Class<? extends TransactionContextEditor> transactionContextEditorClass){
        if(StringUtils.isNotEmpty(invocationContext.getMethodName())){
            try{
                Object target = FactoryBuilder.factoryOf(invocationContext.getTargetClass()).getInstance();
                Method method;
                method = target.getClass().getMethod(invocationContext.getMethodName(),invocationContext.getParameterType());
                FactoryBuilder.factoryOf(transactionContextEditorClass).getInstance().set(transactionContext,target,method,invocationContext.getArgs());
                return method.invoke(target,invocationContext.getArgs());
            }catch (Exception e){
                throw new SystemException(e);
            }
        }
        return null;
    }
}
