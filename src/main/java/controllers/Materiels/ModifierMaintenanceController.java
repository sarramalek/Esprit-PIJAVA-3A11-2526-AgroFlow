package controllers.Materiels;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Materiels.Machine;
import models.Materiels.Maintenance;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ModifierMaintenanceController implements Initializable {

    @FXML private ComboBox<Machine> comboMachine;
    @FXML private TextField txtTypePanne;
    @FXML private DatePicker datePickerMaintenance;
    @FXML private TextField txtCout;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboPriorite;
    @FXML private TextField txtKilometrage;
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtRecommandation;

    @FXML private Label errMachine;
    @FXML private Label errTypePanne;
    @FXML private Label errDate;
    @FXML private Label errCout;
    @FXML private Label errStatut;
    @FXML private Label errPriorite;
    @FXML private Label errKilometrage;
    @FXML private Label errDescription;

    private final MachineService machineService = new MachineService();
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private Maintenance maintenanceAModifier;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerMachineCombo();
        comboStatut.setItems(FXCollections.observableArrayList("planifie", "en_cours", "termine"));
        comboPriorite.setItems(FXCollections.observableArrayList("faible", "moyenne", "haute", "urgente"));

        txtKilometrage.textProperty().addListener((obs, old, value) -> {
            if (value != null && !value.matches("\\d*")) {
                txtKilometrage.setText(value.replaceAll("[^\\d]", ""));
            }
        });
    }

    public void initialiserDonnees(Maintenance maintenance) {
        this.maintenanceAModifier = maintenance;
        remplirChamps();
    }

    private void configurerMachineCombo() {
        try {
            List<Machine> machines = machineService.recuperer();
            comboMachine.setItems(FXCollections.observableArrayList(machines));
            comboMachine.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    setText(empty || machine == null ? null : machine.getNom());
                }
            });
            comboMachine.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    setText(empty || machine == null ? "Selectionner une machine" : machine.getNom());
                }
            });
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les machines : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void remplirChamps() {
        if (maintenanceAModifier == null) return;

        for (Machine machine : comboMachine.getItems()) {
            if (machine.getIdM() == maintenanceAModifier.getIdM()) {
                comboMachine.setValue(machine);
                break;
            }
        }

        txtTypePanne.setText(valeur(maintenanceAModifier.getTypePanne()));
        datePickerMaintenance.setValue(maintenanceAModifier.getDateMain() != null
                ? maintenanceAModifier.getDateMain()
                : LocalDate.now());
        txtCout.setText(String.valueOf(maintenanceAModifier.getCout()));
        comboStatut.setValue(maintenanceAModifier.getStatut() != null
                ? maintenanceAModifier.getStatut()
                : "planifie");
        comboPriorite.setValue(maintenanceAModifier.getPriorite() != null
                ? maintenanceAModifier.getPriorite()
                : "moyenne");
        txtKilometrage.setText(String.valueOf(maintenanceAModifier.getKilometrage()));
        txtDescription.setText(valeur(maintenanceAModifier.getDescription()));
        txtRecommandation.setText(valeur(maintenanceAModifier.getRecommandation()));
    }

    @FXML
    private void modifier(ActionEvent event) {
        if (maintenanceAModifier == null || !valider()) return;

        try {
            Machine machine = comboMachine.getValue();
            String typePanne = txtTypePanne.getText().trim();
            String recommandation = texte(txtRecommandation);

            maintenanceAModifier.setIdM(machine.getIdM());
            maintenanceAModifier.setTypePanne(typePanne);
            maintenanceAModifier.setDateMain(datePickerMaintenance.getValue());
            maintenanceAModifier.setCout(Double.parseDouble(txtCout.getText().trim().replace(",", ".")));
            maintenanceAModifier.setStatut(comboStatut.getValue());
            maintenanceAModifier.setPriorite(comboPriorite.getValue());
            maintenanceAModifier.setKilometrage(Integer.parseInt(txtKilometrage.getText().trim()));
            maintenanceAModifier.setDescription(texte(txtDescription));
            maintenanceAModifier.setRecommandation(recommandation == null
                    ? genererRecommandationIA(machine.getNom(), typePanne)
                    : recommandation);

            maintenanceService.modifier(maintenanceAModifier);
            afficherAlerte("Succes", "Maintenance modifiee avec succes", Alert.AlertType.INFORMATION);
            fermerFenetre(event);
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean valider() {
        effacerErreurs();

        if (comboMachine.getValue() == null) {
            errMachine.setText("Selectionnez une machine");
            return false;
        }
        if (txtTypePanne.getText() == null || txtTypePanne.getText().trim().isEmpty()) {
            errTypePanne.setText("Type de panne obligatoire");
            return false;
        }
        if (txtTypePanne.getText().trim().length() < 3) {
            errTypePanne.setText("Minimum 3 caracteres");
            return false;
        }
        if (datePickerMaintenance.getValue() == null) {
            errDate.setText("Date obligatoire");
            return false;
        }
        if (datePickerMaintenance.getValue().isAfter(LocalDate.now())) {
            errDate.setText("La date ne peut pas etre dans le futur");
            return false;
        }
        try {
            double cout = Double.parseDouble(txtCout.getText().trim().replace(",", "."));
            if (cout < 0) throw new NumberFormatException();
        } catch (Exception e) {
            errCout.setText("Cout valide obligatoire");
            return false;
        }
        if (comboStatut.getValue() == null) {
            errStatut.setText("Statut obligatoire");
            return false;
        }
        if (comboPriorite.getValue() == null) {
            errPriorite.setText("Priorite obligatoire");
            return false;
        }
        try {
            int km = Integer.parseInt(txtKilometrage.getText().trim());
            if (km < 0) throw new NumberFormatException();
        } catch (Exception e) {
            errKilometrage.setText("Kilometrage valide obligatoire");
            return false;
        }
        return true;
    }

    private void effacerErreurs() {
        errMachine.setText("");
        errTypePanne.setText("");
        errDate.setText("");
        errCout.setText("");
        errStatut.setText("");
        errPriorite.setText("");
        errKilometrage.setText("");
        if (errDescription != null) errDescription.setText("");
    }

    private String texte(TextInputControl input) {
        String value = input.getText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String valeur(String value) {
        return value == null ? "" : value;
    }

    private String genererRecommandationIA(String nomMachine, String typePanne) {
        String machine = nomMachine != null ? nomMachine : "la machine";
        String type = typePanne != null ? typePanne.toLowerCase(Locale.ROOT) : "";
        if (type.contains("moteur") || type.contains("surchauffe")) {
            return "IA: Pour " + machine + ", controler le moteur, l'huile et le refroidissement avant utilisation.";
        }
        if (type.contains("hydraul")) {
            return "IA: Pour " + machine + ", verifier les flexibles, joints et la pression hydraulique.";
        }
        if (type.contains("elect") || type.contains("batterie")) {
            return "IA: Pour " + machine + ", tester la batterie, les fusibles et le cablage.";
        }
        if (type.contains("frein")) {
            return "IA: Pour " + machine + ", inspecter les freins et tester le freinage.";
        }
        return "IA: Pour " + machine + ", effectuer un diagnostic complet et planifier une maintenance preventive.";
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre(event);
    }

    private void fermerFenetre(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
