package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DataValidator;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;
import java.util.Comparator;

public class EditarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final CategoriaRepository categoriaRepository;
    private final FaturaRepository faturaRepository;

    public EditarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                   TagRepository tagRepository,
                                   CategoriaRepository categoriaRepository,
                                   FaturaRepository faturaRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.categoriaRepository = categoriaRepository;
        this.faturaRepository = faturaRepository;
    }

    public Lancamento executar(String id, RegistrarLancamentoInput input) {
        if (input == null) {
            throw new DomainException("Input do lançamento é obrigatório.");
        }

        String idValidado = DomainException.requireNonBlank(id, "Id do lançamento é obrigatório.");
        String dataValidada = DataValidator.validarData(input.getData());

        Lancamento existente = lancamentoRepository.buscarPorId(idValidado)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        if (input.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }

        // Valida que a nova fatura (se diferente da atual) está aberta
        String novaFaturaId = input.getFaturaId();
        String faturaAtualId = existente.getFaturaId();
        boolean trocandoFatura = novaFaturaId != null && !novaFaturaId.equals(faturaAtualId);
        if (trocandoFatura) {
            Fatura novaFatura = faturaRepository.buscarPorId(novaFaturaId)
                    .orElseThrow(() -> new DomainException("Fatura não encontrada."));
            if (novaFatura.getStatus() != StatusFatura.ABERTO) {
                throw new DomainException("Apenas faturas abertas podem receber lançamentos.");
            }
        }

        String categoriaId = resolverCategoriaId(input.getCategoriaId(), input.getTipo());

        existente.atualizarDados(
            input.getValor(),
            input.getTipo(),
            dataValidada,
            input.getDescricao(),
            categoriaId);
        existente.setFaturaId(input.getFaturaId());
        existente.setAtualizadoEm(System.currentTimeMillis());

        lancamentoRepository.atualizar(existente);

        tagRepository.desvincularLancamento(existente.getId());
        for (String tagId : input.getTags()) {
            tagRepository.vincularLancamento(existente.getId(), tagId);
        }

        return existente;
    }

    private String resolverCategoriaId(String categoriaIdInput, TipoLancamento tipoLancamento) {
        String categoriaId = categoriaIdInput;
        if (categoriaId == null || categoriaId.trim().isEmpty()) {
            return buscarIdCategoriaSemCategoria();
        }

        categoriaId = DomainException.requireNonBlank(categoriaId, "Categoria é obrigatória.");
        Categoria categoria = categoriaRepository.buscarPorId(categoriaId)
                .orElseThrow(() -> new DomainException("Categoria não encontrada."));
        if (categoria.getPaiId() == null && categoria.getTipo() != TipoCategoria.SEM_TIPO) {
            throw new DomainException(
                    "Lançamentos só podem ser registrados em subcategorias, não em categorias principais.");
        }
        if (!categoria.getTipo().isCompativelCom(tipoLancamento)) {
            throw new DomainException(
                    "Categoria '" + categoria.getNome() + "' não é compatível com o tipo de lançamento informado.");
        }
        return categoria.getId();
    }

    private String buscarIdCategoriaSemCategoria() {
        return categoriaRepository.listarTodas().stream()
                .filter(categoria -> "Sem Categoria".equalsIgnoreCase(categoria.getNome()))
                .filter(categoria -> categoria.getTipo() == TipoCategoria.SEM_TIPO)
                .sorted(Comparator.comparing(categoria -> categoria.getPaiId() == null))
                .map(Categoria::getId)
                .findFirst()
                .orElseThrow(() -> new DomainException("Categoria padrão 'Sem Categoria' não encontrada."));
    }
}
