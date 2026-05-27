package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.util.DomainException;

public class CancelarRecorrenciaCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;

    public CancelarRecorrenciaCartaoUseCase(CompraCartaoRepository compraCartaoRepository) {
        this.compraCartaoRepository = compraCartaoRepository;
    }

    public void executar(String compraCartaoId) {
        CompraCartao compra = compraCartaoRepository.buscarPorId(compraCartaoId)
                .orElseThrow(() -> new DomainException("Compra no cartão não encontrada."));

        if (compra.getTipo() != TipoCompraCartao.RECORRENTE) {
            throw new DomainException("Apenas compras recorrentes podem ser canceladas.");
        }

        compra.setAtivo(false);
        compra.setAtualizadoEm(System.currentTimeMillis());
        compraCartaoRepository.atualizar(compra);
    }
}
