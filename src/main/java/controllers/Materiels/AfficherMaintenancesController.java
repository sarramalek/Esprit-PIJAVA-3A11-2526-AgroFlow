package controllers.Materiels;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import models.User.Personne;
import org.apache.poi.ss.usermodel.Row;
import com.itextpdf.layout.Document;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.layout.properties.UnitValue;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Materiels.Maintenance;
import models.Materiels.Machine;
import services.Materiels.MaintenanceApiService;
import services.Materiels.MaintenanceService;
import services.Materiels.MachineService;
import utils.MyDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
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

import utils.MyDatabase;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.SessionManager;

import static com.itextpdf.layout.properties.TextAlignment.CENTER;

public class AfficherMaintenancesController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;

    @FXML private TableView<Maintenance> tableMaintenances;
    @FXML private TableColumn<Maintenance, String>  colMachine;
    @FXML private TableColumn<Maintenance, String>  colTypePanne;
    @FXML private TableColumn<Maintenance, String> colDate;
    @FXML private TableColumn<Maintenance, Double>  colCout;
    @FXML private TableColumn<Maintenance, String>  colDescription;
    @FXML private TableColumn<Maintenance, Void>    colActions;

    @FXML private TextField  champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;   // nouveau label dans le FXML

    private MaintenanceService maintenanceService = new MaintenanceService();
    private MachineService     machineService     = new MachineService();

    // Liste maître – jamais filtrée directement
    private final ObservableList<Maintenance> listeMaintenances = FXCollections.observableArrayList();

    // ============================================================
    //  JOINTURE : idM  →  Machine
    //  Chargé UNE SEULE FOIS avant configurationTableau()
    // ============================================================
    private final Map<Integer, Machine> mapMachines = new HashMap<>();

    // ============================================================
    //  INITIALISATION
    // ============================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        maintenanceService = new MaintenanceService();
        machineService     = new MachineService();
        apiService         = new MaintenanceApiService();
        connection         = MyDatabase.getInstance().getConnection();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();

        tableMaintenances.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (lblSelectionInfo != null) {
                lblSelectionInfo.setText(nv != null
                        ? "Sélectionné : " + nomMachineParId(nv.getIdM()) + " — " + nv.getTypePanne()
                        : "Sélectionnez une ligne pour Modifier / Supprimer");
            }
        }); // 4) totaux
    }
    // ── FXML ─────────────────────────────────────────────────────
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

    @FXML private ComboBox<String> comboTypePanne;


    @FXML private Label lblMachineCouteuse;
    @FXML private Label lblPctTotal;
    @FXML private Label lblPctCoutEleve;
    @FXML private Label lblPctSousLaMoyenne;
    @FXML private Label lblPctMachine;
    @FXML private Label lblSelectionInfo;

    // Nouvelle carte statistique
    @FXML private Label lblTypeDominant;
    @FXML private Label lblNbTypeDominant;

    // ── Services ─────────────────────────────────────────────────

    private MaintenanceApiService apiService;
    private Connection connection;

    // ── Données ──────────────────────────────────────────────────
    private final ObservableList<Maintenance> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Maintenance> displayList = FXCollections.observableArrayList();
    private List<Machine> machines = new ArrayList<>();

    private enum SortMode { DATE_DESC, DATE_ASC, COUT_ASC, COUT_DESC, MACHINE_AZ, NONE }
    private SortMode currentSort = SortMode.DATE_DESC;

    // ═══════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════


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
        if (comboMachine != null) {
            comboMachine.getItems().clear();
            comboMachine.getItems().add("Toutes les machines");
            for (Machine m : machines)
                comboMachine.getItems().add(m.getNom());
            comboMachine.getSelectionModel().selectFirst();
        }

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
    //  RECHERCHE DYNAMIQUE
    // ═══════════════════════════════════════════════════════════
    private void configurerRecherche() {
        if (champRecherche != null) {
            // Recherche dynamique : déclenche le filtre à chaque frappe
            champRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
                appliquerFiltres();
            });
        }
    }

    @FXML private void rechercher() { appliquerFiltres(); }
    @FXML private void filtrer()    { appliquerFiltres(); }

    @FXML
    private void effacerRecherche() {
        if (champRecherche != null) champRecherche.clear();
        appliquerFiltres();
    }

    // ── Tri ──────────────────────────────────────────────────────
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

    @FXML private void actualiser() { reinitialiserFiltres(); chargerDonnees(); }

    // ═══════════════════════════════════════════════════════════
    //  FILTRES + TRI
    // ═══════════════════════════════════════════════════════════
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

            // Filtre texte dynamique : cherche dans tous les champs avec .contains()
            boolean matchR = recherche.isEmpty()
                    || nomM.toLowerCase().contains(recherche)
                    || (m.getTypePanne()   != null && m.getTypePanne().toLowerCase().contains(recherche))
                    || (m.getDescription() != null && m.getDescription().toLowerCase().contains(recherche))
                    || String.valueOf(m.getCout()).contains(recherche)
                    || (m.getDateMain()    != null && m.getDateMain().toString().contains(recherche));

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

        long   nbEleves = displayList.stream().filter(m -> m.getCout() > moy).count();
        double pctEleve = nb > 0 ? (nbEleves * 100.0 / nb) : 0;
        long   nbSous   = nb - nbEleves;
        double pctSous  = nb > 0 ? (nbSous * 100.0 / nb) : 0;

        // Machine la plus coûteuse
        Map<Integer, Double> coutParMachine = new HashMap<>();
        for (Maintenance m : displayList)
            coutParMachine.merge(m.getIdM(), m.getCout(), Double::sum);
        int    idMachMax   = coutParMachine.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(-1);
        String nomMachMax  = idMachMax >= 0 ? nomMachineParId(idMachMax) : "-";
        double coutMachMax = idMachMax >= 0 ? coutParMachine.get(idMachMax) : 0;
        double pctMach     = total > 0 ? (coutMachMax * 100.0 / total) : 0;

        // Type de panne dominant
        Map<String, Long> compteParType = new HashMap<>();
        for (Maintenance m : displayList) {
            String t = m.getTypePanne() != null ? m.getTypePanne() : "Inconnu";
            compteParType.merge(t, 1L, Long::sum);
        }
        String typeDominant = compteParType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
        long   nbTypeDom    = compteParType.getOrDefault(typeDominant, 0L);

        // Mise à jour des labels
        if (lblTotal            != null) lblTotal.setText(String.valueOf(nb));
        if (lblCoutTotal        != null) lblCoutTotal.setText(String.format("%.2f DT", total));
        if (lblCoutMoyen        != null) lblCoutMoyen.setText(String.format("%.2f DT", moy));
        if (lblMachineCouteuse  != null) lblMachineCouteuse.setText(nomMachMax);
        if (lblPctTotal         != null) lblPctTotal.setText("100%");
        if (lblPctCoutEleve     != null) lblPctCoutEleve.setText(String.format("%.0f%% élevés", pctEleve));
        if (lblPctSousLaMoyenne != null) lblPctSousLaMoyenne.setText(String.format("%.0f%% sous moy.", pctSous));
        if (lblPctMachine       != null) lblPctMachine.setText(String.format("%.0f%% du total", pctMach));
        if (lblTypeDominant     != null) lblTypeDominant.setText(typeDominant);
        if (lblNbTypeDominant   != null) lblNbTypeDominant.setText(nbTypeDom + " occurrence(s)");
    }

    // ═══════════════════════════════════════════════════════════
    //  CRUD
    // ═══════════════════════════════════════════════════════════
    @FXML private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/AjouterMaintenance.fxml"));
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

    @FXML private void modifierSelection() {
        Maintenance sel = tableMaintenances.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Sélection", "Sélectionnez une maintenance à modifier."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/ModifierMaintenance.fxml"));
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
    //  STATISTIQUES
    // ═══════════════════════════════════════════════════════════
    @FXML private void afficherStatistiques() {
        if (displayList.isEmpty()) { showInfo("Stats", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques des Maintenances");
        stage.setResizable(true);

        Map<String, Double>  coutMap = new LinkedHashMap<>();
        Map<String, Integer> nbMap   = new LinkedHashMap<>();
        for (Maintenance m : displayList) {
            String nom = nomMachineParId(m.getIdM());
            coutMap.merge(nom, m.getCout(), Double::sum);
            nbMap.merge(nom, 1, Integer::sum);
        }

        Map<String, Long> typeMap = new LinkedHashMap<>();
        for (Maintenance m : displayList) {
            String t = m.getTypePanne() != null ? m.getTypePanne() : "Inconnu";
            typeMap.merge(t, 1L, Long::sum);
        }

        List<String>  machKeys = new ArrayList<>(coutMap.keySet());
        List<Double>  machVals = new ArrayList<>(coutMap.values());
        List<String>  typeKeys = new ArrayList<>(typeMap.keySet());
        List<Long>    typeVals = new ArrayList<>(typeMap.values());

        int barW = 55, gap = 25, padL = 75, padTop = 50, chartH = 250;
        int nM = machKeys.size(), nT = typeKeys.size();

        int cwL = padL + nM * (barW + gap) + gap + 20;
        int chL = chartH + 90 + padTop;
        Canvas canvasL = new Canvas(cwL, chL);
        GraphicsContext gl = canvasL.getGraphicsContext2D();

        gl.setFill(Color.WHITE); gl.fillRect(0, 0, cwL, chL);
        gl.setFill(Color.web("#2c3e50"));
        gl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gl.fillText("💰 Coût total par machine (DT)", padL, 30);

        double maxCout = machVals.stream().mapToDouble(v -> v).max().orElse(1);
        double coutMoyenGlobal = displayList.stream().mapToDouble(Maintenance::getCout).average().orElse(0);

        Color[] colors = { Color.web("#3498db"), Color.web("#27ae60"), Color.web("#e74c3c"),
                Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63") };

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
            double x = padL + gap + i * (barW + gap);
            double y = padTop + chartH - barH;
            Color c = colors[i % colors.length];
            gl.setFill(Color.rgb(0, 0, 0, 0.07));
            gl.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);
            gl.setFill(c);
            gl.fillRoundRect(x, y, barW, barH, 6, 6);
            gl.setFill(Color.web("#2c3e50"));
            gl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.format("%.0f", machVals.get(i));
            gl.fillText(vs, x + barW / 2.0 - vs.length() * 3.5, y - 6);
            gl.setFont(Font.font("Arial", 10));
            String lbl = machKeys.get(i).length() > 11 ? machKeys.get(i).substring(0, 11) + "…" : machKeys.get(i);
            gl.fillText(lbl, x + barW / 2.0 - lbl.length() * 3, padTop + chartH + 16);
        }

        double yMoy = padTop + chartH - (coutMoyenGlobal / maxCout) * chartH;
        gl.setStroke(Color.web("#e74c3c")); gl.setLineWidth(2);
        gl.setLineDashes(8, 4);
        gl.strokeLine(padL, yMoy, cwL - 20, yMoy);
        gl.setLineDashes();
        gl.setFill(Color.web("#e74c3c"));
        gl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gl.fillText(String.format("Moy. %.0f DT", coutMoyenGlobal), cwL - 80, yMoy - 4);

        int cwR = 400;
        int chR = padTop + 40 + nT * 42 + 20;
        Canvas canvasR = new Canvas(cwR, Math.max(chR, chL));
        GraphicsContext gr = canvasR.getGraphicsContext2D();

        gr.setFill(Color.WHITE); gr.fillRect(0, 0, cwR, Math.max(chR, chL));
        gr.setFill(Color.web("#2c3e50"));
        gr.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gr.fillText("🔧 Répartition par type de panne", 10, 30);

        long maxType = typeVals.stream().mapToLong(v -> v).max().orElse(1);
        int barAreaW = cwR - 160;

        for (int i = 0; i < nT; i++) {
            double bH = 24;
            double y  = padTop + i * 42;
            double bW = (typeVals.get(i) / (double) maxType) * barAreaW;
            Color  c  = colors[i % colors.length];
            gr.setFill(Color.rgb(0, 0, 0, 0.06));
            gr.fillRoundRect(153, y + 3, bW, bH, 6, 6);
            gr.setFill(c);
            gr.fillRoundRect(150, y, bW, bH, 6, 6);
            gr.setFill(Color.web("#2c3e50"));
            gr.setFont(Font.font("Arial", 11));
            String lbl = typeKeys.get(i).length() > 14 ? typeKeys.get(i).substring(0, 14) + "…" : typeKeys.get(i);
            gr.fillText(lbl, 5, y + 17);
            gr.setFill(Color.web("#2c3e50"));
            gr.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gr.fillText(String.valueOf(typeVals.get(i)), 150 + bW + 6, y + 17);
        }

        ScrollPane spL = new ScrollPane(canvasL); spL.setFitToHeight(true);
        spL.setPrefSize(Math.min(cwL + 20, 700), chL + 20);
        ScrollPane spR = new ScrollPane(canvasR); spR.setFitToHeight(true);
        spR.setPrefWidth(cwR + 20);

        HBox charts = new HBox(16, spL, spR);
        charts.setPadding(new Insets(10));

        Label legende = new Label("  ── Ligne rouge pointillée = coût moyen global par maintenance");
        legende.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox root = new VBox(8, legende, charts);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════
    //  API — COÛT PAR MACHINE
    // ═══════════════════════════════════════════════════════════
    @FXML private void ouvrirCoutMachine() {
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
                    .setFontSize(18).setBold().setTextAlignment(CENTER));
            doc.add(new Paragraph("Généré le : " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setTextAlignment(CENTER));
            doc.add(new Paragraph("\n"));

            float[] colW = {100f, 110f, 70f, 70f, 180f};
            Table table  = new Table(UnitValue.createPointArray(colW));
            table.setWidth(UnitValue.createPercentValue(100));

            for (String h : new String[]{"Machine", "Type Panne", "Date", "Coût (DT)", "Description"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(CENTER));
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
    //  NAVIGATION
    // ═══════════════════════════════════════════════════════════

    @FXML private void retourAccueil()      { nav("/MaterielsInterface/AccueilMateriel.fxml"); }



    private void nav(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) tableMaintenances.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
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

    private void showErr (String t, String m) { alert(Alert.AlertType.ERROR,       t, m); }
    private void showWarn(String t, String m) { alert(Alert.AlertType.WARNING,     t, m); }

    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    //navigguer vers les autres modules

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}
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