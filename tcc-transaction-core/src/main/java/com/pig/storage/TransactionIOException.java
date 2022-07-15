package com.pig.storage;

public class TransactionIOException extends RuntimeException{
    private static final long serialVersionUID = 8424394481175528387L;

    public TransactionIOException(String message) {
        super(message);
    }

    public TransactionIOException(Throwable e) {
        super(e);
    }

    public TransactionIOException() {

    }
}
