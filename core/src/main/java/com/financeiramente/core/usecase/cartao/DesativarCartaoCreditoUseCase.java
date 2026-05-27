package com.financeiramente.core.usecase.cartao;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.util.DomainException;

public class DesativarCartaoCreditoUseCase {

    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;

    public DesativarCartaoCreditoUseCase(CartaoCreditoRepository cartaoRepository,
                                         FaturaRepository faturaRepository) {
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
    }

    public void executar(String cartaoId) {
        CartaoCredito cartao = cartaoRepository.buscarPorId(cartaoId)
                .orElseThrow(() -> new DomainException("Cartão não encontrado."));

        if (faturaRepository.existeFaturaComStatus(cartaoId, StatusFatura.ABERTO)
                || faturaRepository.existeFaturaComStatus(cartaoId, StatusFatura.FECHADO)) {
            throw new DomainException("Cartão possui faturas em aberto.");
        }

        cartaoRepository.desativar(cartao.getId(), System.currentTimeMillis());
    }
}
