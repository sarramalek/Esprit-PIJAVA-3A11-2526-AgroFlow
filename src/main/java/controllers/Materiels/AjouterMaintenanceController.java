package controllers.Materiels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Materiels.Machine;
import models.Materiels.Maintenance;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AjouterMaintenanceController implements Initializable {
    @FXML private Button logoutBtn;
    @FXML private VBox gestionSubmenu;
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


    //navigguer vers les autres modules

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.naviguerVers("/UsersInterface/GestionAbonnements.fxml",event);}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.naviguerVers("/UsersInterface/GestionOffre.fxml",event);}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.naviguerVers("/UsersInterface/Acceuil.fxml", actionEvent);

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",mouseEvent);

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.naviguerVers("/StocksInterface/afficherarticle.fxml",mouseEvent);
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.naviguerVers("/TerrainsInterface/acceuilterrain.fxml",mouseEvent);
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.naviguerVers("/G-Evenements/Accueil.fxml",mouseEvent);
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",mouseEvent);
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
    // ================= ALERT METHODS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void naviguerVers(String fxmlPath , Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }
}