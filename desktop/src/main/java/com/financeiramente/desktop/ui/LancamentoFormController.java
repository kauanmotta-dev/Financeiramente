package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoInput;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.desktop.app.AppContext;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LancamentoFormController {

    @FXML private Label lblTitulo;
    @FXML private RadioButton rbDespesa;
    @FXML private RadioButton rbReceita;
    @FXML private ToggleGroup tgTipo;
    @FXML private TextField tfValor;
    @FXML private TextField tfDescricao;
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private DatePicker dpData;
    @FXML private Label lblErro;

    private final RegistrarLancamentoUseCase registrar;
    private final EditarLancamentoUseCase editar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String editandoId;
    private Consumer<Void> onSucessoCallback;

    public LancamentoFormController() {
        AppContext ctx = AppContext.get();
        this.registrar = ctx.getRegistrarLancamentoUseCase();
        this.editar = ctx.getEditarLancamentoUseCase();
    }

    @FXML
    public void initialize() {
        dpData.setValue(LocalDate.now());
        tfValor.requestFocus();

        cbCategoria.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
        cbCategoria.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });

        // Carrega subcategorias compatíveis com DESPESA por padrão
        executor.execute(() -> {
            List<Categoria> cats = subcategoriasPorTipo(TipoLancamento.DESPESA);
            Platform.runLater(() -> cbCategoria.setItems(FXCollections.observableArrayList(cats)));
        });

        // Recarrega categorias quando o tipo mudar
        tgTipo.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) return;
            TipoLancamento tipo = (newT == rbReceita) ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;
            executor.execute(() -> {
                List<Categoria> cats = subcategoriasPorTipo(tipo);
                Platform.runLater(() -> cbCategoria.setItems(FXCollections.observableArrayList(cats)));
            });
        });
    }

    private List<Categoria> subcategoriasPorTipo(TipoLancamento tipo) {
        return AppContext.get().getCoreServices().getCategoriaRepository().listarTodas().stream()
                .filter(c -> c.getPaiId() != null)
                .filter(c -> c.getTipo().isCompativelCom(tipo))
                .collect(Collectors.toList());
    }

    public void carregarParaEdicao(Lancamento lancamento) {
        this.editandoId = lancamento.getId();
        lblTitulo.setText("Editar Lançamento");
        tfValor.setText(String.valueOf(lancamento.getValor()));
        tfDescricao.setText(lancamento.getDescricao());
        dpData.setValue(LocalDate.parse(lancamento.getData()));
        if (lancamento.getTipo() == TipoLancamento.RECEITA) {
            rbReceita.setSelected(true);
        } else {
            rbDespesa.setSelected(true);
        }
        // Selecionar categoria (apenas subcategorias compatíveis com o tipo)
        executor.execute(() -> {
            List<Categoria> cats = subcategoriasPorTipo(lancamento.getTipo());
            Platform.runLater(() -> {
                cbCategoria.setItems(FXCollections.observableArrayList(cats));
                cats.stream()
                        .filter(c -> c.getId().equals(lancamento.getCategoriaId()))
                        .findFirst()
                        .ifPresent(c -> cbCategoria.setValue(c));
            });
        });
    }

    public void setOnSucesso(Consumer<Void> callback) {
        this.onSucessoCallback = callback;
    }

    @FXML
    private void salvar() {
        lblErro.setText("");

        String valorStr = tfValor.getText().trim().replace(",", ".");
        String descricao = tfDescricao.getText().trim();

        BigDecimal valor;
        try {
            valor = new BigDecimal(valorStr.replace(',', '.'));
            if (valor.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            lblErro.setText("Valor inválido. Informe um número maior que zero.");
            return;
        }
        if (descricao.isEmpty()) {
            lblErro.setText("Descrição é obrigatória.");
            return;
        }
        Categoria categoria = cbCategoria.getValue();
        if (categoria == null) {
            lblErro.setText("Selecione uma categoria.");
            return;
        }

        TipoLancamento tipo = rbReceita.isSelected() ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;
        String data = dpData.getValue() != null
                ? dpData.getValue().toString() : LocalDate.now().toString();

        RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                valor, tipo, data, descricao, categoria.getId(), null, Collections.emptyList());
        executor.execute(() -> {
            try {
                if (editandoId != null) {
                    editar.executar(editandoId, input);
                } else {
                    registrar.executar(input);
                }
                Platform.runLater(() -> {
                    if (onSucessoCallback != null) onSucessoCallback.accept(null);
                    fechar();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> lblErro.setText(ex.getMessage()));
            }
        });
    }

    @FXML
    private void cancelar() {
        fechar();
    }

    private void fechar() {
        if (tfValor.getScene() != null && tfValor.getScene().getWindow() instanceof Stage) {
            ((Stage) tfValor.getScene().getWindow()).close();
        }
        executor.shutdown();
    }
}
