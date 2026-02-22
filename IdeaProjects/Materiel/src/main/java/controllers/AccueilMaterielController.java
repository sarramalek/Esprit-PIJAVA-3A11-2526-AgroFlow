package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class AccueilMaterielController {

    // ════════════════════════════════════════════════════════════════════════
    //  CARTES CENTRALES — MouseEvent (clic sur les VBox)
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void ouvrirMachines(MouseEvent event) {
        naviguerDepuisNode("/AffichageMachine.fxml", (Node) event.getSource());
    }

    @FXML
    private void ouvrirMaintenances(MouseEvent event) {
        naviguerDepuisNode("/AfficherMaintenances.fxml", (Node) event.getSource());
    }

    @FXML
    private void ouvrirAchats(MouseEvent event) {
        naviguerDepuisNode("/AfficherAchats.fxml", (Node) event.getSource());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR — onAction des Button
    //
    //  ✅ CORRECTION : Les méthodes onAction peuvent être SANS paramètre.
    //     JavaFX accepte les deux signatures :
    //       void methode()                        ← sans paramètre ✅
    //       void methode(ActionEvent event)       ← avec paramètre ✅
    //     L'ancienne version passait ActionEvent mais récupérait le stage
    //     depuis event.getSource() — cela fonctionne aussi.
    //     Ici on utilise la version SANS paramètre + Window.getWindows()
    //     pour rester simple et compatible avec tous les FXML.
    // ════════════════════════════════════════════════════════════════════════

    @FXML private void naviguerAnimaux()    { naviguerVers("/AfficherAnimaux.fxml");   }
    @FXML private void naviguerMateriels()  { naviguerVers("/AccueilMateriel.fxml");   }
    @FXML private void naviguerStocks()     { naviguerVers("/AfficherStocks.fxml");    }
    @FXML private void naviguerTerrains()   { naviguerVers("/AfficherTerrains.fxml");  }
    @FXML private void naviguerEvenements() { naviguerVers("/AccueilEvenement.fxml");  }
    @FXML private void naviguerUsers()      { naviguerVers("/AfficherUsers.fxml");     }
    @FXML private void deconnexion()        { naviguerVers("/Login.fxml");             }

    // ════════════════════════════════════════════════════════════════════════
    //  MÉTHODES DE NAVIGATION
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Navigation depuis une carte (MouseEvent → Node source disponible).
     * Le Node source permet de remonter directement à la Stage.
     */
    private void naviguerDepuisNode(String fxmlPath, Node source) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage  stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur",
                    "Impossible de naviguer vers : " + fxmlPath + "\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            afficherAlerte("Erreur",
                    "Erreur lors de la navigation : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Navigation depuis la sidebar (pas de Node source direct).
     * Récupère la Stage active via Window.getWindows().
     */
    private void naviguerVers(String fxmlPath) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage  stage = getStageActive();
            if (stage == null) {
                afficherAlerte("Erreur", "Fenêtre introuvable.", Alert.AlertType.ERROR);
                return;
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur",
                    "Impossible de naviguer vers : " + fxmlPath + "\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            afficherAlerte("Erreur",
                    "Erreur lors de la navigation : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Retourne la première Stage affichée parmi les fenêtres JavaFX ouvertes.
     */
    private Stage getStageActive() {
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w instanceof Stage && w.isShowing()) {
                return (Stage) w;
            }
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRE
    // ════════════════════════════════════════════════════════════════════════

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}