package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.User.Employe;
import models.User.Personne;
import models.User.Tache;
import services.User.OuvrierService;
import utils.MyDatabase;
import utils.SessionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestionOuvriersController implements Initializable {
    @FXML private Button logoutBtn;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;
    private Personne currentUser;
    // ── Onglet Ouvriers ──────────────────────────────────────────────────

    @FXML private TableView<Employe>            ouvrierTable;
    @FXML private TableColumn<Employe, Integer> colCin;
    @FXML private TableColumn<Employe, String>  colNom;
    @FXML private TableColumn<Employe, String>  colPrenom;
    @FXML private TableColumn<Employe, String>  colEmail;
    @FXML private TableColumn<Employe, String>  colTel;
    @FXML private TableColumn<Employe, Integer> colTerrain;

    @FXML private TextField        tfCin;
    @FXML private TextField        tfNom;
    @FXML private TextField        tfPrenom;
    @FXML private TextField        tfTel;
    @FXML private TextField        tfEmail;
    @FXML private TextField        tfAdresse;
    @FXML private TextField        tfVille;
    @FXML private DatePicker       dpDateNaiss;
    @FXML private ComboBox<String> cbTerrain;
    @FXML private Label            lblOuvrierMsg;

    // ── Onglet Tâches ────────────────────────────────────────────────────

    @FXML private TableView<Tache>            tacheTable;
    @FXML private TableColumn<Tache, Integer> colTacheId;
    @FXML private TableColumn<Tache, String>  colNomTache;
    @FXML private TableColumn<Tache, String>  colEtat;
    @FXML private TableColumn<Tache, String>  colPriorite;
    @FXML private TableColumn<Tache, String>  colEcheance;
    @FXML private TableColumn<Tache, Integer> colAssignee;

    @FXML private TextField        tfNomTache;
    @FXML private TextArea         taDescription;
    @FXML private ComboBox<String> cbEtat;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private DatePicker       dpEcheance;
    @FXML private ComboBox<String> cbOuvrier;
    @FXML private Label            lblTacheMsg;

    // ─────────────────────────────────────────────────────────────────────

    private final OuvrierService ouvrierService = new OuvrierService();

    private final ObservableList<Employe> ouvrierList = FXCollections.observableArrayList();
    private final ObservableList<Tache>   tacheList   = FXCollections.observableArrayList();

    private int cinAgriculteur;

    // ═══════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Personne agriculteur = SessionManager.getCurrentUser();
        if (agriculteur == null) {
            showAlert(Alert.AlertType.ERROR, "Session expirée", "Veuillez vous reconnecter.");
            return;
        }
        cinAgriculteur = agriculteur.getCin();

        setupOuvrierTable();
        setupTacheTable();
        setupComboBoxes();
        loadOuvriers();
        loadTaches();
        loadTerrains();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SETUP
    // ═══════════════════════════════════════════════════════════════════

    private void setupOuvrierTable() {
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colTerrain.setCellValueFactory(new PropertyValueFactory<>("idTerrain"));
        ouvrierTable.setItems(ouvrierList);

        // Clic sur un ouvrier → pré-sélectionner dans combo tâche
        ouvrierTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> {
                    if (sel != null)
                        cbOuvrier.setValue(sel.getCin() + " - " + sel.getNom()
                                + " " + sel.getPrenom());
                });
    }

    private void setupTacheTable() {
        // Colonnes mappées sur les getters de Tache (noms réels DB)
        colTacheId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomTache.setCellValueFactory(new PropertyValueFactory<>("nomTache"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colEcheance.setCellValueFactory(new PropertyValueFactory<>("dateEcheance"));
        colAssignee.setCellValueFactory(new PropertyValueFactory<>("assignee"));
        tacheTable.setItems(tacheList);
    }

    private void setupComboBoxes() {
        cbEtat.setItems(FXCollections.observableArrayList(
                "EN_ATTENTE", "EN_COURS", "TERMINEE"));
        cbEtat.setValue("EN_ATTENTE");

        cbPriorite.setItems(FXCollections.observableArrayList(
                "BASSE", "NORMALE", "HAUTE"));
        cbPriorite.setValue("NORMALE");
    }

    // ═══════════════════════════════════════════════════════════════════
    // CHARGEMENT
    // ═══════════════════════════════════════════════════════════════════

    private void loadOuvriers() {
        try {
            ouvrierList.setAll(ouvrierService.getOuvriersParAgriculteur(cinAgriculteur));
            refreshOuvrierCombo();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement ouvriers: " + e.getMessage());
        }
    }

    private void loadTaches() {
        try {
            tacheList.setAll(ouvrierService.getTachesParAgriculteur(cinAgriculteur));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement tâches: " + e.getMessage());
        }
    }

    private void loadTerrains() {
        try (PreparedStatement pst = MyDatabase.getInstance().getConnection()
                .prepareStatement("SELECT id_terrain, nom_terrain FROM terrain WHERE cin = ?")) {
            pst.setInt(1, cinAgriculteur);
            try (ResultSet rs = pst.executeQuery()) {
                ObservableList<String> items = FXCollections.observableArrayList();
                while (rs.next())
                    items.add(rs.getInt("id_terrain") + " - " + rs.getString("nom_terrain"));
                cbTerrain.setItems(items);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chargement terrains: " + e.getMessage());
        }
    }

    private void refreshOuvrierCombo() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Employe o : ouvrierList)
            items.add(o.getCin() + " - " + o.getNom() + " " + o.getPrenom());
        cbOuvrier.setItems(items);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTIONS OUVRIERS
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAjouterOuvrier() {
        if (tfCin.getText().isBlank() || tfNom.getText().isBlank()
                || tfEmail.getText().isBlank() || tfTel.getText().isBlank()
                || cbTerrain.getValue() == null) {
            setMsg(lblOuvrierMsg, "❌ Veuillez remplir tous les champs obligatoires (*)", "red");
            return;
        }

        try {
            int idTerrain = Integer.parseInt(cbTerrain.getValue().split(" - ")[0]);

            Employe o = new Employe();
            o.setCin(Integer.parseInt(tfCin.getText().trim()));
            o.setNom(tfNom.getText().trim());
            o.setPrenom(tfPrenom.getText().trim());
            o.setTel(tfTel.getText().trim());
            o.setEmail(tfEmail.getText().trim());
            o.setAdresse(tfAdresse.getText().trim());
            o.setVille(tfVille.getText().trim());
            o.setDate_naiss(dpDateNaiss.getValue() != null
                    ? dpDateNaiss.getValue().toString() : "");
            o.setIdTerrain(idTerrain);
            o.setCinAgriculteur(cinAgriculteur);

            ouvrierService.ajouterOuvrier(o, cinAgriculteur);

            setMsg(lblOuvrierMsg,
                    "✅ Ouvrier ajouté ! Email envoyé à " + o.getEmail(), "green");
            clearOuvrierForm();
            loadOuvriers();

        } catch (NumberFormatException e) {
            setMsg(lblOuvrierMsg, "❌ CIN invalide (doit être un nombre entier)", "red");
        } catch (Exception e) {
            setMsg(lblOuvrierMsg, "❌ " + e.getMessage(), "red");
        }
    }

    @FXML
    private void handleSupprimerOuvrier() {
        Employe sel = ouvrierTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise",
                    "Veuillez sélectionner un ouvrier à supprimer.");
            return;
        }
        Optional<ButtonType> r = showConfirm("Confirmer suppression",
                "Supprimer " + sel.getNom() + " " + sel.getPrenom()
                        + " ?\nSes tâches seront aussi supprimées.");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                ouvrierService.supprimerOuvrier(sel.getCin());
                loadOuvriers();
                loadTaches();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTIONS TÂCHES
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAssignerTache() {
        if (tfNomTache.getText().isBlank() || cbOuvrier.getValue() == null) {
            setMsg(lblTacheMsg, "❌ Nom de la tâche et ouvrier sont obligatoires.", "red");
            return;
        }
        try {
            int cinOuvrier = Integer.parseInt(cbOuvrier.getValue().split(" - ")[0]);

            Tache t = new Tache();
            t.setNomTache(tfNomTache.getText().trim());
            t.setDescription(taDescription.getText().trim());
            t.setEtat(cbEtat.getValue());
            t.setPriorite(cbPriorite.getValue());
            t.setDateEcheance(dpEcheance.getValue() != null
                    ? dpEcheance.getValue() : LocalDate.now().plusDays(7));
            t.setAssignee(cinOuvrier);
            t.setCinAgriculteur(cinAgriculteur);

            ouvrierService.assignerTache(t);

            setMsg(lblTacheMsg, "✅ Tâche assignée avec succès !", "green");
            clearTacheForm();
            loadTaches();

        } catch (Exception e) {
            setMsg(lblTacheMsg, "❌ " + e.getMessage(), "red");
        }
    }

    @FXML
    private void handleSupprimerTache() {
        Tache sel = tacheTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise",
                    "Veuillez sélectionner une tâche.");
            return;
        }
        Optional<ButtonType> r = showConfirm("Confirmer suppression",
                "Supprimer la tâche \"" + sel.getNomTache() + "\" ?");
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                ouvrierService.supprimerTache(sel.getId());
                loadTaches();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleChangerEtat() {
        Tache sel = tacheTable.getSelectionModel().getSelectedItem();
        if (sel == null || cbEtat.getValue() == null) {
            setMsg(lblTacheMsg, "❌ Sélectionnez une tâche et un état.", "red");
            return;
        }
        try {
            ouvrierService.modifierEtatTache(sel.getId(), cbEtat.getValue());
            setMsg(lblTacheMsg, "✅ État mis à jour : " + cbEtat.getValue(), "green");
            loadTaches();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════

    private void clearOuvrierForm() {
        tfCin.clear(); tfNom.clear(); tfPrenom.clear();
        tfTel.clear(); tfEmail.clear();
        tfAdresse.clear(); tfVille.clear();
        dpDateNaiss.setValue(null);
        cbTerrain.setValue(null);
        lblOuvrierMsg.setText("");
    }

    private void clearTacheForm() {
        tfNomTache.clear(); taDescription.clear();
        cbEtat.setValue("EN_ATTENTE"); cbPriorite.setValue("NORMALE");
        dpEcheance.setValue(null); cbOuvrier.setValue(null);
        lblTacheMsg.setText("");
    }

    private void setMsg(Label lbl, String msg, String color) {
        lbl.setText(msg);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private Optional<ButtonType> showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        return a.showAndWait();
    }
// navigation agricole
    // ── Navigation methods required by GestionOuvrier.fxml ──────────────────────

    @FXML
    private void handleAPropos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setTitle("Mon Profil");
            s.setScene(new Scene(root));
            s.setResizable(true);
            s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen();
            s.showAndWait();
        } catch (IOException e) { showError("Erreur: " + e.getMessage()); }
    }

    @FXML
    private void handleDashboard() { /* Déjà sur cette page */ }

    @FXML
    private void handleMesTerrains(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/acceuilagricoleterrain.fxml", "Mes Terrains");
    }

    @FXML
    private void ouvrirTerrains(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    private void ouvrirPlantes(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    private void ouvrirRotations(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
    }

    @FXML
    private void handleMesAnimaux(MouseEvent event) {
        navigateTo(event, "/AnimalsInterface/acceuilagricoleanimaux.fxml", "Animaux");
    }

    @FXML
    private void handleMesArticles(MouseEvent event) {
        navigateTo(event, "/StocksInterface/AfficherArticleAgr.fxml", "Articles");
    }

    @FXML
    private void ouvrirMachine(MouseEvent event) {
        navigateTo(event, "/MaterielsInterface/AgricoleAffichageMachine.fxml", "Machines");
    }

    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        navigateTo(event, "/UsersInterface/MesAbonnements.fxml", "Mes Abonnements");
    }

    @FXML
    private void handleMesEvenements(MouseEvent event) {
        navigateTo(event, "/G-Evenements/AfficherEvenementsUser.fxml", "Événements");
    }

    @FXML
    private void handleLogout() {
        new Alert(Alert.AlertType.CONFIRMATION, "Se déconnecter ?").showAndWait()
                .filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) logoutBtn.getScene().getWindow();
                        stage.getScene().setRoot(root);
                        stage.show();
                    } catch (IOException e) { e.printStackTrace(); }
                });
    }

// ── Helper ───────────────────────────────────────────────────────────────────

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation : " + fxmlPath);
            e.printStackTrace();
        }
    }

    private static void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
