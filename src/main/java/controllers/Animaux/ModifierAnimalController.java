package controllers.Animaux;

import models.Animaux.Sexe;
import models.Animaux.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Animaux.ServiceAnimal;

import java.io.IOException;
import java.sql.SQLException;

public class ModifierAnimalController {

    // 1. Déclaration des champs FXML (doivent correspondre aux fx:id du fichier .fxml)
    @FXML private TextField tfNom;
    @FXML private TextField tfEspece;
    @FXML private TextField tfPoids;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Sexe> cbSexe;

    // 2. Variables internes
    private ServiceAnimal service = new ServiceAnimal();
    private int idAnimalActuel; // Stocke l'ID pour savoir quel animal modifier en SQL

    @FXML
    public void initialize() {
        // Remplit le combo box au chargement de la page
        cbSexe.getItems().setAll(Sexe.values());
    }

    /**
     * MÉTHODE DE PASSAGE DE DONNÉES
     * Appelée depuis AfficherAnimauxController avant d'afficher cette page.
     */

    public void chargerDonnees(animaux a) {
        // On garde l'ID précieusement pour le "WHERE id = ?" de ta requête SQL
        this.idAnimalActuel = a.getId();

        // On pré-remplit les champs avec les données actuelles de l'animal
        tfNom.setText(a.getNom());
        tfEspece.setText(a.getEspece());
        tfPoids.setText(String.valueOf(a.getPoids()));

        // Gestion de la date (Conversion Date SQL -> LocalDate pour le DatePicker)
        if (a.getDate_naissance() != null) {
            dpDate.setValue(new java.sql.Date(a.getDate_naissance().getTime()).toLocalDate());
        }

        // Sélection du sexe dans le ComboBox
        cbSexe.setValue(a.getSexe());
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (estValide()) { // On appelle la même méthode de contrôle
            try {
                animaux a = new animaux();
                a.setId(idAnimalActuel); // L'ID que tu as récupéré via chargerDonnees
                a.setNom(tfNom.getText());
                a.setEspece(tfEspece.getText());
                a.setPoids(Float.parseFloat(tfPoids.getText()));
                a.setSexe(cbSexe.getValue());
                a.setDate_naissance(java.sql.Date.valueOf(dpDate.getValue()));

                service.modifier(a);
                retourListe(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/AfficherAnimaux.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes de navigation pour la SIDEBAR (Gestion globale)
    @FXML void naviguerAnimaux(ActionEvent event) { retourListe(event); }
    @FXML void naviguerMateriels(ActionEvent event) { /* charger AfficherMateriels.fxml */ }
    @FXML void naviguerStocks(ActionEvent event) { /* charger AfficherStocks.fxml */ }
    @FXML void naviguerTerrains(ActionEvent event) { /* charger AfficherTerrains.fxml */ }
    @FXML void naviguerEvenements(ActionEvent event) { /* charger AfficherEvenements.fxml */ }
    @FXML void naviguerUsers(ActionEvent event) { /* charger AfficherUsers.fxml */ }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.show();
    }
    private boolean estValide() {
        String messageErreur = "";

        // 1. Vérification des champs vides
        if (tfNom.getText().trim().isEmpty() || tfEspece.getText().trim().isEmpty() ||
                tfPoids.getText().trim().isEmpty() || dpDate.getValue() == null || cbSexe.getValue() == null) {
            messageErreur += "Tous les champs doivent être remplis.\n";
        }

        // 2. Contrôle sur le Nom et l'Espèce (Pas de chiffres)
        // On utilise une expression régulière : ^[a-zA-Z\s]+$ (uniquement lettres et espaces)
        if (!tfNom.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "Le nom ne doit contenir que des lettres.\n";
        }
        if (!tfEspece.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "La race/espèce ne doit contenir que des lettres.\n";
        }

        // 3. Contrôle sur le Poids (Doit être un nombre positif)
        try {
            float poids = Float.parseFloat(tfPoids.getText());
            if (poids <= 0) messageErreur += "Le poids doit être supérieur à 0.\n";
        } catch (NumberFormatException e) {
            messageErreur += "Le poids doit être un nombre valide (ex: 15.5).\n";
        }

        // 4. Contrôle sur la Date (Pas de date dans le futur)
        if (dpDate.getValue() != null && dpDate.getValue().isAfter(java.time.LocalDate.now())) {
            messageErreur += "La date de naissance ne peut pas être dans le futur.\n";
        }

        // Affichage de l'alerte si erreur
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
}