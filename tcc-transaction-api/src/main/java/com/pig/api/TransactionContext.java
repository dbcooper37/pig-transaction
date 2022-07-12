package com.pig.api;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionContext implements Serializable {
    private static final long serialVersionUID = -8199390103169700387L;
    private TransactionXid xid;
    private int status = TransactionStatus.TRYING.getId();
    private int participantStatus = ParticipantStatus.TRYING.getId();


    private final Map<String, String> attachments = new ConcurrentHashMap<>();
    private TransactionXid rootXid;

    public TransactionContext() {
    }

    public TransactionContext(TransactionXid rootXid, TransactionXid xid, int status) {
        this(rootXid, xid, status, ParticipantStatus.TRYING.getId());
    }

    public TransactionContext(TransactionXid rootXid, TransactionXid xid, int status, int participantStatus) {
        this.rootXid = rootXid;
        this.xid = xid;
        this.status = status;
        this.participantStatus = participantStatus;
    }

    public TransactionXid getXid() {
        return new TransactionXid(xid);
    }

    public void setXid(TransactionXid xid) {
        this.xid = xid;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        if (attachments != null && !attachments.isEmpty()) {
            this.attachments.putAll(attachments);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getParticipantStatus() {
        return participantStatus;
    }

    public void setParticipantStatus(int participantStatus) {
        this.participantStatus = participantStatus;
    }

    public TransactionXid getRootXid() {
        return rootXid;
    }

    public void setRootXid(TransactionXid rootXid) {
        this.rootXid = rootXid;
    }
}
