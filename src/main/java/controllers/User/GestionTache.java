package controllers.User;

import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Employe;
import models.User.Personne;
import models.User.Tache;
import services.User.PersonneService;
import services.User.TacheService;
import utils.SessionManager;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionTache {

    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn, dashboardBtn, logoutBtn, addTaskBtn;
    @FXML private Label  userNameLabel, totalTasksLabel, enCoursLabel, termineesLabel, enRetardLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox, employeeFilterComboBox;
    @FXML private Label userRoleLabel;

    // PDF
    @FXML private Button pdfBtn;
    @FXML private Label  selectedTacheLabel;

    // Table
    @FXML private TableView<Tache>             tasksTable;
    @FXML private TableColumn<Tache, String>   titleColumn;
    @FXML private TableColumn<Tache, String>   descriptionColumn;
    @FXML private TableColumn<Tache, String>   assignedToColumn;
    @FXML private TableColumn<Tache, String>   statusColumn;
    @FXML private TableColumn<Tache, String>   dueDateColumn;
    @FXML private TableColumn<Tache, Void>     actionsColumn;

    private TacheService    tacheService;
    private PersonneService personneService;
    private ObservableList<Tache> tachesList, allTachesList;
    private List<Employe> employes;
    private Personne currentUser;
    private Tache selectedTache;
    // Image user
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    // ═══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        // ✅ CORRECTION PRINCIPALE : récupérer le user depuis SessionManager dès initialize()
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
        try {
            tacheService    = new TacheService();
            personneService = new PersonneService();
            employes = personneService.getEmployes();
            setupFilters();
            setupTable();
            loadTaches();
            setupSearch();
            setupPdfButton();
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger le module Tâches");
            e.printStackTrace();
        }
    }

    public void setCurrentUser(Personne user) {
        currentUser = user;
        if (user != null && userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
    }
    /**
     * Met à jour les labels nom/rôle dans la sidebar.
     */
    private void updateUserLabels() {
        if (currentUser == null) return;

        if (userNameLabel != null)
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        else
            System.err.println("✗ userNameLabel est NULL (non lié en FXML ?)");

        if (userRoleLabel != null) {
            String roleText = switch (currentUser.getRole()) {
                case 1 -> "🌾 AGRICOLE";
                case 2 -> "👷 EMPLOYÉ";
                case 3 -> "👑 ADMIN";
                default -> "Rôle inconnu";
            };
            userRoleLabel.setText(roleText);
        } else {
            System.err.println("✗ userRoleLabel est NULL (non lié en FXML ?)");
        }
    }
    private void chargerAvatarTopBar(Personne user) {
        if (user == null) return;

        // Afficher le nom
        if (userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }

        // Charger la photo depuis l'URL Cloudinary dans un thread background
        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            // Pas de photo → garder l'emoji par défaut, rien à faire
            return;
        }

        // Appliquer le clip circulaire en Java (ne fonctionne pas correctement en FXML)
        Circle clip = new Circle(24, 24, 24);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 48, 48, false, true, true);

                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF — génère une fiche détaillée de la tâche sélectionnée
    // ═══════════════════════════════════════════════════════════════

    private void setupPdfButton() {
        if (pdfBtn != null) pdfBtn.setDisable(true);
        safeLabel(selectedTacheLabel, "Sélectionnez une tâche pour générer sa fiche PDF");

        tasksTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedTache = newVal;
                    if (newVal != null) {
                        if (pdfBtn != null) pdfBtn.setDisable(false);
                        String assigneNom = resolveAssigne(newVal.getAssignee());
                        safeLabel(selectedTacheLabel,
                                "Sélectionnée : " + newVal.getNom_tache()
                                        + "  |  " + nvl(newVal.getEtat())
                                        + "  |  Assignée à : " + assigneNom);
                    } else {
                        if (pdfBtn != null) pdfBtn.setDisable(true);
                        safeLabel(selectedTacheLabel, "Sélectionnez une tâche pour générer sa fiche PDF");
                    }
                }
        );
    }

    @FXML
    private void handleGeneratePdf() {
        if (selectedTache == null) {
            showError("Aucune sélection", "Cliquez d'abord sur une ligne du tableau.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialFileName("tache_" + selectedTache.getId_tache() + "_"
                + selectedTache.getNom_tache().replaceAll("\\s+", "_") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File bureau = new File(System.getProperty("user.home") + "/Desktop");
        if (bureau.exists()) fc.setInitialDirectory(bureau);

        File fichier = fc.showSaveDialog((Stage) tasksTable.getScene().getWindow());
        if (fichier == null) return;

        try {
            generateTachePdf(selectedTache, fichier.getAbsolutePath());
            showPdfSuccess(fichier);
        } catch (Exception e) {
            showError("Erreur PDF", "Génération échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateTachePdf(Tache tache, String outputPath) throws Exception {
        com.itextpdf.kernel.pdf.PdfWriter   writer  = new com.itextpdf.kernel.pdf.PdfWriter(outputPath);
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc  = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document        doc     = new com.itextpdf.layout.Document(pdfDoc,
                com.itextpdf.kernel.geom.PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        com.itextpdf.kernel.colors.DeviceRgb C_DARK_BLUE = new com.itextpdf.kernel.colors.DeviceRgb(30, 58, 138);
        com.itextpdf.kernel.colors.DeviceRgb C_WHITE     = new com.itextpdf.kernel.colors.DeviceRgb(255,255,255);
        com.itextpdf.kernel.colors.DeviceRgb C_LIGHT     = new com.itextpdf.kernel.colors.DeviceRgb(239,246,255);
        com.itextpdf.kernel.colors.DeviceRgb C_DARK      = new com.itextpdf.kernel.colors.DeviceRgb(30,30,30);
        com.itextpdf.kernel.colors.DeviceRgb C_MUTED     = new com.itextpdf.kernel.colors.DeviceRgb(100,100,100);

        // couleur selon statut
        com.itextpdf.kernel.colors.DeviceRgb C_STATUS = switch (nvl(tache.getEtat())) {
            case "en_cours"   -> new com.itextpdf.kernel.colors.DeviceRgb(37, 99, 235);
            case "terminee"   -> new com.itextpdf.kernel.colors.DeviceRgb(22, 163, 74);
            case "annulee"    -> new com.itextpdf.kernel.colors.DeviceRgb(220, 38, 38);
            default           -> new com.itextpdf.kernel.colors.DeviceRgb(234, 88, 12);
        };

        com.itextpdf.kernel.font.PdfFont bold    = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        com.itextpdf.kernel.font.PdfFont regular = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Header
        com.itextpdf.layout.element.Table banner =
                new com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{35,65}))
                        .setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100))
                        .setBackgroundColor(C_DARK_BLUE);
        banner.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("AgroFlow").setFont(bold).setFontSize(17).setFontColor(C_WHITE))
                .setPadding(14).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        banner.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("FICHE TÂCHE").setFont(bold).setFontSize(20).setFontColor(C_WHITE)
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
                .setPadding(14).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        doc.add(banner);
        doc.add(new com.itextpdf.layout.element.Paragraph(" "));

        // Titre + badge statut
        doc.add(new com.itextpdf.layout.element.Paragraph(tache.getNom_tache())
                .setFont(bold).setFontSize(20).setFontColor(C_DARK_BLUE)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

        com.itextpdf.layout.element.Table badge =
                new com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{100}))
                        .setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(25));
        badge.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(nvl(tache.getEtat()).toUpperCase())
                        .setFont(bold).setFontSize(10).setFontColor(C_WHITE)
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                .setBackgroundColor(C_STATUS).setPadding(5)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        doc.add(badge);
        doc.add(new com.itextpdf.layout.element.Paragraph(" "));

        // Détails
        doc.add(new com.itextpdf.layout.element.Paragraph("Détails de la tâche")
                .setFont(bold).setFontSize(12).setFontColor(C_DARK_BLUE)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(C_DARK_BLUE, 2))
                .setPaddingBottom(4).setMarginBottom(6));

        com.itextpdf.layout.element.Table t =
                new com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{35,65}))
                        .setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        addRow(t, bold, regular, "ID Tâche",        "#" + tache.getId_tache(),             C_LIGHT, C_DARK);
        addRow(t, bold, regular, "Titre",            tache.getNom_tache(),                  C_WHITE, C_DARK);
        addRow(t, bold, regular, "Assignée à",       resolveAssigne(tache.getAssignee()),   C_LIGHT, C_DARK);
        addRow(t, bold, regular, "Statut",           nvl(tache.getEtat()),                  C_WHITE, C_DARK);
        addRow(t, bold, regular, "Priorité",         nvl(tache.getPriorite()),               C_LIGHT, C_DARK);
        addRow(t, bold, regular, "Date d'échéance",  nvl(tache.getDate_echeancee()),         C_WHITE, C_DARK);

        // Retard ?
        boolean enRetard = false;
        try {
            enRetard = tache.getDate_echeancee() != null
                    && LocalDate.parse(tache.getDate_echeancee()).isBefore(LocalDate.now())
                    && !"terminee".equals(tache.getEtat());
        } catch (Exception ignored) {}
        if (enRetard)
            addRow(t, bold, regular, "⚠ Retard", "Cette tâche est en retard !",
                    new com.itextpdf.kernel.colors.DeviceRgb(254,226,226),
                    new com.itextpdf.kernel.colors.DeviceRgb(220,38,38));

        doc.add(t);
        doc.add(new com.itextpdf.layout.element.Paragraph(" "));

        // Description
        if (tache.getDescription() != null && !tache.getDescription().isBlank()) {
            doc.add(new com.itextpdf.layout.element.Paragraph("Description")
                    .setFont(bold).setFontSize(12).setFontColor(C_DARK_BLUE)
                    .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(C_DARK_BLUE, 2))
                    .setPaddingBottom(4).setMarginBottom(6));
            doc.add(new com.itextpdf.layout.element.Paragraph(tache.getDescription())
                    .setFont(regular).setFontSize(11).setFontColor(C_DARK)
                    .setBackgroundColor(C_LIGHT).setPadding(12)
                    .setBorder(new com.itextpdf.layout.borders.SolidBorder(
                            new com.itextpdf.kernel.colors.DeviceRgb(147,197,253), 1)));
            doc.add(new com.itextpdf.layout.element.Paragraph(" "));
        }

        // Footer
        doc.add(new com.itextpdf.layout.element.Paragraph(
                "Fiche générée le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                        + "  |  AgroFlow")
                .setFont(regular).setFontSize(8).setFontColor(C_MUTED)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(C_MUTED, 0.5f))
                .setPaddingTop(8));

        doc.close();
    }

    private void addRow(com.itextpdf.layout.element.Table t,
                        com.itextpdf.kernel.font.PdfFont bold, com.itextpdf.kernel.font.PdfFont regular,
                        String label, String value,
                        com.itextpdf.kernel.colors.DeviceRgb bg,
                        com.itextpdf.kernel.colors.DeviceRgb fg) {
        t.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(label).setFont(bold).setFontSize(10).setFontColor(fg))
                .setBackgroundColor(bg).setPadding(7).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        t.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(value).setFont(regular).setFontSize(10).setFontColor(fg))
                .setBackgroundColor(bg).setPadding(7).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLE / FILTRES / LOAD
    // ═══════════════════════════════════════════════════════════════

    private void setupFilters() {
        if (filterComboBox != null) {
            filterComboBox.setItems(FXCollections.observableArrayList("Tous","en_attente","en_cours","terminee","annulee"));
            filterComboBox.setValue("Tous");
            filterComboBox.setOnAction(e -> applyAllFilters());
        }
        if (employeeFilterComboBox != null && employes != null) {
            ObservableList<String> names = FXCollections.observableArrayList("Tous");
            employes.forEach(emp -> names.add(emp.getPrenom() + " " + emp.getNom() + " (" + emp.getCin() + ")"));
            employeeFilterComboBox.setItems(names);
            employeeFilterComboBox.setValue("Tous");
            employeeFilterComboBox.setOnAction(e -> applyAllFilters());
        }
    }

    private void applyAllFilters() {
        if (allTachesList == null) return;
        String search  = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String statut  = filterComboBox != null ? filterComboBox.getValue() : "Tous";
        String empFilter = employeeFilterComboBox != null ? employeeFilterComboBox.getValue() : "Tous";
        ObservableList<Tache> filtered = FXCollections.observableArrayList();
        for (Tache t : allTachesList) {
            boolean ms = search.isEmpty()
                    || (t.getNom_tache() != null && t.getNom_tache().toLowerCase().contains(search))
                    || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search));
            boolean mst = statut == null || statut.equals("Tous") || statut.equals(t.getEtat());
            boolean me = true;
            if (empFilter != null && !empFilter.equals("Tous") && employes != null) {
                int idx = employeeFilterComboBox.getItems().indexOf(empFilter) - 1;
                if (idx >= 0 && idx < employes.size()) me = t.getAssignee() == employes.get(idx).getCin();
            }
            if (ms && mst && me) filtered.add(t);
        }
        tasksTable.setItems(filtered);
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_tache"));

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.length() > 60 ? item.substring(0,60)+"..." : item);
            }
        });

        assignedToColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                setText(resolveAssigne(getTableView().getItems().get(getIndex()).getAssignee()));
            }
        });

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                String etat = getTableView().getItems().get(getIndex()).getEtat();
                if (etat == null) { setText("N/A"); return; }
                switch (etat) {
                    case "en_attente" -> { setText("⏸ En attente"); setStyle("-fx-text-fill:#F39C12;-fx-font-weight:bold;"); }
                    case "en_cours"   -> { setText("▶ En cours");   setStyle("-fx-text-fill:#3498DB;-fx-font-weight:bold;"); }
                    case "terminee"   -> { setText("✅ Terminée");   setStyle("-fx-text-fill:#27AE60;-fx-font-weight:bold;"); }
                    case "annulee"    -> { setText("❌ Annulée");    setStyle("-fx-text-fill:#E74C3C;-fx-font-weight:bold;"); }
                    default           -> { setText(etat);           setStyle(""); }
                }
            }
        });

        dueDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                Tache t = getTableView().getItems().get(getIndex());
                setText(t.getDate_echeancee() != null ? t.getDate_echeancee() : "N/A");
                try {
                    if (t.getDate_echeancee() != null
                            && LocalDate.parse(t.getDate_echeancee()).isBefore(LocalDate.now())
                            && !"terminee".equals(t.getEtat()))
                        setStyle("-fx-text-fill:#E74C3C;-fx-font-weight:bold;");
                    else setStyle("-fx-text-fill:#2C3E50;");
                } catch (Exception ex) { setStyle(""); }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn   = new Button("👁️");
            private final Button editBtn   = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox   hbox      = new HBox(6, viewBtn, editBtn, deleteBtn);
            {
                hbox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 10;-fx-cursor:hand;");
                editBtn.setStyle("-fx-background-color:#FF9800;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 10;-fx-cursor:hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 10;-fx-cursor:hand;");
                viewBtn.setOnAction(e   -> handleViewTask(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e   -> handleEditTask(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteTask(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : hbox);
            }
        });

        tasksTable.setRowFactory(tv -> {
            TableRow<Tache> row = new TableRow<>();
            row.selectedProperty().addListener((obs, was, is) ->
                    row.setStyle(is ? "-fx-background-color:#dbeafe;" : ""));
            return row;
        });
        tasksTable.setStyle("-fx-background-color:transparent;");
    }

    public void loadTaches() {
        try {
            List<Tache> taches = tacheService.recuperer();
            allTachesList = FXCollections.observableArrayList(taches);
            tachesList    = FXCollections.observableArrayList(taches);
            tasksTable.setItems(tachesList);
            updateStats(taches);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les tâches");
        }
    }

    private void updateStats(List<Tache> taches) {
        safeLabel(totalTasksLabel, String.valueOf(taches.size()));
        safeLabel(enCoursLabel,    String.valueOf(taches.stream().filter(t -> "en_cours".equals(t.getEtat())).count()));
        safeLabel(termineesLabel,  String.valueOf(taches.stream().filter(t -> "terminee".equals(t.getEtat())).count()));
        if (enRetardLabel != null)
            enRetardLabel.setText(String.valueOf(taches.stream().filter(t -> {
                if (t.getDate_echeancee() == null || "terminee".equals(t.getEtat())) return false;
                try { return LocalDate.parse(t.getDate_echeancee()).isBefore(LocalDate.now()); }
                catch (Exception e) { return false; }
            }).count()));
    }

    private void setupSearch() {
        if (searchField != null) searchField.textProperty().addListener((obs, o, n) -> applyAllFilters());
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

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
        } catch (IOException e) { showError("Erreur", "Impossible d'ouvrir le formulaire"); }
    }

    private void handleViewTask(Tache t) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails"); alert.setHeaderText("📋 " + t.getNom_tache());
        alert.setContentText(
                "Description : " + t.getDescription() + "\n" +
                        "Assignée à  : " + resolveAssigne(t.getAssignee()) + "\n" +
                        "Statut      : " + nvl(t.getEtat()) + "\n" +
                        "Priorité    : " + nvl(t.getPriorite()) + "\n" +
                        "Échéance    : " + nvl(t.getDate_echeancee()));
        alert.getDialogPane().setMinWidth(480);
        alert.showAndWait();
    }

    private void handleEditTask(Tache t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierTache.fxml"));
            Parent root = loader.load();
            ModifierTache controller = loader.getController();
            controller.setParentController(this);
            controller.setTache(t);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Tâche");
            stage.setScene(new Scene(root, 600, 520));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) { showError("Erreur", "Impossible d'ouvrir le formulaire"); }
    }

    private void handleDeleteTask(Tache t) {
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + t.getNom_tache() + "\" ?")
                .showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    try { tacheService.supprimer(t.getId_tache()); loadTaches(); showSuccess("Succès", "Tâche supprimée"); }
                    catch (SQLException e) { showError("Erreur", "Impossible de supprimer"); }
                });
    }

    @FXML private void handleRefresh() { loadTaches(); if (filterComboBox != null) filterComboBox.setValue("Tous"); if (searchField != null) searchField.clear(); }

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════

    @FXML private void handlePersonnes(MouseEvent e)   { navigateTo(e, "/UsersInterface/DahboardPersonne.fxml",    "Personnes");    }
    @FXML private void handleTaches(MouseEvent e)      { /* déjà ici */ }
    @FXML private void handleAbonnements(MouseEvent e) { navigateTo(e, "/UsersInterface/GestionAbonnements.fxml",  "Abonnements"); }
    @FXML private void handleOffres(MouseEvent e)      { navigateTo(e, "/UsersInterface/GestionOffre.fxml",        "Offres");      }
    @FXML private void handleGestion(MouseEvent e)     {}
    @FXML private void handleAnimals(MouseEvent e)     { navigateTo(e, "/AnimalsInterface/AfficherAnimaux.fxml",   "Animaux");     }
    @FXML private void handleStocks(MouseEvent e)      { navigateTo(e, "/StocksInterface/afficherarticle.fxml",    "Stocks");      }
    @FXML private void handleTerrains(MouseEvent e)    { navigateTo(e, "/TerrainsInterface/acceuilterrain.fxml",   "Terrains");    }
    @FXML private void handleEvents(MouseEvent e)      { navigateTo(e, "/G-Evenements/Accueil.fxml",               "Événements"); }
    @FXML private void handleMateriels(MouseEvent e)   { navigateTo(e, "/MaterielsInterface/AccueilMateriel.fxml", "Matériels");  }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/Acceuil.fxml"));
            Parent root = loader.load();
            Acceuil controller = loader.getController();
            if (currentUser != null) controller.setCurrentUser(currentUser);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1500, 700)); stage.setTitle("AgroFlow - Dashboard"); stage.setMaximized(true);
        } catch (IOException e) { showError("Erreur", "Impossible de charger le dashboard"); }
    }

    @FXML
    private void handleLogout() {
        new Alert(Alert.AlertType.CONFIRMATION, "Se déconnecter ?").showAndWait()
                .filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) logoutBtn.getScene().getWindow();
                        stage.setScene(new Scene(root, 1500, 700)); stage.setTitle("AgroFlow - Connexion"); stage.setMaximized(true);
                    } catch (IOException e) { showError("Erreur", "Impossible de se déconnecter"); }
                });
    }

    @FXML
    private void openDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/StatsDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage(); stage.setTitle("Statistiques AgroFlow"); stage.setScene(new Scene(root)); stage.show();
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private String resolveAssigne(int cin) {
        if (cin <= 0) return "Non assigné";
        if (employes == null) return "CIN: " + cin;
        return employes.stream().filter(e -> e.getCin() == cin)
                .map(e -> e.getPrenom() + " " + e.getNom())
                .findFirst().orElse("CIN: " + cin);
    }

    private void showPdfSuccess(File f) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PDF généré"); alert.setHeaderText("✅ PDF créé avec succès"); alert.setContentText(f.getAbsolutePath());
        ButtonType open = new ButtonType("📂 Ouvrir", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(open, close);
        alert.showAndWait().ifPresent(btn -> { if (btn == open) try { Desktop.getDesktop().open(f); } catch (Exception e) {} });
    }

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Transmettre currentUser si le contrôleur le supporte
            Object controller = loader.getController();
            if (controller instanceof GestionTache dp) {
                dp.setCurrentUser(this.currentUser);
            }
            // Ajoutez d'autres types si nécessaire

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
            stage.setScene(new Scene(root, 1500, 700));
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
    private void showGestionSubmenu() { if (gestionSubmenu != null) { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true); } }
    private void hideGestionSubmenu() { if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); } }
    private void safeLabel(Label l, String v) { if (l != null) l.setText(v); }
    private String nvl(String s) { return s != null ? s : "—"; }
    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR);       a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void showSuccess(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}