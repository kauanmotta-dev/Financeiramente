package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.vo.StatusSaldo;
import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.SaldoCategoria;
import com.financeiramente.core.usecase.SaldoMensalResult;
import com.financeiramente.desktop.app.AppContext;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblSaldoDisponivel;
    @FXML private Label lblReceita;
    @FXML private Label lblGasto;
    @FXML private Label lblProvisoes;
    @FXML private Label lblReserva;
    @FXML private ProgressBar pbGlobal;
    @FXML private Label lblAlerta;
    @FXML private TableView<SaldoCategoriaRow> tblCategorias;

    @FXML
    public void initialize() {
        carregarDashboard();
    }

    public void carregarDashboard() {
        LocalDate hoje = LocalDate.now();
        int ano = hoje.getYear();
        int mes = hoje.getMonthValue();

        CalcularSaldoMensalUseCase uc = AppContext.get().getCalcularSaldoMensalUseCase();

        Task<SaldoMensalResult> task = new Task<>() {
            @Override
            protected SaldoMensalResult call() {
                return uc.executar(ano, mes);
            }
        };

        task.setOnSucceeded(e -> atualizarUI(task.getValue()));
        task.setOnFailed(e -> lblSaldoDisponivel.setText("Erro ao carregar dashboard"));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void atualizarUI(SaldoMensalResult r) {
        // Saldo disponível
        lblSaldoDisponivel.setText(moeda(r.getSaldoDisponivel()));
        String corSaldo = r.getSaldoDisponivel() >= 0 ? "#2196F3" : "#F44336";
        lblSaldoDisponivel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + corSaldo + ";");

        lblReceita.setText(moeda(r.getReceitaRealizada()));
        lblGasto.setText(moeda(r.getTotalGasto()));
        lblProvisoes.setText(moeda(r.getTotalProvisoesMensais()));
        lblReserva.setText(moeda(r.getReservaImprevisto()));

        // Barra de progresso global
        double receita = r.getReceitaRealizada();
        double progresso = receita > 0 ? Math.min(1.0, r.getTotalGasto() / receita) : 0.0;
        pbGlobal.setProgress(progresso);

        // Tabela de categorias
        List<SaldoCategoriaRow> rows = r.getSaldosPorCategoria().stream()
                .map(SaldoCategoriaRow::new)
                .collect(Collectors.toList());
        ObservableList<SaldoCategoriaRow> obsRows = FXCollections.observableArrayList(rows);
        tblCategorias.setItems(obsRows);

        // Alerta
        List<SaldoCategoria> emAlerta = r.getSaldosPorCategoria().stream()
                .filter(s -> s.getStatus() == StatusSaldo.AMARELO || s.getStatus() == StatusSaldo.VERMELHO)
                .collect(Collectors.toList());

        if (emAlerta.isEmpty()) {
            lblAlerta.setVisible(false);
            lblAlerta.setManaged(false);
        } else {
            long estouradas = emAlerta.stream()
                    .filter(s -> s.getStatus() == StatusSaldo.VERMELHO)
                    .count();
            String nomes = emAlerta.stream()
                    .map(SaldoCategoria::getCategoriaNome)
                    .collect(Collectors.joining(", "));
            if (estouradas > 0) {
                lblAlerta.setText("⚠ Categoria(s) estourada(s): " + nomes);
            } else {
                lblAlerta.setText("⚠ Categoria(s) próximas do limite: " + nomes);
            }
            lblAlerta.setVisible(true);
            lblAlerta.setManaged(true);
        }
    }

    private String moeda(double valor) {
        return String.format(Locale.getDefault(), "R$ %.2f", valor);
    }

    // ─── Inner DTO para TableView ───────────────────────────────────────────

    public static class SaldoCategoriaRow {

        private final SimpleStringProperty statusStr;
        private final SimpleStringProperty categoriaNome;
        private final SimpleStringProperty limiteStr;
        private final SimpleStringProperty gastoStr;
        private final SimpleStringProperty saldoStr;
        private final SimpleStringProperty progressoStr;

        public SaldoCategoriaRow(SaldoCategoria sc) {
            this.statusStr     = new SimpleStringProperty(traduzirStatus(sc.getStatus()));
            this.categoriaNome = new SimpleStringProperty(sc.getCategoriaNome());
            this.limiteStr     = new SimpleStringProperty(String.format(Locale.getDefault(), "%.2f", sc.getLimite()));
            this.gastoStr      = new SimpleStringProperty(String.format(Locale.getDefault(), "%.2f", sc.getGastoRealizado()));
            this.saldoStr      = new SimpleStringProperty(String.format(Locale.getDefault(), "%.2f", sc.getSaldo()));
            double pct = sc.getLimite() > 0 ? Math.min(100.0, (sc.getGastoRealizado() / sc.getLimite()) * 100.0) : 0.0;
            this.progressoStr  = new SimpleStringProperty(String.format(Locale.getDefault(), "%.0f%%", pct));
        }

        private static String traduzirStatus(StatusSaldo status) {
            switch (status) {
                case VERDE:    return "✅ Verde";
                case AMARELO:  return "⚠ Amarelo";
                case VERMELHO: return "❌ Vermelho";
                default:       return "-";
            }
        }

        public String getStatusStr()     { return statusStr.get(); }
        public String getCategoriaNome() { return categoriaNome.get(); }
        public String getLimiteStr()     { return limiteStr.get(); }
        public String getGastoStr()      { return gastoStr.get(); }
        public String getSaldoStr()      { return saldoStr.get(); }
        public String getProgressoStr()  { return progressoStr.get(); }
    }
}
