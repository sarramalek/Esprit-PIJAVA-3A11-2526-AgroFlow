package services.User;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service d'upload d'images vers Cloudinary.
 *
 * ── Configuration ──────────────────────────────────────────────────────────
 * 1. Créez un compte gratuit sur https://cloudinary.com
 * 2. Dashboard → Settings → renseignez les 3 constantes ci-dessous
 * ───────────────────────────────────────────────────────────────────────────
 */
public class CloudinaryService {

    // ══════════════════════════════════════════════════════════════
    // 🔧 À CONFIGURER — Vos identifiants Cloudinary
    // ══════════════════════════════════════════════════════════════
    private static final String CLOUD_NAME  = "dp0ily6qa";   // ex: "agroflow123"
    private static final String API_KEY     = "274268384616998";      // ex: "123456789012345"
    private static final String API_SECRET  = "WIAECRPukptzimm_8KR-EnzvqnY";   // ex: "abcDEF_ghiJKL..."

    private static final String UPLOAD_URL  =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    // Dossier dans Cloudinary où seront rangées les photos
    private static final String FOLDER = "agroflow/profils";

    // ══════════════════════════════════════════════════════════════
    // Upload principal
    // ══════════════════════════════════════════════════════════════

    /**
     * Upload un fichier image vers Cloudinary et retourne l'URL sécurisée (https).
     *
     * @param imageFile fichier image local (jpg/png)
     * @param publicId  identifiant unique dans Cloudinary (ex: "employe_42")
     * @return URL https de l'image, ou null en cas d'erreur
     */
    public String uploadImage(File imageFile, String publicId) {
        try {
            // 1. Lire et encoder le fichier en Base64
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);
            String mimeType = detectMimeType(imageFile);
            String dataUri = "data:" + mimeType + ";base64," + base64Image;

            // 2. Paramètres signés (SANS file, api_key, resource_type)
            //    Règle Cloudinary : triés alphabétiquement, format "key=value&key=value" + API_SECRET
            long   timestamp    = System.currentTimeMillis() / 1000;
            String fullPublicId = FOLDER + "/" + publicId;  // ex: agroflow/profils/employe_12345

            // Paramètres triés alphabétiquement : folder < overwrite < public_id < timestamp
            String toSign = "folder="    + FOLDER
                    + "&overwrite=true"
                    + "&public_id="      + fullPublicId
                    + "&timestamp="      + timestamp
                    + API_SECRET;        // concaténé directement, sans "&"

            String signature = sha1Hex(toSign);

            // 3. Body POST — doit contenir exactement les mêmes paramètres que toSign
            String body = "file="        + encode(dataUri)
                    + "&api_key="        + encode(API_KEY)
                    + "&timestamp="      + timestamp
                    + "&public_id="      + encode(fullPublicId)
                    + "&folder="         + encode(FOLDER)
                    + "&overwrite=true"
                    + "&signature="      + encode(signature);

            // 4. Envoyer la requête HTTP POST
            HttpURLConnection conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            // 5. Lire la réponse JSON
            int status = conn.getResponseCode();
            InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
            String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            if (status == 200) {
                String url = extractJsonValue(response, "secure_url");
                System.out.println("✓ Photo uploadée : " + url);
                return url;
            } else {
                System.err.println("✗ Cloudinary erreur " + status + " : " + response);
                return null;
            }

        } catch (Exception e) {
            System.err.println("✗ Erreur upload Cloudinary : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Supprimer une image sur Cloudinary (optionnel, appelé lors du remove photo).
     *
     * @param publicId identifiant complet dans Cloudinary (ex: "agroflow/profils/employe_42")
     */
    public boolean deleteImage(String publicId) {
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            String toSign   = "public_id=" + publicId + "&timestamp=" + timestamp + API_SECRET;
            String signature = sha1Hex(toSign);

            String body = "public_id=" + encode(publicId)
                    + "&api_key="      + encode(API_KEY)
                    + "&timestamp="    + timestamp
                    + "&signature="    + encode(signature);

            String destroyUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/destroy";
            HttpURLConnection conn = (HttpURLConnection) new URL(destroyUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            System.out.println(status == 200 ? "✓ Image supprimée Cloudinary" : "⚠️ Suppression échouée");
            return status == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers privés
    // ══════════════════════════════════════════════════════════════

    private String sha1Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Extrait la valeur d'une clé dans un JSON simple (sans librairie externe) */
    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }

    /** URL-encode basique */
    private String encode(String value) throws Exception {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Détecte le MIME type selon l'extension */
    private String detectMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".gif"))  return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";
        return "image/jpeg"; // jpg par défaut
    }
}