package controllers.User;

import com.itextpdf.layout.element.Cell;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Abonnements;
import models.User.offres;
import models.User.Personne;
import services.User.AbonnementService;
import services.User.OffresServicees;
import utils.SessionManager;

// iText 7
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MesAbonnements {

    // ══════════════════════════════════════════════════════════════
    // FXML Components
    // ══════════════════════════════════════════════════════════════

    @FXML private AnchorPane rootPane;
    @FXML private Label      userNameLabel, userNameLabel1, welcomeNameLabel, userRoleLabel;
    @FXML private Hyperlink  aproposLink;
    @FXML private Button     logoutBtn;

    // Containers
    @FXML private VBox  mesAbonnementsContainer;
    @FXML private VBox  offresContainer;
    @FXML private Label noAbonnementLabel;
    @FXML private Label noOffresLabel;

    // ── Recherche / filtre / tri — Abonnements ──
    @FXML private TextField        searchAbonnementField;
    @FXML private ComboBox<String> filterSituationCombo;
    @FXML private ComboBox<String> sortAbonnementCombo;
    @FXML private Label            countAbonnementLabel;

    // ── Recherche / tri — Offres ──
    @FXML private TextField        searchOffreField;
    @FXML private ComboBox<String> sortOffreCombo;
    @FXML private Label            countOffreLabel;

    // Dialog souscription
    @FXML private StackPane  dialogOverlay;
    @FXML private Label      dialogTitleLabel, dialogDescriptionLabel, dialogPrixLabel, dialogDureeLabel;
    @FXML private DatePicker dateDebutPicker;
    @FXML private Button     souscrireButton;
    @FXML private VBox       dialogContent;

    // Menu gestion
    @FXML private VBox   gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;

    // ══════════════════════════════════════════════════════════════
    // État interne
    // ══════════════════════════════════════════════════════════════

    private AbonnementService abonnementService;
    private OffresServicees   offreService;
    private Personne          currentUser;
    private offres            selectedOffre;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Listes maîtresses — jamais mutées, filtrées/triées à la demande */
    private List<Abonnements> allAbonnements = new ArrayList<>();
    private List<offres>      allOffres      = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        System.out.println("✓ MesAbonnements Controller initialisé");

        // Récupérer le user depuis SessionManager
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser SessionManager: " + currentUser.getNom());
            updateUserLabels(currentUser);
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }

        try {
            abonnementService = new AbonnementService();
            offreService      = new OffresServicees();

            if (dateDebutPicker != null) dateDebutPicker.setValue(LocalDate.now());

            setupSearchAndFilters();

            if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
            if (gestionBtn != null && gestionContainer != null) {
                gestionBtn.setOnMouseEntered(e -> toggleMenu(true));
                gestionContainer.setOnMouseEntered(e -> toggleMenu(true));
                gestionContainer.setOnMouseExited(e  -> toggleMenu(false));
            }

            if (currentUser != null) { loadMesAbonnements(); loadOffresDisponibles(); }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SETUP RECHERCHE / FILTRE / TRI
    // ══════════════════════════════════════════════════════════════

    private void setupSearchAndFilters() {

        // ── Abonnements ──────────────────────────────────────────
        if (filterSituationCombo != null) {
            filterSituationCombo.setItems(FXCollections.observableArrayList(
                    "Tous", "actif", "expiré", "en attente"));
            filterSituationCombo.setValue("Tous");
            filterSituationCombo.setOnAction(e -> applyAbonnementFilters());
        }
        if (sortAbonnementCombo != null) {
            sortAbonnementCombo.setItems(FXCollections.observableArrayList(
                    "Date inscription ↓", "Date inscription ↑",
                    "Date expiration ↓",  "Date expiration ↑",
                    "Situation A→Z"));
            sortAbonnementCombo.setValue("Date inscription ↓");
            sortAbonnementCombo.setOnAction(e -> applyAbonnementFilters());
        }
        if (searchAbonnementField != null)
            searchAbonnementField.textProperty().addListener((obs, o, n) -> applyAbonnementFilters());

        // ── Offres ───────────────────────────────────────────────
        if (sortOffreCombo != null) {
            sortOffreCombo.setItems(FXCollections.observableArrayList(
                    "Nom A→Z", "Nom Z→A",
                    "Prix ↑",  "Prix ↓",
                    "Durée ↑", "Durée ↓"));
            sortOffreCombo.setValue("Nom A→Z");
            sortOffreCombo.setOnAction(e -> applyOffreFilters());
        }
        if (searchOffreField != null)
            searchOffreField.textProperty().addListener((obs, o, n) -> applyOffreFilters());
    }

    // ══════════════════════════════════════════════════════════════
    // LOGIQUE FILTRE + TRI
    // ══════════════════════════════════════════════════════════════

    private void applyAbonnementFilters() {
        if (allAbonnements.isEmpty()) { renderAbonnements(new ArrayList<>()); return; }

        String search = searchAbonnementField != null ? searchAbonnementField.getText().toLowerCase().trim() : "";
        String sit    = filterSituationCombo   != null ? filterSituationCombo.getValue()   : "Tous";
        String sort   = sortAbonnementCombo    != null ? sortAbonnementCombo.getValue()    : "Date inscription ↓";

        // 1 — Filtrage
        List<Abonnements> result = allAbonnements.stream()
                .filter(a -> {
                    boolean ms = search.isEmpty()
                            || String.valueOf(a.getId_abonn()).contains(search)
                            || String.valueOf(a.getCin()).contains(search)
                            || (a.getSituation()        != null && a.getSituation().toLowerCase().contains(search))
                            || (a.getDate_inscription() != null && a.getDate_inscription().contains(search))
                            || (a.getDate_expiration()  != null && a.getDate_expiration().contains(search));
                    boolean mf = sit == null || sit.equals("Tous") || sit.equalsIgnoreCase(a.getSituation());
                    return ms && mf;
                })
                .collect(Collectors.toList());

        // 2 — Tri
        Comparator<Abonnements> cmp = switch (sort != null ? sort : "") {
            case "Date inscription ↑" -> Comparator.comparing(a -> safe(a.getDate_inscription()));
            case "Date expiration ↓"  -> Comparator.comparing((Abonnements a) -> safe(a.getDate_expiration())).reversed();
            case "Date expiration ↑"  -> Comparator.comparing(a -> safe(a.getDate_expiration()));
            case "Situation A→Z"      -> Comparator.comparing(a -> safe(a.getSituation()));
            default                   -> Comparator.comparing((Abonnements a) -> safe(a.getDate_inscription())).reversed();
        };
        result.sort(cmp);

        renderAbonnements(result);
    }

    private void applyOffreFilters() {
        if (allOffres.isEmpty()) { renderOffres(new ArrayList<>()); return; }

        String search = searchOffreField != null ? searchOffreField.getText().toLowerCase().trim() : "";
        String sort   = sortOffreCombo   != null ? sortOffreCombo.getValue() : "Nom A→Z";

        // 1 — Filtrage
        List<offres> result = allOffres.stream()
                .filter(o -> search.isEmpty()
                        || (o.getNom_offre()   != null && o.getNom_offre().toLowerCase().contains(search))
                        || (o.getDescription() != null && o.getDescription().toLowerCase().contains(search))
                        || String.valueOf(o.getPrix()).contains(search))
                .collect(Collectors.toList());

        // 2 — Tri
        Comparator<offres> cmp = switch (sort != null ? sort : "") {
            case "Nom Z→A"  -> Comparator.comparing((offres o) -> safe(o.getNom_offre())).reversed();
            case "Prix ↑"   -> Comparator.comparingDouble(offres::getPrix);
            case "Prix ↓"   -> Comparator.comparingDouble((offres o) -> o.getPrix()).reversed();
            case "Durée ↑"  -> Comparator.comparingInt(offres::getDuree_offre);
            case "Durée ↓"  -> Comparator.comparingInt((offres o) -> o.getDuree_offre()).reversed();
            default         -> Comparator.comparing(o -> safe(o.getNom_offre()));
        };
        result.sort(cmp);

        renderOffres(result);
    }

    // ══════════════════════════════════════════════════════════════
    // GESTION DU USER
    // ══════════════════════════════════════════════════════════════

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            SessionManager.setCurrentUser(user);
            System.out.println("✓ setCurrentUser MesAbonnements: " + user.getNom());
            updateUserLabels(user);
            loadMesAbonnements();
            loadOffresDisponibles();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    private void updateUserLabels(Personne user) {
        String full = user.getPrenom() + " " + user.getNom();
        if (userNameLabel    != null) userNameLabel.setText("Utilisateur: " + full);
        if (userNameLabel1   != null) userNameLabel1.setText(full);
        if (welcomeNameLabel != null) welcomeNameLabel.setText(user.getPrenom() + " !");
        if (userRoleLabel    != null) userRoleLabel.setText("🌾 AGRICULTEUR");
    }

    // ══════════════════════════════════════════════════════════════
    // CHARGEMENT DES DONNÉES
    // ══════════════════════════════════════════════════════════════

    private void loadMesAbonnements() {
        if (currentUser == null || mesAbonnementsContainer == null) return;
        try {
            allAbonnements = abonnementService.getAbonnementsByUser(currentUser.getCin());
            applyAbonnementFilters();
            System.out.println("✓ " + allAbonnements.size() + " abonnements chargés");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos abonnements: " + e.getMessage());
        }
    }

    private void loadOffresDisponibles() {
        if (offresContainer == null) return;
        try {
            allOffres = offreService.recuperer();
            applyOffreFilters();
            System.out.println("✓ " + allOffres.size() + " offres chargées");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les offres: " + e.getMessage());
        }
    }

    @FXML
    private void onRefreshOffres() {
        loadOffresDisponibles();
        loadMesAbonnements();
    }

    // ══════════════════════════════════════════════════════════════
    // RENDU DES LISTES
    // ══════════════════════════════════════════════════════════════

    private void renderAbonnements(List<Abonnements> list) {
        mesAbonnementsContainer.getChildren().clear();
        if (countAbonnementLabel != null)
            countAbonnementLabel.setText(list.size() + " abonnement(s)");

        boolean empty = list.isEmpty();
        if (noAbonnementLabel != null) { noAbonnementLabel.setVisible(empty); noAbonnementLabel.setManaged(empty); }
        if (!empty)
            for (Abonnements a : list)
                mesAbonnementsContainer.getChildren().add(buildAbonnementCard(a));
    }

    private void renderOffres(List<offres> list) {
        offresContainer.getChildren().clear();
        if (countOffreLabel != null)
            countOffreLabel.setText(list.size() + " offre(s)");

        boolean empty = list.isEmpty();
        if (noOffresLabel != null) { noOffresLabel.setVisible(empty); noOffresLabel.setManaged(empty); }
        if (!empty)
            for (offres o : list)
                offresContainer.getChildren().add(buildOffreCard(o));
    }

    // ══════════════════════════════════════════════════════════════
    // CONSTRUCTION DES CARTES
    // ══════════════════════════════════════════════════════════════

    private VBox buildAbonnementCard(Abonnements abo) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:#f1f8e9;-fx-background-radius:8;-fx-padding:15;" +
                "-fx-border-color:#aed581;-fx-border-width:2;-fx-border-radius:8;");
        try {
            offres offre = offreService.rechercherParId(abo.getId_offre());
            if (offre != null) fillAbonnementCard(card, abo, offre);
            else               card.getChildren().add(errLabel("Offre introuvable (ID: " + abo.getId_offre() + ")"));
        } catch (SQLException e) { card.getChildren().add(errLabel("Erreur chargement")); }
        return card;
    }

    private void fillAbonnementCard(VBox card, Abonnements abo, offres offre) {

        // Nom offre
        Label nom = new Label(offre.getNom_offre());
        nom.setFont(Font.font("System", FontWeight.BOLD, 16));
        nom.setTextFill(Color.web("#2e7d32"));

        // Description
        Label desc = new Label(offre.getDescription());
        desc.setWrapText(true); desc.setFont(Font.font(12));

        // Prix
        Label prix = new Label(String.format("Prix : %.2f DT / mois", offre.getPrix()));
        prix.setFont(Font.font("System", FontWeight.BOLD, 14));
        prix.setTextFill(Color.web("#1b5e20"));

        // Dates
        HBox dates = new HBox(20);
        dates.getChildren().addAll(
                new Label("📅 Début : " + nvl(abo.getDate_inscription())),
                new Label("⏰ Fin : "   + nvl(abo.getDate_expiration())));

        // Badge statut
        Label badge = situationBadge(abo.getSituation());

        card.getChildren().addAll(nom, desc, prix, dates, badge);

        // Jours restants si actif
        if ("actif".equalsIgnoreCase(abo.getSituation()))
            addJoursRestants(card, abo.getDate_expiration());

        // ── Actions ──────────────────────────────────────────────
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        // PDF individuel
        Button pdfBtn = new Button("📄 Télécharger PDF");
        pdfBtn.setStyle("-fx-background-color:#1565C0;-fx-text-fill:white;-fx-cursor:hand;" +
                "-fx-padding:6 14;-fx-background-radius:6;-fx-font-size:12;");
        pdfBtn.setOnAction(e -> generateSinglePDF(abo, offre));
        actions.getChildren().add(pdfBtn);

        // Renouveler si expiré
        if ("expiré".equalsIgnoreCase(abo.getSituation())) {
            Button ren = new Button("🔄 Renouveler");
            ren.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-cursor:hand;" +
                    "-fx-padding:6 14;-fx-background-radius:6;");
            ren.setOnAction(e -> showOffreDialog(offre));
            actions.getChildren().add(ren);
        }
        card.getChildren().add(actions);
    }

    private Label situationBadge(String situation) {
        if (situation == null) situation = "inconnu";
        Label b = new Label(situation.toUpperCase());
        b.setFont(Font.font("System", FontWeight.BOLD, 12));
        b.setPadding(new Insets(4, 12, 4, 12));
        String bg = switch (situation.toLowerCase()) {
            case "actif"  -> "#4caf50";
            case "expiré" -> "#f44336";
            default       -> "#ff9800";
        };
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:15;");
        return b;
    }

    private void addJoursRestants(VBox card, String dateExp) {
        try {
            long j = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(dateExp, DATE_FMT));
            Label l = new Label("⏳ Jours restants : " + j);
            l.setFont(Font.font("System", FontWeight.BOLD, 12));
            l.setTextFill(Color.web(j < 7 ? "#d32f2f" : "#388e3c"));
            card.getChildren().add(l);
        } catch (Exception ignored) {}
    }

    private VBox buildOffreCard(offres offre) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-padding:20;" +
                "-fx-border-color:#e0e0e0;-fx-border-width:1;-fx-border-radius:10;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,2);");

        Label nom = new Label(offre.getNom_offre());
        nom.setFont(Font.font("System", FontWeight.BOLD, 18));
        nom.setTextFill(Color.web("#2e7d32"));

        Label desc = new Label(offre.getDescription());
        desc.setWrapText(true); desc.setFont(Font.font(13));
        desc.setTextFill(Color.web("#666")); desc.setMaxHeight(55);

        Label prix = new Label(String.format("%.2f DT / mois", offre.getPrix()));
        prix.setFont(Font.font("System", FontWeight.BOLD, 26));
        prix.setTextFill(Color.web("#1b5e20"));

        HBox meta = new HBox(24);
        meta.getChildren().addAll(
                new Label("⏱️ Durée : " + offre.getDuree_offre() + " mois"),
                new Label("💰 Total : " + String.format("%.2f DT", offre.getPrix() * offre.getDuree_offre())));

        Button btn = new Button("📝 Souscrire à cette offre");
        btn.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-cursor:hand;" +
                "-fx-padding:10 20;-fx-font-weight:bold;-fx-background-radius:8;");
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setOnAction(e -> showOffreDialog(offre));

        card.getChildren().addAll(nom, desc, prix, meta, btn);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // PDF — abonnement individuel
    // ══════════════════════════════════════════════════════════════

    private void generateSinglePDF(Abonnements abo, offres offre) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialFileName("abonnement_" + abo.getId_abonn() + "_"
                + offre.getNom_offre().replaceAll("\\s+", "_") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        desktopDir(fc);

        File f = fc.showSaveDialog(getStage());
        if (f == null) return;
        try { buildSinglePDF(abo, offre, f.getAbsolutePath()); showPdfSuccess(f); }
        catch (Exception e) { showError("Erreur PDF", e.getMessage()); e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // PDF — tous les abonnements
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleGeneratePDFTous() {
        if (allAbonnements.isEmpty()) {
            showError("Aucun abonnement", "Vous n'avez aucun abonnement à exporter.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("rapport_abonnements_"
                + (currentUser != null ? currentUser.getNom() : "user") + "_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        desktopDir(fc);

        File f = fc.showSaveDialog(getStage());
        if (f == null) return;
        try { buildAllPDF(allAbonnements, f.getAbsolutePath()); showPdfSuccess(f); }
        catch (Exception e) { showError("Erreur PDF", e.getMessage()); e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════════════
    // PDF Builders
    // ══════════════════════════════════════════════════════════════

    private void buildSinglePDF(Abonnements abo, offres offre, String path) throws Exception {
        DeviceRgb GREEN = rgb(22,90,22), WHITE = rgb(255,255,255),
                LIGHT = rgb(232,245,233), DARK = rgb(30,30,30), MUTED = rgb(120,120,120);
        String sit = nvl(abo.getSituation());
        DeviceRgb SIT = sitRgb(sit);

        try (Document doc = doc(path, PageSize.A4, 40,50,40,50)) {
            PdfFont B = bold(), R = reg();

            // Header banderole
            doc.add(headerBand("ATTESTATION D'ABONNEMENT", B, GREEN, WHITE));

            // Badge statut
            Table badge = tbl(new float[]{100}, 100);
            badge.addCell(cell(p("STATUT : " + sit.toUpperCase(), B, 13, WHITE, TextAlignment.CENTER), SIT, 10));
            doc.add(badge);
            doc.add(sp());

            // Titre offre
            doc.add(p(offre.getNom_offre(), B, 22, GREEN, TextAlignment.CENTER).setMarginBottom(4));
            if (offre.getDescription() != null)
                doc.add(p(offre.getDescription(), R, 11, MUTED, TextAlignment.CENTER).setMarginBottom(14));

            // Détails abonnement
            doc.add(secTitle("Détails de l'abonnement", B, GREEN));
            Table t = tbl(new float[]{45,55}, 100);
            row(t,B,R, "N° Abonnement",      "#" + abo.getId_abonn(),                          LIGHT, DARK);
            row(t,B,R, "Offre souscrite",     offre.getNom_offre(),                             WHITE, DARK);
            row(t,B,R, "Prix mensuel",        f2(offre.getPrix()) + " DT",                     LIGHT, DARK);
            row(t,B,R, "Durée",               offre.getDuree_offre() + " mois",                WHITE, DARK);
            row(t,B,R, "Montant total",       f2(offre.getPrix() * offre.getDuree_offre()) + " DT", LIGHT, DARK);
            row(t,B,R, "Date d'inscription",  nvl(abo.getDate_inscription()),                  WHITE, DARK);
            row(t,B,R, "Date d'expiration",   nvl(abo.getDate_expiration()),                   LIGHT, DARK);
            row(t,B,R, "Situation",           sit.toUpperCase(),                               WHITE, DARK);
            if ("actif".equalsIgnoreCase(sit) && abo.getDate_expiration() != null) {
                try {
                    long j = ChronoUnit.DAYS.between(LocalDate.now(),
                            LocalDate.parse(abo.getDate_expiration(), DATE_FMT));
                    row(t,B,R, "Jours restants", j + " jour(s)", LIGHT, DARK);
                } catch (Exception ignored) {}
            }
            doc.add(t);
            doc.add(sp());

            // Souscripteur
            if (currentUser != null) {
                doc.add(secTitle("Souscripteur", B, GREEN));
                Table ts = tbl(new float[]{45,55}, 100);
                row(ts,B,R, "Nom complet", currentUser.getPrenom() + " " + currentUser.getNom(), LIGHT, DARK);
                row(ts,B,R, "CIN",         String.valueOf(currentUser.getCin()),                 WHITE, DARK);
                row(ts,B,R, "Email",       nvl(currentUser.getEmail()),                          LIGHT, DARK);
                doc.add(ts);
                doc.add(sp());
            }
            doc.add(footer(R, MUTED));
        }
    }

    private void buildAllPDF(List<Abonnements> list, String path) throws Exception {
        DeviceRgb GREEN = rgb(22,90,22), WHITE = rgb(255,255,255),
                LIGHT = rgb(232,245,233), DARK = rgb(30,30,30),
                MUTED = rgb(120,120,120), HEAD = rgb(46,125,50);

        try (Document doc = doc(path, PageSize.A4, 40,40,40,40)) {
            PdfFont B = bold(), R = reg();

            doc.add(headerBand("RAPPORT MES ABONNEMENTS", B, GREEN, WHITE));
            doc.add(sp());

            // Souscripteur
            if (currentUser != null) {
                doc.add(secTitle("Souscripteur", B, GREEN));
                Table ts = tbl(new float[]{45,55}, 100);
                row(ts,B,R, "Nom complet", currentUser.getPrenom() + " " + currentUser.getNom(), LIGHT, DARK);
                row(ts,B,R, "CIN",         String.valueOf(currentUser.getCin()),                 WHITE, DARK);
                row(ts,B,R, "Email",       nvl(currentUser.getEmail()),                          LIGHT, DARK);
                doc.add(ts); doc.add(sp());
            }

            // Stats résumé
            long actifs  = list.stream().filter(a -> "actif".equalsIgnoreCase(a.getSituation())).count();
            long expires = list.stream().filter(a -> "expiré".equalsIgnoreCase(a.getSituation())).count();
            long attente = list.size() - actifs - expires;

            doc.add(secTitle("Résumé", B, GREEN));
            Table stats = tbl(new float[]{25,25,25,25}, 100);
            statCell(stats, B, "TOTAL",      String.valueOf(list.size()), rgb(21,101,192), WHITE);
            statCell(stats, B, "ACTIFS",     String.valueOf(actifs),     rgb(56,142,60),  WHITE);
            statCell(stats, B, "EXPIRÉS",    String.valueOf(expires),    rgb(211,47,47),  WHITE);
            statCell(stats, B, "EN ATTENTE", String.valueOf(attente),    rgb(245,124,0),  WHITE);
            doc.add(stats); doc.add(sp());

            // Tableau détaillé
            doc.add(secTitle("Détail de tous les abonnements", B, GREEN));
            Table table = tbl(new float[]{7,24,15,15,13,13,13}, 100);
            for (String h : new String[]{"#","Offre","Début","Fin","Statut","Prix/mois","Total"})
                table.addHeaderCell(cell(p(h, B, 9, WHITE, null), HEAD, 7));

            boolean alt = false;
            for (Abonnements a : list) {
                DeviceRgb bg = alt ? LIGHT : WHITE; alt = !alt;
                String sit  = nvl(a.getSituation());
                String nom  = "—", prix = "—", total = "—";
                try {
                    offres o = offreService.rechercherParId(a.getId_offre());
                    if (o != null) {
                        nom   = o.getNom_offre();
                        prix  = f2(o.getPrix())  + " DT";
                        total = f2(o.getPrix() * o.getDuree_offre()) + " DT";
                    }
                } catch (Exception ignored) {}

                table.addCell(tc(String.valueOf(a.getId_abonn()), R, bg, DARK));
                table.addCell(tc(nom,                              R, bg, DARK));
                table.addCell(tc(nvl(a.getDate_inscription()),     R, bg, DARK));
                table.addCell(tc(nvl(a.getDate_expiration()),      R, bg, DARK));
                table.addCell(cell(p(sit.toUpperCase(), B, 8, sitRgb(sit), null), bg, 6));
                table.addCell(tc(prix,  R, bg, DARK));
                table.addCell(tc(total, R, bg, DARK));
            }
            doc.add(table); doc.add(sp());
            doc.add(footer(R, MUTED));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Micro-helpers PDF
    // ══════════════════════════════════════════════════════════════

    private Document doc(String path, PageSize ps, float t, float r, float b, float l) throws Exception {
        Document d = new Document(new PdfDocument(new PdfWriter(path)), ps);
        d.setMargins(t, r, b, l); return d;
    }
    private PdfFont bold() throws Exception { return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD); }
    private PdfFont reg()  throws Exception { return PdfFontFactory.createFont(StandardFonts.HELVETICA); }
    private DeviceRgb rgb(int r, int g, int b) { return new DeviceRgb(r, g, b); }
    private DeviceRgb sitRgb(String s) {
        return switch (s.toLowerCase()) {
            case "actif"  -> rgb(56,142,60);
            case "expiré" -> rgb(211,47,47);
            default       -> rgb(245,124,0);
        };
    }
    private Table tbl(float[] cols, float pct) {
        return new Table(UnitValue.createPercentArray(cols))
                .setWidth(UnitValue.createPercentValue(pct));
    }
    private Cell cell(Paragraph content, DeviceRgb bg, float pad) {
        Cell c = new Cell().add(content).setPadding(pad).setBorder(Border.NO_BORDER);
        if (bg != null) c.setBackgroundColor(bg); return c;
    }
    private Cell tc(String v, PdfFont f, DeviceRgb bg, DeviceRgb fg) {
        return cell(p(v, f, 9, fg, null), bg, 6);
    }
    private Paragraph p(String text, PdfFont f, float size, DeviceRgb color, TextAlignment align) {
        Paragraph para = new Paragraph(text).setFont(f).setFontSize(size);
        if (color != null) para.setFontColor(color);
        if (align != null) para.setTextAlignment(align);
        return para;
    }
    private void row(Table t, PdfFont B, PdfFont R, String label, String value, DeviceRgb bg, DeviceRgb fg) {
        t.addCell(cell(p(label, B, 10, fg, null), bg, 8));
        t.addCell(cell(p(value, R, 10, fg, null), bg, 8));
    }
    private void statCell(Table t, PdfFont B, String label, String value, DeviceRgb bg, DeviceRgb fg) {
        t.addCell(cell(p(value + "\n" + label, B, 13, fg, TextAlignment.CENTER), bg, 14));
    }
    private Table headerBand(String title, PdfFont B, DeviceRgb bg, DeviceRgb fg) {
        Table h = tbl(new float[]{38,62}, 100).setBackgroundColor(bg);
        h.addCell(cell(p("🌱 AgroFlow",  B, 18, fg, null),                   null, 16));
        h.addCell(cell(p(title,          B, 13, fg, TextAlignment.RIGHT),     null, 16));
        return h;
    }
    private Paragraph secTitle(String text, PdfFont B, DeviceRgb color) {
        return p(text, B, 13, color, null)
                .setBorderBottom(new SolidBorder(color, 2)).setPaddingBottom(4).setMarginBottom(8);
    }
    private Paragraph footer(PdfFont R, DeviceRgb color) {
        return p("Document généré le "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                + "  |  AgroFlow – Plateforme de Gestion Agricole", R, 8, color, TextAlignment.CENTER)
                .setBorderTop(new SolidBorder(color, 0.5f)).setPaddingTop(8);
    }
    private Paragraph sp() { return new Paragraph(" "); }
    private String f2(double v) { return String.format("%.2f", v); }

    // ══════════════════════════════════════════════════════════════
    // DIALOG SOUSCRIPTION (code original conservé + SessionManager)
    // ══════════════════════════════════════════════════════════════

    private void showOffreDialog(offres offre) {
        if (dialogOverlay == null) return;
        this.selectedOffre = offre;
        dialogTitleLabel.setText(offre.getNom_offre());
        dialogDescriptionLabel.setText(offre.getDescription());
        dialogPrixLabel.setText(f2(offre.getPrix()) + " DT");
        dialogDureeLabel.setText(offre.getDuree_offre() + " mois");
        dateDebutPicker.setValue(LocalDate.now());
        dialogOverlay.setVisible(true);
        dialogOverlay.setManaged(true);
    }

    @FXML
    private void onCloseDialog() {
        if (dialogOverlay != null) { dialogOverlay.setVisible(false); dialogOverlay.setManaged(false); }
        selectedOffre = null;
    }

    @FXML
    private void onSouscrire() {
        if (currentUser == null || selectedOffre == null) { showError("Erreur", "Session ou offre invalide"); return; }
        LocalDate debut = dateDebutPicker.getValue();
        if (debut == null) { showError("Erreur", "Veuillez sélectionner une date de début"); return; }
        try {
            LocalDate fin = debut.plusMonths(selectedOffre.getDuree_offre());
            Abonnements a = new Abonnements();
            a.setCin(currentUser.getCin());
            a.setId_offre(selectedOffre.getId_offres());
            a.setDate_inscription(debut.format(DATE_FMT));
            a.setDate_expiration(fin.format(DATE_FMT));
            a.setSituation("actif");
            abonnementService.ajouter(a);
            onCloseDialog();
            loadMesAbonnements();
            showSuccess("Succès", "Abonnement à \"" + selectedOffre.getNom_offre() + "\" créé !");
        } catch (SQLException e) { showError("Erreur", "Impossible de créer l'abonnement: " + e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════
    // NAVIGATION (code original conservé)
    // ══════════════════════════════════════════════════════════════
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        nav(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        nav(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        nav(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
    }

    @FXML private void handleDashboard(MouseEvent e)   { nav(e, "/UsersInterface/AcceuillAgr.fxml", "AgroFlow - Dashboard Agricole"); }
    @FXML private void handleMesTerrains(MouseEvent e) { nav(e, "/TerrainsInterface/agricoleaffichageterrain.fxml",  "Mes Terrains"); }
    @FXML private void handleMesAnimaux(MouseEvent e)  { nav(e, "/MesAnimaux.fxml",   "Mes Animaux");  }
    @FXML private void handleMesStocks(MouseEvent e)   { nav(e, "/MesStocks.fxml",    "Mes Stocks");   }
    @FXML private void handleMonMateriel(MouseEvent e) { nav(e, "/MonMateriel.fxml",  "Mon Matériel"); }
    @FXML private void handleMonAbonnement() { /* déjà sur cette page */ }

    @FXML
    private void handleAPropos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setTitle("Mon Profil"); s.setScene(new Scene(root));
            s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen(); s.showAndWait();
        } catch (IOException e) { showError("Erreur", e.getMessage()); }
    }

    @FXML
    private void handleLogout() {
        new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment vous déconnecter ?")
                .showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    SessionManager.setCurrentUser(null);
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                        Parent root = loader.load();
                        Stage s = getStage();
                        if (s != null) { s.setScene(new Scene(root, 1500, 700)); s.setTitle("AgroFlow - Connexion"); s.setMaximized(true); }
                    } catch (IOException e) { showError("Erreur", e.getMessage()); }
                });
    }

    private void nav(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            try {
                loader.getController().getClass().getMethod("setCurrentUser", Personne.class)
                        .invoke(loader.getController(), this.currentUser);
            } catch (NoSuchMethodException ignored) { } catch (Exception ex) { ex.printStackTrace(); }
            Stage s = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean max = s.isMaximized();
            s.setScene(new Scene(root)); s.setTitle(title); s.setMaximized(max); s.show();
        } catch (IOException e) { System.err.println("Erreur navigation: " + fxmlPath); }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS DIVERS
    // ══════════════════════════════════════════════════════════════

    private void showPdfSuccess(File f) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PDF généré"); alert.setHeaderText("✅ PDF créé avec succès");
        alert.setContentText(f.getAbsolutePath());
        ButtonType open = new ButtonType("📂 Ouvrir", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(open, new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.showAndWait().ifPresent(b -> {
            if (b == open) try { Desktop.getDesktop().open(f); } catch (Exception ignored) {}
        });
    }

    private void desktopDir(FileChooser fc) {
        File d = new File(System.getProperty("user.home") + "/Desktop");
        if (d.exists()) fc.setInitialDirectory(d);
    }

    private void toggleMenu(boolean show) {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(show); gestionSubmenu.setManaged(show); }
    }

    private Stage getStage() {
        if (rootPane  != null && rootPane.getScene()  != null) return (Stage) rootPane.getScene().getWindow();
        if (logoutBtn != null && logoutBtn.getScene() != null) return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    private Label errLabel(String msg) { Label l = new Label(msg); l.setTextFill(Color.RED); return l; }
    private String nvl(String s)       { return s != null ? s : "—"; }
    private String safe(String s)      { return s != null ? s : ""; }

    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR);       a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void showSuccess(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}