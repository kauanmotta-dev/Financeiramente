package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.util.DomainException;

public class EditarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final TagRepository tagRepository;
    private final ProvisaoRepository provisaoRepository;

    public EditarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                   TagRepository tagRepository,
                                   ProvisaoRepository provisaoRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.tagRepository = tagRepository;
        this.provisaoRepository = provisaoRepository;
    }

    public Lancamento executar(String id, RegistrarLancamentoInput input) {
        Lancamento existente = lancamentoRepository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        if (input.getDescricao() == null || input.getDescricao().trim().isEmpty()) {
            throw new DomainException("Descrição é obrigatória.");
        }
        if (input.getValor() <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }
        if (input.getRecorrenteId() != null && input.getProvisaoId() != null) {
            throw new DomainException("Lançamento não pode ter recorrente_id e provisao_id simultaneamente.");
        }

        // Estorna provisão anterior se havia
        String provisaoAnteriorId = existente.getProvisaoId();
        if (provisaoAnteriorId != null) {
            provisaoRepository.buscarPorId(provisaoAnteriorId).ifPresent(p ->
                    provisaoRepository.atualizarSaldo(p.getId(),
                            p.getSaldoAcumulado() + existente.getValor()));
        }

        // Debita nova provisão se houver
        if (input.getProvisaoId() != null) {
            Provisao provisao = provisaoRepository.buscarPorId(input.getProvisaoId())
                    .orElseThrow(() -> new DomainException("Provisão não encontrada."));
            double novoSaldo = provisao.getSaldoAcumulado() - input.getValor();
            if (novoSaldo < 0) {
                throw new DomainException("Saldo da provisão insuficiente.");
            }
            provisaoRepository.atualizarSaldo(provisao.getId(), novoSaldo);
        }

        existente.setValor(input.getValor());
        existente.setTipo(input.getTipo());
        existente.setData(input.getData());
        existente.setDescricao(input.getDescricao().trim());
        existente.setCategoriaId(input.getCategoriaId());
        existente.setRecorrenteId(input.getRecorrenteId());
        existente.setProvisaoId(input.getProvisaoId());
        existente.setAtualizadoEm(System.currentTimeMillis());

        lancamentoRepository.atualizar(existente);

        tagRepository.desvincularLancamento(existente.getId());
        for (String tagId : input.getTags()) {
            tagRepository.vincularLancamento(existente.getId(), tagId);
        }

        return existente;
    }
}
