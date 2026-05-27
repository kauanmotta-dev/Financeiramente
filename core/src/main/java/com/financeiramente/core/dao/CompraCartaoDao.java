package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;

import java.util.List;
import java.util.Optional;

public class CompraCartaoDao implements CompraCartaoRepository {

    private final DatabaseDriver db;

    public CompraCartaoDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(CompraCartao compra) {
        db.execute(
            "INSERT INTO compra_cartao(id, cartao_id, descricao, valor_total, tipo, total_parcelas, categoria_id, data_compra, dia_recorrencia, ativo, criado_em, atualizado_em) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
            compra.getId(),
            compra.getCartaoId(),
            compra.getDescricao(),
            compra.getValorTotal(),
            compra.getTipo().name().toLowerCase(),
            compra.getTotalParcelas(),
            compra.getCategoriaId(),
            compra.getDataCompra(),
            compra.getDiaRecorrencia(),
            compra.isAtivo() ? 1 : 0,
            compra.getCriadoEm(),
            compra.getAtualizadoEm()
        );
    }

    @Override
    public void atualizar(CompraCartao compra) {
        db.execute(
            "UPDATE compra_cartao SET cartao_id=?, descricao=?, valor_total=?, tipo=?, total_parcelas=?, categoria_id=?, data_compra=?, dia_recorrencia=?, ativo=?, atualizado_em=? WHERE id=?",
            compra.getCartaoId(),
            compra.getDescricao(),
            compra.getValorTotal(),
            compra.getTipo().name().toLowerCase(),
            compra.getTotalParcelas(),
            compra.getCategoriaId(),
            compra.getDataCompra(),
            compra.getDiaRecorrencia(),
            compra.isAtivo() ? 1 : 0,
            compra.getAtualizadoEm(),
            compra.getId()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM compra_cartao WHERE id=?", id);
    }

    @Override
    public Optional<CompraCartao> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM compra_cartao WHERE id=?", MAPPER, id);
    }

    @Override
    public List<CompraCartao> listarPorCartao(String cartaoId) {
        return db.query(
            "SELECT * FROM compra_cartao WHERE cartao_id=? ORDER BY data_compra DESC",
            MAPPER,
            cartaoId
        );
    }

    @Override
    public List<CompraCartao> listarRecorrentesAtivos() {
        return db.query(
            "SELECT * FROM compra_cartao WHERE tipo='recorrente' AND ativo=1 ORDER BY atualizado_em DESC",
            MAPPER
        );
    }

    @Override
    public boolean existeLancamentoRecorrenteNaFatura(String compraCartaoId, String faturaId) {
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS cnt FROM lancamento WHERE compra_cartao_id=? AND fatura_id=?",
            row -> row.getInt("cnt"),
            compraCartaoId,
            faturaId
        );
        return result.orElse(0) > 0;
    }

    @Override
    public boolean existeLancamentoRecorrenteNoMes(String compraCartaoId, String mes) {
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS cnt FROM lancamento WHERE compra_cartao_id=? AND substr(data,1,7)=?",
            row -> row.getInt("cnt"),
            compraCartaoId,
            mes
        );
        return result.orElse(0) > 0;
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<CompraCartao> MAPPER = row -> new CompraCartao(
        row.getString("id"),
        row.getString("cartao_id"),
        row.getString("descricao"),
        row.getDouble("valor_total"),
        TipoCompraCartao.valueOf(row.getString("tipo").toUpperCase()),
        row.isNull("total_parcelas") ? null : row.getInt("total_parcelas"),
        row.isNull("categoria_id") ? null : row.getString("categoria_id"),
        row.getString("data_compra"),
        row.isNull("dia_recorrencia") ? null : row.getInt("dia_recorrencia"),
        row.getInt("ativo") == 1,
        row.getLong("criado_em"),
        row.getLong("atualizado_em")
    );
}