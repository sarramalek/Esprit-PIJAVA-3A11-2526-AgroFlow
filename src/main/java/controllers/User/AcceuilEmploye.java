package controllers.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import models.User.Tache;
import services.User.TacheService;
import utils.SessionManager;

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
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
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
        chargerAvatarSidebar(SessionManager.getCurrentUser());

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
    private void chargerAvatarSidebar(Personne user) {
        if (user == null) return;

        String photoUrl = user.getPhotoUrl();

        if (photoUrl == null || photoUrl.isBlank()
                || photoUrl.equals("0") || photoUrl.equals("null")) {
            // Pas de photo → emoji par défaut, rien à faire
            return;
        }

        // Clip circulaire appliqué en Java (pas possible en FXML)
        Circle clip = new Circle(32, 32, 32);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 64, 64, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        avatarDefaultLabel.setManaged(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Erreur chargement avatar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
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

        Label titleLabel = new Label(tache.getNomTache());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label statusLabel = new Label("Statut: " + tache.getEtat());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        Label dateLabel = new Label("📅 " + tache.getDateEcheance());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95A5A6;");

        card.getChildren().addAll(titleLabel, statusLabel, dateLabel);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Handlers
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleDashboardEmp() {
        System.out.println("📊 Dashboard Employé (page actuelle)");
    }

    @FXML
    private void handleMesTaches(MouseEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/MesTaches.fxml"));
            Parent root = loader.load();

            MesTaches ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser); // ← c'est ce qui manque !

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
            stage.setTitle("AgroFlow - Mes Tâches");
        } catch (IOException e) {
            e.printStackTrace();
        }    }

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
            stage.setScene(new Scene(root, 800, 700));
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
    private void handleLogout(MouseEvent event) {
        System.out.println("🚪 Déconnexion Employé...");
navigateTo(event,"/UsersInterface/login.fxml", "Login");
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Utility
    // ══════════════════════════════════════════════════════════════

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
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

    public void handleMesTerrains(ActionEvent actionEvent) {
    }

    public void ouvrirTerrains(MouseEvent mouseEvent) {
    }

    public void ouvrirPlantes(MouseEvent mouseEvent) {
    }

    public void ouvrirRotations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent, "/TerrainsInterface/EmployeRotation.fxml", "EmployeRotation");
    }

    public void handleMateriel(MouseEvent mouseEvent) {
        navigateTo(mouseEvent, "/MaterielsInterface/MaintenanceFront.fxml", "EmployeMaintenance");

    }

    public void handleEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsEmp.fxml", "EvenementsAfficher");
    }
}