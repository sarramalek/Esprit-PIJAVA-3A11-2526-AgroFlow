package controllers.Materiels;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Materiels.Machine;
import models.User.Personne;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import services.Materiels.MachineService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AfficherMachinesController {

    @FXML private Button logoutBtn, gestionBtn;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;
    private Scene scene;

    @FXML
    private TableView<Machine> tableMachines;

    @FXML private TableColumn<Machine, Integer> colId;
    @FXML private TableColumn<Machine, String> colNom;
    @FXML private TableColumn<Machine, String> colMarque;
    @FXML private TableColumn<Machine, String> colModele;
    @FXML private TableColumn<Machine, String> colNumeroSerie;
    @FXML private TableColumn<Machine, String> colEtat;
    @FXML private TableColumn<Machine, LocalDate> colDateAchat;
    @FXML private TableColumn<Machine, Integer> colKilometrage;
    @FXML private TableColumn<Machine, LocalDate> colDateLastVisite;
    @FXML private TableColumn<Machine, Integer> colKmLastVisite;
    @FXML private TableColumn<Machine, LocalDate> colProchaineMaintenance;
    @FXML private TableColumn<Machine, Integer> colCin;

    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboNom;
    @FXML private ComboBox<String> comboModele;

    @FXML private Label lblTotal;
    @FXML private Label lblNeuves;
    @FXML private Label lblOccasion;
    @FXML private Label lblEnPanne;
    @FXML private Label lblFiltres;
    @FXML private PieChart pieStats;

    private final MachineService machineService = new MachineService();
    private final ObservableList<Machine> masterList = FXCollections.observableArrayList();
    private FilteredList<Machine> filteredList;
    private SortedList<Machine> sortedList;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] PIE_COLORS = {"#27ae60", "#e67e22", "#e74c3c", "#95a5a6"};

    // ════════════════════════════════════════════════════════════════════════
    //  MÉTHODE RETOUR ACCUEIL (AJOUTÉE)
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void retourAccueil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/AccueilMateriel.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de retourner à l'accueil", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ════════════════════════════════════════════════════════════════════════

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idM"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colNumeroSerie.setCellValueFactory(new PropertyValueFactory<>("numeroSerie"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatM"));
        colDateAchat.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colKilometrage.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        colDateLastVisite.setCellValueFactory(new PropertyValueFactory<>("dateLastVisite"));
        colKmLastVisite.setCellValueFactory(new PropertyValueFactory<>("kmLastVisite"));
        colProchaineMaintenance.setCellValueFactory(new PropertyValueFactory<>("prochaineMaintenance"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));

        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(val);
                String style;
                switch (val.toLowerCase()) {
                    case "neuf":
                    case "disponible":
                        style = "-fx-text-fill:#27ae60;-fx-font-weight:bold;";
                        break;
                    case "occasion":
                    case "bon":
                        style = "-fx-text-fill:#e67e22;-fx-font-weight:bold;";
                        break;
                    case "en panne":
                        style = "-fx-text-fill:#e74c3c;-fx-font-weight:bold;";
                        break;
                    default:
                        style = "-fx-text-fill:#7f8c8d;";
                }
                setStyle(style);
            }
        });

        colDateAchat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText((empty || val == null) ? null : val.format(DATE_FMT));
            }
        });

        colDateLastVisite.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText((empty || val == null) ? null : (val != null ? val.format(DATE_FMT) : ""));
            }
        });

        colProchaineMaintenance.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(val.format(DATE_FMT));
                if (val.isBefore(LocalDate.now())) {
                    setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;");
                } else {
                    setStyle("");
                }
            }
        });

        colId.setStyle("-fx-alignment: CENTER;");
        colKilometrage.setStyle("-fx-alignment: CENTER-RIGHT;");
        colKmLastVisite.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCin.setStyle("-fx-alignment: CENTER;");
    }

    private void configurerListeFiltrée() {
        filteredList = new FilteredList<>(masterList, m -> true);
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableMachines.comparatorProperty());
        tableMachines.setItems(sortedList);
        sortedList.addListener((javafx.collections.ListChangeListener<Machine>) m -> majStatistiques());
    }

    private void chargerMachines() {
        try {
            masterList.clear();
            masterList.addAll(machineService.recuperer());
            peuplerCombos();
            majStatistiques();
            appliquerFiltres();
        } catch (Exception e) {
            alerte("Erreur chargement", "Impossible de charger les machines :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void peuplerCombos() {
        List<String> noms = masterList.stream()
                .map(Machine::getNom)
                .filter(n -> n != null && !n.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());
        noms.add(0, "Tous les noms");
        comboNom.setItems(FXCollections.observableArrayList(noms));
        comboNom.getSelectionModel().selectFirst();

        List<String> modeles = masterList.stream()
                .map(Machine::getModele)
                .filter(m -> m != null && !m.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());
        modeles.add(0, "Tous les modeles");
        comboModele.setItems(FXCollections.observableArrayList(modeles));
        comboModele.getSelectionModel().selectFirst();
    }

    @FXML
    private void appliquerFiltres() {
        String texte = champRecherche.getText() == null ? "" : champRecherche.getText().trim().toLowerCase();
        String nom = comboNom.getValue();
        String modele = comboModele.getValue();

        filteredList.setPredicate(m -> {
            boolean okRecherche = texte.isEmpty()
                    || contient(m.getMarque(), texte)
                    || contient(m.getModele(), texte)
                    || contient(m.getEtatM(), texte)
                    || contient(m.getNumeroSerie(), texte)
                    || contient(m.getNom(), texte);

            boolean okNom = nom == null || nom.startsWith("Tous") || nom.equalsIgnoreCase(m.getNom());
            boolean okModele = modele == null || modele.startsWith("Tous") || modele.equalsIgnoreCase(m.getModele());

            return okRecherche && okNom && okModele;
        });
        majStatistiques();
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboNom.getSelectionModel().selectFirst();
        comboModele.getSelectionModel().selectFirst();
        appliquerFiltres();
    }

    @FXML
    private void actualiser() {
        chargerMachines();
    }

    private boolean contient(String champ, String recherche) {
        return champ != null && champ.toLowerCase().contains(recherche);
    }

    @FXML
    private void trierDateDesc() {
        sortedList.comparatorProperty().unbind();
        sortedList.setComparator(Comparator.comparing(Machine::getDateAchat, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    @FXML
    private void trierDateAsc() {
        sortedList.comparatorProperty().unbind();
        sortedList.setComparator(Comparator.comparing(Machine::getDateAchat, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private void majStatistiques() {
        long total = masterList.size();
        long neuves = compterEtats(new String[]{"neuf", "disponible"});
        long occasion = compterEtats(new String[]{"occasion", "bon"});
        long enPanne = compterEtatSimple("en panne");
        long filtres = sortedList.size();

        lblTotal.setText(String.valueOf(total));
        lblNeuves.setText(String.valueOf(neuves));
        lblOccasion.setText(String.valueOf(occasion));
        lblEnPanne.setText(String.valueOf(enPanne));
        lblFiltres.setText(String.valueOf(filtres));

        majPieChart(neuves, occasion, enPanne, total);
    }

    private long compterEtats(String[] etats) {
        return masterList.stream()
                .filter(m -> m.getEtatM() != null)
                .filter(m -> {
                    for (String etat : etats) {
                        if (m.getEtatM().equalsIgnoreCase(etat)) return true;
                    }
                    return false;
                })
                .count();
    }

    private long compterEtatSimple(String etat) {
        return masterList.stream()
                .filter(m -> m.getEtatM() != null && m.getEtatM().equalsIgnoreCase(etat))
                .count();
    }

    private void majPieChart(long neuves, long occasion, long enPanne, long total) {
        if (pieStats == null) return;
        long autres = total - neuves - occasion - enPanne;
        ObservableList<PieChart.Data> d = FXCollections.observableArrayList();
        if (neuves > 0) d.add(new PieChart.Data("Neuves/Dispo (" + neuves + ")", neuves));
        if (occasion > 0) d.add(new PieChart.Data("Occasion/Bon (" + occasion + ")", occasion));
        if (enPanne > 0) d.add(new PieChart.Data("En panne (" + enPanne + ")", enPanne));
        if (autres > 0) d.add(new PieChart.Data("Autres (" + autres + ")", autres));
        if (d.isEmpty()) d.add(new PieChart.Data("Aucune donnee", 1));
        pieStats.setData(d);
        Platform.runLater(() -> appliquerCouleursPie(d, PIE_COLORS));
    }

    private void appliquerCouleursPie(ObservableList<PieChart.Data> data, String[] colors) {
        for (int i = 0; i < data.size() && i < colors.length; i++) {
            if (data.get(i).getNode() != null) {
                data.get(i).getNode().setStyle("-fx-pie-color:" + colors[i] + ";");
            }
        }
    }

    @FXML
    private void afficherStatistiques() {
        long total = masterList.size();
        long neuves = compterEtats(new String[]{"neuf", "disponible"});
        long occasion = compterEtats(new String[]{"occasion", "bon"});
        long enPanne = compterEtatSimple("en panne");
        long autres = total - neuves - occasion - enPanne;

        Label titre = new Label("Statistiques des Machines");
        titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        Label sousTitre = new Label("Total : " + total + "   |   Affiches : " + sortedList.size());
        sousTitre.setStyle("-fx-font-size:12px;-fx-text-fill:#7f8c8d;");

        ObservableList<PieChart.Data> pd = FXCollections.observableArrayList();
        if (total > 0) {
            if (neuves > 0) pd.add(new PieChart.Data(String.format("Neuves/Dispo\n%d (%.1f%%)", neuves, pct(neuves, total)), neuves));
            if (occasion > 0) pd.add(new PieChart.Data(String.format("Occasion/Bon\n%d (%.1f%%)", occasion, pct(occasion, total)), occasion));
            if (enPanne > 0) pd.add(new PieChart.Data(String.format("En panne\n%d (%.1f%%)", enPanne, pct(enPanne, total)), enPanne));
            if (autres > 0) pd.add(new PieChart.Data(String.format("Autres\n%d (%.1f%%)", autres, pct(autres, total)), autres));
        } else {
            pd.add(new PieChart.Data("Aucune donnee", 1));
        }

        PieChart chart = new PieChart(pd);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(false);
        chart.setPrefSize(500, 340);
        chart.setStyle("-fx-background-color:transparent;");
        Platform.runLater(() -> appliquerCouleursPie(pd, PIE_COLORS));

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefSize(140, 40);
        btnFermer.setStyle("-fx-background-color:#8e44ad;-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");

        VBox header = new VBox(4, titre, sousTitre);
        header.setAlignment(Pos.CENTER);

        VBox layout = new VBox(14, header, new Separator(), chart, buildLegende(), new Separator(),
                buildCartes(total, neuves, occasion, enPanne), buildBarre(total, neuves, occasion, enPanne, autres), btnFermer);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24, 28, 24, 28));
        layout.setStyle("-fx-background-color:#f0f2f5;");

        Stage popup = new Stage();
        popup.setTitle("Statistiques - Gestion des Machines");
        popup.setScene(new Scene(layout, 620, 720));
        popup.initModality(Modality.APPLICATION_MODAL);
        if (tableMachines.getScene() != null) {
            popup.initOwner(tableMachines.getScene().getWindow());
        }
        popup.setResizable(false);
        btnFermer.setOnAction(e -> popup.close());
        popup.show();
        Platform.runLater(() -> appliquerCouleursPie(pd, PIE_COLORS));
    }

    @FXML
    private void ouvrirMindSpherePage() {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/MaterielsInterface/MindSpherePage.fxml");
            if (fxmlUrl == null) throw new IOException("Fichier MindSpherePage.fxml introuvable.");
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            alerte("Erreur MindSphere", "Impossible d'ouvrir la page MindSphere :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXPORT PDF
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void exporterPDF() {
        File fichier = choisirFichier("PDF", "*.pdf", "machines_" + LocalDate.now() + ".pdf");
        if (fichier == null) return;

        List<Machine> lignes = List.copyOf(sortedList);
        float pageW = PDRectangle.A4.getWidth();
        float pageH = PDRectangle.A4.getHeight();
        float marg = 40f;
        float rowH = 20f;
        float hdrH = 26f;
        int perPg = Math.max(1, (int)((pageH - marg * 3 - hdrH - 80) / rowH));
        int pages = Math.max(1, (int) Math.ceil((double) lignes.size() / perPg));
        float[] cw = {95, 85, 90, 120, 90, 35};
        String[] ch = {"Marque", "Modele", "Etat", "N Serie", "Date Achat", "Nom"};

        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            for (int pi = 0; pi < pages; pi++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = pageH - marg;

                    cs.beginText();
                    cs.setFont(bold, 16);
                    cs.newLineAtOffset(marg, y);
                    cs.showText("AGROFLOW - Gestion des Machines");
                    cs.endText();
                    y -= 20;

                    cs.beginText();
                    cs.setFont(plain, 9);
                    cs.newLineAtOffset(marg, y);
                    cs.showText("Exporte le " + LocalDate.now().format(DATE_FMT) + " | Resultats : " + lignes.size() + " / Total : " + masterList.size() + " | Page " + (pi + 1) + "/" + pages);
                    cs.endText();
                    y -= 14;

                    cs.setLineWidth(0.5f);
                    cs.setStrokingColor(0.7f, 0.7f, 0.7f);
                    cs.moveTo(marg, y);
                    cs.lineTo(pageW - marg, y);
                    cs.stroke();
                    y -= 8;

                    cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                    cs.addRect(marg, y - hdrH + 5, pageW - 2 * marg, hdrH);
                    cs.fill();

                    float xc = marg + 5;
                    for (int i = 0; i < ch.length; i++) {
                        cs.setNonStrokingColor(1, 1, 1);
                        cs.beginText();
                        cs.setFont(bold, 9);
                        cs.newLineAtOffset(xc, y - 16);
                        cs.showText(ch[i]);
                        cs.endText();
                        xc += cw[i];
                    }
                    y -= hdrH;

                    int start = pi * perPg;
                    int end = Math.min(start + perPg, lignes.size());
                    boolean alt = false;
                    for (int i = start; i < end; i++) {
                        Machine m = lignes.get(i);
                        if (alt) {
                            cs.setNonStrokingColor(0.95f, 0.97f, 0.99f);
                            cs.addRect(marg, y - rowH + 5, pageW - 2 * marg, rowH);
                            cs.fill();
                        }
                        alt = !alt;

                        String ev = s(m.getEtatM());
                        float[] ec = switch (ev.toLowerCase()) {
                            case "neuf", "disponible" -> new float[]{0.15f, 0.53f, 0.38f};
                            case "occasion", "bon" -> new float[]{0.90f, 0.49f, 0.13f};
                            case "en panne" -> new float[]{0.91f, 0.30f, 0.24f};
                            default -> new float[]{0.31f, 0.31f, 0.31f};
                        };

                        String ds = m.getDateAchat() != null ? m.getDateAchat().format(DATE_FMT) : "";
                        String[] vals = {s(m.getMarque()), s(m.getModele()), ev, s(m.getNumeroSerie()), ds, s(m.getNom())};
                        xc = marg + 5;
                        for (int c = 0; c < vals.length; c++) {
                            boolean ie = (c == 2);
                            float[] color = ie ? ec : new float[]{0.1f, 0.1f, 0.1f};
                            cs.setNonStrokingColor(color[0], color[1], color[2]);
                            cs.beginText();
                            cs.setFont(ie ? bold : plain, 9);
                            cs.newLineAtOffset(xc, y - 14);
                            String t = vals[c];
                            if (t.length() > 17) t = t.substring(0, 14) + "...";
                            cs.showText(t);
                            cs.endText();
                            xc += cw[c];
                        }

                        cs.setStrokingColor(0.88f, 0.88f, 0.88f);
                        cs.setLineWidth(0.3f);
                        cs.moveTo(marg, y - rowH + 5);
                        cs.lineTo(pageW - marg, y - rowH + 5);
                        cs.stroke();
                        y -= rowH;
                    }

                    if (pi == pages - 1) {
                        y -= 14;
                        cs.setLineWidth(0.5f);
                        cs.setStrokingColor(0.6f, 0.6f, 0.6f);
                        cs.moveTo(marg, y);
                        cs.lineTo(pageW - marg, y);
                        cs.stroke();
                        y -= 14;
                        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                        cs.beginText();
                        cs.setFont(bold, 9);
                        cs.newLineAtOffset(marg, y);
                        cs.showText("Stats - Total:" + masterList.size() + " | Neuves:" + lblNeuves.getText() + " | Occasion:" + lblOccasion.getText() + " | Pannes:" + lblEnPanne.getText());
                        cs.endText();
                    }
                }
            }
            doc.save(fichier);
            alerte("Export PDF", "Fichier sauvegarde :\n" + fichier.getPath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur PDF", "Export echoue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RAPPORT STATISTIQUES GRAPHIQUE PDF
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void genererRapportStatistiques() {
        File fichier = choisirFichier("PDF", "*.pdf", "rapport_stats_" + LocalDate.now() + ".pdf");
        if (fichier == null) return;

        long total = masterList.size();
        long neuves = compterEtats(new String[]{"neuf", "disponible"});
        long occasion = compterEtats(new String[]{"occasion", "bon"});
        long enPanne = compterEtatSimple("en panne");
        long autres = total - neuves - occasion - enPanne;

        float pageW = PDRectangle.A4.getWidth();
        float pageH = PDRectangle.A4.getHeight();
        float marg = 45f;

        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPage p1 = new PDPage(PDRectangle.A4);
            doc.addPage(p1);

            try (PDPageContentStream cs = new PDPageContentStream(doc, p1)) {
                float y = pageH - marg;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(0, y - 50, pageW, 60);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText();
                cs.setFont(bold, 20);
                cs.newLineAtOffset(marg, y - 20);
                cs.showText("AGROFLOW  -  RAPPORT STATISTIQUES");
                cs.endText();
                cs.beginText();
                cs.setFont(plain, 10);
                cs.newLineAtOffset(marg, y - 38);
                cs.showText("Genere le : " + LocalDate.now().format(DATE_FMT) + "  |  Total machines : " + total + "  |  Resultats filtres : " + sortedList.size());
                cs.endText();
                y -= 70;

                float[][] kpiColors = {{0.20f, 0.60f, 0.86f}, {0.15f, 0.68f, 0.38f}, {0.90f, 0.49f, 0.13f}, {0.91f, 0.30f, 0.24f}};
                String[] kpiLabels = {"TOTAL", "NEUVES/DISPO", "OCCASION/BON", "EN PANNE"};
                long[] kpiVals = {total, neuves, occasion, enPanne};
                float kpiW = (pageW - 2 * marg - 30) / 4;

                for (int i = 0; i < 4; i++) {
                    float kx = marg + i * (kpiW + 10);
                    cs.setNonStrokingColor(kpiColors[i][0], kpiColors[i][1], kpiColors[i][2]);
                    cs.addRect(kx, y - 65, kpiW, 68);
                    cs.fill();
                    cs.setNonStrokingColor(1f, 1f, 1f);
                    cs.beginText();
                    cs.setFont(bold, 28);
                    cs.newLineAtOffset(kx + 8, y - 34);
                    cs.showText(String.valueOf(kpiVals[i]));
                    cs.endText();
                    cs.beginText();
                    cs.setFont(plain, 10);
                    cs.newLineAtOffset(kx + 8, y - 50);
                    cs.showText(String.format("%.1f%%", pct(kpiVals[i], total)));
                    cs.endText();
                    cs.beginText();
                    cs.setFont(bold, 8);
                    cs.newLineAtOffset(kx + 8, y - 62);
                    cs.showText(kpiLabels[i]);
                    cs.endText();
                }
                y -= 85;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText();
                cs.setFont(bold, 13);
                cs.newLineAtOffset(marg, y);
                cs.showText("Repartition par etat");
                cs.endText();
                y -= 18;

                float maxBarW = pageW - 2 * marg - 120;
                long[] barVals = {neuves, occasion, enPanne, autres};
                String[] barLbls = {"Neuves / Disponibles", "Occasion / Bon etat", "En panne", "Autres etats"};
                float[][] barColors = {{0.15f, 0.68f, 0.38f}, {0.90f, 0.49f, 0.13f}, {0.91f, 0.30f, 0.24f}, {0.59f, 0.60f, 0.60f}};

                for (int i = 0; i < 4; i++) {
                    float bw = (total == 0) ? 0 : (float) (barVals[i] * maxBarW / total);
                    cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                    cs.beginText();
                    cs.setFont(plain, 10);
                    cs.newLineAtOffset(marg, y);
                    cs.showText(barLbls[i]);
                    cs.endText();
                    y -= 14;
                    cs.setNonStrokingColor(0.92f, 0.93f, 0.94f);
                    cs.addRect(marg, y - 2, maxBarW, 16);
                    cs.fill();
                    if (bw > 0) {
                        cs.setNonStrokingColor(barColors[i][0], barColors[i][1], barColors[i][2]);
                        cs.addRect(marg, y - 2, bw, 16);
                        cs.fill();
                    }
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.beginText();
                    cs.setFont(bold, 9);
                    cs.newLineAtOffset(marg + maxBarW + 5, y + 2);
                    cs.showText(barVals[i] + " (" + String.format("%.1f", pct(barVals[i], total)) + "%)");
                    cs.endText();
                    y -= 24;
                }
                y -= 20;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText();
                cs.setFont(bold, 13);
                cs.newLineAtOffset(marg, y);
                cs.showText("Distribution visuelle");
                cs.endText();
                y -= 25;

                float pieH = 40f;
                float totalW = pageW - 2 * marg;
                float cx = marg;
                for (int i = 0; i < 4; i++) {
                    if (barVals[i] == 0 || total == 0) continue;
                    float segW = (float) (barVals[i] * totalW / total);
                    cs.setNonStrokingColor(barColors[i][0], barColors[i][1], barColors[i][2]);
                    cs.addRect(cx, y - pieH, segW - 1, pieH);
                    cs.fill();
                    if (segW > 30) {
                        cs.setNonStrokingColor(1f, 1f, 1f);
                        cs.beginText();
                        cs.setFont(bold, 8);
                        cs.newLineAtOffset(cx + 3, y - pieH / 2 - 4);
                        cs.showText(String.format("%.0f%%", pct(barVals[i], total)));
                        cs.endText();
                    }
                    cx += segW;
                }
                y -= pieH + 12;

                String[] legLabels = {"Neuves/Dispo", "Occasion/Bon", "En panne", "Autres"};
                float lx = marg;
                for (int i = 0; i < 4; i++) {
                    cs.setNonStrokingColor(barColors[i][0], barColors[i][1], barColors[i][2]);
                    cs.addRect(lx, y - 10, 12, 12);
                    cs.fill();
                    cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                    cs.beginText();
                    cs.setFont(plain, 9);
                    cs.newLineAtOffset(lx + 16, y - 4);
                    cs.showText(legLabels[i]);
                    cs.endText();
                    lx += 110;
                }
                y -= 30;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText();
                cs.setFont(bold, 13);
                cs.newLineAtOffset(marg, y);
                cs.showText("Synthese chiffree");
                cs.endText();
                y -= 16;

                String[][] recap = {
                        {"Indicateur", "Valeur", "Pourcentage"},
                        {"Total machines", String.valueOf(total), "100.0%"},
                        {"Neuves / Disponibles", String.valueOf(neuves), String.format("%.1f%%", pct(neuves, total))},
                        {"Occasion / Bon etat", String.valueOf(occasion), String.format("%.1f%%", pct(occasion, total))},
                        {"En panne", String.valueOf(enPanne), String.format("%.1f%%", pct(enPanne, total))},
                        {"Autres etats", String.valueOf(autres), String.format("%.1f%%", pct(autres, total))},
                        {"Resultats filtres", String.valueOf(sortedList.size()), total > 0 ? String.format("%.1f%%", pct(sortedList.size(), total)) : "0%"},
                        {"Date generation", LocalDate.now().format(DATE_FMT), "---"}
                };

                for (int i = 0; i < recap.length; i++) {
                    if (i == 0) {
                        cs.setNonStrokingColor(0.22f, 0.34f, 0.42f);
                        cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 18);
                        cs.fill();
                        cs.setNonStrokingColor(1f, 1f, 1f);
                    } else if (i % 2 == 0) {
                        cs.setNonStrokingColor(0.95f, 0.97f, 1f);
                        cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 16);
                        cs.fill();
                        cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    } else {
                        cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    }
                    cs.beginText();
                    cs.setFont(i == 0 ? bold : plain, 9);
                    cs.newLineAtOffset(marg, y);
                    cs.showText(recap[i][0]);
                    cs.endText();
                    cs.beginText();
                    cs.setFont(bold, 9);
                    cs.newLineAtOffset(marg + 200, y);
                    cs.showText(recap[i][1]);
                    cs.endText();
                    cs.beginText();
                    cs.setFont(i == 0 ? bold : plain, 9);
                    cs.newLineAtOffset(marg + 310, y);
                    cs.showText(recap[i][2]);
                    cs.endText();
                    y -= 17;
                }

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(0, 0, pageW, marg - 10);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText();
                cs.setFont(plain, 8);
                cs.newLineAtOffset(marg, 14);
                cs.showText("AGROFLOW - Rapport Statistiques Machines - " + LocalDate.now().format(DATE_FMT) + "  |  Confidentiel");
                cs.endText();
            }

            doc.save(fichier);
            alerte("Rapport Statistiques", "Rapport genere :\n" + fichier.getPath(), Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            alerte("Erreur Stats", "Generation echouee : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        updateUserLabels();

        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);

        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());

        configurerColonnes();
        configurerListeFiltrée();
        chargerMachines();
    }

    private void chargerAvatarTopBar(Personne user) {
        if (user == null) return;

        if (userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }

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
                        if (avatarDefaultLabel != null) avatarDefaultLabel.setVisible(false);
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

    @FXML
    private void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setTitle("Mon Profil");
            s.setScene(new Scene(root));
            s.setResizable(true);
            s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen();
            s.showAndWait();
        } catch (IOException e) {
            showError("Erreur", e.getMessage());
        }
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            SessionManager.setCurrentUser(user);
            System.out.println("✓ setCurrentUser: " + user.getPrenom() + " " + user.getNom());
            updateUserLabels();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL");
        }
    }

    private void updateUserLabels() {
        if (currentUser == null) return;

        if (userNameLabel != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        }

        if (userRoleLabel != null) {
            String roleText = switch (currentUser.getRole()) {
                case 1 -> "🌾 AGRICOLE";
                case 2 -> "👷 EMPLOYÉ";
                case 3 -> "👑 ADMIN";
                default -> "Rôle inconnu";
            };
            userRoleLabel.setText(roleText);
        }
    }

    @FXML
    private void versAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/AjoutMachine.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void versModifier() {
        Machine machineSelectionnee = tableMachines.getSelectionModel().getSelectedItem();

        if (machineSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une machine à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/ModifierMachine.fxml"));
            Parent root = loader.load();

            ModifierMachineController controller = loader.getController();
            controller.setMachine(machineSelectionnee);

            Stage stage = (Stage) tableMachines.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        Machine machineSelectionnee = tableMachines.getSelectionModel().getSelectedItem();

        if (machineSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une machine à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la machine");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer la machine : " + machineSelectionnee.getNom() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                machineService.supprimer(machineSelectionnee.getIdM());
                afficherAlerte("Succès", "Machine supprimée avec succès", Alert.AlertType.INFORMATION);
                chargerMachines();
            } catch (Exception e) {
                afficherAlerte("Erreur", "Impossible de supprimer la machine", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // Navigation
    @FXML
    private void naviguerAnimaux(Event event) {
        naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml", event);
    }

    @FXML
    private void naviguerMateriels(Event event) {
        naviguerVers("/MaterielsInterface/AfficherMachines.fxml", event);
    }

    @FXML
    private void naviguerStocks(Event event) {
        naviguerVers("/StocksInterface/afficherarticle.fxml", event);
    }

    @FXML
    private void naviguerTerrains(Event event) {
        naviguerVers("/TerrainsInterface/AfficherTerrains.fxml", event);
    }

    @FXML
    private void naviguerEvenements(Event event) {
        naviguerVers("/EventsInterface/AccueilEvenement.fxml", event);
    }

    @FXML
    private void naviguerUsers(Event event) {
        naviguerVers("/UsersInterface/Acceuil.fxml", event);
    }

    private void naviguerVers(String fxmlPath, Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger la page : " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleMateriels(MouseEvent mouseEvent) {
        naviguerVers("/MaterielsInterface/AfficherMachines.fxml", mouseEvent);
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handlePersonnes(MouseEvent event) {
        naviguerVers("/UsersInterface/DahboardPersonne.fxml", event);
    }

    @FXML
    private void handleTaches(MouseEvent event) {
        naviguerVers("/UsersInterface/GestionTache.fxml", event);
    }

    @FXML
    private void handleAbonnements(MouseEvent event) {
        naviguerVers("/UsersInterface/GestionAbonnements.fxml", event);
    }

    @FXML
    private void handleOffres(MouseEvent event) {
        naviguerVers("/UsersInterface/GestionOffre.fxml", event);
    }

    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        naviguerVers("/UsersInterface/Acceuil.fxml", actionEvent);
    }

    public void handleAnimals(MouseEvent mouseEvent) {
        naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml", mouseEvent);
    }

    public void handleStocks(MouseEvent mouseEvent) {
        naviguerVers("/StocksInterface/afficherarticle.fxml", mouseEvent);
    }

    public void handleTerrains(MouseEvent mouseEvent) {
        naviguerVers("/TerrainsInterface/acceuilterrain.fxml", mouseEvent);
    }

    public void handleEvents(MouseEvent mouseEvent) {
        naviguerVers("/G-Evenements/Accueil.fxml", mouseEvent);
    }

    @FXML
    private void handleLogout() {
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
                Scene scene = stage.getScene();
                scene.setRoot(root);
                stage.show();
                SessionManager.clearSession();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= MÉTHODES UTILITAIRES =================
    private double pct(long v, long t) {
        return t == 0 ? 0.0 : (v * 100.0) / t;
    }

    private String s(String v) {
        return v == null ? "" : v;
    }

    private File choisirFichier(String type, String ext, String nomDefaut) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer en " + type);
        fc.setInitialFileName(nomDefaut);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers " + type, ext));
        return fc.showSaveDialog(tableMachines.getScene().getWindow());
    }

    private void alerte(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private HBox buildLegende() {
        HBox h = new HBox(18);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(0, 0, 6, 0));
        String[][] data = {{"Neuves/Disponibles", PIE_COLORS[0]}, {"Occasion/Bon", PIE_COLORS[1]}, {"En panne", PIE_COLORS[2]}, {"Autres etats", PIE_COLORS[3]}};
        for (String[] ld : data) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill:" + ld[1] + ";-fx-font-size:18px;");
            Label txt = new Label(ld[0]);
            txt.setStyle("-fx-font-size:12px;-fx-text-fill:#2c3e50;");
            HBox item = new HBox(4, dot, txt);
            item.setAlignment(Pos.CENTER_LEFT);
            h.getChildren().add(item);
        }
        return h;
    }

    private HBox buildCartes(long total, long neuves, long occasion, long enPanne) {
        HBox h = new HBox(12);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(10, 20, 10, 20));
        Object[][] data = {{"Total", total, "#3498db", total > 0 ? 100.0 : 0.0},
                {"Neuves/Dispo", neuves, "#27ae60", pct(neuves, total)},
                {"Occasion/Bon", occasion, "#e67e22", pct(occasion, total)},
                {"En panne", enPanne, "#e74c3c", pct(enPanne, total)}};
        for (Object[] cd : data) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(145);
            card.setPadding(new Insets(12, 10, 12, 10));
            card.setStyle("-fx-background-color:white;-fx-border-color:" + cd[2] + ";-fx-border-width:0 0 0 5;-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);");
            Label lN = new Label((String) cd[0]);
            lN.setStyle("-fx-font-size:11px;-fx-text-fill:#7f8c8d;-fx-font-weight:bold;");
            lN.setWrapText(true);
            Label lV = new Label(String.valueOf(cd[1]));
            lV.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:" + cd[2] + ";");
            Label lP = new Label(String.format("%.1f%%", (double) cd[3]));
            lP.setStyle("-fx-font-size:13px;-fx-text-fill:" + cd[2] + ";-fx-font-weight:bold;");
            card.getChildren().addAll(lN, lV, lP);
            h.getChildren().add(card);
        }
        return h;
    }

    private VBox buildBarre(long total, long neuves, long occasion, long enPanne, long autres) {
        HBox b = new HBox(0);
        b.setPrefHeight(18);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-radius:9;-fx-background-color:#ecf0f1;");
        if (total > 0) {
            long[] vals = {neuves, occasion, enPanne, autres};
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] <= 0) continue;
                Region seg = new Region();
                seg.setPrefWidth((vals[i] * 500.0) / total);
                seg.setPrefHeight(18);
                String r = (i == 0) ? "-fx-background-radius:9 0 0 9;" : (i == 3) ? "-fx-background-radius:0 9 9 0;" : "";
                seg.setStyle("-fx-background-color:" + PIE_COLORS[i] + ";" + r);
                b.getChildren().add(seg);
            }
        }
        VBox box = new VBox(4);
        box.setPadding(new Insets(0, 20, 0, 20));
        Label lbl = new Label("Repartition visuelle");
        lbl.setStyle("-fx-font-size:11px;-fx-text-fill:#7f8c8d;-fx-font-weight:bold;");
        box.getChildren().addAll(lbl, b);
        return box;
    }
}