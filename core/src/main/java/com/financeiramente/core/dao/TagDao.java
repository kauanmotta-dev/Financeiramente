package com.financeiramente.core.dao;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.RowMapper;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.repository.TagRepository;

import java.util.List;
import java.util.Optional;

public class TagDao implements TagRepository {

    private final DatabaseDriver db;

    public TagDao(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public void salvar(Tag tag) {
        db.execute(
            "INSERT INTO tag(id, nome, emoji, cor, criado_em) VALUES (?,?,?,?,?)",
            tag.getId(), tag.getNome(), tag.getEmoji(), tag.getCor(), tag.getCriadoEm()
        );
    }

    @Override
    public void atualizar(Tag tag) {
        db.execute(
            "UPDATE tag SET nome=?, emoji=?, cor=? WHERE id=?",
            tag.getNome(), tag.getEmoji(), tag.getCor(), tag.getId()
        );
    }

    @Override
    public void deletar(String id) {
        db.execute("DELETE FROM lancamento_tag WHERE tag_id=?", id);
        db.execute("DELETE FROM tag WHERE id=?", id);
    }

    @Override
    public List<Tag> listarTodas() {
        return db.query("SELECT * FROM tag ORDER BY nome", MAPPER);
    }

    @Override
    public Optional<Tag> buscarPorNome(String nome) {
        return db.queryOne("SELECT * FROM tag WHERE lower(nome)=lower(?)", MAPPER, nome);
    }

    @Override
    public void vincularLancamento(String lancamentoId, String tagId) {
        db.execute(
            "INSERT OR IGNORE INTO lancamento_tag(lancamento_id, tag_id) VALUES (?,?)",
            lancamentoId, tagId
        );
    }

    @Override
    public void desvincularLancamento(String lancamentoId) {
        db.execute("DELETE FROM lancamento_tag WHERE lancamento_id=?", lancamentoId);
    }

    @Override
    public List<Tag> listarPorLancamento(String lancamentoId) {
        return db.query(
            "SELECT t.* FROM tag t JOIN lancamento_tag lt ON lt.tag_id = t.id WHERE lt.lancamento_id=? ORDER BY t.nome",
            MAPPER, lancamentoId
        );
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<Tag> MAPPER = row -> new Tag(
        row.getString("id"),
        row.getString("nome"),
        row.getString("emoji"),
        row.getString("cor"),
        row.getLong("criado_em")
    );
}
