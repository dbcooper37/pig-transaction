package com.pig.storage;

import com.pig.serializer.SerializerType;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedisPool;

import javax.sql.DataSource;

public interface StoreConfig {

    StorageType getStorageType();

    StorageMode getStorageMode();

    String getDomain();

    String getTransactionStorageClass();

    long getRequestTimeoutMillis();

    String getLocation();

    JedisPool getJedisPool();

    ShardedJedisPool getShardedJedisPool();

    JedisCluster getJedisCluster();

    String getRemoteCluster();

    String getTbSuffix();

    DataSource getDataSource();

    SerializerType getSerializerType();

    String getTransactionSerializerClassName();

    int getKryoPoolSize();

    int getMaxTransactionSize();

    int getMaxAttempts();

}

