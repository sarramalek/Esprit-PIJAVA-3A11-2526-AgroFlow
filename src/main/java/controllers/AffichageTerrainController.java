package controllers;

import entities.terrain;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ApiMeteoService;
import services.TerrainService;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AffichageTerrainController implements Initializable {

    @FXML private TableView<terrain> tableTerrains;
    @FXML private TableColumn<terrain, String> colNom;
    @FXML private TableColumn<terrain, Float>  colSurface;
    @FXML private TableColumn<terrain, String> colTypeSol;
    @FXML private TableColumn<terrain, String> colLocalisation;
    @FXML private TableColumn<terrain, Float>  colPH;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;

    private final TerrainService ts = new TerrainService();
    private ObservableList<terrain> listeTerrains;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        configurerRecherche();
        configurerTri();
        chargerDonnees();
    }

    private void configurerTableau() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_terrain"));
        colSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));
        colTypeSol.setCellValueFactory(new PropertyValueFactory<>("type_sol"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colPH.setCellValueFactory(new PropertyValueFactory<>("p_h"));
    }

    private void configurerRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) chargerDonnees();
            else rechercherTerrains(newValue);
        });
    }

    private void configurerTri() {
        comboTri.setItems(FXCollections.observableArrayList(
                "Nom (A-Z)", "Nom (Z-A)",
                "Surface (croissante)", "Surface (décroissante)",
                "pH (acide au basique)", "pH (basique à acide)",
                "Type de sol (A-Z)"
        ));
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) trierTerrains(critere);
        });
    }

    private void chargerDonnees() {
        listeTerrains = FXCollections.observableArrayList(ts.afficherTous());
        tableTerrains.setItems(listeTerrains);
    }

    private void rechercherTerrains(String motCle) {
        listeTerrains = FXCollections.observableArrayList(ts.rechercher(motCle));
        tableTerrains.setItems(listeTerrains);
    }

    private void trierTerrains(String critere) {
        listeTerrains = FXCollections.observableArrayList(ts.trierPar(critere));
        tableTerrains.setItems(listeTerrains);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboTri.setValue(null);
        chargerDonnees();
    }

    // ============================================================
    // MÉTÉO - Open-Meteo + Nominatim
    // ============================================================
    @FXML
    public void afficherMeteo(ActionEvent event) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();

        String localisation = (terrainSelectionne != null
                && terrainSelectionne.getLocalisation() != null
                && !terrainSelectionne.getLocalisation().isEmpty())
                ? terrainSelectionne.getLocalisation()
                : "Tunis, Tunisie";

        // Chargement dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                ApiMeteoService apiService = new ApiMeteoService();
                ApiMeteoService.MeteoResult meteo = apiService.getMeteoParAdresse(localisation);

                Platform.runLater(() -> {
                    Stage stageMeteo = new Stage();
                    stageMeteo.setTitle("🌤️ Météo - " + localisation);

                    VBox vbox = new VBox(15);
                    vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

                    // Titre
                    Label lblTitre = new Label("🌤️ Météo - " + localisation);
                    lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

                    // Emoji + condition
                    Label lblCondition = new Label(meteo.emoji + "  " + meteo.condition);
                    lblCondition.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

                    // Grille des données météo
                    GridPane grid = new GridPane();
                    grid.setHgap(25);
                    grid.setVgap(10);
                    grid.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10;");

                    ajouterLigne(grid, 0, "🌡️ Température",    meteo.temperature + " °C");
                    ajouterLigne(grid, 1, "💧 Humidité",        meteo.humidite + " %");
                    ajouterLigne(grid, 2, "💨 Vent",            meteo.vent + " km/h");
                    ajouterLigne(grid, 3, "🌧️ Précipitations",  meteo.precipitation + " mm");
                    ajouterLigne(grid, 4, "📍 Coordonnées",
                            String.format("%.4f, %.4f", meteo.latitude, meteo.longitude));

                    // Conseil arrosage agricole
                    Label lblConseil = new Label("💡 " + meteo.conseilArrosage);
                    lblConseil.setStyle(
                            "-fx-font-size: 13px; -fx-text-fill: #2D5A27; " +
                                    "-fx-background-color: #A8C69F; -fx-padding: 12; " +
                                    "-fx-background-radius: 8;");
                    lblConseil.setWrapText(true);
                    lblConseil.setMaxWidth(420);

                    vbox.getChildren().addAll(lblTitre, lblCondition, grid, lblConseil);

                    stageMeteo.setScene(new Scene(vbox, 470, 400));
                    stageMeteo.show();
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert("❌ Erreur Météo",
                                "Impossible de récupérer la météo :\n" + e.getMessage(),
                                Alert.AlertType.ERROR));
            }
        }).start();
    }

    // Utilitaire grille
    private void ajouterLigne(GridPane grid, int row, String label, String valeur) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 13px;");
        Label val = new Label(valeur);
        val.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // ============================================================
    // STATISTIQUES EN PIE CHART
    // ============================================================
    @FXML
    public void afficherStatistiques(ActionEvent event) {
        Map<String, Integer> repartition = ts.getRepartitionTypeSol();
        Map<String, Object>  stats       = ts.getStatistiques();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : repartition.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Répartition par Type de Sol");
        pieChart.setLabelsVisible(true);
        pieChart.setPrefSize(500, 380);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        Text txtStats = new Text(sb.toString());
        txtStats.setFont(Font.font("System", 13));

        VBox vbox = new VBox(15, pieChart, txtStats);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6;");

        Stage stageChart = new Stage();
        stageChart.setTitle("📊 Statistiques des Terrains");
        stageChart.setScene(new Scene(vbox, 560, 600));
        stageChart.show();
    }

    // ============================================================
    // CERTIFICAT PDF
    // ============================================================
    @FXML
    public void exporterCertificat(ActionEvent event) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();
        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain pour générer son certificat.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Certificat PDF");
        fileChooser.setInitialFileName("certificat_terrain_" +
                terrainSelectionne.getNom_terrain().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        Stage stage = (Stage) tableTerrains.getScene().getWindow();
        java.io.File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        // ── RÉCUPÉRER LA MÉTÉO EN TEMPS RÉEL ──
        ApiMeteoService.MeteoResult meteo = null;
        try {
            ApiMeteoService apiService = new ApiMeteoService();
            String localisation = (terrainSelectionne.getLocalisation() != null
                    && !terrainSelectionne.getLocalisation().isEmpty())
                    ? terrainSelectionne.getLocalisation()
                    : "Tunis, Tunisie";
            meteo = apiService.getMeteoParAdresse(localisation);
        } catch (Exception e) {
            System.out.println("Météo non disponible : " + e.getMessage());
            // On continue sans météo si l'API est indisponible
        }

        try {
            PdfWriter writer   = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document  = new Document(pdfDoc);

            // ── COULEURS ──
            DeviceRgb vertFonce = new DeviceRgb(45, 90, 39);
            DeviceRgb vertClair = new DeviceRgb(168, 198, 159);
            DeviceRgb beige     = new DeviceRgb(252, 248, 230);
            DeviceRgb gris      = new DeviceRgb(44, 62, 80);
            DeviceRgb bleuCiel  = new DeviceRgb(52, 152, 219);
            DeviceRgb orange    = new DeviceRgb(230, 126, 34);

            // ── BORDURE DÉCORATIVE ──
            Table borderTable = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth();
            Cell borderCell = new Cell()
                    .setBorder(new SolidBorder(vertFonce, 4))
                    .setBackgroundColor(beige).setPadding(28);

            // ── EN-TÊTE ──
            borderCell.add(new Paragraph("🌿 AGROFLOW")
                    .setFontSize(13).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setBold());

            borderCell.add(new Paragraph("CERTIFICAT DE TERRAIN AGRICOLE")
                    .setFontSize(24).setFontColor(vertFonce).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(8).setMarginBottom(5));

            // Ligne déco
            Table ligneDeco = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(15);
            ligneDeco.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneDeco);

            // Intro
            borderCell.add(new Paragraph(
                    "Il est certifié que le terrain agricole suivant est enregistré " +
                            "et validé dans le système de gestion AGROFLOW.")
                    .setFontSize(11).setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER).setItalic().setMarginBottom(18));

            // Nom du terrain
            borderCell.add(new Paragraph(terrainSelectionne.getNom_terrain().toUpperCase())
                    .setFontSize(26).setFontColor(vertFonce).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(18));

            // ── TABLEAU INFOS TERRAIN ──
            borderCell.add(new Paragraph("Informations du Terrain")
                    .setFontSize(13).setBold().setFontColor(vertFonce).setMarginBottom(6));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth().setMarginBottom(18);

            String[][] infos = {
                    {"📍 Localisation",  terrainSelectionne.getLocalisation()},
                    {"🪨 Type de sol",   terrainSelectionne.getType_sol()},
                    {"📐 Surface",       terrainSelectionne.getSurface() + " m²"},
                    {"🧪 pH du sol",     String.valueOf(terrainSelectionne.getP_h())},
                    {"🆔 Identifiant",   "TRN-" + String.format("%04d", terrainSelectionne.getId_terrain())},
            };

            for (String[] info : infos) {
                infoTable.addCell(new Cell()
                        .add(new Paragraph(info[0]).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(vertClair).setPadding(7)
                        .setBorder(new SolidBorder(vertFonce, 1)));
                infoTable.addCell(new Cell()
                        .add(new Paragraph(info[1]).setFontColor(gris))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
            }
            borderCell.add(infoTable);

            // Analyse pH
            float ph = terrainSelectionne.getP_h();
            String qualitePH = ph < 6.0f
                    ? "Sol acide - Convient aux myrtilles, pommes de terre"
                    : ph <= 7.0f
                    ? "Sol neutre - Idéal pour la majorité des cultures"
                    : "Sol basique - Convient aux asperges, choux";

            borderCell.add(new Paragraph("Analyse : " + qualitePH)
                    .setFontSize(10).setFontColor(gris).setItalic()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // ── SECTION MÉTÉO (si disponible) ──
            if (meteo != null) {

                // Ligne séparatrice bleue
                Table ligneMeteo = new Table(UnitValue.createPercentArray(new float[]{100}))
                        .useAllAvailableWidth().setMarginBottom(10);
                ligneMeteo.addCell(new Cell().setHeight(2).setBackgroundColor(bleuCiel).setBorder(Border.NO_BORDER));
                borderCell.add(ligneMeteo);

                // Titre météo
                borderCell.add(new Paragraph("🌤️ Conditions Météo du Jour")
                        .setFontSize(13).setBold().setFontColor(bleuCiel).setMarginBottom(6));

                // Tableau météo (2 colonnes x 2 rangées)
                Table meteoTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                        .useAllAvailableWidth().setMarginBottom(10);

                // Condition
                meteoTable.addCell(new Cell()
                        .add(new Paragraph("Condition").setBold().setFontColor(bleuCiel).setFontSize(10))
                        .setBackgroundColor(new DeviceRgb(235, 245, 255))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));
                meteoTable.addCell(new Cell()
                        .add(new Paragraph(meteo.emoji + " " + meteo.condition).setFontColor(gris).setFontSize(10))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));

                // Température
                meteoTable.addCell(new Cell()
                        .add(new Paragraph("Température").setBold().setFontColor(bleuCiel).setFontSize(10))
                        .setBackgroundColor(new DeviceRgb(235, 245, 255))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));
                meteoTable.addCell(new Cell()
                        .add(new Paragraph(meteo.temperature + " °C").setFontColor(gris).setFontSize(10))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));

                // Humidité
                meteoTable.addCell(new Cell()
                        .add(new Paragraph("Humidité").setBold().setFontColor(bleuCiel).setFontSize(10))
                        .setBackgroundColor(new DeviceRgb(235, 245, 255))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));
                meteoTable.addCell(new Cell()
                        .add(new Paragraph(meteo.humidite + " %").setFontColor(gris).setFontSize(10))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));

                // Vent
                meteoTable.addCell(new Cell()
                        .add(new Paragraph("Vent").setBold().setFontColor(bleuCiel).setFontSize(10))
                        .setBackgroundColor(new DeviceRgb(235, 245, 255))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));
                meteoTable.addCell(new Cell()
                        .add(new Paragraph(meteo.vent + " km/h").setFontColor(gris).setFontSize(10))
                        .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));

                borderCell.add(meteoTable);

                // Conseil arrosage
                DeviceRgb couleurConseil = meteo.temperature > 30
                        ? orange   // orange si chaleur
                        : vertFonce; // vert si normal

                borderCell.add(new Paragraph("💡 " + meteo.conseilArrosage)
                        .setFontSize(10).setFontColor(couleurConseil).setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(15));
            }

            // ── PIED DE PAGE ──
            Table ligneFin = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(10);
            ligneFin.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneFin);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            borderCell.add(new Paragraph("Délivré le : " + dateStr)
                    .setFontSize(10).setFontColor(gris).setTextAlignment(TextAlignment.RIGHT));

            borderCell.add(new Paragraph("AGROFLOW - Système de Gestion Agricole")
                    .setFontSize(10).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setItalic());

            borderTable.addCell(borderCell);
            document.add(borderTable);
            document.close();

            showAlert("✅ Certificat généré !",
                    "Le certificat du terrain '" + terrainSelectionne.getNom_terrain() +
                            "' avec météo a été exporté avec succès !",
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("❌ Erreur", "Impossible de générer le certificat : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    public void afficherRecommandation(ActionEvent event) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();

        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain.", Alert.AlertType.WARNING);
            return;
        }

        float ph    = terrainSelectionne.getP_h();
        int score   = ts.calculerScoreSante(terrainSelectionne);
        String desc = ts.getDescriptionScore(score);
        String reco = ts.getRecommandationPlante(ph);

        // ── FENÊTRE DE RÉSULTAT ──
        Stage stageReco = new Stage();
        stageReco.setTitle("🌱 Analyse - " + terrainSelectionne.getNom_terrain());

        VBox vbox = new VBox(15);
        vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        // Titre
        Label lblTitre = new Label("🌱 Analyse du Terrain");
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label lblNom = new Label(terrainSelectionne.getNom_terrain().toUpperCase());
        lblNom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

        // ── SCORE ──
        Label lblScoreTitre = new Label("📊 Score de Santé du Terrain");
        lblScoreTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Barre de progression score
        ProgressBar progressBar = new ProgressBar(score / 100.0);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(25);
        String couleurBarre = score >= 70 ? "#2D5A27" : score >= 50 ? "#F39C12" : "#E74C3C";
        progressBar.setStyle("-fx-accent: " + couleurBarre + ";");

        Label lblScore = new Label(score + " / 100  —  " + desc);
        lblScore.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Détails score
        Label lblDetailScore = new Label(
                "  pH (" + ph + ") : " + (ph >= 6.0f && ph <= 7.0f ? "Optimal ✅" : "À améliorer ⚠️") + "\n" +
                        "  Surface : " + terrainSelectionne.getSurface() + " m²\n" +
                        "  Type sol : " + terrainSelectionne.getType_sol()
        );
        lblDetailScore.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; " +
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8;");

        // ── RECOMMANDATION PLANTE ──
        Label lblRecoTitre = new Label("🌿 Recommandation de Plantes (pH = " + ph + ")");
        lblRecoTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label lblReco = new Label(reco);
        lblReco.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; " +
                "-fx-background-color: #EAF5EA; -fx-padding: 12; -fx-background-radius: 8;");
        lblReco.setWrapText(true);
        lblReco.setMaxWidth(430);

        Separator sep = new Separator();
        sep.setOpacity(0.3);

        vbox.getChildren().addAll(
                lblTitre, lblNom, sep,
                lblScoreTitre, progressBar, lblScore, lblDetailScore,
                new Separator(),
                lblRecoTitre, lblReco
        );

        stageReco.setScene(new Scene(vbox, 480, 500));
        stageReco.show();
    }


    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    public void versModifier(ActionEvent actionEvent) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();
        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain à modifier.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierTerrain.fxml"));
            Parent root = loader.load();
            ModifierTerrainController controller = loader.getController();
            controller.initialiserAvecTerrain(terrainSelectionne);
            Stage stage = (Stage) tableTerrains.getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();
        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain à supprimer.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ Supprimer le terrain '" + terrainSelectionne.getNom_terrain() + "' ?\n\n" +
                        "Cela supprimera aussi toutes ses rotations.",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                ts.supprimerAvecRotations(terrainSelectionne.getId_terrain());
                chargerDonnees();
                showAlert("Succès", "Terrain supprimé avec succès.", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutTerrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableTerrains.getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void versAccueil(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/acceuilterrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableTerrains.getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}