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
import models.CategorieEvenement;
import models.Evenement;
import services.CategorieEvenementService;
import services.EvenementService;
import utils.CalendarViewService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AfficherEvenementsController {

    @FXML private TableView<Evenement> eventsTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> typeColumn;
    @FXML private TableColumn<Evenement, Date> dateDebutColumn;
    @FXML private TableColumn<Evenement, Date> dateFinColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> categorieColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;

    // Filtres
    @FXML private TextField searchField;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;
    @FXML private TextField filterLieu;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private Label resultsCountLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();

    private ObservableList<Evenement> evenements;
    private FilteredList<Evenement> filteredData;
    private final Map<String, Integer> categoriesMap = new HashMap<>();

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        initColumns();
        initFilters();

        try {
            loadEvenements();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    // ================= INITIALISATION DES FILTRES (identique à l'original) =================
    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Planifié", "Annulé", "Terminé");
        filterStatut.setValue("Tous les statuts");

        try {
            List<CategorieEvenement> categories = categorieService.recuperer();
            filterCategorie.getItems().add("Toutes");
            for (CategorieEvenement cat : categories) {
                String nom = cat.getNom_categorie();
                filterCategorie.getItems().add(nom);
                categoriesMap.put(nom, cat.getId_categorie());
            }
            filterCategorie.setValue("Toutes");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        filterStatut.setOnAction(e -> appliquerFiltres());
        filterCategorie.setOnAction(e -> appliquerFiltres());
        filterDateDebut.setOnAction(e -> appliquerFiltres());
        filterDateFin.setOnAction(e -> appliquerFiltres());
        filterLieu.textProperty().addListener((obs, old, newVal) -> appliquerFiltres());
    }

    // ================= TABLE COLUMNS (identique à l'original) =================
    private void initColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date.toLocalDate()));
            }
        });

        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date.toLocalDate()));
            }
        });

        categorieColumn.setCellValueFactory(cellData -> {
            try {
                int idCategorie = cellData.getValue().getIdCategorie();
                String nomCategorie = categorieService.getNomCategorieById(idCategorie);
                return new SimpleStringProperty(nomCategorie);
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

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
                        case "planifié":
                            setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;");
                            break;
                        case "terminé":
                            setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-font-weight: bold; -fx-background-radius: 4;");
                            break;
                        case "annulé":
                            setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;");
                            break;
                    }
                }
            }
        });

        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadEvenements() throws SQLException {
        evenements = FXCollections.observableArrayList(evenementService.recuperer());
        filteredData = new FilteredList<>(evenements, e -> true);
        eventsTable.setItems(filteredData);
        updateResultsCount();
    }

    // ================= RECHERCHE RÉACTIVE =================
    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> appliquerFiltres());
    }

    // ================= APPLIQUER TOUS LES FILTRES (identique à l'original) =================
    private void appliquerFiltres() {
        filteredData.setPredicate(evenement ->
                matchesSearchText(evenement)
                        && matchesDateDebutFilter(evenement)
                        && matchesDateFinFilter(evenement)
                        && matchesLieuFilter(evenement)
                        && matchesStatutFilter(evenement)
                        && matchesCategorieFilter(evenement)
        );
        updateResultsCount();
    }

    private boolean matchesSearchText(Evenement evenement) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) return true;
        return evenement.getTitre().toLowerCase().contains(searchText.toLowerCase());
    }

    private boolean matchesDateDebutFilter(Evenement evenement) {
        LocalDate dateFiltre = filterDateDebut.getValue();
        if (dateFiltre == null) return true;
        return evenement.getDateDebut().toLocalDate().equals(dateFiltre);
    }

    private boolean matchesDateFinFilter(Evenement evenement) {
        LocalDate dateFiltre = filterDateFin.getValue();
        if (dateFiltre == null) return true;
        return evenement.getDateFin().toLocalDate().equals(dateFiltre);
    }

    private boolean matchesLieuFilter(Evenement evenement) {
        String lieuFiltre = filterLieu.getText();
        if (lieuFiltre == null || lieuFiltre.trim().isEmpty()) return true;
        return evenement.getLieu().toLowerCase().contains(lieuFiltre.toLowerCase());
    }

    private boolean matchesStatutFilter(Evenement evenement) {
        String statut = filterStatut.getValue();
        if (statut == null || statut.equals("Tous les statuts")) return true;
        return evenement.getStatut().equalsIgnoreCase(statut);
    }

    private boolean matchesCategorieFilter(Evenement evenement) {
        String categorie = filterCategorie.getValue();
        if (categorie == null || categorie.equals("Toutes")) return true;
        try {
            String nomCategorie = categorieService.getNomCategorieById(evenement.getIdCategorie());
            return nomCategorie.equals(categorie);
        } catch (SQLException e) {
            return true;
        }
    }

    // ================= RÉINITIALISER LES FILTRES (identique à l'original) =================
    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);
        filterLieu.clear();
        filterStatut.setValue("Tous les statuts");
        filterCategorie.setValue("Toutes");
        appliquerFiltres();
    }

    private void updateResultsCount() {
        int count = filteredData.size();
        resultsCountLabel.setText(count + " événement(s) trouvé(s)");
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

                // ===== MODIFIER → ouvre un pop-up =====
                editBtn.setOnAction(e -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    ouvrirPopup("ModifierEvenement.fxml", evenement);
                });

                // ===== SUPPRIMER (identique à l'original) =====
                deleteBtn.setOnAction(e -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression d'événement");
                    alert.setContentText("Voulez-vous supprimer l'événement \"" + evenement.getTitre() + "\" ?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            evenementService.supprimer(evenement);
                            showSuccess("Succès", "Événement supprimé !");
                            loadEvenements();
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

    // Ajouter → ouvre un pop-up
    @FXML
    private void handleAddEvenement(ActionEvent event) {
        ouvrirPopup("AjouterEvenement.fxml", null);
    }

    // Calendrier, Carte, Générateur d'idées → identiques à l'original
    @FXML
    private void handleOpenGoogleCalendar(ActionEvent event) {
        try {
            CalendarViewService calendarService = new CalendarViewService();
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            calendarService.afficherCalendrier(stage);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le calendrier : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadEvenements();
            handleResetFilters(event);
            showSuccess("Actualisation", "Liste actualisée !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPageSimple("Accueil.fxml");
    }

    @FXML
    private void handleOpenCarte(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/G-Evenements/CarteEvenements.fxml");
            if (fxmlUrl == null) {
                showError("Erreur", "Fichier CarteEvenements.fxml introuvable.\n"
                        + "Vérifiez qu'il est dans : resources/G-Evenements/");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("🗺️ Carte des événements — Tunisie");
            popupStage.setScene(new Scene(root, 900, 580));
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(eventsTable.getScene().getWindow());
            popupStage.setResizable(true);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateIdeas(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/G-Evenements/GenerateurIdees.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("GenerateurIdees.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getClassLoader().getResource("G-Evenements/GenerateurIdees.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getClassLoader().getResource("GenerateurIdees.fxml");

            if (fxmlUrl == null) {
                showError("Erreur - Fichier introuvable",
                        "GenerateurIdees.fxml est introuvable.\n\n"
                                + "Vérifiez que le fichier est bien dans :\n"
                                + "  src/main/resources/G-Evenements/GenerateurIdees.fxml\n\n"
                                + "Et que Maven/IntelliJ a bien copié les resources dans target.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("💡 Générateur d'idées d'événements IA");
            popupStage.setScene(new Scene(root, 680, 620));
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(eventsTable.getScene().getWindow());
            popupStage.setResizable(true);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le générateur d'idées : " + e.getMessage());
        }
    }

    // ================= POPUP MODAL =================
    private void ouvrirPopup(String fxml, Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(eventsTable.getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.setTitle(evenement == null ? "Nouvel Événement" : "Modifier l'Événement");
            popupStage.setScene(new Scene(root));

            if (evenement != null) {
                ModifierEvenementController controller = loader.getController();
                controller.setEvenement(evenement);
            } else {
                AjouterEvenementController controller = loader.getController();
            }

            popupStage.showAndWait(); // BLOQUANT : on reprend ici après fermeture du pop-up

            loadEvenements();
            appliquerFiltres();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= NAVIGATION SIMPLE =================
    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger : " + e.getMessage());
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