package com.financeiramente.core;

import com.financeiramente.core.dao.AporteMetaDao;
import com.financeiramente.core.dao.CategoriaDao;
import com.financeiramente.core.dao.LancamentoDao;
import com.financeiramente.core.dao.LancamentoRecorrenteDao;
import com.financeiramente.core.dao.MetaDao;
import com.financeiramente.core.dao.PlanejamentoDao;
import com.financeiramente.core.dao.TagDao;
import com.financeiramente.core.db.DatabaseMigrator;
import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.ResultRow;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceIntegrationTest {

    private DatabaseDriver driver;
    private CategoriaDao categoriaDao;
    private LancamentoDao lancamentoDao;
    private TagDao tagDao;
    private PlanejamentoDao planejamentoDao;
    private LancamentoRecorrenteDao recorrenteDao;
    private MetaDao metaDao;
    private AporteMetaDao aporteMetaDao;

    @BeforeEach
    void setUp() {
        driver = InMemoryJdbcDriver.create();
        new DatabaseMigrator(driver).migrate();

        categoriaDao      = new CategoriaDao(driver);
        lancamentoDao     = new LancamentoDao(driver);
        tagDao            = new TagDao(driver);
        planejamentoDao   = new PlanejamentoDao(driver);
        recorrenteDao     = new LancamentoRecorrenteDao(driver);
        metaDao           = new MetaDao(driver);
        aporteMetaDao     = new AporteMetaDao(driver);
    }

    // ─── Onboarding ───────────────────────────────────────────────────────────

    @Test
    void migrate_insertsOnboardingCategories() {
        List<Categoria> raizes = categoriaDao.listarRaizes();
        assertEquals(3, raizes.size(), "Deve ter 3 categorias raiz de onboarding");

        long totalFilhas = raizes.stream()
                .mapToLong(r -> categoriaDao.listarFilhas(r.getId()).size())
                .sum();
        assertEquals(9, totalFilhas, "Deve ter 9 subcategorias no total");
    }

    // ─── Categoria CRUD ───────────────────────────────────────────────────────

    @Test
    void categoria_saveAndRetrieve() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Teste")
                .tipo(TipoCategoria.ESSENCIAL)
                .ordem(99)
                .build();
        categoriaDao.salvar(cat);

        Optional<Categoria> found = categoriaDao.buscarPorId(cat.getId());
        assertTrue(found.isPresent());
        assertEquals("Teste", found.get().getNome());
        assertEquals(TipoCategoria.ESSENCIAL, found.get().getTipo());
    }

    @Test
    void categoria_update() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Original")
                .tipo(TipoCategoria.NAO_ESSENCIAL)
                .build();
        categoriaDao.salvar(cat);

        cat.setNome("Atualizado");
        categoriaDao.atualizar(cat);

        assertEquals("Atualizado", categoriaDao.buscarPorId(cat.getId()).get().getNome());
    }

    @Test
    void categoria_delete() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Temp")
                .tipo(TipoCategoria.IMPREVISTO)
                .build();
        categoriaDao.salvar(cat);
        categoriaDao.deletar(cat.getId());

        assertTrue(categoriaDao.buscarPorId(cat.getId()).isEmpty());
    }

    // ─── Lançamento CRUD ──────────────────────────────────────────────────────

    @Test
    void lancamento_saveAndRetrieve() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat")
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        categoriaDao.salvar(cat);

        Lancamento l = Lancamento.builder(UUID.randomUUID().toString())
                .valor(100.0)
                .tipo(TipoLancamento.DESPESA)
                .data("2026-05-01")
                .descricao("Aluguel")
                .categoriaId(cat.getId())
                .build();
        lancamentoDao.salvar(l);

        Optional<Lancamento> found = lancamentoDao.buscarPorId(l.getId());
        assertTrue(found.isPresent());
        assertEquals(100.0, found.get().getValor());
        assertEquals("Aluguel", found.get().getDescricao());
    }

    @Test
    void lancamento_listarPorMes() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat")
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        categoriaDao.salvar(cat);

        lancamentoDao.salvar(Lancamento.builder(UUID.randomUUID().toString())
                .valor(50.0).tipo(TipoLancamento.DESPESA)
                .data("2026-05-10").descricao("A").categoriaId(cat.getId()).build());
        lancamentoDao.salvar(Lancamento.builder(UUID.randomUUID().toString())
                .valor(30.0).tipo(TipoLancamento.DESPESA)
                .data("2026-05-15").descricao("B").categoriaId(cat.getId()).build());
        lancamentoDao.salvar(Lancamento.builder(UUID.randomUUID().toString())
                .valor(20.0).tipo(TipoLancamento.DESPESA)
                .data("2026-04-01").descricao("C").categoriaId(cat.getId()).build());

        List<Lancamento> maio = lancamentoDao.listarPorMes(2026, 5);
        assertEquals(2, maio.size());
    }

    @Test
    void lancamento_somarPorTipoEMes() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat")
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        categoriaDao.salvar(cat);

        lancamentoDao.salvar(Lancamento.builder(UUID.randomUUID().toString())
                .valor(200.0).tipo(TipoLancamento.RECEITA)
                .data("2026-05-01").descricao("Salário").categoriaId(cat.getId()).build());
        lancamentoDao.salvar(Lancamento.builder(UUID.randomUUID().toString())
                .valor(80.0).tipo(TipoLancamento.DESPESA)
                .data("2026-05-05").descricao("Gasto").categoriaId(cat.getId()).build());

        double receitas = lancamentoDao.somarPorTipoEMes(TipoLancamento.RECEITA, 2026, 5);
        double despesas = lancamentoDao.somarPorTipoEMes(TipoLancamento.DESPESA, 2026, 5);

        assertEquals(200.0, receitas, 0.001);
        assertEquals(80.0, despesas, 0.001);
    }

    @Test
    void lancamento_constraintMutuamenteExclusivo() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat")
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        categoriaDao.salvar(cat);

        LancamentoRecorrente rec = new LancamentoRecorrente(
                UUID.randomUUID().toString(), "Recorrente", 50.0,
                TipoLancamento.DESPESA, cat.getId(), TipoRecorrencia.MENSAL, 5, true,
                System.currentTimeMillis());
        recorrenteDao.salvar(rec);

        // Deve salvar corretamente lançamento vinculado a recorrente
        assertDoesNotThrow(() -> lancamentoDao.salvar(
                Lancamento.builder(UUID.randomUUID().toString())
                        .valor(50.0).tipo(TipoLancamento.DESPESA)
                        .data("2026-05-01").descricao("Recorrente gerado")
                        .categoriaId(cat.getId())
                        .recorrenteId(rec.getId())
                        .build()
        ));
    }

    // ─── Tag CRUD + Vínculo ───────────────────────────────────────────────────

    @Test
    void tag_saveAndLink() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat")
                .tipo(TipoCategoria.ESSENCIAL)
                .build();
        categoriaDao.salvar(cat);

        Tag tag = new Tag(UUID.randomUUID().toString(), "viagem", System.currentTimeMillis());
        tagDao.salvar(tag);

        Lancamento l = Lancamento.builder(UUID.randomUUID().toString())
                .valor(300.0).tipo(TipoLancamento.DESPESA)
                .data("2026-05-20").descricao("Hotel").categoriaId(cat.getId()).build();
        lancamentoDao.salvar(l);

        tagDao.vincularLancamento(l.getId(), tag.getId());

        List<Tag> tags = tagDao.listarPorLancamento(l.getId());
        assertEquals(1, tags.size());
        assertEquals("viagem", tags.get(0).getNome());
    }

    @Test
    void tag_buscarPorNomeCaseInsensitive() {
        tagDao.salvar(new Tag(UUID.randomUUID().toString(), "Restaurante", System.currentTimeMillis()));
        assertTrue(tagDao.buscarPorNome("restaurante").isPresent());
        assertTrue(tagDao.buscarPorNome("RESTAURANTE").isPresent());
    }

    // ─── Planejamento CRUD ────────────────────────────────────────────────────

    @Test
    void planejamento_saveAndRetrieve() {
        PlanejamentoMensal plano = new PlanejamentoMensal(
                UUID.randomUUID().toString(), 2026, 5, 5000.0, 300.0, false, false,
                System.currentTimeMillis());
        planejamentoDao.salvar(plano);

        Optional<PlanejamentoMensal> found = planejamentoDao.buscarPorMes(2026, 5);
        assertTrue(found.isPresent());
        assertEquals(5000.0, found.get().getReceitaEsperada(), 0.001);
    }

    @Test
    void planejamento_unicidadeAnoMes() {
        PlanejamentoMensal p1 = new PlanejamentoMensal(UUID.randomUUID().toString(), 2026, 6,
                4000.0, 200.0, false, false, System.currentTimeMillis());
        planejamentoDao.salvar(p1);

        PlanejamentoMensal p2 = new PlanejamentoMensal(UUID.randomUUID().toString(), 2026, 6,
                4500.0, 200.0, false, false, System.currentTimeMillis());
        assertThrows(RuntimeException.class, () -> planejamentoDao.salvar(p2));
    }

    @Test
    void planejamentoCategoria_salvarEListar() {
        PlanejamentoMensal plano = new PlanejamentoMensal(UUID.randomUUID().toString(), 2026, 7,
                3000.0, 100.0, false, false, System.currentTimeMillis());
        planejamentoDao.salvar(plano);

        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Moradia").tipo(TipoCategoria.ESSENCIAL).build();
        categoriaDao.salvar(cat);

        PlanejamentoCategoria item = new PlanejamentoCategoria(
                UUID.randomUUID().toString(), plano.getId(), cat.getId(), 1500.0);
        planejamentoDao.salvarItemCategoria(item);

        List<PlanejamentoCategoria> items = planejamentoDao.listarItensPorPlano(plano.getId());
        assertEquals(1, items.size());
        assertEquals(1500.0, items.get(0).getLimite(), 0.001);
    }

    // ─── Meta + Aporte CRUD ───────────────────────────────────────────────────

    @Test
    void meta_saveAndRetrieve() {
        Meta m = new Meta(UUID.randomUUID().toString(), "Carro", 30000.0, 0.0,
                "2028-12-31", "Meta do carro", true, System.currentTimeMillis());
        metaDao.salvar(m);

        Optional<Meta> found = metaDao.buscarPorId(m.getId());
        assertTrue(found.isPresent());
        assertEquals("Carro", found.get().getNome());
    }

    @Test
    void aporteMeta_saveAndSum() {
        Meta m = new Meta(UUID.randomUUID().toString(), "Viagem", 5000.0, 0.0,
                null, null, true, System.currentTimeMillis());
        metaDao.salvar(m);

        aporteMetaDao.salvar(new AporteMeta(UUID.randomUUID().toString(), m.getId(), 500.0,
                "2026-01-15", "Aporte 1", System.currentTimeMillis()));
        aporteMetaDao.salvar(new AporteMeta(UUID.randomUUID().toString(), m.getId(), 300.0,
                "2026-02-15", "Aporte 2", System.currentTimeMillis()));

        double total = aporteMetaDao.somarPorMeta(m.getId());
        assertEquals(800.0, total, 0.001);

        List<AporteMeta> aportes = aporteMetaDao.listarPorMeta(m.getId());
        assertEquals(2, aportes.size());
    }

    // ─── LancamentoRecorrente CRUD ────────────────────────────────────────────

    @Test
    void recorrente_saveAndList() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat").tipo(TipoCategoria.ESSENCIAL).build();
        categoriaDao.salvar(cat);

        LancamentoRecorrente rec = new LancamentoRecorrente(
                UUID.randomUUID().toString(), "Netflix", 45.0,
                TipoLancamento.DESPESA, cat.getId(), TipoRecorrencia.MENSAL, 10, true,
                System.currentTimeMillis());
        recorrenteDao.salvar(rec);

        List<LancamentoRecorrente> ativos = recorrenteDao.listarAtivos();
        assertEquals(1, ativos.size());
        assertEquals("Netflix", ativos.get(0).getDescricao());
    }

    @Test
    void recorrente_desativar() {
        Categoria cat = Categoria.builder(UUID.randomUUID().toString())
                .nome("Cat").tipo(TipoCategoria.ESSENCIAL).build();
        categoriaDao.salvar(cat);

        LancamentoRecorrente rec = new LancamentoRecorrente(
                UUID.randomUUID().toString(), "Academia", 80.0,
                TipoLancamento.DESPESA, cat.getId(), TipoRecorrencia.MENSAL, 5, true,
                System.currentTimeMillis());
        recorrenteDao.salvar(rec);
        recorrenteDao.desativar(rec.getId());

        assertTrue(recorrenteDao.listarAtivos().isEmpty());
    }

    // ─── Driver em memória (inline para não depender do Desktop) ─────────────

    /**
     * Cria um JdbcDatabaseDriver apontando para :memory: sem depender do módulo desktop.
     * Usa reflection do driver JDBC via String de conexão direta.
     */
    static DatabaseDriver InMemoryJdbcDriver_create() {
        return new InMemoryJdbcDriver();
    }

    // Inner class para evitar dependência circular — replica JdbcDatabaseDriver com :memory:
    private static class InMemoryJdbcDriver implements DatabaseDriver {

        private final java.sql.Connection connection;

        private InMemoryJdbcDriver() {
            try {
                Class.forName("org.sqlite.JDBC");
                this.connection = java.sql.DriverManager.getConnection("jdbc:sqlite::memory:");
                this.connection.setAutoCommit(true);
            } catch (Exception e) {
                throw new RuntimeException("Não foi possível criar banco em memória", e);
            }
        }

        static InMemoryJdbcDriver create() { return new InMemoryJdbcDriver(); }

        @Override public void execute(String sql) {
            try (java.sql.PreparedStatement s = connection.prepareStatement(sql)) { s.execute(); }
            catch (java.sql.SQLException e) { throw new RuntimeException(sql, e); }
        }

        @Override public void execute(String sql, Object... args) {
            try (java.sql.PreparedStatement s = connection.prepareStatement(sql)) {
                bindArgs(s, args); s.execute();
            } catch (java.sql.SQLException e) { throw new RuntimeException(sql, e); }
        }

        @Override public <T> List<T> query(String sql, RowMapper<T> mapper, Object... args) {
            try (java.sql.PreparedStatement s = connection.prepareStatement(sql)) {
                bindArgs(s, args);
                try (java.sql.ResultSet rs = s.executeQuery()) {
                    List<T> list = new java.util.ArrayList<>();
                    while (rs.next()) list.add(mapper.map(new RsRow(rs)));
                    return list;
                }
            } catch (java.sql.SQLException e) { throw new RuntimeException(sql, e); }
        }

        @Override public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... args) {
            List<T> r = query(sql, mapper, args);
            return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
        }

        @Override public void beginTransaction() {
            try { connection.setAutoCommit(false); } catch (java.sql.SQLException e) { throw new RuntimeException(e); }
        }

        @Override public void commitTransaction() {
            try { connection.commit(); connection.setAutoCommit(true); } catch (java.sql.SQLException e) { throw new RuntimeException(e); }
        }

        @Override public void rollbackTransaction() {
            try { connection.rollback(); connection.setAutoCommit(true); } catch (java.sql.SQLException e) { throw new RuntimeException(e); }
        }

        @Override public int getSchemaVersion() {
            try (java.sql.PreparedStatement s = connection.prepareStatement(
                    "SELECT version FROM schema_version ORDER BY version DESC LIMIT 1");
                 java.sql.ResultSet rs = s.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            } catch (java.sql.SQLException e) { return 0; }
        }

        @Override public void setSchemaVersion(int version) {
            execute("INSERT OR REPLACE INTO schema_version(version, aplicado_em) VALUES (?,?)",
                    version, System.currentTimeMillis());
        }

        private void bindArgs(java.sql.PreparedStatement s, Object... args) throws java.sql.SQLException {
            for (int i = 0; i < args.length; i++) {
                Object a = args[i];
                if (a == null)              s.setNull(i+1, java.sql.Types.NULL);
                else if (a instanceof String)  s.setString(i+1, (String)a);
                else if (a instanceof Integer) s.setInt(i+1, (Integer)a);
                else if (a instanceof Long)    s.setLong(i+1, (Long)a);
                else if (a instanceof Double)  s.setDouble(i+1, (Double)a);
                else if (a instanceof Boolean) s.setInt(i+1, (Boolean)a ? 1 : 0);
                else s.setString(i+1, a.toString());
            }
        }

        private static class RsRow implements ResultRow {
            private final java.sql.ResultSet rs;
            RsRow(java.sql.ResultSet rs) { this.rs = rs; }
            @Override public String getString(String c) { try { return rs.getString(c); } catch (java.sql.SQLException e) { throw new RuntimeException(e); } }
            @Override public int getInt(String c) { try { return rs.getInt(c); } catch (java.sql.SQLException e) { throw new RuntimeException(e); } }
            @Override public long getLong(String c) { try { return rs.getLong(c); } catch (java.sql.SQLException e) { throw new RuntimeException(e); } }
            @Override public double getDouble(String c) { try { return rs.getDouble(c); } catch (java.sql.SQLException e) { throw new RuntimeException(e); } }
            @Override public boolean isNull(String c) { try { rs.getObject(c); return rs.wasNull(); } catch (java.sql.SQLException e) { throw new RuntimeException(e); } }
        }
    }
}
