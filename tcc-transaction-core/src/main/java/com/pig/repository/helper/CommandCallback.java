package com.pig.repository.helper;

public interface CommandCallback<T> {
    T execute(RedisCommands commands);
}
