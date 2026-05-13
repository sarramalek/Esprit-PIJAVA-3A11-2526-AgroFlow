package controllers.Stocks;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Stocks.Categorie;
import models.User.Personne;
import services.Stocks.CategorieService;
import services.User.PersonneService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class ajoutercategorieController {

    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private Label lblTitre;
    @FXML private Label msgNom;
    @FXML private Label msgDescription;
    @FXML private ComboBox<Personne> cbAgriculteur;
    @FXML private Label lblAgriculteur;

    @FXML private Button logoutBtn;
    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;

    private final CategorieService catService = new CategorieService();
    private boolean isModification = false;
    private int idCategorieActuel = 0;

    @FXML
    public void initialize() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
        }

        Personne currentUser = SessionManager.getCurrentUser();
        // Role 3 = ADMIN
        if (currentUser != null && currentUser.getRole() == 3) {
            if (lblAgriculteur != null) { lblAgriculteur.setVisible(true); lblAgriculteur.setManaged(true); }
            if (cbAgriculteur != null) {
                cbAgriculteur.setVisible(true);
                cbAgriculteur.setManaged(true);
                chargerAgriculteurs();
            }
        }

        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Nom requis (min 3 car.)", true);
            afficherFeedback(msgDescription, "⚠️ Description requise (min 5 car.)", true);
        }
        ajouterEcouteurs();
    }

    private void chargerAgriculteurs() {
        try {
            PersonneService ps = new PersonneService();
            // Role 2 = Agriculteur
            cbAgriculteur.setItems(FXCollections.observableArrayList(
                ps.recuperer().stream().filter(p -> p.getRole() == 2).toList()
            ));
            
            cbAgriculteur.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Personne p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty ? null : p.getNom() + " " + p.getPrenom());
                }
            });
            cbAgriculteur.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Personne p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty ? null : p.getNom() + " " + p.getPrenom());
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        if (label == null) return;
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        tfNom.textProperty().addListener((obs, old, nv) -> {
            if (nv.trim().length() < 3) afficherFeedback(msgNom, "⚠️ Trop court (min 3 car.)", true);
            else afficherFeedback(msgNom, "✅ Valide", false);
        });
        taDescription.textProperty().addListener((obs, old, nv) -> {
            if (nv.trim().length() < 5) afficherFeedback(msgDescription, "⚠️ Trop courte (min 5 car.)", true);
            else afficherFeedback(msgDescription, "✅ Valide", false);
        });
    }

    public void preparerModification(Categorie c) {
        isModification = true;
        idCategorieActuel = c.getId();
        if (lblTitre != null) lblTitre.setText("Modifier la Catégorie");
        tfNom.setText(c.getNom());
        taDescription.setText(c.getDescription());

        if (cbAgriculteur != null && cbAgriculteur.isVisible()) {
            for (Personne p : cbAgriculteur.getItems()) {
                if (p.getCin() == c.getIdUser()) { cbAgriculteur.setValue(p); break; }
            }
        }
    }

    @FXML
    void validerAjout(ActionEvent event) {
        String nom = tfNom.getText().trim();
        String desc = taDescription.getText().trim();
        Personne user = SessionManager.getCurrentUser();

        if (nom.length() < 3 || desc.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Format invalide", "Veuillez respecter les contraintes.");
            return;
        }

        int ownerId = (user != null && user.getRole() == 3 && cbAgriculteur.getValue() != null) ? cbAgriculteur.getValue().getCin() : (user != null ? user.getCin() : 0);

        try {
            if (!isModification && catService.existeDeja(nom)) {
                afficherFeedback(msgNom, "❌ Ce nom existe déjà !", true);
                return;
            }

            Categorie c = new Categorie();
            c.setId(isModification ? idCategorieActuel : 0);
            c.setNom(nom);
            c.setDescription(desc);
            c.setIdUser(ownerId);
            if (user != null && user.getRole() == 3) c.setIdAdmin(user.getCin());

            if (isModification) catService.modifier(c);
            else catService.ajouter(c);

            retourListe(event);
        } catch (SQLException | IOException e) { e.printStackTrace(); }
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Personne user = SessionManager.getCurrentUser();
        String fxml = (user != null && user.getRole() == 2) ? "/StocksInterface/AfficherCategorieAgr.fxml" : "/StocksInterface/affichercategorie.fxml";
        navigateTo(event, fxml);
    }

    // --- Navigation Sidebar ---
    @FXML public void handleDashboard(MouseEvent event) { navigateTo(event, "/UsersInterface/Acceuil.fxml"); }
    @FXML public void handleStocks(MouseEvent event) { 
        Personne user = SessionManager.getCurrentUser();
        String fxml = (user != null && user.getRole() == 2) ? "/StocksInterface/AfficherArticleAgr.fxml" : "/StocksInterface/afficherarticle.fxml";
        navigateTo(event, fxml); 
    }
    @FXML public void handleLogout(MouseEvent event) {
        SessionManager.clearSession();
        navigateTo(event, "/UsersInterface/login.fxml");
    }

    private void navigateTo(Event event, String fxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showGestionSubmenu() { if (gestionSubmenu != null) { gestionSubmenu.setVisible(true); gestionSubmenu.setManaged(true); } }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}