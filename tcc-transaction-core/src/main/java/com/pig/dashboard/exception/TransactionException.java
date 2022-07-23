package com.pig.dashboard.exception;

import com.pig.dashboard.enums.ResponseCodeEnum;

public class TransactionException extends RuntimeException{
    private String errorCode;
    private String errorMessage;

    public TransactionException() {
        super();
    }

    public TransactionException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public TransactionException(ResponseCodeEnum responseCodeEnum) {
        this(responseCodeEnum.getCode(), responseCodeEnum.getMessage());
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return this.errorCode + "-" + this.errorMessage;
    }
}
