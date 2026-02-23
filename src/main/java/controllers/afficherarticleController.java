package controllers;

import entities.Article;
import entities.Categorie;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ArticleService;
import services.CategorieService;
import services.EmailService;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class afficherarticleController {

    @FXML private TableView<Article> tableArticles;
    @FXML private TableColumn<Article, String> colNom, colUnite, colCategorie;
    @FXML private TableColumn<Article, Double> colQuantite, colSeuil;
    @FXML private TableColumn<Article, Void> colActions;

    @FXML private TextField tfRecherche;
    @FXML private ComboBox<String> cbFiltreCategorie;

    @FXML private ImageView ivQRCode;
    @FXML private Label lblNomSelection, lblNbAlertes, lblTotalArticles, lblWarning;

    private final ArticleService articleService = new ArticleService();
    private final CategorieService catService = new CategorieService();
    private ObservableList<Article> masterData = FXCollections.observableArrayList();

    // UTILISATION D'UN SET POUR BLOQUER LES DOUBLONS
    private final Set<Integer> alertesDejaEnvoyees = new HashSet<>();

    @FXML
    public void initialize() {
        configurerColonnes();
        tableArticles.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) afficherQR(nv);
        });
        configurerStyleLignes();
        chargerDonnees();
    }

    private void configurerColonnes() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteEnStock"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("uniteMesure"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("nomCategorie"));
        configurerColonneActions();
    }

    private void chargerDonnees() {
        try {
            masterData = FXCollections.observableArrayList(articleService.recuperer());

            ObservableList<String> cats = FXCollections.observableArrayList("Toutes");
            cats.addAll(catService.recuperer().stream().map(Categorie::getNom).collect(Collectors.toList()));
            cbFiltreCategorie.setItems(cats);
            cbFiltreCategorie.getSelectionModel().selectFirst();

            FilteredList<Article> filteredData = new FilteredList<>(masterData, p -> true);
            tfRecherche.textProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));
            cbFiltreCategorie.valueProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));

            SortedList<Article> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableArticles.comparatorProperty());
            tableArticles.setItems(sortedData);

            mettreAJourKPI();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void appliquerFiltres(FilteredList<Article> filteredData) {
        filteredData.setPredicate(article -> {
            String search = (tfRecherche.getText() == null) ? "" : tfRecherche.getText().toLowerCase();
            boolean matchesNom = article.getNom().toLowerCase().contains(search);

            String catSel = cbFiltreCategorie.getValue();
            boolean matchesCat = true;

            // MODIFICATION ICI : On compare directement avec le texte du ComboBox
            if (catSel != null && !catSel.equals("Toutes")) {
                matchesCat = article.getNomCategorie().equals(catSel);
            }

            return matchesNom && matchesCat;
        });
        mettreAJourKPI();
    }
    private void mettreAJourKPI() {
        // 1. On calcule le total et on filtre les articles en alerte
        int total = masterData.size();
        List<Article> alertes = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        // 2. Mise à jour des Labels de l'interface
        lblTotalArticles.setText(String.valueOf(total));
        lblNbAlertes.setText(String.valueOf(alertes.size()));

        // 3. Gestion de l'affichage du Warning (Le label clignotant)
        if (!alertes.isEmpty()) {
            lblWarning.setVisible(true);
            appliquerAnimationAlerte();

            // --- NOTE : LA BOUCLE D'ENVOI D'EMAIL A ÉTÉ SUPPRIMÉE D'ICI ---
            // L'email est désormais géré par ArticleService.modifier()
            // pour garantir qu'il ne s'envoie qu'une seule fois.
        } else {
            lblWarning.setVisible(false);
        }
    }

    @FXML
    void toutReapprovisionner(ActionEvent event) {
        List<Article> enAlerte = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (enAlerte.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun article en alerte.").show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Réapprovisionner " + enAlerte.size() + " articles ?");
        if (confirm.showAndWait().get() == ButtonType.YES) {
            try {
                for (Article a : enAlerte) {
                    a.setQuantiteEnStock(a.getSeuilAlerte() * 2);
                    articleService.modifier(a);
                    // On retire du SET pour que si le stock redescend plus tard, l'alerte puisse repartir
                    alertesDejaEnvoyees.remove(a.getId());
                }
                chargerDonnees();
                new Alert(Alert.AlertType.INFORMATION, "Stocks mis à jour !").show();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- NAVIGATION ET AUTRES MÉTHODES ---

    @FXML
    void allerVersCategories(ActionEvent event) throws IOException {
        changerScene("/affichercategorie.fxml", event);
    }

    @FXML
    void deconnexion(ActionEvent event) throws IOException {
        changerScene("/login.fxml", event);
    }

    private void changerScene(String fxml, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML void ouvrirFormulaireAjout(ActionEvent event) { ouvrirFormulaire(null, event); }

    private void ouvrirFormulaire(Article a, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterarticle.fxml"));
            Parent root = loader.load();
            if (a != null) {
                ajouterarticleController ctrl = loader.getController();
                ctrl.preparerModification(a);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

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
                    PdfPCell h = new PdfPCell(new Phrase(t, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
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

    @FXML
    void enregistrerQRCode(ActionEvent event) {
        if (ivQRCode.getImage() != null) {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("QR_" + lblNomSelection.getText().replace(" ", "_") + ".png");
            File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(ivQRCode.getImage(), null), "png", file);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    private void appliquerAnimationAlerte() {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), lblWarning);
        fade.setFromValue(1.0); fade.setToValue(0.1);
        fade.setCycleCount(Timeline.INDEFINITE); fade.setAutoReverse(true);
        fade.play();
    }

    private void afficherQR(Article a) {
        lblNomSelection.setText(a.getNom());
        ivQRCode.setImage(new Image(articleService.genererLienQRCode(a), true));
    }

    private void configurerColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel = new Button("Supprimer");
            private final HBox container = new HBox(btnEdit, btnDel);
            {
                container.setSpacing(10);
                container.setStyle("-fx-alignment: center;");
                btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                btnEdit.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex()), e));
                btnDel.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void supprimer(Article a) {
        if (new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + a.getNom() + " ?").showAndWait().get() == ButtonType.YES) {
            try { articleService.supprimer(a.getId()); chargerDonnees(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void configurerStyleLignes() {
        tableArticles.setRowFactory(tv -> new TableRow<Article>() {
            @Override protected void updateItem(Article a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) setStyle("");
                else if (a.getQuantiteEnStock() <= a.getSeuilAlerte()) setStyle("-fx-background-color: #fab1a0;");
                else setStyle("-fx-background-color: #fdfae7;");
            }
        });
    }
}