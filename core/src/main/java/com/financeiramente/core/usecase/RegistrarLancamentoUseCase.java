package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class RegistrarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final CategoriaRepository categoriaRepository;

    public RegistrarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                      TagRepository tagRepository,
                                      CategoriaRepository categoriaRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Lancamento executar(RegistrarLancamentoInput input) {
        if (input.getValor() <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }

        Categoria categoria = categoriaRepository.buscarPorId(input.getCategoriaId())
                .orElseThrow(() -> new DomainException("Categoria não encontrada."));
        if (categoria.getPaiId() == null) {
            throw new DomainException(
                    "Lançamentos só podem ser registrados em subcategorias, não em categorias principais.");
        }
        if (!categoria.getTipo().isCompativelCom(input.getTipo())) {
            throw new DomainException(
                    "Categoria '" + categoria.getNome() + "' não é compatível com o tipo de lançamento informado.");
        }
        
        String descricao = input.getDescricao() != null ? input.getDescricao().trim() : categoria.getNome().toString();

        long now = System.currentTimeMillis();
        Lancamento lancamento = Lancamento.builder(UUID.randomUUID().toString())
                .valor(input.getValor())
                .tipo(input.getTipo())
                .data(input.getData())
                .descricao(descricao)
                .categoriaId(input.getCategoriaId())
                .recorrenteId(input.getRecorrenteId())
                .criadoEm(now)
                .atualizadoEm(now)
                .build();

        lancamentoRepository.salvar(lancamento);

        for (String tagId : input.getTags()) {
            tagRepository.vincularLancamento(lancamento.getId(), tagId);
        }

        return lancamento;
    }
}
