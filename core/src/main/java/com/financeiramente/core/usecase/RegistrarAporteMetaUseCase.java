package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class RegistrarAporteMetaUseCase {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteRepository;

    public RegistrarAporteMetaUseCase(MetaRepository metaRepository,
                                       AporteMetaRepository aporteRepository) {
        this.metaRepository = metaRepository;
        this.aporteRepository = aporteRepository;
    }

    public AporteMeta executar(String metaId, double valor, String data, String descricao) {
        Meta meta = metaRepository.buscarPorId(metaId)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));

        if (valor <= 0) {
            throw new DomainException("Valor do aporte deve ser maior que zero.");
        }
        if (data == null || data.trim().isEmpty()) {
            throw new DomainException("Data do aporte é obrigatória.");
        }

        AporteMeta aporte = new AporteMeta(
                UUID.randomUUID().toString(),
                metaId,
                valor,
                data.trim(),
                descricao,
                System.currentTimeMillis()
        );
        aporteRepository.salvar(aporte);

        double novoValorAtual = aporteRepository.somarPorMeta(metaId);
        meta.setValorAtual(novoValorAtual);
        metaRepository.atualizar(meta);

        return aporte;
    }
}
