package controllers;

import entities.Sexe;
import entities.animaux;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceAnimal;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List; // Import manquant pour les suggestions

public class ModifierAnimalController {

    @FXML private TextField tfNom;
    @FXML private ComboBox<String> comboEspece;
    @FXML private TextField tfPoids;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Sexe> cbSexe;

    private ServiceAnimal service = new ServiceAnimal();

    private int idAnimalActuel;

    @FXML
    public void initialize() {
        cbSexe.getItems().setAll(Sexe.values());
        comboEspece.setItems(FXCollections.observableArrayList(
                "Chien", "Chat", "Vache", "Chèvre", "Mouton", "Cheval"
        ));


    }



    public void chargerDonnees(animaux a) {
        this.idAnimalActuel = a.getId();
        tfNom.setText(a.getNom());
        comboEspece.setValue(a.getEspece());
        tfPoids.setText(String.valueOf(a.getPoids()));

        if (a.getDate_naissance() != null) {
            dpDate.setValue(new java.sql.Date(a.getDate_naissance().getTime()).toLocalDate());
        }
        cbSexe.setValue(a.getSexe());


    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (estValide()) {
            try {
                animaux a = new animaux();
                a.setId(idAnimalActuel);
                a.setNom(tfNom.getText());
                a.setEspece(comboEspece.getValue());
                a.setPoids(Float.parseFloat(tfPoids.getText()));
                a.setSexe(cbSexe.getValue());
                a.setDate_naissance(java.sql.Date.valueOf(dpDate.getValue()));

                service.modifier(a);
                retourListe(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        changerScene(event, "AfficherAnimaux.fxml");
    }

    private void changerScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean estValide() {
        // ... (ton code de validation existant est correct)
        return true;
    }

    // Navigation Sidebar
    @FXML void naviguerAnimaux(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerMateriels(ActionEvent event) { changerScene(event, "AfficherMateriels.fxml"); }
    @FXML void naviguerStocks(ActionEvent event) { changerScene(event, "AfficherStocks.fxml"); }
    @FXML void naviguerTerrains(ActionEvent event) { changerScene(event, "AfficherTerrains.fxml"); }
    @FXML void naviguerEvenements(ActionEvent event) { changerScene(event, "AfficherEvenements.fxml"); }
    @FXML void naviguerUsers(ActionEvent event) { changerScene(event, "AfficherUsers.fxml"); }
}