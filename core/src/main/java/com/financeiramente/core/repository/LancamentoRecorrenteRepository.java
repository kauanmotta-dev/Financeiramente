package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import java.util.List;

public interface LancamentoRecorrenteRepository {
    void salvar(LancamentoRecorrente recorrente);
    void atualizar(LancamentoRecorrente recorrente);
    void desativar(String id);
    List<LancamentoRecorrente> listarAtivos();
}
