package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.repository.FaturaRepository;

import java.util.List;

public class ListarFaturasUseCase {

    private final FaturaRepository faturaRepository;

    public ListarFaturasUseCase(FaturaRepository faturaRepository) {
        this.faturaRepository = faturaRepository;
    }

    public List<Fatura> executar(String cartaoId) {
        return faturaRepository.listarPorCartao(cartaoId);
    }
}
