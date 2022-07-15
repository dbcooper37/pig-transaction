package com.pig.storage.helper;


import redis.clients.jedis.ShardedJedis;

public interface ShardedJedisCallback<T> {
    public T doInJedis(ShardedJedis jedis);
}
