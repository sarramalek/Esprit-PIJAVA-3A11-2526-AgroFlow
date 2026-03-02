package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Evenement;
import models.Participation;
import services.ParticipationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SInscrireEvenementController {

    @FXML private Label titreEvenementLabel;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label lieuLabel;
    @FXML private Label statutEvenementLabel;
    @FXML private DatePicker dpDateInscription;
    @FXML private ComboBox<String> cbPresence;
    @FXML private Label errorLabel;

    private final ParticipationService participationService = new ParticipationService();
    private Evenement evenement;
    private int idUtilisateur;

    public void setIdUtilisateur(int id) {
        this.idUtilisateur = id;
    }

    public void setEvenement(Evenement ev) {
        this.evenement = ev;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        titreEvenementLabel.setText(ev.getTitre());
        dateDebutLabel.setText(fmt.format(ev.getDateDebut().toLocalDate()));
        dateFinLabel.setText(fmt.format(ev.getDateFin().toLocalDate()));
        lieuLabel.setText(ev.getLieu());
        statutEvenementLabel.setText(ev.getStatut());
    }

    @FXML
    public void initialize() {
        cbPresence.getItems().addAll("Oui", "Non");
        cbPresence.setValue("Oui");

        // Date d'inscription par defaut = aujourd'hui
        dpDateInscription.setValue(LocalDate.now());
    }

    // ================= S'INSCRIRE =================
    @FXML
    void sInscrire(ActionEvent event) {
        if (!validerChamps()) return;

        Participation participation = new Participation();
        participation.setId_evenement(evenement.getIdEvenement());
        participation.setDate_inscription(dpDateInscription.getValue());
        participation.setStatut_participation("Inscrit"); // statut auto pour user
        participation.setPresence(cbPresence.getValue().equals("Oui"));
        // Note : si vous avez un champ id_utilisateur dans Participation, ajoutez :
        // participation.setId_utilisateur(idUtilisateur);

        try {
            participationService.ajouter(participation);
            showSuccess("Inscription confirmee",
                    "Vous etes bien inscrit(e) a l'evenement \"" + evenement.getTitre() + "\" !");
            fermerPopup();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de s'inscrire : " + e.getMessage());
        }
    }

    // ================= VALIDATION =================
    private boolean validerChamps() {
        cacherErreur();

        if (dpDateInscription.getValue() == null) {
            afficherErreur("Veuillez selectionner une date d'inscription !");
            return false;
        }

        if (dpDateInscription.getValue().isAfter(LocalDate.now())) {
            afficherErreur("La date d'inscription ne peut pas etre dans le futur !");
            return false;
        }

        if (cbPresence.getValue() == null || cbPresence.getValue().isEmpty()) {
            afficherErreur("Veuillez indiquer si vous serez present(e) !");
            cbPresence.setStyle("-fx-border-color: #C62828; -fx-border-width: 2;");
            return false;
        }

        return true;
    }

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
        cbPresence.setStyle("");
        dpDateInscription.setStyle("");
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