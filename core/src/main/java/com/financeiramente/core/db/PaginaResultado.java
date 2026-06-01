package com.financeiramente.core.db;

import java.util.List;

public final class PaginaResultado<T> {

    private final List<T> itens;
    private final int total;
    private final boolean temProxima;

    public PaginaResultado(List<T> itens, int total, boolean temProxima) {
        this.itens = itens;
        this.total = total;
        this.temProxima = temProxima;
    }

    public List<T> getItens() {
        return itens;
    }

    public int getTotal() {
        return total;
    }

    public boolean isTemProxima() {
        return temProxima;
    }
}