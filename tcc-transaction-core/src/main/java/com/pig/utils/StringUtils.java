package com.pig.utils;

public class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isNotEmpty(String value){
        if(value==null){
            return false;
        }
        return !value.equals("");
    }
}
