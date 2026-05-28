package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.AppLogger;
import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;
import com.financeiramente.core.util.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarCompraCartaoUseCaseTest {

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
    private RegistrarCompraCartaoUseCase useCase;

        private void executarTransacao() {
                doAnswer(invocation -> {
                        Runnable action = invocation.getArgument(0);
                        action.run();
                        return null;
                }).when(transactionManager).executeInTransaction(any());
        }

    @Test
    void executar_credito_devePersistirCompraRegistrarLancamentoEComitar() {
                executarTransacao();
        Fatura fatura = Fatura.builder(UUID.randomUUID().toString())
                .cartaoId("cart-1")
                .mes("2026-05")
                .dataFechamento("2026-05-25")
                .dataVencimento("2026-06-05")
                .status(StatusFatura.ABERTO)
                .build();
        when(resolverFatura.executar("cart-1", "2026-05-10")).thenReturn(fatura);

        RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                "cart-1",
                TipoCompraCartao.CREDITO,
                BigDecimal.valueOf(120.0),
                "2026-05-10",
                "Mercado",
                "cat-1",
                List.of("t1"),
                1,
                null
        );

        useCase.executar(input);

        verify(compraCartaoRepository).salvar(any(CompraCartao.class));
        verify(registrarLancamento).executar(any(RegistrarLancamentoInput.class));
                verify(transactionManager).executeInTransaction(any());
    }

    @Test
    void executar_parcelado_deveGerarUmaCobrancaPorParcela() {
                executarTransacao();
        Fatura fatura = Fatura.builder("fatura")
                .cartaoId("cart-1")
                .mes("2026-05")
                .dataFechamento("2026-05-25")
                .dataVencimento("2026-06-05")
                .status(StatusFatura.ABERTO)
                .build();
        when(resolverFatura.executar(eq("cart-1"), any())).thenReturn(fatura);

        RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                "cart-1",
                TipoCompraCartao.PARCELADO,
                BigDecimal.valueOf(300.0),
                "2026-05-10",
                "Notebook",
                "cat-1",
                List.of(),
                3,
                null
        );

        useCase.executar(input);

        verify(registrarLancamento, times(3)).executar(any(RegistrarLancamentoInput.class));
                verify(transactionManager).executeInTransaction(any());
    }

    @Test
    void executar_deveFalharComParceladoSemParcelasMinimas() {
        RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                "cart-1",
                TipoCompraCartao.PARCELADO,
                BigDecimal.valueOf(100.0),
                "2026-05-10",
                "Teste",
                "cat-1",
                List.of(),
                1,
                null
        );

        assertThrows(DomainException.class, () -> useCase.executar(input));
    }

        @Test
        void executar_deveFalharQuandoDataInvalida() {
                RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                                "cart-1",
                                TipoCompraCartao.CREDITO,
                                BigDecimal.valueOf(100.0),
                                "2026-99-10",
                                "Teste",
                                "cat-1",
                                List.of(),
                                1,
                                null
                );

                assertThrows(DomainException.class, () -> useCase.executar(input));
        }

    @Test
    void executar_quandoFalhaDuranteFluxo_deveFazerRollback() {
                executarTransacao();
        Fatura fatura = Fatura.builder("fatura")
                .cartaoId("cart-1")
                .mes("2026-05")
                .dataFechamento("2026-05-25")
                .dataVencimento("2026-06-05")
                .status(StatusFatura.ABERTO)
                .build();
        when(resolverFatura.executar("cart-1", "2026-05-10")).thenReturn(fatura);
        doThrow(new RuntimeException("falha"))
                .when(registrarLancamento).executar(any(RegistrarLancamentoInput.class));

        RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                "cart-1",
                TipoCompraCartao.CREDITO,
                BigDecimal.valueOf(120.0),
                "2026-05-10",
                "Mercado",
                "cat-1",
                List.of("t1"),
                1,
                null
        );

        assertThrows(RuntimeException.class, () -> useCase.executar(input));
                verify(transactionManager).executeInTransaction(any());
    }
}
