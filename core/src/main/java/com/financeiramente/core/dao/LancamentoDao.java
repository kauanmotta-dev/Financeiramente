package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.Pagina;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.MonetaryValues;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LancamentoDao implements LancamentoRepository {

    private final DatabaseDriver db;

    public LancamentoDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(Lancamento lancamento) {
        db.execute(
            "INSERT INTO lancamento(id, valor, tipo, data, descricao, categoria_id, fatura_id, compra_cartao_id, criado_em, atualizado_em) VALUES (?,?,?,?,?,?,?,?,?,?)",
            lancamento.getId(),
            MonetaryValues.toDouble(lancamento.getValor()),
            lancamento.getTipo().name().toLowerCase(),
            lancamento.getData(),
            lancamento.getDescricao(),
            lancamento.getCategoriaId(),
            lancamento.getFaturaId(),
            lancamento.getCompraCartaoId(),
            lancamento.getCriadoEm(),
            lancamento.getAtualizadoEm()
        );
    }

    @Override
    public void atualizar(Lancamento lancamento) {
        db.execute(
            "UPDATE lancamento SET valor=?, tipo=?, data=?, descricao=?, categoria_id=?, fatura_id=?, compra_cartao_id=?, atualizado_em=? WHERE id=?",
            MonetaryValues.toDouble(lancamento.getValor()),
            lancamento.getTipo().name().toLowerCase(),
            lancamento.getData(),
            lancamento.getDescricao(),
            lancamento.getCategoriaId(),
            lancamento.getFaturaId(),
            lancamento.getCompraCartaoId(),
            lancamento.getAtualizadoEm(),
            lancamento.getId()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM lancamento WHERE id=?", id);
    }

    @Override
    public Optional<Lancamento> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM lancamento WHERE id=?", MAPPER, id);
    }

    @Override
    public List<Lancamento> listarPorMes(int ano, int mes) {
        return listarPorMes(ano, mes, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorMes(int ano, int mes, Pagina pagina) {
        String prefix = String.format("%04d-%02d", ano, mes);
        return db.query(
            aplicarPagina("SELECT * FROM lancamento WHERE substr(data,1,7)=? ORDER BY data DESC", pagina),
            MAPPER, prefix
        );
    }

    @Override
    public List<Lancamento> listarPorCategoria(String categoriaId, int ano, int mes) {
        return listarPorCategoria(categoriaId, ano, mes, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorCategoria(String categoriaId, int ano, int mes, Pagina pagina) {
        String prefix = String.format("%04d-%02d", ano, mes);
        return db.query(
            aplicarPagina("SELECT * FROM lancamento WHERE categoria_id=? AND substr(data,1,7)=? ORDER BY data DESC", pagina),
            MAPPER, categoriaId, prefix
        );
    }

    @Override
    public List<Lancamento> listarPorTag(String tagId, int ano, int mes) {
        return listarPorTag(tagId, ano, mes, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorTag(String tagId, int ano, int mes, Pagina pagina) {
        String prefix = String.format("%04d-%02d", ano, mes);
        return db.query(
            aplicarPagina(
                "SELECT l.* FROM lancamento l " +
                "JOIN lancamento_tag lt ON lt.lancamento_id = l.id " +
                "WHERE lt.tag_id=? AND substr(l.data,1,7)=? ORDER BY l.data DESC",
                pagina
            ),
            MAPPER, tagId, prefix
        );
    }

    @Override
    public List<Lancamento> listarPorPeriodo(String dataInicio, String dataFim) {
        return listarPorPeriodo(dataInicio, dataFim, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorPeriodo(String dataInicio, String dataFim, Pagina pagina) {
        return db.query(
            aplicarPagina("SELECT * FROM lancamento WHERE data BETWEEN ? AND ? ORDER BY data DESC", pagina),
            MAPPER, dataInicio, dataFim
        );
    }

    @Override
    public List<Lancamento> listarPorPeriodoComFiltros(String dataInicio,
                                                       String dataFim,
                                                       String categoriaId,
                                                       TipoLancamento tipo,
                                                       String tagId) {
        return listarPorPeriodoComFiltros(dataInicio, dataFim, categoriaId, tipo, tagId, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorPeriodoComFiltros(String dataInicio,
                                                       String dataFim,
                                                       String categoriaId,
                                                       TipoLancamento tipo,
                                                       String tagId,
                                                       Pagina pagina) {
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT l.* FROM lancamento l "
        );
        List<Object> args = new ArrayList<>();

        if (tagId != null) {
            sql.append("LEFT JOIN lancamento_tag lt ON lt.lancamento_id = l.id ");
        }

        sql.append("WHERE l.data BETWEEN ? AND ?");
        args.add(dataInicio);
        args.add(dataFim);

        if (categoriaId != null) {
            sql.append(" AND l.categoria_id = ?");
            args.add(categoriaId);
        }

        if (tipo != null) {
            sql.append(" AND l.tipo = ?");
            args.add(tipo.name().toLowerCase());
        }

        if (tagId != null) {
            sql.append(" AND lt.tag_id = ?");
            args.add(tagId);
        }

        sql.append(" ORDER BY l.data DESC");
        return db.query(aplicarPagina(sql.toString(), pagina), MAPPER, args.toArray());
    }

    @Override
    public BigDecimal somarPorTipoEMes(TipoLancamento tipo, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        StringBuilder sql = new StringBuilder(
            "SELECT COALESCE(SUM(l.valor), 0) AS total FROM lancamento l WHERE l.tipo=? AND substr(l.data,1,7)=?"
        );
        if (tipo == TipoLancamento.DESPESA) {
            sql.append(" AND NOT EXISTS (SELECT 1 FROM fatura f WHERE f.lancamento_pagamento_id = l.id)");
        }
        Optional<BigDecimal> result = db.queryOne(
            sql.toString(),
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            tipo.name().toLowerCase(), prefix
        );
        return result.orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal somarPorCategoria(String categoriaId, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<BigDecimal> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM lancamento WHERE categoria_id=? AND substr(data,1,7)=?",
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            categoriaId, prefix
        );
        return result.orElse(BigDecimal.ZERO);
    }

    @Override
    public List<Lancamento> listarPorFatura(String faturaId) {
        return listarPorFatura(faturaId, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorFatura(String faturaId, Pagina pagina) {
        return db.query(
            aplicarPagina("SELECT * FROM lancamento WHERE fatura_id=? ORDER BY data DESC", pagina),
            MAPPER, faturaId
        );
    }

    @Override
    public List<Lancamento> listarPorCompraCartao(String compraCartaoId) {
        return listarPorCompraCartao(compraCartaoId, Pagina.padrao());
    }

    @Override
    public List<Lancamento> listarPorCompraCartao(String compraCartaoId, Pagina pagina) {
        return db.query(
            aplicarPagina("SELECT * FROM lancamento WHERE compra_cartao_id=? ORDER BY data ASC", pagina),
            MAPPER,
            compraCartaoId
        );
    }

    @Override
    public boolean existePorCompraCartaoEmFaturaPaga(String compraCartaoId) {
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS cnt " +
            "FROM lancamento l " +
            "JOIN fatura f ON f.id = l.fatura_id " +
            "WHERE l.compra_cartao_id=? AND f.status IN ('pago','pago_parcial')",
            row -> row.getInt("cnt"),
            compraCartaoId
        );
        return result.orElse(0) > 0;
    }

    @Override
    public void deletarPorCompraCartaoEmFaturaNaoPaga(String compraCartaoId) {
        db.execute(
            "DELETE FROM lancamento " +
            "WHERE compra_cartao_id=? AND (" +
            "fatura_id IS NULL OR " +
            "fatura_id IN (SELECT id FROM fatura WHERE status NOT IN ('pago','pago_parcial'))" +
            ")",
            compraCartaoId
        );
    }

    @Override
    public BigDecimal somarPorFatura(String faturaId) {
        Optional<BigDecimal> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM lancamento WHERE fatura_id=?",
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            faturaId
        );
        return result.orElse(BigDecimal.ZERO);
    }

    @Override
    public boolean existePorCategoria(String categoriaId) {
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS cnt FROM lancamento WHERE categoria_id=?",
            row -> row.getInt("cnt"),
            categoriaId
        );
        return result.orElse(0) > 0;
    }

    @Override
    public BigDecimal somarDespesasSemFaturaPorMes(int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<BigDecimal> result = db.queryOne(
            "SELECT COALESCE(SUM(l.valor), 0) AS total FROM lancamento l " +
            "WHERE l.tipo='despesa' AND l.fatura_id IS NULL AND substr(l.data,1,7)=? " +
            "AND NOT EXISTS (SELECT 1 FROM fatura f WHERE f.lancamento_pagamento_id = l.id)",
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            prefix
        );
        return result.orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal somarDespesasPorTipoCategoriaEMes(TipoCategoria tipo, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<BigDecimal> result = db.queryOne(
            "SELECT COALESCE(SUM(l.valor), 0) AS total " +
            "FROM lancamento l JOIN categoria c ON l.categoria_id = c.id " +
            "WHERE c.tipo=? AND l.tipo='despesa' AND substr(l.data,1,7)=? " +
            "AND NOT EXISTS (SELECT 1 FROM fatura f WHERE f.lancamento_pagamento_id = l.id)",
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            tipo.name().toLowerCase(), prefix
        );
        return result.orElse(BigDecimal.ZERO);
    }

    @Override
    public Map<String, BigDecimal> somarDespesasPorCategoriaEPeriodo(String dataInicio, String dataFim) {
        return somarDespesasPorCategoriaEPeriodo(dataInicio, dataFim, null, null, null);
    }

    @Override
    public Map<String, BigDecimal> somarDespesasPorCategoriaEPeriodo(String dataInicio,
                                                                     String dataFim,
                                                                     String categoriaId,
                                                                     TipoLancamento tipo,
                                                                     String tagId) {
        StringBuilder sql = new StringBuilder(
            "SELECT l.categoria_id AS categoria_id, COALESCE(SUM(l.valor), 0) AS total FROM lancamento l "
        );
        List<Object> args = new ArrayList<>();

        if (tagId != null) {
            sql.append("LEFT JOIN lancamento_tag lt ON lt.lancamento_id = l.id ");
        }

        sql.append("WHERE l.tipo='despesa' AND l.categoria_id IS NOT NULL AND l.data BETWEEN ? AND ? ");
        sql.append("AND NOT EXISTS (SELECT 1 FROM fatura f WHERE f.lancamento_pagamento_id = l.id)");
        args.add(dataInicio);
        args.add(dataFim);

        if (categoriaId != null) {
            sql.append(" AND l.categoria_id = ?");
            args.add(categoriaId);
        }

        if (tipo != null) {
            sql.append(" AND l.tipo = ?");
            args.add(tipo.name().toLowerCase());
        }

        if (tagId != null) {
            sql.append(" AND lt.tag_id = ?");
            args.add(tagId);
        }

        sql.append(" GROUP BY l.categoria_id");

        List<Map.Entry<String, BigDecimal>> rows = db.query(
            sql.toString(),
            row -> Map.entry(
                row.getString("categoria_id"),
                MonetaryValues.fromDouble(row.getDouble("total"))
            ),
            args.toArray()
        );

        Map<String, BigDecimal> totalPorCategoriaId = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> row : rows) {
            totalPorCategoriaId.put(row.getKey(), row.getValue());
        }
        return totalPorCategoriaId;
    }

    @Override
    public int contarPorMes(int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS total FROM lancamento WHERE substr(data,1,7)=?",
            row -> row.getInt("total"),
            prefix
        );
        return result.orElse(0);
    }

    @Override
    public int contarPorPeriodo(String dataInicio, String dataFim) {
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS total FROM lancamento WHERE data BETWEEN ? AND ?",
            row -> row.getInt("total"),
            dataInicio, dataFim
        );
        return result.orElse(0);
    }

    @Override
    public int contarPorCategoria(String categoriaId, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS total FROM lancamento WHERE categoria_id=? AND substr(data,1,7)=?",
            row -> row.getInt("total"),
            categoriaId, prefix
        );
        return result.orElse(0);
    }

    @Override
    public Optional<String> buscarFaturaIdPorLancamentoPagamento(String lancamentoId) {
        return db.queryOne(
            "SELECT id FROM fatura WHERE lancamento_pagamento_id=? LIMIT 1",
            row -> row.getString("id"),
            lancamentoId
        );
    }

    @Override
    public void transferirLancamentosDeFatura(String deFaturaId, String paraFaturaId) {
        db.execute(
            "UPDATE lancamento SET fatura_id=? WHERE fatura_id=?",
            paraFaturaId, deFaturaId
        );
    }

    private String aplicarPagina(String sql, Pagina pagina) {
        if (pagina == null) {
            return sql;
        }
        return sql + " LIMIT " + pagina.getLimit() + " OFFSET " + pagina.getOffset();
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Lancamento> MAPPER = row -> new Lancamento(
        row.getString("id"),
        MonetaryValues.fromDouble(row.getDouble("valor")),
        TipoLancamento.valueOf(row.getString("tipo").toUpperCase()),
        row.getString("data"),
        row.getString("descricao"),
        row.isNull("categoria_id") ? null : row.getString("categoria_id"),
        row.isNull("fatura_id") ? null : row.getString("fatura_id"),
        row.isNull("compra_cartao_id") ? null : row.getString("compra_cartao_id"),
        row.getLong("criado_em"),
        row.getLong("atualizado_em")
    );
}
