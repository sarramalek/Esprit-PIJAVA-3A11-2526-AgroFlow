package controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.CategorieEvenement;
import services.CategorieEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AfficherCategoriesController {

    @FXML
    private TableView<CategorieEvenement> tasksTable;

    @FXML
    private TableColumn<CategorieEvenement, String> titleColumn;

    @FXML
    private TableColumn<CategorieEvenement, String> descriptionColumn;

    @FXML
    private TableColumn<CategorieEvenement, Void> actionsColumn;

    @FXML
    private TextField searchField;

    private final CategorieEvenementService service = new CategorieEvenementService();
    private ObservableList<CategorieEvenement> categories;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        initColumns();
        try {
            loadCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_categorie"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description_categorie"));

        // ⚠️ LIGNE CRITIQUE (SINON TABLE VIDE)
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(null));

        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadCategories() throws SQLException {
        categories = FXCollections.observableArrayList(service.recuperer());
        tasksTable.setItems(categories);
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white;");

                editBtn.setOnAction(e -> {
                    CategorieEvenement c = getTableView().getItems().get(getIndex());
                    ouvrirPage("ModifierCategorie.fxml", c);  // Passe la catégorie
                });

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
                            loadCategories();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
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

    // ================= ADD CATEGORY =================
    @FXML
    private void handleAddCategorie(ActionEvent event) {
        ouvrirPage("AjouterCategorie.fxml", null);
    }

    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= SIDEBAR NAVIGATION =================
    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPageSimple("Accueil.fxml");
    }

    // ================= NAVIGATION METHODS =================
    private void ouvrirPage(String fxml, CategorieEvenement categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            if (categorie != null) {
                ModifierCategorieController controller = loader.getController();
                controller.setCategorie(categorie);  // ← Remplit les champs !
            }

            Stage stage = (Stage) tasksTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) tasksTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}