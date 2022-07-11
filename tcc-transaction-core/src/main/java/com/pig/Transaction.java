package com.pig;

import com.pig.api.ParticipantStatus;
import com.pig.api.TransactionContext;
import com.pig.api.TransactionStatus;
import com.pig.api.TransactionXid;
import com.pig.common.TransactionType;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Transaction implements Serializable {
    private static final long serialVersionUID = -6593867390722870403L;
    private final Date createTime = new Date();
    private final List<Participant> participants = new ArrayList<>();
    private final Map<String, Object> attachments = new ConcurrentHashMap<>();
    private final TransactionXid xid;
    private TransactionStatus status;
    private final TransactionType transactionType;
    private volatile int retriedCount = 0;
    private Date lastUpdateTime = new Date();
    private long version = 0;
    protected TransactionXid rootXid;

    public Transaction(TransactionContext transactionContext) {
        this.xid = transactionContext.getXid();
        this.rootXid = transactionContext.getRootXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = TransactionType.BRANCH;
    }

    public Transaction(TransactionType transactionType) {
        this(null, transactionType);
    }

    public Transaction(Object uniqueIdentity, TransactionType transactionType) {
        this.xid = new TransactionXid(uniqueIdentity);
        this.status = TransactionStatus.TRYING;
        this.transactionType = transactionType;
        if (transactionType.equals(TransactionType.ROOT)) {
            this.rootXid = xid;
        }
    }

    public void enlistParticipant(Participant participant) {
        participants.add(participant);
    }


    public Xid getXid() {
        return new TransactionXid(xid);
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void changeStatus(TransactionStatus status) {
        this.status = status;
    }

    public void commit() {
        for (Participant participant : participants) {
            if (!participant.getStatus().equals(ParticipantStatus.CANCEL_SUCCESS)) {
                participant.commit();
                participant.setStatus(ParticipantStatus.CONFIRM_SUCCESS);
            }
        }
    }

    public void rollback() {
        for (Participant participant : participants) {
            if (!participant.getStatus().equals(ParticipantStatus.CANCEL_SUCCESS)) {
                participant.rollback();
                participant.setStatus(ParticipantStatus.CANCEL_SUCCESS);
            }
        }
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void setRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public synchronized void addRetriedCount() {
        this.retriedCount++;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void updateVersion() {
        this.version++;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date date) {
        this.lastUpdateTime = date;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void updateTime() {
        this.lastUpdateTime = new Date();
    }

    public boolean isTryFailed() {
        for (Participant participant : participants) {
            if (participant.getStatus().equals(ParticipantStatus.TRY_FAILED)) {
                return true;
            }
        }
        return false;
    }

    public TransactionXid getRootXid() {
        return rootXid;
    }

    public void setRootXid(TransactionXid rootXid) {
        this.rootXid = rootXid;
    }
}
