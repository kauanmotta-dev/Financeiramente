package com.financeiramente.desktop.ui;

import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;
import com.financeiramente.core.usecase.saldo.SaldoDashboardResult;
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
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblSaldoDisponivel;
    @FXML private Label lblReceita;
    @FXML private Label lblGasto;
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

        CalcularSaldoDashboardUseCase uc = AppContext.get().getCalcularSaldoDashboardUseCase();

        Task<SaldoDashboardResult> task = new Task<>() {
            @Override
            protected SaldoDashboardResult call() {
                return uc.executar(ano, mes);
            }
        };

        task.setOnSucceeded(e -> atualizarUI(task.getValue()));
        task.setOnFailed(e -> lblSaldoDisponivel.setText("Erro ao carregar dashboard"));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void atualizarUI(SaldoDashboardResult r) {
        // Saldo disponível
        lblSaldoDisponivel.setText(moeda(r.getSaldoConta()));
        String corSaldo = r.getSaldoConta() >= 0 ? "#2196F3" : "#F44336";
        lblSaldoDisponivel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + corSaldo + ";");

        lblReceita.setText(moeda(r.getTotalReceita()));
        lblGasto.setText(moeda(r.getTotalGastoConta()));

        // Barra de progresso global
        double receita = r.getTotalReceita();
        double progresso = receita > 0 ? Math.min(1.0, r.getTotalGastoConta() / receita) : 0.0;
        pbGlobal.setProgress(progresso);

        // Tabela de resumo por blocos para reaproveitar o saldo consolidado.
        ObservableList<SaldoCategoriaRow> obsRows = FXCollections.observableArrayList(
                SaldoCategoriaRow.from("Conta", r.getTotalReceita(), r.getTotalGastoConta()),
                SaldoCategoriaRow.from("Essenciais", r.getLimiteEssenciais(), r.getGastoEssenciais()),
                SaldoCategoriaRow.from("Não essenciais", r.getLimiteNaoEssenciais(), r.getGastoNaoEssenciais())
        );
        tblCategorias.setItems(obsRows);

        // Alerta
        double limiteEssenciais = r.getLimiteEssenciais();
        double gastoEssenciais = r.getGastoEssenciais();
        if (limiteEssenciais <= 0) {
            lblAlerta.setVisible(false);
            lblAlerta.setManaged(false);
        } else {
            double pctEssenciais = gastoEssenciais / limiteEssenciais;
            if (pctEssenciais > 1.0) {
                lblAlerta.setText("⚠ Essenciais estouraram o limite planejado.");
            } else if (pctEssenciais >= 0.75) {
                lblAlerta.setText("⚠ Essenciais próximos do limite planejado.");
            } else {
                lblAlerta.setText("");
            }
            boolean mostrar = !lblAlerta.getText().isBlank();
            lblAlerta.setVisible(mostrar);
            lblAlerta.setManaged(mostrar);
        }
    }

    private String moeda(double valor) {
        return String.format(Locale.getDefault(), "R$ %.2f", valor);
    }

    // ─── Inner DTO para TableView ───────────────────────────────────────────

    public static class SaldoCategoriaRow {

        private final SimpleStringProperty statusIcon;
        private final SimpleStringProperty categoriaNome;
        private final SimpleStringProperty limiteStr;
        private final SimpleStringProperty gastoStr;
        private final SimpleStringProperty saldoStr;
        private final SimpleStringProperty progressoStr;

        private SaldoCategoriaRow(String nome, double limite, double gasto) {
            this.statusIcon     = new SimpleStringProperty(resolverIcone(limite, gasto));
            this.categoriaNome  = new SimpleStringProperty(nome);
            this.limiteStr      = new SimpleStringProperty(String.format(Locale.getDefault(), "%.2f", limite));
            this.gastoStr       = new SimpleStringProperty(String.format(Locale.getDefault(), "%.2f", gasto));
            this.saldoStr       = new SimpleStringProperty(String.format(Locale.getDefault(), "%.2f", limite - gasto));
            double pct = limite > 0 ? Math.min(100.0, (gasto / limite) * 100.0) : 0.0;
            this.progressoStr   = new SimpleStringProperty(String.format(Locale.getDefault(), "%.0f%%", pct));
        }

        public static SaldoCategoriaRow from(String nome, double limite, double gasto) {
            return new SaldoCategoriaRow(nome, limite, gasto);
        }

        /** Ícone visual baseado na proporção gasto/limite — sem exibir nome da cor. */
        private static String resolverIcone(double limite, double gasto) {
            if (limite <= 0) return gasto > 0 ? "❌" : "✅";
            double pct = gasto / limite;
            if (pct < 0.75)  return "✅";
            if (pct <= 1.00) return "⚠";
            return "❌";
        }

        public String getStatusIcon()    { return statusIcon.get(); }
        public String getCategoriaNome() { return categoriaNome.get(); }
        public String getLimiteStr()     { return limiteStr.get(); }
        public String getGastoStr()      { return gastoStr.get(); }
        public String getSaldoStr()      { return saldoStr.get(); }
        public String getProgressoStr()  { return progressoStr.get(); }
    }
}
