package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;

import java.util.List;

public class LancamentoRecorrenteDao implements LancamentoRecorrenteRepository {

    private final DatabaseDriver db;

    public LancamentoRecorrenteDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(LancamentoRecorrente recorrente) {
        db.execute(
            "INSERT INTO lancamento_recorrente(id, descricao, valor, tipo, categoria_id, recorrencia, dia_recorrencia, ativo, criado_em) VALUES (?,?,?,?,?,?,?,?,?)",
            recorrente.getId(),
            recorrente.getDescricao(),
            recorrente.getValor(),
            recorrente.getTipo().name().toLowerCase(),
            recorrente.getCategoriaId(),
            recorrente.getRecorrencia().name().toLowerCase(),
            recorrente.getDiaRecorrencia(),
            recorrente.isAtivo() ? 1 : 0,
            recorrente.getCriadoEm()
        );
    }

    @Override
    public void atualizar(LancamentoRecorrente recorrente) {
        db.execute(
            "UPDATE lancamento_recorrente SET descricao=?, valor=?, tipo=?, categoria_id=?, recorrencia=?, dia_recorrencia=?, ativo=? WHERE id=?",
            recorrente.getDescricao(),
            recorrente.getValor(),
            recorrente.getTipo().name().toLowerCase(),
            recorrente.getCategoriaId(),
            recorrente.getRecorrencia().name().toLowerCase(),
            recorrente.getDiaRecorrencia(),
            recorrente.isAtivo() ? 1 : 0,
            recorrente.getId()
        );
    }

    @Override
    public void desativar(String id) {
        db.execute("UPDATE lancamento_recorrente SET ativo=0 WHERE id=?", id);
    }

    @Override
    public List<LancamentoRecorrente> listarAtivos() {
        return db.query("SELECT * FROM lancamento_recorrente WHERE ativo=1", MAPPER);
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<LancamentoRecorrente> MAPPER = row -> new LancamentoRecorrente(
        row.getString("id"),
        row.getString("descricao"),
        row.getDouble("valor"),
        TipoLancamento.valueOf(row.getString("tipo").toUpperCase()),
        row.getString("categoria_id"),
        TipoRecorrencia.valueOf(row.getString("recorrencia").toUpperCase()),
        row.isNull("dia_recorrencia") ? null : row.getInt("dia_recorrencia"),
        row.getInt("ativo") == 1,
        row.getLong("criado_em")
    );
}
