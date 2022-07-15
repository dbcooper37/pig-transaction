package com.pig.serializer.kryo;

import com.pig.serializer.TransactionSerializer;
import com.pig.serializer.kryo.KryoPoolSerializer;
import com.pig.transaction.Transaction;

public class KryoTransactionSerializer extends KryoPoolSerializer<Transaction> implements TransactionSerializer {
}
