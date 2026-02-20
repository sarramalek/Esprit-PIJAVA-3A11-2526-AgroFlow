package controllers.Stocks;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Stocks.Categorie;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.Stocks.CategorieService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class affichercategorieController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, String> colNom, colDescription;

    private final CategorieService catService = new CategorieService();

    @FXML
    public void initialize() {

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
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Rendre le tableau transparent et styliser les lignes (comme pour les articles)
        tableCategories.setRowFactory(tv -> new TableRow<Categorie>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: #fdfae7; -fx-border-color: #dcdde1; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        tableCategories.setPlaceholder(new Label("Aucune catégorie enregistrée"));
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            tableCategories.setItems(FXCollections.observableArrayList(catService.recuperer()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- NAVIGATION BARRE LATÉRALE ---
    @FXML void allerVersArticles(ActionEvent event) throws IOException {
        changerScene(event, "/StocksInterface/afficherarticle.fxml");
    }

    @FXML void allerVersCategories(ActionEvent event) { /* Déjà sur cette page */ }

    @FXML void deconnexion(ActionEvent event) throws IOException {
        changerScene(event, "/UsersInterface/login.fxml");
    }

    private void changerScene(Event event, String fxml) throws IOException {
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

    // --- ACTIONS GESTION ---
    @FXML void ouvrirFormulaireAjout(ActionEvent event) throws IOException {
        changerScene(event, "/StocksInterface/ajoutercategorie.fxml");
    }

    @FXML void modifierCategorie(ActionEvent event) throws IOException {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/ajoutercategorie.fxml"));
            Parent root = loader.load();
            ajoutercategorieController controller = loader.getController();
            controller.preparerModification(selected);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }

    @FXML void supprimerCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                catService.supprimer(selected.getId());
                chargerDonnees();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur : Cette catégorie est liée à des articles !").show();
            }
        }
    }
    //navigation vers les autres modules
    @FXML
    private void handlePersonnes(MouseEvent event ) throws IOException {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(MouseEvent event ) throws IOException { /* Charger vue Tâches */
        this.changerScene(event,"/UsersInterface/GestionTache.fxml");}



    @FXML private void handleAbonnements(MouseEvent event) throws IOException { /* Charger vue Abonnements */
        this.changerScene(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) throws IOException { /* Charger vue Offres */
        this.changerScene(event,"/UsersInterface/GestionOffre.fxml");}



    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) throws IOException {
        this.changerScene(actionEvent, "/UsersInterface/Acceuil.fxml");

    }
    public void handleAnimals(MouseEvent mouseEvent) throws IOException {
        this.changerScene(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(MouseEvent mouseEvent) throws IOException {
        this.changerScene(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(MouseEvent mouseEvent) throws IOException {
        this.changerScene(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) throws IOException {
        this.changerScene(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(MouseEvent mouseEvent) throws IOException {
        this.changerScene(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
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