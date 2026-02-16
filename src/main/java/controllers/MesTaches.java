package controllers;
import java.sql.ResultSet;
import java.sql.Statement;
import models.Tache ;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import models.Personne;
import models.Tache;
import services.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la vue "Mes Tâches" des employés
 * Affiche uniquement les tâches assignées à l'employé connecté
 */
public class MesTaches {

    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
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
    @FXML private TableColumn<Tache, LocalDate> dateColumn;
    @FXML private TableColumn<Tache, Void> actionsColumn;

    private TacheService tacheService;
    private ObservableList<Tache> tachesList, allTachesList;
    private Personne currentUser;

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
            showError("Erreur", "Impossible de charger le module Mes Tâches");
        }
    }

    /**
     * Définir l'utilisateur connecté et charger ses tâches
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            }
            System.out.println("✓ Utilisateur défini: " + user.getNom());

            // Charger les tâches de cet utilisateur
            loadMyTaches();
        }
    }

    /**
     * Configurer la table
     */
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_tache"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_tache"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        prioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_echeance"));

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
                    switch (item) {
                        case "En cours":
                            setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                            break;
                        case "Terminée":
                            setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                            break;
                        case "En retard":
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
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
                    switch (item) {
                        case "Urgente":
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                            break;
                        case "Haute":
                            setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                            break;
                        case "Moyenne":
                            setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #95A5A6; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colonne Actions - Employé peut voir et changer le statut
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️ Voir");
            private final Button statusBtn = new Button("📝 Statut");
            private final HBox hbox = new HBox(5, viewBtn, statusBtn);

            {
                hbox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
                statusBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");

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

    /**
     * Charger les tâches de l'utilisateur connecté
     */
    public void loadMyTaches() {
        if (currentUser == null) {
            System.err.println("⚠️ Aucun utilisateur connecté");
            return;
        }

        try {
            // Récupérer uniquement les tâches assignées à cet employé
            List<Tache> taches = tacheService.recupererTachesParPersonne(currentUser.getCin());
            allTachesList = FXCollections.observableArrayList(taches);
            tachesList = FXCollections.observableArrayList(taches);

            if (taskTable != null) {
                taskTable.setItems(tachesList);
            }

            System.out.println("✓ " + taches.size() + " tâches chargées pour " + currentUser.getNom());

        } catch (SQLException e) {
            System.err.println("✗ Erreur chargement tâches");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos tâches");
        }
    }

    /**
     * Configurer la recherche
     */
    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newVal) -> applyFilter(newVal));
        }
    }

    /**
     * Appliquer le filtre de recherche
     */
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
                        String.valueOf(t.getId_tache()).contains(filter)) {
                    filtered.add(t);
                }
            }
            taskTable.setItems(filtered);
        }
    }

    /**
     * Voir les détails d'une tâche
     */
    private void handleViewTask(Tache tache) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Tâche #" + tache.getId_tache());
        alert.setHeaderText(tache.getNom_tache());
        alert.setContentText(
                "Description: " + tache.getDescription() + "\n" +
                        "Statut: " + tache.getEtat() + "\n" +
                        "Priorité: " + tache.getPriorite() + "\n" +
                        "Date d'échéance: " + tache.getDate_echeancee()
        );
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    /**
     * Changer le statut d'une tâche
     */
    private void handleChangeStatus(Tache tache) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(tache.getEtat(),
                "En attente", "En cours", "Terminée");
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Tâche: " + tache.getNom_tache());
        dialog.setContentText("Nouveau statut:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nouveauStatut -> {
            try {
                tache.setEtat(nouveauStatut);
                tacheService.modifier(tache);
                loadMyTaches();
                showSuccess("Succès", "Statut mis à jour");
                System.out.println("✓ Statut changé: " + nouveauStatut);
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de modifier le statut");
            }
        });
    }

    /**
     * Rafraîchir
     */
    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement...");
        loadMyTaches();
    }

    /**
     * Retour au dashboard
     */
    @FXML
    private void handleDashboard() {
        System.out.println("📊 Retour au dashboard employé...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Acceuil_emp.fxml"));
            Parent root = loader.load();

            AcceuilEmploye controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Dashboard Employé");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Déconnexion
     */
    @FXML
    private void handleLogout() {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showSuccess(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}