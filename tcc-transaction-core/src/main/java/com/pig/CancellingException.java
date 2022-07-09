package com.pig;

public class CancellingException extends RuntimeException {


    private static final long serialVersionUID = -303017754217525684L;

    public CancellingException(Throwable cause) {
        super(cause);
    }
}
