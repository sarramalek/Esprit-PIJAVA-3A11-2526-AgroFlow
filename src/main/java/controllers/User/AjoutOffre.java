package controllers.User;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.offres;
import services.User.OffresServicees;

import java.sql.SQLException;

public class AjoutOffre {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField prixField;
    @FXML private TextField dureeField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private GestionOffres parentController;
    private OffresServicees offresService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        offresService = new OffresServicees();
        System.out.println("✓ AjouterOffreController initialisé");
    }

    /**
     * Définir le contrôleur parent
     */
    public void setGestionOffresController(GestionOffres controller) {
        this.parentController = controller;
    }

    /**
     * Gérer la sauvegarde
     */
    @FXML
    private void handleSave() {
        System.out.println("💾 Tentative d'ajout d'offre...");

        // Validation
        if (!validateFields()) {
            return;
        }

        try {
            // Créer une nouvelle offre
            offres newOffre = new offres();
            newOffre.setNom_offre(nomField.getText().trim());
            newOffre.setDescription(descriptionArea.getText().trim());
            newOffre.setPrix(Float.parseFloat(prixField.getText().trim()));
            newOffre.setDuree_offre(Integer.parseInt(dureeField.getText().trim()));

            System.out.println("🔍 Données de l'offre:");
            System.out.println("  Nom: " + newOffre.getNom_offre());
            System.out.println("  Prix: " + newOffre.getPrix());
            System.out.println("  Durée: " + newOffre.getDuree_offre());

            // Ajouter l'offre
            offresService.ajouter(newOffre);

            // Rafraîchir la liste du parent
            if (parentController != null) {
                parentController.loadOffres();
            }

            System.out.println("✓ Offre ajoutée avec succès");
            showSuccess();

            // Fermer la fenêtre
            closeWindow();

        } catch (NumberFormatException e) {
            showError("Le prix et la durée doivent être des nombres valides");
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
        // Nom
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom de l'offre est obligatoire");
            nomField.requestFocus();
            return false;
        }

        // Description
        if (descriptionArea.getText().trim().isEmpty()) {
            showError("La description est obligatoire");
            descriptionArea.requestFocus();
            return false;
        }

        // Prix
        if (prixField.getText().trim().isEmpty()) {
            showError("Le prix est obligatoire");
            prixField.requestFocus();
            return false;
        }

        try {
            double prix = Double.parseDouble(prixField.getText().trim());
            if (prix <= 0) {
                showError("Le prix doit être supérieur à 0");
                prixField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Le prix doit être un nombre valide (ex: 99.99)");
            prixField.requestFocus();
            return false;
        }

        // Durée
        if (dureeField.getText().trim().isEmpty()) {
            showError("La durée est obligatoire");
            dureeField.requestFocus();
            return false;
        }

        try {
            int duree = Integer.parseInt(dureeField.getText().trim());
            if (duree <= 0) {
                showError("La durée doit être supérieure à 0");
                dureeField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("La durée doit être un nombre entier valide");
            dureeField.requestFocus();
            return false;
        }

        hideError();
        return true;
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        errorLabel.setVisible(true);
    }

    /**
     * Masquer le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }

    /**
     * Afficher un message de succès
     */
    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Offre ajoutée avec succès !");
        alert.showAndWait();
    }

    /**
     * Fermer la fenêtre
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}