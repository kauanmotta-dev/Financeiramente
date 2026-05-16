package com.financeiramente.desktop.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("Financeiramente — pronto.");
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
}

