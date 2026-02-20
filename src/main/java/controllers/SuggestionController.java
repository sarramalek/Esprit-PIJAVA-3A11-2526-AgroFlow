package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.FoodApiService;
import java.util.List;

public class SuggestionController {
    @FXML private ComboBox<String> comboEspecePop;
    @FXML private ListView<String> lvSuggestionsPop;

    private FoodApiService foodApi = new FoodApiService();

    @FXML
    public void initialize() {
        comboEspecePop.setItems(FXCollections.observableArrayList("Chien", "Chat", "Vache", "Chèvre", "Mouton"));

        // Lancer la recherche dès qu'on change l'espèce dans la pop-up
        comboEspecePop.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                new Thread(() -> {
                    List<String> results = foodApi.chercherAliments(newV);
                    Platform.runLater(() -> lvSuggestionsPop.setItems(FXCollections.observableArrayList(results)));
                }).start();
            }
        });
    }

    @FXML void fermer() {
        ((Stage) lvSuggestionsPop.getScene().getWindow()).close();
    }
}