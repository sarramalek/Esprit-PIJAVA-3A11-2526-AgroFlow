package controllers.Materiels;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import models.Materiels.Machine;
import models.Materiels.Maintenance;
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
import models.User.Personne;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceApiService;
import services.Materiels.MaintenanceService;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import utils.SessionManager;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import  utils.SessionManager;

public class AgricoleMaintenanceController implements Initializable {
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
    @FXML private TableView<Maintenance>           tableMaintenances;
    @FXML private TableColumn<Maintenance, String> colMachine;
    @FXML private TableColumn<Maintenance, String> colTypePanne;
    @FXML private TableColumn<Maintenance, String> colDate;
    @FXML private TableColumn<Maintenance, Double> colCout;
    @FXML private TableColumn<Maintenance, String> colDescription;

    // ══════════════════════════════════════════════════════
    //  FXML — Filtres & Recherche
    // ══════════════════════════════════════════════════════
    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private ComboBox<String> comboTypePanne;
    @FXML private Label            lblSelectionInfo;

    // ══════════════════════════════════════════════════════
    //  FXML — Cards BAS (statistiques)
    // ══════════════════════════════════════════════════════
    @FXML private Label lblTotalBas;
    @FXML private Label lblPctTotal;
    @FXML private Label lblCoutTotalBas;
    @FXML private Label lblPctCoutEleve;
    @FXML private Label lblCoutMoyenBas;
    @FXML private Label lblPctSousLaMoyenne;
    @FXML private Label lblMachineCouteuseBas;
    @FXML private Label lblPctMachine;
    @FXML private Label lblTypeDominant;
    @FXML private Label lblNbTypeDominant;

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar
    // ══════════════════════════════════════════════════════
    @FXML private Button logoutBtn;

    // ══════════════════════════════════════════════════════
    //  Services & Données
    // ══════════════════════════════════════════════════════
    private MaintenanceService    maintenanceService;
    private MachineService        machineService;
    // FIX 1 : apiService était utilisé mais jamais déclaré ni instancié
    private MaintenanceApiService apiService;

    private List<Machine>                     machines    = new ArrayList<>();
    private final ObservableList<Maintenance> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Maintenance> displayList = FXCollections.observableArrayList();

    private enum SortMode { DATE_DESC, DATE_ASC, COUT_ASC, COUT_DESC, MACHINE_AZ, NONE }
    private SortMode currentSort = SortMode.DATE_DESC;

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

        maintenanceService = new MaintenanceService();
        machineService     = new MachineService();
        // FIX 1 (suite) : instanciation de apiService
        apiService         = new MaintenanceApiService();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();

        tableMaintenances.getSelectionModel().selectedItemProperty()
                .addListener((obs, ov, nv) -> {
                    if (lblSelectionInfo != null) {
                        lblSelectionInfo.setText(nv != null
                                ? "Sélectionné : " + nomMachineParId(nv.getIdM())
                                + " — " + nv.getTypePanne()
                                : "Cliquez sur une ligne pour voir les détails");
                    }
                });
    }

    // ══════════════════════════════════════════════════════
    //  CONFIGURATION TABLEAU (lecture seule)
    // ══════════════════════════════════════════════════════
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

        // Couleur selon niveau de coût
        colCout.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.2f DT", val));
                if      (val > 1000) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                else if (val >  500) setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                else                 setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        colDescription.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDescription()));

        // Lignes alternées + rouge si coût élevé
        tableMaintenances.setRowFactory(tv -> new TableRow<Maintenance>() {
            @Override
            protected void updateItem(Maintenance item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                if      (item.getCout() > 1000) setStyle("-fx-background-color: #fff5f5;");
                else if (getIndex() % 2 == 1)   setStyle("-fx-background-color: #f0f9f0;");
                else                            setStyle("");
            }
        });

        tableMaintenances.setEditable(false);
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT DONNÉES
    // ══════════════════════════════════════════════════════
    private void chargerDonnees() {
        try { machines = machineService.recuperer(); }
        catch (SQLException e) { showErr("Erreur chargement machines", e.getMessage()); }
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
        if (comboMachine != null) {
            comboMachine.getItems().clear();
            comboMachine.getItems().add("Toutes les machines");
            machines.forEach(m -> comboMachine.getItems().add(m.getNom()));
            comboMachine.getSelectionModel().selectFirst();
        }
        if (comboTypePanne != null) {
            Set<String> types = new LinkedHashSet<>();
            types.add("Tous les types");
            masterList.stream()
                    .map(Maintenance::getTypePanne)
                    .filter(Objects::nonNull)
                    .forEach(types::add);
            comboTypePanne.setItems(FXCollections.observableArrayList(types));
            comboTypePanne.getSelectionModel().selectFirst();
        }
    }

    // ══════════════════════════════════════════════════════
    //  RECHERCHE DYNAMIQUE
    // ══════════════════════════════════════════════════════
    private void configurerRecherche() {
        if (champRecherche != null)
            champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    @FXML private void rechercher()       { appliquerFiltres(); }
    @FXML private void filtrer()          { appliquerFiltres(); }
    @FXML private void effacerRecherche() {
        if (champRecherche != null) champRecherche.clear();
        appliquerFiltres();
    }

    // ══════════════════════════════════════════════════════
    //  TRIS
    // ══════════════════════════════════════════════════════
    @FXML private void trierPlusRecent() { currentSort = SortMode.DATE_DESC;  appliquerFiltres(); }
    @FXML private void trierPlusAncien() { currentSort = SortMode.DATE_ASC;   appliquerFiltres(); }
    @FXML private void trierCoutEleve()  { currentSort = SortMode.COUT_DESC;  appliquerFiltres(); }
    @FXML private void trierCoutFaible() { currentSort = SortMode.COUT_ASC;   appliquerFiltres(); }
    @FXML private void trierMachineAZ()  { currentSort = SortMode.MACHINE_AZ; appliquerFiltres(); }

    @FXML
    private void reinitialiserFiltres() {
        if (champRecherche != null) champRecherche.clear();
        if (comboMachine   != null) comboMachine.getSelectionModel().selectFirst();
        if (comboTypePanne != null) comboTypePanne.getSelectionModel().selectFirst();
        currentSort = SortMode.DATE_DESC;
        appliquerFiltres();
    }

    // ══════════════════════════════════════════════════════
    //  FILTRES + TRI (logique centrale)
    // ══════════════════════════════════════════════════════
    private void appliquerFiltres() {
        String recherche = champRecherche != null && champRecherche.getText() != null
                ? champRecherche.getText().toLowerCase().trim() : "";
        String machineSel = comboMachine != null && comboMachine.getValue() != null
                ? comboMachine.getValue() : "Toutes les machines";
        String typeSel = comboTypePanne != null && comboTypePanne.getValue() != null
                ? comboTypePanne.getValue() : "Tous les types";

        List<Maintenance> filtered = masterList.stream()
                .filter(m -> {
                    String nomM = nomMachineParId(m.getIdM());
                    boolean matchR = recherche.isEmpty()
                            || nomM.toLowerCase().contains(recherche)
                            || (m.getTypePanne()   != null && m.getTypePanne().toLowerCase().contains(recherche))
                            || (m.getDescription() != null && m.getDescription().toLowerCase().contains(recherche))
                            || String.valueOf(m.getCout()).contains(recherche)
                            || (m.getDateMain()    != null && m.getDateMain().toString().contains(recherche));
                    boolean matchM = machineSel.equals("Toutes les machines") || nomM.equals(machineSel);
                    boolean matchT = typeSel.equals("Tous les types")
                            || (m.getTypePanne() != null && m.getTypePanne().equals(typeSel));
                    return matchR && matchM && matchT;
                })
                .collect(Collectors.toList());

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

    // ══════════════════════════════════════════════════════
    //  STATISTIQUES (cards BAS)
    // ══════════════════════════════════════════════════════
    private void mettreAJourStats() {
        int    nb    = displayList.size();
        double total = displayList.stream().mapToDouble(Maintenance::getCout).sum();
        double moy   = nb > 0 ? total / nb : 0;

        long   nbEleves = displayList.stream().filter(m -> m.getCout() > moy).count();
        double pctEleve = nb > 0 ? (nbEleves * 100.0 / nb) : 0;
        long   nbSous   = nb - nbEleves;
        double pctSous  = nb > 0 ? (nbSous * 100.0 / nb) : 0;

        // Machine la plus coûteuse
        Map<Integer, Double> coutParMachine = new HashMap<>();
        for (Maintenance m : displayList)
            coutParMachine.merge(m.getIdM(), m.getCout(), Double::sum);
        int    idMachMax   = coutParMachine.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(-1);
        String nomMachMax  = idMachMax >= 0 ? nomMachineParId(idMachMax) : "—";
        double coutMachMax = idMachMax >= 0 ? coutParMachine.get(idMachMax) : 0;
        double pctMach     = total > 0 ? (coutMachMax * 100.0 / total) : 0;

        // Type dominant
        Map<String, Long> compteParType = new HashMap<>();
        for (Maintenance m : displayList) {
            String t = m.getTypePanne() != null ? m.getTypePanne() : "Inconnu";
            compteParType.merge(t, 1L, Long::sum);
        }
        String typeDominant = compteParType.entrySet().stream()
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("—");
        long   nbTypeDom    = compteParType.getOrDefault(typeDominant, 0L);

        set(lblTotalBas,           String.valueOf(nb));
        set(lblPctTotal,           "100%");
        set(lblCoutTotalBas,       String.format("%.2f DT", total));
        set(lblPctCoutEleve,       String.format("%.0f%% élevés", pctEleve));
        set(lblCoutMoyenBas,       String.format("%.2f DT", moy));
        set(lblPctSousLaMoyenne,   String.format("%.0f%% sous moy.", pctSous));
        set(lblMachineCouteuseBas, nomMachMax);
        set(lblPctMachine,         String.format("%.0f%% du total", pctMach));
        set(lblTypeDominant,       typeDominant);
        set(lblNbTypeDominant,     nbTypeDom + " occurrence(s)");
    }

    private void set(Label lbl, String val) {
        if (lbl != null) lbl.setText(val);
    }

    // ══════════════════════════════════════════════════════
    //  STATISTIQUES GRAPHIQUES (popup Canvas)
    // ══════════════════════════════════════════════════════
    @FXML
    private void afficherStatistiques() {
        if (displayList.isEmpty()) { showInfo("Stats", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Statistiques de Mes Maintenances");
        stage.setResizable(true);

        Map<String, Double> coutMap = new LinkedHashMap<>();
        Map<String, Long>   typeMap = new LinkedHashMap<>();
        for (Maintenance m : displayList) {
            String nom = nomMachineParId(m.getIdM());
            coutMap.merge(nom, m.getCout(), Double::sum);
            String t = m.getTypePanne() != null ? m.getTypePanne() : "Inconnu";
            typeMap.merge(t, 1L, Long::sum);
        }

        List<String> machKeys = new ArrayList<>(coutMap.keySet());
        List<Double> machVals = new ArrayList<>(coutMap.values());
        List<String> typeKeys = new ArrayList<>(typeMap.keySet());
        List<Long>   typeVals = new ArrayList<>(typeMap.values());

        Color[] colors = {
                Color.web("#27ae60"), Color.web("#3498db"), Color.web("#e74c3c"),
                Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63")
        };

        int barW = 55, gap = 25, padL = 75, padTop = 50, chartH = 250;
        int nM   = machKeys.size();
        int cwL  = padL + nM * (barW + gap) + gap + 20;
        int chL  = chartH + 90 + padTop;

        Canvas canvasL = new Canvas(cwL, chL);
        GraphicsContext gl = canvasL.getGraphicsContext2D();
        gl.setFill(Color.WHITE); gl.fillRect(0, 0, cwL, chL);
        gl.setFill(Color.web("#2E7D32"));
        gl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gl.fillText("Cout total par machine (DT)", padL, 30);

        double maxCout = machVals.stream().mapToDouble(v -> v).max().orElse(1);
        if (maxCout == 0) maxCout = 1;
        double coutMoyenGlobal = displayList.stream().mapToDouble(Maintenance::getCout).average().orElse(0);

        gl.setFont(Font.font("Arial", 10));
        for (int i = 0; i <= 5; i++) {
            double y = padTop + chartH - (chartH * i / 5.0);
            gl.setStroke(Color.LIGHTGRAY); gl.setLineWidth(1);
            gl.strokeLine(padL, y, cwL - 20, y);
            gl.setFill(Color.GRAY);
            gl.fillText(String.format("%.0f", maxCout * i / 5.0), 5, y + 4);
        }
        for (int i = 0; i < nM; i++) {
            double barH = (machVals.get(i) / maxCout) * chartH;
            double x    = padL + gap + i * (barW + gap);
            double y    = padTop + chartH - barH;
            Color  c    = colors[i % colors.length];
            gl.setFill(Color.rgb(0, 0, 0, 0.07));
            gl.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);
            gl.setFill(c);
            gl.fillRoundRect(x, y, barW, barH, 6, 6);
            gl.setFill(Color.web("#2E7D32"));
            gl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.format("%.0f", machVals.get(i));
            gl.fillText(vs, x + barW / 2.0 - vs.length() * 3.5, y - 6);
            gl.setFont(Font.font("Arial", 10));
            String lbl = machKeys.get(i).length() > 11 ? machKeys.get(i).substring(0, 11) + "..." : machKeys.get(i);
            gl.fillText(lbl, x + barW / 2.0 - lbl.length() * 3, padTop + chartH + 16);
        }
        // Ligne moyenne clippée pour ne pas sortir du canvas
        if (coutMoyenGlobal > 0 && coutMoyenGlobal <= maxCout) {
            double yMoy = padTop + chartH - (coutMoyenGlobal / maxCout) * chartH;
            gl.setStroke(Color.web("#e74c3c")); gl.setLineWidth(2);
            gl.setLineDashes(8, 4);
            gl.strokeLine(padL, yMoy, cwL - 20, yMoy);
            gl.setLineDashes();
            gl.setFill(Color.web("#e74c3c"));
            gl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gl.fillText(String.format("Moy. %.0f DT", coutMoyenGlobal), cwL - 80, yMoy - 4);
        }

        // Graphique types
        int nT  = typeKeys.size();
        int cwR = 400;
        int chR = padTop + 40 + nT * 42 + 20;

        Canvas canvasR = new Canvas(cwR, Math.max(chR, chL));
        GraphicsContext gr = canvasR.getGraphicsContext2D();
        gr.setFill(Color.WHITE); gr.fillRect(0, 0, cwR, Math.max(chR, chL));
        gr.setFill(Color.web("#2E7D32"));
        gr.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gr.fillText("Repartition par type de panne", 10, 30);

        long maxType  = typeVals.stream().mapToLong(v -> v).max().orElse(1);
        int  barAreaW = cwR - 160;
        for (int i = 0; i < nT; i++) {
            double bH = 24;
            double y  = padTop + i * 42;
            double bW = (typeVals.get(i) / (double) maxType) * barAreaW;
            Color  c  = colors[i % colors.length];
            gr.setFill(Color.rgb(0, 0, 0, 0.06));
            gr.fillRoundRect(153, y + 3, bW, bH, 6, 6);
            gr.setFill(c);
            gr.fillRoundRect(150, y, bW, bH, 6, 6);
            gr.setFill(Color.web("#2E7D32"));
            gr.setFont(Font.font("Arial", 11));
            String lbl = typeKeys.get(i).length() > 14 ? typeKeys.get(i).substring(0, 14) + "..." : typeKeys.get(i);
            gr.fillText(lbl, 5, y + 17);
            gr.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gr.fillText(String.valueOf(typeVals.get(i)), 150 + bW + 6, y + 17);
        }

        ScrollPane spL = new ScrollPane(canvasL); spL.setFitToHeight(true);
        spL.setPrefSize(Math.min(cwL + 20, 700), chL + 20);
        ScrollPane spR = new ScrollPane(canvasR); spR.setFitToHeight(true);
        spR.setPrefWidth(cwR + 20);

        HBox charts = new HBox(16, spL, spR);
        charts.setPadding(new Insets(10));

        Label legende = new Label("  -- Ligne rouge pointillee = cout moyen global par maintenance");
        legende.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox root = new VBox(8, legende, charts);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ══════════════════════════════════════════════════════
    //  ANALYSE COÛT PAR MACHINE — popup
    //  FIX 2 : méthode renommée ouvrirAnalyseCout pour
    //           correspondre au onAction="#ouvrirAnalyseCout"
    //           déclaré dans le FXML (était ouvrirCoutMachine)
    // ══════════════════════════════════════════════════════
    @FXML
    private void ouvrirAnalyseCout() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🏭 Coût Total de Maintenance par Machine");
        stage.setResizable(true);

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
                MaintenanceApiService.ResultatCout res;
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

    // ══════════════════════════════════════════════════════
    //  EXPORT PDF — Analyse coût par machine
    // ══════════════════════════════════════════════════════
    private void exporterAnalysePDF(
            List<Map<String, Object>> rows,
            double grandTotal, double grandMoyen,
            int nbTotal, String nomMachMax, double coutMachMax,
            Stage parentStage) {

        if (rows.isEmpty()) { showInfo("Export PDF", "Aucune donnee a exporter."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("analyse_cout_maintenance_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        File file = fc.showSaveDialog(parentStage);
        if (file == null) return;

        try (PdfWriter   writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf    = new PdfDocument(writer);
             Document    doc    = new Document(pdf)) {

            DeviceRgb vertFonce  = new DeviceRgb(46,  125, 50);
            DeviceRgb vertClair  = new DeviceRgb(200, 230, 201);
            DeviceRgb grisLeger  = new DeviceRgb(245, 245, 245);
            DeviceRgb rouge      = new DeviceRgb(231, 76,  60);
            DeviceRgb orange     = new DeviceRgb(230, 126, 34);
            DeviceRgb vertOk     = new DeviceRgb(39,  174, 96);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            doc.add(new Paragraph("AgroFlow — Rapport Analyse Maintenance")
                    .setFontSize(22).setBold()
                    .setFontColor(vertFonce)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Analyse du cout de maintenance par machine")
                    .setFontSize(13)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Genere le : " + LocalDate.now().format(fmt))
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(120, 120, 120))
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("Resume global")
                    .setFontSize(14).setBold().setFontColor(vertFonce));

            float[] kpiW = {200f, 200f, 200f};
            Table kpiTable = new Table(UnitValue.createPointArray(kpiW));
            kpiTable.setWidth(UnitValue.createPercentValue(100));
            kpiTable.setMarginBottom(15);

            ajouterCellulesKpi(kpiTable, vertClair, vertFonce,
                    new String[]{
                            "Total maintenances : " + nbTotal,
                            "Cout total : " + String.format("%.2f DT", grandTotal),
                            "Cout moyen : " + String.format("%.2f DT", grandMoyen)
                    });
            ajouterCellulesKpi(kpiTable, grisLeger, vertFonce,
                    new String[]{
                            "Machine la plus couteuse :",
                            nomMachMax,
                            String.format("%.2f DT", coutMachMax)
                    });
            doc.add(kpiTable);

            doc.add(new Paragraph("Detail par machine (tri decroissant par cout)")
                    .setFontSize(14).setBold().setFontColor(vertFonce));
            doc.add(new Paragraph("\n").setFontSize(4));

            float[] colW = {130f, 60f, 85f, 85f, 75f, 75f, 65f};
            Table table = new Table(UnitValue.createPointArray(colW));
            table.setWidth(UnitValue.createPercentValue(100));

            // FIX 5 : utilisation cohérente de ColorConstants (import en tête de fichier)
            String[] headers = {"Machine", "Nb", "Total (DT)", "Moyen (DT)", "Max (DT)", "Min (DT)", "% Total"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(9).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(vertFonce)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5));
            }

            boolean pair = true;
            for (Map<String, Object> row : rows) {
                DeviceRgb bgColor = pair ? new DeviceRgb(255, 255, 255) : new DeviceRgb(242, 247, 242);
                pair = !pair;

                double tot = (double) row.get("total");
                DeviceRgb coutColor = tot >= 1000 ? rouge : (tot >= 500 ? orange : vertOk);

                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(row.get("nom"))).setFontSize(9))
                        .setBackgroundColor(bgColor).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(row.get("nb"))).setFontSize(9))
                        .setBackgroundColor(bgColor).setTextAlignment(TextAlignment.CENTER).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", tot)).setFontSize(9).setBold().setFontColor(coutColor))
                        .setBackgroundColor(bgColor).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", (double) row.get("moyenne"))).setFontSize(9))
                        .setBackgroundColor(bgColor).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", (double) row.get("max"))).setFontSize(9))
                        .setBackgroundColor(bgColor).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
                boolean hasData = Boolean.TRUE.equals(row.get("hasData"));
                String minTxt = hasData ? String.format("%.2f", (double) row.get("min")) : "—";
                table.addCell(new Cell()
                        .add(new Paragraph(minTxt).setFontSize(9))
                        .setBackgroundColor(bgColor).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.format("%.1f%%", (double) row.get("pct"))).setFontSize(9))
                        .setBackgroundColor(bgColor).setTextAlignment(TextAlignment.CENTER).setPadding(4));
            }

            table.addCell(new Cell()
                    .add(new Paragraph("TOTAL").setBold().setFontSize(10).setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setPadding(5));
            long totalNb = rows.stream().mapToLong(r -> (long) r.get("nb")).sum();
            table.addCell(new Cell()
                    .add(new Paragraph(String.valueOf(totalNb)).setBold().setFontSize(10))
                    .setBackgroundColor(vertClair).setTextAlignment(TextAlignment.CENTER).setPadding(5));
            table.addCell(new Cell()
                    .add(new Paragraph(String.format("%.2f", grandTotal)).setBold().setFontSize(10).setFontColor(rouge))
                    .setBackgroundColor(vertClair).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
            for (int i = 0; i < 4; i++)
                table.addCell(new Cell()
                        .add(new Paragraph("").setFontSize(9))
                        .setBackgroundColor(vertClair).setPadding(5));

            doc.add(table);
            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph(
                    "Rapport genere automatiquement par AgroFlow — " + LocalDate.now().format(fmt))
                    .setFontSize(9)
                    .setFontColor(new DeviceRgb(150, 150, 150))
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            showErr("Erreur PDF", "Impossible de generer le PDF : " + e.getMessage());
            return;
        }

        showInfo("Export PDF reussi", "Fichier enregistre :\n" + file.getAbsolutePath());
    }

    private void ajouterCellulesKpi(Table table, DeviceRgb bg, DeviceRgb textColor, String[] texts) {
        for (String txt : texts) {
            table.addCell(new Cell()
                    .add(new Paragraph(txt).setBold().setFontSize(10).setFontColor(textColor))
                    .setBackgroundColor(bg)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8));
        }
    }

    // ══════════════════════════════════════════════════════
    //  EXPORT PDF — Liste des maintenances affichées
    // ══════════════════════════════════════════════════════
    @FXML
    private void exporterMaintenancesPDF() {
        if (displayList.isEmpty()) {
            showInfo("Export PDF", "Aucune maintenance a exporter.\nVerifiez vos filtres.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("maintenances_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        File file = fc.showSaveDialog(tableMaintenances.getScene().getWindow());
        if (file == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int    nb       = displayList.size();
        double total    = displayList.stream().mapToDouble(Maintenance::getCout).sum();
        double moy      = nb > 0 ? total / nb : 0;
        double maxCout  = displayList.stream().mapToDouble(Maintenance::getCout).max().orElse(0);

        try (PdfWriter   writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf    = new PdfDocument(writer);
             Document    doc    = new Document(pdf)) {

            DeviceRgb vertFonce = new DeviceRgb(46,  125, 50);
            DeviceRgb vertClair = new DeviceRgb(200, 230, 201);
            DeviceRgb grisLeger = new DeviceRgb(245, 246, 250);
            DeviceRgb rouge     = new DeviceRgb(231, 76,  60);
            DeviceRgb orange    = new DeviceRgb(230, 126, 34);
            DeviceRgb vertOk    = new DeviceRgb(39,  174, 96);
            DeviceRgb grisTexte = new DeviceRgb(120, 120, 120);

            doc.add(new Paragraph("AgroFlow — Rapport des Maintenances")
                    .setFontSize(22).setBold()
                    .setFontColor(vertFonce)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Historique complet des maintenances de machines")
                    .setFontSize(12)
                    .setFontColor(grisTexte)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Genere le : " + LocalDate.now().format(fmt)
                    + "   |   Nombre d'enregistrements : " + nb)
                    .setFontSize(10)
                    .setFontColor(grisTexte)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n").setFontSize(6));

            doc.add(new Paragraph("Resume statistique")
                    .setFontSize(13).setBold().setFontColor(vertFonce));

            float[] kpiW = {150f, 150f, 150f, 150f};
            Table kpiTbl = new Table(UnitValue.createPointArray(kpiW));
            kpiTbl.setWidth(UnitValue.createPercentValue(100)).setMarginBottom(14);

            String[][] kpiData = {
                    {"Total maintenances", String.valueOf(nb)},
                    {"Cout total",         String.format("%.2f DT", total)},
                    {"Cout moyen",         String.format("%.2f DT", moy)},
                    {"Cout max",           String.format("%.2f DT", maxCout)}
            };
            for (String[] kv : kpiData) {
                kpiTbl.addCell(new Cell()
                        .add(new Paragraph(kv[0]).setFontSize(9).setFontColor(vertFonce))
                        .setBackgroundColor(vertClair)
                        .setTextAlignment(TextAlignment.CENTER).setPadding(6));
            }
            for (String[] kv : kpiData) {
                kpiTbl.addCell(new Cell()
                        .add(new Paragraph(kv[1]).setFontSize(12).setBold().setFontColor(vertFonce))
                        .setBackgroundColor(grisLeger)
                        .setTextAlignment(TextAlignment.CENTER).setPadding(8));
            }
            doc.add(kpiTbl);

            doc.add(new Paragraph("Detail des maintenances")
                    .setFontSize(13).setBold().setFontColor(vertFonce));
            doc.add(new Paragraph("\n").setFontSize(4));

            float[] colW = {115f, 110f, 70f, 75f, 210f};
            Table table = new Table(UnitValue.createPointArray(colW));
            table.setWidth(UnitValue.createPercentValue(100));

            // FIX 5 : utilisation cohérente de ColorConstants (import en tête de fichier)
            for (String h : new String[]{"Machine", "Type Panne", "Date", "Cout (DT)", "Description"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(9).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(vertFonce)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(5));
            }

            boolean pair = true;
            for (Maintenance m : displayList) {
                DeviceRgb bg = pair ? new DeviceRgb(255, 255, 255) : new DeviceRgb(242, 247, 242);
                pair = !pair;

                double cout = m.getCout();
                DeviceRgb coutColor = cout > 1000 ? rouge : (cout > 500 ? orange : vertOk);

                table.addCell(new Cell()
                        .add(new Paragraph(nomMachineParId(m.getIdM())).setFontSize(8))
                        .setBackgroundColor(bg).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(m.getTypePanne() != null ? m.getTypePanne() : "—").setFontSize(8))
                        .setBackgroundColor(bg).setPadding(4));
                String dateStr = m.getDateMain() != null ? m.getDateMain().format(fmt) : "—";
                table.addCell(new Cell()
                        .add(new Paragraph(dateStr).setFontSize(8))
                        .setBackgroundColor(bg).setTextAlignment(TextAlignment.CENTER).setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f", cout)).setFontSize(8).setBold().setFontColor(coutColor))
                        .setBackgroundColor(bg).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
                String desc = m.getDescription() != null ? m.getDescription() : "—";
                if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
                table.addCell(new Cell()
                        .add(new Paragraph(desc).setFontSize(8))
                        .setBackgroundColor(bg).setPadding(4));
            }

            table.addCell(new Cell(1, 3)
                    .add(new Paragraph("TOTAL").setBold().setFontSize(10).setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
            table.addCell(new Cell()
                    .add(new Paragraph(String.format("%.2f DT", total)).setBold().setFontSize(10).setFontColor(rouge))
                    .setBackgroundColor(vertClair).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
            table.addCell(new Cell()
                    .add(new Paragraph(nb + " maintenance(s)").setFontSize(9).setFontColor(vertFonce))
                    .setBackgroundColor(vertClair).setPadding(5));

            doc.add(table);
            doc.add(new Paragraph("\n").setFontSize(6));

            doc.add(new Paragraph(
                    "AgroFlow — Rapport genere automatiquement le " + LocalDate.now().format(fmt))
                    .setFontSize(9)
                    .setFontColor(grisTexte)
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            showErr("Erreur PDF", "Impossible de generer le PDF :\n" + e.getMessage());
            return;
        }

        showInfo("Export PDF reussi", "Rapport sauvegarde :\n" + file.getAbsolutePath());
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS POPUP — FIX 3 & 4 : méthodes manquantes
    // ══════════════════════════════════════════════════════

    /**
     * FIX 4a : creerBouton était appelé dans ouvrirAnalyseCout (ex ouvrirCoutMachine)
     * mais n'était jamais défini dans le contrôleur.
     */
    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; "
                + "-fx-padding: 8 18;");
        return btn;
    }

    /**
     * FIX 4b : creerLabelStat était appelé mais jamais défini.
     */
    private Label creerLabelStat(String valeur) {
        Label lbl = new Label(valeur);
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        lbl.setWrapText(true);
        return lbl;
    }

    /**
     * FIX 4c : creerCarte était appelé mais jamais défini.
     */
    private VBox creerCarte(String titre, Label valeurLabel, String couleur) {
        Label t = new Label(titre);
        t.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.85);");
        VBox c = new VBox(4, t, valeurLabel);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(10, 14, 10, 14));
        c.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private VBox creerCartePopup(String titre, String valeur, String couleur) {
        Label t = new Label(titre);
        t.setStyle("-fx-font-size:10px;-fx-text-fill:rgba(255,255,255,0.85);");
        Label v = new Label(valeur);
        v.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:white;");
        v.setWrapText(true);
        VBox c = new VBox(4, t, v);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(10, 14, 10, 14));
        c.setStyle("-fx-background-color:" + couleur + ";-fx-background-radius:10;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR AGRICOLE
    // ══════════════════════════════════════════════════════


    private void nav(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) throw new IOException("FXML introuvable : " + path);
            Parent root  = FXMLLoader.load(url);
            Stage  stage = (Stage) tableMaintenances.getScene().getWindow();
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
        a.setTitle("Deconnexion");
        a.setHeaderText("Deconnexion");
        a.setContentText("Voulez-vous vraiment vous deconnecter ?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Parent root  = FXMLLoader.load(getClass().getResource("/UsersInterface/Login.fxml"));
                    Stage  stage = (Stage) logoutBtn.getScene().getWindow();
                    // On récupère le Stage et la Scene ACTUELLE
                    Scene scene = stage.getScene();

                    // SOLUTION MIRACLE : On change la racine, pas la scène !
                    scene.setRoot(root);

                    // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                    stage.show();
                } catch (IOException e) {
                    showErr("Erreur", "Erreur deconnexion : " + e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════
    private String nomMachineParId(int idM) {
        for (Machine m : machines)
            if (m.getIdM() == idM) return m.getNom();
        return "Machine #" + idM;
    }

    private void showInfo(String t, String m) { alert(Alert.AlertType.INFORMATION, t, m); }
    private void showErr (String t, String m) { alert(Alert.AlertType.ERROR,       t, m); }
    // FIX 3 : showWarn était appelé dans ouvrirAnalyseCout mais jamais défini
    private void showWarn(String t, String m) { alert(Alert.AlertType.WARNING,     t, m); }

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
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); }}

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
    public void ouvrirMaintenance(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMaintenance.fxml","Maintenance");
    }

    public void ouvrirAchat(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageAchat.fxml","Maintenance");

    }

    public void ouvrirMachine(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Maintenance");

    }
    public void handleMesEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsUser.fxml","Evenements");
    }

    public void ouvrirParticipations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherParticipationsUser.fxml","Participations");
    }
}