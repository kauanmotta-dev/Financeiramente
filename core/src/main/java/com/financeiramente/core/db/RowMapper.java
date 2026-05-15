package com.financeiramente.core.db;

/**
 * Mapeia uma linha do resultado de uma query para um objeto de domínio.
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultRow row);
}
