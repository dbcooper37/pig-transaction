package com.pig.remoting.exception;

public class RemotingConnectException extends RemotingException {
    public RemotingConnectException(String message) {
        super(message);
    }

    public RemotingConnectException(String message, Throwable e) {
        super("connect to " + message + " failed", e);
    }
}
