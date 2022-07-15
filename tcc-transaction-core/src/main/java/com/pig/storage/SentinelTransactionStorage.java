package com.pig.storage;

import com.pig.api.Xid;
import com.pig.serializer.TransactionStoreSerializer;

public class SentinelTransactionStorage extends AbstractTransactionStorage {

    public SentinelTransactionStorage(TransactionStoreSerializer serializer, StoreConfig storeConfig) {
        super(serializer, storeConfig);
    }

    @Override
    protected int doCreate(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doUpdate(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doDelete(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doMarkDeleted(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doRestore(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected TransactionStore doFindOne(String domain, Xid xid, boolean isMarkDeleted) {
        return null;
    }

    @Override
    public TransactionStore findByXid(String domain, Xid xid) {
        return null;
    }

    @Override
    public TransactionStore findMarkDeletedByXid(String domain, Xid xid) {
        return null;
    }

    @Override
    public boolean supportStorageRecoverable() {
        return false;
    }
}
