package controllers.Materiels;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Materiels.Machine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.Materiels.MachineService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AjoutMachineController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML
    private TextField tfMarque;

    @FXML
    private TextField tfModele;

    @FXML
    private TextField tfEtatM;

    @FXML
    private TextField tfNumeroSerie;

    @FXML
    private DatePicker dpDateAchat;

    @FXML
    private TextField tfNom;

    private MachineService machineService;

    // États valides prédéfinis
    private static final List<String> ETATS_VALIDES = Arrays.asList(
            "Disponible", "En panne", "En maintenance"
    );
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

    public AjoutMachineController() {
        machineService = new MachineService();
    }

    @FXML
    private void handleAjouter() {
        // Récupérer les valeurs
        String marque = tfMarque.getText().trim();
        String modele = tfModele.getText().trim();
        String etatM = tfEtatM.getText().trim();
        String numeroSerie = tfNumeroSerie.getText().trim();
        LocalDate dateAchat = dpDateAchat.getValue();
        String nom = tfNom.getText().trim();

        // Validation complète
        if (!validerTousLesChamps(marque, modele, etatM, numeroSerie, dateAchat, nom)) {
            return;
        }

        try {
            // Vérifier l'unicité du numéro de série
            if (numeroSerieExiste(numeroSerie)) {
                afficherAlerte("Erreur de validation",
                        "Le numéro de série '" + numeroSerie + "' existe déjà dans la base de données.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Création de la machine
            Machine machine = new Machine();
            machine.setMarque(marque);
            machine.setModele(modele);
            machine.setEtatM(etatM);
            machine.setNumeroSerie(numeroSerie);
            machine.setDateAchat(dateAchat);
            machine.setNom(nom);

            // Ajout dans la base
            machineService.ajouter(machine);

            // Confirmation
            afficherAlerte("Succès", "Machine ajoutée avec succès !", Alert.AlertType.INFORMATION);

            // Retourner à la liste
            retourListeMachines();

        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Erreur lors de l'ajout de la machine : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Validation complète de tous les champs
     */
    private boolean validerTousLesChamps(String marque, String modele, String etatM,
                                         String numeroSerie, LocalDate dateAchat, String nom) {

        // 1. Validation de la marque
        if (!validerMarque(marque)) {
            return false;
        }

        // 2. Validation du modèle
        if (!validerModele(modele)) {
            return false;
        }

        // 3. Validation de l'état
        if (!validerEtat(etatM)) {
            return false;
        }

        // 4. Validation du numéro de série
        if (!validerNumeroSerie(numeroSerie)) {
            return false;
        }

        // 5. Validation de la date d'achat
        if (!validerDateAchat(dateAchat)) {
            return false;
        }

        // 6. Validation du nom
        if (!validerNom(nom)) {
            return false;
        }

        return true;
    }

    /**
     * Validation de la marque
     * - Champ obligatoire (non vide)
     * - Minimum 2 caractères
     * - Uniquement des lettres et espaces
     */
    private boolean validerMarque(String marque) {
        if (marque.isEmpty()) {
            afficherAlerte("Erreur de validation",
                    "La marque est obligatoire.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (marque.length() < 2) {
            afficherAlerte("Erreur de validation",
                    "La marque doit contenir au minimum 2 caractères.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (!marque.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
            afficherAlerte("Erreur de validation",
                    "La marque ne doit contenir que des lettres et des espaces (pas de chiffres ni caractères spéciaux).",
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Validation du modèle
     * - Champ obligatoire
     * - Minimum 2 caractères
     * - Peut contenir lettres et chiffres
     */
    private boolean validerModele(String modele) {
        if (modele.isEmpty()) {
            afficherAlerte("Erreur de validation",
                    "Le modèle est obligatoire.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (modele.length() < 2) {
            afficherAlerte("Erreur de validation",
                    "Le modèle doit contenir au minimum 2 caractères.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (!modele.matches("^[a-zA-Z0-9À-ÿ\\s-]+$")) {
            afficherAlerte("Erreur de validation",
                    "Le modèle peut contenir des lettres, chiffres, espaces et tirets uniquement.",
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Validation de l'état
     * - Champ obligatoire
     * - Doit appartenir à la liste prédéfinie
     */
    private boolean validerEtat(String etatM) {
        if (etatM.isEmpty()) {
            afficherAlerte("Erreur de validation",
                    "L'état est obligatoire.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (!ETATS_VALIDES.contains(etatM)) {
            afficherAlerte("Erreur de validation",
                    "L'état doit être l'une des valeurs suivantes : " + String.join(", ", ETATS_VALIDES),
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Validation du numéro de série
     * - Champ obligatoire
     * - Minimum 5 caractères
     * - Lettres et chiffres uniquement (pas d'espaces)
     */
    private boolean validerNumeroSerie(String numeroSerie) {
        if (numeroSerie.isEmpty()) {
            afficherAlerte("Erreur de validation",
                    "Le numéro de série est obligatoire.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (numeroSerie.length() < 5) {
            afficherAlerte("Erreur de validation",
                    "Le numéro de série doit contenir au minimum 5 caractères.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (!numeroSerie.matches("^[a-zA-Z0-9]+$")) {
            afficherAlerte("Erreur de validation",
                    "Le numéro de série ne doit contenir que des lettres et des chiffres (pas d'espaces ni caractères spéciaux).",
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Validation de la date d'achat
     * - Champ obligatoire
     * - Ne doit pas être dans le futur
     * - Doit être après 1900
     */
    private boolean validerDateAchat(LocalDate dateAchat) {
        if (dateAchat == null) {
            afficherAlerte("Erreur de validation",
                    "La date d'achat est obligatoire.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (dateAchat.isAfter(LocalDate.now())) {
            afficherAlerte("Erreur de validation",
                    "La date d'achat ne peut pas être dans le futur.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (dateAchat.isBefore(LocalDate.of(1900, 1, 1))) {
            afficherAlerte("Erreur de validation",
                    "La date d'achat doit être après le 1er janvier 1900.",
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Validation du nom
     * - Champ obligatoire
     * - Minimum 3 caractères
     * - Ne doit pas contenir uniquement des chiffres
     */
    private boolean validerNom(String nom) {
        if (nom.isEmpty()) {
            afficherAlerte("Erreur de validation",
                    "Le nom est obligatoire.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (nom.length() < 3) {
            afficherAlerte("Erreur de validation",
                    "Le nom doit contenir au minimum 3 caractères.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (nom.matches("^[0-9]+$")) {
            afficherAlerte("Erreur de validation",
                    "Le nom ne peut pas contenir uniquement des chiffres.",
                    Alert.AlertType.WARNING);
            return false;
        }

        if (!nom.matches("^[a-zA-Z0-9À-ÿ\\s-]+$")) {
            afficherAlerte("Erreur de validation",
                    "Le nom ne doit pas contenir de caractères spéciaux excessifs.",
                    Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Vérifier si le numéro de série existe déjà
     */
    private boolean numeroSerieExiste(String numeroSerie) {
        try {
            List<Machine> machines = machineService.recuperer();
            for (Machine m : machines) {
                if (m.getNumeroSerie().equalsIgnoreCase(numeroSerie)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void handleAnnuler() {
        retourListeMachines();
    }

    private void retourListeMachines() {
        naviguerVers("/AffichageMachine.fxml");
    }

    // ================= NAVIGATION =================

    @FXML
    private void naviguerAnimaux() {
        naviguerVers("/AfficherAnimaux.fxml");
    }

    @FXML
    private void naviguerMateriels() {
        naviguerVers("/AccueilMateriel.fxml");
    }

    @FXML
    private void naviguerStocks() {
        naviguerVers("/AfficherStocks.fxml");
    }

    @FXML
    private void naviguerTerrains() {
        naviguerVers("/AfficherTerrains.fxml");
    }

    @FXML
    private void naviguerEvenements() {
        naviguerVers("/AccueilEvenement.fxml");
    }

    @FXML
    private void naviguerUsers() {
        naviguerVers("/AfficherUsers.fxml");
    }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tfMarque.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de naviguer vers la page demandée", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml");}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.naviguerVers("/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.naviguerVers("/UsersInterface/GestionOffre.fxml");}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.naviguerVers("/UsersInterface/Acceuil.fxml");

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.naviguerVers("/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.naviguerVers("/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.naviguerVers("/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.naviguerVers("/MaterielsInterface/AccueilMateriel.fxml");
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

}