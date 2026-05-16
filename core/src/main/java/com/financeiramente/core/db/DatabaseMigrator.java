package com.financeiramente.core.db;

import java.util.UUID;

/**
 * Aplica migrações do schema SQLite em cadeia até atingir SchemaVersion.CURRENT.
 * Na primeira execução (version == 0): cria schema + insere categorias padrão de onboarding.
 */
public class DatabaseMigrator {

    private static final String CREATE_SCHEMA_VERSION =
            "CREATE TABLE IF NOT EXISTS schema_version (" +
            "    version     INTEGER PRIMARY KEY," +
            "    aplicado_em INTEGER NOT NULL" +
            ")";

    private static final String CREATE_CATEGORIA =
            "CREATE TABLE IF NOT EXISTS categoria (" +
            "    id            TEXT PRIMARY KEY," +
            "    nome          TEXT NOT NULL," +
            "    pai_id        TEXT REFERENCES categoria(id) ON DELETE RESTRICT," +
            "    tipo          TEXT NOT NULL CHECK(tipo IN ('essencial','nao_essencial','imprevisto','provisao'))," +
            "    limite_mensal REAL," +
            "    ordem         INTEGER NOT NULL DEFAULT 0," +
            "    criado_em     INTEGER NOT NULL" +
            ")";

    private static final String CREATE_LANCAMENTO_RECORRENTE =
            "CREATE TABLE IF NOT EXISTS lancamento_recorrente (" +
            "    id              TEXT PRIMARY KEY," +
            "    descricao       TEXT NOT NULL," +
            "    valor           REAL NOT NULL CHECK(valor > 0)," +
            "    tipo            TEXT NOT NULL CHECK(tipo IN ('receita','despesa'))," +
            "    categoria_id    TEXT NOT NULL REFERENCES categoria(id)," +
            "    recorrencia     TEXT NOT NULL CHECK(recorrencia IN ('diaria','semanal','mensal','anual'))," +
            "    dia_recorrencia INTEGER," +
            "    ativo           INTEGER NOT NULL DEFAULT 1 CHECK(ativo IN (0,1))," +
            "    criado_em       INTEGER NOT NULL" +
            ")";

    private static final String CREATE_PROVISAO =
            "CREATE TABLE IF NOT EXISTS provisao (" +
            "    id               TEXT PRIMARY KEY," +
            "    nome             TEXT NOT NULL," +
            "    total_anual      REAL NOT NULL CHECK(total_anual > 0)," +
            "    valor_mensal     REAL NOT NULL CHECK(valor_mensal > 0)," +
            "    saldo_acumulado  REAL NOT NULL DEFAULT 0 CHECK(saldo_acumulado >= 0)," +
            "    categoria_id     TEXT REFERENCES categoria(id)," +
            "    ativo            INTEGER NOT NULL DEFAULT 1 CHECK(ativo IN (0,1))," +
            "    criado_em        INTEGER NOT NULL" +
            ")";

    private static final String CREATE_LANCAMENTO =
            "CREATE TABLE IF NOT EXISTS lancamento (" +
            "    id             TEXT PRIMARY KEY," +
            "    valor          REAL NOT NULL CHECK(valor > 0)," +
            "    tipo           TEXT NOT NULL CHECK(tipo IN ('receita','despesa'))," +
            "    data           TEXT NOT NULL," +
            "    descricao      TEXT NOT NULL CHECK(length(trim(descricao)) > 0)," +
            "    categoria_id   TEXT NOT NULL REFERENCES categoria(id)," +
            "    recorrente_id  TEXT REFERENCES lancamento_recorrente(id)," +
            "    provisao_id    TEXT REFERENCES provisao(id)," +
            "    criado_em      INTEGER NOT NULL," +
            "    atualizado_em  INTEGER NOT NULL," +
            "    CHECK(NOT (recorrente_id IS NOT NULL AND provisao_id IS NOT NULL))" +
            ")";

    private static final String CREATE_IDX_LANCAMENTO_DATA =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_data ON lancamento(data)";
    private static final String CREATE_IDX_LANCAMENTO_CATEGORIA =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_categoria ON lancamento(categoria_id)";
    private static final String CREATE_IDX_LANCAMENTO_ANO_MES =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_ano_mes ON lancamento(substr(data,1,7))";

    private static final String CREATE_TAG =
            "CREATE TABLE IF NOT EXISTS tag (" +
            "    id        TEXT PRIMARY KEY," +
            "    nome      TEXT NOT NULL UNIQUE COLLATE NOCASE," +
            "    criado_em INTEGER NOT NULL" +
            ")";

    private static final String CREATE_LANCAMENTO_TAG =
            "CREATE TABLE IF NOT EXISTS lancamento_tag (" +
            "    lancamento_id TEXT NOT NULL REFERENCES lancamento(id) ON DELETE CASCADE," +
            "    tag_id        TEXT NOT NULL REFERENCES tag(id)," +
            "    PRIMARY KEY (lancamento_id, tag_id)" +
            ")";

    private static final String CREATE_PLANEJAMENTO_MENSAL =
            "CREATE TABLE IF NOT EXISTS planejamento_mensal (" +
            "    id                 TEXT PRIMARY KEY," +
            "    ano                INTEGER NOT NULL," +
            "    mes                INTEGER NOT NULL CHECK(mes BETWEEN 1 AND 12)," +
            "    receita_esperada   REAL NOT NULL DEFAULT 0," +
            "    reserva_imprevisto REAL NOT NULL DEFAULT 0," +
            "    padrao             INTEGER NOT NULL DEFAULT 0 CHECK(padrao IN (0,1))," +
            "    confirmado         INTEGER NOT NULL DEFAULT 0 CHECK(confirmado IN (0,1))," +
            "    criado_em          INTEGER NOT NULL," +
            "    UNIQUE(ano, mes)" +
            ")";

    private static final String CREATE_PLANEJAMENTO_CATEGORIA =
            "CREATE TABLE IF NOT EXISTS planejamento_categoria (" +
            "    id              TEXT PRIMARY KEY," +
            "    planejamento_id TEXT NOT NULL REFERENCES planejamento_mensal(id) ON DELETE CASCADE," +
            "    categoria_id    TEXT NOT NULL REFERENCES categoria(id)," +
            "    limite          REAL NOT NULL DEFAULT 0," +
            "    UNIQUE(planejamento_id, categoria_id)" +
            ")";

    private static final String CREATE_META =
            "CREATE TABLE IF NOT EXISTS meta (" +
            "    id             TEXT PRIMARY KEY," +
            "    nome           TEXT NOT NULL," +
            "    valor_objetivo REAL NOT NULL CHECK(valor_objetivo > 0)," +
            "    valor_atual    REAL NOT NULL DEFAULT 0 CHECK(valor_atual >= 0)," +
            "    data_alvo      TEXT," +
            "    descricao      TEXT," +
            "    ativo          INTEGER NOT NULL DEFAULT 1 CHECK(ativo IN (0,1))," +
            "    criado_em      INTEGER NOT NULL" +
            ")";

    private static final String CREATE_APORTE_META =
            "CREATE TABLE IF NOT EXISTS aporte_meta (" +
            "    id        TEXT PRIMARY KEY," +
            "    meta_id   TEXT NOT NULL REFERENCES meta(id) ON DELETE CASCADE," +
            "    valor     REAL NOT NULL CHECK(valor > 0)," +
            "    data      TEXT NOT NULL," +
            "    descricao TEXT," +
            "    criado_em INTEGER NOT NULL" +
            ")";

    private final DatabaseDriver driver;

    public DatabaseMigrator(DatabaseDriver driver) {
        this.driver = driver;
    }

    /**
     * Aplica todas as migrações pendentes até SchemaVersion.CURRENT.
     */
    public void migrate() {
        driver.execute("PRAGMA journal_mode = WAL");
        driver.execute("PRAGMA foreign_keys = ON");

        driver.execute(CREATE_SCHEMA_VERSION);

        int currentVersion = driver.getSchemaVersion();

        if (currentVersion < 1) {
            applyMigration1();
            driver.setSchemaVersion(1);
        }
        // Futuras: if (currentVersion < 2) { applyMigration2(); driver.setSchemaVersion(2); }
    }

    private void applyMigration1() {
        driver.execute(CREATE_CATEGORIA);
        driver.execute(CREATE_LANCAMENTO_RECORRENTE);
        driver.execute(CREATE_PROVISAO);
        driver.execute(CREATE_LANCAMENTO);
        driver.execute(CREATE_IDX_LANCAMENTO_DATA);
        driver.execute(CREATE_IDX_LANCAMENTO_CATEGORIA);
        driver.execute(CREATE_IDX_LANCAMENTO_ANO_MES);
        driver.execute(CREATE_TAG);
        driver.execute(CREATE_LANCAMENTO_TAG);
        driver.execute(CREATE_PLANEJAMENTO_MENSAL);
        driver.execute(CREATE_PLANEJAMENTO_CATEGORIA);
        driver.execute(CREATE_META);
        driver.execute(CREATE_APORTE_META);

        insertOnboardingData();
    }

    private void insertOnboardingData() {
        long now = System.currentTimeMillis();

        // Raízes
        String essenciaisId   = UUID.randomUUID().toString();
        String naoEssenciaisId = UUID.randomUUID().toString();
        String imprevistoId   = UUID.randomUUID().toString();
        String provisoesId    = UUID.randomUUID().toString();

        insertCategoria(essenciaisId,   "Gastos Essenciais",    null,           "essencial",    0,  now);
        insertCategoria(naoEssenciaisId,"Gastos Não Essenciais",null,           "nao_essencial",1,  now);
        insertCategoria(imprevistoId,   "Imprevistos",          null,           "imprevisto",   2,  now);
        insertCategoria(provisoesId,    "Provisões",            null,           "provisao",     3,  now);

        // Filhas de Gastos Essenciais
        insertCategoria(UUID.randomUUID().toString(), "Moradia",          essenciaisId, "essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Transporte",       essenciaisId, "essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Alimentação",      essenciaisId, "essencial", 2, now);
        insertCategoria(UUID.randomUUID().toString(), "Saúde & Bem-estar",essenciaisId, "essencial", 3, now);
        insertCategoria(UUID.randomUUID().toString(), "Estudos",          essenciaisId, "essencial", 4, now);
        insertCategoria(UUID.randomUUID().toString(), "Doações",          essenciaisId, "essencial", 5, now);

        // Filhas de Gastos Não Essenciais
        insertCategoria(UUID.randomUUID().toString(), "Lazer",          naoEssenciaisId, "nao_essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Presentes",      naoEssenciaisId, "nao_essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Compras & Luxo", naoEssenciaisId, "nao_essencial", 2, now);
    }

    private void insertCategoria(String id, String nome, String paiId, String tipo, int ordem, long now) {
        driver.execute(
            "INSERT OR IGNORE INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em) VALUES (?,?,?,?,NULL,?,?)",
            id, nome, paiId, tipo, ordem, now
        );
    }
}
