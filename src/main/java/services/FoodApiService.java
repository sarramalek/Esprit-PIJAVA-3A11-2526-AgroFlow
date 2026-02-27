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

        // 1. On prépare des secours de qualité pour CHAQUE espèce
        List<String> secours = obtenirSecours(espece);

        // 2. Traduction simple pour l'URL
        String query = traduireEspece(espece);
        String urlString = "https://world.openfoodfacts.org/cgi/search.pl?search_terms="
                + query.replace(" ", "%20") + "&json=1&page_size=5";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "AgroFlowApp/1.0");

            // On réduit le timeout à 5 secondes pour plus de réactivité
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray products = json.optJSONArray("products");

                if (products != null && products.length() > 0) {
                    for (int i = 0; i < products.length(); i++) {
                        String name = products.getJSONObject(i).optString("product_name");
                        if (!name.isEmpty()) suggestions.add(name);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Note : API trop lente pour " + espece + ", passage au mode local.");
        }

        // 3. Si l'API a crashé (Timeout) ou est vide, on utilise le secours
        return suggestions.isEmpty() ? secours : suggestions;
    }

    private List<String> obtenirSecours(String espece) {
        List<String> liste = new ArrayList<>();
        switch (espece.toLowerCase()) {
            case "vache":
                liste.add("Foin de luzerne"); liste.add("Granulés bovins croissance"); break;
            case "mouton":
                liste.add("Mélange céréales ovins"); liste.add("Bloc à lécher minéral"); break;
            case "chèvre":
                liste.add("Fourrage sec chèvre"); liste.add("Complément orge/avoine"); break;
            case "chien":
                liste.add("Croquettes premium chien"); liste.add("Pâtée équilibrée"); break;
            case "chat":
                liste.add("Croquettes saumon chat"); liste.add("Sachets fraîcheur"); break;
            default:
                liste.add("Aliment complet animal");
        }
        return liste;
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