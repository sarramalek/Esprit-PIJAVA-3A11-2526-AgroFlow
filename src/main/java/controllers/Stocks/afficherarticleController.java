package controllers.Stocks;

import controllers.User.ProfilEmploye;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Stocks.Article;
import models.User.Personne;
import services.Stocks.ArticleService;
import services.Stocks.CategorieService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import utils.SessionManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class afficherarticleController {
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;

    // ══════════════════════════════════════════════════════
    //  FXML — Tableau & Colonnes
    // ══════════════════════════════════════════════════════
    @FXML private TableView<Article>            tableArticles;
    @FXML private TableColumn<Article, String>  colNom, colUnite, colCategorie;
    @FXML private TableColumn<Article, Double>  colQuantite, colSeuil;
    @FXML private TableColumn<Article, Void>    colActions;

    // ══════════════════════════════════════════════════════
    //  FXML — Recherche & Filtres
    // ══════════════════════════════════════════════════════
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbFiltreCategorie;

    // ══════════════════════════════════════════════════════
    //  FXML — QR Code & KPI Labels
    // ══════════════════════════════════════════════════════
    @FXML private ImageView ivQRCode;
    @FXML private Label     lblNomSelection;
    @FXML private Label     lblNbAlertes;
    @FXML private Label     lblTotalArticles;
    @FXML private Label     lblWarning;

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar & Navigation
    // ══════════════════════════════════════════════════════
    @FXML private Button logoutBtn;
    @FXML private Button gestionBtn;
    @FXML private VBox   gestionSubmenu;
    @FXML private VBox   gestionContainer;

    // ══════════════════════════════════════════════════════
    //  Services & Données
    // ══════════════════════════════════════════════════════
    private final ArticleService   articleService        = new ArticleService();
    private final CategorieService catService            = new CategorieService();
    private ObservableList<Article> masterData           = FXCollections.observableArrayList();

    // Bloque les doublons d'alertes email
    private final Set<Integer> alertesDejaEnvoyees = new HashSet<>();

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        // Mise à jour des labels
        updateUserLabels();
        chargerSidebarAvatar(SessionManager.getCurrentUser());
        // Sidebar submenu caché par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
        if (gestionBtn != null)
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        if (gestionContainer != null)
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        configurerColonnes();

        // Sélection → affichage QR
        tableArticles.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, nv) -> { if (nv != null) afficherQR(nv); });

        configurerStyleLignes();
        chargerDonnees();
    }
    private void chargerAvatarTopBar(Personne user) {
        if (user == null) return;

        // Afficher le nom
        if (userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }

        // Charger la photo depuis l'URL Cloudinary dans un thread background
        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            // Pas de photo → garder l'emoji par défaut, rien à faire
            return;
        }

        // Appliquer le clip circulaire en Java (ne fonctionne pas correctement en FXML)
        Circle clip = new Circle(24, 24, 24);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 48, 48, false, true, true);

                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * Met à jour les labels nom/rôle dans la sidebar.
     */
    private void updateUserLabels() {
        if (currentUser == null) return;

        if (userNameLabel != null)
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        else
            System.err.println("✗ userNameLabel est NULL (non lié en FXML ?)");

        if (userRoleLabel != null) {
            String roleText = switch (currentUser.getRole()) {
                case 1 -> "🌾 AGRICOLE";
                case 2 -> "👷 EMPLOYÉ";
                case 3 -> "👑 ADMIN";
                default -> "Rôle inconnu";
            };
            userRoleLabel.setText(roleText);
        } else {
            System.err.println("✗ userRoleLabel est NULL (non lié en FXML ?)");
        }
    }
    // ══════════════════════════════════════════════════════
    //  CONFIGURATION DES COLONNES
    // ══════════════════════════════════════════════════════
    private void configurerColonnes() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteEnStock"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("uniteMesure"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));

        // Affichage du nom de catégorie (résolution depuis l'id)
        colCategorie.setCellValueFactory(cellData -> {
            int idCat = cellData.getValue().getIdCategorie();
            try {
                String nomCat = catService.getNomById(idCat);
                return new SimpleStringProperty(nomCat != null ? nomCat : "ID: " + idCat);
            } catch (Exception e) {
                return new SimpleStringProperty("ID: " + idCat);
            }
        });

        configurerColonneActions();
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT DES DONNÉES
    // ══════════════════════════════════════════════════════
    private void chargerDonnees() {
        try {
            masterData = FXCollections.observableArrayList(articleService.recuperer());

            // Combo catégories
            if (cbFiltreCategorie != null) {
                ObservableList<String> cats = FXCollections.observableArrayList("Toutes");
                cats.addAll(catService.recuperer().stream()
                        .map(c -> c.getNom())
                        .collect(Collectors.toList()));
                cbFiltreCategorie.setItems(cats);
                cbFiltreCategorie.getSelectionModel().selectFirst();
            }

            // Recherche + filtre combinés
            FilteredList<Article> filteredData = new FilteredList<>(masterData, p -> true);

            tfRecherche.textProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));
            if (cbFiltreCategorie != null)
                cbFiltreCategorie.valueProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));

            SortedList<Article> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableArticles.comparatorProperty());
            tableArticles.setItems(sortedData);

            mettreAJourKPI();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════
    //  FILTRAGE COMBINÉ
    // ══════════════════════════════════════════════════════
    private void appliquerFiltres(FilteredList<Article> filteredData) {
        filteredData.setPredicate(article -> {
            String search = (tfRecherche.getText() == null) ? ""
                    : tfRecherche.getText().toLowerCase();
            boolean matchesNom = article.getNom().toLowerCase().contains(search);

            boolean matchesCat = true;
            if (cbFiltreCategorie != null) {
                String catSel = cbFiltreCategorie.getValue();
                if (catSel != null && !catSel.equals("Toutes")) {
                    try {
                        String nomCat = catService.getNomById(article.getIdCategorie());
                        matchesCat = catSel.equals(nomCat);
                    } catch (Exception e) {
                        matchesCat = false;
                    }
                }
            }
            return matchesNom && matchesCat;
        });
        mettreAJourKPI();
    }

    // ══════════════════════════════════════════════════════
    //  KPI & ALERTES
    // ══════════════════════════════════════════════════════
    private void mettreAJourKPI() {
        int total = masterData.size();
        List<Article> alertes = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (lblTotalArticles != null) lblTotalArticles.setText(String.valueOf(total));
        if (lblNbAlertes     != null) lblNbAlertes.setText(String.valueOf(alertes.size()));

        if (lblWarning != null) {
            if (!alertes.isEmpty()) {
                lblWarning.setVisible(true);
                appliquerAnimationAlerte();
            } else {
                lblWarning.setVisible(false);
            }
        }
    }

    private void appliquerAnimationAlerte() {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), lblWarning);
        fade.setFromValue(1.0);
        fade.setToValue(0.1);
        fade.setCycleCount(Timeline.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    // ══════════════════════════════════════════════════════
    //  RÉAPPROVISIONNEMENT
    // ══════════════════════════════════════════════════════
    @FXML
    void toutReapprovisionner(ActionEvent event) {
        List<Article> enAlerte = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (enAlerte.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun article en alerte.").show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Réapprovisionner " + enAlerte.size() + " articles ?");
        if (confirm.showAndWait().get() == ButtonType.YES) {
            try {
                for (Article a : enAlerte) {
                    a.setQuantiteEnStock(a.getSeuilAlerte() * 2);
                    articleService.modifier(a);
                    alertesDejaEnvoyees.remove(a.getId());
                }
                chargerDonnees();
                new Alert(Alert.AlertType.INFORMATION, "Stocks mis à jour !").show();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  EXPORT PDF ALERTES
    // ══════════════════════════════════════════════════════
    @FXML
    void exporterAlertesPDF(ActionEvent event) {
        List<Article> articlesEnAlerte = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (articlesEnAlerte.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune alerte à exporter.").show();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Alertes_Stock.pdf");
        File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(new Paragraph("AGROFLOW - RAPPORT D'ALERTES\n\n",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.RED)));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                Stream.of("Article", "Stock", "Seuil", "Unité").forEach(t -> {
                    PdfPCell h = new PdfPCell(new Phrase(t,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                    h.setBackgroundColor(new BaseColor(45, 90, 39));
                    table.addCell(h);
                });

                for (Article a : articlesEnAlerte) {
                    table.addCell(a.getNom());
                    table.addCell(String.valueOf(a.getQuantiteEnStock()));
                    table.addCell(String.valueOf(a.getSeuilAlerte()));
                    table.addCell(a.getUniteMesure());
                }
                document.add(table);
                document.close();
                new Alert(Alert.AlertType.INFORMATION, "Rapport exporté !").show();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  QR CODE
    // ══════════════════════════════════════════════════════
    private void afficherQR(Article a) {
        if (lblNomSelection != null) lblNomSelection.setText(a.getNom());
        if (ivQRCode != null)
            ivQRCode.setImage(new Image(articleService.genererLienQRCode(a), true));
    }

    @FXML
    void enregistrerQRCode(ActionEvent event) {
        if (ivQRCode != null && ivQRCode.getImage() != null) {
            FileChooser fc = new FileChooser();
            String nom = (lblNomSelection != null)
                    ? lblNomSelection.getText().replace(" ", "_") : "article";
            fc.setInitialFileName("QR_" + nom + ".png");
            File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(ivQRCode.getImage(), null), "png", file);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  COLONNES ACTIONS (Modifier / Supprimer)
    // ══════════════════════════════════════════════════════
    private void configurerColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel  = new Button("Supprimer");
            private final HBox container = new HBox(btnEdit, btnDel);
            {
                container.setSpacing(10);
                container.setStyle("-fx-alignment: center;");
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;"
                        + "-fx-font-weight: bold; -fx-cursor: hand;");
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;"
                        + "-fx-font-weight: bold; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> ouvrirFormulaire(
                        getTableView().getItems().get(getIndex()), e));
                btnDel.setOnAction(e -> supprimer(
                        getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  STYLE DES LIGNES (alerte rouge / normal jaune)
    // ══════════════════════════════════════════════════════
    private void configurerStyleLignes() {
        tableArticles.setRowFactory(tv -> new TableRow<Article>() {
            @Override protected void updateItem(Article a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) setStyle("");
                else if (a.getQuantiteEnStock() <= a.getSeuilAlerte())
                    setStyle("-fx-background-color: #fab1a0;");
                else
                    setStyle("-fx-background-color: #fdfae7;");
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════
    private void supprimer(Article a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + a.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try {
                articleService.supprimer(a.getId());
                chargerDonnees();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void ouvrirFormulaire(Article a, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/StocksInterface/ajouterarticle.fxml"));
            Parent root = loader.load();
            if (a != null) {
                ajouterarticleController ctrl = loader.getController();
                ctrl.preparerModification(a);
            }

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void ouvrirFormulaireAjout(ActionEvent event) { ouvrirFormulaire(null, event); }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION INTERNE (Stocks)
    // ══════════════════════════════════════════════════════
    @FXML void allerVersCategories(MouseEvent event) throws IOException {
        changerScene("/StocksInterface/affichercategorie.fxml", event);
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR (autres modules)
    // ══════════════════════════════════════════════════════
    @FXML public void handleDashboard(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/Acceuil.fxml", event);
    }
    @FXML public void handleAnimals(MouseEvent event) throws IOException {
        changerScene("/AnimalsInterface/AfficherAnimaux.fxml", event);
    }
    @FXML public void handleStocks(MouseEvent event) throws IOException {
        changerScene("/StocksInterface/afficherarticle.fxml", event);
    }
    @FXML public void handleTerrains(MouseEvent event) throws IOException {
        changerScene("/TerrainsInterface/acceuilterrain.fxml", event);
    }
    @FXML public void handleEvents(MouseEvent event) throws IOException {
        changerScene("/G-Evenements/Accueil.fxml", event);
    }
    @FXML public void handleMateriels(MouseEvent event) throws IOException {
        changerScene("/MaterielsInterface/AccueilMateriel.fxml", event);
    }
    @FXML private void handlePersonnes(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/DahboardPersonne.fxml", event);
    }
    @FXML private void handleTaches(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/GestionTache.fxml", event);
    }
    @FXML private void handleAbonnements(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/GestionAbonnements.fxml", event);
    }
    @FXML private void handleOffres(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/GestionOffre.fxml", event);
    }
    @FXML private void handleGestion(MouseEvent event) { /* Vue principale Gestion */ }

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
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();
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
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════
    private void changerScene(String fxml, Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // navigation Front Office Agricole
    @FXML private Button dashboardBtn;

    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;


    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;
    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;

        // Nom et rôle
        if (userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Clip circulaire appliqué en Java (radius=35, centre=35,35 pour fitWidth/Height=70)
        if (sidebarAvatarImageView != null) {
            Circle clip = new Circle(35, 35, 35);
            sidebarAvatarImageView.setClip(clip);
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Avatar sidebar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
    }

    private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }
    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboardAgricole(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         navigateTo(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml","Terrains");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/AnimalsInterface/acceuilagricoleanimaux.fxml","Animaux");
    }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Animaux"); }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }




    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Ajouter cette méthode getStage() pour ProfilAgricole
    public Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null)
            return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (welcomeNameLabel != null)
                welcomeNameLabel.setText(user.getPrenom() + " !");
            else
                System.err.println("✗ welcomeNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText("🌾 AGRICULTEUR");


        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }



    /**
     * Transfère l'utilisateur courant au contrôleur cible via réflexion
     */
    private void transferUserToController(Object controller) {
        try {
            controller.getClass()
                    .getMethod("setCurrentUser", Personne.class)
                    .invoke(controller, currentUser);
            System.out.println("✓ Utilisateur transféré au contrôleur");
        } catch (NoSuchMethodException e) {
            System.out.println("ℹ Le contrôleur n'a pas de méthode setCurrentUser()");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors du transfert utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère les erreurs de navigation de manière appropriée
     */
    private void handleNavigationError(String fxmlPath, String title, IOException e) {
        e.printStackTrace();

        // Vérifier si c'est un fichier manquant ou une autre erreur
        if (e.getMessage() != null && e.getMessage().contains("Location is not set")) {
            showInfo("Module à venir",
                    "Le module \"" + title + "\" sera disponible prochainement.");
        } else if (fxmlPath.contains("MesTerrains") ||
                fxmlPath.contains("MesAnimaux") ||
                fxmlPath.contains("MesStocks") ||
                fxmlPath.contains("MonMateriel")) {
            // Modules pas encore implémentés
            showInfo("Fonctionnalité à venir",
                    "Cette fonctionnalité est en cours de développement.");
        } else {
            // Erreur réelle
            showError("Erreur de chargement\n\n" +
                    "Impossible de charger " + title + ".\n" +
                    "Détails: " + e.getMessage());
        }
    }

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); } }




    public void handleMesEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsUser.fxml","Evenements");
    }

    public void ouvrirParticipations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherParticipationsUser.fxml","Participations");
    }


}