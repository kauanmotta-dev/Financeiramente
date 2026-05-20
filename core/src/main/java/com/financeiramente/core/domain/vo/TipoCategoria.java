package com.financeiramente.core.domain.vo;

public enum TipoCategoria {
    ESSENCIAL,
    NAO_ESSENCIAL,
    RECEITA;

    public boolean isCompativelCom(TipoLancamento tipoLancamento) {
        switch (tipoLancamento) {
            case RECEITA: return this == RECEITA;
            case DESPESA: return this == ESSENCIAL || this == NAO_ESSENCIAL;
            default:      return false;
        }
    }
}
