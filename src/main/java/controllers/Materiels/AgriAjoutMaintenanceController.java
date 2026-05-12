package controllers.Materiels;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Materiels.Machine;
import models.Materiels.Maintenance;
import models.User.Personne;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class AgriAjoutMaintenanceController implements Initializable {

    // ── Champs du formulaire ──
    @FXML private ComboBox<Machine> comboMachine;
    @FXML private ComboBox<String>  comboTypePanne;
    @FXML private DatePicker        datePicker;
    @FXML private TextField         txtCout;
    @FXML private TextArea          txtDescription;
    @FXML private ComboBox<String>  comboStatut;
    @FXML private TextArea          txtRecommandation;
    @FXML private ComboBox<String>  comboPriorite;
    @FXML private TextField         txtKilometrage;

    // ── Boutons ──
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnGenererRecommandation;

    // ── Labels d'erreur inline ──
    @FXML private Label lblErrMachine;
    @FXML private Label lblErrTypePanne;
    @FXML private Label lblErrDate;
    @FXML private Label lblErrCout;
    @FXML private Label lblErrKilometrage;
    @FXML private Label lblErrStatut;
    @FXML private Label lblErrPriorite;

    // ── Bandeau d'alerte interne ──
    @FXML private Label  lblAlerteBandeau;

    private Personne                      currentUser;
    private AgricoleMaintenanceController parentController;
    private MachineService                machineService;
    private MaintenanceService            maintenanceService;
    private List<Machine>                 machines;

    // ── Types de panne prédéfinis ──
    private final List<String> typesPanne = Arrays.asList(
            "Moteur - Surchauffe",
            "Moteur - Démarrage difficile",
            "Moteur - Perte de puissance",
            "Transmission - Embrayage défectueux",
            "Transmission - Boîte de vitesses bruyante",
            "Freinage - Usure des plaquettes",
            "Freinage - Fuite liquide",
            "Direction - Jeu volant",
            "Direction - Crépitements",
            "Électricité - Batterie déchargée",
            "Électricité - Alternateur HS",
            "Électricité - Court-circuit",
            "Hydraulique - Vérin qui fuit",
            "Hydraulique - Pompe bruyante",
            "Hydraulique - Pression insuffisante",
            "Pneumatique - Crevaison",
            "Pneumatique - Usure anormale",
            "Climatisation - Plus de froid",
            "Climatisation - Odeur désagréable",
            "Échappement - Fumée excessive",
            "Filtre - Colmatage",
            "Courroie - Usure/cassure",
            "Radiateur - Fuite",
            "Réservoir - Fuite carburant"
    );

    // ══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        machineService     = new MachineService();
        maintenanceService = new MaintenanceService();

        // Remplissage des ComboBox
        comboStatut.getItems().addAll("en_cours", "termine", "planifie");
        comboPriorite.getItems().addAll("faible", "moyenne", "haute", "urgente");
        comboTypePanne.getItems().addAll(typesPanne);

        // Affichage du nom de machine dans la liste déroulante
        comboMachine.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Machine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getNom() + " (ID:" + m.getIdM() + ")");
            }
        });
        comboMachine.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Machine m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty || m == null ? null : m.getNom());
            }
        });

        chargerMachines();

        // Valeurs par défaut
        datePicker.setValue(LocalDate.now());
        comboStatut.setValue("planifie");
        comboPriorite.setValue("moyenne");

        // Bloquer les dates futures
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
                if (date.isAfter(LocalDate.now())) setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #ccc;");
            }
        });

        // Validation en temps réel
        ajouterValidateurs();

        // Masquer les labels d'erreur
        masquerErreursInline();

        // Masquer le bandeau d'alerte
        if (lblAlerteBandeau != null) lblAlerteBandeau.setVisible(false);

        // Boutons
        btnGenererRecommandation.setOnAction(e -> genererRecommandationIA());
        btnEnregistrer.setOnAction(e -> enregistrer());
        btnAnnuler.setOnAction(e -> fermer());
    }

    private void masquerErreursInline() {
        for (Label l : new Label[]{lblErrMachine, lblErrTypePanne, lblErrDate,
                lblErrCout, lblErrKilometrage, lblErrStatut, lblErrPriorite}) {
            if (l != null) { l.setText(""); l.setVisible(false); l.setManaged(false); }
        }
    }

    private void setErreurLabel(Label lbl, String msg) {
        if (lbl == null) return;
        if (msg == null || msg.isEmpty()) {
            lbl.setText(""); lbl.setVisible(false); lbl.setManaged(false);
        } else {
            lbl.setText("⚠ " + msg);
            lbl.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 11px; -fx-font-style: italic;");
            lbl.setVisible(true); lbl.setManaged(true);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CHARGEMENT MACHINES
    // ══════════════════════════════════════════════════════════════
    private void chargerMachines() {
        try {
            machines = machineService.recuperer();
            comboMachine.getItems().addAll(machines);
        } catch (SQLException e) {
            afficherBandeau("Erreur chargement machines : " + e.getMessage(), "error");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // VALIDATION EN TEMPS RÉEL
    // ══════════════════════════════════════════════════════════════
    private void ajouterValidateurs() {
        // Coût : chiffres + 2 décimales max
        txtCout.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.matches("\\d*\\.?\\d{0,2}")) {
                    txtCout.setText(oldVal);
                } else {
                    try {
                        double v = Double.parseDouble(newVal);
                        if (v < 0)         setErreurLabel(lblErrCout, "Le coût ne peut pas être négatif");
                        else if (v > 1e6)  setErreurLabel(lblErrCout, "Coût trop élevé (> 1 000 000 DT)");
                        else               setErreurLabel(lblErrCout, null);
                    } catch (NumberFormatException ignored) {}
                }
            } else { setErreurLabel(lblErrCout, null); }
        });

        // Kilométrage : entiers uniquement
        txtKilometrage.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.matches("\\d*")) {
                    txtKilometrage.setText(oldVal);
                } else {
                    try {
                        int v = Integer.parseInt(newVal);
                        if (v < 0)       setErreurLabel(lblErrKilometrage, "Kilométrage négatif interdit");
                        else if (v > 500_000) setErreurLabel(lblErrKilometrage, "Kilométrage > 500 000 km");
                        else             setErreurLabel(lblErrKilometrage, null);
                    } catch (NumberFormatException ignored) {}
                }
            } else { setErreurLabel(lblErrKilometrage, null); }
        });

        // Machine sélectionnée → effacer erreur
        comboMachine.valueProperty().addListener((obs, o, n) -> {
            if (n != null) setErreurLabel(lblErrMachine, null);
        });

        // Type panne sélectionné → effacer erreur
        comboTypePanne.valueProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isEmpty()) setErreurLabel(lblErrTypePanne, null);
        });

        // Date → effacer erreur
        datePicker.valueProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isAfter(LocalDate.now())) setErreurLabel(lblErrDate, null);
        });

        // Priorité → alerte urgente
        comboPriorite.valueProperty().addListener((obs, o, n) -> {
            if ("urgente".equals(n))
                afficherBandeau("⚠️ Priorité URGENTE sélectionnée — intervention immédiate requise !", "warning");
            else if (lblAlerteBandeau != null)
                lblAlerteBandeau.setVisible(false);
        });
    }

    // ══════════════════════════════════════════════════════════════
    // BANDEAU D'ALERTE INTERNE AU FORMULAIRE
    // ══════════════════════════════════════════════════════════════
    private void afficherBandeau(String message, String type) {
        if (lblAlerteBandeau == null) return;
        String color = switch (type) {
            case "warning" -> "#F39C12";
            case "error"   -> "#E74C3C";
            default        -> "#27AE60";
        };
        lblAlerteBandeau.setText(message);
        lblAlerteBandeau.setStyle(
                "-fx-background-color: " + color + "20; "
                        + "-fx-border-color: " + color + "; -fx-border-width: 1; "
                        + "-fx-background-radius: 8; -fx-border-radius: 8; "
                        + "-fx-padding: 8 14; -fx-text-fill: " + color + "; "
                        + "-fx-font-weight: bold; -fx-font-size: 12px;");
        lblAlerteBandeau.setVisible(true);
        lblAlerteBandeau.setManaged(true);
        // Auto-fermeture après 6 s (sauf erreur)
        if (!"error".equals(type)) {
            new Thread(() -> {
                try { Thread.sleep(6000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                Platform.runLater(() -> {
                    if (lblAlerteBandeau != null) lblAlerteBandeau.setVisible(false);
                });
            }).start();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // GÉNÉRATION RECOMMANDATION IA
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void genererRecommandationIA() {
        String typePanne  = comboTypePanne.getValue();
        String priorite   = comboPriorite.getValue();
        String description= txtDescription.getText();
        String statut     = comboStatut.getValue();
        Machine machine   = comboMachine.getValue();

        if (typePanne == null || typePanne.isBlank()) {
            afficherBandeau("Sélectionnez d'abord un type de panne pour générer une recommandation IA.", "warning");
            return;
        }

        // Animation bouton
        btnGenererRecommandation.setText("⏳ Génération…");
        btnGenererRecommandation.setDisable(true);
        btnGenererRecommandation.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; "
                        + "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");

        new Thread(() -> {
            try { Thread.sleep(600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            String rec = genererRecommandationSelonCriteres(typePanne, priorite, description, machine, statut);
            Platform.runLater(() -> {
                txtRecommandation.setText(rec);
                btnGenererRecommandation.setText("🤖 IA");
                btnGenererRecommandation.setDisable(false);
                btnGenererRecommandation.setStyle(
                        "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; "
                                + "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
                afficherBandeau("✅ Recommandation IA générée avec succès !", "success");
            });
        }).start();
    }

    private String genererRecommandationSelonCriteres(String typePanne, String priorite,
                                                      String description, Machine machine,
                                                      String statut) {
        StringBuilder rec = new StringBuilder();
        rec.append("🔧 RECOMMANDATIONS TECHNIQUES :\n\n");

        String tp = typePanne.toLowerCase();
        if (tp.contains("moteur")) {
            rec.append("• Vérifier le niveau d'huile et sa qualité\n");
            rec.append("• Contrôler les bougies d'allumage\n");
            rec.append("• Tester la compression des cylindres\n");
            rec.append("• Nettoyer ou remplacer le filtre à air\n");
            if (tp.contains("surchauffe")) {
                rec.append("• Vérifier le circuit de refroidissement (liquide, thermostat, pompe à eau)\n");
                rec.append("• Contrôler le ventilateur et le radiateur\n");
            } else if (tp.contains("démarrage")) {
                rec.append("• Tester la batterie et le démarreur\n");
                rec.append("• Vérifier le circuit d'alimentation en carburant\n");
            } else if (tp.contains("puissance")) {
                rec.append("• Contrôler l'injection / le carburateur\n");
                rec.append("• Inspecter le filtre à carburant\n");
            }
        } else if (tp.contains("frein")) {
            rec.append("• Contrôler l'usure des plaquettes et disques\n");
            rec.append("• Vérifier le niveau du liquide de frein\n");
            rec.append("• Purger le circuit de freinage\n");
            rec.append("• Inspecter les flexibles pour détecter des fuites\n");
        } else if (tp.contains("électricité") || tp.contains("batterie") || tp.contains("alternateur")) {
            rec.append("• Tester la batterie (tension à vide et en charge)\n");
            rec.append("• Contrôler l'alternateur (tension de charge)\n");
            rec.append("• Vérifier toutes les connexions électriques et cosses\n");
            rec.append("• Contrôler les fusibles et relais\n");
        } else if (tp.contains("transmission") || tp.contains("embrayage")) {
            rec.append("• Vérifier le niveau d'huile de boîte de vitesses\n");
            rec.append("• Contrôler l'état de l'embrayage (jeu, usure)\n");
            rec.append("• Inspecter les joints et paliers\n");
        } else if (tp.contains("hydraulique")) {
            rec.append("• Vérifier le niveau d'huile hydraulique\n");
            rec.append("• Contrôler l'état de tous les flexibles et raccords\n");
            rec.append("• Tester la pression du circuit hydraulique\n");
            rec.append("• Inspecter la pompe hydraulique\n");
        } else if (tp.contains("pneumatique") || tp.contains("crevaison")) {
            rec.append("• Inspecter visuellement le pneu (corps étranger, déchirure)\n");
            rec.append("• Vérifier la pression de gonflage de tous les pneus\n");
            rec.append("• Contrôler l'état des jantes\n");
        } else if (tp.contains("direction")) {
            rec.append("• Contrôler le jeu de la direction (rotules, biellettes)\n");
            rec.append("• Vérifier le niveau d'huile de direction assistée\n");
            rec.append("• Inspecter les roulements de roue\n");
        } else if (tp.contains("radiateur") || tp.contains("refroidissement")) {
            rec.append("• Vérifier l'étanchéité du radiateur\n");
            rec.append("• Contrôler le niveau et la qualité du liquide de refroidissement\n");
            rec.append("• Nettoyer le radiateur (poussière, débris)\n");
        } else if (tp.contains("filtre")) {
            rec.append("• Remplacer le filtre concerné\n");
            rec.append("• Contrôler le circuit en amont et en aval du filtre\n");
        } else if (tp.contains("courroie")) {
            rec.append("• Remplacer la courroie et les galets tendeurs\n");
            rec.append("• Contrôler l'alignement des poulies\n");
        } else {
            rec.append("• Effectuer un diagnostic complet\n");
            rec.append("• Consulter la documentation technique du constructeur\n");
            rec.append("• Faire appel à un technicien spécialisé si nécessaire\n");
        }

        rec.append("\n⏰ PRIORITÉ D'INTERVENTION :\n");
        if ("urgente".equals(priorite)) {
            rec.append("• ⛔ ARRÊT IMMÉDIAT — Ne pas utiliser la machine\n");
            rec.append("• Contacter un technicien d'urgence dans les plus brefs délais\n");
            rec.append("• Sécuriser la machine et baliser la zone si nécessaire\n");
        } else if ("haute".equals(priorite)) {
            rec.append("• Intervention dans les 24 à 48 h\n");
            rec.append("• Planifier rapidement la réparation\n");
            rec.append("• Limiter l'utilisation de la machine en attendant\n");
        } else if ("moyenne".equals(priorite)) {
            rec.append("• Intervention sous 1 semaine\n");
            rec.append("• À inclure dans la prochaine maintenance planifiée\n");
        } else {
            rec.append("• Planifier lors de la prochaine révision périodique\n");
            rec.append("• Surveillance régulière conseillée\n");
        }

        // Analyse kilométrage
        String kmStr = txtKilometrage.getText();
        if (kmStr != null && !kmStr.isBlank()) {
            try {
                int km = Integer.parseInt(kmStr.trim());
                rec.append("\n📊 ANALYSE KILOMÉTRAGE (").append(km).append(" km) :\n");
                if (km > 10000) {
                    rec.append("• Kilométrage élevé — révision complète fortement recommandée\n");
                    rec.append("• Vérifier la courroie de distribution et ses galets\n");
                    rec.append("• Changer tous les filtres (huile, air, carburant)\n");
                } else if (km > 5000) {
                    rec.append("• Kilométrage intermédiaire — vérifier les consommables\n");
                    rec.append("• Contrôler les niveaux de fluides\n");
                } else {
                    rec.append("• Kilométrage faible — vérification ciblée suffisante\n");
                }
            } catch (NumberFormatException ignored) {}
        }

        // Analyse statut
        if (statut != null) {
            rec.append("\n📋 STATUT ACTUEL : ").append(statut).append("\n");
            if ("planifie".equals(statut))
                rec.append("• Confirmer la disponibilité des pièces avant l'intervention\n");
            else if ("en_cours".equals(statut))
                rec.append("• Suivi en cours — vérifier l'avancement régulièrement\n");
            else if ("termine".equals(statut))
                rec.append("• Effectuer un test de validation après réparation\n");
        }

        // Spécifique à la machine
        if (machine != null) {
            rec.append("\n🚜 SPÉCIFIQUE À LA MACHINE « ").append(machine.getNom()).append(" » :\n");
            rec.append("• Consulter le manuel d'entretien du constructeur\n");
            rec.append("• Vérifier l'historique complet des maintenances\n");
            rec.append("• Commander les pièces d'origine si disponibles\n");
        }

        // Analyse description
        if (description != null && !description.isBlank()) {
            String desc = description.toLowerCase();
            rec.append("\n💬 ANALYSE DE LA DESCRIPTION :\n");
            if (desc.contains("bruit") || desc.contains("craquement") || desc.contains("grincement"))
                rec.append("• Bruits anormaux → probable usure ou desserrage d'éléments mécaniques\n");
            if (desc.contains("fumée") || desc.contains("fumee"))
                rec.append("• Fumée → vérifier combustion, joints de culasse, huile moteur\n");
            if (desc.contains("fuite") || desc.contains("huile"))
                rec.append("• Fuite → localiser précisément la source avant toute intervention\n");
            if (desc.contains("vibration") || desc.contains("tremblement"))
                rec.append("• Vibrations → contrôler l'équilibrage et les fixations\n");
            if (desc.contains("surchauffe") || desc.contains("chaud") || desc.contains("temperature"))
                rec.append("• Surchauffe → vérifier le système de refroidissement en priorité\n");
        }

        rec.append("\n✅ PLAN D'ACTION RECOMMANDÉ :\n");
        rec.append("  1. Réaliser un diagnostic complet et documenté\n");
        rec.append("  2. Identifier et commander les pièces de rechange\n");
        rec.append("  3. Planifier et réaliser l'intervention\n");
        rec.append("  4. Effectuer les tests de validation post-réparation\n");
        rec.append("  5. Mettre à jour le carnet d'entretien de la machine\n");
        rec.append("  6. Informer le responsable de la clôture de la maintenance\n");

        return rec.toString();
    }

    // ══════════════════════════════════════════════════════════════
    // VALIDATION COMPLÈTE DU FORMULAIRE
    // ══════════════════════════════════════════════════════════════
    private boolean validerFormulaire() {
        masquerErreursInline();
        boolean ok = true;

        // 1. Machine obligatoire
        if (comboMachine.getValue() == null) {
            setErreurLabel(lblErrMachine, "Sélectionnez une machine");
            comboMachine.requestFocus(); ok = false;
        }

        // 2. Type de panne obligatoire
        if (comboTypePanne.getValue() == null || comboTypePanne.getValue().isBlank()) {
            setErreurLabel(lblErrTypePanne, "Sélectionnez un type de panne");
            if (ok) { comboTypePanne.requestFocus(); ok = false; } else ok = false;
        }

        // 3. Date obligatoire et non future
        if (datePicker.getValue() == null) {
            setErreurLabel(lblErrDate, "Sélectionnez une date");
            if (ok) { datePicker.requestFocus(); ok = false; } else ok = false;
        } else if (datePicker.getValue().isAfter(LocalDate.now())) {
            setErreurLabel(lblErrDate, "La date ne peut pas être dans le futur");
            if (ok) { datePicker.requestFocus(); ok = false; } else ok = false;
        }

        // 4. Coût obligatoire, numérique, ≥ 0, ≤ 1 000 000
        String coutStr = txtCout.getText();
        if (coutStr == null || coutStr.isBlank()) {
            setErreurLabel(lblErrCout, "Saisissez le coût");
            if (ok) { txtCout.requestFocus(); ok = false; } else ok = false;
        } else {
            try {
                double v = Double.parseDouble(coutStr.trim());
                if (v < 0) {
                    setErreurLabel(lblErrCout, "Le coût ne peut pas être négatif");
                    if (ok) { txtCout.requestFocus(); ok = false; } else ok = false;
                } else if (v > 1_000_000) {
                    setErreurLabel(lblErrCout, "Coût trop élevé (> 1 000 000 DT)");
                    if (ok) { txtCout.requestFocus(); ok = false; } else ok = false;
                }
            } catch (NumberFormatException e) {
                setErreurLabel(lblErrCout, "Nombre invalide");
                if (ok) { txtCout.requestFocus(); ok = false; } else ok = false;
            }
        }

        // 5. Kilométrage facultatif mais valide si renseigné
        String kmStr = txtKilometrage.getText();
        if (kmStr != null && !kmStr.isBlank()) {
            try {
                int km = Integer.parseInt(kmStr.trim());
                if (km < 0) {
                    setErreurLabel(lblErrKilometrage, "Kilométrage négatif interdit");
                    if (ok) { txtKilometrage.requestFocus(); ok = false; } else ok = false;
                } else if (km > 500_000) {
                    setErreurLabel(lblErrKilometrage, "Kilométrage > 500 000 km");
                    if (ok) { txtKilometrage.requestFocus(); ok = false; } else ok = false;
                }
            } catch (NumberFormatException e) {
                setErreurLabel(lblErrKilometrage, "Nombre entier invalide");
                if (ok) { txtKilometrage.requestFocus(); ok = false; } else ok = false;
            }
        }

        // 6. Statut — valeur par défaut si vide
        if (comboStatut.getValue() == null) comboStatut.setValue("planifie");

        // 7. Priorité — valeur par défaut si vide
        if (comboPriorite.getValue() == null) comboPriorite.setValue("moyenne");

        if (!ok) afficherBandeau("Veuillez corriger les erreurs indiquées en rouge.", "error");
        return ok;
    }

    // ══════════════════════════════════════════════════════════════
    // ENREGISTREMENT
    // ══════════════════════════════════════════════════════════════
    private void enregistrer() {
        if (!validerFormulaire()) return;

        try {
            double cout       = Double.parseDouble(txtCout.getText().trim());
            int    kilometrage = 0;
            String kmStr       = txtKilometrage.getText();
            if (kmStr != null && !kmStr.isBlank())
                kilometrage = Integer.parseInt(kmStr.trim());

            Machine machineSelect = comboMachine.getValue();

            Maintenance m = new Maintenance();
            m.setIdM(machineSelect.getIdM());
            m.setTypePanne(comboTypePanne.getValue().trim());
            m.setCout(cout);
            m.setDateMain(datePicker.getValue());
            m.setDescription(txtDescription.getText() != null ? txtDescription.getText().trim() : "");
            m.setStatut(comboStatut.getValue() != null ? comboStatut.getValue() : "planifie");
            m.setRecommandation(txtRecommandation.getText() != null ? txtRecommandation.getText().trim() : "");
            m.setPriorite(comboPriorite.getValue() != null ? comboPriorite.getValue() : "moyenne");
            m.setKilometrage(kilometrage);

            maintenanceService.ajouter(m);

            if (parentController != null) parentController.rafraichirTableau();

            showInfo("✅ Maintenance ajoutée",
                    "La maintenance a été enregistrée avec succès.\n\n"
                            + "📝 Récapitulatif :\n"
                            + "• Machine    : " + machineSelect.getNom() + "\n"
                            + "• Type panne : " + m.getTypePanne() + "\n"
                            + "• Coût       : " + String.format("%.2f DT", cout) + "\n"
                            + "• Date       : " + m.getDateMain() + "\n"
                            + "• Statut     : " + m.getStatut() + "\n"
                            + "• Priorité   : " + m.getPriorite() + "\n"
                            + "• Kilométrage: " + kilometrage + " km");
            fermer();

        } catch (SQLException e) {
            afficherBandeau("Erreur SQL : " + e.getMessage(), "error");
        } catch (Exception e) {
            afficherBandeau("Erreur inattendue : " + e.getMessage(), "error");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════
    private void fermer() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    public void setCurrentUser(Personne user)                              { this.currentUser = user; }
    public void setParentController(AgricoleMaintenanceController ctrl)    { this.parentController = ctrl; }
}