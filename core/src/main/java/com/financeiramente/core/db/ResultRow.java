package com.financeiramente.core.db;





public interface ResultRow {
    String getString(String column);
    int getInt(String column);
    long getLong(String column);
    double getDouble(String column);
    boolean isNull(String column);
}
