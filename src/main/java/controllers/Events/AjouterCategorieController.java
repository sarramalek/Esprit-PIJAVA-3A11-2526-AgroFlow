package controllers.Events;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import services.Events.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterCategorieController {

    @FXML
    private TextField tfNom;

    @FXML
    private TextArea taDescription;

    @FXML
    private Label errorLabel;

    private final CategorieEvenementService service = new CategorieEvenementService();

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        setupRealtimeValidation();
    }

    // ================= VALIDATION EN TEMPS RÉEL =================
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

    // ================= AJOUTER CATÉGORIE =================
    @FXML
    void ajouterCategorie(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        if (!validerChamps()) {
            return;
        }

        CategorieEvenement c = new CategorieEvenement();
        c.setNom_categorie(tfNom.getText().trim());
        c.setDescription_categorie(taDescription.getText().trim());

        try {
            System.out.println("Ajout de la catégorie : " + c.getNom_categorie());
            service.ajouter(c);
            System.out.println("✅ Catégorie ajoutée avec succès !");

            showSuccess("Succès", "La catégorie \"" + c.getNom_categorie() + "\" a été ajoutée avec succès !");

            retourCategories(event);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'ajout", "Impossible d'ajouter la catégorie : " + e.getMessage());
        }
    }

    // ================= VALIDATION =================
    private boolean validerChamps() {
        cacherErreur();

        String nom = tfNom.getText();
        String description = taDescription.getText();

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

    // ================= NAVIGATION =================
    @FXML
    void retourCategories(ActionEvent event) {
        System.out.println("=== Navigation vers AfficherCategories ===");
        naviguerVers("AfficherCategories.fxml");
    }

    @FXML
    void retourAccueil(ActionEvent event) {
        System.out.println("=== Navigation vers Accueil ===");
        naviguerVers("Accueil.fxml");
    }

    // ================= MÉTHODE DE NAVIGATION AMÉLIORÉE =================
    private void naviguerVers(String nomFichierFxml) {
        try {
            System.out.println("Chargement de : /G-Evenements/" + nomFichierFxml);

            // Charger le FXML
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + nomFichierFxml));

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) tfNom.getScene().getWindow();

            // Créer et afficher la nouvelle scène
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Navigation réussie vers " + nomFichierFxml);

        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page : " + nomFichierFxml + "\n" + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
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