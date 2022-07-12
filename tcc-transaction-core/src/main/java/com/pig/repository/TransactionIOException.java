package com.pig.repository;

import com.pig.Transaction;

public class TransactionIOException extends RuntimeException{
    private static final long serialVersionUID = 8424394481175528387L;

    public TransactionIOException(String name){
        super(name);
    }

    public TransactionIOException(Throwable e){
        super(e);
    }
}
