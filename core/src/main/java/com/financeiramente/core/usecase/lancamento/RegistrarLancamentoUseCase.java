package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Categoria;
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
import java.util.UUID;

public class RegistrarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final CategoriaRepository categoriaRepository;
    private final FaturaRepository faturaRepository;

    public RegistrarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                      TagRepository tagRepository,
                                      CategoriaRepository categoriaRepository,
                                      FaturaRepository faturaRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.categoriaRepository = categoriaRepository;
        this.faturaRepository = faturaRepository;
    }

    public Lancamento executar(RegistrarLancamentoInput input) {
        if (input == null) {
            throw new DomainException("Input do lançamento é obrigatório.");
        }

        String dataValidada = DataValidator.validarData(input.getData());

        if (input.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }

        
        if (input.getFaturaId() != null) {
            Fatura fatura = faturaRepository.buscarPorId(input.getFaturaId())
                    .orElseThrow(() -> new DomainException("Fatura não encontrada."));
            if (fatura.getStatus() != StatusFatura.ABERTO) {
                throw new DomainException("Apenas faturas abertas podem receber novos lançamentos.");
            }
        }

        String categoriaId = resolverCategoriaId(input.getCategoriaId(), input.getTipo());

        long now = System.currentTimeMillis();
        Lancamento lancamento = Lancamento.builder(UUID.randomUUID().toString())
                .valor(input.getValor())
                .tipo(input.getTipo())
                .data(dataValidada)
                .descricao(input.getDescricao())
                .categoriaId(categoriaId)
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
