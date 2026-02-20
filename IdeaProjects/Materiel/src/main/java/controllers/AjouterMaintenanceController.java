package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import entities.Machine;
import entities.Maintenance;
import services.MachineService;
import services.MaintenanceService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterMaintenanceController implements Initializable {

    @FXML private ComboBox<Machine> comboMachine;
    @FXML private TextField         txtTypePanne;
    @FXML private DatePicker        datePickerMain;
    @FXML private TextField         txtCout;
    @FXML private TextArea          txtDescription;

    // Labels d'erreur inline
    @FXML private Label errMachine;
    @FXML private Label errTypePanne;
    @FXML private Label errDate;
    @FXML private Label errCout;

    private final MachineService     machineService     = new MachineService();
    private final MaintenanceService maintenanceService = new MaintenanceService();

    // ============================================================
    //  INITIALISATION
    // ============================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerComboMachine();
        datePickerMain.setValue(LocalDate.now());
    }

    private void configurerComboMachine() {
        try {
            List<Machine> machines = machineService.recuperer();

            if (machines.isEmpty()) {
                afficherAlerte("Attention",
                        "Aucune machine disponible. Veuillez d'abord ajouter des machines.",
                        Alert.AlertType.WARNING);
                return;
            }

            comboMachine.setItems(FXCollections.observableArrayList(machines));

            // Affiche uniquement le NOM dans la liste deroulante
            comboMachine.setCellFactory(lv -> new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText(null);
                    } else {
                        setText(machine.getNom());  // NOM seulement, sans ID
                    }
                }
            });

            // Affiche uniquement le NOM apres selection dans le bouton
            comboMachine.setButtonCell(new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText("Selectionner une machine");
                        setStyle("-fx-text-fill: #a0aec0;");
                    } else {
                        setText(machine.getNom());  // NOM seulement, sans ID
                        setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    }
                }
            });

        } catch (SQLException e) {
            afficherAlerte("Erreur", "Impossible de charger les machines : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  ENREGISTRER
    // ============================================================
    @FXML
    private void enregistrer() {
        effacerErreurs();
        if (!valider()) return;

        Machine machineSelectionnee = comboMachine.getValue();
        int     idM         = machineSelectionnee.getIdM();
        String  typePanne   = txtTypePanne.getText().trim();
        LocalDate date      = datePickerMain.getValue();
        double  cout        = Double.parseDouble(txtCout.getText().trim().replace(",", "."));
        String  description = txtDescription.getText().trim();

        Maintenance maintenance = new Maintenance();
        maintenance.setIdM(idM);
        maintenance.setTypePanne(typePanne);
        maintenance.setDateMain(date);
        maintenance.setCout(cout);
        maintenance.setDescription(description.isEmpty() ? null : description);

        try {
            maintenanceService.ajouter(maintenance);
            afficherAlerte("Succes",
                    "Maintenance ajoutee avec succes pour : " + machineSelectionnee.getNom(),
                    Alert.AlertType.INFORMATION);
            fermerFenetre();
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  VALIDATION
    // ============================================================
    private boolean valider() {
        boolean valide = true;

        if (comboMachine.getValue() == null) {
            errMachine.setText("Veuillez selectionner une machine.");
            surligner(comboMachine);
            valide = false;
        }

        if (txtTypePanne.getText().trim().isEmpty()) {
            errTypePanne.setText("Le type de panne est obligatoire.");
            surligner(txtTypePanne);
            valide = false;
        }

        if (datePickerMain.getValue() == null) {
            errDate.setText("Veuillez choisir une date.");
            valide = false;
        } else if (datePickerMain.getValue().isAfter(LocalDate.now())) {
            errDate.setText("La date ne peut pas etre dans le futur.");
            valide = false;
        }

        String coutStr = txtCout.getText().trim().replace(",", ".");
        if (coutStr.isEmpty()) {
            errCout.setText("Le cout est obligatoire.");
            surligner(txtCout);
            valide = false;
        } else {
            try {
                double cout = Double.parseDouble(coutStr);
                if (cout < 0) {
                    errCout.setText("Le cout doit etre positif ou nul.");
                    surligner(txtCout);
                    valide = false;
                }
            } catch (NumberFormatException e) {
                errCout.setText("Valeur numerique invalide (ex: 150.00).");
                surligner(txtCout);
                valide = false;
            }
        }

        return valide;
    }

    private void effacerErreurs() {
        errMachine.setText("");
        errTypePanne.setText("");
        errDate.setText("");
        errCout.setText("");

        String styleNormal = "-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px; -fx-padding: 8;";
        txtTypePanne.setStyle(styleNormal);
        txtCout.setStyle(styleNormal);
        comboMachine.setStyle("-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px;");
    }

    private void surligner(Control control) {
        control.setStyle(control.getStyle() + "; -fx-border-color: #e74c3c; -fx-border-width: 2;");
    }

    // ============================================================
    //  ANNULER
    // ============================================================
    @FXML
    private void annuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) comboMachine.getScene().getWindow();
        stage.close();
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}