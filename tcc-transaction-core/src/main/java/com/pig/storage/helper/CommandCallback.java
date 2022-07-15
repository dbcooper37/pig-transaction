package com.pig.storage.helper;

public interface CommandCallback<T> {
    T execute(RedisCommands commands);
}
