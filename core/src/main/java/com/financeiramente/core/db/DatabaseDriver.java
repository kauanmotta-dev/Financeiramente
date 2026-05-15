package com.financeiramente.core.db;

import java.util.List;
import java.util.Optional;

/**
 * Abstração de acesso a banco de dados SQLite.
 * Implementada por AndroidDatabaseDriver (SQLite nativo) e JdbcDatabaseDriver (JDBC).
 */
public interface DatabaseDriver {
    void execute(String sql);
    void execute(String sql, Object... args);
    <T> List<T> query(String sql, RowMapper<T> mapper, Object... args);
    <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... args);
    void beginTransaction();
    void commitTransaction();
    void rollbackTransaction();
    int getSchemaVersion();
    void setSchemaVersion(int version);
}
