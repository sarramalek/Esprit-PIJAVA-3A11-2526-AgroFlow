package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import models.User.Abonnements;
import models.User.Personne;
import services.User.AbonnementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static controllers.User.GestionAbonnements.showInfo;

public class AcceuilAgricole {
    // Ajouter ces champs
    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutBtn;

    // Labels stats abonnements
    @FXML private VBox abonnementsContainer;

    private  Personne currentUser;
    private AbonnementService abonnementService;

    @FXML
    public void initialize() {
        System.out.println("✓ AcceuilAgricole Controller initialisé");

        abonnementService = new AbonnementService();

        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
    }

    /*public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            if (userRoleLabel != null)
                userRoleLabel.setText("Agriculteur");
            System.out.println("✓ Utilisateur Agricole défini: " + user.getNom());

            // Charger les abonnements après avoir défini l'utilisateur
            loadAbonnements();
        }
    }*/

    /**
     * Charger et afficher les abonnements de l'utilisateur connecté
     */
    private void loadAbonnements() {
        if (abonnementsContainer == null || currentUser == null) return;

        abonnementsContainer.getChildren().clear();

        try {
            List<Abonnements> abonnements = abonnementService.getAbonnementsByUser(currentUser.getCin());

            if (abonnements == null || abonnements.isEmpty()) {
                // Afficher message vide
                VBox emptyBox = new VBox(15);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setStyle("-fx-padding: 40;");

                Label icon  = new Label("💳");
                icon.setStyle("-fx-font-size: 48px;");

                Label msg1 = new Label("Aucun abonnement actif");
                msg1.setStyle("-fx-font-size: 16px; -fx-text-fill: #95A5A6;");

                Label msg2 = new Label("Souscrivez à une offre pour accéder à plus de fonctionnalités");
                msg2.setStyle("-fx-font-size: 14px; -fx-text-fill: #BDC3C7;");

                Button btnSouscrire = new Button("➕ Voir les offres");
                btnSouscrire.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
                btnSouscrire.setOnAction(e -> handleMonAbonnement());

                emptyBox.getChildren().addAll(icon, msg1, msg2, btnSouscrire);
                abonnementsContainer.getChildren().add(emptyBox);

            } else {
                // Afficher chaque abonnement sous forme de carte
                for (Abonnements ab : abonnements) {
                    abonnementsContainer.getChildren().add(createAbonnementCard(ab));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errLabel = new Label("⚠️ Impossible de charger les abonnements");
            errLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px;");
            abonnementsContainer.getChildren().add(errLabel);
        }
    }

    /**
     * Créer une carte visuelle pour un abonnement
     */
    private HBox createAbonnementCard(Abonnements ab) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #F8FFF8; -fx-background-radius: 12; " +
                "-fx-border-color: #C8E6C9; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        // Icône
        Label icon = new Label("💳");
        icon.setStyle("-fx-font-size: 36px; -fx-background-color: #E8F5E9; " +
                "-fx-padding: 12; -fx-background-radius: 12;");

        // Infos abonnement
        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);

        // Titre avec id offre
        Label titre = new Label("Abonnement #" + ab.getId_offre());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Dates
        HBox dates = new HBox(20);
        Label dateDebut = new Label("📅 Début : " + (ab.getDate_inscription() != null
                ? ab.getDate_inscription().toString() : "N/A"));
        dateDebut.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F8C8D;");

        Label dateFin = new Label("⏰ Fin : " + (ab.getDate_expiration() != null
                ? ab.getDate_expiration().toString() : "N/A"));
        dateFin.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F8C8D;");
        dates.getChildren().addAll(dateDebut, dateFin);

        infos.getChildren().addAll(titre, dates);

        // Badge statut
        String situation = ab.getSituation() != null ? ab.getSituation() : "inconnu";
        Label badge = new Label();
        switch (situation.toLowerCase()) {
            case "actif", "active" -> {
                badge.setText("✅ ACTIF");
                badge.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;");
            }
            case "expiré", "expire", "expired" -> {
                badge.setText("❌ EXPIRÉ");
                badge.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;");
            }
            default -> {
                badge.setText("⏸ " + situation.toUpperCase());
                badge.setStyle("-fx-background-color: #FFF8E1; -fx-text-fill: #F57F17; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;");
            }
        }

        card.getChildren().addAll(icon, infos, badge);
        return card;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboard()    { /* Déjà sur cette page */ }
    @FXML private void handleMesTerrains()  { System.out.println("🌾 Terrains..."); }
    @FXML private void handleMesAnimaux()   { System.out.println("🐄 Animaux..."); }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel()  { System.out.println("🚜 Matériel..."); }
    @FXML private void handleMonProfil()    { System.out.println("👤 Profil..."); }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement() {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo("/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }
    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(AcceuilAgricole.class.getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    stage.setScene(new Scene(root, 900, 600));
                    stage.setTitle("AgroFlow - Connexion");
                    System.out.println("✓ Déconnexion réussie");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true); }
    }
    public void hideGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
    }
   public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Ajouter cette méthode getStage() pour ProfilAgricole
    public Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null)
            return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (welcomeNameLabel != null)
                welcomeNameLabel.setText(user.getPrenom() + " !");
            else
                System.err.println("✗ welcomeNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText("🌾 AGRICULTEUR");

            loadAbonnements();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    // Ajouter cette méthode handleAPropos()
    @FXML
    private void handleAPropos() {
        navigateTo("/UsersInterface/ProfilAgricole.fxml","ddd");
    }
    private void navigateTo(String fxmlPath, String title) {
        try {
            // 1. Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 2. Transférer l'utilisateur courant au nouveau contrôleur
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                transferUserToController(controller);
            }

            // 3. Obtenir le stage et changer de scène
            Stage stage = getStage();
            if (stage == null) {
                showError("Impossible d'obtenir la fenêtre principale");
                return;
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen(); // Centrer la fenêtre

            System.out.println("✓ Navigation vers: " + title);

        } catch (IOException e) {
            handleNavigationError(fxmlPath, title, e);
        }
    }

    /**
     * Transfère l'utilisateur courant au contrôleur cible via réflexion
     */
    private void transferUserToController(Object controller) {
        try {
            controller.getClass()
                    .getMethod("setCurrentUser", Personne.class)
                    .invoke(controller, currentUser);
            System.out.println("✓ Utilisateur transféré au contrôleur");
        } catch (NoSuchMethodException e) {
            System.out.println("ℹ Le contrôleur n'a pas de méthode setCurrentUser()");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors du transfert utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère les erreurs de navigation de manière appropriée
     */
    private void handleNavigationError(String fxmlPath, String title, IOException e) {
        e.printStackTrace();

        // Vérifier si c'est un fichier manquant ou une autre erreur
        if (e.getMessage() != null && e.getMessage().contains("Location is not set")) {
            showInfo("Module à venir",
                    "Le module \"" + title + "\" sera disponible prochainement.");
        } else if (fxmlPath.contains("MesTerrains") ||
                fxmlPath.contains("MesAnimaux") ||
                fxmlPath.contains("MesStocks") ||
                fxmlPath.contains("MonMateriel")) {
            // Modules pas encore implémentés
            showInfo("Fonctionnalité à venir",
                    "Cette fonctionnalité est en cours de développement.");
        } else {
            // Erreur réelle
            showError("Erreur de chargement\n\n" +
                    "Impossible de charger " + title + ".\n" +
                    "Détails: " + e.getMessage());
        }
    }

}