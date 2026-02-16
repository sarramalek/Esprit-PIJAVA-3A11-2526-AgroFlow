package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Personne;

import java.io.IOException;

/**
 * Contrôleur pour le dashboard Employé
 * Accès limité aux fonctionnalités pour les employés
 */
public class AcceuilEmploye {

    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutBtn;

    private Personne currentUser;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("✓ AcceuilEmploye Controller initialisé");

        // Cacher le sous-menu par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        // Configurer les événements du sous-menu
        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
    }

    /**
     * Définir l'utilisateur connecté
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText("Employé");
            }
            System.out.println("✓ Utilisateur Employé défini: " + user.getNom());
        }
    }

    /**
     * Navigation - Mes Tâches (tâches assignées à cet employé)
     */
    @FXML
    private void handleMesTaches() {
        System.out.println("📋 Ouverture Mes Tâches...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MesTaches.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            // Passer l'utilisateur pour filtrer ses tâches
            if (controller instanceof MesTaches) {
                ((MesTaches) controller).setCurrentUser(currentUser);
            }

            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Mes Tâches");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de charger Mes Tâches");
        }
    }

    /**
     * Navigation - Dashboard (page actuelle)
     */
    @FXML
    private void handleDashboard() {
        System.out.println("📊 Dashboard Employé (page actuelle)");
        // Déjà sur cette page
    }

    /**
     * Navigation - Mon Profil
     */
    @FXML
    private void handleMonProfil() {
        System.out.println("👤 Ouverture Mon Profil...");
        // Implémenter la navigation vers le profil
    }

    /**
     * Navigation - Rapports (lecture seule pour employés)
     */
    @FXML
    private void handleRapports() {
        System.out.println("📊 Ouverture Rapports...");
        // Implémenter la navigation vers les rapports
    }

    /**
     * Déconnexion
     */
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion Employé...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    Scene scene = new Scene(root, 900, 600);
                    stage.setScene(scene);
                    stage.setTitle("AgroFlow - Connexion");

                    System.out.println("✓ Déconnexion réussie");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Afficher le sous-menu Gestion
     */
    private void showGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    /**
     * Cacher le sous-menu Gestion
     */
    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    /**
     * Afficher une erreur
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}