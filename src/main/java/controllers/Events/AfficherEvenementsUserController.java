package controllers.Events;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import models.Events.Evenement;
import services.Events.CategorieEvenementService;
import services.Events.EvenementService;
import services.Events.CalendarViewService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AfficherEvenementsUserController {

    @FXML private TableView<Evenement> eventsTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> typeColumn;
    @FXML private TableColumn<Evenement, Date> dateDebutColumn;
    @FXML private TableColumn<Evenement, Date> dateFinColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> categorieColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;
    @FXML private TextField filterLieu;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private Label resultsCountLabel;
    @FXML private Label userNameLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();

    private ObservableList<Evenement> evenements;
    private FilteredList<Evenement> filteredData;

    // ID de l'utilisateur connecté — à injecter depuis la page de connexion
    private int idUtilisateur = 1; // à remplacer par la session utilisateur réelle

    public void setIdUtilisateur(int id) {
        this.idUtilisateur = id;
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
            loadEvenements();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les evenements : " + e.getMessage());
        }
    }

    // ================= FILTRES =================
    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Planifie", "Annule", "Termine");
        filterStatut.setValue("Tous les statuts");

        try {
            List<CategorieEvenement> categories = categorieService.recuperer();
            filterCategorie.getItems().add("Toutes");
            for (CategorieEvenement cat : categories) {
                filterCategorie.getItems().add(cat.getNom_categorie());
            }
            filterCategorie.setValue("Toutes");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        filterStatut.setOnAction(e -> appliquerFiltres());
        filterCategorie.setOnAction(e -> appliquerFiltres());
        filterDateDebut.setOnAction(e -> appliquerFiltres());
        filterDateFin.setOnAction(e -> appliquerFiltres());
        filterLieu.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    // ================= COLONNES =================
    private void initColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : fmt.format(d.toLocalDate()));
            }
        });

        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : fmt.format(d.toLocalDate()));
            }
        });

        categorieColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(
                        categorieService.getNomCategorieById(cellData.getValue().getIdCategorie()));
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        // Statut avec couleurs
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setText(null); setStyle(""); return; }
                setText(statut);
                switch (statut.toLowerCase()) {
                    case "planifie": case "planifié":
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "termine": case "terminé":
                        setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "annule": case "annulé":
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    default: setStyle("");
                }
            }
        });

        // Colonne Action : bouton "S'inscrire" uniquement
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button inscrireBtn = new Button("S'inscrire");

            {
                inscrireBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-font-weight: bold;");

                inscrireBtn.setOnAction(e -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    // Bloquer l'inscription si evenement annulé ou terminé
                    String statut = evenement.getStatut() != null ? evenement.getStatut().toLowerCase() : "";
                    if (statut.equals("annulé") || statut.equals("annule") || statut.equals("terminé") || statut.equals("termine")) {
                        showWarning("Inscription impossible",
                                "Vous ne pouvez pas vous inscrire a un evenement " + evenement.getStatut() + ".");
                        return;
                    }
                    ouvrirInscription(evenement);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : inscrireBtn);
            }
        });
    }

    // ================= CHARGEMENT =================
    private void loadEvenements() throws SQLException {
        evenements = FXCollections.observableArrayList(evenementService.recuperer());
        filteredData = new FilteredList<>(evenements, e -> true);
        eventsTable.setItems(filteredData);
        updateResultsCount();
    }

    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    // ================= FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(ev ->
                matchesSearch(ev) && matchesDateDebut(ev) && matchesDateFin(ev)
                        && matchesLieu(ev) && matchesStatut(ev) && matchesCategorie(ev)
        );
        updateResultsCount();
    }

    private boolean matchesSearch(Evenement ev) {
        String t = searchField.getText();
        return t == null || t.trim().isEmpty() || ev.getTitre().toLowerCase().contains(t.toLowerCase());
    }
    private boolean matchesDateDebut(Evenement ev) {
        LocalDate f = filterDateDebut.getValue();
        return f == null || ev.getDateDebut().toLocalDate().equals(f);
    }
    private boolean matchesDateFin(Evenement ev) {
        LocalDate f = filterDateFin.getValue();
        return f == null || ev.getDateFin().toLocalDate().equals(f);
    }
    private boolean matchesLieu(Evenement ev) {
        String l = filterLieu.getText();
        return l == null || l.trim().isEmpty() || ev.getLieu().toLowerCase().contains(l.toLowerCase());
    }
    private boolean matchesStatut(Evenement ev) {
        String s = filterStatut.getValue();
        return s == null || s.equals("Tous les statuts") || ev.getStatut().equalsIgnoreCase(s);
    }
    private boolean matchesCategorie(Evenement ev) {
        String c = filterCategorie.getValue();
        if (c == null || c.equals("Toutes")) return true;
        try {
            return categorieService.getNomCategorieById(ev.getIdCategorie()).equals(c);
        } catch (SQLException e) { return true; }
    }

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
        resultsCountLabel.setText(filteredData.size() + " evenement(s) trouve(s)");
    }

    // ================= POP-UP INSCRIPTION =================
    private void ouvrirInscription(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/SInscrireEvenement.fxml"));
            Parent root = loader.load();

            SInscrireEvenementController controller = loader.getController();
            controller.setEvenement(evenement);
            controller.setIdUtilisateur(idUtilisateur);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(eventsTable.getScene().getWindow());
            popup.setResizable(false);
            popup.setTitle("S'inscrire a l'evenement");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            // Pas besoin de recharger les evenements ici (lecture seule)

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }


    // ================= OUTILS =================
    @FXML
    private void handleOpenGoogleCalendar(ActionEvent event) {
        try {
            CalendarViewService calendarService = new CalendarViewService();
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            calendarService.afficherCalendrier(stage);
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le calendrier : " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenCarte(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/G-Evenements/CarteEvenements.fxml");
            if (fxmlUrl == null) { showError("Erreur", "CarteEvenements.fxml introuvable."); return; }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage popup = new Stage();
            popup.setTitle("Carte des evenements");
            popup.setScene(new Scene(root, 600, 580));
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(eventsTable.getScene().getWindow());
            popup.setResizable(true);
            popup.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadEvenements();
            handleResetFilters(event);
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToEvenements(ActionEvent event) {
        // Deja sur cette page
    }

    @FXML
    private void goToMesParticipations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/AfficherParticipationsUser.fxml"));
            Parent root = loader.load();
            AfficherParticipationsUserController controller = loader.getController();
            controller.setIdUtilisateur(idUtilisateur);
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= ALERTS =================
    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}