package controllers.Terrains;

import controllers.User.ProfilEmploye;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import models.Terrains.plante;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User.Personne;
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
import javafx.application.Platform;
import services.ApiMeteoService;
import javafx.stage.Stage;
import javafx.scene.Scene;
import utils.SessionManager;


import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class AffichagePlanteController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;

    @FXML private TableView<plante> tablePlantes;
    @FXML private TableColumn<plante, String>  colNom;
    @FXML private TableColumn<plante, String>  colVariete;
    @FXML private TableColumn<plante, Float>   colBesoinEau;
    @FXML private TableColumn<plante, Integer> colCycle;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboTri;

    private final PlanteService ps = new PlanteService();
    private ObservableList<plante> listePlantes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        // Mise à jour des labels
        updateUserLabels();
        configurerTableau();
        configurerRecherche();
        configurerTri();
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
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage(),"erreur "); } }
    public void setCurrentUser(Personne user) {
        // ✅ CORRECTION : assigner le CHAMP de classe, pas une variable locale
        this.currentUser = user;

        if (user != null) {
            SessionManager.setCurrentUser(user); // synchroniser le SessionManager
            System.out.println("✓ setCurrentUser: " + user.getPrenom() + " " + user.getNom());
            updateUserLabels();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL");
        }
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
    private void configurerTableau() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_p"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete"));
        colBesoinEau.setCellValueFactory(new PropertyValueFactory<>("besoin_eau"));
        colCycle.setCellValueFactory(new PropertyValueFactory<>("cycle_jours"));
    }

    private void configurerRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherPlantes(newValue);
            }
        });
    }

    private void configurerTri() {
        comboTri.setItems(FXCollections.observableArrayList(
                "Nom (A-Z)", "Nom (Z-A)",
                "Besoin en eau (croissant)", "Besoin en eau (décroissant)",
                "Cycle (court au long)", "Cycle (long au court)"
        ));
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) trierPlantes(critere);
        });
    }

    private void chargerDonnees() {
        listePlantes = FXCollections.observableArrayList(ps.afficherToutes());
        tablePlantes.setItems(listePlantes);
    }

    private void rechercherPlantes(String motCle) {
        listePlantes = FXCollections.observableArrayList(ps.rechercher(motCle));
        tablePlantes.setItems(listePlantes);
    }

    private void trierPlantes(String critere) {
        listePlantes = FXCollections.observableArrayList(ps.trierPar(critere));
        tablePlantes.setItems(listePlantes);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboTri.setValue(null);
        chargerDonnees();
    }

    // ============================================================
    // STATISTIQUES : PIE CHART + BAR CHART
    // ============================================================
    @FXML
    public void afficherStatistiques(ActionEvent event) {
        List<plante> plantes       = ps.afficherToutes();
        Map<String, Object> stats  = ps.getStatistiques();

        // ── PIE CHART : Répartition besoin eau (faible / moyen / élevé) ──
        int faible = 0, moyen = 0, eleve = 0;
        for (plante p : plantes) {
            if (p.getBesoin_eau() < 1.5f)      faible++;
            else if (p.getBesoin_eau() <= 3.0f) moyen++;
            else                                eleve++;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (faible > 0) pieData.add(new PieChart.Data("Faible besoin (" + faible + ")", faible));
        if (moyen > 0)  pieData.add(new PieChart.Data("Besoin moyen (" + moyen + ")", moyen));
        if (eleve > 0)  pieData.add(new PieChart.Data("Besoin élevé (" + eleve + ")", eleve));

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Répartition Besoin en Eau");
        pieChart.setLabelsVisible(true);
        pieChart.setPrefSize(350, 300);

        // ── BAR CHART : Cycle de chaque plante ──
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Plante");
        yAxis.setLabel("Cycle (jours)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Cycle de croissance par plante");
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Jours");
        for (plante p : plantes) {
            serie.getData().add(new XYChart.Data<>(p.getNom_p(), p.getCycle_jours()));
        }
        barChart.getData().add(serie);
        barChart.setPrefSize(380, 300);

        // ── Stats texte ──
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        Text txtStats = new Text(sb.toString());
        txtStats.setFont(Font.font("System", 13));

        // ── Fenêtre ──
        HBox hboxCharts = new HBox(20, pieChart, barChart);
        hboxCharts.setStyle("-fx-alignment: center;");
        VBox vbox = new VBox(15, hboxCharts, txtStats);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #fcf8e6; -fx-alignment: center;");

        Stage stageChart = new Stage();
        stageChart.setTitle("📊 Statistiques des Plantes");
        stageChart.setScene(new Scene(vbox, 800, 530));
        stageChart.show();
    }

    // ============================================================
    // EXPORT PDF - MÊME STYLE QUE ROTATION
    // ============================================================
    @FXML
    public void exporterPDF(ActionEvent event) {
        List<plante> plantes = ps.afficherToutes();
        Map<String, Object> stats = ps.getStatistiques();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Rapport PDF");
        fileChooser.setInitialFileName("rapport_plantes_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        Stage stage = (Stage) tablePlantes.getScene().getWindow();
        java.io.File fichier = fileChooser.showSaveDialog(stage);
        if (fichier == null) return;

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(fichier));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // ── COULEURS ──
            DeviceRgb vertFonce = new DeviceRgb(45, 90, 39);
            DeviceRgb vertClair = new DeviceRgb(168, 198, 159);
            DeviceRgb gris = new DeviceRgb(44, 62, 80);
            DeviceRgb bleu = new DeviceRgb(41, 128, 185);
            DeviceRgb orange = new DeviceRgb(230, 126, 34);

            // ── TITRE ──
            document.add(new Paragraph("AGROFLOW")
                    .setFontSize(11).setFontColor(vertClair)
                    .setTextAlignment(TextAlignment.CENTER).setItalic());

            document.add(new Paragraph("Rapport des Plantes Agricoles")
                    .setFontSize(24).setBold().setFontColor(vertFonce)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            document.add(new Paragraph("Généré le : " + dateStr)
                    .setFontSize(10).setItalic().setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // ── LIGNE DÉCO ──
            Table ligne = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginBottom(15);
            ligne.addCell(new Cell().setHeight(3).setBackgroundColor(vertFonce).setBorder(Border.NO_BORDER));
            document.add(ligne);

            // ── STATISTIQUES ──
            document.add(new Paragraph("Statistiques Générales")
                    .setFontSize(15).setBold().setFontColor(vertFonce).setMarginBottom(8));

            Table tableStats = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth().setMarginBottom(20);
            tableStats.addHeaderCell(new Cell()
                    .add(new Paragraph("Indicateur").setBold().setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setPadding(8).setBorder(new SolidBorder(vertFonce, 1)));
            tableStats.addHeaderCell(new Cell()
                    .add(new Paragraph("Valeur").setBold().setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setPadding(8).setBorder(new SolidBorder(vertFonce, 1)));

            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                tableStats.addCell(new Cell().add(new Paragraph(entry.getKey()))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
                tableStats.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue())).setBold())
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
            }

            // Plante max eau et max cycle
            plante maxEau = ps.getPlanteMaxEau();
            plante maxCycle = ps.getPlanteMaxCycle();
            if (maxEau != null) {
                tableStats.addCell(new Cell().add(new Paragraph("Plante + gourmande en eau"))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
                tableStats.addCell(new Cell().add(new Paragraph(maxEau.getNom_p() + " (" + maxEau.getBesoin_eau() + " L)").setBold())
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
            }
            if (maxCycle != null) {
                tableStats.addCell(new Cell().add(new Paragraph("Plante au cycle le plus long"))
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
                tableStats.addCell(new Cell().add(new Paragraph(maxCycle.getNom_p() + " (" + maxCycle.getCycle_jours() + " jours)").setBold())
                        .setPadding(7).setBorder(new SolidBorder(vertClair, 1)));
            }
            document.add(tableStats);

            // ── LISTE PLANTES ──
            document.add(new Paragraph("Liste Complète des Plantes")
                    .setFontSize(15).setBold().setFontColor(vertFonce).setMarginBottom(8));

            Table tablePl = new Table(UnitValue.createPercentArray(new float[]{28, 25, 22, 25}))
                    .useAllAvailableWidth();

            // En-têtes
            String[] headers = {"Nom", "Variété", "Besoin Eau (L)", "Cycle (jours)"};
            for (String h : headers) {
                tablePl.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(vertClair).setPadding(7)
                        .setBorder(new SolidBorder(vertFonce, 1)));
            }

            // Données avec couleur selon besoin eau
            for (plante p : plantes) {
                DeviceRgb couleurEau = p.getBesoin_eau() > 3.0f ? orange : bleu;

                tablePl.addCell(new Cell().add(new Paragraph(p.getNom_p()).setBold())
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tablePl.addCell(new Cell().add(new Paragraph(p.getVariete()))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tablePl.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(p.getBesoin_eau())).setBold().setFontColor(couleurEau))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
                tablePl.addCell(new Cell().add(new Paragraph(String.valueOf(p.getCycle_jours())))
                        .setPadding(6).setBorder(new SolidBorder(vertClair, 1)));
            }
            document.add(tablePl);

            // ── LÉGENDE ──
            document.add(new Paragraph("Besoin eau : bleu = normal (<=3L)  |  orange = élevé (>3L)")
                    .setFontSize(9).setItalic().setFontColor(gris)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(8));

            // ── PIED DE PAGE ──
            Table ligne2 = new Table(UnitValue.createPercentArray(new float[]{100}))
                    .useAllAvailableWidth().setMarginTop(15);
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
    @FXML
    public void afficherRecommandationArrosage (ActionEvent event){
        plante planteSelectionnee = tablePlantes.getSelectionModel().getSelectedItem();

        if (planteSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une plante.", Alert.AlertType.WARNING);
            return;
        }

        // Chargement API dans thread séparé
        new Thread(() -> {
            try {
                // ── Appel API météo (Tunis par défaut) ──
                ApiMeteoService apiService = new ApiMeteoService();
                ApiMeteoService.MeteoResult meteo = apiService.getMeteoParAdresse("Tunis, Tunisie");

                // ── Calcul recommandation ──
                String recommandation = ps.getRecommandationArrosage(
                        planteSelectionnee,
                        meteo.temperature,
                        meteo.humidite,
                        meteo.precipitation
                );

                javafx.application.Platform.runLater(() -> {
                    Stage stageReco = new Stage();
                    stageReco.setTitle("💧 Arrosage - " + planteSelectionnee.getNom_p());

                    VBox vbox = new VBox(15);
                    vbox.setStyle("-fx-padding: 25; -fx-background-color: #fcf8e6; -fx-alignment: center;");

                    // ── Titre ──
                    Label lblTitre = new Label("💧 Recommandation d'Arrosage");
                    lblTitre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

                    Label lblNom = new Label("🌱 " + planteSelectionnee.getNom_p().toUpperCase()
                            + "  •  " + planteSelectionnee.getVariete());
                    lblNom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

                    Separator sep1 = new Separator();
                    sep1.setOpacity(0.3);

                    // ── Infos plante ──
                    Label lblInfoPlante = new Label(
                            "📋 Besoin en eau : " + planteSelectionnee.getBesoin_eau() + " L\n" +
                                    "🔄 Cycle de croissance : " + planteSelectionnee.getCycle_jours() + " jours"
                    );
                    lblInfoPlante.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; " +
                            "-fx-background-color: white; -fx-padding: 10; " +
                            "-fx-background-radius: 8; -fx-border-color: #D5E8D5; " +
                            "-fx-border-radius: 8;");

                    // ── Météo actuelle ──
                    Label lblMeteoTitre = new Label("🌤️ Météo Actuelle");
                    lblMeteoTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2980B9;");

                    Label lblMeteo = new Label(
                            meteo.emoji + "  " + meteo.condition + "\n" +
                                    "🌡️ Température : " + meteo.temperature + " °C\n" +
                                    "💧 Humidité    : " + meteo.humidite + " %\n" +
                                    "🌧️ Pluie       : " + meteo.precipitation + " mm"
                    );
                    lblMeteo.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C3E50; " +
                            "-fx-background-color: #EBF5FB; -fx-padding: 12; " +
                            "-fx-background-radius: 8;");

                    Separator sep2 = new Separator();
                    sep2.setOpacity(0.3);

                    // ── Recommandation finale ──
                    Label lblRecoTitre = new Label("💡 Recommandation");
                    lblRecoTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

                    Label lblReco = new Label(recommandation);
                    lblReco.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; " +
                            "-fx-background-color: #EAF5EA; -fx-padding: 14; " +
                            "-fx-background-radius: 10; -fx-border-color: #A8C69F; " +
                            "-fx-border-radius: 10;");
                    lblReco.setWrapText(true);
                    lblReco.setMaxWidth(430);

                    vbox.getChildren().addAll(
                            lblTitre, lblNom, sep1,
                            lblInfoPlante,
                            lblMeteoTitre, lblMeteo,
                            sep2,
                            lblRecoTitre, lblReco
                    );

                    stageReco.setScene(new Scene(vbox, 480, 530));
                    stageReco.show();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showAlert("❌ Erreur", "Impossible de récupérer la météo :\n" + e.getMessage(),
                                Alert.AlertType.ERROR));
            }
        }).start();

    }


    // ============================================================
    // NAVIGATION
    // ============================================================
    @FXML
    public void versModifier(ActionEvent actionEvent) {
        plante planteSelectionnee = tablePlantes.getSelectionModel().getSelectedItem();
        if (planteSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une plante à modifier.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ModifierPlante.fxml"));
            Parent root = loader.load();
            ModifierPlanteController controller = loader.getController();
            controller.initialiserAvecPlante(planteSelectionnee);
            Stage stage = (Stage) tablePlantes.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        plante planteSelectionnee = tablePlantes.getSelectionModel().getSelectedItem();
        if (planteSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une plante à supprimer.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ ATTENTION ⚠️\n\nSupprimer la plante '" + planteSelectionnee.getNom_p() + "' ?\n\n" +
                        "Cela supprimera aussi :\n• Toutes les rotations de cette plante\n• L'historique des cultures",
                ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    ps.supprimerAvecRotations(planteSelectionnee.getId_plante());
                    chargerDonnees();
                    showAlert("Succès", "Plante et ses rotations supprimées avec succès.", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AjoutPlante.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablePlantes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void versAccueil(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/acceuilterrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablePlantes.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
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
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();

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
        }}

}