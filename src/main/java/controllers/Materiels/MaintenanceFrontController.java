package controllers.Materiels;

import controllers.User.MesTaches;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Materiels.Maintenance;
import models.User.Personne;
import services.Materiels.MaintenanceService;
import utils.MyDatabase;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class MaintenanceFrontController {

    // FXML Components
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private ComboBox<String> comboTypePanne;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<String> comboPriorite;

    @FXML private TilePane cardsContainer;

    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;
    @FXML private Label lblMachineCouteuse;
    @FXML private Label lblTypeDominant;
    @FXML private Label lblFiltres;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Label avatarDefaultLabel;
    @FXML private Circle avatarBg;

    private Personne currentUser;
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final Map<Integer, String> nomMachineMap = new HashMap<>();
    private final ObservableList<Maintenance> toutesMaintenances = FXCollections.observableArrayList();
    private final ObservableList<Maintenance> maintenancesFiltrees = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        chargerNomsMachines();
        chargerDonnees();
        configurerRecherche();
        chargerAvatarSidebar(SessionManager.getCurrentUser());
        setCurrentUser(SessionManager.getCurrentUser());
    }

    private void chargerNomsMachines() {
        nomMachineMap.clear();
        Connection conn = MyDatabase.getInstance().getConnection();
        String sql = "SELECT idM, nom FROM machine";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idM = rs.getInt("idM");
                String nom = rs.getString("nom");
                nomMachineMap.put(idM, (nom != null && !nom.isBlank()) ? nom : "Machine #" + idM);
            }
            System.out.println("✓ " + nomMachineMap.size() + " machine(s) chargée(s).");
        } catch (SQLException e) {
            System.err.println("Erreur chargement machines: " + e.getMessage());
        }
    }

    private String getNomMachine(int idM) {
        return nomMachineMap.getOrDefault(idM, "Machine #" + idM);
    }

    private void chargerDonnees() {
        try {
            List<Maintenance> liste = maintenanceService.recuperer();
            toutesMaintenances.setAll(liste);
            maintenancesFiltrees.setAll(liste);
            remplirCombosFiltres(liste);
            afficherCartes(maintenancesFiltrees);
            mettreAJourStatistiques(liste);

            if (lblFiltres != null) lblFiltres.setText(String.valueOf(liste.size()));
        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les maintenances: " + e.getMessage());
        }
    }

    private void remplirCombosFiltres(List<Maintenance> liste) {
        if (comboMachine != null) {
            List<String> noms = liste.stream()
                    .map(m -> getNomMachine(m.getIdM()))
                    .distinct().sorted().collect(Collectors.toList());
            noms.add(0, "Toutes les machines");
            comboMachine.setItems(FXCollections.observableArrayList(noms));
            comboMachine.setValue("Toutes les machines");
        }

        if (comboTypePanne != null) {
            List<String> types = liste.stream()
                    .map(Maintenance::getTypePanne)
                    .filter(Objects::nonNull)
                    .filter(t -> !t.isBlank())
                    .distinct().sorted().collect(Collectors.toList());
            types.add(0, "Tous les types");
            comboTypePanne.setItems(FXCollections.observableArrayList(types));
            comboTypePanne.setValue("Tous les types");
        }

        if (comboStatut != null) {
            List<String> statuts = liste.stream()
                    .map(Maintenance::getStatut)
                    .filter(Objects::nonNull)
                    .distinct().sorted().collect(Collectors.toList());
            statuts.add(0, "Tous les statuts");
            comboStatut.setItems(FXCollections.observableArrayList(statuts));
            comboStatut.setValue("Tous les statuts");
        }

        if (comboPriorite != null) {
            List<String> priorites = liste.stream()
                    .map(Maintenance::getPriorite)
                    .filter(Objects::nonNull)
                    .distinct().sorted().collect(Collectors.toList());
            priorites.add(0, "Toutes priorités");
            comboPriorite.setItems(FXCollections.observableArrayList(priorites));
            comboPriorite.setValue("Toutes priorités");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RECHERCHE ET FILTRES
    // ═══════════════════════════════════════════════════════════════════════

    private void configurerRecherche() {
        if (champRecherche != null) {
            champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
        }
    }

    @FXML
    private void appliquerFiltres() {
        String texte = champRecherche.getText() == null ? "" : champRecherche.getText().toLowerCase().trim();
        String machine = comboMachine != null ? comboMachine.getValue() : "Toutes les machines";
        String type = comboTypePanne != null ? comboTypePanne.getValue() : "Tous les types";
        String statut = comboStatut != null ? comboStatut.getValue() : "Tous les statuts";
        String priorite = comboPriorite != null ? comboPriorite.getValue() : "Toutes priorités";

        List<Maintenance> res = toutesMaintenances.stream()
                .filter(m -> {
                    if (!texte.isEmpty() && !correspondComplet(m, texte)) return false;
                    if (machine != null && !machine.equals("Toutes les machines")) {
                        if (!getNomMachine(m.getIdM()).equals(machine)) return false;
                    }
                    if (type != null && !type.equals("Tous les types")) {
                        if (m.getTypePanne() == null || !type.equals(m.getTypePanne())) return false;
                    }
                    if (statut != null && !statut.equals("Tous les statuts")) {
                        if (m.getStatut() == null || !statut.equals(m.getStatut())) return false;
                    }
                    if (priorite != null && !priorite.equals("Toutes priorités")) {
                        if (m.getPriorite() == null || !priorite.equals(m.getPriorite())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        maintenancesFiltrees.setAll(res);
        afficherCartes(res);
        mettreAJourStatistiques(res);

        if (lblFiltres != null) lblFiltres.setText(String.valueOf(res.size()));
    }

    private boolean correspondComplet(Maintenance m, String texte) {
        return getNomMachine(m.getIdM()).toLowerCase().contains(texte) ||
                (m.getTypePanne() != null && m.getTypePanne().toLowerCase().contains(texte)) ||
                (m.getDescription() != null && m.getDescription().toLowerCase().contains(texte)) ||
                (m.getDateMain() != null && m.getDateMain().toString().contains(texte)) ||
                String.valueOf(m.getCout()).contains(texte) ||
                (m.getStatut() != null && m.getStatut().toLowerCase().contains(texte)) ||
                (m.getPriorite() != null && m.getPriorite().toLowerCase().contains(texte)) ||
                (m.getRecommandation() != null && m.getRecommandation().toLowerCase().contains(texte)) ||
                String.valueOf(m.getKilometrage()).contains(texte);
    }

    @FXML
    private void effacerRecherche() {
        if (champRecherche != null) champRecherche.clear();
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        if (champRecherche != null) champRecherche.clear();
        if (comboMachine != null) comboMachine.setValue("Toutes les machines");
        if (comboTypePanne != null) comboTypePanne.setValue("Tous les types");
        if (comboStatut != null) comboStatut.setValue("Tous les statuts");
        if (comboPriorite != null) comboPriorite.setValue("Toutes priorités");

        maintenancesFiltrees.setAll(toutesMaintenances);
        afficherCartes(toutesMaintenances);
        mettreAJourStatistiques(toutesMaintenances);

        if (lblFiltres != null) lblFiltres.setText(String.valueOf(toutesMaintenances.size()));
    }

    @FXML
    private void actualiser() {
        chargerNomsMachines();
        chargerDonnees();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AFFICHAGE DES CARTES
    // ═══════════════════════════════════════════════════════════════════════

    private void afficherCartes(List<Maintenance> maintenances) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();

        if (maintenances.isEmpty()) {
            Label emptyLabel = new Label("Aucune maintenance trouvée");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-padding: 40;");
            cardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Maintenance m : maintenances) {
            VBox carte = creerCarteMaintenance(m);
            cardsContainer.getChildren().add(carte);
        }
    }

    private VBox creerCarteMaintenance(Maintenance m) {
        VBox carte = new VBox(10);
        carte.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        carte.setPrefWidth(380);

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String iconeType = getIconeTypePanne(m.getTypePanne());
        Label lblMachine = new Label(iconeType + " " + getNomMachine(m.getIdM()));
        lblMachine.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        Label lblType = new Label(m.getTypePanne() != null ? m.getTypePanne() : "Non spécifié");
        lblType.setStyle("-fx-background-color: #E8EAF6; -fx-padding: 4 10; " +
                "-fx-background-radius: 20; -fx-font-size: 12px; -fx-text-fill: #3949ab;");

        header.getChildren().addAll(lblMachine, lblType);

        // Statut + Priorité
        HBox statutPriorite = new HBox(10);
        statutPriorite.setAlignment(Pos.CENTER_LEFT);

        Label lblStatut = new Label(getStatutText(m.getStatut()));
        lblStatut.setStyle(getStatutStyle(m.getStatut()));

        Label lblPriorite = new Label(getPrioriteText(m.getPriorite()));
        lblPriorite.setStyle(getPrioriteStyle(m.getPriorite()));

        statutPriorite.getChildren().addAll(lblStatut, lblPriorite);

        // Date + Coût
        HBox dateCout = new HBox(15);
        dateCout.setAlignment(Pos.CENTER_LEFT);

        Label lblDate = new Label("📅 " + (m.getDateMain() != null ? m.getDateMain().format(DATE_FMT) : "N/A"));
        lblDate.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        Label lblCout = new Label("💰 " + String.format("%.2f", m.getCout()) + " DT");
        lblCout.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; " +
                (m.getCout() >= 500 ? "-fx-text-fill: #e74c3c;" :
                        (m.getCout() >= 200 ? "-fx-text-fill: #e67e22;" : "-fx-text-fill: #27ae60;")));

        dateCout.getChildren().addAll(lblDate, lblCout);

        // Kilométrage
        Label lblKilometrage = new Label("📊 Kilométrage: " + m.getKilometrage() + " km");
        lblKilometrage.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Description
        Label lblDescription = new Label("📝 " + (m.getDescription() != null ? m.getDescription() : "Aucune description"));
        lblDescription.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-wrap-text: true;");
        lblDescription.setMaxWidth(350);

        // Recommandation
        Label lblRecommandation = new Label("💡 " + (m.getRecommandation() != null ? m.getRecommandation() : "Aucune recommandation"));
        lblRecommandation.setStyle("-fx-font-size: 11px; -fx-text-fill: #2980b9; -fx-font-style: italic; -fx-wrap-text: true;");
        lblRecommandation.setMaxWidth(350);

        carte.getChildren().addAll(header, statutPriorite, dateCout, lblKilometrage, lblDescription, lblRecommandation);

        return carte;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATISTIQUES
    // ═══════════════════════════════════════════════════════════════════════

    private void mettreAJourStatistiques(List<Maintenance> liste) {
        int total = liste.size();
        if (lblTotal != null) lblTotal.setText(String.valueOf(total));

        if (total == 0) {
            if (lblCoutTotal != null) lblCoutTotal.setText("0,00 DT");
            if (lblCoutMoyen != null) lblCoutMoyen.setText("0,00 DT");
            if (lblMachineCouteuse != null) lblMachineCouteuse.setText("—");
            if (lblTypeDominant != null) lblTypeDominant.setText("—");
            return;
        }

        double coutTotal = liste.stream().mapToDouble(Maintenance::getCout).sum();
        double coutMoyen = coutTotal / total;

        if (lblCoutTotal != null) lblCoutTotal.setText(String.format("%.2f DT", coutTotal));
        if (lblCoutMoyen != null) lblCoutMoyen.setText(String.format("%.2f DT", coutMoyen));

        liste.stream()
                .collect(Collectors.groupingBy(
                        m -> getNomMachine(m.getIdM()),
                        Collectors.summingDouble(Maintenance::getCout)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> {
                    if (lblMachineCouteuse != null) {
                        lblMachineCouteuse.setText(e.getKey() + " (" + String.format("%.0f DT", e.getValue()) + ")");
                    }
                });

        liste.stream()
                .filter(m -> m.getTypePanne() != null && !m.getTypePanne().isBlank())
                .collect(Collectors.groupingBy(Maintenance::getTypePanne, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> {
                    if (lblTypeDominant != null) {
                        lblTypeDominant.setText(e.getKey() + " (" + e.getValue() + "x)");
                    }
                });
    }

    @FXML
    private void ouvrirStatistiquesDetaillees() {
        if (maintenancesFiltrees.isEmpty()) {
            afficherAlerte("Statistiques", "Aucune donnée disponible", Alert.AlertType.WARNING);
            return;
        }

        double totalCout = maintenancesFiltrees.stream().mapToDouble(Maintenance::getCout).sum();
        double coutMoyen = totalCout / maintenancesFiltrees.size();
        double coutMax = maintenancesFiltrees.stream().mapToDouble(Maintenance::getCout).max().orElse(0);
        double coutMin = maintenancesFiltrees.stream().mapToDouble(Maintenance::getCout).min().orElse(0);

        Map<String, Long> statsParStatut = maintenancesFiltrees.stream()
                .filter(m -> m.getStatut() != null)
                .collect(Collectors.groupingBy(Maintenance::getStatut, Collectors.counting()));

        Map<String, Long> statsParPriorite = maintenancesFiltrees.stream()
                .filter(m -> m.getPriorite() != null)
                .collect(Collectors.groupingBy(Maintenance::getPriorite, Collectors.counting()));

        Map<String, Long> statsParMachine = maintenancesFiltrees.stream()
                .collect(Collectors.groupingBy(m -> getNomMachine(m.getIdM()), Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("📊 STATISTIQUES DÉTAILLÉES DES MAINTENANCES\n\n");
        sb.append("═══════════════════════════════════════════\n\n");
        sb.append("📋 INFORMATIONS GÉNÉRALES\n");
        sb.append("• Nombre total : ").append(maintenancesFiltrees.size()).append("\n");
        sb.append("• Coût total : ").append(String.format("%.2f", totalCout)).append(" DT\n");
        sb.append("• Coût moyen : ").append(String.format("%.2f", coutMoyen)).append(" DT\n");
        sb.append("• Coût maximum : ").append(String.format("%.2f", coutMax)).append(" DT\n");
        sb.append("• Coût minimum : ").append(String.format("%.2f", coutMin)).append(" DT\n\n");

        sb.append("🏆 MACHINE LA PLUS COÛTEUSE\n");
        sb.append("• ").append(lblMachineCouteuse.getText()).append("\n\n");

        sb.append("🔧 TYPE DE PANNE DOMINANT\n");
        sb.append("• ").append(lblTypeDominant.getText()).append("\n\n");

        sb.append("📊 RÉPARTITION PAR STATUT\n");
        for (Map.Entry<String, Long> entry : statsParStatut.entrySet()) {
            double pourcentage = (entry.getValue() * 100.0) / maintenancesFiltrees.size();
            sb.append("• ").append(getStatutText(entry.getKey()))
                    .append(" : ").append(entry.getValue())
                    .append(" (").append(String.format("%.1f", pourcentage)).append("%)\n");
        }
        sb.append("\n");

        sb.append("⚠️ RÉPARTITION PAR PRIORITÉ\n");
        for (Map.Entry<String, Long> entry : statsParPriorite.entrySet()) {
            double pourcentage = (entry.getValue() * 100.0) / maintenancesFiltrees.size();
            sb.append("• ").append(getPrioriteText(entry.getKey()))
                    .append(" : ").append(entry.getValue())
                    .append(" (").append(String.format("%.1f", pourcentage)).append("%)\n");
        }
        sb.append("\n");

        sb.append("🏭 RÉPARTITION PAR MACHINE\n");
        for (Map.Entry<String, Long> entry : statsParMachine.entrySet()) {
            double pourcentage = (entry.getValue() * 100.0) / maintenancesFiltrees.size();
            sb.append("• ").append(entry.getKey())
                    .append(" : ").append(entry.getValue())
                    .append(" intervention(s) (").append(String.format("%.1f", pourcentage)).append("%)\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques détaillées");
        alert.setHeaderText("Rapport statistique des maintenances");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setPrefHeight(450);
        textArea.setPrefWidth(550);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPORT PDF
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    private void exporterPDF() {
        if (maintenancesFiltrees.isEmpty()) {
            afficherAlerte("Export PDF", "Aucune donnée à exporter", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("maintenances_" + LocalDate.now().format(DATE_FMT) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(cardsContainer.getScene().getWindow());

        if (file == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font plain = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            try {
                float yStart = PDRectangle.A4.getHeight() - 50;
                float yPosition = yStart;
                float margin = 50;
                float pageWidth = PDRectangle.A4.getWidth();

                // Titre
                cs.beginText();
                cs.setFont(bold, 18);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("RAPPORT DES MAINTENANCES");
                cs.endText();
                yPosition -= 25;

                // Sous-titre
                cs.beginText();
                cs.setFont(plain, 10);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText("Généré le : " + LocalDate.now().format(DATE_FMT));
                cs.endText();
                yPosition -= 20;

                // Statistiques
                double totalCout = maintenancesFiltrees.stream().mapToDouble(Maintenance::getCout).sum();
                String stats = "Total maintenances : " + maintenancesFiltrees.size() +
                        " | Coût total : " + String.format("%.2f", totalCout) + " DT" +
                        " | Coût moyen : " + String.format("%.2f", totalCout / maintenancesFiltrees.size()) + " DT";

                cs.beginText();
                cs.setFont(bold, 11);
                cs.newLineAtOffset(margin, yPosition);
                cs.showText(stats);
                cs.endText();
                yPosition -= 30;

                // Ligne séparatrice
                cs.setLineWidth(0.5f);
                cs.moveTo(margin, yPosition + 5);
                cs.lineTo(pageWidth - margin, yPosition + 5);
                cs.stroke();
                yPosition -= 15;

                // Liste des maintenances
                for (Maintenance m : maintenancesFiltrees) {
                    if (yPosition < 80) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        yPosition = yStart;
                    }

                    cs.beginText();
                    cs.setFont(bold, 12);
                    cs.newLineAtOffset(margin, yPosition);
                    cs.showText("🔧 " + getNomMachine(m.getIdM()) + " - " + (m.getTypePanne() != null ? m.getTypePanne() : "Type non spécifié"));
                    cs.endText();
                    yPosition -= 20;

                    cs.beginText();
                    cs.setFont(plain, 10);
                    cs.newLineAtOffset(margin + 10, yPosition);
                    cs.showText("Date: " + (m.getDateMain() != null ? m.getDateMain().format(DATE_FMT) : "N/A") +
                            " | Coût: " + String.format("%.2f", m.getCout()) + " DT" +
                            " | Statut: " + (m.getStatut() != null ? getStatutText(m.getStatut()) : "N/A") +
                            " | Priorité: " + (m.getPriorite() != null ? getPrioriteText(m.getPriorite()) : "N/A"));
                    cs.endText();
                    yPosition -= 15;

                    if (m.getDescription() != null && !m.getDescription().isEmpty()) {
                        cs.beginText();
                        cs.setFont(plain, 10);
                        cs.newLineAtOffset(margin + 10, yPosition);
                        String desc = m.getDescription().length() > 100 ? m.getDescription().substring(0, 97) + "..." : m.getDescription();
                        cs.showText("Description: " + desc);
                        cs.endText();
                        yPosition -= 15;
                    }

                    if (m.getRecommandation() != null && !m.getRecommandation().isEmpty()) {
                        cs.beginText();
                        cs.setFont(plain, 10);
                        cs.newLineAtOffset(margin + 10, yPosition);
                        String rec = m.getRecommandation().length() > 100 ? m.getRecommandation().substring(0, 97) + "..." : m.getRecommandation();
                        cs.showText("Recommandation: " + rec);
                        cs.endText();
                        yPosition -= 15;
                    }

                    yPosition -= 10;
                    cs.setLineWidth(0.3f);
                    cs.moveTo(margin, yPosition);
                    cs.lineTo(pageWidth - margin, yPosition);
                    cs.stroke();
                    yPosition -= 15;
                }

                cs.close();

            } catch (IOException e) {
                if (cs != null) {
                    try { cs.close(); } catch (IOException ex) {}
                }
                throw e;
            }

            doc.save(file);
            afficherAlerte("Export PDF", "Rapport PDF généré avec succès\n" + file.getName(), Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            afficherAlerte("Erreur PDF", "Erreur lors de la génération du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════

    private void navigateTo(MouseEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger la page", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDashboard(MouseEvent event) {
        navigateTo(event, "/UsersInterface/AcceuilEmp.fxml");
    }

    @FXML
    private void handleMesTaches(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/MesTaches.fxml"));
            Parent root = loader.load();
            MesTaches ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger Mes Tâches", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleMonProfil(MouseEvent event) {
        navigateTo(event, "/UsersInterface/ProfilEmplye.fxml");
    }

    @FXML
    private void ouvrirRotations(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/EmployeRotation.fxml");
    }

    @FXML
    private void handleMateriel(MouseEvent event) {
        actualiser();
    }

    @FXML
    private void handleEvenements(MouseEvent event) {
        navigateTo(event, "/G-Evenements/AfficherEvenementsEmp.fxml");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment vous déconnecter ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/UsersInterface/login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                afficherAlerte("Erreur", "Impossible de charger la page de connexion", Alert.AlertType.ERROR);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════

    private void chargerAvatarSidebar(Personne user) {
        if (user == null) return;

        if (userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank() || photoUrl.equals("0") || photoUrl.equals("null")) return;

        Circle clip = new Circle(32, 32, 32);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 64, 64, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        avatarDefaultLabel.setManaged(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Erreur chargement avatar: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            if (userNameLabel != null) userNameLabel.setText(this.currentUser.getPrenom() + " " + this.currentUser.getNom());
            if (userRoleLabel != null) userRoleLabel.setText("🌾 AGRICULTEUR");
            chargerAvatarSidebar(this.currentUser);
        }
    }

    private String getIconeTypePanne(String type) {
        if (type == null) return "🔧";
        if (type.toLowerCase().contains("electrique")) return "⚡";
        if (type.toLowerCase().contains("mecanique")) return "⚙️";
        if (type.toLowerCase().contains("hydraulique")) return "💧";
        if (type.toLowerCase().contains("pneumatique")) return "🌬️";
        if (type.toLowerCase().contains("moteur")) return "🔌";
        if (type.toLowerCase().contains("frein")) return "🛑";
        return "🔧";
    }

    private String getStatutText(String statut) {
        if (statut == null) return "❓ Inconnu";
        switch(statut.toLowerCase()) {
            case "en_cours": return "🟡 En cours";
            case "termine": return "✅ Terminé";
            case "planifie": return "📅 Planifié";
            default: return "❓ " + statut;
        }
    }

    private String getStatutStyle(String statut) {
        if (statut == null) return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
        switch(statut.toLowerCase()) {
            case "en_cours": return "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            case "termine": return "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            case "planifie": return "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            default: return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
        }
    }

    private String getPrioriteText(String priorite) {
        if (priorite == null) return "⚠️ Non définie";
        switch(priorite.toLowerCase()) {
            case "urgente": return "🔴 Urgente";
            case "haute": return "🟠 Haute";
            case "moyenne": return "🟡 Moyenne";
            case "faible": return "🟢 Faible";
            default: return "⚠️ " + priorite;
        }
    }

    private String getPrioriteStyle(String priorite) {
        if (priorite == null) return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
        switch(priorite.toLowerCase()) {
            case "urgente": return "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            case "haute": return "-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            case "moyenne": return "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            case "faible": return "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
            default: return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;";
        }
    }

    private void afficherErreur(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}