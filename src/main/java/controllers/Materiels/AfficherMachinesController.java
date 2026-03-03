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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import services.Materiels.MachineService;
import utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AfficherMachinesController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;

    @FXML
    private TableView<Machine> tableMachines;

    @FXML
    private TableColumn<Machine, String> colMarque;

    @FXML
    private TableColumn<Machine, String> colModele;

    @FXML
    private TableColumn<Machine, String> colEtat;

    @FXML
    private TableColumn<Machine, String> colNumeroSerie;

    @FXML
    private TableColumn<Machine, LocalDate> colDateAchat;

    @FXML
    private TableColumn<Machine, String> colNom;


    private ObservableList<Machine> machinesList = FXCollections.observableArrayList();
    // ── Tableau ──────────────────────────────────────────────────────────────


    // ── Recherche & Filtres ───────────────────────────────────────────────────
    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboNom;
    @FXML private ComboBox<String> comboModele;

    // ── Statistiques ─────────────────────────────────────────────────────────
    @FXML private Label    lblTotal;
    @FXML private Label    lblNeuves;
    @FXML private Label    lblOccasion;
    @FXML private Label    lblEnPanne;
    @FXML private Label    lblFiltres;
    @FXML private PieChart pieStats;

    // ── Service ───────────────────────────────────────────────────────────────
    private final MachineService machineService = new MachineService();

    // ── Donnees ───────────────────────────────────────────────────────────────
    private final ObservableList<Machine> masterList   = FXCollections.observableArrayList();
    private FilteredList<Machine> filteredList;
    private SortedList<Machine> sortedList;

    private static final DateTimeFormatter DATE_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[]          PIE_COLORS = {":#27ae60",":#e67e22",":#e74c3c",":#95a5a6"};

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ════════════════════════════════════════════════════════════════════════



    private void configurerColonnes() {
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatM"));
        colNumeroSerie.setCellValueFactory(new PropertyValueFactory<>("numeroSerie"));
        colDateAchat.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                String style;
                switch (val.toLowerCase()) {
                    case "neuf":       style = "-fx-text-fill:#27ae60;-fx-font-weight:bold;"; break;
                    case "disponible": style = "-fx-text-fill:#2980b9;-fx-font-weight:bold;"; break;
                    case "occasion":   style = "-fx-text-fill:#e67e22;-fx-font-weight:bold;"; break;
                    case "bon":        style = "-fx-text-fill:#8e44ad;-fx-font-weight:bold;"; break;
                    case "en panne":   style = "-fx-text-fill:#e74c3c;-fx-font-weight:bold;"; break;
                    default:           style = "-fx-text-fill:#7f8c8d;";
                }
            }
        });

        colDateAchat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText((empty || val == null) ? null : val.format(DATE_FMT));
            }
        });
    }

    private void configurerListeFiltrée() {
        filteredList = new FilteredList<>(masterList, m -> true);
        sortedList   = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableMachines.comparatorProperty());
        tableMachines.setItems(sortedList);
        sortedList.addListener(
                (javafx.collections.ListChangeListener<Machine>) c -> majStatistiques());
    }

    private void chargerMachines() {
        try {
            masterList.clear();
            masterList.addAll(machineService.recuperer());
            peuplerCombos();
            majStatistiques();
            appliquerFiltres();
        } catch (Exception e) {
            alerte("Erreur chargement",
                    "Impossible de charger les machines :\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
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

    // ════════════════════════════════════════════════════════════════════════
    //  FILTRES & RECHERCHE
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void appliquerFiltres() {
        String texte  = champRecherche.getText() == null ? ""
                : champRecherche.getText().trim().toLowerCase();
        String nom    = comboNom.getValue();
        String modele = comboModele.getValue();

        filteredList.setPredicate(m -> {
            boolean okRecherche = texte.isEmpty()
                    || contient(m.getMarque(),      texte)
                    || contient(m.getModele(),      texte)
                    || contient(m.getEtatM(),       texte)
                    || contient(m.getNumeroSerie(), texte)
                    || contient(m.getNom(),         texte);

            boolean okNom = nom == null || nom.startsWith("Tous")
                    || nom.equalsIgnoreCase(m.getNom());

            boolean okModele = modele == null || modele.startsWith("Tous")
                    || modele.equalsIgnoreCase(m.getModele());

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

    // ════════════════════════════════════════════════════════════════════════
    //  TRI PAR DATE
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void trierDateDesc() {
        sortedList.comparatorProperty().unbind();
        sortedList.setComparator(Comparator.comparing(Machine::getDateAchat,
                Comparator.nullsLast(Comparator.reverseOrder())));
    }

    @FXML
    private void trierDateAsc() {
        sortedList.comparatorProperty().unbind();
        sortedList.setComparator(Comparator.comparing(Machine::getDateAchat,
                Comparator.nullsLast(Comparator.naturalOrder())));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STATISTIQUES
    // ════════════════════════════════════════════════════════════════════════

    private void majStatistiques() {
        long total    = masterList.size();
        long neuves   = compterEtat("neuf", "disponible");
        long occasion = compterEtat("occasion", "bon");
        long enPanne  = compterEtatSimple("en panne");
        long filtres  = sortedList.size();

        lblTotal.setText(String.valueOf(total));
        lblNeuves.setText(String.valueOf(neuves));
        lblOccasion.setText(String.valueOf(occasion));
        lblEnPanne.setText(String.valueOf(enPanne));
        lblFiltres.setText(String.valueOf(filtres));

        majPieChart(neuves, occasion, enPanne, total);
    }

    private long compterEtat(String e1, String e2) {
        return masterList.stream()
                .filter(m -> m.getEtatM() != null
                        && (m.getEtatM().equalsIgnoreCase(e1)
                        || m.getEtatM().equalsIgnoreCase(e2)))
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
        if (neuves   > 0) d.add(new PieChart.Data("Neuves/Dispo (" + neuves   + ")", neuves));
        if (occasion > 0) d.add(new PieChart.Data("Occasion/Bon (" + occasion + ")", occasion));
        if (enPanne  > 0) d.add(new PieChart.Data("En panne ("     + enPanne  + ")", enPanne));
        if (autres   > 0) d.add(new PieChart.Data("Autres ("       + autres   + ")", autres));
        if (d.isEmpty())  d.add(new PieChart.Data("Aucune donnee", 1));
        pieStats.setData(d);
        Platform.runLater(() -> appliquerCouleursPie(d, PIE_COLORS));
    }

    private void appliquerCouleursPie(ObservableList<PieChart.Data> data, String[] colors) {
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getNode() != null)
                data.get(i).getNode().setStyle(
                        "-fx-pie-color:" + colors[Math.min(i, colors.length - 1)] + ";");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  POPUP STATISTIQUES DETAILLEES
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void afficherStatistiques() {
        long total    = masterList.size();
        long neuves   = compterEtat("neuf", "disponible");
        long occasion = compterEtat("occasion", "bon");
        long enPanne  = compterEtatSimple("en panne");
        long autres   = total - neuves - occasion - enPanne;

        Label titre = new Label("Statistiques des Machines");
        titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        Label sousTitre = new Label("Total : " + total + "   |   Affiches : " + sortedList.size());
        sousTitre.setStyle("-fx-font-size:12px;-fx-text-fill:#7f8c8d;");

        ObservableList<PieChart.Data> pd = FXCollections.observableArrayList();
        if (total > 0) {
            if (neuves   > 0) pd.add(new PieChart.Data(
                    String.format("Neuves/Dispo\n%d (%.1f%%)", neuves,   pct(neuves,   total)), neuves));
            if (occasion > 0) pd.add(new PieChart.Data(
                    String.format("Occasion/Bon\n%d (%.1f%%)", occasion, pct(occasion, total)), occasion));
            if (enPanne  > 0) pd.add(new PieChart.Data(
                    String.format("En panne\n%d (%.1f%%)",     enPanne,  pct(enPanne,  total)), enPanne));
            if (autres   > 0) pd.add(new PieChart.Data(
                    String.format("Autres\n%d (%.1f%%)",       autres,   pct(autres,   total)), autres));
        } else {
            pd.add(new PieChart.Data("Aucune donnee", 1));
        }

        PieChart chart = new PieChart(pd);
        chart.setLabelsVisible(true); chart.setLegendVisible(false);
        chart.setPrefSize(500, 340);
        chart.setStyle("-fx-background-color:transparent;");
        Platform.runLater(() -> appliquerCouleursPie(pd, PIE_COLORS));

        Button btnFermer = new Button("Fermer");
        btnFermer.setPrefSize(140, 40);
        btnFermer.setStyle("-fx-background-color:#8e44ad;-fx-text-fill:white;"
                + "-fx-font-size:14px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-cursor:hand;");

        VBox header = new VBox(4, titre, sousTitre);
        header.setAlignment(Pos.CENTER);

        VBox layout = new VBox(14, header, new Separator(), chart,
                buildLegende(), new Separator(),
                buildCartes(total, neuves, occasion, enPanne),
                buildBarre(total, neuves, occasion, enPanne, autres),
                btnFermer);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24, 28, 24, 28));
        layout.setStyle("-fx-background-color:#f0f2f5;");

        Stage popup = new Stage();
        popup.setTitle("Statistiques - Gestion des Machines");
        popup.setScene(new Scene(layout, 620, 720));
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(tableMachines.getScene().getWindow());
        popup.setResizable(false);
        btnFermer.setOnAction(e -> popup.close());
        popup.show();
        Platform.runLater(() -> appliquerCouleursPie(pd, PIE_COLORS));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MINDSPHERE
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void ouvrirMindSpherePage() {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/MaterielsInterface/MindSpherePage.fxml");
            if (fxmlUrl == null)
                throw new IOException("Fichier MindSpherePage.fxml introuvable.");
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            alerte("Erreur MindSphere",
                    "Impossible d'ouvrir la page MindSphere :\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CRUD
    // ════════════════════════════════════════════════════════════════════════





    // ════════════════════════════════════════════════════════════════════════
    //  EXPORT PDF
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void exporterPDF() {
        File fichier = choisirFichier("PDF", "*.pdf", "machines_" + LocalDate.now() + ".pdf");
        if (fichier == null) return;

        List<Machine> lignes = List.copyOf(sortedList);
        float pageW = PDRectangle.A4.getWidth(), pageH = PDRectangle.A4.getHeight();
        float marg = 40f, rowH = 20f, hdrH = 26f;
        int perPg = (int)((pageH - marg * 3 - hdrH - 80) / rowH);
        int pages = Math.max(1, (int) Math.ceil((double) lignes.size() / perPg));
        float[] cw = {95, 85, 90, 120, 90, 35};
        String[] ch = {"Marque","Modele","Etat","N Serie","Date Achat","Nom"};

        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            for (int pi = 0; pi < pages; pi++) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = pageH - marg;
                    cs.beginText(); cs.setFont(bold, 16);
                    cs.newLineAtOffset(marg, y);
                    cs.showText("AGROFLOW - Gestion des Machines");
                    cs.endText(); y -= 20;

                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(marg, y);
                    cs.showText("Exporte le " + LocalDate.now().format(DATE_FMT)
                            + " | Resultats : " + lignes.size()
                            + " / Total : " + masterList.size()
                            + " | Page " + (pi + 1) + "/" + pages);
                    cs.endText(); y -= 14;

                    cs.setLineWidth(0.5f); cs.setStrokingColor(0.7f, 0.7f, 0.7f);
                    cs.moveTo(marg, y); cs.lineTo(pageW - marg, y); cs.stroke(); y -= 8;

                    cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                    cs.addRect(marg, y - hdrH + 5, pageW - 2 * marg, hdrH);
                    cs.fill();

                    float xc = marg + 5;
                    for (int i = 0; i < ch.length; i++) {
                        cs.setNonStrokingColor(1, 1, 1);
                        cs.beginText(); cs.setFont(bold, 9);
                        cs.newLineAtOffset(xc, y - 16);
                        cs.showText(ch[i]); cs.endText();
                        xc += cw[i];
                    }
                    y -= hdrH;

                    int start = pi * perPg, end = Math.min(start + perPg, lignes.size());
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
                            case "neuf"       -> new float[]{0.15f, 0.53f, 0.38f};
                            case "disponible" -> new float[]{0.16f, 0.50f, 0.73f};
                            case "occasion"   -> new float[]{0.90f, 0.49f, 0.13f};
                            case "bon"        -> new float[]{0.56f, 0.27f, 0.68f};
                            case "en panne"   -> new float[]{0.91f, 0.30f, 0.24f};
                            default           -> new float[]{0.31f, 0.31f, 0.31f};
                        };
                        String ds = m.getDateAchat() != null ? m.getDateAchat().format(DATE_FMT) : "";
                        String[] vals = {s(m.getMarque()), s(m.getModele()), ev,
                                s(m.getNumeroSerie()), ds, s(m.getNom())};
                        xc = marg + 5;
                        for (int c = 0; c < vals.length; c++) {
                            boolean ie = (c == 2);
                            float[] color = ie ? ec : new float[]{0.1f, 0.1f, 0.1f};
                            cs.setNonStrokingColor(color[0], color[1], color[2]);
                            cs.beginText(); cs.setFont(ie ? bold : plain, 9);
                            cs.newLineAtOffset(xc, y - 14);
                            String t = vals[c];
                            if (t.length() > 17) t = t.substring(0, 14) + "...";
                            cs.showText(t); cs.endText();
                            xc += cw[c];
                        }
                        cs.setStrokingColor(0.88f, 0.88f, 0.88f); cs.setLineWidth(0.3f);
                        cs.moveTo(marg, y - rowH + 5);
                        cs.lineTo(pageW - marg, y - rowH + 5); cs.stroke();
                        y -= rowH;
                    }

                    if (pi == pages - 1) {
                        y -= 14;
                        cs.setLineWidth(0.5f); cs.setStrokingColor(0.6f, 0.6f, 0.6f);
                        cs.moveTo(marg, y); cs.lineTo(pageW - marg, y); cs.stroke(); y -= 14;
                        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                        cs.beginText(); cs.setFont(bold, 9);
                        cs.newLineAtOffset(marg, y);
                        cs.showText("Stats - Total:" + masterList.size()
                                + " | Neuves:" + lblNeuves.getText()
                                + " | Occasion:" + lblOccasion.getText()
                                + " | Pannes:" + lblEnPanne.getText());
                        cs.endText();
                    }
                }
            }
            doc.save(fichier);
            alerte("Export PDF", "Fichier sauvegarde :\n" + fichier.getPath(),
                    Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur PDF", "Export echoue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXPORT EXCEL
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void exporterExcel() {
        File fichier = choisirFichier("Excel", "*.xlsx",
                "machines_" + LocalDate.now() + ".xlsx");
        if (fichier == null) return;
        List<Machine> lignes = List.copyOf(sortedList);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Machines");
            sheet.setDefaultColumnWidth(20);

            CellStyle csT = wb.createCellStyle(); Font fT = wb.createFont();
            fT.setBold(true); fT.setFontHeightInPoints((short) 14); csT.setFont(fT);

            CellStyle csH = wb.createCellStyle(); Font fH = wb.createFont();
            fH.setBold(true); fH.setColor(IndexedColors.WHITE.getIndex()); csH.setFont(fH);
            csH.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            csH.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csH.setAlignment(HorizontalAlignment.CENTER);
            csH.setBorderBottom(BorderStyle.THIN);

            CellStyle csA = wb.createCellStyle();
            csA.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            csA.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row r0 = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell c0 = r0.createCell(0);
            c0.setCellValue("AGROFLOW - Machines - Exporte le " + LocalDate.now().format(DATE_FMT));
            c0.setCellStyle(csT);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            sheet.createRow(1).createCell(0).setCellValue(
                    "Resultats : " + lignes.size() + " / Total : " + masterList.size());

            String[] hdr = {"Marque","Modele","Etat","N Serie","Date Achat","Nom"};
            Row rH = sheet.createRow(3);
            for (int i = 0; i < hdr.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = rH.createCell(i);
                c.setCellValue(hdr[i]); c.setCellStyle(csH);
            }

            for (int i = 0; i < lignes.size(); i++) {
                Machine m = lignes.get(i);
                Row row = sheet.createRow(i + 4);
                org.apache.poi.ss.usermodel.Cell ca = row.createCell(0);
                org.apache.poi.ss.usermodel.Cell cb = row.createCell(1);
                org.apache.poi.ss.usermodel.Cell cc = row.createCell(2);
                org.apache.poi.ss.usermodel.Cell cd = row.createCell(3);
                org.apache.poi.ss.usermodel.Cell ce = row.createCell(4);
                org.apache.poi.ss.usermodel.Cell cf = row.createCell(5);
                ca.setCellValue(s(m.getMarque()));
                cb.setCellValue(s(m.getModele()));
                cc.setCellValue(s(m.getEtatM()));
                cd.setCellValue(s(m.getNumeroSerie()));
                ce.setCellValue(m.getDateAchat() != null ? m.getDateAchat().format(DATE_FMT) : "");
                cf.setCellValue(s(m.getNom()));
                if (i % 2 == 0) {
                    ca.setCellStyle(csA); cb.setCellStyle(csA); cc.setCellStyle(csA);
                    cd.setCellStyle(csA); ce.setCellStyle(csA); cf.setCellStyle(csA);
                }
            }

            Sheet ss = wb.createSheet("Statistiques");
            ss.setDefaultColumnWidth(28);
            CellStyle csS = wb.createCellStyle(); Font fS = wb.createFont();
            fS.setBold(true); fS.setColor(IndexedColors.WHITE.getIndex()); csS.setFont(fS);
            csS.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            csS.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[][] stats = {
                    {"Indicateur",         "Valeur"},
                    {"Total",              String.valueOf(masterList.size())},
                    {"Neuves/Disponibles", lblNeuves.getText()},
                    {"Occasion/Bon",       lblOccasion.getText()},
                    {"En panne",           lblEnPanne.getText()},
                    {"Resultats affiches", lblFiltres.getText()},
                    {"Date export",        LocalDate.now().format(DATE_FMT)}
            };
            for (int i = 0; i < stats.length; i++) {
                Row row = ss.createRow(i);
                for (int j = 0; j < 2; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue(stats[i][j]);
                    if (i == 0) cell.setCellStyle(csS);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(fichier)) { wb.write(fos); }
            alerte("Export Excel", "Fichier sauvegarde :\n" + fichier.getPath()
                    + "\n(2 onglets : 'Machines' + 'Statistiques')", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur Excel", "Export echoue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ════════════════════════════════════════════════════════════════════════

    @FXML private void retourAccueil()      { naviguerVers("/MaterielsInterface/AccueilMateriel.fxml"); }


    private void naviguerVers(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) throw new IOException("FXML introuvable : " + path);
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            alerte("Navigation", "Impossible d'ouvrir : " + path + "\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ════════════════════════════════════════════════════════════════════════

    private double pct(long v, long t) { return t == 0 ? 0.0 : (v * 100.0) / t; }
    private String s(String v)         { return v == null ? "" : v; }

    private File choisirFichier(String type, String ext, String nomDefaut) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer en " + type);
        fc.setInitialFileName(nomDefaut);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers " + type, ext));
        return fc.showSaveDialog(tableMachines.getScene().getWindow());
    }

    private void alerte(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }

    private HBox buildLegende() {
        HBox h = new HBox(18);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(0, 0, 6, 0));
        String[][] data = {
                {"Neuves/Disponibles", PIE_COLORS[0]},
                {"Occasion/Bon",       PIE_COLORS[1]},
                {"En panne",           PIE_COLORS[2]},
                {"Autres etats",       PIE_COLORS[3]}
        };
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
        Object[][] data = {
                {"Total",        total,    "#3498db", total > 0 ? 100.0 : 0.0},
                {"Neuves/Dispo", neuves,   "#27ae60", pct(neuves,   total)},
                {"Occasion/Bon", occasion, "#e67e22", pct(occasion, total)},
                {"En panne",     enPanne,  "#e74c3c", pct(enPanne,  total)}
        };
        for (Object[] cd : data) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(145);
            card.setPadding(new Insets(12, 10, 12, 10));
            card.setStyle("-fx-background-color:white;-fx-border-color:" + cd[2]
                    + ";-fx-border-width:0 0 0 5;-fx-background-radius:10;"
                    + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);");
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
        b.setPrefHeight(18); b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-radius:9;-fx-background-color:#ecf0f1;");
        if (total > 0) {
            long[] vals = {neuves, occasion, enPanne, autres};
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] <= 0) continue;
                Region seg = new Region();
                seg.setPrefWidth((vals[i] * 500.0) / total);
                seg.setPrefHeight(18);
                String r = (i == 0) ? "-fx-background-radius:9 0 0 9;"
                        : (i == 3) ? "-fx-background-radius:0 9 9 0;" : "";
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

    // ════════════════════════════════════════════════════════════════════════
    //  GENERER FACTURE PDF
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void genererFacture() {
        Machine sel = tableMachines.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerte("Attention", "Selectionnez une machine pour generer sa facture.", Alert.AlertType.WARNING);
            return;
        }

        File fichier = choisirFichier("PDF", "*.pdf",
                "facture_" + sel.getNumeroSerie() + "_" + LocalDate.now() + ".pdf");
        if (fichier == null) return;

        float pageW = PDRectangle.A4.getWidth(), pageH = PDRectangle.A4.getHeight();
        float marg = 50f;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain   = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font oblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = pageH - marg;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(marg - 10, y - 70, pageW - 2 * marg + 20, 75);
                cs.fill();

                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 22);
                cs.newLineAtOffset(marg, y - 30);
                cs.showText("AGROFLOW"); cs.endText();

                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(marg, y - 48);
                cs.showText("Gestion Agricole Intelligente"); cs.endText();

                cs.beginText(); cs.setFont(plain, 9);
                cs.newLineAtOffset(marg, y - 62);
                cs.showText("contact@agroflow.tn  |  www.agroflow.tn  |  +216 XX XXX XXX"); cs.endText();

                String numFacture = "FAC-" + LocalDate.now().getYear()
                        + String.format("%04d", (int)(Math.random() * 9999 + 1));
                cs.setNonStrokingColor(0.96f, 0.76f, 0.15f);
                cs.beginText(); cs.setFont(bold, 13);
                cs.newLineAtOffset(pageW - marg - 120, y - 30);
                cs.showText("FACTURE"); cs.endText();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(pageW - marg - 120, y - 46);
                cs.showText("N " + numFacture); cs.endText();
                cs.beginText(); cs.setFont(plain, 9);
                cs.newLineAtOffset(pageW - marg - 120, y - 59);
                cs.showText("Date : " + LocalDate.now().format(DATE_FMT)); cs.endText();

                y -= 90;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText(); cs.setFont(bold, 15);
                cs.newLineAtOffset(marg, y);
                cs.showText("FACTURE D'ACQUISITION DE MACHINE"); cs.endText();
                y -= 6;
                cs.setStrokingColor(0.17f, 0.24f, 0.31f);
                cs.setLineWidth(2f);
                cs.moveTo(marg, y); cs.lineTo(pageW - marg, y); cs.stroke();
                y -= 20;

                cs.setNonStrokingColor(0.27f, 0.60f, 0.38f);
                cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 20);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 10);
                cs.newLineAtOffset(marg, y + 2);
                cs.showText("DETAILS DE LA MACHINE"); cs.endText();
                y -= 18;

                String[][] infos = {
                        {"Marque",          s(sel.getMarque())},
                        {"Modele",          s(sel.getModele())},
                        {"Numero de serie", s(sel.getNumeroSerie())},
                        {"Etat",            s(sel.getEtatM())},
                        {"Date d achat",    sel.getDateAchat() != null ? sel.getDateAchat().format(DATE_FMT) : "N/A"},
                        {"Responsable",     s(sel.getNom())}
                };

                boolean alt = false;
                for (String[] info : infos) {
                    if (alt) {
                        cs.setNonStrokingColor(0.95f, 0.97f, 0.99f);
                        cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 18);
                        cs.fill();
                    }
                    alt = !alt;
                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    cs.beginText(); cs.setFont(bold, 10);
                    cs.newLineAtOffset(marg, y);
                    cs.showText(info[0] + " :"); cs.endText();

                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.beginText(); cs.setFont(plain, 10);
                    cs.newLineAtOffset(marg + 140, y);
                    cs.showText(info[1]); cs.endText();
                    y -= 18;
                }
                y -= 16;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 20);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 10);
                cs.newLineAtOffset(marg, y + 2);
                cs.showText("RECAPITULATIF FINANCIER"); cs.endText();
                y -= 20;

                float[] cw2 = {200, 80, 80, 80};
                String[] hdr2 = {"Designation", "Qte", "P.U (TND)", "Total (TND)"};
                float xc = marg;
                cs.setNonStrokingColor(0.85f, 0.90f, 0.95f);
                cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 18);
                cs.fill();
                cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                for (int i = 0; i < hdr2.length; i++) {
                    cs.beginText(); cs.setFont(bold, 9);
                    cs.newLineAtOffset(xc, y);
                    cs.showText(hdr2[i]); cs.endText();
                    xc += cw2[i];
                }
                y -= 18;

                xc = marg;
                String[] row1 = {s(sel.getMarque()) + " " + s(sel.getModele()), "1", "Sur devis", "Sur devis"};
                cs.setNonStrokingColor(0.95f, 0.97f, 0.99f);
                cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 18);
                cs.fill();
                cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                for (int i = 0; i < row1.length; i++) {
                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(xc, y);
                    cs.showText(row1[i]); cs.endText();
                    xc += cw2[i];
                }
                y -= 40;

                cs.setLineWidth(0.5f); cs.setStrokingColor(0.7f, 0.7f, 0.7f);
                cs.moveTo(pageW - marg - 160, y + 15); cs.lineTo(pageW - marg, y + 15); cs.stroke();
                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(pageW - marg - 160, y);
                cs.showText("Sous-total HT :"); cs.endText();
                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(pageW - marg - 40, y);
                cs.showText("Sur devis"); cs.endText();
                y -= 16;

                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(pageW - marg - 160, y);
                cs.showText("TVA (19%) :"); cs.endText();
                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(pageW - marg - 40, y);
                cs.showText("---"); cs.endText();
                y -= 16;

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(pageW - marg - 165, y - 4, 170, 20);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 11);
                cs.newLineAtOffset(pageW - marg - 160, y);
                cs.showText("TOTAL TTC :"); cs.endText();
                cs.beginText(); cs.setFont(bold, 11);
                cs.newLineAtOffset(pageW - marg - 40, y);
                cs.showText("Sur devis"); cs.endText();
                y -= 40;

                cs.setLineWidth(1f); cs.setStrokingColor(0.17f, 0.24f, 0.31f);
                cs.moveTo(marg, marg + 50); cs.lineTo(pageW - marg, marg + 50); cs.stroke();
                cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                cs.beginText(); cs.setFont(oblique, 8);
                cs.newLineAtOffset(marg, marg + 36);
                cs.showText("Ce document est genere automatiquement par AGROFLOW - Systeme de Gestion Agricole"); cs.endText();
                cs.beginText(); cs.setFont(plain, 8);
                cs.newLineAtOffset(marg, marg + 22);
                cs.showText("Numero de facture : " + numFacture + "  |  Emise le : " + LocalDate.now().format(DATE_FMT)); cs.endText();
                cs.beginText(); cs.setFont(plain, 8);
                cs.newLineAtOffset(marg, marg + 10);
                cs.showText("Signature & cachet de l entreprise : ____________________"); cs.endText();

                cs.beginText(); cs.setFont(plain, 8);
                cs.newLineAtOffset(pageW - marg - 140, marg + 22);
                cs.showText("Signature du client :"); cs.endText();
                cs.beginText(); cs.setFont(plain, 8);
                cs.newLineAtOffset(pageW - marg - 140, marg + 10);
                cs.showText("____________________"); cs.endText();
            }

            doc.save(fichier);
            alerte("Facture generee",
                    "Facture sauvegardee :\n" + fichier.getPath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur Facture", "Generation echouee : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RAPPORT MENSUEL DES ACQUISITIONS
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void genererRapportMensuel() {
        File fichier = choisirFichier("PDF", "*.pdf",
                "rapport_mensuel_" + LocalDate.now().getYear()
                        + "_" + String.format("%02d", LocalDate.now().getMonthValue()) + ".pdf");
        if (fichier == null) return;

        java.util.Map<String, List<Machine>> parMois = masterList.stream()
                .filter(m -> m.getDateAchat() != null)
                .collect(Collectors.groupingBy(m -> {
                    LocalDate d = m.getDateAchat();
                    return String.format("%04d-%02d", d.getYear(), d.getMonthValue());
                }));

        List<String> moisTries = parMois.keySet().stream()
                .sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        float pageW = PDRectangle.A4.getWidth(), pageH = PDRectangle.A4.getHeight();
        float marg = 45f, rowH = 18f;

        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPage cover = new PDPage(PDRectangle.A4);
            doc.addPage(cover);
            try (PDPageContentStream cs = new PDPageContentStream(doc, cover)) {
                cs.setNonStrokingColor(0.11f, 0.16f, 0.19f);
                cs.addRect(0, pageH * 0.55f, pageW, pageH * 0.45f); cs.fill();
                cs.setNonStrokingColor(0.15f, 0.60f, 0.38f);
                cs.addRect(0, pageH * 0.48f, pageW, pageH * 0.08f); cs.fill();
                cs.setNonStrokingColor(0.95f, 0.97f, 1f);
                cs.addRect(0, 0, pageW, pageH * 0.48f); cs.fill();

                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 36);
                cs.newLineAtOffset(marg, pageH * 0.55f + 100);
                cs.showText("AGROFLOW"); cs.endText();

                cs.setNonStrokingColor(0.60f, 0.93f, 0.68f);
                cs.beginText(); cs.setFont(plain, 14);
                cs.newLineAtOffset(marg, pageH * 0.55f + 72);
                cs.showText("Gestion Agricole Intelligente"); cs.endText();

                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText(); cs.setFont(bold, 26);
                cs.newLineAtOffset(marg, pageH * 0.48f - 60);
                cs.showText("RAPPORT MENSUEL"); cs.endText();
                cs.beginText(); cs.setFont(bold, 20);
                cs.newLineAtOffset(marg, pageH * 0.48f - 90);
                cs.showText("DES ACQUISITIONS MACHINES"); cs.endText();

                cs.beginText(); cs.setFont(plain, 12);
                cs.newLineAtOffset(marg, pageH * 0.48f - 120);
                cs.showText("Annee " + LocalDate.now().getYear()
                        + "  |  Genere le " + LocalDate.now().format(DATE_FMT)); cs.endText();

                cs.setNonStrokingColor(0.27f, 0.60f, 0.38f);
                cs.addRect(marg - 5, pageH * 0.48f - 190, pageW - 2 * marg + 10, 55);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 11);
                cs.newLineAtOffset(marg, pageH * 0.48f - 148);
                cs.showText("SYNTHESE GLOBALE"); cs.endText();
                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(marg, pageH * 0.48f - 164);
                cs.showText("Total machines : " + masterList.size()
                        + "    |    Mois analyses : " + moisTries.size()
                        + "    |    Neuves : " + lblNeuves.getText()
                        + "    |    En panne : " + lblEnPanne.getText()); cs.endText();
            }

            String[] moisNoms = {"","Janvier","Fevrier","Mars","Avril","Mai","Juin",
                    "Juillet","Aout","Septembre","Octobre","Novembre","Decembre"};

            for (String moisKey : moisTries) {
                List<Machine> mMachines = parMois.get(moisKey);
                String[] parts = moisKey.split("-");
                String moisLabel = moisNoms[Integer.parseInt(parts[1])] + " " + parts[0];

                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float y = pageH - marg;

                    cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                    cs.addRect(marg - 5, y - 35, pageW - 2 * marg + 10, 40);
                    cs.fill();
                    cs.setNonStrokingColor(0.96f, 0.76f, 0.15f);
                    cs.beginText(); cs.setFont(bold, 16);
                    cs.newLineAtOffset(marg, y - 24);
                    cs.showText(moisLabel.toUpperCase()); cs.endText();
                    cs.setNonStrokingColor(1f, 1f, 1f);
                    cs.beginText(); cs.setFont(plain, 10);
                    cs.newLineAtOffset(pageW - marg - 80, y - 24);
                    cs.showText(mMachines.size() + " machine(s)"); cs.endText();
                    y -= 52;

                    long nv = mMachines.stream().filter(m -> m.getEtatM() != null
                            && (m.getEtatM().equalsIgnoreCase("neuf") || m.getEtatM().equalsIgnoreCase("disponible"))).count();
                    long oc = mMachines.stream().filter(m -> m.getEtatM() != null
                            && (m.getEtatM().equalsIgnoreCase("occasion") || m.getEtatM().equalsIgnoreCase("bon"))).count();
                    long ep = mMachines.stream().filter(m -> m.getEtatM() != null
                            && m.getEtatM().equalsIgnoreCase("en panne")).count();

                    cs.setNonStrokingColor(0.17f, 0.53f, 0.25f);
                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(marg, y);
                    cs.showText("Neuves/Dispo : " + nv); cs.endText();
                    cs.setNonStrokingColor(0.90f, 0.49f, 0.13f);
                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(marg + 130, y);
                    cs.showText("Occasion/Bon : " + oc); cs.endText();
                    cs.setNonStrokingColor(0.91f, 0.30f, 0.24f);
                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(marg + 260, y);
                    cs.showText("En panne : " + ep); cs.endText();
                    y -= 16;

                    cs.setStrokingColor(0.8f, 0.8f, 0.8f); cs.setLineWidth(0.5f);
                    cs.moveTo(marg, y); cs.lineTo(pageW - marg, y); cs.stroke();
                    y -= 10;

                    float[] cw3 = {90, 85, 80, 120, 85, 45};
                    String[] hdr3 = {"Marque","Modele","Etat","N Serie","Date Achat","Nom"};
                    cs.setNonStrokingColor(0.22f, 0.34f, 0.42f);
                    cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, 18);
                    cs.fill();
                    float xc = marg;
                    cs.setNonStrokingColor(1f, 1f, 1f);
                    for (int i = 0; i < hdr3.length; i++) {
                        cs.beginText(); cs.setFont(bold, 9);
                        cs.newLineAtOffset(xc, y);
                        cs.showText(hdr3[i]); cs.endText();
                        xc += cw3[i];
                    }
                    y -= 18;

                    boolean alt = false;
                    for (Machine m : mMachines) {
                        if (alt) {
                            cs.setNonStrokingColor(0.95f, 0.97f, 1f);
                            cs.addRect(marg - 5, y - 4, pageW - 2 * marg + 10, rowH);
                            cs.fill();
                        }
                        alt = !alt;
                        String ds = m.getDateAchat() != null ? m.getDateAchat().format(DATE_FMT) : "";
                        String[] vals = {s(m.getMarque()), s(m.getModele()), s(m.getEtatM()),
                                s(m.getNumeroSerie()), ds, s(m.getNom())};
                        xc = marg;
                        for (int i = 0; i < vals.length; i++) {
                            boolean isEtat = (i == 2);
                            if (isEtat) {
                                float[] ec = switch (vals[i].toLowerCase()) {
                                    case "neuf","disponible" -> new float[]{0.15f,0.53f,0.38f};
                                    case "occasion","bon"    -> new float[]{0.90f,0.49f,0.13f};
                                    case "en panne"          -> new float[]{0.91f,0.30f,0.24f};
                                    default                  -> new float[]{0.3f,0.3f,0.3f};
                                };
                                cs.setNonStrokingColor(ec[0], ec[1], ec[2]);
                                cs.beginText(); cs.setFont(bold, 9);
                            } else {
                                cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                                cs.beginText(); cs.setFont(plain, 9);
                            }
                            cs.newLineAtOffset(xc, y);
                            String t = vals[i]; if (t.length() > 15) t = t.substring(0, 13) + "..";
                            cs.showText(t); cs.endText();
                            xc += cw3[i];
                        }
                        y -= rowH;
                        if (y < marg + 40) break;
                    }
                }
            }

            PDPage lastPage = new PDPage(PDRectangle.A4);
            doc.addPage(lastPage);
            try (PDPageContentStream cs = new PDPageContentStream(doc, lastPage)) {
                float y = pageH - marg;
                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(marg - 5, y - 35, pageW - 2 * marg + 10, 40);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 16);
                cs.newLineAtOffset(marg, y - 24);
                cs.showText("RECAPITULATIF ANNUEL - " + LocalDate.now().getYear()); cs.endText();
                y -= 55;

                String[] mn = {"","Jan","Fev","Mar","Avr","Mai","Jun","Jul","Aou","Sep","Oct","Nov","Dec"};
                for (String moisKey : moisTries) {
                    List<Machine> ml = parMois.get(moisKey);
                    String[] p = moisKey.split("-");
                    String lbl = mn[Integer.parseInt(p[1])] + " " + p[0];

                    float barW = (ml.size() * 1.0f / masterList.size()) * (pageW - 2 * marg - 60);
                    cs.setNonStrokingColor(0.16f, 0.50f, 0.73f);
                    cs.addRect(marg + 55, y - 12, Math.max(barW, 5), 14); cs.fill();
                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(marg, y - 4); cs.showText(lbl); cs.endText();
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.beginText(); cs.setFont(bold, 9);
                    cs.newLineAtOffset(marg + 57 + barW, y - 4);
                    cs.showText(" " + ml.size()); cs.endText();
                    y -= 20;
                    if (y < marg + 40) break;
                }
            }

            doc.save(fichier);
            alerte("Rapport Mensuel",
                    "Rapport sauvegarde :\n" + fichier.getPath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur Rapport", "Generation echouee : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RAPPORT STATISTIQUES GRAPHIQUE PDF
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void genererRapportStatistiques() {
        File fichier = choisirFichier("PDF", "*.pdf",
                "rapport_stats_" + LocalDate.now() + ".pdf");
        if (fichier == null) return;

        long total    = masterList.size();
        long neuves   = compterEtat("neuf", "disponible");
        long occasion = compterEtat("occasion", "bon");
        long enPanne  = compterEtatSimple("en panne");
        long autres   = total - neuves - occasion - enPanne;

        float pageW = PDRectangle.A4.getWidth(), pageH = PDRectangle.A4.getHeight();
        float marg = 45f;

        try (PDDocument doc = new PDDocument()) {
            PDType1Font bold  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPage p1 = new PDPage(PDRectangle.A4);
            doc.addPage(p1);

            try (PDPageContentStream cs = new PDPageContentStream(doc, p1)) {
                float y = pageH - marg;

                // ── En-tete ───────────────────────────────────────────────────
                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(0, y - 50, pageW, 60);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(bold, 20);
                cs.newLineAtOffset(marg, y - 20);
                cs.showText("AGROFLOW  -  RAPPORT STATISTIQUES");
                cs.endText();
                cs.beginText(); cs.setFont(plain, 10);
                cs.newLineAtOffset(marg, y - 38);
                cs.showText("Genere le : " + LocalDate.now().format(DATE_FMT)
                        + "  |  Total machines : " + total
                        + "  |  Resultats filtres : " + sortedList.size());
                cs.endText();
                y -= 70;

                // ── KPI Cards ─────────────────────────────────────────────────
                float[][] kpiColors = {
                        {0.20f, 0.60f, 0.86f},
                        {0.15f, 0.68f, 0.38f},
                        {0.90f, 0.49f, 0.13f},
                        {0.91f, 0.30f, 0.24f}
                };
                String[] kpiLabels = {"TOTAL", "NEUVES/DISPO", "OCCASION/BON", "EN PANNE"};
                long[]   kpiVals   = {total, neuves, occasion, enPanne};
                float    kpiW      = (pageW - 2 * marg - 30) / 4;

                for (int i = 0; i < 4; i++) {
                    float kx = marg + i * (kpiW + 10);
                    cs.setNonStrokingColor(kpiColors[i][0], kpiColors[i][1], kpiColors[i][2]);
                    cs.addRect(kx, y - 65, kpiW, 68);
                    cs.fill();
                    cs.setNonStrokingColor(1f, 1f, 1f);
                    cs.beginText(); cs.setFont(bold, 28);
                    cs.newLineAtOffset(kx + 8, y - 34);
                    cs.showText(String.valueOf(kpiVals[i]));
                    cs.endText();
                    cs.beginText(); cs.setFont(plain, 10);
                    cs.newLineAtOffset(kx + 8, y - 50);
                    cs.showText(String.format("%.1f%%", pct(kpiVals[i], total)));
                    cs.endText();
                    cs.beginText(); cs.setFont(bold, 8);
                    cs.newLineAtOffset(kx + 8, y - 62);
                    cs.showText(kpiLabels[i]);
                    cs.endText();
                }
                y -= 85;

                // ── Barres horizontales ───────────────────────────────────────
                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText(); cs.setFont(bold, 13);
                cs.newLineAtOffset(marg, y);
                cs.showText("Repartition par etat");
                cs.endText();
                y -= 18;

                float maxBarW = pageW - 2 * marg - 120;
                long[]    barVals   = {neuves, occasion, enPanne, autres};
                String[]  barLbls   = {"Neuves / Disponibles", "Occasion / Bon etat", "En panne", "Autres etats"};
                float[][] barColors = {
                        {0.15f, 0.68f, 0.38f},
                        {0.90f, 0.49f, 0.13f},
                        {0.91f, 0.30f, 0.24f},
                        {0.59f, 0.60f, 0.60f}
                };

                for (int i = 0; i < 4; i++) {
                    float bw = (total == 0) ? 0 : (float)(barVals[i] * maxBarW / total);
                    cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                    cs.beginText(); cs.setFont(plain, 10);
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
                    cs.beginText(); cs.setFont(bold, 9);
                    cs.newLineAtOffset(marg + maxBarW + 5, y + 2);
                    cs.showText(barVals[i] + " (" + String.format("%.1f", pct(barVals[i], total)) + "%)");
                    cs.endText();
                    y -= 24;
                }
                y -= 20;

                // ── Distribution visuelle ─────────────────────────────────────
                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText(); cs.setFont(bold, 13);
                cs.newLineAtOffset(marg, y);
                cs.showText("Distribution visuelle");
                cs.endText();
                y -= 25;

                float pieH   = 40f;
                float totalW = pageW - 2 * marg;
                float cx = marg;
                for (int i = 0; i < 4; i++) {
                    if (barVals[i] == 0 || total == 0) continue;
                    float segW = (float)(barVals[i] * totalW / total);
                    cs.setNonStrokingColor(barColors[i][0], barColors[i][1], barColors[i][2]);
                    cs.addRect(cx, y - pieH, segW - 1, pieH);
                    cs.fill();
                    if (segW > 30) {
                        cs.setNonStrokingColor(1f, 1f, 1f);
                        cs.beginText(); cs.setFont(bold, 8);
                        cs.newLineAtOffset(cx + 3, y - pieH / 2 - 4);
                        cs.showText(String.format("%.0f%%", pct(barVals[i], total)));
                        cs.endText();
                    }
                    cx += segW;
                }
                y -= pieH + 12;

                // Legende
                String[] legLabels = {"Neuves/Dispo", "Occasion/Bon", "En panne", "Autres"};
                float lx = marg;
                for (int i = 0; i < 4; i++) {
                    cs.setNonStrokingColor(barColors[i][0], barColors[i][1], barColors[i][2]);
                    cs.addRect(lx, y - 10, 12, 12);
                    cs.fill();
                    cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                    cs.beginText(); cs.setFont(plain, 9);
                    cs.newLineAtOffset(lx + 16, y - 4);
                    cs.showText(legLabels[i]);
                    cs.endText();
                    lx += 110;
                }
                y -= 30;

                // ── Tableau recapitulatif ─────────────────────────────────────
                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.beginText(); cs.setFont(bold, 13);
                cs.newLineAtOffset(marg, y);
                cs.showText("Synthese chiffree");
                cs.endText();
                y -= 16;

                String[][] recap = {
                        {"Indicateur",           "Valeur",                          "Pourcentage"},
                        {"Total machines",        String.valueOf(total),              "100.0%"},
                        {"Neuves / Disponibles",  String.valueOf(neuves),             String.format("%.1f%%", pct(neuves,   total))},
                        {"Occasion / Bon etat",   String.valueOf(occasion),           String.format("%.1f%%", pct(occasion, total))},
                        {"En panne",              String.valueOf(enPanne),            String.format("%.1f%%", pct(enPanne,  total))},
                        {"Autres etats",          String.valueOf(autres),             String.format("%.1f%%", pct(autres,   total))},
                        {"Resultats filtres",      String.valueOf(sortedList.size()),  total > 0 ? String.format("%.1f%%", pct(sortedList.size(), total)) : "0%"},
                        {"Date generation",       LocalDate.now().format(DATE_FMT),   "---"}
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
                    cs.beginText(); cs.setFont(i == 0 ? bold : plain, 9);
                    cs.newLineAtOffset(marg, y);
                    cs.showText(recap[i][0]);
                    cs.endText();
                    cs.beginText(); cs.setFont(bold, 9);
                    cs.newLineAtOffset(marg + 200, y);
                    cs.showText(recap[i][1]);
                    cs.endText();
                    cs.beginText(); cs.setFont(i == 0 ? bold : plain, 9);
                    cs.newLineAtOffset(marg + 310, y);
                    cs.showText(recap[i][2]);
                    cs.endText();
                    y -= 17;
                }

                // ── Pied de page ──────────────────────────────────────────────
                cs.setNonStrokingColor(0.17f, 0.24f, 0.31f);
                cs.addRect(0, 0, pageW, marg - 10);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText(); cs.setFont(plain, 8);
                cs.newLineAtOffset(marg, 14);
                cs.showText("AGROFLOW - Rapport Statistiques Machines - "
                        + LocalDate.now().format(DATE_FMT) + "  |  Confidentiel");
                cs.endText();
            }

            doc.save(fichier);
            alerte("Rapport Statistiques",
                    "Rapport genere :\n" + fichier.getPath(), Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            alerte("Erreur Stats", "Generation echouee : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    } // fin genererRapportStatistiques
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
            // Cacher submenu par défaut
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);

            // 1. Hover sur le bouton Gestion → Ouvre submenu
            gestionBtn.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });

            // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
            gestionContainer.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });
        configurerColonnes();
        configurerListeFiltrée();
        chargerMachines();
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


    @FXML
    private void versAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/AjoutMachine.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
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

            // Passer la machine sélectionnée au contrôleur de modification
            ModifierMachineController controller = loader.getController();
            controller.setMachine(machineSelectionnee);

            Stage stage = (Stage) tableMachines.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
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

        try {
            machineService.supprimer(machineSelectionnee.getIdM());
            afficherAlerte("Succès", "Machine supprimée avec succès", Alert.AlertType.INFORMATION);
            chargerMachines(); // Rafraîchir la table
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de supprimer la machine", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }



    // Navigation vers les autres pages
    @FXML
    private void naviguerAnimaux(Event event) {
        naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",event);
    }

    @FXML
    private void naviguerMateriels(Event event) {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml", event);
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

    private void naviguerVers(String fxmlPath , Event event) {
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

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //naviguer vers les autres modules

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.naviguerVers("/UsersInterface/GestionAbonnements.fxml",event);}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.naviguerVers("/UsersInterface/GestionOffre.fxml",event);}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.naviguerVers("/UsersInterface/Acceuil.fxml", actionEvent);

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",mouseEvent);

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.naviguerVers("/StocksInterface/afficherarticle.fxml",mouseEvent);
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.naviguerVers("/TerrainsInterface/acceuilterrain.fxml",mouseEvent);
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.naviguerVers("/G-Evenements/Accueil.fxml",mouseEvent);
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",mouseEvent);
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
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // ================= ALERT METHODS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}