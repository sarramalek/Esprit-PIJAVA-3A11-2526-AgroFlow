package controllers.User;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import models.User.Personne;
import models.User.Employe;
import models.User.Utilisateur;
import services.User.PersonneService;
import services.User.PdfReportService;
import services.User.AbonnementService;
import models.User.Abonnements;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardPersonnes {

    // sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu, gestionContainer;
    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;

    // Boutons de filtre
    @FXML private Button filterAllBtn;
    @FXML private Button filterAgricoleBtn;
    @FXML private Button filterEmployeBtn;
    @FXML private Button filterAdminBtn;

    // Navigation
    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addEmployeeBtn;
    @FXML private Label  userNameLabel;
    @FXML private TextField searchField;

    // Bouton PDF (à ajouter dans le FXML)
    @FXML private Button pdfBtn;
    @FXML private Label  selectedPersonLabel;

    // Table
    @FXML private TableView<Personne> employeeTable;
    @FXML private TableColumn<Personne, String>  nomColumn;
    @FXML private TableColumn<Personne, String>  emailColumn;
    @FXML private TableColumn<Personne, Integer> roleColumn;
    @FXML private TableColumn<Personne, String>  dateColumn;
    @FXML private TableColumn<Personne, Void>    actionsColumn;

    // Formulaire tâche
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField        taskDescriptionField;
    @FXML private DatePicker       dueDatePicker;
    @FXML private Button           assignTaskBtn;

    private PersonneService    personneService;
    private AbonnementService  abonnementService;
    private PdfReportService   pdfReportService;

    private ObservableList<Personne> employeeList;
    private ObservableList<Personne> allPersonsList;
    private Personne currentUser;
    private Personne selectedPersonne;   // ← personne cliquée dans la table
    private String   currentFilter = "all";

    // ═══════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);

        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());

        try {
            personneService   = new PersonneService();
            abonnementService = new AbonnementService();
            pdfReportService  = new PdfReportService();

            setupTable();
            loadEmployees();
            setupSearch();
           // setupPdfButton();

        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger le dashboard");
            e.printStackTrace();
        }
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
    }

    @FXML
    private void handleExportStats() {
        PdfReportService pdfService = new PdfReportService();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport");
        fileChooser.setInitialFileName("rapport_stats_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File file = fileChooser.showSaveDialog(pdfBtn.getScene().getWindow());

        if (file != null) {
            try {
                pdfService.generateStatistiquesPdf(file.getAbsolutePath());
                showSuccess("Rapport exporté !");
            } catch (Exception e) {
                showAlert("Erreur : " + e.getMessage());
            }
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }
    // ═══════════════════════════════════════════════════════════════════
    // TABLE
    // ═══════════════════════════════════════════════════════════════════

    private void setupTable() {
        nomColumn.setCellValueFactory(cellData -> {
            Personne p = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(p.getPrenom() + " " + p.getNom());
        });

        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(col -> new TableCell<Personne, Integer>() {
            @Override
            protected void updateItem(Integer role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setText(null); setStyle(""); return; }
                String txt; String color;
                switch (role) {
                    case 1: txt = "🌾 Agricole"; color = "#27AE60"; break;
                    case 2: txt = "👷 Employé";  color = "#F39C12"; break;
                    case 3: txt = "👑 Admin";    color = "#9B59B6"; break;
                    default: txt = "Inconnu";    color = "#95A5A6";
                }
                setText(txt);
                setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
            }
        });

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_creationcpt"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn   = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox   hbox      = new HBox(8, editBtn, deleteBtn);
            {
                hbox.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color:#3498DB;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;");
                editBtn.setOnAction(e   -> handleEditEmployee(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteEmployee(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        // Highlight de la ligne sélectionnée
        employeeTable.setRowFactory(tv -> {
            TableRow<Personne> row = new TableRow<>();
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) row.setStyle("-fx-background-color: #e8f5e9;");
                else            row.setStyle("");
            });
            return row;
        });

        employeeTable.setStyle("-fx-background-color:transparent;");
    }

    public void loadEmployees() {
        try {
            List<Personne> personnes = personneService.recuperer();
            allPersonsList = FXCollections.observableArrayList(personnes);
            employeeList   = FXCollections.observableArrayList(personnes);
            employeeTable.setItems(employeeList);

            ObservableList<String> names = FXCollections.observableArrayList();
            personnes.forEach(p -> names.add(p.getPrenom() + " " + p.getNom()));
            if (employeeComboBox != null) employeeComboBox.setItems(names);

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les personnes");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // RECHERCHE & FILTRES
    // ═══════════════════════════════════════════════════════════════════

    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    @FXML private void handleFilterAll()     { currentFilter = "all";     applyFilters(); updateFilterButtonStyles(); }
    @FXML private void handleFilterAgricole(){ currentFilter = "agricole"; applyFilters(); updateFilterButtonStyles(); }
    @FXML private void handleFilterEmploye() { currentFilter = "employe";  applyFilters(); updateFilterButtonStyles(); }
    @FXML private void handleFilterAdmin()   { currentFilter = "admin";    applyFilters(); updateFilterButtonStyles(); }

    private void applyFilters() {
        if (allPersonsList == null) return;
        String search = searchField.getText().toLowerCase();
        ObservableList<Personne> filtered = FXCollections.observableArrayList();

        for (Personne p : allPersonsList) {
            boolean matchSearch = search.isEmpty()
                    || (p.getNom()    != null && p.getNom().toLowerCase().contains(search))
                    || (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(search))
                    || (p.getEmail()  != null && p.getEmail().toLowerCase().contains(search));
            boolean matchRole = switch (currentFilter) {
                case "agricole" -> p.getRole() == 1;
                case "employe"  -> p.getRole() == 2;
                case "admin"    -> p.getRole() == 3;
                default         -> true;
            };
            if (matchSearch && matchRole) filtered.add(p);
        }
        employeeTable.setItems(filtered);
    }

    private void updateFilterButtonStyles() {
        String active       = "-fx-background-color:#3498DB;-fx-text-fill:white;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inAll        = "-fx-background-color:transparent;-fx-border-color:#3498DB;-fx-border-width:2;-fx-text-fill:#3498DB;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inAgricole   = "-fx-background-color:transparent;-fx-border-color:#27AE60;-fx-border-width:2;-fx-text-fill:#27AE60;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inEmploye    = "-fx-background-color:transparent;-fx-border-color:#F39C12;-fx-border-width:2;-fx-text-fill:#F39C12;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inAdmin      = "-fx-background-color:transparent;-fx-border-color:#9B59B6;-fx-border-width:2;-fx-text-fill:#9B59B6;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";

        filterAllBtn.setStyle(currentFilter.equals("all")     ? active : inAll);
        filterAgricoleBtn.setStyle(currentFilter.equals("agricole") ? active : inAgricole);
        filterEmployeBtn.setStyle(currentFilter.equals("employe")   ? active : inEmploye);
        filterAdminBtn.setStyle(currentFilter.equals("admin")       ? active : inAdmin);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD PERSONNES
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAddEmployee(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/AjoutPersonne.fxml"));
            Parent root = loader.load();
            AjoutPersonne controller = loader.getController();
            controller.setDashboardController(this);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Employé");
            stage.setScene(new Scene(root, 550, 650));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    private void handleEditEmployee(Personne personne) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierPersonne.fxml"));
                Parent root = loader.load();
                ModifierPersonne controller = loader.getController();
                controller.setDashboardController(this);
                controller.setEmploye(personne);
                Stage stage = new Stage();
                stage.setTitle("Modifier l'Employé");
                stage.setScene(new Scene(root, 550, 650));
                stage.setResizable(false);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.centerOnScreen();
                stage.showAndWait();
            } catch (IOException e) {
                showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
            }

    }

    private void handleDeleteEmployee(Personne personne) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la personne");
        alert.setContentText("Voulez-vous vraiment supprimer "
                + personne.getPrenom() + " " + personne.getNom() + " ?");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                personneService.supprimer(personne.getCin());
                loadEmployees();
                applyFilters();
                showSuccess("Succès", "Personne supprimée avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer la personne");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean maximise = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(maximise);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation : " + fxmlPath);
        }
    }

    @FXML private void handleDashboard(MouseEvent event)    { navigateTo(event, "/UsersInterface/Acceuil.fxml",             "Accueil - AgroFlow"); }
    @FXML private void handlePersonnes(MouseEvent event)    { navigateTo(event, "/UsersInterface/Acceuil.fxml",             "Personnes - AgroFlow"); }
    @FXML private void handleTaches(MouseEvent event)       { navigateTo(event, "/UsersInterface/GestionTache.fxml",        "Tâches - AgroFlow"); }
    @FXML private void handleAbonnements(MouseEvent event)  { navigateTo(event, "/UsersInterface/GestionAbonnements.fxml",  "Abonnements - AgroFlow"); }
    @FXML private void handleOffres(MouseEvent event)       { navigateTo(event, "/UsersInterface/GestionOffre.fxml",        "Offres - AgroFlow"); }
    @FXML private void handleAnimals(MouseEvent event)      { navigateTo(event, "/AnimalsInterface/AfficherAnimaux.fxml",   "Animaux - AgroFlow"); }
    @FXML private void handleStocks(MouseEvent event)       { navigateTo(event, "/StocksInterface/afficherarticle.fxml",    "Stocks - AgroFlow"); }
    @FXML private void handleTerrains(MouseEvent event)     { navigateTo(event, "/TerrainsInterface/acceuilterrain.fxml",   "Terrains - AgroFlow"); }
    @FXML private void handleEvents(MouseEvent event)       { navigateTo(event, "/G-Evenements/Accueil.fxml",               "Événements - AgroFlow"); }
    @FXML private void handleMateriels(MouseEvent event)    { navigateTo(event, "/MaterielsInterface/AccueilMateriel.fxml", "Matériels - AgroFlow"); }

    @FXML
    private void handleTaches() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/GestionTache.fxml"));
            Parent root = loader.load();
            GestionTache controller = loader.getController();
            if (currentUser != null) controller.setCurrentUser(currentUser);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("AgroFlow - Gestion des Tâches");
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la gestion des tâches");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 700));
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);
            } catch (IOException e) {
                showError("Erreur", "Impossible de retourner à la connexion");
            }
        });
    }

    @FXML
    private void openDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/StatsDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Statistiques AgroFlow");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SOUS-MENUS
    // ═══════════════════════════════════════════════════════════════════

    @FXML private void handleGestionToggle() {
        gestionSubmenu.setVisible(!gestionSubmenu.isVisible());
        gestionToggle.setText(gestionSubmenu.isVisible() ? "⚙️  Gestion ▼" : "⚙️  Gestion ▶");
    }
    @FXML private void handleOperationsToggle() {
        operationsSubmenu.setVisible(!operationsSubmenu.isVisible());
        operationsToggle.setText(operationsSubmenu.isVisible() ? "🚜  Opérations ▼" : "🚜  Opérations ▶");
    }
    @FXML private void handleGestion(MouseEvent event) { /* Vue principale Gestion */ }
    private void showGestionSubmenu() { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true);  }
    private void hideGestionSubmenu() { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }

    // ═══════════════════════════════════════════════════════════════════
    // TÂCHE
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAssignTask() {
        String employee   = employeeComboBox.getValue();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();
        if (employee == null || description.isEmpty() || dueDate == null) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }
        showSuccess("Succès", "Tâche assignée à " + employee);
        employeeComboBox.setValue(null);
        taskDescriptionField.clear();
        dueDatePicker.setValue(null);
    }

    public void handleAddTask(ActionEvent e) {}
    public void handleRefresh(ActionEvent e) { loadEmployees(); }

    // ═══════════════════════════════════════════════════════════════════
    // ALERTES
    // ═══════════════════════════════════════════════════════════════════

    private void showError(String title, String msg)   { alert(Alert.AlertType.ERROR,       title, msg); }
    private void showSuccess(String title, String msg) { alert(Alert.AlertType.INFORMATION, title, msg); }
    private void showInfo(String title, String msg)    { alert(Alert.AlertType.INFORMATION, title, msg); }
    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}