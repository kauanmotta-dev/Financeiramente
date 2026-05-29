package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoInput;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagarFaturaUseCaseTest {

    @Mock
    private FaturaRepository faturaRepository;
    @Mock
    private LancamentoRepository lancamentoRepository;
    @Mock
    private RegistrarLancamentoUseCase registrarLancamento;
    @Mock
    private ResolverFaturaParaLancamentoUseCase resolverFatura;
    @Mock
    private TransactionManager transactionManager;

    @InjectMocks
    private PagarFaturaUseCase useCase;

    @Test
    void executar_deveGerarAjusteComBaseNoValorTotalMesmoQuandoPagamentoParcial() {
        Fatura faturaAtual = novaFatura("fat-1", "cartao-1", "2026-05", StatusFatura.ABERTO);
        Fatura proximaFatura = novaFatura("fat-2", "cartao-1", "2026-06", StatusFatura.ABERTO);

        when(faturaRepository.buscarPorId("fat-1")).thenReturn(Optional.of(faturaAtual));
        when(lancamentoRepository.somarPorFatura("fat-1")).thenReturn(BigDecimal.valueOf(300));
        when(resolverFatura.executarPorMes("cartao-1", "2026-06")).thenReturn(proximaFatura);

        AtomicInteger sequencia = new AtomicInteger(1);
        when(registrarLancamento.executar(any(RegistrarLancamentoInput.class))).thenAnswer(invocation -> {
            RegistrarLancamentoInput input = invocation.getArgument(0);
            return Lancamento.builder("lanc-" + sequencia.getAndIncrement())
                    .valor(input.getValor())
                    .tipo(input.getTipo())
                    .data(input.getData())
                    .descricao(input.getDescricao())
                    .categoriaId(input.getCategoriaId())
                    .faturaId(input.getFaturaId())
                    .compraCartaoId(input.getCompraCartaoId())
                    .build();
        });

        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionManager).executeInTransaction(any(Runnable.class));

        PagamentoFaturaResult result = useCase.executar("fat-1", 1000.0, 300.0);

        ArgumentCaptor<RegistrarLancamentoInput> captor = ArgumentCaptor.forClass(RegistrarLancamentoInput.class);
        verify(registrarLancamento, times(3)).executar(captor.capture());
        List<RegistrarLancamentoInput> inputs = captor.getAllValues();

        RegistrarLancamentoInput ajuste = inputs.get(0);
        assertEquals(PagarFaturaUseCase.DESCRICAO_AJUSTE_FATURA, ajuste.getDescricao());
        assertEquals(0, ajuste.getValor().compareTo(new BigDecimal("700.00")));
        assertEquals("fat-1", ajuste.getFaturaId());

        RegistrarLancamentoInput pagamento = inputs.get(1);
        assertEquals(0, pagamento.getValor().compareTo(new BigDecimal("300.00")));
        assertNull(pagamento.getFaturaId());
        assertEquals(TipoLancamento.DESPESA, pagamento.getTipo());

        RegistrarLancamentoInput rollover = inputs.get(2);
        assertEquals(PagarFaturaUseCase.descricaoSaldoAnterior("2026-05"), rollover.getDescricao());
        assertEquals(0, rollover.getValor().compareTo(new BigDecimal("700.00")));
        assertEquals("fat-2", rollover.getFaturaId());

        assertEquals(StatusFatura.PAGO_PARCIAL, result.getStatusResultante());
        assertEquals(BigDecimal.valueOf(700.00).setScale(2), result.getSaldoDevedor());
        assertEquals("lanc-2", result.getLancamentoPagamentoId());
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
