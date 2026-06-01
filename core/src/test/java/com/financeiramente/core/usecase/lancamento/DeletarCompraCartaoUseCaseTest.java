package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletarCompraCartaoUseCaseTest {

    @Mock
    private CompraCartaoRepository compraCartaoRepository;

    @Mock
    private LancamentoRepository lancamentoRepository;

    @InjectMocks
    private DeletarCompraCartaoUseCase useCase;

    @Test
    void executar_quandoRecorrenteDeveApenasDesativarSemApagarLancamentos() {
        String compraId = UUID.randomUUID().toString();
        CompraCartao recorrente = CompraCartao.builder(compraId)
                .cartaoId("cart-1")
                .descricao("Streaming")
                .valorTotal(BigDecimal.valueOf(39.90))
                .tipo(TipoCompraCartao.RECORRENTE)
                .categoriaId("cat-1")
                .dataCompra("2026-05-10")
                .diaRecorrencia(10)
                .ativo(true)
                .build();

        when(compraCartaoRepository.buscarPorId(compraId)).thenReturn(java.util.Optional.of(recorrente));

        useCase.executar(compraId);

        verify(lancamentoRepository, never()).deletarPorCompraCartaoEmFaturaNaoPaga(compraId);
        verify(compraCartaoRepository).atualizar(recorrente);
    }

    @Test
    void executar_quandoNaoRecorrenteComLancamentoPagoDeveDeletarPendentesEManterCompra() {
        String compraId = UUID.randomUUID().toString();
        CompraCartao compra = CompraCartao.builder(compraId)
                .cartaoId("cart-1")
                .descricao("Notebook")
                .valorTotal(BigDecimal.valueOf(3000.0))
                .tipo(TipoCompraCartao.PARCELADO)
                .totalParcelas(3)
                .categoriaId("cat-1")
                .dataCompra("2026-05-10")
                .ativo(true)
                .build();

        when(compraCartaoRepository.buscarPorId(compraId)).thenReturn(java.util.Optional.of(compra));
        when(lancamentoRepository.existePorCompraCartaoEmFaturaPaga(compraId)).thenReturn(true);

        useCase.executar(compraId);

        verify(lancamentoRepository).deletarPorCompraCartaoEmFaturaNaoPaga(compraId);
        verify(compraCartaoRepository).atualizar(compra);
    }
}