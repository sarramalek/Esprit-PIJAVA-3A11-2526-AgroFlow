package controllers.Stocks;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import models.Stocks.Article;
import models.Stocks.Categorie;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Personne;
import services.Stocks.ArticleService;
import services.Stocks.CategorieService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ajouterarticleController {

    // ══════════════════════════════════════════════════════
    //  FXML — Formulaire
    // ══════════════════════════════════════════════════════
    @FXML private TextField           tfNom;
    @FXML private TextField           tfQuantite;
    @FXML private TextField           tfSeuil;
    @FXML private TextField           tfUnite;
    @FXML private ComboBox<Categorie> cbCategories;
    @FXML private Label               lblTitre;

    // Labels de feedback en temps réel
    @FXML private Label msgNom;
    @FXML private Label msgQuantite;
    @FXML private Label msgSeuil;
    @FXML private Label msgUnite;
    @FXML private Label msgCategorie;

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
    private final ArticleService   articleService = new ArticleService();
    private final CategorieService catService     = new CategorieService();
    private boolean isModification  = false;
    private int     idArticleActuel = 0;

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

        chargerCategories();

        // Feedback initial (uniquement en mode ajout)
        if (!isModification) {
            afficherFeedback(msgNom,       "⚠️ Veuillez remplir le nom (min 3 car.)", true);
            afficherFeedback(msgQuantite,  "⚠️ Veuillez saisir une quantité (≥ 0)",  true);
            afficherFeedback(msgSeuil,     "⚠️ Veuillez saisir un seuil (≥ 0)",      true);
            afficherFeedback(msgUnite,     "⚠️ Veuillez saisir l'unité",             true);
            afficherFeedback(msgCategorie, "⚠️ Veuillez sélectionner une catégorie", true);
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
            if (nv.trim().isEmpty())        afficherFeedback(msgNom, "⚠️ Obligatoire", true);
            else if (nv.trim().length() < 3) afficherFeedback(msgNom, "⚠️ Trop court (min 3)", true);
            else                             afficherFeedback(msgNom, "✅ Correct", false);
        });

        tfQuantite.textProperty().addListener((obs, old, nv) -> {
            try {
                double val = Double.parseDouble(nv);
                afficherFeedback(msgQuantite, val < 0 ? "⚠️ Pas de négatif" : "✅ Correct", val < 0);
            } catch (NumberFormatException e) {
                afficherFeedback(msgQuantite, "⚠️ Chiffres uniquement", true);
            }
        });

        tfSeuil.textProperty().addListener((obs, old, nv) -> {
            try {
                double val = Double.parseDouble(nv);
                afficherFeedback(msgSeuil, val < 0 ? "⚠️ Pas de négatif" : "✅ Correct", val < 0);
            } catch (NumberFormatException e) {
                afficherFeedback(msgSeuil, "⚠️ Chiffres uniquement", true);
            }
        });

        tfUnite.textProperty().addListener((obs, old, nv) -> {
            afficherFeedback(msgUnite,
                    nv.trim().isEmpty() ? "⚠️ Obligatoire" : "✅ Correct",
                    nv.trim().isEmpty());
        });

        cbCategories.valueProperty().addListener((obs, old, nv) -> {
            if (nv != null) afficherFeedback(msgCategorie, "✅ Sélectionné", false);
        });
    }

    // ══════════════════════════════════════════════════════
    //  VALIDATION & ENREGISTREMENT
    // ══════════════════════════════════════════════════════
    @FXML
    void validerAjout(ActionEvent event) throws IOException {
        if (!verifierTout()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Formulaire invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez corriger les erreurs avant de confirmer.");
            alert.show();
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
            else                articleService.ajouter(a);
            retourListe(event);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean verifierTout() {
        try {
            return tfNom.getText().trim().length() >= 3
                    && !tfUnite.getText().trim().isEmpty()
                    && cbCategories.getValue() != null
                    && Double.parseDouble(tfQuantite.getText()) >= 0
                    && Double.parseDouble(tfSeuil.getText()) >= 0;
        } catch (Exception e) { return false; }
    }

    public void preparerModification(Article a) {
        isModification = true;
        if (lblTitre != null) lblTitre.setText("Modifier l'Article");
        idArticleActuel = a.getId();
        tfNom.setText(a.getNom());
        tfQuantite.setText(String.valueOf(a.getQuantiteEnStock()));
        tfSeuil.setText(String.valueOf(a.getSeuilAlerte()));
        tfUnite.setText(a.getUniteMesure());
        for (Categorie c : cbCategories.getItems()) {
            if (c.getId() == a.getIdCategorie()) { cbCategories.setValue(c); break; }
        }
    }

    // ══════════════════════════════════════════════════════
    //  POPUP — AJOUT RAPIDE CATÉGORIE
    // ══════════════════════════════════════════════════════
    @FXML
    void allerAjouterCategorie(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("AgroFlow - Ajout Rapide");
        dialog.setHeaderText("Créer une nouvelle catégorie");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #fdfae7;");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom (ex: Engrais)");
        nomField.setPrefHeight(35);

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description...");
        descArea.setPrefRowCount(3);

        Label msgNomPop  = new Label("⚠️ Nom requis (min 3)");
        Label msgDescPop = new Label("⚠️ Description requise (min 5)");
        String styleErreur = "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11px;";
        String styleSucces = "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 11px;";
        msgNomPop.setStyle(styleErreur);
        msgDescPop.setStyle(styleErreur);

        // Écouteurs en temps réel dans la popup
        nomField.textProperty().addListener((obs, old, nv) -> {
            if (nv.trim().length() >= 3) { msgNomPop.setText("✅ Nom valide");         msgNomPop.setStyle(styleSucces); }
            else                         { msgNomPop.setText("⚠️ Nom requis (min 3)"); msgNomPop.setStyle(styleErreur); }
        });
        descArea.textProperty().addListener((obs, old, nv) -> {
            if (nv.trim().length() >= 5) { msgDescPop.setText("✅ Description valide");         msgDescPop.setStyle(styleSucces); }
            else                         { msgDescPop.setText("⚠️ Description requise (min 5)"); msgDescPop.setStyle(styleErreur); }
        });

        VBox layout = new VBox(8,
                new Label("Nom :"), msgNomPop, nomField,
                new Label("Description :"), msgDescPop, descArea);
        layout.setPadding(new Insets(20));
        dialogPane.setContent(layout);

        ButtonType btnAjouter = new ButtonType("AJOUTER", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnAjouter, ButtonType.CANCEL);

        // Bloquer si invalide ou doublon
        final Button btOk = (Button) dialogPane.lookupButton(btnAjouter);
        btOk.addEventFilter(ActionEvent.ACTION, ae -> {
            String nom  = nomField.getText().trim();
            String desc = descArea.getText().trim();
            try {
                if (catService.existeDeja(nom)) {
                    msgNomPop.setText("❌ Ce nom existe déjà !");
                    msgNomPop.setStyle(styleErreur);
                    ae.consume();
                } else if (nom.length() < 3 || desc.length() < 5) {
                    if (nom.length()  < 3) { msgNomPop.setText("⚠️ Trop court (min 3)");   msgNomPop.setStyle(styleErreur); }
                    if (desc.length() < 5) { msgDescPop.setText("⚠️ Trop courte (min 5)"); msgDescPop.setStyle(styleErreur); }
                    ae.consume();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnAjouter) {
                try {
                    catService.ajouter(new Categorie(0,
                            nomField.getText().trim(),
                            descArea.getText().trim()));
                    chargerCategories();
                    // Sélection automatique de la nouvelle catégorie
                    cbCategories.getItems().stream()
                            .filter(c -> c.getNom().equalsIgnoreCase(nomField.getText().trim()))
                            .findFirst()
                            .ifPresent(c -> cbCategories.setValue(c));
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT DES CATÉGORIES
    // ══════════════════════════════════════════════════════
    private void chargerCategories() {
        try {
            List<Categorie> list = catService.recuperer();
            cbCategories.setItems(FXCollections.observableArrayList(list));
            cbCategories.setConverter(new javafx.util.StringConverter<Categorie>() {
                @Override public String toString(Categorie c) { return c == null ? "" : c.getNom(); }
                @Override public Categorie fromString(String s) {
                    return cbCategories.getItems().stream()
                            .filter(c -> c.getNom().equals(s)).findFirst().orElse(null);
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION INTERNE (Stocks)
    // ══════════════════════════════════════════════════════
    @FXML void allerVersArticles(ActionEvent event) {
        try { retourListe(event); } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void allerVersCategories(ActionEvent event) {
        changerScene(event, "/StocksInterface/affichercategorie.fxml");
    }

    @FXML void retourListe(ActionEvent event) throws IOException {
        try {
            Personne currentUser = SessionManager.getCurrentUser();
            String fxml = (currentUser != null && currentUser.getRole() == 1)
                    ? "/StocksInterface/AfficherArticleAgr.fxml"
                    : "/StocksInterface/afficherarticle.fxml";

            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML void deconnexion(ActionEvent event) {
        changerScene(event, "/UsersInterface/login.fxml");
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR (autres modules)
    // ══════════════════════════════════════════════════════
    @FXML public void handleDashboard(MouseEvent event) { changerScene(event, "/UsersInterface/Acceuil.fxml"); }
    @FXML public void handleAnimals(MouseEvent event)   { changerScene(event, "/AnimalsInterface/AfficherAnimaux.fxml"); }
    @FXML public void handleStocks(MouseEvent event)    { changerScene(event, "/StocksInterface/afficherarticle.fxml"); }
    @FXML public void handleTerrains(MouseEvent event)  { changerScene(event, "/TerrainsInterface/acceuilterrain.fxml"); }
    @FXML public void handleEvents(MouseEvent event)    { changerScene(event, "/G-Evenements/Accueil.fxml"); }
    @FXML public void handleMateriels(MouseEvent event) { changerScene(event, "/MaterielsInterface/AccueilMateriel.fxml"); }
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

    private static void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}