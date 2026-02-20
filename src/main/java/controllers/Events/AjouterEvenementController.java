package controllers.Events;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
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
import java.util.Optional;

public class AjouterEvenementController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
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

            // Cacher submenu par défaut
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);

            // 1. Hover sur le bouton Gestion → Ouvre submenu
            gestionBtn.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });

            // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
            gestionContainer.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });
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
        chargerPage(event,"AfficherEvenements.fxml");
    }

    @FXML
    private void goToAccueil(ActionEvent event) {
        chargerPage(event,"Accueil.fxml");
    }

    // ================= CHARGER PAGE =================
    private void chargerPage(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
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

    @FXML
    private void handlePersonnes(Event event )  {
        this.chargerPage(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.chargerPage(event,"/UsersInterface/GestionTache.fxml");}



    @FXML
    private void handleAbonnements(Event event) { /* Charger vue Abonnements */
        this.chargerPage(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(Event event) { /* Charger vue Offres */
        this.chargerPage(event,"/UsersInterface/GestionOffre.fxml");}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.chargerPage(actionEvent,"/UsersInterface/Acceuil.fxml");

    }
    public void handleAnimals(Event mouseEvent) {
        this.chargerPage(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(Event mouseEvent) {
        this.chargerPage(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(Event mouseEvent) {
        this.chargerPage(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(Event mouseEvent) {
        this.chargerPage(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(Event mouseEvent) {
        this.chargerPage(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
    }
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }



    /**
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }





}