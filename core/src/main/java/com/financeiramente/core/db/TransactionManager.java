package com.financeiramente.core.db;

public interface TransactionManager {
    void executeInTransaction(Runnable action);
}