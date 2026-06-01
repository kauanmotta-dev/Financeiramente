package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

public class DeletarCompraCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;
    private final LancamentoRepository lancamentoRepository;

    public DeletarCompraCartaoUseCase(CompraCartaoRepository compraCartaoRepository,
                                      LancamentoRepository lancamentoRepository) {
        this.compraCartaoRepository = compraCartaoRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public void executar(String compraCartaoId) {
        var compra = compraCartaoRepository.buscarPorId(compraCartaoId)
                .orElseThrow(() -> new DomainException("Compra no cartão não encontrada."));

        if (compra.getTipo() == TipoCompraCartao.RECORRENTE) {
            compra.cancelarRecorrencia(System.currentTimeMillis());
            compraCartaoRepository.atualizar(compra);
            return;
        }

        boolean possuiLancamentoEmFaturaPaga = lancamentoRepository.existePorCompraCartaoEmFaturaPaga(compraCartaoId);

        if (possuiLancamentoEmFaturaPaga) {
            lancamentoRepository.deletarPorCompraCartaoEmFaturaNaoPaga(compraCartaoId);
            compra.desativar(System.currentTimeMillis());
            compraCartaoRepository.atualizar(compra);
            return;
        }

        compraCartaoRepository.deletar(compraCartaoId);
    }
}
