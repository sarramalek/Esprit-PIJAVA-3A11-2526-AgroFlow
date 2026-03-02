package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Participation;
import services.EvenementService;
import services.ParticipationService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AfficherParticipationsUserController {

    @FXML private TableView<Participation> participationsTable;
    @FXML private TableColumn<Participation, String> evenementColumn;
    @FXML private TableColumn<Participation, LocalDate> dateInscriptionColumn;
    @FXML private TableColumn<Participation, String> statutColumn;
    @FXML private TableColumn<Participation, Boolean> presenceColumn;
    @FXML private TableColumn<Participation, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private DatePicker filterDate;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterPresence;
    @FXML private Label resultsCountLabel;
    @FXML private Label userNameLabel;

    // Stats
    @FXML private Label totalLabel;
    @FXML private Label confirmeLabel;
    @FXML private Label inscritLabel;
    @FXML private Label annuleLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();

    private ObservableList<Participation> participations;
    private FilteredList<Participation> filteredData;
    private int idUtilisateur = 1; // a injecter depuis la session

    public void setIdUtilisateur(int id) {
        this.idUtilisateur = id;
        try { loadParticipations(); } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setUserName(String name) {
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        initColumns();
        initFilters();
        try {
            loadParticipations();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les participations : " + e.getMessage());
        }
    }

    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Inscrit", "Confirme", "Annule");
        filterStatut.setValue("Tous les statuts");
        filterPresence.getItems().addAll("Toutes", "Oui", "Non");
        filterPresence.setValue("Toutes");

        filterStatut.setOnAction(e -> appliquerFiltres());
        filterPresence.setOnAction(e -> appliquerFiltres());
        filterDate.setOnAction(e -> appliquerFiltres());
    }

    // ================= COLONNES =================
    private void initColumns() {
        evenementColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(
                        evenementService.getNomEvenementById(cellData.getValue().getId_evenement()));
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        dateInscriptionColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : fmt.format(d));
            }
        });

        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut_participation"));
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setText(null); setStyle(""); return; }
                setText(statut);
                switch (statut.toLowerCase()) {
                    case "inscrit":
                        setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "confirme":
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "annule": case "annulé":
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    default: setStyle("");
                }
            }
        });

        presenceColumn.setCellValueFactory(new PropertyValueFactory<>("presence"));
        presenceColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean presence, boolean empty) {
                super.updateItem(presence, empty);
                if (empty || presence == null) { setText(null); setStyle(""); return; }
                setText(presence ? "Oui" : "Non");
                setStyle(presence ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
            }
        });

        addActionButtons();
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button annulerBtn = new Button("Annuler");
            private final HBox box = new HBox(8, modifierBtn, annulerBtn);

            {
                modifierBtn.setStyle("-fx-background-color: #546E7A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                annulerBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

                // ===== MODIFIER (seule la presence) =====
                modifierBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    // Interdire modification si deja annulee
                    if ("annule".equalsIgnoreCase(p.getStatut_participation()) ||
                            "annulé".equalsIgnoreCase(p.getStatut_participation())) {
                        showWarning("Action impossible", "Vous ne pouvez pas modifier une participation annulee.");
                        return;
                    }
                    ouvrirModification(p);
                });

                // ===== ANNULER LA PARTICIPATION =====
                annulerBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    if ("annule".equalsIgnoreCase(p.getStatut_participation()) ||
                            "annulé".equalsIgnoreCase(p.getStatut_participation())) {
                        showWarning("Deja annulee", "Cette participation est deja annulee.");
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmer l'annulation");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Voulez-vous vraiment annuler votre participation ?");
                    Optional<ButtonType> result = confirm.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            p.setStatut_participation("Annule");
                            participationService.modifier(p);
                            showSuccess("Annulation", "Votre participation a ete annulee.");
                            loadParticipations();
                            appliquerFiltres();
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible d'annuler : " + ex.getMessage());
                        }
                    }
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Participation p = getTableView().getItems().get(getIndex());
                // Griser les boutons si annulee
                boolean annulee = "annule".equalsIgnoreCase(p.getStatut_participation()) ||
                        "annulé".equalsIgnoreCase(p.getStatut_participation());
                modifierBtn.setDisable(annulee);
                annulerBtn.setDisable(annulee);
                setGraphic(box);
            }
        });
    }

    // ================= CHARGEMENT =================
    private void loadParticipations() throws SQLException {
        // Charger uniquement les participations de cet utilisateur
        // Si votre service a une methode par utilisateur, utilisez-la.
        // Sinon on filtre cote client (a adapter selon votre modele) :
        List<Participation> toutes = participationService.recuperer();
        // Filtrage par idUtilisateur si le modele le supporte :
        // List<Participation> miennes = toutes.stream()
        //     .filter(p -> p.getId_utilisateur() == idUtilisateur)
        //     .collect(Collectors.toList());
        // Pour l'instant, on affiche toutes (a adapter) :
        participations = FXCollections.observableArrayList(toutes);
        filteredData = new FilteredList<>(participations, p -> true);
        participationsTable.setItems(filteredData);
        updateStats();
        updateResultsCount();
    }

    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    // ================= FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(p ->
                matchesSearch(p) && matchesStatut(p) && matchesPresence(p) && matchesDate(p)
        );
        updateResultsCount();
    }

    private boolean matchesSearch(Participation p) {
        String t = searchField.getText();
        if (t == null || t.trim().isEmpty()) return true;
        try {
            return evenementService.getNomEvenementById(p.getId_evenement())
                    .toLowerCase().contains(t.toLowerCase());
        } catch (SQLException e) { return true; }
    }
    private boolean matchesStatut(Participation p) {
        String s = filterStatut.getValue();
        return s == null || s.equals("Tous les statuts") || p.getStatut_participation().equalsIgnoreCase(s);
    }
    private boolean matchesPresence(Participation p) {
        String pres = filterPresence.getValue();
        if (pres == null || pres.equals("Toutes")) return true;
        return p.isPresence() == pres.equals("Oui");
    }
    private boolean matchesDate(Participation p) {
        LocalDate f = filterDate.getValue();
        return f == null || p.getDate_inscription().equals(f);
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        filterDate.setValue(null);
        filterStatut.setValue("Tous les statuts");
        filterPresence.setValue("Toutes");
        appliquerFiltres();
    }

    // ================= STATS =================
    private void updateStats() {
        if (totalLabel == null) return;
        long total = participations.size();
        long confirme = participations.stream()
                .filter(p -> "confirme".equalsIgnoreCase(p.getStatut_participation()) ||
                        "confirme".equalsIgnoreCase(p.getStatut_participation())).count();
        long inscrit = participations.stream()
                .filter(p -> "inscrit".equalsIgnoreCase(p.getStatut_participation())).count();
        long annule = participations.stream()
                .filter(p -> "annule".equalsIgnoreCase(p.getStatut_participation()) ||
                        "annulé".equalsIgnoreCase(p.getStatut_participation())).count();

        totalLabel.setText(String.valueOf(total));
        confirmeLabel.setText(String.valueOf(confirme));
        inscritLabel.setText(String.valueOf(inscrit));
        annuleLabel.setText(String.valueOf(annule));
    }

    private void updateResultsCount() {
        resultsCountLabel.setText(filteredData.size() + " participation(s) trouvee(s)");
    }

    // ================= POP-UP MODIFICATION =================
    private void ouvrirModification(Participation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/ModifierParticipationUser.fxml"));
            Parent root = loader.load();

            ModifierParticipationUserController controller = loader.getController();
            controller.setParticipation(participation);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(participationsTable.getScene().getWindow());
            popup.setResizable(false);
            popup.setTitle("Modifier ma participation");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            // Recharger apres fermeture
            loadParticipations();
            appliquerFiltres();

        } catch (IOException | SQLException e) {
            showError("Erreur", "Impossible d'ouvrir la modification : " + e.getMessage());
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadParticipations();
            handleResetFilters(event);
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    @FXML
    private void goToEvenements(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/AfficherEvenementsUser.fxml"));
            Parent root = loader.load();
            AfficherEvenementsUserController controller = loader.getController();
            controller.setIdUtilisateur(idUtilisateur);
            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    @FXML
    private void goToMesParticipations(ActionEvent event) {
        // Deja sur cette page
    }

    // ================= ALERTS =================
    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
    private void showSuccess(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
    private void showWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}