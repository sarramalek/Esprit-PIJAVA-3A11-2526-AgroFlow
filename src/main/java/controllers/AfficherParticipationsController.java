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
import models.User;
import services.EvenementService;
import services.ParticipationService;
import services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AfficherParticipationsController {

    @FXML private TableView<Participation>              participationsTable;
    @FXML private TableColumn<Participation, String>    evenementColumn;
    @FXML private TableColumn<Participation, String>    idUserColumn;       // NOUVEAU
    @FXML private TableColumn<Participation, LocalDate> dateInscriptionColumn;
    @FXML private TableColumn<Participation, String>    statutColumn;
    @FXML private TableColumn<Participation, Boolean>   presenceColumn;
    @FXML private TableColumn<Participation, Void>      actionsColumn;

    // Filtres
    @FXML private TextField        searchField;
    @FXML private DatePicker       filterDate;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterPresence;
    @FXML private TextField        filterIdUser;        // NOUVEAU
    @FXML private Label            resultsCountLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService     evenementService     = new EvenementService();
    private final UserService          userService          = new UserService();

    private ObservableList<Participation> participations;
    private FilteredList<Participation>   filteredData;

    // Cache des utilisateurs — chargé une seule fois pour éviter N requêtes SQL
    private List<User> cachedUsers = new ArrayList<>();

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        chargerCacheUsers();
        initColumns();
        initFilters();

        try {
            loadParticipations();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les participations : " + e.getMessage());
        }
    }

    // ================= CACHE UTILISATEURS =================
    private void chargerCacheUsers() {
        try {
            cachedUsers = userService.recuperer();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrouver le nom d'un user depuis le cache par son ID
    private String getNomUserById(int idUser) {
        return cachedUsers.stream()
                .filter(u -> u.getId_user() == idUser)
                .findFirst()
                .map(u -> u.getNom() + " (#" + u.getId_user() + ")")
                .orElse("ID: " + idUser);
    }

    // ================= INITIALISATION DES FILTRES =================
    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Inscrit", "Confirmé", "Annulé");
        filterStatut.setValue("Tous les statuts");

        filterPresence.getItems().addAll("Toutes", "Oui", "Non");
        filterPresence.setValue("Toutes");

        filterStatut.setOnAction(e  -> appliquerFiltres());
        filterPresence.setOnAction(e -> appliquerFiltres());
        filterDate.setOnAction(e    -> appliquerFiltres());
        // filterIdUser est géré dans setupReactiveSearch()
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {

        // Colonne Événement
        evenementColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(
                        evenementService.getNomEvenementById(cellData.getValue().getId_evenement()));
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        // Colonne Utilisateur (NOUVEAU) — utilise le cache
        idUserColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getNomUserById(cellData.getValue().getId_user()))
        );

        // Colonne Date
        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        dateInscriptionColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date));
            }
        });

        // Colonne Statut avec couleurs
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut_participation"));
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setText(null); setStyle(""); return; }
                setText(statut);
                switch (statut.toLowerCase()) {
                    case "inscrit":
                        setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold; -fx-background-radius: 4;");
                        break;
                    case "confirmé":
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;");
                        break;
                    case "annulé":
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;");
                        break;
                    default:
                        setStyle("");
                }
            }
        });

        // Colonne Présence
        presenceColumn.setCellValueFactory(new PropertyValueFactory<>("presence"));
        presenceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean presence, boolean empty) {
                super.updateItem(presence, empty);
                if (empty || presence == null) { setText(null); setStyle(""); return; }
                setText(presence ? "✓ Oui" : "✗ Non");
                setStyle(presence
                        ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
            }
        });

        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadParticipations() throws SQLException {
        participations = FXCollections.observableArrayList(participationService.recuperer());
        filteredData   = new FilteredList<>(participations, p -> true);
        participationsTable.setItems(filteredData);
        updateResultsCount();
        System.out.println("✅ " + participations.size() + " participations chargées");
    }

    // ================= RECHERCHE RÉACTIVE =================
    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
        filterIdUser.textProperty().addListener((obs, old, nv) -> appliquerFiltres()); // NOUVEAU
    }

    // ================= APPLIQUER TOUS LES FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(p ->
                matchesSearchText(p)
                        && matchesStatutFilter(p)
                        && matchesPresenceFilter(p)
                        && matchesDateFilter(p)
                        && matchesIdUserFilter(p)   // NOUVEAU
        );
        updateResultsCount();
    }

    // ================= FILTRES INDIVIDUELS =================

    private boolean matchesSearchText(Participation p) {
        String t = searchField.getText();
        if (t == null || t.trim().isEmpty()) return true;
        try {
            return evenementService.getNomEvenementById(p.getId_evenement())
                    .toLowerCase().contains(t.toLowerCase());
        } catch (SQLException e) {
            return true;
        }
    }

    private boolean matchesStatutFilter(Participation p) {
        String s = filterStatut.getValue();
        if (s == null || s.equals("Tous les statuts")) return true;
        return p.getStatut_participation().equalsIgnoreCase(s);
    }

    private boolean matchesPresenceFilter(Participation p) {
        String pres = filterPresence.getValue();
        if (pres == null || pres.equals("Toutes")) return true;
        return p.isPresence() == pres.equals("Oui");
    }

    private boolean matchesDateFilter(Participation p) {
        LocalDate d = filterDate.getValue();
        if (d == null) return true;
        return p.getDate_inscription().equals(d);
    }

    // NOUVEAU : filtre par nom ou ID utilisateur
    private boolean matchesIdUserFilter(Participation p) {
        String f = filterIdUser.getText().trim().toLowerCase();
        if (f.isEmpty()) return true;
        // Chercher par ID exact
        if (String.valueOf(p.getId_user()).contains(f)) return true;
        // Chercher par nom
        return cachedUsers.stream()
                .filter(u -> u.getId_user() == p.getId_user())
                .anyMatch(u -> u.getNom().toLowerCase().contains(f));
    }

    // ================= RÉINITIALISER LES FILTRES =================
    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        filterDate.setValue(null);
        filterStatut.setValue("Tous les statuts");
        filterPresence.setValue("Toutes");
        filterIdUser.clear();   // NOUVEAU
        appliquerFiltres();
    }

    // ================= MISE À JOUR DU COMPTEUR =================
    private void updateResultsCount() {
        resultsCountLabel.setText(filteredData.size() + " participation(s) trouvée(s)");
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox   box       = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

                editBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    ouvrirPopup("ModifierParticipation.fxml", p);
                });

                deleteBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression de participation");
                    alert.setContentText("Voulez-vous vraiment supprimer cette participation ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            participationService.supprimer(p);
                            showSuccess("Succès", "Participation supprimée avec succès !");
                            loadParticipations();
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible de supprimer : " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ================= HANDLERS =================
    @FXML
    private void handleAddParticipation(ActionEvent event) {
        ouvrirPopup("AjouterParticipation.fxml", null);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            chargerCacheUsers(); // Recharger le cache aussi
            loadParticipations();
            handleResetFilters(event);
            showSuccess("Actualisation", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    @FXML
    private void goToAccueil(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/Accueil.fxml"));
            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= NAVIGATION — POP-UP MODAL =================
    private void ouvrirPopup(String fxml, Participation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(participationsTable.getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.setTitle(participation == null ? "Nouvelle Participation" : "Modifier la Participation");
            popupStage.setScene(new Scene(root));

            if (participation != null) {
                ModifierParticipationController controller = loader.getController();
                controller.setParticipation(participation);
            }

            popupStage.showAndWait(); // BLOQUANT : on reprend ici après fermeture du pop-up

            // Recharger cache + liste après fermeture
            chargerCacheUsers();
            loadParticipations();
            appliquerFiltres();

        } catch (IOException | SQLException e) {
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= ALERTS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}