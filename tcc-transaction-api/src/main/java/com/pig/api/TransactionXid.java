package com.pig.api;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class TransactionXid implements Xid, Serializable {
    private static final long serialVersionUID = -6817267250789142043L;
    private static final byte[] CUSTOMIZED_TRANSACTION_ID = "UniqueIdentity".getBytes();
    private byte[] globalTransactionId;
    private byte[] branchQualifier;

    public TransactionXid() {
        globalTransactionId = uuidToByteArray(UUID.randomUUID());
        branchQualifier = uuidToByteArray(UUID.randomUUID());
    }

    public TransactionXid(Object uniqueIdentity) {
        if (uniqueIdentity == null) {
            globalTransactionId = uuidToByteArray(UUID.randomUUID());
            branchQualifier = uuidToByteArray(UUID.randomUUID());
        } else {
            globalTransactionId = CUSTOMIZED_TRANSACTION_ID;
            branchQualifier = uniqueIdentity.toString().getBytes();
        }
    }

    public TransactionXid(byte[] globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = uuidToByteArray(UUID.randomUUID());
    }

    public TransactionXid(byte[] globalTransactionId, byte[] branchQualifier) {
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }

    private static byte[] uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID byteArrayToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    @Override
    public int getFormatId() {
        int formatId = 1;
        return formatId;
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return globalTransactionId;
    }

    @Override
    public byte[] getBranchQualifier() {
        return branchQualifier;
    }

    public void setGlobalTransactionId(byte[] globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
    }


    public void setBranchQualifier(byte[] branchQualifier) {
        this.branchQualifier = branchQualifier;
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        if (Arrays.equals(CUSTOMIZED_TRANSACTION_ID, globalTransactionId)) {

            stringBuilder.append(new String(globalTransactionId));
            stringBuilder.append(":").append(new String(branchQualifier));

        } else {

            stringBuilder.append(UUID.nameUUIDFromBytes(globalTransactionId).toString());
            stringBuilder.append(":").append(UUID.nameUUIDFromBytes(branchQualifier).toString());
        }

        return stringBuilder.toString();
    }

    public TransactionXid(TransactionXid sourceXid){

        if (sourceXid.globalTransactionId != null) {
            this.globalTransactionId = new byte[sourceXid.globalTransactionId.length];
            System.arraycopy(sourceXid.globalTransactionId, 0, this.globalTransactionId, 0, globalTransactionId.length);
        }

        if (sourceXid.branchQualifier != null) {
            this.branchQualifier = new byte[sourceXid.branchQualifier.length];
            System.arraycopy(branchQualifier, 0, this.branchQualifier, 0, branchQualifier.length);
        }

    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getFormatId();
        result = prime * result + Arrays.hashCode(branchQualifier);
        result = prime * result + Arrays.hashCode(globalTransactionId);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        TransactionXid other = (TransactionXid) obj;
        if (this.getFormatId() != other.getFormatId()) {
            return false;
        } else if (!Arrays.equals(branchQualifier, other.branchQualifier)) {
            return false;
        } else return Arrays.equals(globalTransactionId, other.globalTransactionId);
    }
}
