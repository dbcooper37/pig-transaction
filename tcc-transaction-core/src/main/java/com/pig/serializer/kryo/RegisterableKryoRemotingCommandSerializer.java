package com.pig.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.collect.Lists;
import com.pig.remoting.protocol.RemotingCommand;
import com.pig.remoting.protocol.RemotingCommandCode;
import com.pig.serializer.RemotingCommandSerializer;
import com.pig.utils.CollectionUtils;

import java.util.List;

public class RegisterableKryoRemotingCommandSerializer extends RegisterableKryoSerializer<RemotingCommand> implements RemotingCommandSerializer {
    static List<Class> remotingCommandClasses = Lists.newArrayList(
            RemotingCommand.class,
            RemotingCommandCode.class);

    public RegisterableKryoRemotingCommandSerializer() {
        this(remotingCommandClasses);
    }

    public RegisterableKryoRemotingCommandSerializer(int initPoolSize) {
        this(initPoolSize, remotingCommandClasses);
    }

    public RegisterableKryoRemotingCommandSerializer(List<Class> registerClasses) {
        super(CollectionUtils.merge(remotingCommandClasses, registerClasses));
    }

    public RegisterableKryoRemotingCommandSerializer(int initPoolSize, List<Class> registerClasses) {
        super(initPoolSize, CollectionUtils.merge(remotingCommandClasses, registerClasses));
    }

    public RegisterableKryoRemotingCommandSerializer(int initPoolSize, List<Class> registerClasses, boolean warnUnregisteredClasses) {
        super(initPoolSize, CollectionUtils.merge(remotingCommandClasses, registerClasses), warnUnregisteredClasses);
    }

    protected void initHook(Kryo kryo) {
        super.initHook(kryo);
    }
}
