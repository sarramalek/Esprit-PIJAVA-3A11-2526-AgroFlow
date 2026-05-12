package controllers.Materiels;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Materiels.Machine;
import models.Materiels.Maintenance;
import models.User.Personne;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AgriModifierMaintenanceController implements Initializable {

    // ── fx:id — doivent correspondre exactement au FXML ───────────
    @FXML private ComboBox<Machine> comboMachine;
    @FXML private ComboBox<String>  comboTypePanne;
    @FXML private DatePicker        datePicker;
    @FXML private TextField         txtCout;
    @FXML private TextArea          txtDescription;
    @FXML private ComboBox<String>  comboStatut;
    @FXML private TextArea          txtRecommandation;
    @FXML private ComboBox<String>  comboPriorite;
    @FXML private TextField         txtKilometrage;
    @FXML private Button            btnEnregistrer;
    @FXML private Button            btnAnnuler;

    // ── État interne ───────────────────────────────────────────────
    private Personne                      currentUser;
    private AgricoleMaintenanceController parentController;
    private MachineService                machineService;
    private MaintenanceService            maintenanceService;
    private List<Machine>                 machines;
    private Maintenance                   maintenanceAModifier;

    private static final List<String> TYPES_PANNES = List.of(
            "Panne moteur",
            "Panne hydraulique",
            "Panne électrique",
            "Usure des pneus",
            "Panne de transmission",
            "Panne de freins",
            "Fuite d'huile",
            "Surchauffe",
            "Panne de direction",
            "Dysfonctionnement capteurs",
            "Panne batterie",
            "Autre"
    );

    // ══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        machineService     = new MachineService();
        maintenanceService = new MaintenanceService();

        chargerMachines();
        initialiserComboTypePanne();
        initialiserComboStatut();
        initialiserComboPriorite();

        btnEnregistrer.setOnAction(e -> modifier());
        btnAnnuler.setOnAction(e -> fermer());
    }

    // ══════════════════════════════════════════════════════════════
    // LISTES DÉROULANTES
    // ══════════════════════════════════════════════════════════════

    private void chargerMachines() {
        try {
            machines = machineService.recuperer();
            comboMachine.setItems(FXCollections.observableArrayList(machines));

            comboMachine.setCellFactory(lv -> new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine m, boolean empty) {
                    super.updateItem(m, empty);
                    if (empty || m == null) setText(null);
                    else setText(m.getNom());
                }
            });

            comboMachine.setButtonCell(new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine m, boolean empty) {
                    super.updateItem(m, empty);
                    if (empty || m == null) setText("Sélectionner une machine");
                    else setText(m.getNom());
                }
            });

        } catch (SQLException e) {
            showError("Erreur chargement machines : " + e.getMessage());
        }
    }

    private void initialiserComboTypePanne() {
        comboTypePanne.setItems(FXCollections.observableArrayList(TYPES_PANNES));
        // editable="true" est déjà dans le FXML
    }

    private void initialiserComboStatut() {
        comboStatut.setItems(FXCollections.observableArrayList(
                "en_cours", "termine", "planifie"));

        comboStatut.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(switch (item) {
                    case "en_cours" -> "🔧 En cours";
                    case "termine"  -> "✅ Terminé";
                    case "planifie" -> "📅 Planifié";
                    default         -> item;
                });
            }
        });
        comboStatut.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText("Sélectionner un statut"); return; }
                setText(switch (item) {
                    case "en_cours" -> "🔧 En cours";
                    case "termine"  -> "✅ Terminé";
                    case "planifie" -> "📅 Planifié";
                    default         -> item;
                });
            }
        });
    }

    private void initialiserComboPriorite() {
        comboPriorite.setItems(FXCollections.observableArrayList(
                "faible", "moyenne", "haute", "urgente"));

        comboPriorite.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(switch (item) {
                    case "faible"  -> "🟢 Faible";
                    case "moyenne" -> "🟡 Moyenne";
                    case "haute"   -> "🟠 Haute";
                    case "urgente" -> "🔴 Urgente";
                    default        -> item;
                });
            }
        });
        comboPriorite.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText("Sélectionner une priorité"); return; }
                setText(switch (item) {
                    case "faible"  -> "🟢 Faible";
                    case "moyenne" -> "🟡 Moyenne";
                    case "haute"   -> "🟠 Haute";
                    case "urgente" -> "🔴 Urgente";
                    default        -> item;
                });
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // REMPLISSAGE DU FORMULAIRE
    // ══════════════════════════════════════════════════════════════
    public void setMaintenance(Maintenance maintenance) {
        this.maintenanceAModifier = maintenance;
        remplirChamps();
    }

    private void remplirChamps() {
        if (maintenanceAModifier == null) return;

        // Machine
        if (machines != null) {
            machines.stream()
                    .filter(m -> m.getIdM() == maintenanceAModifier.getIdM())
                    .findFirst()
                    .ifPresent(comboMachine::setValue);
        }

        // Type de panne
        String tp = maintenanceAModifier.getTypePanne();
        if (tp != null && !tp.isBlank()) {
            if (!comboTypePanne.getItems().contains(tp))
                comboTypePanne.getItems().add(0, tp);
            comboTypePanne.setValue(tp);
        }

        datePicker.setValue(maintenanceAModifier.getDateMain());
        txtCout.setText(String.valueOf(maintenanceAModifier.getCout()));
        txtDescription.setText(
                maintenanceAModifier.getDescription() != null ? maintenanceAModifier.getDescription() : "");
        comboStatut.setValue(maintenanceAModifier.getStatut());
        txtRecommandation.setText(
                maintenanceAModifier.getRecommandation() != null ? maintenanceAModifier.getRecommandation() : "");
        comboPriorite.setValue(maintenanceAModifier.getPriorite());
        txtKilometrage.setText(String.valueOf(maintenanceAModifier.getKilometrage()));
    }

    // ══════════════════════════════════════════════════════════════
    // ENREGISTREMENT
    // ══════════════════════════════════════════════════════════════
    private void modifier() {

        if (comboMachine.getValue() == null) {
            showError("Veuillez sélectionner une machine."); return;
        }
        String typePanne = comboTypePanne.getValue();
        if (typePanne == null || typePanne.isBlank()) {
            showError("Veuillez sélectionner ou saisir le type de panne."); return;
        }
        if (datePicker.getValue() == null) {
            showError("Veuillez sélectionner une date."); return;
        }
        if (txtCout.getText() == null || txtCout.getText().isBlank()) {
            showError("Veuillez saisir le coût."); return;
        }

        try {
            double cout       = Double.parseDouble(txtCout.getText().trim());
            int    kilometrage = 0;
            if (txtKilometrage.getText() != null && !txtKilometrage.getText().isBlank())
                kilometrage = Integer.parseInt(txtKilometrage.getText().trim());

            maintenanceAModifier.setIdM(comboMachine.getValue().getIdM());
            maintenanceAModifier.setTypePanne(typePanne.trim());
            maintenanceAModifier.setCout(cout);
            maintenanceAModifier.setDateMain(datePicker.getValue());
            maintenanceAModifier.setDescription(
                    txtDescription.getText() != null ? txtDescription.getText().trim() : "");
            maintenanceAModifier.setStatut(
                    comboStatut.getValue() != null ? comboStatut.getValue() : "planifie");
            maintenanceAModifier.setRecommandation(
                    txtRecommandation.getText() != null ? txtRecommandation.getText().trim() : "");
            maintenanceAModifier.setPriorite(
                    comboPriorite.getValue() != null ? comboPriorite.getValue() : "moyenne");
            maintenanceAModifier.setKilometrage(kilometrage);

            maintenanceService.modifier(maintenanceAModifier);

            if (parentController != null) parentController.rafraichirTableau();

            showInfo("✅ Succès", "Maintenance modifiée avec succès.");
            fermer();

        } catch (NumberFormatException e) {
            showError("Le coût et le kilométrage doivent être des nombres valides.");
        } catch (SQLException e) {
            showError("Erreur lors de la modification : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════
    private void fermer() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public void setCurrentUser(Personne user)                           { this.currentUser = user; }
    public void setParentController(AgricoleMaintenanceController ctrl) { this.parentController = ctrl; }
}