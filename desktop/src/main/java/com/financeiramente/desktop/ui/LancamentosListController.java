package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.RegistrarLancamentoUseCase;
import com.financeiramente.desktop.app.AppContext;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LancamentosListController {

    @FXML private Label lblMes;
    @FXML private TableView<Lancamento> tabelaLancamentos;
    @FXML private TableColumn<Lancamento, String> colData;
    @FXML private TableColumn<Lancamento, String> colDescricao;
    @FXML private TableColumn<Lancamento, String> colTipo;
    @FXML private TableColumn<Lancamento, String> colValor;

    private final ListarLancamentosUseCase listar;
    private final DeletarLancamentoUseCase deletar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int ano;
    private int mes;

    public LancamentosListController() {
        AppContext ctx = AppContext.get();
        this.listar = ctx.getListarLancamentosUseCase();
        this.deletar = ctx.getDeletarLancamentoUseCase();
        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
    }

    @FXML
    public void initialize() {
        colData.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getData()));
        colDescricao.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDescricao()));
        colTipo.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getTipo().name().toLowerCase()));
        colValor.setCellValueFactory(p ->
                new SimpleStringProperty(String.format("R$ %.2f", p.getValue().getValor())));

        atualizarLabelMes();
        carregarLancamentos();
    }

    private void atualizarLabelMes() {
        LocalDate d = LocalDate.of(ano, mes, 1);
        String nomeMes = d.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
        lblMes.setText(nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1) + " / " + ano);
    }

    private void carregarLancamentos() {
        executor.execute(() -> {
            List<Lancamento> lista = listar.porMes(ano, mes);
            Platform.runLater(() ->
                    tabelaLancamentos.setItems(FXCollections.observableArrayList(lista)));
        });
    }

    @FXML
    private void mesAnterior() {
        if (mes == 1) { mes = 12; ano--; } else { mes--; }
        atualizarLabelMes();
        carregarLancamentos();
    }

    @FXML
    private void mesSeguinte() {
        if (mes == 12) { mes = 1; ano++; } else { mes++; }
        atualizarLabelMes();
        carregarLancamentos();
    }

    @FXML
    private void novoLancamento() {
        abrirFormulario(null);
    }

    @FXML
    private void editarSelecionado() {
        Lancamento sel = tabelaLancamentos.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        abrirFormulario(sel);
    }

    @FXML
    private void excluirSelecionado() {
        Lancamento sel = tabelaLancamentos.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja excluir \"" + sel.getDescricao() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Excluir lançamento");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                executor.execute(() -> {
                    try {
                        deletar.executar(sel.getId());
                        List<Lancamento> lista = listar.porMes(ano, mes);
                        Platform.runLater(() ->
                                tabelaLancamentos.setItems(FXCollections.observableArrayList(lista)));
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
                        });
                    }
                });
            }
        });
    }

    private void abrirFormulario(Lancamento lancamento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/financeiramente/desktop/fxml/lancamento_form.fxml"));
            Parent root = loader.load();
            LancamentoFormController ctrl = loader.getController();
            if (lancamento != null) ctrl.carregarParaEdicao(lancamento);
            ctrl.setOnSucesso(v -> carregarLancamentos());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(lancamento == null ? "Novo Lançamento" : "Editar Lançamento");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Erro ao abrir formulário: " + ex.getMessage()).show();
        }
    }
}
