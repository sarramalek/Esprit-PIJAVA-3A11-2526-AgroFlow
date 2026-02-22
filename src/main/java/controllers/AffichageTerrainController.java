package controllers;

import entities.terrain;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.TerrainService;
import com.itextpdf.kernel.colors.ColorConstants;
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
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherTerrains(newValue);
            }
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
    // STATISTIQUES EN GRAPHIQUE CERCLE (PIE CHART)
    // ============================================================
    @FXML
    public void afficherStatistiques(ActionEvent event) {
        Map<String, Integer> repartition = ts.getRepartitionTypeSol();
        Map<String, Object> stats       = ts.getStatistiques();

        // ── PIE CHART répartition par type de sol ──
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : repartition.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Répartition par Type de Sol");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(500, 400);

        // ── Texte des stats générales ──
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        Text txtStats = new Text(sb.toString());
        txtStats.setFont(Font.font("System", 13));

        // ── Fenêtre ──
        VBox vbox = new VBox(15, pieChart, txtStats);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6;");

        Stage stageChart = new Stage();
        stageChart.setTitle("📊 Statistiques des Terrains");
        stageChart.setScene(new Scene(vbox, 550, 600));
        stageChart.show();
    }

    // ============================================================
    // CERTIFICAT PDF TERRAIN (INNOVANT)
    // ============================================================
    @FXML
    public void exporterCertificat(ActionEvent event) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();

        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain pour générer son certificat.", Alert.AlertType.WARNING);
            return;
        }

        // FileChooser pour choisir l'emplacement
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Certificat PDF");
        fileChooser.setInitialFileName("certificat_terrain_" + terrainSelectionne.getNom_terrain().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        Stage stage = (Stage) tableTerrains.getScene().getWindow();
        java.io.File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        try {
            PdfWriter writer   = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document  = new Document(pdfDoc);

            // ── COULEURS ──
            DeviceRgb vertFonce  = new DeviceRgb(45, 90, 39);   // #2D5A27
            DeviceRgb vertClair  = new DeviceRgb(168, 198, 159); // #A8C69F
            DeviceRgb beige      = new DeviceRgb(252, 248, 230); // #fcf8e6
            DeviceRgb gris       = new DeviceRgb(44, 62, 80);    // #2C3E50

            // ── BORDURE DÉCORATIVE (tableau 1 cellule) ──
            Table borderTable = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth();
            Cell borderCell = new Cell()
                    .setBorder(new SolidBorder(vertFonce, 4))
                    .setBackgroundColor(beige)
                    .setPadding(30);

            // ── EN-TÊTE CERTIFICAT ──
            borderCell.add(new Paragraph("🌿 AGROFLOW")
                    .setFontSize(13)
                    .setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());

            borderCell.add(new Paragraph("CERTIFICAT DE TERRAIN AGRICOLE")
                    .setFontSize(26)
                    .setFontColor(vertFonce)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10)
                    .setMarginBottom(5));

            // ── LIGNE DÉCORATIVE ──
            Table ligneDeco = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);
            ligneDeco.addCell(new Cell()
                    .setHeight(3)
                    .setBackgroundColor(vertFonce)
                    .setBorder(Border.NO_BORDER));
            borderCell.add(ligneDeco);

            // ── TEXTE INTRODUCTION ──
            borderCell.add(new Paragraph(
                    "Il est certifié que le terrain agricole suivant est enregistré " +
                            "et validé dans le système de gestion AGROFLOW.")
                    .setFontSize(12)
                    .setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setMarginBottom(25));

            // ── NOM DU TERRAIN (mis en valeur) ──
            borderCell.add(new Paragraph(terrainSelectionne.getNom_terrain().toUpperCase())
                    .setFontSize(28)
                    .setFontColor(vertFonce)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(25));

            // ── TABLEAU DES INFORMATIONS ──
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(25);

            String[][] infos = {
                    {"📍 Localisation",  terrainSelectionne.getLocalisation()},
                    {"🪨 Type de sol",   terrainSelectionne.getType_sol()},
                    {"📐 Surface",       terrainSelectionne.getSurface() + " m²"},
                    {"🧪 pH du sol",     String.valueOf(terrainSelectionne.getP_h())},
                    {"🆔 Identifiant",   "TRN-" + String.format("%04d", terrainSelectionne.getId_terrain())},
            };

            for (String[] info : infos) {
                Cell labelCell = new Cell()
                        .add(new Paragraph(info[0]).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(vertClair)
                        .setPadding(8)
                        .setBorder(new SolidBorder(vertFonce, 1));

                Cell valueCell = new Cell()
                        .add(new Paragraph(info[1]).setFontColor(gris))
                        .setPadding(8)
                        .setBorder(new SolidBorder(vertClair, 1));

                infoTable.addCell(labelCell);
                infoTable.addCell(valueCell);
            }
            borderCell.add(infoTable);

            // ── QUALITÉ DU SOL (analyse pH) ──
            float ph = terrainSelectionne.getP_h();
            String qualitePH;
            if (ph < 6.0f)       qualitePH = "Sol acide - Convient aux myrtilles, pommes de terre";
            else if (ph <= 7.0f) qualitePH = "Sol neutre - Idéal pour la majorité des cultures";
            else                 qualitePH = "Sol basique - Convient aux asperges, choux";

            borderCell.add(new Paragraph("Analyse du sol : " + qualitePH)
                    .setFontSize(11)
                    .setFontColor(gris)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // ── LIGNE DÉCORATIVE BAS ──
            Table ligneDeco2 = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);
            ligneDeco2.addCell(new Cell()
                    .setHeight(3)
                    .setBackgroundColor(vertFonce)
                    .setBorder(Border.NO_BORDER));
            borderCell.add(ligneDeco2);

            // ── DATE ET PIED DE PAGE ──
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            borderCell.add(new Paragraph("Délivré le : " + dateStr)
                    .setFontSize(10)
                    .setFontColor(gris)
                    .setTextAlignment(TextAlignment.RIGHT));

            borderCell.add(new Paragraph("AGROFLOW - Système de Gestion Agricole")
                    .setFontSize(10)
                    .setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());

            borderTable.addCell(borderCell);
            document.add(borderTable);
            document.close();

            showAlert("✅ Certificat généré !",
                    "Le certificat du terrain '" + terrainSelectionne.getNom_terrain() + "' a été exporté avec succès !",
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("❌ Erreur", "Impossible de générer le certificat : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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
                "⚠️ ATTENTION ⚠️\n\nSupprimer le terrain '" + terrainSelectionne.getNom_terrain() + "' ?\n\n" +
                        "Cela supprimera aussi :\n• Toutes les rotations de ce terrain\n• L'historique des cultures\n\nLes plantes seront conservées.",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    ts.supprimerAvecRotations(terrainSelectionne.getId_terrain());
                    chargerDonnees();
                    showAlert("Succès", "Terrain et ses rotations supprimés avec succès.", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
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
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
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