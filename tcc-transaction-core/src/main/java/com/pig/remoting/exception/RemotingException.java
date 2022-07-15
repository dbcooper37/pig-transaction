package com.pig.remoting.exception;

import com.pig.exception.SystemException;

public class RemotingException extends SystemException {
    public RemotingException(String message) {
        super(message);
    }

    public RemotingException(String message, Throwable e) {
        super(message, e);
    }
}
