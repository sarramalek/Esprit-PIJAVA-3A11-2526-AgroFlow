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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ModifierMaintenanceController implements Initializable {

    @FXML private ComboBox<String> comboMachine;
    @FXML private TextField txtTypePanne;
    @FXML private DatePicker datePickerMaintenance;
    @FXML private TextField txtCout;
    @FXML private TextArea txtDescription;

    private MachineService machineService = new MachineService();
    private MaintenanceService maintenanceService = new MaintenanceService();

    // Map pour la jointure : Nom Machine -> Machine Object
    private Map<String, Machine> mapMachines = new HashMap<>();

    private Maintenance maintenanceAModifier;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerMachines();
        comboMachine.setPromptText("🔧 Sélectionner une machine");
    }

    /**
     * Méthode appelée par AfficherMaintenancesController pour initialiser les données
     */
    public void initialiserDonnees(Maintenance maintenance) {
        this.maintenanceAModifier = maintenance;
        remplirChamps();
    }

    private void chargerMachines() {
        try {
            List<Machine> machines = machineService.recuperer();

            mapMachines.clear();
            ObservableList<String> nomsMachines = FXCollections.observableArrayList();

            for (Machine machine : machines) {
                String nomMachine = machine.getNom();
                nomsMachines.add(nomMachine);
                mapMachines.put(nomMachine, machine);
            }

            comboMachine.setItems(nomsMachines);
            System.out.println("✅ " + machines.size() + " machines chargées pour la modification");

        } catch (SQLException e) {
            afficherAlerte("Erreur", "❌ Erreur lors du chargement des machines: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Remplir les champs avec les données de la maintenance à modifier
     */
    private void remplirChamps() {
        if (maintenanceAModifier != null) {
            // Trouver le nom de la machine correspondant à idM (JOINTURE)
            String nomMachine = null;
            for (Map.Entry<String, Machine> entry : mapMachines.entrySet()) {
                if (entry.getValue().getIdM() == maintenanceAModifier.getIdM()) {
                    nomMachine = entry.getKey();
                    break;
                }
            }

            if (nomMachine != null) {
                comboMachine.setValue(nomMachine);
            } else {
                System.err.println("⚠️ Machine introuvable pour idM: " + maintenanceAModifier.getIdM());
            }

            txtTypePanne.setText(maintenanceAModifier.getTypePanne());
            datePickerMaintenance.setValue(maintenanceAModifier.getDateMain());
            txtCout.setText(String.valueOf(maintenanceAModifier.getCout()));
            txtDescription.setText(maintenanceAModifier.getDescription());
        }
    }

    @FXML
    private void modifier() {
        System.out.println("🔄 Tentative de modification de maintenance...");

        // ✅ VALIDATION COMPLÈTE
        if (!validerTousLesChamps()) {
            return;
        }

        try {
            // Récupérer la machine sélectionnée (JOINTURE)
            String nomMachineSelectionnee = comboMachine.getValue();
            Machine machine = mapMachines.get(nomMachineSelectionnee);

            if (machine == null) {
                afficherAlerte("Erreur", "❌ Machine non trouvée dans la base de données", Alert.AlertType.ERROR);
                return;
            }

            // Mettre à jour l'objet Maintenance
            maintenanceAModifier.setIdM(machine.getIdM());  // 🔑 JOINTURE via idM
            maintenanceAModifier.setTypePanne(txtTypePanne.getText().trim());
            maintenanceAModifier.setDateMain(datePickerMaintenance.getValue());
            maintenanceAModifier.setCout(Double.parseDouble(txtCout.getText().trim()));
            maintenanceAModifier.setDescription(txtDescription.getText().trim());

            System.out.println("📝 Maintenance à modifier:");
            System.out.println("   🔧 Machine: " + machine.getNom() + " (ID: " + machine.getIdM() + ")");
            System.out.println("   ⚠️ Type Panne: " + maintenanceAModifier.getTypePanne());
            System.out.println("   📅 Date: " + maintenanceAModifier.getDateMain());
            System.out.println("   💰 Coût: " + maintenanceAModifier.getCout() + " DT");

            // Modifier dans la base de données
            maintenanceService.modifier(maintenanceAModifier);

            afficherAlerte("Succès", "✅ Maintenance modifiée avec succès !", Alert.AlertType.INFORMATION);
            fermerFenetre();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "❌ Le coût doit être un nombre valide (ex: 450.0)", Alert.AlertType.ERROR);
            txtCout.requestFocus();
        } catch (SQLException e) {
            afficherAlerte("Erreur", "❌ Erreur lors de la modification dans la base de données:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            afficherAlerte("Erreur", "❌ Erreur inattendue:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * ✅ VALIDATION COMPLÈTE DE TOUS LES CHAMPS
     */
    private boolean validerTousLesChamps() {
        if (!validerMachine()) return false;
        if (!validerTypePanne()) return false;
        if (!validerDate()) return false;
        if (!validerCout()) return false;
        if (!validerDescription()) return false;
        return true;
    }

    private boolean validerMachine() {
        if (comboMachine.getValue() == null || comboMachine.getValue().isEmpty()) {
            afficherAlerte("⚠️ Validation",
                    "Veuillez sélectionner une machine dans la liste",
                    Alert.AlertType.WARNING);
            comboMachine.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validerTypePanne() {
        String typePanne = txtTypePanne.getText().trim();

        if (typePanne.isEmpty()) {
            afficherAlerte("⚠️ Validation",
                    "Le type de panne est obligatoire",
                    Alert.AlertType.WARNING);
            txtTypePanne.requestFocus();
            return false;
        }

        if (typePanne.length() < 3) {
            afficherAlerte("⚠️ Validation",
                    "Le type de panne doit contenir au moins 3 caractères",
                    Alert.AlertType.WARNING);
            txtTypePanne.requestFocus();
            return false;
        }

        if (!typePanne.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
            afficherAlerte("⚠️ Validation",
                    "Le type de panne ne doit contenir que des lettres, espaces, apostrophes et tirets\n" +
                            "❌ Exemple invalide: 'Panne123' ou 'Panne@#$'\n" +
                            "✅ Exemple valide: 'Panne moteur' ou 'Court-circuit'",
                    Alert.AlertType.WARNING);
            txtTypePanne.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validerDate() {
        LocalDate date = datePickerMaintenance.getValue();

        if (date == null) {
            afficherAlerte("⚠️ Validation",
                    "La date de maintenance est obligatoire",
                    Alert.AlertType.WARNING);
            datePickerMaintenance.requestFocus();
            return false;
        }

        if (date.isAfter(LocalDate.now())) {
            afficherAlerte("⚠️ Validation",
                    "La date de maintenance ne peut pas être dans le futur\n" +
                            "📅 Date sélectionnée: " + date + "\n" +
                            "📅 Date actuelle: " + LocalDate.now(),
                    Alert.AlertType.WARNING);
            datePickerMaintenance.requestFocus();
            return false;
        }

        if (date.isBefore(LocalDate.now().minusYears(10))) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("⚠️ Date ancienne");
            confirmation.setHeaderText("La date sélectionnée est ancienne");
            confirmation.setContentText("La date est antérieure à " + LocalDate.now().minusYears(10) + "\n" +
                    "Voulez-vous continuer ?");

            return confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        }

        return true;
    }

    private boolean validerCout() {
        String coutTexte = txtCout.getText().trim();

        if (coutTexte.isEmpty()) {
            afficherAlerte("⚠️ Validation",
                    "Le coût est obligatoire",
                    Alert.AlertType.WARNING);
            txtCout.requestFocus();
            return false;
        }

        try {
            double cout = Double.parseDouble(coutTexte);

            if (cout < 0) {
                afficherAlerte("⚠️ Validation",
                        "Le coût ne peut pas être négatif\n" +
                                "💰 Coût saisi: " + cout + " DT",
                        Alert.AlertType.WARNING);
                txtCout.requestFocus();
                return false;
            }

            if (cout == 0) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("⚠️ Coût nul");
                confirmation.setHeaderText("Le coût est de 0 DT");
                confirmation.setContentText("Êtes-vous sûr que cette maintenance n'a coûté aucun frais ?");

                if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    txtCout.requestFocus();
                    return false;
                }
            }

            if (cout > 100000) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("⚠️ Coût élevé");
                confirmation.setHeaderText("Le coût est très élevé");
                confirmation.setContentText("💰 Coût: " + String.format("%.2f", cout) + " DT\n" +
                        "Voulez-vous continuer ?");

                return confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
            }

        } catch (NumberFormatException e) {
            afficherAlerte("⚠️ Validation",
                    "Le coût doit être un nombre valide\n\n" +
                            "✅ Exemples valides:\n" +
                            "   - 450\n" +
                            "   - 450.50\n" +
                            "   - 1250.75\n\n" +
                            "❌ Format invalide: '" + coutTexte + "'",
                    Alert.AlertType.WARNING);
            txtCout.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validerDescription() {
        String description = txtDescription.getText().trim();

        if (description.isEmpty()) {
            afficherAlerte("⚠️ Validation",
                    "La description est obligatoire",
                    Alert.AlertType.WARNING);
            txtDescription.requestFocus();
            return false;
        }

        if (description.length() < 10) {
            afficherAlerte("⚠️ Validation",
                    "La description doit contenir au moins 10 caractères\n" +
                            "📝 Caractères actuels: " + description.length() + "\n" +
                            "📝 Caractères minimum requis: 10\n\n" +
                            "Veuillez fournir plus de détails sur la panne et les travaux effectués.",
                    Alert.AlertType.WARNING);
            txtDescription.requestFocus();
            return false;
        }

        String[] mots = description.split("\\s+");
        if (mots.length < 3) {
            afficherAlerte("⚠️ Validation",
                    "La description doit contenir au moins 3 mots\n" +
                            "💡 Décrivez la panne, les travaux effectués, et les pièces remplacées.",
                    Alert.AlertType.WARNING);
            txtDescription.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void annuler() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("⚠️ Annulation");
        confirmation.setHeaderText("Annuler la modification");
        confirmation.setContentText("Les modifications ne seront pas enregistrées.\nVoulez-vous vraiment annuler ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            fermerFenetre();
        }
    }

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