package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.AporteMeta;
import java.math.BigDecimal;
import java.util.List;

public interface AporteMetaRepository {
    void salvar(AporteMeta aporte);
    void deletar(String id);
    List<AporteMeta> listarPorMeta(String metaId);
    BigDecimal somarPorMeta(String metaId);
    BigDecimal somarPorMesEAno(int mes, int ano);
}
