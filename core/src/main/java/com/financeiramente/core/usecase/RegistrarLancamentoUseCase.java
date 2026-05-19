package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class RegistrarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final ProvisaoRepository provisaoRepository;

    public RegistrarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                      TagRepository tagRepository,
                                      ProvisaoRepository provisaoRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.provisaoRepository = provisaoRepository;
    }

    public Lancamento executar(RegistrarLancamentoInput input) {
        if (input.getDescricao() == null || input.getDescricao().trim().isEmpty()) {
            throw new DomainException("Descrição é obrigatória.");
        }
        if (input.getValor() <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }
        if (input.getRecorrenteId() != null && input.getProvisaoId() != null) {
            throw new DomainException("Lançamento não pode ter recorrente_id e provisao_id simultaneamente.");
        }

        if (input.getProvisaoId() != null) {
            Provisao provisao = provisaoRepository.buscarPorId(input.getProvisaoId())
                    .orElseThrow(() -> new DomainException("Provisão não encontrada."));
            double novoSaldo = provisao.getSaldoAcumulado() - input.getValor();
            if (novoSaldo < 0) {
                throw new DomainException("Saldo da provisão insuficiente.");
            }
            provisaoRepository.atualizarSaldo(provisao.getId(), novoSaldo);
        }

        long now = System.currentTimeMillis();
        Lancamento lancamento = Lancamento.builder(UUID.randomUUID().toString())
                .valor(input.getValor())
                .tipo(input.getTipo())
                .data(input.getData())
                .descricao(input.getDescricao().trim())
                .categoriaId(input.getCategoriaId())
                .recorrenteId(input.getRecorrenteId())
                .provisaoId(input.getProvisaoId())
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
