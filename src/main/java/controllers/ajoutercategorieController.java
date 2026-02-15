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
        // Validation immédiate au démarrage pour guider l'utilisateur
        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Veuillez remplir le nom (min 3 car.)", true);
            afficherFeedback(msgDescription, "⚠️ Veuillez remplir la description (min 5 car.)", true);
        }

        ajouterEcouteurs();
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        // Validation du Nom (min 3)
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.isEmpty()) {
                afficherFeedback(msgNom, "⚠️ Le nom est obligatoire", true);
            } else if (val.length() < 3) {
                afficherFeedback(msgNom, "⚠️ Trop court (min 3 car.)", true);
            } else {
                afficherFeedback(msgNom, "✅ Nom valide", false);
            }
        });

        // Validation de la Description (min 5)
        taDescription.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.isEmpty()) {
                afficherFeedback(msgDescription, "⚠️ La description est obligatoire", true);
            } else if (val.length() < 5) {
                afficherFeedback(msgDescription, "⚠️ Trop courte (min 5 car.)", true);
            } else {
                afficherFeedback(msgDescription, "✅ Description valide", false);
            }
        });
    }

    public void preparerModification(Categorie c) {
        isModification = true;
        lblTitre.setText("Modifier la Catégorie");
        idCategorieActuel = c.getId();
        tfNom.setText(c.getNom());
        taDescription.setText(c.getDescription());

        // Validation instantanée des données chargées
        if (c.getNom().length() >= 3) afficherFeedback(msgNom, "✅ Nom valide", false);
        if (c.getDescription().length() >= 5) afficherFeedback(msgDescription, "✅ Description valide", false);
    }

    @FXML
    void validerAjout(ActionEvent event) {
        String nom = tfNom.getText().trim();
        String desc = taDescription.getText().trim();

        // 1. Validation des longueurs minimales
        if (nom.length() < 3 || desc.length() < 5) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Format invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez respecter les contraintes :\n- Nom : 3 caractères\n- Description : 5 caractères");
            alert.show();
            return;
        }

        try {
            // 2. Vérification de l'unicité (uniquement pour un nouvel ajout ou si le nom a changé en modification)
            // Note: On suppose que idCategorieActuel est 0 pour un nouvel ajout
            if (catService.existeDeja(nom) && !isModification) {
                afficherFeedback(msgNom, "❌ Ce nom de catégorie existe déjà !", true);
                return;
            }

            // 3. Procéder à l'enregistrement
            Categorie c = new Categorie(isModification ? idCategorieActuel : 0, nom, desc);

            if (isModification) {
                catService.modifier(c);
            } else {
                catService.ajouter(c);
            }

            retourListe(event);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur Système", "Une erreur est survenue lors de l'accès à la base de données.");
        }
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/affichercategorie.fxml"));
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
}