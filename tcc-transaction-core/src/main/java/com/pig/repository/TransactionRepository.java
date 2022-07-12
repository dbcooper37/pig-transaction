package com.pig.repository;

import com.pig.Transaction;

import javax.transaction.xa.Xid;
import java.io.Closeable;
import java.util.Date;

public interface TransactionRepository extends Closeable {
    String getDomain();
    String getRootDomain();
    int create(Transaction transaction);
    int update(Transaction transaction);
    int delete(Transaction transaction);
    Transaction findByXid(Xid xid);

    Transaction findByRootXid(Xid xid);
    Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize);

    @Override
    default void close(){}
}