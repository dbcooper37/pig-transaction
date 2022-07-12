package com.pig.repository;

import com.google.common.collect.Lists;
import com.pig.Transaction;
import com.pig.repository.helper.ShardHolder;
import com.pig.repository.helper.ShardOffset;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.Xid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class RockDbTransactionRepository extends AbstractKVStoreTransactionRepository<RocksDB> implements LocalStoreable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RockDbTransactionRepository.class.getSimpleName());

    static {
        RocksDB.loadLibrary();
    }

    private Options options;
    private RocksDB db;
    private Options rootOptions;
    private RocksDB rootDb;
    private volatile boolean initialized = false;
    private String location = "/var/log/";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public RockDbTransactionRepository() {
    }

    public void init() throws RocksDBException {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    if (options == null) {
                        options = new Options()
                                .setCreateIfMissing(true)
                                .setKeepLogFileNum(1l);
                        String filePath = getPath(this.location, this.getDomain());
                        db = RocksDB.open(options, filePath);
                        if (this.getRootDomain() != null) {
                            rootOptions = new Options()
                                    .setCreateIfMissing(true)
                                    .setKeepLogFileNum(1l);
                            String rootFilePath = getPath(this.location, this.getRootDomain());
                            rootDb = RocksDB.open(rootOptions, rootFilePath);
                        }
                        initialized = true;
                    }
                }
            }
        }
    }

    @Override
    public String getRootDomain() {
        return super.getRootDomain();
    }

    @Override
    List<Transaction> findTransactionFormOneShard(RocksDB shard, Set keys) {
        List<Transaction> list = null;
        List<byte[]> allValues = null;
        try {
            allValues = shard.multiGetAsList(Lists.newLinkedList(keys));
        }catch (RocksDBException ex){
            LOGGER.error("get transaction data from RockDB failed");
        }
        list = new ArrayList<>();
        for (byte[] value: allValues){
            if (value!=null){
                list.add(getSerializer().deserialize(value));
            }
        }
        return list;
    }

    @Override
    Page<byte[]> findKeysFromOneShard(RocksDB shard, String currentCursor, int maxFindCount) {
        Page<byte[]> page = new Page<>();
        try(final RocksIterator iterator = shard.newIterator()){
            if (ShardOffset.SCAN_INIT_CURSOR.equals(currentCursor)){
                iterator.seekToFirst();
            }else{
                iterator.seek(currentCursor.getBytes());
            }
            int count = 0;
            while(iterator.isValid()&&count<maxFindCount){
                page.getData().add(iterator.key());
                count++;
                iterator.next();
            }
            String nextCursor = ShardOffset.SCAN_INIT_CURSOR;
            if (iterator.isValid() && count==maxFindCount){
                nextCursor = new String(iterator.key());
            }
            page.setAttachment(nextCursor);
        }
        return page;
    }

    @Override
    protected ShardHolder<RocksDB> getShardHolder() {
        return new ShardHolder<>() {
            @Override
            public List<RocksDB> getAllShards() {
                return Lists.newArrayList(db);
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    @Override
    protected int doCreate(Transaction transaction) {
        try {
            db.put(transaction.getXid().toString().getBytes(), getSerializer().serialize(transaction));
            return 1;
        } catch (RocksDBException ex) {
            throw new TransactionIOException(ex);
        }
    }

    @Override
    protected int doUpdate(Transaction transaction) {
        try {
            Transaction foundTransaction = doFindOne(transaction.getXid());
            if (foundTransaction.getVersion() != transaction.getVersion()) {
                return 0;
            }
            transaction.setVersion(transaction.getVersion() + 1);
            transaction.setLastUpdateTime(new Date());
            db.put(transaction.getXid().toString().getBytes(), getSerializer().serialize(transaction));
            return 1;
        } catch (RocksDBException ex) {
            throw new TransactionIOException(ex);
        }
    }

    @Override
    protected int doDelete(Transaction transaction) {
        try {
            db.delete(transaction.getXid().toString().getBytes());

        } catch (RocksDBException e) {
            throw new TransactionIOException(e);
        }
        return 1;
    }

    @Override
    protected Transaction doFindOne(Xid xid) {
        return doFind(db, xid);
    }

    @Override
    protected Transaction doFindRootOne(Xid xid) {
        return doFind(rootDb, xid);
    }

    public void close() {
        if (db != null) {
            db.close();
        }
        if (rootDb != null) {
            rootDb.close();
        }
        if (options != null) {
            options.close();
        }

        if (rootOptions != null) {
            rootOptions.close();
        }
    }

    private String getPath(String location, String domain) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(location)) {
            stringBuilder.append(location);
            if (!location.endsWith("/")) {
                stringBuilder.append("/");
            }
        }
        stringBuilder.append(domain);
        return stringBuilder.toString();
    }

    private Transaction doFind(RocksDB db, Xid xid) {
        try {
            byte[] values = db.get(xid.toString().getBytes());
            if (ArrayUtils.isNotEmpty(values)) {
                return getSerializer().deserialize(values);
            }
        } catch (RocksDBException e) {
            throw new TransactionIOException(e);
        }
        return null;
    }
}
