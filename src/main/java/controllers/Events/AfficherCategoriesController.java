package controllers.Events;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import services.Events.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AfficherCategoriesController {

    @FXML private Button logoutBtn, gestionBtn;
    @FXML private VBox gestionSubmenu, gestionContainer;

    @FXML private TableView<CategorieEvenement> tasksTable;
    @FXML private TableColumn<CategorieEvenement, String> titleColumn;
    @FXML private TableColumn<CategorieEvenement, String> descriptionColumn;
    @FXML private TableColumn<CategorieEvenement, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Label resultsCountLabel;  // ← à ajouter dans le FXML

    private final CategorieEvenementService service = new CategorieEvenementService();
    private ObservableList<CategorieEvenement> categories;
    private FilteredList<CategorieEvenement> filteredData;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {

        // Cacher submenu par défaut
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);

        // Hover sur le bouton Gestion → Ouvre submenu
        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());

        // Hover sur TOUT le container Gestion → Garde submenu ouvert
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        initColumns();
        try {
            loadCategories();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_categorie"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description_categorie"));
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(null));
        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadCategories() throws SQLException {
        categories = FXCollections.observableArrayList(service.recuperer());
        filteredData = new FilteredList<>(categories, c -> true);
        tasksTable.setItems(filteredData);
        updateResultsCount();
        System.out.println("✅ " + categories.size() + " catégories chargées");
    }

    // ================= RECHERCHE RÉACTIVE =================
    private void setupReactiveSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> appliquerRecherche());
    }

    private void appliquerRecherche() {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.setPredicate(c -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredData.setPredicate(categorie -> {
                String nom = categorie.getNom_categorie();
                if (nom == null) return false;
                return nom.toLowerCase().contains(lowerCaseFilter);
            });
        }
        updateResultsCount();
    }

    // ================= MISE À JOUR DU COMPTEUR =================
    private void updateResultsCount() {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(filteredData.size() + " catégorie(s) trouvée(s)");
        }
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn   = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox   box       = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-cursor: hand;");

                // ===== MODIFIER → ouvre un pop-up modal =====
                editBtn.setOnAction(e -> {
                    CategorieEvenement c = getTableView().getItems().get(getIndex());
                    ouvrirPopup("/G-Evenements/ModifierCategorie.fxml", c);
                });

                // ===== SUPPRIMER =====
                deleteBtn.setOnAction(e -> {
                    CategorieEvenement c = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression de catégorie");
                    alert.setContentText("Voulez-vous supprimer cette catégorie ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            service.supprimer(c.getId_categorie());
                            showSuccess("Succès", "Catégorie supprimée avec succès !");
                            loadCategories();
                            appliquerRecherche();
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible de supprimer : " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ================= ADD CATEGORY → ouvre un pop-up modal =================
    @FXML
    private void handleAddCategorie(ActionEvent event) {
        ouvrirPopup("/G-Evenements/AjouterCategorie.fxml", null);
    }

    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadCategories();
            searchField.clear();
            showSuccess("Actualisation", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    // ================= SIDEBAR NAVIGATION =================
    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPage(event, "/G-Evenements/Accueil.fxml");
    }

    // ================= POPUP MODAL =================
    private void ouvrirPopup(String fxml, CategorieEvenement categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(tasksTable.getScene().getWindow());
            popupStage.setResizable(true);
            popupStage.setTitle(categorie == null ? "Nouvelle Catégorie" : "Modifier la Catégorie");
            popupStage.setScene(new Scene(root));

            if (categorie != null) {
                ModifierCategorieController controller = loader.getController();
                controller.setCategorie(categorie);
            }

            popupStage.showAndWait(); // BLOQUANT : on reprend ici après fermeture du pop-up

            loadCategories();
            appliquerRecherche();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= NAVIGATION METHODS =================
    private void ouvrirPage(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // On change la racine, pas la scène → la fenêtre ne bouge pas d'un pixel
            scene.setRoot(root);

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) tasksTable.getScene().getWindow();
            Scene scene = stage.getScene();

            // On change la racine, pas la scène → la fenêtre ne bouge pas d'un pixel
            scene.setRoot(root);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= SIDEBAR HANDLERS =================
    @FXML
    private void handlePersonnes(Event event) {
        ouvrirPage(event, "/UsersInterface/DahboardPersonne.fxml");
    }

    @FXML
    private void handleTaches(Event event) {
        ouvrirPage(event, "/UsersInterface/GestionTache.fxml");
    }

    @FXML
    private void handleAbonnements(Event event) {
        ouvrirPage(event, "/UsersInterface/GestionAbonnements.fxml");
    }

    @FXML
    private void handleOffres(Event event) {
        ouvrirPage(event, "/UsersInterface/GestionOffre.fxml");
    }

    public void handleDashboard(MouseEvent actionEvent) {
        ouvrirPage(actionEvent, "/UsersInterface/Acceuil.fxml");
    }

    public void handleAnimals(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/AnimalsInterface/AfficherAnimaux.fxml");
    }

    public void handleStocks(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/StocksInterface/afficherarticle.fxml");
    }

    public void handleTerrains(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/TerrainsInterface/acceuilterrain.fxml");
    }

    public void handleEvents(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/G-Evenements/Accueil.fxml");
    }

    public void handleMateriels(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/MaterielsInterface/AccueilMateriel.fxml");
    }

    // ================= SUBMENU HELPERS =================
    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    // ================= LOGOUT =================
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

                System.out.println("✓ Déconnexion réussie");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    // ================= ALERT METHODS =================
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}