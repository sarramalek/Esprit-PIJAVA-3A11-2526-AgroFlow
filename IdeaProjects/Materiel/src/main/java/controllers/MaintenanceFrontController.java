package controllers;

import entities.Maintenance;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.MaintenanceService;
import utils.MyDatabase;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MaintenanceFrontController {

    // ══════════════════════════════════════════════════════
    //  FXML — tous les fx:id présents dans le FXML
    // ══════════════════════════════════════════════════════

    // Recherche unique
    @FXML private TextField            champRecherche;

    // Filtres
    @FXML private ComboBox<String>     comboMachine;
    @FXML private ComboBox<String>     comboTypePanne;

    // Tableau
    @FXML private TableView<Maintenance>              tableMaintenances;
    @FXML private TableColumn<Maintenance, String>    colMachine;
    @FXML private TableColumn<Maintenance, String>    colTypePanne;
    @FXML private TableColumn<Maintenance, LocalDate> colDate;
    @FXML private TableColumn<Maintenance, Double>    colCout;
    @FXML private TableColumn<Maintenance, String>    colDescription;

    // Statistiques
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;
    @FXML private Label lblMachineCouteuse;
    @FXML private Label lblTypeDominant;
    @FXML private Label lblFiltres;

    // ══════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════

    private final MaintenanceService           maintenanceService = new MaintenanceService();
    private final Map<Integer, String>         nomMachineMap      = new HashMap<>();
    private final ObservableList<Maintenance>  toutesMaintenances  = FXCollections.observableArrayList();
    private final ObservableList<Maintenance>  maintenancesFiltrees = FXCollections.observableArrayList();

    // ══════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        chargerNomsMachines();
        configurerColonnes();
        chargerDonnees();
        configurerRechercheDynamique();
    }

    // ──────────────────────────────────────────────────────
    //  Charger la map idM → nom  (connexion partagée)
    // ──────────────────────────────────────────────────────
    private void chargerNomsMachines() {
        nomMachineMap.clear();
        Connection conn = MyDatabase.getInstance().getConnection();
        String sql = "SELECT idM, nom FROM machine";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int    idM = rs.getInt("idM");
                String nom = rs.getString("nom");
                nomMachineMap.put(idM, (nom != null && !nom.isBlank()) ? nom : "Machine #" + idM);
            }
            System.out.println("✓ " + nomMachineMap.size() + " machine(s) chargée(s).");
        } catch (SQLException e) {
            System.err.println("⚠ Erreur chargement noms machines : " + e.getMessage());
        }
    }

    private String getNomMachine(int idM) {
        return nomMachineMap.getOrDefault(idM, "Machine #" + idM);
    }

    // ──────────────────────────────────────────────────────
    //  Configuration des colonnes
    // ──────────────────────────────────────────────────────
    private void configurerColonnes() {

        colMachine.setCellValueFactory(cellData ->
                new SimpleStringProperty(getNomMachine(cellData.getValue().getIdM()))
        );

        colTypePanne.setCellValueFactory(new PropertyValueFactory<>("typePanne"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateMain"));

        colCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colCout.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null); setStyle("");
                } else {
                    setText(String.format("%.2f DT", val));
                    if      (val >= 500) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (val >= 200) setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    else                 setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableMaintenances.setEditable(false);
    }

    // ──────────────────────────────────────────────────────
    //  Chargement des maintenances
    // ──────────────────────────────────────────────────────
    private void chargerDonnees() {
        try {
            List<Maintenance> liste = maintenanceService.recuperer();
            toutesMaintenances.setAll(liste);
            maintenancesFiltrees.setAll(liste);
            tableMaintenances.setItems(maintenancesFiltrees);
            remplirComboMachine(liste);
            remplirComboTypePanne(liste);
            mettreAJourStatistiques(liste);
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement",
                    "Impossible de récupérer les maintenances : " + e.getMessage());
        }
    }

    private void remplirComboMachine(List<Maintenance> liste) {
        List<String> noms = liste.stream()
                .map(m -> getNomMachine(m.getIdM()))
                .distinct().sorted().collect(Collectors.toList());
        noms.add(0, "Toutes les machines");
        comboMachine.setItems(FXCollections.observableArrayList(noms));
        comboMachine.setValue("Toutes les machines");
    }

    private void remplirComboTypePanne(List<Maintenance> liste) {
        List<String> types = liste.stream()
                .map(Maintenance::getTypePanne)
                .filter(t -> t != null && !t.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        types.add(0, "Tous les types");
        comboTypePanne.setItems(FXCollections.observableArrayList(types));
        comboTypePanne.setValue("Tous les types");
    }

    // ──────────────────────────────────────────────────────
    //  Listener dynamique (frappe au clavier)
    // ──────────────────────────────────────────────────────
    private void configurerRechercheDynamique() {
        champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    // ══════════════════════════════════════════════════════
    //  FILTRAGE COMBINÉ  (texte + combobox)
    // ══════════════════════════════════════════════════════

    /** Appelé par le KeyReleased du TextField ET les onAction des ComboBox */
    @FXML
    private void appliquerFiltres() {
        String texte   = champRecherche.getText() == null ? "" : champRecherche.getText().toLowerCase().trim();
        String machine = comboMachine.getValue();
        String type    = comboTypePanne.getValue();

        List<Maintenance> res = toutesMaintenances.stream()
                .filter(m -> {
                    // ── Filtre texte libre ────────────────────────
                    if (!texte.isEmpty() && !correspond(m, texte)) return false;

                    // ── Filtre machine par NOM ────────────────────
                    if (machine != null && !machine.equals("Toutes les machines")) {
                        if (!getNomMachine(m.getIdM()).equals(machine)) return false;
                    }

                    // ── Filtre type de panne ──────────────────────
                    if (type != null && !type.equals("Tous les types")) {
                        if (!type.equals(m.getTypePanne())) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        maintenancesFiltrees.setAll(res);
        mettreAJourStatistiques(res);
    }

    /** Vérifie si une maintenance correspond au texte libre */
    private boolean correspond(Maintenance m, String texte) {
        String nomMachine = getNomMachine(m.getIdM()).toLowerCase();
        String type       = m.getTypePanne()   != null ? m.getTypePanne().toLowerCase()   : "";
        String desc       = m.getDescription() != null ? m.getDescription().toLowerCase() : "";
        String date       = m.getDateMain()    != null ? m.getDateMain().toString()        : "";
        String cout       = String.valueOf(m.getCout());
        return nomMachine.contains(texte)
                || type.contains(texte)
                || desc.contains(texte)
                || date.contains(texte)
                || cout.contains(texte);
    }

    // ──────────────────────────────────────────────────────
    //  Boutons Effacer / Réinit / Actualiser
    // ──────────────────────────────────────────────────────

    @FXML
    private void effacerRecherche() {
        champRecherche.clear();
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        if (comboMachine.getItems()   != null && !comboMachine.getItems().isEmpty())
            comboMachine.setValue("Toutes les machines");
        if (comboTypePanne.getItems() != null && !comboTypePanne.getItems().isEmpty())
            comboTypePanne.setValue("Tous les types");
        maintenancesFiltrees.setAll(toutesMaintenances);
        tableMaintenances.setItems(maintenancesFiltrees);
        mettreAJourStatistiques(toutesMaintenances);
    }

    @FXML
    private void actualiser() {
        chargerNomsMachines();
        chargerDonnees();
    }

    // ══════════════════════════════════════════════════════
    //  TRIS
    // ══════════════════════════════════════════════════════

    @FXML
    private void trierPlusRecent() {
        maintenancesFiltrees.sort(Comparator.comparing(
                Maintenance::getDateMain, Comparator.nullsLast(Comparator.reverseOrder())));
        tableMaintenances.refresh();
    }

    @FXML
    private void trierPlusAncien() {
        maintenancesFiltrees.sort(Comparator.comparing(
                Maintenance::getDateMain, Comparator.nullsLast(Comparator.naturalOrder())));
        tableMaintenances.refresh();
    }

    @FXML
    private void trierCoutDesc() {
        maintenancesFiltrees.sort(Comparator.comparingDouble(Maintenance::getCout).reversed());
        tableMaintenances.refresh();
    }

    // ══════════════════════════════════════════════════════
    //  STATISTIQUES
    // ══════════════════════════════════════════════════════

    private void mettreAJourStatistiques(List<? extends Maintenance> liste) {
        int total = liste.size();
        lblTotal.setText(String.valueOf(total));
        lblFiltres.setText(String.valueOf(total));

        if (total == 0) {
            lblCoutTotal.setText("0,00 DT");
            lblCoutMoyen.setText("0,00 DT");
            lblMachineCouteuse.setText("—");
            lblTypeDominant.setText("—");
            return;
        }

        double coutTotal = liste.stream().mapToDouble(Maintenance::getCout).sum();
        lblCoutTotal.setText(String.format("%.2f DT", coutTotal));
        lblCoutMoyen.setText(String.format("%.2f DT", coutTotal / total));

        liste.stream()
                .collect(Collectors.groupingBy(
                        m -> getNomMachine(m.getIdM()),
                        Collectors.summingDouble(Maintenance::getCout)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> lblMachineCouteuse.setText(
                        e.getKey() + " (" + String.format("%.0f DT", e.getValue()) + ")"));

        liste.stream()
                .filter(m -> m.getTypePanne() != null && !m.getTypePanne().isBlank())
                .collect(Collectors.groupingBy(Maintenance::getTypePanne, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> lblTypeDominant.setText(
                        e.getKey() + " (" + e.getValue() + "x)"));
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR
    // ══════════════════════════════════════════════════════

    @FXML private void naviguerAnimaux()    { naviguerVers("/views/GestionAnimaux.fxml",  "Gestion Animaux");   }
    @FXML private void naviguerMateriels()  { naviguerVers("/views/AccueilMateriel.fxml", "Gestion Matériels"); }
    @FXML private void naviguerStocks()     { naviguerVers("/views/GestionStocks.fxml",   "Gestion Stocks");    }
    @FXML private void naviguerTerrains()   { naviguerVers("/views/GestionTerrains.fxml", "Gestion Terrains");  }
    @FXML private void naviguerEvenements() { naviguerVers("/views/Evenements.fxml",      "Événements");        }
    @FXML private void naviguerUsers()      { naviguerVers("/views/Utilisateurs.fxml",    "Utilisateurs");      }
    @FXML private void deconnexion()        { naviguerVers("/views/Login.fxml",           "Connexion");         }

    private void naviguerVers(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tableMaintenances.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setTitle("AgroFlow — " + titre);
            stage.setMaximized(wasMaximized);
            stage.show();
        } catch (IOException e) {
            System.err.println("⚠ Impossible de charger : " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════

    private void afficherErreur(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}