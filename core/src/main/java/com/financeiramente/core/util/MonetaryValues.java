package com.financeiramente.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MonetaryValues {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    private MonetaryValues() {}

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        return value.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal fromDouble(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, ROUNDING);
    }

    public static double toDouble(BigDecimal value) {
        return normalize(value).doubleValue();
    }
}
