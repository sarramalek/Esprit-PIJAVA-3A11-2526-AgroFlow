package controllers.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Employe;
import models.User.Tache;
import services.User.PersonneService;
import services.User.TacheService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifierTache {

    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private TacheService tacheService;
    private PersonneService personneService;
    private GestionTache parentController;
    private Tache tacheAModifier;
    private List<Employe> employes;

    @FXML
    public void initialize() {
        tacheService = new TacheService();
        personneService = new PersonneService();

        // Statuts
        statusComboBox.setItems(FXCollections.observableArrayList(
                "en_attente", "en_cours", "terminee", "annulee"
        ));

        // Priorités
        priorityComboBox.setItems(FXCollections.observableArrayList(
                "basse", "moyenne", "haute", "urgente"
        ));

        // Charger les employés
        loadEmployes();
    }

    private void loadEmployes() {
        try {
            employes = personneService.getEmployes();
            List<String> employeNames = employes.stream()
                    .map(e -> e.getPrenom() + " " + e.getNom() + " (" + e.getCin() + ")")
                    .collect(java.util.stream.Collectors.toList());
            employeeComboBox.setItems(FXCollections.observableArrayList(employeNames));
        } catch (SQLException e) {
            showError("Impossible de charger les employés : " + e.getMessage());
        }
    }

    public void setParentController(GestionTache parentController) {
        this.parentController = parentController;
    }

    public void setTache(Tache tache) {
        this.tacheAModifier = tache;
        remplirFormulaire(tache);
    }

    private void remplirFormulaire(Tache tache) {
        titleLabel.setText("Modifier la tâche #" + tache.getId());
        titleField.setText(tache.getNomTache());
        descriptionArea.setText(tache.getDescription());

        // Statut
        if (tache.getEtat() != null) {
            statusComboBox.setValue(tache.getEtat());
        }

        // Priorité
        if (tache.getPriorite() != null) {
            priorityComboBox.setValue(tache.getPriorite());
        }

        // Date d'échéance
        if (tache.getDateEcheance() != null) {
            try {
                dueDatePicker.setValue(tache.getDateEcheance());            } catch (Exception e) {
                dueDatePicker.setValue(LocalDate.now().plusDays(7));
            }
        }

        // Employé assigné
        if (employes != null && tache.getAssignee() > 0) {
            for (int i = 0; i < employes.size(); i++) {
                if (employes.get(i).getCin() == tache.getAssignee()) {
                    employeeComboBox.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {
            tacheAModifier.setNomTache(titleField.getText().trim());
            tacheAModifier.setDescription(descriptionArea.getText().trim());
            tacheAModifier.setEtat(statusComboBox.getValue());
            tacheAModifier.setPriorite(priorityComboBox.getValue());
            tacheAModifier.setDateEcheance(dueDatePicker.getValue());
            // Récupérer le CIN de l'employé sélectionné
            int selectedIndex = employeeComboBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && employes != null && !employes.isEmpty()) {
                tacheAModifier.setAssignee(employes.get(selectedIndex).getCin());
            }

            tacheService.modifier(tacheAModifier);

            // Rafraîchir la liste parente
            if (parentController != null) {
                parentController.loadTaches();
            }

            showSuccess("Tâche modifiée avec succès !");
            closeWindow();

        } catch (SQLException e) {
            showError("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateFields() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showError("Le titre est obligatoire.");
            return false;
        }
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showError("La description est obligatoire.");
            return false;
        }
        if (statusComboBox.getValue() == null) {
            showError("Le statut est obligatoire.");
            return false;
        }
        if (dueDatePicker.getValue() == null) {
            showError("La date d'échéance est obligatoire.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}