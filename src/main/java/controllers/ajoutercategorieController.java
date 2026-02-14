package controllers;

import entities.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.CategorieService;
import java.io.IOException;
import java.sql.SQLException;

public class ajoutercategorieController {

    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private Label lblTitre, msgNom, msgDescription;

    private final CategorieService catService = new CategorieService();
    private boolean isModification = false;
    private int idCategorieActuel;

    @FXML
    public void initialize() {
        msgNom.setText("");
        msgDescription.setText("");
        ajouterEcouteurs();
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) afficherFeedback(msgNom, "⚠️ Nom requis", true);
            else if (newValue.length() < 3) afficherFeedback(msgNom, "⚠️ Trop court (min 3 car.)", true);
            else afficherFeedback(msgNom, "✅ Correct", false);
        });

        taDescription.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) afficherFeedback(msgDescription, "⚠️ Description requise", true);
            else afficherFeedback(msgDescription, "✅ Correct", false);
        });
    }

    public void preparerModification(Categorie c) {
        isModification = true;
        lblTitre.setText("Modifier la Catégorie");
        idCategorieActuel = c.getId();
        tfNom.setText(c.getNom());
        taDescription.setText(c.getDescription());
    }

    @FXML
    void validerAjout(ActionEvent event) {
        if (tfNom.getText().trim().isEmpty() || taDescription.getText().trim().isEmpty()) return;

        try {
            Categorie c = new Categorie(isModification ? idCategorieActuel : 0, tfNom.getText(), taDescription.getText());
            if (isModification) catService.modifier(c);
            else catService.ajouter(c);
            retourListe(event);
        } catch (SQLException | IOException e) { e.printStackTrace(); }
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/affichercategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}