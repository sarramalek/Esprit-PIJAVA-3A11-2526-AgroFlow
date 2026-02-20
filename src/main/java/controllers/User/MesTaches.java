package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import models.User.Tache;
import services.User.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la vue "Mes Tâches" des employés
 */
public class MesTaches {

    // ══════════════════════════════════════════════════════════════
    // FXML Components
    // ══════════════════════════════════════════════════════════════

    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private TextField searchField;
    @FXML private Button refreshBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;

    // Table et colonnes
    @FXML private TableView<Tache> taskTable;
    @FXML private TableColumn<Tache, Integer> idColumn;
    @FXML private TableColumn<Tache, String> titleColumn;
    @FXML private TableColumn<Tache, String> descriptionColumn;
    @FXML private TableColumn<Tache, String> statutColumn;
    @FXML private TableColumn<Tache, String> prioriteColumn;
    @FXML private TableColumn<Tache, String> dateColumn;
    @FXML private TableColumn<Tache, Void> actionsColumn;

    // ══════════════════════════════════════════════════════════════
    // Instance Variables
    // ══════════════════════════════════════════════════════════════

    private TacheService tacheService;
    private ObservableList<Tache> tachesList;
    private ObservableList<Tache> allTachesList;
    private Personne currentUser;

    // ══════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        try {
            tacheService = new TacheService();
            System.out.println("✓ MesTaches Controller initialisé");

            if (taskTable != null) {
                setupTable();
            }

            setupSearch();

        } catch (Exception e) {
            System.err.println("✗ Erreur initialisation MesTaches");
            e.printStackTrace();
            showError("Erreur d'initialisation",
                    "Impossible de charger le module Mes Tâches: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // User Management
    // ══════════════════════════════════════════════════════════════

    /**
     * CRITIQUE: Définir l'utilisateur connecté et charger ses tâches
     */
    /**
     * Définir l'utilisateur connecté et charger les données
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            // Mise à jour des labels utilisateur
            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText(getRoleText(user.getRole())); // Méthode helper ci-dessous
            else
                System.err.println("✗ userRoleLabel est NULL !");

            // Charger les données spécifiques au contrôleur
            loadData();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    /**
     * Helper pour obtenir le texte du rôle
     */
    private String getRoleText(int role) {
        switch (role) {
            case 1: return "🌾 AGRICULTEUR";
            case 2: return "👷 EMPLOYÉ";
            case 3: return "👨‍💼 ADMIN";
            default: return "👤 UTILISATEUR";
        }
    }

    /**
     * Charger les données (à implémenter dans chaque contrôleur)
     */
    private void loadData() {
        // AcceuilEmploye: loadStatistiques()
        // MesTaches: loadMyTaches()
        // ProfilEmploye: loadUserData()
        // MesAbonnements: loadMesAbonnements() + loadOffresDisponibles()
    }

    // ══════════════════════════════════════════════════════════════
    // Table Setup
    // ══════════════════════════════════════════════════════════════

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_tache"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_tache"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        prioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_echeancee"));

        // Style pour le statut
        statutColumn.setCellFactory(col -> new TableCell<Tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "en cours":
                            setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                            break;
                        case "terminée":
                        case "terminee":
                            setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                            break;
                        case "en retard":
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                            break;
                        case "en attente":
                            setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #95A5A6; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Style pour la priorité
        prioriteColumn.setCellFactory(col -> new TableCell<Tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "urgente":
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                            break;
                        case "haute":
                            setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                            break;
                        case "moyenne":
                            setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                            break;
                        case "basse":
                            setStyle("-fx-text-fill: #95A5A6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colonne Actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️ Voir");
            private final Button statusBtn = new Button("📝 Statut");
            private final HBox hbox = new HBox(5, viewBtn, statusBtn);

            {
                hbox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; " +
                        "-fx-font-size: 11px;");
                statusBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; " +
                        "-fx-font-size: 11px;");

                viewBtn.setOnAction(e -> {
                    Tache t = getTableView().getItems().get(getIndex());
                    handleViewTask(t);
                });

                statusBtn.setOnAction(e -> {
                    Tache t = getTableView().getItems().get(getIndex());
                    handleChangeStatus(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Data Loading
    // ══════════════════════════════════════════════════════════════

    private void loadMyTaches() {
        System.out.println("\n=== loadMyTaches appelé ===");

        if (currentUser == null) {
            System.err.println("✗ ERREUR CRITIQUE: currentUser est NULL !");
            System.err.println("⚠️ Aucun utilisateur connecté - impossible de charger les tâches");
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        System.out.println("✓ currentUser présent: " + currentUser.getNom());

        if (taskTable == null) {
            System.err.println("⚠️ taskTable est NULL");
            return;
        }

        try {
            System.out.println("🔄 Récupération des tâches pour CIN: " + currentUser.getCin());
            List<Tache> taches = tacheService.recupererTachesParPersonne(currentUser.getCin());

            allTachesList = FXCollections.observableArrayList(taches);
            tachesList = FXCollections.observableArrayList(taches);

            taskTable.setItems(tachesList);

            System.out.println("✓ " + taches.size() + " tâches chargées pour " + currentUser.getNom());
            System.out.println("============================\n");

        } catch (SQLException e) {
            System.err.println("✗ Erreur chargement tâches");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos tâches: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Search Functionality
    // ══════════════════════════════════════════════════════════════

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newVal) -> applyFilter(newVal));
        }
    }

    private void applyFilter(String searchText) {
        if (allTachesList == null || taskTable == null) return;

        String filter = searchText.toLowerCase().trim();

        if (filter.isEmpty()) {
            taskTable.setItems(allTachesList);
        } else {
            ObservableList<Tache> filtered = FXCollections.observableArrayList();
            for (Tache t : allTachesList) {
                if ((t.getNom_tache() != null && t.getNom_tache().toLowerCase().contains(filter)) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(filter)) ||
                        (t.getEtat() != null && t.getEtat().toLowerCase().contains(filter)) ||
                        (t.getPriorite() != null && t.getPriorite().toLowerCase().contains(filter)) ||
                        String.valueOf(t.getId_tache()).contains(filter)) {
                    filtered.add(t);
                }
            }
            taskTable.setItems(filtered);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Task Actions
    // ══════════════════════════════════════════════════════════════

    private void handleViewTask(Tache tache) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Tâche #" + tache.getId_tache());
        alert.setHeaderText(tache.getNom_tache());

        StringBuilder content = new StringBuilder();
        content.append("Description: ").append(tache.getDescription() != null ? tache.getDescription() : "N/A").append("\n\n");
        content.append("Statut: ").append(tache.getEtat() != null ? tache.getEtat() : "N/A").append("\n");
        content.append("Priorité: ").append(tache.getPriorite() != null ? tache.getPriorite() : "N/A").append("\n");
        content.append("Date d'échéance: ").append(tache.getDate_echeancee() != null ? tache.getDate_echeancee() : "N/A");

        alert.setContentText(content.toString());
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    private void handleChangeStatus(Tache tache) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                tache.getEtat(),
                "En attente", "En cours", "Terminée"
        );

        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Tâche: " + tache.getNom_tache());
        dialog.setContentText("Nouveau statut:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauStatut -> {
            try {
                tache.setEtat(nouveauStatut);
                tacheService.modifier(tache);
                loadMyTaches();
                showSuccess("Succès", "Statut mis à jour avec succès");
                System.out.println("✓ Statut changé: " + nouveauStatut);
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de modifier le statut: " + e.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Handlers
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement...");

        if (currentUser == null) {
            System.err.println("✗ Impossible de rafraîchir: currentUser est NULL");
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        loadMyTaches();
        if (searchField != null) {
            searchField.clear();
        }
    }

    @FXML
    private void handleDashboard(MouseEvent event) {
        System.out.println("\n=== handleDashboard appelé ===");
        System.out.println("📊 Retour au dashboard employé...");

        // CRITIQUE: Vérifier currentUser AVANT la navigation
        if (currentUser == null) {
            System.err.println("✗ ERREUR CRITIQUE: currentUser est NULL !");
            System.err.println("⚠️ Impossible de naviguer sans utilisateur");
            showError("Erreur de session",
                    "Votre session a expiré.\nVeuillez vous reconnecter.");

            // Rediriger vers login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = getStage();
                if (stage != null) {
                    stage.setScene(new Scene(root, 900, 600));
                    stage.setTitle("AgroFlow - Connexion");
                    stage.setMaximized(true);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        System.out.println("✓ currentUser présent: " + currentUser.getNom() + " (CIN: " + currentUser.getCin() + ")");

        // Navigation vers le dashboard
        navigateTo(event,"/UsersInterface/AcceuilEmp.fxml", "AgroFlow - Dashboard Employé");
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
                        stage.setMaximized(true);

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

    /**
     * CRITIQUE: Navigation avec transfert d'utilisateur
     */
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
    private Stage getStage() {
        if (dashboardBtn != null && dashboardBtn.getScene() != null) {
            return (Stage) dashboardBtn.getScene().getWindow();
        }
        if (logoutBtn != null && logoutBtn.getScene() != null) {
            return (Stage) logoutBtn.getScene().getWindow();
        }
        if (taskTable != null && taskTable.getScene() != null) {
            return (Stage) taskTable.getScene().getWindow();
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    // Alert Helpers
    // ══════════════════════════════════════════════════════════════

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showSuccess(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}