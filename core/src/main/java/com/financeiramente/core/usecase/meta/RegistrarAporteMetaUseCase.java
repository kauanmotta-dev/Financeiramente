package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DataValidator;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;
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
        String metaIdValidado = DomainException.requireNonBlank(metaId, "Meta é obrigatória.");
        String dataValidada = DataValidator.validarData(data);

        Meta meta = metaRepository.buscarPorId(metaIdValidado)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));

        if (valor <= 0) {
            throw new DomainException("Valor do aporte deve ser maior que zero.");
        }
        AporteMeta aporte = new AporteMeta(
                UUID.randomUUID().toString(),
            metaIdValidado,
                BigDecimal.valueOf(valor),
            dataValidada,
                descricao,
                System.currentTimeMillis()
        );
        aporteRepository.salvar(aporte);

        BigDecimal novoValorAtual = aporteRepository.somarPorMeta(metaId);
        meta.atualizarValorAtual(novoValorAtual);
        metaRepository.atualizar(meta);

        return aporte;
    }
}
