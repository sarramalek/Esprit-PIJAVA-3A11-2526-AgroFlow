package controllers.User;
import javax.activation.*;  // Pour DataHandler et FileDataSource

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import models.User.Personne;
import models.User.Employe;
import models.User.Utilisateur;
import services.User.*;
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
import utils.SessionManager;
import services.User.SmsService;



import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LogReportService logReportService;

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
        logReportService = new LogReportService();

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
    @FXML
    private void handleExportPDF() {
        // Récupérer la personne sélectionnée
        Personne selectedPerson = employeeTable.getSelectionModel().getSelectedItem();

        if (selectedPerson == null) {
            showWarning("Aucune personne sélectionnée",
                    "Veuillez sélectionner une personne dans le tableau.");
            return;
        }

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Générer l'historique des connexions");
        confirm.setContentText("Voulez-vous générer le rapport PDF pour " +
                selectedPerson.getPrenom() + " " + selectedPerson.getNom() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            generatePDF(selectedPerson);
        }
    }

    /**
     * Génère le PDF
     */
    private void generatePDF(Personne personne) {
        try {
            // Ouvrir le dialogue de sauvegarde
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");

            // Nom de fichier par défaut
            String defaultFileName = String.format("Historique_Connexions_%s_%s_%s.pdf",
                    personne.getNom().replace(" ", "_"),
                    personne.getPrenom().replace(" ", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            );
            fileChooser.setInitialFileName(defaultFileName);

            // Filtre d'extension
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            // Dossier par défaut (Documents ou Bureau)
            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Documents");
            if (!initialDir.exists()) {
                initialDir = new File(userHome, "Desktop");
            }
            if (initialDir.exists()) {
                fileChooser.setInitialDirectory(initialDir);
            }

            // Afficher le dialogue
            File file = fileChooser.showSaveDialog(pdfBtn.getScene().getWindow());

            if (file != null) {
                // Afficher un indicateur de chargement
                showProgress("Génération du PDF en cours...");

                // Générer le PDF
                boolean success = logReportService.generateLogHistoryPDF(personne, file.getAbsolutePath());

                if (success) {
                    showSuccess("PDF généré avec succès",
                            "Le rapport a été enregistré à :\n" + file.getAbsolutePath());

                    // Proposer d'ouvrir le fichier
                    Alert openAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    openAlert.setTitle("Ouvrir le fichier");
                    openAlert.setHeaderText("PDF généré avec succès");
                    openAlert.setContentText("Voulez-vous ouvrir le fichier maintenant ?");

                    Optional<ButtonType> openResult = openAlert.showAndWait();
                    if (openResult.isPresent() && openResult.get() == ButtonType.OK) {
                        openPDF(file);
                    }
                } else {
                    showError("Erreur lors de la génération du PDF","erreur de generation du pdf historique ");
                }
            }

        } catch (Exception e) {
            showError("Erreur lors de la génération du PDF : " , e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvre le PDF avec l'application par défaut
     */
    private void openPDF(File file) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showWarning("Ouverture impossible",
                        "Impossible d'ouvrir le fichier automatiquement.\n" +
                                "Veuillez l'ouvrir manuellement à l'emplacement :\n" +
                                file.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture du fichier","erreur de l'ouverture du fichier PDF ");
            e.printStackTrace();
        }
    }
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showProgress(String message) {
        // Simple toast-like notification
        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("En cours...");
        progress.setHeaderText(null);
        progress.setContentText(message);
        progress.show();

        // Auto-fermer après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(progress::close);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void handleEmailPDF() throws IOException {
        Personne selected = employeeTable.getSelectionModel().getSelectedItem();

        // Générer le PDF dans un fichier temporaire
        File tempFile = File.createTempFile("rapport_", ".pdf");
        logReportService.generateLogHistoryPDF(selected, tempFile.getAbsolutePath());

        // Envoyer par email avec JavaMail
        EmailService.sendPDFAttachment(
                selected.getEmail(),
                "Votre historique de connexions",
                "Veuillez trouver en pièce jointe votre historique.",
                tempFile
        );
    }
    // -------------------------------------------------------------------------------
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

    @FXML
    private void handleNavigateToSettings2FA(MouseEvent event) {
        try {
            Personne currentUser = SessionManager.getCurrentUser();
            String telephone = currentUser.getTel();

            if (telephone == null || telephone.isEmpty()) {
                // Pas de numéro → aller directement sans vérification SMS
                navigateToSettings2FA(event);
                return;
            }

            // Générer et envoyer OTP
            String otp = String.format("%06d", (int)(Math.random() * 999999));
            SessionManager.setTempOtp(otp);

            SmsService smsService = new SmsService();
            boolean sent = smsService.sendOtpCode(telephone, otp);

            if (!sent) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer le SMS.");
                return;
            }

            // Masquer le numéro
            String masked = telephone.length() > 4
                    ? telephone.substring(0, telephone.length() - 4).replaceAll("\\d", "*")
                    + telephone.substring(telephone.length() - 4)
                    : telephone;

            // Dialogue de saisie du code
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Vérification SMS");
            dialog.setHeaderText("Code envoyé au : " + masked);

            ButtonType verifyBtn = new ButtonType("Vérifier", ButtonBar.ButtonData.OK_DONE);
            ButtonType resendBtn = new ButtonType("Renvoyer", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyBtn, resendBtn, ButtonType.CANCEL);

            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(200);
            codeField.setStyle(
                    "-fx-font-family: 'Courier New'; -fx-font-size: 24px;" +
                            "-fx-alignment: center; -fx-pref-height: 52px;" +
                            "-fx-border-color: #52B788; -fx-border-radius: 8;" +
                            "-fx-background-radius: 8; -fx-border-width: 2;"
            );
            codeField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("\\d*")) codeField.setText(n.replaceAll("[^\\d]", ""));
                if (n.length() > 6)     codeField.setText(n.substring(0, 6));
            });

            VBox content = new VBox(14);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            content.getChildren().addAll(
                    new Label("📱 Entrez le code reçu par SMS :"),
                    codeField,
                    new Label("⏱  Valable 5 minutes")
            );
            dialog.getDialogPane().setContent(content);

            final String[] currentOtp = { otp };

            dialog.setResultConverter(btn -> {
                if (btn == resendBtn) {
                    String newOtp = String.format("%06d", (int)(Math.random() * 999999));
                    currentOtp[0] = newOtp;
                    SessionManager.setTempOtp(newOtp);
                    boolean reSent = smsService.sendOtpCode(telephone, newOtp);
                    Alert info = new Alert(reSent ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    info.setTitle(reSent ? "SMS renvoyé" : "Erreur");
                    info.setHeaderText(null);
                    info.setContentText(reSent ? "Nouveau code envoyé au " + masked : "Échec envoi SMS.");
                    info.showAndWait();
                    return null;
                }
                if (btn == verifyBtn) return codeField.getText();
                return null;
            });

            dialog.showAndWait().ifPresent(code -> {
                if (code.equals(currentOtp[0])) {
                    SessionManager.clearTempOtp();
                    navigateToSettings2FA(event); // ✅ Accès autorisé
                } else {
                    showAlert(Alert.AlertType.ERROR, "Code incorrect",
                            "Le code SMS est invalide. Accès refusé.");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navigation réelle vers Settings2FA
    private void navigateToSettings2FA(MouseEvent event) {
        Settings2FA settings = new Settings2FA();
        settings.launch();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}