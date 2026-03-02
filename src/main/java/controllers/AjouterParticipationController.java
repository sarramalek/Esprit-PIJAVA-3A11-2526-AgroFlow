package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Evenement;
import models.Participation;
import models.User;
import services.EvenementService;
import services.ParticipationService;
import services.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterParticipationController {

    @FXML private ComboBox<String> cbEvenement;
    @FXML private ComboBox<User>   cbUser;
    @FXML private DatePicker       dpDateInscription;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbPresence;
    @FXML private Label            errorLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService     evenementService     = new EvenementService();
    private final UserService          userService          = new UserService();

    private List<Evenement> evenements;

    // Callback appelé après ajout réussi (pour rafraîchir la liste parente)
    private Runnable onSuccessCallback;

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        remplirComboBoxes();
        chargerEvenements();
        chargerUtilisateurs();

        // Valeurs par défaut
        dpDateInscription.setValue(LocalDate.now());
        cbStatut.setValue("Inscrit");
        cbPresence.setValue("Non");
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
        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger les événements : " + e.getMessage());
        }
    }

    // ================= CHARGER LES UTILISATEURS =================
    private void chargerUtilisateurs() {
        try {
            List<User> users = userService.recuperer();
            cbUser.getItems().setAll(users);

            // Afficher "Nom  (ID: X)" dans la liste déroulante
            cbUser.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null
                            ? null
                            : user.getNom() + "  (ID: " + user.getId_user() + ")");
                }
            });

            // Afficher la même chose dans le bouton de sélection
            cbUser.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null
                            ? null
                            : user.getNom() + "  (ID: " + user.getId_user() + ")");
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    // ================= AJOUTER PARTICIPATION =================
    @FXML
    void ajouterParticipation(ActionEvent event) {
        if (!validerChamps()) return;

        int idEvenement = getIdEvenementFromTitre(cbEvenement.getValue());
        if (idEvenement == -1) {
            afficherErreur("Événement invalide !");
            return;
        }

        Participation participation = new Participation();
        participation.setId_evenement(idEvenement);
        participation.setId_user(cbUser.getValue().getId_user());
        participation.setDate_inscription(dpDateInscription.getValue());
        participation.setStatut_participation(cbStatut.getValue());
        participation.setPresence(cbPresence.getValue().equals("Oui"));

        try {
            participationService.ajouter(participation);

            if (onSuccessCallback != null) onSuccessCallback.run();

            showSuccess("Succès", "La participation a été ajoutée avec succès !");
            fermerPopup();

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Impossible d'ajouter la participation : " + e.getMessage());
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

        if (cbUser.getValue() == null) {
            afficherErreur("Veuillez sélectionner un utilisateur !");
            cbUser.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        if (dpDateInscription.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date d'inscription !");
            dpDateInscription.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        if (dpDateInscription.getValue().isAfter(LocalDate.now())) {
            afficherErreur("La date d'inscription ne peut pas être dans le futur !");
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

        return true;
    }

    // ================= UTILITAIRES =================
    private int getIdEvenementFromTitre(String titre) {
        for (Evenement evt : evenements) {
            if (evt.getTitre().equals(titre)) return evt.getIdEvenement();
        }
        return -1;
    }

    private void afficherErreur(String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠️ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void cacherErreur() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        cbEvenement.setStyle("");
        cbUser.setStyle("");
        cbStatut.setStyle("");
        cbPresence.setStyle("");
        dpDateInscription.setStyle("");
    }

    // ================= NAVIGATION =================
    @FXML
    void retourParticipations(ActionEvent event) {
        fermerPopup();
    }

    @FXML
    void goToAccueil(ActionEvent event) {
        fermerPopup();
    }

    private void fermerPopup() {
        Stage stage = (Stage) cbEvenement.getScene().getWindow();
        stage.close();
    }

    // ================= ALERTS =================
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}