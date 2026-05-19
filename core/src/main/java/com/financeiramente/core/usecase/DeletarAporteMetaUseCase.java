package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

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

        double novoValorAtual = aporteRepository.somarPorMeta(metaId);
        meta.setValorAtual(novoValorAtual);
        metaRepository.atualizar(meta);
    }
}
