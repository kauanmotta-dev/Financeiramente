package com.financeiramente.core.db;




@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultRow row);
}
