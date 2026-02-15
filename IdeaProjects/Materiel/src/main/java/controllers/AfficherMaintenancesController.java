package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import entities.Maintenance;
import entities.Machine;
import services.MaintenanceService;
import services.MachineService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AfficherMaintenancesController implements Initializable {

    @FXML private TableView<Maintenance> tableMaintenances;
    @FXML private TableColumn<Maintenance, String>  colMachine;
    @FXML private TableColumn<Maintenance, String>  colTypePanne;
    @FXML private TableColumn<Maintenance, LocalDate> colDate;
    @FXML private TableColumn<Maintenance, Double>  colCout;
    @FXML private TableColumn<Maintenance, String>  colDescription;
    @FXML private TableColumn<Maintenance, Void>    colActions;

    @FXML private TextField  champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;   // nouveau label dans le FXML

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final MachineService     machineService     = new MachineService();

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
        chargerMachines();          // 1) remplir mapMachines + comboBox
        configurationTableau();    // 2) wirer les colonnes (mapMachines doit être prêt)
        chargerMaintenances();     // 3) charger les lignes
        mettreAJourStatistiques(); // 4) totaux
    }

    // ============================================================
    //  CHARGEMENT DES MACHINES  (jointure + comboBox)
    // ============================================================
    private void chargerMachines() {
        try {
            List<Machine> machines = machineService.recuperer();

            mapMachines.clear();
            for (Machine m : machines) {
                mapMachines.put(m.getIdM(), m);
            }

            ObservableList<String> noms = FXCollections.observableArrayList("Toutes les machines");
            noms.addAll(machines.stream().map(Machine::getNom).collect(Collectors.toList()));
            comboMachine.setItems(noms);
            comboMachine.setValue("Toutes les machines");

            System.out.println("[OK] " + machines.size() + " machine(s) chargee(s) dans la Map de jointure");

        } catch (SQLException e) {
            afficherAlerte("Erreur", "Chargement des machines : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  CONFIGURATION DES COLONNES
    // ============================================================
    private void configurationTableau() {

        // --- Colonne Machine (JOINTURE idM -> Nom) ---
        colMachine.setCellValueFactory(cellData -> {
            int idM = cellData.getValue().getIdM();
            Machine machine = mapMachines.get(idM);
            String nomMachine = (machine != null)
                    ? machine.getNom()
                    : "Inconnu (ID=" + idM + ")";
            return new javafx.beans.property.SimpleStringProperty(nomMachine);
        });

        // Style de la cellule Machine : fond coloré si inconnu
        colMachine.setCellFactory(col -> new TableCell<Maintenance, String>() {
            @Override
            protected void updateItem(String nom, boolean empty) {
                super.updateItem(nom, empty);
                if (empty || nom == null) {
                    setText(null);
                    setStyle("");
                } else if (nom.startsWith("Inconnu")) {
                    setText(nom);
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                } else {
                    setText(nom);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        // --- Type de panne ---
        colTypePanne.setCellValueFactory(new PropertyValueFactory<>("typePanne"));
        colTypePanne.setCellFactory(col -> new TableCell<Maintenance, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) { setText(null); setStyle(""); }
                else {
                    setText(type);
                    String bg = switch (type.toLowerCase()) {
                        case "electricite", "électricité" -> "#f0e6ff";
                        case "moteur"                     -> "#fff3e0";
                        case "hydraulique"                -> "#e3f2fd";
                        default                           -> "#f5f5f5";
                    };
                    setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 4;");
                }
            }
        });

        // --- Date ---
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateMain"));

        // --- Coût formaté ---
        colCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colCout.setCellFactory(col -> new TableCell<Maintenance, Double>() {
            @Override
            protected void updateItem(Double cout, boolean empty) {
                super.updateItem(cout, empty);
                if (empty || cout == null) { setText(null); setStyle(""); }
                else {
                    setText(String.format("%.2f DT", cout));
                    setStyle(cout > 300
                            ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;"
                            : "-fx-text-fill: #27ae60;");
                }
            }
        });

        // --- Description ---
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Afficher le texte complet en tooltip si trop long
        colDescription.setCellFactory(col -> new TableCell<Maintenance, String>() {
            @Override
            protected void updateItem(String desc, boolean empty) {
                super.updateItem(desc, empty);
                if (empty || desc == null) { setText(null); setTooltip(null); }
                else {
                    setText(desc);
                    setTooltip(new Tooltip(desc));
                }
            }
        });

        // --- Colonne Actions ---
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier  = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox   conteneur   = new HBox(8, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle(
                        "-fx-background-color: #f39c12; -fx-text-fill: white; " +
                                "-fx-padding: 5 12; -fx-cursor: hand; " +
                                "-fx-background-radius: 4; -fx-font-size: 12px; -fx-font-weight: bold;");

                btnSupprimer.setStyle(
                        "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                "-fx-padding: 5 12; -fx-cursor: hand; " +
                                "-fx-background-radius: 4; -fx-font-size: 12px; -fx-font-weight: bold;");

                btnModifier.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    ouvrirModification(m);
                });
                btnSupprimer.setOnAction(e -> {
                    Maintenance m = getTableView().getItems().get(getIndex());
                    supprimerMaintenance(m);
                });
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                if (vide) {
                    setGraphic(null);
                } else {
                    conteneur.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(conteneur);
                }
            }
        });

        // Activer le tri par clic sur en-tête
        tableMaintenances.setSortPolicy(tv -> {
            FXCollections.sort(tableMaintenances.getItems(), tableMaintenances.getComparator());
            return true;
        });
    }

    // ============================================================
    //  CHARGEMENT DES MAINTENANCES
    // ============================================================
    private void chargerMaintenances() {
        try {
            List<Maintenance> maintenances = maintenanceService.recuperer();
            listeMaintenances.setAll(maintenances);
            tableMaintenances.setItems(listeMaintenances);
            System.out.println("[OK] " + maintenances.size() + " maintenance(s) chargee(s)");
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Chargement des maintenances : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  RECHERCHE
    // ============================================================
    @FXML
    private void rechercher() {
        appliquerFiltres();
    }

    // ============================================================
    //  FILTRE PAR MACHINE
    // ============================================================
    @FXML
    private void filtrer() {
        appliquerFiltres();
    }

    /**
     * Applique simultanément le filtre texte ET le filtre machine.
     * C'est la méthode centrale de filtrage.
     */
    private void appliquerFiltres() {
        String recherche          = champRecherche.getText().toLowerCase().trim();
        String machineSelectionnee = comboMachine.getValue();

        // Trouver l'idM de la machine sélectionnée (null = toutes)
        Integer idMachineFiltree = null;
        if (machineSelectionnee != null && !machineSelectionnee.equals("Toutes les machines")) {
            for (Map.Entry<Integer, Machine> entry : mapMachines.entrySet()) {
                if (entry.getValue().getNom().equals(machineSelectionnee)) {
                    idMachineFiltree = entry.getKey();
                    break;
                }
            }
        }
        final Integer idMachineFinal = idMachineFiltree;

        ObservableList<Maintenance> filtrees = listeMaintenances.filtered(m -> {
            // Filtre machine
            if (idMachineFinal != null && m.getIdM() != idMachineFinal) return false;

            // Filtre texte
            if (!recherche.isEmpty()) {
                boolean matchType = m.getTypePanne() != null &&
                        m.getTypePanne().toLowerCase().contains(recherche);
                boolean matchDesc = m.getDescription() != null &&
                        m.getDescription().toLowerCase().contains(recherche);
                // Jointure : chercher aussi dans le nom de la machine
                Machine machine = mapMachines.get(m.getIdM());
                boolean matchMachine = machine != null &&
                        machine.getNom().toLowerCase().contains(recherche);

                if (!matchType && !matchDesc && !matchMachine) return false;
            }
            return true;
        });

        tableMaintenances.setItems(filtrees);
        mettreAJourStatistiques();
    }

    // ============================================================
    //  STATISTIQUES
    // ============================================================
    private void mettreAJourStatistiques() {
        int    total     = tableMaintenances.getItems().size();
        double coutTotal = tableMaintenances.getItems().stream()
                .mapToDouble(Maintenance::getCout).sum();
        double coutMoyen = (total > 0) ? coutTotal / total : 0.0;

        lblTotal.setText(String.valueOf(total));
        lblCoutTotal.setText(String.format("%.2f DT", coutTotal));
        if (lblCoutMoyen != null) {
            lblCoutMoyen.setText(String.format("%.2f DT", coutMoyen));
        }
    }

    // ============================================================
    //  ACTUALISER
    // ============================================================
    @FXML
    private void actualiser() {
        chargerMachines();
        chargerMaintenances();
        champRecherche.clear();
        comboMachine.setValue("Toutes les machines");
        mettreAJourStatistiques();
        afficherAlerte("Actualisation", "Donnees actualisees avec succes", Alert.AlertType.INFORMATION);
    }

    // ============================================================
    //  OUVERTURE FORMULAIRES
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

    private void ouvrirModification(Maintenance maintenance) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMaintenance.fxml"));
            Parent root = loader.load();
            ModifierMaintenanceController controller = loader.getController();
            controller.initialiserDonnees(maintenance);
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

    // ============================================================
    //  SUPPRESSION
    // ============================================================
    private void supprimerMaintenance(Maintenance maintenance) {
        Machine machine  = mapMachines.get(maintenance.getIdM());
        String nomMachine = (machine != null) ? machine.getNom() : "Inconnue (ID=" + maintenance.getIdM() + ")";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette maintenance ?");
        confirmation.setContentText(
                "Machine  : " + nomMachine + "\n" +
                        "Type     : " + maintenance.getTypePanne() + "\n" +
                        "Cout     : " + String.format("%.2f DT", maintenance.getCout()) + "\n" +
                        "Date     : " + maintenance.getDateMain()
        );

        Optional<ButtonType> res = confirmation.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                maintenanceService.supprimer(maintenance.getIdMain());
                afficherAlerte("Succes", "Maintenance supprimee avec succes", Alert.AlertType.INFORMATION);
                actualiser();
            } catch (SQLException e) {
                afficherAlerte("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // ============================================================
    //  NAVIGATION
    // ============================================================
    @FXML
    private void retourAccueil(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AccueilMateriel.fxml", event);
    }

    @FXML private void naviguerAnimaux(javafx.event.ActionEvent event)    { naviguerDepuisBouton("/AfficherAnimaux.fxml", event); }
    @FXML private void naviguerMateriels(javafx.event.ActionEvent event)  { naviguerDepuisBouton("/AccueilMateriel.fxml", event); }
    @FXML private void naviguerStocks(javafx.event.ActionEvent event)     { naviguerDepuisBouton("/AfficherStocks.fxml", event); }
    @FXML private void naviguerTerrains(javafx.event.ActionEvent event)   { naviguerDepuisBouton("/AfficherTerrains.fxml", event); }
    @FXML private void naviguerEvenements(javafx.event.ActionEvent event) { naviguerDepuisBouton("/AccueilEvenement.fxml", event); }
    @FXML private void naviguerUsers(javafx.event.ActionEvent event)      { naviguerDepuisBouton("/AfficherUsers.fxml", event); }

    @FXML
    private void deconnexion(javafx.event.ActionEvent event) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Deconnexion");
        conf.setHeaderText("Voulez-vous vraiment vous deconnecter ?");
        conf.setContentText("Vous serez redirige vers la page de connexion.");
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
            afficherAlerte("Erreur", "Impossible de naviguer vers : " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
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