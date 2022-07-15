package com.pig.storage.domain;

import java.util.HashMap;
import java.util.Map;

public enum AlertType {
    DING,
    SMS,
    PHONE;

    private static Map<String, AlertType> alertTypeMap = new HashMap<>(3);

    static {
        for (AlertType alertType : AlertType.values()) {
            alertTypeMap.put(alertType.name(), alertType);
        }
    }

    public static AlertType nameOf(String name) {
        return alertTypeMap.get(name);
    }
}
