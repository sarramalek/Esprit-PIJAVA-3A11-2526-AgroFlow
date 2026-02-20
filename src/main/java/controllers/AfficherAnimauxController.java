package controllers;

import entities.animaux;
import entities.Sexe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // MANQUANT
import javafx.collections.transformation.SortedList;   // MANQUANT
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // INDISPENSABLE pour initialize
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ServiceAnimal;

import java.io.IOException;
import java.net.URL; // MANQUANT
import java.sql.SQLException;
import java.util.Date;
import java.util.List; // MANQUANT
import java.util.Optional;
import java.util.ResourceBundle; // MANQUANT

public class AfficherAnimauxController implements Initializable { // Ajout de implements Initializable

    @FXML private TableView<animaux> tableAnimaux;
    @FXML private TableColumn<animaux, String> colNom;
    @FXML private TableColumn<animaux, String> colEspece;
    @FXML private TableColumn<animaux, Float> colPoids;
    @FXML private TableColumn<animaux, Date> colDate;
    @FXML private TableColumn<animaux, Sexe> colSexe;
    @FXML private TextField filterField;

    private ServiceAnimal service = new ServiceAnimal();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation des colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));

        refreshTable();
    }

    private void refreshTable() {
        try {
            List<animaux> list = service.afficher();
            ObservableList<animaux> observableList = FXCollections.observableArrayList(list);

            // On configure la recherche avec la liste fraîchement chargée
            setupSearch(observableList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupSearch(ObservableList<animaux> animalList) {
        FilteredList<animaux> filteredData = new FilteredList<>(animalList, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(animal -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (animal.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (animal.getEspece().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<animaux> sortedData = new SortedList<>(filteredData);
        // CORRECTION : Utilisation de tableAnimaux (ton ID FXML) au lieu de tvAnimaux
        sortedData.comparatorProperty().bind(tableAnimaux.comparatorProperty());
        tableAnimaux.setItems(sortedData);
    }

    // --- Méthodes de Gestion ---

    @FXML
    void handleSupprimer(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer " + selectionne.getNom() + " ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(selectionne.getId());
                    refreshTable();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        } else { alerteSelection(); }
    }

    @FXML
    void versModifier(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnimal.fxml"));
                Parent root = loader.load();
                ModifierAnimalController controller = loader.getController();
                controller.chargerDonnees(selectionne);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) { e.printStackTrace(); }
        } else { alerteSelection(); }
    }

    @FXML
    void versAjout(ActionEvent event) {
        changerScene(event, "ajoutAnimaux.fxml");
    }
    @FXML
    void ouvrirStats(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/StatsAnimaux.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Statistiques - AgroFlow");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Navigation ---

    private void changerScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
        }
    }

    @FXML void naviguerVersExamens(ActionEvent event) { changerScene(event, "AfficherExamens.fxml"); }
    @FXML void naviguerAnimaux(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerMateriels(ActionEvent event) { changerScene(event, "AfficherMateriels.fxml"); }
    @FXML void naviguerStocks(ActionEvent event) { changerScene(event, "AfficherStocks.fxml"); }
    @FXML void naviguerTerrains(ActionEvent event) { changerScene(event, "AfficherTerrains.fxml"); }
    @FXML void naviguerEvenements(ActionEvent event) { changerScene(event, "AfficherEvenements.fxml"); }
    @FXML void naviguerUsers(ActionEvent event) { changerScene(event, "AfficherUsers.fxml"); }

    private void alerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un animal.");
        alert.show();
    }
}