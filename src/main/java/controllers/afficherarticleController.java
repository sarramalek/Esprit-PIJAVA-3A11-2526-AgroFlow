package controllers;

import entities.Article;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.ArticleService;
import services.CategorieService;

import java.io.IOException;
import java.sql.SQLException;

public class afficherarticleController {

    @FXML private TableView<Article> tableArticles;
    @FXML private TableColumn<Article, String> colNom, colUnite, colCategorie;
    @FXML private TableColumn<Article, Double> colQuantite, colSeuil;
    @FXML private TableColumn<Article, Void> colActions;
    @FXML private TextField tfRecherche;

    private final ArticleService articleService = new ArticleService();
    private final CategorieService catService = new CategorieService();
    private ObservableList<Article> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Liaison des colonnes de base
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteEnStock"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("uniteMesure"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));

        // 2. CORRECTION : Affichage du Nom au lieu de "Erreur"
        colCategorie.setCellValueFactory(cellData -> {
            int idCat = cellData.getValue().getIdCategorie();
            try {
                String nomCat = catService.getNomById(idCat);
                return new SimpleStringProperty(nomCat);
            } catch (Exception e) {
                // Si la requête échoue, on affiche l'ID pour le debug
                return new SimpleStringProperty("ID: " + idCat);
            }
        });

        // 3. Configuration visuelle
        configurerColonneActions();
        configurerStyleLignes();
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            masterData = FXCollections.observableArrayList(articleService.recuperer());

            // Mise en place de la recherche
            FilteredList<Article> filteredData = new FilteredList<>(masterData, p -> true);
            tfRecherche.textProperty().addListener((obs, old, nv) -> {
                filteredData.setPredicate(article -> {
                    if (nv == null || nv.isEmpty()) return true;
                    return article.getNom().toLowerCase().contains(nv.toLowerCase());
                });
            });
            SortedList<Article> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableArticles.comparatorProperty());
            tableArticles.setItems(sortedData);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void configurerColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            // Remplacement des icônes par du texte
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Supprimer");
            private final HBox container = new HBox(btnEdit, btnDel);

            {
                container.setSpacing(10);
                container.setStyle("-fx-alignment: center;"); // Centre les boutons dans la cellule

                // Style du bouton Modifier (Orange/Jaune)
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                // Style du bouton Supprimer (Rouge)
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                // Actions des boutons
                btnEdit.setOnAction(e -> {
                    Article a = getTableView().getItems().get(getIndex());
                    ouvrirFormulaire(a, e);
                });

                btnDel.setOnAction(e -> {
                    Article a = getTableView().getItems().get(getIndex());
                    supprimer(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }
    private void configurerStyleLignes() {
        tableArticles.setRowFactory(tv -> new TableRow<Article>() {
            @Override
            protected void updateItem(Article a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) setStyle("");
                else if (a.getQuantiteEnStock() <= a.getSeuilAlerte())
                    setStyle("-fx-background-color: #fab1a0;"); // Alerte stock bas
                else setStyle("-fx-background-color: #fdfae7;");
            }
        });
    }

    private void supprimer(Article a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + a.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try {
                articleService.supprimer(a.getId());
                chargerDonnees();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void ouvrirFormulaire(Article a, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterarticle.fxml"));
            Parent root = loader.load();
            if (a != null) {
                ajouterarticleController ctrl = loader.getController();
                ctrl.preparerModification(a);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void ouvrirFormulaireAjout(ActionEvent event) { ouvrirFormulaire(null, event); }
    @FXML void allerVersCategories(ActionEvent event) throws IOException { changerScene("/affichercategorie.fxml", event); }
    @FXML void deconnexion(ActionEvent event) throws IOException { changerScene("/login.fxml", event); }

    private void changerScene(String fxml, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}