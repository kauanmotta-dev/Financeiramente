package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.AporteMeta;
import java.util.List;

public interface AporteMetaRepository {
    void salvar(AporteMeta aporte);
    void deletar(String id);
    List<AporteMeta> listarPorMeta(String metaId);
    double somarPorMeta(String metaId);
}
