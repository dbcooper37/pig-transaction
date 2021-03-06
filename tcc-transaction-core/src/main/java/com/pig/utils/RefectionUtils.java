package com.pig.utils;

import .support.FactoryBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RefectionUtils {

    private static ConcurrentHashMap<Class<?>, Class<?>> cachedDeclaredClasses = new ConcurrentHashMap<>();

    public static void makeAccessible(Method method){
        if((!Modifier.isPublic(method.getModifiers())
            || !Modifier.isPrivate(method.getDeclaringClass().getModifiers())
        )){
            method.trySetAccessible();
        }
    }

    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) throws IllegalArgumentException,IllegalAccessException, NoSuchFieldException, SecurityException{
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f = handler.getClass().getDeclaredField("memberValues");
        f.setAccessible(true);
        Map<String, Object> memberValues;

        memberValues = (Map<String, Object>) f.get(handler);
        Object oldValue = memberValues.get(key);
        if (oldValue==null||oldValue.getClass()!=newValue.getClass()){
            throw new IllegalArgumentException();
        }

        memberValues.put(key,newValue);
        return oldValue;
    }

    public static Class<?> getDeclaringType(Class<?> targetClass, String methodName, Class<?>[] parameterTypes){
        if (cachedDeclaredClasses.get(targetClass)==null){
            Class<?> foundClass = tryFindDeclaredClass(targetClass, methodName, parameterTypes);
            cachedDeclaredClasses.putIfAbsent(targetClass,foundClass);
        }
        return cachedDeclaredClasses.get(targetClass);
    }

    public static Object getNullValue(Class<?> type){
        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        } else if (char.class.equals(type)) {
            return ' ';
        }

        return null;
    }

    private static Class<?> tryFindDeclaredClass(Class<?> aClass, String methodName, Class<?>[] parametersTypes){
        Method method;
        Class<?> findClass = aClass;
        do {
            Class<?>[] clazzes = findClass.getInterfaces();
            for (Class<?> clazz: clazzes){
                try {
                    method = clazz.getDeclaredMethod(methodName,parametersTypes);
                }catch (NoSuchMethodException e){
                    method = null;
                }
                if (method!=null){
                    Object target;
                    try {
                        target = FactoryBuilder.factoryOf(clazz).getInstance();
                    }catch (Exception ignored){
                        target=null;
                    }
                    if (target==null){
                        return aClass;
                    }else{
                        return clazz;
                    }
                }
            }
            findClass= findClass.getSuperclass();
        }while (!findClass.equals(Object.class));

        return aClass;
     }
}

