package controllers.Terrains;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import models.Terrains.rotation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.Terrains.RotationService;
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
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class AffichageRotationController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private TableView<rotation> tableRotations;
    @FXML private TableColumn<rotation, String> colTerrain;
    @FXML private TableColumn<rotation, String> colPlante;
    @FXML private TableColumn<rotation, String> colVariete;
    @FXML private TableColumn<rotation, Date>   colDateDebut;
    @FXML private TableColumn<rotation, Date>   colDateFin;
    @FXML private TableColumn<rotation, String> colStatus;

    @FXML private TextField      txtRecherche;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboTri;

    private final RotationService rs = new RotationService();
    private ObservableList<rotation> listeRotations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        configurerRecherche();
        configurerFiltreStatut();
        configurerTri();
        chargerDonnees();
    }

    private void configurerTableau() {
        colTerrain.setCellValueFactory(new PropertyValueFactory<>("nom_terrain"));
        colPlante.setCellValueFactory(new PropertyValueFactory<>("nom_plante"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete_plante"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut_t"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin_t"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
    }

    private void configurerRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherRotations(newValue);
            }
        });
    }

    private void configurerFiltreStatut() {
        comboStatut.setItems(FXCollections.observableArrayList("Tous", "En cours", "Terminée"));
        comboStatut.setValue("Tous");
        comboStatut.setOnAction(event -> {
            String statut = comboStatut.getValue();
            if (statut != null) filtrerParStatut(statut);
        });
    }

    private void configurerTri() {
        comboTri.setItems(FXCollections.observableArrayList(
                "Date début (récente)", "Date début (ancienne)",
                "Date fin (récente)", "Date fin (ancienne)",
                "Terrain (A-Z)", "Terrain (Z-A)",
                "Plante (A-Z)", "Statut (En cours d'abord)"
        ));
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) trierRotations(critere);
        });
    }

    private void chargerDonnees() {
        listeRotations = FXCollections.observableArrayList(rs.afficherToutes());
        tableRotations.setItems(listeRotations);
    }

    private void rechercherRotations(String motCle) {
        listeRotations = FXCollections.observableArrayList(rs.rechercher(motCle));
        tableRotations.setItems(listeRotations);
    }

    private void filtrerParStatut(String statut) {
        if (statut.equals("Tous")) {
            chargerDonnees();
        } else {
            int statutInt = statut.equals("En cours") ? 1 : 0;
            listeRotations = FXCollections.observableArrayList(rs.filtrerParStatut(statutInt));
            tableRotations.setItems(listeRotations);
        }
    }

    private void trierRotations(String critere) {
        listeRotations = FXCollections.observableArrayList(rs.trierPar(critere));
        tableRotations.setItems(listeRotations);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboStatut.setValue("Tous");
        comboTri.setValue(null);
        chargerDonnees();
    }

    // ============================================================
    // STATISTIQUES EN PIE CHART (GRAPHIQUE CERCLE)
    // ============================================================
    @FXML
    public void afficherStatistiques(ActionEvent event) {
        Map<String, Integer> repartitionStatut = rs.getRepartitionStatut();
        Map<String, Integer> topPlantes        = rs.getTopPlantes();
        Map<String, Object>  stats             = rs.getStatistiques();

        // ── PIE CHART 1 : En cours vs Terminées ──
        ObservableList<PieChart.Data> pieStatut = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : repartitionStatut.entrySet()) {
            if (entry.getValue() > 0) {
                pieStatut.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        PieChart chartStatut = new PieChart(pieStatut);
        chartStatut.setTitle("Statut des Rotations");
        chartStatut.setLabelsVisible(true);
        chartStatut.setPrefSize(350, 300);

        // ── PIE CHART 2 : Top 5 plantes cultivées ──
        ObservableList<PieChart.Data> piePlantes = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : topPlantes.entrySet()) {
            piePlantes.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }
        PieChart chartPlantes = new PieChart(piePlantes);
        chartPlantes.setTitle("Top Plantes Cultivées");
        chartPlantes.setLabelsVisible(true);
        chartPlantes.setPrefSize(350, 300);

        // ── Texte stats générales ──
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        Text txtStats = new Text(sb.toString());
        txtStats.setFont(Font.font("System", 13));

        // ── Layout ──
        HBox hboxCharts = new HBox(20, chartStatut, chartPlantes);
        hboxCharts.setStyle("-fx-alignment: center;");
        VBox vbox = new VBox(15, hboxCharts, txtStats);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        Stage stageChart = new Stage();
        stageChart.setTitle("📊 Statistiques des Rotations");
        stageChart.setScene(new Scene(vbox, 780, 520));
        stageChart.show();
    }

    // ============================================================
    // RAPPORT PDF COMPLET DES ROTATIONS
    // ============================================================
    @FXML
    public void exporterPDF(ActionEvent event) {
        List<rotation> rotations   = rs.afficherToutes();
        Map<String, Object> stats  = rs.getStatistiques();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Rapport PDF");
        fileChooser.setInitialFileName("rapport_rotations_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        Stage stage = (Stage) tableRotations.getScene().getWindow();
        java.io.File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        try {
            PdfWriter writer   = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document  = new Document(pdfDoc);

            // ── COULEURS ──
            DeviceRgb vertFonce = new DeviceRgb(45, 90, 39);
            DeviceRgb vertClair = new DeviceRgb(168, 198, 159);
            DeviceRgb gris      = new DeviceRgb(44, 62, 80);
            DeviceRgb orange    = new DeviceRgb(230, 126, 34);
            DeviceRgb rouge     = new DeviceRgb(231, 76, 60);

            // ── TITRE ──
            document.add(new Paragraph("AGROFLOW")
                    .setFontSize(11).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setItalic());

            document.add(new Paragraph("Rapport des Rotations Agricoles")
                    .setFontSize(24).setBold().setFontColor(vertFonce)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            document.add(new Paragraph("Généré le : " + dateStr)
                    .setFontSize(10).setItalic().setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // ── LIGNE DÉCO ──
            Table ligne = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth().setMarginBottom(15);
            ligne.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            document.add(ligne);

            // ── STATISTIQUES ──
            document.add(new Paragraph("Statistiques Générales")
                    .setFontSize(15).setBold().setFontColor(vertFonce).setMarginBottom(8));

            Table tableStats = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth().setMarginBottom(20);
            tableStats.addHeaderCell(new Cell().add(new Paragraph("Indicateur").setBold().setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setPadding(8).setBorder(new SolidBorder(vertFonce, 1)));
            tableStats.addHeaderCell(new Cell().add(new Paragraph("Valeur").setBold().setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setPadding(8).setBorder(new SolidBorder(vertFonce, 1)));

            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                tableStats.addCell(new Cell().add(new Paragraph(entry.getKey()))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
                tableStats.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue())).setBold())
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
            }
            document.add(tableStats);

            // ── LISTE ROTATIONS ──
            document.add(new Paragraph("Liste Complète des Rotations")
                    .setFontSize(15).setBold().setFontColor(vertFonce).setMarginBottom(8));

            Table tableRot = new Table(UnitValue.createPercentArray(new float[]{22, 18, 16, 14, 14, 16}))
                    .useAllAvailableWidth();

            // En-têtes
            String[] headers = {"Terrain", "Plante", "Variété", "Date Début", "Date Fin", "Statut"};
            for (String h : headers) {
                tableRot.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(vertClair).setPadding(7)
                        .setBorder(new SolidBorder(vertFonce, 1)));
            }

            // Données
            for (rotation r : rotations) {
                String statutTxt = r.getStatus() == 1 ? "En cours" : "Terminée";
                DeviceRgb couleurStatut = r.getStatus() == 1 ? orange : rouge;

                tableRot.addCell(new Cell().add(new Paragraph(r.getNom_terrain() != null ? r.getNom_terrain() : "-"))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tableRot.addCell(new Cell().add(new Paragraph(r.getNom_plante() != null ? r.getNom_plante() : "-"))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tableRot.addCell(new Cell().add(new Paragraph(r.getVariete_plante() != null ? r.getVariete_plante() : "-"))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tableRot.addCell(new Cell().add(new Paragraph(r.getDate_debut_t() != null ? r.getDate_debut_t().toString() : "-"))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tableRot.addCell(new Cell().add(new Paragraph(r.getDate_fin_t() != null ? r.getDate_fin_t().toString() : "-"))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tableRot.addCell(new Cell().add(new Paragraph(statutTxt).setBold().setFontColor(couleurStatut))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
            }
            document.add(tableRot);

            // ── PIED DE PAGE ──
            Table ligne2 = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth().setMarginTop(15);
            ligne2.addCell(new Cell().setHeight(2).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            document.add(ligne2);

            document.add(new Paragraph("AGROFLOW - Système de Gestion Agricole")
                    .setFontSize(9).setItalic().setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(5));

            document.close();

            showAlert("✅ Succès", "Rapport PDF exporté avec succès !", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("❌ Erreur", "Impossible de générer le PDF : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    public void versModifier(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();
        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à modifier.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ModifierRotation.fxml"));
            Parent root = loader.load();
            ModifierRotationController controller = loader.getController();
            controller.initialiserAvecRotation(rotationSelectionnee);
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();
        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à supprimer.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cette rotation ?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                rs.supprimer(rotationSelectionnee.getId_rotation());
                chargerDonnees();
                showAlert("Succès", "Rotation supprimée avec succès.", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AjoutRotation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void versAccueil(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/acceuilterrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
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
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.chargerPage(event,"/UsersInterface/DahboardPersonne.fxml","Personnes - agroflow");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.chargerPage(event,"/UsersInterface/GestionTache.fxml","taches - agroflow");}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.chargerPage(event,"/UsersInterface/GestionAbonnements.fxml","abonnements - agroflow - Agroflow");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.chargerPage(event,"/UsersInterface/GestionOffre.fxml","offres - agroflow - Agroflow");}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) throws IOException {
        this.chargerPage(actionEvent, "/UsersInterface/Acceuil.fxml","Acceuil - Agroflow ");

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml","Animals - agroflow");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/StocksInterface/afficherarticle.fxml","Stocks - agroflow");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml","Terrains - agroflow");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/G-Evenements/Accueil.fxml","Evenements - agroflow");
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml","Materiels - agroflow");
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


    private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }}

}