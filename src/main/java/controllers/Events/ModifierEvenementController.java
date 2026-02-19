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
import java.util.List;

public class ModifierEvenementController {

    @FXML
    private TextField tfId;

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

    @FXML
    private Label infoLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();
    private Evenement evenementActuel;
    private List<CategorieEvenement> categories;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        setupRealtimeValidation();
        chargerCategories();
        remplirComboBoxes();
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

    // ================= SETTER POUR RECEVOIR L'ÉVÉNEMENT =================
    public void setEvenement(Evenement evenement) {
        System.out.println("=== Événement reçu pour modification ===");
        System.out.println("ID : " + evenement.getIdEvenement());
        System.out.println("Titre : " + evenement.getTitre());

        this.evenementActuel = evenement;

        // Pré-remplir les champs
        tfId.setText(String.valueOf(evenement.getIdEvenement()));
        tfTitre.setText(evenement.getTitre());
        taDescription.setText(evenement.getDescription());
        cbTypeEvenement.setValue(evenement.getTypeEvenement());
        dpDateDebut.setValue(evenement.getDateDebut().toLocalDate());
        dpDateFin.setValue(evenement.getDateFin().toLocalDate());
        tfLieu.setText(evenement.getLieu());
        cbStatut.setValue(evenement.getStatut());

        // Sélectionner la catégorie correspondante
        try {
            String nomCategorie = categorieService.getNomCategorieById(evenement.getIdCategorie());
            cbCategorie.setValue(nomCategorie);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Mettre à jour le label d'info
        if (infoLabel != null) {
            infoLabel.setText("Modification de : " + evenement.getTitre());
        }

        System.out.println("✅ Champs pré-remplis avec succès !");
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
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    // ================= VALIDATION EN TEMPS RÉEL =================
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

    // ================= MODIFIER ÉVÉNEMENT =================
    @FXML
    void modifierEvenement(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        if (!validerChamps()) {
            return;
        }

        if (evenementActuel == null) {
            showError("Erreur", "Aucun événement sélectionné !");
            return;
        }

        // Récupérer l'ID de la catégorie
        int idCategorie = getIdCategorieFromNom(cbCategorie.getValue());
        if (idCategorie == -1) {
            afficherErreur("Catégorie invalide !");
            return;
        }

        // Mettre à jour l'événement
        evenementActuel.setTitre(tfTitre.getText().trim());
        evenementActuel.setDescription(taDescription.getText().trim());
        evenementActuel.setTypeEvenement(cbTypeEvenement.getValue());
        evenementActuel.setDateDebut(Date.valueOf(dpDateDebut.getValue()));
        evenementActuel.setDateFin(Date.valueOf(dpDateFin.getValue()));
        evenementActuel.setLieu(tfLieu.getText().trim());
        evenementActuel.setStatut(cbStatut.getValue());
        evenementActuel.setIdCategorie(idCategorie);

        try {
            System.out.println("Modification de l'événement ID : " + evenementActuel.getIdEvenement());
            evenementService.modifier(evenementActuel);
            System.out.println("✅ Événement modifié avec succès !");

            showSuccess("Succès", "L'événement \"" + evenementActuel.getTitre() + "\" a été modifié avec succès !");
            retourEvenements(event);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de modifier l'événement : " + e.getMessage());
        }
    }

    // ================= VALIDATION STRICTE =================
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
            if (cat.getNom_categorie().equals(nomCategorie)) {
                return cat.getId_categorie();
            }
        }
        return -1;
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

        tfTitre.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0;");
        taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0;");
        tfLieu.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #E0E0E0;");
        cbTypeEvenement.setStyle("");
        cbCategorie.setStyle("");
        cbStatut.setStyle("");
        dpDateDebut.setStyle("");
        dpDateFin.setStyle("");
    }

    // ================= NAVIGATION =================
    @FXML
    void retourEvenements(ActionEvent event) {
        chargerPage("AfficherEvenements.fxml");
    }

    @FXML
    private void goToAccueil(ActionEvent event) {
        chargerPage("Accueil.fxml");
    }

    private void chargerPage(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
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