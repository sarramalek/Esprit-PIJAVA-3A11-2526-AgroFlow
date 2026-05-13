package controllers.Stocks;

import controllers.User.ProfilEmploye;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Stocks.Article;
import models.User.Personne;
import services.Stocks.ArticleService;
import services.Stocks.CategorieService;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.SessionManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class afficherarticleController {
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;

    // ══════════════════════════════════════════════════════
    //  FXML — Tableau & Colonnes
    // ══════════════════════════════════════════════════════
    @FXML private TableView<Article>            tableArticles;
    @FXML private TableColumn<Article, String>  colNom, colUnite, colCategorie, colDevise, colAgriculteur;
    @FXML private TableColumn<Article, Double>  colQuantite, colSeuil, colPrix;
    @FXML private TableColumn<Article, Void>    colActions;

    // ══════════════════════════════════════════════════════
    //  FXML — Recherche & Filtres
    // ══════════════════════════════════════════════════════
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbFiltreCategorie;
    @FXML private ComboBox<String> cbFiltreAgriculteur;
    @FXML private Label            lblFiltreAgriculteur;

    // ══════════════════════════════════════════════════════
    //  FXML — QR Code & KPI Labels
    // ══════════════════════════════════════════════════════
    @FXML private ImageView ivQRCode;
    @FXML private Label     lblNomSelection;
    @FXML private Label     lblNbAlertes;
    @FXML private Label     lblTotalArticles;
    @FXML private Label     lblWarning;
    @FXML private VBox      qrCodeContainer;

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar & Navigation
    // ══════════════════════════════════════════════════════
    @FXML private Button logoutBtn;
    @FXML private Button gestionBtn;
    @FXML private VBox   gestionSubmenu;
    @FXML private VBox   gestionContainer;

    // ══════════════════════════════════════════════════════
    //  Services & Données
    // ══════════════════════════════════════════════════════
    private final ArticleService   articleService        = new ArticleService();
    private final CategorieService catService            = new CategorieService();
    private ObservableList<Article> masterData           = FXCollections.observableArrayList();

    // Bloque les doublons d'alertes email
    private final Set<Integer> alertesDejaEnvoyees = new HashSet<>();

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        //chargerAvatarTopBar(SessionManager.getCurrentUser());

        // Mise à jour des labels
        updateUserLabels();
        //chargerSidebarAvatar(SessionManager.getCurrentUser());
        // Sidebar submenu caché par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
        if (gestionBtn != null)
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        if (gestionContainer != null)
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        configurerColonnes();

        // Sélection → affichage QR
        tableArticles.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, nv) -> { if (nv != null) afficherQR(nv); });

        // Visibilité QR Code : Masquer pour ADMIN (Role 3)
        if (qrCodeContainer != null) {
            boolean isNotAdmin = (currentUser != null && currentUser.getRole() != 3);
            qrCodeContainer.setVisible(isNotAdmin);
            qrCodeContainer.setManaged(isNotAdmin);
        }

        // Visibilité Colonne Agriculteur & Filtre : Uniquement pour ADMIN (Role 3)
        if (colAgriculteur != null) {
            boolean isAdmin = (currentUser != null && currentUser.getRole() == 3);
            colAgriculteur.setVisible(isAdmin);
            
            if (cbFiltreAgriculteur != null && lblFiltreAgriculteur != null) {
                cbFiltreAgriculteur.setVisible(isAdmin);
                cbFiltreAgriculteur.setManaged(isAdmin);
                lblFiltreAgriculteur.setVisible(isAdmin);
                lblFiltreAgriculteur.setManaged(isAdmin);
            }
        }

        configurerStyleLignes();
        chargerDonnees();
    }
   /* private void chargerAvatarTopBar(Personne user) {
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
    }*/


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
                case 1 -> "👷 EMPLOYÉ";
                case 2 -> "🌾 AGRICOLE";
                case 3 -> "👑 ADMIN";
                default -> "Rôle inconnu";
            };
            userRoleLabel.setText(roleText);
        } else {
            System.err.println("✗ userRoleLabel est NULL (non lié en FXML ?)");
        }
    }
    // ══════════════════════════════════════════════════════
    //  CONFIGURATION DES COLONNES
    // ══════════════════════════════════════════════════════
    private void configurerColonnes() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        if (colAgriculteur != null) {
            colAgriculteur.setCellValueFactory(new PropertyValueFactory<>("nomAgriculteur"));
        }
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteEnStock"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("uniteMesure"));
        if (colPrix != null) {
            colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        }
        if (colDevise != null) {
            colDevise.setCellValueFactory(new PropertyValueFactory<>("devise"));
        }
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));

        // Affichage du nom de catégorie (résolution depuis l'id)
        colCategorie.setCellValueFactory(cellData -> {
            int idCat = cellData.getValue().getIdCategorie();
            try {
                String nomCat = catService.getNomById(idCat);
                return new SimpleStringProperty(nomCat != null ? nomCat : "ID: " + idCat);
            } catch (Exception e) {
                return new SimpleStringProperty("ID: " + idCat);
            }
        });

        configurerColonneActions();
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT DES DONNÉES
    // ══════════════════════════════════════════════════════
    private void chargerDonnees() {
        try {
            if (currentUser != null && currentUser.getRole() == 1) { // 1 = AGRICOLE
                masterData = FXCollections.observableArrayList(articleService.recupererParUser(currentUser.getCin()));
            } else {
                masterData = FXCollections.observableArrayList(articleService.recuperer());
            }

            // Combo catégories
            if (cbFiltreCategorie != null) {
                ObservableList<String> cats = FXCollections.observableArrayList("Toutes");
                List<models.Stocks.Categorie> allCats;
                if (currentUser != null && currentUser.getRole() == 1) {
                    allCats = catService.recupererParUser(currentUser.getCin());
                } else {
                    allCats = catService.recuperer();
                }
                
                cats.addAll(allCats.stream()
                        .map(c -> c.getNom())
                        .collect(Collectors.toList()));
                cbFiltreCategorie.setItems(cats);
                cbFiltreCategorie.getSelectionModel().selectFirst();
            }

            // Combo agriculteurs (pour Admin)
            if (cbFiltreAgriculteur != null && currentUser != null && currentUser.getRole() == 3) {
                ObservableList<String> agris = FXCollections.observableArrayList("Tous");
                List<models.User.Utilisateur> allAgris = new services.User.PersonneService().getUtilisateurs();
                agris.addAll(allAgris.stream()
                        .map(u -> u.getPrenom() + " " + u.getNom())
                        .collect(Collectors.toList()));
                cbFiltreAgriculteur.setItems(agris);
                cbFiltreAgriculteur.getSelectionModel().selectFirst();
            }

            // Recherche + filtre combinés
            FilteredList<Article> filteredData = new FilteredList<>(masterData, p -> true);

            tfRecherche.textProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));
            if (cbFiltreCategorie != null)
                cbFiltreCategorie.valueProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));
            if (cbFiltreAgriculteur != null)
                cbFiltreAgriculteur.valueProperty().addListener((o, old, nv) -> appliquerFiltres(filteredData));

            SortedList<Article> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableArticles.comparatorProperty());
            tableArticles.setItems(sortedData);

            mettreAJourKPI();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════
    //  FILTRAGE COMBINÉ
    // ══════════════════════════════════════════════════════
    private void appliquerFiltres(FilteredList<Article> filteredData) {
        filteredData.setPredicate(article -> {
            String search = (tfRecherche.getText() == null) ? ""
                    : tfRecherche.getText().toLowerCase();
            boolean matchesNom = article.getNom().toLowerCase().contains(search);

            boolean matchesCat = true;
            if (cbFiltreCategorie != null) {
                String catSel = cbFiltreCategorie.getValue();
                if (catSel != null && !catSel.equals("Toutes")) {
                    try {
                        String nomCat = catService.getNomById(article.getIdCategorie());
                        matchesCat = catSel.equals(nomCat);
                    } catch (Exception e) {
                        matchesCat = false;
                    }
                }
            }

            boolean matchesAgri = true;
            if (cbFiltreAgriculteur != null && cbFiltreAgriculteur.isVisible()) {
                String agriSel = cbFiltreAgriculteur.getValue();
                if (agriSel != null && !agriSel.equals("Tous")) {
                    matchesAgri = agriSel.equals(article.getNomAgriculteur());
                }
            }

            return matchesNom && matchesCat && matchesAgri;
        });
        mettreAJourKPI();
    }

    // ══════════════════════════════════════════════════════
    //  KPI & ALERTES
    // ══════════════════════════════════════════════════════
    private void mettreAJourKPI() {
        int total = masterData.size();
        List<Article> alertes = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (lblTotalArticles != null) lblTotalArticles.setText(String.valueOf(total));
        if (lblNbAlertes     != null) lblNbAlertes.setText(String.valueOf(alertes.size()));

        if (lblWarning != null) {
            if (!alertes.isEmpty()) {
                lblWarning.setVisible(true);
                appliquerAnimationAlerte();
            } else {
                lblWarning.setVisible(false);
            }
        }
    }

    private void appliquerAnimationAlerte() {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), lblWarning);
        fade.setFromValue(1.0);
        fade.setToValue(0.1);
        fade.setCycleCount(Timeline.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    // ══════════════════════════════════════════════════════
    //  RÉAPPROVISIONNEMENT
    // ══════════════════════════════════════════════════════
    @FXML
    void toutReapprovisionner(ActionEvent event) {
        List<Article> enAlerte = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (enAlerte.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun article en alerte.").show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Réapprovisionner " + enAlerte.size() + " articles ?");
        if (confirm.showAndWait().get() == ButtonType.YES) {
            try {
                for (Article a : enAlerte) {
                    a.setQuantiteEnStock(a.getSeuilAlerte() * 2);
                    articleService.modifier(a);
                    alertesDejaEnvoyees.remove(a.getId());
                }
                chargerDonnees();
                new Alert(Alert.AlertType.INFORMATION, "Stocks mis à jour !").show();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  EXPORT PDF ALERTES
    // ══════════════════════════════════════════════════════
    @FXML
    void exporterPDF(ActionEvent event) {
        ObservableList<Article> data = tableArticles.getItems();
        if (data.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune donnée à exporter.").show();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Inventaire_Stock.pdf");
        File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Titre
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
                document.add(new Paragraph("AGROFLOW - INVENTAIRE DES STOCKS\n\n", titleFont));
                document.add(new Paragraph("Date : " + java.time.LocalDate.now() + "\n\n"));

                // Tableau (7 ou 8 colonnes selon si admin)
                boolean isAdmin = (currentUser != null && currentUser.getRole() == 3);
                int colCount = isAdmin ? 8 : 7;
                PdfPTable table = new PdfPTable(colCount);
                table.setWidthPercentage(100);

                // En-têtes
                String[] headers = isAdmin 
                    ? new String[]{"Nom", "Agriculteur", "Catégorie", "Qte", "Unité", "Prix", "Devise", "Seuil"}
                    : new String[]{"Nom", "Catégorie", "Qte", "Unité", "Prix", "Devise", "Seuil"};

                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                    cell.setBackgroundColor(new BaseColor(45, 90, 39));
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Données
                for (Article a : data) {
                    table.addCell(a.getNom());
                    if (isAdmin) table.addCell(a.getNomAgriculteur() != null ? a.getNomAgriculteur() : "-");
                    table.addCell(a.getNomCategorie());
                    table.addCell(String.valueOf(a.getQuantiteEnStock()));
                    table.addCell(a.getUniteMesure());
                    table.addCell(String.valueOf(a.getPrixUnitaire()));
                    table.addCell(a.getDevise());
                    table.addCell(String.valueOf(a.getSeuilAlerte()));
                }

                document.add(table);
                document.close();
                new Alert(Alert.AlertType.INFORMATION, "PDF généré avec succès !").show();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    void exporterExcel(ActionEvent event) {
        ObservableList<Article> data = tableArticles.getItems();
        if (data.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune donnée à exporter.").show();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Inventaire_Stock.xlsx");
        File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Stocks");
                
                // Style En-tête
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // En-têtes
                boolean isAdmin = (currentUser != null && currentUser.getRole() == 3);
                String[] headers = isAdmin 
                    ? new String[]{"Nom", "Agriculteur", "Catégorie", "Quantité", "Unité", "Prix", "Devise", "Seuil"}
                    : new String[]{"Nom", "Catégorie", "Quantité", "Unité", "Prix", "Devise", "Seuil"};

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Données
                int rowNum = 1;
                for (Article a : data) {
                    Row row = sheet.createRow(rowNum++);
                    int col = 0;
                    row.createCell(col++).setCellValue(a.getNom());
                    if (isAdmin) row.createCell(col++).setCellValue(a.getNomAgriculteur() != null ? a.getNomAgriculteur() : "-");
                    row.createCell(col++).setCellValue(a.getNomCategorie());
                    row.createCell(col++).setCellValue(a.getQuantiteEnStock());
                    row.createCell(col++).setCellValue(a.getUniteMesure());
                    row.createCell(col++).setCellValue(a.getPrixUnitaire());
                    row.createCell(col++).setCellValue(a.getDevise());
                    row.createCell(col++).setCellValue(a.getSeuilAlerte());
                }

                // Ajuster colonnes
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                new Alert(Alert.AlertType.INFORMATION, "Fichier Excel généré avec succès !").show();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    void exporterAlertesPDF(ActionEvent event) {
        List<Article> articlesEnAlerte = masterData.stream()
                .filter(a -> a.getQuantiteEnStock() <= a.getSeuilAlerte())
                .collect(Collectors.toList());

        if (articlesEnAlerte.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune alerte à exporter.").show();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Alertes_Stock.pdf");
        File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(new Paragraph("AGROFLOW - RAPPORT D'ALERTES\n\n",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.RED)));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                Stream.of("Article", "Stock", "Seuil", "Unité").forEach(t -> {
                    PdfPCell h = new PdfPCell(new Phrase(t,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                    h.setBackgroundColor(new BaseColor(45, 90, 39));
                    table.addCell(h);
                });

                for (Article a : articlesEnAlerte) {
                    table.addCell(a.getNom());
                    table.addCell(String.valueOf(a.getQuantiteEnStock()));
                    table.addCell(String.valueOf(a.getSeuilAlerte()));
                    table.addCell(a.getUniteMesure());
                }
                document.add(table);
                document.close();
                new Alert(Alert.AlertType.INFORMATION, "Rapport exporté !").show();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  QR CODE
    // ══════════════════════════════════════════════════════
    private void afficherQR(Article a) {
        if (lblNomSelection != null) lblNomSelection.setText(a.getNom());
        if (ivQRCode != null)
            ivQRCode.setImage(new Image(articleService.genererLienQRCode(a), true));
    }

    @FXML
    void enregistrerQRCode(ActionEvent event) {
        if (ivQRCode != null && ivQRCode.getImage() != null) {
            FileChooser fc = new FileChooser();
            String nom = (lblNomSelection != null)
                    ? lblNomSelection.getText().replace(" ", "_") : "article";
            fc.setInitialFileName("QR_" + nom + ".png");
            File file = fc.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(ivQRCode.getImage(), null), "png", file);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  COLONNES ACTIONS (Modifier / Supprimer)
    // ══════════════════════════════════════════════════════
    private void configurerColonneActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("📝 MODIF");
            private final Button btnMvt  = new Button("📈 MVT");
            private final Button btnHist = new Button("📋 HISTO");
            private final Button btnDel  = new Button("🗑️ SUPP");
            private final HBox container = new HBox(btnEdit, btnMvt, btnHist, btnDel);

            {
                container.setSpacing(6);
                container.setStyle("-fx-alignment: center; -fx-padding: 0 5 0 5;");
                
                // Tooltips explicites
                btnEdit.setTooltip(new Tooltip("Modifier les détails de l'article"));
                btnMvt.setTooltip(new Tooltip("Ajouter une entrée ou sortie de stock"));
                btnHist.setTooltip(new Tooltip("Consulter tous les mouvements de cet article"));
                btnDel.setTooltip(new Tooltip("Supprimer cet article du stock"));

                String commonStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6; -fx-font-size: 10px; -fx-padding: 5 8 5 8;";
                
                btnEdit.setStyle("-fx-background-color: #f39c12; " + commonStyle);
                btnMvt.setStyle("-fx-background-color: #27ae60; " + commonStyle);
                btnHist.setStyle("-fx-background-color: #2980b9; " + commonStyle);
                btnDel.setStyle("-fx-background-color: #e74c3c; " + commonStyle);

                btnEdit.setOnAction(e -> ouvrirFormulaire(getTableView().getItems().get(getIndex()), e));
                btnMvt.setOnAction(e -> ouvrirMouvement(getTableView().getItems().get(getIndex())));
                btnHist.setOnAction(e -> ouvrirHistoriqueArticle(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e -> supprimer(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void ouvrirMouvement(Article a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/enregistrerMouvement.fxml"));
            Parent root = loader.load();
            enregistrerMouvementController ctrl = loader.getController();
            ctrl.setArticle(a);
            
            Stage stage = new Stage();
            stage.setTitle("Enregistrer - " + a.getNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            if (ctrl.isSuccess()) {
                chargerDonnees();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void ouvrirHistoriqueArticle(Article a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/historiqueMouvements.fxml"));
            Parent root = loader.load();
            historiqueMouvementsController ctrl = loader.getController();
            ctrl.chargerHistoriqueArticle(a.getId());
            
            Stage stage = new Stage();
            stage.setTitle("Historique - " + a.getNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void voirHistoriqueGlobal(ActionEvent event) {
        if (currentUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/historiqueMouvements.fxml"));
            Parent root = loader.load();
            historiqueMouvementsController ctrl = loader.getController();
            ctrl.chargerHistoriqueGlobal(currentUser.getCin());
            
            Stage stage = new Stage();
            stage.setTitle("Mon Historique de Mouvements");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════════════
    //  STYLE DES LIGNES
    // ══════════════════════════════════════════════════════
    private void configurerStyleLignes() {
        tableArticles.setRowFactory(tv -> new TableRow<Article>() {
            @Override protected void updateItem(Article a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) setStyle("");
                else if (a.getQuantiteEnStock() <= a.getSeuilAlerte())
                    setStyle("-fx-background-color: #fab1a0;");
                else
                    setStyle("-fx-background-color: #fdfae7;");
            }
        });
    }

    private void supprimer(Article a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + a.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            try {
                articleService.supprimer(a.getId());
                chargerDonnees();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void ouvrirFormulaire(Article a, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/StocksInterface/ajouterarticle.fxml"));
            Parent root = loader.load();
            if (a != null) {
                ajouterarticleController ctrl = loader.getController();
                ctrl.preparerModification(a);
            }

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void ouvrirFormulaireAjout(ActionEvent event) { ouvrirFormulaire(null, event); }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION INTERNE (Stocks)
    // ══════════════════════════════════════════════════════
    @FXML void allerVersCategories(MouseEvent event) throws IOException {
        changerScene("/StocksInterface/affichercategorie.fxml", event);
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR (autres modules)
    // ══════════════════════════════════════════════════════
    @FXML public void handleDashboard(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/Acceuil.fxml", event);
    }
    @FXML public void handleDashboardAgricole(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/AcceuillAgr.fxml", event);
    }
    @FXML public void handleAnimals(MouseEvent event) throws IOException {
        changerScene("/AnimauxInterface/AfficherAnimaux.fxml", event);
    }
    @FXML public void handleMesAnimaux(MouseEvent event) throws IOException {
        changerScene("/AnimauxInterface/AfficherAnimaux.fxml", event);
    }
    @FXML public void handleStocks(MouseEvent event) throws IOException {
        Personne currentUser = SessionManager.getCurrentUser();
        String fxml = (currentUser != null && currentUser.getRole() == 2)
                ? "/StocksInterface/AfficherArticleAgr.fxml"
                : "/StocksInterface/afficherarticle.fxml";
        changerScene(fxml, event);
    }
    @FXML public void handleMesStocks(ActionEvent event) throws IOException {
        handleStocks(null);
    }
    @FXML public void handleMesArticles(MouseEvent event) throws IOException {
        handleStocks(event);
    }
    @FXML public void handleMesCatégories(MouseEvent event) throws IOException {
        Personne currentUser = SessionManager.getCurrentUser();
        String fxml = (currentUser != null && currentUser.getRole() == 2)
                ? "/StocksInterface/AfficherCategorieAgr.fxml"
                : "/StocksInterface/affichercategorie.fxml";
        changerScene(fxml, event);
    }
    @FXML public void handleTerrains(MouseEvent event) throws IOException {
        changerScene("/TerrainsInterface/acceuilterrain.fxml", event);
    }
    @FXML public void handleMesTerrains(MouseEvent event) throws IOException {
        changerScene("/TerrainsInterface/acceuilterrain.fxml", event);
    }
    @FXML public void handleMonMateriel(MouseEvent event) throws IOException {
        changerScene("/MaterielsInterface/AgricoleAffichageMachine.fxml", event);
    }
    @FXML public void handleMonAbonnement(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/MesAbonnements.fxml", event);
    }
    @FXML public void handleMesEvenements(MouseEvent event) throws IOException {
        changerScene("/G-Evenements/AfficherEvenementsUser.fxml", event);
    }
    @FXML void ouvrirTerrains(MouseEvent event) throws IOException {
        changerScene("/TerrainsInterface/agricoleaffichageterrain.fxml", event);
    }
    @FXML void ouvrirPlantes(MouseEvent event) throws IOException {
        changerScene("/TerrainsInterface/agricoleaffichageplante.fxml", event);
    }
    @FXML void ouvrirRotations(MouseEvent event) throws IOException {
        changerScene("/TerrainsInterface/agricoleaffichagerotation.fxml", event);
    }

    @FXML public void handleEvents(MouseEvent event) throws IOException {
        changerScene("/G-Evenements/Accueil.fxml", event);
    }
    @FXML public void handleMateriels(MouseEvent event) throws IOException {
        changerScene("/MaterielsInterface/AccueilMateriel.fxml", event);
    }
    @FXML private void handlePersonnes(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/DahboardPersonne.fxml", event);
    }
    @FXML private void handleTaches(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/GestionTache.fxml", event);
    }
    @FXML private void handleAbonnements(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/GestionAbonnements.fxml", event);
    }
    @FXML private void handleOffres(MouseEvent event) throws IOException {
        changerScene("/UsersInterface/GestionOffre.fxml", event);
    }
    @FXML private void handleGestion(MouseEvent event) { /* Vue principale Gestion */ }

    // ══════════════════════════════════════════════════════
    //  DÉCONNEXION
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  SIDEBAR SUBMENU
    // ══════════════════════════════════════════════════════
    private void showGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════
    // ══════════════════════════════════════════════════════
    //  UTILITAIRES & NAVIGATION
    // ══════════════════════════════════════════════════════
    private void changerScene(String fxml, Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setScene(new Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.show();
        } catch (IOException e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    public Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null) return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        updateUserLabels();
    }

}