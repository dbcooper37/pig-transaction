package com.pig.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.collect.Lists;
import com.pig.serializer.TransactionSerializer;
import com.pig.serializer.kryo.RegisterableKryoSerializer;
import com.pig.transaction.InvocationContext;
import com.pig.transaction.Participant;
import com.pig.transaction.Transaction;
import com.pig.api.TransactionStatus;
import com.pig.api.Xid;
import com.pig.common.TransactionType;
import com.pig.utils.CollectionUtils;

import java.util.List;

public class RegisterableKryoTransactionSerializer extends RegisterableKryoSerializer<Transaction> implements TransactionSerializer {

    static List<Class> transactionClasses = Lists.newArrayList(
            Transaction.class,
            InvocationContext.class,
            Xid.class,
            TransactionStatus.class,
            Participant.class,
            TransactionType.class
    );

    public RegisterableKryoTransactionSerializer() {
        this(transactionClasses);
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize) {
        this(initPoolSize, Lists.newArrayList(
                Transaction.class, InvocationContext.class, Xid.class, TransactionStatus.class,
                Participant.class, TransactionType.class
        ));
    }

    public RegisterableKryoTransactionSerializer(List<Class> registerClasses) {
        super(CollectionUtils.merge(transactionClasses, registerClasses));
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize, List<Class> registerClasses) {
        super(initPoolSize, CollectionUtils.merge(transactionClasses, registerClasses));
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize, List<Class> registerClasses, boolean warnUnregisteredClasses) {
        super(initPoolSize, CollectionUtils.merge(transactionClasses, registerClasses), warnUnregisteredClasses);
    }

    protected void initHook(Kryo kryo) {
        super.initHook(kryo);
    }
}
