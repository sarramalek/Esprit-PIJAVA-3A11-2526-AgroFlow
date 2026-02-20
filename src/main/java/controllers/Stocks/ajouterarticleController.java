package controllers.Stocks;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
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
import services.Stocks.ArticleService;
import services.Stocks.CategorieService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class ajouterarticleController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private TextField tfNom, tfQuantite, tfSeuil, tfUnite;
    @FXML private ComboBox<Categorie> cbCategories;
    @FXML private Label lblTitre;

    // Labels d'erreur (Assure-toi qu'ils existent dans ton FXML avec ces fx:id)
    @FXML private Label msgNom, msgQuantite, msgSeuil, msgUnite, msgCategorie;

    private final ArticleService articleService = new ArticleService();
    private final CategorieService catService = new CategorieService();
    private boolean isModification = false;
    private int idArticleActuel;

    @FXML
    public void initialize() {

            // Cacher submenu par défaut
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);

            // 1. Hover sur le bouton Gestion → Ouvre submenu
            gestionBtn.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });

            // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
            gestionContainer.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });
        chargerCategories();

        // 1. Validation immédiate au démarrage
        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Veuillez remplir le nom (min 3 car.)", true);
            afficherFeedback(msgQuantite, "⚠️ Veuillez saisir une quantité (≥ 0)", true);
            afficherFeedback(msgSeuil, "⚠️ Veuillez saisir un seuil (≥ 0)", true);
            afficherFeedback(msgUnite, "⚠️ Veuillez saisir l'unité", true);
            afficherFeedback(msgCategorie, "⚠️ Veuillez sélectionner une catégorie", true);
        }

        // 2. Activation des écouteurs en temps réel
        ajouterEcouteurs();
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        if (label == null) return;
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) afficherFeedback(msgNom, "⚠️ Obligatoire", true);
            else if (newValue.trim().length() < 3) afficherFeedback(msgNom, "⚠️ Trop court", true);
            else afficherFeedback(msgNom, "✅ Correct", false);
        });

        tfQuantite.textProperty().addListener((obs, old, newValue) -> {
            try {
                double val = Double.parseDouble(newValue);
                if (val < 0) afficherFeedback(msgQuantite, "⚠️ Pas de négatif", true);
                else afficherFeedback(msgQuantite, "✅ Correct", false);
            } catch (NumberFormatException e) {
                afficherFeedback(msgQuantite, "⚠️ Chiffres uniquement", true);
            }
        });

        tfSeuil.textProperty().addListener((obs, old, newValue) -> {
            try {
                double val = Double.parseDouble(newValue);
                if (val < 0) afficherFeedback(msgSeuil, "⚠️ Pas de négatif", true);
                else afficherFeedback(msgSeuil, "✅ Correct", false);
            } catch (NumberFormatException e) {
                afficherFeedback(msgSeuil, "⚠️ Chiffres uniquement", true);
            }
        });

        tfUnite.textProperty().addListener((obs, old, newValue) -> {
            if (newValue.trim().isEmpty()) afficherFeedback(msgUnite, "⚠️ Obligatoire", true);
            else afficherFeedback(msgUnite, "✅ Correct", false);
        });

        cbCategories.valueProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) afficherFeedback(msgCategorie, "✅ Sélectionné", false);
        });
    }

    // --- NAVIGATION (RÉSOUT LES ERREURS LOADEXCEPTION) ---

    @FXML
    void allerVersArticles(ActionEvent event) {
        try { retourListe(event); } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void allerVersCategories(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/StocksInterface/affichercategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }



    @FXML
    void allerAjouterCategorie(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("AgroFlow - Ajout Rapide");
        dialog.setHeaderText("Créer une nouvelle catégorie");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #fdfae7;"); // Ton thème beige

        // Champs de saisie
        TextField nomField = new TextField();
        nomField.setPromptText("Nom (ex: Engrais)");
        nomField.setPrefHeight(35);

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description...");
        descArea.setPrefRowCount(3);

        // Labels de feedback
        Label msgNomPop = new Label("⚠️ Nom requis (min 3)");
        Label msgDescPop = new Label("⚠️ Description requise (min 5)");

        String styleErreur = "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11px;";
        String styleSucces = "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 11px;";
        msgNomPop.setStyle(styleErreur);
        msgDescPop.setStyle(styleErreur);

        // Mise en page
        VBox layout = new VBox(8, new Label("Nom :"), msgNomPop, nomField, new Label("Description :"), msgDescPop, descArea);
        layout.setPadding(new Insets(20));
        dialogPane.setContent(layout);

        ButtonType btnAjouter = new ButtonType("AJOUTER", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(btnAjouter, ButtonType.CANCEL);

        // --- LOGIQUE DE VÉRIFICATION À L'AJOUT ---
        final Button btOk = (Button) dialogPane.lookupButton(btnAjouter);
        btOk.addEventFilter(ActionEvent.ACTION, ae -> {
            String nom = nomField.getText().trim();
            String desc = descArea.getText().trim();

            try {
                // 1. Vérifier si le nom existe déjà
                if (catService.existeDeja(nom)) {
                    msgNomPop.setText("❌ Ce nom existe déjà !");
                    msgNomPop.setStyle(styleErreur);
                    ae.consume(); // Empêche la fermeture de la pop-up
                }
                // 2. Vérifier les longueurs minimales
                else if (nom.length() < 3 || desc.length() < 5) {
                    msgNomPop.setText(nom.length() < 3 ? "⚠️ Trop court (min 3)" : "✅ Correct");
                    msgDescPop.setText(desc.length() < 5 ? "⚠️ Trop courte (min 5)" : "✅ Correct");
                    ae.consume();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // --- TRAITEMENT APRÈS VALIDATION ---
        dialog.showAndWait().ifPresent(response -> {
            if (response == btnAjouter) {
                try {
                    catService.ajouter(new Categorie(0, nomField.getText().trim(), descArea.getText().trim()));
                    chargerCategories(); // Rafraîchit ta ComboBox d'articles

                    // Sélection automatique de la nouvelle catégorie
                    cbCategories.getItems().stream()
                            .filter(c -> c.getNom().equalsIgnoreCase(nomField.getText().trim()))
                            .findFirst()
                            .ifPresent(c -> cbCategories.setValue(c));

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void deconnexion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/StocksInterface/afficherarticle.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // --- LOGIQUE MÉTIER ---

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
            else articleService.ajouter(a);

            retourListe(event);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean verifierTout() {
        try {
            return tfNom.getText().trim().length() >= 3 &&
                    !tfUnite.getText().trim().isEmpty() &&
                    cbCategories.getValue() != null &&
                    Double.parseDouble(tfQuantite.getText()) >= 0 &&
                    Double.parseDouble(tfSeuil.getText()) >= 0;
        } catch (Exception e) { return false; }
    }

    public void preparerModification(Article a) {
        isModification = true;
        lblTitre.setText("Modifier l'Article");
        idArticleActuel = a.getId();
        tfNom.setText(a.getNom());
        tfQuantite.setText(String.valueOf(a.getQuantiteEnStock()));
        tfSeuil.setText(String.valueOf(a.getSeuilAlerte()));
        tfUnite.setText(a.getUniteMesure());

        // Sélection auto de la catégorie
        for (Categorie c : cbCategories.getItems()) {
            if (c.getId() == a.getIdCategorie()) {
                cbCategories.setValue(c);
                break;
            }
        }
    }

    private void chargerCategories() {
        try {
            List<Categorie> list = catService.recuperer();
            cbCategories.setItems(FXCollections.observableArrayList(list));

            // Afficher uniquement le NOM dans la liste et dans le champ sélectionné
            cbCategories.setConverter(new javafx.util.StringConverter<Categorie>() {
                @Override
                public String toString(Categorie object) {
                    return (object == null) ? "" : object.getNom();
                }
                @Override
                public Categorie fromString(String string) {
                    return cbCategories.getItems().stream()
                            .filter(c -> c.getNom().equals(string))
                            .findFirst().orElse(null);
                }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }
    //navigation vers les autres modules
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.changerScene(event,"/UsersInterface/GestionTache.fxml");}



    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.changerScene(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.changerScene(event,"/UsersInterface/GestionOffre.fxml");}

    @FXML private void handleGestion(MouseEvent event) { /* Vue principale Gestion */
    }

    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) throws IOException {
        this.changerScene(actionEvent, "/UsersInterface/Acceuil.fxml");

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
    }
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    /**
     * Afficher une erreur
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void changerScene(Event event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlFile);
            e.printStackTrace();
        }
    }

}