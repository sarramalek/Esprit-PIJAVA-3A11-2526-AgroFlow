package controllers.Materiels;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import models.Materiels.Machine;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;
import models.Materiels.Maintenance;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AgricoleAffichageMachineController implements Initializable {
    @FXML private Button dashboardBtn;

    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;


    // ══════════════════════════════════════════════════════
    //  FXML — Tableau
    // ══════════════════════════════════════════════════════

    @FXML private TableView<Machine>           tableMachines;
    @FXML private TableColumn<Machine, String> colNom;
    @FXML private TableColumn<Machine, String> colMarque;
    @FXML private TableColumn<Machine, String> colModele;
    @FXML private TableColumn<Machine, String> colEtat;
    @FXML private TableColumn<Machine, String> colNumeroSerie;
    @FXML private TableColumn<Machine, String> colDateAchat;

    // ══════════════════════════════════════════════════════
    //  FXML — Filtres & Recherche
    // ══════════════════════════════════════════════════════

    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboEtat;
    @FXML private ComboBox<String> comboMarque;
    @FXML private Label            lblSelectionInfo;

    // ══════════════════════════════════════════════════════
    //  FXML — Cards BAS
    // ══════════════════════════════════════════════════════

    @FXML private Label lblTotal;
    @FXML private Label lblTotalFiltre;
    @FXML private Label lblNbBonEtat;
    @FXML private Label lblPctBonEtat;
    @FXML private Label lblNbMaintenance;
    @FXML private Label lblPctMaintenance;
    @FXML private Label lblNbHorsService;
    @FXML private Label lblPctHorsService;
    @FXML private Label lblMarqueDominante;
    @FXML private Label lblNbMarqueDominante;

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar
    // ══════════════════════════════════════════════════════

    @FXML private Button logoutBtn;

    // ══════════════════════════════════════════════════════
    //  Services & Données
    // ══════════════════════════════════════════════════════

    private MachineService     machineService;
    private MaintenanceService maintenanceService;

    private final ObservableList<Machine> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Machine> displayList = FXCollections.observableArrayList();

    public void ouvrirMaintenance(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMaintenance.fxml","Maintenance");
    }

    public void ouvrirAchat(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageAchat.fxml","Maintenance");

    }

    public void ouvrirMachine(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Maintenance");

    }

    private enum SortMode { NOM_AZ, NOM_ZA, DATE_RECENT, DATE_ANCIEN, MARQUE_AZ, NONE }
    private SortMode currentSort = SortMode.NOM_AZ;

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());
        machineService     = new MachineService();
        maintenanceService = new MaintenanceService();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();

        // Sélection ligne → info
        tableMachines.getSelectionModel().selectedItemProperty()
                .addListener((obs, ov, nv) -> {
                    if (lblSelectionInfo != null && nv != null) {
                        lblSelectionInfo.setText(
                                "Sélectionné : " + nv.getNom()
                                        + " — " + nv.getMarque()
                                        + " " + nv.getModele()
                                        + " | État : " + nv.getEtatM());
                    } else if (lblSelectionInfo != null) {
                        lblSelectionInfo.setText("Cliquez sur une ligne pour voir les détails");
                    }
                });
    }

    // ══════════════════════════════════════════════════════
    //  CONFIGURATION TABLEAU  (lecture seule)
    // ══════════════════════════════════════════════════════

    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colNom.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNom()));
        colMarque.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMarque()));
        colModele.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getModele()));
        colNumeroSerie.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumeroSerie()));
        colDateAchat.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDateAchat();
            return new SimpleStringProperty(date != null ? date.format(fmt) : "");
        });

        // Colonne État — colorée selon valeur
        colEtat.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEtatM()));
        colEtat.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                String lower = val.toLowerCase();
                if (lower.contains("bon") || lower.contains("neuf") || lower.contains("excellent"))
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                else if (lower.contains("maintenance") || lower.contains("réparation") || lower.contains("reparation"))
                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                else if (lower.contains("hors") || lower.contains("panne") || lower.contains("défectueux"))
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #2c3e50;");
            }
        });

        // Lignes alternées + rouge si hors service
        tableMachines.setRowFactory(tv -> new TableRow<Machine>() {
            @Override
            protected void updateItem(Machine item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                String etat = item.getEtatM() != null ? item.getEtatM().toLowerCase() : "";
                if (etat.contains("hors") || etat.contains("panne"))
                    setStyle("-fx-background-color: #fff5f5;");
                else if (getIndex() % 2 == 1)
                    setStyle("-fx-background-color: #f0f9f0;");
                else
                    setStyle("");
            }
        });

        tableMachines.setEditable(false);
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT DONNÉES
    // ══════════════════════════════════════════════════════

    private void chargerDonnees() {
        masterList.clear();
        try { masterList.addAll(machineService.recuperer()); }
        catch (SQLException e) { showErr("Erreur SQL", e.getMessage()); }
        initialiserCombos();
        appliquerFiltres();
    }

    private void initialiserCombos() {
        // ComboBox État
        if (comboEtat != null) {
            Set<String> etats = new LinkedHashSet<>();
            etats.add("Tous les états");
            masterList.stream()
                    .map(Machine::getEtatM)
                    .filter(e -> e != null && !e.isBlank())
                    .distinct().sorted()
                    .forEach(etats::add);
            comboEtat.setItems(FXCollections.observableArrayList(etats));
            comboEtat.getSelectionModel().selectFirst();
        }
        // ComboBox Marque
        if (comboMarque != null) {
            Set<String> marques = new LinkedHashSet<>();
            marques.add("Toutes les marques");
            masterList.stream()
                    .map(Machine::getMarque)
                    .filter(m -> m != null && !m.isBlank())
                    .distinct().sorted()
                    .forEach(marques::add);
            comboMarque.setItems(FXCollections.observableArrayList(marques));
            comboMarque.getSelectionModel().selectFirst();
        }
    }

    // ══════════════════════════════════════════════════════
    //  RECHERCHE DYNAMIQUE
    // ══════════════════════════════════════════════════════

    private void configurerRecherche() {
        if (champRecherche != null)
            champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    @FXML private void rechercher() { appliquerFiltres(); }
    @FXML private void filtrer()    { appliquerFiltres(); }

    @FXML
    private void effacerRecherche() {
        if (champRecherche != null) champRecherche.clear();
        appliquerFiltres();
    }

    // ══════════════════════════════════════════════════════
    //  TRIS
    // ══════════════════════════════════════════════════════

    @FXML private void trierNomAZ()      { currentSort = SortMode.NOM_AZ;      appliquerFiltres(); }
    @FXML private void trierNomZA()      { currentSort = SortMode.NOM_ZA;      appliquerFiltres(); }
    @FXML private void trierDateRecent() { currentSort = SortMode.DATE_RECENT; appliquerFiltres(); }
    @FXML private void trierDateAncien() { currentSort = SortMode.DATE_ANCIEN; appliquerFiltres(); }
    @FXML private void trierMarqueAZ()   { currentSort = SortMode.MARQUE_AZ;   appliquerFiltres(); }

    @FXML
    private void reinitialiserFiltres() {
        if (champRecherche != null) champRecherche.clear();
        if (comboEtat      != null) comboEtat.getSelectionModel().selectFirst();
        if (comboMarque    != null) comboMarque.getSelectionModel().selectFirst();
        currentSort = SortMode.NOM_AZ;
        appliquerFiltres();
    }

    @FXML private void actualiser() { chargerDonnees(); }

    // ══════════════════════════════════════════════════════
    //  FILTRES + TRI  (logique centrale)
    // ══════════════════════════════════════════════════════

    private void appliquerFiltres() {
        String recherche = champRecherche != null && champRecherche.getText() != null
                ? champRecherche.getText().toLowerCase().trim() : "";
        String etatSel   = comboEtat   != null && comboEtat.getValue()   != null
                ? comboEtat.getValue()   : "Tous les états";
        String marqueSel = comboMarque != null && comboMarque.getValue() != null
                ? comboMarque.getValue() : "Toutes les marques";

        List<Machine> filtered = masterList.stream()
                .filter(m -> {
                    // Filtre texte libre
                    boolean matchR = recherche.isEmpty()
                            || safe(m.getNom()).contains(recherche)
                            || safe(m.getMarque()).contains(recherche)
                            || safe(m.getModele()).contains(recherche)
                            || safe(m.getEtatM()).contains(recherche)
                            || safe(m.getNumeroSerie()).contains(recherche)
                            || (m.getDateAchat() != null && m.getDateAchat().toString().contains(recherche));

                    // Filtre état
                    boolean matchE = etatSel.equals("Tous les états")
                            || safe(m.getEtatM()).equalsIgnoreCase(etatSel.toLowerCase());

                    // Filtre marque
                    boolean matchM = marqueSel.equals("Toutes les marques")
                            || safe(m.getMarque()).equalsIgnoreCase(marqueSel.toLowerCase());

                    return matchR && matchE && matchM;
                })
                .collect(Collectors.toList());

        // Tri
        switch (currentSort) {
            case NOM_AZ      -> filtered.sort(Comparator.comparing(
                    m -> safe(m.getNom())));
            case NOM_ZA      -> filtered.sort(Comparator.comparing(
                    (Machine m) -> safe(m.getNom())).reversed());
            case DATE_RECENT -> filtered.sort(Comparator.comparing(
                    (Machine m) -> m.getDateAchat() != null ? m.getDateAchat() : LocalDate.MIN).reversed());
            case DATE_ANCIEN -> filtered.sort(Comparator.comparing(
                    m -> m.getDateAchat() != null ? m.getDateAchat() : LocalDate.MIN));
            case MARQUE_AZ   -> filtered.sort(Comparator.comparing(
                    m -> safe(m.getMarque())));
            default -> {}
        }

        displayList.setAll(filtered);
        tableMachines.setItems(displayList);
        mettreAJourStats();
    }

    // ══════════════════════════════════════════════════════
    //  STATISTIQUES  (cards BAS)
    // ══════════════════════════════════════════════════════

    private void mettreAJourStats() {
        int nb = displayList.size();

        // Comptage par état
        long nbBon         = displayList.stream().filter(m -> {
            String e = safe(m.getEtatM());
            return e.contains("bon") || e.contains("neuf") || e.contains("excellent");
        }).count();
        long nbMainten     = displayList.stream().filter(m -> {
            String e = safe(m.getEtatM());
            return e.contains("maintenance") || e.contains("réparation") || e.contains("reparation");
        }).count();
        long nbHors        = displayList.stream().filter(m -> {
            String e = safe(m.getEtatM());
            return e.contains("hors") || e.contains("panne") || e.contains("défectueux");
        }).count();

        double pctBon     = nb > 0 ? (nbBon     * 100.0 / nb) : 0;
        double pctMainten = nb > 0 ? (nbMainten * 100.0 / nb) : 0;
        double pctHors    = nb > 0 ? (nbHors    * 100.0 / nb) : 0;

        // Marque dominante
        Map<String, Long> parMarque = new HashMap<>();
        for (Machine m : displayList) {
            String mk = m.getMarque() != null ? m.getMarque() : "Inconnue";
            parMarque.merge(mk, 1L, Long::sum);
        }
        String marqueDom  = parMarque.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("—");
        long   nbMarqueDom = parMarque.getOrDefault(marqueDom, 0L);

        set(lblTotal,             String.valueOf(nb));
        set(lblTotalFiltre,       nb + " affiché(s) / " + masterList.size() + " total");
        set(lblNbBonEtat,         String.valueOf(nbBon));
        set(lblPctBonEtat,        String.format("%.0f%%", pctBon));
        set(lblNbMaintenance,     String.valueOf(nbMainten));
        set(lblPctMaintenance,    String.format("%.0f%%", pctMainten));
        set(lblNbHorsService,     String.valueOf(nbHors));
        set(lblPctHorsService,    String.format("%.0f%%", pctHors));
        set(lblMarqueDominante,   marqueDom);
        set(lblNbMarqueDominante, nbMarqueDom + " machine(s)");
    }

    // ══════════════════════════════════════════════════════
    //  STATS GRAPHIQUES  (popup Canvas)
    // ══════════════════════════════════════════════════════

    @FXML
    private void afficherStatistiquesGraphiques() {
        if (displayList.isEmpty()) { showInfo("Stats", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques — Mon Matériel");
        stage.setResizable(true);

        // Données : répartition par état
        Map<String, Long> parEtat = new LinkedHashMap<>();
        for (Machine m : displayList) {
            String e = m.getEtatM() != null ? m.getEtatM() : "Inconnu";
            parEtat.merge(e, 1L, Long::sum);
        }
        // Données : répartition par marque
        Map<String, Long> parMarque = new LinkedHashMap<>();
        for (Machine m : displayList) {
            String mk = m.getMarque() != null ? m.getMarque() : "Inconnue";
            parMarque.merge(mk, 1L, Long::sum);
        }

        Color[] colors = {
                Color.web("#27ae60"), Color.web("#f39c12"), Color.web("#e74c3c"),
                Color.web("#3498db"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63")
        };

        // ── Graphique barres par état ──
        List<String> etatKeys  = new ArrayList<>(parEtat.keySet());
        List<Long>   etatVals  = new ArrayList<>(parEtat.values());
        int nE = etatKeys.size();
        int barW = 60, gap = 30, padL = 70, padTop = 50, chartH = 220;
        int cwE = padL + nE * (barW + gap) + gap + 20;

        Canvas canvasEtat = new Canvas(cwE, chartH + 90 + padTop);
        GraphicsContext ge = canvasEtat.getGraphicsContext2D();
        ge.setFill(Color.WHITE); ge.fillRect(0, 0, cwE, chartH + 90 + padTop);
        ge.setFill(Color.web("#2E7D32"));
        ge.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ge.fillText("⚙ Répartition par état", padL, 30);

        long maxE = etatVals.stream().mapToLong(v -> v).max().orElse(1);
        for (int i = 0; i <= 4; i++) {
            double y = padTop + chartH - (chartH * i / 4.0);
            ge.setStroke(Color.LIGHTGRAY); ge.setLineWidth(1);
            ge.strokeLine(padL, y, cwE - 20, y);
            ge.setFill(Color.GRAY); ge.setFont(Font.font("Arial", 10));
            ge.fillText(String.format("%.0f", maxE * i / 4.0), 5, y + 4);
        }
        for (int i = 0; i < nE; i++) {
            double bH = (etatVals.get(i) / (double) maxE) * chartH;
            double x  = padL + gap + i * (barW + gap);
            double y  = padTop + chartH - bH;
            Color  c  = colors[i % colors.length];
            ge.setFill(Color.rgb(0,0,0,0.07));
            ge.fillRoundRect(x+3, y+3, barW, bH, 6, 6);
            ge.setFill(c);
            ge.fillRoundRect(x, y, barW, bH, 6, 6);
            ge.setFill(Color.web("#2E7D32")); ge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.valueOf(etatVals.get(i));
            ge.fillText(vs, x + barW/2.0 - vs.length()*3.5, y - 6);
            ge.setFont(Font.font("Arial", 10));
            String lbl = etatKeys.get(i).length() > 12
                    ? etatKeys.get(i).substring(0, 12)+"…" : etatKeys.get(i);
            ge.fillText(lbl, x + barW/2.0 - lbl.length()*3, padTop + chartH + 16);
        }

        // ── Graphique barres par marque ──
        List<String> marqueKeys = new ArrayList<>(parMarque.keySet());
        List<Long>   marqueVals = new ArrayList<>(parMarque.values());
        int nM = marqueKeys.size();
        int cwM  = 400;
        int chM  = padTop + 40 + nM * 42 + 20;

        Canvas canvasMarque = new Canvas(cwM, Math.max(chM, chartH + 90 + padTop));
        GraphicsContext gm  = canvasMarque.getGraphicsContext2D();
        gm.setFill(Color.WHITE); gm.fillRect(0, 0, cwM, Math.max(chM, chartH+90+padTop));
        gm.setFill(Color.web("#2E7D32"));
        gm.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gm.fillText("🏷 Répartition par marque", 10, 30);

        long maxM    = marqueVals.stream().mapToLong(v -> v).max().orElse(1);
        int  barAreaW = cwM - 160;
        for (int i = 0; i < nM; i++) {
            double bH = 24, y = padTop + i * 42;
            double bW = (marqueVals.get(i) / (double) maxM) * barAreaW;
            Color  c  = colors[i % colors.length];
            gm.setFill(Color.rgb(0,0,0,0.06));
            gm.fillRoundRect(153, y+3, bW, bH, 6, 6);
            gm.setFill(c);
            gm.fillRoundRect(150, y, bW, bH, 6, 6);
            gm.setFill(Color.web("#2E7D32")); gm.setFont(Font.font("Arial", 11));
            String lbl = marqueKeys.get(i).length() > 14
                    ? marqueKeys.get(i).substring(0,14)+"…" : marqueKeys.get(i);
            gm.fillText(lbl, 5, y+17);
            gm.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gm.fillText(String.valueOf(marqueVals.get(i)), 150 + bW + 6, y + 17);
        }

        ScrollPane spL = new ScrollPane(canvasEtat);   spL.setFitToHeight(true); spL.setPrefWidth(cwE + 20);
        ScrollPane spR = new ScrollPane(canvasMarque); spR.setFitToHeight(true); spR.setPrefWidth(cwM + 20);

        HBox charts = new HBox(16, spL, spR);
        charts.setPadding(new Insets(10));

        VBox root = new VBox(8, charts);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ══════════════════════════════════════════════════════
    //  ANALYSE AVANCÉE (API maintenances par machine)
    // ══════════════════════════════════════════════════════

    @FXML
    private void afficherStatistiquesAvancees() {
        if (masterList.isEmpty()) { showInfo("Analyse", "Aucune machine disponible."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🏭 Analyse Avancée — Coût de Maintenance par Machine");
        stage.setResizable(true);

        // ── Charger toutes les maintenances ──
        List<Maintenance> toutesMaintenances = new ArrayList<>();
        try { toutesMaintenances = maintenanceService.recuperer(); }
        catch (SQLException e) { showErr("Erreur", e.getMessage()); return; }

        final List<Maintenance> maintenancesFinal = toutesMaintenances;

        // ── KPI globaux ──
        int    nbMachines     = masterList.size();
        int    nbMaintenances = maintenancesFinal.size();
        double coutTotal      = maintenancesFinal.stream().mapToDouble(Maintenance::getCout).sum();
        double coutMoyen      = nbMaintenances > 0 ? coutTotal / nbMaintenances : 0;

        // ── Coût par machine ──
        Map<Integer, Double> coutParId = new HashMap<>();
        Map<Integer, Long>   nbParId   = new HashMap<>();
        for (Maintenance m : maintenancesFinal) {
            coutParId.merge(m.getIdM(), m.getCout(), Double::sum);
            nbParId.merge(m.getIdM(), 1L, Long::sum);
        }

        // Machine la plus coûteuse
        int    idMax      = coutParId.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(-1);
        String nomMax     = idMax >= 0 ? nomMachineParId(idMax) : "—";
        double coutMax    = idMax >= 0 ? coutParId.get(idMax) : 0;

        // ── Tableau synthèse par machine ──
        TableView<Map<String, Object>> tblSynth = new TableView<>();
        tblSynth.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSynth.setPrefHeight(200);

        TableColumn<Map<String, Object>, String> sNom = new TableColumn<>("Machine");
        sNom.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("nom")));
        TableColumn<Map<String, Object>, String> sNbM = new TableColumn<>("Nb Maintenances");
        sNbM.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().get("nb"))));
        TableColumn<Map<String, Object>, String> sTot = new TableColumn<>("Coût Total (DT)");
        sTot.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.2f", (double) d.getValue().get("total"))));
        TableColumn<Map<String, Object>, String> sMoy = new TableColumn<>("Coût Moyen (DT)");
        sMoy.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.2f", (double) d.getValue().get("moyenne"))));
        TableColumn<Map<String, Object>, String> sPct = new TableColumn<>("% du total");
        sPct.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.1f%%", (double) d.getValue().get("pct"))));

        tblSynth.getColumns().addAll(sNom, sNbM, sTot, sMoy, sPct);

        // Remplir le tableau
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Machine m : masterList) {
            double tot  = coutParId.getOrDefault(m.getIdM(), 0.0);
            long   nb   = nbParId.getOrDefault(m.getIdM(), 0L);
            double moy  = nb > 0 ? tot / nb : 0;
            double pct  = coutTotal > 0 ? (tot * 100.0 / coutTotal) : 0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nom",     m.getNom());
            row.put("nb",      nb);
            row.put("total",   tot);
            row.put("moyenne", moy);
            row.put("pct",     pct);
            rows.add(row);
        }
        // Trier par coût décroissant
        rows.sort((a, b) -> Double.compare((double) b.get("total"), (double) a.get("total")));
        tblSynth.setItems(FXCollections.observableArrayList(rows));

        // ── Cards KPI ──
        HBox cartes = new HBox(10,
                creerCarte("🚜 Machines",            String.valueOf(nbMachines),     "#27ae60"),
                creerCarte("🔧 Maintenances",        String.valueOf(nbMaintenances),  "#3498db"),
                creerCarte("💰 Coût Total (DT)",     String.format("%.2f", coutTotal), "#e74c3c"),
                creerCarte("📊 Coût Moyen (DT)",     String.format("%.2f", coutMoyen), "#f39c12"),
                creerCarte("🏭 + Coûteuse",          nomMax + " (" + String.format("%.0f DT", coutMax) + ")", "#9b59b6")
        );
        cartes.setPadding(new Insets(10));

        // ── Graphique Canvas coût par machine ──
        int nG = Math.min(rows.size(), 10); // max 10 pour lisibilité
        int bW = 55, gp = 20, pL = 75, pT = 50, cH = 200;
        int cw = pL + nG * (bW + gp) + gp + 20;
        Canvas canvas = new Canvas(cw, cH + 80 + pT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE); gc.fillRect(0, 0, cw, cH + 80 + pT);
        gc.setFill(Color.web("#2E7D32")); gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("💰 Coût de maintenance par machine (DT)", pL, 30);

        double maxVal = rows.stream().mapToDouble(r -> (double) r.get("total")).max().orElse(1);
        Color[] cls = {Color.web("#27ae60"), Color.web("#3498db"), Color.web("#e74c3c"),
                Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63"), Color.web("#2ecc71"), Color.web("#16a085")};

        for (int i = 0; i <= 4; i++) {
            double y = pT + cH - (cH * i / 4.0);
            gc.setStroke(Color.LIGHTGRAY); gc.setLineWidth(1);
            gc.strokeLine(pL, y, cw - 20, y);
            gc.setFill(Color.GRAY); gc.setFont(Font.font("Arial", 10));
            gc.fillText(String.format("%.0f", maxVal * i / 4.0), 5, y + 4);
        }
        for (int i = 0; i < nG; i++) {
            double tot  = (double) rows.get(i).get("total");
            double bH   = (tot / maxVal) * cH;
            double x    = pL + gp + i * (bW + gp);
            double y    = pT + cH - bH;
            Color  c    = cls[i % cls.length];
            gc.setFill(Color.rgb(0,0,0,0.07)); gc.fillRoundRect(x+3, y+3, bW, bH, 6, 6);
            gc.setFill(c);               gc.fillRoundRect(x, y, bW, bH, 6, 6);
            gc.setFill(Color.web("#2E7D32")); gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.format("%.0f", tot);
            gc.fillText(vs, x + bW/2.0 - vs.length()*3.5, y - 6);
            gc.setFont(Font.font("Arial", 10));
            String lbl = (String) rows.get(i).get("nom");
            if (lbl.length() > 10) lbl = lbl.substring(0, 10) + "…";
            gc.fillText(lbl, x + bW/2.0 - lbl.length()*3, pT + cH + 16);
        }
        // Ligne moyenne
        double yMoy = pT + cH - (coutMoyen / maxVal) * cH;
        if (maxVal > 0 && coutMoyen <= maxVal) {
            gc.setStroke(Color.web("#e74c3c")); gc.setLineWidth(2);
            gc.setLineDashes(8, 4);
            gc.strokeLine(pL, yMoy, cw - 20, yMoy);
            gc.setLineDashes();
            gc.setFill(Color.web("#e74c3c")); gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText(String.format("Moy. %.0f DT", coutMoyen), cw - 80, yMoy - 4);
        }

        ScrollPane spCanvas = new ScrollPane(canvas);
        spCanvas.setFitToHeight(true);
        spCanvas.setPrefSize(Math.min(cw + 20, 900), cH + 100 + pT);

        Label titreSynth = new Label("📋 Synthèse détaillée par machine (triée par coût décroissant)");
        titreSynth.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        Label legende = new Label("  ── Ligne rouge = coût moyen par maintenance · Top 10 machines affichées dans le graphique");
        legende.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox root = new VBox(12,
                cartes,
                new Separator(),
                legende,
                spCanvas,
                new Separator(),
                titreSynth,
                tblSynth
        );
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8f9fa;");
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR AGRICOLE
    // ══════════════════════════════════════════════════════

    @FXML private void handleMesMaintenances(){ nav("/AgricoleAffichageMaintenance.fxml"); }

    private void nav(String path) {
        try {
            URL url = getClass().getResource(path);
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
            showErr("Navigation", "Impossible d'ouvrir : " + path + "\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  LOGOUT
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion");
        a.setHeaderText("Déconnexion");
        a.setContentText("Voulez-vous vraiment vous déconnecter ?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/UsersInterface/Login.fxml"));
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    // On récupère le Stage et la Scene ACTUELLE
                    Scene scene = stage.getScene();

                    // SOLUTION MIRACLE : On change la racine, pas la scène !
                    scene.setRoot(root);

                    // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                    stage.show();
                } catch (IOException e) {
                    showErr("Erreur", "Erreur déconnexion : " + e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════

    /** Retourne le nom d'une machine par son idM */
    private String nomMachineParId(int idM) {
        return masterList.stream()
                .filter(m -> m.getIdM() == idM)
                .map(Machine::getNom)
                .findFirst()
                .orElse("Machine #" + idM);
    }

    /** String null-safe en lowercase */
    private String safe(String s) {
        return s != null ? s.toLowerCase() : "";
    }

    /** Setter null-safe pour Label */
    private void set(Label lbl, String val) {
        if (lbl != null) lbl.setText(val);
    }

    /** Crée une carte KPI colorée pour la popup */
    private VBox creerCarte(String titre, String valeur, String couleur) {
        Label t = new Label(titre);
        t.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.85);");
        Label v = new Label(valeur);
        v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        v.setWrapText(true);
        VBox c = new VBox(4, t, v);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(10, 14, 10, 14));
        c.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private void showInfo(String t, String m) { alert(Alert.AlertType.INFORMATION, t, m); }
    private void showErr (String t, String m) { alert(Alert.AlertType.ERROR,       t, m); }

    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }


    // navigation Front Office Agricole
    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;

        // Nom et rôle
        if (userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Clip circulaire appliqué en Java (radius=35, centre=35,35 pour fitWidth/Height=70)
        if (sidebarAvatarImageView != null) {
            Circle clip = new Circle(35, 35, 35);
            sidebarAvatarImageView.setClip(clip);
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Avatar sidebar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
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
        }
    }
    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }
    @FXML private void handleDashboardAgricole(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         navigateTo(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml","Terrains");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/AnimalsInterface/acceuilagricoleanimaux.fxml","Animaux");
    }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel()  { System.out.println("🚜 Matériel..."); }
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); } }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
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

}