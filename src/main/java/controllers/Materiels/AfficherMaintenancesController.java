package controllers.Materiels;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Materiels.Maintenance;
import models.Materiels.Machine;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

public class AfficherMaintenancesController implements Initializable {

    // =========================================================
    //  FXML FIELDS
    // =========================================================

    @FXML private TableView<Maintenance>           tableMaintenances;
    @FXML private TableColumn<Maintenance, String> colMachine;
    @FXML private TableColumn<Maintenance, String> colTypePanne;
    @FXML private TableColumn<Maintenance, String> colDate;
    @FXML private TableColumn<Maintenance, Double> colCout;
    @FXML private TableColumn<Maintenance, String> colDescription;
    @FXML private TableColumn<Maintenance, String> colStatut;
    @FXML private TableColumn<Maintenance, String> colPriorite;
    @FXML private TableColumn<Maintenance, String> colKilometrage;
    @FXML private TableColumn<Maintenance, String> colRecommandation; // présent FXML, affiché vide

    @FXML private TextField        champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private ComboBox<String> comboTypePanne;
    @FXML private Label            lblTotal;
    @FXML private Label            lblCoutTotal;
    @FXML private Label            lblCoutMoyen;
    @FXML private Label            lblMachineCouteuse;
    @FXML private Label            lblTypeDominant;
    @FXML private Button           logoutBtn; // nécessaire pour handleLogout

    // =========================================================
    //  SERVICES & DONNÉES
    // =========================================================

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final MachineService     machineService     = new MachineService();

    private List<Machine> machines = new ArrayList<>();

    private final ObservableList<Maintenance> allMaintenancesSource = FXCollections.observableArrayList();
    private final ObservableList<Maintenance> filteredMaintenances  = FXCollections.observableArrayList();
    private String currentSearch        = "";
    private String currentMachineFilter = "Toutes les machines";
    private String currentTypeFilter    = "Tous les types";

    private static final String[] AJOUT_MAINTENANCE_FXML = {
            "/MaterielsInterface/AjouterMaintenances.fxml",
            "/MaterielsInterface/AjouterMaintenance.fxml"
    };

    private static final String[] MODIFIER_MAINTENANCE_FXML = {
            "/MaterielsInterface/ModfifierMaintenance.fxml",
            "/MaterielsInterface/ModifierMaintenance.fxml"
    };

    // =========================================================
    //  INITIALISATION
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        chargerMachines();
        chargerMaintenances();
        configurerRecherche();
    }

    private void configurerTableau() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colMachine    .setCellValueFactory(d -> new SimpleStringProperty(getNomMachine(d.getValue().getIdM())));
        colTypePanne  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTypePanne()   != null ? d.getValue().getTypePanne()   : ""));
        colDate       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateMain().format(fmt)));
        colCout       .setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getCout()).asObject());
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription() != null ? d.getValue().getDescription() : ""));
        colStatut     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatut()      != null ? d.getValue().getStatut()      : ""));
        colPriorite   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriorite()    != null ? d.getValue().getPriorite()    : ""));
        colKilometrage.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getKilometrage())));

        // colRecommandation : pas de getter dans le modèle → afficher vide
        if (colRecommandation != null)
            colRecommandation.setCellValueFactory(d -> new SimpleStringProperty(obtenirRecommandationIA(d.getValue())));
        tableMaintenances.setItems(filteredMaintenances);
    }

    private void configurerRecherche() {
        champRecherche.textProperty().addListener((obs, old, nw) -> {
            currentSearch = nw != null ? nw.toLowerCase() : "";
            appliquerFiltres();
        });
        comboMachine.valueProperty().addListener((obs, old, nw) -> {
            currentMachineFilter = nw != null ? nw : "Toutes les machines";
            appliquerFiltres();
        });
        comboTypePanne.valueProperty().addListener((obs, old, nw) -> {
            currentTypeFilter = nw != null ? nw : "Tous les types";
            appliquerFiltres();
        });
    }

    // =========================================================
    //  CHARGEMENT DONNÉES
    // =========================================================

    private void chargerMachines() {
        try {
            machines = machineService.recuperer();
            comboMachine.getItems().clear();
            comboMachine.getItems().add("Toutes les machines");
            for (Machine m : machines) comboMachine.getItems().add(m.getNom());
            comboMachine.setValue("Toutes les machines");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les machines", Alert.AlertType.ERROR);
        }
    }

    private void chargerMaintenances() {
        try {
            allMaintenancesSource.clear();
            allMaintenancesSource.addAll(maintenanceService.recuperer());

            Set<String> types = new LinkedHashSet<>();
            types.add("Tous les types");
            for (Maintenance m : allMaintenancesSource)
                if (m.getTypePanne() != null) types.add(m.getTypePanne());

            comboTypePanne.setItems(FXCollections.observableArrayList(types));
            comboTypePanne.setValue("Tous les types");

            currentSearch        = "";
            currentMachineFilter = "Toutes les machines";
            currentTypeFilter    = "Tous les types";

            appliquerFiltres();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les maintenances", Alert.AlertType.ERROR);
        }
    }

    // =========================================================
    //  FILTRES
    // =========================================================

    private void appliquerFiltres() {
        List<Maintenance> filtered = allMaintenancesSource.stream()
                .filter(m -> {
                    if (!currentSearch.isEmpty()) {
                        String nom = getNomMachine(m.getIdM()).toLowerCase();
                        return nom.contains(currentSearch)
                                || (m.getTypePanne()   != null && m.getTypePanne().toLowerCase().contains(currentSearch))
                                || (m.getDescription() != null && m.getDescription().toLowerCase().contains(currentSearch))
                                || obtenirRecommandationIA(m).toLowerCase().contains(currentSearch);
                    }
                    return true;
                })
                .filter(m -> {
                    if (!"Toutes les machines".equals(currentMachineFilter))
                        return getNomMachine(m.getIdM()).equals(currentMachineFilter);
                    return true;
                })
                .filter(m -> {
                    if (!"Tous les types".equals(currentTypeFilter))
                        return m.getTypePanne() != null && m.getTypePanne().equals(currentTypeFilter);
                    return true;
                })
                .collect(Collectors.toList());

        filteredMaintenances.setAll(filtered);
        mettreAJourStatistiques();
    }

    // =========================================================
    //  STATISTIQUES
    // =========================================================

    private void mettreAJourStatistiques() {
        double total = filteredMaintenances.stream().mapToDouble(Maintenance::getCout).sum();
        double moy   = filteredMaintenances.isEmpty() ? 0 : total / filteredMaintenances.size();

        lblTotal    .setText(String.valueOf(filteredMaintenances.size()));
        lblCoutTotal.setText(String.format("%.2f DT", total));
        lblCoutMoyen.setText(String.format("%.2f DT", moy));

        Map<Integer, Double> coutParMachine = new HashMap<>();
        for (Maintenance m : filteredMaintenances)
            coutParMachine.merge(m.getIdM(), m.getCout(), Double::sum);

        int maxMachineId = coutParMachine.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(-1);
        lblMachineCouteuse.setText(maxMachineId != -1 ? getNomMachine(maxMachineId) : "-");

        Map<String, Long> typeCount = filteredMaintenances.stream()
                .filter(m -> m.getTypePanne() != null)
                .collect(Collectors.groupingBy(Maintenance::getTypePanne, Collectors.counting()));
        lblTypeDominant.setText(typeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-"));
    }

    // =========================================================
    //  CRUD
    // =========================================================

    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            URL resource = getFXMLResource(AJOUT_MAINTENANCE_FXML);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une maintenance");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerMaintenances();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void modifierSelection() {
        Maintenance selected = tableMaintenances.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner une maintenance", Alert.AlertType.WARNING);
            return;
        }
        try {
            URL resource = getFXMLResource(MODIFIER_MAINTENANCE_FXML);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            ModifierMaintenanceController controller = loader.getController();
            controller.initialiserDonnees(selected);
            Stage stage = new Stage();
            stage.setTitle("Modifier une maintenance");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerMaintenances();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void supprimerSelection() {
        Maintenance selected = tableMaintenances.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélection", "Veuillez sélectionner une maintenance", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la maintenance");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette maintenance ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                maintenanceService.supprimer(selected.getIdMain());
                chargerMaintenances();
                showAlert("Succès", "Maintenance supprimée avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML private void actualiser()       { chargerMaintenances(); }
    @FXML private void effacerRecherche() { champRecherche.clear(); }
    @FXML private void filtrer()          { appliquerFiltres(); }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboMachine.setValue("Toutes les machines");
        comboTypePanne.setValue("Tous les types");
        chargerMaintenances();
    }

    // =========================================================
    //  TRI
    // =========================================================

    @FXML private void trierPlusRecent() {
        List<Maintenance> s = new ArrayList<>(filteredMaintenances);
        s.sort((a, b) -> b.getDateMain().compareTo(a.getDateMain()));
        filteredMaintenances.setAll(s);
    }
    @FXML private void trierPlusAncien() {
        List<Maintenance> s = new ArrayList<>(filteredMaintenances);
        s.sort(Comparator.comparing(Maintenance::getDateMain));
        filteredMaintenances.setAll(s);
    }
    @FXML private void trierCoutEleve() {
        List<Maintenance> s = new ArrayList<>(filteredMaintenances);
        s.sort((a, b) -> Double.compare(b.getCout(), a.getCout()));
        filteredMaintenances.setAll(s);
    }
    @FXML private void trierCoutFaible() {
        List<Maintenance> s = new ArrayList<>(filteredMaintenances);
        s.sort(Comparator.comparingDouble(Maintenance::getCout));
        filteredMaintenances.setAll(s);
    }

    @FXML
    private void afficherAlertesMaintenance() {
        List<Maintenance> alertes = filteredMaintenances.stream()
                .filter(m -> m.getCout() >= 1000
                        || "urgente".equalsIgnoreCase(m.getPriorite())
                        || "haute".equalsIgnoreCase(m.getPriorite())
                        || "en_cours".equalsIgnoreCase(m.getStatut()))
                .collect(Collectors.toList());

        if (alertes.isEmpty()) {
            showAlert("Alertes maintenance", "Aucune alerte critique detectee.", Alert.AlertType.INFORMATION);
            return;
        }

        StringBuilder message = new StringBuilder();
        int limite = Math.min(alertes.size(), 8);
        for (int i = 0; i < limite; i++) {
            Maintenance m = alertes.get(i);
            message.append("- ")
                    .append(getNomMachine(m.getIdM()))
                    .append(" | ")
                    .append(m.getTypePanne() != null ? m.getTypePanne() : "Type inconnu")
                    .append(" | ")
                    .append(String.format("%.2f DT", m.getCout()))
                    .append(" | Priorite: ")
                    .append(m.getPriorite() != null ? m.getPriorite() : "-")
                    .append("\n");
        }
        if (alertes.size() > limite) {
            message.append("\n+ ").append(alertes.size() - limite).append(" autre(s) alerte(s).");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Alertes maintenance");
        alert.setHeaderText(alertes.size() + " maintenance(s) demandent une verification");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    // =========================================================
    //  NAVIGATION — RETOUR ACCUEIL
    //  ✅ CORRECTION : retourAccueil() sans paramètre.
    //     On utilise tableMaintenances (Node toujours présent)
    //     pour récupérer le Stage courant.
    // =========================================================

    @FXML
    private void retourAccueil() {
        naviguerDepuisNode("/MaterielsInterface/AccueilMateriel.fxml", tableMaintenances);
    }

    // =========================================================
    //  PROFIL
    // =========================================================

    @FXML
    private void handleMonProfil(MouseEvent event) {
        System.out.println("Profil utilisateur");
    }

    // =========================================================
    //  NAVIGATION SIDEBAR (MouseEvent)
    // =========================================================

    @FXML private void naviguerAnimaux(MouseEvent e)    { naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml", e); }
    @FXML private void naviguerMateriels(MouseEvent e)  { naviguerVers("/MaterielsInterface/AccueilMateriel.fxml", e); }
    @FXML private void naviguerStocks(MouseEvent e)     { naviguerVers("/StocksInterface/afficherarticle.fxml", e); }
    @FXML private void naviguerTerrains(MouseEvent e)   { naviguerVers("/TerrainsInterface/AfficherTerrains.fxml", e); }
    @FXML private void naviguerEvenements(MouseEvent e) { naviguerVers("/EventsInterface/AccueilEvenement.fxml", e); }
    @FXML private void naviguerUsers(MouseEvent e)      { naviguerVers("/UsersInterface/Acceuil.fxml", e); }
    @FXML private void handlePersonnes(MouseEvent e)    { naviguerVers("/UsersInterface/DahboardPersonne.fxml", e); }
    @FXML private void handleTaches(MouseEvent e)       { naviguerVers("/UsersInterface/GestionTache.fxml", e); }
    @FXML private void handleAbonnements(MouseEvent e)  { naviguerVers("/UsersInterface/GestionAbonnements.fxml", e); }
    @FXML private void handleOffres(MouseEvent e)       { naviguerVers("/UsersInterface/GestionOffre.fxml", e); }
    @FXML private void handleDashboard(MouseEvent e)    { naviguerVers("/UsersInterface/Acceuil.fxml", e); }
    @FXML private void handleAnimals(MouseEvent e)      { naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml", e); }
    @FXML private void handleStocks(MouseEvent e)       { naviguerVers("/StocksInterface/afficherarticle.fxml", e); }
    @FXML private void handleTerrains(MouseEvent e)     { naviguerVers("/TerrainsInterface/acceuilterrain.fxml", e); }
    @FXML private void handleEvents(MouseEvent e)       { naviguerVers("/G-Evenements/Accueil.fxml", e); }
    @FXML private void handleMateriels(MouseEvent e)    { naviguerVers("/MaterielsInterface/AccueilMateriel.fxml", e); }

    // =========================================================
    //  DÉCONNEXION
    //  ✅ CORRECTION : onAction dans FXML → ActionEvent ici
    // =========================================================

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            naviguerDepuisNode("/UsersInterface/login.fxml", logoutBtn);
        }
    }

    // =========================================================
    //  EXPORT PDF
    // =========================================================

    @FXML
    private void exporterPDF() {
        if (filteredMaintenances.isEmpty()) {
            showAlert("Export", "Aucune donnée à exporter", Alert.AlertType.WARNING);
            return;
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setInitialFileName("maintenances_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(tableMaintenances.getScene().getWindow());
        if (file != null) {
            try {
                PdfWriter   writer = new PdfWriter(file);
                PdfDocument pdf    = new PdfDocument(writer);
                Document    doc    = new Document(pdf);
                doc.add(new Paragraph("Rapport des Maintenances").setFontSize(18).setBold());
                doc.add(new Paragraph("Généré le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFontSize(12));
                doc.add(new Paragraph("\n"));
                float[] colWidths = {90, 90, 70, 65, 120, 70, 130, 70, 60};
                Table table = new Table(UnitValue.createPointArray(colWidths));
                table.setWidth(UnitValue.createPercentValue(100));
                for (String h : new String[]{"Machine","Type Panne","Date","Coût","Description","Statut","Recommandation","Priorité","Km"})
                    table.addHeaderCell(new Cell().add(new Paragraph(h)));
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Maintenance m : filteredMaintenances) {
                    table.addCell(new Cell().add(new Paragraph(getNomMachine(m.getIdM()))));
                    table.addCell(new Cell().add(new Paragraph(m.getTypePanne()   != null ? m.getTypePanne()   : "")));
                    table.addCell(new Cell().add(new Paragraph(m.getDateMain().format(fmt))));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", m.getCout()))));
                    table.addCell(new Cell().add(new Paragraph(m.getDescription() != null ? m.getDescription() : "")));
                    table.addCell(new Cell().add(new Paragraph(m.getStatut()      != null ? m.getStatut()      : "")));
                    table.addCell(new Cell().add(new Paragraph(obtenirRecommandationIA(m))));
                    table.addCell(new Cell().add(new Paragraph(m.getPriorite()    != null ? m.getPriorite()    : "")));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(m.getKilometrage()))));
                }
                doc.add(table);
                doc.close();
                showAlert("Succès", "PDF exporté avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur export PDF : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // =========================================================
    //  EXPORT EXCEL
    // =========================================================

    @FXML
    private void exporterExcel() {
        if (filteredMaintenances.isEmpty()) {
            showAlert("Export", "Aucune donnée à exporter", Alert.AlertType.WARNING);
            return;
        }
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setInitialFileName("maintenances_" + LocalDate.now() + ".xlsx");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(tableMaintenances.getScene().getWindow());
        if (file != null) {
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Maintenances");
                String[] headers = {"Machine","Type Panne","Date","Coût (DT)","Description","Statut","Recommandation","Priorité","Kilométrage"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                int rowNum = 1;
                for (Maintenance m : filteredMaintenances) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(getNomMachine(m.getIdM()));
                    row.createCell(1).setCellValue(m.getTypePanne()   != null ? m.getTypePanne()   : "");
                    row.createCell(2).setCellValue(m.getDateMain().format(fmt));
                    row.createCell(3).setCellValue(m.getCout());
                    row.createCell(4).setCellValue(m.getDescription() != null ? m.getDescription() : "");
                    row.createCell(5).setCellValue(m.getStatut()      != null ? m.getStatut()      : "");
                    row.createCell(6).setCellValue(obtenirRecommandationIA(m));
                    row.createCell(7).setCellValue(m.getPriorite()    != null ? m.getPriorite()    : "");
                    row.createCell(8).setCellValue(m.getKilometrage());
                }
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
                try (FileOutputStream fos = new FileOutputStream(file)) { workbook.write(fos); }
                showAlert("Succès", "Excel exporté avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur export Excel : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // =========================================================
    //  UTILITAIRES
    // =========================================================

    private String getNomMachine(int idM) {
        for (Machine m : machines)
            if (m.getIdM() == idM) return m.getNom();
        return "Machine #" + idM;
    }

    private String obtenirRecommandationIA(Maintenance maintenance) {
        if (maintenance == null) return "";

        String recommandationExistante = maintenance.getRecommandation();
        if (recommandationExistante != null && !recommandationExistante.isBlank()) {
            return recommandationExistante;
        }

        String nomMachine = getNomMachine(maintenance.getIdM());
        String machineLower = nomMachine.toLowerCase(Locale.ROOT);
        String typePanne = maintenance.getTypePanne() != null ? maintenance.getTypePanne() : "";
        String typeLower = typePanne.toLowerCase(Locale.ROOT);

        String action;
        if (typeLower.contains("moteur") || typeLower.contains("surchauffe")) {
            action = "contrôler le moteur, le niveau d'huile et le circuit de refroidissement";
        } else if (typeLower.contains("hydraul")) {
            action = "vérifier les flexibles, les joints et la pression hydraulique";
        } else if (typeLower.contains("frein")) {
            action = "inspecter les plaquettes, le liquide de frein et tester le freinage";
        } else if (typeLower.contains("elect") || typeLower.contains("batterie")) {
            action = "tester la batterie, les fusibles et le câblage électrique";
        } else if (typeLower.contains("pneu") || typeLower.contains("roue")) {
            action = "contrôler la pression, l'usure des pneus et le serrage des roues";
        } else if (typeLower.contains("huile") || typeLower.contains("fuite")) {
            action = "localiser la fuite, remplacer les joints usés et refaire le niveau";
        } else {
            action = "réaliser un diagnostic complet et planifier une maintenance préventive";
        }

        String contexteMachine;
        if (machineLower.contains("tracteur")) {
            contexteMachine = "avant la prochaine sortie au champ";
        } else if (machineLower.contains("moissonneuse")) {
            contexteMachine = "avant la prochaine campagne de récolte";
        } else if (machineLower.contains("pompe")) {
            contexteMachine = "avant une utilisation prolongée";
        } else {
            contexteMachine = "avant la prochaine utilisation";
        }

        return "IA: Pour " + nomMachine + ", " + action + " " + contexteMachine + ".";
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private URL getFXMLResource(String... paths) throws IOException {
        for (String path : paths) {
            URL resource = getClass().getResource(path);
            if (resource != null) return resource;
        }
        throw new IOException("FXML introuvable : " + String.join(" ou ", paths));
    }

    /**
     * Navigation depuis un MouseEvent (sidebar buttons)
     */
    private void naviguerVers(String fxmlPath, MouseEvent event) {
        naviguerDepuisNode(fxmlPath, (Node) event.getSource());
    }

    /**
     * ✅ Méthode centrale de navigation.
     * Accepte n'importe quel Node déjà dans la scène
     * (tableMaintenances, logoutBtn, bouton sidebar…).
     * Remplace getScene().getWindow() de façon uniforme.
     */
    private void naviguerDepuisNode(String fxmlPath, Node source) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showAlert("Erreur", "Fichier FXML introuvable : " + fxmlPath, Alert.AlertType.ERROR);
                System.err.println("FXML introuvable : " + fxmlPath);
                return;
            }
            Parent root  = FXMLLoader.load(resource);
            Stage  stage = (Stage) source.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger : " + fxmlPath + "\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
