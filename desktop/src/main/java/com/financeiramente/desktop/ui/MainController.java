package com.financeiramente.desktop.ui;

import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.GerarLancamentosRecorrentesUseCase;
import com.financeiramente.core.usecase.SaldoMensalResult;
import com.financeiramente.desktop.app.AppContext;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        gerarLancamentosRecorrentesDoMes();
        abrirDashboard();
    }

    /**
     * Gera automaticamente os lançamentos recorrentes do mês atual (EP-08).
     * Idempotente: não duplica lançamentos já gerados no mesmo mês.
     */
    private void gerarLancamentosRecorrentesDoMes() {
        GerarLancamentosRecorrentesUseCase uc = AppContext.get().getGerarLancamentosRecorrentesUseCase();
        LocalDate hoje = LocalDate.now();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                uc.executar(hoje.getYear(), hoje.getMonthValue());
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
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
    private void abrirRecorrentes() {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/financeiramente/desktop/fxml/recorrentes.fxml"));
            contentPane.getChildren().setAll(view);
            statusLabel.setText("Lançamentos Recorrentes");
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Recorrentes: " + e.getMessage());
        }
    }

    @FXML
    private void abrirMetas() {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource("/com/financeiramente/desktop/fxml/metas.fxml"));
            contentPane.getChildren().setAll(view);
            statusLabel.setText("Metas");
        } catch (IOException e) {
            statusLabel.setText("Erro ao abrir Metas: " + e.getMessage());
        }
    }

    /** Atualiza a barra de status com o saldo disponível do mês atual. */
    private void atualizarStatusSaldo() {
        CalcularSaldoMensalUseCase uc = AppContext.get().getCalcularSaldoMensalUseCase();
        LocalDate hoje = LocalDate.now();

        Task<SaldoMensalResult> task = new Task<>() {
            @Override
            protected SaldoMensalResult call() {
                return uc.executar(hoje.getYear(), hoje.getMonthValue());
            }
        };

        task.setOnSucceeded(e -> {
            SaldoMensalResult r = task.getValue();
            String corSaldo = r.getSaldoDisponivel() >= 0 ? "#2196F3" : "#F44336";
            statusLabel.setText(String.format(Locale.getDefault(),
                    "Dashboard — Saldo disponível: R$ %.2f", r.getSaldoDisponivel()));
            statusLabel.setStyle("-fx-padding: 4 8; -fx-background-color: #f0f0f0; " +
                    "-fx-font-size: 12; -fx-text-fill: " + corSaldo + ";");
        });

        task.setOnFailed(e -> statusLabel.setText("Financeiramente — pronto."));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
}

