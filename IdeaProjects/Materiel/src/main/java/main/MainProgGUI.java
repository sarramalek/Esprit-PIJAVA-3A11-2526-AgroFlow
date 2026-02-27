package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainProgGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Charge le fichier FXML pour l'accueil du module matériel
       //FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilMateriel.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgricoleAffichageMachine.fxml"));
         //FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgricoleAffichageAchat.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("AGROFLOW - Gestion Agricole");
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.show();
    }
}