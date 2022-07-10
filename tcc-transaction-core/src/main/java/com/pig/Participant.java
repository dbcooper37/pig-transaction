package com.pig;

import com.pig.api.*;

import java.io.Serializable;

public class Participant implements Serializable {

    private static final long serialVersionUID = 2541637624547305165L;
    Class<? extends TransactionContextEditor> transactionContextEditorClass;
    private TransactionXid rootXid;
    private TransactionXid transactionXid;
    private InvocationContext confirmInvocationContext;
    private InvocationContext cancelInvocationContext;
    private int status = ParticipantStatus.TRYING.getId();

    public Participant() {
    }

    public Participant(Class<? extends TransactionContextEditor> transactionContextEditorClass,
                       TransactionXid transactionXid, InvocationContext confirmInvocationContext,
                       InvocationContext cancelInvocationContext, int status) {
        this.transactionContextEditorClass = transactionContextEditorClass;
        this.transactionXid = transactionXid;
        this.confirmInvocationContext = confirmInvocationContext;
        this.cancelInvocationContext = cancelInvocationContext;
        this.status = status;
    }

    public void rollback(){
        Terminator.invoke(new TransactionContext(rootXid,transactionXid, TransactionStatus.CANCELLING.getId(),status),cancelInvocationContext,transactionContextEditorClass);
    }

    public void commit(){
        Terminator.invoke(new TransactionContext(rootXid,transactionXid,TransactionStatus.CONFIRMING.getId(),status),confirmInvocationContext,transactionContextEditorClass);
    }

    public InvocationContext getConfirmInvocationContext(){
        return this.confirmInvocationContext;
    }

    public InvocationContext getCancelInvocationContext(){
        return this.cancelInvocationContext;
    }

    public void setStatus(ParticipantStatus status){
        this.status=status.getId();
    }

    public ParticipantStatus getStatus(){
        return ParticipantStatus.valueOf(this.status);
    }

    public TransactionXid getXid(){
        return this.transactionXid;
    }

    public Class<? extends TransactionContextEditor> getTransactionContextEditorClass(){
        return transactionContextEditorClass;
    }
}
