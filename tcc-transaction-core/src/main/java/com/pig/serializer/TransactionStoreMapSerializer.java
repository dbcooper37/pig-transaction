package com.pig.serializer;

import com.pig.exception.SystemException;
import com.pig.storage.TransactionStore;
import com.pig.utils.ByteUtils;
import com.pig.xid.TransactionXid;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class TransactionStoreMapSerializer {
    public static final String XID = "XID";
    public static final String ROOT_XID = "ROOT_XID";
    public static final String ROOT_DOMAIN = "ROOT_DOMAIN";
    public static final String STATUS = "STATUS";
    public static final String TRANSACTION_TYPE = "TRANSACTION_TYPE";
    public static final String RETRIED_COUNT = "RETRIED_COUNT";
    public static final String CREATE_TIME = "CREATE_TIME";
    public static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";
    public static final String VERSION = "VERSION";
    public static final String CONTENT = "CONTENT";
    public static final String DOMAIN = "DOMAIN";

    public static Map<byte[], byte[]> serialize(TransactionStore transactionStore) {

        Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();

        map.put(DOMAIN.getBytes(), transactionStore.getDomain().getBytes());
        map.put(XID.getBytes(), transactionStore.getXid().toString().getBytes());
        if (transactionStore.getRootXid() != null) {
            map.put(ROOT_XID.getBytes(), transactionStore.getRootXid().toString().getBytes());
        }
        map.put(ROOT_DOMAIN.getBytes(), transactionStore.getRootDomain().getBytes());
        map.put(STATUS.getBytes(), ByteUtils.intToBytes(transactionStore.getStatusId()));
        map.put(TRANSACTION_TYPE.getBytes(), ByteUtils.intToBytes(transactionStore.getTransactionTypeId()));
        map.put(RETRIED_COUNT.getBytes(), ByteUtils.intToBytes(transactionStore.getRetriedCount()));
        map.put(CREATE_TIME.getBytes(), DateFormatUtils.format(transactionStore.getCreateTime(), "yyyy-MM-dd HH:mm:ss").getBytes());
        map.put(LAST_UPDATE_TIME.getBytes(), DateFormatUtils.format(transactionStore.getLastUpdateTime(), "yyyy-MM-dd HH:mm:ss").getBytes());
        map.put(VERSION.getBytes(), ByteUtils.longToBytes(transactionStore.getVersion()));
        map.put(CONTENT.getBytes(), transactionStore.getContent());
        return map;
    }

    public static TransactionStore deserialize(Map<byte[], byte[]> map) {

        Map<String, byte[]> propertyMap = new HashMap<String, byte[]>();

        for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
            propertyMap.put(new String(entry.getKey()), entry.getValue());
        }

        TransactionStore transactionStore = new TransactionStore();
        transactionStore.setDomain(new String(propertyMap.get(DOMAIN)));

        transactionStore.setXid(new TransactionXid(new String(propertyMap.get(XID))));

        if (propertyMap.containsKey(ROOT_XID)) {
            transactionStore.setRootXid(new TransactionXid(new String(propertyMap.get(ROOT_XID))));
        }

        if (propertyMap.containsKey(ROOT_DOMAIN)) {
            transactionStore.setRootDomain(new String(propertyMap.get(ROOT_DOMAIN)));
        }

        transactionStore.setStatusId(ByteUtils.bytesToInt(propertyMap.get(STATUS)));
        transactionStore.setTransactionTypeId(ByteUtils.bytesToInt(propertyMap.get(TRANSACTION_TYPE)));
        transactionStore.setRetriedCount(ByteUtils.bytesToInt(propertyMap.get(RETRIED_COUNT)));

        try {
            transactionStore.setCreateTime(DateUtils.parseDate(new String(propertyMap.get(CREATE_TIME)), "yyyy-MM-dd HH:mm:ss"));
            transactionStore.setLastUpdateTime(DateUtils.parseDate(new String(propertyMap.get(LAST_UPDATE_TIME)), "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            throw new SystemException(e);
        }

        transactionStore.setVersion(ByteUtils.bytesToLong(propertyMap.get(VERSION)));
        transactionStore.setContent(propertyMap.get(CONTENT));

        return transactionStore;
    }
}
