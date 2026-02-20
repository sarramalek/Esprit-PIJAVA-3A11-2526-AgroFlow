package controllers;

import entities.Machine;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.MachineService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class AjoutMachineController {

    // ── Champs du formulaire ───────────────────────────────────────────────────
    @FXML private TextField  tfMarque;
    @FXML private TextField  tfModele;
    @FXML private ComboBox<String> tfEtatM;      // ← ComboBox (pas TextField)
    @FXML private TextField  tfNumeroSerie;
    @FXML private DatePicker dpDateAchat;
    @FXML private TextField  tfNom;

    // ── Service ───────────────────────────────────────────────────────────────
    private final MachineService machineService = new MachineService();

    // ── États valides ─────────────────────────────────────────────────────────
    private static final List<String> ETATS_VALIDES = Arrays.asList(
            "Neuf", "Disponible", "Occasion", "Bon", "En panne", "En maintenance"
    );

    // ══════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        // Peupler le ComboBox avec les états valides
        tfEtatM.setItems(FXCollections.observableArrayList(ETATS_VALIDES));
        tfEtatM.setPromptText("Sélectionnez l'état");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AJOUTER
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAjouter() {
        String    marque      = tfMarque.getText() == null      ? "" : tfMarque.getText().trim();
        String    modele      = tfModele.getText() == null      ? "" : tfModele.getText().trim();
        String    etatM       = tfEtatM.getValue();                     // ComboBox → getValue()
        String    numeroSerie = tfNumeroSerie.getText() == null ? "" : tfNumeroSerie.getText().trim();
        LocalDate dateAchat   = dpDateAchat.getValue();
        String    nom         = tfNom.getText() == null         ? "" : tfNom.getText().trim();

        // Validation
        if (!validerTousLesChamps(marque, modele, etatM, numeroSerie, dateAchat, nom)) return;

        try {
            // Unicité du numéro de série
            if (numeroSerieExiste(numeroSerie)) {
                alerte("Erreur de validation",
                        "Le numéro de série « " + numeroSerie + " » existe déjà.",
                        Alert.AlertType.ERROR);
                return;
            }

            Machine machine = new Machine();
            machine.setMarque(marque);
            machine.setModele(modele);
            machine.setEtatM(etatM);
            machine.setNumeroSerie(numeroSerie);
            machine.setDateAchat(dateAchat);
            machine.setNom(nom);

            machineService.ajouter(machine);
            alerte("Succès", "Machine ajoutée avec succès !", Alert.AlertType.INFORMATION);
            retourListeMachines();

        } catch (Exception e) {
            e.printStackTrace();
            alerte("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════════════════════════════════

    private boolean validerTousLesChamps(String marque, String modele, String etatM,
                                         String numeroSerie, LocalDate dateAchat, String nom) {
        return validerMarque(marque)
                && validerModele(modele)
                && validerEtat(etatM)
                && validerNumeroSerie(numeroSerie)
                && validerDateAchat(dateAchat)
                && validerNom(nom);
    }

    /** Marque : obligatoire, ≥ 2 caractères, lettres et espaces uniquement. */
    private boolean validerMarque(String marque) {
        if (marque.isEmpty()) {
            alerte("Validation", "La marque est obligatoire.", Alert.AlertType.WARNING);
            return false;
        }
        if (marque.length() < 2) {
            alerte("Validation", "La marque doit contenir au moins 2 caractères.", Alert.AlertType.WARNING);
            return false;
        }
        if (!marque.matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
            alerte("Validation", "La marque ne doit contenir que des lettres et des espaces.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /** Modèle : obligatoire, ≥ 2 caractères, lettres/chiffres/espaces/tirets. */
    private boolean validerModele(String modele) {
        if (modele.isEmpty()) {
            alerte("Validation", "Le modèle est obligatoire.", Alert.AlertType.WARNING);
            return false;
        }
        if (modele.length() < 2) {
            alerte("Validation", "Le modèle doit contenir au moins 2 caractères.", Alert.AlertType.WARNING);
            return false;
        }
        if (!modele.matches("^[a-zA-Z0-9À-ÿ\\s-]+$")) {
            alerte("Validation", "Le modèle peut contenir des lettres, chiffres, espaces et tirets.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /** État : obligatoire, doit être sélectionné dans le ComboBox. */
    private boolean validerEtat(String etatM) {
        if (etatM == null || etatM.isEmpty()) {
            alerte("Validation",
                    "L'état est obligatoire. Sélectionnez une valeur dans la liste :\n"
                            + String.join(", ", ETATS_VALIDES),
                    Alert.AlertType.WARNING);
            return false;
        }
        // La valeur vient forcément du ComboBox → toujours valide
        return true;
    }

    /** N° Série : obligatoire, ≥ 5 caractères, lettres et chiffres uniquement. */
    private boolean validerNumeroSerie(String numeroSerie) {
        if (numeroSerie.isEmpty()) {
            alerte("Validation", "Le numéro de série est obligatoire.", Alert.AlertType.WARNING);
            return false;
        }
        if (numeroSerie.length() < 5) {
            alerte("Validation", "Le numéro de série doit contenir au moins 5 caractères.", Alert.AlertType.WARNING);
            return false;
        }
        if (!numeroSerie.matches("^[a-zA-Z0-9]+$")) {
            alerte("Validation", "Le numéro de série ne doit contenir que des lettres et des chiffres.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /** Date d'achat : obligatoire, pas dans le futur, après 1900. */
    private boolean validerDateAchat(LocalDate dateAchat) {
        if (dateAchat == null) {
            alerte("Validation", "La date d'achat est obligatoire.", Alert.AlertType.WARNING);
            return false;
        }
        if (dateAchat.isAfter(LocalDate.now())) {
            alerte("Validation", "La date d'achat ne peut pas être dans le futur.", Alert.AlertType.WARNING);
            return false;
        }
        if (dateAchat.isBefore(LocalDate.of(1900, 1, 1))) {
            alerte("Validation", "La date d'achat doit être après le 1er janvier 1900.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /** Nom : obligatoire, ≥ 3 caractères, pas uniquement des chiffres. */
    private boolean validerNom(String nom) {
        if (nom.isEmpty()) {
            alerte("Validation", "Le nom est obligatoire.", Alert.AlertType.WARNING);
            return false;
        }
        if (nom.length() < 3) {
            alerte("Validation", "Le nom doit contenir au moins 3 caractères.", Alert.AlertType.WARNING);
            return false;
        }
        if (nom.matches("^[0-9]+$")) {
            alerte("Validation", "Le nom ne peut pas contenir uniquement des chiffres.", Alert.AlertType.WARNING);
            return false;
        }
        if (!nom.matches("^[a-zA-Z0-9À-ÿ\\s-]+$")) {
            alerte("Validation", "Le nom ne doit pas contenir de caractères spéciaux.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /** Vérifie si le numéro de série existe déjà en base. */
    private boolean numeroSerieExiste(String numeroSerie) {
        try {
            return machineService.recuperer().stream()
                    .anyMatch(m -> m.getNumeroSerie() != null
                            && m.getNumeroSerie().equalsIgnoreCase(numeroSerie));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ANNULER / RETOUR
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAnnuler() {
        retourListeMachines();
    }

    private void retourListeMachines() {
        naviguerVers("/AffichageMachine.fxml");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════

    @FXML private void naviguerAnimaux()    { naviguerVers("/AfficherAnimaux.fxml");   }
    @FXML private void naviguerMateriels()  { naviguerVers("/AccueilMateriel.fxml");   }
    @FXML private void naviguerStocks()     { naviguerVers("/AfficherStocks.fxml");    }
    @FXML private void naviguerTerrains()   { naviguerVers("/AfficherTerrains.fxml"); }
    @FXML private void naviguerEvenements() { naviguerVers("/AccueilEvenement.fxml"); }
    @FXML private void naviguerUsers()      { naviguerVers("/AfficherUsers.fxml");     }
    @FXML private void deconnexion()        { naviguerVers("/Login.fxml");             }

    private void naviguerVers(String fxmlPath) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage  stage = (Stage) tfMarque.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            alerte("Erreur", "Impossible d'ouvrir : " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILITAIRE
    // ══════════════════════════════════════════════════════════════════════════

    private void alerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}