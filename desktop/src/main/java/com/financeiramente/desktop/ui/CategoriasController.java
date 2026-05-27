package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.usecase.categoria.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.ReordenarCategoriasUseCase;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.desktop.app.AppContext;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.List;

public class CategoriasController {

    @FXML private TreeView<Categoria> treeCategories;
    @FXML private Label lblFormTitle;
    @FXML private TextField tfNome;
    @FXML private ComboBox<TipoCategoria> cbTipo;
    @FXML private TextField tfLimite;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNovaRaiz;
    @FXML private Button btnNovaFilha;
    @FXML private Button btnDeletar;

    private final CriarCategoriaUseCase criarCategoria;
    private final EditarCategoriaUseCase editarCategoria;
    private final DeletarCategoriaUseCase deletarCategoria;
    private final ReordenarCategoriasUseCase reordenarCategorias;

    private String editandoId;
    private String paiIdSelecionado;

    public CategoriasController() {
        AppContext ctx = AppContext.get();
        this.criarCategoria = ctx.getCriarCategoriaUseCase();
        this.editarCategoria = ctx.getEditarCategoriaUseCase();
        this.deletarCategoria = ctx.getDeletarCategoriaUseCase();
        this.reordenarCategorias = ctx.getReordenarCategoriasUseCase();
    }

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(TipoCategoria.values()));
        cbTipo.getSelectionModel().selectFirst();

        configurarTreeView();
        carregarCategorias();
        limparFormulario();
    }

    private void configurarTreeView() {
        treeCategories.setShowRoot(false);
        treeCategories.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String text = item.getNome();
                    if (item.getLimiteMensal() != null) {
                        text += String.format("  [R$ %.2f/mês]", item.getLimiteMensal());
                    }
                    setText(text);
                }
            }
        });

        treeCategories.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        Categoria sel = newVal.getValue();
                        tfNome.setText(sel.getNome());
                        cbTipo.setValue(sel.getTipo());
                        tfLimite.setText(sel.getLimiteMensal() != null
                                ? String.valueOf(sel.getLimiteMensal()) : "");
                        editandoId = sel.getId();
                        paiIdSelecionado = sel.getPaiId();
                        lblFormTitle.setText("Editar: " + sel.getNome());
                        btnDeletar.setDisable(false);
                        // Se é subcategoria, o tipo é herdado — não permite edição
                        cbTipo.setDisable(sel.getPaiId() != null);
                    }
                });
    }

    private void carregarCategorias() {
        AppContext ctx = AppContext.get();
        TreeItem<Categoria> raiz = new TreeItem<>();

        List<Categoria> raizes = ctx.getCoreServices().getCategoriaRepository().listarRaizes();
        for (Categoria cat : raizes) {
            TreeItem<Categoria> grupoItem = new TreeItem<>(cat);
            grupoItem.setExpanded(true);
            for (Categoria filha : ctx.getCoreServices().getCategoriaRepository().listarFilhas(cat.getId())) {
                grupoItem.getChildren().add(new TreeItem<>(filha));
            }
            raiz.getChildren().add(grupoItem);
        }

        treeCategories.setRoot(raiz);
    }

    @FXML
    private void onNovaRaiz() {
        limparFormulario();
        cbTipo.setDisable(false);
        lblFormTitle.setText("Nova Categoria");
    }

    @FXML
    private void onNovaFilha() {
        TreeItem<Categoria> selected = treeCategories.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            mostrarErro("Selecione uma categoria para criar uma subcategoria.");
            return;
        }
        Categoria sel = selected.getValue();
        // Always use the root category as parent (enforce max 2 levels)
        String paiId = sel.getPaiId() == null ? sel.getId() : sel.getPaiId();
        limparFormulario();
        paiIdSelecionado = paiId;
        // Tipo é herdado do pai — não deve ser editado pelo usuário
        cbTipo.setDisable(true);
        lblFormTitle.setText("Nova Subcategoria");
    }

    @FXML
    private void onSalvar() {
        String nome = tfNome.getText() != null ? tfNome.getText().trim() : "";
        if (nome.isEmpty()) {
            mostrarErro("Nome da categoria é obrigatório.");
            return;
        }

        TipoCategoria tipo = cbTipo.getValue();
        if (tipo == null) {
            mostrarErro("Selecione o tipo.");
            return;
        }

        Double limite = null;
        String limiteStr = tfLimite.getText() != null ? tfLimite.getText().trim() : "";
        if (!limiteStr.isEmpty()) {
            try {
                limite = Double.parseDouble(limiteStr);
            } catch (NumberFormatException e) {
                mostrarErro("Limite mensal inválido. Use ponto como separador decimal.");
                return;
            }
        }

        try {
            if (editandoId != null) {
                editarCategoria.executar(editandoId, nome, tipo, limite, null, null);
            } else {
                criarCategoria.executar(nome, tipo, paiIdSelecionado, limite, null, null);
            }
            carregarCategorias();
            limparFormulario();
        } catch (DomainException e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        limparFormulario();
        cbTipo.setDisable(false);
    }

    @FXML
    private void onDeletar() {
        if (editandoId == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja excluir esta categoria?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Excluir categoria");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    deletarCategoria.executar(editandoId);
                    carregarCategorias();
                    limparFormulario();
                } catch (DomainException e) {
                    mostrarErro(e.getMessage());
                }
            }
        });
    }

    private void limparFormulario() {
        tfNome.clear();
        cbTipo.getSelectionModel().selectFirst();
        cbTipo.setDisable(false);
        tfLimite.clear();
        editandoId = null;
        paiIdSelecionado = null;
        lblFormTitle.setText("Nova Categoria");
        btnDeletar.setDisable(true);
        treeCategories.getSelectionModel().clearSelection();
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensagem, ButtonType.OK);
        alert.setTitle("Erro");
        alert.showAndWait();
    }
}
