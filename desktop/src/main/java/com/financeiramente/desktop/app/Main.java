package com.financeiramente.desktop.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Ponto de entrada da aplicação Desktop JavaFX.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/financeiramente/desktop/fxml/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Financeiramente");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
