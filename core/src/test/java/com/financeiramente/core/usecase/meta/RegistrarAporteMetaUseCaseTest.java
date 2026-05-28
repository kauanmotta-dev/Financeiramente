package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RegistrarAporteMetaUseCaseTest {

    @Mock
    private MetaRepository metaRepository;
    @Mock
    private AporteMetaRepository aporteRepository;

    @InjectMocks
    private RegistrarAporteMetaUseCase useCase;

    @Test
    void executar_deveFalharQuandoDataInvalida() {
        assertThrows(DomainException.class, () -> useCase.executar("m1", 100.0, "2026-14-01", "aporte"));
    }

    @Test
    void executar_deveFalharQuandoMetaIdVazio() {
        assertThrows(DomainException.class, () -> useCase.executar("  ", 100.0, "2026-05-01", "aporte"));
    }
}