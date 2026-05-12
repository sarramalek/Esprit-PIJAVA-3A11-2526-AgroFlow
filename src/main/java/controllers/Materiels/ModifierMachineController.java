package controllers.Materiels;

import javafx.event.ActionEvent;
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
import models.User.Personne;
import services.Materiels.MachineService;
import services.User.PersonneService;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ModifierMachineController {

    @FXML private Button logoutBtn, gestionBtn;
    @FXML private VBox gestionSubmenu, gestionContainer;

    // Champs du formulaire
    @FXML private TextField tfMarque;
    @FXML private TextField tfModele;
    @FXML private ComboBox<String> cbEtat;
    @FXML private TextField tfNumeroSerie;
    @FXML private TextField tfNom;
    @FXML private DatePicker dpDateAchat;
    @FXML private TextField tfKilometrage;
    @FXML private DatePicker dpDateLastVisite;
    @FXML private TextField tfKmLastVisite;
    @FXML private DatePicker dpProchaineMaintenance;
    @FXML private ComboBox<String> cbCin;

    private MachineService machineService;
    private PersonneService personneService;
    private Machine machineAModifier;
    private Personne currentUser;

    // Liste des états valides
    private static final List<String> ETATS_VALIDES = Arrays.asList(
            "Neuf", "Disponible", "Occasion", "Bon", "En panne"
    );

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ModifierMachineController() {
        machineService = new MachineService();
        personneService = new PersonneService();
    }

    public void initialize() {
        // Récupérer l'utilisateur connecté
        this.currentUser = SessionManager.getCurrentUser();

        // Configurer la ComboBox des états
        cbEtat.getItems().addAll(ETATS_VALIDES);

        // Configurer la ComboBox des CIN
        chargerCinDisponibles();

        // Ajouter des tooltips
        ajouterTooltips();

        // Cacher submenu par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        if (gestionBtn != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        }
        if (gestionContainer != null) {
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
    }

    private void chargerCinDisponibles() {
        try {
            List<Personne> personnes = personneService.recuperer();
            if (personnes != null && !personnes.isEmpty()) {
                cbCin.getItems().clear();
                for (Personne p : personnes) {
                    String display = String.format("%d - %s %s", p.getCin(), p.getPrenom(), p.getNom());
                    cbCin.getItems().add(display);
                }
                cbCin.setPromptText("Sélectionnez un responsable");
            } else {
                cbCin.getItems().add("Aucun responsable disponible");
                cbCin.setDisable(true);
                cbCin.setPromptText("Ajoutez des personnes d'abord");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des CIN: " + e.getMessage());
            cbCin.getItems().add("Erreur de chargement");
            cbCin.setDisable(true);
        }
    }

    private void ajouterTooltips() {
        tfMarque.setTooltip(new Tooltip("Ex: John Deere, New Holland (2-50 caractères)"));
        tfModele.setTooltip(new Tooltip("Ex: 5075E, T7.210 (2-50 caractères)"));
        cbEtat.setTooltip(new Tooltip("État actuel de la machine"));
        tfNumeroSerie.setTooltip(new Tooltip("Identifiant unique (5-30 caractères)"));
        tfNom.setTooltip(new Tooltip("Nom descriptif de la machine (3-50 caractères)"));
        dpDateAchat.setTooltip(new Tooltip("Date d'achat (ne peut pas être dans le futur)"));
        tfKilometrage.setTooltip(new Tooltip("Kilométrage ou heures d'utilisation (nombre ≥ 0)"));
        dpDateLastVisite.setTooltip(new Tooltip("Date de la dernière maintenance (optionnel)"));
        tfKmLastVisite.setTooltip(new Tooltip("Kilométrage lors de la dernière visite (optionnel)"));
        dpProchaineMaintenance.setTooltip(new Tooltip("Date prévue pour la prochaine maintenance"));
        cbCin.setTooltip(new Tooltip("CIN du responsable - Sélectionnez dans la liste"));
    }

    /**
     * Méthode appelée par AfficherMachinesController pour passer la machine à modifier
     */
    public void setMachine(Machine machine) {
        this.machineAModifier = machine;
        remplirChamps();
    }

    /**
     * Remplit les champs avec les données de la machine à modifier
     */
    private void remplirChamps() {
        if (machineAModifier != null) {
            tfMarque.setText(machineAModifier.getMarque());
            tfModele.setText(machineAModifier.getModele());
            cbEtat.setValue(machineAModifier.getEtatM());
            tfNumeroSerie.setText(machineAModifier.getNumeroSerie());
            tfNom.setText(machineAModifier.getNom());
            dpDateAchat.setValue(machineAModifier.getDateAchat());
            tfKilometrage.setText(String.valueOf(machineAModifier.getKilometrage()));

            if (machineAModifier.getDateLastVisite() != null) {
                dpDateLastVisite.setValue(machineAModifier.getDateLastVisite());
            }

            tfKmLastVisite.setText(String.valueOf(machineAModifier.getKmLastVisite()));

            if (machineAModifier.getProchaineMaintenance() != null) {
                dpProchaineMaintenance.setValue(machineAModifier.getProchaineMaintenance());
            }

            // Sélectionner le CIN dans la ComboBox
            if (machineAModifier.getCin() > 0) {
                String cinDisplay = trouverCinDisplay(machineAModifier.getCin());
                if (cinDisplay != null) {
                    cbCin.setValue(cinDisplay);
                }
            }
        }
    }

    private String trouverCinDisplay(int cin) {
        for (String item : cbCin.getItems()) {
            if (item.startsWith(String.valueOf(cin) + " - ")) {
                return item;
            }
        }
        return null;
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        // Récupérer les nouvelles valeurs
        String marque = tfMarque.getText().trim();
        String modele = tfModele.getText().trim();
        String etatM = cbEtat.getValue();
        String numeroSerie = tfNumeroSerie.getText().trim();
        String nom = tfNom.getText().trim();
        LocalDate dateAchat = dpDateAchat.getValue();
        String kilometrageStr = tfKilometrage.getText().trim();
        LocalDate dateLastVisite = dpDateLastVisite.getValue();
        String kmLastVisiteStr = tfKmLastVisite.getText().trim();
        LocalDate prochaineMaintenance = dpProchaineMaintenance.getValue();

        // Extraction du CIN depuis la ComboBox
        String cinSelection = cbCin.getValue();
        int cin = -1;

        // Validation du CIN
        if (cinSelection == null || cinSelection.isEmpty() ||
                cinSelection.contains("Aucun") || cinSelection.contains("Erreur")) {
            afficherAlerte("Erreur de validation",
                    "Veuillez sélectionner un responsable valide.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            String cinStr = cinSelection.split(" - ")[0];
            cin = Integer.parseInt(cinStr);
        } catch (Exception e) {
            afficherAlerte("Erreur de validation",
                    "Format de CIN invalide.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Validation complète
        StringBuilder erreurs = new StringBuilder();

        if (!validerMarque(marque, erreurs)) {}
        if (!validerModele(modele, erreurs)) {}
        if (!validerEtat(etatM, erreurs)) {}
        if (!validerNumeroSerie(numeroSerie, erreurs)) {}
        if (!validerNom(nom, erreurs)) {}
        if (!validerDateAchat(dateAchat, erreurs)) {}

        int kilometrage = validerKilometrage(kilometrageStr, erreurs);
        int kmLastVisite = validerKmLastVisite(kmLastVisiteStr, erreurs);

        // Validation des relations entre dates
        if (!validerDatesCoherentes(dateAchat, dateLastVisite, prochaineMaintenance, erreurs)) {}

        // Validation des relations entre kilométrages
        if (!validerKmCoherents(kilometrage, kmLastVisite, erreurs)) {}

        if (erreurs.length() > 0) {
            afficherAlerte("Erreur de validation", erreurs.toString(), Alert.AlertType.ERROR);
            return;
        }

        try {
            // Vérifier l'unicité du numéro de série (sauf si c'est le même)
            if (!numeroSerie.equalsIgnoreCase(machineAModifier.getNumeroSerie())) {
                if (numeroSerieExiste(numeroSerie)) {
                    afficherAlerte("Erreur de validation",
                            "Le numéro de série '" + numeroSerie + "' existe déjà dans la base de données.",
                            Alert.AlertType.ERROR);
                    return;
                }
            }

            // Mettre à jour les données de la machine
            machineAModifier.setMarque(marque);
            machineAModifier.setModele(modele);
            machineAModifier.setEtatM(etatM);
            machineAModifier.setNumeroSerie(numeroSerie);
            machineAModifier.setNom(nom);
            machineAModifier.setDateAchat(dateAchat);
            machineAModifier.setKilometrage(kilometrage);
            machineAModifier.setDateLastVisite(dateLastVisite);
            machineAModifier.setKmLastVisite(kmLastVisite);
            machineAModifier.setProchaineMaintenance(prochaineMaintenance);
            machineAModifier.setCin(cin);

            // Modifier dans la base
            machineService.modifier(machineAModifier);

            // Confirmation
            afficherAlerte("Succès",
                    "Machine modifiée avec succès !\n\n" +
                            "📌 " + marque + " " + modele + "\n" +
                            "🔢 N° Série: " + numeroSerie + "\n" +
                            "👤 Responsable: " + cinSelection,
                    Alert.AlertType.INFORMATION);

            // Retourner à la liste
            retourListeMachines(event);

        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ================= VALIDATIONS =================

    private boolean validerMarque(String marque, StringBuilder erreurs) {
        if (marque.isEmpty()) {
            erreurs.append("• La marque est obligatoire.\n");
            return false;
        }
        if (marque.length() < 2) {
            erreurs.append("• La marque doit contenir au minimum 2 caractères.\n");
            return false;
        }
        if (marque.length() > 50) {
            erreurs.append("• La marque ne doit pas dépasser 50 caractères.\n");
            return false;
        }
        if (!marque.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
            erreurs.append("• La marque ne doit contenir que des lettres, espaces et tirets.\n");
            return false;
        }
        return true;
    }

    private boolean validerModele(String modele, StringBuilder erreurs) {
        if (modele.isEmpty()) {
            erreurs.append("• Le modèle est obligatoire.\n");
            return false;
        }
        if (modele.length() < 2) {
            erreurs.append("• Le modèle doit contenir au minimum 2 caractères.\n");
            return false;
        }
        if (modele.length() > 50) {
            erreurs.append("• Le modèle ne doit pas dépasser 50 caractères.\n");
            return false;
        }
        if (!modele.matches("^[a-zA-Z0-9À-ÿ\\s-]+$")) {
            erreurs.append("• Le modèle peut contenir des lettres, chiffres, espaces et tirets uniquement.\n");
            return false;
        }
        return true;
    }

    private boolean validerEtat(String etat, StringBuilder erreurs) {
        if (etat == null || etat.isEmpty()) {
            erreurs.append("• L'état est obligatoire.\n");
            return false;
        }
        if (!ETATS_VALIDES.contains(etat)) {
            erreurs.append("• L'état doit être l'une des valeurs suivantes : " + String.join(", ", ETATS_VALIDES) + "\n");
            return false;
        }
        return true;
    }

    private boolean validerNumeroSerie(String numeroSerie, StringBuilder erreurs) {
        if (numeroSerie.isEmpty()) {
            erreurs.append("• Le numéro de série est obligatoire.\n");
            return false;
        }
        if (numeroSerie.length() < 5) {
            erreurs.append("• Le numéro de série doit contenir au minimum 5 caractères.\n");
            return false;
        }
        if (numeroSerie.length() > 30) {
            erreurs.append("• Le numéro de série ne doit pas dépasser 30 caractères.\n");
            return false;
        }
        if (!numeroSerie.matches("^[a-zA-Z0-9-]+$")) {
            erreurs.append("• Le numéro de série ne doit contenir que des lettres, chiffres et tirets.\n");
            return false;
        }
        return true;
    }

    private boolean validerNom(String nom, StringBuilder erreurs) {
        if (nom.isEmpty()) {
            erreurs.append("• Le nom est obligatoire.\n");
            return false;
        }
        if (nom.length() < 3) {
            erreurs.append("• Le nom doit contenir au minimum 3 caractères.\n");
            return false;
        }
        if (nom.length() > 50) {
            erreurs.append("• Le nom ne doit pas dépasser 50 caractères.\n");
            return false;
        }
        if (nom.matches("^[0-9]+$")) {
            erreurs.append("• Le nom ne peut pas contenir uniquement des chiffres.\n");
            return false;
        }
        return true;
    }

    private boolean validerDateAchat(LocalDate dateAchat, StringBuilder erreurs) {
        if (dateAchat == null) {
            erreurs.append("• La date d'achat est obligatoire.\n");
            return false;
        }
        if (dateAchat.isAfter(LocalDate.now())) {
            erreurs.append("• La date d'achat ne peut pas être dans le futur.\n");
            return false;
        }
        if (dateAchat.isBefore(LocalDate.of(1900, 1, 1))) {
            erreurs.append("• La date d'achat doit être après le 1er janvier 1900.\n");
            return false;
        }
        return true;
    }

    private int validerKilometrage(String kilometrageStr, StringBuilder erreurs) {
        if (kilometrageStr.isEmpty()) {
            erreurs.append("• Le kilométrage est obligatoire.\n");
            return -1;
        }
        try {
            int km = Integer.parseInt(kilometrageStr);
            if (km < 0) {
                erreurs.append("• Le kilométrage ne peut pas être négatif.\n");
                return -1;
            }
            if (km > 999999) {
                erreurs.append("• Le kilométrage ne peut pas dépasser 999999.\n");
                return -1;
            }
            return km;
        } catch (NumberFormatException e) {
            erreurs.append("• Le kilométrage doit être un nombre valide.\n");
            return -1;
        }
    }

    private int validerKmLastVisite(String kmLastVisiteStr, StringBuilder erreurs) {
        if (kmLastVisiteStr.isEmpty()) {
            return 0;
        }
        try {
            int km = Integer.parseInt(kmLastVisiteStr);
            if (km < 0) {
                erreurs.append("• Le kilométrage de la dernière visite ne peut pas être négatif.\n");
                return -1;
            }
            if (km > 999999) {
                erreurs.append("• Le kilométrage de la dernière visite ne peut pas dépasser 999999.\n");
                return -1;
            }
            return km;
        } catch (NumberFormatException e) {
            erreurs.append("• Le kilométrage de la dernière visite doit être un nombre valide.\n");
            return -1;
        }
    }

    private boolean validerDatesCoherentes(LocalDate dateAchat, LocalDate dateLastVisite,
                                           LocalDate prochaineMaintenance, StringBuilder erreurs) {
        boolean valide = true;

        if (dateLastVisite != null && dateAchat != null) {
            if (dateLastVisite.isBefore(dateAchat)) {
                erreurs.append("• La date de dernière visite ne peut pas être antérieure à la date d'achat.\n");
                valide = false;
            }
            if (dateLastVisite.isAfter(LocalDate.now())) {
                erreurs.append("• La date de dernière visite ne peut pas être dans le futur.\n");
                valide = false;
            }
        }

        if (prochaineMaintenance != null && dateAchat != null) {
            if (prochaineMaintenance.isBefore(dateAchat)) {
                erreurs.append("• La date de prochaine maintenance ne peut pas être antérieure à la date d'achat.\n");
                valide = false;
            }
        }

        if (prochaineMaintenance != null && dateLastVisite != null) {
            if (prochaineMaintenance.isBefore(dateLastVisite)) {
                erreurs.append("• La date de prochaine maintenance doit être après la dernière visite.\n");
                valide = false;
            }
        }

        return valide;
    }

    private boolean validerKmCoherents(int kilometrage, int kmLastVisite, StringBuilder erreurs) {
        if (kmLastVisite > 0 && kilometrage < kmLastVisite) {
            erreurs.append("• Le kilométrage actuel ne peut pas être inférieur au kilométrage de la dernière visite.\n");
            return false;
        }
        return true;
    }

    private boolean numeroSerieExiste(String numeroSerie) {
        try {
            List<Machine> machines = machineService.recuperer();
            for (Machine m : machines) {
                if (m.getNumeroSerie() != null &&
                        m.getNumeroSerie().equalsIgnoreCase(numeroSerie) &&
                        m.getIdM() != machineAModifier.getIdM()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler la modification");
        confirm.setContentText("Voulez-vous vraiment annuler ? Les modifications seront perdues.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            retourListeMachines(event);
        }
    }

    @FXML
    private void rafraichirCin() {
        chargerCinDisponibles();
        if (machineAModifier != null && machineAModifier.getCin() > 0) {
            String cinDisplay = trouverCinDisplay(machineAModifier.getCin());
            if (cinDisplay != null) {
                cbCin.setValue(cinDisplay);
            }
        }
    }

    private void retourListeMachines(ActionEvent event) {
        try {
            String fxml = "/MaterielsInterface/AfficherMachines.fxml";
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de retourner à la liste des machines", Alert.AlertType.ERROR);
        }
    }

    // ================= NAVIGATION =================

    @FXML
    private void handleDashboard(MouseEvent event) {
        naviguerVers("/UsersInterface/Acceuil.fxml");
    }

    @FXML
    private void handleAnimals(MouseEvent event) {
        naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml");
    }

    @FXML
    private void handleStocks(MouseEvent event) {
        naviguerVers("/StocksInterface/afficherarticle.fxml");
    }

    @FXML
    private void handleTerrains(MouseEvent event) {
        naviguerVers("/TerrainsInterface/acceuilterrain.fxml");
    }

    @FXML
    private void handleEvents(MouseEvent event) {
        naviguerVers("/G-Evenements/Accueil.fxml");
    }

    @FXML
    private void handleMateriels(MouseEvent event) {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml");
    }

    @FXML
    private void handlePersonnes(MouseEvent event) {
        naviguerVers("/UsersInterface/DahboardPersonne.fxml");
    }

    @FXML
    private void handleTaches(MouseEvent event) {
        naviguerVers("/UsersInterface/GestionTache.fxml");
    }

    @FXML
    private void handleAbonnements(MouseEvent event) {
        naviguerVers("/UsersInterface/GestionAbonnements.fxml");
    }

    @FXML
    private void handleOffres(MouseEvent event) {
        naviguerVers("/UsersInterface/GestionOffre.fxml");
    }

    private void naviguerVers(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) tfMarque.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de naviguer vers la page demandée", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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
                Scene scene = stage.getScene();
                scene.setRoot(root);
                stage.show();

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    private void showGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}