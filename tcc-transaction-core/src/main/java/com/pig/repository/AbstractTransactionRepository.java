package com.pig.repository;

import com.pig.Transaction;
import com.pig.TransactionOptimisticLockException;

import javax.transaction.xa.Xid;
import java.util.Date;

public abstract class AbstractTransactionRepository implements TransactionRepository, AutoCloseable{
    public AbstractTransactionRepository(){}

    @Override
    public int create(Transaction transaction){
        transaction.setVersion(1l);
        return doCreate(transaction);
    }

    @Override
    public int update(Transaction transaction){
        int result =0;
        result = doUpdate(transaction);
        if (result<=0){
            throw new TransactionOptimisticLockException();
        }
        return result;
    }

    @Override
    public int delete(Transaction transaction){
        return doDelete(transaction);
    }

    @Override
    public Transaction findByXid(Xid transactionXid){
        return doFindOne(transactionXid);
    }

    @Override
    public Transaction findByRootXid(Xid transactionXid){
        return doFindRootOne(transactionXid);
    }

    @Override
    public Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize){
        return doFindAllUnmodifiedSince(date,offset,pageSize);
    }

    protected abstract int doCreate(Transaction transaction);
    protected abstract int doUpdate(Transaction transaction);
    protected abstract int doDelete(Transaction transaction);
    protected abstract Transaction doFindOne(Xid xid);
    protected abstract Transaction doFindRootOne(Xid xid);
    protected abstract Page<Transaction> doFindAllUnmodifiedSince(Date date, String offset, int pageSize);
}
