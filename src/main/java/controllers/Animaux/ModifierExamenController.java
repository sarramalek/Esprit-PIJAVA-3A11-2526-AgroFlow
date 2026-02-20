package controllers.Animaux;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Animaux.examens;
import models.Animaux.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Animaux.ServiceExamen;
import services.Animaux.ServiceAnimal;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ModifierExamenController {

    @FXML private ComboBox<animaux> cbAnimal;
    @FXML private TextField tfType;
    @FXML private DatePicker dpDate;
    @FXML private TextArea taDiagnostic;
    @FXML private TextArea taTraitement;
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    private ServiceExamen serviceEx = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();
    private examens examenSelectionne;
    public void initialize() {
        // Cacher submenu par défaut
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);

        // 1. Hover sur le bouton Gestion → Ouvre submenu
        gestionBtn.setOnMouseEntered(e -> {
            showGestionSubmenu();
        });

        // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
        gestionContainer.setOnMouseEntered(e -> {
            showGestionSubmenu();
        });}
    public void chargerDonnees(examens e) {
        this.examenSelectionne = e;

        try {
            cbAnimal.getItems().setAll(serviceAn.afficher());
            for (animaux a : cbAnimal.getItems()) {
                if (a.getId() == e.getId_animal()) {
                    cbAnimal.setValue(a);
                    break;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tfType.setText(e.getType_examen());
        taDiagnostic.setText(e.getDiagnostic());
        taTraitement.setText(e.getTraitement());

        // --- CORRECTION ICI : De l'entité (Date) vers le DatePicker (LocalDate) ---
        if (e.getDate_examen() != null) {
            // On s'assure de traiter la valeur comme une java.util.Date
            java.util.Date utilDate = e.getDate_examen();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            dpDate.setValue(sqlDate.toLocalDate());
        }
    }

    @FXML
    void handleModifier(MouseEvent event) {
        if (estValide()) {
            try {
                examenSelectionne.setId_animal(cbAnimal.getValue().getId());
                examenSelectionne.setType_examen(tfType.getText());
                examenSelectionne.setDiagnostic(taDiagnostic.getText());
                examenSelectionne.setTraitement(taTraitement.getText());

                // --- DEUXIÈME CONVERSION : Du DatePicker vers l'Entité ---
                if (dpDate.getValue() != null) {
                    examenSelectionne.setDate_examen(java.sql.Date.valueOf(dpDate.getValue()));
                }

                serviceEx.modifier(examenSelectionne);
                retourListe(event);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean estValide() {
        String msg = "";
        if (cbAnimal.getValue() == null) msg += "- Animal requis.\n";
        if (tfType.getText().trim().isEmpty()) msg += "- Type d'examen requis.\n";
        if (dpDate.getValue() == null) msg += "- Date requise.\n";
        if (taDiagnostic.getText().trim().isEmpty()) msg += "- Diagnostic requis.\n";

        if (!msg.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de modification");
            alert.setHeaderText("Veuillez corriger :");
            alert.setContentText(msg);
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    void retourListe(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/AfficherExamens.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //naviguer vers les autres modules :
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.changerScene(event,"/UsersInterface/GestionTache.fxml");}



    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.changerScene(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.changerScene(event,"/UsersInterface/GestionOffre.fxml");}



    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.changerScene(actionEvent,"/UsersInterface/Acceuil.fxml");
    }

    public void handleAnimals( MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
    }
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }
    // Méthode utilitaire pour simplifier la navigation
    private void changerScene(MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlFile);
            e.printStackTrace();
        }
    }
    /**
     * Afficher une erreur
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}