package controllers;

import entities.rotation;
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
import services.RotationService;
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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EmployeRotationController implements Initializable {

    @FXML private Label nbRotationsLabel;
    @FXML private Label nbEnCoursLabel;
    @FXML private Label nbTermineesLabel;
    @FXML private Label nbPlanifieesLabel;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private VBox rotationsContainer;

    private final RotationService rs = new RotationService();
    private List<rotation> toutesRotations;

    // ============================================================
    // STATUS : 1 = En cours  |  0 = Terminée
    // ============================================================
    private String getStatutLibelle(int status) { return status == 1 ? "En cours" : "Terminée"; }
    private String getCouleurStatut(int status)  { return status == 1 ? "#27AE60" : "#95A5A6"; }
    private String getEmojiStatut(int status)    { return status == 1 ? "🔄" : "✅"; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerFiltre();
        configurerRecherche();
        chargerDonneesRotations();
    }

    // ============================================================
    // FILTRE + RECHERCHE DYNAMIQUE
    // ============================================================
    private void configurerFiltre() {
        comboFiltre.setItems(FXCollections.observableArrayList("Tous", "En cours", "Terminée"));
        comboFiltre.setValue("Tous");
        comboFiltre.setOnAction(e -> filtrerEtAfficher(txtRecherche.getText(), comboFiltre.getValue()));
    }

    private void configurerRecherche() {
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) ->
                filtrerEtAfficher(newVal, comboFiltre.getValue()));
    }

    private void filtrerEtAfficher(String motCle, String statut) {
        if (toutesRotations == null) return;
        List<rotation> filtrees = toutesRotations.stream()
                .filter(r -> {
                    boolean matchMC = true;
                    if (motCle != null && !motCle.trim().isEmpty()) {
                        String mc = motCle.toLowerCase();
                        matchMC = (r.getNom_plante()  != null && r.getNom_plante().toLowerCase().contains(mc))
                                || (r.getNom_terrain() != null && r.getNom_terrain().toLowerCase().contains(mc))
                                || getStatutLibelle(r.getStatus()).toLowerCase().contains(mc);
                    }
                    boolean matchStatut = true;
                    if (statut != null && !statut.equals("Tous"))
                        matchStatut = statut.equals(getStatutLibelle(r.getStatus()));
                    return matchMC && matchStatut;
                })
                .collect(Collectors.toList());
        afficherRotations(filtrees);
    }

    // ============================================================
    // CHARGER DEPUIS LA BASE
    // ============================================================
    private void chargerDonneesRotations() {
        toutesRotations = rs.afficherToutes();   // ← méthode correcte

        long enCours   = toutesRotations.stream().filter(r -> r.getStatus() == 1).count();
        long terminees = toutesRotations.stream().filter(r -> r.getStatus() == 0).count();

        if (nbRotationsLabel != null)  nbRotationsLabel.setText(String.valueOf(toutesRotations.size()));
        if (nbEnCoursLabel != null)    nbEnCoursLabel.setText(enCours + " rotation(s)");
        if (nbTermineesLabel != null)  nbTermineesLabel.setText(terminees + " rotation(s)");
        if (nbPlanifieesLabel != null) nbPlanifieesLabel.setText("—");

        afficherRotations(toutesRotations);
    }

    private void afficherRotations(List<rotation> rotations) {
        rotationsContainer.getChildren().clear();
        if (rotations.isEmpty()) {
            Label lbl = new Label("Aucune rotation trouvée.");
            lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #95A5A6; -fx-padding: 10;");
            rotationsContainer.getChildren().add(lbl);
            return;
        }
        for (rotation r : rotations)
            rotationsContainer.getChildren().add(creerCarteRotation(r));
    }

    // ============================================================
    // CARTE ROTATION
    // ============================================================
    private VBox creerCarteRotation(rotation r) {
        String couleur  = getCouleurStatut(r.getStatus());
        String emoji    = getEmojiStatut(r.getStatus());
        String libelle  = getStatutLibelle(r.getStatus());
        String bgCarte  = r.getStatus() == 1 ? "#F8FFF8" : "#F8F9FA";

        VBox carte = new VBox(10);
        carte.setStyle("-fx-background-color: " + bgCarte + "; -fx-padding: 15; " +
                "-fx-background-radius: 12; -fx-border-color: " + couleur + "; " +
                "-fx-border-radius: 12; -fx-border-width: 1.5;");

        // Ligne 1 : Emoji + Plante + Terrain + Badge
        HBox ligne1 = new HBox(10);
        ligne1.setStyle("-fx-alignment: CENTER_LEFT;");
        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 22px;");

        VBox infos = new VBox(3);
        Label lblPlante = new Label("🌱 " + (r.getNom_plante() != null ? r.getNom_plante() : "N/A"));
        lblPlante.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");
        Label lblTerrain = new Label("🌍 " + (r.getNom_terrain() != null ? r.getNom_terrain() : "N/A")
                + (r.getVariete_plante() != null ? "   🌿 " + r.getVariete_plante() : ""));
        lblTerrain.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");
        infos.getChildren().addAll(lblPlante, lblTerrain);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatut = new Label(emoji + "  " + libelle);
        lblStatut.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 20;");

        ligne1.getChildren().addAll(lblEmoji, infos, spacer, lblStatut);

        // Ligne 2 : Tags dates
        HBox ligne2 = new HBox(10);
        ligne2.setStyle("-fx-alignment: CENTER_LEFT;");
        ligne2.getChildren().addAll(
                creerTag("📅 Début : " + formatDate(r.getDate_debut_t()), "#EAF5EA", "#2D5A27"),
                creerTag("🏁 Fin : "   + formatDate(r.getDate_fin_t()),   "#FEF9E7", "#B7950B")
        );

        long duree = calculerDuree(r.getDate_debut_t(), r.getDate_fin_t());
        Label lblDuree = new Label("⏱️ Durée : " + (duree > 0 ? duree + " jours" : "N/A"));
        lblDuree.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        // Boutons
        HBox boutons = new HBox(10);
        boutons.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 0 0 0;");

        Button btnPDF = creerBouton("📄  Rapport PDF", "linear-gradient(to right, #1A5276, #2E86C1)");
        btnPDF.setOnAction(e -> exporterRapportRotation(r));

        Button btnAnalyse = creerBouton("📊  Analyse", "linear-gradient(to right, #4A235A, #7D3C98)");
        btnAnalyse.setOnAction(e -> afficherAnalyseRotation(r));

        boutons.getChildren().addAll(btnPDF, btnAnalyse);
        carte.getChildren().addAll(ligne1, ligne2, lblDuree, boutons);
        return carte;
    }

    private Button creerBouton(String texte, String gradient) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: " + gradient + "; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; " +
                "-fx-cursor: hand; -fx-padding: 6 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");
        return btn;
    }

    // ============================================================
    // EXPORT PDF
    // ============================================================
    private void exporterRapportRotation(rotation r) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le Rapport PDF");
        fc.setInitialFileName("rapport_rotation_"
                + (r.getNom_plante() != null ? r.getNom_plante().replace(" ", "_") : "rotation") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Stage stage = (Stage) rotationsContainer.getScene().getWindow();
        java.io.File fichier = fc.showSaveDialog(stage);
        if (fichier == null) return;
        genererPDFRotation(r, fichier);
    }

    private void genererPDFRotation(rotation r, java.io.File fichier) {
        try {
            PdfWriter writer   = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document  = new Document(pdfDoc);

            DeviceRgb vertFonce = new DeviceRgb(45, 90, 39);
            DeviceRgb vertClair = new DeviceRgb(168, 198, 159);
            DeviceRgb gris      = new DeviceRgb(44, 62, 80);
            DeviceRgb beige     = new DeviceRgb(252, 248, 230);
            DeviceRgb rgbStatut = r.getStatus() == 1
                    ? new DeviceRgb(39, 174, 96) : new DeviceRgb(149, 165, 166);

            Table borderTable = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth();
            Cell borderCell = new Cell().setBorder(new SolidBorder(vertFonce, 4))
                    .setBackgroundColor(beige).setPadding(28);

            borderCell.add(new Paragraph("🌿 AGROFLOW").setFontSize(12).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setBold());
            borderCell.add(new Paragraph("RAPPORT DE ROTATION AGRICOLE").setFontSize(22)
                    .setFontColor(vertFonce).setBold().setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

            Table ligneH = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(15);
            ligneH.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneH);

            String libelleStatut = getStatutLibelle(r.getStatus());
            borderCell.add(new Paragraph("Statut : " + libelleStatut).setFontSize(14).setBold()
                    .setFontColor(rgbStatut).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            borderCell.add(new Paragraph("Informations de la Rotation").setFontSize(13)
                    .setBold().setFontColor(vertFonce).setMarginBottom(6));

            long duree = calculerDuree(r.getDate_debut_t(), r.getDate_fin_t());
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth().setMarginBottom(15);
            String[][] infos = {
                    {"🌱 Plante",      r.getNom_plante()},
                    {"🌿 Variété",     r.getVariete_plante()},
                    {"🌍 Terrain",     r.getNom_terrain()},
                    {"📅 Date début",  formatDate(r.getDate_debut_t())},
                    {"🏁 Date fin",    formatDate(r.getDate_fin_t())},
                    {"⏱️ Durée",       duree > 0 ? duree + " jours" : "N/A"},
                    {"📊 Statut",      libelleStatut},
                    {"🆔 Identifiant", "ROT-" + String.format("%04d", r.getId_rotation())}
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

            String analyse = r.getStatus() == 1
                    ? "✅ Rotation active — Culture de " + r.getNom_plante()
                    + " en progression sur " + r.getNom_terrain()
                    : "🏆 Rotation terminée — Cycle de culture complété avec succès !";
            borderCell.add(new Paragraph(analyse).setFontSize(11).setBold().setFontColor(rgbStatut)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            Table ligneFin = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(8);
            ligneFin.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            borderCell.add(ligneFin);

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            borderCell.add(new Paragraph("Généré le : " + dateStr).setFontSize(9).setFontColor(gris)
                    .setTextAlignment(TextAlignment.RIGHT));
            borderCell.add(new Paragraph("AGROFLOW - Système de Gestion Agricole").setFontSize(9)
                    .setFontColor(vertClair).setTextAlignment(TextAlignment.CENTER).setItalic());

            borderTable.addCell(borderCell);
            document.add(borderTable);
            document.close();
            showAlert("✅ PDF généré !", "Rapport rotation '" + r.getNom_plante() + "' exporté !");

        } catch (Exception e) {
            showAlert("❌ Erreur PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // ANALYSE ROTATION
    // ============================================================
    private void afficherAnalyseRotation(rotation r) {
        Stage stage = new Stage();
        stage.setTitle("📊 Analyse - " + r.getNom_plante());

        VBox vbox = new VBox(14);
        vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        Label lblTitre = new Label("📊 Analyse de la Rotation");
        lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        Label lblNom = new Label("🌱 " + r.getNom_plante() + "  →  🌍 " + r.getNom_terrain());
        lblNom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

        Separator sep1 = new Separator(); sep1.setOpacity(0.3);

        String couleur  = getCouleurStatut(r.getStatus());
        String libelleS = getStatutLibelle(r.getStatus());
        Label lblStatut = new Label(getEmojiStatut(r.getStatus()) + "  " + libelleS);
        lblStatut.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20;");

        long duree = calculerDuree(r.getDate_debut_t(), r.getDate_fin_t());
        ProgressBar barDuree = new ProgressBar(Math.min(Math.max(duree / 365.0, 0), 1.0));
        barDuree.setPrefWidth(420); barDuree.setPrefHeight(18);
        barDuree.setStyle("-fx-accent: " + couleur + ";");
        Label lblDuree = new Label("⏱️ Durée : " + (duree > 0 ? duree + " jours" : "N/A") + " / 365j");
        lblDuree.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // PieChart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (duree > 0) {
            pieData.add(new PieChart.Data("Durée (" + duree + "j)", duree));
            long reste = Math.max(365 - duree, 0);
            if (reste > 0) pieData.add(new PieChart.Data("Restant (" + reste + "j)", reste));
        }
        PieChart pie = new PieChart(pieData);
        pie.setTitle("Durée / Cycle annuel");
        pie.setPrefSize(280, 200);
        pie.setLabelsVisible(true);

        Separator sep2 = new Separator(); sep2.setOpacity(0.3);

        Label lblDetail = new Label(
                "  🌱 Plante : "  + r.getNom_plante() + "\n" +
                        "  🌿 Variété : " + (r.getVariete_plante() != null ? r.getVariete_plante() : "N/A") + "\n" +
                        "  🌍 Terrain : " + r.getNom_terrain() + "\n" +
                        "  📅 Début : "   + formatDate(r.getDate_debut_t()) + "\n" +
                        "  🏁 Fin : "     + formatDate(r.getDate_fin_t()) + "\n" +
                        "  ⏱️ Durée : "   + (duree > 0 ? duree + " jours" : "N/A") + "\n" +
                        "  📊 Statut : "  + libelleS
        );
        lblDetail.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; " +
                "-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8;");

        String msg = r.getStatus() == 1
                ? "✅ Rotation active — La culture de " + r.getNom_plante()
                + " est actuellement en cours sur " + r.getNom_terrain()
                : "🏆 Rotation terminée — Cycle de culture complété avec succès !";
        Label lblMsg = new Label(msg);
        lblMsg.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; " +
                "-fx-background-color: #EAF5EA; -fx-padding: 12; -fx-background-radius: 8;");
        lblMsg.setWrapText(true); lblMsg.setMaxWidth(430);

        vbox.getChildren().addAll(lblTitre, lblNom, sep1, lblStatut, barDuree, lblDuree, pie, sep2, lblDetail, lblMsg);
        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        stage.setScene(new Scene(scroll, 500, 650));
        stage.show();
    }

    // ============================================================
    // STATISTIQUES GLOBALES (avec les vraies méthodes du service)
    // ============================================================
    @FXML
    public void afficherStatistiquesGlobales(ActionEvent event) {
        Map<String, Object>  stats       = rs.getStatistiques();       // ✅ existe
        Map<String, Integer> repartition = rs.getRepartitionStatut();  // ✅ existe
        Map<String, Integer> topPlantes  = rs.getTopPlantes();         // ✅ existe

        // PieChart statut
        ObservableList<PieChart.Data> pieStatut = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : repartition.entrySet())
            if (e.getValue() > 0)
                pieStatut.add(new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
        PieChart pieS = new PieChart(pieStatut);
        pieS.setTitle("Statut des Rotations");
        pieS.setLabelsVisible(true);
        pieS.setPrefSize(320, 250);

        // PieChart top plantes
        ObservableList<PieChart.Data> piePlantes = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : topPlantes.entrySet())
            piePlantes.add(new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()));
        PieChart pieP = new PieChart(piePlantes);
        pieP.setTitle("Top 5 Plantes Cultivées");
        pieP.setLabelsVisible(true);
        pieP.setPrefSize(320, 250);

        HBox charts = new HBox(20, pieS, pieP);
        charts.setStyle("-fx-alignment: center;");

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : stats.entrySet())
            sb.append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
        Label txtStats = new Label(sb.toString());
        txtStats.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; " +
                "-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8;");

        VBox vbox = new VBox(15, charts, txtStats);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6; -fx-alignment: center;");
        Stage st = new Stage();
        st.setTitle("📊 Statistiques Globales des Rotations");
        st.setScene(new Scene(vbox, 700, 460));
        st.show();
    }

    // ============================================================
    // RÉINITIALISER
    // ============================================================
    @FXML
    public void reinitialiserRecherche(ActionEvent event) {
        txtRecherche.clear();
        comboFiltre.setValue("Tous");
        afficherRotations(toutesRotations);
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    public void versRotations(ActionEvent event) { naviguer("/AffichageRotation.fxml", event); }

    @FXML
    public void versAccueil(ActionEvent event) { naviguer("/.fxml", event); }

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
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 20;");
        return tag;
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    private long calculerDuree(java.util.Date debut, java.util.Date fin) {
        if (debut == null || fin == null) return 0;
        return (fin.getTime() - debut.getTime()) / (1000L * 60 * 60 * 24);
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}