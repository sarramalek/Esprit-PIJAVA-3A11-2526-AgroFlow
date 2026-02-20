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

public class ModifierCategorieController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML
    private TextField tfId;

    @FXML
    private TextField tfNom;

    @FXML
    private TextArea taDescription;

    @FXML
    private Label errorLabel;

    @FXML
    private Label infoLabel;

    private final CategorieEvenementService service = new CategorieEvenementService();
    private CategorieEvenement categorieActuelle;

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

    // ================= SETTER POUR RECEVOIR LA CATÉGORIE =================
    /**
     * Cette méthode est appelée depuis AfficherCategoriesController
     * pour passer la catégorie à modifier
     */
    public void setCategorie(CategorieEvenement categorie) {
        System.out.println("=== Catégorie reçue pour modification ===");
        System.out.println("ID : " + categorie.getId_categorie());
        System.out.println("Nom : " + categorie.getNom_categorie());
        System.out.println("Description : " + categorie.getDescription_categorie());

        this.categorieActuelle = categorie;

        // Pré-remplir les champs avec les données existantes
        tfId.setText(String.valueOf(categorie.getId_categorie()));
        tfNom.setText(categorie.getNom_categorie());
        taDescription.setText(categorie.getDescription_categorie());

        // Mettre à jour le label d'info
        if (infoLabel != null) {
            infoLabel.setText("Modification de : " + categorie.getNom_categorie());
        }

        System.out.println("✅ Champs pré-remplis avec succès !");
    }

    // ================= VALIDATION EN TEMPS RÉEL =================
    private void setupRealtimeValidation() {
        // Bordure rouge si vide, verte si valide
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

    // ================= MODIFIER CATÉGORIE =================
    @FXML
    void modifierCategorie(ActionEvent event) {
        System.out.println("=== Bouton Enregistrer cliqué ===");

        // Validation stricte des champs
        if (!validerChamps()) {
            return;
        }

        // Vérifier que la catégorie actuelle existe
        if (categorieActuelle == null) {
            showError("Erreur", "Aucune catégorie sélectionnée pour la modification !");
            return;
        }

        // Mettre à jour les données de la catégorie
        categorieActuelle.setNom_categorie(tfNom.getText().trim());
        categorieActuelle.setDescription_categorie(taDescription.getText().trim());

        try {
            System.out.println("Modification de la catégorie ID : " + categorieActuelle.getId_categorie());
            System.out.println("Nouveau nom : " + categorieActuelle.getNom_categorie());

            // Appeler le service pour modifier en base de données
            service.modifier(categorieActuelle);

            System.out.println("✅ Catégorie modifiée avec succès !");

            // Afficher un message de succès
            showSuccess("Succès",
                    "La catégorie \"" + categorieActuelle.getNom_categorie() + "\" a été modifiée avec succès !");

            // Retourner à la liste des catégories
            retourCategories(event);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de modification",
                    "Impossible de modifier la catégorie : " + e.getMessage());
        }
    }

    // ================= VALIDATION STRICTE =================
    private boolean validerChamps() {
        cacherErreur();

        String nom = tfNom.getText();
        String description = taDescription.getText();

        // ===== VÉRIFICATION 1 : Champs NULL ou VIDES =====
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

        // ===== VÉRIFICATION 2 : Longueur minimale =====
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

        // ===== VÉRIFICATION 3 : Longueur maximale =====
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
        tfNom.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
        taDescription.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");
    }

    // ================= RETOUR CATÉGORIES =================
    @FXML
    void retourCategories(ActionEvent event) {
        chargerPage(event,"AfficherCategories.fxml");
    }

    @FXML
    void retourAccueil(ActionEvent event) {
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