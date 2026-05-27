package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.util.DomainException;

public class DeletarCompraCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;

    public DeletarCompraCartaoUseCase(CompraCartaoRepository compraCartaoRepository) {
        this.compraCartaoRepository = compraCartaoRepository;
    }

    public void executar(String compraCartaoId) {
        compraCartaoRepository.buscarPorId(compraCartaoId)
                .orElseThrow(() -> new DomainException("Compra no cartão não encontrada."));

        compraCartaoRepository.deletar(compraCartaoId);
    }
}
