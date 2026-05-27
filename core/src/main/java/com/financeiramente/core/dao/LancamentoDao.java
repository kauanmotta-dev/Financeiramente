package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.LancamentoRepository;

import java.util.List;
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
            lancamento.getValor(),
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
            lancamento.getValor(),
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
        String prefix = String.format("%04d-%02d", ano, mes);
        return db.query(
            "SELECT * FROM lancamento WHERE substr(data,1,7)=? ORDER BY data DESC",
            MAPPER, prefix
        );
    }

    @Override
    public List<Lancamento> listarPorCategoria(String categoriaId, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        return db.query(
            "SELECT * FROM lancamento WHERE categoria_id=? AND substr(data,1,7)=? ORDER BY data DESC",
            MAPPER, categoriaId, prefix
        );
    }

    @Override
    public List<Lancamento> listarPorTag(String tagId, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        return db.query(
            "SELECT l.* FROM lancamento l " +
            "JOIN lancamento_tag lt ON lt.lancamento_id = l.id " +
            "WHERE lt.tag_id=? AND substr(l.data,1,7)=? ORDER BY l.data DESC",
            MAPPER, tagId, prefix
        );
    }

    @Override
    public List<Lancamento> listarPorPeriodo(String dataInicio, String dataFim) {
        return db.query(
            "SELECT * FROM lancamento WHERE data BETWEEN ? AND ? ORDER BY data DESC",
            MAPPER, dataInicio, dataFim
        );
    }

    @Override
    public double somarPorTipoEMes(TipoLancamento tipo, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM lancamento WHERE tipo=? AND substr(data,1,7)=?",
            row -> row.getDouble("total"),
            tipo.name().toLowerCase(), prefix
        );
        return result.orElse(0.0);
    }

    @Override
    public double somarPorCategoria(String categoriaId, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM lancamento WHERE categoria_id=? AND substr(data,1,7)=?",
            row -> row.getDouble("total"),
            categoriaId, prefix
        );
        return result.orElse(0.0);
    }

    @Override
    public List<Lancamento> listarPorFatura(String faturaId) {
        return db.query(
            "SELECT * FROM lancamento WHERE fatura_id=? ORDER BY data DESC",
            MAPPER, faturaId
        );
    }

    @Override
    public List<Lancamento> listarPorCompraCartao(String compraCartaoId) {
        return db.query(
            "SELECT * FROM lancamento WHERE compra_cartao_id=? ORDER BY data ASC",
            MAPPER,
            compraCartaoId
        );
    }

    @Override
    public double somarPorFatura(String faturaId) {
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM lancamento WHERE fatura_id=?",
            row -> row.getDouble("total"),
            faturaId
        );
        return result.orElse(0.0);
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
    public double somarDespesasSemFaturaPorMes(int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM lancamento " +
            "WHERE tipo='despesa' AND fatura_id IS NULL AND substr(data,1,7)=?",
            row -> row.getDouble("total"),
            prefix
        );
        return result.orElse(0.0);
    }

    @Override
    public double somarDespesasPorTipoCategoriaEMes(TipoCategoria tipo, int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(l.valor), 0) AS total " +
            "FROM lancamento l JOIN categoria c ON l.categoria_id = c.id " +
            "WHERE c.tipo=? AND l.tipo='despesa' AND substr(l.data,1,7)=?",
            row -> row.getDouble("total"),
            tipo.name().toLowerCase(), prefix
        );
        return result.orElse(0.0);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Lancamento> MAPPER = row -> new Lancamento(
        row.getString("id"),
        row.getDouble("valor"),
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
