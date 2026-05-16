package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.PlanejamentoRepository;

import java.util.List;
import java.util.Optional;

public class PlanejamentoDao implements PlanejamentoRepository {

    private final DatabaseDriver db;

    public PlanejamentoDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(PlanejamentoMensal plano) {
        db.execute(
            "INSERT INTO planejamento_mensal(id, ano, mes, receita_esperada, reserva_imprevisto, padrao, confirmado, criado_em) VALUES (?,?,?,?,?,?,?,?)",
            plano.getId(),
            plano.getAno(),
            plano.getMes(),
            plano.getReceitaEsperada(),
            plano.getReservaImprevisto(),
            plano.isPadrao() ? 1 : 0,
            plano.isConfirmado() ? 1 : 0,
            plano.getCriadoEm()
        );
    }

    @Override
    public void atualizar(PlanejamentoMensal plano) {
        db.execute(
            "UPDATE planejamento_mensal SET receita_esperada=?, reserva_imprevisto=?, padrao=?, confirmado=? WHERE id=?",
            plano.getReceitaEsperada(),
            plano.getReservaImprevisto(),
            plano.isPadrao() ? 1 : 0,
            plano.isConfirmado() ? 1 : 0,
            plano.getId()
        );
    }

    @Override
    public Optional<PlanejamentoMensal> buscarPorMes(int ano, int mes) {
        return db.queryOne(
            "SELECT * FROM planejamento_mensal WHERE ano=? AND mes=?",
            PLANO_MAPPER, ano, mes
        );
    }

    @Override
    public Optional<PlanejamentoMensal> buscarPadrao() {
        return db.queryOne(
            "SELECT * FROM planejamento_mensal WHERE padrao=1 LIMIT 1",
            PLANO_MAPPER
        );
    }

    @Override
    public void salvarItemCategoria(PlanejamentoCategoria item) {
        db.execute(
            "INSERT INTO planejamento_categoria(id, planejamento_id, categoria_id, limite) VALUES (?,?,?,?)",
            item.getId(), item.getPlanejamentoId(), item.getCategoriaId(), item.getLimite()
        );
    }

    @Override
    public void atualizarItemCategoria(PlanejamentoCategoria item) {
        db.execute(
            "UPDATE planejamento_categoria SET limite=? WHERE id=?",
            item.getLimite(), item.getId()
        );
    }

    @Override
    public List<PlanejamentoCategoria> listarItensPorPlano(String planejamentoId) {
        return db.query(
            "SELECT * FROM planejamento_categoria WHERE planejamento_id=?",
            ITEM_MAPPER, planejamentoId
        );
    }

    @Override
    public Optional<PlanejamentoCategoria> buscarItemPorCategoria(String planejamentoId, String categoriaId) {
        return db.queryOne(
            "SELECT * FROM planejamento_categoria WHERE planejamento_id=? AND categoria_id=?",
            ITEM_MAPPER, planejamentoId, categoriaId
        );
    }

    // ─── RowMappers ───────────────────────────────────────────────────────────

    private static final RowMapper<PlanejamentoMensal> PLANO_MAPPER = row -> new PlanejamentoMensal(
        row.getString("id"),
        row.getInt("ano"),
        row.getInt("mes"),
        row.getDouble("receita_esperada"),
        row.getDouble("reserva_imprevisto"),
        row.getInt("padrao") == 1,
        row.getInt("confirmado") == 1,
        row.getLong("criado_em")
    );

    private static final RowMapper<PlanejamentoCategoria> ITEM_MAPPER = row -> new PlanejamentoCategoria(
        row.getString("id"),
        row.getString("planejamento_id"),
        row.getString("categoria_id"),
        row.getDouble("limite")
    );
}
