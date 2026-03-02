package controllers;

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
import services.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AfficherCategoriesController {

    @FXML private TableView<CategorieEvenement> tasksTable;
    @FXML private TableColumn<CategorieEvenement, String> titleColumn;
    @FXML private TableColumn<CategorieEvenement, String> descriptionColumn;
    @FXML private TableColumn<CategorieEvenement, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label resultsCountLabel;

    private final CategorieEvenementService service = new CategorieEvenementService();
    private ObservableList<CategorieEvenement> categories;
    private FilteredList<CategorieEvenement> filteredData;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        initColumns();
        try {
            loadCategories();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_categorie"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description_categorie"));
        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadCategories() throws SQLException {
        categories = FXCollections.observableArrayList(service.recuperer());
        filteredData = new FilteredList<>(categories, c -> true);
        tasksTable.setItems(filteredData);
        updateResultsCount();
        System.out.println("✅ " + categories.size() + " catégories chargées");
    }

    // ================= RECHERCHE RÉACTIVE =================
    private void setupReactiveSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            appliquerRecherche();
        });
    }

    // ================= APPLIQUER LA RECHERCHE =================
    private void appliquerRecherche() {
        String searchText = searchField.getText();

        if (searchText == null || searchText.trim().isEmpty()) {
            filteredData.setPredicate(c -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredData.setPredicate(categorie -> {
                String nomCategorie = categorie.getNom_categorie();
                if (nomCategorie == null) return false;
                return nomCategorie.toLowerCase().contains(lowerCaseFilter);
            });
        }

        updateResultsCount();
    }

    // ================= MISE À JOUR DU COMPTEUR =================
    private void updateResultsCount() {
        int count = filteredData.size();
        resultsCountLabel.setText(count + " catégorie(s) trouvée(s)");
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
                    CategorieEvenement c = getTableView().getItems().get(getIndex());
                    ouvrirPopup("ModifierCategorie.fxml", c);
                });

                // ===== SUPPRIMER (identique à l'original) =====
                deleteBtn.setOnAction(e -> {
                    CategorieEvenement c = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression de catégorie");
                    alert.setContentText("Voulez-vous supprimer cette catégorie ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            service.supprimer(c);
                            showSuccess("Succès", "Catégorie supprimée avec succès !");
                            loadCategories();
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

    // ================= ADD CATEGORY → ouvre un pop-up =================
    @FXML
    private void handleAddCategorie(ActionEvent event) {
        ouvrirPopup("AjouterCategorie.fxml", null);
    }

    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadCategories();
            searchField.clear();
            showSuccess("Actualisation", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    // ================= SIDEBAR NAVIGATION =================
    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPageSimple("Accueil.fxml");
    }

    // ================= POPUP MODAL =================
    private void ouvrirPopup(String fxml, CategorieEvenement categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(tasksTable.getScene().getWindow());
            popupStage.setResizable(false);
            popupStage.setTitle(categorie == null ? "Nouvelle Catégorie" : "Modifier la Catégorie");
            popupStage.setScene(new Scene(root));

            if (categorie != null) {
                ModifierCategorieController controller = loader.getController();
                controller.setCategorie(categorie);
            }

            popupStage.showAndWait(); // BLOQUANT : on reprend ici après fermeture du pop-up

            loadCategories();
            appliquerRecherche();

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
            Stage stage = (Stage) tasksTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= ALERT METHODS =================
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