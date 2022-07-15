package com.pig.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.pig.serializer.kryo.KryoPoolSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegisterableKryoSerializer<T> extends KryoPoolSerializer<T> {
    List<Class> registerClasses = null;
    boolean warnUnregisteredClasses = false;
    public RegisterableKryoSerializer(){}

    public RegisterableKryoSerializer(int initPoolSize){
        super(initPoolSize);
    }

    public RegisterableKryoSerializer(List<Class> registerClasses){
        this.registerClasses = registerClasses;
        this.init();
    }

    public RegisterableKryoSerializer(int initPoolSize, List<Class> registerClasses){
        this(initPoolSize,registerClasses,false);
    }

    public RegisterableKryoSerializer(int initPooSize, List<Class> registerClasses,boolean warnUnregisteredClasses){
        this.initPoolSize = initPooSize;
        this.registerClasses = registerClasses;
        this.warnUnregisteredClasses = warnUnregisteredClasses;
        this.init();
    }

    protected void initHook(Kryo kryo){
        kryo.setWarnUnregisteredClasses(this.warnUnregisteredClasses);
        SerializerFactory.CompatibleFieldSerializerFactory factory = new SerializerFactory.CompatibleFieldSerializerFactory();
        factory.getConfig().setReadUnknownFieldData(true);
        factory.getConfig().setChunkedEncoding(true);
        kryo.setDefaultSerializer(factory);
        registerClasses(kryo,this.registerClasses);
    }

    private void registerClasses(Kryo kryo, List<Class> registerClasses){
        List<Class> allClasses = registerJdkClasses();
        Set<Class> classesSet = new HashSet<>();
        List<Class> externalClasses = registerClasses;

        if (externalClasses != null){
            for (Class clazz: externalClasses){
                if (clazz != null && !classesSet.contains(clazz)){
                    classesSet.add(clazz);
                }
            }
        }
        for (Class clazz: allClasses){
            kryo.register(clazz);
        }
    }

    private List<Class> registerJdkClasses(){
        List<Class> jdkClasses = new ArrayList<>();
        jdkClasses.add(Class.class);
        jdkClasses.add(Class[].class);

        return jdkClasses;
    }
}


