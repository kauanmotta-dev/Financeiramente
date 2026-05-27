package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;

import java.util.List;
import java.util.Optional;

public class MetaDao implements MetaRepository {

    private final DatabaseDriver db;

    public MetaDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(Meta meta) {
        db.execute(
            "INSERT INTO meta(id, nome, valor_objetivo, valor_atual, valor_inicial, data_alvo, descricao, criado_em) VALUES (?,?,?,?,?,?,?,?)",
            meta.getId(),
            meta.getNome(),
            meta.getValorObjetivo(),
            meta.getValorAtual(),
            meta.getValorInicial(),
            meta.getDataAlvo(),
            meta.getDescricao(),
            meta.getCriadoEm()
        );
    }

    @Override
    public void atualizar(Meta meta) {
        db.execute(
            "UPDATE meta SET nome=?, valor_objetivo=?, valor_atual=?, valor_inicial=?, data_alvo=?, descricao=? WHERE id=?",
            meta.getNome(),
            meta.getValorObjetivo(),
            meta.getValorAtual(),
            meta.getValorInicial(),
            meta.getDataAlvo(),
            meta.getDescricao(),
            meta.getId()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM meta WHERE id=?", id);
    }

    @Override
    public Optional<Meta> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM meta WHERE id=?", MAPPER, id);
    }

    @Override
    public List<Meta> listarAtivas() {
        return db.query("SELECT * FROM meta ORDER BY nome", MAPPER);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Meta> MAPPER = row -> new Meta(
        row.getString("id"),
        row.getString("nome"),
        row.getDouble("valor_objetivo"),
        row.getDouble("valor_atual"),
        row.isNull("valor_inicial") ? 0.0 : row.getDouble("valor_inicial"),
        row.isNull("data_alvo") ? null : row.getString("data_alvo"),
        row.isNull("descricao") ? null : row.getString("descricao"),
        row.getLong("criado_em")
    );
}
