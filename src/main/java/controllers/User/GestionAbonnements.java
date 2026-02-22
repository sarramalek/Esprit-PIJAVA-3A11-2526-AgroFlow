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
import services.User.SmsService;
import utils.SessionManager;

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
    @FXML private Label userRoleLabel;

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
        // ✅ CORRECTION PRINCIPALE : récupérer le user depuis SessionManager dès initialize()
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }

        // Mise à jour des labels
        updateUserLabels();

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
            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();

            System.out.println("✓ Modal profil fermée");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
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

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Transmettre currentUser si le contrôleur le supporte
            Object controller = loader.getController();
            if (controller instanceof GestionAbonnements dp) {
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
    //--------2 F-A --------------------------------------------------
    @FXML
    private void handleNavigateToSettings2FA(MouseEvent event) {
        try {
            // ✅ Utiliser this.currentUser (déjà chargé) plutôt que SessionManager ici
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée.");
                return;
            }
            String telephone = currentUser.getTel();

            if (telephone == null || telephone.isEmpty()) {
                navigateToSettings2FA(event);
                return;
            }

            String otp = String.format("%06d", (int)(Math.random() * 999999));
            SessionManager.setTempOtp(otp);
            SmsService smsService = new SmsService();
            boolean sent = smsService.sendOtpCode(telephone, otp);

            if (!sent) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer le SMS.");
                return;
            }

            String masked = telephone.length() > 4
                    ? telephone.substring(0, telephone.length() - 4).replaceAll("\\d", "*")
                    + telephone.substring(telephone.length() - 4)
                    : telephone;

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Vérification SMS");
            dialog.setHeaderText("Code envoyé au : " + masked);

            ButtonType verifyBtn = new ButtonType("Vérifier", ButtonBar.ButtonData.OK_DONE);
            ButtonType resendBtn = new ButtonType("Renvoyer", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyBtn, resendBtn, ButtonType.CANCEL);

            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(200);
            codeField.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 24px;" +
                    "-fx-alignment: center; -fx-pref-height: 52px;" +
                    "-fx-border-color: #52B788; -fx-border-radius: 8;" +
                    "-fx-background-radius: 8; -fx-border-width: 2;");
            codeField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("\\d*")) codeField.setText(n.replaceAll("[^\\d]", ""));
                if (n.length() > 6)     codeField.setText(n.substring(0, 6));
            });

            VBox content = new VBox(14);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            content.getChildren().addAll(
                    new Label("📱 Entrez le code reçu par SMS :"),
                    codeField,
                    new Label("⏱  Valable 5 minutes"));
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
                    navigateToSettings2FA(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Code incorrect", "Code SMS invalide. Accès refusé.");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ═══════════════════════════════════════════════════════════════════
    // ALERTES
    // ═══════════════════════════════════════════════════════════════════


    private void showWarning(String title, String msg) { alert(Alert.AlertType.WARNING,     title, msg); }
    private void showSuccess(String msg)               { alert(Alert.AlertType.INFORMATION, "Succès", msg); }
    private void showAlertSimple(String msg)           { alert(Alert.AlertType.ERROR,       "Erreur", msg); }



    private void showAlert(Alert.AlertType type, String title, String content) {
        alert(type, title, content);
    }

    private void showProgress(String message) {
        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("En cours...");
        progress.setHeaderText(null);
        progress.setContentText(message);
        progress.show();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(progress::close);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void navigateToSettings2FA(MouseEvent event) {
        Settings2FA settings = new Settings2FA();
        settings.launch();
    }
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            SessionManager.setCurrentUser(null); // ✅ vider la session
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