package controllers.Materiels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.ResourceBundle;

public class AjouterMaintenanceController implements Initializable {

    // ============================================================
    //  CHAMPS FXML
    // ============================================================

    /**
     * ComboBox<Machine> : affiche le NOM de la machine (via cellFactory)
     * mais stocke l'objet Machine complet → on récupère l'idM proprement.
     */
    @FXML private ComboBox<Machine> comboMachine;

    @FXML private TextField  txtTypePanne;
    @FXML private DatePicker datePickerMain;
    @FXML private TextField  txtCout;
    @FXML private TextArea   txtDescription;

    // Labels d'erreur inline
    @FXML private Label errMachine;
    @FXML private Label errTypePanne;
    @FXML private Label errDate;
    @FXML private Label errCout;

    // ============================================================
    //  SERVICES
    // ============================================================
    private final MachineService     machineService     = new MachineService();
    private final MaintenanceService maintenanceService = new MaintenanceService();

    // ============================================================
    //  INITIALISATION
    // ============================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerComboMachine();   // JOINTURE : charge les machines et configure l'affichage
        datePickerMain.setValue(LocalDate.now());  // date par défaut = aujourd'hui
    }

    /**
     * Configure le ComboBox pour :
     * - afficher uniquement le NOM de la machine dans la liste
     * - stocker l'objet Machine complet (idM accessible via machine.getIdM())
     *
     * C'est LA clé de la jointure idM ↔ nom_machine.
     */
    private void configurerComboMachine() {
        try {
            List<Machine> machines = machineService.recuperer();

            if (machines.isEmpty()) {
                afficherAlerte("Attention",
                        "Aucune machine disponible. Veuillez d'abord ajouter des machines.",
                        Alert.AlertType.WARNING);
                return;
            }

            ObservableList<Machine> listeMachines = FXCollections.observableArrayList(machines);
            comboMachine.setItems(listeMachines);

            // -------------------------------------------------------
            //  cellFactory  : affiche le NOM dans chaque ligne de la liste déroulante
            // -------------------------------------------------------
            comboMachine.setCellFactory(lv -> new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText(null);
                    } else {
                        // Affiche : "Tracteur  (ID: 3)"  — retirez la partie ID si non souhaitée
                        setText(machine.getNom() + "   (ID: " + machine.getIdM() + ")");
                    }
                }
            });

            // -------------------------------------------------------
            //  buttonCell  : affiche le NOM dans le bouton du ComboBox après sélection
            // -------------------------------------------------------
            comboMachine.setButtonCell(new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText("Selectionner une machine");
                        setStyle("-fx-text-fill: #a0aec0;");
                    } else {
                        setText(machine.getNom());
                        setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    }
                }
            });

            System.out.println("[OK] " + machines.size() + " machine(s) chargee(s) dans le ComboBox");

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
        // 1. Réinitialiser les erreurs
        effacerErreurs();

        // 2. Valider les champs
        if (!valider()) return;

        // 3. Récupérer la machine sélectionnée → idM via JOINTURE
        Machine machineSelectionnee = comboMachine.getValue();
        int idM = machineSelectionnee.getIdM();  // clé étrangère vers table Machine

        // 4. Construire l'objet Maintenance
        String   typePanne   = txtTypePanne.getText().trim();
        LocalDate date       = datePickerMain.getValue();
        double   cout        = Double.parseDouble(txtCout.getText().trim().replace(",", "."));
        String   description = txtDescription.getText().trim();

        Maintenance maintenance = new Maintenance();
        maintenance.setIdM(idM);                // FK → Machine.idM  (jointure)
        maintenance.setTypePanne(typePanne);
        maintenance.setDateMain(date);
        maintenance.setCout(cout);
        maintenance.setDescription(description.isEmpty() ? null : description);

        // 5. Persister
        try {
            maintenanceService.ajouter(maintenance);
            afficherAlerte("Succes",
                    "Maintenance ajoutee avec succes pour la machine : " + machineSelectionnee.getNom(),
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

        // Machine obligatoire
        if (comboMachine.getValue() == null) {
            errMachine.setText("Veuillez selectionner une machine.");
            surligner(comboMachine);
            valide = false;
        }

        // Type de panne obligatoire
        if (txtTypePanne.getText().trim().isEmpty()) {
            errTypePanne.setText("Le type de panne est obligatoire.");
            surligner(txtTypePanne);
            valide = false;
        }

        // Date obligatoire
        if (datePickerMain.getValue() == null) {
            errDate.setText("Veuillez choisir une date.");
            valide = false;
        } else if (datePickerMain.getValue().isAfter(LocalDate.now())) {
            errDate.setText("La date ne peut pas etre dans le futur.");
            valide = false;
        }

        // Coût : obligatoire + numérique + positif
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

        // Retirer le surlignage rouge
        String styleNormal = "-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px; -fx-padding: 8;";
        txtTypePanne.setStyle(styleNormal);
        txtCout.setStyle(styleNormal);
        comboMachine.setStyle("-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px;");
    }

    /** Surligne un champ en rouge pour signaler une erreur */
    private void surligner(Control control) {
        control.setStyle(control.getStyle() +
                "; -fx-border-color: #e74c3c; -fx-border-width: 2;");
    }

    // ============================================================
    //  ANNULER
    // ============================================================
    @FXML
    private void annuler() {
        fermerFenetre();
    }

    // ============================================================
    //  UTILITAIRES
    // ============================================================
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