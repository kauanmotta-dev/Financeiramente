package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.AppLogger;
import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarCobrancasRecorrentesCartaoUseCaseTest {

    @Mock
    private CompraCartaoRepository compraCartaoRepository;
    @Mock
    private ResolverFaturaParaLancamentoUseCase resolverFatura;
    @Mock
    private RegistrarLancamentoUseCase registrarLancamento;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private AppLogger logger;

    @InjectMocks
    private GerarCobrancasRecorrentesCartaoUseCase useCase;

    private void executarTransacao() {
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionManager).executeInTransaction(any());
    }

    @Test
    void executar_deveRetornarZeroSemRecorrentes() {
        when(compraCartaoRepository.listarRecorrentesAtivos()).thenReturn(List.of());

        int result = useCase.executar();

        assertEquals(0, result);
        verify(transactionManager, never()).executeInTransaction(any());
    }

    @Test
    void executar_deveGerarCobrancaQuandoNaoExisteNoMesNemNaFatura() {
        executarTransacao();
        CompraCartao compra = CompraCartao.builder("compra-1")
                .cartaoId("cart-1")
                .descricao("Streaming")
                .valorTotal(BigDecimal.valueOf(49.9))
                .tipo(TipoCompraCartao.RECORRENTE)
                .categoriaId("cat-1")
                .dataCompra("2026-05-01")
                .diaRecorrencia(10)
                .ativo(true)
                .build();
        when(compraCartaoRepository.listarRecorrentesAtivos()).thenReturn(List.of(compra));

        String mesAtual = java.time.YearMonth.from(LocalDate.now()).toString();
        when(compraCartaoRepository.existeLancamentoRecorrenteNoMes("compra-1", mesAtual)).thenReturn(false);

        Fatura fatura = Fatura.builder("fat-1")
                .cartaoId("cart-1")
                .mes("2026-05")
                .dataFechamento("2026-05-25")
                .dataVencimento("2026-06-05")
                .status(StatusFatura.ABERTO)
                .build();
        when(resolverFatura.executar(eq("cart-1"), any())).thenReturn(fatura);
        when(compraCartaoRepository.existeLancamentoRecorrenteNaFatura("compra-1", "fat-1")).thenReturn(false);

        int result = useCase.executar();

        assertEquals(1, result);
        verify(registrarLancamento).executar(any(RegistrarLancamentoInput.class));
        verify(transactionManager).executeInTransaction(any());
    }

    @Test
    void executar_devePularQuandoJaExisteNoMes() {
        executarTransacao();
        CompraCartao compra = CompraCartao.builder("compra-1")
                .cartaoId("cart-1")
                .descricao("Streaming")
                .valorTotal(BigDecimal.valueOf(49.9))
                .tipo(TipoCompraCartao.RECORRENTE)
                .categoriaId("cat-1")
                .dataCompra("2026-05-01")
                .diaRecorrencia(10)
                .ativo(true)
                .build();
        when(compraCartaoRepository.listarRecorrentesAtivos()).thenReturn(List.of(compra));

        String mesAtual = java.time.YearMonth.from(LocalDate.now()).toString();
        when(compraCartaoRepository.existeLancamentoRecorrenteNoMes("compra-1", mesAtual)).thenReturn(true);

        int result = useCase.executar();

        assertEquals(0, result);
        verify(registrarLancamento, never()).executar(any(RegistrarLancamentoInput.class));
        verify(transactionManager).executeInTransaction(any());
    }
}
