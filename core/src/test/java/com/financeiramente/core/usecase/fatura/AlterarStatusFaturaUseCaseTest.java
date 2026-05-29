package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterarStatusFaturaUseCaseTest {

    @Mock
    private FaturaRepository faturaRepository;
    @Mock
    private LancamentoRepository lancamentoRepository;
    @Mock
    private PagarFaturaUseCase pagarFaturaUseCase;
    @Mock
    private TransactionManager transactionManager;

    @InjectMocks
    private AlterarStatusFaturaUseCase useCase;

    @Test
    void executar_deveRemoverPagamentoEAjusteAoVoltarFaturaPagaParaFechada() {
        Fatura fatura = novaFatura("fat-1", "cartao-1", "2026-05", StatusFatura.PAGO, "pag-1");
        Lancamento ajuste = Lancamento.builder("ajuste-1")
                .descricao(PagarFaturaUseCase.DESCRICAO_AJUSTE_FATURA)
                .faturaId("fat-1")
                .build();
        Lancamento compraOriginal = Lancamento.builder("compra-1")
                .descricao("Compra original")
                .faturaId("fat-1")
                .build();

        when(faturaRepository.buscarPorId("fat-1")).thenReturn(Optional.of(fatura));
        when(lancamentoRepository.listarPorFatura("fat-1")).thenReturn(List.of(ajuste, compraOriginal));
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionManager).executeInTransaction(any(Runnable.class));

        useCase.executar("fat-1", StatusFatura.FECHADO, 0, 0);

        verify(lancamentoRepository).deletar("pag-1");
        verify(lancamentoRepository).deletar("ajuste-1");
        verify(lancamentoRepository, never()).deletar("compra-1");

        InOrder inOrder = inOrder(faturaRepository, lancamentoRepository);
        inOrder.verify(faturaRepository).atualizar(any(Fatura.class));
        inOrder.verify(lancamentoRepository).deletar("pag-1");

        ArgumentCaptor<Fatura> captor = ArgumentCaptor.forClass(Fatura.class);
        verify(faturaRepository).atualizar(captor.capture());
        assertEquals(StatusFatura.FECHADO, captor.getValue().getStatus());
        assertEquals(BigDecimal.ZERO, captor.getValue().getValorPago());
        assertNull(captor.getValue().getLancamentoPagamentoId());
    }

    @Test
    void executar_deveRemoverSaldoAnteriorAoVoltarFaturaPagaParcialParaAberta() {
        Fatura fatura = novaFatura("fat-1", "cartao-1", "2026-05", StatusFatura.PAGO_PARCIAL, "pag-1");
        Fatura proximaFatura = novaFatura("fat-2", "cartao-1", "2026-06", StatusFatura.ABERTO, null);
        Lancamento ajuste = Lancamento.builder("ajuste-1")
                .descricao(PagarFaturaUseCase.DESCRICAO_AJUSTE_FATURA)
                .faturaId("fat-1")
                .build();
        Lancamento saldoAnterior = Lancamento.builder("saldo-1")
                .descricao(PagarFaturaUseCase.descricaoSaldoAnterior("2026-05"))
                .faturaId("fat-2")
                .build();
        Lancamento compraSeguinte = Lancamento.builder("compra-next")
                .descricao("Compra da proxima fatura")
                .faturaId("fat-2")
                .build();

        when(faturaRepository.buscarPorId("fat-1")).thenReturn(Optional.of(fatura));
        when(lancamentoRepository.listarPorFatura("fat-1")).thenReturn(List.of(ajuste));
        when(faturaRepository.buscarPorCartaoEMes("cartao-1", "2026-06")).thenReturn(Optional.of(proximaFatura));
        when(lancamentoRepository.listarPorFatura("fat-2")).thenReturn(List.of(saldoAnterior, compraSeguinte));
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionManager).executeInTransaction(any(Runnable.class));

        useCase.executar("fat-1", StatusFatura.ABERTO, 0, 0);

        verify(lancamentoRepository).deletar("pag-1");
        verify(lancamentoRepository).deletar("ajuste-1");
        verify(lancamentoRepository).deletar("saldo-1");
        verify(lancamentoRepository, never()).deletar("compra-next");

        InOrder inOrder = inOrder(faturaRepository, lancamentoRepository);
        inOrder.verify(faturaRepository).atualizar(any(Fatura.class));
        inOrder.verify(lancamentoRepository).deletar("pag-1");

        ArgumentCaptor<Fatura> captor = ArgumentCaptor.forClass(Fatura.class);
        verify(faturaRepository).atualizar(captor.capture());
        assertEquals(StatusFatura.ABERTO, captor.getValue().getStatus());
        assertEquals(BigDecimal.ZERO, captor.getValue().getValorPago());
        assertNull(captor.getValue().getLancamentoPagamentoId());
    }

    @Test
    void executar_deveExcluirFaturaQuandoElaFicarSemLancamentosAoReverterPagamento() {
        Fatura fatura = novaFatura("fat-1", "cartao-1", "2026-05", StatusFatura.PAGO, "pag-1");
        Lancamento ajuste = Lancamento.builder("ajuste-1")
                .descricao(PagarFaturaUseCase.DESCRICAO_AJUSTE_FATURA)
                .faturaId("fat-1")
                .build();

        when(faturaRepository.buscarPorId("fat-1")).thenReturn(Optional.of(fatura));
        when(lancamentoRepository.listarPorFatura("fat-1")).thenReturn(List.of(ajuste)).thenReturn(List.of());
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionManager).executeInTransaction(any(Runnable.class));

        useCase.executar("fat-1", StatusFatura.FECHADO, 0, 0);

        verify(lancamentoRepository).deletar("pag-1");
        verify(lancamentoRepository).deletar("ajuste-1");
        verify(faturaRepository).deletar("fat-1");
        verify(faturaRepository, never()).atualizar(fatura);

        InOrder inOrder = inOrder(faturaRepository, lancamentoRepository);
        inOrder.verify(faturaRepository).deletar("fat-1");
        inOrder.verify(lancamentoRepository).deletar("pag-1");
    }

    private Fatura novaFatura(String id, String cartaoId, String mes, StatusFatura status, String lancamentoPagamentoId) {
        return Fatura.builder(id)
                .cartaoId(cartaoId)
                .mes(mes)
                .dataFechamento(mes + "-10")
                .dataVencimento(mes + "-20")
                .status(status)
                .valorPago(BigDecimal.valueOf(100))
                .lancamentoPagamentoId(lancamentoPagamentoId)
                .build();
    }
}