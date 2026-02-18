package controllers.Stocks;

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

public class affichercategorieController {

    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, String> colNom, colDescription;

    private final CategorieService catService = new CategorieService();

    @FXML
    public void initialize() {
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

    private void changerScene(ActionEvent event, String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
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
}