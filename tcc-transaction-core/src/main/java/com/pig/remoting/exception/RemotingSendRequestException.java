package com.pig.remoting.exception;

public class RemotingSendRequestException extends RemotingException {
    public RemotingSendRequestException(String message) {
        super(message);
    }

    public RemotingSendRequestException(String message, Throwable e) {
        super("Send request to <" + message + "> failed", e);
    }
}
