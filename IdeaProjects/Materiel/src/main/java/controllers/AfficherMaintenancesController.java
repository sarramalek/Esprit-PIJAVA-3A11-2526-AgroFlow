package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entities.Maintenance;
import entities.Machine;
import services.MaintenanceService;
import services.MachineService;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AfficherMaintenancesController implements Initializable {

    // TABLE
    @FXML private TableView<Maintenance>              tableMaintenances;
    @FXML private TableColumn<Maintenance, String>    colMachine;
    @FXML private TableColumn<Maintenance, String>    colTypePanne;
    @FXML private TableColumn<Maintenance, LocalDate> colDate;
    @FXML private TableColumn<Maintenance, Double>    colCout;
    @FXML private TableColumn<Maintenance, String>    colDescription;

    // FILTRES
    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private ComboBox<String> comboTypePanne;

    // BOUTONS TRI
    @FXML private Button btnPlusRecent;
    @FXML private Button btnPlusAncien;
    @FXML private Button btnCoutEleve;
    @FXML private Button btnCoutFaible;
    @FXML private Button btnMachineAZ;

    // LABELS STATS
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;
    @FXML private Label lblMachineCouteuse;
    @FXML private Label lblSelectionInfo;

    // LABELS POURCENTAGES
    @FXML private Label lblPctTotal;
    @FXML private Label lblPctCoutEleve;
    @FXML private Label lblPctSousLaMoyenne;
    @FXML private Label lblPctMachine;

    // ETAT TRI ACTIF
    private String triActif = "date_recent";

    // SERVICES & DONNEES
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final MachineService     machineService     = new MachineService();
    private final ObservableList<Maintenance> listeMaintenances = FXCollections.observableArrayList();
    private final Map<Integer, Machine>       mapMachines       = new HashMap<>();

    // Couleurs PieChart
    private static final String[] PIE_COLORS = {
            "#3498db", "#e74c3c", "#f39c12", "#27ae60",
            "#8e44ad", "#16a085", "#e67e22", "#2c3e50"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerMachines();
        configurationTableau();
        chargerMaintenances();
        chargerTypesPannes();
        mettreAJourStatistiques();
        mettreAJourStyleBoutonsTri();
    }

    // ============================================================
    //  CHARGEMENT MACHINES
    // ============================================================
    private void chargerMachines() {
        try {
            List<Machine> machines = machineService.recuperer();
            mapMachines.clear();
            for (Machine m : machines) mapMachines.put(m.getIdM(), m);
            ObservableList<String> noms = FXCollections.observableArrayList("Toutes les machines");
            noms.addAll(machines.stream().map(Machine::getNom).sorted().collect(Collectors.toList()));
            comboMachine.setItems(noms);
            comboMachine.setValue("Toutes les machines");
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Chargement machines : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ============================================================
    //  CHARGEMENT TYPES DE PANNES DISTINCTS
    // ============================================================
    private void chargerTypesPannes() {
        ObservableList<String> types = FXCollections.observableArrayList("Tous les types");
        listeMaintenances.stream()
                .map(Maintenance::getTypePanne)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .sorted()
                .forEach(types::add);
        comboTypePanne.setItems(types);
        comboTypePanne.setValue("Tous les types");
    }

    // ============================================================
    //  CONFIGURATION COLONNES
    // ============================================================
    private void configurationTableau() {
        colMachine.setCellValueFactory(cellData -> {
            int idM = cellData.getValue().getIdM();
            Machine machine = mapMachines.get(idM);
            String nom = (machine != null) ? machine.getNom() : "Inconnu (ID=" + idM + ")";
            return new javafx.beans.property.SimpleStringProperty(nom);
        });
        colMachine.setCellFactory(col -> new TableCell<Maintenance, String>() {
            @Override protected void updateItem(String nom, boolean empty) {
                super.updateItem(nom, empty);
                if (empty || nom == null) { setText(null); setStyle(""); }
                else if (nom.startsWith("Inconnu")) { setText(nom); setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;"); }
                else { setText(nom); setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;"); }
            }
        });

        colTypePanne.setCellValueFactory(new PropertyValueFactory<>("typePanne"));
        colTypePanne.setCellFactory(col -> new TableCell<Maintenance, String>() {
            @Override protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) { setText(null); setStyle(""); }
                else {
                    setText(type);
                    String t = type.toLowerCase();
                    String bg = t.contains("elect") ? "#f0e6ff"
                            : t.contains("mot")   ? "#fff3e0"
                            : t.contains("hyd")   ? "#e3f2fd" : "#f5f5f5";
                    setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 4;");
                }
            }
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateMain"));

        colCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colCout.setCellFactory(col -> new TableCell<Maintenance, Double>() {
            @Override protected void updateItem(Double cout, boolean empty) {
                super.updateItem(cout, empty);
                if (empty || cout == null) { setText(null); setStyle(""); }
                else {
                    setText(String.format("%.2f DT", cout));
                    setStyle(cout > 300 ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60;");
                }
            }
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setCellFactory(col -> new TableCell<Maintenance, String>() {
            @Override protected void updateItem(String desc, boolean empty) {
                super.updateItem(desc, empty);
                if (empty || desc == null) { setText(null); setTooltip(null); }
                else { setText(desc); setTooltip(new Tooltip(desc)); }
            }
        });

        tableMaintenances.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                Machine m = mapMachines.get(newVal.getIdM());
                String nomM = (m != null) ? m.getNom() : "?";
                lblSelectionInfo.setText("Selectionne : " + nomM + " - " + newVal.getTypePanne());
                lblSelectionInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
            } else {
                lblSelectionInfo.setText("Selectionnez une ligne pour Modifier / Supprimer");
                lblSelectionInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
            }
        });
    }

    // ============================================================
    //  CHARGEMENT MAINTENANCES
    // ============================================================
    private void chargerMaintenances() {
        try {
            listeMaintenances.setAll(maintenanceService.recuperer());
            tableMaintenances.setItems(listeMaintenances);
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Chargement maintenances : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ============================================================
    //  ACTIONS RECHERCHE
    // ============================================================
    @FXML private void rechercher() { appliquerFiltres(); }
    @FXML private void filtrer()    { appliquerFiltres(); }

    @FXML
    private void effacerRecherche() {
        champRecherche.clear();
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboMachine.setValue("Toutes les machines");
        if (comboTypePanne != null) comboTypePanne.setValue("Tous les types");
        triActif = "date_recent";
        mettreAJourStyleBoutonsTri();
        appliquerFiltres();
    }

    // ============================================================
    //  ACTIONS TRI
    // ============================================================
    @FXML private void trierPlusRecent()  { triActif = "date_recent";  mettreAJourStyleBoutonsTri(); appliquerFiltres(); }
    @FXML private void trierPlusAncien()  { triActif = "date_ancien";  mettreAJourStyleBoutonsTri(); appliquerFiltres(); }
    @FXML private void trierCoutEleve()   { triActif = "cout_eleve";   mettreAJourStyleBoutonsTri(); appliquerFiltres(); }
    @FXML private void trierCoutFaible()  { triActif = "cout_faible";  mettreAJourStyleBoutonsTri(); appliquerFiltres(); }
    @FXML private void trierMachineAZ()   { triActif = "machine_az";   mettreAJourStyleBoutonsTri(); appliquerFiltres(); }

    private void mettreAJourStyleBoutonsTri() {
        String gris   = "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:6 12;-fx-background-radius:6;-fx-cursor:hand;-fx-text-fill:white;-fx-background-color:#7f8c8d;";
        String violet = "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:6 12;-fx-background-radius:6;-fx-cursor:hand;-fx-text-fill:white;-fx-background-color:#8e44ad;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),4,0,0,2);";
        String orange = "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:6 12;-fx-background-radius:6;-fx-cursor:hand;-fx-text-fill:white;-fx-background-color:#e67e22;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),4,0,0,2);";
        String teal   = "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:6 12;-fx-background-radius:6;-fx-cursor:hand;-fx-text-fill:white;-fx-background-color:#16a085;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),4,0,0,2);";
        if (btnPlusRecent != null) btnPlusRecent.setStyle("date_recent".equals(triActif) ? violet : gris);
        if (btnPlusAncien != null) btnPlusAncien.setStyle("date_ancien".equals(triActif) ? violet : gris);
        if (btnCoutEleve  != null) btnCoutEleve.setStyle( "cout_eleve".equals(triActif)  ? orange : gris);
        if (btnCoutFaible != null) btnCoutFaible.setStyle("cout_faible".equals(triActif) ? orange : gris);
        if (btnMachineAZ  != null) btnMachineAZ.setStyle( "machine_az".equals(triActif)  ? teal   : gris);
    }

    // ============================================================
    //  APPLICATION FILTRES + TRI
    // ============================================================
    private void appliquerFiltres() {
        String recherche           = champRecherche.getText().toLowerCase().trim();
        String machineSelectionnee = comboMachine.getValue();
        String typeSelectionne     = (comboTypePanne != null) ? comboTypePanne.getValue() : null;

        Integer idMFiltree = null;
        if (machineSelectionnee != null && !machineSelectionnee.equals("Toutes les machines")) {
            for (Map.Entry<Integer, Machine> e : mapMachines.entrySet()) {
                if (e.getValue().getNom().equals(machineSelectionnee)) { idMFiltree = e.getKey(); break; }
            }
        }
        final Integer idMFinal = idMFiltree;

        List<Maintenance> filtrees = listeMaintenances.stream().filter(m -> {
            if (idMFinal != null && m.getIdM() != idMFinal) return false;
            if (typeSelectionne != null && !typeSelectionne.equals("Tous les types")) {
                if (m.getTypePanne() == null || !m.getTypePanne().equalsIgnoreCase(typeSelectionne)) return false;
            }
            if (!recherche.isEmpty()) {
                Machine mac = mapMachines.get(m.getIdM());
                boolean matchMachine = mac != null && mac.getNom().toLowerCase().contains(recherche);
                boolean matchType    = m.getTypePanne()   != null && m.getTypePanne().toLowerCase().contains(recherche);
                boolean matchDesc    = m.getDescription() != null && m.getDescription().toLowerCase().contains(recherche);
                if (!matchMachine && !matchType && !matchDesc) return false;
            }
            return true;
        }).collect(Collectors.toList());

        switch (triActif) {
            case "date_recent"  -> filtrees.sort(Comparator.comparing(Maintenance::getDateMain, Comparator.nullsLast(Comparator.reverseOrder())));
            case "date_ancien"  -> filtrees.sort(Comparator.comparing(Maintenance::getDateMain, Comparator.nullsLast(Comparator.naturalOrder())));
            case "cout_eleve"   -> filtrees.sort(Comparator.comparingDouble(Maintenance::getCout).reversed());
            case "cout_faible"  -> filtrees.sort(Comparator.comparingDouble(Maintenance::getCout));
            case "machine_az"   -> filtrees.sort(Comparator.comparing(m -> {
                Machine mac = mapMachines.get(m.getIdM());
                return mac != null ? mac.getNom().toLowerCase() : "";
            }));
        }

        tableMaintenances.setItems(FXCollections.observableArrayList(filtrees));
        mettreAJourStatistiques();
    }

    // ============================================================
    //  STATISTIQUES LABELS (barre du bas)
    // ============================================================
    private void mettreAJourStatistiques() {
        ObservableList<Maintenance> items = tableMaintenances.getItems();
        int    total     = items.size();
        double coutTotal = items.stream().mapToDouble(Maintenance::getCout).sum();
        double coutMoyen = (total > 0) ? coutTotal / total : 0.0;

        lblTotal.setText(String.valueOf(total));
        lblCoutTotal.setText(String.format("%.2f DT", coutTotal));
        if (lblCoutMoyen != null) lblCoutMoyen.setText(String.format("%.2f DT", coutMoyen));

        int totalGlobal = listeMaintenances.size();
        if (lblPctTotal != null) {
            int pct = totalGlobal > 0 ? (int) Math.round((double) total / totalGlobal * 100) : 100;
            lblPctTotal.setText(pct + "% de " + totalGlobal + " au total");
        }
        if (lblPctCoutEleve != null) {
            long nbEleve = items.stream().filter(m -> m.getCout() > 300).count();
            int pctEleve = total > 0 ? (int) Math.round((double) nbEleve / total * 100) : 0;
            lblPctCoutEleve.setText(pctEleve + "% coûts élevés (>300 DT)");
        }
        if (lblPctSousLaMoyenne != null) {
            final double moy = coutMoyen;
            long nbSous = items.stream().filter(m -> m.getCout() < moy).count();
            int pctSous = total > 0 ? (int) Math.round((double) nbSous / total * 100) : 0;
            lblPctSousLaMoyenne.setText(pctSous + "% sous la moyenne");
        }
        if (lblMachineCouteuse != null) {
            Map<Integer, Double> coutParMachine = new HashMap<>();
            for (Maintenance m : items) coutParMachine.merge(m.getIdM(), m.getCout(), Double::sum);
            if (!coutParMachine.isEmpty()) {
                int idMax = coutParMachine.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
                Machine mac = mapMachines.get(idMax);
                String nomMax  = (mac != null) ? mac.getNom() : "ID=" + idMax;
                double coutMax = coutParMachine.get(idMax);
                lblMachineCouteuse.setText(nomMax + "  -  " + String.format("%.2f DT", coutMax));
                if (lblPctMachine != null) {
                    int pctMac = coutTotal > 0 ? (int) Math.round(coutMax / coutTotal * 100) : 0;
                    lblPctMachine.setText(pctMac + "% du coût total");
                }
            } else {
                lblMachineCouteuse.setText("-");
                if (lblPctMachine != null) lblPctMachine.setText("0% du coût total");
            }
        }
    }

    // ============================================================
    //  ★ POPUP STATISTIQUES DÉTAILLÉES ★
    // ============================================================
    @FXML
    private void afficherStatistiques() {
        ObservableList<Maintenance> items = tableMaintenances.getItems();
        int    total     = items.size();
        double coutTotal = items.stream().mapToDouble(Maintenance::getCout).sum();
        double coutMoyen = total > 0 ? coutTotal / total : 0.0;

        // ── Calculs par type de panne ────────────────────────────
        Map<String, Long>   nbParType   = new LinkedHashMap<>();
        Map<String, Double> coutParType = new LinkedHashMap<>();
        for (Maintenance m : items) {
            String type = (m.getTypePanne() != null && !m.getTypePanne().isBlank())
                    ? m.getTypePanne() : "Autre";
            nbParType.merge(type, 1L, Long::sum);
            coutParType.merge(type, m.getCout(), Double::sum);
        }

        // ── Calculs par machine ──────────────────────────────────
        Map<String, Double> coutParMachine = new LinkedHashMap<>();
        for (Maintenance m : items) {
            Machine mac = mapMachines.get(m.getIdM());
            String nom = (mac != null) ? mac.getNom() : "Inconnu";
            coutParMachine.merge(nom, m.getCout(), Double::sum);
        }

        // ── Seuils ──────────────────────────────────────────────
        long nbEleve     = items.stream().filter(m -> m.getCout() > 300).count();
        long nbSousMoy   = items.stream().filter(m -> m.getCout() < coutMoyen).count();
        long nbNormal    = total - nbEleve - nbSousMoy;
        double pctEleve  = pct(nbEleve, total);
        double pctSous   = pct(nbSousMoy, total);
        double pctNormal = pct(nbNormal, total);

        // Machine la plus coûteuse
        String nomMachineMax = "-";
        double coutMachineMax = 0;
        if (!coutParMachine.isEmpty()) {
            Map.Entry<String, Double> maxEntry = coutParMachine.entrySet()
                    .stream().max(Map.Entry.comparingByValue()).get();
            nomMachineMax  = maxEntry.getKey();
            coutMachineMax = maxEntry.getValue();
        }

        // ════════════════════════════════════════════════════════
        //  1. TITRE
        // ════════════════════════════════════════════════════════
        Label titre = new Label("📊  Statistiques des Maintenances");
        titre.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");

        Label sousTitre = new Label("Données affichées : " + total + " maintenance(s)  |  "
                + "Coût total : " + String.format("%.2f DT", coutTotal)
                + "  |  Coût moyen : " + String.format("%.2f DT", coutMoyen));
        sousTitre.setStyle("-fx-font-size:12px;-fx-text-fill:#7f8c8d;");

        VBox headerBox = new VBox(4, titre, sousTitre);
        headerBox.setAlignment(Pos.CENTER);

        // ════════════════════════════════════════════════════════
        //  2. CARTES KPI (ligne du haut)
        // ════════════════════════════════════════════════════════
        HBox cartes = new HBox(10);
        cartes.setAlignment(Pos.CENTER);
        cartes.setPadding(new Insets(0, 10, 0, 10));

        Object[][] kpi = {
                {"📋 Total",           String.valueOf(total),
                        String.format("%.0f%% du global", pct(total, listeMaintenances.size())), "#3498db"},
                {"💰 Coût Total",      String.format("%.2f DT", coutTotal),
                        String.format("%.0f%% coûts élevés", pctEleve),                           "#e74c3c"},
                {"📈 Coût Moyen",      String.format("%.2f DT", coutMoyen),
                        String.format("%.0f%% sous la moy.", pctSous),                            "#f39c12"},
                {"🏆 Machine Max",     nomMachineMax,
                        String.format("%.2f DT (%.0f%%)", coutMachineMax, pct(coutMachineMax, coutTotal)), "#27ae60"},
        };

        for (Object[] k : kpi) {
            VBox card = new VBox(4);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(148);
            card.setPadding(new Insets(10, 12, 10, 12));
            card.setStyle(
                    "-fx-background-color:white;" +
                            "-fx-border-color:" + k[3] + ";" +
                            "-fx-border-width:0 0 0 5;" +
                            "-fx-background-radius:10;" +
                            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.09),6,0,0,2);"
            );
            Label lNom = new Label((String) k[0]);
            lNom.setStyle("-fx-font-size:10px;-fx-text-fill:#7f8c8d;-fx-font-weight:bold;");
            lNom.setWrapText(true);
            Label lVal = new Label((String) k[1]);
            lVal.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + k[3] + ";");
            lVal.setWrapText(true);
            Label lPct = new Label((String) k[2]);
            lPct.setStyle("-fx-font-size:10px;-fx-text-fill:" + k[3] + ";-fx-font-weight:bold;");
            lPct.setWrapText(true);
            card.getChildren().addAll(lNom, lVal, lPct);
            cartes.getChildren().add(card);
        }

        // ════════════════════════════════════════════════════════
        //  3. PIE CHART — Répartition par Type de Panne
        // ════════════════════════════════════════════════════════
        ObservableList<PieChart.Data> pieTypesData = FXCollections.observableArrayList();
        List<String> typeKeys = new ArrayList<>(nbParType.keySet());
        for (String type : typeKeys) {
            long nb = nbParType.get(type);
            pieTypesData.add(new PieChart.Data(
                    String.format("%s\n%d (%.0f%%)", type, nb, pct(nb, total)), nb));
        }
        if (pieTypesData.isEmpty())
            pieTypesData.add(new PieChart.Data("Aucune donnée", 1));

        PieChart pieTypes = new PieChart(pieTypesData);
        pieTypes.setTitle("Pannes par type");
        pieTypes.setLabelsVisible(true);
        pieTypes.setLegendVisible(false);
        pieTypes.setPrefSize(310, 270);
        pieTypes.setStyle("-fx-background-color:transparent;");

        // ── PIE CHART — Répartition des Coûts par Machine
        ObservableList<PieChart.Data> pieMachinesData = FXCollections.observableArrayList();
        List<String> machineKeys = new ArrayList<>(coutParMachine.keySet());
        // Trier par coût desc
        machineKeys.sort((a, b) -> Double.compare(coutParMachine.get(b), coutParMachine.get(a)));
        for (String nom : machineKeys) {
            double c = coutParMachine.get(nom);
            pieMachinesData.add(new PieChart.Data(
                    String.format("%s\n%.0f DT (%.0f%%)", nom, c, pct(c, coutTotal)), c));
        }
        if (pieMachinesData.isEmpty())
            pieMachinesData.add(new PieChart.Data("Aucune donnée", 1));

        PieChart pieMachines = new PieChart(pieMachinesData);
        pieMachines.setTitle("Coûts par machine");
        pieMachines.setLabelsVisible(true);
        pieMachines.setLegendVisible(false);
        pieMachines.setPrefSize(310, 270);
        pieMachines.setStyle("-fx-background-color:transparent;");

        // Colorisation après rendu
        Platform.runLater(() -> {
            appliquerCouleurs(pieTypesData,    PIE_COLORS);
            appliquerCouleurs(pieMachinesData, PIE_COLORS);
        });

        // Titres des graphiques
        Label lblPieTypes    = new Label("🔧 Répartition par Type de Panne");
        lblPieTypes.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        Label lblPieMachines = new Label("🚜 Répartition des Coûts par Machine");
        lblPieMachines.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");

        VBox boxPie1 = new VBox(6, lblPieTypes, pieTypes);
        boxPie1.setAlignment(Pos.CENTER);
        boxPie1.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                "-fx-padding:12;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");

        VBox boxPie2 = new VBox(6, lblPieMachines, pieMachines);
        boxPie2.setAlignment(Pos.CENTER);
        boxPie2.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                "-fx-padding:12;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");

        HBox deuxPies = new HBox(14, boxPie1, boxPie2);
        deuxPies.setAlignment(Pos.CENTER);
        deuxPies.setPadding(new Insets(0, 10, 0, 10));

        // ════════════════════════════════════════════════════════
        //  4. PIE CHART — Répartition par Niveau de Coût
        // ════════════════════════════════════════════════════════
        ObservableList<PieChart.Data> pieNiveauData = FXCollections.observableArrayList();
        if (nbEleve   > 0) pieNiveauData.add(new PieChart.Data(
                String.format("Élevé >300 DT\n%d (%.0f%%)", nbEleve, pctEleve), nbEleve));
        if (nbNormal  > 0) pieNiveauData.add(new PieChart.Data(
                String.format("Normal\n%d (%.0f%%)", nbNormal, pctNormal), nbNormal));
        if (nbSousMoy > 0) pieNiveauData.add(new PieChart.Data(
                String.format("Sous la moy.\n%d (%.0f%%)", nbSousMoy, pctSous), nbSousMoy));
        if (pieNiveauData.isEmpty()) pieNiveauData.add(new PieChart.Data("Aucune donnée", 1));

        PieChart pieNiveau = new PieChart(pieNiveauData);
        pieNiveau.setTitle("Niveau des coûts");
        pieNiveau.setLabelsVisible(true);
        pieNiveau.setLegendVisible(false);
        pieNiveau.setPrefSize(310, 230);
        pieNiveau.setStyle("-fx-background-color:transparent;");

        String[] niveauColors = {"#e74c3c", "#27ae60", "#f39c12"};
        Platform.runLater(() -> appliquerCouleurs(pieNiveauData, niveauColors));

        Label lblPieNiveau = new Label("💰 Répartition par Niveau de Coût");
        lblPieNiveau.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");

        // ════════════════════════════════════════════════════════
        //  5. BARRE DE PROGRESSION — Coûts par type
        // ════════════════════════════════════════════════════════
        VBox barresBox = new VBox(6);
        barresBox.setPadding(new Insets(10, 14, 10, 14));
        barresBox.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");

        Label lblBarres = new Label("📊 Coût par Type de Panne (barres)");
        lblBarres.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        barresBox.getChildren().add(lblBarres);

        double maxCoutType = coutParType.values().stream().mapToDouble(d -> d).max().orElse(1);
        int colorIdx = 0;
        List<String> sortedTypes = coutParType.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey).collect(Collectors.toList());

        for (String type : sortedTypes) {
            double c = coutParType.get(type);
            long nb  = nbParType.getOrDefault(type, 0L);
            double ratio = c / maxCoutType;
            String color = PIE_COLORS[colorIdx % PIE_COLORS.length];
            colorIdx++;

            Label lType = new Label(String.format("%-22s", type)
                    + String.format("  %d maint.  —  %.2f DT  (%.0f%%)", nb, c, pct(c, coutTotal)));
            lType.setStyle("-fx-font-size:11px;-fx-text-fill:#2c3e50;-fx-font-weight:bold;");

            Region barre = new Region();
            barre.setPrefHeight(14);
            barre.setPrefWidth(Math.max(ratio * 400, 4));
            barre.setStyle("-fx-background-color:" + color + ";-fx-background-radius:4;");

            HBox row = new HBox(8, lType, barre);
            row.setAlignment(Pos.CENTER_LEFT);
            barresBox.getChildren().add(row);
        }
        if (sortedTypes.isEmpty()) {
            barresBox.getChildren().add(new Label("Aucune donnée disponible"));
        }

        // ════════════════════════════════════════════════════════
        //  6. Assemblage : pie niveau + barres côte à côte
        // ════════════════════════════════════════════════════════
        VBox boxPieNiveau = new VBox(6, lblPieNiveau, pieNiveau);
        boxPieNiveau.setAlignment(Pos.CENTER);
        boxPieNiveau.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                "-fx-padding:12;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);");
        boxPieNiveau.setPrefWidth(330);

        HBox ligneInferieure = new HBox(14, boxPieNiveau, barresBox);
        ligneInferieure.setAlignment(Pos.CENTER_LEFT);
        ligneInferieure.setPadding(new Insets(0, 10, 0, 10));
        HBox.setHgrow(barresBox, Priority.ALWAYS);

        // ════════════════════════════════════════════════════════
        //  7. Bouton Fermer
        // ════════════════════════════════════════════════════════
        Button btnFermer = new Button("✖  Fermer");
        btnFermer.setPrefWidth(150);
        btnFermer.setPrefHeight(42);
        btnFermer.setStyle(
                "-fx-background-color:#e74c3c;-fx-text-fill:white;" +
                        "-fx-font-size:14px;-fx-font-weight:bold;" +
                        "-fx-background-radius:8;-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(gaussian,rgba(231,76,60,0.4),8,0,0,2);"
        );

        // ════════════════════════════════════════════════════════
        //  8. Layout principal
        // ════════════════════════════════════════════════════════
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f0f2f5;");

        VBox layout = new VBox(14);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(22, 20, 22, 20));
        layout.setStyle("-fx-background-color:#f0f2f5;");
        layout.getChildren().addAll(
                headerBox,
                new Separator(),
                cartes,
                new Separator(),
                deuxPies,
                ligneInferieure,
                new Separator(),
                btnFermer
        );

        scroll.setContent(layout);

        // ════════════════════════════════════════════════════════
        //  9. Stage modal
        // ════════════════════════════════════════════════════════
        Stage popup = new Stage();
        popup.setTitle("📊 Statistiques — Gestion des Maintenances");
        popup.setScene(new Scene(scroll, 720, 720));
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(tableMaintenances.getScene().getWindow());
        popup.setResizable(true);

        btnFermer.setOnAction(e -> popup.close());

        popup.show();

        // Colorisation garantie après rendu complet
        Platform.runLater(() -> {
            appliquerCouleurs(pieTypesData,    PIE_COLORS);
            appliquerCouleurs(pieMachinesData, PIE_COLORS);
            appliquerCouleurs(pieNiveauData,   niveauColors);
        });
    }

    /** Applique les couleurs hex aux segments d'un PieChart. */
    private void appliquerCouleurs(ObservableList<PieChart.Data> data, String[] colors) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getNode() != null) {
                data.get(i).getNode().setStyle(
                        "-fx-pie-color: " + colors[i % colors.length] + ";");
            }
        }
    }

    /** Calcule un pourcentage, retourne 0 si dénominateur nul. */
    private double pct(double num, double den) {
        return den == 0 ? 0.0 : (num / den) * 100.0;
    }

    // ============================================================
    //  ACTUALISER
    // ============================================================
    @FXML
    private void actualiser() {
        chargerMachines();
        chargerMaintenances();
        chargerTypesPannes();
        champRecherche.clear();
        comboMachine.setValue("Toutes les machines");
        if (comboTypePanne != null) comboTypePanne.setValue("Tous les types");
        triActif = "date_recent";
        mettreAJourStyleBoutonsTri();
        appliquerFiltres();
    }

    // ============================================================
    //  BOUTONS AJOUTER / MODIFIER / SUPPRIMER
    // ============================================================
    @FXML
    private void ouvrirAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMaintenance.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Maintenance");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            actualiser();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierSelection() {
        Maintenance selected = tableMaintenances.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherAlerte("Attention", "Veuillez selectionner une maintenance a modifier.", Alert.AlertType.WARNING); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMaintenance.fxml"));
            Parent root = loader.load();
            ModifierMaintenanceController controller = loader.getController();
            controller.initialiserDonnees(selected);
            Stage stage = new Stage();
            stage.setTitle("Modifier une Maintenance");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            actualiser();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerSelection() {
        Maintenance selected = tableMaintenances.getSelectionModel().getSelectedItem();
        if (selected == null) { afficherAlerte("Attention", "Veuillez selectionner une maintenance a supprimer.", Alert.AlertType.WARNING); return; }
        Machine machine = mapMachines.get(selected.getIdM());
        String nomMachine = (machine != null) ? machine.getNom() : "Inconnue";
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette maintenance ?");
        confirmation.setContentText("Machine : " + nomMachine + "\nType    : " + selected.getTypePanne() +
                "\nCout    : " + String.format("%.2f DT", selected.getCout()) + "\nDate    : " + selected.getDateMain());
        Optional<ButtonType> res = confirmation.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                maintenanceService.supprimer(selected.getIdMain());
                afficherAlerte("Succes", "Maintenance supprimee avec succes", Alert.AlertType.INFORMATION);
                actualiser();
            } catch (SQLException e) {
                afficherAlerte("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ============================================================
    //  EXPORT HTML -> PDF
    // ============================================================
    @FXML
    private void exporterPDF() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("rapport_maintenances.html");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier HTML imprimable", "*.html"));
        Stage stage = (Stage) tableMaintenances.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {
            ObservableList<Maintenance> items = tableMaintenances.getItems();
            int total = items.size();
            double coutTotal = items.stream().mapToDouble(Maintenance::getCout).sum();
            double coutMoyen = total > 0 ? coutTotal / total : 0.0;
            long nbEleve = items.stream().filter(m -> m.getCout() > 300).count();
            int pctEleve = total > 0 ? (int) Math.round((double) nbEleve / total * 100) : 0;
            int pctTotal = listeMaintenances.size() > 0 ? (int) Math.round((double) total / listeMaintenances.size() * 100) : 100;
            Map<Integer, Double> cpm = new HashMap<>();
            for (Maintenance m : items) cpm.merge(m.getIdM(), m.getCout(), Double::sum);
            String nomMax = "-"; double cMax = 0; int pctMac = 0;
            if (!cpm.isEmpty()) {
                int idMax = cpm.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
                Machine mac = mapMachines.get(idMax);
                nomMax = (mac != null) ? mac.getNom() : "ID=" + idMax;
                cMax = cpm.get(idMax);
                pctMac = coutTotal > 0 ? (int) Math.round(cMax / coutTotal * 100) : 0;
            }

            w.println("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'><title>Rapport AGROFLOW</title><style>");
            w.println("@page{size:A4 landscape;margin:12mm}*{box-sizing:border-box;margin:0;padding:0}");
            w.println("body{font-family:Arial,sans-serif;font-size:11px;color:#2c3e50}");
            w.println(".hdr{background:#27ae60;color:#fff;padding:14px 20px;border-radius:6px;margin-bottom:14px}");
            w.println(".hdr h1{font-size:20px;margin-bottom:4px}.hdr p{font-size:11px;opacity:.85}");
            w.println(".stats{display:flex;gap:10px;margin-bottom:14px}");
            w.println(".sc{flex:1;padding:10px 14px;border-radius:6px;border-left:4px solid;background:#fff}");
            w.println(".sc .lbl{font-size:9px;text-transform:uppercase;color:#7f8c8d;font-weight:bold}");
            w.println(".sc .val{font-size:17px;font-weight:bold;margin-top:3px}");
            w.println(".sc .pct{display:inline-block;margin-top:5px;padding:2px 8px;border-radius:10px;font-size:10px;font-weight:bold}");
            w.println(".blue{border-color:#3498db}.blue .val{color:#3498db}.blue .pct{background:#eaf4fb;color:#3498db}");
            w.println(".red{border-color:#e74c3c}.red .val{color:#e74c3c}.red .pct{background:#fdf0ef;color:#e74c3c}");
            w.println(".orange{border-color:#f39c12}.orange .val{color:#f39c12}.orange .pct{background:#fef9ec;color:#f39c12}");
            w.println(".green{border-color:#27ae60}.green .val{color:#27ae60;font-size:13px}.green .pct{background:#eafaf1;color:#27ae60}");
            w.println("table{width:100%;border-collapse:collapse;background:#fff}");
            w.println("th{background:#2c3e50;color:#fff;padding:8px 10px;text-align:left;font-size:11px}");
            w.println("td{padding:6px 10px;border-bottom:1px solid #ecf0f1;font-size:11px}");
            w.println("tr:nth-child(even) td{background:#f8f9fa}");
            w.println(".be{background:#f0e6ff;color:#8e44ad;padding:2px 7px;border-radius:10px;font-size:10px;font-weight:bold}");
            w.println(".bm{background:#fff3e0;color:#e67e22;padding:2px 7px;border-radius:10px;font-size:10px;font-weight:bold}");
            w.println(".bh{background:#e3f2fd;color:#2980b9;padding:2px 7px;border-radius:10px;font-size:10px;font-weight:bold}");
            w.println(".bd{background:#f5f5f5;color:#7f8c8d;padding:2px 7px;border-radius:10px;font-size:10px;font-weight:bold}");
            w.println(".ch{color:#c0392b;font-weight:bold}.co{color:#27ae60}");
            w.println(".footer{margin-top:12px;font-size:10px;color:#95a5a6;text-align:right}");
            w.println(".pbtn{position:fixed;top:10px;right:10px;background:#27ae60;color:#fff;border:none;padding:9px 18px;border-radius:6px;cursor:pointer;font-size:13px;font-weight:bold}");
            w.println("@media print{.pbtn{display:none}}</style></head><body>");
            w.println("<button class='pbtn' onclick='window.print()'>Imprimer / PDF</button>");
            w.println("<div class='hdr'><h1>AGROFLOW - Rapport des Maintenances</h1>");
            w.printf("<p>Genere le : %s  |  %d maintenance(s)  |  Tri actif : %s</p></div>%n", LocalDate.now(), total, triActif.replace("_", " "));
            w.println("<div class='stats'>");
            w.printf("<div class='sc blue'><div class='lbl'>Total</div><div class='val'>%d</div><div class='pct'>%d%% du total</div></div>%n", total, pctTotal);
            w.printf("<div class='sc red'><div class='lbl'>Cout Total</div><div class='val'>%.2f DT</div><div class='pct'>%d%% couts eleves</div></div>%n", coutTotal, pctEleve);
            w.printf("<div class='sc orange'><div class='lbl'>Cout Moyen</div><div class='val'>%.2f DT</div><div class='pct'>Seuil : 300 DT</div></div>%n", coutMoyen);
            w.printf("<div class='sc green'><div class='lbl'>Machine + couteuse</div><div class='val'>%s - %.2f DT</div><div class='pct'>%d%% du cout total</div></div>%n", escapeHtml(nomMax), cMax, pctMac);
            w.println("</div>");
            w.println("<table><thead><tr><th>Machine</th><th>Type de Panne</th><th>Date</th><th>Cout (DT)</th><th>Description</th></tr></thead><tbody>");
            for (Maintenance m : items) {
                Machine mac = mapMachines.get(m.getIdM());
                String nomMac = (mac != null) ? escapeHtml(mac.getNom()) : "Inconnu";
                String type  = m.getTypePanne()   != null ? escapeHtml(m.getTypePanne())   : "";
                String date  = m.getDateMain()    != null ? m.getDateMain().toString()      : "";
                String desc  = m.getDescription() != null ? escapeHtml(m.getDescription()) : "";
                double cout  = m.getCout();
                String t = type.toLowerCase();
                String bc = t.contains("elect") ? "be" : t.contains("mot") ? "bm" : t.contains("hyd") ? "bh" : "bd";
                String cc = cout > 300 ? "ch" : "co";
                w.printf("<tr><td><strong>%s</strong></td><td><span class='%s'>%s</span></td><td>%s</td><td class='%s'>%.2f DT</td><td>%s</td></tr>%n",
                        nomMac, bc, type, date, cc, cout, desc);
            }
            w.println("</tbody></table><div class='footer'>Rapport genere par AGROFLOW - Gestion Agricole</div></body></html>");
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().browse(file.toURI());
            afficherAlerte("Succes", "Rapport PDF genere !\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur export PDF : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  EXPORT EXCEL (CSV)
    // ============================================================
    @FXML
    private void exporterExcel() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en Excel");
        fc.setInitialFileName("maintenances.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV Excel", "*.csv"));
        Stage stage = (Stage) tableMaintenances.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
        if (file == null) return;
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.Charset.forName("windows-1252")))) {
            w.println("Machine;Type de Panne;Date;Cout (DT);Description");
            for (Maintenance m : tableMaintenances.getItems()) {
                Machine mac = mapMachines.get(m.getIdM());
                String nomMac = (mac != null) ? mac.getNom() : "Inconnu";
                w.printf("%s;%s;%s;%.2f;%s%n",
                        csvVal(nomMac), csvVal(m.getTypePanne()),
                        m.getDateMain() != null ? m.getDateMain().toString() : "",
                        m.getCout(), csvVal(m.getDescription()));
            }
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(file);
            afficherAlerte("Succes", "Export Excel reussi !\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur export Excel : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private String csvVal(String s) { return s != null ? "\"" + s.replace("\"","\"\"") + "\"" : ""; }
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    // ============================================================
    //  NAVIGATION
    // ============================================================
    @FXML private void retourAccueil(javafx.event.ActionEvent e)      { naviguerDepuisBouton("/AccueilMateriel.fxml", e); }
    @FXML private void naviguerAnimaux(javafx.event.ActionEvent e)    { naviguerDepuisBouton("/AfficherAnimaux.fxml", e); }
    @FXML private void naviguerMateriels(javafx.event.ActionEvent e)  { naviguerDepuisBouton("/AccueilMateriel.fxml", e); }
    @FXML private void naviguerStocks(javafx.event.ActionEvent e)     { naviguerDepuisBouton("/AfficherStocks.fxml", e); }
    @FXML private void naviguerTerrains(javafx.event.ActionEvent e)   { naviguerDepuisBouton("/AfficherTerrains.fxml", e); }
    @FXML private void naviguerEvenements(javafx.event.ActionEvent e) { naviguerDepuisBouton("/AccueilEvenement.fxml", e); }
    @FXML private void naviguerUsers(javafx.event.ActionEvent e)      { naviguerDepuisBouton("/AfficherUsers.fxml", e); }

    @FXML
    private void deconnexion(javafx.event.ActionEvent event) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Deconnexion");
        conf.setHeaderText("Voulez-vous vraiment vous deconnecter ?");
        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) System.exit(0);
    }

    private void naviguerDepuisBouton(String fxmlPath, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Navigation impossible : " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}