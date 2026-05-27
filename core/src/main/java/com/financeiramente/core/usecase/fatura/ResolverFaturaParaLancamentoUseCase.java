package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.core.util.FaturaDateCalculator;

import java.time.YearMonth;
import java.util.UUID;

public class ResolverFaturaParaLancamentoUseCase {

    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;

    public ResolverFaturaParaLancamentoUseCase(CartaoCreditoRepository cartaoRepository,
                                               FaturaRepository faturaRepository) {
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
    }

    public Fatura executar(String cartaoId, String dataLancamento) {
        CartaoCredito cartao = cartaoRepository.buscarPorId(cartaoId)
                .orElseThrow(() -> new DomainException("Cartão não encontrado."));

        if (!cartao.isAtivo()) {
            throw new DomainException("Cartão inativo não pode receber lançamentos.");
        }

        YearMonth mesCompetencia = FaturaDateCalculator.resolverMesCompetencia(
                cartao.getDiaVencimento(),
                cartao.getDiasParaFechamento(),
                dataLancamento
        );

        String mesStr = mesCompetencia.toString(); // YYYY-MM

        return faturaRepository.buscarPorCartaoEMes(cartaoId, mesStr)
                .orElseGet(() -> criarFatura(cartao, mesStr));
    }

    private Fatura criarFatura(CartaoCredito cartao, String mes) {
        String dataFechamento = FaturaDateCalculator.calcularDataFechamento(
                cartao.getDiaVencimento(), cartao.getDiasParaFechamento(), mes);
        String dataVencimento = FaturaDateCalculator.calcularDataVencimento(
                cartao.getDiaVencimento(), mes);

        long now = System.currentTimeMillis();
        Fatura fatura = Fatura.builder(UUID.randomUUID().toString())
                .cartaoId(cartao.getId())
                .mes(mes)
                .dataFechamento(dataFechamento)
                .dataVencimento(dataVencimento)
                .criadoEm(now)
                .atualizadoEm(now)
                .build();

        faturaRepository.salvar(fatura);
        return fatura;
    }
}
