package controllers.Materiels;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
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

public class AgricoleMaintenanceController implements Initializable {

    @FXML private Button dashboardBtn;
    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;

    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;

    // ── Colonnes tableau (TOUS les attributs) ──
    @FXML private TableView<Maintenance> tableMaintenances;
    @FXML private TableColumn<Maintenance, String>  colMachine;
    @FXML private TableColumn<Maintenance, String>  colTypePanne;
    @FXML private TableColumn<Maintenance, String>  colDate;
    @FXML private TableColumn<Maintenance, Double>  colCout;
    @FXML private TableColumn<Maintenance, String>  colDescription;
    @FXML private TableColumn<Maintenance, String>  colStatut;
    @FXML private TableColumn<Maintenance, String>  colPriorite;
    @FXML private TableColumn<Maintenance, Integer> colKilometrage;
    @FXML private TableColumn<Maintenance, String>  colRecommandation;

    @FXML private Pagination pagination;

    @FXML private TextField      champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private ComboBox<String> comboTypePanne;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboPriorite;
    @FXML private Label            lblSelectionInfo;

    // ── Cartes statistiques ──
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

    // ── Alerte IA ──
    @FXML private VBox  alertContainer;
    @FXML private Label alertTitleLabel;
    @FXML private Label alertMessageLabel;
    @FXML private Button alertCloseButton;

    @FXML private Button logoutBtn;

    private MaintenanceService    maintenanceService;
    private MachineService        machineService;
    private MaintenanceApiService apiService;

    private List<Machine>         machines           = new ArrayList<>();
    private final ObservableList<Maintenance> masterList = FXCollections.observableArrayList();
    private List<Maintenance>     currentFilteredList = new ArrayList<>();

    public void handlemesouvriers(MouseEvent mouseEvent) {
    }

    private enum SortMode { DATE_DESC, DATE_ASC, COUT_ASC, COUT_DESC, MACHINE_AZ, NONE }
    private SortMode currentSort = SortMode.DATE_DESC;
    private static final int ROWS_PER_PAGE = 10;

    // ══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.currentUser = SessionManager.getCurrentUser();
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        maintenanceService = new MaintenanceService();
        machineService     = new MachineService();
        apiService         = new MaintenanceApiService();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();
        configurerPagination();

        if (alertContainer != null) {
            alertContainer.setVisible(false);
            alertContainer.setManaged(false);
        }

        // Sélection d'une ligne → afficher détails + recommandation IA
        tableMaintenances.getSelectionModel().selectedItemProperty()
                .addListener((obs, ov, nv) -> {
                    if (lblSelectionInfo != null) {
                        lblSelectionInfo.setText(nv != null
                                ? "Sélectionné : " + nomMachineParId(nv.getIdM())
                                + " — " + nv.getTypePanne()
                                + " | Statut : " + formatterStatut(nv.getStatut())
                                + " | Priorité : " + formatterPriorite(nv.getPriorite())
                                + " | Km : " + nv.getKilometrage()
                                : "Cliquez sur une ligne pour voir les détails");
                    }
                    if (nv != null && nv.getRecommandation() != null && !nv.getRecommandation().isEmpty()) {
                        showAlert("💡 Recommandation IA pour " + nomMachineParId(nv.getIdM()),
                                nv.getRecommandation(), "info");
                    }
                });
    }

    // ══════════════════════════════════════════════════════════════
    // CONFIGURATION TABLEAU — tous les attributs
    // ══════════════════════════════════════════════════════════════
    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colMachine.setCellValueFactory(d ->
                new SimpleStringProperty(nomMachineParId(d.getValue().getIdM())));

        colTypePanne.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTypePanne()));

        colDate.setCellValueFactory(d -> {
            LocalDate date = d.getValue().getDateMain();
            return new SimpleStringProperty(date != null ? date.format(fmt) : "—");
        });

        colCout.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getCout()).asObject());

        colDescription.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDescription() != null
                        ? d.getValue().getDescription() : "—"));

        colStatut.setCellValueFactory(d ->
                new SimpleStringProperty(formatterStatut(d.getValue().getStatut())));

        colPriorite.setCellValueFactory(d ->
                new SimpleStringProperty(formatterPriorite(d.getValue().getPriorite())));

        colKilometrage.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getKilometrage()).asObject());

        colRecommandation.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRecommandation() != null
                        && !d.getValue().getRecommandation().isBlank()
                        ? "✔ Disponible" : "—"));

        // ── Cellule Coût colorée ──
        colCout.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.2f DT", val));
                if      (val > 1000) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                else if (val > 500)  setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                else                  setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        // ── Cellule Statut colorée ──
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if      (item.contains("Terminé"))  setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                else if (item.contains("En cours")) setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                else if (item.contains("Planifié")) setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
            }
        });

        // ── Cellule Priorité colorée ──
        colPriorite.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if      (item.contains("Urgente")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                else if (item.contains("Haute"))   setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                else if (item.contains("Moyenne")) setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                else                                setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        // ── Cellule Recommandation cliquable ──
        colRecommandation.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equals("✔ Disponible")) {
                    setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-cursor: hand;");
                    setOnMouseClicked(ev -> {
                        Maintenance m = getTableView().getItems().get(getIndex());
                        if (m != null && m.getRecommandation() != null) {
                            afficherRecommandationDetail(m);
                        }
                    });
                } else {
                    setStyle("-fx-text-fill: #95a5a6;");
                    setOnMouseClicked(null);
                }
            }
        });

        // ── Lignes alternées + rouge si coût élevé ──
        tableMaintenances.setRowFactory(tv -> new TableRow<Maintenance>() {
            @Override protected void updateItem(Maintenance item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setStyle(""); return; }
                if (item.getCout() > 1000)   setStyle("-fx-background-color: #fff5f5;");
                else if (getIndex() % 2 == 1) setStyle("-fx-background-color: #f0f9f0;");
                else                           setStyle("");
            }
        });

        tableMaintenances.setEditable(false);
    }

    /** Fenêtre détail de la recommandation IA */
    private void afficherRecommandationDetail(Maintenance m) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("💡 Recommandation IA — " + nomMachineParId(m.getIdM()));

        Label titre = new Label("💡 Recommandation IA");
        titre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        Label info = new Label("Machine : " + nomMachineParId(m.getIdM())
                + "   |   Type : " + m.getTypePanne()
                + "   |   Priorité : " + formatterPriorite(m.getPriorite()));
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: #7F8C8D;");

        TextArea ta = new TextArea(m.getRecommandation());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefHeight(320);
        ta.setStyle("-fx-font-size: 13px; -fx-background-color: #f9f9f9;");

        Button btnFermer = new Button("✕  Fermer");
        btnFermer.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 20;");
        btnFermer.setOnAction(e -> stage.close());

        VBox root = new VBox(12, titre, info, new Separator(), ta, btnFermer);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(ta, Priority.ALWAYS);

        stage.setScene(new Scene(root, 600, 460));
        stage.show();
    }

    private String formatterStatut(String statut) {
        if (statut == null) return "—";
        return switch (statut) {
            case "en_cours" -> "🔧 En cours";
            case "termine"  -> "✅ Terminé";
            case "planifie" -> "📅 Planifié";
            default         -> statut;
        };
    }

    private String formatterPriorite(String priorite) {
        if (priorite == null) return "—";
        return switch (priorite) {
            case "urgente" -> "🔴 Urgente";
            case "haute"   -> "🟠 Haute";
            case "moyenne" -> "🟡 Moyenne";
            case "faible"  -> "🟢 Faible";
            default        -> priorite;
        };
    }

    // ══════════════════════════════════════════════════════════════
    // PAGINATION
    // ══════════════════════════════════════════════════════════════
    private void configurerPagination() {
        if (pagination != null) pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        int from = pageIndex * ROWS_PER_PAGE;
        int to   = Math.min(from + ROWS_PER_PAGE, currentFilteredList.size());
        if (from >= currentFilteredList.size()) return new Label("Aucune donnée");
        tableMaintenances.setItems(FXCollections.observableArrayList(
                currentFilteredList.subList(from, to)));
        return tableMaintenances;
    }

    private void updatePagination() {
        if (pagination != null) {
            int pages = (int) Math.ceil((double) currentFilteredList.size() / ROWS_PER_PAGE);
            pagination.setPageCount(pages > 0 ? pages : 1);
            pagination.setCurrentPageIndex(0);
            createPage(0);
        } else {
            tableMaintenances.setItems(FXCollections.observableArrayList(currentFilteredList));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CHARGEMENT DES DONNÉES
    // ══════════════════════════════════════════════════════════════
    private void chargerDonnees() {
        try { machines = machineService.recuperer(); }
        catch (SQLException e) { showErr("Erreur chargement machines", e.getMessage()); }
        chargerMaintenances();
        initialiserCombos();
    }

    public void rafraichirTableau() { chargerMaintenances(); }

    private void chargerMaintenances() {
        masterList.clear();
        try {
            masterList.addAll(maintenanceService.recuperer());
            verifierAlertesIA();
        } catch (SQLException e) { showErr("Erreur SQL", e.getMessage()); }
        appliquerFiltres();
    }

    private void verifierAlertesIA() {
        List<Maintenance> alertes = masterList.stream()
                .filter(m -> {
                    boolean urgent  = "urgente".equalsIgnoreCase(m.getPriorite());
                    boolean hautCout = m.getCout() > 1000;
                    boolean overdue  = m.getDateMain() != null
                            && m.getDateMain().isBefore(LocalDate.now())
                            && !"termine".equalsIgnoreCase(m.getStatut());
                    return urgent || hautCout || overdue;
                })
                .collect(Collectors.toList());

        if (!alertes.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append(alertes.size()).append(" maintenance(s) nécessitent votre attention :\n\n");
            alertes.stream().limit(3).forEach(m -> {
                msg.append("• ").append(nomMachineParId(m.getIdM()))
                        .append(" — ").append(m.getTypePanne());
                if ("urgente".equalsIgnoreCase(m.getPriorite())) msg.append("  [⚠ URGENT]");
                if (m.getCout() > 1000)                          msg.append("  [💸 COÛT ÉLEVÉ]");
                if (m.getDateMain() != null && m.getDateMain().isBefore(LocalDate.now())
                        && !"termine".equalsIgnoreCase(m.getStatut()))
                    msg.append("  [📅 EN RETARD]");
                msg.append("\n");
            });
            if (alertes.size() > 3)
                msg.append("... et ").append(alertes.size() - 3).append(" autre(s)");
            showAlert("⚠️ Alertes IA — Attention requise", msg.toString(), "warning");
        }
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
            masterList.stream().map(Maintenance::getTypePanne)
                    .filter(Objects::nonNull).forEach(types::add);
            comboTypePanne.setItems(FXCollections.observableArrayList(types));
            comboTypePanne.getSelectionModel().selectFirst();
        }
        if (comboStatut != null) {
            comboStatut.getItems().addAll("Tous les statuts","🔧 En cours","✅ Terminé","📅 Planifié");
            comboStatut.getSelectionModel().selectFirst();
        }
        if (comboPriorite != null) {
            comboPriorite.getItems().addAll(
                    "Toutes les priorités","🔴 Urgente","🟠 Haute","🟡 Moyenne","🟢 Faible");
            comboPriorite.getSelectionModel().selectFirst();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ALERTE IA (bandeau)
    // ══════════════════════════════════════════════════════════════
    private void showAlert(String title, String message, String type) {
        if (alertContainer == null) return;
        Platform.runLater(() -> {
            alertTitleLabel.setText(title);
            alertMessageLabel.setText(message);
            String color = switch (type) {
                case "warning" -> "#F39C12";
                case "error"   -> "#E74C3C";
                default        -> "#27AE60";
            };
            alertContainer.setStyle(
                    "-fx-background-color: " + color + "20; -fx-background-radius: 10; "
                            + "-fx-border-color: " + color + "; -fx-border-radius: 10; -fx-border-width: 2;");
            alertTitleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            alertContainer.setVisible(true);
            alertContainer.setManaged(true);

            new Thread(() -> {
                try { Thread.sleep(10000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                Platform.runLater(() -> {
                    if (alertContainer != null) {
                        alertContainer.setVisible(false);
                        alertContainer.setManaged(false);
                    }
                });
            }).start();
        });
    }

    @FXML private void closeAlert() {
        if (alertContainer != null) {
            alertContainer.setVisible(false);
            alertContainer.setManaged(false);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // FILTRES & RECHERCHE
    // ══════════════════════════════════════════════════════════════
    private void configurerRecherche() {
        if (champRecherche != null)
            champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    @FXML private void rechercher()        { appliquerFiltres(); }
    @FXML private void filtrer()           { appliquerFiltres(); }
    @FXML private void effacerRecherche()  { if (champRecherche != null) champRecherche.clear(); appliquerFiltres(); }
    @FXML private void trierPlusRecent()   { currentSort = SortMode.DATE_DESC;  appliquerFiltres(); }
    @FXML private void trierPlusAncien()   { currentSort = SortMode.DATE_ASC;   appliquerFiltres(); }
    @FXML private void trierCoutEleve()    { currentSort = SortMode.COUT_DESC;  appliquerFiltres(); }
    @FXML private void trierCoutFaible()   { currentSort = SortMode.COUT_ASC;   appliquerFiltres(); }
    @FXML private void trierMachineAZ()    { currentSort = SortMode.MACHINE_AZ; appliquerFiltres(); }

    @FXML private void reinitialiserFiltres() {
        if (champRecherche != null) champRecherche.clear();
        if (comboMachine   != null) comboMachine.getSelectionModel().selectFirst();
        if (comboTypePanne != null) comboTypePanne.getSelectionModel().selectFirst();
        if (comboStatut    != null) comboStatut.getSelectionModel().selectFirst();
        if (comboPriorite  != null) comboPriorite.getSelectionModel().selectFirst();
        currentSort = SortMode.DATE_DESC;
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        String recherche   = champRecherche != null && champRecherche.getText() != null
                ? champRecherche.getText().toLowerCase().trim() : "";
        String machineSel  = comboMachine   != null && comboMachine.getValue()   != null
                ? comboMachine.getValue()   : "Toutes les machines";
        String typeSel     = comboTypePanne != null && comboTypePanne.getValue() != null
                ? comboTypePanne.getValue() : "Tous les types";
        String statutSel   = comboStatut    != null && comboStatut.getValue()    != null
                ? comboStatut.getValue()    : "Tous les statuts";
        String prioriteSel = comboPriorite  != null && comboPriorite.getValue()  != null
                ? comboPriorite.getValue()  : "Toutes les priorités";

        List<Maintenance> filtered = masterList.stream().filter(m -> {
            String nomM = nomMachineParId(m.getIdM());
            boolean matchR = recherche.isEmpty()
                    || nomM.toLowerCase().contains(recherche)
                    || (m.getTypePanne()       != null && m.getTypePanne().toLowerCase().contains(recherche))
                    || (m.getDescription()     != null && m.getDescription().toLowerCase().contains(recherche))
                    || (m.getStatut()          != null && m.getStatut().toLowerCase().contains(recherche))
                    || (m.getPriorite()        != null && m.getPriorite().toLowerCase().contains(recherche))
                    || (m.getRecommandation()  != null && m.getRecommandation().toLowerCase().contains(recherche))
                    || String.valueOf(m.getCout()).contains(recherche)
                    || String.valueOf(m.getKilometrage()).contains(recherche)
                    || (m.getDateMain() != null && m.getDateMain().toString().contains(recherche));

            boolean matchM = machineSel.equals("Toutes les machines")  || nomM.equals(machineSel);
            boolean matchT = typeSel.equals("Tous les types")
                    || (m.getTypePanne() != null && m.getTypePanne().equals(typeSel));
            boolean matchS = statutSel.equals("Tous les statuts")
                    || formatterStatut(m.getStatut()).equals(statutSel);
            boolean matchP = prioriteSel.equals("Toutes les priorités")
                    || formatterPriorite(m.getPriorite()).equals(prioriteSel);

            return matchR && matchM && matchT && matchS && matchP;
        }).collect(Collectors.toList());

        switch (currentSort) {
            case DATE_ASC  -> filtered.sort(Comparator.comparing(
                    m -> m.getDateMain() != null ? m.getDateMain() : LocalDate.MIN));
            case DATE_DESC -> filtered.sort(Comparator.comparing(
                    (Maintenance m) -> m.getDateMain() != null ? m.getDateMain() : LocalDate.MIN).reversed());
            case COUT_ASC  -> filtered.sort(Comparator.comparingDouble(Maintenance::getCout));
            case COUT_DESC -> filtered.sort(Comparator.comparingDouble(Maintenance::getCout).reversed());
            case MACHINE_AZ-> filtered.sort(Comparator.comparing(m -> nomMachineParId(m.getIdM()).toLowerCase()));
            default -> {}
        }

        currentFilteredList = filtered;
        updatePagination();
        mettreAJourStats();
    }

    private void mettreAJourStats() {
        int nb     = currentFilteredList.size();
        double tot = currentFilteredList.stream().mapToDouble(Maintenance::getCout).sum();
        double moy = nb > 0 ? tot / nb : 0;

        long nbEleves  = currentFilteredList.stream().filter(m -> m.getCout() > moy).count();
        double pctEl   = nb > 0 ? (nbEleves * 100.0 / nb) : 0;
        double pctSous = nb > 0 ? ((nb - nbEleves) * 100.0 / nb) : 0;

        Map<Integer, Double> coutParM = new HashMap<>();
        currentFilteredList.forEach(m -> coutParM.merge(m.getIdM(), m.getCout(), Double::sum));
        int    idMax   = coutParM.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(-1);
        String nomMax  = idMax >= 0 ? nomMachineParId(idMax) : "—";
        double coutMax = idMax >= 0 ? coutParM.get(idMax) : 0;
        double pctMach = tot > 0 ? (coutMax * 100.0 / tot) : 0;

        Map<String,Long> typeCount = new HashMap<>();
        currentFilteredList.forEach(m -> {
            String t = m.getTypePanne() != null ? m.getTypePanne() : "Inconnu";
            typeCount.merge(t, 1L, Long::sum);
        });
        String typeDom = typeCount.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("—");
        long nbTypeDom = typeCount.getOrDefault(typeDom, 0L);

        if (lblTotalBas         != null) lblTotalBas.setText(String.valueOf(nb));
        if (lblPctTotal         != null) lblPctTotal.setText("100%");
        if (lblCoutTotalBas     != null) lblCoutTotalBas.setText(String.format("%.2f DT", tot));
        if (lblPctCoutEleve     != null) lblPctCoutEleve.setText(String.format("%.0f%% élevés", pctEl));
        if (lblCoutMoyenBas     != null) lblCoutMoyenBas.setText(String.format("%.2f DT", moy));
        if (lblPctSousLaMoyenne != null) lblPctSousLaMoyenne.setText(String.format("%.0f%% sous moy.", pctSous));
        if (lblMachineCouteuseBas != null) lblMachineCouteuseBas.setText(nomMax);
        if (lblPctMachine       != null) lblPctMachine.setText(String.format("%.0f%% du total", pctMach));
        if (lblTypeDominant     != null) lblTypeDominant.setText(typeDom);
        if (lblNbTypeDominant   != null) lblNbTypeDominant.setText(nbTypeDom + " occurrence(s)");
    }

    // ══════════════════════════════════════════════════════════════
    // STATISTIQUES GRAPHIQUES
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void afficherStatistiques() {
        if (currentFilteredList.isEmpty()) { showInfo("Stats", "Aucune donnée à afficher."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📊 Statistiques des Maintenances");
        stage.setResizable(true);

        Map<String, Double> coutMap  = new LinkedHashMap<>();
        Map<String, Long>   typeMap  = new LinkedHashMap<>();
        Map<String, Long>   statMap  = new LinkedHashMap<>();

        for (Maintenance m : currentFilteredList) {
            String nom = nomMachineParId(m.getIdM());
            coutMap.merge(nom, m.getCout(), Double::sum);
            String t = m.getTypePanne() != null ? m.getTypePanne() : "Inconnu";
            typeMap.merge(t, 1L, Long::sum);
            String s = formatterStatut(m.getStatut());
            statMap.merge(s, 1L, Long::sum);
        }

        Color[] colors = {
                Color.web("#27ae60"), Color.web("#3498db"), Color.web("#e74c3c"),
                Color.web("#f39c12"), Color.web("#9b59b6"), Color.web("#1abc9c"),
                Color.web("#e67e22"), Color.web("#e91e63")
        };

        List<String> machKeys = new ArrayList<>(coutMap.keySet());
        List<Double> machVals = new ArrayList<>(coutMap.values());
        List<String> typeKeys = new ArrayList<>(typeMap.keySet());
        List<Long>   typeVals = new ArrayList<>(typeMap.values());

        int barW = 55, gap = 25, padL = 75, padTop = 50, chartH = 250;
        int nM   = machKeys.size();
        int cwL  = padL + nM * (barW + gap) + gap + 20;
        int chL  = chartH + 90 + padTop;

        Canvas canvasL = new Canvas(cwL, chL);
        GraphicsContext gl = canvasL.getGraphicsContext2D();
        gl.setFill(Color.WHITE); gl.fillRect(0, 0, cwL, chL);
        gl.setFill(Color.web("#2E7D32"));
        gl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gl.fillText("Coût total par machine (DT)", padL, 30);

        double maxCout = machVals.stream().mapToDouble(v -> v).max().orElse(1);
        if (maxCout == 0) maxCout = 1;
        double coutMoyG = currentFilteredList.stream().mapToDouble(Maintenance::getCout).average().orElse(0);

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
            Color c = colors[i % colors.length];
            gl.setFill(Color.rgb(0, 0, 0, 0.07));
            gl.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);
            gl.setFill(c);
            gl.fillRoundRect(x, y, barW, barH, 6, 6);
            gl.setFill(Color.web("#2E7D32"));
            gl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            String vs = String.format("%.0f", machVals.get(i));
            gl.fillText(vs, x + barW / 2.0 - vs.length() * 3.5, y - 6);
            gl.setFont(Font.font("Arial", 10));
            String lbl = machKeys.get(i).length() > 11 ? machKeys.get(i).substring(0, 11) + "…" : machKeys.get(i);
            gl.fillText(lbl, x + barW / 2.0 - lbl.length() * 3, padTop + chartH + 16);
        }
        if (coutMoyG > 0 && coutMoyG <= maxCout) {
            double yMoy = padTop + chartH - (coutMoyG / maxCout) * chartH;
            gl.setStroke(Color.web("#e74c3c")); gl.setLineWidth(2); gl.setLineDashes(8, 4);
            gl.strokeLine(padL, yMoy, cwL - 20, yMoy);
            gl.setLineDashes();
            gl.setFill(Color.web("#e74c3c"));
            gl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gl.fillText(String.format("Moy. %.0f DT", coutMoyG), cwL - 80, yMoy - 4);
        }

        int nT  = typeKeys.size();
        int cwR = 420;
        int chR = padTop + 40 + nT * 42 + 20;

        Canvas canvasR = new Canvas(cwR, Math.max(chR, chL));
        GraphicsContext gr = canvasR.getGraphicsContext2D();
        gr.setFill(Color.WHITE); gr.fillRect(0, 0, cwR, Math.max(chR, chL));
        gr.setFill(Color.web("#2E7D32"));
        gr.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gr.fillText("Répartition par type de panne", 10, 30);

        long   maxType   = typeVals.stream().mapToLong(v -> v).max().orElse(1);
        int    barAreaW  = cwR - 160;
        for (int i = 0; i < nT; i++) {
            double bH = 24, y = padTop + i * 42;
            double bW = (typeVals.get(i) / (double) maxType) * barAreaW;
            Color c = colors[i % colors.length];
            gr.setFill(Color.rgb(0, 0, 0, 0.06));
            gr.fillRoundRect(153, y + 3, bW, bH, 6, 6);
            gr.setFill(c);
            gr.fillRoundRect(150, y, bW, bH, 6, 6);
            gr.setFill(Color.web("#2E7D32"));
            gr.setFont(Font.font("Arial", 11));
            String lbl = typeKeys.get(i).length() > 14 ? typeKeys.get(i).substring(0, 14) + "…" : typeKeys.get(i);
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

        Label legende = new Label("  ── Ligne rouge pointillée = coût moyen global par maintenance");
        legende.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox root = new VBox(8, legende, charts);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: white;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ══════════════════════════════════════════════════════════════
    // ANALYSE COÛT
    // ══════════════════════════════════════════════════════════════
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
        comboMach.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Machine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getNom());
            }
        });
        comboMach.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Machine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? "Sélectionner…" : m.getNom());
            }
        });
        comboMach.setDisable(true);
        rbMachine.setOnAction(e -> comboMach.setDisable(false));
        rbGlobal.setOnAction(e -> comboMach.setDisable(true));

        Button btnCalc = creerBouton("Calculer", "#27ae60");
        HBox ctrl = new HBox(15, rbGlobal, rbMachine, comboMach, btnCalc);
        ctrl.setAlignment(Pos.CENTER_LEFT);
        ctrl.setPadding(new Insets(12));
        ctrl.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        Label cTotal = creerLabelStat("—"), cMoy = creerLabelStat("—"),
                cNb    = creerLabelStat("—"), cMax = creerLabelStat("—"),
                cMin   = creerLabelStat("—");

        HBox cartes = new HBox(10,
                creerCarte("💰 Total (DT)", cTotal, "#27ae60"),
                creerCarte("📊 Moyenne",    cMoy,   "#3498db"),
                creerCarte("🔢 Nb",         cNb,    "#9b59b6"),
                creerCarte("⬆️ Max",        cMax,   "#e74c3c"),
                creerCarte("⬇️ Min",        cMin,   "#f39c12"));
        cartes.setPadding(new Insets(12));

        TableView<Map<String, Object>> tblSynth = new TableView<>();
        tblSynth.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSynth.setPrefHeight(160);
        TableColumn<Map<String,Object>,String> sNom = new TableColumn<>("Machine");
        sNom.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue().get("nom")));
        TableColumn<Map<String,Object>,String> sTot = new TableColumn<>("Total (DT)");
        sTot.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double)d.getValue().get("total"))));
        TableColumn<Map<String,Object>,String> sMoy = new TableColumn<>("Moyenne");
        sMoy.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double)d.getValue().get("moyenne"))));
        TableColumn<Map<String,Object>,String> sNbC = new TableColumn<>("Nb");
        sNbC.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nb"))));
        TableColumn<Map<String,Object>,String> sMax = new TableColumn<>("Max");
        sMax.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double)d.getValue().get("max"))));
        tblSynth.getColumns().addAll(sNom, sTot, sMoy, sNbC, sMax);

        TableView<Map<String, Object>> tblDet = new TableView<>();
        tblDet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblDet, Priority.ALWAYS);

        TableColumn<Map<String,Object>,String> dMach = new TableColumn<>("Machine");
        dMach.setCellValueFactory(d -> new SimpleStringProperty((String)d.getValue().get("nomMachine")));
        TableColumn<Map<String,Object>,String> dType = new TableColumn<>("Type");
        dType.setCellValueFactory(d -> new SimpleStringProperty((String)d.getValue().get("typePanne")));
        TableColumn<Map<String,Object>,String> dDate = new TableColumn<>("Date");
        dDate.setCellValueFactory(d -> new SimpleStringProperty((String)d.getValue().get("dateMain")));
        TableColumn<Map<String,Object>,String> dCout = new TableColumn<>("Coût (DT)");
        dCout.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (double)d.getValue().get("cout"))));
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

        VBox root = new VBox(10, ctrl, new VBox(6, titre, cartes), new Separator(),
                new VBox(6, new Label("Synthèse par machine :"), tblSynth),
                new Separator(), new VBox(6, lblTitreDet, tblDet));
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: #f5f6fa;");
        stage.setScene(new Scene(root, 980, 660));
        stage.show();
    }

    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 18;");
        return btn;
    }
    private Label creerLabelStat(String v) {
        Label l = new Label(v);
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        l.setWrapText(true);
        return l;
    }
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

    // ══════════════════════════════════════════════════════════════
    // EXPORT PDF
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void exporterMaintenancesPDF() {
        if (currentFilteredList.isEmpty()) {
            showInfo("Export PDF", "Aucune maintenance à exporter.\nVérifiez vos filtres."); return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("maintenances_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf"));
        File file = fc.showSaveDialog(tableMaintenances.getScene().getWindow());
        if (file == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int nb    = currentFilteredList.size();
        double tot = currentFilteredList.stream().mapToDouble(Maintenance::getCout).sum();
        double moy = nb > 0 ? tot / nb : 0;
        double max = currentFilteredList.stream().mapToDouble(Maintenance::getCout).max().orElse(0);

        try (PdfWriter wr = new PdfWriter(file.getAbsolutePath());
             PdfDocument pdf = new PdfDocument(wr);
             Document doc = new Document(pdf)) {

            DeviceRgb vertF  = new DeviceRgb(46, 125, 50);
            DeviceRgb vertC  = new DeviceRgb(200, 230, 201);
            DeviceRgb grisL  = new DeviceRgb(245, 246, 250);
            DeviceRgb rouge  = new DeviceRgb(231, 76, 60);
            DeviceRgb orange = new DeviceRgb(230, 126, 34);
            DeviceRgb vertOk = new DeviceRgb(39, 174, 96);
            DeviceRgb grisT  = new DeviceRgb(120, 120, 120);

            doc.add(new Paragraph("AgroFlow — Rapport des Maintenances")
                    .setFontSize(22).setBold().setFontColor(vertF).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Historique complet des maintenances de machines")
                    .setFontSize(12).setFontColor(grisT).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Généré le : " + LocalDate.now().format(fmt) + "   |   " + nb + " enregistrement(s)")
                    .setFontSize(10).setFontColor(grisT).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n").setFontSize(6));

            // KPI
            doc.add(new Paragraph("Résumé statistique").setFontSize(13).setBold().setFontColor(vertF));
            float[] kpiW = {150f, 150f, 150f, 150f};
            Table kpiTbl = new Table(UnitValue.createPointArray(kpiW)).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(14);
            String[][] kpiData = {{"Total maintenances", String.valueOf(nb)},
                    {"Coût total", String.format("%.2f DT", tot)},
                    {"Coût moyen", String.format("%.2f DT", moy)},
                    {"Coût max",   String.format("%.2f DT", max)}};
            for (String[] kv : kpiData)
                kpiTbl.addCell(new Cell().add(new Paragraph(kv[0]).setFontSize(9).setFontColor(vertF))
                        .setBackgroundColor(vertC).setTextAlignment(TextAlignment.CENTER).setPadding(6));
            for (String[] kv : kpiData)
                kpiTbl.addCell(new Cell().add(new Paragraph(kv[1]).setFontSize(12).setBold().setFontColor(vertF))
                        .setBackgroundColor(grisL).setTextAlignment(TextAlignment.CENTER).setPadding(8));
            doc.add(kpiTbl);

            // Tableau complet avec TOUS les attributs
            doc.add(new Paragraph("Détail des maintenances").setFontSize(13).setBold().setFontColor(vertF));
            doc.add(new Paragraph("\n").setFontSize(4));

            float[] colW = {80f, 90f, 55f, 60f, 55f, 55f, 55f, 135f};
            Table table  = new Table(UnitValue.createPointArray(colW)).setWidth(UnitValue.createPercentValue(100));

            for (String h : new String[]{"Machine","Type Panne","Date","Coût (DT)","Statut","Priorité","Km","Description"})
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(8).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(vertF).setTextAlignment(TextAlignment.CENTER).setPadding(5));

            boolean pair = true;
            for (Maintenance m : currentFilteredList) {
                DeviceRgb bg    = pair ? new DeviceRgb(255,255,255) : new DeviceRgb(242,247,242);
                pair = !pair;
                double cout = m.getCout();
                DeviceRgb cC    = cout > 1000 ? rouge : (cout > 500 ? orange : vertOk);
                String desc = m.getDescription() != null ? m.getDescription() : "—";
                if (desc.length() > 60) desc = desc.substring(0, 60) + "…";

                table.addCell(new Cell().add(new Paragraph(nomMachineParId(m.getIdM())).setFontSize(7)).setBackgroundColor(bg).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(m.getTypePanne() != null ? m.getTypePanne() : "—").setFontSize(7)).setBackgroundColor(bg).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(m.getDateMain() != null ? m.getDateMain().format(fmt) : "—").setFontSize(7)).setBackgroundColor(bg).setTextAlignment(TextAlignment.CENTER).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", cout)).setFontSize(7).setBold().setFontColor(cC)).setBackgroundColor(bg).setTextAlignment(TextAlignment.RIGHT).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(m.getStatut() != null ? m.getStatut() : "—").setFontSize(7)).setBackgroundColor(bg).setTextAlignment(TextAlignment.CENTER).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(m.getPriorite() != null ? m.getPriorite() : "—").setFontSize(7)).setBackgroundColor(bg).setTextAlignment(TextAlignment.CENTER).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(m.getKilometrage())).setFontSize(7)).setBackgroundColor(bg).setTextAlignment(TextAlignment.CENTER).setPadding(3));
                table.addCell(new Cell().add(new Paragraph(desc).setFontSize(7)).setBackgroundColor(bg).setPadding(3));
            }
            // Ligne totaux
            table.addCell(new Cell(1, 3).add(new Paragraph("TOTAL").setBold().setFontSize(9).setFontColor(vertF)).setBackgroundColor(vertC).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f DT", tot)).setBold().setFontSize(9).setFontColor(rouge)).setBackgroundColor(vertC).setTextAlignment(TextAlignment.RIGHT).setPadding(4));
            table.addCell(new Cell(1, 4).add(new Paragraph(nb + " maintenance(s)").setFontSize(8).setFontColor(vertF)).setBackgroundColor(vertC).setPadding(4));
            doc.add(table);

            doc.add(new Paragraph("\n").setFontSize(6));
            doc.add(new Paragraph("AgroFlow — Rapport généré automatiquement le " + LocalDate.now().format(fmt))
                    .setFontSize(9).setFontColor(grisT).setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            showErr("Erreur PDF", "Impossible de générer le PDF :\n" + e.getMessage()); return;
        }
        showInfo("✅ Export PDF réussi", "Rapport sauvegardé :\n" + file.getAbsolutePath());
    }

    // ══════════════════════════════════════════════════════════════
    // CRUD
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void ajouterMaintenance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/AgriAjoutMaintenance.fxml"));
            Parent root = loader.load();
            AgriAjoutMaintenanceController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("➕ Ajouter une maintenance");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.showAndWait();
            chargerMaintenances();
        } catch (IOException e) {
            showErr("Erreur", "Impossible d'ouvrir le formulaire d'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierMaintenance() {
        Maintenance sel = tableMaintenances.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Aucune sélection", "Veuillez sélectionner une maintenance à modifier."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaterielsInterface/AgriModifierMaintenance.fxml"));
            Parent root = loader.load();
            AgriModifierMaintenanceController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.setMaintenance(sel);
            ctrl.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("✏️ Modifier une maintenance");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.showAndWait();
            chargerMaintenances();
        } catch (IOException e) {
            showErr("Erreur", "Impossible d'ouvrir le formulaire de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerMaintenance() {
        Maintenance sel = tableMaintenances.getSelectionModel().getSelectedItem();
        if (sel == null) { showWarn("Aucune sélection", "Veuillez sélectionner une maintenance à supprimer."); return; }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Confirmation de suppression");
        conf.setHeaderText("⚠️ Supprimer la maintenance");
        conf.setContentText("Êtes-vous sûr de vouloir supprimer la maintenance de\n"
                + nomMachineParId(sel.getIdM()) + " du "
                + (sel.getDateMain() != null ? sel.getDateMain().toString() : "?") + " ?");
        conf.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                maintenanceService.supprimer(sel.getIdMain());
                showInfo("✅ Succès", "Maintenance supprimée avec succès.");
                chargerMaintenances();
            } catch (SQLException e) {
                showErr("Erreur", "Impossible de supprimer la maintenance : " + e.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════
    private void nav(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) throw new IOException("FXML introuvable : " + path);
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) tableMaintenances.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) { showErr("Navigation", "Impossible d'ouvrir : " + path + "\n" + e.getMessage()); }
    }

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

    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════
    private String nomMachineParId(int idM) {
        for (Machine m : machines) if (m.getIdM() == idM) return m.getNom();
        return "Machine #" + idM;
    }

    private void showInfo(String t, String m) { alert(Alert.AlertType.INFORMATION, t, m); }
    private void showErr(String t,  String m) { alert(Alert.AlertType.ERROR,       t, m); }
    private void showWarn(String t, String m) { alert(Alert.AlertType.WARNING,     t, m); }

    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;
        if (userNameLabel != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        if (sidebarAvatarImageView != null) sidebarAvatarImageView.setClip(new Circle(35, 35, 35));
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
            } catch (Exception e) { System.err.println("Avatar sidebar : " + e.getMessage()); }
        });
        thread.setDaemon(true); thread.start();
    }

    // ── Handlers navigation sidebar ──
    @FXML void ouvrirTerrains(MouseEvent e)   { chargerPage(e, "/TerrainsInterface/agricoleaffichageterrain.fxml",  "Gestion des Terrains"); }
    @FXML void ouvrirPlantes(MouseEvent e)    { chargerPage(e, "/TerrainsInterface/agricoleaffichageplante.fxml",   "Liste des Plantes"); }
    @FXML void ouvrirRotations(MouseEvent e)  { chargerPage(e, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations"); }

    private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root); stage.show();
        } catch (IOException e) { System.err.println("Erreur FXML : " + fxmlPath); e.printStackTrace(); }
    }

    @FXML private void handleMesArticles(MouseEvent e)       { navigateTo(e, "/StocksInterface/AfficherArticleAgr.fxml",      "Articles"); }
    @FXML private void handleMesCatégories(MouseEvent e)     { navigateTo(e, "/StocksInterface/AfficherCategorieAgr.fxml",    "Catégories"); }
    @FXML private void handleDashboardAgricole(MouseEvent e) { navigateTo(e, "/UsersInterface/AcceuillAgr.fxml",               "Dashboard"); }
    @FXML private void handleMesTerrains(MouseEvent e)       { navigateTo(e, "/TerrainsInterface/acceuilagricoleterrain.fxml", "Terrains"); }
    @FXML private void handleMesAnimaux(MouseEvent e)        { navigateTo(e, "/AnimalsInterface/acceuilagricoleanimaux.fxml",  "Animaux"); }
    @FXML private void handleMesStocks()                     { System.out.println("Stocks..."); }
    @FXML private void handleMonMateriel()                   { System.out.println("Matériel..."); }

    @FXML private void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setTitle("Mon Profil"); s.setScene(new Scene(root));
            s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen(); s.showAndWait();
        } catch (IOException e) { showError("Erreur " + e.getMessage()); }
    }

    @FXML private void handleMonAbonnement(MouseEvent e) { navigateTo(e, "/UsersInterface/MesAbonnements.fxml", "Mes Abonnements"); }
    @FXML private void handleAPropos(MouseEvent e)       { navigateTo(e, "/UsersInterface/ProfilAgricole.fxml", "À Propos"); }

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
        if (logoutBtn != null && logoutBtn.getScene() != null)
            return (Stage) logoutBtn.getScene().getWindow();
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

    public void ouvrirMaintenance(MouseEvent e) { navigateTo(e, "/MaterielsInterface/AgricoleAffichageMaintenance.fxml", "Maintenance"); }
    public void ouvrirMachine(MouseEvent e)     { navigateTo(e, "/MaterielsInterface/AgricoleAffichageMachine.fxml",     "Machine"); }
    public void handleMesEvenements(MouseEvent e) { navigateTo(e, "/G-Evenements/AfficherEvenementsUser.fxml",         "Événements"); }
    public void ouvrirParticipations(MouseEvent e){ navigateTo(e, "/G-Evenements/AfficherParticipationsUser.fxml",     "Participations"); }
}