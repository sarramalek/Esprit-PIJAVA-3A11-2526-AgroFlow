package controllers.Events;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Events.Evenement;
import models.Events.Participation;
import services.Events.EvenementService;
import services.Events.ParticipationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ModifierParticipationController {

    @FXML
    private TextField tfId;

    @FXML
    private ComboBox<String> cbEvenement;

    @FXML
    private DatePicker dpDateInscription;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private ComboBox<String> cbPresence;

    @FXML
    private Label errorLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();
    private Participation participationActuelle;
    private List<Evenement> evenements;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        remplirComboBoxes();
        chargerEvenements();
    }

    // ================= SETTER POUR RECEVOIR LA PARTICIPATION =================
    public void setParticipation(Participation participation) {
        System.out.println("=== Participation reçue pour modification ===");
        System.out.println("ID : " + participation.getId_participation());

        this.participationActuelle = participation;

        // Pré-remplir les champs
        tfId.setText(String.valueOf(participation.getId_participation()));
        dpDateInscription.setValue(participation.getDate_inscription());
        cbStatut.setValue(participation.getStatut_participation());
        cbPresence.setValue(participation.isPresence() ? "Oui" : "Non");

        // Sélectionner l'événement correspondant
        try {
            String titre = evenementService.getNomEvenementById(participation.getId_evenement());
            cbEvenement.setValue(titre);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Champs pré-remplis avec succès !");
    }

    // ================= REMPLIR LES COMBOBOXES =================
    private void remplirComboBoxes() {
        cbStatut.getItems().addAll("Inscrit", "Confirmé", "Annulé");
        cbPresence.getItems().addAll("Oui", "Non");
    }

    // ================= CHARGER LES ÉVÉNEMENTS =================
    private void chargerEvenements() {
        try {
            evenements = evenementService.recuperer();

            for (Evenement evt : evenements) {
                cbEvenement.getItems().add(evt.getTitre());
            }

            System.out.println("✅ " + evenements.size() + " événements chargés");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    // ================= MODIFIER PARTICIPATION =================
    @FXML
    void modifierParticipation(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        if (!validerChamps()) {
            return;
        }

        if (participationActuelle == null) {
            showError("Erreur", "Aucune participation sélectionnée !");
            return;
        }

        // Récupérer l'ID de l'événement
        int idEvenement = getIdEvenementFromTitre(cbEvenement.getValue());
        if (idEvenement == -1) {
            afficherErreur("Événement invalide !");
            return;
        }

        // Mettre à jour la participation
        participationActuelle.setStatut_participation(cbStatut.getValue());
        participationActuelle.setDate_inscription(dpDateInscription.getValue());
        participationActuelle.setPresence(cbPresence.getValue().equals("Oui"));
        participationActuelle.setId_evenement(idEvenement);

        try {
            System.out.println("Modification de la participation ID : " + participationActuelle.getId_participation());
            participationService.modifier(participationActuelle);
            System.out.println("✅ Participation modifiée avec succès !");

            showSuccess("Succès", "La participation a été modifiée avec succès !");
            retourParticipations(event);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de modifier la participation : " + e.getMessage());
        }
    }

    // ================= VALIDATION =================
    private boolean validerChamps() {
        cacherErreur();

        if (cbEvenement.getValue() == null || cbEvenement.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un événement !");
            cbEvenement.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        if (dpDateInscription.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date !");
            dpDateInscription.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        if (cbStatut.getValue() == null || cbStatut.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un statut !");
            cbStatut.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        if (cbPresence.getValue() == null || cbPresence.getValue().isEmpty()) {
            afficherErreur("Veuillez indiquer la présence !");
            cbPresence.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        System.out.println("✅ Validation réussie !");
        return true;
    }

    // ================= UTILITAIRES =================
    private int getIdEvenementFromTitre(String titre) {
        for (Evenement evt : evenements) {
            if (evt.getTitre().equals(titre)) {
                return evt.getIdEvenement();
            }
        }
        return -1;
    }

    private void afficherErreur(String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠️ " + message);
            errorLabel.setVisible(true);
        }
        showWarning("Validation", message);
    }

    private void cacherErreur() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        cbEvenement.setStyle("");
        cbStatut.setStyle("");
        cbPresence.setStyle("");
        dpDateInscription.setStyle("");
    }

    // ================= NAVIGATION =================
    @FXML
    void retourParticipations(ActionEvent event) {
        chargerPage("AfficherParticipations.fxml");
    }

    @FXML
    void goToAccueil(ActionEvent event) {
        chargerPage("Accueil.fxml");
    }

    private void chargerPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) cbEvenement.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= ALERT METHODS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}