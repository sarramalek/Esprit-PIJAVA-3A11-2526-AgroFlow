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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
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

public class AfficherMachinesController {

    // ── Tableau ──────────────────────────────────────────────────────────────
    @FXML private TableView<Machine>              tableMachines;
    @FXML private TableColumn<Machine, String>    colMarque;
    @FXML private TableColumn<Machine, String>    colModele;
    @FXML private TableColumn<Machine, String>    colEtat;
    @FXML private TableColumn<Machine, String>    colNumeroSerie;
    @FXML private TableColumn<Machine, LocalDate> colDateAchat;
    @FXML private TableColumn<Machine, String>    colNom;

    // ── Recherche & Filtres ───────────────────────────────────────────────────
    /** Recherche libre sur tous les attributs sauf la date */
    @FXML private TextField        champRecherche;
    /** Filtre combo par NOM */
    @FXML private ComboBox<String> comboNom;
    /** Filtre combo par MODELE */
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

    // ── Données ───────────────────────────────────────────────────────────────
    private final ObservableList<Machine> masterList   = FXCollections.observableArrayList();
    private       FilteredList<Machine>   filteredList;
    private       SortedList<Machine>     sortedList;

    private static final DateTimeFormatter DATE_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[]          PIE_COLORS = {"#27ae60","#e67e22","#e74c3c","#95a5a6"};

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerListeFiltrée();
        chargerMachines();
    }

    private void configurerColonnes() {
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatM"));
        colNumeroSerie.setCellValueFactory(new PropertyValueFactory<>("numeroSerie"));
        colDateAchat.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Colonne État colorée
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle(switch (val.toLowerCase()) {
                    case "neuf"       -> "-fx-text-fill:#27ae60;-fx-font-weight:bold;";
                    case "disponible" -> "-fx-text-fill:#2980b9;-fx-font-weight:bold;";
                    case "occasion"   -> "-fx-text-fill:#e67e22;-fx-font-weight:bold;";
                    case "bon"        -> "-fx-text-fill:#8e44ad;-fx-font-weight:bold;";
                    case "en panne"   -> "-fx-text-fill:#e74c3c;-fx-font-weight:bold;";
                    default           -> "-fx-text-fill:#7f8c8d;";
                });
            }
        });

        // Colonne Date formatée
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

    /**
     * Peuple comboNom (valeurs distinctes du champ Nom)
     * et comboModele (valeurs distinctes du champ Modele).
     */
    private void peuplerCombos() {
        // ── Combo NOM ─────────────────────────────────────────────────────────
        List<String> noms = masterList.stream()
                .map(Machine::getNom)
                .filter(n -> n != null && !n.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());
        noms.add(0, "Tous les noms");
        comboNom.setItems(FXCollections.observableArrayList(noms));
        comboNom.getSelectionModel().selectFirst();

        // ── Combo MODELE ──────────────────────────────────────────────────────
        List<String> modeles = masterList.stream()
                .map(Machine::getModele)
                .filter(m -> m != null && !m.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());
        modeles.add(0, "Tous les modèles");
        comboModele.setItems(FXCollections.observableArrayList(modeles));
        comboModele.getSelectionModel().selectFirst();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FILTRES & RECHERCHE
    //
    //  champRecherche → recherche sur TOUS les attributs sauf la date
    //                   (Marque, Modèle, État, N° Série, Nom)
    //  comboNom       → filtre supplémentaire par NOM exact
    //  comboModele    → filtre supplémentaire par MODELE exact
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void appliquerFiltres() {
        String texte  = champRecherche.getText() == null ? ""
                : champRecherche.getText().trim().toLowerCase();
        String nom    = comboNom.getValue();
        String modele = comboModele.getValue();

        filteredList.setPredicate(m -> {

            // ── 1. Recherche libre (tous attributs sauf date) ─────────────────
            boolean okRecherche = texte.isEmpty()
                    || contient(m.getMarque(),      texte)
                    || contient(m.getModele(),      texte)
                    || contient(m.getEtatM(),       texte)
                    || contient(m.getNumeroSerie(), texte)
                    || contient(m.getNom(),         texte);

            // ── 2. Filtre combo Nom ───────────────────────────────────────────
            boolean okNom = nom == null || nom.startsWith("Tous")
                    || nom.equalsIgnoreCase(m.getNom());

            // ── 3. Filtre combo Modèle ────────────────────────────────────────
            boolean okModele = modele == null || modele.startsWith("Tous")
                    || modele.equalsIgnoreCase(m.getModele());

            return okRecherche && okNom && okModele;
        });

        majStatistiques();
    }

    /** Remet à zéro tous les filtres et relance l'affichage complet. */
    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboNom.getSelectionModel().selectFirst();
        comboModele.getSelectionModel().selectFirst();
        appliquerFiltres();
    }

    /**
     * Recharge toutes les données depuis la base de données,
     * rafraîchit les combos et réapplique les filtres courants.
     */
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
        if (d.isEmpty())  d.add(new PieChart.Data("Aucune donnée", 1));
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
    //  POPUP STATISTIQUES DÉTAILLÉES
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void afficherStatistiques() {
        long total    = masterList.size();
        long neuves   = compterEtat("neuf", "disponible");
        long occasion = compterEtat("occasion", "bon");
        long enPanne  = compterEtatSimple("en panne");
        long autres   = total - neuves - occasion - enPanne;

        Label titre = new Label("📊  Statistiques des Machines");
        titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        Label sousTitre = new Label("Total : " + total + "   |   Affichés : " + sortedList.size());
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
            pd.add(new PieChart.Data("Aucune donnée", 1));
        }

        PieChart chart = new PieChart(pd);
        chart.setLabelsVisible(true); chart.setLegendVisible(false);
        chart.setPrefSize(500, 340);
        chart.setStyle("-fx-background-color:transparent;");
        Platform.runLater(() -> appliquerCouleursPie(pd, PIE_COLORS));

        Button btnFermer = new Button("✖  Fermer");
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
        popup.setTitle("📊 Statistiques — Gestion des Machines");
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
            java.net.URL fxmlUrl = getClass().getResource("/MindSpherePage.fxml");
            if (fxmlUrl == null)
                throw new IOException("Fichier MindSpherePage.fxml introuvable.");
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root, 1300, 820));
            stage.setTitle("⚡ MindSphere IoT — API Avancée | AGROFLOW");
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

    @FXML private void versAjout() { naviguerVers("/AjoutMachine.fxml"); }

    @FXML
    private void versModifier() {
        Machine sel = tableMachines.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alerte("Attention", "Sélectionnez une machine à modifier.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMachine.fxml"));
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
            alerte("Attention", "Sélectionnez une machine à supprimer.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer « " + sel.getMarque() + " " + sel.getModele() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation suppression"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    machineService.supprimer(sel.getIdM());
                    alerte("Succès", "Machine supprimée avec succès.", Alert.AlertType.INFORMATION);
                    chargerMachines();
                } catch (Exception e) {
                    alerte("Erreur", "Impossible de supprimer : " + e.getMessage(),
                            Alert.AlertType.ERROR);
                }
            }
        });
    }

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
                    cs.showText("Exporté le " + LocalDate.now().format(DATE_FMT)
                            + " | Résultats : " + lignes.size()
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
                        cs.showText("Stats — Total:" + masterList.size()
                                + " | Neuves:" + lblNeuves.getText()
                                + " | Occasion:" + lblOccasion.getText()
                                + " | Pannes:" + lblEnPanne.getText());
                        cs.endText();
                    }
                }
            }
            doc.save(fichier);
            alerte("Export PDF", "Fichier sauvegardé :\n" + fichier.getPath(),
                    Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur PDF", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
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
            c0.setCellValue("AGROFLOW - Machines - Exporté le " + LocalDate.now().format(DATE_FMT));
            c0.setCellStyle(csT);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            sheet.createRow(1).createCell(0).setCellValue(
                    "Résultats : " + lignes.size() + " / Total : " + masterList.size());

            String[] hdr = {"Marque","Modèle","État","N° Série","Date Achat","Nom"};
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

            // Onglet Statistiques
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
                    {"Résultats affichés", lblFiltres.getText()},
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
            alerte("Export Excel", "Fichier sauvegardé :\n" + fichier.getPath()
                    + "\n(2 onglets : 'Machines' + 'Statistiques')", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            alerte("Erreur Excel", "Export échoué : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ════════════════════════════════════════════════════════════════════════

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
            java.net.URL url = getClass().getResource(path);
            if (url == null) throw new IOException("FXML introuvable : " + path);
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
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
                {"Autres états",       PIE_COLORS[3]}
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
                {"📋 Total",       total,    "#3498db", total > 0 ? 100.0 : 0.0},
                {"✅ Neuves/Dispo", neuves,   "#27ae60", pct(neuves,   total)},
                {"🔧 Occasion/Bon", occasion, "#e67e22", pct(occasion, total)},
                {"🔴 En panne",    enPanne,  "#e74c3c", pct(enPanne,  total)}
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
        Label lbl = new Label("Répartition visuelle");
        lbl.setStyle("-fx-font-size:11px;-fx-text-fill:#7f8c8d;-fx-font-weight:bold;");
        box.getChildren().addAll(lbl, b);
        return box;
    }
}