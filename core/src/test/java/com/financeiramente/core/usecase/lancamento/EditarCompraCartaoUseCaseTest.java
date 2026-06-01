package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditarCompraCartaoUseCaseTest {

    @Mock
    private CompraCartaoRepository compraCartaoRepository;

    @InjectMocks
    private EditarCompraCartaoUseCase useCase;

    @Test
    void executar_deveAtualizarCompraMantendoIdAtivoEDataCriacao() {
        String compraId = UUID.randomUUID().toString();
        CompraCartao existente = CompraCartao.builder(compraId)
                .cartaoId("cart-1")
                .descricao("Assinatura antiga")
                .valorTotal(BigDecimal.valueOf(29.90))
                .tipo(TipoCompraCartao.RECORRENTE)
                .categoriaId("cat-1")
                .dataCompra("2026-05-10")
                .diaRecorrencia(10)
                .ativo(true)
                .criadoEm(1234L)
                .atualizadoEm(1234L)
                .build();

        when(compraCartaoRepository.buscarPorId(compraId)).thenReturn(java.util.Optional.of(existente));

        RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                "cart-2",
                TipoCompraCartao.RECORRENTE,
                BigDecimal.valueOf(39.90),
                "2026-06-15",
                "Assinatura nova",
                "cat-2",
                List.of(),
                1,
                15
        );

        CompraCartao atualizada = useCase.executar(compraId, input);

        ArgumentCaptor<CompraCartao> captor = ArgumentCaptor.forClass(CompraCartao.class);
        verify(compraCartaoRepository).atualizar(captor.capture());

        CompraCartao salvo = captor.getValue();
        assertEquals(compraId, salvo.getId());
        assertEquals("cart-2", salvo.getCartaoId());
        assertEquals("Assinatura nova", salvo.getDescricao());
        assertEquals(BigDecimal.valueOf(39.90), salvo.getValorTotal());
        assertEquals(TipoCompraCartao.RECORRENTE, salvo.getTipo());
        assertEquals(Integer.valueOf(15), salvo.getDiaRecorrencia());
        assertEquals(1234L, salvo.getCriadoEm());
        assertEquals(true, salvo.isAtivo());
        assertNotNull(atualizada);
    }
}