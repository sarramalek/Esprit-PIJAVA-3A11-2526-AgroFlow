package controllers.Materiels; // ou votre package approprié

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ouvrirMindSpherePage {

    public static void ouvrir(Stage stage) {
        try {
            URL fxmlUrl = ouvrirMindSpherePage.class.getResource("/MaterielsInterface/MindSpherePage.fxml");

            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable : /fxml/MindSpherePage.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Erreur MindSphere");
            alert.setHeaderText("Impossible d'ouvrir la page MindSphere");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}