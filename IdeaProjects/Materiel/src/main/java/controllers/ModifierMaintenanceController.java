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
import java.util.Optional;
import java.util.ResourceBundle;

public class ModifierMaintenanceController implements Initializable {

    // ============================================================
    //  CHAMPS FXML
    // ============================================================

    /**
     * ComboBox<Machine> : affiche le NOM (via cellFactory/buttonCell)
     * stocke l'objet Machine complet → idM récupéré proprement.
     * Même pattern que AjouterMaintenanceController.
     */
    @FXML private ComboBox<Machine> comboMachine;

    @FXML private TextField  txtTypePanne;
    @FXML private DatePicker datePickerMaintenance;
    @FXML private TextField  txtCout;
    @FXML private TextArea   txtDescription;

    // Labels d'erreur inline
    @FXML private Label errMachine;
    @FXML private Label errTypePanne;
    @FXML private Label errDate;
    @FXML private Label errCout;
    @FXML private Label errDescription;

    // ============================================================
    //  SERVICES + ETAT
    // ============================================================
    private final MachineService     machineService     = new MachineService();
    private final MaintenanceService maintenanceService = new MaintenanceService();

    /** La maintenance passée par AfficherMaintenancesController */
    private Maintenance maintenanceAModifier;

    // ============================================================
    //  INITIALISATION
    // ============================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerComboMachine();
    }

    /**
     * Appelé par AfficherMaintenancesController juste après le chargement du FXML.
     * Configure le ComboBox PUIS pré-remplit les champs avec la maintenance à modifier.
     */
    public void initialiserDonnees(Maintenance maintenance) {
        this.maintenanceAModifier = maintenance;
        remplirChamps();
    }

    // ============================================================
    //  CONFIGURATION COMBOBOX (JOINTURE idM ↔ nom)
    // ============================================================

    /**
     * Charge toutes les machines et configure le ComboBox pour :
     *  - afficher le NOM dans la liste déroulante  (cellFactory)
     *  - afficher le NOM dans le bouton            (buttonCell)
     *  - stocker l'objet Machine complet           → machine.getIdM() = clé étrangère
     */
    private void configurerComboMachine() {
        try {
            List<Machine> machines = machineService.recuperer();

            if (machines.isEmpty()) {
                afficherAlerte("Attention",
                        "Aucune machine disponible.",
                        Alert.AlertType.WARNING);
                return;
            }

            ObservableList<Machine> listeMachines = FXCollections.observableArrayList(machines);
            comboMachine.setItems(listeMachines);

            // Affichage dans la liste déroulante : "Tracteur  (ID: 3)"
            comboMachine.setCellFactory(lv -> new ListCell<Machine>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText(null);
                    } else {
                        setText(machine.getNom() + "   (ID: " + machine.getIdM() + ")");
                    }
                }
            });

            // Affichage dans le bouton après sélection : juste "Tracteur"
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
            afficherAlerte("Erreur",
                    "Impossible de charger les machines : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  PRE-REMPLISSAGE DES CHAMPS
    // ============================================================

    /**
     * Pré-remplit tous les champs avec les valeurs de la maintenance existante.
     * Pour la machine : cherche dans la liste du ComboBox l'objet dont l'idM
     * correspond à maintenance.getIdM() → sélection directe, sans Map<String, Machine>.
     */
    private void remplirChamps() {
        if (maintenanceAModifier == null) return;

        // --- Machine : JOINTURE idM -> objet Machine dans le ComboBox ---
        int idMRecherche = maintenanceAModifier.getIdM();
        Machine machineSelectionnee = null;

        for (Machine m : comboMachine.getItems()) {
            if (m.getIdM() == idMRecherche) {
                machineSelectionnee = m;
                break;
            }
        }

        if (machineSelectionnee != null) {
            comboMachine.setValue(machineSelectionnee);
            System.out.println("[OK] Machine pre-selectionnee : " + machineSelectionnee.getNom()
                    + " (idM=" + idMRecherche + ")");
        } else {
            System.err.println("[WARN] Aucune machine trouvee pour idM=" + idMRecherche);
        }

        // --- Autres champs ---
        txtTypePanne.setText(maintenanceAModifier.getTypePanne() != null
                ? maintenanceAModifier.getTypePanne() : "");

        datePickerMaintenance.setValue(maintenanceAModifier.getDateMain() != null
                ? maintenanceAModifier.getDateMain() : LocalDate.now());

        txtCout.setText(String.valueOf(maintenanceAModifier.getCout()));

        txtDescription.setText(maintenanceAModifier.getDescription() != null
                ? maintenanceAModifier.getDescription() : "");
    }

    // ============================================================
    //  MODIFIER (enregistrement)
    // ============================================================
    @FXML
    private void modifier() {
        effacerErreurs();

        if (!valider()) return;

        try {
            // --- JOINTURE : récupérer l'idM depuis la machine sélectionnée ---
            Machine machine = comboMachine.getValue();
            int idM = machine.getIdM();  // clé étrangère vers table Machine

            // --- Mettre à jour l'objet ---
            maintenanceAModifier.setIdM(idM);
            maintenanceAModifier.setTypePanne(txtTypePanne.getText().trim());
            maintenanceAModifier.setDateMain(datePickerMaintenance.getValue());
            maintenanceAModifier.setCout(Double.parseDouble(
                    txtCout.getText().trim().replace(",", ".")));
            String desc = txtDescription.getText().trim();
            maintenanceAModifier.setDescription(desc.isEmpty() ? null : desc);

            System.out.println("[OK] Modification :");
            System.out.println("     Machine  : " + machine.getNom() + " (idM=" + idM + ")");
            System.out.println("     Type     : " + maintenanceAModifier.getTypePanne());
            System.out.println("     Date     : " + maintenanceAModifier.getDateMain());
            System.out.println("     Cout     : " + maintenanceAModifier.getCout() + " DT");

            // --- Persister ---
            maintenanceService.modifier(maintenanceAModifier);

            afficherAlerte("Succes",
                    "Maintenance modifiee avec succes pour : " + machine.getNom(),
                    Alert.AlertType.INFORMATION);
            fermerFenetre();

        } catch (NumberFormatException e) {
            errCout.setText("Valeur numerique invalide.");
            txtCout.requestFocus();
        } catch (SQLException e) {
            afficherAlerte("Erreur",
                    "Erreur base de donnees : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ============================================================
    //  VALIDATION
    // ============================================================
    private boolean valider() {
        boolean ok = true;

        // Machine obligatoire
        if (comboMachine.getValue() == null) {
            errMachine.setText("Veuillez selectionner une machine.");
            surligner(comboMachine);
            ok = false;
        }

        // Type de panne
        String type = txtTypePanne.getText().trim();
        if (type.isEmpty()) {
            errTypePanne.setText("Le type de panne est obligatoire.");
            surligner(txtTypePanne);
            ok = false;
        } else if (type.length() < 3) {
            errTypePanne.setText("Minimum 3 caracteres requis.");
            surligner(txtTypePanne);
            ok = false;
        } else if (!type.matches("^[a-zA-ZÀ-ÿ\\s'\\-]+$")) {
            errTypePanne.setText("Lettres, espaces, apostrophes et tirets uniquement.");
            surligner(txtTypePanne);
            ok = false;
        }

        // Date
        LocalDate date = datePickerMaintenance.getValue();
        if (date == null) {
            errDate.setText("La date est obligatoire.");
            ok = false;
        } else if (date.isAfter(LocalDate.now())) {
            errDate.setText("La date ne peut pas etre dans le futur.");
            ok = false;
        } else if (date.isBefore(LocalDate.now().minusYears(10))) {
            // Confirmation pour date ancienne (non bloquant)
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
            conf.setTitle("Date ancienne");
            conf.setHeaderText("La date selectionnee est tres ancienne (" + date + ").");
            conf.setContentText("Voulez-vous continuer ?");
            Optional<ButtonType> res = conf.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) ok = false;
        }

        // Coût
        String coutStr = txtCout.getText().trim().replace(",", ".");
        if (coutStr.isEmpty()) {
            errCout.setText("Le cout est obligatoire.");
            surligner(txtCout);
            ok = false;
        } else {
            try {
                double cout = Double.parseDouble(coutStr);
                if (cout < 0) {
                    errCout.setText("Le cout ne peut pas etre negatif.");
                    surligner(txtCout);
                    ok = false;
                } else if (cout == 0) {
                    Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
                    conf.setTitle("Cout nul");
                    conf.setHeaderText("Le cout est de 0 DT.");
                    conf.setContentText("Confirmer ?");
                    Optional<ButtonType> res = conf.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) ok = false;
                } else if (cout > 100000) {
                    Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
                    conf.setTitle("Cout eleve");
                    conf.setHeaderText(String.format("Cout: %.2f DT — Confirmer ?", cout));
                    conf.setContentText("Ce montant semble tres eleve.");
                    Optional<ButtonType> res = conf.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) ok = false;
                }
            } catch (NumberFormatException e) {
                errCout.setText("Valeur numerique invalide (ex: 450.00).");
                surligner(txtCout);
                ok = false;
            }
        }

        // Description
        String desc = txtDescription.getText().trim();
        if (desc.isEmpty()) {
            errDescription.setText("La description est obligatoire.");
            surligner(txtDescription);
            ok = false;
        } else if (desc.length() < 10) {
            errDescription.setText("Minimum 10 caracteres requis (" + desc.length() + " actuellement).");
            surligner(txtDescription);
            ok = false;
        } else if (desc.split("\\s+").length < 3) {
            errDescription.setText("Minimum 3 mots requis.");
            surligner(txtDescription);
            ok = false;
        }

        return ok;
    }

    private void effacerErreurs() {
        errMachine.setText("");
        errTypePanne.setText("");
        errDate.setText("");
        errCout.setText("");
        errDescription.setText("");

        String styleNormal = "-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px; -fx-padding: 8;";
        txtTypePanne.setStyle(styleNormal);
        txtCout.setStyle(styleNormal);
        txtDescription.setStyle("-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px;");
        comboMachine.setStyle("-fx-background-radius: 6; -fx-border-color: #cbd5e0; " +
                "-fx-border-radius: 6; -fx-font-size: 13px; -fx-padding: 4;");
    }

    private void surligner(Control control) {
        control.setStyle(control.getStyle() +
                "; -fx-border-color: #e74c3c; -fx-border-width: 2;");
    }

    // ============================================================
    //  ANNULER
    // ============================================================
    @FXML
    private void annuler() {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Annulation");
        conf.setHeaderText("Annuler la modification ?");
        conf.setContentText("Les modifications ne seront pas enregistrees.");
        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            fermerFenetre();
        }
    }

    // ============================================================
    //  UTILITAIRES
    // ============================================================
    private void fermerFenetre() {
        Stage stage = (Stage) txtTypePanne.getScene().getWindow();
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