package controllers.Stocks;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Stocks.Categorie;
import models.User.Personne;
import services.Stocks.CategorieService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class affichercategorieController {

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar & Navigation
    // ══════════════════════════════════════════════════════
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Label avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Button logoutBtn;
    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;

    // ══════════════════════════════════════════════════════
    //  FXML — Tableau & Filtres
    // ══════════════════════════════════════════════════════
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, String> colNom;
    @FXML private TableColumn<Categorie, String> colDescription;
    @FXML private TableColumn<Categorie, String> colAgriculteur;
    @FXML private TableColumn<Categorie, Integer> colNbArticles;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cbFiltreAgriculteur;

    // ══════════════════════════════════════════════════════
    //  State & Services
    // ══════════════════════════════════════════════════════
    private final CategorieService catService = new CategorieService();
    private Personne currentUser;
    private ObservableList<Categorie> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        updateUserLabels();
        chargerAvatar(currentUser);

        // Configuration Sidebar
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
        }

        // Configuration Table
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (colNbArticles != null) colNbArticles.setCellValueFactory(new PropertyValueFactory<>("nbArticles"));
        
        if (colAgriculteur != null) {
            colAgriculteur.setCellValueFactory(new PropertyValueFactory<>("nomAgriculteur"));
            colAgriculteur.setVisible(currentUser != null && currentUser.getRole() == 3);
        }

        tableCategories.setPlaceholder(new Label("Aucune catégorie enregistrée"));
        setupFiltering();
        chargerDonnees();
    }

    private void updateUserLabels() {
        if (currentUser == null) return;
        if (userNameLabel != null) userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        if (userRoleLabel != null) {
            String roleText = switch (currentUser.getRole()) {
                case 1 -> "👷 EMPLOYÉ";
                case 2 -> "🌾 AGRICOLE";
                case 3 -> "👑 ADMIN";
                default -> "Rôle inconnu";
            };
            userRoleLabel.setText(roleText);
        }
    }

    private void chargerAvatar(Personne user) {
        if (user == null || avatarImageView == null) return;
        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Circle clip = new Circle(24, 24, 24);
        avatarImageView.setClip(clip);

        new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 48, 48, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarDefaultLabel.setVisible(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void chargerDonnees() {
        try {
            masterData.clear();
            if (currentUser != null && currentUser.getRole() == 2) { // 2 = AGRICOLE
                masterData.addAll(catService.recupererParUser(currentUser.getCin()));
                if (cbFiltreAgriculteur != null) {
                    cbFiltreAgriculteur.setVisible(false);
                    cbFiltreAgriculteur.setManaged(false);
                }
            } else {
                masterData.addAll(catService.recuperer());
                if (cbFiltreAgriculteur != null) chargerFiltreAgriculteurs();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupFiltering() {
        FilteredList<Categorie> filteredData = new FilteredList<>(masterData, p -> true);
        if (searchField != null) searchField.textProperty().addListener((obs, old, nv) -> appliquerFiltres(filteredData));
        if (cbFiltreAgriculteur != null) cbFiltreAgriculteur.valueProperty().addListener((obs, old, nv) -> appliquerFiltres(filteredData));

        SortedList<Categorie> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableCategories.comparatorProperty());
        tableCategories.setItems(sortedData);
    }

    private void appliquerFiltres(FilteredList<Categorie> filteredData) {
        filteredData.setPredicate(cat -> {
            String search = (searchField == null) ? "" : searchField.getText().toLowerCase().trim();
            String filterAgri = (cbFiltreAgriculteur == null) ? null : cbFiltreAgriculteur.getValue();

            boolean matchesSearch = search.isEmpty() || cat.getNom().toLowerCase().contains(search) || cat.getDescription().toLowerCase().contains(search);
            boolean matchesAgri = filterAgri == null || filterAgri.equals("Tous les Agriculteurs") || (cat.getNomAgriculteur() != null && cat.getNomAgriculteur().equals(filterAgri));
            return matchesSearch && matchesAgri;
        });
    }

    private void chargerFiltreAgriculteurs() {
        ObservableList<String> agriculteurs = FXCollections.observableArrayList("Tous les Agriculteurs");
        for (Categorie c : masterData) {
            if (c.getNomAgriculteur() != null && !agriculteurs.contains(c.getNomAgriculteur())) agriculteurs.add(c.getNomAgriculteur());
        }
        cbFiltreAgriculteur.setItems(agriculteurs);
        cbFiltreAgriculteur.getSelectionModel().selectFirst();
    }

    // ══════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════
    @FXML void ouvrirFormulaireAjout(ActionEvent event) { navigateTo(event, "/StocksInterface/ajoutercategorie.fxml"); }

    @FXML void modifierCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/ajoutercategorie.fxml"));
                Parent root = loader.load();
                ajoutercategorieController controller = loader.getController();
                controller.preparerModification(selected);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML void supprimerCategorie() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                catService.supprimer(selected.getId());
                chargerDonnees();
            } catch (SQLException e) {
                showError("Erreur", "Cette catégorie est liée à des articles !");
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════
    @FXML public void handleDashboard(MouseEvent event) {
        if (currentUser != null && currentUser.getRole() == 1) navigateTo(event, "/UsersInterface/AcceuilEmp.fxml");
        else navigateTo(event, "/UsersInterface/Acceuil.fxml");
    }

    @FXML public void handleDashboardAgricole(MouseEvent event) {
        navigateTo(event, "/UsersInterface/AcceuillAgr.fxml");
    }

    @FXML public void handleStocks(MouseEvent event) {
        if (currentUser != null && currentUser.getRole() == 1) navigateTo(event, "/StocksInterface/AfficherStockOuvrier.fxml");
        else if (currentUser != null && currentUser.getRole() == 2) navigateTo(event, "/StocksInterface/AfficherArticleAgr.fxml");
        else navigateTo(event, "/StocksInterface/afficherarticle.fxml");
    }

    @FXML public void handleMesStocks(ActionEvent event) {
        handleStocks(null);
    }

    @FXML public void handleMesArticles(MouseEvent event) {
        handleStocks(event);
    }

    @FXML public void handleMesCatégories(MouseEvent event) {
        if (currentUser != null && currentUser.getRole() == 2) navigateTo(event, "/StocksInterface/AfficherCategorieAgr.fxml");
        else navigateTo(event, "/StocksInterface/affichercategorie.fxml");
    }

    @FXML public void handleCategories() { /* Déjà ici */ }

    @FXML public void handleMesTaches(MouseEvent event) { navigateTo(event, "/UsersInterface/MesTaches.fxml"); }
    @FXML public void handleTerrains(MouseEvent event) { navigateTo(event, "/TerrainsInterface/acceuilterrain.fxml"); }
    @FXML public void handleMesTerrains(MouseEvent event) { navigateTo(event, "/TerrainsInterface/acceuilterrain.fxml"); }
    
    @FXML public void handleMesAnimaux(MouseEvent event) { navigateTo(event, "/AnimalsInterface/AfficherAnimaux.fxml"); }

    @FXML public void handleMonMateriel(MouseEvent event) { navigateTo(event, "/MaterielsInterface/AgricoleAffichageMachine.fxml"); }

    @FXML public void handleMonAbonnement(MouseEvent event) { navigateTo(event, "/UsersInterface/MesAbonnements.fxml"); }

    @FXML public void handleMesEvenements(MouseEvent event) { navigateTo(event, "/G-Evenements/AfficherEvenementsUser.fxml"); }

    @FXML public void handleEvents(MouseEvent event) {
        if (currentUser != null && currentUser.getRole() == 1) navigateTo(event, "/G-Evenements/AfficherEvenementsEmp.fxml");
        else navigateTo(event, "/G-Evenements/Accueil.fxml");
    }

    @FXML public void handleMateriels(MouseEvent event) {
        if (currentUser != null && currentUser.getRole() == 1) navigateTo(event, "/MaterielsInterface/MaintenanceFront.fxml");
        else navigateTo(event, "/MaterielsInterface/AccueilMateriel.fxml");
    }

    @FXML public void voirHistoriqueGlobal(ActionEvent event) {
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/historiqueMouvements.fxml"));
            Parent root = loader.load();
            historiqueMouvementsController ctrl = loader.getController();
            ctrl.chargerHistoriqueGlobal(currentUser.getCin());
            Stage stage = new Stage();
            stage.setTitle("Mon Historique de Mouvements");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void ouvrirTerrains(MouseEvent event) { navigateTo(event, "/TerrainsInterface/agricoleaffichageterrain.fxml"); }
    @FXML public void ouvrirPlantes(MouseEvent event) { navigateTo(event, "/TerrainsInterface/agricoleaffichageplante.fxml"); }
    @FXML public void ouvrirRotations(MouseEvent event) { navigateTo(event, "/TerrainsInterface/agricoleaffichagerotation.fxml"); }

    @FXML public void handleLogout(MouseEvent event) {
        SessionManager.clearSession();
        navigateTo(event, "/UsersInterface/login.fxml");
    }

    @FXML public void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setScene(new Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.show();
        } catch (IOException e) { showError("Erreur", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  UTILS
    // ══════════════════════════════════════════════════════
    private void navigateTo(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private void showGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
