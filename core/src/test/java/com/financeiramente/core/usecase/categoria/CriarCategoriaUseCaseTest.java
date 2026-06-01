package com.financeiramente.core.usecase.categoria;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarCategoriaUseCaseTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CriarCategoriaUseCase useCase;

    @Test
    void executar_deveCriarCategoriaRaizComDefaults() {
        when(categoriaRepository.listarRaizes()).thenReturn(List.of());

        useCase.executar("Nova", TipoCategoria.ESSENCIAL, null, BigDecimal.valueOf(200.0), null, null);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).salvar(captor.capture());
        assertEquals("Nova", captor.getValue().getNome());
        assertEquals("#6366F1", captor.getValue().getCor());
        assertEquals("📦", captor.getValue().getIcone());
    }

    @Test
    void executar_deveFalharComNomeVazio() {
        assertThrows(DomainException.class,
                () -> useCase.executar("   ", TipoCategoria.ESSENCIAL, null, null, null, null));
    }

    @Test
    void executar_deveFalharQuandoLimiteSubcategoriaUltrapassaPai() {
        Categoria pai = Categoria.builder("pai")
                .nome("Pai")
                .tipo(TipoCategoria.ESSENCIAL)
                .limiteMensal(BigDecimal.valueOf(100.0))
                .build();
        Categoria filhaExistente = Categoria.builder("filha1")
                .nome("Filha")
                .tipo(TipoCategoria.ESSENCIAL)
                .paiId("pai")
                .limiteMensal(BigDecimal.valueOf(80.0))
                .build();

        when(categoriaRepository.buscarPorId("pai")).thenReturn(Optional.of(pai));
        when(categoriaRepository.listarFilhas("pai")).thenReturn(List.of(filhaExistente));

        assertThrows(DomainException.class,
            () -> useCase.executar("Filha nova", TipoCategoria.NAO_ESSENCIAL, "pai", BigDecimal.valueOf(30.0), null, null));
    }
}
