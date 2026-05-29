package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerarRelatorioUseCaseTest {

    @Mock
    private LancamentoRepository lancamentoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private GerarRelatorioUseCase useCase;

    @Test
    void executar_deveSomarReceitasEDespesas() {
        Lancamento receita = Lancamento.builder("l1")
                .valor(BigDecimal.valueOf(1000.0))
                .tipo(TipoLancamento.RECEITA)
                .data("2026-05-05")
                .descricao("Salario")
                .categoriaId("c1")
                .build();
        Lancamento despesa = Lancamento.builder("l2")
                .valor(BigDecimal.valueOf(250.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-06")
                .descricao("Mercado")
                .categoriaId("c2")
                .build();
        Lancamento pagamentoFatura = Lancamento.builder("l3")
                .valor(BigDecimal.valueOf(250.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-10")
                .descricao("Pagamento Total MAI 26")
                .build();

        when(lancamentoRepository.listarPorPeriodoComFiltros("2026-05-01", "2026-05-31", null, null, null))
                .thenReturn(List.of(receita, despesa, pagamentoFatura));
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l1")).thenReturn(java.util.Optional.empty());
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l2")).thenReturn(java.util.Optional.empty());
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l3")).thenReturn(java.util.Optional.of("f1"));
        when(categoriaRepository.listarPorIds(List.of("c2"))).thenReturn(List.of(
            Categoria.builder("c2").nome("Alimentacao").tipo(TipoCategoria.ESSENCIAL).build()
        ));

        RelatorioResult result = useCase.executar(
                new FiltroRelatorio("2026-05-01", "2026-05-31", null, null, null)
        );

        assertEquals(2, result.getLancamentos().size());
        assertEquals(BigDecimal.valueOf(1000.0), result.getTotalReceitas());
        assertEquals(BigDecimal.valueOf(250.0), result.getTotalDespesas());
        assertEquals(BigDecimal.valueOf(750.0), result.getSaldo());
        assertEquals(BigDecimal.valueOf(250.0), result.getTotalPorCategoria().get("Alimentacao"));
    }

    @Test
    void executar_naoDeveContarLancamentosDerivadosDaFatura() {
        Lancamento despesa = Lancamento.builder("l2")
                .valor(BigDecimal.valueOf(250.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-06")
                .descricao("Mercado")
                .categoriaId("c2")
                .build();
        Lancamento pagamentoFatura = Lancamento.builder("l3")
                .valor(BigDecimal.valueOf(250.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-10")
                .descricao("Pagamento Total MAI 26")
                .categoriaId("sem1")
                .build();
        Lancamento ajusteFatura = Lancamento.builder("l4")
                .valor(BigDecimal.valueOf(250.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-10")
                .descricao("Ajuste de fatura")
                .categoriaId("sem1")
                .build();

        when(lancamentoRepository.listarPorPeriodoComFiltros("2026-05-01", "2026-05-31", null, null, null))
                .thenReturn(List.of(despesa, pagamentoFatura, ajusteFatura));
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l2")).thenReturn(java.util.Optional.empty());
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l3")).thenReturn(java.util.Optional.of("f1"));
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l4")).thenReturn(java.util.Optional.empty());
        when(categoriaRepository.listarPorIds(List.of("c2"))).thenAnswer(invocation -> {
            List<Categoria> categorias = new ArrayList<>();
            categorias.add(Categoria.builder("c2").nome("Alimentacao").tipo(TipoCategoria.ESSENCIAL).build());
            return categorias;
        });

        RelatorioResult result = useCase.executar(
                new FiltroRelatorio("2026-05-01", "2026-05-31", null, null, null)
        );

        assertEquals(1, result.getLancamentos().size());
        assertEquals(BigDecimal.valueOf(250.0), result.getTotalDespesas());
        assertEquals(BigDecimal.valueOf(250.0), result.getTotalPorCategoria().get("Alimentacao"));
        assertEquals(1, result.getTotalPorCategoria().size());
    }

    @Test
    void executar_deveFiltrarPorTag() {
        Lancamento l1 = Lancamento.builder("l1")
                .valor(BigDecimal.valueOf(100.0)).tipo(TipoLancamento.DESPESA).data("2026-05-01").descricao("A").categoriaId("c1").build();

        when(lancamentoRepository.listarPorPeriodoComFiltros("2026-05-01", "2026-05-31", null, null, "t1"))
                .thenReturn(List.of(l1));
        when(lancamentoRepository.buscarFaturaIdPorLancamentoPagamento("l1")).thenReturn(java.util.Optional.empty());
        when(categoriaRepository.listarPorIds(List.of("c1"))).thenReturn(List.of(
            Categoria.builder("c1").nome("Lazer").tipo(TipoCategoria.NAO_ESSENCIAL).build()
        ));

        RelatorioResult result = useCase.executar(
                new FiltroRelatorio("2026-05-01", "2026-05-31", null, "t1", null)
        );

        assertEquals(1, result.getLancamentos().size());
        assertEquals("l1", result.getLancamentos().get(0).getId());
    }
}
