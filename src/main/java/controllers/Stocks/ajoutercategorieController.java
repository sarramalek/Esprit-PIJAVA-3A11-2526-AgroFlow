package controllers.Stocks;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Stocks.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Personne;
import services.Stocks.CategorieService;
import services.Stocks.ImageService;
import services.Stocks.TranslatorService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class ajoutercategorieController {

    // ══════════════════════════════════════════════════════
    //  FXML — Formulaire
    // ══════════════════════════════════════════════════════
    @FXML private TextField  tfNom;
    @FXML private TextArea   taDescription;
    @FXML private Label      lblTitre;
    @FXML private Label      msgNom;
    @FXML private Label      msgDescription;

    // Traduction & image (optionnels — protégés par null check)
    @FXML private Label     lblTradEn;
    @FXML private Label     lblTradAr;
    @FXML private ImageView imgPreview;

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar
    // ══════════════════════════════════════════════════════
    @FXML private Button logoutBtn;
    @FXML private Button gestionBtn;
    @FXML private VBox   gestionSubmenu;
    @FXML private VBox   gestionContainer;

    // ══════════════════════════════════════════════════════
    //  Services & État
    // ══════════════════════════════════════════════════════
    private final CategorieService  catService    = new CategorieService();
    private final TranslatorService translator    = new TranslatorService();
    private final ImageService      imageService  = new ImageService();

    private boolean isModification    = false;
    private int     idCategorieActuel = 0;
    private String  nomAnglais        = "";
    private String  nomArabe          = "";
    private String  imageUrl          = "";

    private Timer timerTraduction = new Timer();

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        // Sidebar submenu caché par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
        if (gestionBtn != null)
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        if (gestionContainer != null)
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        // Feedback initial
        if (!isModification) {
            afficherFeedback(msgNom,         "⚠️ Veuillez remplir le nom (min 3 car.)",         true);
            afficherFeedback(msgDescription, "⚠️ Veuillez remplir la description (min 5 car.)", true);
        }

        ajouterEcouteurs();
    }

    // ══════════════════════════════════════════════════════
    //  FEEDBACK EN TEMPS RÉEL
    // ══════════════════════════════════════════════════════
    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        if (label == null) return;
        label.setText(texte);
        label.setStyle(estErreur
                ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        tfNom.textProperty().addListener((obs, old, nv) -> {
            String val = nv.trim();

            // Annuler le timer précédent
            if (timerTraduction != null) timerTraduction.cancel();

            if (val.isEmpty()) {
                afficherFeedback(msgNom, "⚠️ Le nom est obligatoire", true);
                resetTraduction();
            } else if (val.length() < 3) {
                afficherFeedback(msgNom, "⚠️ Trop court (min 3 car.)", true);
                resetTraduction();
            } else {
                // Déclencher traduction + image après 600ms de pause
                timerTraduction = new Timer();
                timerTraduction.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String anglais        = translator.traduire(val, "en");
                        String arabe          = translator.traduire(val, "ar");
                        String fetchedImageUrl = imageService.chercherImage(anglais);

                        Platform.runLater(() -> {
                            nomAnglais = anglais;
                            nomArabe   = arabe;
                            imageUrl   = fetchedImageUrl;

                            if (lblTradEn != null) lblTradEn.setText(anglais);
                            if (lblTradAr != null) lblTradAr.setText(arabe);

                            if (imgPreview != null && imageUrl != null && !imageUrl.isEmpty())
                                imgPreview.setImage(new Image(imageUrl));

                            afficherFeedback(msgNom, "✅ Nom valide", false);
                        });
                    }
                }, 600);
            }
        });

        taDescription.textProperty().addListener((obs, old, nv) -> {
            String val = nv.trim();
            if (val.isEmpty())        afficherFeedback(msgDescription, "⚠️ La description est obligatoire", true);
            else if (val.length() < 5) afficherFeedback(msgDescription, "⚠️ Trop courte (min 5 car.)", true);
            else                       afficherFeedback(msgDescription, "✅ Description valide", false);
        });
    }

    private void resetTraduction() {
        if (lblTradEn  != null) lblTradEn.setText("...");
        if (lblTradAr  != null) lblTradAr.setText("...");
        if (imgPreview != null) imgPreview.setImage(null);
    }

    // ══════════════════════════════════════════════════════
    //  PRÉPARER MODIFICATION
    // ══════════════════════════════════════════════════════
    public void preparerModification(Categorie c) {
        isModification    = true;
        idCategorieActuel = c.getId();
        if (lblTitre != null) lblTitre.setText("Modifier la Catégorie");

        tfNom.setText(c.getNom());
        taDescription.setText(c.getDescription());

        this.nomAnglais = c.getNomEn()    != null ? c.getNomEn()    : "";
        this.nomArabe   = c.getNomAr()    != null ? c.getNomAr()    : "";
        this.imageUrl   = c.getImageUrl() != null ? c.getImageUrl() : "";

        if (c.getNom().length() >= 3) {
            afficherFeedback(msgNom, "✅ Prêt à modifier", false);
            if (lblTradEn != null) lblTradEn.setText(nomAnglais);
            if (lblTradAr != null) lblTradAr.setText(nomArabe);
            if (imgPreview != null && !imageUrl.isEmpty())
                imgPreview.setImage(new Image(imageUrl));
        }
        if (c.getDescription().length() >= 5)
            afficherFeedback(msgDescription, "✅ Description valide", false);
    }

    // ══════════════════════════════════════════════════════
    //  VALIDATION & ENREGISTREMENT
    // ══════════════════════════════════════════════════════
    @FXML
    void validerAjout(ActionEvent event) {
        String nom  = tfNom.getText().trim();
        String desc = taDescription.getText().trim();

        if (nom.length() < 3 || desc.length() < 5) {
            afficherAlerte(Alert.AlertType.WARNING, "Format invalide",
                    "Veuillez respecter les contraintes :\n- Nom : 3 caractères min\n- Description : 5 caractères min");
            return;
        }

        try {
            if (catService.existeDeja(nom) && !isModification) {
                afficherFeedback(msgNom, "❌ Ce nom de catégorie existe déjà !", true);
                return;
            }

            // Constructeur 6 paramètres (avec traductions et image)
            Categorie c = new Categorie(
                    isModification ? idCategorieActuel : 0,
                    nom, nomAnglais, nomArabe, desc, imageUrl);

            if (isModification) catService.modifier(c);
            else                catService.ajouter(c);

            retourListe(event);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur Système",
                    "Une erreur est survenue lors de l'accès à la base de données.");
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION INTERNE
    // ══════════════════════════════════════════════════════
    @FXML
    void retourListe(ActionEvent event) throws IOException {
        try {
            Personne currentUser = SessionManager.getCurrentUser();
            String fxml = (currentUser != null && currentUser.getRole() == 1)
                    ? "/StocksInterfaeInterface/AfficherArticleAgr.fxml"
                    : "/StocksInterface/afficherarticle.fxml";

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR (autres modules)
    // ══════════════════════════════════════════════════════
    @FXML public void handleDashboard(MouseEvent event)  { changerScene(event, "/UsersInterface/Acceuil.fxml"); }
    @FXML public void handleAnimals(MouseEvent event)    { changerScene(event, "/AnimalsInterface/AfficherAnimaux.fxml"); }
    @FXML public void handleStocks(MouseEvent event)     { changerScene(event, "/StocksInterface/afficherarticle.fxml"); }
    @FXML public void handleTerrains(MouseEvent event)   { changerScene(event, "/TerrainsInterface/acceuilterrain.fxml"); }
    @FXML public void handleEvents(MouseEvent event)     { changerScene(event, "/G-Evenements/Accueil.fxml"); }
    @FXML public void handleMateriels(MouseEvent event)  { changerScene(event, "/MaterielsInterface/AccueilMateriel.fxml"); }
    @FXML private void handlePersonnes(MouseEvent event)   { changerScene(event, "/UsersInterface/DahboardPersonne.fxml"); }
    @FXML private void handleTaches(Event event)           { changerScene(event, "/UsersInterface/GestionTache.fxml"); }
    @FXML private void handleAbonnements(MouseEvent event) { changerScene(event, "/UsersInterface/GestionAbonnements.fxml"); }
    @FXML private void handleOffres(MouseEvent event)      { changerScene(event, "/UsersInterface/GestionOffre.fxml"); }
    @FXML private void handleGestion(MouseEvent event)     { /* Vue principale Gestion */ }

    // ══════════════════════════════════════════════════════
    //  DÉCONNEXION
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                stage.setScene(new Scene(root, 900, 600));
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  SIDEBAR SUBMENU
    // ══════════════════════════════════════════════════════
    private void showGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true);  }
    }
    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════
    private void changerScene(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}