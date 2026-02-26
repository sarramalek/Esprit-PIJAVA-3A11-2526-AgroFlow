package controllers.Terrains;

import controllers.User.AcceuilAgricole;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import models.Terrains.plante;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User.Personne;
import services.ApiMeteoService;
import services.Terrains.PlanteService;
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

import static controllers.User.GestionAbonnements.showInfo;

public class agricoleplanteController implements Initializable {

    @FXML private Label nbPlantesLabel;
    @FXML private Label nbFaibleEauLabel;
    @FXML private Label nbMoyenEauLabel;
    @FXML private Label nbEleveEauLabel;
    @FXML private VBox plantesContainer;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutBtn;
    private  Personne currentUser;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Label welcomeNameLabel;



    private final PlanteService ps = new PlanteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerDonneesPlantes();
        chargerMeteoGlobale();
    }

    // ============================================================
    // CHARGER PLANTES
    // ============================================================
    private void chargerDonneesPlantes() {
        List<plante> plantes = ps.afficherToutes();

        if (nbPlantesLabel != null)
            nbPlantesLabel.setText(String.valueOf(plantes.size()));

        // Compteurs besoin eau
        int faible = 0, moyen = 0, eleve = 0;
        for (plante p : plantes) {
            if (p.getBesoin_eau() < 1.5f)       faible++;
            else if (p.getBesoin_eau() <= 3.0f)  moyen++;
            else                                  eleve++;
        }
        if (nbFaibleEauLabel != null) nbFaibleEauLabel.setText(faible + " plantes");
        if (nbMoyenEauLabel != null)  nbMoyenEauLabel.setText(moyen + " plantes");
        if (nbEleveEauLabel != null)  nbEleveEauLabel.setText(eleve + " plantes");

        plantesContainer.getChildren().clear();
        if (plantes.isEmpty()) {
            Label lblVide = new Label("Aucune plante enregistrée.");
            lblVide.setStyle("-fx-font-size: 13px; -fx-text-fill: #95A5A6;");
            plantesContainer.getChildren().add(lblVide);
            return;
        }
        for (plante p : plantes)
            plantesContainer.getChildren().add(creerCartePlante(p));
    }

    // ============================================================
    // CARTE PLANTE avec Boutons PDF / Analyse / Arrosage
    // ============================================================
    private VBox creerCartePlante(plante p) {
        VBox carte = new VBox(10);
        carte.setStyle("-fx-background-color: #F8FFF8; -fx-padding: 15; " +
                "-fx-background-radius: 12; -fx-border-color: #A8C69F; " +
                "-fx-border-radius: 12; -fx-border-width: 1;");

        // ── Ligne 1 : Emoji + Nom + Variété ──
        HBox ligne1 = new HBox(10);
        ligne1.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblEmoji = new Label("🌱");
        lblEmoji.setStyle("-fx-font-size: 22px;");
        VBox infos = new VBox(3);
        Label lblNom = new Label(p.getNom_p());
        lblNom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");
        Label lblVariete = new Label("🌿 Variété : " + p.getVariete());
        lblVariete.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
        infos.getChildren().addAll(lblNom, lblVariete);
        ligne1.getChildren().addAll(lblEmoji, infos);

        // ── Ligne 2 : Tags ──
        HBox ligne2 = new HBox(10);
        ligne2.setStyle("-fx-alignment: CENTER_LEFT;");
        String couleurEau = p.getBesoin_eau() > 3.0f ? "#2E86C1" : p.getBesoin_eau() >= 1.5f ? "#27AE60" : "#F39C12";
        String niveauEau  = p.getBesoin_eau() > 3.0f ? "💧💧 Élevé" : p.getBesoin_eau() >= 1.5f ? "💧 Moyen" : "🌵 Faible";
        ligne2.getChildren().addAll(
                creerTag("💧 " + p.getBesoin_eau() + " L/j", couleurEau, "#FFFFFF"),
                creerTag("🔄 " + p.getCycle_jours() + " jours", "#EAF5EA", "#2D5A27"),
                creerTag(niveauEau, "#F0F0F0", "#555555")
        );

        // ── Ligne 3 : Barre cycle ──
        int maxCycle = 365;
        double ratio = Math.min(p.getCycle_jours() / (double) maxCycle, 1.0);
        ProgressBar progressBar = new ProgressBar(ratio);
        progressBar.setPrefWidth(350);
        progressBar.setPrefHeight(10);
        String couleurCycle = p.getCycle_jours() < 60 ? "#27AE60"
                : p.getCycle_jours() < 120 ? "#F39C12" : "#E74C3C";
        progressBar.setStyle("-fx-accent: " + couleurCycle + ";");
        Label lblCycle = new Label("Cycle : " + p.getCycle_jours() + " jours  —  "
                + (p.getCycle_jours() < 60 ? "Court" : p.getCycle_jours() < 120 ? "Moyen" : "Long"));
        lblCycle.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        // ── Ligne 4 : Boutons ──
        HBox boutons = new HBox(10);
        boutons.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 0 0 0;");

        Button btnPDF = creerBouton("📄  Fiche PDF",
                "linear-gradient(to right, #1A5276, #2E86C1)");
        btnPDF.setOnAction(e -> exporterFichePlante(p));

        Button btnStats = creerBouton("📊  Analyse",
                "linear-gradient(to right, #4A235A, #7D3C98)");
        btnStats.setOnAction(e -> afficherAnalysePlante(p));

        Button btnArrosage = creerBouton("💧  Arrosage",
                "linear-gradient(to right, #0E6655, #1ABC9C)");
        btnArrosage.setOnAction(e -> afficherRecommandationArrosage(p));

        boutons.getChildren().addAll(btnPDF, btnStats, btnArrosage);
        carte.getChildren().addAll(ligne1, ligne2, progressBar, lblCycle, boutons);
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
    // EXPORT FICHE PDF PAR PLANTE
    // ============================================================
    private void exporterFichePlante(plante p) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la Fiche PDF");
        fileChooser.setInitialFileName("fiche_plante_"
                + p.getNom_p().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Stage stage = (Stage) plantesContainer.getScene().getWindow();
        java.io.File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        // Récupérer météo pour conseil arrosage
        new Thread(() -> {
            ApiMeteoService.MeteoResult meteo = null;
            try {
                meteo = new ApiMeteoService().getMeteoParAdresse("Tunis, Tunisie");
            } catch (Exception ex) {
                System.out.println("Météo non dispo pour PDF : " + ex.getMessage());
            }
            final ApiMeteoService.MeteoResult meteoFinal = meteo;
            Platform.runLater(() -> genererFichePDF(p, meteoFinal, fichier));
        }).start();
    }

    private void genererFichePDF(plante p, ApiMeteoService.MeteoResult meteo, java.io.File fichier) {
        try {
            PdfWriter writer   = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document  = new Document(pdfDoc);

            DeviceRgb vertFonce = new DeviceRgb(45, 90, 39);
            DeviceRgb vertClair = new DeviceRgb(168, 198, 159);
            DeviceRgb gris      = new DeviceRgb(44, 62, 80);
            DeviceRgb bleu      = new DeviceRgb(41, 128, 185);
            DeviceRgb orange    = new DeviceRgb(230, 126, 34);
            DeviceRgb beige     = new DeviceRgb(252, 248, 230);
            DeviceRgb bleuCiel  = new DeviceRgb(52, 152, 219);

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
            borderCell.add(new Paragraph("FICHE TECHNIQUE DE PLANTE")
                    .setFontSize(22).setFontColor(vertFonce).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

            // Ligne déco
            Table ligneH = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(15);
            ligneH.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneH);

            // Nom plante
            borderCell.add(new Paragraph(p.getNom_p().toUpperCase())
                    .setFontSize(20).setFontColor(vertFonce).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
            borderCell.add(new Paragraph("Variété : " + p.getVariete())
                    .setFontSize(13).setFontColor(gris).setItalic()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // Infos plante
            borderCell.add(new Paragraph("Informations de la Plante")
                    .setFontSize(13).setBold().setFontColor(vertFonce).setMarginBottom(6));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth().setMarginBottom(15);
            DeviceRgb couleurEau = p.getBesoin_eau() > 3.0f ? orange : bleu;
            String niveauEau = p.getBesoin_eau() > 3.0f ? "Élevé (> 3L)"
                    : p.getBesoin_eau() >= 1.5f ? "Moyen (1.5–3L)" : "Faible (< 1.5L)";
            String dureeCycle = p.getCycle_jours() < 60 ? "Court (< 60j)"
                    : p.getCycle_jours() < 120 ? "Moyen (60–120j)" : "Long (> 120j)";

            String[][] infos = {
                    {"🌱 Nom",           p.getNom_p()},
                    {"🌿 Variété",       p.getVariete()},
                    {"💧 Besoin en eau", p.getBesoin_eau() + " L/jour  —  " + niveauEau},
                    {"🔄 Cycle",         p.getCycle_jours() + " jours  —  " + dureeCycle},
                    {"🆔 Identifiant",   "PLT-" + String.format("%04d", p.getId_plante())}
            };
            for (String[] info : infos) {
                infoTable.addCell(new Cell()
                        .add(new Paragraph(info[0]).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(vertClair).setPadding(7)
                        .setBorder(new SolidBorder(vertFonce, 1)));
                Cell valCell = new Cell()
                        .add(new Paragraph(info[1]).setFontColor(gris))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1));
                if (info[0].contains("eau")) valCell.setFontColor(couleurEau);
                infoTable.addCell(valCell);
            }
            borderCell.add(infoTable);

            // Conseil culture
            String conseilCulture = p.getBesoin_eau() > 3.0f
                    ? "⚠️ Plante gourmande en eau — Arrosage fréquent requis"
                    : p.getBesoin_eau() >= 1.5f
                    ? "✅ Besoin en eau modéré — Arrosage standard recommandé"
                    : "🌵 Plante économe en eau — Peu d'arrosage nécessaire";
            borderCell.add(new Paragraph(conseilCulture)
                    .setFontSize(11).setBold()
                    .setFontColor(p.getBesoin_eau() > 3.0f ? orange : vertFonce)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // Météo + Recommandation arrosage
            if (meteo != null) {
                Table ligneMeteo = new Table(UnitValue.createPercentArray(new float[]{100}))
                        .useAllAvailableWidth().setMarginBottom(10);
                ligneMeteo.addCell(new Cell().setHeight(2)
                        .setBackgroundColor(bleuCiel).setBorder(Border.NO_BORDER));
                borderCell.add(ligneMeteo);

                borderCell.add(new Paragraph("🌤️ Conditions Météo & Conseil Arrosage")
                        .setFontSize(13).setBold().setFontColor(bleuCiel).setMarginBottom(6));

                Table meteoTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                        .useAllAvailableWidth().setMarginBottom(10);
                DeviceRgb bleuClair = new DeviceRgb(235, 245, 255);
                String[][] meteoData = {
                        {"Condition",     meteo.emoji + " " + meteo.condition},
                        {"Température",   meteo.temperature + " °C"},
                        {"Humidité",      meteo.humidite + " %"},
                        {"Précipitations", meteo.precipitation + " mm"}
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

                String reco = ps.getRecommandationArrosage(p,
                        meteo.temperature, meteo.humidite, meteo.precipitation);
                borderCell.add(new Paragraph("💡 " + reco)
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
                    "Fiche de '" + p.getNom_p() + "' exportée avec succès !");

        } catch (Exception e) {
            showAlert("❌ Erreur PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // ANALYSE PLANTE : Besoin eau + Cycle + PieChart
    // ============================================================
    private void afficherAnalysePlante(plante p) {
        Stage stage = new Stage();
        stage.setTitle("📊 Analyse - " + p.getNom_p());

        VBox vbox = new VBox(14);
        vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        Label lblTitre = new Label("📊 Analyse de la Plante");
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label lblNom = new Label(p.getNom_p().toUpperCase() + "  •  " + p.getVariete());
        lblNom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

        Separator sep1 = new Separator(); sep1.setOpacity(0.3);

        // ── Barre besoin eau ──
        Label lblEauTitre = new Label("💧 Besoin en Eau");
        lblEauTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2980B9;");

        double ratioEau = Math.min(p.getBesoin_eau() / 6.0, 1.0);
        ProgressBar barEau = new ProgressBar(ratioEau);
        barEau.setPrefWidth(420); barEau.setPrefHeight(18);
        String cEau = p.getBesoin_eau() > 3.0f ? "#2E86C1" : p.getBesoin_eau() >= 1.5f ? "#27AE60" : "#F39C12";
        barEau.setStyle("-fx-accent: " + cEau + ";");
        String niveauEau = p.getBesoin_eau() > 3.0f ? "Élevé" : p.getBesoin_eau() >= 1.5f ? "Moyen" : "Faible";
        Label lblEau = new Label(p.getBesoin_eau() + " L/jour  —  Niveau : " + niveauEau);
        lblEau.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; -fx-font-weight: bold;");

        // ── Barre cycle ──
        Label lblCycleTitre = new Label("🔄 Cycle de Croissance");
        lblCycleTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #8E44AD;");

        double ratioCycle = Math.min(p.getCycle_jours() / 365.0, 1.0);
        ProgressBar barCycle = new ProgressBar(ratioCycle);
        barCycle.setPrefWidth(420); barCycle.setPrefHeight(18);
        String cCycle = p.getCycle_jours() < 60 ? "#27AE60" : p.getCycle_jours() < 120 ? "#F39C12" : "#E74C3C";
        barCycle.setStyle("-fx-accent: " + cCycle + ";");
        String dureeCycle = p.getCycle_jours() < 60 ? "Court" : p.getCycle_jours() < 120 ? "Moyen" : "Long";
        Label lblCycle = new Label(p.getCycle_jours() + " jours  —  Cycle : " + dureeCycle);
        lblCycle.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; -fx-font-weight: bold;");

        // ── PieChart besoin eau catégorie ──
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Faible (<1.5L)", p.getBesoin_eau() < 1.5f ? 1 : 0),
                new PieChart.Data("Moyen (1.5–3L)", (p.getBesoin_eau() >= 1.5f && p.getBesoin_eau() <= 3f) ? 1 : 0),
                new PieChart.Data("Élevé (>3L)",    p.getBesoin_eau() > 3.0f ? 1 : 0)
        );
        pieData.removeIf(d -> d.getPieValue() == 0);
        PieChart pie = new PieChart(pieData);
        pie.setTitle("Catégorie Besoin Eau");
        pie.setPrefSize(280, 200);
        pie.setLabelsVisible(true);

        Separator sep2 = new Separator(); sep2.setOpacity(0.3);

        // ── Infos ──
        Label lblDetail = new Label(
                "  🌱 Nom : " + p.getNom_p() + "\n" +
                        "  🌿 Variété : " + p.getVariete() + "\n" +
                        "  💧 Besoin eau : " + p.getBesoin_eau() + " L/jour\n" +
                        "  🔄 Cycle : " + p.getCycle_jours() + " jours"
        );
        lblDetail.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; " +
                "-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8;");

        vbox.getChildren().addAll(
                lblTitre, lblNom, sep1,
                lblEauTitre, barEau, lblEau,
                lblCycleTitre, barCycle, lblCycle, pie,
                sep2, lblDetail
        );

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        stage.setScene(new Scene(scroll, 500, 600));
        stage.show();
    }

    // ============================================================
    // RECOMMANDATION ARROSAGE PAR PLANTE
    // ============================================================
    private void afficherRecommandationArrosage(plante p) {
        new Thread(() -> {
            try {
                ApiMeteoService api = new ApiMeteoService();
                ApiMeteoService.MeteoResult meteo = api.getMeteoParAdresse("Tunis, Tunisie");
                String recommandation = ps.getRecommandationArrosage(
                        p, meteo.temperature, meteo.humidite, meteo.precipitation);

                Platform.runLater(() -> {
                    Stage st = new Stage();
                    st.setTitle("💧 Arrosage - " + p.getNom_p());
                    VBox vbox = new VBox(14);
                    vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

                    Label lblTitre = new Label("💧 Recommandation d'Arrosage");
                    lblTitre.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
                    Label lblNom = new Label("🌱 " + p.getNom_p().toUpperCase() + "  •  " + p.getVariete());
                    lblNom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

                    Separator sep1 = new Separator(); sep1.setOpacity(0.3);

                    Label lblInfoPlante = new Label(
                            "📋 Besoin en eau : " + p.getBesoin_eau() + " L\n" +
                                    "🔄 Cycle : " + p.getCycle_jours() + " jours");
                    lblInfoPlante.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; " +
                            "-fx-background-color: white; -fx-padding: 10; " +
                            "-fx-background-radius: 8; -fx-border-color: #D5E8D5; -fx-border-radius: 8;");

                    Label lblMeteoTitre = new Label("🌤️ Météo Actuelle");
                    lblMeteoTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2980B9;");
                    Label lblMeteo = new Label(
                            meteo.emoji + "  " + meteo.condition + "\n" +
                                    "🌡️ " + meteo.temperature + "°C   " +
                                    "💧 " + meteo.humidite + "%   " +
                                    "🌧️ " + meteo.precipitation + " mm");
                    lblMeteo.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; " +
                            "-fx-background-color: #EBF5FB; -fx-padding: 12; -fx-background-radius: 8;");

                    Separator sep2 = new Separator(); sep2.setOpacity(0.3);

                    Label lblRecoTitre = new Label("💡 Recommandation");
                    lblRecoTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");
                    Label lblReco = new Label(recommandation);
                    lblReco.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; " +
                            "-fx-background-color: #EAF5EA; -fx-padding: 14; " +
                            "-fx-background-radius: 10; -fx-border-color: #A8C69F; -fx-border-radius: 10;");
                    lblReco.setWrapText(true);
                    lblReco.setMaxWidth(430);

                    vbox.getChildren().addAll(
                            lblTitre, lblNom, sep1,
                            lblInfoPlante, lblMeteoTitre, lblMeteo,
                            sep2, lblRecoTitre, lblReco
                    );
                    st.setScene(new Scene(vbox, 480, 490));
                    st.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("❌ Erreur Météo", e.getMessage()));
            }
        }).start();
    }

    // ============================================================
    // STATISTIQUES GLOBALES
    // ============================================================
    @FXML
    public void afficherStatistiquesGlobales(ActionEvent event) {
        List<plante> plantes = ps.afficherToutes();
        Map<String, Object> stats = ps.getStatistiques();

        // PieChart besoin eau
        int faible = 0, moyen = 0, eleve = 0;
        for (plante p : plantes) {
            if (p.getBesoin_eau() < 1.5f)       faible++;
            else if (p.getBesoin_eau() <= 3.0f)  moyen++;
            else                                  eleve++;
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (faible > 0) pieData.add(new PieChart.Data("Faible (" + faible + ")", faible));
        if (moyen > 0)  pieData.add(new PieChart.Data("Moyen (" + moyen + ")", moyen));
        if (eleve > 0)  pieData.add(new PieChart.Data("Élevé (" + eleve + ")", eleve));

        PieChart pie = new PieChart(pieData);
        pie.setTitle("Répartition Besoin en Eau");
        pie.setLabelsVisible(true);
        pie.setPrefSize(380, 280);

        // BarChart cycle
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis   = new NumberAxis();
        xAxis.setLabel("Plante");
        yAxis.setLabel("Cycle (jours)");
        BarChart<String, Number> bar = new BarChart<>(xAxis, yAxis);
        bar.setTitle("Cycle de Croissance");
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Jours");
        for (plante p : plantes)
            serie.getData().add(new XYChart.Data<>(p.getNom_p(), p.getCycle_jours()));
        bar.getData().add(serie);
        bar.setPrefSize(380, 280);

        // Stats texte
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stats.entrySet())
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        Label txtStats = new Label(sb.toString());
        txtStats.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; " +
                "-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8;");

        HBox charts = new HBox(20, pie, bar);
        charts.setStyle("-fx-alignment: center;");
        VBox vbox = new VBox(15, charts, txtStats);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        Stage st = new Stage();
        st.setTitle("📊 Statistiques Globales des Plantes");
        st.setScene(new Scene(vbox, 820, 540));
        st.show();
    }

    // ============================================================
    // MÉTÉO AU DÉMARRAGE
    // ============================================================
    private void chargerMeteoGlobale() {
        new Thread(() -> {
            try {
                ApiMeteoService.MeteoResult meteo =
                        new ApiMeteoService().getMeteoParAdresse("Tunis, Tunisie");
                Platform.runLater(() -> {
                    Label lbl = new Label("🌤️ Météo du jour : " + meteo.emoji + " " + meteo.condition
                            + "  |  🌡️ " + meteo.temperature + "°C"
                            + "  |  💧 " + meteo.humidite + "%"
                            + "  |  💡 " + meteo.conseilArrosage);
                    lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #2D5A27; " +
                            "-fx-background-color: #EAF5EA; -fx-padding: 10; -fx-background-radius: 8;");
                    lbl.setWrapText(true);
                    plantesContainer.getChildren().add(0, lbl);
                    plantesContainer.getChildren().add(1, new Separator());
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
    public void versPlantes(ActionEvent event) {
        naviguer("//AffichagePlante.fxml", event);
    }

    @FXML
    public void versAccueilAgricole(ActionEvent event) {
        naviguer("/TerrainsInterface/acceuilagricoleterrain.fxml", event);
    }

    private void naviguer(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
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

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML private void handleDashboard(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains()  { System.out.println("🌾 Current Page "); }
    @FXML private void handleMesAnimaux()   { System.out.println("🐄 Animaux..."); }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel()  { System.out.println("🚜 Matériel..."); }
    @FXML private void handleMonProfil(MouseEvent event )    {navigateTo(event,"/UsersInterface/ProfilEmplye.fxml","Mon Profil");  }

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
                    stage.setScene(new Scene(root, 1500, 700));
                    stage.setTitle("AgroFlow - Connexion");
                    stage.setMaximized(true);
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

    // Ajouter cette méthode handleAPropos()
    @FXML
    private void handleAPropos(MouseEvent event) {
        navigateTo(event,"/UsersInterface/ProfilAgricole.fxml","ddd");
    }
    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

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