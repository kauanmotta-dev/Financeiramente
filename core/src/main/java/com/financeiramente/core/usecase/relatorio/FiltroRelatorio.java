package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.vo.TipoLancamento;

/**
 * DTO de entrada para o GerarRelatorioUseCase.
 * Todos os campos são opcionais; null significa "sem filtro".
 */
public class FiltroRelatorio {
    private final String dataInicio;
    private final String dataFim;
    private final String categoriaId;
    private final String tagId;
    private final TipoLancamento tipo;

    public FiltroRelatorio(String dataInicio, String dataFim,
                           String categoriaId, String tagId,
                           TipoLancamento tipo) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.categoriaId = categoriaId;
        this.tagId = tagId;
        this.tipo = tipo;
    }

    public String getDataInicio() { return dataInicio; }
    public String getDataFim() { return dataFim; }
    public String getCategoriaId() { return categoriaId; }
    public String getTagId() { return tagId; }
    public TipoLancamento getTipo() { return tipo; }
}
