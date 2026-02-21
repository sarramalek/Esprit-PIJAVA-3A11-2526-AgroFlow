package controllers;

import entities.animaux;
import entities.examens;
import entities.Sexe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.ServiceAnimal;
import services.ServiceExamen;
import services.PdfService;
import javafx.stage.Modality;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AfficherAnimauxController implements Initializable {

    @FXML private TableView<animaux> tableAnimaux;
    @FXML private TableColumn<animaux, String> colNom;
    @FXML private TableColumn<animaux, String> colEspece;
    @FXML private TableColumn<animaux, Float> colPoids;
    @FXML private TableColumn<animaux, Date> colDate;
    @FXML private TableColumn<animaux, Sexe> colSexe;
    @FXML private TextField filterField;
    @FXML private TableColumn<animaux, String> colAvatar;

    private ServiceAnimal service = new ServiceAnimal();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));

        // --- CONFIGURATION PIXABAY / IMAGE REELLE ---
        colAvatar.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colAvatar.setCellFactory(column -> {
            return new TableCell<animaux, String>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        animaux animal = getTableRow().getItem();
                        String espece = animal.getEspece().toLowerCase().trim();

                        // Traduction rapide pour l'API pour être sûr d'avoir les bonnes photos
                        String search = espece;
                        if(espece.contains("vache") || espece.contains("bovin")) search = "cow,farm";
                        if(espece.contains("poule") || espece.contains("volaille")) search = "chicken,hen";
                        if(espece.contains("chien")) search = "dog";
                        if(espece.contains("chat")) search = "cat";

                        // API Source Unsplash (recherche par mot-clé)
                        String imageUrl = "https://loremflickr.com/100/100/" + search + "/all";

                        Image img = new Image(imageUrl, 50, 50, true, true, true);
                        imageView.setImage(img);

                        // Style pour rendre l'image plus propre (optionnel)
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);

                        setGraphic(imageView);
                    }
                }
            };
        });
        refreshTable();
    }

    private void refreshTable() {
        try {
            List<animaux> list = service.afficher();
            ObservableList<animaux> observableList = FXCollections.observableArrayList(list);
            setupSearch(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupSearch(ObservableList<animaux> animalList) {
        FilteredList<animaux> filteredData = new FilteredList<>(animalList, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(animal -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return animal.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        animal.getEspece().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<animaux> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableAnimaux.comparatorProperty());
        tableAnimaux.setItems(sortedData);
    }

    @FXML
    void handleGenererPDF(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            try {
                ServiceExamen sEx = new ServiceExamen();
                List<examens> historique = sEx.afficher().stream()
                        .filter(e -> e.getId_animal() == selectionne.getId())
                        .collect(Collectors.toList());
                new PdfService().genererCarnetSante(selectionne, historique);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Le carnet de santé de " + selectionne.getNom() + " a été généré !");
                alert.show();
            } catch (SQLException e) { e.printStackTrace(); }
        } else { alerteSelection(); }
    }

    @FXML
    void handleGenererCouples(ActionEvent event) {
        animaux selection = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selection != null) {
            List<animaux> partenaires = service.trouverPartenaires(selection);
            if (partenaires.isEmpty()) {
                afficherAlerte("Aucun partenaire trouvé pour " + selection.getNom());
            } else {
                String liste = partenaires.stream()
                        .map(a -> a.getNom() + " (ID: " + a.getId() + ")")
                        .collect(Collectors.joining("\n"));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Partenaires Potentiels");
                alert.setContentText(liste);
                alert.show();
            }
        } else { afficherAlerte("Veuillez d'abord sélectionner un animal !"); }
    }

    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer " + selectionne.getNom() + " ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(selectionne.getId());
                    refreshTable();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        } else { alerteSelection(); }
    }

    @FXML
    void versModifier(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnimal.fxml"));
                Parent root = loader.load();
                ModifierAnimalController controller = loader.getController();
                controller.chargerDonnees(selectionne);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) { e.printStackTrace(); }
        } else { alerteSelection(); }
    }

    @FXML void versAjout(ActionEvent event) { changerScene(event, "ajoutAnimaux.fxml"); }

    @FXML
    void ouvrirStats(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/StatsAnimaux.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Statistiques - AgroFlow");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void ouvrirSuggestions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SuggestionFood.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Aide à l'alimentation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void changerScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { System.err.println("Erreur de navigation : " + e.getMessage()); }
    }

    @FXML void naviguerVersExamens(ActionEvent event) { changerScene(event, "AfficherExamens.fxml"); }
    @FXML void naviguerAnimaux(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerMateriels(ActionEvent event) { changerScene(event, "AfficherMateriels.fxml"); }
    @FXML void naviguerStocks(ActionEvent event) { changerScene(event, "AfficherStocks.fxml"); }
    @FXML void naviguerTerrains(ActionEvent event) { changerScene(event, "AfficherTerrains.fxml"); }
    @FXML void naviguerEvenements(ActionEvent event) { changerScene(event, "AfficherEvenements.fxml"); }
    @FXML void naviguerUsers(ActionEvent event) { changerScene(event, "AfficherUsers.fxml"); }

    private void alerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un animal.");
        alert.show();
    }
}