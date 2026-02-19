package controllers.Events;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import models.Events.Evenement;
import services.Events.CategorieEvenementService;
import services.Events.EvenementService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterEvenementController {

    @FXML
    private TextField tfTitre;

    @FXML
    private TextArea taDescription;

    @FXML
    private ComboBox<String> cbTypeEvenement;

    @FXML
    private DatePicker dpDateDebut;

    @FXML
    private DatePicker dpDateFin;

    @FXML
    private TextField tfLieu;

    @FXML
    private ComboBox<String> cbCategorie;

    @FXML
    private ComboBox<String> cbStatut;

    @FXML
    private Label errorLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();

    // Map pour stocker les catégories (nom -> id)
    private List<CategorieEvenement> categories;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        setupRealtimeValidation();
        chargerCategories();
        remplirComboBoxes();

        // Définir le statut par défaut
        cbStatut.setValue("Planifié");
    }

    // ================= REMPLIR LES COMBOBOXES =================
    private void remplirComboBoxes() {
        // Remplir ComboBox Type d'événement
        cbTypeEvenement.getItems().addAll(
                "Formation",
                "Intervention agricole",
                "Foire",
                "Réunion",
                "Alerte saisonnière"
        );

        // Remplir ComboBox Statut
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

            // Remplir la ComboBox avec les noms des catégories
            for (CategorieEvenement cat : categories) {
                cbCategorie.getItems().add(cat.getNom_categorie());
            }

            System.out.println("✅ " + categories.size() + " catégories chargées");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    // ================= VALIDATION EN TEMPS RÉEL =================
    private void setupRealtimeValidation() {
        // Validation pour le titre
        tfTitre.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && tfTitre.getText().trim().isEmpty()) {
                tfTitre.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (!isNowFocused) {
                tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });

        // Validation pour la description
        taDescription.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && taDescription.getText().trim().isEmpty()) {
                taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            } else if (!isNowFocused) {
                taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        });

        // Validation pour le lieu
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

        // Validation stricte
        if (!validerChamps()) {
            return;
        }

        // Récupérer l'ID de la catégorie sélectionnée
        int idCategorie = getIdCategorieFromNom(cbCategorie.getValue());

        if (idCategorie == -1) {
            afficherErreur("Catégorie invalide sélectionnée !");
            return;
        }

        // Créer l'événement
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
            retourEvenements(event);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur d'ajout", "Impossible d'ajouter l'événement : " + e.getMessage());
        }
    }

    // ================= VALIDATION STRICTE =================
    private boolean validerChamps() {
        cacherErreur();

        // ===== VÉRIFICATION 1 : Titre =====
        String titre = tfTitre.getText();
        if (titre == null || titre.trim().isEmpty()) {
            afficherErreur("Le titre de l'événement ne peut pas être vide !");
            tfTitre.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            tfTitre.requestFocus();
            return false;
        }

        if (titre.trim().length() < 5) {
            afficherErreur("Le titre doit contenir au moins 5 caractères !");
            tfTitre.requestFocus();
            return false;
        }

        // ===== VÉRIFICATION 2 : Description =====
        String description = taDescription.getText();
        if (description == null || description.trim().isEmpty()) {
            afficherErreur("La description ne peut pas être vide !");
            taDescription.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            taDescription.requestFocus();
            return false;
        }

        if (description.trim().length() < 10) {
            afficherErreur("La description doit contenir au moins 10 caractères !");
            taDescription.requestFocus();
            return false;
        }

        // ===== VÉRIFICATION 3 : Type d'événement =====
        if (cbTypeEvenement.getValue() == null || cbTypeEvenement.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un type d'événement !");
            cbTypeEvenement.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            cbTypeEvenement.requestFocus();
            return false;
        }

        // ===== VÉRIFICATION 4 : Dates =====
        if (dpDateDebut.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date de début !");
            dpDateDebut.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            dpDateDebut.requestFocus();
            return false;
        }

        if (dpDateFin.getValue() == null) {
            afficherErreur("Veuillez sélectionner une date de fin !");
            dpDateFin.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            dpDateFin.requestFocus();
            return false;
        }

        // Vérifier que la date de fin n'est pas avant la date de début
        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            afficherErreur("La date de fin ne peut pas être avant la date de début !");
            dpDateFin.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            dpDateFin.requestFocus();
            return false;
        }

        // Vérifier que les dates ne sont pas trop anciennes
        LocalDate aujourdhui = LocalDate.now();
        if (dpDateDebut.getValue().isBefore(aujourdhui)) {
            afficherErreur("La date de début ne peut pas être dans le passé!");
            dpDateDebut.requestFocus();
            return false;
        }

        // ===== VÉRIFICATION 5 : Lieu =====
        String lieu = tfLieu.getText();
        if (lieu == null || lieu.trim().isEmpty()) {
            afficherErreur("Le lieu ne peut pas être vide !");
            tfLieu.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #E74C3C; -fx-border-width: 2; -fx-border-radius: 8;");
            tfLieu.requestFocus();
            return false;
        }

        if (lieu.trim().length() < 3) {
            afficherErreur("Le lieu doit contenir au moins 3 caractères !");
            tfLieu.requestFocus();
            return false;
        }

        // ===== VÉRIFICATION 6 : Catégorie =====
        if (cbCategorie.getValue() == null || cbCategorie.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner une catégorie !");
            cbCategorie.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            cbCategorie.requestFocus();
            return false;
        }

        // ===== VÉRIFICATION 7 : Statut =====
        if (cbStatut.getValue() == null || cbStatut.getValue().isEmpty()) {
            afficherErreur("Veuillez sélectionner un statut !");
            cbStatut.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2;");
            cbStatut.requestFocus();
            return false;
        }

        System.out.println("✅ Validation réussie !");
        return true;
    }

    // ================= RÉCUPÉRER L'ID DE LA CATÉGORIE =================
    private int getIdCategorieFromNom(String nomCategorie) {
        for (CategorieEvenement cat : categories) {
            if (cat.getNom_categorie().equals(nomCategorie)) {
                return cat.getId_categorie();
            }
        }
        return -1;
    }

    // ================= AFFICHER/CACHER ERREUR =================
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

        // Réinitialiser les styles
        tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0; -fx-border-radius: 8;");
        taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0; -fx-border-radius: 8;");
        tfLieu.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0; -fx-border-radius: 8;");
        cbTypeEvenement.setStyle("");
        cbCategorie.setStyle("");
        cbStatut.setStyle("");
        dpDateDebut.setStyle("");
        dpDateFin.setStyle("");
    }

    // ================= RETOUR ÉVÉNEMENTS =================
    @FXML
    void retourEvenements(ActionEvent event) {
        System.out.println("=== Navigation vers AfficherEvenements ===");
        chargerPage("AfficherEvenements.fxml");
    }

    @FXML
    private void goToAccueil(ActionEvent event) {
        chargerPage("Accueil.fxml");
    }

    // ================= CHARGER PAGE =================
    private void chargerPage(String fxml) {
        try {
            System.out.println("Chargement de : /G-Evenements/" + fxml);

            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) tfTitre.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Navigation réussie vers " + fxml);

        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de navigation",
                    "Impossible de charger la page : " + fxml + "\n" + e.getMessage());
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