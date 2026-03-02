package controllers.Events;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Events.Evenement;
import models.Events.Participation;
import models.User.Personne;
import services.Events.EvenementService;
import services.Events.ParticipationService;
import services.User.PersonneService;

import java.sql.SQLException;
import java.util.List;

public class ModifierParticipationController {

    @FXML private TextField        tfId;
    @FXML private ComboBox<String> cbEvenement;
    @FXML private ComboBox<Personne>   cbUser;
    @FXML private DatePicker       dpDateInscription;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbPresence;
    @FXML private Label            errorLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService     evenementService     = new EvenementService();
    private final PersonneService          userService          = new PersonneService();

    private Participation participationActuelle;
    private List<Evenement> evenements;

    // Callback appelé après modification réussie (pour rafraîchir la liste parente)
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
    }

    // ================= SETTER POUR RECEVOIR LA PARTICIPATION =================
    public void setParticipation(Participation participation) {
        this.participationActuelle = participation;

        tfId.setText(String.valueOf(participation.getId_participation()));
        dpDateInscription.setValue(participation.getDate_inscription());
        cbStatut.setValue(participation.getStatut_participation());
        cbPresence.setValue(participation.isPresence() ? "Oui" : "Non");

        // Pré-sélectionner l'événement
        try {
            String titre = evenementService.getNomEvenementById(participation.getId_evenement());
            cbEvenement.setValue(titre);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Pré-sélectionner l'utilisateur — attend que cbUser soit chargé
        cbUser.getItems().stream()
                .filter(u -> u.getCin() == participation.getId_user())
                .findFirst()
                .ifPresent(cbUser::setValue);
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
            List<Personne> users = userService.recuperer();
            cbUser.getItems().setAll(users);

            // Afficher "Nom  (ID: X)" dans la liste déroulante
            cbUser.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Personne user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null
                            ? null
                            : user.getNom() + "  (ID: " + user.getCin() + ")");
                }
            });

            // Afficher la même chose dans le bouton de sélection
            cbUser.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Personne user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null
                            ? null
                            : user.getNom() + "  (ID: " + user.getCin() + ")");
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    // ================= MODIFIER PARTICIPATION =================
    @FXML
    void modifierParticipation(ActionEvent event) {
        if (!validerChamps()) return;

        if (participationActuelle == null) {
            afficherErreur("Aucune participation sélectionnée !");
            return;
        }

        int idEvenement = getIdEvenementFromTitre(cbEvenement.getValue());
        if (idEvenement == -1) {
            afficherErreur("Événement invalide !");
            return;
        }

        participationActuelle.setId_evenement(idEvenement);
        participationActuelle.setId_user(cbUser.getValue().getCin());
        participationActuelle.setDate_inscription(dpDateInscription.getValue());
        participationActuelle.setStatut_participation(cbStatut.getValue());
        participationActuelle.setPresence(cbPresence.getValue().equals("Oui"));

        try {
            participationService.modifier(participationActuelle);

            if (onSuccessCallback != null) onSuccessCallback.run();

            showSuccess("Succès", "La participation a été modifiée avec succès !");
            fermerPopup();

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Impossible de modifier la participation : " + e.getMessage());
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