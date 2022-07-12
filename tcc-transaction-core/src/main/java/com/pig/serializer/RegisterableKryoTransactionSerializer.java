package com.pig.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.collect.Lists;
import com.pig.InvocationContext;
import com.pig.Participant;
import com.pig.Transaction;
import com.pig.api.TransactionStatus;
import com.pig.api.TransactionXid;
import com.pig.common.TransactionType;
import com.pig.utils.CollectionUtils;

import java.util.List;

public class RegisterableKryoTransactionSerializer extends RegisterableKryoSerializer<Transaction> implements TransactionSerializer {

    static List<Class> transactionClasses = Lists.newArrayList(
            Transaction.class,
            InvocationContext.class,
            TransactionXid.class,
            TransactionStatus.class,
            Participant.class,
            TransactionType.class
    );

    public RegisterableKryoTransactionSerializer() {
        this(transactionClasses);
    }

    public RegisterableKryoTransactionSerializer(int initPoolSize) {
        this(initPoolSize, Lists.newArrayList(
                Transaction.class, InvocationContext.class, TransactionXid.class, TransactionStatus.class,
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
