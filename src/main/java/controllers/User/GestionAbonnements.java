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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Abonnements;
import models.User.Personne;
import services.User.AbonnementService;
import services.User.PdfReportService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestionAbonnements {

    @FXML private VBox gestionSubmenu, operationsSubmenu, gestionContainer;
    @FXML private Button gestionToggle, operationsToggle, gestionBtn;
    @FXML private Button dashboardBtn, personnesBtn, tachesBtn, offresBtn;
    @FXML private Button abonnementsBtn, logoutBtn, addAbonnementBtn;
    @FXML private Label  userNameLabel;
    @FXML private Label  totalAbonnementsLabel, actifsLabel, expiresLabel, enAttenteLabel;
    @FXML private TextField searchField;

    // PDF
    @FXML private Button  pdfBtn;
    @FXML private Label   selectedAbonnementLabel;

    // Table
    @FXML private TableView<Abonnements>             abonnementsTable;
    @FXML private TableColumn<Abonnements, Integer>  idColumn;
    @FXML private TableColumn<Abonnements, Integer>  cinColumn;
    @FXML private TableColumn<Abonnements, Integer>  offreColumn;
    @FXML private TableColumn<Abonnements, String>   dateInscriptionColumn;
    @FXML private TableColumn<Abonnements, String>   dateExpirationColumn;
    @FXML private TableColumn<Abonnements, String>   situationColumn;
    @FXML private TableColumn<Abonnements, Void>     actionsColumn;

    private AbonnementService abonnementService;
    private PdfReportService  pdfReportService;
    private ObservableList<Abonnements> abonnementsList;
    private ObservableList<Abonnements> allAbonnementsList;
    private Abonnements selectedAbonnement;
    private static Personne currentUser;

    // ═══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        try {
            abonnementService = new AbonnementService();
            pdfReportService  = new PdfReportService();
            setupTable();
            loadAbonnements();
            setupSearch();
            updateStatistics();
            setupPdfButton();
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger les abonnements");
            e.printStackTrace();
        }
    }

    public void setCurrentUser(Personne user) {
        currentUser = user;
        if (user != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
    }

    // ═══════════════════════════════════════════════════════════════
    // PDF
    // ═══════════════════════════════════════════════════════════════

    private void setupPdfButton() {
        if (pdfBtn != null) pdfBtn.setDisable(true);
        safeLabel(selectedAbonnementLabel, "Sélectionnez un abonnement pour générer son PDF");

        abonnementsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    selectedAbonnement = newVal;
                    if (newVal != null) {
                        if (pdfBtn != null) pdfBtn.setDisable(false);
                        safeLabel(selectedAbonnementLabel,
                                "Sélectionné : Abonnement #" + newVal.getId_abonn()
                                        + "  |  CIN : " + newVal.getCin()
                                        + "  |  " + nvl(newVal.getSituation()));
                    } else {
                        if (pdfBtn != null) pdfBtn.setDisable(true);
                        safeLabel(selectedAbonnementLabel, "Sélectionnez un abonnement pour générer son PDF");
                    }
                }
        );
    }

    @FXML
    private void handleGeneratePdf() {
        if (selectedAbonnement == null) {
            showError("Aucune sélection", "Cliquez d'abord sur une ligne du tableau.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialFileName("abonnement_" + selectedAbonnement.getId_abonn() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File bureau = new File(System.getProperty("user.home") + "/Desktop");
        if (bureau.exists()) fc.setInitialDirectory(bureau);

        File fichier = fc.showSaveDialog((Stage) abonnementsTable.getScene().getWindow());
        if (fichier == null) return;

        try {
            pdfReportService.generateAbonnementPdf(selectedAbonnement.getId_abonn(), fichier.getAbsolutePath());
            showPdfSuccess(fichier);
        } catch (Exception e) {
            showError("Erreur PDF", "Génération échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TABLE
    // ═══════════════════════════════════════════════════════════════

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_abonn"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        offreColumn.setCellValueFactory(new PropertyValueFactory<>("id_offre"));
        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        dateExpirationColumn.setCellValueFactory(new PropertyValueFactory<>("date_expiration"));
        situationColumn.setCellValueFactory(new PropertyValueFactory<>("situation"));

        situationColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle("-fx-text-fill:" + switch (s.toLowerCase()) {
                    case ".actif" -> "#27AE60:green";
                    case ".expiré", ".expire" -> "#E74C3C:red";
                    case  "en.attente" -> "#F39C12:orange";
                    default -> "#95A5A6:blue";
                } + "; -fx-font-weight:bold;");
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
                editBtn.setOnAction(e   -> handleEditAbonnement(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteAbonnement(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        // Highlight sélection
        abonnementsTable.setRowFactory(tv -> {
            TableRow<Abonnements> row = new TableRow<>();
            row.selectedProperty().addListener((obs, was, is) ->
                    row.setStyle(is ? "-fx-background-color:#e8f5e9;" : ""));
            return row;
        });

        abonnementsTable.setStyle("-fx-background-color:transparent;");
    }

    public void loadAbonnements() {
        try {
            List<Abonnements> list = abonnementService.recuperer();
            allAbonnementsList = FXCollections.observableArrayList(list);
            abonnementsList    = FXCollections.observableArrayList(list);
            abonnementsTable.setItems(abonnementsList);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les abonnements");
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void applyFilters() {
        if (allAbonnementsList == null) return;
        String s = searchField.getText().toLowerCase();
        ObservableList<Abonnements> filtered = FXCollections.observableArrayList();
        for (Abonnements a : allAbonnementsList) {
            if (s.isEmpty()
                    || String.valueOf(a.getCin()).contains(s)
                    || (a.getSituation() != null && a.getSituation().toLowerCase().contains(s)))
                filtered.add(a);
        }
        abonnementsTable.setItems(filtered);
    }

    private void updateStatistics() {
        if (allAbonnementsList == null || allAbonnementsList.isEmpty()) {
            safeLabel(totalAbonnementsLabel, "0");
            safeLabel(actifsLabel, "0");
            safeLabel(expiresLabel, "0");
            safeLabel(enAttenteLabel, "0");
            return;
        }
        safeLabel(totalAbonnementsLabel, String.valueOf(allAbonnementsList.size()));
        safeLabel(actifsLabel,    count("actif"));
        safeLabel(expiresLabel,   count("expire"));
        safeLabel(enAttenteLabel, count("en attente"));
    }

    private String count(String situation) {
        return String.valueOf(allAbonnementsList.stream()
                .filter(a -> situation.equalsIgnoreCase(a.getSituation())
                        || (situation.equals("expire") && "expiré".equalsIgnoreCase(a.getSituation())))
                .count());
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════════

    @FXML
    private void handleAddAbonnement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/AjoutAbonnement.fxml"));
            Parent root = loader.load();
            AjoutAbonnements controller = loader.getController();
            controller.setGestionAbonnementsController(this);
            Stage stage = new Stage();
            stage.setTitle("Nouvel Abonnement");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void handleEditAbonnement(Abonnements a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierAbonnement.fxml"));
            Parent root = loader.load();
            ModifierAbonnement controller = loader.getController();
            controller.setGestionAbonnementsController(this);
            controller.setAbonnement(a);
            Stage stage = new Stage();
            stage.setTitle("Modifier l'Abonnement");
            stage.setScene(new Scene(root, 600, 700));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void handleDeleteAbonnement(Abonnements a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Supprimer l'abonnement #" + a.getId_abonn() + " ?");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                abonnementService.supprimer(a.getId_abonn());
                loadAbonnements();
                updateStatistics();
                showSuccess("Succès", "Abonnement supprimé");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════

    @FXML private void handleDashboard(MouseEvent e)   { navigateTo(e, "/UsersInterface/Acceuil.fxml",              "Accueil");       }
    @FXML private void handlePersonnes(MouseEvent e)   { navigateTo(e, "/UsersInterface/DahboardPersonne.fxml",     "Personnes");     }
    @FXML private void handleTaches(MouseEvent e)      { navigateTo(e, "/UsersInterface/GestionTache.fxml",         "Tâches");        }
    @FXML private void handleOffres(MouseEvent e)      { navigateTo(e, "/UsersInterface/GestionOffre.fxml",         "Offres");        }
    @FXML private void handleAnimals(MouseEvent e)     { navigateTo(e, "/AnimalsInterface/AfficherAnimaux.fxml",    "Animaux");       }
    @FXML private void handleStocks(MouseEvent e)      { navigateTo(e, "/StocksInterface/afficherarticle.fxml",     "Stocks");        }
    @FXML private void handleTerrains(MouseEvent e)    { navigateTo(e, "/TerrainsInterface/acceuilterrain.fxml",    "Terrains");      }
    @FXML private void handleEvents(MouseEvent e)      { navigateTo(e, "/G-Evenements/Accueil.fxml",                "Événements");    }
    @FXML private void handleMateriels(MouseEvent e)   { navigateTo(e, "/MaterielsInterface/AccueilMateriel.fxml",  "Matériels");     }

    private void navigateTo(MouseEvent event, String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean max = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(max);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        new Alert(Alert.AlertType.CONFIRMATION, "Se déconnecter ?").showAndWait()
                .filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) logoutBtn.getScene().getWindow();
                        stage.setScene(new Scene(root, 1500, 700));
                        stage.setTitle("AgroFlow - Connexion");
                        stage.setMaximized(true);
                    } catch (IOException e) { showError("Erreur", "Impossible de se déconnecter"); }
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

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private void showPdfSuccess(File f) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PDF généré");
        alert.setHeaderText("✅ PDF créé avec succès");
        alert.setContentText(f.getAbsolutePath());
        ButtonType open  = new ButtonType("📂 Ouvrir", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Fermer",    ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(open, close);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == open) try { Desktop.getDesktop().open(f); }
            catch (Exception e) { showError("Erreur", "Impossible d'ouvrir le fichier"); }
        });
    }

    private void showGestionSubmenu() { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true);  }
    private void hideGestionSubmenu() { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
    private void safeLabel(Label l, String v) { if (l != null) l.setText(v); }
    private String nvl(String s) { return s != null ? s : "—"; }
    private void showError(String t, String m)   { alert(Alert.AlertType.ERROR,       t, m); }
    private void showSuccess(String t, String m) { alert(Alert.AlertType.INFORMATION, t, m); }
    static void showInfo(String t, String m)     { alert(Alert.AlertType.INFORMATION, t, m); }
    private static void alert(Alert.AlertType type, String t, String m) {
        Alert a = new Alert(type); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}