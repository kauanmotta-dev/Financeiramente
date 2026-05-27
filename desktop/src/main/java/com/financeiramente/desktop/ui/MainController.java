package com.financeiramente.desktop.ui;

import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;
import com.financeiramente.core.usecase.saldo.SaldoDashboardResult;
import com.financeiramente.desktop.app.AppContext;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private StackPane contentPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnLancamentos;
    @FXML private Button btnMetas;
    @FXML private Button btnRelatorios;
    @FXML private Button btnCategorias;

    @FXML
    public void initialize() {
        abrirDashboard();
        setActiveNav(btnDashboard);
    }

    private void setActiveNav(Button activeButton) {
        List.of(btnDashboard, btnLancamentos, btnMetas,
                btnRelatorios, btnCategorias).forEach(b -> {
            b.getStyleClass().removeAll("nav-button-active");
            if (!b.getStyleClass().contains("nav-button"))
                b.getStyleClass().add("nav-button");
        });
        activeButton.getStyleClass().remove("nav-button");
        activeButton.getStyleClass().add("nav-button-active");
    }

    @FXML
    public void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/financeiramente/desktop/fxml/dashboard.fxml"));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            atualizarStatusSaldo();
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void abrirCategorias() {
        setActiveNav(btnCategorias);
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/financeiramente/desktop/fxml/categorias.fxml"));
            contentPane.getChildren().setAll(view);
            statusLabel.setText("Categorias");
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Categorias: " + e.getMessage());
        }
    }

    @FXML
    private void abrirLancamentos() {
        setActiveNav(btnLancamentos);
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/financeiramente/desktop/fxml/lancamentos_list.fxml"));
            contentPane.getChildren().setAll(view);
            statusLabel.setText("Lançamentos");
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Lançamentos: " + e.getMessage());
        }
    }

    @FXML
    private void abrirMetas() {
        setActiveNav(btnMetas);
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/financeiramente/desktop/fxml/metas.fxml"));
            contentPane.getChildren().setAll(view);
            statusLabel.setText("Metas");
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Metas: " + e.getMessage());
        }
    }

    @FXML
    private void abrirRelatorios() {
        setActiveNav(btnRelatorios);
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/financeiramente/desktop/fxml/relatorios.fxml"));
            contentPane.getChildren().setAll(view);
            statusLabel.setText("Relatórios");
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Relatórios: " + e.getMessage());
        }
    }

    /** Atualiza a barra de status com o saldo disponível do mês atual. */
    private void atualizarStatusSaldo() {
        CalcularSaldoDashboardUseCase uc = AppContext.get().getCalcularSaldoDashboardUseCase();
        LocalDate hoje = LocalDate.now();

        Task<SaldoDashboardResult> task = new Task<>() {
            @Override
            protected SaldoDashboardResult call() {
                return uc.executar(hoje.getYear(), hoje.getMonthValue());
            }
        };

        task.setOnSucceeded(e -> {
            SaldoDashboardResult r = task.getValue();
            String corSaldo = r.getSaldoConta() >= 0 ? "#2196F3" : "#F44336";
            statusLabel.setText(String.format(Locale.getDefault(),
                    "Dashboard — Saldo disponível: R$ %.2f", r.getSaldoConta()));
            statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #f0f0f0; " +
                    "-fx-font-size: 12; -fx-text-fill: " + corSaldo + ";");
        });

        task.setOnFailed(e -> statusLabel.setText("Financeiramente — pronto."));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}

