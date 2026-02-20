package controllers.Events;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import services.Events.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AfficherCategoriesController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML
    private TableView<CategorieEvenement> tasksTable;

    @FXML
    private TableColumn<CategorieEvenement, String> titleColumn;

    @FXML
    private TableColumn<CategorieEvenement, String> descriptionColumn;

    @FXML
    private TableColumn<CategorieEvenement, Void> actionsColumn;

    @FXML
    private TextField searchField;

    private final CategorieEvenementService service = new CategorieEvenementService();
    private ObservableList<CategorieEvenement> categories;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize(Event event) {

            // Cacher submenu par défaut
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);

            // 1. Hover sur le bouton Gestion → Ouvre submenu
            gestionBtn.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });

            // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
            gestionContainer.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });
        initColumns(event);
        try {
            loadCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns(Event event) {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_categorie"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description_categorie"));

        // ⚠️ LIGNE CRITIQUE (SINON TABLE VIDE)
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(null));

        addActionButtons(event);
    }

    // ================= LOAD DATA =================
    private void loadCategories() throws SQLException {
        categories = FXCollections.observableArrayList(service.recuperer());
        tasksTable.setItems(categories);
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons(Event event) {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white;");

                editBtn.setOnAction(e -> {
                    CategorieEvenement c = getTableView().getItems().get(getIndex());
                    ouvrirPage(event,"ModifierCategorie.fxml");  // Passe la catégorie
                });

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
                            loadCategories();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
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

    // ================= ADD CATEGORY =================
    @FXML
    private void handleAddCategorie(ActionEvent event) {
        ouvrirPage(event,"AjouterCategorie.fxml");
    }

    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= SIDEBAR NAVIGATION =================
    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPage(event,"/G-Evenements/Accueil.fxml");
    }

    // ================= NAVIGATION METHODS =================
    private void ouvrirPage(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

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
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePersonnes(Event event )  {
        this.ouvrirPage(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.ouvrirPage(event,"/UsersInterface/GestionTache.fxml");}



    @FXML
    private void handleAbonnements(Event event) { /* Charger vue Abonnements */
        this.ouvrirPage(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(Event event) { /* Charger vue Offres */
        this.ouvrirPage(event,"/UsersInterface/GestionOffre.fxml");}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.ouvrirPage(actionEvent,"/UsersInterface/Acceuil.fxml");

    }
    public void handleAnimals(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
    }
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion...");

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

    /**
     * Afficher une erreur
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




}