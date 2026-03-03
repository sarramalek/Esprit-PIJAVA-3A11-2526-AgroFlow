package controllers.Materiels;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import org.apache.poi.ss.usermodel.Row; // Ensure it is from org.apache.poi
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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



import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import models.Materiels.Achat;
import models.Materiels.Machine;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.Materiels.AchatService;
import services.Materiels.MachineService;
import utils.MyDatabase;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AfficherAchatsController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;
    // ================= COMPOSANTS FXML =================
    @FXML private TableView<AchatVM> tableAchats;
    @FXML private TableColumn<AchatVM, String> colDateAchat;
    @FXML private TableColumn<AchatVM, Integer> colQuantite;
    @FXML private TableColumn<AchatVM, String> colMachine;
    @FXML private TableColumn<AchatVM, String> colClient;
    @FXML private TableColumn<AchatVM, Integer> colCin;
    @FXML private TableColumn<AchatVM, Void> colActions;

    // Champs pour recherche et filtres
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private Label lblTotal;
    @FXML private Label lblQuantiteTotal;

    // ================= SERVICES =================
    private AchatService achatService;
    private MachineService machineService;
    private Connection connection;

    // ================= LISTES =================
    private ObservableList<AchatVM> achatsObservableList;
    private ObservableList<AchatVM> achatsFiltres;


    // ── Listes ───────────────────────────────────────────────────────
    private final ObservableList<AchatVM> masterList  = FXCollections.observableArrayList();
    private final ObservableList<AchatVM> displayList = FXCollections.observableArrayList();
    private List<Machine> machines = new ArrayList<>();

    // ── Tri ──────────────────────────────────────────────────────────
    private enum SortMode { DATE_DESC, DATE_ASC, NONE }
    private SortMode currentSort = SortMode.DATE_DESC;

    // ═══════════════════════════════════════════════════════════════
    //  ViewModel
    // ═══════════════════════════════════════════════════════════════
    public static class AchatVM {
        private final int idAchat, quantite, idM, cin;
        private final LocalDate dateAchat;
        private final String machineNom;
        private final String nomClient;

        public AchatVM(int idAchat, LocalDate dateAchat, int quantite,
                       int idM, int cin, String machineNom, String nomClient) {
            this.idAchat    = idAchat;
            this.dateAchat  = dateAchat;
            this.quantite   = quantite;
            this.idM        = idM;
            this.cin        = cin;
            this.machineNom = machineNom;
            this.nomClient  = nomClient;
        }

        public int       getIdAchat()    { return idAchat;    }
        public LocalDate getDateAchat()  { return dateAchat;  }
        public int       getQuantite()   { return quantite;   }
        public int       getIdM()        { return idM;        }
        public int       getCin()        { return cin;        }
        public String    getMachineNom() { return machineNom; }
        public String    getNomClient()  { return nomClient;  }
    }

    // ── Helpers internes ─────────────────────────────────────────────
    private static class UserInfo {
        final int cin;
        final String nom, prenom;
        UserInfo(int cin, String nom, String prenom) {
            this.cin = cin; this.nom = nom; this.prenom = prenom;
        }
        @Override public String toString() { return "CIN: " + cin + " - " + prenom + " " + nom; }
    }

    private static class MachineItem {
        final int    idM;
        final String label;
        MachineItem(int idM, String label) { this.idM = idM; this.label = label; }
        @Override public String toString() { return label; }
    }

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
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        // Mise à jour des labels
        updateUserLabels();
        achatService   = new AchatService();
        machineService = new MachineService();
        connection     = MyDatabase.getInstance().getConnection();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();
    }

    // ═══════════════════════════════════════════════════════════════
    //  TABLEAU
    // ═══════════════════════════════════════════════════════════════
    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colDateAchat.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDateAchat().format(fmt)));
        colQuantite.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getQuantite()).asObject());
        colMachine.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getMachineNom()));
        colClient.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomClient()));
        colCin.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getCin()).asObject());

        tableAchats.setRowFactory(tv -> new TableRow<AchatVM>() {
            @Override protected void updateItem(AchatVM item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(!empty && getIndex() % 2 == 1 ? "-fx-background-color: #f0f4f8;" : "");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  CHARGEMENT
    // ═══════════════════════════════════════════════════════════════
    private void chargerDonnees() {
        try { machines = machineService.recuperer(); }
        catch (SQLException e) { showErr("Erreur", "Chargement machines : " + e.getMessage()); }
        chargerAchats();
        initialiserComboMachines();
    }

    private void chargerAchats() {
        masterList.clear();
        String sql =
                "SELECT a.idAchat, a.dateAchat, a.quantite, a.idM, a.cin, " +
                        "       u.nom, u.prenom, m.marque, m.modele " +
                        "FROM achat a " +
                        "LEFT JOIN users   u ON a.cin = u.cin " +
                        "LEFT JOIN machine m ON a.idM = m.idM " +
                        "ORDER BY a.dateAchat DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String marque  = rs.getString("marque");
                String modele  = rs.getString("modele");
                String machNom = (marque != null) ? marque + " " + modele : "Machine inconnue";

                String nom    = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String client = (nom != null) ? prenom + " " + nom : "Inconnu";

                masterList.add(new AchatVM(
                        rs.getInt("idAchat"),
                        rs.getDate("dateAchat").toLocalDate(),
                        rs.getInt("quantite"),
                        rs.getInt("idM"),
                        rs.getInt("cin"),
                        machNom,
                        client
                ));
            }
        } catch (SQLException e) { showErr("Erreur SQL", e.getMessage()); }

        appliquerFiltres();
    }

    private void initialiserComboMachines() {
        comboMachine.getItems().clear();
        comboMachine.getItems().add("Toutes les machines");
        for (Machine m : machines)
            comboMachine.getItems().add(m.getMarque() + " " + m.getModele());
        comboMachine.getSelectionModel().selectFirst();
    }

    // ═══════════════════════════════════════════════════════════════
    //  RECHERCHE DYNAMIQUE
    // ═══════════════════════════════════════════════════════════════
    private void configurerRecherche() {
        // Recherche dynamique : chaque frappe déclenche le filtre
        champRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            appliquerFiltres();
        });
    }

    @FXML private void rechercher() { appliquerFiltres(); }
    @FXML private void filtrer()    { appliquerFiltres(); }

    @FXML
    private void effacerRecherche() {
        champRecherche.clear();
        appliquerFiltres();
    }

    @FXML private void trierPlusRecent() { currentSort = SortMode.DATE_DESC; appliquerFiltres(); }
    @FXML private void trierPlusAncien() { currentSort = SortMode.DATE_ASC;  appliquerFiltres(); }

    @FXML
    private void reinitialiser() {
        champRecherche.clear();
        comboMachine.getSelectionModel().selectFirst();
        currentSort = SortMode.DATE_DESC;
        appliquerFiltres();
    }

    @FXML
    private void actualiser() {
        reinitialiser();
        chargerDonnees();
    }

    // ═══════════════════════════════════════════════════════════════
    //  FILTRES + TRI
    // ═══════════════════════════════════════════════════════════════
    private void appliquerFiltres() {
        String recherche  = champRecherche.getText() == null ? "" : champRecherche.getText().toLowerCase().trim();
        String machineSel = comboMachine.getValue() == null ? "Toutes les machines" : comboMachine.getValue();

        List<AchatVM> filtered = new ArrayList<>();

        for (AchatVM a : masterList) {

            // Filtre texte dynamique (machine, client, CIN, date, quantité)
            boolean matchR = recherche.isEmpty()
                    || a.getMachineNom().toLowerCase().contains(recherche)
                    || a.getNomClient().toLowerCase().contains(recherche)
                    || String.valueOf(a.getCin()).contains(recherche)
                    || a.getDateAchat().toString().contains(recherche)
                    || String.valueOf(a.getQuantite()).contains(recherche);

            // Filtre machine combo
            boolean matchM = machineSel.equals("Toutes les machines")
                    || a.getMachineNom().equals(machineSel);

            if (matchR && matchM) filtered.add(a);
        }

        // Tri
        switch (currentSort) {
            case DATE_ASC  -> filtered.sort(Comparator.comparing(AchatVM::getDateAchat));
            case DATE_DESC -> filtered.sort(Comparator.comparing(AchatVM::getDateAchat).reversed());
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
        int totalQ = displayList.stream().mapToInt(AchatVM::getQuantite).sum();
        lblQuantiteTotal.setText(String.valueOf(totalQ));
    }

    @FXML
    private void afficherStatistiques() {
        Map<String, Integer> dataMap = new LinkedHashMap<>();
        for (AchatVM a : displayList)
            dataMap.merge(a.getMachineNom(), a.getQuantite(), Integer::sum);

        if (dataMap.isEmpty()) { showInfo("Statistiques", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques — Quantités par Machine");
        stage.setResizable(true);

        int barW = 60, gap = 30, padL = 70, padB = 60, padTop = 50, chartH = 320;
        List<String>  keys = new ArrayList<>(dataMap.keySet());
        List<Integer> vals = new ArrayList<>(dataMap.values());
        int n       = keys.size();
        int canvasW = padL + n * (barW + gap) + gap + 20;
        int canvasH = chartH + padB + padTop;

        Canvas canvas = new Canvas(canvasW, canvasH);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasW, canvasH);
        gc.setFill(Color.web("#2c3e50"));
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

        gc.setStroke(Color.web("#bdc3c7")); gc.setLineWidth(2);
        gc.strokeLine(padL, padTop + chartH, canvasW - 20, padTop + chartH);

        Color[] colors = {
                Color.web("#3498db"), Color.web("#27ae60"), Color.web("#e74c3c"),
                Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63")
        };

        for (int i = 0; i < n; i++) {
            double barH = (vals.get(i) / (double) maxVal) * chartH;
            double x    = padL + gap + i * (barW + gap);
            double y    = padTop + chartH - barH;
            Color c     = colors[i % colors.length];

            gc.setFill(Color.rgb(0, 0, 0, 0.08));
            gc.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);
            gc.setFill(c);
            gc.fillRoundRect(x, y, barW, barH, 6, 6);

            gc.setFill(Color.web("#2c3e50"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            String valStr = String.valueOf(vals.get(i));
            gc.fillText(valStr, x + barW / 2.0 - (valStr.length() * 3.5), y - 6);

            gc.setFont(Font.font("Arial", 10));
            String lbl = keys.get(i).length() > 14 ? keys.get(i).substring(0, 14) + "…" : keys.get(i);
            gc.setFill(Color.web("#2c3e50"));
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

            doc.add(new Paragraph("Rapport — Gestion des Achats")
                    .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Généré le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));

            float[] colW = {100f, 70f, 160f, 160f, 90f};
            Table table  = new Table(UnitValue.createPointArray(colW));
            table.setWidth(UnitValue.createPercentValue(100));

            for (String h : new String[]{"Date", "Quantité", "Machine", "Client", "CIN"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(11))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (AchatVM a : displayList) {
                table.addCell(new Cell().add(new Paragraph(a.getDateAchat().format(fmt)).setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(a.getQuantite())).setFontSize(10).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(a.getMachineNom()).setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(a.getNomClient()).setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(a.getCin())).setFontSize(10)));
            }

            doc.add(table);
            doc.add(new Paragraph("\nTotal Achats : " + displayList.size()).setFontSize(11).setBold());
            int totalQ = displayList.stream().mapToInt(AchatVM::getQuantite).sum();
            doc.add(new Paragraph("Quantité Totale : " + totalQ).setFontSize(11).setBold());

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

            Row header = (Row) sheet.createRow(0);
            String[] cols = {"Date Achat", "Quantité", "Machine", "Client", "CIN"};
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
            hFont.setBold(true);
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i < cols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowIdx = 1;
            for (AchatVM a : displayList) {
                Row row = (Row) sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getDateAchat().format(fmt));
                row.createCell(1).setCellValue(a.getQuantite());
                row.createCell(2).setCellValue(a.getMachineNom());
                row.createCell(3).setCellValue(a.getNomClient());
                row.createCell(4).setCellValue(a.getCin());
            }

            Row total = sheet.createRow(rowIdx + 1);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(1).setCellValue(displayList.stream().mapToInt(AchatVM::getQuantite).sum());
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }

        } catch (Exception e) { showErr("Erreur Excel", e.getMessage()); return; }

        showInfo("✅ Export Excel", "Fichier enregistré :\n" + file.getAbsolutePath());
    }

    // ═══════════════════════════════════════════════════════════════
    //  CRUD
    // ═══════════════════════════════════════════════════════════════
    @FXML private void ouvrirAjout() { afficherDialog(null); }

    @FXML
    private void modifierSelectionne() {
        AchatVM sel = tableAchats.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Aucune sélection", "Sélectionnez un achat à modifier."); return; }
        afficherDialog(sel);
    }

    @FXML
    private void supprimerSelectionne() {
        AchatVM sel = tableAchats.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Aucune sélection", "Sélectionnez un achat à supprimer."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'achat #" + sel.getIdAchat() + " ?");
        confirm.setContentText("Client : " + sel.getNomClient() +
                "\nMachine : " + sel.getMachineNom() + "\n\nCette action est irréversible !");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                achatService.supprimer(sel.getIdAchat());
                showInfo("✅ Succès", "Achat supprimé.");
                chargerAchats();
            } catch (SQLException e) { showErr("Erreur", e.getMessage()); }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  DIALOG AJOUTER / MODIFIER
    // ═══════════════════════════════════════════════════════════════
    private void afficherDialog(AchatVM existant) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existant == null ? "➕ Nouvel Achat" : "✏️ Modifier l'Achat #" + existant.getIdAchat());
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");

        Label titre = new Label(existant == null ? "📝 Nouvel Achat" : "✏️ Modifier l'Achat");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(260);

        TextField qteField = new TextField();
        qteField.setPrefWidth(260);
        qteField.setPromptText("Ex : 5");

        ComboBox<MachineItem> machineCombo = new ComboBox<>();
        machineCombo.setPrefWidth(260);
        machineCombo.setPromptText("Sélectionner une machine");
        for (Machine m : machines)
            machineCombo.getItems().add(new MachineItem(m.getIdM(), m.getMarque() + " " + m.getModele()));

        ComboBox<UserInfo> clientCombo = new ComboBox<>();
        clientCombo.setPrefWidth(260);
        clientCombo.setPromptText("Sélectionner un client");
        clientCombo.setItems(recupererUsers());

        if (existant != null) {
            datePicker.setValue(existant.getDateAchat());
            qteField.setText(String.valueOf(existant.getQuantite()));
            for (MachineItem mi : machineCombo.getItems())
                if (mi.idM == existant.getIdM()) { machineCombo.getSelectionModel().select(mi); break; }
            for (UserInfo u : clientCombo.getItems())
                if (u.cin == existant.getCin()) { clientCombo.getSelectionModel().select(u); break; }
        }

        grid.add(makeLabel("📅 Date d'Achat *"), 0, 0); grid.add(datePicker,   1, 0);
        grid.add(makeLabel("📦 Quantité *"),     0, 1); grid.add(qteField,     1, 1);
        grid.add(makeLabel("⚙️ Machine *"),       0, 2); grid.add(machineCombo, 1, 2);
        grid.add(makeLabel("👤 Client *"),        0, 3); grid.add(clientCombo,  1, 3);

        Button btnOK  = new Button(existant == null ? "✅ Ajouter" : "✅ Modifier");
        btnOK.setPrefSize(150, 40);
        btnOK.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");

        Button btnAnn = new Button("❌ Annuler");
        btnAnn.setPrefSize(150, 40);
        btnAnn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");

        btnOK.setOnAction(e -> {
            if (!valider(datePicker, qteField, machineCombo, clientCombo)) return;
            try {
                Achat achat = new Achat();
                if (existant != null) achat.setIdAchat(existant.getIdAchat());
                achat.setDateAchat(datePicker.getValue());
                achat.setQuantite(Integer.parseInt(qteField.getText().trim()));
                achat.setIdM(machineCombo.getValue().idM);
                achat.setCin(clientCombo.getValue().cin);

                if (existant == null) { achatService.ajouter(achat);  showInfo("✅", "Achat ajouté !"); }
                else                  { achatService.modifier(achat); showInfo("✅", "Achat modifié !"); }

                chargerAchats();
                dialog.close();
            } catch (SQLException ex) { showErr("Erreur", ex.getMessage()); }
        });

        btnAnn.setOnAction(e -> dialog.close());

        HBox btns = new HBox(15, btnOK, btnAnn);
        btns.setAlignment(Pos.CENTER);
        btns.setPadding(new Insets(15, 0, 0, 0));

        root.getChildren().addAll(titre, grid, btns);
        dialog.setScene(new Scene(root, 540, 420));
        dialog.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    //  VALIDATION
    // ═══════════════════════════════════════════════════════════════
    private boolean valider(DatePicker dp, TextField qte,
                            ComboBox<MachineItem> machine, ComboBox<UserInfo> client) {
        if (dp.getValue() == null)
        { showWarn("Champ requis",   "Sélectionnez une date."); return false; }
        if (dp.getValue().isAfter(LocalDate.now()))
        { showWarn("Date invalide",  "La date ne peut pas être dans le futur."); return false; }
        if (dp.getValue().isBefore(LocalDate.of(2000, 1, 1)))
        { showWarn("Date invalide",  "Date trop ancienne (avant 2000)."); return false; }
        if (qte.getText().trim().isEmpty())
        { showWarn("Champ requis",   "Entrez une quantité."); return false; }
        if (!qte.getText().trim().matches("\\d+"))
        { showWarn("Format invalide","La quantité doit être un entier positif."); return false; }
        int q = Integer.parseInt(qte.getText().trim());
        if (q < 1 || q > 10000)
        { showWarn("Valeur invalide","Quantité entre 1 et 10 000."); return false; }
        if (machine.getValue() == null)
        { showWarn("Champ requis",   "Sélectionnez une machine."); return false; }
        if (client.getValue() == null)
        { showWarn("Champ requis",   "Sélectionnez un client."); return false; }
        return true;
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ═══════════════════════════════════════════════════════════════
    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        return l;
    }

    private ObservableList<UserInfo> recupererUsers() {
        ObservableList<UserInfo> list = FXCollections.observableArrayList();
        String sql = "SELECT cin, nom, prenom FROM users ORDER BY nom, prenom";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new UserInfo(rs.getInt("cin"), rs.getString("nom"), rs.getString("prenom")));
        } catch (SQLException e) { showErr("Erreur", "Chargement utilisateurs : " + e.getMessage()); }
        return list;
    }


    @FXML private void retourAccueil()      { nav("/MaterielsInterface/AccueilMateriel.fxml"); }



    private void nav(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) tableAchats.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) { showErr("Navigation", "Impossible de charger : " + path); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ALERTES UI
    // ═══════════════════════════════════════════════════════════════
    private void showErr (String t, String m) { alert(Alert.AlertType.ERROR,       t, m); }
    private void showWarn(String t, String m) { alert(Alert.AlertType.WARNING,     t, m); }

    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    //naviguer vers les autres modules

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}



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
    private void naviguerVers(String fxmlPath,Event event ) {
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
}