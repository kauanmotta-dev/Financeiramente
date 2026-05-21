package com.financeiramente.core.db;

import java.util.UUID;

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
            "    tipo          TEXT NOT NULL CHECK(tipo IN ('essencial','nao_essencial','receita'))," +
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

    private static final String CREATE_LANCAMENTO =
            "CREATE TABLE IF NOT EXISTS lancamento (" +
            "    id             TEXT PRIMARY KEY," +
            "    valor          REAL NOT NULL CHECK(valor > 0)," +
            "    tipo           TEXT NOT NULL CHECK(tipo IN ('receita','despesa'))," +
            "    data           TEXT NOT NULL," +
            "    descricao      TEXT NOT NULL CHECK(length(trim(descricao)) > 0)," +
            "    categoria_id   TEXT NOT NULL REFERENCES categoria(id)," +
            "    recorrente_id  TEXT REFERENCES lancamento_recorrente(id)," +
            "    criado_em      INTEGER NOT NULL," +
            "    atualizado_em  INTEGER NOT NULL" +
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

    public void migrate() {
        driver.execute("PRAGMA journal_mode = WAL");
        driver.execute("PRAGMA foreign_keys = ON");

        driver.execute(CREATE_SCHEMA_VERSION);

        int currentVersion = driver.getSchemaVersion();

        if (currentVersion < 1) {
            applyMigration1();
            driver.setSchemaVersion(1);
        }

        if (currentVersion < 2) {
            applyMigration2();
            driver.setSchemaVersion(2);
        }
    }
    
    private void applyMigration2() {
        driver.execute("ALTER TABLE categoria ADD COLUMN icone TEXT DEFAULT '\uD83D\uDCE6'");
        driver.execute("ALTER TABLE categoria ADD COLUMN cor TEXT DEFAULT '#6366F1'");
    }

    private void applyMigration1() {
        driver.execute(CREATE_CATEGORIA);
        driver.execute(CREATE_LANCAMENTO_RECORRENTE);
        driver.execute(CREATE_LANCAMENTO);
        driver.execute(CREATE_IDX_LANCAMENTO_DATA);
        driver.execute(CREATE_IDX_LANCAMENTO_CATEGORIA);
        driver.execute(CREATE_IDX_LANCAMENTO_ANO_MES);
        driver.execute(CREATE_TAG);
        driver.execute(CREATE_LANCAMENTO_TAG);
        driver.execute(CREATE_META);
        driver.execute(CREATE_APORTE_META);

        insertOnboardingData();
    }

    private void insertOnboardingData() {
        long now = System.currentTimeMillis();

        // ─── Cat. Essenciais ────────────────────────────────────────────────────
        String moradiaId      = UUID.randomUUID().toString();
        String transporteId   = UUID.randomUUID().toString();
        String alimentacaoId  = UUID.randomUUID().toString();
        String saudeId        = UUID.randomUUID().toString();
        String estudosId      = UUID.randomUUID().toString();
        String imprevistoId   = UUID.randomUUID().toString();

        insertCategoria(moradiaId, "Moradia",           null, "essencial", 0, now);
        insertCategoria(transporteId, "Transporte",        null, "essencial", 1, now);
        insertCategoria(alimentacaoId, "Alimentação",       null, "essencial", 2, now);
        insertCategoria(saudeId, "Saúde",             null, "essencial", 3, now);
        insertCategoria(estudosId, "Estudos",           null, "essencial", 4, now);
        insertCategoria(imprevistoId, "Imprevistos",       null, "essencial", 5, now);

        // Moradia → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Aluguel",              moradiaId, "essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Condomínio",           moradiaId, "essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Luz",                  moradiaId, "essencial", 2, now);
        insertCategoria(UUID.randomUUID().toString(), "Água",                 moradiaId, "essencial", 3, now);
        insertCategoria(UUID.randomUUID().toString(), "Internet",             moradiaId, "essencial", 4, now);

        // Transporte → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Combustível",          transporteId, "essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Aplicativo",           transporteId, "essencial", 1, now);

        // Alimentação → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Supermercado",         alimentacaoId, "essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Restaurante",          alimentacaoId, "essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Delivery",             alimentacaoId, "essencial", 2, now);

        // Saúde → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Plano de Saúde",       saudeId, "essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Farmácia",             saudeId, "essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Academia",             saudeId, "essencial", 2, now);

        // Estudos → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Cursos",               estudosId, "essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Materiais",            estudosId, "essencial", 1, now);

        // Imprevistos → subcategoria
        insertCategoria(UUID.randomUUID().toString(), "Imprevistos",          imprevistoId, "essencial", 0, now);

        // ─── Cat. Não Essenciais ────────────────────────────────────────────────
        String lazerId     = UUID.randomUUID().toString();
        String comprasId   = UUID.randomUUID().toString();
        String doacoesId   = UUID.randomUUID().toString();
        String imprevistosId = UUID.randomUUID().toString();

        insertCategoria(lazerId,     "Lazer",          null, "nao_essencial", 6, now);
        insertCategoria(comprasId,   "Compras & Luxo", null, "nao_essencial", 7, now);
        insertCategoria(doacoesId,   "Doações",        null, "nao_essencial", 8, now);
        insertCategoria(imprevistosId, "Imprevistos",      null, "nao_essencial", 9, now);

        // Lazer → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Streaming",           lazerId, "nao_essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Hobby",               lazerId, "nao_essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Eventos",             lazerId, "nao_essencial", 2, now);

        // Compras & Luxo → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Roupas",              comprasId, "nao_essencial", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Eletrônicos",         comprasId, "nao_essencial", 1, now);
        insertCategoria(UUID.randomUUID().toString(), "Outros",              comprasId, "nao_essencial", 2, now);
        
        // Doações → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Igreja",              doacoesId, "nao_essencial", 0, now);

        // Imprevistos → subcategoria
        insertCategoria(UUID.randomUUID().toString(), "Imprevistos",         imprevistosId, "nao_essencial", 0, now);
        
        // ─── Cat. Receita ────────────────────────────────────────────────────────
        String salarioId    = UUID.randomUUID().toString();
        String rendaExtraId = UUID.randomUUID().toString();

        insertCategoria(salarioId,    "Salário",     null, "receita", 10, now);
        insertCategoria(rendaExtraId, "Renda Extra", null, "receita", 11, now);

        // Salário → subcategoria
        insertCategoria(UUID.randomUUID().toString(), "Salário CLT",         salarioId,    "receita", 0, now);

        // Renda Extra → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Freelance",           rendaExtraId, "receita", 0, now);
        insertCategoria(UUID.randomUUID().toString(), "Outros",              rendaExtraId, "receita", 1, now);
    }

    private void insertCategoria(String id, String nome, String paiId, String tipo, int ordem, long now) {
        driver.execute(
            "INSERT OR IGNORE INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em) VALUES (?,?,?,?,NULL,?,?)",
            id, nome, paiId, tipo, ordem, now
        );
    }
}
