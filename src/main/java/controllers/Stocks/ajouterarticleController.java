package controllers.Stocks;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Stocks.Article;
import models.Stocks.Categorie;
import models.User.Personne;
import models.User.Utilisateur;
import services.Stocks.ArticleService;
import services.Stocks.CategorieService;
import services.Stocks.CurrencyService;
import services.User.PersonneService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ajouterarticleController {

    @FXML private TextField tfNom, tfQuantite, tfSeuil, tfUnite, tfPrixUnitaire;
    @FXML private ComboBox<Categorie> cbCategories;
    @FXML private ComboBox<String> cbDevise;
    @FXML private Label lblTitre, msgNom, msgQuantite, msgSeuil, msgUnite, msgCategorie, msgPrixUnitaire;
    @FXML private ComboBox<Personne> cbAgriculteurs;
    @FXML private VBox boxAgriculteur;
    @FXML private Button logoutBtn;
    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;

    private final ArticleService articleService = new ArticleService();
    private final CategorieService catService = new CategorieService();
    private final PersonneService personneService = new PersonneService();
    private boolean isModification = false;
    private int idArticleActuel = 0;

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

        chargerCategories();
        if (cbDevise != null) {
            cbDevise.setItems(FXCollections.observableArrayList("Dinar Tunisien (TND)", "Euro (€)", "Dollar ($)"));
            cbDevise.getSelectionModel().selectFirst();
        }

        Personne user = SessionManager.getCurrentUser();
        boolean isAdmin = (user != null && user.getRole() == 3);
        if (boxAgriculteur != null) {
            boxAgriculteur.setVisible(isAdmin);
            boxAgriculteur.setManaged(isAdmin);
        }
        if (isAdmin) chargerAgriculteurs();

        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Nom requis (min 3 car.)", true);
            afficherFeedback(msgQuantite, "⚠️ Quantité requise", true);
        }
        ajouterEcouteurs();
    }

    private void chargerAgriculteurs() {
        try {
            List<Utilisateur> agris = personneService.getUtilisateurs().stream().filter(u -> u.getRole() == 2).toList();
            cbAgriculteurs.setItems(FXCollections.observableArrayList(agris.stream().map(u -> (Personne)u).toList()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerCategories() {
        try {
            cbCategories.setItems(FXCollections.observableArrayList(catService.recuperer()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        if (label == null) return;
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        tfNom.textProperty().addListener((obs, old, nv) -> {
            if (nv.trim().length() < 3) afficherFeedback(msgNom, "⚠️ Trop court", true);
            else afficherFeedback(msgNom, "✅ Valide", false);
        });
        // ... more listeners if needed, but let's keep it simple for stability
    }

    public void preparerModification(Article a) {
        isModification = true;
        idArticleActuel = a.getId();
        if (lblTitre != null) lblTitre.setText("Modifier l'Article");
        tfNom.setText(a.getNom());
        tfQuantite.setText(String.valueOf(a.getQuantiteEnStock()));
        tfSeuil.setText(String.valueOf(a.getSeuilAlerte()));
        tfUnite.setText(a.getUniteMesure());
        tfPrixUnitaire.setText(String.valueOf(a.getPrixUnitaire()));
    }

    @FXML
    void validerAjout(ActionEvent event) {
        // Simple validation
        if (tfNom.getText().trim().length() < 3) return;

        Personne user = SessionManager.getCurrentUser();
        int ownerId = (user != null && user.getRole() == 3 && cbAgriculteurs.getValue() != null) ? cbAgriculteurs.getValue().getCin() : (user != null ? user.getCin() : 0);

        try {
            Article a = new Article();
            a.setId(isModification ? idArticleActuel : 0);
            a.setNom(tfNom.getText().trim());
            a.setQuantiteEnStock(Double.parseDouble(tfQuantite.getText()));
            a.setSeuilAlerte(Double.parseDouble(tfSeuil.getText()));
            a.setUniteMesure(tfUnite.getText().trim());
            a.setPrixUnitaire(Double.parseDouble(tfPrixUnitaire.getText()));
            a.setIdCategorie(cbCategories.getValue() != null ? cbCategories.getValue().getId() : 0);
            a.setIdUser(ownerId);
            a.setDevise(cbDevise.getValue());

            if (isModification) articleService.modifier(a);
            else articleService.ajouter(a);

            retourListe(event);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Personne user = SessionManager.getCurrentUser();
        String fxml = (user != null && user.getRole() == 2) ? "/StocksInterface/AfficherArticleAgr.fxml" : "/StocksInterface/afficherarticle.fxml";
        navigateTo(event, fxml);
    }

    @FXML public void handleDashboard(MouseEvent event) { navigateTo(event, "/UsersInterface/Acceuil.fxml"); }
    @FXML public void handleStocks(MouseEvent event) { navigateTo(event, "/StocksInterface/afficherarticle.fxml"); }
    @FXML public void handleLogout(MouseEvent event) { navigateTo(event, "/UsersInterface/login.fxml"); }

    private void navigateTo(Event event, String fxml) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showGestionSubmenu() { if (gestionSubmenu != null) { gestionSubmenu.setVisible(true); gestionSubmenu.setManaged(true); } }
}