package com.financeiramente.core.domain.vo;

public enum BandeiraCartao {
    VISA, MASTERCARD, ELO, AMEX, HIPERCARD, OUTRO;

    public static BandeiraCartao fromString(String value) {
        if (value == null || value.isEmpty()) return OUTRO;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OUTRO;
        }
    }
}
