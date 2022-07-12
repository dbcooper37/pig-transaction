package com.pig.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class KryoPoolSerializer<T> implements ObjectSerializer<T>{
    public static final int DEFAULT_MAX_POOL_SIZE = 300;
    protected int initPoolSize = DEFAULT_MAX_POOL_SIZE;

    Pool<Kryo> kryoPool = null;
    public KryoPoolSerializer(){
        init();
    }

    public KryoPoolSerializer(int initPoolSize){
        this.initPoolSize = initPoolSize;
    }

    public void init(){
        kryoPool = new Pool<Kryo>(true,true,initPoolSize) {
            @Override
            protected Kryo create() {
                Kryo kryo = new Kryo();
                kryo.setReferences(true);
                kryo.setRegistrationRequired(false);
                ((DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
                initHook(kryo);
                return kryo;
            }
        };
        List<Kryo> preCreatedKryos = new ArrayList<>();
        for (int i=0;i< initPoolSize;i++){
            preCreatedKryos.add(kryoPool.obtain());
        }
        for(Kryo kryo: preCreatedKryos){
            kryoPool.free(kryo);
        }
    }
    @Override
    public byte[] serialize(final T t) {
        Kryo kryo=kryoPool.obtain();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output,t);
            output.flush();
            return byteArrayOutputStream.toByteArray();
        }finally {
            if (kryo!=null){
                kryoPool.free(kryo);
            }
        }
    }

    @Override
    public T deserialize(byte[] bytes) {
        Kryo kryo = kryoPool.obtain();
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream);
            return (T) kryo.readClassAndObject(input);
        }finally {
            if (kryo!=null){
                kryoPool.free(kryo);
            }
        }
    }

    @Override
    public T clone(T object) {
        Kryo kryo = kryoPool.obtain();
        try {
            return kryo.copy(object);
        }finally {
            if (kryo!=null){
                kryoPool.free(kryo);
            }
        }
    }

    protected void initHook(Kryo kryo){}
}
