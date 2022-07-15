package com.pig.storage;

import com.pig.exception.SystemException;
import com.pig.serializer.TccDomainStoreSerializer;
import com.pig.serializer.TransactionStoreSerializer;
import com.pig.storage.domain.DomainStore;
import com.pig.storage.helper.RedisHelper;
import com.pig.storage.helper.ShardHolder;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pig.api.Xid;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class RocksDbTransactionStorage extends AbstractKVTransactionStorage<RocksDB>{
    static final Logger LOGGER = LoggerFactory.getLogger(RocksDbTransactionStorage.class.getSimpleName());

    static {
        RocksDB.loadLibrary();
    }

    private Options options;

    private RocksDB db;

    private String location = "/var/log/";

    private volatile boolean initialized = false;

    private TccDomainStoreSerializer domainStoreSerializer = new TccDomainStoreSerializer();

    public RocksDbTransactionStorage(TransactionStoreSerializer serializer, StoreConfig storeConfig) {
        super(serializer, storeConfig);
        this.location = storeConfig.getLocation();
        init();
    }

    @Override
    protected int doCreate(TransactionStore transactionStore) {
        try {
            byte[] key = RedisHelper.getRedisKey(transactionStore.getDomain(), transactionStore.getXid());
            db.put(key, getSerializer().serialize(transactionStore));
            return 1;
        } catch (RocksDBException e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doUpdate(TransactionStore transactionStore) {
        try{
            TransactionStore foundTransaction = doFindOne(transactionStore.getDomain(), transactionStore.getXid(),false);
            if (foundTransaction.getVersion()!= transactionStore.getVersion()){
                return  0;
            }
            Date lastUpdateTime = new Date();
            long currentVersion = foundTransaction.getVersion();
            try{
                byte[] key = RedisHelper.getRedisKey(transactionStore.getDomain(),transactionStore.getXid());
                db.put(key,getSerializer().serialize(transactionStore));
            }finally {
                transactionStore.setLastUpdateTime(lastUpdateTime);
                transactionStore.setVersion(currentVersion);
            }

            return 1;
        }catch (RocksDBException ex){
            throw new TransactionIOException(ex);
        }
    }

    @Override
    protected int doDelete(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doMarkDeleted(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected int doRestore(TransactionStore transactionStore) {
        return 0;
    }

    @Override
    protected TransactionStore doFindOne(String domain, Xid xid, boolean isMarkDeleted) {
        return null;
    }

    @Override
    protected List<TransactionStore> findTransactionsFromOneShard(String domain, RocksDB shard, Set keys) {
        return null;
    }

    @Override
    Page findKeysFromOneShard(String domain, RocksDB shard, String currentCursor, int maxFindCount, boolean isMarkDeleted) {
        return null;
    }

    @Override
    int count(String domain, RocksDB shard, boolean isMarkDeleted) {
        return 0;
    }

    @Override
    protected ShardHolder<RocksDB> getShardHolder() {
        return null;
    }

    public String getLocation(){
        return this.location;
    }

    public void init() {
        if (!initialized){
            synchronized (this){
                if (!initialized){
                    if (options==null){
                        options = new Options().setCreateIfMissing(true).setKeepLogFileNum(1L);
                        String filePath = this.getLocation();
                        try{
                            db = RocksDB.open(options,filePath);
                        }catch (RocksDBException e){
                            throw new SystemException("open rockdb failed",e);
                        }
                        initialized=true;
                    }
                }
            }
        }
    }

    @Override
    public void registerDomain(DomainStore domainStore) {

    }

    @Override
    public void updateDomain(DomainStore domainStore) {

    }

    @Override
    public void removeDomain(String domain) {

    }

    @Override
    public DomainStore findDomain(String domain) {
        return null;
    }

    @Override
    public List<DomainStore> getAllDomains() {
        return null;
    }

    @Override
    public TransactionStore findByXid(String domain, com.pig.api.Xid xid) {
        return null;
    }

    @Override
    public TransactionStore findMarkDeletedByXid(String domain, com.pig.api.Xid xid) {
        return null;
    }
}
