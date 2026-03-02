package controllers.Events;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.Events.IdeesEvenementsAIService;  // ✅ package utils, cohérent avec la structure projet

/**
 * Contrôleur de la popup "Générateur d'idées d'événements IA".
 */
public class GenerateurIdeesController {

    @FXML private ComboBox<String> saisonCombo;
    @FXML private ComboBox<String> typeExploitCombo;
    @FXML private Spinner<Integer> nbIdeesSpinner;
    @FXML private Button genererBtn;
    @FXML private Button fermerBtn;
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;
    @FXML private HBox loadingBox;

    private final IdeesEvenementsAIService aiService = new IdeesEvenementsAIService();

    @FXML
    public void initialize() {
        // Saisons
        saisonCombo.getItems().addAll("Printemps", "Été", "Automne", "Hiver", "Toute saison");
        saisonCombo.setValue("Toute saison");

        // Types d'exploitation
        typeExploitCombo.getItems().addAll(
                "Polyculture-élevage",
                "Maraîchage",
                "Céréales",
                "Arboriculture fruitière",
                "Élevage bovin / ovin",
                "Viticulture",
                "Agriculture biologique",
                "Apiculture"
        );
        typeExploitCombo.setValue("Polyculture-élevage");

        // Spinner nombre d'idées (1 à 10, défaut 5)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5);
        nbIdeesSpinner.setValueFactory(valueFactory);

        // État initial
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);
        resultArea.setWrapText(true);
        resultArea.setEditable(false);
        statusLabel.setText("Configurez les paramètres et cliquez sur Générer.");
    }

    @FXML
    private void handleGenerer() {
        String saison     = saisonCombo.getValue();
        String typeExploit = typeExploitCombo.getValue();
        int    nbIdees    = nbIdeesSpinner.getValue();

        setLoading(true);
        resultArea.clear();
        statusLabel.setText("🤖 L'IA génère vos idées, veuillez patienter...");

        // Appel API en thread séparé pour ne pas bloquer l'UI JavaFX
        Thread thread = new Thread(() -> {
            try {
                String idees = aiService.genererIdees(saison, typeExploit, nbIdees);

                Platform.runLater(() -> {
                    resultArea.setText(idees);
                    statusLabel.setText("✅ " + nbIdees + " idée(s) générée(s) avec succès !");
                    setLoading(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultArea.setText(
                            "❌ Erreur lors de la génération :\n" + e.getMessage()
                                    + "\n\n💡 Vérifiez :\n"
                                    + "  • Votre clé API dans IdeesEvenementsAIService.java (ligne API_KEY)\n"
                                    + "  • Votre connexion internet"
                    );
                    statusLabel.setText("⚠️ Erreur — voir détails dans la zone de résultat.");
                    setLoading(false);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCopier() {
        String content = resultArea.getText();
        if (content != null && !content.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            clipboard.setContent(clipboardContent);
            statusLabel.setText("📋 Idées copiées dans le presse-papiers !");
        } else {
            statusLabel.setText("⚠️ Rien à copier — générez d'abord des idées.");
        }
    }

    @FXML
    private void handleFermer() {
        Stage stage = (Stage) fermerBtn.getScene().getWindow();
        stage.close();
    }

    private void setLoading(boolean loading) {
        loadingBox.setVisible(loading);
        loadingBox.setManaged(loading);
        genererBtn.setDisable(loading);
        saisonCombo.setDisable(loading);
        typeExploitCombo.setDisable(loading);
        nbIdeesSpinner.setDisable(loading);
    }
}