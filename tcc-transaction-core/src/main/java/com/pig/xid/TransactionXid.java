package com.pig.xid;

import com.pig.api.Xid;
import com.pig.support.FactoryBuilder;

import java.io.Serializable;

public class TransactionXid implements Xid, Serializable {
    private String xid;

    public TransactionXid() {

    }

    public TransactionXid(String xidString) {
        this.xid = xidString;
    }

    public static TransactionXid withUniqueIdentity(Object uniqueIdentity) {
        String xid = null;
        if (uniqueIdentity == null) {
            xid = FactoryBuilder.factoryOf(UUIDGenerator.class).getInstance().generate();
        } else {
            xid = uniqueIdentity.toString();
        }
        return new TransactionXid(xid);
    }

    public static TransactionXid withUuid() {
        return new TransactionXid(FactoryBuilder.factoryOf(UUIDGenerator.class).getInstance().generate());
    }

    @Override
    public String toString() {
        return this.xid;
    }

    public int hashCode() {
        if (this.xid == null) {
            return 0;
        }
        return this.xid.hashCode();
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
        return this.xid.equals(other.xid);
    }

    @Override
    public String getXid() {
        return xid;
    }

}
