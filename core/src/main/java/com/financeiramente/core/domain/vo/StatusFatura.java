package com.financeiramente.core.domain.vo;

public enum StatusFatura {
    ABERTO, FECHADO, PAGO, PAGO_PARCIAL;

    public static StatusFatura fromString(String value) {
        if (value == null || value.isEmpty()) return ABERTO;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ABERTO;
        }
    }
}
