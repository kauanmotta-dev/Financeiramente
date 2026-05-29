package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnteciparLancamentosFaturaUseCaseTest {

    @Mock
    private FaturaRepository faturaRepository;
    @Mock
    private LancamentoRepository lancamentoRepository;

    @Test
    void executar_deveExcluirFaturaFuturaQuandoTodosLancamentosForemAntecipados() {
        Fatura faturaAtual = novaFatura("fat-atual", "cartao-1", "2026-05", StatusFatura.ABERTO);
        Fatura faturaFutura = novaFatura("fat-futura", "cartao-1", "2026-06", StatusFatura.FECHADO);

        when(faturaRepository.buscarPorId("fat-futura")).thenReturn(Optional.of(faturaFutura));
        when(faturaRepository.buscarFaturaAbertaPorCartao("cartao-1")).thenReturn(Optional.of(faturaAtual));
        when(lancamentoRepository.listarPorFatura("fat-futura")).thenReturn(List.of());

        AnteciparLancamentosFaturaUseCase useCase = new AnteciparLancamentosFaturaUseCase(
                faturaRepository,
                lancamentoRepository);

        Fatura resultado = useCase.executar("fat-futura");

        assertEquals("fat-atual", resultado.getId());
        verify(lancamentoRepository).transferirLancamentosDeFatura("fat-futura", "fat-atual");
        verify(faturaRepository).deletar("fat-futura");
    }

    private Fatura novaFatura(String id, String cartaoId, String mes, StatusFatura status) {
        return Fatura.builder(id)
                .cartaoId(cartaoId)
                .mes(mes)
                .dataFechamento(mes + "-10")
                .dataVencimento(mes + "-20")
                .status(status)
                .valorPago(BigDecimal.ZERO)
                .build();
    }
}