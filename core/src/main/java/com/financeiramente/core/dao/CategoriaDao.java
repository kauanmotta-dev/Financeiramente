package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.util.MonetaryValues;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
            "INSERT INTO categoria(id, nome, pai_id, tipo, limite_mensal, ordem, criado_em, icone, cor) VALUES (?,?,?,?,?,?,?,?,?)",
            categoria.getId(),
            categoria.getNome(),
            categoria.getPaiId(),
            categoria.getTipo().name().toLowerCase(),
            categoria.getLimiteMensal() == null ? null : MonetaryValues.toDouble(categoria.getLimiteMensal()),
            categoria.getOrdem(),
            categoria.getCriadoEm(),
            categoria.getIcone(),
            categoria.getCor()
        );
    }

    @Override
    public void atualizar(Categoria categoria) {
        db.execute(
            "UPDATE categoria SET nome=?, pai_id=?, tipo=?, limite_mensal=?, ordem=?, icone=?, cor=? WHERE id=?",
            categoria.getNome(),
            categoria.getPaiId(),
            categoria.getTipo().name().toLowerCase(),
            categoria.getLimiteMensal() == null ? null : MonetaryValues.toDouble(categoria.getLimiteMensal()),
            categoria.getOrdem(),
            categoria.getIcone(),
            categoria.getCor(),
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

    @Override
    public List<Categoria> listarPorIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder placeholders = new StringBuilder();
        List<Object> args = new ArrayList<>();
        for (String id : ids) {
            if (id == null) {
                continue;
            }
            if (placeholders.length() > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
            args.add(id);
        }

        if (args.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM categoria WHERE id IN (" + placeholders + ")";
        return db.query(sql, MAPPER, args.toArray());
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Categoria> MAPPER = row -> {
        String tipoStr = row.getString("tipo");
        TipoCategoria tipo = TipoCategoria.valueOf(tipoStr.toUpperCase());
        BigDecimal limiteMensal = row.isNull("limite_mensal") ? null : MonetaryValues.fromDouble(row.getDouble("limite_mensal"));
        String icone = row.isNull("icone") ? "\uD83D\uDCE6" : row.getString("icone");
        String cor = row.isNull("cor") ? "#6366F1" : row.getString("cor");
        return Categoria.builder(row.getString("id"))
            .nome(row.getString("nome"))
            .paiId(row.isNull("pai_id") ? null : row.getString("pai_id"))
            .tipo(tipo)
            .limiteMensal(limiteMensal)
            .ordem(row.getInt("ordem"))
            .criadoEm(row.getLong("criado_em"))
            .icone(icone)
            .cor(cor)
            .build();
    };
}
