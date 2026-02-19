package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import models.User.Employe;
import models.User.Personne;
import models.User.Tache;
import services.User.PersonneService;
import services.User.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class GestionTache {

    // Sidebar
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn, dashboardBtn, logoutBtn, addTaskBtn;

    // Labels stats
    @FXML private Label userNameLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label termineesLabel;
    @FXML private Label enRetardLabel;

    // Filtres
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> employeeFilterComboBox;

    // Table — fx:id="tasksTable" dans le FXML
    @FXML private TableView<Tache> tasksTable;
    @FXML private TableColumn<Tache, String> titleColumn;
    @FXML private TableColumn<Tache, String> descriptionColumn;
    @FXML private TableColumn<Tache, String> assignedToColumn;
    @FXML private TableColumn<Tache, String> statusColumn;
    @FXML private TableColumn<Tache, String> dueDateColumn;
    @FXML private TableColumn<Tache, Void> actionsColumn;

    private TacheService tacheService;
    private PersonneService personneService;
    private ObservableList<Tache> tachesList;
    private ObservableList<Tache> allTachesList;
    private List<Employe> employes;
    private Personne currentUser;

    @FXML
    public void initialize() {
        // Cacher submenu par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        // Hover handlers pour le sous-menu
        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }

        try {
            tacheService = new TacheService();
            personneService = new PersonneService();
            System.out.println("✓ GestionTache Controller initialisé");

            // Charger les employés pour les filtres
            employes = personneService.getEmployes();

            setupFilters();
            setupTable();
            loadTaches();
            setupSearch();

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation de GestionTache");
            e.printStackTrace();
            showError("Erreur d'initialisation", "Impossible de charger le module Tâches");
        }
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null && userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }
    }

    // ── Filtres ──────────────────────────────────────────────────────────────

    private void setupFilters() {
        // Filtre statut
        if (filterComboBox != null) {
            filterComboBox.setItems(FXCollections.observableArrayList(
                    "Tous", "en_attente", "en_cours", "terminee", "annulee"
            ));
            filterComboBox.setValue("Tous");
            filterComboBox.setOnAction(e -> applyAllFilters());
        }

        // Filtre employé
        if (employeeFilterComboBox != null && employes != null) {
            ObservableList<String> employeNames = FXCollections.observableArrayList("Tous");
            for (Employe emp : employes) {
                employeNames.add(emp.getPrenom() + " " + emp.getNom() + " (" + emp.getCin() + ")");
            }
            employeeFilterComboBox.setItems(employeNames);
            employeeFilterComboBox.setValue("Tous");
            employeeFilterComboBox.setOnAction(e -> applyAllFilters());
        }
    }

    private void applyAllFilters() {
        if (allTachesList == null) return;

        String search = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String statut = filterComboBox != null ? filterComboBox.getValue() : "Tous";
        String empFilter = employeeFilterComboBox != null ? employeeFilterComboBox.getValue() : "Tous";

        ObservableList<Tache> filtered = FXCollections.observableArrayList();

        for (Tache t : allTachesList) {
            // Filtre recherche
            boolean matchSearch = search.isEmpty()
                    || (t.getNom_tache() != null && t.getNom_tache().toLowerCase().contains(search))
                    || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search));

            // Filtre statut
            boolean matchStatut = statut == null || statut.equals("Tous")
                    || statut.equals(t.getEtat());

            // Filtre employé
            boolean matchEmp = true;
            if (empFilter != null && !empFilter.equals("Tous") && employes != null) {
                int idx = employeeFilterComboBox.getItems().indexOf(empFilter) - 1;
                if (idx >= 0 && idx < employes.size()) {
                    matchEmp = t.getAssignee() == employes.get(idx).getCin();
                }
            }

            if (matchSearch && matchStatut && matchEmp) {
                filtered.add(t);
            }
        }

        tasksTable.setItems(filtered);
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private void setupTable() {
        // Titre
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_tache"));

        // Description (tronquée)
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.length() > 60 ? item.substring(0, 60) + "..." : item);
            }
        });

        // Assignée à — afficher nom de l'employé
        assignedToColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                Tache t = getTableView().getItems().get(getIndex());
                if (t.getAssignee() <= 0) { setText("Non assigné"); return; }
                if (employes != null) {
                    employes.stream()
                            .filter(e -> e.getCin() == t.getAssignee())
                            .findFirst()
                            .ifPresentOrElse(
                                    e -> setText(e.getPrenom() + " " + e.getNom()),
                                    () -> setText("CIN: " + t.getAssignee())
                            );
                } else {
                    setText("CIN: " + t.getAssignee());
                }
            }
        });

        // Statut — avec badge coloré
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                Tache t = getTableView().getItems().get(getIndex());
                String etat = t.getEtat();
                if (etat == null) { setText("N/A"); return; }
                switch (etat) {
                    case "en_attente" -> { setText("⏸ En attente");  setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;"); }
                    case "en_cours"   -> { setText("▶ En cours");    setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;"); }
                    case "terminee"   -> { setText("✅ Terminée");    setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); }
                    case "annulee"    -> { setText("❌ Annulée");     setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;"); }
                    default           -> { setText(etat);            setStyle(""); }
                }
            }
        });

        // Date d'échéance — avec couleur si en retard
        dueDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                Tache t = getTableView().getItems().get(getIndex());
                String date = t.getDate_echeancee();
                if (date == null) { setText("N/A"); setStyle(""); return; }
                setText(date);
                try {
                    LocalDate d = LocalDate.parse(date);
                    if (d.isBefore(LocalDate.now()) && !"terminee".equals(t.getEtat())) {
                        setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2C3E50;");
                    }
                } catch (Exception ex) {
                    setStyle("");
                }
            }
        });

        // Actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn   = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button viewBtn   = new Button("👁️");
            private final HBox hbox = new HBox(6, viewBtn, editBtn, deleteBtn);

            {
                hbox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 10; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> handleViewTask(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEditTask(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteTask(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        tasksTable.setStyle("-fx-background-color: transparent;");
    }

    // ── Chargement ────────────────────────────────────────────────────────────

    public void loadTaches() {
        try {
            List<Tache> taches = tacheService.recuperer();
            allTachesList = FXCollections.observableArrayList(taches);
            tachesList    = FXCollections.observableArrayList(taches);
            tasksTable.setItems(tachesList);
            updateStats(taches);
            System.out.println("✓ " + taches.size() + " tâches chargées");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les tâches");
        }
    }

    private void updateStats(List<Tache> taches) {
        if (totalTasksLabel != null)
            totalTasksLabel.setText(String.valueOf(taches.size()));

        if (enCoursLabel != null)
            enCoursLabel.setText(String.valueOf(
                    taches.stream().filter(t -> "en_cours".equals(t.getEtat())).count()));

        if (termineesLabel != null)
            termineesLabel.setText(String.valueOf(
                    taches.stream().filter(t -> "terminee".equals(t.getEtat())).count()));

        if (enRetardLabel != null) {
            long retard = taches.stream().filter(t -> {
                if (t.getDate_echeancee() == null || "terminee".equals(t.getEtat())) return false;
                try { return LocalDate.parse(t.getDate_echeancee()).isBefore(LocalDate.now()); }
                catch (Exception e) { return false; }
            }).count();
            enRetardLabel.setText(String.valueOf(retard));
        }
    }

    // ── Recherche ─────────────────────────────────────────────────────────────

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyAllFilters());
        }
    }

    // ── Actions CRUD ──────────────────────────────────────────────────────────

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/AjoutTache.fxml"));
            Parent root = loader.load();
            AjoutTache controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Tâche");
            stage.setScene(new Scene(root, 1500, 520));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    private void handleViewTask(Tache tache) {
        // Résoudre le nom de l'employé
        String assigneNom = "Non assigné";
        if (tache.getAssignee() > 0 && employes != null) {
            assigneNom = employes.stream()
                    .filter(e -> e.getCin() == tache.getAssignee())
                    .map(e -> e.getPrenom() + " " + e.getNom())
                    .findFirst().orElse("CIN: " + tache.getAssignee());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Tâche");
        alert.setHeaderText("📋 " + tache.getNom_tache());
        alert.setContentText(
                "📝 Description  : " + tache.getDescription() + "\n\n" +
                        "👤 Assignée à   : " + assigneNom + "\n" +
                        "📊 Statut       : " + (tache.getEtat() != null ? tache.getEtat() : "N/A") + "\n" +
                        "⚡ Priorité     : " + (tache.getPriorite() != null ? tache.getPriorite() : "N/A") + "\n" +
                        "📅 Échéance     : " + (tache.getDate_echeancee() != null ? tache.getDate_echeancee() : "N/A")
        );
        alert.getDialogPane().setMinWidth(480);
        alert.showAndWait();
    }

    private void handleEditTask(Tache tache) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierTache.fxml"));
            Parent root = loader.load();
            ModifierTache controller = loader.getController();
            controller.setParentController(this);
            controller.setTache(tache);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Tâche");
            stage.setScene(new Scene(root, 600, 520));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void handleDeleteTask(Tache tache) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la tâche");
        alert.setContentText("Voulez-vous vraiment supprimer \"" + tache.getNom_tache() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                tacheService.supprimer(tache.getId_tache());
                loadTaches();
                showSuccess("Succès", "Tâche supprimée avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de supprimer la tâche");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Rafraîchissement...");
        loadTaches();
        if (filterComboBox != null) filterComboBox.setValue("Tous");
        if (employeeFilterComboBox != null) employeeFilterComboBox.setValue("Tous");
        if (searchField != null) searchField.clear();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handlePersonnes(MouseEvent event)    { Acceuil.ouvrirPersonnes(event); }
    @FXML private void handleTaches(MouseEvent event)       { /* déjà sur cette page */ }
    @FXML private void handleAffectations(MouseEvent event) { Acceuil.ouvrirAffectations(event); }
    @FXML private void handleAbonnements(MouseEvent event)  { Acceuil.ouvrirAbonnements(event); }
    @FXML private void handleOffres(MouseEvent event)       { Acceuil.ouvrirOffres(event); }
    @FXML private void handleGestion(MouseEvent event)      { }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/Acceuil.fxml"));
            Parent root = loader.load();
            Acceuil controller = loader.getController();
            if (currentUser != null) controller.setCurrentUser(currentUser);

            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1500, 700));
            stage.setTitle("AgroFlow - Dashboard");
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le dashboard");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setScene(new Scene(root, 900, 600));
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    // ── Sous-menu ─────────────────────────────────────────────────────────────

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

    // ── Helpers ───────────────────────────────────────────────────────────────

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
}