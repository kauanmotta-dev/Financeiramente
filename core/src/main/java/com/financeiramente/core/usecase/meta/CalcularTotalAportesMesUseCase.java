package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;

import java.util.List;

public class CalcularTotalAportesMesUseCase {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public CalcularTotalAportesMesUseCase(MetaRepository metaRepository,
                                          AporteMetaRepository aporteMetaRepository) {
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
    }

    public double executar(int ano, int mes) {
        List<Meta> metasAtivas = metaRepository.listarAtivas();
        if (metasAtivas == null || metasAtivas.isEmpty()) {
            return 0.0;
        }

        String prefixoMes = String.format("%04d-%02d", ano, mes);
        double totalAportesMes = 0.0;

        for (Meta meta : metasAtivas) {
            List<AporteMeta> aportes = aporteMetaRepository.listarPorMeta(meta.getId());
            if (aportes == null || aportes.isEmpty()) {
                continue;
            }
            for (AporteMeta aporte : aportes) {
                String data = aporte.getData();
                if (data != null && data.startsWith(prefixoMes)) {
                    totalAportesMes += aporte.getValor();
                }
            }
        }

        return totalAportesMes;
    }
}
