package com.pig.repository;

import com.pig.Transaction;
import com.pig.repository.helper.ShardHolder;
import com.pig.repository.helper.ShardOffset;
import com.pig.serializer.RegisterableKryoTransactionSerializer;
import com.pig.serializer.TransactionSerializer;
import com.pig.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractKVStoreTransactionRepository<T> extends AbstractTransactionRepository {

    static final Logger LOGGER = LoggerFactory.getLogger(AbstractKVStoreTransactionRepository.class.getSimpleName());
    private String domain;
    private String rootDomain;
    private TransactionSerializer serializer = new RegisterableKryoTransactionSerializer();

    @Override
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getRootDomain() {
        return rootDomain;
    }

    public void setRootDomain(String rootDomain) {
        this.rootDomain = rootDomain;
    }

    public void setSerializer(TransactionSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected Page<Transaction> doFindAllUnmodifiedSince(Date date, String offset, int pageSize) {
        List<Transaction> fetchedTransactions = new ArrayList<>();
        String tryFetchOffset = offset;
        int haveFetchedCount = 0;
        do {
            Page<Transaction> page = doFindAll(tryFetchOffset, pageSize - haveFetchedCount);
            tryFetchOffset = page.getNextOffset();
            fetchedTransactions.addAll(page.getData());
            haveFetchedCount += page.getData().size();
            if (page.getData().size() <= 0 || haveFetchedCount >= pageSize) {
                break;
            }
        } while (true);
        return new Page<>(tryFetchOffset, fetchedTransactions);
    }

    protected Page<Transaction> doFindAll(String offset, int maxFindCount) {
        ShardOffset currentShardOffset = new ShardOffset(offset);
        ShardOffset nextShardOffset = new ShardOffset();
        Page<Transaction> page = new Page<>();
        try (ShardHolder<T> shardHolder = getShardHolder()) {
            List<T> allShards = shardHolder.getAllShards();
            List<Transaction> transactions = findAllTransactionFromShards(allShards, currentShardOffset, nextShardOffset, maxFindCount);
            page.setNextOffset(nextShardOffset.toString());
            page.setData(transactions);
            return page;
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    private List<Transaction> findAllTransactionFromShards(final List<T> allShards, ShardOffset currentShardOffset, ShardOffset nextShardOffset, int maxFindCount) {
        List<Transaction> transactions = new ArrayList<>();
        Set<byte[]> allKeySet = new HashSet<>();
        int currentShardIndex = currentShardOffset.getShardIndex();
        String currentCursor = currentShardOffset.getCursor();
        String nextCursor = null;
        while (currentShardIndex < allShards.size()) {
            T currentShard = allShards.get(currentShardIndex);
            Page<byte[]> keyPage = findKeysFromOneShard(currentShard, currentCursor, maxFindCount);
            List<byte[]> keys = keyPage.getData();
            if (!keys.isEmpty()) {
                List<Transaction> currentTransactions = findTransactionFormOneShard(currentShard, new HashSet<>());
                if (CollectionUtils.isEmpty(currentTransactions)) {
                    LOGGER.info("No transaction not found while key size: {}", keys.size());
                }
                transactions.addAll(currentTransactions);
            }
            nextCursor = (String) keyPage.getAttachment();
            allKeySet.addAll(keyPage.getData());
            if (CollectionUtils.isEmpty(allKeySet)) {
                if (ShardOffset.SCAN_INIT_CURSOR.equals(nextCursor)) {
                    currentShardIndex += 1;
                    currentCursor = ShardOffset.SCAN_INIT_CURSOR;

                    currentShardOffset.setShardIndex(currentShardIndex);
                    currentShardOffset.setCursor(currentCursor);
                } else {
                    currentCursor = nextCursor;
                }
            } else {
                break;
            }
        }
        if (CollectionUtils.isEmpty(allKeySet)) {
            nextShardOffset.setShardIndex(currentShardIndex);
            nextShardOffset.setCursor(currentCursor);
        } else {
            if (ShardOffset.SCAN_INIT_CURSOR.equals(nextCursor)) {
                nextShardOffset.setShardIndex(currentShardIndex + 1);
                nextShardOffset.setCursor(ShardOffset.SCAN_INIT_CURSOR);
            } else {
                nextShardOffset.setShardIndex(currentShardIndex);
                nextShardOffset.setCursor(nextCursor);
            }
        }
        return transactions;
    }

    abstract List<Transaction> findTransactionFormOneShard(T shard, Set key);

    abstract Page<byte[]> findKeysFromOneShard(T shard, String currentCursor, int maxFindCount);

    protected abstract ShardHolder<T> getShardHolder();

    public TransactionSerializer getSerializer() {
        return serializer;
    }
}
