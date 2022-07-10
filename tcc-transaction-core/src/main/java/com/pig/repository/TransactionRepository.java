package com.pig.repository;

import com.pig.Transaction;

import javax.transaction.xa.Xid;
import java.io.Closeable;
import java.util.Date;

public interface TransactionRepository extends Closeable {
    String Domain();
    String getRootDomain();
    int create(Transaction transaction);
    int update(Transaction transaction);
    int delete(Transaction transaction);
    Transaction findById(Xid id);
    Transaction findByRootId(Xid id);
    Page<Transaction> findAllUnmodifiedSince(Date date, String offset, int pageSize);

    @Override
    default void close(){}
}