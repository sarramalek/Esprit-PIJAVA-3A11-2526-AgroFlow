package controllers.Terrains;

import controllers.User.AcceuilAgricole;
import controllers.User.ProfilEmploye;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import models.Terrains.terrain;
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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User.Personne;
import services.ApiMeteoService;
import services.Terrains.TerrainService;
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
import utils.SessionManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static controllers.User.GestionAbonnements.showInfo;

public class agricoleterrainController implements Initializable {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label welcomeNameLabel;
    @FXML private Label nbTerrainsLabel;
    @FXML private Label nbAnimauxLabel;
    @FXML private Label nbStocksLabel;
    @FXML private Label nbEquipementsLabel;
    @FXML private VBox abonnementsContainer;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button logoutBtn,gestionBtn;
    private  Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;

    private final TerrainService ts = new TerrainService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (nbAnimauxLabel != null)     nbAnimauxLabel.setText("0");
        if (nbStocksLabel != null)      nbStocksLabel.setText("0");
        if (nbEquipementsLabel != null) nbEquipementsLabel.setText("0");
        chargerDonneesTerrains();
        chargerMeteo();
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        System.out.println("✓ AcceuilAgricole Controller initialisé");



        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
    }

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



    // ============================================================
    // CHARGER TERRAINS
    // ============================================================
    private void chargerDonneesTerrains() {
        List<terrain> terrains = ts.afficherTous();
        if (nbTerrainsLabel != null)
            nbTerrainsLabel.setText(String.valueOf(terrains.size()));

        abonnementsContainer.getChildren().clear();
        if (terrains.isEmpty()) {
            Label lblVide = new Label("Aucun terrain enregistré.");
            lblVide.setStyle("-fx-font-size: 13px; -fx-text-fill: #95A5A6;");
            abonnementsContainer.getChildren().add(lblVide);
            return;
        }
        for (terrain t : terrains)
            abonnementsContainer.getChildren().add(creerCarteTerrain(t));
    }

    // ============================================================
    // CARTE TERRAIN avec Score + Boutons PDF / Stats / Météo
    // ============================================================
    private VBox creerCarteTerrain(terrain t) {
        VBox carte = new VBox(10);
        carte.setStyle("-fx-background-color: #F8FFF8; -fx-padding: 15; " +
                "-fx-background-radius: 12; -fx-border-color: #A8C69F; " +
                "-fx-border-radius: 12; -fx-border-width: 1;");

        // ── Ligne 1 : Emoji + Nom + Localisation ──
        HBox ligne1 = new HBox(10);
        ligne1.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblEmoji = new Label("🌍");
        lblEmoji.setStyle("-fx-font-size: 22px;");
        VBox infos = new VBox(3);
        Label lblNom = new Label(t.getNom_terrain());
        lblNom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");
        Label lblLoc = new Label("📍 " + (t.getLocalisation() != null ? t.getLocalisation() : "Non définie"));
        lblLoc.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
        infos.getChildren().addAll(lblNom, lblLoc);
        ligne1.getChildren().addAll(lblEmoji, infos);

        // ── Ligne 2 : Tags ──
        HBox ligne2 = new HBox(10);
        ligne2.setStyle("-fx-alignment: CENTER_LEFT;");
        ligne2.getChildren().addAll(
                creerTag("📐 " + t.getSurface() + " m²", "#EAF5EA", "#2D5A27"),
                creerTag("🪨 " + t.getType_sol(), "#F0F0F0", "#555555"),
                creerTag("🧪 pH " + t.getP_h(), getPHColor(t.getP_h()), "#FFFFFF")
        );

        // ── Ligne 3 : Barre de score ──
        int score = ts.calculerScoreSante(t);
        String couleurBarre = score >= 70 ? "#2D5A27" : score >= 50 ? "#F39C12" : "#E74C3C";
        ProgressBar progressBar = new ProgressBar(score / 100.0);
        progressBar.setPrefWidth(350);
        progressBar.setPrefHeight(12);
        progressBar.setStyle("-fx-accent: " + couleurBarre + ";");
        Label lblScore = new Label("Score santé : " + score + "/100  —  " + ts.getDescriptionScore(score));
        lblScore.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        // ── Ligne 4 : Boutons ──
        HBox boutons = new HBox(10);
        boutons.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 0 0 0;");

        Button btnPDF = creerBouton("📄  Rapport PDF",
                "linear-gradient(to right, #1A5276, #2E86C1)");
        btnPDF.setOnAction(e -> exporterPDFTerrain(t));

        Button btnStats = creerBouton("📊  Analyse",
                "linear-gradient(to right, #4A235A, #7D3C98)");
        btnStats.setOnAction(e -> afficherAnalyseTerrain(t));

        Button btnMeteo = creerBouton("🌤️  Météo",
                "linear-gradient(to right, #0E6655, #1ABC9C)");
        btnMeteo.setOnAction(e -> afficherMeteoTerrain(t));

        boutons.getChildren().addAll(btnPDF, btnStats, btnMeteo);
        carte.getChildren().addAll(ligne1, ligne2, progressBar, lblScore, boutons);
        return carte;
    }

    private Button creerBouton(String texte, String gradient) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: " + gradient + "; " +
                "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 6 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");
        return btn;
    }

    // ============================================================
    // EXPORT PDF PAR TERRAIN
    // ============================================================
    private void exporterPDFTerrain(terrain t) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Rapport PDF");
        fileChooser.setInitialFileName("rapport_terrain_"
                + t.getNom_terrain().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Stage stage = (Stage) abonnementsContainer.getScene().getWindow();
        java.io.File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        new Thread(() -> {
            ApiMeteoService.MeteoResult meteo = null;
            try {
                ApiMeteoService api = new ApiMeteoService();
                String loc = (t.getLocalisation() != null && !t.getLocalisation().isEmpty())
                        ? t.getLocalisation() : "Tunis, Tunisie";
                meteo = api.getMeteoParAdresse(loc);
            } catch (Exception ex) {
                System.out.println("Météo non dispo pour PDF : " + ex.getMessage());
            }
            final ApiMeteoService.MeteoResult meteoFinal = meteo;
            Platform.runLater(() -> genererPDF(t, meteoFinal, fichier));
        }).start();
    }

    private void genererPDF(terrain t, ApiMeteoService.MeteoResult meteo, java.io.File fichier) {
        try {
            PdfWriter writer   = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document  = new Document(pdfDoc);

            DeviceRgb vertFonce = new DeviceRgb(45, 90, 39);
            DeviceRgb vertClair = new DeviceRgb(168, 198, 159);
            DeviceRgb gris      = new DeviceRgb(44, 62, 80);
            DeviceRgb bleuCiel  = new DeviceRgb(52, 152, 219);
            DeviceRgb beige     = new DeviceRgb(252, 248, 230);
            DeviceRgb orange    = new DeviceRgb(230, 126, 34);

            // Bordure globale
            Table borderTable = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth();
            Cell borderCell = new Cell()
                    .setBorder(new SolidBorder(vertFonce, 4))
                    .setBackgroundColor(beige).setPadding(28);

            // En-tête
            borderCell.add(new Paragraph("🌿 AGROFLOW")
                    .setFontSize(12).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setBold());
            borderCell.add(new Paragraph("RAPPORT DE TERRAIN AGRICOLE")
                    .setFontSize(22).setFontColor(vertFonce).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

            // Ligne déco
            Table ligneH = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(15);
            ligneH.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneH);

            // Nom terrain
            borderCell.add(new Paragraph(t.getNom_terrain().toUpperCase())
                    .setFontSize(20).setFontColor(vertFonce).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(18));

            // Infos terrain
            borderCell.add(new Paragraph("Informations du Terrain")
                    .setFontSize(13).setBold().setFontColor(vertFonce).setMarginBottom(6));
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth().setMarginBottom(15);
            String[][] infos = {
                    {"📍 Localisation", t.getLocalisation()},
                    {"🪨 Type de sol",  t.getType_sol()},
                    {"📐 Surface",      t.getSurface() + " m²"},
                    {"🧪 pH du sol",    String.valueOf(t.getP_h())},
                    {"🆔 Identifiant",  "TRN-" + String.format("%04d", t.getId_terrain())}
            };
            for (String[] info : infos) {
                infoTable.addCell(new Cell()
                        .add(new Paragraph(info[0]).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(vertClair).setPadding(7)
                        .setBorder(new SolidBorder(vertFonce, 1)));
                infoTable.addCell(new Cell()
                        .add(new Paragraph(info[1] != null ? info[1] : "-").setFontColor(gris))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
            }
            borderCell.add(infoTable);

            // Score santé
            int score = ts.calculerScoreSante(t);
            DeviceRgb couleurScore = score >= 70 ? vertFonce
                    : score >= 50 ? orange : new DeviceRgb(231, 76, 60);
            borderCell.add(new Paragraph("Score de Santé : " + score
                    + "/100  —  " + ts.getDescriptionScore(score))
                    .setFontSize(11).setBold().setFontColor(couleurScore)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));

            // Recommandation pH
            String reco = ts.getRecommandationPlante(t.getP_h());
            borderCell.add(new Paragraph("🌿 " + reco)
                    .setFontSize(10).setItalic().setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // Météo
            if (meteo != null) {
                Table ligneMeteo = new Table(UnitValue.createPercentArray(new float[]{100}))
                        .useAllAvailableWidth().setMarginBottom(10);
                ligneMeteo.addCell(new Cell().setHeight(2)
                        .setBackgroundColor(bleuCiel).setBorder(Border.NO_BORDER));
                borderCell.add(ligneMeteo);
                borderCell.add(new Paragraph("🌤️ Conditions Météo du Jour")
                        .setFontSize(13).setBold().setFontColor(bleuCiel).setMarginBottom(6));
                Table meteoTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                        .useAllAvailableWidth().setMarginBottom(10);
                DeviceRgb bleuClair = new DeviceRgb(235, 245, 255);
                String[][] meteoData = {
                        {"Condition",     meteo.emoji + " " + meteo.condition},
                        {"Température",   meteo.temperature + " °C"},
                        {"Humidité",      meteo.humidite + " %"},
                        {"Vent",          meteo.vent + " km/h"}
                };
                for (String[] row : meteoData) {
                    meteoTable.addCell(new Cell()
                            .add(new Paragraph(row[0]).setBold().setFontColor(bleuCiel).setFontSize(10))
                            .setBackgroundColor(bleuClair).setPadding(6)
                            .setBorder(new SolidBorder(bleuCiel, 1)));
                    meteoTable.addCell(new Cell()
                            .add(new Paragraph(row[1]).setFontColor(gris).setFontSize(10))
                            .setPadding(6).setBorder(new SolidBorder(bleuCiel, 1)));
                }
                borderCell.add(meteoTable);
                borderCell.add(new Paragraph("💡 " + meteo.conseilArrosage)
                        .setFontSize(10).setBold().setFontColor(vertFonce)
                        .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));
            }

            // Pied de page
            Table ligneFin = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(8);
            ligneFin.addCell(new Cell().setHeight(3)
                    .setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneFin);
            String dateStr = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            borderCell.add(new Paragraph("Généré le : " + dateStr)
                    .setFontSize(9).setFontColor(gris)
                    .setTextAlignment(TextAlignment.RIGHT));
            borderCell.add(new Paragraph("AGROFLOW - Système de Gestion Agricole")
                    .setFontSize(9).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setItalic());

            borderTable.addCell(borderCell);
            document.add(borderTable);
            document.close();

            showAlert("✅ PDF généré !",
                    "Rapport de '" + t.getNom_terrain() + "' exporté avec succès !");
        } catch (Exception e) {
            showAlert("❌ Erreur PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // ANALYSE TERRAIN : Score + PieChart + Recommandation
    // ============================================================
    private void afficherAnalyseTerrain(terrain t) {
        int score   = ts.calculerScoreSante(t);
        String desc = ts.getDescriptionScore(score);
        String reco = ts.getRecommandationPlante(t.getP_h());

        Stage stage = new Stage();
        stage.setTitle("📊 Analyse - " + t.getNom_terrain());

        VBox vbox = new VBox(15);
        vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        Label lblTitre = new Label("📊 Analyse du Terrain");
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label lblNom = new Label(t.getNom_terrain().toUpperCase());
        lblNom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

        Separator sep1 = new Separator(); sep1.setOpacity(0.3);

        // Score + barre
        Label lblScoreTitre = new Label("📊 Score de Santé du Terrain");
        lblScoreTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        ProgressBar bar = new ProgressBar(score / 100.0);
        bar.setPrefWidth(420); bar.setPrefHeight(22);
        String c = score >= 70 ? "#2D5A27" : score >= 50 ? "#F39C12" : "#E74C3C";
        bar.setStyle("-fx-accent: " + c + ";");
        Label lblScore = new Label(score + " / 100  —  " + desc);
        lblScore.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label lblDetail = new Label(
                "  pH (" + t.getP_h() + ") : " +
                        (t.getP_h() >= 6.0f && t.getP_h() <= 7.0f ? "Optimal ✅" : "À améliorer ⚠️") + "\n" +
                        "  Surface : " + t.getSurface() + " m²\n" +
                        "  Type sol : " + t.getType_sol()
        );
        lblDetail.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; " +
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8;");

        // PieChart pH
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Acide (pH<6)", t.getP_h() < 6 ? 1 : 0),
                new PieChart.Data("Neutre (6-7)", (t.getP_h() >= 6 && t.getP_h() <= 7) ? 1 : 0),
                new PieChart.Data("Basique (>7)", t.getP_h() > 7 ? 1 : 0)
        );
        // Garder uniquement la catégorie du terrain
        pieData.removeIf(d -> d.getPieValue() == 0);
        PieChart pie = new PieChart(pieData);
        pie.setTitle("Catégorie pH");
        pie.setPrefSize(300, 200);
        pie.setLabelsVisible(true);

        Separator sep2 = new Separator(); sep2.setOpacity(0.3);

        // Recommandation
        Label lblRecoTitre = new Label("🌿 Plantes recommandées (pH = " + t.getP_h() + ")");
        lblRecoTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label lblReco = new Label(reco);
        lblReco.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; " +
                "-fx-background-color: #EAF5EA; -fx-padding: 12; -fx-background-radius: 8;");
        lblReco.setWrapText(true);
        lblReco.setMaxWidth(440);

        vbox.getChildren().addAll(
                lblTitre, lblNom, sep1,
                lblScoreTitre, bar, lblScore, lblDetail, pie,
                sep2, lblRecoTitre, lblReco
        );

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        stage.setScene(new Scene(scroll, 500, 620));
        stage.show();
    }

    // ============================================================
    // STATISTIQUES GLOBALES TOUS LES TERRAINS
    // ============================================================
    @FXML
    public void afficherStatistiquesGlobales(ActionEvent event) {
        List<terrain> terrains = ts.afficherTous();
        Map<String, Object> stats = ts.getStatistiques();
        Map<String, Integer> repartition = ts.getRepartitionTypeSol();

        // PieChart type de sol
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : repartition.entrySet())
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));

        PieChart pie = new PieChart(pieData);
        pie.setTitle("Répartition par Type de Sol");
        pie.setLabelsVisible(true);
        pie.setPrefSize(420, 330);

        // Stats texte
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stats.entrySet())
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        Label txtStats = new Label(sb.toString());
        txtStats.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; " +
                "-fx-background-color: white; -fx-padding: 14; -fx-background-radius: 10;");

        // Score moyen
        int scoreMoyen = terrains.stream().mapToInt(ts::calculerScoreSante).sum()
                / Math.max(1, terrains.size());
        Label lblScoreMoyen = new Label("📊 Score moyen de santé : " + scoreMoyen + "/100  —  "
                + ts.getDescriptionScore(scoreMoyen));
        lblScoreMoyen.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-text-fill: #2D5A27; -fx-background-color: #EAF5EA; " +
                "-fx-padding: 10; -fx-background-radius: 8;");

        VBox vbox = new VBox(15, pie, txtStats, lblScoreMoyen);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6; -fx-alignment: center;");
        Stage st = new Stage();
        st.setTitle("📊 Statistiques Globales des Terrains");
        st.setScene(new Scene(vbox, 500, 580));
        st.show();
    }

    // ============================================================
    // MÉTÉO PAR TERRAIN
    // ============================================================
    private void afficherMeteoTerrain(terrain t) {
        String loc = (t.getLocalisation() != null && !t.getLocalisation().isEmpty())
                ? t.getLocalisation() : "Tunis, Tunisie";
        new Thread(() -> {
            try {
                ApiMeteoService api = new ApiMeteoService();
                ApiMeteoService.MeteoResult meteo = api.getMeteoParAdresse(loc);
                Platform.runLater(() -> {
                    Stage st = new Stage();
                    st.setTitle("🌤️ Météo - " + loc);
                    VBox vbox = new VBox(15);
                    vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");
                    Label lblTitre = new Label("🌤️ Météo - " + loc);
                    lblTitre.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
                    Label lblCond = new Label(meteo.emoji + "  " + meteo.condition);
                    lblCond.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");
                    GridPane grid = new GridPane();
                    grid.setHgap(25); grid.setVgap(10);
                    grid.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 10;");
                    String[][] rows = {
                            {"🌡️ Température", meteo.temperature + " °C"},
                            {"💧 Humidité",     meteo.humidite + " %"},
                            {"💨 Vent",         meteo.vent + " km/h"},
                            {"🌧️ Précipitations", meteo.precipitation + " mm"}
                    };
                    for (int i = 0; i < rows.length; i++) {
                        Label lbl = new Label(rows[i][0]);
                        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 13px;");
                        Label val = new Label(rows[i][1]);
                        val.setStyle("-fx-text-fill: #34495E; -fx-font-size: 13px;");
                        grid.add(lbl, 0, i); grid.add(val, 1, i);
                    }
                    Label lblConseil = new Label("💡 " + meteo.conseilArrosage);
                    lblConseil.setStyle("-fx-font-size: 13px; -fx-text-fill: #2D5A27; " +
                            "-fx-background-color: #A8C69F; -fx-padding: 10; -fx-background-radius: 8;");
                    lblConseil.setWrapText(true); lblConseil.setMaxWidth(400);
                    vbox.getChildren().addAll(lblTitre, lblCond, grid, lblConseil);
                    st.setScene(new Scene(vbox, 440, 350));
                    st.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("❌ Erreur Météo", e.getMessage()));
            }
        }).start();
    }

    // ============================================================
    // MÉTÉO AU DÉMARRAGE
    // ============================================================
    private void chargerMeteo() {
        new Thread(() -> {
            try {
                ApiMeteoService api = new ApiMeteoService();
                ApiMeteoService.MeteoResult meteo = api.getMeteoParAdresse("Tunis, Tunisie");
                Platform.runLater(() -> {
                    Label lbl = new Label("🌤️ Météo du jour : " + meteo.emoji + " " + meteo.condition
                            + "  |  🌡️ " + meteo.temperature + "°C"
                            + "  |  💧 " + meteo.humidite + "%"
                            + "  |  💡 " + meteo.conseilArrosage);
                    lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #2D5A27; " +
                            "-fx-background-color: #EAF5EA; -fx-padding: 10; -fx-background-radius: 8;");
                    lbl.setWrapText(true);
                    abonnementsContainer.getChildren().add(0, lbl);
                    abonnementsContainer.getChildren().add(1, new Separator());
                });
            } catch (Exception e) {
                System.out.println("Météo non disponible : " + e.getMessage());
            }
        }).start();
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    public void versTerrains(ActionEvent event) {
        naviguer("/AffichageTerrain.fxml", event);
    }

    @FXML
    public void versAccueilAgricole(ActionEvent event) {
        naviguer("/TerrainsInterface/acceuilagricoleterrain.fxml", event);
    }

    private void naviguer(String fxml, ActionEvent event) {
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
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Navigation", "Impossible d'ouvrir : " + fxml);
        }
    }

    // ── Utilitaires ──
    private Label creerTag(String texte, String bg, String fg) {
        Label tag = new Label(texte);
        tag.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-padding: 4 10; -fx-background-radius: 20;");
        return tag;
    }

    private String getPHColor(float ph) {
        return ph < 6.0f ? "#E67E22" : ph <= 7.0f ? "#27AE60" : "#8E44AD";
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }
    @FXML private void handleDashboard(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains()  { System.out.println("🌾 Current Page "); }
    @FXML private void handleMesAnimaux(MouseEvent event)  { navigateTo(event,"/AnimalsInterface/acceuilagricoleanimaux.fxml","Dashboard"); }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel(MouseEvent mouseEvent)  {        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Materiels");
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
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); }  }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }
    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(AcceuilAgricole.class.getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    // On récupère le Stage et la Scene ACTUELLE
                    Scene scene = stage.getScene();

                    // SOLUTION MIRACLE : On change la racine, pas la scène !
                    scene.setRoot(root);

                    // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                    stage.show();
                    System.out.println("✓ Déconnexion réussie");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true); }
    }
    public void hideGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
    }
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
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
    }
}