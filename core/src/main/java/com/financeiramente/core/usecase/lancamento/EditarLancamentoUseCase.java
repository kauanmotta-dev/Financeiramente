package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DomainException;

public class EditarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final CategoriaRepository categoriaRepository;

    public EditarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                   TagRepository tagRepository,
                                   CategoriaRepository categoriaRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Lancamento executar(String id, RegistrarLancamentoInput input) {
        Lancamento existente = lancamentoRepository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        if (input.getValor() <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }

        if (input.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.buscarPorId(input.getCategoriaId())
                    .orElseThrow(() -> new DomainException("Categoria não encontrada."));
            if (categoria.getPaiId() == null && categoria.getTipo() != TipoCategoria.SEM_TIPO) {
                throw new DomainException(
                        "Lançamentos só podem ser registrados em subcategorias, não em categorias principais.");
            }
            if (!categoria.getTipo().isCompativelCom(input.getTipo())) {
                throw new DomainException(
                        "Categoria '" + categoria.getNome() + "' não é compatível com o tipo de lançamento informado.");
            }
        }

        existente.setValor(input.getValor());
        existente.setTipo(input.getTipo());
        existente.setData(input.getData());
        existente.setDescricao(input.getDescricao());
        existente.setCategoriaId(input.getCategoriaId());
        existente.setFaturaId(input.getFaturaId());
        existente.setAtualizadoEm(System.currentTimeMillis());

        lancamentoRepository.atualizar(existente);

        tagRepository.desvincularLancamento(existente.getId());
        for (String tagId : input.getTags()) {
            tagRepository.vincularLancamento(existente.getId(), tagId);
        }

        return existente;
    }
}
