package com.financeiramente.core.domain.vo;

public enum TipoCategoria {
    ESSENCIAL,
    NAO_ESSENCIAL,
    RECEITA,
    SEM_TIPO;

    public boolean isCompativelCom(TipoLancamento tipo) {
        if (this == SEM_TIPO) return true;
        if (this == RECEITA) return tipo == TipoLancamento.RECEITA;
        return tipo == TipoLancamento.DESPESA;
    }
}
