package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import models.User.Tache;
import services.User.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour le dashboard Employé
 */
public class AcceuilEmploye {

    // ══════════════════════════════════════════════════════════════
    // FXML Components
    // ══════════════════════════════════════════════════════════════

    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutBtn;
    @FXML private Button dashboardBtn;

    // Labels statistiques
    @FXML private Label tachesEnCoursLabel;
    @FXML private Label tachesTermineesLabel;
    @FXML private Label tachesEnRetardLabel;

    // Container pour tâches récentes
    @FXML private VBox recentTasksContainer;

    // ══════════════════════════════════════════════════════════════
    // Instance Variables
    // ══════════════════════════════════════════════════════════════

    private Personne currentUser;
    private TacheService tacheService;

    // ══════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        System.out.println("✓ AcceuilEmploye Controller initialisé");

        try {
            tacheService = new TacheService();
        } catch (Exception e) {
            System.err.println("✗ Erreur initialisation TacheService");
            e.printStackTrace();
        }

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

    // ══════════════════════════════════════════════════════════════
    // User Management
    // ══════════════════════════════════════════════════════════════

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText("👷 EMPLOYÉ");
            else
                System.err.println("✗ userRoleLabel est NULL !");

            // IMPORTANT: Charger les statistiques seulement si tacheService est initialisé
            if (tacheService != null) {
                loadStatistiques();
            } else {
                System.err.println("⚠️ tacheService est NULL, statistiques non chargées");
            }
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Load Statistics
    // ══════════════════════════════════════════════════════════════

    private void loadStatistiques() {
        System.out.println("\n=== loadStatistiques appelé ===");

        // Vérification CRITIQUE
        if (currentUser == null) {
            System.err.println("✗ ERREUR: currentUser est NULL dans loadStatistiques");
            // NE PAS afficher d'erreur à l'utilisateur ici, c'est un problème de code
            return;
        }

        if (tacheService == null) {
            System.err.println("✗ ERREUR: tacheService est NULL");
            return;
        }

        System.out.println("✓ currentUser: " + currentUser.getNom() + " (CIN: " + currentUser.getCin() + ")");

        try {
            List<Tache> taches = tacheService.recupererTachesParPersonne(currentUser.getCin());
            System.out.println("✓ " + taches.size() + " tâches récupérées");

            int enCours = 0;
            int terminees = 0;
            int enRetard = 0;

            for (Tache tache : taches) {
                String statut = tache.getEtat();
                if (statut != null) {
                    switch (statut.toLowerCase()) {
                        case "en cours":
                            enCours++;
                            break;
                        case "terminée":
                        case "terminee":
                            terminees++;
                            break;
                        case "en retard":
                            enRetard++;
                            break;
                    }
                }
            }

            // Mettre à jour les labels
            if (tachesEnCoursLabel != null) {
                tachesEnCoursLabel.setText(String.valueOf(enCours));
            }
            if (tachesTermineesLabel != null) {
                tachesTermineesLabel.setText(String.valueOf(terminees));
            }
            if (tachesEnRetardLabel != null) {
                tachesEnRetardLabel.setText(String.valueOf(enRetard));
            }

            System.out.println("✓ Statistiques: " + enCours + " en cours, " +
                    terminees + " terminées, " + enRetard + " en retard");

            // Charger les tâches récentes
            loadRecentTasks(taches);

            System.out.println("============================\n");

        } catch (SQLException e) {
            System.err.println("✗ Erreur chargement statistiques");
            e.printStackTrace();
            // N'afficher l'erreur que si c'est vraiment une erreur SQL
            showError("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void loadRecentTasks(List<Tache> taches) {
        if (recentTasksContainer == null) {
            System.err.println("⚠️ recentTasksContainer est NULL");
            return;
        }

        recentTasksContainer.getChildren().clear();

        if (taches.isEmpty()) {
            // Message vide déjà présent dans le FXML
            System.out.println("ℹ️ Aucune tâche récente");
            return;
        }

        // Afficher les 3 premières tâches
        int count = Math.min(3, taches.size());
        for (int i = 0; i < count; i++) {
            Tache tache = taches.get(i);
            VBox taskCard = createMiniTaskCard(tache);
            recentTasksContainer.getChildren().add(taskCard);
        }

        System.out.println("✓ " + count + " tâches récentes affichées");
    }

    private VBox createMiniTaskCard(Tache tache) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 8; " +
                "-fx-padding: 15; -fx-border-color: #E0E0E0; -fx-border-width: 1; " +
                "-fx-border-radius: 8;");

        Label titleLabel = new Label(tache.getNom_tache());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label statusLabel = new Label("Statut: " + tache.getEtat());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        Label dateLabel = new Label("📅 " + tache.getDate_echeancee());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95A5A6;");

        card.getChildren().addAll(titleLabel, statusLabel, dateLabel);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Handlers
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleDashboard() {
        System.out.println("📊 Dashboard Employé (page actuelle)");
    }

    @FXML
    private void handleMesTaches() {
        System.out.println("📋 Ouverture Mes Tâches...");

        // Vérifier que l'utilisateur existe
        if (currentUser == null) {
            System.err.println("✗ currentUser est NULL !");
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        navigateTo("/UsersInterface/MesTaches.fxml", "AgroFlow - Mes Tâches", 1200, 700);
    }

    @FXML
    private void handleMonProfil() {
        System.out.println("👤 Ouverture Mon Profil...");

        if (currentUser == null) {
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();

            ProfilEmploye controller = loader.getController();
            if (controller != null) {
                controller.setCurrentUser(currentUser);
                System.out.println("✓ Utilisateur passé au profil");
            }

            Stage stage = new Stage();
            stage.setTitle("Mon Profil - Employé");
            stage.setScene(new Scene(root, 600, 700));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();

            System.out.println("✓ Modal profil fermée");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleRapports() {
        System.out.println("📊 Ouverture Rapports...");
        showInfo("À venir", "Le module Rapports sera disponible prochainement.");
    }

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
                    // Nettoyer la session
                    currentUser = null;

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();

                    Stage stage = getStage();
                    if (stage != null) {
                        stage.setScene(new Scene(root, 900, 600));
                        stage.setTitle("AgroFlow - Connexion");
                        System.out.println("✓ Déconnexion réussie");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de se déconnecter: " + e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Utility
    // ══════════════════════════════════════════════════════════════

    private void navigateTo(String fxmlPath, String title, int width, int height) {
        System.out.println("\n=== Navigation vers: " + fxmlPath + " ===");

        if (currentUser == null) {
            System.err.println("✗ ERREUR: currentUser est NULL");
            showError("Erreur de session", "Votre session a expiré. Veuillez vous reconnecter.");
            return;
        }

        System.out.println("✓ currentUser: " + currentUser.getNom());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass()
                            .getMethod("setCurrentUser", Personne.class)
                            .invoke(controller, currentUser);
                    System.out.println("✓ Utilisateur transféré");
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur transfert utilisateur: " + e.getMessage());
                }
            }

            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(new Scene(root, width, height));
                stage.setTitle(title);
                System.out.println("✓ Navigation réussie");
            } else {
                showError("Erreur", "Impossible d'obtenir la fenêtre principale");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation",
                    "Impossible de charger " + title + ": " + e.getMessage());
        }

        System.out.println("=====================================\n");
    }

    // ══════════════════════════════════════════════════════════════
    // UI Helpers
    // ══════════════════════════════════════════════════════════════

    private void showGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null) {
            return (Stage) logoutBtn.getScene().getWindow();
        }
        if (dashboardBtn != null && dashboardBtn.getScene() != null) {
            return (Stage) dashboardBtn.getScene().getWindow();
        }
        if (userNameLabel != null && userNameLabel.getScene() != null) {
            return (Stage) userNameLabel.getScene().getWindow();
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    // Alert Helpers
    // ══════════════════════════════════════════════════════════════

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}