package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;

public class DeletarAporteMetaUseCase {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteRepository;

    public DeletarAporteMetaUseCase(MetaRepository metaRepository,
                                     AporteMetaRepository aporteRepository) {
        this.metaRepository = metaRepository;
        this.aporteRepository = aporteRepository;
    }

    public void executar(String aporteId, String metaId) {
        Meta meta = metaRepository.buscarPorId(metaId)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));

        aporteRepository.deletar(aporteId);

        BigDecimal novoValorAtual = aporteRepository.somarPorMeta(metaId);
        meta.atualizarValorAtual(novoValorAtual);
        metaRepository.atualizar(meta);
    }
}
