package services.Materiels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Materiels.Machine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║   MindSphereService — Intégration API Avancée MindSphere (Siemens)      ║
 * ║                                                                          ║
 * ║  ✅ Authentification OAuth2 (Client Credentials)                         ║
 * ║  ✅ Gestion des Assets (Machines) via Asset Management API               ║
 * ║  ✅ Envoi de TimeSeries (données capteurs / métriques)                   ║
 * ║  ✅ Récupération des IoT Time Series                                      ║
 * ║  ✅ Gestion des Aspects (propriétés dynamiques)                           ║
 * ║  ✅ Synchronisation bidirectionnelle BDD ↔ MindSphere                    ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 *
 * Configuration requise dans mindSphere.properties :
 *   mindsphere.host        = <votre-tenant>.piam.eu1.mindsphere.io
 *   mindsphere.tenant      = <votre-tenant>
 *   mindsphere.client_id   = <client_id>
 *   mindsphere.client_secret = <client_secret>
 *   mindsphere.asset_type  = agroflow.Machine
 */
public class MindSphereService {

    private static final Logger LOG = Logger.getLogger(MindSphereService.class.getName());

    // ── Configuration MindSphere ────────────────────────────────────────────
    // ⚠️  Remplacez ces valeurs par vos vraies credentials MindSphere
    private static final String TENANT          = "votre-tenant";
    private static final String HOST            = TENANT + ".piam.eu1.mindsphere.io";
    private static final String BASE_URL        = "https://" + HOST;
    private static final String CLIENT_ID       = "votre_client_id";
    private static final String CLIENT_SECRET   = "votre_client_secret";
    private static final String ASSET_TYPE_ID   = "agroflow.Machine";
    private static final String ASPECT_NAME     = "etatMachine";
    private static final String PARENT_ASSET_ID = ""; // ID de l'asset parent (optionnel)

    // ── Endpoints MindSphere ────────────────────────────────────────────────
    private static final String TOKEN_URL       = BASE_URL + "/oauth/token";
    private static final String ASSET_URL       = "https://gateway." + TENANT + ".eu1.mindsphere.io/api/assetmanagement/v3/assets";
    private static final String TIMESERIES_URL  = "https://gateway." + TENANT + ".eu1.mindsphere.io/api/iottimeseries/v3/timeseries";
    private static final String ASPECT_TYPE_URL = "https://gateway." + TENANT + ".eu1.mindsphere.io/api/assetmanagement/v3/aspecttypes";

    // ── HTTP Client ─────────────────────────────────────────────────────────
    private final HttpClient     httpClient;
    private final ObjectMapper   mapper;
    private       String         accessToken;
    private       long           tokenExpiresAt = 0;

    public MindSphereService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.mapper = new ObjectMapper();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  1. AUTHENTIFICATION OAuth2 — Client Credentials Flow
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Obtient ou renouvelle le token OAuth2 MindSphere.
     * Le token est mis en cache jusqu'à 5 minutes avant son expiration.
     */
    public String getAccessToken() throws MindSphereException {
        long now = System.currentTimeMillis();

        // Renouvellement si expiré (marge de 5 minutes)
        if (accessToken != null && now < tokenExpiresAt - 300_000) {
            return accessToken;
        }

        try {
            // Encodage Basic Auth : Base64(client_id:client_secret)
            String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
            String basicAuth   = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            String body = "grant_type=client_credentials"
                    + "&client_id=" + CLIENT_ID
                    + "&client_secret=" + CLIENT_SECRET;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type",  "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + basicAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new MindSphereException("Authentification échouée [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            JsonNode json      = mapper.readTree(response.body());
            accessToken        = json.get("access_token").asText();
            long expiresIn     = json.has("expires_in") ? json.get("expires_in").asLong() : 3600;
            tokenExpiresAt     = now + expiresIn * 1000;

            LOG.info("✅ Token MindSphere obtenu. Expire dans " + expiresIn + "s");
            return accessToken;

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur lors de l'authentification OAuth2 : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2. GESTION DES ASSETS — Asset Management API v3
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Crée un Asset MindSphere à partir d'une Machine locale.
     * Retourne l'ID MindSphere de l'asset créé.
     */
    public String creerAsset(Machine machine) throws MindSphereException {
        try {
            String token = getAccessToken();

            ObjectNode body = mapper.createObjectNode();
            body.put("name",        machine.getNom() + " - " + machine.getMarque());
            body.put("externalId",  "machine-" + machine.getIdM());
            body.put("typeId",      ASSET_TYPE_ID);
            body.put("description", buildDescription(machine));
            body.put("timezone",    "Europe/Paris");

            // Ajout du parent si défini
            if (PARENT_ASSET_ID != null && !PARENT_ASSET_ID.isBlank()) {
                body.put("parentId", PARENT_ASSET_ID);
            }

            // Métadonnées de la machine
            ObjectNode location = mapper.createObjectNode();
            location.put("country",   "Tunisie");
            location.put("locality",  "Agroflow Farm");
            location.put("streetAddress", "Machine ID: " + machine.getIdM());
            body.set("location", location);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ASSET_URL))
                    .header("Content-Type",  "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new MindSphereException("Création asset échouée [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            JsonNode json    = mapper.readTree(response.body());
            String assetId   = json.get("assetId").asText();
            LOG.info("✅ Asset MindSphere créé : " + assetId + " pour machine " + machine.getIdM());
            return assetId;

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur création asset : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère tous les assets MindSphere de type agroflow.Machine.
     * Retourne une liste de machines reconstruites depuis MindSphere.
     */
    public List<Machine> recupererAssetsDepuisMindsphere() throws MindSphereException {
        try {
            String token = getAccessToken();

            // Filtre par type d'asset + pagination
            String url = ASSET_URL + "?size=200&page=0&filter="
                    + java.net.URLEncoder.encode(
                    "{\"typeId\":{\"eq\":\"" + ASSET_TYPE_ID + "\"}}",
                    StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept",        "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new MindSphereException("Récupération assets échouée [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            JsonNode root     = mapper.readTree(response.body());
            JsonNode embedded = root.path("_embedded").path("assets");

            List<Machine> machines = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode assetNode : embedded) {
                    machines.add(assetVersEntiteMachine(assetNode));
                }
            }

            LOG.info("✅ " + machines.size() + " assets récupérés depuis MindSphere");
            return machines;

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur récupération assets : " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un asset existant MindSphere (PATCH).
     * @param mindSphereAssetId  l'ID MindSphere de l'asset
     * @param machine            les nouvelles données
     * @param etag               l'ETag récupéré lors du GET (obligatoire pour le PATCH)
     */
    public void mettreAJourAsset(String mindSphereAssetId, Machine machine, String etag)
            throws MindSphereException {
        try {
            String token = getAccessToken();

            ObjectNode body = mapper.createObjectNode();
            body.put("name",        machine.getNom() + " - " + machine.getMarque());
            body.put("description", buildDescription(machine));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ASSET_URL + "/" + mindSphereAssetId))
                    .header("Content-Type",  "application/merge-patch+json")
                    .header("Authorization", "Bearer " + token)
                    .header("If-Match",      etag)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new MindSphereException("Mise à jour asset échouée [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            LOG.info("✅ Asset " + mindSphereAssetId + " mis à jour");

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur mise à jour asset : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un asset MindSphere par son ID.
     */
    public void supprimerAsset(String mindSphereAssetId, String etag) throws MindSphereException {
        try {
            String token = getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ASSET_URL + "/" + mindSphereAssetId))
                    .header("Authorization", "Bearer " + token)
                    .header("If-Match",      etag)
                    .DELETE()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204) {
                throw new MindSphereException("Suppression asset échouée [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            LOG.info("✅ Asset " + mindSphereAssetId + " supprimé");

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur suppression asset : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère l'ETag d'un asset (nécessaire pour PUT/PATCH/DELETE).
     */
    public String getEtagAsset(String mindSphereAssetId) throws MindSphereException {
        try {
            String token = getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ASSET_URL + "/" + mindSphereAssetId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new MindSphereException("GET asset échoué [HTTP "
                        + response.statusCode() + "]");
            }

            return response.headers().firstValue("ETag")
                    .orElseThrow(() -> new MindSphereException("ETag absent de la réponse"));

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur récupération ETag : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3. IoT TIME SERIES — Envoi de données capteurs
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Envoie des données IoT Time Series pour un asset MindSphere.
     * Exemple : état de la machine, heure de mesure.
     *
     * @param assetId    l'ID MindSphere de l'asset
     * @param machine    la machine source
     */
    public void envoyerTimeSeries(String assetId, Machine machine) throws MindSphereException {
        try {
            String token = getAccessToken();

            // Construction du payload Time Series
            ArrayNode dataPoints = mapper.createArrayNode();
            ObjectNode dataPoint = mapper.createObjectNode();

            // Timestamp ISO 8601
            String timestamp = java.time.Instant.now().toString();
            dataPoint.put("_time", timestamp);

            // Variables de l'aspect
            dataPoint.put("etat",        machine.getEtatM() != null ? machine.getEtatM() : "inconnu");
            dataPoint.put("marque",      machine.getMarque() != null ? machine.getMarque() : "");
            dataPoint.put("modele",      machine.getModele() != null ? machine.getModele() : "");
            dataPoint.put("disponible",  !"en panne".equalsIgnoreCase(machine.getEtatM()));

            // Score numérique selon état (pour graphiques IoT)
            int scoreEtat = scoreEtat(machine.getEtatM());
            dataPoint.put("scoreEtat", scoreEtat);

            dataPoints.add(dataPoint);

            // URL : /timeseries/{assetId}/{aspectName}
            String url = TIMESERIES_URL + "/" + assetId + "/" + ASPECT_NAME;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type",  "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(dataPoints.toString()))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new MindSphereException("Envoi TimeSeries échoué [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            LOG.info("✅ TimeSeries envoyé pour asset " + assetId
                    + " | état=" + machine.getEtatM() + " | score=" + scoreEtat);

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur envoi TimeSeries : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les dernières Time Series d'un asset MindSphere.
     *
     * @param assetId   l'ID MindSphere de l'asset
     * @param from      date de début (ISO 8601)
     * @param to        date de fin   (ISO 8601)
     * @return JSON brut des time series
     */
    public String recupererTimeSeries(String assetId, String from, String to)
            throws MindSphereException {
        try {
            String token = getAccessToken();

            String url = TIMESERIES_URL + "/" + assetId + "/" + ASPECT_NAME
                    + "?from=" + java.net.URLEncoder.encode(from, StandardCharsets.UTF_8)
                    + "&to="   + java.net.URLEncoder.encode(to,   StandardCharsets.UTF_8)
                    + "&limit=1000&sort=asc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept",        "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new MindSphereException("Récupération TimeSeries échouée [HTTP "
                        + response.statusCode() + "] : " + response.body());
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur récupération TimeSeries : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4. ASPECT TYPES — Définition du schéma de données
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Crée (ou vérifie) l'AspectType "etatMachine" dans MindSphere.
     * Doit être appelé une seule fois lors de la configuration initiale.
     */
    public void creerAspectTypeSiAbsent() throws MindSphereException {
        try {
            String token = getAccessToken();
            String aspectTypeId = TENANT + "." + ASPECT_NAME;

            // Vérifier si l'aspect existe déjà
            HttpRequest checkReq = HttpRequest.newBuilder()
                    .uri(URI.create(ASPECT_TYPE_URL + "/" + aspectTypeId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> checkResp = httpClient.send(
                    checkReq, HttpResponse.BodyHandlers.ofString());

            if (checkResp.statusCode() == 200) {
                LOG.info("ℹ️  AspectType déjà existant : " + aspectTypeId);
                return;
            }

            // Création de l'aspect type
            ObjectNode body = mapper.createObjectNode();
            body.put("name",     ASPECT_NAME);
            body.put("category", "dynamic");

            ArrayNode variables = mapper.createArrayNode();

            variables.add(buildVariable("etat",       "STRING", "État de la machine", ""));
            variables.add(buildVariable("marque",     "STRING", "Marque",              ""));
            variables.add(buildVariable("modele",     "STRING", "Modèle",              ""));
            variables.add(buildVariable("disponible", "BOOLEAN","Disponibilité",       ""));
            variables.add(buildVariable("scoreEtat",  "INT",    "Score état (0-100)",  "score"));

            body.set("variables", variables);

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create(ASPECT_TYPE_URL + "/" + aspectTypeId))
                    .header("Content-Type",  "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> createResp = httpClient.send(
                    createReq, HttpResponse.BodyHandlers.ofString());

            if (createResp.statusCode() != 201 && createResp.statusCode() != 200) {
                throw new MindSphereException("Création AspectType échouée [HTTP "
                        + createResp.statusCode() + "] : " + createResp.body());
            }

            LOG.info("✅ AspectType créé : " + aspectTypeId);

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur création AspectType : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  5. SYNCHRONISATION — BDD locale ↔ MindSphere
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Synchronise une liste de machines vers MindSphere.
     * Pour chaque machine :
     *   - Si l'asset n'existe pas → le crée
     *   - Envoie les données en TimeSeries
     *
     * @param machines   liste des machines à synchroniser
     * @return           rapport de synchronisation
     */
    public SyncReport synchroniserVersMindSphere(List<Machine> machines)
            throws MindSphereException {

        SyncReport rapport = new SyncReport();
        rapport.total = machines.size();

        for (Machine machine : machines) {
            try {
                // Chercher si l'asset existe déjà par externalId
                String assetId = trouverAssetParExternalId("machine-" + machine.getIdM());

                if (assetId == null) {
                    // Créer l'asset
                    assetId = creerAsset(machine);
                    rapport.crees++;
                } else {
                    // Mettre à jour
                    String etag = getEtagAsset(assetId);
                    mettreAJourAsset(assetId, machine, etag);
                    rapport.misAJour++;
                }

                // Envoyer les données IoT
                envoyerTimeSeries(assetId, machine);
                rapport.timeSeriesEnvoyes++;

            } catch (MindSphereException e) {
                rapport.erreurs++;
                rapport.messages.add("❌ Machine " + machine.getIdM()
                        + " (" + machine.getMarque() + ") : " + e.getMessage());
                LOG.warning("Erreur sync machine " + machine.getIdM() + " : " + e.getMessage());
            }
        }

        LOG.info("🔄 Sync terminée : " + rapport.toString());
        return rapport;
    }

    /**
     * Trouve un asset MindSphere par son externalId.
     * Retourne null si non trouvé.
     */
    public String trouverAssetParExternalId(String externalId) throws MindSphereException {
        try {
            String token = getAccessToken();

            String filter = java.net.URLEncoder.encode(
                    "{\"externalId\":{\"eq\":\"" + externalId + "\"}}",
                    StandardCharsets.UTF_8);

            String url = ASSET_URL + "?filter=" + filter + "&size=1";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept",        "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) return null;

            if (response.statusCode() != 200) {
                throw new MindSphereException("Recherche asset échouée [HTTP "
                        + response.statusCode() + "]");
            }

            JsonNode root     = mapper.readTree(response.body());
            JsonNode embedded = root.path("_embedded").path("assets");

            if (embedded.isArray() && embedded.size() > 0) {
                return embedded.get(0).get("assetId").asText();
            }

            return null;

        } catch (IOException | InterruptedException e) {
            throw new MindSphereException("Erreur recherche par externalId : " + e.getMessage(), e);
        }
    }

    /**
     * Teste la connexion MindSphere et retourne true si OK.
     */
    public boolean testerConnexion() {
        try {
            getAccessToken();
            return true;
        } catch (MindSphereException e) {
            LOG.warning("Test connexion MindSphere échoué : " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UTILITAIRES PRIVÉS
    // ══════════════════════════════════════════════════════════════════════

    private String buildDescription(Machine m) {
        return String.format(
                "Marque: %s | Modèle: %s | État: %s | N°Série: %s | Achat: %s | Nom: %s",
                m.getMarque(),
                m.getModele(),
                m.getEtatM(),
                m.getNumeroSerie(),
                m.getDateAchat() != null ? m.getDateAchat().format(DateTimeFormatter.ISO_LOCAL_DATE) : "N/A",
                m.getNom()
        );
    }

    private Machine assetVersEntiteMachine(JsonNode assetNode) {
        Machine m = new Machine();
        String name = assetNode.path("name").asText("");

        // Extraction depuis externalId (format: "machine-123")
        String externalId = assetNode.path("externalId").asText("");
        if (externalId.startsWith("machine-")) {
            try {
                m.setIdM(Integer.parseInt(externalId.substring("machine-".length())));
            } catch (NumberFormatException ignored) {}
        }

        // Extraction depuis la description
        String desc = assetNode.path("description").asText("");
        parseDescription(m, desc);

        // Fallback sur le nom
        if (m.getMarque() == null || m.getMarque().isBlank()) {
            m.setMarque(name.contains(" - ") ? name.split(" - ")[1] : name);
        }
        if (m.getNom() == null || m.getNom().isBlank()) {
            m.setNom(name.contains(" - ") ? name.split(" - ")[0] : name);
        }

        return m;
    }

    private void parseDescription(Machine m, String desc) {
        if (desc == null || desc.isBlank()) return;
        String[] parts = desc.split("\\|");
        for (String part : parts) {
            String[] kv = part.trim().split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().toLowerCase();
            String val = kv[1].trim();
            switch (key) {
                case "marque"   -> m.setMarque(val);
                case "modèle"   -> m.setModele(val);
                case "état"     -> m.setEtatM(val);
                case "n°série"  -> m.setNumeroSerie(val);
                case "achat"    -> { try { m.setDateAchat(LocalDate.parse(val)); } catch (Exception ignored) {} }
                case "nom"      -> m.setNom(val);
            }
        }
    }

    private int scoreEtat(String etat) {
        if (etat == null) return 0;
        return switch (etat.toLowerCase()) {
            case "neuf"      -> 100;
            case "disponible"-> 85;
            case "bon"       -> 70;
            case "occasion"  -> 50;
            case "en panne"  -> 0;
            default          -> 30;
        };
    }

    private ObjectNode buildVariable(String name, String dataType,
                                     String description, String unit) {
        ObjectNode v = mapper.createObjectNode();
        v.put("name",        name);
        v.put("dataType",    dataType);
        v.put("description", description);
        if (!unit.isBlank()) v.put("unit", unit);
        v.put("searchable",  true);
        return v;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CLASSES INTERNES
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Rapport de synchronisation retourné après un appel à synchroniserVersMindSphere().
     */
    public static class SyncReport {
        public int          total             = 0;
        public int          crees             = 0;
        public int          misAJour          = 0;
        public int          timeSeriesEnvoyes = 0;
        public int          erreurs           = 0;
        public List<String> messages          = new ArrayList<>();

        public boolean estUnSucces() { return erreurs == 0; }

        @Override
        public String toString() {
            return "SyncReport{total=" + total
                    + ", créés=" + crees
                    + ", màj=" + misAJour
                    + ", timeSeries=" + timeSeriesEnvoyes
                    + ", erreurs=" + erreurs + "}";
        }

        public String toTexteResume() {
            StringBuilder sb = new StringBuilder();
            sb.append("📊 Rapport de Synchronisation MindSphere\n");
            sb.append("─────────────────────────────────────────\n");
            sb.append("Total machines traitées : ").append(total).append("\n");
            sb.append("✅ Assets créés          : ").append(crees).append("\n");
            sb.append("✏️  Assets mis à jour    : ").append(misAJour).append("\n");
            sb.append("📡 TimeSeries envoyés   : ").append(timeSeriesEnvoyes).append("\n");
            sb.append("❌ Erreurs               : ").append(erreurs).append("\n");
            if (!messages.isEmpty()) {
                sb.append("\nDétail des erreurs :\n");
                messages.forEach(m -> sb.append("  • ").append(m).append("\n"));
            }
            return sb.toString();
        }
    }

    /**
     * Exception spécifique MindSphere avec message lisible.
     */
    public static class MindSphereException extends Exception {
        public MindSphereException(String message)                       { super(message); }
        public MindSphereException(String message, Throwable cause)      { super(message, cause); }
    }
}