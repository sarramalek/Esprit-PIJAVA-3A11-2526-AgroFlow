package controllers;

import entities.Article;
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
import services.ArticleService;
import java.io.IOException;
import java.sql.SQLException;

public class afficherarticleController {

    @FXML private TableView<Article> tableArticles;
    @FXML private TableColumn<Article, String> colNom, colUnite;
    @FXML private TableColumn<Article, Double> colQuantite, colSeuil;

    private final ArticleService articleService = new ArticleService();

    @FXML
    public void initialize() {
        // Liaison des colonnes avec les attributs de l'entité Article
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteEnStock"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("uniteMesure"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));

        // Gestion dynamique du style des lignes et suppression du vide en bas
        tableArticles.setRowFactory(tv -> new TableRow<Article>() {
            @Override
            protected void updateItem(Article article, boolean empty) {
                super.updateItem(article, empty);
                if (empty || article == null) {
                    setStyle("-fx-background-color: transparent;"); // Cache les lignes vides
                } else {
                    // Si l'article est en alerte (stock <= seuil)
                    if (article.getQuantiteEnStock() <= article.getSeuilAlerte()) {
                        setStyle("-fx-background-color: #fab1a0; -fx-border-color: #e17055; -fx-border-width: 0 0 1 0;");
                    } else {
                        // Style normal
                        setStyle("-fx-background-color: #fdfae7; -fx-border-color: #dcdde1; -fx-border-width: 0 0 1 0;");
                    }
                }
            }
        });

        tableArticles.setPlaceholder(new Label("Aucun article dans la liste"));
        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            tableArticles.setItems(FXCollections.observableArrayList(articleService.recuperer()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- NAVIGATION (AJOUT DES MÉTHODES MANQUANTES) ---

    @FXML
    void allerVersArticles(ActionEvent event) {
        // Déjà présent sur la vue
        System.out.println("Déjà sur la page des articles.");
    }

    @FXML
    void allerVersCategories(ActionEvent event) throws IOException {
        // Redirection vers la gestion des catégories
        // Assurez-vous que le fichier fxml existe avec ce nom exact
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/affichercategorie.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            System.err.println("Erreur de navigation vers catégories : " + e.getMessage());
        }
    }

    // --- ACTIONS SUR LES ARTICLES ---

    @FXML
    void supprimerArticle(ActionEvent event) {
        Article selected = tableArticles.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                articleService.supprimer(selected.getId());
                chargerDonnees();
                afficherAlerte(Alert.AlertType.INFORMATION, "Suppression", "Article supprimé avec succès !");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un article dans le tableau.");
        }
    }

    @FXML
    void ouvrirFormulaireAjout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajouterarticle.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    void modifierArticle(ActionEvent event) throws IOException {
        Article selected = tableArticles.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterarticle.fxml"));
            Parent root = loader.load();

            // Accès au contrôleur d'ajout pour injecter les données
            ajouterarticleController controller = loader.getController();
            controller.preparerModification(selected);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } else {
            afficherAlerte(Alert.AlertType.WARNING, "Attention", "Sélectionnez un article à modifier.");
        }
    }

    // --- UTILITAIRE ---

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    void deconnexion(ActionEvent event) {
        try {
            // Chargement de la page de connexion
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Erreur de déconnexion : " + e.getMessage());
        }
    }
}