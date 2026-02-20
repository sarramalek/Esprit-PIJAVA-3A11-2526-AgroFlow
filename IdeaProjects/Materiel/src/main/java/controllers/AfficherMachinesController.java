package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entities.Machine;
import services.MachineService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║     AfficherMachinesController — VERSION COMPLÈTE               ║
 * ║  ✅ PieChart miniature dans les stats                            ║
 * ║  ✅ Popup Statistiques avec PieChart détaillé + pourcentages     ║
 * ║  ✅ Bouton "📊 STATISTIQUES" dans la barre CRUD                  ║
 * ║  ✅ lblEnPanne + Export PDF/Excel                                ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class AfficherMachinesController {

    // ── Tableau ────────────────────────────────────────────────────
    @FXML private TableView<Machine>              tableMachines;
    @FXML private TableColumn<Machine, String>    colMarque;
    @FXML private TableColumn<Machine, String>    colModele;
    @FXML private TableColumn<Machine, String>    colEtat;
    @FXML private TableColumn<Machine, String>    colNumeroSerie;
    @FXML private TableColumn<Machine, LocalDate> colDateAchat;
    @FXML private TableColumn<Machine, String>    colNom;

    // ── Recherche & Filtres ────────────────────────────────────────
    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboEtat;

    // ── Statistiques (Labels) ──────────────────────────────────────
    @FXML private Label lblTotal;
    @FXML private Label lblNeuves;
    @FXML private Label lblOccasion;
    @FXML private Label lblEnPanne;
    @FXML private Label lblFiltres;

    // ── Statistiques (Graphique miniature) ────────────────────────
    @FXML private PieChart pieStats;

    // ── Données ───────────────────────────────────────────────────
    private final MachineService          machineService = new MachineService();
    private final ObservableList<Machine> masterList     = FXCollections.observableArrayList();
    private       FilteredList<Machine>   filteredList;
    private       SortedList<Machine>     sortedList;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Couleurs communes aux deux graphiques
    private static final String[] PIE_COLORS = {"#27ae60", "#e67e22", "#e74c3c", "#95a5a6"};

    // ══════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerListeFiltrée();
        chargerMachines();
    }

    // ── Configuration colonnes ─────────────────────────────────────

    private void configurerColonnes() {
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatM"));
        colNumeroSerie.setCellValueFactory(new PropertyValueFactory<>("numeroSerie"));
        colDateAchat.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Colonne État colorée
        colEtat.setCellFactory(col -> new TableCell<Machine, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                switch (val.toLowerCase()) {
                    case "neuf":       setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;"); break;
                    case "disponible": setStyle("-fx-text-fill:#2980b9;-fx-font-weight:bold;"); break;
                    case "occasion":   setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;"); break;
                    case "bon":        setStyle("-fx-text-fill:#8e44ad;-fx-font-weight:bold;"); break;
                    case "en panne":   setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;"); break;
                    default:           setStyle("-fx-text-fill:#7f8c8d;"); break;
                }
            }
        });

        // Colonne Date formatée
        colDateAchat.setCellFactory(col -> new TableCell<Machine, LocalDate>() {
            @Override
            protected void updateItem(LocalDate val, boolean empty) {
                super.updateItem(val, empty);
                setText((empty || val == null) ? null : val.format(DATE_FMT));
            }
        });
    }

    // ── FilteredList + SortedList ──────────────────────────────────

    private void configurerListeFiltrée() {
        filteredList = new FilteredList<>(masterList, m -> true);
        sortedList   = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tableMachines.comparatorProperty());
        tableMachines.setItems(sortedList);
        sortedList.addListener(
                (javafx.collections.ListChangeListener<Machine>) c -> majStatistiques());
    }

    // ── Chargement des données ─────────────────────────────────────

    private void chargerMachines() {
        try {
            masterList.clear();
            masterList.addAll(machineService.recuperer());
            peuplerComboEtat();
            majStatistiques();
            appliquerFiltres();
        } catch (Exception e) {
            alerte("Erreur chargement",
                    "Impossible de charger les machines :\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void peuplerComboEtat() {
        List<String> etats = masterList.stream()
                .map(Machine::getEtatM)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        etats.add(0, "Tous les états");
        comboEtat.setItems(FXCollections.observableArrayList(etats));
        comboEtat.getSelectionModel().selectFirst();
    }

    // ══════════════════════════════════════════════════════════════
    //  FILTRES & RECHERCHE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void appliquerFiltres() {
        String texte = champRecherche.getText() == null ? ""
                : champRecherche.getText().trim().toLowerCase();
        String etat  = comboEtat.getValue();

        filteredList.setPredicate(machine -> {
            boolean okTexte = texte.isEmpty()
                    || contient(machine.getMarque(),      texte)
                    || contient(machine.getModele(),      texte)
                    || contient(machine.getEtatM(),       texte)
                    || contient(machine.getNumeroSerie(), texte);

            boolean okEtat = etat == null
                    || etat.startsWith("Tous")
                    || etat.equalsIgnoreCase(machine.getEtatM());

            return okTexte && okEtat;
        });

        majStatistiques();
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboEtat.getSelectionModel().selectFirst();
        appliquerFiltres();
    }

    private boolean contient(String champ, String recherche) {
        return champ != null && champ.toLowerCase().contains(recherche);
    }

    // ══════════════════════════════════════════════════════════════
    //  TRI PAR DATE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void trierDateDesc() {
        sortedList.comparatorProperty().unbind();
        sortedList.setComparator(
                Comparator.comparing(Machine::getDateAchat,
                        Comparator.nullsLast(Comparator.reverseOrder())));
    }

    @FXML
    private void trierDateAsc() {
        sortedList.comparatorProperty().unbind();
        sortedList.setComparator(
                Comparator.comparing(Machine::getDateAchat,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    // ══════════════════════════════════════════════════════════════
    //  STATISTIQUES  (Labels + PieChart miniature)
    // ══════════════════════════════════════════════════════════════

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

    /** Compte les machines dont l'état correspond à l'un des deux termes fournis. */
    private long compterEtat(String etat1, String etat2) {
        return masterList.stream().filter(m ->
                m.getEtatM() != null &&
                        (m.getEtatM().equalsIgnoreCase(etat1) ||
                                m.getEtatM().equalsIgnoreCase(etat2))).count();
    }

    /** Compte les machines dont l'état correspond exactement au terme fourni. */
    private long compterEtatSimple(String etat) {
        return masterList.stream().filter(m ->
                m.getEtatM() != null &&
                        m.getEtatM().equalsIgnoreCase(etat)).count();
    }

    /**
     * Met à jour le PieChart miniature affiché dans la barre de stats.
     */
    private void majPieChart(long neuves, long occasion, long enPanne, long total) {
        if (pieStats == null) return;

        long autres = total - neuves - occasion - enPanne;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (neuves   > 0) pieData.add(new PieChart.Data("Neuves/Dispo (" + neuves + ")",   neuves));
        if (occasion > 0) pieData.add(new PieChart.Data("Occasion/Bon (" + occasion + ")", occasion));
        if (enPanne  > 0) pieData.add(new PieChart.Data("En panne (" + enPanne + ")",      enPanne));
        if (autres   > 0) pieData.add(new PieChart.Data("Autres (" + autres + ")",         autres));

        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Aucune donnée", 1));
        }

        pieStats.setData(pieData);
        pieStats.setTitle("Répartition");

        // Colorisation des segments (après rendu)
        Platform.runLater(() -> appliquerCouleursPie(pieData, PIE_COLORS));
    }

    /** Applique les couleurs aux segments d'un PieChart. */
    private void appliquerCouleursPie(ObservableList<PieChart.Data> data, String[] colors) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getNode() != null) {
                String color = colors[Math.min(i, colors.length - 1)];
                data.get(i).getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  POPUP STATISTIQUES DÉTAILLÉES
    // ══════════════════════════════════════════════════════════════

    /**
     * Ouvre une fenêtre modale avec :
     * - Un PieChart grand format avec les pourcentages sur les labels
     * - Des cartes de résumé (Total, Neuves, Occasion, En panne)
     * - Une légende colorée détaillée
     */
    @FXML
    private void afficherStatistiques() {
        long total    = masterList.size();
        long neuves   = compterEtat("neuf", "disponible");
        long occasion = compterEtat("occasion", "bon");
        long enPanne  = compterEtatSimple("en panne");
        long autres   = total - neuves - occasion - enPanne;

        // ── 1. Titre du popup ─────────────────────────────────────
        Label titre = new Label("📊  Statistiques des Machines");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label sousTitre = new Label("Total en base : " + total + " machine(s)   |   "
                + "Résultats affichés : " + sortedList.size());
        sousTitre.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // ── 2. PieChart détaillé avec pourcentages ────────────────
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        if (total > 0) {
            if (neuves   > 0) pieData.add(new PieChart.Data(
                    String.format("Neuves / Disponibles\n%d  (%.1f%%)", neuves,   pct(neuves,   total)), neuves));
            if (occasion > 0) pieData.add(new PieChart.Data(
                    String.format("Occasion / Bon\n%d  (%.1f%%)",       occasion, pct(occasion, total)), occasion));
            if (enPanne  > 0) pieData.add(new PieChart.Data(
                    String.format("En panne\n%d  (%.1f%%)",             enPanne,  pct(enPanne,  total)), enPanne));
            if (autres   > 0) pieData.add(new PieChart.Data(
                    String.format("Autres états\n%d  (%.1f%%)",         autres,   pct(autres,   total)), autres));
        } else {
            pieData.add(new PieChart.Data("Aucune donnée", 1));
        }

        PieChart chart = new PieChart(pieData);
        chart.setTitle(null);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(false);
        chart.setPrefSize(500, 360);
        chart.setStyle("-fx-background-color: transparent;");

        // Colorisation après rendu JavaFX
        Platform.runLater(() -> appliquerCouleursPie(pieData, PIE_COLORS));

        // ── 3. Légende personnalisée ──────────────────────────────
        HBox legende = new HBox(18);
        legende.setAlignment(Pos.CENTER);
        legende.setPadding(new Insets(0, 0, 6, 0));

        String[][] legendeData = {
                {"Neuves / Disponibles", PIE_COLORS[0]},
                {"Occasion / Bon",       PIE_COLORS[1]},
                {"En panne",             PIE_COLORS[2]},
                {"Autres états",         PIE_COLORS[3]},
        };
        for (String[] ld : legendeData) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + ld[1] + "; -fx-font-size: 18px;");
            Label txt = new Label(ld[0]);
            txt.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
            HBox item = new HBox(4, dot, txt);
            item.setAlignment(Pos.CENTER_LEFT);
            legende.getChildren().add(item);
        }

        // ── 4. Cartes résumé ──────────────────────────────────────
        HBox cartes = new HBox(12);
        cartes.setAlignment(Pos.CENTER);
        cartes.setPadding(new Insets(10, 20, 10, 20));

        Object[][] cartesData = {
                {"📋 Total",             total,    "#3498db", total > 0   ? 100.0           : 0.0},
                {"✅ Neuves / Dispo",    neuves,   "#27ae60", pct(neuves,   total)},
                {"🔧 Occasion / Bon",   occasion, "#e67e22", pct(occasion, total)},
                {"🔴 En panne",         enPanne,  "#e74c3c", pct(enPanne,  total)},
        };

        for (Object[] cd : cartesData) {
            String label   = (String) cd[0];
            long   valeur  = (long)   cd[1];
            String color   = (String) cd[2];
            double percent = (double) cd[3];

            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(145);
            card.setPadding(new Insets(12, 10, 12, 10));
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 0 0 0 5;" +
                            "-fx-background-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,2);"
            );

            Label lNom = new Label(label);
            lNom.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            lNom.setWrapText(true);

            Label lVal = new Label(String.valueOf(valeur));
            lVal.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

            Label lPct = new Label(String.format("%.1f%%", percent));
            lPct.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

            card.getChildren().addAll(lNom, lVal, lPct);
            cartes.getChildren().add(card);
        }

        // ── 5. Barre de progression visuelle ─────────────────────
        HBox barres = new HBox(0);
        barres.setPrefHeight(18);
        barres.setMaxWidth(Double.MAX_VALUE);
        barres.setStyle("-fx-background-radius: 9; -fx-background-color: #ecf0f1;");

        if (total > 0) {
            long[]   vals   = {neuves, occasion, enPanne, autres};
            String[] colBr  = PIE_COLORS;
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] <= 0) continue;
                double width = (vals[i] * 500.0) / total;
                Region seg = new Region();
                seg.setPrefWidth(width);
                seg.setPrefHeight(18);
                String radius = "";
                if (i == 0)                          radius = "-fx-background-radius: 9 0 0 9;";
                if (i == vals.length - 1 || i == 3) radius = "-fx-background-radius: 0 9 9 0;";
                seg.setStyle("-fx-background-color: " + colBr[i] + ";" + radius);
                barres.getChildren().add(seg);
            }
        }

        VBox barreBox = new VBox(4);
        barreBox.setPadding(new Insets(0, 20, 0, 20));
        Label barreLabel = new Label("Répartition visuelle");
        barreLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        barreBox.getChildren().addAll(barreLabel, barres);

        // ── 6. Bouton Fermer ──────────────────────────────────────
        javafx.scene.control.Button btnFermer = new javafx.scene.control.Button("✖  Fermer");
        btnFermer.setPrefWidth(140);
        btnFermer.setPrefHeight(40);
        btnFermer.setStyle(
                "-fx-background-color: #8e44ad; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );

        // ── 7. Assemblage du layout ───────────────────────────────
        VBox header = new VBox(4, titre, sousTitre);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 6, 0));

        VBox layout = new VBox(14);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24, 28, 24, 28));
        layout.setStyle("-fx-background-color: #f0f2f5;");
        layout.getChildren().addAll(
                header,
                new javafx.scene.control.Separator(),
                chart,
                legende,
                new javafx.scene.control.Separator(),
                cartes,
                barreBox,
                btnFermer
        );

        // ── 8. Ouverture du Stage modal ───────────────────────────
        Stage popup = new Stage();
        popup.setTitle("📊 Statistiques — Gestion des Machines");
        popup.setScene(new Scene(layout, 600, 700));
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(tableMachines.getScene().getWindow());
        popup.setResizable(false);

        btnFermer.setOnAction(e -> popup.close());

        popup.show();

        // Colorisation garantie après affichage complet
        Platform.runLater(() -> appliquerCouleursPie(pieData, PIE_COLORS));
    }

    /** Calcule le pourcentage, retourne 0 si total == 0. */
    private double pct(long valeur, long total) {
        return total == 0 ? 0.0 : (valeur * 100.0) / total;
    }

    // ══════════════════════════════════════════════════════════════
    //  EXPORT PDF  (Apache PDFBox 3.x)
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void exporterPDF() {
        File fichier = choisirFichier("PDF", "*.pdf",
                "machines_" + LocalDate.now() + ".pdf");
        if (fichier == null) return;

        List<Machine> lignes = List.copyOf(sortedList);

        float pageW = PDRectangle.A4.getWidth();
        float pageH = PDRectangle.A4.getHeight();
        float marg  = 40f;
        float rowH  = 20f;
        float hdrH  = 26f;
        int   perPg = (int) ((pageH - marg * 3 - hdrH - 80) / rowH);
        int   pages = Math.max(1, (int) Math.ceil((double) lignes.size() / perPg));

        float[]  cw = {95, 85, 90, 120, 90, 35};
        String[] ch = {"Marque", "Modele", "Etat", "N Serie", "Date Achat", "Nom"};

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
                    cs.showText("Exporté le " + LocalDate.now().format(DATE_FMT)
                            + "   |   Résultats : " + lignes.size()
                            + " / Total : " + masterList.size()
                            + "   |   Page " + (pi + 1) + "/" + pages);
                    cs.endText(); y -= 14;

                    cs.setLineWidth(0.5f);
                    cs.setStrokingColor(0.7f, 0.7f, 0.7f);
                    cs.moveTo(marg, y); cs.lineTo(pageW - marg, y); cs.stroke();
                    y -= 8;

                    // En-tête tableau
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

                    // Données
                    int start = pi * perPg;
                    int end   = Math.min(start + perPg, lignes.size());
                    boolean alt = false;

                    for (int i = start; i < end; i++) {
                        Machine m = lignes.get(i);
                        if (alt) {
                            cs.setNonStrokingColor(0.95f, 0.97f, 0.99f);
                            cs.addRect(marg, y - rowH + 5, pageW - 2 * marg, rowH);
                            cs.fill();
                        }
                        alt = !alt;

                        String etatVal = s(m.getEtatM());
                        float[] ec;
                        switch (etatVal.toLowerCase()) {
                            case "neuf":       ec = new float[]{0.15f, 0.53f, 0.38f}; break;
                            case "disponible": ec = new float[]{0.16f, 0.50f, 0.73f}; break;
                            case "occasion":   ec = new float[]{0.90f, 0.49f, 0.13f}; break;
                            case "bon":        ec = new float[]{0.56f, 0.27f, 0.68f}; break;
                            case "en panne":   ec = new float[]{0.91f, 0.30f, 0.24f}; break;
                            default:           ec = new float[]{0.31f, 0.31f, 0.31f}; break;
                        }

                        String dateStr = m.getDateAchat() != null
                                ? m.getDateAchat().format(DATE_FMT) : "";
                        String[] vals = {
                                s(m.getMarque()), s(m.getModele()), etatVal,
                                s(m.getNumeroSerie()), dateStr, s(m.getNom())
                        };

                        xc = marg + 5;
                        for (int c = 0; c < vals.length; c++) {
                            boolean isEtat = (c == 2);
                            float[] color  = isEtat ? ec : new float[]{0.1f, 0.1f, 0.1f};
                            cs.setNonStrokingColor(color[0], color[1], color[2]);
                            cs.beginText();
                            cs.setFont(isEtat ? bold : plain, 9);
                            cs.newLineAtOffset(xc, y - 14);
                            String txt = vals[c];
                            if (txt.length() > 17) txt = txt.substring(0, 14) + "...";
                            cs.showText(txt); cs.endText();
                            xc += cw[c];
                        }

                        cs.setStrokingColor(0.88f, 0.88f, 0.88f);
                        cs.setLineWidth(0.3f);
                        cs.moveTo(marg, y - rowH + 5);
                        cs.lineTo(pageW - marg, y - rowH + 5);
                        cs.stroke();
                        y -= rowH;
                    }

                    // Stats (dernière page)
                    if (pi == pages - 1) {
                        y -= 14;
                        cs.setLineWidth(0.5f);
                        cs.setStrokingColor(0.6f, 0.6f, 0.6f);
                        cs.moveTo(marg, y); cs.lineTo(pageW - marg, y); cs.stroke();
                        y -= 14;
                        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                        cs.beginText(); cs.setFont(bold, 9);
                        cs.newLineAtOffset(marg, y);
                        cs.showText("Stats — Total : " + masterList.size()
                                + "  |  Neuves/Dispo : " + lblNeuves.getText()
                                + "  |  Occasion/Bon : " + lblOccasion.getText()
                                + "  |  En panne : " + lblEnPanne.getText()
                                + "  |  Affichés : " + lblFiltres.getText());
                        cs.endText();
                    }
                }
            }

            doc.save(fichier);
            alerte("Export PDF", "Fichier sauvegardé :\n" + fichier.getPath(),
                    Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            alerte("Erreur PDF", "Export échoué : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  EXPORT EXCEL  (Apache POI)
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void exporterExcel() {
        File fichier = choisirFichier("Excel", "*.xlsx",
                "machines_" + LocalDate.now() + ".xlsx");
        if (fichier == null) return;

        List<Machine> lignes = List.copyOf(sortedList);

        try (Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Machines");
            sheet.setDefaultColumnWidth(20);

            org.apache.poi.ss.usermodel.CellStyle csTitle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fTitle = wb.createFont();
            fTitle.setBold(true); fTitle.setFontHeightInPoints((short) 14);
            csTitle.setFont(fTitle);

            org.apache.poi.ss.usermodel.CellStyle csHeader = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fHeader = wb.createFont();
            fHeader.setBold(true);
            fHeader.setColor(IndexedColors.WHITE.getIndex());
            csHeader.setFont(fHeader);
            csHeader.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            csHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csHeader.setAlignment(HorizontalAlignment.CENTER);
            csHeader.setBorderBottom(BorderStyle.THIN);

            org.apache.poi.ss.usermodel.CellStyle csAlt = wb.createCellStyle();
            csAlt.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            csAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row rTitre = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell cTitre = rTitre.createCell(0);
            cTitre.setCellValue("AGROFLOW - Gestion des Machines - Exporté le "
                    + LocalDate.now().format(DATE_FMT));
            cTitre.setCellStyle(csTitle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            Row rInfo = sheet.createRow(1);
            rInfo.createCell(0).setCellValue("Résultats affichés : " + lignes.size()
                    + " / Total : " + masterList.size());

            String[] headers = {"Marque", "Modèle", "État", "N° Série", "Date Achat", "Nom"};
            Row rHeader = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = rHeader.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(csHeader);
            }

            for (int i = 0; i < lignes.size(); i++) {
                Machine m   = lignes.get(i);
                Row     row = sheet.createRow(i + 4);
                org.apache.poi.ss.usermodel.Cell c0 = row.createCell(0);
                org.apache.poi.ss.usermodel.Cell c1 = row.createCell(1);
                org.apache.poi.ss.usermodel.Cell c2 = row.createCell(2);
                org.apache.poi.ss.usermodel.Cell c3 = row.createCell(3);
                org.apache.poi.ss.usermodel.Cell c4 = row.createCell(4);
                org.apache.poi.ss.usermodel.Cell c5 = row.createCell(5);
                c0.setCellValue(s(m.getMarque()));
                c1.setCellValue(s(m.getModele()));
                c2.setCellValue(s(m.getEtatM()));
                c3.setCellValue(s(m.getNumeroSerie()));
                c4.setCellValue(m.getDateAchat() != null ? m.getDateAchat().format(DATE_FMT) : "");
                c5.setCellValue(s(m.getNom()));
                if (i % 2 == 0) {
                    c0.setCellStyle(csAlt); c1.setCellStyle(csAlt);
                    c2.setCellStyle(csAlt); c3.setCellStyle(csAlt);
                    c4.setCellStyle(csAlt); c5.setCellStyle(csAlt);
                }
            }

            // Onglet Statistiques
            Sheet shStats = wb.createSheet("Statistiques");
            shStats.setDefaultColumnWidth(28);

            org.apache.poi.ss.usermodel.CellStyle csStatHdr = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fSH = wb.createFont();
            fSH.setBold(true); fSH.setColor(IndexedColors.WHITE.getIndex());
            csStatHdr.setFont(fSH);
            csStatHdr.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            csStatHdr.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[][] stats = {
                    {"Indicateur",              "Valeur"},
                    {"Total machines en base",  String.valueOf(masterList.size())},
                    {"Neuves / Disponibles",    lblNeuves.getText()},
                    {"Occasion / Bon",          lblOccasion.getText()},
                    {"En panne",                lblEnPanne.getText()},
                    {"Résultats affichés",      lblFiltres.getText()},
                    {"Date export",             LocalDate.now().format(DATE_FMT)}
            };

            for (int i = 0; i < stats.length; i++) {
                Row row = shStats.createRow(i);
                for (int j = 0; j < 2; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue(stats[i][j]);
                    if (i == 0) cell.setCellStyle(csStatHdr);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(fichier)) {
                wb.write(fos);
            }

            alerte("Export Excel",
                    "Fichier sauvegardé :\n" + fichier.getPath()
                            + "\n(2 onglets : 'Machines' + 'Statistiques')",
                    Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            alerte("Erreur Excel", "Export échoué : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void versAjout() {
        naviguerVers("/AjoutMachine.fxml");
    }

    @FXML
    private void versModifier() {
        Machine sel = tableMachines.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerte("Attention", "Sélectionnez une machine à modifier.",
                    Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ModifierMachine.fxml"));
            Parent root = loader.load();
            ((ModifierMachineController) loader.getController()).setMachine(sel);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            alerte("Erreur", "Impossible d'ouvrir la modification : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSupprimer() {
        Machine sel = tableMachines.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerte("Attention", "Sélectionnez une machine à supprimer.",
                    Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer « " + sel.getMarque() + " " + sel.getModele() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    machineService.supprimer(sel.getIdM());
                    alerte("Succès", "Machine supprimée avec succès.",
                            Alert.AlertType.INFORMATION);
                    chargerMachines();
                } catch (Exception e) {
                    alerte("Erreur", "Impossible de supprimer : " + e.getMessage(),
                            Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════

    @FXML private void retourAccueil()      { naviguerVers("/AccueilMateriel.fxml"); }
    @FXML private void naviguerAnimaux()    { naviguerVers("/AfficherAnimaux.fxml"); }
    @FXML private void naviguerMateriels()  { naviguerVers("/AccueilMateriel.fxml"); }
    @FXML private void naviguerStocks()     { naviguerVers("/AfficherStocks.fxml"); }
    @FXML private void naviguerTerrains()   { naviguerVers("/AfficherTerrains.fxml"); }
    @FXML private void naviguerEvenements() { naviguerVers("/AccueilEvenement.fxml"); }
    @FXML private void naviguerUsers()      { naviguerVers("/AfficherUsers.fxml"); }
    @FXML private void deconnexion()        { naviguerVers("/Login.fxml"); }

    private void naviguerVers(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage  stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            alerte("Navigation", "Impossible d'ouvrir : " + path,
                    Alert.AlertType.ERROR);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════════════

    private String s(String v) { return v == null ? "" : v; }

    private File choisirFichier(String type, String ext, String nomDefaut) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer en " + type);
        fc.setInitialFileName(nomDefaut);
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers " + type, ext));
        return fc.showSaveDialog(tableMachines.getScene().getWindow());
    }

    private void alerte(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}