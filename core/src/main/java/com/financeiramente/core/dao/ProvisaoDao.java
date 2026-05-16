package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.ProvisaoRepository;

import java.util.List;
import java.util.Optional;

public class ProvisaoDao implements ProvisaoRepository {

    private final DatabaseDriver db;

    public ProvisaoDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(Provisao provisao) {
        db.execute(
            "INSERT INTO provisao(id, nome, total_anual, valor_mensal, saldo_acumulado, categoria_id, ativo, criado_em) VALUES (?,?,?,?,?,?,?,?)",
            provisao.getId(),
            provisao.getNome(),
            provisao.getTotalAnual(),
            provisao.getValorMensal(),
            provisao.getSaldoAcumulado(),
            provisao.getCategoriaId(),
            provisao.isAtivo() ? 1 : 0,
            provisao.getCriadoEm()
        );
    }

    @Override
    public void atualizar(Provisao provisao) {
        db.execute(
            "UPDATE provisao SET nome=?, total_anual=?, valor_mensal=?, saldo_acumulado=?, categoria_id=?, ativo=? WHERE id=?",
            provisao.getNome(),
            provisao.getTotalAnual(),
            provisao.getValorMensal(),
            provisao.getSaldoAcumulado(),
            provisao.getCategoriaId(),
            provisao.isAtivo() ? 1 : 0,
            provisao.getId()
        );
    }

    @Override
    public void desativar(String id) {
        db.execute("UPDATE provisao SET ativo=0 WHERE id=?", id);
    }

    @Override
    public Optional<Provisao> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM provisao WHERE id=?", MAPPER, id);
    }

    @Override
    public List<Provisao> listarAtivas() {
        return db.query("SELECT * FROM provisao WHERE ativo=1", MAPPER);
    }

    @Override
    public void atualizarSaldo(String id, double novoSaldo) {
        db.execute("UPDATE provisao SET saldo_acumulado=? WHERE id=?", novoSaldo, id);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Provisao> MAPPER = row -> new Provisao(
        row.getString("id"),
        row.getString("nome"),
        row.getDouble("total_anual"),
        row.getDouble("valor_mensal"),
        row.getDouble("saldo_acumulado"),
        row.isNull("categoria_id") ? null : row.getString("categoria_id"),
        row.getInt("ativo") == 1,
        row.getLong("criado_em")
    );
}
