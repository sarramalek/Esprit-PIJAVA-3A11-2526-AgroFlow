package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Abonnements;
import models.offres;
import services.AbonnementService;
import services.OffresServicees;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AjoutAbonnements {

    @FXML private TextField cinField;
    @FXML private ComboBox<String> offreComboBox;
    @FXML private DatePicker dateInscriptionPicker;
    @FXML private DatePicker dateExpirationPicker;
    @FXML private ComboBox<String> situationComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private GestionAbonnements parentController;
    private AbonnementService abonnementService;
    private OffresServicees offresService;
    private List<offres> offresList;

    /**
     * Initialisation
     */
    @FXML
    public void initialize() {
        abonnementService = new AbonnementService();
        offresService = new OffresServicees();

        System.out.println("✓ AjouterAbonnementController initialisé");

        // Charger les offres
        loadOffres();

        // Remplir le ComboBox des situations
        situationComboBox.setItems(FXCollections.observableArrayList(
                "Actif",
                "En attente",
                "Expiré"
        ));
        situationComboBox.setValue("En attente");

        // Définir la date d'inscription par défaut à aujourd'hui
        dateInscriptionPicker.setValue(LocalDate.now());
    }

    /**
     * Charger les offres disponibles
     */
    private void loadOffres() {
        try {
            offresList = offresService.recuperer();

            for (offres offre : offresList) {
                offreComboBox.getItems().add(offre.getId_offres() + " - " + offre.getNom_offre());
            }

            System.out.println("✓ " + offresList.size() + " offres chargées");

        } catch (SQLException e) {
            System.err.println("✗ Erreur lors du chargement des offres");
            e.printStackTrace();
        }
    }

    /**
     * Définir le contrôleur parent
     */
    public void setGestionAbonnementsController(GestionAbonnements controller) {
        this.parentController = controller;
    }

    /**
     * Gérer la sauvegarde
     */
    @FXML
    private void handleSave() {
        System.out.println("💾 Tentative d'ajout d'abonnement...");

        if (!validateFields()) {
            return;
        }

        try {
            Abonnements newAbonnement = new Abonnements();
            newAbonnement.setCin(Integer.parseInt(cinField.getText().trim()));

            // Extraire l'ID de l'offre sélectionnée
            String selectedOffre = offreComboBox.getValue();
            int idOffre = Integer.parseInt(selectedOffre.split(" - ")[0]);
            newAbonnement.setId_offre(idOffre);

            // Formater les dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            newAbonnement.setDate_inscription(dateInscriptionPicker.getValue().format(formatter));
            newAbonnement.setDate_expiration(dateExpirationPicker.getValue().format(formatter));
            newAbonnement.setSituation(situationComboBox.getValue());

            System.out.println("🔍 Données de l'abonnement:");
            System.out.println("  CIN: " + newAbonnement.getCin());
            System.out.println("  Offre: " + newAbonnement.getId_offre());
            System.out.println("  Situation: " + newAbonnement.getSituation());

            abonnementService.ajouter(newAbonnement);

            if (parentController != null) {
                parentController.loadAbonnements();
            }

            System.out.println("✓ Abonnement ajouté avec succès");
            showSuccess();
            closeWindow();

        } catch (NumberFormatException e) {
            showError("Le CIN doit être un nombre valide");
            System.err.println("✗ Erreur de format numérique");
        } catch (SQLException e) {
            showError("Erreur lors de l'enregistrement dans la base de données");
            System.err.println("✗ Erreur SQL:");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Une erreur inattendue est survenue");
            System.err.println("✗ Erreur:");
            e.printStackTrace();
        }
    }

    /**
     * Gérer l'annulation
     */
    @FXML
    private void handleCancel() {
        System.out.println("❌ Ajout annulé");
        closeWindow();
    }

    /**
     * Valider les champs
     */
    private boolean validateFields() {
        // CIN
        if (cinField.getText().trim().isEmpty()) {
            showError("Le CIN est obligatoire");
            cinField.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(cinField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Le CIN doit être un nombre valide");
            cinField.requestFocus();
            return false;
        }

        // Offre
        if (offreComboBox.getValue() == null) {
            showError("Veuillez sélectionner une offre");
            offreComboBox.requestFocus();
            return false;
        }

        // Date inscription
        if (dateInscriptionPicker.getValue() == null) {
            showError("La date d'inscription est obligatoire");
            dateInscriptionPicker.requestFocus();
            return false;
        }

        // Date expiration
        if (dateExpirationPicker.getValue() == null) {
            showError("La date d'expiration est obligatoire");
            dateExpirationPicker.requestFocus();
            return false;
        }

        // Vérifier que la date d'expiration est après la date d'inscription
        if (dateExpirationPicker.getValue().isBefore(dateInscriptionPicker.getValue())) {
            showError("La date d'expiration doit être après la date d'inscription");
            dateExpirationPicker.requestFocus();
            return false;
        }

        // Situation
        if (situationComboBox.getValue() == null) {
            showError("Veuillez sélectionner une situation");
            situationComboBox.requestFocus();
            return false;
        }

        hideError();
        return true;
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Abonnement ajouté avec succès !");
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}