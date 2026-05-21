package com.financeiramente.android.ui.common;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;

import java.util.Locale;

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
        if (!isDefaultIcon(atual)) {
            return atual;
        }

        String nome = normalize(categoria.getNome());
        String pai = normalize(nomePai);

        if ("moradia".equals(nome)) return "🏠";
        if ("transporte".equals(nome)) return "🚗";
        if ("alimentacao".equals(nome)) return "🍽️";
        if ("saude".equals(nome)) return "🩺";
        if ("estudos".equals(nome)) return "📚";
        if ("imprevistos".equals(nome) && categoria.getPaiId() == null) return "🚨";
        if ("lazer".equals(nome)) return "🎬";
        if ("compras luxo".equals(nome)) return "🛍️";
        if ("doacoes".equals(nome)) return "🤝";
        if ("salario".equals(nome)) return "💼";
        if ("renda extra".equals(nome)) return "💰";

        if ("aluguel".equals(nome)) return "🧾";
        if ("condominio".equals(nome)) return "🏢";
        if ("luz".equals(nome)) return "💡";
        if ("agua".equals(nome)) return "💧";
        if ("internet".equals(nome)) return "🌐";
        if ("combustivel".equals(nome)) return "⛽";
        if ("aplicativo".equals(nome)) return "🛵";
        if ("supermercado".equals(nome)) return "🛒";
        if ("restaurante".equals(nome)) return "🍴";
        if ("delivery".equals(nome)) return "🥡";
        if ("plano de saude".equals(nome)) return "🏥";
        if ("farmacia".equals(nome)) return "💊";
        if ("academia".equals(nome)) return "🏋️";
        if ("cursos".equals(nome)) return "🎓";
        if ("materiais".equals(nome)) return "✏️";
        if ("streaming".equals(nome)) return "📺";
        if ("hobby".equals(nome)) return "🎨";
        if ("eventos".equals(nome)) return "🎉";
        if ("roupas".equals(nome)) return "👕";
        if ("eletronicos".equals(nome)) return "📱";
        if ("igreja".equals(nome)) return "⛪";
        if ("salario clt".equals(nome)) return "👔";
        if ("freelance".equals(nome)) return "💻";

        if ("outros".equals(nome)) {
            return categoria.getTipo() == TipoCategoria.RECEITA ? "🧾" : "🧩";
        }
        if ("imprevistos".equals(nome)) {
            return categoria.getTipo() == TipoCategoria.NAO_ESSENCIAL ? "🆘" : "🧯";
        }

        if ("renda extra".equals(pai)) return "🧾";
        if ("compras luxo".equals(pai)) return "🧩";

        return DEFAULT_ICONE;
    }

    public static String cor(Categoria categoria, String nomePai) {
        if (categoria == null) {
            return DEFAULT_COR;
        }

        String atual = categoria.getCor();
        if (!isDefaultColor(atual)) {
            return atual;
        }

        String pai = normalize(nomePai);
        if (!pai.isEmpty()) {
            return corPorFamilia(pai, categoria.getTipo());
        }

        return corPorFamilia(normalize(categoria.getNome()), categoria.getTipo());
    }

    private static boolean isDefaultIcon(String icone) {
        return icone == null || icone.trim().isEmpty() || DEFAULT_ICONE.equals(icone.trim());
    }

    private static boolean isDefaultColor(String cor) {
        return cor == null || cor.trim().isEmpty() || DEFAULT_COR.equalsIgnoreCase(cor.trim());
    }

    private static String corPorFamilia(String chave, TipoCategoria tipo) {
        if ("moradia".equals(chave)) return "#6366F1";
        if ("transporte".equals(chave)) return "#0EA5E9";
        if ("alimentacao".equals(chave)) return "#F97316";
        if ("saude".equals(chave)) return "#10B981";
        if ("estudos".equals(chave)) return "#8B5CF6";
        if ("lazer".equals(chave)) return "#EC4899";
        if ("compras luxo".equals(chave)) return "#EF4444";
        if ("doacoes".equals(chave)) return "#14B8A6";
        if ("salario".equals(chave)) return "#3B82F6";
        if ("renda extra".equals(chave)) return "#22C55E";

        // Subcategorias de Moradia
        if ("aluguel".equals(chave)) return "#6366F1";
        if ("condominio".equals(chave)) return "#6366F1";
        if ("luz".equals(chave)) return "#6366F1";
        if ("agua".equals(chave)) return "#6366F1";
        if ("internet".equals(chave)) return "#6366F1";

        // Subcategorias de Transporte
        if ("combustivel".equals(chave)) return "#0EA5E9";
        if ("aplicativo".equals(chave)) return "#0EA5E9";

        // Subcategorias de Alimentação
        if ("supermercado".equals(chave)) return "#F97316";
        if ("restaurante".equals(chave)) return "#F97316";
        if ("delivery".equals(chave)) return "#F97316";

        // Subcategorias de Saúde
        if ("plano de saude".equals(chave)) return "#10B981";
        if ("farmacia".equals(chave)) return "#10B981";
        if ("academia".equals(chave)) return "#10B981";

        // Subcategorias de Estudos
        if ("cursos".equals(chave)) return "#8B5CF6";
        if ("materiais".equals(chave)) return "#8B5CF6";

        // Subcategorias não essenciais
        if ("streaming".equals(chave)) return "#EC4899";
        if ("hobby".equals(chave)) return "#EC4899";
        if ("eventos".equals(chave)) return "#EC4899";
        if ("roupas".equals(chave)) return "#EF4444";
        if ("eletronicos".equals(chave)) return "#EF4444";
        if ("igreja".equals(chave)) return "#14B8A6";

        // Subcategorias de receitas
        if ("salario clt".equals(chave)) return "#3B82F6";
        if ("freelance".equals(chave)) return "#22C55E";

        if ("outros".equals(chave) && tipo == TipoCategoria.RECEITA) return "#22C55E";
        if ("outros".equals(chave) && tipo == TipoCategoria.NAO_ESSENCIAL) return "#EF4444";
        if ("imprevistos".equals(chave) && tipo == TipoCategoria.NAO_ESSENCIAL) return "#F59E0B";
        if ("imprevistos".equals(chave)) return "#64748B";
        return DEFAULT_COR;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String normalized = value.toLowerCase(Locale.ROOT)
                .replace("&", " ")
                .replace("á", "a")
                .replace("ã", "a")
                .replace("â", "a")
                .replace("à", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c");
        return normalized.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }
}