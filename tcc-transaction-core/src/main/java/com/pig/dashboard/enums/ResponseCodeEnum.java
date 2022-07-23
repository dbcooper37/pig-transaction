package com.pig.dashboard.enums;

public enum ResponseCodeEnum {
    SUCCESS(200, "Success"),
    REQUEST_PARAM_ERROR(400, "Parameter error"),
    UNKOWN_ERROR(500, "Unknown exception"),
    NOT_SUPPORT(405, "Not supported"),
    NOT_SUPPORT_WITH_MESSAGE(405, "%s does not support"),

    // Domain management related
    DOMAIN_ILLEGAL_ALERT_TYPE(10100, "Illegal Alert Type"),
    DOMAIN_NOT_EXIST(10101, "Domain record does not exist"),

    // login related
    LOGIN_USER_NOT_EXST(10200, "User does not exist"),
    LOGIN_PASSWORD_ILLEGAL(10201, "Incorrect password"),
    LOGIN_ERROR(10209, "Login failed"),
    LOGIN_ERROR_WITH_MESSAGE(10209, "Login failed: %s"),

    // Task management related
    TASK_OPERATE_NOT_SUPPORT(10300, "Task operation not supported"),
    TASK_STATUS_ERROR(10301, "Task status is abnormal"),
    TASK_MODIFY_CRON_ERROR(10302, "Update cron exception"),
    TASK_OPERATE_ERROR(10309, "Task operation abnormal"),

    // alarm related
    ALERT_DING_ERROR(10400, "Dingding alarm is abnormal"),


    ;

    private int responseCode;
    private String responseMessage;

    ResponseCodeEnum(int responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage(Object... args) {
        try {
            return String.format(this.responseMessage, args);
        } catch (Exception e) {
            return this.responseMessage;
        }
    }

    public String getCode() {
        return String.valueOf(this.responseCode);
    }

    public String getMessage() {
        return this.responseMessage;
    }
}
