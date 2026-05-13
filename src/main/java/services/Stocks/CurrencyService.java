package services.Stocks;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class CurrencyService {
    private static final String API_KEY = "48b793401dedece04062bf2a";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/pair/";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Convertit un montant d'une devise étrangère vers le Dinar Tunisien (TND)
     */
    public double convertToTND(double amount, String fromCurrency) {
        // Nettoyage au cas où on reçoit "Euro (€)" au lieu de "EUR"
        String currencyCode = extractCurrencyCode(fromCurrency);

        if (currencyCode.equals("TND") || amount <= 0) {
            return amount;
        }

        try {
            String url = BASE_URL + currencyCode + "/TND";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());

            if (json.has("result") && json.getString("result").equals("success")) {
                double rate = json.getDouble("conversion_rate");
                return Math.round(amount * rate * 1000.0) / 1000.0;
            }
        } catch (Exception e) {
            System.err.println("Erreur conversion API: " + e.getMessage());
        }

        // Taux manuels si l'API échoue (Fallbacks)
        Map<String, Double> fallbacks = Map.of(
            "EUR", 3.385,
            "USD", 3.120,
            "GBP", 3.920
        );
        
        double rate = fallbacks.getOrDefault(currencyCode, 1.0);
        return Math.round(amount * rate * 1000.0) / 1000.0;
    }

    private String extractCurrencyCode(String currencyLabel) {
        if (currencyLabel.contains("TND")) return "TND";
        if (currencyLabel.contains("EUR") || currencyLabel.contains("€")) return "EUR";
        if (currencyLabel.contains("USD") || currencyLabel.contains("$")) return "USD";
        if (currencyLabel.contains("GBP") || currencyLabel.contains("£")) return "GBP";
        
        // Par défaut, essayer d'extraire entre parenthèses
        if (currencyLabel.contains("(") && currencyLabel.contains(")")) {
            return currencyLabel.substring(currencyLabel.indexOf("(") + 1, currencyLabel.indexOf(")"));
        }
        return "TND";
    }
}
