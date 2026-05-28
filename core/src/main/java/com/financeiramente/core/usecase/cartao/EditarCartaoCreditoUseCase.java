package com.financeiramente.core.usecase.cartao;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.util.DomainException;
import java.math.BigDecimal;

public class EditarCartaoCreditoUseCase {

    private final CartaoCreditoRepository cartaoRepository;

    public EditarCartaoCreditoUseCase(CartaoCreditoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public CartaoCredito executar(EditarCartaoCreditoInput input) {
        CartaoCredito cartao = cartaoRepository.buscarPorId(input.getId())
                .orElseThrow(() -> new DomainException("Cartão não encontrado."));

        if (input.getNome() == null || input.getNome().trim().isEmpty()) {
            throw new DomainException("Nome do cartão não pode ser vazio.");
        }
        if (input.getDiaVencimento() < 1 || input.getDiaVencimento() > 28) {
            throw new DomainException("Dia de vencimento deve estar entre 1 e 28.");
        }
        if (input.getDiasParaFechamento() < 1 || input.getDiasParaFechamento() > 27) {
            throw new DomainException("Dias para fechamento deve estar entre 1 e 27.");
        }
        if (input.getLimite() != null && input.getLimite().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Limite deve ser maior que zero.");
        }

        cartao.setNome(input.getNome().trim());
        cartao.setDiaVencimento(input.getDiaVencimento());
        cartao.setDiasParaFechamento(input.getDiasParaFechamento());
        cartao.setLimite(input.getLimite());
        cartao.setBandeira(input.getBandeira());
        if (input.getIcone() != null && !input.getIcone().isEmpty()) {
            cartao.setIcone(input.getIcone());
        }
        if (input.getCor() != null && !input.getCor().isEmpty()) {
            cartao.setCor(input.getCor());
        }
        cartao.setAtualizadoEm(System.currentTimeMillis());

        cartaoRepository.atualizar(cartao);
        return cartao;
    }
}
