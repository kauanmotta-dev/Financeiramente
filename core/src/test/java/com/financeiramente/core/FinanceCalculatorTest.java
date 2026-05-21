// package com.financeiramente.core;

// import com.financeiramente.core.domain.vo.StatusSaldo;
// import com.financeiramente.core.util.FinanceCalculator;
// import org.junit.jupiter.api.Test;

// import java.time.LocalDate;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// class FinanceCalculatorTest {

//     @Test
//     void saldoDisponivelPlanejado_deveSubtrairLimitesEReserva() {
//         List<Double> limites = Arrays.asList(500.0, 300.0);
//         double resultado = FinanceCalculator.saldoDisponivelPlanejado(3000.0, limites, 200.0);
//         assertEquals(2000.0, resultado, 0.001);
//     }

//     @Test
//     void saldoDisponivelReal_deveSubtrairGastoEReserva() {
//         double resultado = FinanceCalculator.saldoDisponivelReal(3000.0, 1200.0, 200.0);
//         assertEquals(1600.0, resultado, 0.001);
//     }

//     @Test
//     void saldoCategoria_deveRetornarLimiteMenosGasto() {
//         assertEquals(200.0, FinanceCalculator.saldoCategoria(500.0, 300.0), 0.001);
//     }

//     @Test
//     void statusCategoria_verde_quandoGastoMenorQue80Porcento() {
//         assertEquals(StatusSaldo.VERDE, FinanceCalculator.statusCategoria(500.0, 300.0));
//     }

//     @Test
//     void statusCategoria_amarelo_quandoGastoEntre80E100Porcento() {
//         assertEquals(StatusSaldo.AMARELO, FinanceCalculator.statusCategoria(500.0, 450.0));
//     }

//     @Test
//     void statusCategoria_vermelho_quandoGastoAcimaDoLimite() {
//         assertEquals(StatusSaldo.VERMELHO, FinanceCalculator.statusCategoria(500.0, 600.0));
//     }

//     @Test
//     void projecaoConclusaoMeta_retornaNull_quandoSemAportes() {
//         assertNull(FinanceCalculator.projecaoConclusaoMeta(10000.0, 0.0, LocalDate.now().minusDays(10)));
//     }

//     @Test
//     void projecaoConclusaoMeta_retornaDataFutura_comAportes() {
//         LocalDate dataInicio = LocalDate.now().minusDays(30);
//         LocalDate projecao = FinanceCalculator.projecaoConclusaoMeta(10000.0, 1000.0, dataInicio);
//         assertNotNull(projecao);
//         assertTrue(projecao.isAfter(LocalDate.now()));
//     }

//     @Test
//     void projecaoConclusaoMeta_retornaHoje_quandoMetaAtingida() {
//         LocalDate projecao = FinanceCalculator.projecaoConclusaoMeta(1000.0, 1000.0, LocalDate.now().minusDays(10));
//         assertEquals(LocalDate.now(), projecao);
//     }

//     @Test
//     void saldoDisponivelPlanejado_semLimites_retornaReceitaMinusReserva() {
//         double resultado = FinanceCalculator.saldoDisponivelPlanejado(
//                 3000.0, Collections.emptyList(), 300.0);
//         assertEquals(2700.0, resultado, 0.001);
//     }
// }
