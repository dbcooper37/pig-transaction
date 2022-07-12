package com.pig.repository;

import com.pig.SystemException;
import com.pig.Transaction;
import com.pig.ha.SentinelController;

import javax.transaction.xa.Xid;
import java.util.Date;

public class SentinelTransactionRepository extends AbstractTransactionRepository{

    private SentinelController sentinelController;
    private AbstractTransactionRepository workTransactionRepository;
    private AbstractTransactionRepository degradedTransactionRepository;
    public SentinelTransactionRepository(){}

    public void init(){
        if (workTransactionRepository==null){
            throw new SystemException("workTransactionRepository can't be null");
        }
        if (degradedTransactionRepository==null){
            throw new SystemException("degradedTransactionRepository can't be null");
        }
        if (sentinelController==null){
            throw new SystemException("sentinelController can't be null");
        }
    }
    @Override
    protected int doCreate(Transaction transaction) {
        if (!sentinelController.degrade()){
            return workTransactionRepository.doCreate(transaction);
        }else{
            return degradedTransactionRepository.doCreate(transaction);
        }
    }


    @Override
    protected int doUpdate(Transaction transaction) {
        if (!sentinelController.degrade()){
            return workTransactionRepository.doUpdate(transaction);
        }else{
            return degradedTransactionRepository.doUpdate(transaction);
        }
    }

    @Override
    protected int doDelete(Transaction transaction) {
        if (!sentinelController.degrade()){
            return workTransactionRepository.doDelete(transaction);
        }else{
            return degradedTransactionRepository.doDelete(transaction);
        }
    }

    @Override
    protected Transaction doFindOne(Xid xid) {
        if (!sentinelController.degrade()){
            return workTransactionRepository.doFindOne(xid);
        }else{
            return degradedTransactionRepository.doFindOne(xid);
        }
    }

    @Override
    protected Transaction doFindRootOne(Xid xid) {
        if (!sentinelController.degrade()){
            return workTransactionRepository.doFindRootOne(xid);
        }else{
            return degradedTransactionRepository.doFindRootOne(xid);
        }
    }

    @Override
    protected Page<Transaction> doFindAllUnmodifiedSince(Date date, String offset, int pageSize) {
        if (!sentinelController.degrade()){
            return workTransactionRepository.doFindAllUnmodifiedSince(date,offset,pageSize);
        }else{
            return degradedTransactionRepository.doFindAllUnmodifiedSince(date,offset,pageSize);
        }
    }

    @Override
    public String getDomain() {
        if (!sentinelController.degrade()){
            return workTransactionRepository.getDomain();
        }else{
            return degradedTransactionRepository.getDomain();
        }
    }

    @Override
    public String getRootDomain() {
        if (!sentinelController.degrade()){
            return workTransactionRepository.getRootDomain();
        }else{
            return degradedTransactionRepository.getRootDomain();
        }
    }
}
