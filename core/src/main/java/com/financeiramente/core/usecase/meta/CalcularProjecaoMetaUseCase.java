package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.core.util.FinanceCalculator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

public class CalcularProjecaoMetaUseCase {

    private final MetaRepository metaRepository;

    public CalcularProjecaoMetaUseCase(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    public Optional<LocalDate> executar(String metaId) {
        Meta meta = metaRepository.buscarPorId(metaId)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));

        if (meta.getValorAtual().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        // Valor efetivo a atingir descontando o valor inicial já disponível
        BigDecimal valorEfetivo = meta.getValorEfetivo();
        if (valorEfetivo.compareTo(BigDecimal.ZERO) <= 0) {
            // Valor inicial já cobre o objetivo
            return Optional.of(LocalDate.now());
        }

        LocalDate dataInicio = Instant.ofEpochMilli(meta.getCriadoEm())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate projecao = FinanceCalculator.projecaoConclusaoMeta(
                valorEfetivo,
                meta.getValorAtual(),
                dataInicio);

        return Optional.ofNullable(projecao);
    }
}
