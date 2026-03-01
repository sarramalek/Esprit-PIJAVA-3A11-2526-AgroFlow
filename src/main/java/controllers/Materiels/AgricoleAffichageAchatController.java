package controllers.Materiels;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import models.Materiels.Achat;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import services.Materiels.AchatService;

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
import utils.SessionManager;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AgricoleAffichageAchatController implements Initializable {
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
    @FXML private Button logoutBtn;


    // ── FXML ─────────────────────────────────────────────────────────
    @FXML private TableView<Achat>            tableAchats;
    @FXML private TableColumn<Achat, String>  colDateAchat;
    @FXML private TableColumn<Achat, Integer> colQuantite;
    @FXML private TableColumn<Achat, Integer> colIdMachine;
    @FXML private TableColumn<Achat, Integer> colCin;

    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private Label            lblTotal;
    @FXML private Label            lblQuantiteTotal;

    // ── Service ──────────────────────────────────────────────────────
    private AchatService achatService;

    // ── Listes ───────────────────────────────────────────────────────
    private final ObservableList<Achat> masterList  = FXCollections.observableArrayList();
    private final ObservableList<Achat> displayList = FXCollections.observableArrayList();

    // ── Tri ──────────────────────────────────────────────────────────
    private enum SortMode { DATE_DESC, DATE_ASC, NONE }
    private SortMode currentSort = SortMode.DATE_DESC;

    // ═══════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        achatService = new AchatService();
        configurerTableau();
        configurerRecherche();
        chargerDonnees();
    }

    // ═══════════════════════════════════════════════════════════════
    //  TABLEAU — colonnes directement depuis Achat
    // ═══════════════════════════════════════════════════════════════
    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colDateAchat.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getDateAchat() != null
                                ? d.getValue().getDateAchat().format(fmt) : "N/A"));

        colQuantite.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getQuantite()).asObject());

        colIdMachine.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getIdM()).asObject());

        colCin.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getCin()).asObject());

        // Alternance couleurs vert clair / blanc
        tableAchats.setRowFactory(tv -> new TableRow<Achat>() {
            @Override protected void updateItem(Achat item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(!empty && getIndex() % 2 == 1 ? "-fx-background-color: #f0faf0;" : "");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  CHARGEMENT via AchatService
    // ═══════════════════════════════════════════════════════════════
    private void chargerDonnees() {
        masterList.clear();
        try {
            List<Achat> achats = achatService.recuperer();
            masterList.addAll(achats);
        } catch (SQLException e) {
            showErr("Erreur de chargement", e.getMessage());
        }
        initialiserComboMachines();
        appliquerFiltres();
    }

    private void initialiserComboMachines() {
        comboMachine.getItems().clear();
        comboMachine.getItems().add("Toutes les machines");
        masterList.stream()
                .map(a -> "Machine #" + a.getIdM())
                .distinct()
                .sorted()
                .forEach(m -> comboMachine.getItems().add(m));
        comboMachine.getSelectionModel().selectFirst();
    }

    // ═══════════════════════════════════════════════════════════════
    //  RECHERCHE DYNAMIQUE
    // ═══════════════════════════════════════════════════════════════
    private void configurerRecherche() {
        champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    @FXML private void filtrer()          { appliquerFiltres(); }
    @FXML private void effacerRecherche() { champRecherche.clear(); appliquerFiltres(); }
    @FXML private void trierPlusRecent()  { currentSort = SortMode.DATE_DESC; appliquerFiltres(); }
    @FXML private void trierPlusAncien()  { currentSort = SortMode.DATE_ASC;  appliquerFiltres(); }

    @FXML
    private void actualiser() {
        champRecherche.clear();
        comboMachine.getSelectionModel().selectFirst();
        currentSort = SortMode.DATE_DESC;
        chargerDonnees();
    }

    // ═══════════════════════════════════════════════════════════════
    //  FILTRES + TRI
    // ═══════════════════════════════════════════════════════════════
    private void appliquerFiltres() {
        String recherche  = champRecherche.getText() == null ? "" : champRecherche.getText().toLowerCase().trim();
        String machineSel = comboMachine.getValue() == null ? "Toutes les machines" : comboMachine.getValue();

        List<Achat> filtered = new ArrayList<>();

        for (Achat a : masterList) {
            String machLabel = "Machine #" + a.getIdM();

            boolean matchR = recherche.isEmpty()
                    || String.valueOf(a.getIdM()).contains(recherche)
                    || String.valueOf(a.getCin()).contains(recherche)
                    || String.valueOf(a.getQuantite()).contains(recherche)
                    || (a.getDateAchat() != null && a.getDateAchat().toString().contains(recherche));

            boolean matchM = machineSel.equals("Toutes les machines")
                    || machLabel.equals(machineSel);

            if (matchR && matchM) filtered.add(a);
        }

        switch (currentSort) {
            case DATE_ASC  -> filtered.sort(Comparator.comparing(
                    Achat::getDateAchat, Comparator.nullsLast(Comparator.naturalOrder())));
            case DATE_DESC -> filtered.sort(Comparator.comparing(
                    Achat::getDateAchat, Comparator.nullsLast(Comparator.reverseOrder())));
            default        -> {}
        }

        displayList.setAll(filtered);
        tableAchats.setItems(displayList);
        mettreAJourStats();
    }

    // ═══════════════════════════════════════════════════════════════
    //  STATISTIQUES
    // ═══════════════════════════════════════════════════════════════
    private void mettreAJourStats() {
        lblTotal.setText(String.valueOf(displayList.size()));
        int totalQ = displayList.stream().mapToInt(Achat::getQuantite).sum();
        lblQuantiteTotal.setText(String.valueOf(totalQ));
    }

    @FXML
    private void afficherStatistiques() {
        // Grouper quantités par idM
        Map<String, Integer> dataMap = new LinkedHashMap<>();
        for (Achat a : displayList)
            dataMap.merge("Machine #" + a.getIdM(), a.getQuantite(), Integer::sum);

        if (dataMap.isEmpty()) { showInfo("Statistiques", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques — Achats par Machine");
        stage.setResizable(true);

        int barW = 60, gap = 30, padL = 70, padTop = 50, chartH = 320, padB = 60;
        List<String>  keys = new ArrayList<>(dataMap.keySet());
        List<Integer> vals = new ArrayList<>(dataMap.values());
        int n       = keys.size();
        int canvasW = padL + n * (barW + gap) + gap + 20;
        int canvasH = chartH + padB + padTop;

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasW, canvasH);
        gc.setFill(Color.web("#1B4332"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("Quantité totale par machine", padL, 30);

        int maxVal = vals.stream().mapToInt(v -> v).max().orElse(1);
        gc.setFont(Font.font("Arial", 11));
        for (int i = 0; i <= 5; i++) {
            double y = padTop + chartH - (chartH * i / 5.0);
            gc.setStroke(Color.LIGHTGRAY); gc.setLineWidth(1);
            gc.strokeLine(padL, y, canvasW - 20, y);
            gc.setFill(Color.GRAY);
            gc.fillText(String.valueOf((int)(maxVal * i / 5.0)), 5, y + 4);
        }
        gc.setStroke(Color.web("#b7e4c7")); gc.setLineWidth(2);
        gc.strokeLine(padL, padTop + chartH, canvasW - 20, padTop + chartH);

        Color[] colors = {
                Color.web("#2D6A4F"), Color.web("#52b788"), Color.web("#74c69d"),
                Color.web("#1B4332"), Color.web("#40916c"), Color.web("#95d5b2")
        };
        for (int i = 0; i < n; i++) {
            double barH = (vals.get(i) / (double) maxVal) * chartH;
            double x = padL + gap + i * (barW + gap);
            double y = padTop + chartH - barH;
            Color c  = colors[i % colors.length];
            gc.setFill(Color.rgb(0, 0, 0, 0.08));
            gc.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);
            gc.setFill(c);
            gc.fillRoundRect(x, y, barW, barH, 6, 6);
            gc.setFill(Color.web("#1B4332"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            String valStr = String.valueOf(vals.get(i));
            gc.fillText(valStr, x + barW / 2.0 - (valStr.length() * 3.5), y - 6);
            gc.setFont(Font.font("Arial", 10));
            String lbl = keys.get(i);
            gc.fillText(lbl, x + barW / 2.0 - (lbl.length() * 3), padTop + chartH + 16);
        }

        ScrollPane sp = new ScrollPane(canvas);
        sp.setFitToHeight(true);
        sp.setPrefSize(Math.min(canvasW + 20, 950), canvasH + 20);
        VBox root = new VBox(10, sp);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════
    //  EXPORT PDF
    // ═══════════════════════════════════════════════════════════════
    @FXML
    private void exporterPDF() {
        if (displayList.isEmpty()) { showInfo("Export PDF", "Aucune donnée à exporter."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialFileName("achats_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(tableAchats.getScene().getWindow());
        if (file == null) return;

        try (PdfWriter writer = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf  = new PdfDocument(writer);
             Document    doc  = new Document(pdf)) {

            doc.add(new Paragraph("Rapport — Achats de Matériel")
                    .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Généré le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));

            float[] colW = {110f, 80f, 100f, 100f};
            Table table  = new Table(UnitValue.createPointArray(colW));
            table.setWidth(UnitValue.createPercentValue(100));

            for (String h : new String[]{"Date", "Quantité", "ID Machine", "CIN"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(11))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Achat a : displayList) {
                table.addCell(new Cell().add(new Paragraph(
                        a.getDateAchat() != null ? a.getDateAchat().format(fmt) : "N/A").setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(
                        String.valueOf(a.getQuantite())).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(
                        String.valueOf(a.getIdM())).setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(
                        String.valueOf(a.getCin())).setFontSize(10)));
            }

            doc.add(table);
            doc.add(new Paragraph("\nTotal Achats : " + displayList.size()).setFontSize(11).setBold());
            doc.add(new Paragraph("Quantité Totale : " +
                    displayList.stream().mapToInt(Achat::getQuantite).sum()).setFontSize(11).setBold());

        } catch (Exception e) { showErr("Erreur PDF", e.getMessage()); return; }

        showInfo("✅ Export PDF", "Fichier enregistré :\n" + file.getAbsolutePath());
    }

    // ═══════════════════════════════════════════════════════════════
    //  EXPORT EXCEL
    // ═══════════════════════════════════════════════════════════════
    @FXML
    private void exporterExcel() {
        if (displayList.isEmpty()) { showInfo("Export Excel", "Aucune donnée à exporter."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le fichier Excel");
        fc.setInitialFileName("achats_" + LocalDate.now() + ".xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(tableAchats.getScene().getWindow());
        if (file == null) return;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Achats");

            String[] cols = {"Date Achat", "Quantité", "ID Machine", "CIN"};
            Row header = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hf = wb.createFont();
            hf.setBold(true);
            hs.setFont(hf);
            hs.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            hs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowIdx = 1;
            for (Achat a : displayList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getDateAchat() != null ? a.getDateAchat().format(fmt) : "N/A");
                row.createCell(1).setCellValue(a.getQuantite());
                row.createCell(2).setCellValue(a.getIdM());
                row.createCell(3).setCellValue(a.getCin());
            }

            Row total = sheet.createRow(rowIdx + 1);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(1).setCellValue(displayList.stream().mapToInt(Achat::getQuantite).sum());
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }

        } catch (Exception e) { showErr("Erreur Excel", e.getMessage()); return; }

        showInfo("✅ Export Excel", "Fichier enregistré :\n" + file.getAbsolutePath());
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVIGATION (sidebar agricole)
    // ═══════════════════════════════════════════════════════════════
    @FXML private void naviguerDashboard()  { nav("/AcceuilAgricole.fxml"); }
    @FXML private void naviguerTerrains()   { showInfo("À venir", "Module Terrains en cours de développement."); }
    @FXML private void naviguerPlantes()    { nav("/agricoleplante.fxml"); }
    @FXML private void naviguerAnimaux()    { showInfo("À venir", "Module Animaux en cours de développement."); }
    @FXML private void naviguerStocks()     { showInfo("À venir", "Module Stocks en cours de développement."); }
    @FXML private void naviguerAchats()     { /* déjà sur cette page */ }
    @FXML private void retourAccueil()      { nav("/AcceuilAgricole.fxml"); }

    @FXML
    private void deconnexion() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Déconnexion");
        a.setContentText("Voulez-vous vraiment vous déconnecter ?");
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK)
            nav("/UsersInterface/login.fxml");
    }

    private void nav(String path) {
        try {
            // Chercher dans les deux emplacements possibles
            java.net.URL url = getClass().getResource(path);

            // Si non trouvé avec le chemin complet, essayer depuis la racine des resources
            if (url == null) {
                // Extraire le nom de fichier seul et chercher à la racine
                String filename = path.substring(path.lastIndexOf('/'));
                url = getClass().getResource(filename);
            }

            if (url == null) {
                showErr("Fichier FXML introuvable",
                        "Impossible de trouver :\n" + path +
                                "\n\nVérifiez que le fichier existe dans src/main/resources.");
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) tableAchats.getScene().getWindow();
            boolean max = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setMaximized(max);

        } catch (IOException e) {
            showErr("Erreur de navigation", "Impossible de charger : " + path + "\n" + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ALERTES UI
    // ═══════════════════════════════════════════════════════════════
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

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
    @FXML private void handleMonMateriel(MouseEvent event )    {navigateTo(event,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Mon Profil"); }
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); }  }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

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
        }  else {
            // Erreur réelle
            showError("Erreur de chargement\n\n" +
                    "Impossible de charger " + title + ".\n" +
                    "Détails: " + e.getMessage());
        }
    }

}