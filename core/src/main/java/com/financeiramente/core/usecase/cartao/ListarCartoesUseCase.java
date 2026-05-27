package com.financeiramente.core.usecase.cartao;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.repository.CartaoCreditoRepository;

import java.util.List;

public class ListarCartoesUseCase {

    private final CartaoCreditoRepository cartaoRepository;

    public ListarCartoesUseCase(CartaoCreditoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public List<CartaoCredito> executar() {
        return cartaoRepository.listarAtivos();
    }
}
