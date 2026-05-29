package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.util.MonetaryValues;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class FaturaDao implements FaturaRepository {

    private final DatabaseDriver db;

    public FaturaDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(Fatura fatura) {
        db.execute(
            "INSERT INTO fatura(id, cartao_id, mes, data_fechamento, data_vencimento, valor_pago, status, descricao, criado_em, atualizado_em) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)",
            fatura.getId(),
            fatura.getCartaoId(),
            fatura.getMes(),
            fatura.getDataFechamento(),
            fatura.getDataVencimento(),
            MonetaryValues.toDouble(fatura.getValorPago()),
            fatura.getStatus().name().toLowerCase(),
            fatura.getDescricao(),
            fatura.getCriadoEm(),
            fatura.getAtualizadoEm()
        );
    }

    @Override
    public void atualizar(Fatura fatura) {
        db.execute(
            "UPDATE fatura SET valor_pago=?, status=?, descricao=?, lancamento_pagamento_id=?, atualizado_em=? WHERE id=?",
            MonetaryValues.toDouble(fatura.getValorPago()),
            fatura.getStatus().name().toLowerCase(),
            fatura.getDescricao(),
            fatura.getLancamentoPagamentoId(),
            fatura.getAtualizadoEm(),
            fatura.getId()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM fatura WHERE id=?", id);
    }

    @Override
    public Optional<Fatura> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM fatura WHERE id=?", MAPPER, id);
    }

    @Override
    public Optional<Fatura> buscarPorCartaoEMes(String cartaoId, String mes) {
        return db.queryOne(
            "SELECT * FROM fatura WHERE cartao_id=? AND mes=?",
            MAPPER, cartaoId, mes
        );
    }

    @Override
    public List<Fatura> listarPorCartao(String cartaoId) {
        return db.query(
            "SELECT * FROM fatura WHERE cartao_id=? ORDER BY mes DESC",
            MAPPER, cartaoId
        );
    }

    @Override
    public List<Fatura> listarAbertas() {
        return db.query(
            "SELECT * FROM fatura WHERE status='aberto' ORDER BY data_vencimento ASC",
            MAPPER
        );
    }

    @Override
    public boolean existeFaturaComStatus(String cartaoId, StatusFatura status) {
        Optional<Integer> result = db.queryOne(
            "SELECT COUNT(*) AS cnt FROM fatura WHERE cartao_id=? AND status=?",
            row -> row.getInt("cnt"),
            cartaoId, status.name().toLowerCase()
        );
        return result.orElse(0) > 0;
    }

    @Override
    public BigDecimal somarTotalUtilizadoPorCartao(String cartaoId) {
        Optional<BigDecimal> result = db.queryOne(
            "SELECT COALESCE(SUM(l.valor), 0) AS total " +
            "FROM lancamento l JOIN fatura f ON l.fatura_id = f.id " +
            "WHERE f.cartao_id=? AND f.status IN ('aberto','fechado')",
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            cartaoId
        );
        return result.orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal somarValorPagoPorDataDePagamento(int ano, int mes) {
        String prefix = String.format("%04d-%02d", ano, mes);
        Optional<BigDecimal> result = db.queryOne(
            "SELECT COALESCE(SUM(valor_pago), 0) AS total FROM fatura " +
            "WHERE status IN ('pago','pago_parcial') AND substr(mes,1,7)=?",
            row -> MonetaryValues.fromDouble(row.getDouble("total")),
            prefix
        );
        return result.orElse(BigDecimal.ZERO);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Fatura> MAPPER = row -> new Fatura(
        row.getString("id"),
        row.getString("cartao_id"),
        row.getString("mes"),
        row.getString("data_fechamento"),
        row.getString("data_vencimento"),
        MonetaryValues.fromDouble(row.getDouble("valor_pago")),
        StatusFatura.fromString(row.getString("status")),
        row.isNull("descricao") ? null : row.getString("descricao"),
        row.isNull("lancamento_pagamento_id") ? null : row.getString("lancamento_pagamento_id"),
        row.getLong("criado_em"),
        row.getLong("atualizado_em")
    );

    @Override
    public Optional<Fatura> buscarFaturaAbertaPorCartao(String cartaoId) {
        return db.queryOne(
            "SELECT * FROM fatura WHERE cartao_id=? AND status='aberto' ORDER BY mes ASC LIMIT 1",
            MAPPER, cartaoId
        );
    }
}
