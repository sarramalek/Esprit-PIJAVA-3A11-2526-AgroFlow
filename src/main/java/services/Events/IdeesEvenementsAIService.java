package services.Events;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service IA utilisant Groq API (gratuit, rapide, fonctionne partout).
 *
 * ✅ Obtenez une clé GRATUITE en 1 minute :
 *    1. Allez sur https://console.groq.com/
 *    2. Connectez-vous avec Google
 *    3. Cliquez "API Keys" → "Create API Key"
 *    4. Copiez la clé (commence par gsk_...)
 *    5. Remplacez API_KEY ci-dessous
 */
public class IdeesEvenementsAIService {

    // ⚠️ Remplacez par votre clé Groq (gratuite sur https://console.groq.com/)
    private static final String API_KEY = "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";

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile"; // modèle actif et gratuit

    private final HttpClient httpClient;

    public IdeesEvenementsAIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String genererIdees(String saison, String typeExploit, int nbIdees) throws Exception {
        String prompt      = buildPrompt(saison, typeExploit, nbIdees);
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type",  "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur API Groq (code " + response.statusCode()
                    + ") :\n" + response.body());
        }

        return parseResponse(response.body());
    }

    private String buildPrompt(String saison, String typeExploit, int nbIdees) {
        return String.format(
                "Tu es un expert en gestion agricole et planification d'evenements pour exploitations tunisiennes.\n\n"
                        + "Genere exactement %d idees d'evenements agricoles pour :\n"
                        + "- Saison : %s\n"
                        + "- Type d'exploitation : %s\n\n"
                        + "Pour chaque idee, utilise ce format :\n\n"
                        + "TITRE : [titre]\n"
                        + "TYPE : [Formation/Foire/Atelier/Recolte/Reunion/Visite/Marche]\n"
                        + "DESCRIPTION : [2-3 phrases]\n"
                        + "OBJECTIF : [benefice principal]\n"
                        + "DUREE : [duree suggeree]\n"
                        + "---",
                nbIdees, saison, typeExploit
        );
    }

    private String buildRequestBody(String prompt) {
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escaped + "\"}],"
                + "\"max_tokens\":2048,"
                + "\"temperature\":0.8"
                + "}";
    }

    private String parseResponse(String json) {
        int start = json.indexOf("\"content\":\"");
        if (start == -1) {
            throw new RuntimeException("Format de reponse inattendu :\n" + json);
        }
        start += 11;
        int end = findTextEnd(json, start);
        String raw = json.substring(start, end);
        return raw
                .replace("\\n",  "\n")
                .replace("\\r",  "\r")
                .replace("\\t",  "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private int findTextEnd(String json, int start) {
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') { i += 2; }
            else if (c == '"') { return i; }
            else { i++; }
        }
        throw new RuntimeException("Impossible de parser la reponse JSON.");
    }
}