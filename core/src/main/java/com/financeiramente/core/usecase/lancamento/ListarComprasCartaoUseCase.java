package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.LancamentoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ListarComprasCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;
    private final LancamentoRepository lancamentoRepository;

    public ListarComprasCartaoUseCase(CompraCartaoRepository compraCartaoRepository,
                                      LancamentoRepository lancamentoRepository) {
        this.compraCartaoRepository = compraCartaoRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public List<CompraCartaoResumo> executar(String cartaoId) {
        List<CompraCartao> compras = compraCartaoRepository.listarPorCartao(cartaoId);
        LocalDate hoje = LocalDate.now();
        List<CompraCartaoResumo> resumo = new ArrayList<>(compras.size());

        for (CompraCartao compra : compras) {
            // Compras recorrentes podem ser desativadas; compras a vista/parceladas devem continuar visiveis.
            if (compra.getTipo() == TipoCompraCartao.RECORRENTE && !compra.isAtivo()) {
                continue;
            }

            List<Lancamento> lancamentos = lancamentoRepository.listarPorCompraCartao(compra.getId());

            double valorJaDebitado = 0.0;
            int parcelasPagas = 0;

            for (Lancamento lancamento : lancamentos) {
                valorJaDebitado += lancamento.getValor().doubleValue();
                if (compra.getTipo() == TipoCompraCartao.PARCELADO) {
                    LocalDate dataLancamento = LocalDate.parse(lancamento.getData());
                    if (!dataLancamento.isAfter(hoje)) {
                        parcelasPagas++;
                    }
                }
            }

            int totalParcelas = compra.getTipo() == TipoCompraCartao.PARCELADO
                    ? (compra.getTotalParcelas() != null ? compra.getTotalParcelas() : 0)
                    : 0;

            resumo.add(new CompraCartaoResumo(compra, parcelasPagas, totalParcelas, valorJaDebitado));
        }

        return resumo;
    }
}
