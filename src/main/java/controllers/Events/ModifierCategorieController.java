package controllers.Events;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import services.Events.CategorieEvenementService;

import java.sql.SQLException;

public class ModifierCategorieController {

    @FXML private TextField tfId;
    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private Label errorLabel;
    @FXML private Label infoLabel;

    private final CategorieEvenementService service = new CategorieEvenementService();
    private CategorieEvenement categorieActuelle;

    // Callback pour rafraîchir la liste parente après modification réussie
    private Runnable onSuccessCallback;

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        setupRealtimeValidation();
    }

    // ================= SETTER POUR RECEVOIR LA CATÉGORIE (identique à l'original) =================
    public void setCategorie(CategorieEvenement categorie) {
        System.out.println("=== Catégorie reçue pour modification ===");
        System.out.println("ID : " + categorie.getId_categorie());
        System.out.println("Nom : " + categorie.getNom_categorie());
        System.out.println("Description : " + categorie.getDescription_categorie());

        this.categorieActuelle = categorie;

        tfId.setText(String.valueOf(categorie.getId_categorie()));
        tfNom.setText(categorie.getNom_categorie());
        taDescription.setText(categorie.getDescription_categorie());

        if (infoLabel != null) {
            infoLabel.setText("Modification de : " + categorie.getNom_categorie());
        }

        System.out.println("✅ Champs pré-remplis avec succès !");
    }

    // ================= VALIDATION EN TEMPS RÉEL (identique à l'original) =================
    private void setupRealtimeValidation() {
        tfNom.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && tfNom.getText().trim().isEmpty()) {
                tfNom.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 8; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            } else if (!isNowFocused) {
                tfNom.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            } else {
                tfNom.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            }
        });

        taDescription.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && taDescription.getText().trim().isEmpty()) {
                taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 8; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            } else if (!isNowFocused) {
                taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            } else {
                taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            }
        });
    }

    // ================= MODIFIER CATÉGORIE (identique à l'original) =================
    @FXML
    void modifierCategorie(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        if (!validerChamps()) {
            return;
        }

        if (categorieActuelle == null) {
            showError("Erreur", "Aucune catégorie sélectionnée pour la modification !");
            return;
        }

        categorieActuelle.setNom_categorie(tfNom.getText().trim());
        categorieActuelle.setDescription_categorie(taDescription.getText().trim());

        try {
            System.out.println("Modification de la catégorie ID : " + categorieActuelle.getId_categorie());
            System.out.println("Nouveau nom : " + categorieActuelle.getNom_categorie());

            service.modifier(categorieActuelle);

            System.out.println("✅ Catégorie modifiée avec succès !");

            showSuccess("Succès",
                    "La catégorie \"" + categorieActuelle.getNom_categorie() + "\" a été modifiée avec succès !");

            // Rafraîchir la liste parente
            if (onSuccessCallback != null) onSuccessCallback.run();

            fermerPopup();

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de modification",
                    "Impossible de modifier la catégorie : " + e.getMessage());
        }
    }

    // ================= VALIDATION STRICTE (identique à l'original) =================
    private boolean validerChamps() {
        cacherErreur();

        String nom = tfNom.getText();
        String description = taDescription.getText();

        // VÉRIFICATION 1 : Champs NULL ou VIDES
        if (nom == null || nom.trim().isEmpty()) {
            afficherErreur("Le nom de la catégorie ne peut pas être vide !");
            tfNom.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 8; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            tfNom.requestFocus();
            return false;
        }

        if (description == null || description.trim().isEmpty()) {
            afficherErreur("La description ne peut pas être vide !");
            taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 8; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            taDescription.requestFocus();
            return false;
        }

        // VÉRIFICATION 2 : Longueur minimale
        nom = nom.trim();
        description = description.trim();

        if (nom.length() < 3) {
            afficherErreur("Le nom doit contenir au moins 3 caractères !");
            tfNom.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 8; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            tfNom.requestFocus();
            return false;
        }

        if (description.length() < 10) {
            afficherErreur("La description doit contenir au moins 10 caractères !");
            taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-background-radius: 8; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
            taDescription.requestFocus();
            return false;
        }

        // VÉRIFICATION 3 : Longueur maximale
        if (nom.length() > 100) {
            afficherErreur("Le nom ne doit pas dépasser 100 caractères !");
            tfNom.requestFocus();
            return false;
        }

        if (description.length() > 500) {
            afficherErreur("La description ne doit pas dépasser 500 caractères !");
            taDescription.requestFocus();
            return false;
        }

        System.out.println("✅ Validation réussie !");
        return true;
    }

    // ================= AFFICHER/CACHER ERREUR (identique à l'original) =================
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
        tfNom.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
        taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
    }

    // ================= NAVIGATION → fermer le pop-up =================
    @FXML
    void retourCategories(ActionEvent event) {
        fermerPopup();
    }

    // Conservé pour compatibilité si référencé ailleurs
    @FXML
    void retourAccueil(ActionEvent event) {
        fermerPopup();
    }

    private void fermerPopup() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }

    // ================= ALERT METHODS (identiques à l'original) =================
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