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
import java.time.LocalDate;
import java.util.List;

public class AjouterParticipationController {

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
    private List<Evenement> evenements;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        remplirComboBoxes();
        chargerEvenements();
        setupRealtimeValidation();

        // Définir la date d'inscription par défaut à aujourd'hui
        dpDateInscription.setValue(LocalDate.now());

        // Définir le statut par défaut
        cbStatut.setValue("Inscrit");

        // Définir la présence par défaut
        cbPresence.setValue("Non");
    }

    // ================= REMPLIR LES COMBOBOXES =================
    private void remplirComboBoxes() {
        // Statut
        cbStatut.getItems().addAll(
                "Inscrit",
                "Confirmé",
                "Annulé"
        );

        // Présence
        cbPresence.getItems().addAll(
                "Oui",
                "Non"
        );
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

    // ================= VALIDATION EN TEMPS RÉEL =================
    private void setupRealtimeValidation() {
        cbEvenement.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && (cbEvenement.getValue() == null || cbEvenement.getValue().isEmpty())) {
                cbEvenement.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            } else if (!isNowFocused) {
                cbEvenement.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2;");
            }
        });
    }

    // ================= AJOUTER PARTICIPATION =================
    @FXML
    void ajouterParticipation(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        if (!validerChamps()) {
            return;
        }

        // Récupérer l'ID de l'événement
        int idEvenement = getIdEvenementFromTitre(cbEvenement.getValue());
        if (idEvenement == -1) {
            afficherErreur("Événement invalide !");
            return;
        }

        // Convertir la présence
        boolean presence = cbPresence.getValue().equals("Oui");

        // Créer la participation
        Participation participation = new Participation();
        participation.setStatut_participation(cbStatut.getValue());
        participation.setDate_inscription(dpDateInscription.getValue());
        participation.setPresence(presence);
        participation.setId_evenement(idEvenement);

        try {
            System.out.println("Ajout de la participation pour l'événement : " + cbEvenement.getValue());
            participationService.ajouter(participation);
            System.out.println("✅ Participation ajoutée avec succès !");

            showSuccess("Succès", "La participation a été ajoutée avec succès !");
            retourParticipations(event);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'ajout", "Impossible d'ajouter la participation : " + e.getMessage());
        }
    }

    // ================= VALIDATION STRICTE =================
    private boolean validerChamps() {
        cacherErreur();

        // Événement
        if (cbEvenement.getValue() == null || cbEvenement.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un événement !");
            cbEvenement.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            cbEvenement.requestFocus();
            return false;
        }

        // Date d'inscription
        if (dpDateInscription.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date d'inscription !");
            dpDateInscription.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            dpDateInscription.requestFocus();
            return false;
        }

        // Vérifier que la date n'est pas dans le futur
        if (dpDateInscription.getValue().isAfter(LocalDate.now())) {
            afficherErreur("La date d'inscription ne peut pas être dans le futur !");
            dpDateInscription.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            dpDateInscription.requestFocus();
            return false;
        }

        // Statut
        if (cbStatut.getValue() == null || cbStatut.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un statut !");
            cbStatut.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            cbStatut.requestFocus();
            return false;
        }

        // Présence
        if (cbPresence.getValue() == null || cbPresence.getValue().isEmpty()) {
            afficherErreur("Veuillez indiquer la présence !");
            cbPresence.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            cbPresence.requestFocus();
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
        System.out.println("=== Navigation vers AfficherParticipations ===");
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