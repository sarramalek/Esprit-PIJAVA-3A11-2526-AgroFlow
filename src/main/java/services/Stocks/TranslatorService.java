package services.Stocks;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.json.JSONObject;

public class TranslatorService {
    // On garde l'URL de base sans les paramètres problématiques
    private static final String BASE_URL = "https://api.mymemory.translated.net/get";

    public String traduire(String texte, String targetLang) {
        if (texte == null || texte.isEmpty()) return texte;

        try {
            // Encodage propre des paramètres
            String query = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String langPair = URLEncoder.encode("fr|" + targetLang, StandardCharsets.UTF_8);

            // Construction de l'URL finale sécurisée
            String urlFinal = BASE_URL + "?q=" + query + "&langpair=" + langPair;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlFinal)) // L'URL est maintenant 100% propre
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject obj = new JSONObject(response.body());
                return obj.getJSONObject("responseData").getString("translatedText");
            }
        } catch (Exception e) {
            // On affiche l'erreur exacte pour comprendre si le réseau bloque encore
            System.err.println("Erreur MyMemory (" + targetLang + "): " + e.getMessage());
        }
        return texte;
    }
}