package com.financeiramente.core.usecase.saldo;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularSaldoDashboardUseCaseTest {

    @Mock
    private LancamentoRepository lancamentoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private FaturaRepository faturaRepository;
    @Mock
    private AporteMetaRepository aporteMetaRepository;

    @InjectMocks
    private CalcularSaldoDashboardUseCase useCase;

    @Test
    void executar_deveCalcularSaldosComLimitesPorTipo() {
        when(lancamentoRepository.somarPorTipoEMes(TipoLancamento.RECEITA, 2026, 5)).thenReturn(BigDecimal.valueOf(5000.0));
        when(lancamentoRepository.somarDespesasSemFaturaPorMes(2026, 5)).thenReturn(BigDecimal.valueOf(700.0));
        when(faturaRepository.somarValorPagoPorDataDePagamento(2026, 5)).thenReturn(BigDecimal.valueOf(1000.0));
        when(aporteMetaRepository.somarPorMesEAno(5, 2026)).thenReturn(BigDecimal.valueOf(300.0));

        when(lancamentoRepository.somarDespesasPorTipoCategoriaEMes(TipoCategoria.ESSENCIAL, 2026, 5)).thenReturn(BigDecimal.valueOf(1200.0));
        when(lancamentoRepository.somarDespesasPorTipoCategoriaEMes(TipoCategoria.NAO_ESSENCIAL, 2026, 5)).thenReturn(BigDecimal.valueOf(400.0));

        Categoria raizEssencial = Categoria.builder("e1")
                .nome("Essencial")
                .tipo(TipoCategoria.ESSENCIAL)
                .limiteMensal(BigDecimal.valueOf(2000.0))
                .build();
        Categoria raizNaoEssencial = Categoria.builder("n1")
                .nome("Nao Essencial")
                .tipo(TipoCategoria.NAO_ESSENCIAL)
                .limiteMensal(BigDecimal.valueOf(800.0))
                .build();

        when(categoriaRepository.listarTodas()).thenReturn(List.of(raizEssencial, raizNaoEssencial));

        SaldoDashboardResult result = useCase.executar(2026, 5);

        assertEquals(3000.0, result.getSaldoConta(), 0.001);
        assertEquals(2000.0, result.getLimiteEssenciais(), 0.001);
        assertEquals(800.0, result.getSaldoEssenciais(), 0.001);
        assertEquals(800.0, result.getLimiteNaoEssenciais(), 0.001);
        assertEquals(400.0, result.getSaldoNaoEssenciais(), 0.001);
    }
}
