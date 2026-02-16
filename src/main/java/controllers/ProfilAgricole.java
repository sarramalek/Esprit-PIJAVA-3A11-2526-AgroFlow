package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Personne;
import services.PersonneService;

import java.io.IOException;
import java.sql.SQLException;

public class ProfilAgricole {

    @FXML private Label profileNameLabel;
    @FXML private Label cinLabel;
    @FXML private Label emailLabel;
    @FXML private Label telLabel;
    @FXML private Label adresseLabel;
    @FXML private Label villeLabel;
    @FXML private Label dateNaissLabel;
    @FXML private Button modifierBtn;
    @FXML private Button desactiverBtn;

    private Personne currentUser;
    private PersonneService personneService;
    private AcceuilAgricole parentController;

    @FXML
    public void initialize() {
        personneService = new PersonneService();
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) remplirInfos(user);
    }

    public void setParentController(AcceuilAgricole parent) {
        this.parentController = parent;
    }

    private void remplirInfos(Personne user) {
        profileNameLabel.setText(user.getPrenom() + " " + user.getNom());
        cinLabel.setText(String.valueOf(user.getCin()));
        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");
        telLabel.setText(user.getTel() != null ? user.getTel() : "—");
        adresseLabel.setText(user.getAdresse() != null ? user.getAdresse() : "—");
        villeLabel.setText(user.getVille() != null ? user.getVille() : "—");
        dateNaissLabel.setText(user.getDate_naiss() != null ? user.getDate_naiss() : "—");
    }

    @FXML
    private void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPersonne.fxml"));
            Parent root = loader.load();

            ModifierPersonne controller = loader.getController();
            controller.setPersonne(currentUser);
            controller.setProfilAgricoleController(this);

            Stage stage = new Stage();
            stage.setTitle("✏️ Modifier mon profil");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible d'ouvrir le formulaire de modification");
        }
    }

    @FXML
    private void handleDesactiver() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("⚠️ Désactiver votre compte");
        confirm.setContentText(
                "Cette action est irréversible.\n\n" +
                        "Votre compte et toutes vos données seront supprimés définitivement.\n\n" +
                        "Êtes-vous sûr de vouloir continuer ?"
        );

        // Boutons personnalisés
        ButtonType btnSupprimer = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler   = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSupprimer, btnAnnuler);

        confirm.showAndWait().ifPresent(response -> {
            if (response == btnSupprimer) {
                try {
                    personneService.supprimer(currentUser.getCin());
                    System.out.println("✓ Compte supprimé: " + currentUser.getCin());

                    // Fermer cette fenêtre
                    Stage stage = (Stage) desactiverBtn.getScene().getWindow();
                    stage.close();

                    // Retourner à la page de login
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                    Parent root = loader.load();
                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(root, 900, 600));
                    loginStage.setTitle("AgroFlow - Connexion");
                    loginStage.show();

                    // Fermer le dashboard agricole
                    if (parentController != null) {
                        Stage dashStage = parentController.getStage();
                        if (dashStage != null) dashStage.close();
                    }

                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                    showError("Erreur lors de la suppression du compte");
                }
            }
        });
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) modifierBtn.getScene().getWindow();
        stage.close();
    }

    // Appelé par ModifierProfil après sauvegarde
    public void refreshUser(Personne updatedUser) {
        this.currentUser = updatedUser;
        remplirInfos(updatedUser);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}