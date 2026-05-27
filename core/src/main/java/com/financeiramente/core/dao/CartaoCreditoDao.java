package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.vo.BandeiraCartao;
import com.financeiramente.core.repository.CartaoCreditoRepository;

import java.util.List;
import java.util.Optional;

public class CartaoCreditoDao implements CartaoCreditoRepository {

    private final DatabaseDriver db;

    public CartaoCreditoDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(CartaoCredito cartao) {
        db.execute(
            "INSERT INTO cartao_credito(id, nome, dia_vencimento, dias_fechamento, limite, bandeira, icone, cor, ativo, criado_em, atualizado_em) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            cartao.getId(),
            cartao.getNome(),
            cartao.getDiaVencimento(),
            cartao.getDiasParaFechamento(),
            cartao.getLimite(),
            cartao.getBandeira() != null ? cartao.getBandeira().name().toLowerCase() : null,
            cartao.getIcone(),
            cartao.getCor(),
            cartao.isAtivo() ? 1 : 0,
            cartao.getCriadoEm(),
            cartao.getAtualizadoEm()
        );
    }

    @Override
    public void atualizar(CartaoCredito cartao) {
        db.execute(
            "UPDATE cartao_credito SET nome=?, dia_vencimento=?, dias_fechamento=?, limite=?, bandeira=?, icone=?, cor=?, ativo=?, atualizado_em=? WHERE id=?",
            cartao.getNome(),
            cartao.getDiaVencimento(),
            cartao.getDiasParaFechamento(),
            cartao.getLimite(),
            cartao.getBandeira() != null ? cartao.getBandeira().name().toLowerCase() : null,
            cartao.getIcone(),
            cartao.getCor(),
            cartao.isAtivo() ? 1 : 0,
            cartao.getAtualizadoEm(),
            cartao.getId()
        );
    }

    @Override
    public Optional<CartaoCredito> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM cartao_credito WHERE id=?", MAPPER, id);
    }

    @Override
    public List<CartaoCredito> listarAtivos() {
        return db.query("SELECT * FROM cartao_credito WHERE ativo=1 ORDER BY nome ASC", MAPPER);
    }

    @Override
    public void desativar(String id, long atualizadoEm) {
        db.execute("UPDATE cartao_credito SET ativo=0, atualizado_em=? WHERE id=?", atualizadoEm, id);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<CartaoCredito> MAPPER = row -> new CartaoCredito(
        row.getString("id"),
        row.getString("nome"),
        row.getInt("dia_vencimento"),
        row.getInt("dias_fechamento"),
        row.isNull("limite") ? null : row.getDouble("limite"),
        row.isNull("bandeira") ? null : BandeiraCartao.fromString(row.getString("bandeira")),
        row.getString("icone"),
        row.getString("cor"),
        row.getInt("ativo") == 1,
        row.getLong("criado_em"),
        row.getLong("atualizado_em")
    );
}
