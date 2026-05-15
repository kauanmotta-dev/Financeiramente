package com.financeiramente.core.db;

/**
 * Abstração de uma linha de resultado de query.
 * Implementada por CursorResultRow (Android) e ResultSetRow (Desktop/JDBC).
 */
public interface ResultRow {
    String getString(String column);
    int getInt(String column);
    long getLong(String column);
    double getDouble(String column);
    boolean isNull(String column);
}
