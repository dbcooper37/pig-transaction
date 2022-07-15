package com.pig.remoting.exception;

public class RemotingTimeoutException extends RemotingException {
    public RemotingTimeoutException(String addr) {
        super("invoke to channel<" + addr + "> failed.");
    }

    public RemotingTimeoutException(String addr, long timeoutMillis) {
        this(addr, timeoutMillis, null);
    }

    public RemotingTimeoutException(String addr, long timeoutMillis, Throwable cause) {
        super("wait response on the channel <" + addr + "> timeout, " + timeoutMillis + "(ms)", cause);
    }
}
