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

public class AjouterMaintenanceController implements Initializable {

    @FXML private ComboBox<Machine> comboMachine;
    @FXML private TextField txtTypePanne;
    @FXML private DatePicker datePickerMain;
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

    private final MachineService machineService = new MachineService();
    private final MaintenanceService maintenanceService = new MaintenanceService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerMachineCombo();
        comboStatut.setItems(FXCollections.observableArrayList("planifie", "en_cours", "termine"));
        comboStatut.setValue("planifie");
        comboPriorite.setItems(FXCollections.observableArrayList("faible", "moyenne", "haute", "urgente"));
        comboPriorite.setValue("moyenne");
        datePickerMain.setValue(LocalDate.now());

        txtKilometrage.textProperty().addListener((obs, old, value) -> {
            if (value != null && !value.matches("\\d*")) {
                txtKilometrage.setText(value.replaceAll("[^\\d]", ""));
            }
        });
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
            showAlert("Erreur", "Impossible de charger les machines : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void enregistrer(ActionEvent event) {
        if (!valider()) return;

        try {
            Machine machine = comboMachine.getValue();
            String typePanne = txtTypePanne.getText().trim();
            String recommandation = texte(txtRecommandation);
            double cout = Double.parseDouble(txtCout.getText().trim().replace(",", "."));

            Maintenance maintenance = new Maintenance();
            maintenance.setIdM(machine.getIdM());
            maintenance.setTypePanne(typePanne);
            maintenance.setDateMain(datePickerMain.getValue());
            maintenance.setCout(cout);
            maintenance.setStatut(comboStatut.getValue());
            maintenance.setPriorite(comboPriorite.getValue());
            maintenance.setKilometrage(Integer.parseInt(txtKilometrage.getText().trim()));
            maintenance.setDescription(texte(txtDescription));
            maintenance.setRecommandation(recommandation == null
                    ? genererRecommandationIA(machine.getNom(), typePanne)
                    : recommandation);

            if (!confirmerAlerteMaintenance(maintenance)) return;

            maintenanceService.ajouter(maintenance);
            showAlert("Succes", "Maintenance ajoutee avec succes", Alert.AlertType.INFORMATION);
            fermerFenetre(event);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
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
        if (datePickerMain.getValue() == null) {
            errDate.setText("Date obligatoire");
            return false;
        }
        if (datePickerMain.getValue().isAfter(LocalDate.now())) {
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

    @FXML
    private void genererRecommandation(ActionEvent event) {
        if (comboMachine.getValue() == null) {
            errMachine.setText("Selectionnez une machine avant de generer la recommandation");
            return;
        }
        if (txtTypePanne.getText() == null || txtTypePanne.getText().trim().isEmpty()) {
            errTypePanne.setText("Saisissez le type de panne avant de generer la recommandation");
            return;
        }
        effacerErreurs();
        txtRecommandation.setText(genererRecommandationIA(
                comboMachine.getValue().getNom(),
                txtTypePanne.getText().trim()
        ));
    }

    private void effacerErreurs() {
        errMachine.setText("");
        errTypePanne.setText("");
        errDate.setText("");
        errCout.setText("");
        errStatut.setText("");
        errPriorite.setText("");
        errKilometrage.setText("");
    }

    private String texte(TextInputControl input) {
        String value = input.getText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
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

    private boolean confirmerAlerteMaintenance(Maintenance maintenance) {
        boolean prioriteCritique = "urgente".equalsIgnoreCase(maintenance.getPriorite())
                || "haute".equalsIgnoreCase(maintenance.getPriorite());
        boolean coutEleve = maintenance.getCout() >= 1000;

        if (!prioriteCritique && !coutEleve) return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alerte maintenance");
        alert.setHeaderText("Cette maintenance demande une attention particuliere");
        alert.setContentText(
                "Machine : " + comboMachine.getValue().getNom() + "\n"
                        + "Type : " + maintenance.getTypePanne() + "\n"
                        + "Priorite : " + maintenance.getPriorite() + "\n"
                        + "Cout : " + String.format("%.2f DT", maintenance.getCout()) + "\n\n"
                        + "Voulez-vous sauvegarder cette maintenance ?"
        );
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void fermerFenetre(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre(event);
    }
}
