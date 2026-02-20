package services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FoodApiService {

    public List<String> chercherAliments(String espece) {
        List<String> suggestions = new ArrayList<>();

        // 1. On utilise des termes très simples pour éviter de perdre le serveur
        String query = traduireEspece(espece);

        String urlString = "https://world.openfoodfacts.org/cgi/search.pl?search_terms="
                + query.replace(" ", "%20")
                + "&json=1&page_size=5";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "AgroFlowApp - Java - Version 1.0");

            // 2. On définit un temps limite de 5 secondes pour ne pas bloquer l'appli
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // Si tout va bien
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("products")) {
                    JSONArray products = jsonResponse.getJSONArray("products");
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject item = products.getJSONObject(i);
                        if (item.has("product_name")) {
                            suggestions.add(item.getString("product_name"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur API (" + espece + ") : " + e.getMessage());
        }

        // 3. Système de secours : si l'API échoue ou ne trouve rien, on donne une base
        if (suggestions.isEmpty()) {
            suggestions.add("Foin de prairie");
            suggestions.add("Mélange de céréales standard");
        }

        return suggestions;
    }

    private String traduireEspece(String espece) {
        if (espece == null) return "";
        switch (espece.toLowerCase()) {
            case "chat": return " chat stérilisé";
            case "chien": return "chien";
            case "vache": return "avoine"; // Plus simple pour l'API
            case "chèvre": return "Mélange de céréales blé, soja, avoine et orge";
            case "mouton": return "Mélange de céréales blé, soja, avoine et orge";
            case "cheval": return "herbes";
            default: return espece;
        }
    }
}