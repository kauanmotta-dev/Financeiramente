package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarLancamentoUseCaseTest {

    @Mock
    private LancamentoRepository lancamentoRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private RegistrarLancamentoUseCase useCase;

    @Test
    void executar_deveSalvarEVincularTags() {
        Categoria categoria = Categoria.builder("cat-filho")
                .nome("Filha")
                .paiId("cat-pai")
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        when(categoriaRepository.buscarPorId("cat-filho")).thenReturn(Optional.of(categoria));

        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                BigDecimal.valueOf(99.9),
                TipoLancamento.DESPESA,
                "2026-05-27",
                "Mercado",
                "cat-filho",
                null,
                List.of("t1", "t2")
        );

        Lancamento result = useCase.executar(input);

        assertNotNull(result);
        ArgumentCaptor<Lancamento> captor = ArgumentCaptor.forClass(Lancamento.class);
        verify(lancamentoRepository).salvar(captor.capture());
        assertEquals(BigDecimal.valueOf(99.9), captor.getValue().getValor());
        verify(tagRepository, times(2)).vincularLancamento(eq(result.getId()), any());
    }

    @Test
    void executar_deveFalharQuandoValorInvalido() {
        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                BigDecimal.ZERO,
                TipoLancamento.DESPESA,
                "2026-05-27",
                "Teste",
                null,
                null,
                List.of()
        );

        assertThrows(DomainException.class, () -> useCase.executar(input));
    }

    @Test
    void executar_deveFalharQuandoCategoriaNaoExiste() {
        when(categoriaRepository.buscarPorId("cat-x")).thenReturn(Optional.empty());

        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                BigDecimal.TEN,
                TipoLancamento.DESPESA,
                "2026-05-27",
                "Teste",
                "cat-x",
                null,
                List.of()
        );

        assertThrows(DomainException.class, () -> useCase.executar(input));
    }

    @Test
    void executar_deveFalharQuandoCategoriaRaizNaoSemTipo() {
        Categoria categoria = Categoria.builder("cat-raiz")
                .nome("Raiz")
                .paiId(null)
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        when(categoriaRepository.buscarPorId("cat-raiz")).thenReturn(Optional.of(categoria));

        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                BigDecimal.TEN,
                TipoLancamento.DESPESA,
                "2026-05-27",
                "Teste",
                "cat-raiz",
                null,
                List.of()
        );

        assertThrows(DomainException.class, () -> useCase.executar(input));
    }

        @Test
        void executar_deveFalharQuandoDataInvalida() {
                RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                                BigDecimal.TEN,
                                TipoLancamento.DESPESA,
                                "2026-13-01",
                                "Teste",
                                null,
                                null,
                                List.of()
                );

                assertThrows(DomainException.class, () -> useCase.executar(input));
        }
}
