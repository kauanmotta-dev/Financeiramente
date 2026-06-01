package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.util.DomainException;

public class EditarCompraCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;

    public EditarCompraCartaoUseCase(CompraCartaoRepository compraCartaoRepository) {
        this.compraCartaoRepository = compraCartaoRepository;
    }

    public CompraCartao executar(String compraCartaoId, RegistrarCompraCartaoInput input) {
        if (input == null) {
            throw new DomainException("Input da compra no cartão é obrigatório.");
        }

        CompraCartao existente = compraCartaoRepository.buscarPorId(compraCartaoId)
                .orElseThrow(() -> new DomainException("Compra no cartão não encontrada."));

        long atualizadoEm = System.currentTimeMillis();
        CompraCartao atualizada = CompraCartao.builder(existente.getId())
                .cartaoId(input.getCartaoId())
                .descricao(input.getDescricao())
                .valorTotal(input.getValorTotal())
                .tipo(input.getTipo())
                .totalParcelas(input.getTipo() == TipoCompraCartao.PARCELADO ? input.getNumeroParcelas() : null)
                .categoriaId(input.getCategoriaId())
                .dataCompra(input.getData())
                .diaRecorrencia(input.getTipo() == TipoCompraCartao.RECORRENTE ? input.getDiaRecorrencia() : null)
                .ativo(existente.isAtivo())
                .criadoEm(existente.getCriadoEm())
                .atualizadoEm(atualizadoEm)
                .build();

        compraCartaoRepository.atualizar(atualizada);
        return atualizada;
    }
}