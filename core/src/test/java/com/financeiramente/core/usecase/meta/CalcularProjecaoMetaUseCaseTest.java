package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularProjecaoMetaUseCaseTest {

    @Mock
    private MetaRepository metaRepository;

    @InjectMocks
    private CalcularProjecaoMetaUseCase useCase;

    @Test
    void executar_deveRetornarVazioQuandoSemValorAtual() {
        Meta meta = new Meta("m1", "Casa", BigDecimal.valueOf(10000.0), BigDecimal.ZERO, BigDecimal.ZERO, null, null, System.currentTimeMillis());
        when(metaRepository.buscarPorId("m1")).thenReturn(Optional.of(meta));

        Optional<LocalDate> result = useCase.executar("m1");

        assertFalse(result.isPresent());
    }

    @Test
    void executar_deveRetornarHojeQuandoMetaJaAtingidaPorValorInicial() {
        Meta meta = new Meta("m1", "Casa", BigDecimal.valueOf(1000.0), BigDecimal.valueOf(100.0), BigDecimal.valueOf(1000.0), null, null, System.currentTimeMillis());
        when(metaRepository.buscarPorId("m1")).thenReturn(Optional.of(meta));

        Optional<LocalDate> result = useCase.executar("m1");

        assertEquals(LocalDate.now(), result.orElseThrow());
    }

    @Test
    void executar_deveFalharQuandoMetaNaoExiste() {
        when(metaRepository.buscarPorId("m404")).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.executar("m404"));
    }
}
