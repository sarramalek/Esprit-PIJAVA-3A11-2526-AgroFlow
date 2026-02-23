package controllers;

import entities.Categorie;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.CategorieService;
import java.io.IOException;
import java.sql.SQLException;

public class affichercategorieController {

    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, String> colNom, colDescription, colNomEn, colNomAr;

    // --- NOUVELLE COLONNE POUR L'IMAGE ---
    @FXML private TableColumn<Categorie, String> colImage;

    private final CategorieService catService = new CategorieService();

    @FXML
    public void initialize() {
        // 1. Liaison des colonnes textuelles
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        if (colNomEn != null) colNomEn.setCellValueFactory(new PropertyValueFactory<>("nomEn"));
        if (colNomAr != null) colNomAr.setCellValueFactory(new PropertyValueFactory<>("nomAr"));

        // --- 2. CONFIGURATION DE LA COLONNE IMAGE ---
        if (colImage != null) {
            colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));

            colImage.setCellFactory(param -> new TableCell<Categorie, String>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String url, boolean empty) {
                    super.updateItem(url, empty);
                    if (empty || url == null || url.trim().isEmpty()) {
                        setGraphic(null);
                    } else {
                        try {
                            // On charge l'image en arrière-plan avec une taille de 50x50
                            Image image = new Image(url, 50, 50, true, true, true);
                            imageView.setImage(image);

                            // Style optionnel : arrondir les coins de l'image dans la table
                            imageView.setFitWidth(50);
                            imageView.setFitHeight(50);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(null); // En cas d'URL invalide
                        }
                    }
                }
            });
        }

        // 3. Stylisation des lignes
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

    // ... (Reste des méthodes de navigation et CRUD inchangés) ...

    @FXML void allerVersArticles(ActionEvent event) throws IOException { changerScene(event, "/afficherarticle.fxml"); }
    @FXML void allerVersCategories(ActionEvent event) { }
    @FXML void deconnexion(ActionEvent event) throws IOException { changerScene(event, "/login.fxml"); }

    private void changerScene(ActionEvent event, String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML void ouvrirFormulaireAjout(ActionEvent event) throws IOException { changerScene(event, "/ajoutercategorie.fxml"); }

    @FXML void modifierCategorie(ActionEvent event) throws IOException {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutercategorie.fxml"));
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
                new Alert(Alert.AlertType.ERROR, "Erreur : Cette catégorie est liée !").show();
            }
        }
    }
}