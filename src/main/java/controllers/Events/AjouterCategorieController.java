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
import services.Events.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AjouterCategorieController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
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
        naviguerVers("AfficherCategories.fxml",event);
    }

    @FXML
    void retourAccueil(ActionEvent event) {
        System.out.println("=== Navigation vers Accueil ===");
        naviguerVers("Accueil.fxml",event);
    }

    // ================= MÉTHODE DE NAVIGATION AMÉLIORÉE =================
    private void naviguerVers(String nomFichierFxml, Event event) {
        try {
            System.out.println("Chargement de : /G-Evenements/" + nomFichierFxml);

            // Charger le FXML
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + nomFichierFxml));



            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);

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

    @FXML
    private void handlePersonnes(Event event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}



    @FXML
    private void handleAbonnements(Event event) { /* Charger vue Abonnements */
        this.naviguerVers("/UsersInterface/GestionAbonnements.fxml",event);}
    @FXML private void handleOffres(Event event) { /* Charger vue Offres */
        this.naviguerVers("/UsersInterface/GestionOffre.fxml",event);}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.naviguerVers("/UsersInterface/Acceuil.fxml",actionEvent);

    }
    public void handleAnimals(Event mouseEvent) {
        this.naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",mouseEvent);

    }




    public void handleStocks(Event mouseEvent) {
        this.naviguerVers("/StocksInterface/afficherarticle.fxml",mouseEvent);
    }



    public void handleTerrains(Event mouseEvent) {
        this.naviguerVers("/TerrainsInterface/acceuilterrain.fxml",mouseEvent);
    }


    //
    public void handleEvents(Event mouseEvent) {
        this.naviguerVers("/G-Evenements/Accueil.fxml",mouseEvent);
    }


    public void handleMateriels(Event mouseEvent) {
        this.naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",mouseEvent);
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