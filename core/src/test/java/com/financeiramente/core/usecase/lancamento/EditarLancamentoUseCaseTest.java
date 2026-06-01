package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EditarLancamentoUseCaseTest {

    @Mock
    private LancamentoRepository lancamentoRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private EditarLancamentoUseCase useCase;

    @Test
    void executar_deveFalharQuandoDataInvalida() {
        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                BigDecimal.TEN,
                TipoLancamento.DESPESA,
                "2026-15-01",
                "Atualizado",
                null,
                null,
                List.of()
        );

        assertThrows(DomainException.class, () -> useCase.executar("l1", input));
    }
}