package com.pig.serializer;

public interface ObjectSerializer<T> {
    byte[] serialize(T t);

    T deserialize(byte[] bytes);

    T clone(T object);
}
