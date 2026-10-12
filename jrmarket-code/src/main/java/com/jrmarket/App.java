package com.jrmarket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Punto de entrada JavaFX: carga la vista de acceso como primera pantalla. */
public class App extends Application {

    /** Construye y muestra la ventana inicial de inicio de sesión. */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 480, 520);
        stage.setTitle("JRMARKET v1.0 - Acceso al Sistema");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    /** Inicia el ciclo de vida de la aplicación JavaFX. */
    public static void main(String[] args) {
        launch(args);
    }
}