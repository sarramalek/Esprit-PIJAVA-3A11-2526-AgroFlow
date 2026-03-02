package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.CategorieEvenement;
import models.Evenement;
import services.CategorieEvenementService;
import services.EvenementService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class AjouterEvenementController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbTypeEvenement;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private TextField tfLieu;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private Label errorLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();
    private List<CategorieEvenement> categories;

    // Callback pour rafraîchir la liste parente après ajout réussi
    private Runnable onSuccessCallback;

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        setupRealtimeValidation();
        remplirComboBoxes();
        chargerCategories();
    }

    // ================= REMPLIR LES COMBOBOXES (identique à l'original) =================
    private void remplirComboBoxes() {
        cbTypeEvenement.getItems().addAll(
                "Formation",
                "Intervention agricole",
                "Foire",
                "Réunion",
                "Alerte saisonnière"
        );

        cbStatut.getItems().addAll(
                "Planifié",
                "Annulé",
                "Terminé"
        );
    }

    // ================= CHARGER LES CATÉGORIES =================
    private void chargerCategories() {
        try {
            categories = categorieService.recuperer();
            for (CategorieEvenement cat : categories) {
                cbCategorie.getItems().add(cat.getNom_categorie());
            }
            System.out.println("✅ " + categories.size() + " catégories chargées");
        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger les catégories : " + e.getMessage());
        }
    }

    // ================= VALIDATION EN TEMPS RÉEL (identique à l'original) =================
    private void setupRealtimeValidation() {
        tfTitre.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && tfTitre.getText().trim().isEmpty()) {
                tfTitre.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (!isNowFocused) {
                tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });

        taDescription.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && taDescription.getText().trim().isEmpty()) {
                taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (!isNowFocused) {
                taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });

        tfLieu.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && tfLieu.getText().trim().isEmpty()) {
                tfLieu.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (!isNowFocused) {
                tfLieu.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                tfLieu.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });
    }

    // ================= AJOUTER ÉVÉNEMENT =================
    @FXML
    void ajouterEvenement(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        if (!validerChamps()) return;

        int idCategorie = getIdCategorieFromNom(cbCategorie.getValue());
        if (idCategorie == -1) {
            afficherErreur("Catégorie invalide !");
            return;
        }

        Evenement evenement = new Evenement();
        evenement.setTitre(tfTitre.getText().trim());
        evenement.setDescription(taDescription.getText().trim());
        evenement.setTypeEvenement(cbTypeEvenement.getValue());
        evenement.setDateDebut(Date.valueOf(dpDateDebut.getValue()));
        evenement.setDateFin(Date.valueOf(dpDateFin.getValue()));
        evenement.setLieu(tfLieu.getText().trim());
        evenement.setStatut(cbStatut.getValue());
        evenement.setIdCategorie(idCategorie);

        try {
            System.out.println("Ajout de l'événement : " + evenement.getTitre());
            evenementService.ajouter(evenement);
            System.out.println("✅ Événement ajouté avec succès !");

            showSuccess("Succès", "L'événement \"" + evenement.getTitre() + "\" a été ajouté avec succès !");

            if (onSuccessCallback != null) onSuccessCallback.run();

            fermerPopup();

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'ajout", "Impossible d'ajouter l'événement : " + e.getMessage());
        }
    }

    // ================= VALIDATION STRICTE (identique à l'original) =================
    private boolean validerChamps() {
        cacherErreur();

        // Titre
        String titre = tfTitre.getText();
        if (titre == null || titre.trim().isEmpty()) {
            afficherErreur("Le titre ne peut pas être vide !");
            tfTitre.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2;");
            tfTitre.requestFocus();
            return false;
        }
        if (titre.trim().length() < 5) {
            afficherErreur("Le titre doit contenir au moins 5 caractères !");
            tfTitre.requestFocus();
            return false;
        }

        // Description
        String description = taDescription.getText();
        if (description == null || description.trim().isEmpty()) {
            afficherErreur("La description ne peut pas être vide !");
            taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2;");
            taDescription.requestFocus();
            return false;
        }
        if (description.trim().length() < 10) {
            afficherErreur("La description doit contenir au moins 10 caractères !");
            taDescription.requestFocus();
            return false;
        }

        // Type
        if (cbTypeEvenement.getValue() == null || cbTypeEvenement.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un type d'événement !");
            cbTypeEvenement.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        // Dates
        if (dpDateDebut.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date de début !");
            dpDateDebut.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }
        if (dpDateFin.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date de fin !");
            dpDateFin.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }
        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            afficherErreur("La date de fin ne peut pas être avant la date de début !");
            dpDateFin.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        // Lieu
        String lieu = tfLieu.getText();
        if (lieu == null || lieu.trim().isEmpty()) {
            afficherErreur("Le lieu ne peut pas être vide !");
            tfLieu.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2;");
            tfLieu.requestFocus();
            return false;
        }

        // Catégorie
        if (cbCategorie.getValue() == null || cbCategorie.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner une catégorie !");
            cbCategorie.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        // Statut
        if (cbStatut.getValue() == null || cbStatut.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un statut !");
            cbStatut.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            return false;
        }

        System.out.println("✅ Validation réussie !");
        return true;
    }

    // ================= UTILITAIRES =================
    private int getIdCategorieFromNom(String nomCategorie) {
        for (CategorieEvenement cat : categories) {
            if (cat.getNom_categorie().equals(nomCategorie)) return cat.getId_categorie();
        }
        return -1;
    }

    private void afficherErreur(String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠️ " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        showWarning("Validation", message);
    }

    private void cacherErreur() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0;");
        taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0;");
        tfLieu.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0;");
        cbTypeEvenement.setStyle("");
        cbCategorie.setStyle("");
        cbStatut.setStyle("");
        dpDateDebut.setStyle("");
        dpDateFin.setStyle("");
    }

    // ================= NAVIGATION → fermer le pop-up =================
    @FXML
    void retourEvenements(ActionEvent event) {
        fermerPopup();
    }

    @FXML
    void goToAccueil(ActionEvent event) {
        fermerPopup();
    }

    private void fermerPopup() {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
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