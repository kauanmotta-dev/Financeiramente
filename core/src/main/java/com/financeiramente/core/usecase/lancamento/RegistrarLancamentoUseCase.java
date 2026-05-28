package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DataValidator;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;
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
        if (input == null) {
            throw new DomainException("Input do lançamento é obrigatório.");
        }

        String dataValidada = DataValidator.validarData(input.getData());

        if (input.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }

        if (input.getCategoriaId() != null) {
            String categoriaId = DomainException.requireNonBlank(input.getCategoriaId(), "Categoria é obrigatória.");
            Categoria categoria = categoriaRepository.buscarPorId(categoriaId)
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

        long now = System.currentTimeMillis();
        Lancamento lancamento = Lancamento.builder(UUID.randomUUID().toString())
                .valor(input.getValor())
                .tipo(input.getTipo())
                .data(dataValidada)
                .descricao(input.getDescricao())
                .categoriaId(input.getCategoriaId())
                .faturaId(input.getFaturaId())
            .compraCartaoId(input.getCompraCartaoId())
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
