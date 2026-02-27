package controllers.Animaux;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FicheSanteController {
    @FXML private TextField txtRecherche;
    @FXML private Label lblNom, lblLongevite, lblTemperament;
    @FXML private TextArea txtInfos;

    @FXML
    void rechercherRace() {
        String animal = txtRecherche.getText().trim();
        if (animal.isEmpty()) {
            txtInfos.setText("Veuillez saisir une espèce (ex: Vache, Mouton, Cheval).");
            return;
        }

        txtInfos.setText("Recherche d'informations sur : " + animal + "...");

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // On encode le nom pour gérer les accents (ex: "Chèvre")
                String query = URLEncoder.encode(animal, StandardCharsets.UTF_8.toString());

                // API Wikipedia (Résumé de la page)
                URL url = new URL("https://fr.wikipedia.org/api/rest_v1/page/summary/" + query);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "AgroFlow-App/1.0 (Contact: ton-email@example.com)");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (Scanner s = new Scanner(conn.getInputStream(), "UTF-8")) {
                        return s.useDelimiter("\\A").hasNext() ? s.next() : "";
                    }
                } else if (responseCode == 404) {
                    throw new Exception("Espèce introuvable sur Wikipedia.");
                } else {
                    throw new Exception("Erreur serveur Wikipedia : " + responseCode);
                }
            }
        };

        task.setOnSucceeded(e -> {
            String json = task.getValue();
            try {
                // Extraction du titre et du résumé (extract)
                String title = json.split("\"title\":\"")[1].split("\"")[0];
                String extract = json.split("\"extract\":\"")[1].split("\"")[0];

                // On décode les caractères Unicode (\u00e9 -> é)
                extract = unescapeUnicode(extract);

                lblNom.setText("Espèce : " + title);
                lblLongevite.setText("Source : Wikipedia France");
                lblTemperament.setText("Type : Information Générale");

                txtInfos.setText("📚 INFORMATIONS WIKIPEDIA :\n\n" + extract +
                        "\n\n💡 Conseil AgroFlow : Utilisez ces données pour adapter l'environnement de l'animal.");

            } catch (Exception ex) {
                txtInfos.setText("Informations trouvées mais le format est illisible.");
            }
        });

        task.setOnFailed(e -> {
            txtInfos.setText("⚠️ " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // Petite fonction utilitaire pour que les accents s'affichent bien
    private String unescapeUnicode(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                sb.append((char) Integer.parseInt(hex, 16));
                i += 6;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    @FXML
    void fermerFenetre() {
        if (txtInfos.getScene() != null) {
            ((javafx.stage.Stage) txtInfos.getScene().getWindow()).close();
        }
    }
}