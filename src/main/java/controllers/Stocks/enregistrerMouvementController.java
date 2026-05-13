package controllers.Stocks;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Stocks.Article;
import models.Stocks.Mouvement;
import services.Stocks.MouvementService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class enregistrerMouvementController {

    @FXML private Label lblArticleNom;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField tfQuantite;
    @FXML private TextArea taMotif;

    private Article article;
    private final MouvementService mvtService = new MouvementService();
    private boolean success = false;

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList("ENTREE", "SORTIE"));
        cbType.getSelectionModel().selectFirst();
    }

    public void setArticle(Article article) {
        this.article = article;
        lblArticleNom.setText(article.getNom() + " (Stock actuel: " + article.getQuantiteEnStock() + " " + article.getUniteMesure() + ")");
    }

    public boolean isSuccess() {
        return success;
    }

    @FXML
    void handleAnnuler(ActionEvent event) {
        Stage stage = (Stage) lblArticleNom.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleValider(ActionEvent event) {
        if (validateur()) {
            try {
                Mouvement m = new Mouvement();
                m.setArticleId(article.getId());
                m.setType(cbType.getValue());
                m.setQuantite(Double.parseDouble(tfQuantite.getText()));
                m.setDateMouvement(LocalDateTime.now());
                m.setMotif(taMotif.getText());
                
                // On récupère l'utilisateur connecté
                if (SessionManager.getCurrentUser() != null) {
                    m.setIdUser(SessionManager.getCurrentUser().getCin());
                    if (SessionManager.getCurrentUser().getRole() == 3) {
                        m.setIdAdmin(SessionManager.getCurrentUser().getCin());
                    }
                } else {
                    m.setIdUser(0); // Valeur par défaut si pas de session (devrait pas arriver)
                }

                mvtService.ajouterMouvement(m);
                success = true;
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Mouvement enregistré avec succès !");
                alert.showAndWait();
                
                handleAnnuler(event);
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private boolean validateur() {
        try {
            double q = Double.parseDouble(tfQuantite.getText());
            if (q <= 0) {
                showError("La quantité doit être supérieure à 0.");
                return false;
            }
            
            if ("SORTIE".equals(cbType.getValue()) && q > article.getQuantiteEnStock()) {
                showError("Stock insuffisant ! Stock actuel : " + article.getQuantiteEnStock());
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Veuillez saisir une quantité valide.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }
}
