package controllers.Materiels;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
import services.User.PersonneService;
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

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar & Header
    // ══════════════════════════════════════════════════════
    @FXML private Button    dashboardBtn;
    @FXML private Label     welcomeNameLabel;
    @FXML private Hyperlink aproposLink;
    @FXML private Label     userNameLabel;
    @FXML private Label     userRoleLabel;

    private Personne currentUser;

    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle    sidebarAvatarBg;

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
    @FXML private TableColumn<Machine, String> colKilometrage;
    @FXML private TableColumn<Machine, String> colDateLastVisite;
    @FXML private TableColumn<Machine, String> colKmLastVisite;
    @FXML private TableColumn<Machine, String> colProchaineMaintenance;
    @FXML private TableColumn<Machine, String> colCin;

    // ══════════════════════════════════════════════════════
    //  FXML — Filtres & Recherche
    // ══════════════════════════════════════════════════════
    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboEtat;
    @FXML private ComboBox<String> comboMarque;
    @FXML private Label            lblSelectionInfo;

    // ══════════════════════════════════════════════════════
    //  FXML — Pagination
    // ══════════════════════════════════════════════════════
    @FXML private Button btnPremiere;
    @FXML private Button btnPrecedente;
    @FXML private Button btnSuivante;
    @FXML private Button btnDerniere;
    @FXML private Label  lblPageInfo;
    @FXML private ComboBox<Integer> comboTaillePage;

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
    //  FXML — Boutons CRUD
    // ══════════════════════════════════════════════════════
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Label  lblCrudInfo;

    // ══════════════════════════════════════════════════════
    //  Services & Données
    // ══════════════════════════════════════════════════════
    private MachineService     machineService;
    private MaintenanceService maintenanceService;
    private PersonneService    personneService;

    private final ObservableList<Machine> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Machine> filteredList = FXCollections.observableArrayList();
    private final ObservableList<Machine> displayList = FXCollections.observableArrayList();

    // Liste des personnes pour la ComboBox CIN (clé étrangère)
    private List<Personne> listePersonnes = new ArrayList<>();

    // ══════════════════════════════════════════════════════
    //  Pagination
    // ══════════════════════════════════════════════════════
    private int pageCourante   = 1;
    private int taillePage     = 10;
    private int totalPages     = 1;

    // ══════════════════════════════════════════════════════
    //  Tri
    // ══════════════════════════════════════════════════════
    private enum SortMode { NOM_AZ, NOM_ZA, DATE_RECENT, DATE_ANCIEN, MARQUE_AZ, NONE }
    private SortMode currentSort = SortMode.NOM_AZ;

    // ══════════════════════════════════════════════════════
    //  Navigation latérale
    // ══════════════════════════════════════════════════════
    public void ouvrirMaintenance(MouseEvent e) { navigateTo(e, "/MaterielsInterface/AgricoleAffichageMaintenance.fxml", "Maintenance"); }
    public void ouvrirAchat(MouseEvent e)       { navigateTo(e, "/MaterielsInterface/AgricoleAffichageAchat.fxml",       "Achat");       }
    public void ouvrirMachine(MouseEvent e)     { navigateTo(e, "/MaterielsInterface/AgricoleAffichageMachine.fxml",     "Machine");     }

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null)
            System.out.println("✓ currentUser: " + currentUser.getNom());
        else
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");

        chargerSidebarAvatar(SessionManager.getCurrentUser());

        machineService     = new MachineService();
        maintenanceService = new MaintenanceService();
        personneService    = new PersonneService();

        // Charger la liste des personnes (CIN FK)
        try { listePersonnes = personneService.recuperer(); }
        catch (SQLException e) { System.err.println("Erreur chargement personnes : " + e.getMessage()); }

        // Initialiser la ComboBox taille de page
        if (comboTaillePage != null) {
            comboTaillePage.setItems(FXCollections.observableArrayList(5, 10, 20, 50));
            comboTaillePage.setValue(taillePage);
            comboTaillePage.setOnAction(ev -> {
                taillePage     = comboTaillePage.getValue();
                pageCourante   = 1;
                mettreAJourPage();
            });
        }

        configurerTableau();
        chargerDonnees();
        configurerRecherche();
        verifierMaintenancesProches();

        // Sélection ligne → activation boutons CRUD
        tableMachines.getSelectionModel().selectedItemProperty()
                .addListener((obs, ov, nv) -> {
                    boolean sel = (nv != null);
                    if (lblSelectionInfo != null) {
                        if (sel)
                            lblSelectionInfo.setText("Sélectionné : " + nv.getNom()
                                    + " — " + nv.getMarque() + " " + nv.getModele()
                                    + " | État : " + nv.getEtatM());
                        else
                            lblSelectionInfo.setText("Cliquez sur une ligne pour voir les détails");
                    }
                    if (btnModifier  != null) btnModifier.setDisable(!sel);
                    if (btnSupprimer != null) btnSupprimer.setDisable(!sel);
                    if (lblCrudInfo  != null)
                        lblCrudInfo.setText(sel
                                ? "✅ Machine sélectionnée : " + nv.getNom()
                                : "Sélectionnez une machine pour modifier ou supprimer");
                });
    }

    // ══════════════════════════════════════════════════════
    //  ALERTE MAINTENANCE PROCHE
    // ══════════════════════════════════════════════════════
    private void verifierMaintenancesProches() {
        LocalDate aujourd = LocalDate.now();
        LocalDate limite  = aujourd.plusDays(30);

        List<Machine> aAlerter = masterList.stream()
                .filter(m -> m.getProchaineMaintenance() != null
                        && !m.getProchaineMaintenance().isBefore(aujourd)
                        && !m.getProchaineMaintenance().isAfter(limite))
                .collect(Collectors.toList());

        List<Machine> enRetard = masterList.stream()
                .filter(m -> m.getProchaineMaintenance() != null
                        && m.getProchaineMaintenance().isBefore(aujourd))
                .collect(Collectors.toList());

        if (aAlerter.isEmpty() && enRetard.isEmpty()) return;

        StringBuilder msg = new StringBuilder();

        if (!enRetard.isEmpty()) {
            msg.append("⚠️ MAINTENANCE EN RETARD :\n");
            for (Machine m : enRetard) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(
                        m.getProchaineMaintenance(), aujourd);
                msg.append("  • ").append(m.getNom())
                        .append(" (").append(m.getMarque()).append(" ").append(m.getModele()).append(")")
                        .append(" — en retard de ").append(jours).append(" jour(s)\n");
            }
        }

        if (!aAlerter.isEmpty()) {
            if (msg.length() > 0) msg.append("\n");
            msg.append("🔔 MAINTENANCE À VENIR (≤ 30 jours) :\n");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Machine m : aAlerter) {
                long jours = java.time.temporal.ChronoUnit.DAYS.between(
                        aujourd, m.getProchaineMaintenance());
                msg.append("  • ").append(m.getNom())
                        .append(" (").append(m.getMarque()).append(" ").append(m.getModele()).append(")")
                        .append(" — dans ").append(jours).append(" jour(s)")
                        .append(" [").append(m.getProchaineMaintenance().format(fmt)).append("]\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🔧 Alertes Maintenance");
        alert.setHeaderText("Machines nécessitant une attention !");
        alert.setContentText(msg.toString().trim());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show(); // non-bloquant
    }

    // ══════════════════════════════════════════════════════
    //  CONFIGURATION TABLEAU — tous les attributs
    // ══════════════════════════════════════════════════════
    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colNom.setCellValueFactory(d         -> new SimpleStringProperty(safe2(d.getValue().getNom())));
        colMarque.setCellValueFactory(d      -> new SimpleStringProperty(safe2(d.getValue().getMarque())));
        colModele.setCellValueFactory(d      -> new SimpleStringProperty(safe2(d.getValue().getModele())));
        colNumeroSerie.setCellValueFactory(d -> new SimpleStringProperty(safe2(d.getValue().getNumeroSerie())));

        colDateAchat.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDateAchat();
            return new SimpleStringProperty(date != null ? date.format(fmt) : "");
        });
        colKilometrage.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getKilometrage() > 0 ? d.getValue().getKilometrage() + " km" : ""));
        colDateLastVisite.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDateLastVisite();
            return new SimpleStringProperty(date != null ? date.format(fmt) : "");
        });
        colKmLastVisite.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getKmLastVisite() > 0 ? d.getValue().getKmLastVisite() + " km" : ""));
        colProchaineMaintenance.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getProchaineMaintenance();
            return new SimpleStringProperty(date != null ? date.format(fmt) : "");
        });

        // CIN → afficher nom de la personne si trouvée
        colCin.setCellValueFactory(d -> {
            int cin = d.getValue().getCin();
            if (cin <= 0) return new SimpleStringProperty("");
            return new SimpleStringProperty(listePersonnes.stream()
                    .filter(p -> p.getCin() == cin)
                    .map(p -> p.getPrenom() + " " + p.getNom() + " (" + cin + ")")
                    .findFirst()
                    .orElse(String.valueOf(cin)));
        });

        // Colonne État — colorée
        colEtat.setCellValueFactory(d -> new SimpleStringProperty(safe2(d.getValue().getEtatM())));
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

        // Colonne Prochaine Maintenance — colorée selon urgence
        colProchaineMaintenance.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null || val.isBlank()) { setText(null); setStyle(""); return; }
                setText(val);
                Machine m = getTableView().getItems().get(getIndex());
                if (m != null && m.getProchaineMaintenance() != null) {
                    LocalDate now    = LocalDate.now();
                    LocalDate maint  = m.getProchaineMaintenance();
                    if (maint.isBefore(now))
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // retard
                    else if (!maint.isAfter(now.plusDays(30)))
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // proche
                    else
                        setStyle("-fx-text-fill: #27ae60;");
                }
            }
        });

        // Lignes alternées
        tableMachines.setRowFactory(tv -> new TableRow<Machine>() {
            @Override
            protected void updateItem(Machine item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                String etat = safe(item.getEtatM());
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
        pageCourante = 1;
        appliquerFiltres();
        verifierMaintenancesProches();
    }

    private void initialiserCombos() {
        if (comboEtat != null) {
            Set<String> etats = new LinkedHashSet<>();
            etats.add("Tous les états");
            masterList.stream().map(Machine::getEtatM)
                    .filter(e -> e != null && !e.isBlank())
                    .distinct().sorted().forEach(etats::add);
            comboEtat.setItems(FXCollections.observableArrayList(etats));
            comboEtat.getSelectionModel().selectFirst();
        }
        if (comboMarque != null) {
            Set<String> marques = new LinkedHashSet<>();
            marques.add("Toutes les marques");
            masterList.stream().map(Machine::getMarque)
                    .filter(m -> m != null && !m.isBlank())
                    .distinct().sorted().forEach(marques::add);
            comboMarque.setItems(FXCollections.observableArrayList(marques));
            comboMarque.getSelectionModel().selectFirst();
        }
    }

    // ══════════════════════════════════════════════════════
    //  RECHERCHE
    // ══════════════════════════════════════════════════════
    private void configurerRecherche() {
        if (champRecherche != null)
            champRecherche.textProperty().addListener((obs, o, n) -> { pageCourante = 1; appliquerFiltres(); });
    }

    @FXML private void rechercher() { pageCourante = 1; appliquerFiltres(); }
    @FXML private void filtrer()    { pageCourante = 1; appliquerFiltres(); }

    @FXML
    private void effacerRecherche() {
        if (champRecherche != null) champRecherche.clear();
        pageCourante = 1;
        appliquerFiltres();
    }

    // ══════════════════════════════════════════════════════
    //  TRIS
    // ══════════════════════════════════════════════════════
    @FXML private void trierNomAZ()      { currentSort = SortMode.NOM_AZ;      pageCourante = 1; appliquerFiltres(); }
    @FXML private void trierNomZA()      { currentSort = SortMode.NOM_ZA;      pageCourante = 1; appliquerFiltres(); }
    @FXML private void trierDateRecent() { currentSort = SortMode.DATE_RECENT; pageCourante = 1; appliquerFiltres(); }
    @FXML private void trierDateAncien() { currentSort = SortMode.DATE_ANCIEN; pageCourante = 1; appliquerFiltres(); }
    @FXML private void trierMarqueAZ()   { currentSort = SortMode.MARQUE_AZ;   pageCourante = 1; appliquerFiltres(); }

    @FXML
    private void reinitialiserFiltres() {
        if (champRecherche != null) champRecherche.clear();
        if (comboEtat      != null) comboEtat.getSelectionModel().selectFirst();
        if (comboMarque    != null) comboMarque.getSelectionModel().selectFirst();
        currentSort  = SortMode.NOM_AZ;
        pageCourante = 1;
        appliquerFiltres();
    }

    @FXML private void actualiser() { chargerDonnees(); }

    // ══════════════════════════════════════════════════════
    //  FILTRES + TRI + PAGINATION
    // ══════════════════════════════════════════════════════
    private void appliquerFiltres() {
        String recherche = champRecherche != null && champRecherche.getText() != null
                ? champRecherche.getText().toLowerCase().trim() : "";
        String etatSel   = comboEtat   != null && comboEtat.getValue()   != null ? comboEtat.getValue()   : "Tous les états";
        String marqueSel = comboMarque != null && comboMarque.getValue() != null ? comboMarque.getValue() : "Toutes les marques";

        List<Machine> filtered = masterList.stream()
                .filter(m -> {
                    boolean matchR = recherche.isEmpty()
                            || safe(m.getNom()).contains(recherche)
                            || safe(m.getMarque()).contains(recherche)
                            || safe(m.getModele()).contains(recherche)
                            || safe(m.getEtatM()).contains(recherche)
                            || safe(m.getNumeroSerie()).contains(recherche)
                            || (m.getDateAchat() != null && m.getDateAchat().toString().contains(recherche))
                            || (m.getKilometrage() > 0 && String.valueOf(m.getKilometrage()).contains(recherche))
                            || (m.getCin() > 0 && String.valueOf(m.getCin()).contains(recherche));
                    boolean matchE = etatSel.equals("Tous les états")
                            || safe(m.getEtatM()).equalsIgnoreCase(etatSel.toLowerCase());
                    boolean matchM = marqueSel.equals("Toutes les marques")
                            || safe(m.getMarque()).equalsIgnoreCase(marqueSel.toLowerCase());
                    return matchR && matchE && matchM;
                })
                .collect(Collectors.toList());

        switch (currentSort) {
            case NOM_AZ      -> filtered.sort(Comparator.comparing(m -> safe(m.getNom())));
            case NOM_ZA      -> filtered.sort(Comparator.comparing((Machine m) -> safe(m.getNom())).reversed());
            case DATE_RECENT -> filtered.sort(Comparator.comparing(
                    (Machine m) -> m.getDateAchat() != null ? m.getDateAchat() : LocalDate.MIN).reversed());
            case DATE_ANCIEN -> filtered.sort(Comparator.comparing(
                    m -> m.getDateAchat() != null ? m.getDateAchat() : LocalDate.MIN));
            case MARQUE_AZ   -> filtered.sort(Comparator.comparing(m -> safe(m.getMarque())));
            default -> {}
        }

        filteredList.setAll(filtered);
        mettreAJourPage();
        mettreAJourStats();
    }

    // ══════════════════════════════════════════════════════
    //  PAGINATION — logique
    // ══════════════════════════════════════════════════════
    private void mettreAJourPage() {
        int total = filteredList.size();
        totalPages = (int) Math.max(1, Math.ceil((double) total / taillePage));
        if (pageCourante > totalPages) pageCourante = totalPages;
        if (pageCourante < 1)         pageCourante = 1;

        int debut = (pageCourante - 1) * taillePage;
        int fin   = Math.min(debut + taillePage, total);

        displayList.setAll(filteredList.subList(debut, fin));
        tableMachines.setItems(displayList);

        // Mise à jour label pagination
        if (lblPageInfo != null)
            lblPageInfo.setText("Page " + pageCourante + " / " + totalPages
                    + "  |  " + total + " résultat(s)");

        // Activer/désactiver boutons navigation
        if (btnPremiere   != null) btnPremiere.setDisable(pageCourante <= 1);
        if (btnPrecedente != null) btnPrecedente.setDisable(pageCourante <= 1);
        if (btnSuivante   != null) btnSuivante.setDisable(pageCourante >= totalPages);
        if (btnDerniere   != null) btnDerniere.setDisable(pageCourante >= totalPages);
    }

    @FXML private void pagePremiere()  { pageCourante = 1;          mettreAJourPage(); }
    @FXML private void pagePrecedente(){ if (pageCourante > 1) { pageCourante--; mettreAJourPage(); } }
    @FXML private void pageSuivante()  { if (pageCourante < totalPages) { pageCourante++; mettreAJourPage(); } }
    @FXML private void pageDerniere()  { pageCourante = totalPages;  mettreAJourPage(); }

    // ══════════════════════════════════════════════════════
    //  STATISTIQUES  (cards BAS)
    // ══════════════════════════════════════════════════════
    private void mettreAJourStats() {
        int nb = filteredList.size(); // stats sur toute la liste filtrée

        long nbBon     = filteredList.stream().filter(m -> { String e = safe(m.getEtatM()); return e.contains("bon") || e.contains("neuf") || e.contains("excellent"); }).count();
        long nbMainten = filteredList.stream().filter(m -> { String e = safe(m.getEtatM()); return e.contains("maintenance") || e.contains("réparation") || e.contains("reparation"); }).count();
        long nbHors    = filteredList.stream().filter(m -> { String e = safe(m.getEtatM()); return e.contains("hors") || e.contains("panne") || e.contains("défectueux"); }).count();

        double pctBon     = nb > 0 ? (nbBon     * 100.0 / nb) : 0;
        double pctMainten = nb > 0 ? (nbMainten * 100.0 / nb) : 0;
        double pctHors    = nb > 0 ? (nbHors    * 100.0 / nb) : 0;

        Map<String, Long> parMarque = new HashMap<>();
        for (Machine m : filteredList) {
            String mk = m.getMarque() != null ? m.getMarque() : "Inconnue";
            parMarque.merge(mk, 1L, Long::sum);
        }
        String marqueDom   = parMarque.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("—");
        long   nbMarqueDom = parMarque.getOrDefault(marqueDom, 0L);

        set(lblTotal,             String.valueOf(nb));
        set(lblTotalFiltre,       displayList.size() + " affiché(s) / " + masterList.size() + " total");
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
    //  STATS GRAPHIQUES (popup Canvas)
    // ══════════════════════════════════════════════════════
    @FXML
    private void afficherStatistiquesGraphiques() {
        if (filteredList.isEmpty()) { showInfo("Stats", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques — Mon Matériel");
        stage.setResizable(true);

        Map<String, Long> parEtat   = new LinkedHashMap<>();
        Map<String, Long> parMarque = new LinkedHashMap<>();
        for (Machine m : filteredList) {
            parEtat.merge(m.getEtatM() != null ? m.getEtatM() : "Inconnu", 1L, Long::sum);
            parMarque.merge(m.getMarque() != null ? m.getMarque() : "Inconnue", 1L, Long::sum);
        }

        Color[] colors = {
                Color.web("#27ae60"), Color.web("#f39c12"), Color.web("#e74c3c"),
                Color.web("#3498db"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63")
        };

        List<String> etatKeys = new ArrayList<>(parEtat.keySet());
        List<Long>   etatVals = new ArrayList<>(parEtat.values());
        int nE = etatKeys.size();
        int barW = 60, gap = 30, padL = 70, padTop = 50, chartH = 220;
        int cwE  = padL + nE * (barW + gap) + gap + 20;

        Canvas canvasEtat = new Canvas(cwE, chartH + 90 + padTop);
        GraphicsContext ge = canvasEtat.getGraphicsContext2D();
        ge.setFill(Color.WHITE); ge.fillRect(0, 0, cwE, chartH + 90 + padTop);
        ge.setFill(Color.web("#2E7D32")); ge.setFont(Font.font("Arial", FontWeight.BOLD, 13));
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
            ge.setFill(Color.rgb(0,0,0,0.07)); ge.fillRoundRect(x+3, y+3, barW, bH, 6, 6);
            ge.setFill(c);                      ge.fillRoundRect(x, y, barW, bH, 6, 6);
            ge.setFill(Color.web("#2E7D32")); ge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.valueOf(etatVals.get(i));
            ge.fillText(vs, x + barW/2.0 - vs.length()*3.5, y - 6);
            ge.setFont(Font.font("Arial", 10));
            String lbl = etatKeys.get(i).length() > 12 ? etatKeys.get(i).substring(0,12)+"…" : etatKeys.get(i);
            ge.fillText(lbl, x + barW/2.0 - lbl.length()*3, padTop + chartH + 16);
        }

        List<String> marqueKeys = new ArrayList<>(parMarque.keySet());
        List<Long>   marqueVals = new ArrayList<>(parMarque.values());
        int nM = marqueKeys.size(), cwM = 400;
        int chM = padTop + 40 + nM * 42 + 20;
        Canvas canvasMarque = new Canvas(cwM, Math.max(chM, chartH + 90 + padTop));
        GraphicsContext gm  = canvasMarque.getGraphicsContext2D();
        gm.setFill(Color.WHITE); gm.fillRect(0, 0, cwM, Math.max(chM, chartH+90+padTop));
        gm.setFill(Color.web("#2E7D32")); gm.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gm.fillText("🏷 Répartition par marque", 10, 30);
        long maxM = marqueVals.stream().mapToLong(v -> v).max().orElse(1);
        int barAreaW = cwM - 160;
        for (int i = 0; i < nM; i++) {
            double bH = 24, y = padTop + i * 42;
            double bW = (marqueVals.get(i) / (double) maxM) * barAreaW;
            Color  c  = colors[i % colors.length];
            gm.setFill(Color.rgb(0,0,0,0.06)); gm.fillRoundRect(153, y+3, bW, bH, 6, 6);
            gm.setFill(c);                      gm.fillRoundRect(150, y, bW, bH, 6, 6);
            gm.setFill(Color.web("#2E7D32")); gm.setFont(Font.font("Arial", 11));
            String lbl = marqueKeys.get(i).length() > 14 ? marqueKeys.get(i).substring(0,14)+"…" : marqueKeys.get(i);
            gm.fillText(lbl, 5, y+17);
            gm.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gm.fillText(String.valueOf(marqueVals.get(i)), 150 + bW + 6, y + 17);
        }

        ScrollPane spL = new ScrollPane(canvasEtat);   spL.setFitToHeight(true); spL.setPrefWidth(cwE + 20);
        ScrollPane spR = new ScrollPane(canvasMarque); spR.setFitToHeight(true); spR.setPrefWidth(cwM + 20);
        HBox charts = new HBox(16, spL, spR); charts.setPadding(new Insets(10));
        VBox root = new VBox(8, charts); root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root)); stage.show();
    }

    // ══════════════════════════════════════════════════════
    //  ANALYSE AVANCÉE
    // ══════════════════════════════════════════════════════
    @FXML
    private void afficherStatistiquesAvancees() {
        if (masterList.isEmpty()) { showInfo("Analyse", "Aucune machine disponible."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🏭 Analyse Avancée — Coût de Maintenance par Machine");
        stage.setResizable(true);

        List<Maintenance> toutesMaintenances = new ArrayList<>();
        try { toutesMaintenances = maintenanceService.recuperer(); }
        catch (SQLException e) { showErr("Erreur", e.getMessage()); return; }

        final List<Maintenance> maintenancesFinal = toutesMaintenances;
        int    nbMachines     = masterList.size();
        int    nbMaintenances = maintenancesFinal.size();
        double coutTotal      = maintenancesFinal.stream().mapToDouble(Maintenance::getCout).sum();
        double coutMoyen      = nbMaintenances > 0 ? coutTotal / nbMaintenances : 0;

        Map<Integer, Double> coutParId = new HashMap<>();
        Map<Integer, Long>   nbParId   = new HashMap<>();
        for (Maintenance m : maintenancesFinal) {
            coutParId.merge(m.getIdM(), m.getCout(), Double::sum);
            nbParId.merge(m.getIdM(), 1L, Long::sum);
        }

        int    idMax   = coutParId.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(-1);
        String nomMax  = idMax >= 0 ? nomMachineParId(idMax) : "—";
        double coutMax = idMax >= 0 ? coutParId.get(idMax) : 0;

        TableView<Map<String, Object>> tblSynth = new TableView<>();
        tblSynth.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSynth.setPrefHeight(200);

        TableColumn<Map<String, Object>, String> sNom = new TableColumn<>("Machine");
        sNom.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("nom")));
        TableColumn<Map<String, Object>, String> sNbM = new TableColumn<>("Nb Maintenances");
        sNbM.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nb"))));
        TableColumn<Map<String, Object>, String> sTot = new TableColumn<>("Coût Total (DT)");
        sTot.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("total"))));
        TableColumn<Map<String, Object>, String> sMoy = new TableColumn<>("Coût Moyen (DT)");
        sMoy.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double) d.getValue().get("moyenne"))));
        TableColumn<Map<String, Object>, String> sPct = new TableColumn<>("% du total");
        sPct.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f%%", (double) d.getValue().get("pct"))));
        tblSynth.getColumns().addAll(sNom, sNbM, sTot, sMoy, sPct);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Machine m : masterList) {
            double tot = coutParId.getOrDefault(m.getIdM(), 0.0);
            long   nb  = nbParId.getOrDefault(m.getIdM(), 0L);
            double moy = nb > 0 ? tot / nb : 0;
            double pct = coutTotal > 0 ? (tot * 100.0 / coutTotal) : 0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nom", m.getNom()); row.put("nb", nb);
            row.put("total", tot);      row.put("moyenne", moy); row.put("pct", pct);
            rows.add(row);
        }
        rows.sort((a, b) -> Double.compare((double) b.get("total"), (double) a.get("total")));
        tblSynth.setItems(FXCollections.observableArrayList(rows));

        HBox cartes = new HBox(10,
                creerCarte("🚜 Machines",        String.valueOf(nbMachines),             "#27ae60"),
                creerCarte("🔧 Maintenances",    String.valueOf(nbMaintenances),          "#3498db"),
                creerCarte("💰 Coût Total (DT)", String.format("%.2f", coutTotal),       "#e74c3c"),
                creerCarte("📊 Coût Moyen (DT)", String.format("%.2f", coutMoyen),       "#f39c12"),
                creerCarte("🏭 + Coûteuse",      nomMax + " (" + String.format("%.0f DT", coutMax) + ")", "#9b59b6")
        );
        cartes.setPadding(new Insets(10));

        int nG = Math.min(rows.size(), 10), bW = 55, gp = 20, pL = 75, pT = 50, cH = 200;
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
            gc.setStroke(Color.LIGHTGRAY); gc.setLineWidth(1); gc.strokeLine(pL, y, cw-20, y);
            gc.setFill(Color.GRAY); gc.setFont(Font.font("Arial", 10));
            gc.fillText(String.format("%.0f", maxVal * i / 4.0), 5, y+4);
        }
        for (int i = 0; i < nG; i++) {
            double tot = (double) rows.get(i).get("total");
            double bH  = (tot / maxVal) * cH;
            double x   = pL + gp + i * (bW + gp);
            double y   = pT + cH - bH;
            Color  c   = cls[i % cls.length];
            gc.setFill(Color.rgb(0,0,0,0.07)); gc.fillRoundRect(x+3, y+3, bW, bH, 6, 6);
            gc.setFill(c);                      gc.fillRoundRect(x, y, bW, bH, 6, 6);
            gc.setFill(Color.web("#2E7D32")); gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.format("%.0f", tot);
            gc.fillText(vs, x + bW/2.0 - vs.length()*3.5, y - 6);
            gc.setFont(Font.font("Arial", 10));
            String lbl = (String) rows.get(i).get("nom");
            if (lbl.length() > 10) lbl = lbl.substring(0,10)+"…";
            gc.fillText(lbl, x + bW/2.0 - lbl.length()*3, pT + cH + 16);
        }
        double yMoy = pT + cH - (coutMoyen / maxVal) * cH;
        if (maxVal > 0 && coutMoyen <= maxVal) {
            gc.setStroke(Color.web("#e74c3c")); gc.setLineWidth(2);
            gc.setLineDashes(8, 4); gc.strokeLine(pL, yMoy, cw-20, yMoy); gc.setLineDashes();
            gc.setFill(Color.web("#e74c3c")); gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText(String.format("Moy. %.0f DT", coutMoyen), cw - 80, yMoy - 4);
        }

        ScrollPane spCanvas = new ScrollPane(canvas); spCanvas.setFitToHeight(true);
        spCanvas.setPrefSize(Math.min(cw+20, 900), cH+100+pT);
        Label titreSynth = new Label("📋 Synthèse détaillée par machine");
        titreSynth.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        Label legende = new Label("  ── Ligne rouge = coût moyen par maintenance");
        legende.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-style: italic;");
        VBox root = new VBox(12, cartes, new Separator(), legende, spCanvas, new Separator(), titreSynth, tblSynth);
        root.setPadding(new Insets(16)); root.setStyle("-fx-background-color: #f8f9fa;");
        stage.setScene(new Scene(root, 1000, 700)); stage.show();
    }

    // ══════════════════════════════════════════════════════
    //  CRUD — AJOUTER
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleAjouter() {
        Machine nouvelle = new Machine();
        boolean confirmed = ouvrirFormulaireDialog(nouvelle, "➕ Ajouter une Machine", false);
        if (!confirmed) return;
        try {
            machineService.ajouter(nouvelle);
            chargerDonnees();
            showInfo("Succès", "Machine \"" + nouvelle.getNom() + "\" ajoutée avec succès.");
        } catch (SQLException e) { showErr("Erreur ajout", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  CRUD — MODIFIER
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleModifier() {
        Machine sel = tableMachines.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo("Info", "Veuillez sélectionner une machine."); return; }

        Machine copie = new Machine(
                sel.getIdM(), sel.getNom(), sel.getMarque(), sel.getModele(),
                sel.getNumeroSerie(), sel.getEtatM(), sel.getDateAchat(),
                sel.getKilometrage(), sel.getDateLastVisite(), sel.getKmLastVisite(),
                sel.getProchaineMaintenance(), sel.getCin()
        );

        boolean confirmed = ouvrirFormulaireDialog(copie, "✏ Modifier : " + sel.getNom(), true);
        if (!confirmed) return;
        try {
            machineService.modifier(copie);
            chargerDonnees();
            showInfo("Succès", "Machine \"" + copie.getNom() + "\" modifiée avec succès.");
        } catch (SQLException e) { showErr("Erreur modification", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  CRUD — SUPPRIMER
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleSupprimer() {
        Machine sel = tableMachines.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo("Info", "Veuillez sélectionner une machine."); return; }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Confirmation de suppression");
        conf.setHeaderText("Supprimer la machine : " + sel.getNom() + " ?");
        conf.setContentText(
                "Marque   : " + sel.getMarque()      + "\n" +
                        "Modèle   : " + sel.getModele()      + "\n" +
                        "N° Série : " + sel.getNumeroSerie() + "\n\n" +
                        "⚠ Cette action est irréversible.");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    machineService.supprimer(sel.getIdM());
                    chargerDonnees();
                    showInfo("Supprimé", "Machine \"" + sel.getNom() + "\" supprimée.");
                } catch (SQLException e) { showErr("Erreur suppression", e.getMessage()); }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  FORMULAIRE DIALOG — tous attributs + contrôles saisie + CIN FK
    // ══════════════════════════════════════════════════════
    private boolean ouvrirFormulaireDialog(Machine machine, String titre, boolean modeEdit) {

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(titre);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        String lblStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2D5A27;";
        String fldStyle = "-fx-background-radius: 8; -fx-border-color: #A8C69F; "
                + "-fx-border-radius: 8; -fx-font-size: 12px; -fx-padding: 6 10;";
        String errStyle = "-fx-border-color: #E74C3C; -fx-background-color: #fff5f5; "
                + "-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-font-size: 12px; -fx-padding: 6 10;";

        // ── Champs ─────────────────────────────────────
        TextField tfNom = styled(new TextField(safe2(machine.getNom())), fldStyle);
        tfNom.setPromptText("Nom de la machine *");

        TextField tfMarque = styled(new TextField(safe2(machine.getMarque())), fldStyle);
        tfMarque.setPromptText("Marque *");

        TextField tfModele = styled(new TextField(safe2(machine.getModele())), fldStyle);
        tfModele.setPromptText("Modèle");

        TextField tfNumeroSerie = styled(new TextField(safe2(machine.getNumeroSerie())), fldStyle);
        tfNumeroSerie.setPromptText("Numéro de série");

        ComboBox<String> cbEtat = new ComboBox<>();
        cbEtat.getItems().addAll("Bon état", "Neuf", "Excellent",
                "En maintenance", "En réparation",
                "Hors service", "En panne", "Défectueux");
        cbEtat.setValue(machine.getEtatM() != null ? machine.getEtatM() : "Bon état");
        cbEtat.setMaxWidth(Double.MAX_VALUE);
        cbEtat.setStyle("-fx-background-radius: 8; -fx-border-color: #A8C69F; -fx-border-radius: 8; -fx-font-size: 12px;");

        DatePicker dpDateAchat = new DatePicker(machine.getDateAchat());
        dpDateAchat.setPromptText("jj/mm/aaaa");
        dpDateAchat.setMaxWidth(Double.MAX_VALUE);
        dpDateAchat.setStyle(fldStyle);

        TextField tfKm = styled(new TextField(
                machine.getKilometrage() > 0 ? String.valueOf(machine.getKilometrage()) : ""), fldStyle);
        tfKm.setPromptText("Kilométrage (entier ≥ 0)");

        DatePicker dpLastVisite = new DatePicker(machine.getDateLastVisite());
        dpLastVisite.setPromptText("Date dernière visite");
        dpLastVisite.setMaxWidth(Double.MAX_VALUE);
        dpLastVisite.setStyle(fldStyle);

        TextField tfKmLastVisite = styled(new TextField(
                machine.getKmLastVisite() > 0 ? String.valueOf(machine.getKmLastVisite()) : ""), fldStyle);
        tfKmLastVisite.setPromptText("Km à la dernière visite (entier ≥ 0)");

        DatePicker dpProchMaint = new DatePicker(machine.getProchaineMaintenance());
        dpProchMaint.setPromptText("Prochaine maintenance");
        dpProchMaint.setMaxWidth(Double.MAX_VALUE);
        dpProchMaint.setStyle(fldStyle);

        // ── CIN — Liste déroulante (clé étrangère → Personne) ──
        ComboBox<Personne> cbCin = new ComboBox<>();
        cbCin.setMaxWidth(Double.MAX_VALUE);
        cbCin.setStyle("-fx-background-radius: 8; -fx-border-color: #A8C69F; -fx-border-radius: 8; -fx-font-size: 12px;");

        // Affichage : "Prénom Nom (CIN)"
        cbCin.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Personne p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getPrenom() + " " + p.getNom() + " (" + p.getCin() + ")");
            }
        });
        cbCin.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Personne p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "— Sélectionner un propriétaire —"
                        : p.getPrenom() + " " + p.getNom() + " (" + p.getCin() + ")");
            }
        });
        cbCin.setItems(FXCollections.observableArrayList(listePersonnes));

        // Pré-sélectionner si CIN déjà défini
        if (machine.getCin() > 0) {
            listePersonnes.stream()
                    .filter(p -> p.getCin() == machine.getCin())
                    .findFirst()
                    .ifPresent(cbCin::setValue);
        }

        // Labels d'erreur inline
        Label errNom        = errLabel();
        Label errMarque     = errLabel();
        Label errKm         = errLabel();
        Label errKmVisite   = errLabel();
        Label errDateAchat  = errLabel();
        Label errProchMaint = errLabel();

        // Validation en temps réel
        tfNom.textProperty().addListener((obs, o, n) -> {
            if (n.isBlank()) { tfNom.setStyle(errStyle); errNom.setText("⚠ Nom obligatoire"); }
            else { tfNom.setStyle(fldStyle); errNom.setText(""); }
        });
        tfMarque.textProperty().addListener((obs, o, n) -> {
            if (n.isBlank()) { tfMarque.setStyle(errStyle); errMarque.setText("⚠ Marque obligatoire"); }
            else { tfMarque.setStyle(fldStyle); errMarque.setText(""); }
        });
        tfKm.textProperty().addListener((obs, o, n) -> {
            if (!n.isBlank()) {
                try {
                    int v = Integer.parseInt(n.trim());
                    if (v < 0) throw new NumberFormatException();
                    tfKm.setStyle(fldStyle); errKm.setText("");
                } catch (NumberFormatException e) {
                    tfKm.setStyle(errStyle); errKm.setText("⚠ Entier ≥ 0 requis");
                }
            } else { tfKm.setStyle(fldStyle); errKm.setText(""); }
        });
        tfKmLastVisite.textProperty().addListener((obs, o, n) -> {
            if (!n.isBlank()) {
                try {
                    int v = Integer.parseInt(n.trim());
                    if (v < 0) throw new NumberFormatException();
                    tfKmLastVisite.setStyle(fldStyle); errKmVisite.setText("");
                } catch (NumberFormatException e) {
                    tfKmLastVisite.setStyle(errStyle); errKmVisite.setText("⚠ Entier ≥ 0 requis");
                }
            } else { tfKmLastVisite.setStyle(fldStyle); errKmVisite.setText(""); }
        });
        dpDateAchat.valueProperty().addListener((obs, o, n) -> {
            if (n != null && n.isAfter(LocalDate.now())) {
                dpDateAchat.setStyle(errStyle); errDateAchat.setText("⚠ Date d'achat ne peut pas être future");
            } else { dpDateAchat.setStyle(fldStyle); errDateAchat.setText(""); }
        });
        dpProchMaint.valueProperty().addListener((obs, o, n) -> {
            if (n != null && dpDateAchat.getValue() != null && n.isBefore(dpDateAchat.getValue())) {
                dpProchMaint.setStyle(errStyle); errProchMaint.setText("⚠ Doit être après la date d'achat");
            } else { dpProchMaint.setStyle(fldStyle); errProchMaint.setText(""); }
        });

        // ── Grille ─────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(6);
        grid.setPadding(new Insets(16, 20, 10, 20));
        grid.setStyle("-fx-background-color: #F8F9FA;");

        int row = 0;

        // Nom
        grid.add(labelOf("📛 Nom *", lblStyle), 0, row);
        tfNom.setPrefWidth(220); grid.add(tfNom, 1, row++);
        GridPane.setColumnSpan(errNom, 2); grid.add(errNom, 1, row++);

        // Marque + Modèle
        grid.add(labelOf("🏷 Marque *", lblStyle), 0, row);
        tfMarque.setPrefWidth(160); grid.add(tfMarque, 1, row);
        grid.add(labelOf("🔩 Modèle", lblStyle), 2, row);
        tfModele.setPrefWidth(160); grid.add(tfModele, 3, row++);
        GridPane.setColumnSpan(errMarque, 2); grid.add(errMarque, 1, row++);

        // N° Série
        grid.add(labelOf("🔢 N° Série", lblStyle), 0, row);
        tfNumeroSerie.setPrefWidth(220);
        GridPane.setColumnSpan(tfNumeroSerie, 3); grid.add(tfNumeroSerie, 1, row++);

        // État
        grid.add(labelOf("⚙ État *", lblStyle), 0, row);
        GridPane.setColumnSpan(cbEtat, 3); grid.add(cbEtat, 1, row++);

        // Date Achat + Kilométrage
        grid.add(labelOf("📅 Date Achat", lblStyle), 0, row);
        grid.add(dpDateAchat, 1, row);
        grid.add(labelOf("🛣 Kilométrage", lblStyle), 2, row);
        tfKm.setPrefWidth(120); grid.add(tfKm, 3, row++);
        grid.add(errDateAchat, 1, row);
        grid.add(errKm, 3, row++);

        // Dernière visite + Km visite
        grid.add(labelOf("🗓 Dern. Visite", lblStyle), 0, row);
        grid.add(dpLastVisite, 1, row);
        grid.add(labelOf("🛣 Km Visite", lblStyle), 2, row);
        tfKmLastVisite.setPrefWidth(120); grid.add(tfKmLastVisite, 3, row++);
        grid.add(errKmVisite, 3, row++);

        // Prochaine maintenance
        grid.add(labelOf("🔧 Proch. Maint.", lblStyle), 0, row);
        GridPane.setColumnSpan(dpProchMaint, 3); grid.add(dpProchMaint, 1, row++);
        GridPane.setColumnSpan(errProchMaint, 3); grid.add(errProchMaint, 1, row++);

        // CIN — clé étrangère
        grid.add(labelOf("🪪 Propriétaire (CIN) *", lblStyle), 0, row);
        GridPane.setColumnSpan(cbCin, 3); grid.add(cbCin, 1, row++);

        // Légende
        Label legende = new Label("* Champs obligatoires  |  🔴 Rouge = erreur de saisie");
        legende.setStyle("-fx-font-size: 11px; -fx-text-fill: #E74C3C; -fx-font-style: italic;");
        GridPane.setColumnSpan(legende, 4); grid.add(legende, 0, row);

        ColumnConstraints cc = new ColumnConstraints(); cc.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().addAll(new ColumnConstraints(130), cc, new ColumnConstraints(120), cc);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true); scroll.setPrefHeight(480);
        scroll.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(680);
        dialog.getDialogPane().setStyle("-fx-background-color: #F8F9FA;");

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText(modeEdit ? "💾 Enregistrer" : "➕ Ajouter");
        btnOk.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18;");
        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Annuler");
        btnCancel.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18;");

        // ── Validation finale avant fermeture ──────────
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            StringBuilder errors = new StringBuilder();

            if (tfNom.getText().isBlank())
                errors.append("• Le nom est obligatoire.\n");
            if (tfMarque.getText().isBlank())
                errors.append("• La marque est obligatoire.\n");
            if (cbEtat.getValue() == null)
                errors.append("• L'état est obligatoire.\n");
            if (cbCin.getValue() == null)
                errors.append("• Le propriétaire (CIN) est obligatoire.\n");
            if (dpDateAchat.getValue() != null && dpDateAchat.getValue().isAfter(LocalDate.now()))
                errors.append("• La date d'achat ne peut pas être dans le futur.\n");
            if (dpProchMaint.getValue() != null && dpDateAchat.getValue() != null
                    && dpProchMaint.getValue().isBefore(dpDateAchat.getValue()))
                errors.append("• La prochaine maintenance doit être après la date d'achat.\n");
            if (!tfKm.getText().isBlank()) {
                try {
                    int v = Integer.parseInt(tfKm.getText().trim());
                    if (v < 0) throw new NumberFormatException();
                } catch (NumberFormatException e) { errors.append("• Kilométrage : entier ≥ 0 requis.\n"); }
            }
            if (!tfKmLastVisite.getText().isBlank()) {
                try {
                    int v = Integer.parseInt(tfKmLastVisite.getText().trim());
                    if (v < 0) throw new NumberFormatException();
                } catch (NumberFormatException e) { errors.append("• Km visite : entier ≥ 0 requis.\n"); }
            }

            if (errors.length() > 0) {
                evt.consume();
                showErr("Formulaire invalide", errors.toString().trim());
                return;
            }

            // Remplir l'objet machine
            machine.setNom(tfNom.getText().trim());
            machine.setMarque(tfMarque.getText().trim());
            machine.setModele(tfModele.getText().trim());
            machine.setNumeroSerie(tfNumeroSerie.getText().trim());
            machine.setEtatM(cbEtat.getValue());
            machine.setDateAchat(dpDateAchat.getValue());
            machine.setKilometrage(tfKm.getText().isBlank() ? 0 : Integer.parseInt(tfKm.getText().trim()));
            machine.setDateLastVisite(dpLastVisite.getValue());
            machine.setKmLastVisite(tfKmLastVisite.getText().isBlank() ? 0 : Integer.parseInt(tfKmLastVisite.getText().trim()));
            machine.setProchaineMaintenance(dpProchMaint.getValue());
            machine.setCin(cbCin.getValue().getCin());
        });

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? Boolean.TRUE : null);
        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR
    // ══════════════════════════════════════════════════════
    @FXML private void handleMesMaintenances() { nav("/AgricoleAffichageMaintenance.fxml"); }

    private void nav(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) throw new IOException("FXML introuvable : " + path);
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.getScene().setRoot(root); stage.show();
        } catch (IOException e) { showErr("Navigation", "Impossible d'ouvrir : " + path + "\n" + e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  LOGOUT
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion"); a.setHeaderText("Déconnexion");
        a.setContentText("Voulez-vous vraiment vous déconnecter ?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/UsersInterface/Login.fxml"));
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    stage.getScene().setRoot(root); stage.show();
                } catch (IOException e) { showErr("Erreur", "Erreur déconnexion : " + e.getMessage()); }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════
    private String nomMachineParId(int idM) {
        return masterList.stream().filter(m -> m.getIdM() == idM)
                .map(Machine::getNom).findFirst().orElse("Machine #" + idM);
    }

    private String safe(String s)  { return s != null ? s.toLowerCase() : ""; }
    private String safe2(String s) { return s != null ? s : ""; }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val); }

    private Label errLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 11px;");
        return l;
    }

    private VBox creerCarte(String titre, String valeur, String couleur) {
        Label t = new Label(titre); t.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.85);");
        Label v = new Label(valeur); v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        v.setWrapText(true);
        VBox c = new VBox(4, t, v); c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(10, 14, 10, 14));
        c.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    private Label labelOf(String text, String style) { Label l = new Label(text); l.setStyle(style); return l; }
    private TextField styled(TextField tf, String style) { tf.setStyle(style); return tf; }

    private void showInfo(String t, String m) { alert(Alert.AlertType.INFORMATION, t, m); }
    private void showErr (String t, String m) { alert(Alert.AlertType.ERROR,       t, m); }
    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type); a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ══════════════════════════════════════════════════════
    //  SIDEBAR AVATAR
    // ══════════════════════════════════════════════════════
    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;
        if (userNameLabel != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        if (sidebarAvatarImageView != null) { Circle clip = new Circle(35, 35, 35); sidebarAvatarImageView.setClip(clip); }
        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;
        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true); sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) { System.err.println("⚠️ Avatar sidebar : " + e.getMessage()); }
        });
        thread.setDaemon(true); thread.start();
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION AGRICOLE — PAGES
    // ══════════════════════════════════════════════════════
    @FXML void ouvrirTerrains(MouseEvent e)  { chargerPage(e, "/TerrainsInterface/agricoleaffichageterrain.fxml",  "Gestion des Terrains"); }
    @FXML void ouvrirPlantes(MouseEvent e)   { chargerPage(e, "/TerrainsInterface/agricoleaffichageplante.fxml",   "Liste des Plantes"); }
    @FXML void ouvrirRotations(MouseEvent e) { chargerPage(e, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations"); }

    private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root); stage.show();
        } catch (IOException e) { System.err.println("Erreur FXML : " + fxmlPath); e.printStackTrace(); }
    }

    @FXML private void handleMesArticles(MouseEvent e)       { navigateTo(e, "/StocksInterface/AfficherArticleAgr.fxml",       "Articles");   }
    @FXML private void handleMesCatégories(MouseEvent e)     { navigateTo(e, "/StocksInterface/AfficherCategorieAgr.fxml",      "Catégories"); }
    @FXML private void handleDashboardAgricole(MouseEvent e) { navigateTo(e, "/UsersInterface/AcceuillAgr.fxml",                "Dashboard");  }
    @FXML private void handleMesTerrains(MouseEvent e)       { navigateTo(e, "/TerrainsInterface/acceuilagricoleterrain.fxml",  "Terrains");   }
    @FXML private void handleMesAnimaux(MouseEvent e)        { navigateTo(e, "/AnimalsInterface/acceuilagricoleanimaux.fxml",   "Animaux");    }
    @FXML private void handleMesStocks()                     { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel()                   { System.out.println("🚜 Matériel..."); }

    @FXML
    private void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage(); s.setTitle("Mon Profil"); s.setScene(new Scene(root));
            s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen(); s.showAndWait();
        } catch (IOException e) { showError("Erreur " + e.getMessage()); }
    }

    @FXML private void handleMonAbonnement(MouseEvent e) { navigateTo(e, "/UsersInterface/MesAbonnements.fxml", "Mes Abonnements"); }
    @FXML private void handleAPropos(MouseEvent e)       { navigateTo(e, "/UsersInterface/ProfilAgricole.fxml",  "À propos");        }

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root); stage.show();
        } catch (IOException e) { System.err.println("Erreur FXML : " + fxmlPath); e.printStackTrace(); }
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    public Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null) return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameLabel    != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            if (welcomeNameLabel != null) welcomeNameLabel.setText(user.getPrenom() + " !");
            if (userRoleLabel    != null) userRoleLabel.setText("🌾 AGRICULTEUR");
        }
    }

    public void handleMesEvenements(MouseEvent e)  { navigateTo(e, "/G-Evenements/AfficherEvenementsUser.fxml",     "Evenements");    }
    public void ouvrirParticipations(MouseEvent e) { navigateTo(e, "/G-Evenements/AfficherParticipationsUser.fxml", "Participations"); }
}