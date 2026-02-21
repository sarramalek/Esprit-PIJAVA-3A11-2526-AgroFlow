package controllers;

import java.sql.SQLException;
import java.time.LocalDate;
import entities.examens;
import entities.animaux;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import services.ServiceExamen;
import services.ServiceAnimal;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class AfficherExamensController {

    @FXML private TableView<examens> tvExamens;
    @FXML private TableColumn<examens, String> colAnimal;
    @FXML private TableColumn<examens, String> colType;
    @FXML private TableColumn<examens, java.sql.Date> colDate;
    @FXML private TableColumn<examens, String> colDiagnostic;
    @FXML private TableColumn<examens, String> colTraitement;

    // NOUVELLES COLONNES SÉPARÉES
    @FXML private TableColumn<examens, String> colTraduction;
    @FXML private TableColumn<examens, String> colConseils;

    @FXML private Circle badgeRouge;
    @FXML private Label lblNbAlertes;
    @FXML private TextField filterType;
    @FXML private DatePicker filterDate;

    private ObservableList<examens> masterData = FXCollections.observableArrayList();
    private FilteredList<examens> filteredData;
    private ServiceExamen service = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerDonnees();

        filteredData = new FilteredList<>(masterData, p -> true);
        filterType.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
        filterDate.valueProperty().addListener((obs, old, nv) -> appliquerFiltres());
        tvExamens.setItems(filteredData);

        demarrerSystemeAlerte();
    }

    private void configurerColonnes() {
        colAnimal.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getId_animal();
            try {
                return new SimpleStringProperty(serviceAn.afficher().stream()
                        .filter(a -> a.getId() == id).findFirst().map(animaux::getNom).orElse("Inconnu"));
            } catch (SQLException e) { return new SimpleStringProperty("Erreur"); }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type_examen"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_examen"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        colTraitement.setCellValueFactory(new PropertyValueFactory<>("traitement"));

        // --- COLONNE 1 : TRADUCTION (API) ---
        colTraduction.setCellFactory(column -> new TableCell<examens, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    String diag = getTableRow().getItem().getDiagnostic();
                    if (diag == null || diag.isEmpty()) {
                        setText("-");
                    } else {
                        Task<String> task = new Task<String>() {
                            @Override protected String call() throws Exception {
                                String query = diag.replace(" ", "%20");
                                URL url = new URL("https://api.mymemory.translated.net/get?q=" + query + "&langpair=fr|en");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                try (Scanner s = new Scanner(conn.getInputStream())) {
                                    String response = s.useDelimiter("\\A").next();
                                    return response.split("\"translatedText\":\"")[1].split("\"")[0];
                                }
                            }
                        };
                        task.setOnSucceeded(e -> setText("🇬🇧 " + task.getValue()));
                        new Thread(task).start();
                    }
                }
            }
        });

        // --- COLONNE 2 : CONSEILS MÉDICAUX (LOGIQUE LOCALE) ---
        colConseils.setCellFactory(column -> new TableCell<examens, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String diag = getTableRow().getItem().getDiagnostic().toLowerCase();
                    if (diag.contains("infection")) {
                        setText("⚠️ Isoler l'animal");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (diag.contains("urgence") || diag.contains("fracture")) {
                        setText("🚨 Rappel Vétérinaire");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setText("✅ Suivi normal");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: normal;");
                    }
                }
            }
        });
    }

    private void chargerDonnees() {
        try { masterData.setAll(service.afficher()); } catch (Exception e) { e.printStackTrace(); }
    }

    private void appliquerFiltres() {
        filteredData.setPredicate(ex -> {
            boolean typeMatch = filterType.getText() == null || filterType.getText().isEmpty() ||
                    ex.getType_examen().toLowerCase().contains(filterType.getText().toLowerCase());
            boolean dateMatch = filterDate.getValue() == null ||
                    ((java.sql.Date) ex.getDate_examen()).toLocalDate().equals(filterDate.getValue());
            return typeMatch && dateMatch;
        });
    }

    private void demarrerSystemeAlerte() {
        verifierRappelsAujourdhui();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> verifierRappelsAujourdhui()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void verifierRappelsAujourdhui() {
        LocalDate today = LocalDate.now();
        long nb = masterData.stream()
                .filter(e -> e.getDate_examen() != null && ((java.sql.Date) e.getDate_examen()).toLocalDate().equals(today))
                .count();

        if (nb > 0) {
            if(badgeRouge != null) badgeRouge.setVisible(true);
            if(lblNbAlertes != null) {
                lblNbAlertes.setVisible(true);
                lblNbAlertes.setText(String.valueOf(nb));
            }
        } else {
            if(badgeRouge != null) badgeRouge.setVisible(false);
            if(lblNbAlertes != null) lblNbAlertes.setVisible(false);
        }
    }

    @FXML void ouvrirDetailsAlertes() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rappels du jour");
        alert.setHeaderText("Examens à effectuer aujourd'hui (" + LocalDate.now() + ")");
        long nb = masterData.stream()
                .filter(e -> e.getDate_examen() != null && ((java.sql.Date) e.getDate_examen()).toLocalDate().equals(LocalDate.now()))
                .count();
        alert.setContentText("Vous avez " + nb + " examen(s) prévu(s).");
        alert.show();
    }

    @FXML void handleSupprimer(ActionEvent event) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null && new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ?").showAndWait().get() == ButtonType.OK) {
            try { service.supprimer(selection.getId()); chargerDonnees(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML void handleModifier(ActionEvent event) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null) changerScene(event, "/ModifierExamen.fxml", selection);
    }

    @FXML void naviguerAjout(ActionEvent event) { changerScene(event, "/AjoutExamen.fxml", null); }
    @FXML void naviguerVersAnimaux(ActionEvent event) { changerScene(event, "/AfficherAnimaux.fxml", null); }
    @FXML void reinitialiserFiltres() { filterType.clear(); filterDate.setValue(null); }
    @FXML void ouvrirStats() { /* Logique stats */ }
    @FXML void handleDeconnexion(ActionEvent event) { changerScene(event, "/Login.fxml", null); }

    private void changerScene(ActionEvent event, String fxml, examens ex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void traduireDiagnostics(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mode International");
        alert.setHeaderText("Traduction activée");
        alert.setContentText("La colonne de traduction est mise à jour automatiquement via l'API MyMemory.");
        alert.show();
    }

    @FXML
    void afficherConseilsSante(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aide au Diagnostic");
        alert.setHeaderText("Analyse intelligente");
        alert.setContentText("Les conseils de biosécurité s'affichent dynamiquement selon les mots-clés de votre diagnostic.");
        alert.show();
    }
}