package com.financeiramente.core.db;

import java.util.List;
import java.util.Optional;

public interface DatabaseDriver extends TransactionManager {
    void execute(String sql);
    void execute(String sql, Object... args);
    <T> List<T> query(String sql, RowMapper<T> mapper, Object... args);
    <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... args);
    void beginTransaction();
    void commitTransaction();
    void rollbackTransaction();
    int getSchemaVersion();
    void setSchemaVersion(int version);

    @Override
    default void executeInTransaction(Runnable action) {
        beginTransaction();
        try {
            action.run();
            commitTransaction();
        } catch (RuntimeException | Error e) {
            rollbackTransaction();
            throw e;
        }
    }
}
