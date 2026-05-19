package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.util.DomainException;

public class EditarLancamentoRecorrenteUseCase {

    private final LancamentoRecorrenteRepository repository;

    public EditarLancamentoRecorrenteUseCase(LancamentoRecorrenteRepository repository) {
        this.repository = repository;
    }

    public LancamentoRecorrente executar(String id, String descricao, double valor,
                                         TipoLancamento tipo, String categoriaId,
                                         TipoRecorrencia recorrencia, Integer diaRecorrencia) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new DomainException("Descrição é obrigatória.");
        }
        if (valor <= 0) {
            throw new DomainException("Valor deve ser maior que zero.");
        }
        if (categoriaId == null || categoriaId.trim().isEmpty()) {
            throw new DomainException("Categoria é obrigatória.");
        }

        LancamentoRecorrente rec = repository.listarAtivos().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new DomainException("Lançamento recorrente não encontrado."));

        rec.setDescricao(descricao.trim());
        rec.setValor(valor);
        rec.setTipo(tipo);
        rec.setCategoriaId(categoriaId);
        rec.setRecorrencia(recorrencia);
        rec.setDiaRecorrencia(diaRecorrencia);

        repository.atualizar(rec);
        return rec;
    }
}
