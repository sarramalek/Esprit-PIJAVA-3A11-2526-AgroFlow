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
import models.User.Personne;
import models.User.offres;
import services.User.AbonnementService;
import services.User.OffresServicees;
import services.User.PdfReportService;
import utils.SessionManager;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestionOffres {

    @FXML private VBox gestionSubmenu, operationsSubmenu, gestionContainer;
    @FXML private Button gestionToggle, operationsToggle, gestionBtn;
    @FXML private Button dashboardBtn, personnesBtn, tachesBtn, offresBtn;
    @FXML private Button abonnementsBtn, logoutBtn, addOffreBtn;
    @FXML private Label  userNameLabel;
    @FXML private Label  totalOffresLabel, maxPrixLabel, avgPrixLabel;
    @FXML private TextField searchField;
    @FXML private Label userRoleLabel;

    // PDF
    @FXML private Button pdfBtn;
    @FXML private Label  selectedOffreLabel;

    // Table
    @FXML private TableView<offres>             offresTable;
    @FXML private TableColumn<offres, Integer>  idColumn;
    @FXML private TableColumn<offres, String>   nomColumn;
    @FXML private TableColumn<offres, String>   descriptionColumn;
    @FXML private TableColumn<offres, Float>    prixColumn;
    @FXML private TableColumn<offres, Integer>  dureeColumn;
    @FXML private TableColumn<offres, Void>     actionsColumn;

    private OffresServicees  offresService;
    private AbonnementService abonnementService;
    private PdfReportService  pdfReportService;
    private ObservableList<offres> offresList;
    private ObservableList<offres> allOffresList;
    private offres selectedOffre;
    private Personne currentUser;
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

        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        try {
            offresService     = new OffresServicees();
            abonnementService = new AbonnementService();
            pdfReportService  = new PdfReportService();
            setupTable();
            loadOffres();
            setupSearch();
            updateStatistics();
            setupPdfButton();
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger les offres");
            e.printStackTrace();
        }
    }

    public void setCurrentUser(Personne user) {
        currentUser = user;
        if (user != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
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
    }    // ═══════════════════════════════════════════════════════════════
    // PDF — génère une fiche offre avec le nombre d'abonnés
    // ═══════════════════════════════════════════════════════════════

    private void setupPdfButton() {
        if (pdfBtn != null) pdfBtn.setDisable(true);
        safeLabel(selectedOffreLabel, "Sélectionnez une offre pour générer sa fiche PDF");

        offresTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedOffre = newVal;
                    if (newVal != null) {
                        if (pdfBtn != null) pdfBtn.setDisable(false);
                        safeLabel(selectedOffreLabel,
                                "Sélectionnée : " + newVal.getNom_offre()
                                        + "  |  " + String.format("%.2f TND", newVal.getPrix())
                                        + "  |  " + newVal.getDuree_offre() + " mois");
                    } else {
                        if (pdfBtn != null) pdfBtn.setDisable(true);
                        safeLabel(selectedOffreLabel, "Sélectionnez une offre pour générer sa fiche PDF");
                    }
                }
        );
    }

    @FXML
    private void handleGeneratePdf() {
        if (selectedOffre == null) {
            showError("Aucune sélection", "Cliquez d'abord sur une ligne du tableau.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialFileName("offre_" + selectedOffre.getNom_offre().replaceAll("\\s+", "_") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File bureau = new File(System.getProperty("user.home") + "/Desktop");
        if (bureau.exists()) fc.setInitialDirectory(bureau);

        File fichier = fc.showSaveDialog((Stage) offresTable.getScene().getWindow());
        if (fichier == null) return;

        try {
            // Récupérer le nb d'abonnés pour cette offre
            long nbAbonnes = abonnementService.recuperer().stream()
                    .filter(a -> a.getId_offre() == selectedOffre.getId_offres())
                    .count();

            generateOffrePdf(selectedOffre, nbAbonnes, fichier.getAbsolutePath());
            showPdfSuccess(fichier);
        } catch (Exception e) {
            showError("Erreur PDF", "Génération échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère un PDF avec les détails de l'offre.
     * Utilise iText directement car PdfReportService n'a pas de méthode dédiée aux offres.
     */
    private void generateOffrePdf(offres offre, long nbAbonnes, String outputPath) throws Exception {
        com.itextpdf.kernel.pdf.PdfWriter  writer  = new com.itextpdf.kernel.pdf.PdfWriter(outputPath);
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document doc           = new com.itextpdf.layout.Document(pdfDoc,
                com.itextpdf.kernel.geom.PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        com.itextpdf.kernel.colors.DeviceRgb C_GREEN  = new com.itextpdf.kernel.colors.DeviceRgb(22, 90, 22);
        com.itextpdf.kernel.colors.DeviceRgb C_WHITE  = new com.itextpdf.kernel.colors.DeviceRgb(255,255,255);
        com.itextpdf.kernel.colors.DeviceRgb C_LIGHT  = new com.itextpdf.kernel.colors.DeviceRgb(240,248,240);
        com.itextpdf.kernel.colors.DeviceRgb C_DARK   = new com.itextpdf.kernel.colors.DeviceRgb(30,30,30);
        com.itextpdf.kernel.colors.DeviceRgb C_MUTED  = new com.itextpdf.kernel.colors.DeviceRgb(100,100,100);

        com.itextpdf.kernel.font.PdfFont bold    = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        com.itextpdf.kernel.font.PdfFont regular = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Header
        com.itextpdf.layout.element.Table banner =
                new com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{35,65}))
                        .setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100))
                        .setBackgroundColor(C_GREEN);
        banner.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("AgroFlow").setFont(bold).setFontSize(17).setFontColor(C_WHITE))
                .setPadding(14).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        banner.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("FICHE OFFRE").setFont(bold).setFontSize(20).setFontColor(C_WHITE)
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
                .setPadding(14).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        doc.add(banner);
        doc.add(new com.itextpdf.layout.element.Paragraph(" "));

        // Titre offre
        doc.add(new com.itextpdf.layout.element.Paragraph(offre.getNom_offre())
                .setFont(bold).setFontSize(22).setFontColor(C_GREEN)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(16));

        // Section détails
        doc.add(new com.itextpdf.layout.element.Paragraph("Détails de l'offre")
                .setFont(bold).setFontSize(12).setFontColor(C_GREEN)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(C_GREEN, 2))
                .setPaddingBottom(4).setMarginBottom(6));

        com.itextpdf.layout.element.Table t =
                new com.itextpdf.layout.element.Table(
                        com.itextpdf.layout.properties.UnitValue.createPercentArray(new float[]{40,60}))
                        .setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        addRow(t, bold, regular, "Nom de l'offre",  offre.getNom_offre(), C_LIGHT, C_DARK);
        addRow(t, bold, regular, "Description",     offre.getDescription() != null ? offre.getDescription() : "—", C_WHITE, C_DARK);
        addRow(t, bold, regular, "Prix mensuel",    String.format("%.2f TND", offre.getPrix()), C_LIGHT, C_DARK);
        addRow(t, bold, regular, "Durée",           offre.getDuree_offre() + " mois", C_WHITE, C_DARK);
        addRow(t, bold, regular, "Montant total",   String.format("%.2f TND", offre.getPrix() * offre.getDuree_offre()), C_LIGHT, C_DARK);
        addRow(t, bold, regular, "Abonnés actifs",  String.valueOf(nbAbonnes), C_WHITE, C_DARK);
        doc.add(t);
        doc.add(new com.itextpdf.layout.element.Paragraph(" "));

        // Footer
        doc.add(new com.itextpdf.layout.element.Paragraph(
                "Fiche générée le " + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
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
    // TABLE
    // ═══════════════════════════════════════════════════════════════

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_offres"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom_offre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree_offre"));

        prixColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) { setText(null); return; }
                setText(String.format("%.2f TND", prix));
                setStyle("-fx-text-fill:#27AE60; -fx-font-weight:bold;");
            }
        });

        dureeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d + " mois");
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn   = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox   hbox      = new HBox(8, editBtn, deleteBtn);
            {
                hbox.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color:#3498DB;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;");
                editBtn.setOnAction(e   -> handleEditOffre(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteOffre(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        offresTable.setRowFactory(tv -> {
            TableRow<offres> row = new TableRow<>();
            row.selectedProperty().addListener((obs, was, is) ->
                    row.setStyle(is ? "-fx-background-color:#e8f5e9;" : ""));
            return row;
        });

        offresTable.setStyle("-fx-background-color:transparent;");
    }

    public void loadOffres() {
        try {
            List<offres> list = offresService.recuperer();
            allOffresList = FXCollections.observableArrayList(list);
            offresList    = FXCollections.observableArrayList(list);
            offresTable.setItems(offresList);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les offres");
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void applyFilters() {
        if (allOffresList == null) return;
        String s = searchField.getText().toLowerCase();
        ObservableList<offres> filtered = FXCollections.observableArrayList();
        for (offres o : allOffresList)
            if (s.isEmpty()
                    || (o.getNom_offre()    != null && o.getNom_offre().toLowerCase().contains(s))
                    || (o.getDescription()  != null && o.getDescription().toLowerCase().contains(s)))
                filtered.add(o);
        offresTable.setItems(filtered);
    }

    private void updateStatistics() {
        if (allOffresList == null || allOffresList.isEmpty()) {
            safeLabel(totalOffresLabel, "0"); safeLabel(maxPrixLabel, "0"); safeLabel(avgPrixLabel, "0"); return;
        }
        safeLabel(totalOffresLabel, String.valueOf(allOffresList.size()));
        double max = allOffresList.stream().mapToDouble(offres::getPrix).max().orElse(0);
        double avg = allOffresList.stream().mapToDouble(offres::getPrix).average().orElse(0);
        safeLabel(maxPrixLabel, String.format("%.2f TND", max));
        safeLabel(avgPrixLabel, String.format("%.2f TND", avg));
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    @FXML
    private void handleAddOffre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/AjoutOffre.fxml"));
            Parent root = loader.load();
            AjoutOffre controller = loader.getController();
            controller.setGestionOffresController(this);
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Offre");
            stage.setScene(new Scene(root, 500, 550));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) { showError("Erreur", "Impossible d'ouvrir le formulaire"); }
    }

    private void handleEditOffre(offres o) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierOffre.fxml"));
            Parent root = loader.load();
            ModifierOffre controller = loader.getController();
            controller.setGestionOffresController(this);
            controller.setOffre(o);
            Stage stage = new Stage();
            stage.setTitle("Modifier l'Offre");
            stage.setScene(new Scene(root, 500, 550));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) { showError("Erreur", "Impossible d'ouvrir le formulaire"); }
    }

    private void handleDeleteOffre(offres o) {
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer \"" + o.getNom_offre() + "\" ?")
                .showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    try {
                        offresService.supprimer(o.getId_offres());
                        loadOffres(); updateStatistics();
                        showSuccess("Succès", "Offre supprimée");
                    } catch (SQLException e) { showError("Erreur", "Impossible de supprimer"); }
                });
    }

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════

    @FXML private void handleDashboard(MouseEvent e)  { navigateTo(e, "/UsersInterface/Acceuil.fxml",              "Accueil");    }
    @FXML private void handlePersonnes(MouseEvent e)  { navigateTo(e, "/UsersInterface/DahboardPersonne.fxml",     "Personnes");  }
    @FXML private void handleTaches(MouseEvent e)     { navigateTo(e, "/UsersInterface/GestionTache.fxml",         "Tâches");     }
    @FXML private void handleAbonnements(MouseEvent e){ navigateTo(e, "/UsersInterface/GestionAbonnements.fxml",   "Abonnements");}
    @FXML private void handleAnimals(MouseEvent e)    { navigateTo(e, "/AnimalsInterface/AfficherAnimaux.fxml",    "Animaux");    }
    @FXML private void handleStocks(MouseEvent e)     { navigateTo(e, "/StocksInterface/afficherarticle.fxml",     "Stocks");     }
    @FXML private void handleTerrains(MouseEvent e)   { navigateTo(e, "/TerrainsInterface/acceuilterrain.fxml",    "Terrains");   }
    @FXML private void handleEvents(MouseEvent e)     { navigateTo(e, "/G-Evenements/Accueil.fxml",                "Événements"); }
    @FXML private void handleMateriels(MouseEvent e)  { navigateTo(e, "/MaterielsInterface/AccueilMateriel.fxml",  "Matériels");  }

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Transmettre currentUser si le contrôleur le supporte
            Object controller = loader.getController();
            if (controller instanceof GestionOffres dp) {
                dp.setCurrentUser(this.currentUser);
            }
            // Ajoutez d'autres types si nécessaire

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation : " + fxmlPath);
        }
    }

    @FXML
    private void handleLogout() {
        new Alert(Alert.AlertType.CONFIRMATION, "Se déconnecter ?").showAndWait()
                .filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) logoutBtn.getScene().getWindow();
                        // On récupère le Stage et la Scene ACTUELLE
                        Scene scene = stage.getScene();

                        // SOLUTION MIRACLE : On change la racine, pas la scène !
                        scene.setRoot(root);

                        // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                        stage.show();                    } catch (IOException e) { showError("Erreur", "Impossible de se déconnecter"); }
                });
    }

    @FXML
    private void openDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/StatsDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage(); stage.setTitle("Statistiques AgroFlow"); stage.setScene(new Scene(root)); stage.show();
    }

    private void showPdfSuccess(File f) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PDF généré"); alert.setHeaderText("✅ PDF créé avec succès"); alert.setContentText(f.getAbsolutePath());
        ButtonType open = new ButtonType("📂 Ouvrir", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(open, close);
        alert.showAndWait().ifPresent(btn -> { if (btn == open) try { Desktop.getDesktop().open(f); } catch (Exception e) {} });
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
    private void showGestionSubmenu() { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true);  }
    private void hideGestionSubmenu() { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
    private void safeLabel(Label l, String v) { if (l != null) l.setText(v); }
    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR);       a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void showSuccess(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}