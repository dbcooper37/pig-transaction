package com.pig;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvocationContext implements Serializable {
    private static final long serialVersionUID = 3826194178550594569L;
    private Class targetClass;
    private String methodName;
    private Class[] parameterType;
    private Object[] args;
    private final Map<String,String> attachments = new ConcurrentHashMap<>();

    public InvocationContext(){}

    public InvocationContext(Class targetClass, String methodName, Class[] parameterType, Object[] args) {
        this.targetClass = targetClass;
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.args = args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterType() {
        return parameterType;
    }

    public Object[] getArgs() {
        return args;
    }

    public void addAttachment(String key, String value){
        attachments.put(key, value);
    }
}
