package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.repository.AporteMetaRepository;

import java.util.List;
import java.util.Optional;

public class AporteMetaDao implements AporteMetaRepository {

    private final DatabaseDriver db;

    public AporteMetaDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(AporteMeta aporte) {
        db.execute(
            "INSERT INTO aporte_meta(id, meta_id, valor, data, descricao, criado_em) VALUES (?,?,?,?,?,?)",
            aporte.getId(),
            aporte.getMetaId(),
            aporte.getValor(),
            aporte.getData(),
            aporte.getDescricao(),
            aporte.getCriadoEm()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM aporte_meta WHERE id=?", id);
    }

    @Override
    public List<AporteMeta> listarPorMeta(String metaId) {
        return db.query(
            "SELECT * FROM aporte_meta WHERE meta_id=? ORDER BY data DESC",
            MAPPER, metaId
        );
    }

    @Override
    public double somarPorMeta(String metaId) {
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM aporte_meta WHERE meta_id=?",
            row -> row.getDouble("total"),
            metaId
        );
        return result.orElse(0.0);
    }

    @Override
    public double somarPorMesEAno(int mes, int ano) {
        String mesStr = String.format("%02d", mes);
        String dataPrefixo = ano + "-" + mesStr;
        Optional<Double> result = db.queryOne(
            "SELECT COALESCE(SUM(valor), 0) AS total FROM aporte_meta WHERE data LIKE ? || '%'",
            row -> row.getDouble("total"),
            dataPrefixo
        );
        return result.orElse(0.0);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<AporteMeta> MAPPER = row -> new AporteMeta(
        row.getString("id"),
        row.getString("meta_id"),
        row.getDouble("valor"),
        row.getString("data"),
        row.isNull("descricao") ? null : row.getString("descricao"),
        row.getLong("criado_em")
    );
}
