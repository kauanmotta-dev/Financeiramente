package com.financeiramente.core.db;

public final class Pagina {

    private static final String DEFAULT_LIMIT_PROPERTY = "financeiramente.pagination.default-limit";
    private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private final int limit;
    private final int offset;

    private Pagina(int limit, int offset) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit deve ser maior que zero");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset nao pode ser negativo");
        }
        this.limit = limit;
        this.offset = offset;
    }

    public static Pagina of(int limit, int offset) {
        return new Pagina(limit, offset);
    }

    public static Pagina padrao() {
        return new Pagina(obterLimitePadrao(), 0);
    }

    private static int obterLimitePadrao() {
        String configured = System.getProperty(DEFAULT_LIMIT_PROPERTY);
        if (configured == null || configured.isBlank()) {
            return DEFAULT_LIMIT;
        }

        try {
            int parsed = Integer.parseInt(configured.trim());
            return parsed > 0 ? parsed : DEFAULT_LIMIT;
        } catch (NumberFormatException ignored) {
            return DEFAULT_LIMIT;
        }
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
}