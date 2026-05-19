package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.core.util.FinanceCalculator;

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

        if (meta.getValorAtual() <= 0) {
            return Optional.empty();
        }

        LocalDate dataInicio = Instant.ofEpochMilli(meta.getCriadoEm())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate projecao = FinanceCalculator.projecaoConclusaoMeta(
                meta.getValorObjetivo(),
                meta.getValorAtual(),
                dataInicio);

        return Optional.ofNullable(projecao);
    }
}
