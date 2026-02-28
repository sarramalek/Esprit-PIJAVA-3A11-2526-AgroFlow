package controllers.Materiels; // ou votre package approprié

import javafx.fxml.FXMLLoader;
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

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("MindSphere IoT");
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