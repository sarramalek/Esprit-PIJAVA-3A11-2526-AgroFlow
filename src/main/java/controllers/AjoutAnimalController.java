package controllers;

import entities.Sexe;
import entities.animaux;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceAnimal;
import services.FoodApiService; // Assure-toi que cette classe est créée
import java.util.List;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjoutAnimalController {

    @FXML private TextField tfNom;
    @FXML private ComboBox<String> comboEspece; // Remplacé TextField par ComboBox
    @FXML private TextField tfPoids;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Sexe> cbSexe;
     private ServiceAnimal service = new ServiceAnimal();
    private FoodApiService foodApi = new FoodApiService(); // Corrige l'erreur 'foodApi'
    @FXML private ListView<String> lvSuggestions; // Corrige l'erreur 'lvSuggestions'

    @FXML
    public void initialize() {
        // 1. Remplit le ComboBox des espèces
        comboEspece.setItems(FXCollections.observableArrayList(
                "Chien", "Chat", "Vache", "Chèvre", "Mouton", "Cheval"
        ));

        // 2. Remplit le ComboBox des sexes
        if (cbSexe != null) {
            cbSexe.getItems().setAll(Sexe.values());
        }



    }



    @FXML
    void handleAjouter(ActionEvent event) {
        if (estValide()) {
            try {
                animaux a = new animaux();
                a.setNom(tfNom.getText());
                a.setEspece(comboEspece.getValue()); // Utilise la valeur du ComboBox
                a.setPoids(Float.parseFloat(tfPoids.getText()));
                a.setSexe(cbSexe.getValue());
                a.setDate_naissance(java.sql.Date.valueOf(dpDate.getValue()));

                service.ajouter(a);
                retourListe(event);
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean estValide() {
        StringBuilder messageErreur = new StringBuilder();

        if (tfNom.getText().trim().isEmpty() || comboEspece.getValue() == null ||
                tfPoids.getText().trim().isEmpty() || dpDate.getValue() == null || cbSexe.getValue() == null) {
            messageErreur.append("Tous les champs doivent être remplis.\n");
        }

        if (!tfNom.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur.append("Le nom ne doit contenir que des lettres.\n");
        }

        try {
            float poids = Float.parseFloat(tfPoids.getText());
            if (poids <= 0) messageErreur.append("Le poids doit être supérieur à 0.\n");
        } catch (NumberFormatException e) {
            messageErreur.append("Le poids doit être un nombre valide.\n");
        }

        if (dpDate.getValue() != null && dpDate.getValue().isAfter(LocalDate.now())) {
            messageErreur.append("La date de naissance ne peut pas être dans le futur.\n");
        }

        if (messageErreur.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setContentText(messageErreur.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // --- NAVIGATION ---
    @FXML void retourListe(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerAnimaux(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerMateriels(ActionEvent event) { changerScene(event, "AfficherMateriels.fxml"); }
    @FXML void naviguerStocks(ActionEvent event) { changerScene(event, "AfficherStocks.fxml"); }
    @FXML void naviguerTerrains(ActionEvent event) { changerScene(event, "AfficherTerrains.fxml"); }
    @FXML void naviguerEvenements(ActionEvent event) { changerScene(event, "AfficherEvenements.fxml"); }
    @FXML void naviguerUsers(ActionEvent event) { changerScene(event, "AfficherUsers.fxml"); }

    private void changerScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlFile + " : " + e.getMessage());
        }
    }
}