package com.pig.common;

public enum TransactionType {
    ROOT(1),
    BRANCH(2);

    final int id;

    TransactionType(int id) {
        this.id = id;
    }

    public static TransactionType valueOf(int id) {
        switch (id) {
            case 1:
                return ROOT;
            case 2:
                return BRANCH;
            default:
                return null;
        }
    }

    public int getId() {
        return id;
    }
}
