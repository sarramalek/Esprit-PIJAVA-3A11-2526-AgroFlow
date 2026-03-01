package controllers;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AccueilEvenementController {


    private void chargerPage(Event event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ouvrirEvenements(Event event) {
        chargerPage(event, "AfficherEvenements.fxml");
    }

    public void ouvrirCategories(Event event) {
        chargerPage(event, "AfficherCategories.fxml");
    }

    public void ouvrirParticipations(Event event) {
        chargerPage(event, "AfficherParticipations.fxml");
    }
}
