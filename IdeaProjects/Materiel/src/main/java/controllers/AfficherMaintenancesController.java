package controllers;

import entities.Machine;
import entities.Maintenance;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.MachineService;
import services.MaintenanceApiService;
import services.MaintenanceApiService.ResultatAlerte;
import services.MaintenanceApiService.ResultatCout;
import services.MaintenanceService;
import utils.MyDatabase;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AfficherMaintenancesController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────
    @FXML private TableView<Maintenance>             tableMaintenances;
    @FXML private TableColumn<Maintenance, String>   colMachine;
    @FXML private TableColumn<Maintenance, String>   colTypePanne;
    @FXML private TableColumn<Maintenance, String>   colDate;
    @FXML private TableColumn<Maintenance, Double>   colCout;
    @FXML private TableColumn<Maintenance, String>   colDescription;

    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private ComboBox<String> comboTypePanne;

    // Labels stats bas de page
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;
    @FXML private Label lblMachineCouteuse;
    @FXML private Label lblPctTotal;
    @FXML private Label lblPctCoutEleve;
    @FXML private Label lblPctSousLaMoyenne;
    @FXML private Label lblPctMachine;
    @FXML private Label lblSelectionInfo;

    // ── Services ─────────────────────────────────────────────────
    private MaintenanceService    maintenanceService;
    private MachineService        machineService;
    private MaintenanceApiService apiService;
    private Connection            connection;

    // ── Données ──────────────────────────────────────────────────
    private final ObservableList<Maintenance> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Maintenance> displayList = FXCollections.observableArrayList();
    private List<Machine> machines = new ArrayList<>();

    private enum SortMode { DATE_DESC, DATE_ASC, COUT_ASC, COUT_DESC, MACHINE_AZ, NONE }
    private SortMode currentSort = SortMode.DATE_DESC;

    // ═══════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        maintenanceService = new MaintenanceService();
        machineService     = new MachineService();
        apiService         = new MaintenanceApiService();
        connection         = MyDatabase.getInstance().getConnection();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();

        // Listener sélection tableau
        tableMaintenances.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (lblSelectionInfo != null) {
                lblSelectionInfo.setText(nv != null
                        ? "Sélectionné : " + nomMachineParId(nv.getIdM()) + " — " + nv.getTypePanne()
                        : "Sélectionnez une ligne pour Modifier / Supprimer");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  TABLEAU
    // ═══════════════════════════════════════════════════════════
    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colMachine.setCellValueFactory(d ->
                new SimpleStringProperty(nomMachineParId(d.getValue().getIdM())));
        colTypePanne.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTypePanne()));
        colDate.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDateMain();
            return new SimpleStringProperty(date != null ? date.format(fmt) : "");
        });
        colCout.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getCout()).asObject());
        colDescription.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDescription()));

        // Coloriage alternée + couleur si coût élevé
        tableMaintenances.setRowFactory(tv -> new TableRow<Maintenance>() {
            @Override protected void updateItem(Maintenance item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                if (item.getCout() > 1000)
                    setStyle("-fx-background-color: #fff5f5;");
                else if (getIndex() % 2 == 1)
                    setStyle("-fx-background-color: #f0f4f8;");
                else
                    setStyle("");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  CHARGEMENT
    // ═══════════════════════════════════════════════════════════
    private void chargerDonnees() {
        try { machines = machineService.recuperer(); }
        catch (SQLException e) { showErr("Erreur", e.getMessage()); }
        chargerMaintenances();
        initialiserCombos();
    }

    private void chargerMaintenances() {
        masterList.clear();
        try { masterList.addAll(maintenanceService.recuperer()); }
        catch (SQLException e) { showErr("Erreur SQL", e.getMessage()); }
        appliquerFiltres();
    }

    private void initialiserCombos() {
        // Combo machines
        if (comboMachine != null) {
            comboMachine.getItems().clear();
            comboMachine.getItems().add("Toutes les machines");
            for (Machine m : machines)
                comboMachine.getItems().add(m.getNom());
            comboMachine.getSelectionModel().selectFirst();
        }

        // Combo types de panne
        if (comboTypePanne != null) {
            Set<String> types = new LinkedHashSet<>();
            types.add("Tous les types");
            for (Maintenance m : masterList)
                if (m.getTypePanne() != null) types.add(m.getTypePanne());
            comboTypePanne.setItems(FXCollections.observableArrayList(types));
            comboTypePanne.getSelectionModel().selectFirst();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  RECHERCHE / FILTRES / TRI
    //  ← Toutes les méthodes référencées dans le FXML
    // ═══════════════════════════════════════════════════════════
    private void configurerRecherche() {
        if (champRecherche != null)
            champRecherche.textProperty().addListener((o, ov, nv) -> appliquerFiltres());
    }

    @FXML private void rechercher()          { appliquerFiltres(); }
    @FXML private void filtrer()             { appliquerFiltres(); }

    @FXML private void effacerRecherche() {
        if (champRecherche != null) champRecherche.clear();
        appliquerFiltres();
    }

    // ← Tri date
    @FXML private void trierPlusRecent() { currentSort = SortMode.DATE_DESC; appliquerFiltres(); }
    @FXML private void trierPlusAncien() { currentSort = SortMode.DATE_ASC;  appliquerFiltres(); }

    // ← Tri coût  (noms exacts du FXML : trierCoutEleve / trierCoutFaible)
    @FXML private void trierCoutEleve()  { currentSort = SortMode.COUT_DESC; appliquerFiltres(); }
    @FXML private void trierCoutFaible() { currentSort = SortMode.COUT_ASC;  appliquerFiltres(); }

    // ← Tri machine A→Z
    @FXML private void trierMachineAZ()  { currentSort = SortMode.MACHINE_AZ; appliquerFiltres(); }

    // ← Réinitialiser (nom exact du FXML : reinitialiserFiltres)
    @FXML private void reinitialiserFiltres() {
        if (champRecherche  != null) champRecherche.clear();
        if (comboMachine    != null) comboMachine.getSelectionModel().selectFirst();
        if (comboTypePanne  != null) comboTypePanne.getSelectionModel().selectFirst();
        currentSort = SortMode.DATE_DESC;
        appliquerFiltres();
    }

    @FXML private void actualiser() { reinitialiserFiltres(); chargerDonnees(); }

    private void appliquerFiltres() {
        String recherche = champRecherche != null && champRecherche.getText() != null
                ? champRecherche.getText().toLowerCase().trim() : "";

        String machineSel = comboMachine != null && comboMachine.getValue() != null
                ? comboMachine.getValue() : "Toutes les machines";

        String typeSel = comboTypePanne != null && comboTypePanne.getValue() != null
                ? comboTypePanne.getValue() : "Tous les types";

        List<Maintenance> filtered = new ArrayList<>();
        for (Maintenance m : masterList) {
            String nomM = nomMachineParId(m.getIdM());

            boolean matchR = recherche.isEmpty()
                    || nomM.toLowerCase().contains(recherche)
                    || (m.getTypePanne()   != null && m.getTypePanne().toLowerCase().contains(recherche))
                    || (m.getDescription() != null && m.getDescription().toLowerCase().contains(recherche))
                    || String.valueOf(m.getCout()).contains(recherche)
                    || (m.getDateMain() != null && m.getDateMain().toString().contains(recherche));

            boolean matchM = machineSel.equals("Toutes les machines") || nomM.equals(machineSel);
            boolean matchT = typeSel.equals("Tous les types")
                    || (m.getTypePanne() != null && m.getTypePanne().equals(typeSel));

            if (matchR && matchM && matchT) filtered.add(m);
        }

        switch (currentSort) {
            case DATE_ASC   -> filtered.sort(Comparator.comparing(
                    m -> m.getDateMain() != null ? m.getDateMain() : LocalDate.MIN));
            case DATE_DESC  -> filtered.sort(Comparator.comparing(
                    (Maintenance m) -> m.getDateMain() != null ? m.getDateMain() : LocalDate.MIN).reversed());
            case COUT_ASC   -> filtered.sort(Comparator.comparingDouble(Maintenance::getCout));
            case COUT_DESC  -> filtered.sort(Comparator.comparingDouble(Maintenance::getCout).reversed());
            case MACHINE_AZ -> filtered.sort(Comparator.comparing(
                    m -> nomMachineParId(m.getIdM()).toLowerCase()));
            default -> {}
        }

        displayList.setAll(filtered);
        tableMaintenances.setItems(displayList);
        mettreAJourStats();
    }

    // ═══════════════════════════════════════════════════════════
    //  STATS BAS DE PAGE
    // ═══════════════════════════════════════════════════════════
    private void mettreAJourStats() {
        int    nb    = displayList.size();
        double total = displayList.stream().mapToDouble(Maintenance::getCout).sum();
        double moy   = nb > 0 ? total / nb : 0;

        // % élevés (> moyenne)
        long   nbEleves = displayList.stream().filter(m -> m.getCout() > moy).count();
        double pctEleve = nb > 0 ? (nbEleves * 100.0 / nb) : 0;

        // % sous la moyenne
        long   nbSous   = nb - nbEleves;
        double pctSous  = nb > 0 ? (nbSous * 100.0 / nb) : 0;

        // Machine la plus coûteuse
        Map<Integer, Double> coutParMachine = new HashMap<>();
        for (Maintenance m : displayList)
            coutParMachine.merge(m.getIdM(), m.getCout(), Double::sum);
        int idMachMax = coutParMachine.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(-1);
        String nomMachMax  = idMachMax >= 0 ? nomMachineParId(idMachMax) : "-";
        double coutMachMax = idMachMax >= 0 ? coutParMachine.get(idMachMax) : 0;
        double pctMach     = total > 0 ? (coutMachMax * 100.0 / total) : 0;

        if (lblTotal             != null) lblTotal.setText(String.valueOf(nb));
        if (lblCoutTotal         != null) lblCoutTotal.setText(String.format("%.2f DT", total));
        if (lblCoutMoyen         != null) lblCoutMoyen.setText(String.format("%.2f DT", moy));
        if (lblMachineCouteuse   != null) lblMachineCouteuse.setText(nomMachMax);
        if (lblPctTotal          != null) lblPctTotal.setText("100%");
        if (lblPctCoutEleve      != null) lblPctCoutEleve.setText(String.format("%.0f%% élevés", pctEleve));
        if (lblPctSousLaMoyenne  != null) lblPctSousLaMoyenne.setText(String.format("%.0f%% sous moy.", pctSous));
        if (lblPctMachine        != null) lblPctMachine.setText(String.format("%.0f%% du total", pctMach));
    }

    // ═══════════════════════════════════════════════════════════
    //  CRUD  ← noms exacts du FXML
    // ═══════════════════════════════════════════════════════════

    // ouvrirFormulaireAjout (FXML : onAction="#ouvrirFormulaireAjout")
    @FXML private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMaintenance.fxml"));
            Parent root = loader.load();
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Ajouter une Maintenance");
            st.setScene(new Scene(root));
            st.showAndWait();
            chargerMaintenances();
        } catch (IOException e) {
            showErr("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    // modifierSelection (FXML : onAction="#modifierSelection")
    @FXML private void modifierSelection() {
        Maintenance sel = tableMaintenances.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélection", "Sélectionnez une maintenance à modifier."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMaintenance.fxml"));
            Parent root = loader.load();
            ModifierMaintenanceController ctrl = loader.getController();
            ctrl.initialiserDonnees(sel);
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Modifier la Maintenance");
            st.setScene(new Scene(root));
            st.showAndWait();
            chargerMaintenances();
        } catch (IOException e) {
            showErr("Erreur", "Impossible d'ouvrir : " + e.getMessage());
        }
    }

    // supprimerSelection (FXML : onAction="#supprimerSelection")
    @FXML private void supprimerSelection() {
        Maintenance sel = tableMaintenances.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélection", "Sélectionnez une maintenance à supprimer."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la maintenance #" + sel.getIdMain() + " ?");
        confirm.setContentText("Machine : " + nomMachineParId(sel.getIdM())
                + "\nType : " + sel.getTypePanne()
                + "\nCoût : " + String.format("%.2f DT", sel.getCout())
                + "\n\nAction irréversible !");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                maintenanceService.supprimer(sel.getIdMain());
                showInfo("Succès", "Maintenance supprimée.");
                chargerMaintenances();
            } catch (SQLException e) { showErr("Erreur", e.getMessage()); }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  STATISTIQUES (FXML : onAction="#afficherStatistiques")
    // ═══════════════════════════════════════════════════════════
    @FXML private void afficherStatistiques() {
        if (displayList.isEmpty()) { showInfo("Stats", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques des Maintenances");
        stage.setResizable(true);

        // Bar chart par machine
        Map<String, Double> dataMap = new LinkedHashMap<>();
        for (Maintenance m : displayList)
            dataMap.merge(nomMachineParId(m.getIdM()), m.getCout(), Double::sum);

        int barW = 60, gap = 30, padL = 70, padTop = 50, chartH = 300;
        List<String> keys = new ArrayList<>(dataMap.keySet());
        List<Double> vals = new ArrayList<>(dataMap.values());
        int n = keys.size();
        int canvasW = padL + n * (barW + gap) + gap + 20;
        int canvasH = chartH + 80 + padTop;

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasW, canvasH);
        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Coût total par machine (DT)", padL, 30);

        double maxVal = vals.stream().mapToDouble(v -> v).max().orElse(1);
        Color[] colors = { Color.web("#3498db"), Color.web("#27ae60"), Color.web("#e74c3c"),
                Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c") };

        for (int i = 0; i <= 5; i++) {
            double y = padTop + chartH - (chartH * i / 5.0);
            gc.setStroke(Color.LIGHTGRAY); gc.setLineWidth(1);
            gc.strokeLine(padL, y, canvasW - 20, y);
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(String.format("%.0f", maxVal * i / 5.0), 5, y + 4);
        }

        for (int i = 0; i < n; i++) {
            double barH = (vals.get(i) / maxVal) * chartH;
            double x = padL + gap + i * (barW + gap);
            double y = padTop + chartH - barH;
            Color c = colors[i % colors.length];

            gc.setFill(Color.rgb(0, 0, 0, 0.08));
            gc.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);
            gc.setFill(c);
            gc.fillRoundRect(x, y, barW, barH, 6, 6);

            gc.setFill(Color.web("#2c3e50"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.format("%.0f", vals.get(i));
            gc.fillText(vs, x + barW / 2.0 - vs.length() * 3.5, y - 6);

            gc.setFont(Font.font("Arial", 10));
            String lbl = keys.get(i).length() > 12 ? keys.get(i).substring(0, 12) + "…" : keys.get(i);
            gc.fillText(lbl, x + barW / 2.0 - lbl.length() * 3, padTop + chartH + 16);
        }

        ScrollPane sp = new ScrollPane(canvas);
        sp.setFitToHeight(true);
        sp.setPrefSize(Math.min(canvasW + 20, 900), canvasH + 20);

        VBox root = new VBox(10, sp);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════
    //  API 1 — COÛT PAR MACHINE (FXML : onAction="#ouvrirCoutMachine")
    // ═══════════════════════════════════════════════════════════
    @FXML private void ouvrirCoutMachine() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🏭 Coût Total de Maintenance par Machine");
        stage.setResizable(true);

        // Contrôles
        ToggleGroup tg = new ToggleGroup();
        RadioButton rbGlobal  = new RadioButton("Global");
        RadioButton rbMachine = new RadioButton("Par machine");
        rbGlobal.setToggleGroup(tg); rbMachine.setToggleGroup(tg);
        rbGlobal.setSelected(true);

        ComboBox<Machine> comboMach = new ComboBox<>();
        comboMach.setItems(FXCollections.observableArrayList(machines));
        comboMach.setPromptText("Sélectionner une machine");
        comboMach.setPrefWidth(200);
        comboMach.setCellFactory(lv -> new ListCell<Machine>() {
            @Override protected void updateItem(Machine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getNom());
            }
        });
        comboMach.setButtonCell(new ListCell<Machine>() {
            @Override protected void updateItem(Machine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? "Sélectionner..." : m.getNom());
            }
        });
        comboMach.setDisable(true);
        rbMachine.setOnAction(e -> comboMach.setDisable(false));
        rbGlobal.setOnAction(e  -> comboMach.setDisable(true));

        Button btnCalc = creerBouton("🔍 Calculer", "#27ae60");

        HBox ctrl = new HBox(15, rbGlobal, rbMachine, comboMach, btnCalc);
        ctrl.setAlignment(Pos.CENTER_LEFT);
        ctrl.setPadding(new Insets(12));
        ctrl.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        // Cartes
        Label cTotal = creerLabelStat("—"); Label cMoy = creerLabelStat("—");
        Label cNb    = creerLabelStat("—"); Label cMax = creerLabelStat("—");
        Label cMin   = creerLabelStat("—");

        HBox cartes = new HBox(10,
                creerCarte("💰 Total (DT)",   cTotal, "#27ae60"),
                creerCarte("📊 Moyenne",       cMoy,   "#3498db"),
                creerCarte("🔢 Nb",            cNb,    "#9b59b6"),
                creerCarte("⬆️ Max",            cMax,   "#e74c3c"),
                creerCarte("⬇️ Min",            cMin,   "#f39c12"));
        cartes.setPadding(new Insets(12));

        // Tableau synthèse
        TableView<Map<String, Object>> tblSynth = new TableView<>();
        tblSynth.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSynth.setPrefHeight(160);

        TableColumn<Map<String, Object>, String> sNom = new TableColumn<>("Machine");
        sNom.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("nom")));
        TableColumn<Map<String, Object>, String> sTot = new TableColumn<>("Total (DT)");
        sTot.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("total"))));
        TableColumn<Map<String, Object>, String> sMoy = new TableColumn<>("Moyenne");
        sMoy.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("moyenne"))));
        TableColumn<Map<String, Object>, String> sNb  = new TableColumn<>("Nb");
        sNb.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nb"))));
        TableColumn<Map<String, Object>, String> sMax = new TableColumn<>("Max");
        sMax.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("max"))));
        tblSynth.getColumns().addAll(sNom, sTot, sMoy, sNb, sMax);

        // Tableau détails
        TableView<Map<String, Object>> tblDet = new TableView<>();
        tblDet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblDet, Priority.ALWAYS);

        TableColumn<Map<String, Object>, String> dMach = new TableColumn<>("Machine");
        dMach.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("nomMachine")));
        TableColumn<Map<String, Object>, String> dType = new TableColumn<>("Type");
        dType.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("typePanne")));
        TableColumn<Map<String, Object>, String> dDate = new TableColumn<>("Date");
        dDate.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("dateMain")));
        TableColumn<Map<String, Object>, String> dCout = new TableColumn<>("Coût (DT)");
        dCout.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("cout"))));
        tblDet.getColumns().addAll(dMach, dType, dDate, dCout);

        Label lblTitreDet = new Label("Détail des maintenances");
        lblTitreDet.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        btnCalc.setOnAction(ev -> {
            try {
                ResultatCout res;
                if (rbGlobal.isSelected()) {
                    res = apiService.getCoutTotalGlobal();
                    lblTitreDet.setText("Détail — Toutes machines");
                } else {
                    Machine m = comboMach.getValue();
                    if (m == null) { showWarn("Sélection", "Sélectionnez une machine."); return; }
                    res = apiService.getCoutTotalParIdMachine(m.getIdM());
                    lblTitreDet.setText("Détail — " + m.getNom());
                }
                cTotal.setText(String.format("%.2f DT", res.total));
                cMoy.setText(String.format("%.2f DT",   res.moyenne));
                cNb.setText(String.valueOf(res.nombre));
                cMax.setText(String.format("%.2f DT",   res.max));
                cMin.setText(String.format("%.2f DT",   res.min));
                tblDet.setItems(FXCollections.observableArrayList(res.maintenances));
                tblSynth.setItems(FXCollections.observableArrayList(apiService.getCoutsParMachine()));
            } catch (SQLException e) { showErr("Erreur", e.getMessage()); }
        });

        Label titre = new Label("🏭 Coût Total de Maintenance par Machine");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox root = new VBox(10,
                ctrl,
                new VBox(6, titre, cartes),
                new Separator(),
                new VBox(6, new Label("Synthèse par machine :"), tblSynth),
                new Separator(),
                new VBox(6, lblTitreDet, tblDet));
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: #f5f6fa;");
        stage.setScene(new Scene(root, 980, 660));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════
    //  API 2 — ALERTES COÛTEUSES (FXML : onAction="#ouvrirAlertesCouteuses")
    // ═══════════════════════════════════════════════════════════
    @FXML private void ouvrirAlertesCouteuses() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🚨 Alertes — Maintenances Coûteuses");
        stage.setResizable(true);

        // Contrôles
        Label lblS = new Label("Seuil (DT):");
        lblS.setStyle("-fx-font-weight: bold;");
        TextField txtSeuil = new TextField("500");
        txtSeuil.setPrefWidth(90);
        txtSeuil.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-radius: 6;");

        Slider slider = new Slider(0, 5000, 500);
        slider.setPrefWidth(200);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(1000);
        slider.valueProperty().addListener((o, ov, nv) -> txtSeuil.setText(String.valueOf(nv.intValue())));
        txtSeuil.textProperty().addListener((o, ov, nv) -> {
            try { slider.setValue(Double.parseDouble(nv)); } catch (NumberFormatException ignored) {}
        });

        Button b200  = creerBoutonPreset("200 DT");
        Button b500  = creerBoutonPreset("500 DT");
        Button b1000 = creerBoutonPreset("1000 DT");
        Button bMoy  = creerBoutonPreset("Moy. auto");
        b200.setOnAction(e  -> { txtSeuil.setText("200");  slider.setValue(200); });
        b500.setOnAction(e  -> { txtSeuil.setText("500");  slider.setValue(500); });
        b1000.setOnAction(e -> { txtSeuil.setText("1000"); slider.setValue(1000); });
        bMoy.setOnAction(e  -> { txtSeuil.setText("0");    slider.setValue(0); });

        Label lblM = new Label("Machine:");
        lblM.setStyle("-fx-font-weight: bold;");
        TextField txtMach = new TextField();
        txtMach.setPromptText("(optionnel)");
        txtMach.setPrefWidth(130);

        Button btnAnalyser = creerBouton("🔍 Analyser", "#e74c3c");

        HBox barre = new HBox(10, lblS, txtSeuil, slider, b200, b500, b1000, bMoy, lblM, txtMach, btnAnalyser);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setPadding(new Insets(12));
        barre.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #fecaca; -fx-border-width: 0 0 1 0;");

        Label lblInfo = new Label("Saisissez un seuil et cliquez Analyser.");
        lblInfo.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-padding: 4 12;");
        Label lblErr  = new Label("");
        lblErr.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 2 12;");

        // Cartes
        Label cNb   = creerLabelStat("—"); Label cCout = creerLabelStat("—");
        Label cPctM = creerLabelStat("—"); Label cBudg = creerLabelStat("—");
        Label cSeui = creerLabelStat("—"); Label cMoyG = creerLabelStat("—");

        HBox cartes = new HBox(8,
                creerCarte("🚨 Nb Alertes",   cNb,   "#e74c3c"),
                creerCarte("💸 Coût Alertes", cCout, "#f39c12"),
                creerCarte("📊 % Mainten.",    cPctM, "#9b59b6"),
                creerCarte("💰 % Budget",      cBudg, "#e74c3c"),
                creerCarte("🎯 Seuil Appl.",  cSeui, "#3498db"),
                creerCarte("📈 Moy. Glob.",   cMoyG, "#27ae60"));
        cartes.setPadding(new Insets(10));

        // Tableau alertes
        TableView<Map<String, Object>> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tbl, Priority.ALWAYS);
        tbl.setPlaceholder(new Label("Aucune alerte — lancez une analyse."));

        TableColumn<Map<String, Object>, String> aId    = new TableColumn<>("ID");
        aId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("id"))));
        aId.setPrefWidth(45);
        TableColumn<Map<String, Object>, String> aMach  = new TableColumn<>("Machine");
        aMach.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("nomMachine")));
        TableColumn<Map<String, Object>, String> aType  = new TableColumn<>("Type");
        aType.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("typePanne")));
        TableColumn<Map<String, Object>, String> aDate  = new TableColumn<>("Date");
        aDate.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("dateMain")));
        TableColumn<Map<String, Object>, String> aCout  = new TableColumn<>("Coût (DT)");
        aCout.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("cout"))));
        TableColumn<Map<String, Object>, String> aNiv   = new TableColumn<>("Niveau");
        aNiv.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("niveau")));
        aNiv.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.contains("CRITIQUE") ? "-fx-text-fill:#c0392b;-fx-font-weight:bold;"
                        : item.contains("ÉLEVÉ")   ? "-fx-text-fill:#e67e22;-fx-font-weight:bold;"
                        :                            "-fx-text-fill:#f1c40f;-fx-font-weight:bold;");
            }
        });
        TableColumn<Map<String, Object>, String> aDesc  = new TableColumn<>("Description");
        aDesc.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("description")));
        tbl.getColumns().addAll(aId, aMach, aType, aDate, aCout, aNiv, aDesc);

        tbl.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                String n = (String) item.get("niveau");
                setStyle(n != null && n.contains("CRITIQUE") ? "-fx-background-color:#fde8e8;"
                        : n != null && n.contains("ÉLEVÉ")   ? "-fx-background-color:#fef3e2;"
                        :                                      "-fx-background-color:#fefce8;");
            }
        });

        // Tableaux par type / par machine
        TableView<Map.Entry<String, Integer>> tblType = new TableView<>();
        tblType.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblType.setPrefHeight(140);
        TableColumn<Map.Entry<String, Integer>, String> ptT = new TableColumn<>("Type");
        ptT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKey()));
        TableColumn<Map.Entry<String, Integer>, String> ptN = new TableColumn<>("Nb");
        ptN.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getValue())));
        tblType.getColumns().addAll(ptT, ptN);

        TableView<Map.Entry<String, Integer>> tblMachA = new TableView<>();
        tblMachA.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblMachA.setPrefHeight(140);
        TableColumn<Map.Entry<String, Integer>, String> pmM = new TableColumn<>("Machine");
        pmM.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKey()));
        TableColumn<Map.Entry<String, Integer>, String> pmN = new TableColumn<>("Nb");
        pmN.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getValue())));
        tblMachA.getColumns().addAll(pmM, pmN);

        HBox infTbl = new HBox(16,
                new VBox(6, new Label("🔧 Répartition par type :"), tblType),
                new VBox(6, new Label("🏭 Par machine:"),           tblMachA));
        HBox.setHgrow(infTbl.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(infTbl.getChildren().get(1), Priority.ALWAYS);

        btnAnalyser.setOnAction(ev -> {
            lblErr.setText("");
            double seuil;
            try {
                String s = txtSeuil.getText().trim();
                seuil = s.isEmpty() ? 0 : Double.parseDouble(s);
            } catch (NumberFormatException e) { lblErr.setText("❌ Seuil invalide."); return; }

            lblInfo.setText("Analyse : seuil=" + seuil
                    + (txtMach.getText().trim().isEmpty() ? "" : " | machine=" + txtMach.getText().trim()));
            try {
                ResultatAlerte res = apiService.getMaintenancesCoûteuses(seuil, txtMach.getText().trim());

                double coutAl = res.alertes.stream().mapToDouble(a -> (double) a.get("cout")).sum();
                double pctM   = res.nbTotal > 0 ? (res.alertes.size() * 100.0 / res.nbTotal) : 0;
                double pctB   = res.coutTotal > 0 ? (coutAl * 100.0 / res.coutTotal) : 0;

                cNb.setText(String.valueOf(res.alertes.size()));
                cCout.setText(String.format("%.2f DT", coutAl));
                cPctM.setText(String.format("%.1f%%", pctM));
                cBudg.setText(String.format("%.1f%%", pctB));
                cSeui.setText(String.format("%.2f DT", res.seuilApplique));
                cMoyG.setText(String.format("%.2f DT", res.moyenneGlobale));

                tbl.setItems(FXCollections.observableArrayList(res.alertes));
                tblType.setItems(FXCollections.observableArrayList(new ArrayList<>(res.parType.entrySet())));
                tblMachA.setItems(FXCollections.observableArrayList(new ArrayList<>(res.parMachine.entrySet())));
            } catch (SQLException e) { lblErr.setText("❌ Erreur : " + e.getMessage()); }
        });

        Label titre = new Label("🚨 Alertes — Maintenances Coûteuses");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");

        VBox root = new VBox(0,
                barre, new HBox(lblInfo), new HBox(lblErr), new Separator(),
                new VBox(6, titre, cartes), new Separator(),
                new VBox(6, new Label("🔴 Maintenances dépassant le seuil :"), tbl),
                new Separator(), infTbl);
        root.setPadding(new Insets(12));
        root.setSpacing(8);
        root.setStyle("-fx-background-color: #f5f6fa;");
        stage.setScene(new Scene(root, 1200, 760));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════
    //  EXPORT PDF
    // ═══════════════════════════════════════════════════════════
    @FXML private void exporterPDF() {
        if (displayList.isEmpty()) { showInfo("Export PDF", "Aucune donnée."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer PDF");
        fc.setInitialFileName("maintenances_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(tableMaintenances.getScene().getWindow());
        if (file == null) return;

        try (PdfWriter writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf  = new PdfDocument(writer);
             Document    doc  = new Document(pdf)) {

            doc.add(new Paragraph("Rapport — Gestion des Maintenances")
                    .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Généré le : " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));

            float[] colW = {100f, 110f, 70f, 70f, 180f};
            Table table  = new Table(UnitValue.createPointArray(colW));
            table.setWidth(UnitValue.createPercentValue(100));

            for (String h : new String[]{"Machine", "Type Panne", "Date", "Coût (DT)", "Description"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Maintenance m : displayList) {
                table.addCell(new Cell().add(new Paragraph(nomMachineParId(m.getIdM())).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(m.getTypePanne() != null ? m.getTypePanne() : "").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(m.getDateMain() != null ? m.getDateMain().format(fmt) : "").setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", m.getCout())).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(m.getDescription() != null ? m.getDescription() : "").setFontSize(9)));
            }
            doc.add(table);
            doc.add(new Paragraph("\nTotal : " + displayList.size() + " maintenances").setFontSize(10).setBold());
            double total = displayList.stream().mapToDouble(Maintenance::getCout).sum();
            doc.add(new Paragraph("Coût total : " + String.format("%.2f DT", total)).setFontSize(10).setBold());

        } catch (Exception e) { showErr("Erreur PDF", e.getMessage()); return; }
        showInfo("✅ PDF", "Fichier : " + file.getAbsolutePath());
    }

    // ═══════════════════════════════════════════════════════════
    //  EXPORT EXCEL
    // ═══════════════════════════════════════════════════════════
    @FXML private void exporterExcel() {
        if (displayList.isEmpty()) { showInfo("Export Excel", "Aucune donnée."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer Excel");
        fc.setInitialFileName("maintenances_" + LocalDate.now() + ".xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(tableMaintenances.getScene().getWindow());
        if (file == null) return;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Maintenances");

            String[] hdr = {"Machine", "Type Panne", "Date", "Coût (DT)", "Description"};
            CellStyle csH = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font fH = wb.createFont();
            fH.setBold(true); csH.setFont(fH);
            csH.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            csH.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row rH = sheet.createRow(0);
            for (int i = 0; i < hdr.length; i++) {
                // ← Nom qualifié complet pour éviter ambiguïté JavaFX Cell / POI Cell
                org.apache.poi.ss.usermodel.Cell c = rH.createCell(i);
                c.setCellValue(hdr[i]);
                c.setCellStyle(csH);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowIdx = 1;
            for (Maintenance m : displayList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nomMachineParId(m.getIdM()));
                row.createCell(1).setCellValue(m.getTypePanne() != null ? m.getTypePanne() : "");
                row.createCell(2).setCellValue(m.getDateMain() != null ? m.getDateMain().format(fmt) : "");
                row.createCell(3).setCellValue(m.getCout());
                row.createCell(4).setCellValue(m.getDescription() != null ? m.getDescription() : "");
            }

            Row total = sheet.createRow(rowIdx + 1);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(3).setCellValue(displayList.stream().mapToDouble(Maintenance::getCout).sum());
            for (int i = 0; i < hdr.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }

        } catch (Exception e) { showErr("Erreur Excel", e.getMessage()); return; }
        showInfo("✅ Excel", "Fichier : " + file.getAbsolutePath());
    }

    // ═══════════════════════════════════════════════════════════
    //  NAVIGATION ← noms exacts du FXML
    // ═══════════════════════════════════════════════════════════
    @FXML private void naviguerAnimaux()    { nav("/GestionAnimaux.fxml"); }
    @FXML private void naviguerMateriels()  { nav("/AccueilMateriel.fxml"); }
    @FXML private void naviguerStocks()     { nav("/GestionStocks.fxml"); }
    @FXML private void naviguerTerrains()   { nav("/GestionTerrains.fxml"); }
    @FXML private void naviguerEvenements() { nav("/GestionEvenements.fxml"); }
    @FXML private void naviguerUsers()      { nav("/GestionUsers.fxml"); }
    @FXML private void retourAccueil()      { nav("/AccueilMateriel.fxml"); }

    @FXML private void deconnexion() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion");
        a.setContentText("Voulez-vous vous déconnecter ?");
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) nav("/Login.fxml");
    }

    private void nav(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) tableMaintenances.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showErr("Navigation", "Impossible de naviguer vers : " + path + "\n" + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════
    private String nomMachineParId(int idM) {
        for (Machine m : machines)
            if (m.getIdM() == idM) return m.getNom();
        return "Machine #" + idM;
    }

    private Label creerLabelStat(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        return l;
    }

    private VBox creerCarte(String titre, Label valeur, String couleur) {
        Label t = new Label(titre);
        t.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");
        VBox c = new VBox(4, t, valeur);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(10, 14, 10, 14));
        c.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 8;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private Button creerBouton(String texte, String couleur) {
        Button b = new Button(texte);
        b.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 16;");
        return b;
    }

    private Button creerBoutonPreset(String texte) {
        Button b = new Button(texte);
        b.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 4 10;");
        return b;
    }

    private void showInfo(String t, String m) { alert(Alert.AlertType.INFORMATION, t, m); }
    private void showErr (String t, String m) { alert(Alert.AlertType.ERROR,       t, m); }
    private void showWarn(String t, String m) { alert(Alert.AlertType.WARNING,     t, m); }

    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}