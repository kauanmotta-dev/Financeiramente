package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.db.AppLogger;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;
import com.financeiramente.core.util.DataValidator;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.core.util.FinanceCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class RegistrarCompraCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;
    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final TransactionManager transactionManager;
    private final AppLogger logger;

    public RegistrarCompraCartaoUseCase(CompraCartaoRepository compraCartaoRepository,
                                        ResolverFaturaParaLancamentoUseCase resolverFatura,
                                        RegistrarLancamentoUseCase registrarLancamento,
                                        TransactionManager transactionManager,
                                        AppLogger logger) {
        this.compraCartaoRepository = compraCartaoRepository;
        this.resolverFatura = resolverFatura;
        this.registrarLancamento = registrarLancamento;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    public CompraCartao executar(RegistrarCompraCartaoInput input) {
        validarInput(input);

        final CompraCartao[] compraCriada = new CompraCartao[1];

        try {
            transactionManager.executeInTransaction(() -> {
                CompraCartao compra = criarCompra(input);
                compraCartaoRepository.salvar(compra);

                if (compra.getTipo() == TipoCompraCartao.CREDITO) {
                    registrarCredito(input, compra);
                } else if (compra.getTipo() == TipoCompraCartao.PARCELADO) {
                    registrarParcelado(input, compra);
                } else {
                    registrarRecorrente(input, compra);
                }

                compraCriada[0] = compra;
            });
        } catch (RuntimeException exception) {
            logger.error("Rollback ao registrar compra no cartao.", exception);
            throw exception;
        }

        return compraCriada[0];
    }

    private void validarInput(RegistrarCompraCartaoInput input) {
        if (input == null) {
            throw new DomainException("Input da compra no cartão é obrigatório.");
        }
        DomainException.requireNonBlank(input.getCartaoId(), "Cartão da compra é obrigatório.");
        if (input.getTipo() == null) {
            throw new DomainException("Tipo da compra no cartão é obrigatório.");
        }
        DataValidator.validarData(input.getData());
        DomainException.requireNonBlank(input.getDescricao(), "Descrição da compra é obrigatória.");
        if (input.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Valor da compra deve ser maior que zero.");
        }

        if (input.getTipo() == TipoCompraCartao.PARCELADO && input.getNumeroParcelas() < 2) {
            throw new DomainException("Compra parcelada deve ter pelo menos 2 parcelas.");
        }
        if (input.getTipo() == TipoCompraCartao.RECORRENTE) {
            Integer diaRecorrencia = input.getDiaRecorrencia();
            if (diaRecorrencia == null || diaRecorrencia < 1 || diaRecorrencia > 28) {
                throw new DomainException("Dia da recorrência deve estar entre 1 e 28.");
            }
        }
    }

    private CompraCartao criarCompra(RegistrarCompraCartaoInput input) {
        long now = System.currentTimeMillis();

        return CompraCartao.builder(UUID.randomUUID().toString())
                .cartaoId(input.getCartaoId())
                .descricao(input.getDescricao())
                .valorTotal(input.getValorTotal())
                .tipo(input.getTipo())
                .totalParcelas(input.getTipo() == TipoCompraCartao.PARCELADO ? input.getNumeroParcelas() : null)
                .categoriaId(input.getCategoriaId())
                .dataCompra(input.getData())
                .diaRecorrencia(input.getTipo() == TipoCompraCartao.RECORRENTE ? input.getDiaRecorrencia() : null)
                .ativo(input.getTipo() == TipoCompraCartao.RECORRENTE)
                .criadoEm(now)
                .atualizadoEm(now)
                .build();
    }

    private void registrarCredito(RegistrarCompraCartaoInput input, CompraCartao compra) {
        Fatura fatura = resolverFatura.executar(input.getCartaoId(), input.getData());

        RegistrarLancamentoInput lancamentoInput = new RegistrarLancamentoInput(
                input.getValorTotal(),
                TipoLancamento.DESPESA,
                input.getData(),
                input.getDescricao(),
                input.getCategoriaId(),
                fatura.getId(),
                compra.getId(),
                input.getTags()
        );

        registrarLancamento.executar(lancamentoInput);
    }

    private void registrarParcelado(RegistrarCompraCartaoInput input, CompraCartao compra) {
        int parcelas = input.getNumeroParcelas();
        BigDecimal[] valores = FinanceCalculator.distribuirParcelas(input.getValorTotal(), parcelas);
        LocalDate dataBase = LocalDate.parse(input.getData());

        for (int i = 0; i < parcelas; i++) {
            LocalDate dataParcela = dataBase.plusMonths(i);
            String descricaoParcela = input.getDescricao() + " (" + (i + 1) + "/" + parcelas + ")";
            Fatura fatura = resolverFatura.executar(input.getCartaoId(), dataParcela.toString());

            RegistrarLancamentoInput lancamentoInput = new RegistrarLancamentoInput(
                    valores[i],
                    TipoLancamento.DESPESA,
                    dataParcela.toString(),
                    descricaoParcela,
                    input.getCategoriaId(),
                    fatura.getId(),
                    compra.getId(),
                    input.getTags(),
                    parcelas
            );

            registrarLancamento.executar(lancamentoInput);
        }
    }

    private void registrarRecorrente(RegistrarCompraCartaoInput input, CompraCartao compra) {
        LocalDate dataBase = LocalDate.parse(input.getData());
        LocalDate dataCobranca = dataBase.withDayOfMonth(input.getDiaRecorrencia());
        if (dataBase.isAfter(dataCobranca)) {
            dataCobranca = dataCobranca.plusMonths(1);
        }

        Fatura fatura = resolverFatura.executar(input.getCartaoId(), dataCobranca.toString());

        RegistrarLancamentoInput lancamentoInput = new RegistrarLancamentoInput(
                input.getValorTotal(),
                TipoLancamento.DESPESA,
                dataCobranca.toString(),
                input.getDescricao(),
                input.getCategoriaId(),
                fatura.getId(),
                compra.getId(),
                input.getTags()
        );

        registrarLancamento.executar(lancamentoInput);
    }
}
