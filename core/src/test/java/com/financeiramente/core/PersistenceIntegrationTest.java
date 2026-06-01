package com.financeiramente.core;

import com.financeiramente.core.dao.CategoriaDao;
import com.financeiramente.core.dao.LancamentoDao;
import com.financeiramente.core.dao.TagDao;
import com.financeiramente.core.db.AppLogger;
import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.DatabaseMigrator;
import com.financeiramente.core.db.InMemoryJdbcDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.relatorio.FiltroRelatorio;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.relatorio.RelatorioResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceIntegrationTest {

    private DatabaseDriver driver;
    private CategoriaDao categoriaDao;
    private LancamentoDao lancamentoDao;
    private TagDao tagDao;

    @BeforeEach
    void setUp() {
        driver = InMemoryJdbcDriver.create();
        new DatabaseMigrator(driver, new AppLogger() {}).migrate();

        categoriaDao = new CategoriaDao(driver);
        lancamentoDao = new LancamentoDao(driver);
        tagDao = new TagDao(driver);
    }

    @Test
    void migrate_deveCriarTabelasBase() {
        Set<String> tables = driver.query(
                "SELECT name FROM sqlite_master WHERE type='table'",
                row -> row.getString("name")
        ).stream().collect(Collectors.toSet());

        assertTrue(tables.contains("schema_version"));
        assertTrue(tables.contains("categoria"));
        assertTrue(tables.contains("lancamento"));
        assertTrue(tables.contains("tag"));
        assertTrue(tables.contains("lancamento_tag"));
        assertTrue(tables.contains("meta"));
        assertTrue(tables.contains("aporte_meta"));
        assertTrue(tables.contains("cartao_credito"));
        assertTrue(tables.contains("fatura"));
        assertTrue(tables.contains("compra_cartao"));
    }

    @Test
    void migrate_deveSerIdempotenteESemDuplicarOnboarding() {
        int totalAntes = categoriaDao.listarTodas().size();

        new DatabaseMigrator(driver, new AppLogger() {}).migrate();

        int totalDepois = categoriaDao.listarTodas().size();
        assertEquals(totalAntes, totalDepois);
        assertEquals(3, driver.getSchemaVersion());
    }

    @Test
    void lancamentoDao_crudQueriesESomatorios() {
        Categoria categoria = criarCategoria("Moradia Teste", TipoCategoria.ESSENCIAL);

        Lancamento receita = Lancamento.builder(UUID.randomUUID().toString())
            .valor(BigDecimal.valueOf(4000.0))
                .tipo(TipoLancamento.RECEITA)
                .data("2026-05-01")
                .descricao("Salario")
                .categoriaId(categoria.getId())
                .build();

        Lancamento despesaA = Lancamento.builder(UUID.randomUUID().toString())
            .valor(BigDecimal.valueOf(1200.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-05")
                .descricao("Aluguel")
                .categoriaId(categoria.getId())
                .build();

        Lancamento despesaB = Lancamento.builder(UUID.randomUUID().toString())
            .valor(BigDecimal.valueOf(300.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-04-15")
                .descricao("Mercado")
                .categoriaId(categoria.getId())
                .build();

        lancamentoDao.salvar(receita);
        lancamentoDao.salvar(despesaA);
        lancamentoDao.salvar(despesaB);

        Optional<Lancamento> encontrado = lancamentoDao.buscarPorId(receita.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Salario", encontrado.get().getDescricao());

        List<Lancamento> maio = lancamentoDao.listarPorMes(2026, 5);
        assertEquals(2, maio.size());

        List<Lancamento> periodo = lancamentoDao.listarPorPeriodo("2026-04-01", "2026-05-31");
        assertEquals(3, periodo.size());

        BigDecimal receitasMaio = lancamentoDao.somarPorTipoEMes(TipoLancamento.RECEITA, 2026, 5);
        BigDecimal despesasMaio = lancamentoDao.somarPorTipoEMes(TipoLancamento.DESPESA, 2026, 5);
        BigDecimal totalCategoriaMaio = lancamentoDao.somarPorCategoria(categoria.getId(), 2026, 5);

        assertEquals(new BigDecimal("4000.00"), receitasMaio);
        assertEquals(new BigDecimal("1200.00"), despesasMaio);
        assertEquals(new BigDecimal("5200.00"), totalCategoriaMaio);

        receita.atualizarDados(receita.getValor(), receita.getTipo(), receita.getData(), "Salario atualizado", receita.getCategoriaId());
        receita.setAtualizadoEm(System.currentTimeMillis());
        lancamentoDao.atualizar(receita);

        Optional<Lancamento> atualizado = lancamentoDao.buscarPorId(receita.getId());
        assertTrue(atualizado.isPresent());
        assertEquals("Salario atualizado", atualizado.get().getDescricao());

        assertTrue(lancamentoDao.existePorCategoria(categoria.getId()));

        lancamentoDao.deletar(receita.getId());
        assertTrue(lancamentoDao.buscarPorId(receita.getId()).isEmpty());
    }

    @Test
    void lancamentoDao_listarPorTag_deveRetornarApenasVinculados() {
        Categoria categoria = criarCategoria("Lazer Teste", TipoCategoria.NAO_ESSENCIAL);

        Lancamento l1 = Lancamento.builder(UUID.randomUUID().toString())
            .valor(BigDecimal.valueOf(150.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-10")
                .descricao("Cinema")
                .categoriaId(categoria.getId())
                .build();

        Lancamento l2 = Lancamento.builder(UUID.randomUUID().toString())
            .valor(BigDecimal.valueOf(200.0))
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-11")
                .descricao("Show")
                .categoriaId(categoria.getId())
                .build();

        lancamentoDao.salvar(l1);
        lancamentoDao.salvar(l2);

        Tag viagem = new Tag(UUID.randomUUID().toString(), "viagem", System.currentTimeMillis());
        tagDao.salvar(viagem);
        tagDao.vincularLancamento(l2.getId(), viagem.getId());

        List<Lancamento> porTag = lancamentoDao.listarPorTag(viagem.getId(), 2026, 5);
        assertEquals(1, porTag.size());
        assertEquals("Show", porTag.get(0).getDescricao());

        List<Tag> tagsLancamento = tagDao.listarPorLancamento(l2.getId());
        assertEquals(1, tagsLancamento.size());
        assertEquals("viagem", tagsLancamento.get(0).getNome());

        assertNotNull(tagDao.buscarPorNome("VIAGEM").orElse(null));
        assertFalse(tagDao.listarPorLancamento(l1.getId()).stream().anyMatch(t -> t.getId().equals(viagem.getId())));
    }

    @Test
    void gerarRelatorio_deveExecutarQuantidadeConstanteDeQueries() {
        QueryCountingDriver driverComPoucosDados = new QueryCountingDriver(InMemoryJdbcDriver.create());
        QueryCountingDriver driverComMuitosDados = new QueryCountingDriver(InMemoryJdbcDriver.create());

        int queriesComPoucosDados = executarRelatorioComDados(driverComPoucosDados, 2);
        int queriesComMuitosDados = executarRelatorioComDados(driverComMuitosDados, 60);

        assertEquals(queriesComPoucosDados, queriesComMuitosDados);
        assertTrue(queriesComPoucosDados <= 3);
    }

    private int executarRelatorioComDados(QueryCountingDriver countingDriver, int quantidadeLancamentos) {
        new DatabaseMigrator(countingDriver, new AppLogger() {}).migrate();

        CategoriaDao categoriaDaoLocal = new CategoriaDao(countingDriver);
        LancamentoDao lancamentoDaoLocal = new LancamentoDao(countingDriver);
        TagDao tagDaoLocal = new TagDao(countingDriver);
        GerarRelatorioUseCase useCase = new GerarRelatorioUseCase(lancamentoDaoLocal, categoriaDaoLocal, tagDaoLocal);

        Categoria categoria = Categoria.builder(UUID.randomUUID().toString())
                .nome("Categoria teste")
                .tipo(TipoCategoria.ESSENCIAL)
                .ordem(999)
                .build();
        categoriaDaoLocal.salvar(categoria);

        Tag tag = new Tag(UUID.randomUUID().toString(), "tag-relatorio", System.currentTimeMillis());
        tagDaoLocal.salvar(tag);

        for (int i = 0; i < quantidadeLancamentos; i++) {
            Lancamento lancamento = Lancamento.builder(UUID.randomUUID().toString())
                    .valor(BigDecimal.valueOf(10.0 + i))
                    .tipo(TipoLancamento.DESPESA)
                    .data("2026-05-10")
                    .descricao("L" + i)
                    .categoriaId(categoria.getId())
                    .build();
            lancamentoDaoLocal.salvar(lancamento);
            tagDaoLocal.vincularLancamento(lancamento.getId(), tag.getId());
        }

        countingDriver.resetCounters();
        RelatorioResult result = useCase.executar(new FiltroRelatorio(
                "2026-05-01",
                "2026-05-31",
                null,
                tag.getId(),
                null
        ));

        assertEquals(quantidadeLancamentos, result.getLancamentos().size());
        return countingDriver.getQueryCount();
    }

    private Categoria criarCategoria(String nome, TipoCategoria tipo) {
        Categoria categoria = Categoria.builder(UUID.randomUUID().toString())
                .nome(nome)
                .tipo(tipo)
                .ordem(999)
                .build();
        categoriaDao.salvar(categoria);
        return categoria;
    }

    private static final class QueryCountingDriver implements DatabaseDriver {
        private final DatabaseDriver delegate;
        private int queryCount;

        private QueryCountingDriver(DatabaseDriver delegate) {
            this.delegate = delegate;
        }

        private int getQueryCount() {
            return queryCount;
        }

        private void resetCounters() {
            queryCount = 0;
        }

        @Override
        public void execute(String sql) {
            delegate.execute(sql);
        }

        @Override
        public void execute(String sql, Object... args) {
            delegate.execute(sql, args);
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
            queryCount++;
            return delegate.query(sql, mapper, args);
        }

        @Override
        public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... args) {
            queryCount++;
            return delegate.queryOne(sql, mapper, args);
        }

        @Override
        public void beginTransaction() {
            delegate.beginTransaction();
        }

        @Override
        public void commitTransaction() {
            delegate.commitTransaction();
        }

        @Override
        public void rollbackTransaction() {
            delegate.rollbackTransaction();
        }

        @Override
        public int getSchemaVersion() {
            return delegate.getSchemaVersion();
        }

        @Override
        public void setSchemaVersion(int version) {
            delegate.setSchemaVersion(version);
        }
    }
}
