package com.pig.storage;

import com.pig.api.Xid;

import java.io.Closeable;

public interface TransactionStorage extends Closeable {

    int create(TransactionStore transactionStore);

    int update(TransactionStore transactionStore);

    int delete(TransactionStore transactionStore);

    TransactionStore findByXid(String domain, Xid xid);

    TransactionStore findMarkDeletedByXid(String domain, Xid xid);

    int markDeleted(TransactionStore transactionStore);

    int restore(TransactionStore transactionStore);

    boolean supportStorageRecoverable();

    @Override
    default void close() {

    }

}
