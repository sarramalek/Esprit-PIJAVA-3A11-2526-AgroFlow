package controllers;

import entities.Categorie;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image; // Import nécessaire
import javafx.scene.image.ImageView; // Import nécessaire
import javafx.stage.Stage;
import services.CategorieService;
import services.ImageService; // Nouveau service
import services.TranslatorService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class ajoutercategorieController {

    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private Label lblTitre, msgNom, msgDescription;
    @FXML private Label lblTradEn, lblTradAr;
    @FXML private ImageView imgPreview; // FXML ID pour l'image

    private final CategorieService catService = new CategorieService();
    private final TranslatorService translator = new TranslatorService();
    private final ImageService imageService = new ImageService(); // Initialisation ImageService

    private boolean isModification = false;
    private int idCategorieActuel;
    private String nomAnglais = "";
    private String nomArabe = "";
    private String imageUrl = ""; // Stocke l'URL récupérée

    private Timer timerTraduction = new Timer();

    @FXML
    public void initialize() {
        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Remplir le nom (min 3 car.)", true);
            afficherFeedback(msgDescription, "⚠️ Remplir la description (min 5 car.)", true);
        }
        ajouterEcouteurs();
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();

            if (timerTraduction != null) {
                timerTraduction.cancel();
            }

            if (val.length() < 3) {
                afficherFeedback(msgNom, "⚠️ Trop court", true);
                lblTradEn.setText("...");
                lblTradAr.setText("...");
                imgPreview.setImage(null);
            } else {
                timerTraduction = new Timer();
                timerTraduction.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // 1. Appel API Traduction
                        String anglais = translator.traduire(val, "en");
                        String arabe = translator.traduire(val, "ar");

                        // 2. Appel API Image (Pixabay) basé sur le nom anglais
                        String fetchedImageUrl = imageService.chercherImage(anglais);

                        Platform.runLater(() -> {
                            nomAnglais = anglais;
                            nomArabe = arabe;
                            imageUrl = fetchedImageUrl; // Mise à jour de l'URL pour la DB

                            if (lblTradEn != null) lblTradEn.setText(anglais);
                            if (lblTradAr != null) lblTradAr.setText(arabe);

                            // 3. Mise à jour de l'aperçu visuel
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                imgPreview.setImage(new Image(imageUrl));
                            }

                            afficherFeedback(msgNom, "✅ Validé", false);
                        });
                    }
                }, 600);
            }
        });

        taDescription.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.length() < 5) {
                afficherFeedback(msgDescription, "⚠️ Trop courte", true);
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
        this.nomAnglais = (c.getNomEn() != null) ? c.getNomEn() : "";
        this.nomArabe = (c.getNomAr() != null) ? c.getNomAr() : "";
        this.imageUrl = (c.getImageUrl() != null) ? c.getImageUrl() : "";

        if (c.getNom().length() >= 3) {
            afficherFeedback(msgNom, "✅ Prêt à modifier", false);
            if (lblTradEn != null) lblTradEn.setText(nomAnglais);
            if (lblTradAr != null) lblTradAr.setText(nomArabe);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imgPreview.setImage(new Image(imageUrl));
            }
        }
    }

    @FXML
    void validerAjout(ActionEvent event) {
        String nom = tfNom.getText().trim();
        String desc = taDescription.getText().trim();
        if (nom.length() < 3 || desc.length() < 5) {
            afficherAlerte(Alert.AlertType.WARNING, "Format invalide", "Veuillez respecter les contraintes.");
            return;
        }
        try {
            if (catService.existeDeja(nom) && !isModification) {
                afficherFeedback(msgNom, "❌ Ce nom existe déjà !", true);
                return;
            }

            // CORRECTION : Passage de 6 paramètres au constructeur de Categorie
            Categorie c = new Categorie(isModification ? idCategorieActuel : 0, nom, nomAnglais, nomArabe, desc, imageUrl);

            if (isModification) catService.modifier(c);
            else catService.ajouter(c);

            retourListe(event);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur Système", "Problème d'accès à la base de données.");
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