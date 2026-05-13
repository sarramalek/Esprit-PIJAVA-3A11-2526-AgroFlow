package controllers.Stocks;

import controllers.User.ProfilEmploye;
import javafx.event.ActionEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Stocks.Article;
import models.Stocks.Categorie;
import models.User.Personne;
import services.Stocks.ArticleService;
import services.Stocks.CategorieService;
import services.User.OuvrierService;
import utils.SessionManager;

import java.io.IOException;
import javafx.stage.Modality;
import javafx.event.Event;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherStockOuvrierController {

    @FXML private Label userNameLabel;
    @FXML private TextField tfRecherche;
    @FXML private FlowPane flowPaneArticles;

    private final ArticleService articleService = new ArticleService();
    private final CategorieService categorieService = new CategorieService();
    private final OuvrierService ouvrierService = new OuvrierService();

    private List<Article> allArticles;
    private int agriculteurCin = -1;

    @FXML
    public void initialize() {
        Personne user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            try {
                agriculteurCin = ouvrierService.getAgriculteurCinForOuvrier(user.getCin());
                chargerDonnees();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        tfRecherche.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    private void chargerDonnees() throws SQLException {
        if (agriculteurCin == -1) return;

        allArticles = articleService.recupererParUser(agriculteurCin);
        afficherArticles(allArticles);
    }

    private void afficherArticles(List<Article> articles) {
        flowPaneArticles.getChildren().clear();
        for (Article art : articles) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/ArticleCard.fxml"));
                VBox card = loader.load();
                ArticleCardController controller = loader.getController();
                controller.setData(art, this);
                flowPaneArticles.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Categorie categorieFiltre = null;

    public void filtrerParCategorie(Categorie c) {
        this.categorieFiltre = c;
        appliquerFiltres();
    }

    @FXML
    public void reinitialiserFiltre() {
        this.categorieFiltre = null;
        tfRecherche.clear();
        appliquerFiltres();
    }

    @FXML
    private void appliquerFiltres() {
        String search = tfRecherche.getText().toLowerCase().trim();

        List<Article> filtered = allArticles.stream()
                .filter(a -> a.getNom().toLowerCase().contains(search))
                .filter(a -> categorieFiltre == null || a.getIdCategorie() == categorieFiltre.getId())
                .collect(Collectors.toList());

        afficherArticles(filtered);
    }

    // --- Actions ---

    @FXML
    public void handleDashboard(MouseEvent event) { 
        changerScene("/UsersInterface/AcceuilEmp.fxml", event); 
    }
    
    @FXML
    public void handleStocks(MouseEvent event) { 
        changerScene("/StocksInterface/AfficherStockOuvrier.fxml", event);
    }

    @FXML
    public void handleLogout(MouseEvent event) {
        SessionManager.clearSession();
        changerScene("/UsersInterface/login.fxml", event);
    }

    @FXML
    public void handleCategories(MouseEvent event) {
        changerScene("/StocksInterface/affichercategorie.fxml", event);
    }

    @FXML
    public void handleMesTaches(MouseEvent event) {
        changerScene("/UsersInterface/MesTaches.fxml", event);
    }

    @FXML
    public void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye controller = loader.getController();
            Personne user = SessionManager.getCurrentUser();
            if (controller != null && user != null) {
                controller.setCurrentUser(user);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void ouvrirRotations(MouseEvent event) {
        changerScene("/TerrainsInterface/EmployeRotation.fxml", event);
    }

    @FXML
    public void handleMateriel(MouseEvent event) {
        changerScene("/MaterielsInterface/MaintenanceFront.fxml", event);
    }

    @FXML
    public void handleEvenements(MouseEvent event) {
        changerScene("/G-Evenements/AfficherEvenementsEmp.fxml", event);
    }

    private void changerScene(String fxml, Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur chargement FXML: " + fxml);
            e.printStackTrace();
        }
    }

    public void rafraichir() {
        try {
            chargerDonnees();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
