package controllers;

import entities.Article;
import entities.Categorie;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ArticleService;
import services.CategorieService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class ajouterarticleController {

    @FXML private TextField tfNom, tfQuantite, tfSeuil, tfUnite;
    @FXML private ComboBox<Categorie> cbCategories;
    @FXML private Label lblTitre;

    private ArticleService articleService = new ArticleService();
    private CategorieService catService = new CategorieService();

    private boolean isModification = false;
    private int idArticleActuel;

    @FXML
    public void initialize() {
        chargerCategories();
    }

    private void chargerCategories() {
        try {
            List<Categorie> list = catService.recuperer();

            // TRI ALPHABÉTIQUE PAR NOM
            list.sort(Comparator.comparing(Categorie::getNom, String.CASE_INSENSITIVE_ORDER));

            cbCategories.setItems(FXCollections.observableArrayList(list));

            // Affichage du nom dans la ComboBox
            cbCategories.setCellFactory(lv -> new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNom());
                }
            });
            cbCategories.setButtonCell(new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNom());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void preparerModification(Article a) {
        isModification = true;
        lblTitre.setText("Modifier l'Article");
        idArticleActuel = a.getId();
        tfNom.setText(a.getNom());
        tfQuantite.setText(String.valueOf(a.getQuantiteEnStock()));
        tfSeuil.setText(String.valueOf(a.getSeuilAlerte()));
        tfUnite.setText(a.getUniteMesure());
    }

    @FXML
    void validerAjout(ActionEvent event) {
        Categorie selected = cbCategories.getSelectionModel().getSelectedItem();

        if (selected == null || tfNom.getText().isEmpty() || tfQuantite.getText().isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir les informations et choisir une catégorie.");
            return;
        }

        try {
            String nom = tfNom.getText();
            double qte = Double.parseDouble(tfQuantite.getText());
            double seuil = Double.parseDouble(tfSeuil.getText());
            String unite = tfUnite.getText();
            int idCat = selected.getId();

            if (isModification) {
                articleService.modifier(new Article(idArticleActuel, nom, qte, seuil, unite, idCat));
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Article modifié avec succès !");
            } else {
                articleService.ajouter(new Article(0, nom, qte, seuil, unite, idCat));
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Article ajouté avec succès !");
            }
            retourListe(event);

        } catch (NumberFormatException e) {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur format", "La quantité et le seuil doivent être des nombres.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void allerAjouterCategorie(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajoutercategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/afficherarticle.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}