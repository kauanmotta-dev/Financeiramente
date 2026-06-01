package com.financeiramente.android.ui.common;

import com.financeiramente.core.domain.entity.Categoria;

public final class CategoriaVisualFallback {

    public static final String DEFAULT_ICONE = "\uD83D\uDCE6";
    public static final String DEFAULT_COR = "#6366F1";

    private CategoriaVisualFallback() {
    }

    public static String icone(Categoria categoria, String nomePai) {
        if (categoria == null) {
            return DEFAULT_ICONE;
        }

        String atual = categoria.getIcone();
        return isDefaultIcon(atual) ? DEFAULT_ICONE : atual;
    }

    public static String cor(Categoria categoria, String nomePai) {
        if (categoria == null) {
            return DEFAULT_COR;
        }

        String atual = categoria.getCor();
        return isDefaultColor(atual) ? DEFAULT_COR : atual;
    }

    private static boolean isDefaultIcon(String icone) {
        return icone == null || icone.trim().isEmpty() || DEFAULT_ICONE.equals(icone.trim());
    }

    private static boolean isDefaultColor(String cor) {
        return cor == null || cor.trim().isEmpty() || DEFAULT_COR.equalsIgnoreCase(cor.trim());
    }
}