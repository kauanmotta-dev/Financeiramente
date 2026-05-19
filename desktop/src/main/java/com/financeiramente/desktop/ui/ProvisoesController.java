package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.usecase.CriarProvisaoUseCase;
import com.financeiramente.core.usecase.DesativarProvisaoUseCase;
import com.financeiramente.core.usecase.EditarProvisaoUseCase;
import com.financeiramente.desktop.app.AppContext;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProvisoesController {

    @FXML private ListView<Provisao> lvProvisoes;
    @FXML private Label lblTituloForm;
    @FXML private TextField tfNome;
    @FXML private TextField tfTotalAnual;
    @FXML private TextField tfValorMensal;
    @FXML private Spinner<String> spCategoria;
    @FXML private Label lblErro;
    @FXML private Label lblStatus;
    @FXML private javafx.scene.control.Button btnEditar;
    @FXML private javafx.scene.control.Button btnDesativar;

    private final CriarProvisaoUseCase criarUseCase;
    private final EditarProvisaoUseCase editarUseCase;
    private final DesativarProvisaoUseCase desativarUseCase;
    private final ProvisaoRepository repository;
    private final CategoriaRepository categoriaRepository;

    private List<Categoria> categorias = new ArrayList<>();
    private String editandoId; // null = novo

    public ProvisoesController() {
        AppContext ctx = AppContext.get();
        this.criarUseCase        = ctx.getCriarProvisaoUseCase();
        this.editarUseCase       = ctx.getEditarProvisaoUseCase();
        this.desativarUseCase    = ctx.getDesativarProvisaoUseCase();
        this.repository          = ctx.getProvisaoRepository();
        this.categoriaRepository = ctx.getCategoriaRepository();
    }

    @FXML
    public void initialize() {
        configurarListView();
        carregarDados();

        lvProvisoes.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean temSelecao = sel != null;
            btnEditar.setDisable(!temSelecao);
            btnDesativar.setDisable(!temSelecao);
        });

        limparFormulario();
    }

    private void configurarListView() {
        lvProvisoes.setCellFactory(lv -> new ListCell<Provisao>() {
            @Override
            protected void updateItem(Provisao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s  |  Saldo: R$ %.2f  |  R$ %.2f/mês",
                            item.getNome(),
                            item.getSaldoAcumulado(),
                            item.getValorMensal()));
                }
            }
        });
    }

    private void carregarDados() {
        Task<List<Object>> task = new Task<>() {
            @Override
            protected List<Object> call() {
                List<Object> result = new ArrayList<>();
                result.add(repository.listarAtivas());
                result.add(categoriaRepository.listarTodas());
                return result;
            }
        };
        task.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<Provisao> provs = (List<Provisao>) task.getValue().get(0);
            @SuppressWarnings("unchecked")
            List<Categoria> cats = (List<Categoria>) task.getValue().get(1);

            lvProvisoes.getItems().setAll(provs);
            categorias = cats;

            List<String> nomes = new ArrayList<>();
            nomes.add("Nenhuma");
            for (Categoria c : cats) nomes.add(c.getNome());
            spCategoria.setValueFactory(
                    new SpinnerValueFactory.ListSpinnerValueFactory<>(
                            FXCollections.observableArrayList(nomes)));
        });
        task.setOnFailed(e -> lblStatus.setText("Erro ao carregar dados."));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void novaProvisao() {
        editandoId = null;
        limparFormulario();
        lblTituloForm.setText("Nova Provisão");
    }

    @FXML
    private void editarSelecionado() {
        Provisao sel = lvProvisoes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        editandoId = sel.getId();
        lblTituloForm.setText("Editar Provisão");
        tfNome.setText(sel.getNome());
        tfTotalAnual.setText(String.format(Locale.US, "%.2f", sel.getTotalAnual()));
        tfValorMensal.setText(String.format(Locale.US, "%.2f", sel.getValorMensal()));
        // Selecionar categoria
        if (sel.getCategoriaId() != null) {
            for (int i = 0; i < categorias.size(); i++) {
                if (categorias.get(i).getId().equals(sel.getCategoriaId())) {
                    spCategoria.getValueFactory().setValue(categorias.get(i).getNome());
                    break;
                }
            }
        } else {
            spCategoria.getValueFactory().setValue("Nenhuma");
        }
        lblErro.setVisible(false);
    }

    @FXML
    private void desativarSelecionado() {
        Provisao sel = lvProvisoes.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Desativar Provisão");
        confirm.setHeaderText("Desativar \"" + sel.getNome() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        desativarUseCase.executar(sel.getId());
                        return null;
                    }
                };
                task.setOnSucceeded(e -> {
                    lvProvisoes.getItems().remove(sel);
                    limparFormulario();
                    lblStatus.setText("Provisão desativada.");
                });
                task.setOnFailed(e -> lblStatus.setText("Erro ao desativar."));
                Thread t = new Thread(task);
                t.setDaemon(true);
                t.start();
            }
        });
    }

    @FXML
    private void salvar() {
        lblErro.setVisible(false);
        String nome = tfNome.getText().trim();
        String totalAnualStr = tfTotalAnual.getText().trim();
        String valorMensalStr = tfValorMensal.getText().trim();

        if (nome.isEmpty()) {
            lblErro.setText("Nome é obrigatório.");
            lblErro.setVisible(true);
            return;
        }

        double totalAnual;
        double valorMensal;
        try {
            totalAnual = Double.parseDouble(totalAnualStr.replace(',', '.'));
            valorMensal = Double.parseDouble(valorMensalStr.replace(',', '.'));
        } catch (NumberFormatException e) {
            lblErro.setText("Valores inválidos.");
            lblErro.setVisible(true);
            return;
        }

        // Categoria: "Nenhuma" → null
        String catNome = spCategoria.getValue();
        String categoriaId = null;
        if (catNome != null && !catNome.equals("Nenhuma")) {
            for (Categoria c : categorias) {
                if (c.getNome().equals(catNome)) {
                    categoriaId = c.getId();
                    break;
                }
            }
        }

        final String finalCategoriaId = categoriaId;
        final String idEdicao = editandoId;

        Task<Provisao> task = new Task<>() {
            @Override
            protected Provisao call() {
                if (idEdicao == null) {
                    return criarUseCase.executar(nome, totalAnual, valorMensal, finalCategoriaId);
                } else {
                    return editarUseCase.executar(idEdicao, nome, totalAnual, valorMensal, finalCategoriaId);
                }
            }
        };
        task.setOnSucceeded(e -> {
            carregarDados();
            limparFormulario();
            lblStatus.setText("Provisão salva com sucesso.");
        });
        task.setOnFailed(e -> {
            String msg = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
            lblErro.setText(msg);
            lblErro.setVisible(true);
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void cancelar() {
        limparFormulario();
    }

    private void limparFormulario() {
        editandoId = null;
        lblTituloForm.setText("Nova Provisão");
        tfNome.clear();
        tfTotalAnual.clear();
        tfValorMensal.clear();
        if (spCategoria.getValueFactory() != null) {
            spCategoria.getValueFactory().setValue("Nenhuma");
        }
        lblErro.setVisible(false);
        btnEditar.setDisable(true);
        btnDesativar.setDisable(true);
        lvProvisoes.getSelectionModel().clearSelection();
    }
}
