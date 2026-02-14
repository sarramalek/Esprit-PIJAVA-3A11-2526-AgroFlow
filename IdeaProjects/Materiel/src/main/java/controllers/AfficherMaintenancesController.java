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
    @FXML private TableColumn<Maintenance, String> colMachine;
    @FXML private TableColumn<Maintenance, String> colTypePanne;
    @FXML private TableColumn<Maintenance, LocalDate> colDate;
    @FXML private TableColumn<Maintenance, Double> colCout;
    @FXML private TableColumn<Maintenance, String> colDescription;
    @FXML private TableColumn<Maintenance, Void> colActions;

    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;

    private MaintenanceService maintenanceService = new MaintenanceService();
    private MachineService machineService = new MachineService();
    private ObservableList<Maintenance> listeMaintenances = FXCollections.observableArrayList();

    // Map pour la jointure : idM -> Machine
    private Map<Integer, Machine> mapMachines = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Charger d'abord les machines pour la jointure
        chargerMachines();
        // Puis configurer le tableau
        configurationTableau();
        // Charger les maintenances
        chargerMaintenances();
        // Mettre à jour les stats
        mettreAJourStatistiques();
    }

    private void configurationTableau() {
        // ✅ Colonne Machine - JOINTURE avec Map
        colMachine.setCellValueFactory(cellData -> {
            int idM = cellData.getValue().getIdM();

            // Récupérer la machine depuis le Map (JOINTURE)
            Machine machine = mapMachines.get(idM);
            String nomMachine = (machine != null) ? machine.getNom() : "Machine inconnue (ID: " + idM + ")";

            return new javafx.beans.property.SimpleStringProperty(nomMachine);
        });

        colTypePanne.setCellValueFactory(new PropertyValueFactory<>("typePanne"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateMain"));

        // Format du coût avec 2 décimales
        colCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colCout.setCellFactory(col -> new TableCell<Maintenance, Double>() {
            @Override
            protected void updateItem(Double cout, boolean empty) {
                super.updateItem(cout, empty);
                if (empty || cout == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", cout));
                }
            }
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // ✅ Colonne Actions avec icônes
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("✏️ Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");
            private final HBox conteneur = new HBox(8, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 6 12; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-size: 12px;");
                btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 6 12; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-size: 12px;");

                btnModifier.setOnAction(event -> {
                    Maintenance maintenance = getTableView().getItems().get(getIndex());
                    ouvrirModification(maintenance);
                });

                btnSupprimer.setOnAction(event -> {
                    Maintenance maintenance = getTableView().getItems().get(getIndex());
                    supprimerMaintenance(maintenance);
                });
            }

            @Override
            protected void updateItem(Void item, boolean vide) {
                super.updateItem(item, vide);
                setGraphic(vide ? null : conteneur);
            }
        });
    }

    private void chargerMachines() {
        try {
            List<Machine> machines = machineService.recuperer();

            // Remplir le Map pour la jointure (idM -> Machine)
            mapMachines.clear();
            for (Machine machine : machines) {
                mapMachines.put(machine.getIdM(), machine);
            }

            // Remplir le ComboBox
            ObservableList<String> nomsMachines = FXCollections.observableArrayList("Toutes les machines");
            nomsMachines.addAll(machines.stream()
                    .map(Machine::getNom)
                    .collect(Collectors.toList()));
            comboMachine.setItems(nomsMachines);
            comboMachine.setValue("Toutes les machines");

            System.out.println("✅ " + machines.size() + " machines chargées pour la jointure");

        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors du chargement des machines: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void chargerMaintenances() {
        try {
            List<Maintenance> maintenances = maintenanceService.recuperer();
            listeMaintenances.setAll(maintenances);
            tableMaintenances.setItems(listeMaintenances);

            System.out.println("✅ " + maintenances.size() + " maintenances chargées");

        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors du chargement des maintenances: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void rechercher() {
        String recherche = champRecherche.getText().toLowerCase().trim();

        if (recherche.isEmpty()) {
            tableMaintenances.setItems(listeMaintenances);
        } else {
            ObservableList<Maintenance> filtrees = listeMaintenances.filtered(m -> {
                // Recherche dans type panne
                if (m.getTypePanne().toLowerCase().contains(recherche)) {
                    return true;
                }
                // Recherche dans description
                if (m.getDescription() != null && m.getDescription().toLowerCase().contains(recherche)) {
                    return true;
                }
                // Recherche dans nom machine (JOINTURE)
                Machine machine = mapMachines.get(m.getIdM());
                if (machine != null && machine.getNom().toLowerCase().contains(recherche)) {
                    return true;
                }
                return false;
            });
            tableMaintenances.setItems(filtrees);
        }
        mettreAJourStatistiques();
    }

    @FXML
    private void filtrer() {
        String machineSelectionnee = comboMachine.getValue();

        if (machineSelectionnee == null || machineSelectionnee.equals("Toutes les machines")) {
            tableMaintenances.setItems(listeMaintenances);
        } else {
            // Trouver l'idM de la machine sélectionnée
            Integer idMachine = null;
            for (Map.Entry<Integer, Machine> entry : mapMachines.entrySet()) {
                if (entry.getValue().getNom().equals(machineSelectionnee)) {
                    idMachine = entry.getKey();
                    break;
                }
            }

            if (idMachine != null) {
                final Integer idMachineFinal = idMachine;
                ObservableList<Maintenance> filtrees = listeMaintenances.filtered(m ->
                        m.getIdM() == idMachineFinal
                );
                tableMaintenances.setItems(filtrees);
            }
        }
        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        int total = tableMaintenances.getItems().size();
        double coutTotal = tableMaintenances.getItems().stream()
                .mapToDouble(Maintenance::getCout)
                .sum();

        lblTotal.setText(String.valueOf(total));
        lblCoutTotal.setText(String.format("%.2f DT", coutTotal));
    }

    @FXML
    private void actualiser() {
        chargerMaintenances();
        champRecherche.clear();
        comboMachine.setValue("Toutes les machines");
        mettreAJourStatistiques();
        afficherAlerte("Actualisation", "✅ Les données ont été actualisées avec succès", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void ouvrirAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMaintenance.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("➕ Ajouter une Maintenance");
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
            stage.setTitle("✏️ Modifier une Maintenance");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            actualiser();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void supprimerMaintenance(Maintenance maintenance) {
        // Récupérer le nom de la machine pour l'affichage (JOINTURE)
        Machine machine = mapMachines.get(maintenance.getIdM());
        String nomMachine = (machine != null) ? machine.getNom() : "Machine inconnue";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("⚠️ Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la maintenance");
        confirmation.setContentText("Voulez-vous vraiment supprimer cette maintenance ?\n\n" +
                "🔧 Machine: " + nomMachine + "\n" +
                "⚠️ Type: " + maintenance.getTypePanne() + "\n" +
                "💰 Coût: " + String.format("%.2f", maintenance.getCout()) + " DT\n" +
                "📅 Date: " + maintenance.getDateMain());

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                maintenanceService.supprimer(maintenance.getIdMain());
                afficherAlerte("Succès", "✅ Maintenance supprimée avec succès", Alert.AlertType.INFORMATION);
                actualiser();
            } catch (SQLException e) {
                afficherAlerte("Erreur", "❌ Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void retourAccueil(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AccueilMateriel.fxml", event);
    }

    // ==================== NAVIGATION SIDEBAR ====================

    @FXML private void naviguerAnimaux(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherAnimaux.fxml", event);
    }

    @FXML private void naviguerMateriels(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AccueilMateriel.fxml", event);
    }

    @FXML private void naviguerStocks(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherStocks.fxml", event);
    }

    @FXML private void naviguerTerrains(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherTerrains.fxml", event);
    }

    @FXML private void naviguerEvenements(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AccueilEvenement.fxml", event);
    }

    @FXML private void naviguerUsers(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherUsers.fxml", event);
    }

    @FXML private void deconnexion(javafx.event.ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("🚪 Déconnexion");
        confirmation.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        confirmation.setContentText("Vous serez redirigé vers la page de connexion.");

        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    private void naviguerDepuisBouton(String fxmlPath, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de naviguer vers: " + fxmlPath, Alert.AlertType.ERROR);
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