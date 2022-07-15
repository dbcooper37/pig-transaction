package com.pig.remoting.exception;

public class RemotingCommandException extends RemotingException {
    public RemotingCommandException(String message) {
        super(message);
    }

    public RemotingCommandException(String message, Throwable e) {
        super(message, e);
    }
}
