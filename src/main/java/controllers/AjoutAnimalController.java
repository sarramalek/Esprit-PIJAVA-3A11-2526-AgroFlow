package controllers;

import entities.Sexe;
import entities.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceAnimal;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjoutAnimalController {

    @FXML private TextField tfNom;
    @FXML private TextField tfEspece;
    @FXML private TextField tfPoids;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Sexe> cbSexe;

    private ServiceAnimal service = new ServiceAnimal();

    @FXML
    public void initialize() {
        // Remplit le ComboBox au chargement de la page
        if (cbSexe != null) {
            cbSexe.getItems().setAll(Sexe.values());
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        // On utilise ta méthode de validation Popup
        if (estValide()) {
            try {
                animaux a = new animaux();
                a.setNom(tfNom.getText());
                a.setEspece(tfEspece.getText());
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

    // TA MÉTHODE DE VALIDATION EN POPUP
    private boolean estValide() {
        String messageErreur = "";

        // 1. Vérification des champs vides
        if (tfNom.getText().trim().isEmpty() || tfEspece.getText().trim().isEmpty() ||
                tfPoids.getText().trim().isEmpty() || dpDate.getValue() == null || cbSexe.getValue() == null) {
            messageErreur += "Tous les champs doivent être remplis.\n";
        }

        // 2. Contrôle sur le Nom et l'Espèce
        if (!tfNom.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "Le nom ne doit contenir que des lettres.\n";
        }
        if (!tfEspece.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "La race/espèce ne doit contenir que des lettres.\n";
        }

        // 3. Contrôle sur le Poids
        try {
            float poids = Float.parseFloat(tfPoids.getText());
            if (poids <= 0) messageErreur += "Le poids doit être supérieur à 0.\n";
        } catch (NumberFormatException e) {
            messageErreur += "Le poids doit être un nombre valide (ex: 15.5).\n";
        }

        // 4. Contrôle sur la Date
        if (dpDate.getValue() != null && dpDate.getValue().isAfter(LocalDate.now())) {
            messageErreur += "La date de naissance ne peut pas être dans le futur.\n";
        }

        // SI ERREUR -> AFFICHE LA POPUP
        if (!messageErreur.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(messageErreur);
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @FXML
    void retourListe(ActionEvent event) {
        changerScene(event, "AfficherAnimaux.fxml");
    }

    private void changerScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
        }
    }

    // Méthodes de navigation pour la barre latérale
    @FXML void naviguerAnimaux(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerMateriels(ActionEvent event) { changerScene(event, "AfficherMateriels.fxml"); }
    @FXML void naviguerStocks(ActionEvent event) { changerScene(event, "AfficherStocks.fxml"); }
    @FXML void naviguerTerrains(ActionEvent event) { changerScene(event, "AfficherTerrains.fxml"); }
    @FXML void naviguerEvenements(ActionEvent event) { changerScene(event, "AfficherEvenements.fxml"); }
    @FXML void naviguerUsers(ActionEvent event) { changerScene(event, "AfficherUsers.fxml"); }
}