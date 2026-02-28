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
import javafx.stage.Stage;
import models.Participation;
import services.EvenementService;
import services.ParticipationService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;

public class AfficherParticipationsController {

    @FXML private TableView<Participation> participationsTable;
    @FXML private TableColumn<Participation, String> evenementColumn;
    @FXML private TableColumn<Participation, LocalDate> dateInscriptionColumn;
    @FXML private TableColumn<Participation, String> statutColumn;
    @FXML private TableColumn<Participation, Boolean> presenceColumn;
    @FXML private TableColumn<Participation, Void> actionsColumn;

    // Filtres
    @FXML private TextField searchField;
    @FXML private DatePicker filterDate;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterPresence;
    @FXML private Label resultsCountLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();

    private ObservableList<Participation> participations;
    private FilteredList<Participation> filteredData;

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

    // ================= INITIALISATION DES FILTRES =================
    private void initFilters() {
        // Remplir le ComboBox Statut
        filterStatut.getItems().addAll("Tous les statuts", "Inscrit", "Confirmé", "Annulé");
        filterStatut.setValue("Tous les statuts");

        // Remplir le ComboBox Présence
        filterPresence.getItems().addAll("Toutes", "Oui", "Non");
        filterPresence.setValue("Toutes");

        // Ajouter des listeners pour filtrage automatique
        filterStatut.setOnAction(e -> appliquerFiltres());
        filterPresence.setOnAction(e -> appliquerFiltres());
        filterDate.setOnAction(e -> appliquerFiltres());
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        // Colonne Événement
        evenementColumn.setCellValueFactory(cellData -> {
            try {
                int idEvenement = cellData.getValue().getId_evenement();
                String titre = evenementService.getNomEvenementById(idEvenement);
                return new SimpleStringProperty(titre);
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("Erreur");
            }
        });

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
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
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
                    }
                }
            }
        });

        // Colonne Présence
        presenceColumn.setCellValueFactory(new PropertyValueFactory<>("presence"));
        presenceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean presence, boolean empty) {
                super.updateItem(presence, empty);
                if (empty || presence == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(presence ? "✓ Oui" : "✗ Non");
                    setStyle(presence
                            ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                            : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
                }
            }
        });

        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadParticipations() throws SQLException {
        participations = FXCollections.observableArrayList(participationService.recuperer());

        // Créer une FilteredList wrappant les données
        filteredData = new FilteredList<>(participations, p -> true);

        participationsTable.setItems(filteredData);
        updateResultsCount();

        System.out.println("✅ " + participations.size() + " participations chargées");
    }

    // ================= RECHERCHE RÉACTIVE =================
    private void setupReactiveSearch() {
        // Recherche en temps réel à chaque frappe
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            appliquerFiltres();
        });
    }

    // ================= APPLIQUER TOUS LES FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(participation -> {
            // Combinaison de tous les filtres
            return matchesSearchText(participation)
                    && matchesStatutFilter(participation)
                    && matchesPresenceFilter(participation)
                    && matchesDateFilter(participation);
        });

        updateResultsCount();
    }

    // ================= FILTRES INDIVIDUELS =================

    // Filtre par texte de recherche (événement)
    private boolean matchesSearchText(Participation participation) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }

        try {
            String nomEvenement = evenementService.getNomEvenementById(participation.getId_evenement());
            String lowerCaseFilter = searchText.toLowerCase();
            return nomEvenement.toLowerCase().contains(lowerCaseFilter);
        } catch (SQLException e) {
            return true;
        }
    }

    // Filtre par statut
    private boolean matchesStatutFilter(Participation participation) {
        String statut = filterStatut.getValue();
        if (statut == null || statut.equals("Tous les statuts")) {
            return true;
        }
        return participation.getStatut_participation().equalsIgnoreCase(statut);
    }

    // Filtre par présence
    private boolean matchesPresenceFilter(Participation participation) {
        String presence = filterPresence.getValue();
        if (presence == null || presence.equals("Toutes")) {
            return true;
        }
        boolean isPresent = presence.equals("Oui");
        return participation.isPresence() == isPresent;
    }

    // Filtre par date d'inscription exacte
    private boolean matchesDateFilter(Participation participation) {
        LocalDate dateInscription = participation.getDate_inscription();
        LocalDate dateFiltre = filterDate.getValue();

        // Si aucune date sélectionnée, pas de filtre
        if (dateFiltre == null) {
            return true;
        }

        // Vérifier si la date d'inscription correspond exactement à la date filtrée
        return dateInscription.equals(dateFiltre);
    }

    // ================= RÉINITIALISER LES FILTRES =================
    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        filterDate.setValue(null);
        filterStatut.setValue("Tous les statuts");
        filterPresence.setValue("Toutes");
        appliquerFiltres();
    }

    // ================= MISE À JOUR DU COMPTEUR =================
    private void updateResultsCount() {
        int count = filteredData.size();
        resultsCountLabel.setText(count + " participation(s) trouvée(s)");
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    ouvrirPage("ModifierParticipation.fxml", participation);
                });

                deleteBtn.setOnAction(e -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression de participation");
                    alert.setContentText("Voulez-vous vraiment supprimer cette participation ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            participationService.supprimer(participation);
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
        ouvrirPage("AjouterParticipation.fxml", null);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadParticipations();
            handleResetFilters(event);
            showSuccess("Actualisation", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPageSimple("Accueil.fxml");
    }

    // ================= NAVIGATION =================
    private void ouvrirPage(String fxml, Participation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            if (participation != null) {
                ModifierParticipationController controller = loader.getController();
                controller.setParticipation(participation);
            }

            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
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