package com.pig.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

public class TccDateFormatUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return DateFormatUtils.format(date, DATE_FORMAT);
    }
}
