package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarLancamentoCartaoUseCaseTest {

    @Mock
    private ResolverFaturaParaLancamentoUseCase resolverFatura;

    @Mock
    private RegistrarLancamentoUseCase registrarLancamento;

    @Mock
    private TransactionManager transactionManager;

    @InjectMocks
    private RegistrarLancamentoCartaoUseCase useCase;

    @Test
    void deveRegistrarLancamentoNoCartaoComTransactionManager() {
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(transactionManager).executeInTransaction(any(Runnable.class));

        when(resolverFatura.executar(anyString(), anyString())).thenReturn(
                Fatura.builder("fat-1")
                        .cartaoId("cartao-1")
                        .mes("2026-05")
                        .dataFechamento("2026-05-28")
                        .dataVencimento("2026-06-05")
                        .build()
        );

        when(registrarLancamento.executar(any(RegistrarLancamentoInput.class))).thenAnswer(invocation -> {
            RegistrarLancamentoInput input = invocation.getArgument(0);
            return Lancamento.builder("lan-1")
                    .valor(input.getValor())
                    .tipo(input.getTipo())
                    .data(input.getData())
                    .descricao(input.getDescricao())
                    .categoriaId(input.getCategoriaId())
                    .faturaId(input.getFaturaId())
                    .compraCartaoId(input.getCompraCartaoId())
                    .build();
        });

        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                new BigDecimal("89.90"),
                TipoLancamento.DESPESA,
                "2026-05-27",
                "Assinatura anual",
                "cat-1",
                null,
                "compra-1",
                List.of("tag-1")
        );

        Lancamento lancamento = useCase.executar(input, "cartao-1");

        assertNotNull(lancamento);
        assertEquals("fat-1", lancamento.getFaturaId());

        ArgumentCaptor<RegistrarLancamentoInput> captor = ArgumentCaptor.forClass(RegistrarLancamentoInput.class);
        verify(registrarLancamento).executar(captor.capture());
        assertEquals("fat-1", captor.getValue().getFaturaId());

        verify(transactionManager).executeInTransaction(any(Runnable.class));
    }
}
