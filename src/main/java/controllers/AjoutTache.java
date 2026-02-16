package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Employe;
import models.Tache;
import services.PersonneService;
import services.TacheService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjoutTache {

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
    private List<Employe> employes;

    @FXML
    public void initialize() {
        tacheService = new TacheService();
        personneService = new PersonneService();

        // Statuts
        statusComboBox.setItems(FXCollections.observableArrayList(
                "en_attente", "en_cours", "terminee", "annulee"
        ));
        statusComboBox.setValue("en_attente");

        // Priorités
        priorityComboBox.setItems(FXCollections.observableArrayList(
                "basse", "moyenne", "haute", "urgente"
        ));
        priorityComboBox.setValue("moyenne");

        // Date par défaut
        dueDatePicker.setValue(LocalDate.now().plusDays(7));

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

    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {
            Tache tache = new Tache();
            tache.setNom_tache(titleField.getText().trim());
            tache.setDescription(descriptionArea.getText().trim());
            tache.setEtat(statusComboBox.getValue());
            tache.setPriorite(priorityComboBox.getValue());
            tache.setDate_echeancee(dueDatePicker.getValue().toString());

            // Récupérer le CIN de l'employé sélectionné
            int selectedIndex = employeeComboBox.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && employes != null && !employes.isEmpty()) {
                tache.setAssignee(employes.get(selectedIndex).getCin());
            } else {
                tache.setAssignee(0);
            }

            tacheService.ajouter(tache);

            // Rafraîchir la liste parente
            if (parentController != null) {
                parentController.loadTaches();
            }

            showSuccess("Tâche ajoutée avec succès !");
            closeWindow();

        } catch (SQLException e) {
            showError("Erreur lors de l'ajout : " + e.getMessage());
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
        if (dueDatePicker.getValue().isBefore(LocalDate.now())) {
            showError("La date d'échéance ne peut pas être dans le passé.");
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