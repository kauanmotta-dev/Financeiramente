package com.financeiramente.core;

import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.StatusSaldo;
import com.financeiramente.core.usecase.fatura.PagamentoFaturaResult;
import com.financeiramente.core.util.FinanceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceCalculatorTest {

	@Test
	void saldoCategoria_deveRetornarLimiteMenosGasto() {
		assertEquals(BigDecimal.valueOf(200.0), FinanceCalculator.saldoCategoria(BigDecimal.valueOf(500.0), BigDecimal.valueOf(300.0)));
	}

	@Test
	void statusCategoria_verde_quandoGastoMenorQue75Porcento() {
		assertEquals(StatusSaldo.VERDE, FinanceCalculator.statusCategoria(BigDecimal.valueOf(500.0), BigDecimal.valueOf(300.0)));
	}

	@Test
	void statusCategoria_amarelo_quandoGastoEntre75E100Porcento() {
		assertEquals(StatusSaldo.AMARELO, FinanceCalculator.statusCategoria(BigDecimal.valueOf(500.0), BigDecimal.valueOf(450.0)));
	}

	@Test
	void statusCategoria_vermelho_quandoGastoAcimaDoLimite() {
		assertEquals(StatusSaldo.VERMELHO, FinanceCalculator.statusCategoria(BigDecimal.valueOf(500.0), BigDecimal.valueOf(600.0)));
	}

	@Test
	void statusCategoria_limiteZeroComGasto_deveSerVermelho() {
		assertEquals(StatusSaldo.VERMELHO, FinanceCalculator.statusCategoria(BigDecimal.ZERO, BigDecimal.ONE));
	}

	@Test
	void calcularPercentualLimitado_deveRespeitarFaixaZeroACem() {
		assertEquals(0, FinanceCalculator.calcularPercentualLimitado(BigDecimal.valueOf(10.0), BigDecimal.ZERO));
		assertEquals(40, FinanceCalculator.calcularPercentualLimitado(BigDecimal.valueOf(40.0), BigDecimal.valueOf(100.0)));
		assertEquals(100, FinanceCalculator.calcularPercentualLimitado(BigDecimal.valueOf(120.0), BigDecimal.valueOf(100.0)));
	}

	@Test
	void projecaoConclusaoMeta_retornaNull_quandoSemAportes() {
		assertNull(FinanceCalculator.projecaoConclusaoMeta(BigDecimal.valueOf(10000.0), BigDecimal.ZERO, LocalDate.now().minusDays(10)));
	}

	@Test
	void projecaoConclusaoMeta_retornaDataFutura_comAportes() {
		LocalDate dataInicio = LocalDate.now().minusDays(40);
		LocalDate projecao = FinanceCalculator.projecaoConclusaoMeta(BigDecimal.valueOf(10000.0), BigDecimal.valueOf(1000.0), dataInicio);
		assertNotNull(projecao);
		assertTrue(!projecao.isBefore(LocalDate.now()));
	}

	@Test
	void projecaoConclusaoMeta_retornaHoje_quandoMetaAtingida() {
		LocalDate projecao = FinanceCalculator.projecaoConclusaoMeta(BigDecimal.valueOf(1000.0), BigDecimal.valueOf(1000.0), LocalDate.now().minusDays(10));
		assertEquals(LocalDate.now(), projecao);
	}

	@Test
	void calcularPagamentoFatura_quandoParcial() {
		PagamentoFaturaResult result = FinanceCalculator.calcularPagamentoFatura(BigDecimal.valueOf(500.0), BigDecimal.valueOf(300.0));
		assertEquals(StatusFatura.PAGO_PARCIAL, result.getStatusResultante());
		assertEquals(new BigDecimal("200.00"), result.getSaldoDevedor());
	}

	@Test
	void calcularPagamentoFatura_quandoQuitada() {
		PagamentoFaturaResult result = FinanceCalculator.calcularPagamentoFatura(BigDecimal.valueOf(500.0), BigDecimal.valueOf(500.0));
		assertEquals(StatusFatura.PAGO, result.getStatusResultante());
		assertEquals(BigDecimal.ZERO.setScale(2), result.getSaldoDevedor());
	}

	@Test
	void distribuirParcelas_devePreservarSomaExata() {
		BigDecimal[] parcelas = FinanceCalculator.distribuirParcelas(BigDecimal.valueOf(100.0), 3);
		assertArrayEquals(new BigDecimal[] {
			new BigDecimal("33.33"),
			new BigDecimal("33.33"),
			new BigDecimal("33.34")
		}, parcelas);
	}

	@Test
	void distribuirParcelas_deveFalharComParametrosInvalidos() {
		assertThrows(IllegalArgumentException.class, () -> FinanceCalculator.distribuirParcelas(BigDecimal.ZERO, 3));
		assertThrows(IllegalArgumentException.class, () -> FinanceCalculator.distribuirParcelas(BigDecimal.valueOf(100.0), 1));
	}
}
