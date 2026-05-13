package controllers.Events;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Events.Participation;
import services.Events.EvenementService;
import services.Events.ParticipationService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ModifierParticipationUserController {

    @FXML private TextField        tfId;
    @FXML private Label            evenementInfoLabel;
    @FXML private Label            dateInscriptionInfoLabel;
    @FXML private Label            statutInfoLabel;
    @FXML private ComboBox<String> cbPresence;
    @FXML private Label            errorLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService     evenementService     = new EvenementService();
    private Participation participationActuelle;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        cbPresence.getItems().addAll("Oui", "Non");
    }

    // ================= SETTER =================
    public void setParticipation(Participation participation) {
        this.participationActuelle = participation;

        tfId.setText(String.valueOf(participation.getId_participation()));

        // Nom de l'événement (lecture seule)
        try {
            evenementInfoLabel.setText(
                    evenementService.getNomEvenementById(participation.getId_evenement()));
        } catch (SQLException e) {
            evenementInfoLabel.setText("Evenement introuvable");
        }

        // Date inscription (lecture seule)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateInscriptionInfoLabel.setText(
                participation.getDate_inscription() != null
                        ? fmt.format(participation.getDate_inscription())
                        : "---"
        );

        // Statut avec couleur (lecture seule)
        String statut = participation.getStatut_participation();
        statutInfoLabel.setText(statut != null ? statut : "---");
        if (statut != null) {
            switch (statut.toLowerCase()) {
                case "inscrit":
                    statutInfoLabel.setStyle(
                            "-fx-font-size: 13px; -fx-text-fill: #F57F17; -fx-background-color: #FFF9C4; " +
                                    "-fx-padding: 8 14; -fx-background-radius: 8; -fx-border-color: #ECEFF1; -fx-border-radius: 8;");
                    break;
                case "confirme": case "confirmé":
                    statutInfoLabel.setStyle(
                            "-fx-font-size: 13px; -fx-text-fill: #2E7D32; -fx-background-color: #E8F5E9; " +
                                    "-fx-padding: 8 14; -fx-background-radius: 8; -fx-border-color: #ECEFF1; -fx-border-radius: 8;");
                    break;
                default:
                    statutInfoLabel.setStyle(
                            "-fx-font-size: 13px; -fx-text-fill: #546E7A; -fx-background-color: #F5F7FA; " +
                                    "-fx-padding: 8 14; -fx-background-radius: 8; -fx-border-color: #ECEFF1; -fx-border-radius: 8;");
            }
        }

        // Présence — seul champ modifiable
        cbPresence.setValue(participation.isPresence() ? "Oui" : "Non");
    }

    // ================= MODIFIER (présence seulement) =================
    @FXML
    void modifierParticipation(ActionEvent event) {
        cacherErreur();

        if (cbPresence.getValue() == null || cbPresence.getValue().isEmpty()) {
            afficherErreur("Veuillez indiquer votre presence !");
            cbPresence.setStyle("-fx-border-color: #C62828; -fx-border-width: 2;");
            return;
        }

        if (participationActuelle == null) {
            showError("Erreur", "Aucune participation selectionnee !");
            return;
        }

        // Seule la présence est modifiable par le user
        participationActuelle.setPresence(cbPresence.getValue().equals("Oui"));

        try {
            participationService.modifier(participationActuelle);
            showSuccess("Succes", "Votre participation a ete mise a jour !");
            fermerPopup();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de modifier : " + e.getMessage());
        }
    }

    // ================= ERREUR =================
    private void afficherErreur(String message) {
        if (errorLabel != null) {
            errorLabel.setText("  " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void cacherErreur() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        // Rétablir le style normal de cbPresence
        cbPresence.setStyle(
                "-fx-background-color: #E3F2FD; -fx-background-radius: 8; " +
                        "-fx-border-color: #1565C0; -fx-border-radius: 8; -fx-font-size: 13px;");
    }

    // ================= NAVIGATION =================
    @FXML
    void annuler(ActionEvent event) {
        fermerPopup();
    }

    private void fermerPopup() {
        Stage stage = (Stage) cbPresence.getScene().getWindow();
        stage.close();
    }

    // ================= ALERTS =================
    private void showSuccess(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}