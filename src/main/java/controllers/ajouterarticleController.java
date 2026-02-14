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

    // Labels d'erreur (doivent exister dans ton FXML)
    @FXML private Label msgNom, msgQuantite, msgSeuil, msgUnite, msgCategorie;

    private ArticleService articleService = new ArticleService();
    private CategorieService catService = new CategorieService();
    private boolean isModification = false;
    private int idArticleActuel;

    @FXML
    public void initialize() {
        chargerCategories();
        nettoyerMessages();
        ajouterEcouteurs(); // Active la validation en temps réel
    }

    private void nettoyerMessages() {
        Label[] labels = {msgNom, msgQuantite, msgSeuil, msgUnite, msgCategorie};
        for (Label l : labels) { if (l != null) l.setText(""); }
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        if (label == null) return;
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    // --- VALIDATION EN TEMPS RÉEL (PENDANT LA SAISIE) ---
    private void ajouterEcouteurs() {
        // Pour le Nom
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) afficherFeedback(msgNom, "⚠️ Obligatoire", true);
            else if (newValue.length() < 3) afficherFeedback(msgNom, "⚠️ Trop court", true);
            else afficherFeedback(msgNom, "✅ Correct", false);
        });

        // Pour la Quantité
        tfQuantite.textProperty().addListener((obs, old, newValue) -> {
            try {
                double val = Double.parseDouble(newValue);
                if (val < 0) afficherFeedback(msgQuantite, "⚠️ Pas de négatif", true);
                else afficherFeedback(msgQuantite, "✅ Correct", false);
            } catch (NumberFormatException e) {
                afficherFeedback(msgQuantite, "⚠️ Chiffres uniquement", true);
            }
        });

        // Pour le Seuil
        tfSeuil.textProperty().addListener((obs, old, newValue) -> {
            try {
                double val = Double.parseDouble(newValue);
                if (val < 0) afficherFeedback(msgSeuil, "⚠️ Pas de négatif", true);
                else afficherFeedback(msgSeuil, "✅ Correct", false);
            } catch (NumberFormatException e) {
                afficherFeedback(msgSeuil, "⚠️ Chiffres uniquement", true);
            }
        });

        // Pour l'Unité
        tfUnite.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) afficherFeedback(msgUnite, "⚠️ Obligatoire", true);
            else afficherFeedback(msgUnite, "✅ Correct", false);
        });

        // Pour la Catégorie
        cbCategories.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) afficherFeedback(msgCategorie, "✅ Correct", false);
        });
    }

    // --- VALIDATION FINALE (AU CLIC SUR CONFIRMER) ---
    private boolean verifierTout() {
        boolean valide = true;
        if (tfNom.getText().isEmpty() || tfNom.getText().length() < 3) valide = false;
        if (tfUnite.getText().isEmpty()) valide = false;
        if (cbCategories.getValue() == null) {
            afficherFeedback(msgCategorie, "⚠️ Sélectionnez une catégorie", true);
            valide = false;
        }
        try {
            if (Double.parseDouble(tfQuantite.getText()) < 0) valide = false;
            if (Double.parseDouble(tfSeuil.getText()) < 0) valide = false;
        } catch (Exception e) { valide = false; }

        return valide;
    }

    @FXML
    void validerAjout(ActionEvent event) {
        if (!verifierTout()) {
            afficherAlerte(Alert.AlertType.WARNING, "Formulaire incomplet", "Veuillez corriger les erreurs affichées au-dessus des champs.");
            return;
        }

        try {
            Article a = new Article(
                    isModification ? idArticleActuel : 0,
                    tfNom.getText().trim(),
                    Double.parseDouble(tfQuantite.getText()),
                    Double.parseDouble(tfSeuil.getText()),
                    tfUnite.getText().trim(),
                    cbCategories.getValue().getId()
            );

            if (isModification) articleService.modifier(a);
            else articleService.ajouter(a);

            retourListe(event);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Garde tes méthodes chargerCategories, retourListe, preparerModification ici ---

    private void chargerCategories() {
        try {
            List<Categorie> list = catService.recuperer();
            list.sort(Comparator.comparing(Categorie::getNom, String.CASE_INSENSITIVE_ORDER));
            cbCategories.setItems(FXCollections.observableArrayList(list));
            cbCategories.setCellFactory(lv -> new ListCell<Categorie>() {
                @Override protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNom());
                }
            });
            cbCategories.setButtonCell(new ListCell<Categorie>() {
                @Override protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNom());
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }
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

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML void allerAjouterCategorie(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajoutercategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/afficherarticle.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
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
}