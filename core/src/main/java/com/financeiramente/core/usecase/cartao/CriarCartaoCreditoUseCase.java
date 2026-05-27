package com.financeiramente.core.usecase.cartao;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class CriarCartaoCreditoUseCase {

    private final CartaoCreditoRepository cartaoRepository;

    public CriarCartaoCreditoUseCase(CartaoCreditoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public CartaoCredito executar(CriarCartaoCreditoInput input) {
        if (input.getNome() == null || input.getNome().trim().isEmpty()) {
            throw new DomainException("Nome do cartão não pode ser vazio.");
        }
        if (input.getDiaVencimento() < 1 || input.getDiaVencimento() > 28) {
            throw new DomainException("Dia de vencimento deve estar entre 1 e 28.");
        }
        if (input.getDiasParaFechamento() < 1 || input.getDiasParaFechamento() > 28) {
            throw new DomainException("Dias para fechamento deve estar entre 1 e 28.");
        }
        if (input.getLimite() != null && input.getLimite() <= 0) {
            throw new DomainException("Limite deve ser maior que zero.");
        }

        long now = System.currentTimeMillis();

        CartaoCredito.Builder builder = CartaoCredito.builder(UUID.randomUUID().toString())
                .nome(input.getNome().trim())
                .diaVencimento(input.getDiaVencimento())
                .diasParaFechamento(input.getDiasParaFechamento())
                .limite(input.getLimite())
                .bandeira(input.getBandeira())
                .criadoEm(now)
                .atualizadoEm(now);

        if (input.getIcone() != null && !input.getIcone().isEmpty()) {
            builder.icone(input.getIcone());
        }
        if (input.getCor() != null && !input.getCor().isEmpty()) {
            builder.cor(input.getCor());
        }

        CartaoCredito cartao = builder.build();
        cartaoRepository.salvar(cartao);
        return cartao;
    }
}
