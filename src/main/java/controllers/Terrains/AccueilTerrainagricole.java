package controllers.Terrains;

import controllers.User.AcceuilAgricole;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import models.User.Personne;
import services.User.AbonnementService;
import utils.SessionManager;

import java.io.IOException;

import static controllers.User.GestionAbonnements.showInfo;

public class AccueilTerrainagricole {
    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutBtn;
    private  Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;

    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        System.out.println("✓ AcceuilAgricole Controller initialisé");



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

    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;

        // Nom et rôle
        if (userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Clip circulaire appliqué en Java (radius=35, centre=35,35 pour fitWidth/Height=70)
        if (sidebarAvatarImageView != null) {
            Circle clip = new Circle(35, 35, 35);
            sidebarAvatarImageView.setClip(clip);
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Avatar sidebar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
        void ouvrirTerrains(MouseEvent event) {
            chargerPage(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
        }

        @FXML
        void ouvrirPlantes(MouseEvent event) {
            chargerPage(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
        }

        @FXML
        void ouvrirRotations(MouseEvent event) {
            chargerPage(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
        }

        private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

                stage.setScene(new Scene(root));
                stage.setTitle(titre);
                stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement FXML : " + fxmlPath);
                e.printStackTrace();
            }
        }
    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboard(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuilAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains()  { System.out.println("🌾 Current Page "); }
    @FXML private void handleMesAnimaux()   { System.out.println("🐄 Animaux..."); }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel()  { System.out.println("🚜 Matériel..."); }
    @FXML private void handleMonProfil(MouseEvent event )    {navigateTo(event,"/UsersInterface/ProfilEmplye.fxml","Mon Profil");  }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
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
                    stage.setScene(new Scene(root, 1500, 700));
                    stage.setTitle("AgroFlow - Connexion");
                    stage.setMaximized(true);
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


        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    // Ajouter cette méthode handleAPropos()
    @FXML
    private void handleAPropos(MouseEvent event) {
        navigateTo(event,"/UsersInterface/ProfilAgricole.fxml","ddd");
    }
    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
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

