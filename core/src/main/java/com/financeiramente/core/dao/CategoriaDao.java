package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;

import java.util.List;
import java.util.Optional;

public class CategoriaDao implements CategoriaRepository {

    private final DatabaseDriver db;

    public CategoriaDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(Categoria categoria) {
        db.execute(
            "INSERT INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em) VALUES (?,?,?,?,?,?,?)",
            categoria.getId(),
            categoria.getNome(),
            categoria.getPaiId(),
            categoria.getTipo().name().toLowerCase(),
            categoria.getLimiteMensal(),
            categoria.getOrdem(),
            categoria.getCriadoEm()
        );
    }

    @Override
    public void atualizar(Categoria categoria) {
        db.execute(
            "UPDATE categoria SET nome=?, pai_id=?, tipo=?, limite_mensal=?, ordem=? WHERE id=?",
            categoria.getNome(),
            categoria.getPaiId(),
            categoria.getTipo().name().toLowerCase(),
            categoria.getLimiteMensal(),
            categoria.getOrdem(),
            categoria.getId()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM categoria WHERE id=?", id);
    }

    @Override
    public Optional<Categoria> buscarPorId(String id) {
        return db.queryOne("SELECT * FROM categoria WHERE id=?", MAPPER, id);
    }

    @Override
    public List<Categoria> listarRaizes() {
        return db.query("SELECT * FROM categoria WHERE pai_id IS NULL ORDER BY ordem", MAPPER);
    }

    @Override
    public List<Categoria> listarFilhas(String paiId) {
        return db.query("SELECT * FROM categoria WHERE pai_id=? ORDER BY ordem", MAPPER, paiId);
    }

    @Override
    public List<Categoria> listarTodas() {
        return db.query("SELECT * FROM categoria ORDER BY ordem", MAPPER);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Categoria> MAPPER = row -> {
        String tipoStr = row.getString("tipo");
        TipoCategoria tipo = TipoCategoria.valueOf(tipoStr.toUpperCase());
        Double limiteMensal = row.isNull("limite_mensal") ? null : row.getDouble("limite_mensal");
        return new Categoria(
            row.getString("id"),
            row.getString("nome"),
            row.isNull("pai_id") ? null : row.getString("pai_id"),
            tipo,
            limiteMensal,
            row.getInt("ordem"),
            row.getLong("criado_em")
        );
    };
}
