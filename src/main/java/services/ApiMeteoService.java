package services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * ============================================================
 * Service API - Météo (Open-Meteo) + Géolocalisation (Nominatim)
 * 100% gratuit - Sans clé API - Sans carte bancaire
 * ============================================================
 *
 * DÉPENDANCE À AJOUTER dans pom.xml :
 * <dependency>
 *     <groupId>org.json</groupId>
 *     <artifactId>json</artifactId>
 *     <version>20231013</version>
 * </dependency>
 * ============================================================
 */
public class ApiMeteoService {

    // ── URLS DES API ──
    private static final String OPEN_METEO_URL  = "https://api.open-meteo.com/v1/forecast";
    private static final String NOMINATIM_URL   = "https://nominatim.openstreetmap.org/search";

    // ============================================================
    // MODÈLE : Résultat météo
    // ============================================================
    public static class MeteoResult {
        public double temperature;
        public double humidite;
        public double vent;
        public double precipitation;
        public int    weatherCode;
        public String condition;
        public String emoji;
        public String conseilArrosage;
        public String ville;
        public double latitude;
        public double longitude;

        @Override
        public String toString() {
            return emoji + " " + condition + "\n" +
                    "Température    : " + temperature + " °C\n" +
                    "Humidité       : " + humidite + " %\n" +
                    "Vent           : " + vent + " km/h\n" +
                    "Précipitations : " + precipitation + " mm\n\n" +
                    "💡 Conseil : " + conseilArrosage;
        }
    }

    // ============================================================
    // MODÈLE : Résultat géolocalisation
    // ============================================================
    public static class GeoResult {
        public double latitude;
        public double longitude;
        public String adresseComplete;
        public String ville;
        public String pays;
    }

    // ============================================================
    // 1. GÉOLOCALISATION - Nominatim (OpenStreetMap)
    //    Convertit une adresse en coordonnées GPS
    // ============================================================
    public GeoResult geoLocaliser(String adresse) throws Exception {
        String adresseEncodee = URLEncoder.encode(adresse, "UTF-8");
        String urlStr = NOMINATIM_URL + "?q=" + adresseEncodee +
                "&format=json&limit=1&addressdetails=1";

        String response = appelAPI(urlStr, "Nominatim/1.0 (agroflow@app.com)");

        JSONArray results = new JSONArray(response);
        if (results.length() == 0) {
            throw new Exception("Adresse introuvable : " + adresse);
        }

        JSONObject premier = results.getJSONObject(0);
        GeoResult geo = new GeoResult();
        geo.latitude       = Double.parseDouble(premier.getString("lat"));
        geo.longitude      = Double.parseDouble(premier.getString("lon"));
        geo.adresseComplete = premier.getString("display_name");

        // Extraire ville et pays si disponibles
        if (premier.has("address")) {
            JSONObject addr = premier.getJSONObject("address");
            geo.ville = addr.optString("city",
                    addr.optString("town",
                            addr.optString("village", "Inconnue")));
            geo.pays  = addr.optString("country", "Inconnu");
        }

        return geo;
    }

    // ============================================================
    // 2. MÉTÉO - Open-Meteo (par coordonnées GPS)
    //    Retourne météo actuelle + prévisions
    // ============================================================
    public MeteoResult getMeteo(double latitude, double longitude, String ville) throws Exception {
        String urlStr = OPEN_METEO_URL +
                "?latitude=" + latitude +
                "&longitude=" + longitude +
                "&current_weather=true" +
                "&hourly=relativehumidity_2m,precipitation" +
                "&daily=precipitation_sum" +
                "&timezone=auto";

        String response = appelAPI(urlStr, null);
        JSONObject json = new JSONObject(response);

        MeteoResult meteo = new MeteoResult();
        meteo.ville     = ville;
        meteo.latitude  = latitude;
        meteo.longitude = longitude;

        // Données météo actuelles
        JSONObject current = json.getJSONObject("current_weather");
        meteo.temperature  = current.getDouble("temperature");
        meteo.vent         = current.getDouble("windspeed");
        meteo.weatherCode  = current.getInt("weathercode");

        // Humidité (première heure disponible)
        if (json.has("hourly")) {
            JSONObject hourly = json.getJSONObject("hourly");
            JSONArray humidites = hourly.getJSONArray("relativehumidity_2m");
            meteo.humidite = humidites.getDouble(0);

            JSONArray precip = hourly.getJSONArray("precipitation");
            meteo.precipitation = precip.getDouble(0);
        }

        // Condition météo selon le code
        meteo.condition = getCondition(meteo.weatherCode);
        meteo.emoji     = getEmoji(meteo.weatherCode);

        // Conseil d'arrosage agricole
        meteo.conseilArrosage = getConseilArrosage(meteo.temperature, meteo.humidite, meteo.precipitation);

        return meteo;
    }

    // ============================================================
    // 3. MÉTHODE COMBINÉE - Adresse → Météo complète
    //    Géolocalise l'adresse PUIS récupère la météo
    // ============================================================
    public MeteoResult getMeteoParAdresse(String adresse) throws Exception {
        // Étape 1 : Géolocaliser l'adresse
        GeoResult geo = geoLocaliser(adresse);

        // Étape 2 : Obtenir la météo avec les coordonnées
        return getMeteo(geo.latitude, geo.longitude, geo.ville);
    }

    // ============================================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ============================================================

    // Appel HTTP générique
    private String appelAPI(String urlStr, String userAgent) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        // Nominatim exige un User-Agent
        if (userAgent != null) {
            conn.setRequestProperty("User-Agent", userAgent);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Erreur API (code " + responseCode + ")");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        return sb.toString();
    }

    // Condition météo selon le code WMO
    private String getCondition(int code) {
        if (code == 0)              return "Ciel dégagé";
        else if (code <= 3)         return "Partiellement nuageux";
        else if (code <= 9)         return "Brouillard";
        else if (code <= 19)        return "Bruine légère";
        else if (code <= 29)        return "Pluie";
        else if (code <= 39)        return "Neige légère";
        else if (code <= 49)        return "Brouillard givrant";
        else if (code <= 59)        return "Bruine";
        else if (code <= 69)        return "Pluie modérée";
        else if (code <= 79)        return "Neige";
        else if (code <= 84)        return "Averses";
        else if (code <= 94)        return "Orage";
        else                        return "Orage violent";
    }

    // Emoji météo
    private String getEmoji(int code) {
        if (code == 0)              return "☀️";
        else if (code <= 3)         return "⛅";
        else if (code <= 9)         return "🌫️";
        else if (code <= 29)        return "🌦️";
        else if (code <= 49)        return "🌫️";
        else if (code <= 69)        return "🌧️";
        else if (code <= 79)        return "❄️";
        else if (code <= 84)        return "🌨️";
        else                        return "⛈️";
    }

    // Conseil d'arrosage agricole intelligent
    private String getConseilArrosage(double temp, double humidite, double precipitation) {
        if (precipitation > 5.0) {
            return "Pas d'arrosage nécessaire - Pluie suffisante aujourd'hui.";
        } else if (temp > 35) {
            return "Arrosage URGENT recommandé - Chaleur extrême ! Arrosez tôt le matin.";
        } else if (temp > 28 && humidite < 40) {
            return "Arrosage recommandé - Chaleur et air sec. Augmentez les doses.";
        } else if (temp > 20 && humidite < 50) {
            return "Arrosage modéré conseillé - Conditions chaudes et peu humides.";
        } else if (humidite > 80) {
            return "Arrosage réduit - Humidité élevée, attention aux maladies fongiques.";
        } else {
            return "Conditions normales - Arrosage standard selon vos plantes.";
        }
    }
}