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
            "    tipo          TEXT NOT NULL CHECK(tipo IN ('essencial','nao_essencial','receita','sem_tipo'))," +
            "    limite_mensal REAL," +
            "    ordem         INTEGER NOT NULL DEFAULT 0," +
            "    criado_em     INTEGER NOT NULL," +
            "    icone         TEXT DEFAULT '\uD83D\uDCE6'," +
            "    cor           TEXT DEFAULT '#6366F1'" +
            ")";

    private static final String CREATE_LANCAMENTO =
            "CREATE TABLE IF NOT EXISTS lancamento (" +
            "    id             TEXT PRIMARY KEY," +
            "    valor          REAL NOT NULL CHECK(valor > 0)," +
            "    tipo           TEXT NOT NULL CHECK(tipo IN ('receita','despesa'))," +
            "    data           TEXT NOT NULL," +
            "    descricao      TEXT," +
            "    categoria_id   TEXT REFERENCES categoria(id)," +
            "    fatura_id      TEXT REFERENCES fatura(id)," +
            "    criado_em      INTEGER NOT NULL," +
            "    atualizado_em  INTEGER NOT NULL" +
            ")";

    private static final String CREATE_CARTAO_CREDITO =
            "CREATE TABLE IF NOT EXISTS cartao_credito (" +
            "    id              TEXT PRIMARY KEY," +
            "    nome            TEXT NOT NULL," +
            "    dia_vencimento  INTEGER NOT NULL CHECK(dia_vencimento BETWEEN 1 AND 28)," +
            "    dias_fechamento INTEGER NOT NULL DEFAULT 10 CHECK(dias_fechamento BETWEEN 1 AND 27)," +
            "    limite          REAL CHECK(limite > 0)," +
            "    bandeira        TEXT," +
            "    icone           TEXT NOT NULL DEFAULT '\uD83D\uDCB3'," +
            "    cor             TEXT NOT NULL DEFAULT '#6366F1'," +
            "    ativo           INTEGER NOT NULL DEFAULT 1 CHECK(ativo IN (0,1))," +
            "    criado_em       INTEGER NOT NULL," +
            "    atualizado_em   INTEGER NOT NULL" +
            ")";

    private static final String CREATE_FATURA =
            "CREATE TABLE IF NOT EXISTS fatura (" +
            "    id              TEXT PRIMARY KEY," +
            "    cartao_id       TEXT NOT NULL REFERENCES cartao_credito(id) ON DELETE RESTRICT," +
            "    mes             TEXT NOT NULL," +
            "    data_fechamento TEXT NOT NULL," +
            "    data_vencimento TEXT NOT NULL," +
            "    valor_pago      REAL NOT NULL DEFAULT 0.0 CHECK(valor_pago >= 0)," +
            "    status          TEXT NOT NULL DEFAULT 'aberto' CHECK(status IN ('aberto','fechado','pago','pago_parcial'))," +
            "    descricao       TEXT," +
            "    criado_em       INTEGER NOT NULL," +
            "    atualizado_em   INTEGER NOT NULL," +
            "    UNIQUE(cartao_id, mes)" +
            ")";

    private static final String CREATE_COMPRA_CARTAO =
            "CREATE TABLE IF NOT EXISTS compra_cartao (" +
            "    id               TEXT PRIMARY KEY," +
            "    cartao_id        TEXT NOT NULL REFERENCES cartao_credito(id) ON DELETE RESTRICT," +
            "    descricao        TEXT NOT NULL," +
            "    valor_total      REAL NOT NULL CHECK(valor_total > 0)," +
            "    tipo             TEXT NOT NULL CHECK(tipo IN ('credito','parcelado','recorrente'))," +
            "    total_parcelas   INTEGER CHECK(total_parcelas BETWEEN 2 AND 480)," +
            "    categoria_id     TEXT REFERENCES categoria(id)," +
            "    data_compra      TEXT NOT NULL," +
            "    dia_recorrencia  INTEGER CHECK(dia_recorrencia BETWEEN 1 AND 28)," +
            "    ativo            INTEGER NOT NULL DEFAULT 1 CHECK(ativo IN (0,1))," +
            "    criado_em        INTEGER NOT NULL," +
            "    atualizado_em    INTEGER NOT NULL" +
            ")";

    private static final String CREATE_IDX_LANCAMENTO_DATA =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_data ON lancamento(data)";
    private static final String CREATE_IDX_LANCAMENTO_CATEGORIA =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_categoria ON lancamento(categoria_id)";
    private static final String CREATE_IDX_LANCAMENTO_ANO_MES =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_ano_mes ON lancamento(substr(data,1,7))";
    private static final String CREATE_IDX_LANCAMENTO_COMPRA_CARTAO =
            "CREATE INDEX IF NOT EXISTS idx_lancamento_compra_cartao ON lancamento(compra_cartao_id)";
    private static final String CREATE_IDX_COMPRA_CARTAO_CARTAO =
            "CREATE INDEX IF NOT EXISTS idx_compra_cartao_cartao ON compra_cartao(cartao_id)";
    private static final String CREATE_IDX_COMPRA_CARTAO_TIPO_ATIVO =
            "CREATE INDEX IF NOT EXISTS idx_compra_cartao_tipo_ativo ON compra_cartao(tipo, ativo)";

    private static final String CREATE_TAG =
            "CREATE TABLE IF NOT EXISTS tag (" +
            "    id        TEXT PRIMARY KEY," +
            "    nome      TEXT NOT NULL UNIQUE COLLATE NOCASE," +
            "    emoji     TEXT NOT NULL DEFAULT '🏷️'," +
            "    cor       TEXT NOT NULL DEFAULT '#6366F1'," +
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
            "    valor_inicial  REAL NOT NULL DEFAULT 0 CHECK(valor_inicial >= 0)," +
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
    private final AppLogger logger;

    public DatabaseMigrator(DatabaseDriver driver, AppLogger logger) {
        this.driver = driver;
        this.logger = logger;
    }

    public void migrate() {
        logger.info("Iniciando migracao do schema do banco.");
        driver.execute("PRAGMA journal_mode = WAL");
        driver.execute("PRAGMA foreign_keys = ON");

        driver.execute(CREATE_SCHEMA_VERSION);

        int currentVersion = driver.getSchemaVersion();

        if (currentVersion < 1) {
            logger.info("Aplicando migracao de schema versao 1.");
            applyMigration1();
            driver.setSchemaVersion(1);
            currentVersion = 1;
        }

        if (currentVersion < 2) {
            logger.info("Aplicando migracao de schema versao 2.");
            applyMigration2();
            driver.setSchemaVersion(2);
            currentVersion = 2;
        }

        if (currentVersion < 3) {
            logger.info("Aplicando migracao de schema versao 3.");
            applyMigration3();
            driver.setSchemaVersion(3);
            currentVersion = 3;
        }

        logger.info("Migracao do schema concluida na versao " + currentVersion + ".");
    }

    private void applyMigration1() {
        driver.execute(CREATE_CATEGORIA);
        driver.execute(CREATE_LANCAMENTO);
        driver.execute(CREATE_IDX_LANCAMENTO_DATA);
        driver.execute(CREATE_IDX_LANCAMENTO_CATEGORIA);
        driver.execute(CREATE_IDX_LANCAMENTO_ANO_MES);
        driver.execute(CREATE_CARTAO_CREDITO);
        driver.execute(CREATE_FATURA);
        driver.execute(CREATE_TAG);
        driver.execute(CREATE_LANCAMENTO_TAG);
        driver.execute(CREATE_META);
        driver.execute(CREATE_APORTE_META);

        insertOnboardingData();
    }

    private void applyMigration2() {
        driver.execute(CREATE_COMPRA_CARTAO);
        driver.execute("ALTER TABLE lancamento ADD COLUMN compra_cartao_id TEXT REFERENCES compra_cartao(id) ON DELETE CASCADE");
        driver.execute(CREATE_IDX_LANCAMENTO_COMPRA_CARTAO);
        driver.execute(CREATE_IDX_COMPRA_CARTAO_CARTAO);
        driver.execute(CREATE_IDX_COMPRA_CARTAO_TIPO_ATIVO);
    }

    private void applyMigration3() {
        long now = System.currentTimeMillis();

        String semCategoriaRaizId = buscarOuCriarSemCategoriaRaiz(now);
        String semCategoriaIdParaLancamento = buscarOuCriarSemCategoriaFilha(semCategoriaRaizId, now);

        // Preencher categoria_id nulo em lançamentos existentes com Sem Categoria.
        driver.execute(
                "UPDATE lancamento SET categoria_id = ? WHERE categoria_id IS NULL",
                semCategoriaIdParaLancamento);

        // Adicionar coluna lancamento_pagamento_id à tabela fatura
        try {
            driver.execute(
                "ALTER TABLE fatura ADD COLUMN lancamento_pagamento_id TEXT REFERENCES lancamento(id)");
        } catch (Exception ignored) {
            // Coluna já existe (segurança em caso de re-execução)
        }
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

        String moradiaIcone = "🏠";
        String moradiaCor = "#6366F1";
        String transporteIcone = "🚗";
        String transporteCor = "#0EA5E9";
        String alimentacaoIcone = "🍽️";
        String alimentacaoCor = "#F97316";
        String saudeIcone = "🩺";
        String saudeCor = "#10B981";
        String estudosIcone = "📚";
        String estudosCor = "#8B5CF6";
        String imprevistoEssencialIcone = "🚨";
        String imprevistoEssencialCor = "#64748B";

        insertCategoria(moradiaId, "Moradia", null, "essencial", 0, now, moradiaIcone, moradiaCor);
        insertCategoria(transporteId, "Transporte", null, "essencial", 1, now, transporteIcone, transporteCor);
        insertCategoria(alimentacaoId, "Alimentação", null, "essencial", 2, now, alimentacaoIcone, alimentacaoCor);
        insertCategoria(saudeId, "Saúde", null, "essencial", 3, now, saudeIcone, saudeCor);
        insertCategoria(estudosId, "Estudos", null, "essencial", 4, now, estudosIcone, estudosCor);
        insertCategoria(imprevistoId, "Imprevistos", null, "essencial", 5, now, imprevistoEssencialIcone, imprevistoEssencialCor);

        // Moradia → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Aluguel", moradiaId, "essencial", 0, now, moradiaIcone, moradiaCor);
        insertCategoria(UUID.randomUUID().toString(), "Condomínio", moradiaId, "essencial", 1, now, moradiaIcone, moradiaCor);
        insertCategoria(UUID.randomUUID().toString(), "Luz", moradiaId, "essencial", 2, now, moradiaIcone, moradiaCor);
        insertCategoria(UUID.randomUUID().toString(), "Água", moradiaId, "essencial", 3, now, moradiaIcone, moradiaCor);
        insertCategoria(UUID.randomUUID().toString(), "Internet", moradiaId, "essencial", 4, now, moradiaIcone, moradiaCor);

        // Transporte → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Combustível", transporteId, "essencial", 0, now, transporteIcone, transporteCor);
        insertCategoria(UUID.randomUUID().toString(), "Aplicativo", transporteId, "essencial", 1, now, transporteIcone, transporteCor);

        // Alimentação → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Supermercado", alimentacaoId, "essencial", 0, now, alimentacaoIcone, alimentacaoCor);
        insertCategoria(UUID.randomUUID().toString(), "Restaurante", alimentacaoId, "essencial", 1, now, alimentacaoIcone, alimentacaoCor);
        insertCategoria(UUID.randomUUID().toString(), "Delivery", alimentacaoId, "essencial", 2, now, alimentacaoIcone, alimentacaoCor);

        // Saúde → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Plano de Saúde", saudeId, "essencial", 0, now, saudeIcone, saudeCor);
        insertCategoria(UUID.randomUUID().toString(), "Farmácia", saudeId, "essencial", 1, now, saudeIcone, saudeCor);
        insertCategoria(UUID.randomUUID().toString(), "Academia", saudeId, "essencial", 2, now, saudeIcone, saudeCor);

        // Estudos → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Cursos", estudosId, "essencial", 0, now, estudosIcone, estudosCor);
        insertCategoria(UUID.randomUUID().toString(), "Materiais", estudosId, "essencial", 1, now, estudosIcone, estudosCor);

        // Imprevistos → subcategoria
        insertCategoria(UUID.randomUUID().toString(), "Imprevistos", imprevistoId, "essencial", 0, now, imprevistoEssencialIcone, imprevistoEssencialCor);

        // ─── Cat. Não Essenciais ────────────────────────────────────────────────
        String lazerId     = UUID.randomUUID().toString();
        String comprasId   = UUID.randomUUID().toString();
        String doacoesId   = UUID.randomUUID().toString();
        String imprevistosId = UUID.randomUUID().toString();

        String lazerIcone = "🎬";
        String lazerCor = "#EC4899";
        String comprasIcone = "🛍️";
        String comprasCor = "#EF4444";
        String doacoesIcone = "🤝";
        String doacoesCor = "#14B8A6";
        String imprevistoNaoEssencialIcone = "🆘";
        String imprevistoNaoEssencialCor = "#F59E0B";

        insertCategoria(lazerId, "Lazer", null, "nao_essencial", 6, now, lazerIcone, lazerCor);
        insertCategoria(comprasId, "Compras & Luxo", null, "nao_essencial", 7, now, comprasIcone, comprasCor);
        insertCategoria(doacoesId, "Doações", null, "nao_essencial", 8, now, doacoesIcone, doacoesCor);
        insertCategoria(imprevistosId, "Imprevistos", null, "nao_essencial", 9, now, imprevistoNaoEssencialIcone, imprevistoNaoEssencialCor);

        // Lazer → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Streaming", lazerId, "nao_essencial", 0, now, lazerIcone, lazerCor);
        insertCategoria(UUID.randomUUID().toString(), "Hobby", lazerId, "nao_essencial", 1, now, lazerIcone, lazerCor);
        insertCategoria(UUID.randomUUID().toString(), "Eventos", lazerId, "nao_essencial", 2, now, lazerIcone, lazerCor);

        // Compras & Luxo → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Roupas", comprasId, "nao_essencial", 0, now, comprasIcone, comprasCor);
        insertCategoria(UUID.randomUUID().toString(), "Eletrônicos", comprasId, "nao_essencial", 1, now, comprasIcone, comprasCor);
        insertCategoria(UUID.randomUUID().toString(), "Outros", comprasId, "nao_essencial", 2, now, comprasIcone, comprasCor);
        
        // Doações → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Igreja", doacoesId, "nao_essencial", 0, now, doacoesIcone, doacoesCor);

        // Imprevistos → subcategoria
        insertCategoria(UUID.randomUUID().toString(), "Imprevistos", imprevistosId, "nao_essencial", 0, now, imprevistoNaoEssencialIcone, imprevistoNaoEssencialCor);
        
        // ─── Cat. Receita ────────────────────────────────────────────────────────
        String salarioId    = UUID.randomUUID().toString();
        String rendaExtraId = UUID.randomUUID().toString();

        String salarioIcone = "💼";
        String salarioCor = "#3B82F6";
        String rendaExtraIcone = "💰";
        String rendaExtraCor = "#22C55E";

        insertCategoria(salarioId, "Salário", null, "receita", 10, now, salarioIcone, salarioCor);
        insertCategoria(rendaExtraId, "Renda Extra", null, "receita", 11, now, rendaExtraIcone, rendaExtraCor);

        // Salário → subcategoria
        insertCategoria(UUID.randomUUID().toString(), "Salário CLT", salarioId, "receita", 0, now, salarioIcone, salarioCor);

        // Renda Extra → subcategorias
        insertCategoria(UUID.randomUUID().toString(), "Freelance", rendaExtraId, "receita", 0, now, rendaExtraIcone, rendaExtraCor);
        insertCategoria(UUID.randomUUID().toString(), "Outros", rendaExtraId, "receita", 1, now, rendaExtraIcone, rendaExtraCor);

        // ─── Sem Categoria ───────────────────────────────────────────────────────
        String semCategoriaIcone = "❓";
        String semCategoriaCor = "#9CA3AF";
        String semCategoriaRaizId = UUID.randomUUID().toString();
        insertCategoria(semCategoriaRaizId, "Sem Categoria", null, "sem_tipo", 12, now, semCategoriaIcone, semCategoriaCor);
        insertCategoria(UUID.randomUUID().toString(), "Sem Categoria", semCategoriaRaizId, "sem_tipo", 0, now, semCategoriaIcone, semCategoriaCor);


        // Meta padrão inicial
        insertMeta(UUID.randomUUID().toString(), "Liberdade financeira", 1_000_000.0, now);
    }

    private void insertCategoria(String id, String nome, String paiId, String tipo, int ordem, long now,
                                 String icone, String cor) {
        driver.execute(
                "INSERT OR IGNORE INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em, icone, cor) VALUES (?,?,?,?,NULL,?,?,?,?)",
                id, nome, paiId, tipo, ordem, now, icone, cor
        );
    }

    private void insertMeta(String id, String nome, double valorObjetivo, long now) {
        driver.execute(
                "INSERT OR IGNORE INTO meta(id, nome, valor_objetivo, valor_atual, data_alvo, descricao, criado_em) VALUES (?,?,?,?,NULL,NULL,?)",
                id, nome, valorObjetivo, 0.0, now
        );
    }

    private String buscarOuCriarSemCategoriaRaiz(long now) {
        return driver.queryOne(
                "SELECT id FROM categoria WHERE nome = 'Sem Categoria' AND tipo = 'sem_tipo' AND pai_id IS NULL ORDER BY ordem LIMIT 1",
                row -> row.getString("id")
        ).orElseGet(() -> {
            String id = UUID.randomUUID().toString();
            driver.execute(
                    "INSERT INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em, icone, cor) VALUES (?, 'Sem Categoria', NULL, 'sem_tipo', NULL, 99, ?, '\u2753', '#9CA3AF')",
                    id, now
            );
            return id;
        });
    }

    private String buscarOuCriarSemCategoriaFilha(String semCategoriaRaizId, long now) {
        return driver.queryOne(
                "SELECT id FROM categoria WHERE nome = 'Sem Categoria' AND tipo = 'sem_tipo' AND pai_id = ? ORDER BY ordem LIMIT 1",
                row -> row.getString("id"),
                semCategoriaRaizId
        ).orElseGet(() -> {
            String id = UUID.randomUUID().toString();
            driver.execute(
                    "INSERT INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em, icone, cor) VALUES (?, 'Sem Categoria', ?, 'sem_tipo', NULL, 0, ?, '\u2753', '#9CA3AF')",
                    id, semCategoriaRaizId, now
            );
            return id;
        });
    }
}
