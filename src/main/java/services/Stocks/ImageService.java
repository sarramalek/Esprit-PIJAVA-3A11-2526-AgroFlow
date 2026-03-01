package services.Stocks;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

public class ImageService {
    private static final String API_KEY = "54764417-9aa561cecf0d3fbd26ded8c0a";

    public String chercherImage(String motCleEn) {
        try {
            String query = URLEncoder.encode(motCleEn, StandardCharsets.UTF_8);
            String url = "https://pixabay.com/api/?key=" + API_KEY + "&q=" + query + "&image_type=photo&per_page=3";

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                JSONArray hits = json.getJSONArray("hits");
                if (hits.length() > 0) {
                    return hits.getJSONObject(0).getString("webformatURL");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur Image API: " + e.getMessage());
        }
        return "https://via.placeholder.com/150?text=Pas+d+image";
    }
}