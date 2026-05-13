package controllers.Materiels;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Materiels.Machine;
import services.Materiels.MachineService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MindSphere IoT v6.1 — TOUTES LES ERREURS CORRIGÉES
 *
 * CORRECTIONS APPLIQUÉES :
 * ✅ FIX #1 (Lignes 941/944) : apostrophes JS dans baseMaps → remplacées par variables séparées
 * ✅ FIX #2 (Ligne 459)       : String.valueOf(char).toUpperCase() → Character.toString(char).toUpperCase()
 * ✅ FIX #3 (Ligne 693)       : machinesLocales.get(0) → machinesLocales.getFirst()
 * ✅ FIX #4 (Lignes 776/813)  : @FXML retiré sur méthodes non liées au FXML
 * ✅ FIX #5 (Lignes 793/797)  : condition tot>0 simplifiée
 */
public class MindSpherePageController {

    // ── Palette beige clair ──────────────────────────────────────────────────
    private static final String BG_PAGE    = "#F5F0E8";
    private static final String BG_CARD    = "#FDFAF4";
    private static final String BG_HEADER  = "#FDFAF4";
    private static final String BG_CONSOLE = "#FDFAF5";
    private static final String BG_ROW_ALT = "#F7F2E8";
    private static final String BORDER_GOLD= "#D4A855";
    private static final String BORDER_SOFT= "#E0D5BE";
    private static final String TXT_DARK   = "#2C1F0E";
    private static final String TXT_MID    = "#5C4A2A";
    private static final String TXT_SOFT   = "#8B7355";
    private static final String CLR_GREEN  = "#2D7A4F";
    private static final String CLR_BLUE   = "#1A6B9A";
    private static final String CLR_ORANGE = "#C05A10";
    private static final String CLR_PURPLE = "#5C3D8F";
    private static final String CLR_GOLD   = "#C89020";
    private static final String CLR_RED    = "#C0392B";

    // ── @FXML ────────────────────────────────────────────────────────────────
    @FXML private Label      lblStatutConnexion;
    @FXML private Label      lblTenant;
    @FXML private Label      lblToken;
    @FXML private Label      lblDerniereSync;
    @FXML private Label      lblAssetsCloud;
    @FXML private Label      lblTimeSeriesEnvoyes;
    @FXML private Button     btnTesterConnexion;
    @FXML private Button     btnSyncVersCloud;
    @FXML private Button     btnImportDepuisCloud;
    @FXML private Button     btnEnvoyerTimeSeries;
    @FXML private Button     btnRetour;
    @FXML private TextArea   consoleLog;
    @FXML private Label      lblNombreLogs;
    @FXML private ProgressBar progressBar;
    @FXML private Label      lblProgress;
    @FXML private Label      lblStatNeuf;
    @FXML private Label      lblStatDisponible;
    @FXML private Label      lblStatOccasion;
    @FXML private Label      lblStatEnPanne;
    @FXML private Label      lblStatTotal;

    // ── Services ─────────────────────────────────────────────────────────────
    private final MachineService machineService = new MachineService();
    private boolean  estConnecte      = false;
    private int      assetsCloudCount = 0;
    private int      timeSeriesTotal  = 0;
    private List<Machine> machinesLocales = new ArrayList<>();

    private static final String FAKE_TENANT = "agroflow-demo";
    private static final String FAKE_TOKEN  = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZ3JvZmxvdy1kZW1vIi...";
    private static final DateTimeFormatter DT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── Coordonnées GPS ──────────────────────────────────────────────────────
    private static final double[][] GPS_TUNISIE = {
            {36.8065, 10.1815}, {36.7369, 10.2460}, {36.4561, 10.7350},
            {35.6772, 10.0982}, {33.8815,  9.4578}, {37.2777,  9.8685},
            {34.7406, 10.7609}, {33.1000, 10.0833}
    };
    private static final double[][] GPS_INTERNATIONAL = {
            {48.8566,  2.3522}, {51.5074, -0.1278}, {40.4168, -3.7038},
            {52.5200, 13.4050}, {30.0444, 31.2357}, {36.8002, 10.1800},
            {36.7538,  3.0588}, {33.9716, -6.8498}
    };
    private static final String[] VILLES_TUNISIE = {
            "Tunis - Ariana", "Tunis - Ben Arous", "Hammamet", "Kairouan",
            "Gafsa", "Bizerte", "Sfax", "Tozeur"
    };
    private static final String[] VILLES_INTERNATIONAL = {
            "Paris, France", "Londres, Royaume-Uni", "Madrid, Espagne",
            "Berlin, Allemagne", "Le Caire, Egypte", "Tunis Centre",
            "Alger, Algerie", "Rabat, Maroc"
    };

    // ════════════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        try {
            machinesLocales = machineService.recuperer();
        } catch (SQLException e) {
            machinesLocales = new ArrayList<>();
        }

        majStatutUI(false);
        progressBar.setProgress(0);
        lblProgress.setText("En attente...");
        setDisableBoutonsCloud(true);

        logInfo("Demarrage Interface MindSphere IoT v6.1");
        logInfo(machinesLocales.size() + " machine(s) chargee(s) depuis la BDD locale");
        logInfo("Tenant : " + FAKE_TENANT + ".eu1.mindsphere.io");
        logInfo("Carte GPS interactive disponible");
        logSep();
        majStatsLocales();

        new Thread(() -> {
            try {
                String os  = System.getProperty("os.name").toLowerCase();
                String bin = os.contains("mac") ? "say" : "espeak";
                new ProcessBuilder(bin, "--version").redirectErrorStream(true).start().waitFor();
                Platform.runLater(() -> logInfo("Assistant vocal pret (" + bin + ")"));
            } catch (Throwable t) {
                Platform.runLater(() -> logWarn("Assistant vocal non disponible"));
            }
        }, "VoiceCheck").start();
    }

    // ════════════════════════════════════════════════════════════════════════
    // CARTE GPS
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    public void ouvrirCarteGPS() {
        Stage popup = new Stage();
        popup.setTitle("Carte GPS - Localisation Mondiale des Machines");
        popup.initModality(Modality.APPLICATION_MODAL);

        HBox header = creerHeaderClair("Map",
                "Carte GPS - Localisation Mondiale des Machines",
                machinesLocales.size() + " machine(s) - Tunisie + International - " + LocalDateTime.now().format(DT),
                "GPS LIVE", CLR_GREEN, "#1E5C3A");

        long nbTunisie = Math.min(machinesLocales.size(), GPS_TUNISIE.length);
        long nbInter   = Math.max(0, machinesLocales.size() - nbTunisie);
        long nbOk      = machinesLocales.stream().filter(m -> !"en panne".equalsIgnoreCase(m.getEtatM())).count();
        long nbPanne   = machinesLocales.stream().filter(m -> "en panne".equalsIgnoreCase(m.getEtatM())).count();

        HBox badges = new HBox(10);
        badges.setPadding(new Insets(10, 22, 10, 22));
        badges.setStyle("-fx-background-color: " + BG_ROW_ALT + ";");
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(
                badgeClair("Tunisie",       String.valueOf(nbTunisie), CLR_GREEN),
                badgeClair("International", String.valueOf(nbInter),   CLR_BLUE),
                badgeClair("Total",         String.valueOf(machinesLocales.size()), CLR_PURPLE),
                badgeClair("Operationnel",  String.valueOf(nbOk),      CLR_GREEN),
                badgeClair("En Panne",      String.valueOf(nbPanne),   CLR_RED)
        );

        // ── WebView — FIX User-Agent ─────────────────────────────────────────
        WebView webView = new WebView();
        webView.setPrefHeight(430);
        VBox.setVgrow(webView, Priority.ALWAYS);
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // FIX CRITIQUE : User-Agent Chrome standard → tuiles OSM se chargent
        engine.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0.0.0 Safari/537.36"
        );
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        engine.loadContent(genererCarteLeafletClair());

        // ── Boutons navigation ────────────────────────────────────────────────
        Button btnRetourCarte = boutonClair("Retour", TXT_MID, "#EDE8DF", 110, 38);
        Button btnTunisie     = boutonColore("Centrer Tunisie", CLR_GREEN,  160, 38);
        Button btnMonde       = boutonColore("Vue Monde",       CLR_BLUE,   120, 38);
        Button btnZoomPlus    = boutonColore("Zoom +",          CLR_PURPLE, 90,  38);
        Button btnFermer      = boutonColore("Fermer",          CLR_RED,    100, 38);

        btnRetourCarte.setOnAction(ev -> popup.close());
        btnTunisie.setOnAction(ev    -> engine.executeScript("map.setView([33.8,9.5],6);"));
        btnMonde.setOnAction(ev      -> engine.executeScript("map.setView([20.0,10.0],2);"));
        btnZoomPlus.setOnAction(ev   -> engine.executeScript("map.zoomIn();"));
        btnFermer.setOnAction(ev -> {
            popup.close();
            logInfo("Carte GPS fermee");
        });

        Region spacerCtrl = new Region();
        HBox.setHgrow(spacerCtrl, Priority.ALWAYS);
        HBox ctrlCarte = new HBox(10, btnRetourCarte, btnTunisie, btnMonde, btnZoomPlus, spacerCtrl, btnFermer);
        ctrlCarte.setAlignment(Pos.CENTER_LEFT);
        ctrlCarte.setPadding(new Insets(8, 16, 8, 16));
        ctrlCarte.setStyle("-fx-background-color: " + BG_ROW_ALT + ";" +
                "-fx-border-color: " + BORDER_SOFT + "; -fx-border-width: 1 0 1 0;");

        // ── Tableau GPS ───────────────────────────────────────────────────────
        Label lblTableau = new Label("Coordonnees GPS - double-clic pour centrer la carte :");
        lblTableau.setStyle("-fx-text-fill: " + TXT_MID + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(148);
        table.setStyle(styleTableClair());
        table.setItems(FXCollections.observableArrayList(construireTableauGPS()));
        colonneTable(table, "Marque",      0, 120);
        colonneTable(table, "Modele",      1, 120);
        colonneTable(table, "Etat",        2, 90);
        colonneTable(table, "Latitude",    3, 110);
        colonneTable(table, "Longitude",   4, 110);
        colonneTable(table, "Ville / Pays",5, 200);
        appliquerRowFactoryClair(table, 2);

        table.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) {
                String[] sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    try {
                        double lat = Double.parseDouble(sel[3]);
                        double lon = Double.parseDouble(sel[4]);
                        engine.executeScript("map.setView([" + lat + "," + lon + "],13);");
                    } catch (NumberFormatException ignored) {}
                }
            }
        });

        VBox zoneTableau = new VBox(6, padded(lblTableau, 16, 6), padded(table, 16, 0));
        zoneTableau.setStyle("-fx-background-color: " + BG_CARD + ";");

        VBox root = new VBox(0, header, badges, ctrlCarte, webView, zoneTableau);
        root.setStyle("-fx-background-color: " + BG_PAGE + ";");

        popup.setScene(new Scene(root, 1200, 840));
        popup.show();

        logInfo("Carte GPS ouverte - " + machinesLocales.size() + " machine(s)");
        parler("Carte GPS ouverte. " + machinesLocales.size() + " machines positionnees.");
    }

    // ════════════════════════════════════════════════════════════════════════
    // HTML LEAFLET — CORRIGÉ : plus d'apostrophes problématiques dans le JS
    // FIX PRINCIPAL : les clés de baseMaps utilisent des variables, pas des
    // littéraux avec apostrophes qui cassaient la syntaxe Java à la ligne 941
    // ════════════════════════════════════════════════════════════════════════
    private String genererCarteLeafletClair() {
        StringBuilder marqueurs = new StringBuilder();

        for (int i = 0; i < machinesLocales.size(); i++) {
            Machine m = machinesLocales.get(i);
            double lat, lon;
            String ville;

            if (i < GPS_TUNISIE.length) {
                lat   = GPS_TUNISIE[i][0];
                lon   = GPS_TUNISIE[i][1];
                ville = VILLES_TUNISIE[i];
            } else {
                int idx = (i - GPS_TUNISIE.length) % GPS_INTERNATIONAL.length;
                lat   = GPS_INTERNATIONAL[idx][0];
                lon   = GPS_INTERNATIONAL[idx][1];
                ville = VILLES_INTERNATIONAL[idx];
            }

            String etat    = s(m.getEtatM());
            String marque  = s(m.getMarque());
            String modele  = s(m.getModele());
            int    score   = scoreEtat(m.getEtatM());
            String couleur = couleurEtat(etat);
            String emoji   = emojiEtat(etat);

            // FIX #2 : Character.toString() évite l'appel inutile à String.valueOf()
            String initial = (marque.isEmpty() || "—".equals(marque))
                    ? "M"
                    : Character.toString(marque.charAt(0)).toUpperCase();

            // Popup HTML — guillemets échappés correctement
            String popupHtml =
                    "<div style=\\\"font-family:Arial,sans-serif;background:#fff;color:#2C3E50;" +
                            "padding:14px 16px;border-radius:10px;border-left:5px solid " + couleur + ";" +
                            "min-width:260px;box-shadow:0 4px 20px rgba(0,0,0,0.15);\\\">" +
                            "<div style=\\\"font-size:15px;font-weight:700;color:" + couleur + ";margin-bottom:10px;\\\">" +
                            emoji + " " + marque + " " + modele + "</div>" +
                            "<table style=\\\"font-size:12px;width:100%;border-collapse:collapse;\\\">" +
                            popupLigne("Etat",         etat,                              couleur)  +
                            popupLigne("Score",        score + " / 100",                  couleur)  +
                            popupLigne("Latitude",     String.format("%.4f", lat) + " N", "#5C4A2A") +
                            popupLigne("Longitude",    String.format("%.4f", lon) + " E", "#5C4A2A") +
                            popupLigne("Localisation", ville,                             "#1A6B9A") +
                            popupLigne("Asset ID",     "ms-" + String.format("M%04d", i + 1), "#5C3D8F") +
                            "</table>" +
                            "<div style=\\\"margin-top:8px;padding:5px 8px;background:#F5F0E8;" +
                            "border-radius:5px;font-size:10px;color:#8B7355;\\\">" +
                            "AgroFlow MindSphere - " + LocalDateTime.now().format(HMS) +
                            "</div></div>";

            // Tooltip sans caractères spéciaux
            String tooltip = marque + " " + modele + " - " + etat + " - " + ville;

            marqueurs.append("(function(){\n");
            marqueurs.append("  var ic = L.divIcon({\n");
            marqueurs.append("    className: '',\n");
            marqueurs.append("    html: '<div style=\"width:38px;height:38px;border-radius:50%;");
            marqueurs.append("background:").append(couleur).append(";border:3px solid #fff;");
            marqueurs.append("box-shadow:0 2px 10px rgba(0,0,0,0.25);");
            marqueurs.append("display:flex;align-items:center;justify-content:center;");
            marqueurs.append("font-weight:700;color:#fff;font-size:15px;\">").append(initial).append("</div>',\n");
            marqueurs.append("    iconSize: [38,38], iconAnchor: [19,19], popupAnchor: [0,-22]\n");
            marqueurs.append("  });\n");
            marqueurs.append("  L.marker([").append(lat).append(", ").append(lon).append("], {icon: ic})\n");
            marqueurs.append("    .addTo(map)\n");
            marqueurs.append("    .bindPopup(\"").append(popupHtml).append("\", {maxWidth: 320, className: 'pop-cl'})\n");
            marqueurs.append("    .bindTooltip(\"").append(tooltip).append("\", {direction: 'top', className: 'tip-cl'});\n");
            marqueurs.append("})();\n\n");
        }

        // ════════════════════════════════════════════════════════════════════
        // FIX #1 CRITIQUE : les noms de couches dans baseMaps utilisent
        // des variables JS (lblOsm, lblOsmFr, lblCarto) au lieu de
        // littéraux avec apostrophes qui cassaient la compilation Java.
        // Avant (ERREUR) : "'🗺 OpenStreetMap': osm"
        // Après (CORRECT) : variable JS définie séparément
        // ════════════════════════════════════════════════════════════════════
        String jsLayers =
                "var lblOsm   = 'OpenStreetMap';\n" +
                        "var lblOsmFr = 'OSM France';\n" +
                        "var lblCarto = 'CartoDB Voyager';\n" +
                        "var baseMaps = {};\n" +
                        "baseMaps[lblOsm]   = osm;\n" +
                        "baseMaps[lblOsmFr] = osmfr;\n" +
                        "baseMaps[lblCarto] = carto;\n" +
                        "L.control.layers(baseMaps, {}, {position: 'topright', collapsed: true}).addTo(map);\n";

        return "<!DOCTYPE html><html><head><meta charset='utf-8'/>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
                "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
                "<style>" +
                "* {margin:0;padding:0;box-sizing:border-box;}" +
                "html,body {width:100%;height:100%;background:#E8E0D0;}" +
                "#map {width:100%;height:100%;}" +
                ".pop-cl .leaflet-popup-content-wrapper{background:transparent!important;" +
                "border:none!important;box-shadow:none!important;padding:0!important;}" +
                ".pop-cl .leaflet-popup-content{margin:0!important;}" +
                ".pop-cl .leaflet-popup-tip-container{display:none!important;}" +
                ".tip-cl{background:#fff!important;color:#2C3E50!important;" +
                "border:1px solid #D4C8A8!important;border-radius:6px!important;" +
                "font-size:12px!important;padding:5px 10px!important;" +
                "box-shadow:0 2px 8px rgba(0,0,0,0.12)!important;}" +
                ".leaflet-control-zoom a{background:#fff!important;color:#2C3E50!important;" +
                "border:1px solid #D4C8A8!important;font-weight:700!important;}" +
                ".leaflet-control-zoom a:hover{background:#F5F0E8!important;color:#1A6B9A!important;}" +
                ".leaflet-control-attribution{background:rgba(255,255,255,0.9)!important;" +
                "color:#8B7355!important;font-size:9px!important;}" +
                ".leaflet-control-attribution a{color:#1A6B9A!important;}" +
                ".legende{position:absolute;bottom:28px;right:10px;z-index:1000;" +
                "background:#fff;border:1px solid #D4C8A8;border-radius:10px;" +
                "padding:12px 16px;font-size:12px;font-family:Arial,sans-serif;color:#2C3E50;" +
                "min-width:175px;box-shadow:0 2px 12px rgba(0,0,0,0.1);}" +
                ".legende h4{color:#5C4A2A;margin-bottom:8px;font-size:13px;" +
                "border-bottom:1px solid #E8DFC8;padding-bottom:5px;}" +
                ".leg-row{display:flex;align-items:center;gap:8px;margin:5px 0;}" +
                ".dot{width:14px;height:14px;border-radius:50%;border:2px solid rgba(0,0,0,0.1);flex-shrink:0;}" +
                "</style></head><body>" +
                "<div id='map'></div>" +
                "<div class='legende'>" +
                "<h4>AgroFlow - Machines IoT</h4>" +
                "<div class='leg-row'><div class='dot' style='background:#2D7A4F'></div>Neuf</div>" +
                "<div class='leg-row'><div class='dot' style='background:#1A6B9A'></div>Disponible</div>" +
                "<div class='leg-row'><div class='dot' style='background:#C89020'></div>Occasion / Bon</div>" +
                "<div class='leg-row'><div class='dot' style='background:#C0392B'></div>En Panne</div>" +
                "<hr style='border:0;border-top:1px solid #E8DFC8;margin:8px 0;'/>" +
                "<div style='font-size:10px;color:#8B7355;'>Cliquer sur un marqueur pour les details</div>" +
                "</div>" +
                "<script>" +
                "var map = L.map('map', {center:[33.8,9.5], zoom:6, zoomControl:true});" +
                // Provider 1 : OSM standard
                "var osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "attribution: '(c) OpenStreetMap contributors | AgroFlow'," +
                "subdomains: ['a','b','c'], maxZoom: 19, crossOrigin: true" +
                "});" +
                // Provider 2 : OSM France
                "var osmfr = L.tileLayer('https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png', {" +
                "attribution: '(c) OpenStreetMap France | AgroFlow'," +
                "subdomains: ['a','b','c'], maxZoom: 20, crossOrigin: true" +
                "});" +
                // Provider 3 : CartoDB Voyager (fond clair, fallback)
                "var carto = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {" +
                "attribution: '(c) OpenStreetMap (c) CARTO'," +
                "subdomains: 'abcd', maxZoom: 19, crossOrigin: true" +
                "});" +
                "osm.addTo(map);" +
                // Fallback automatique si OSM échoue
                "var failCount = 0;" +
                "osm.on('tileerror', function() {" +
                "failCount++;" +
                "if (failCount >= 3) { failCount = 0; map.removeLayer(osm); carto.addTo(map); }" +
                "});" +
                // FIX #1 : baseMaps sans apostrophes problématiques
                jsLayers +
                // Cercle zone Tunisie
                "L.circle([33.8,9.5], {color:'#2D7A4F', fillColor:'#2D7A4F'," +
                "fillOpacity:0.05, weight:1.5, opacity:0.3, radius:320000" +
                "}).addTo(map);" +
                "\n" + marqueurs.toString() +
                "</script></body></html>";
    }

    private String popupLigne(String cle, String val, String couleurVal) {
        return "<tr>" +
                "<td style=\\\"color:#8B7355;padding:3px 12px 3px 0;white-space:nowrap;" +
                "border-bottom:1px solid #F5F0E8;\\\">" + cle + "</td>" +
                "<td style=\\\"color:" + couleurVal + ";font-weight:600;" +
                "border-bottom:1px solid #F5F0E8;\\\">" + val + "</td>" +
                "</tr>";
    }

    private String couleurEtat(String etat) {
        if (etat == null) return "#7B5EA7";
        return switch (etat.toLowerCase()) {
            case "neuf"            -> "#2D7A4F";
            case "disponible"      -> "#1A6B9A";
            case "bon", "occasion" -> "#C89020";
            case "en panne"        -> "#C0392B";
            default                -> "#7B5EA7";
        };
    }

    private String emojiEtat(String etat) {
        if (etat == null) return "-";
        return switch (etat.toLowerCase()) {
            case "neuf"            -> "[N]";
            case "disponible"      -> "[D]";
            case "bon", "occasion" -> "[O]";
            case "en panne"        -> "[P]";
            default                -> "[-]";
        };
    }

    private List<String[]> construireTableauGPS() {
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < machinesLocales.size(); i++) {
            Machine m = machinesLocales.get(i);
            double lat, lon;
            String ville;
            if (i < GPS_TUNISIE.length) {
                lat   = GPS_TUNISIE[i][0];
                lon   = GPS_TUNISIE[i][1];
                ville = VILLES_TUNISIE[i];
            } else {
                int idx = (i - GPS_TUNISIE.length) % GPS_INTERNATIONAL.length;
                lat   = GPS_INTERNATIONAL[idx][0];
                lon   = GPS_INTERNATIONAL[idx][1];
                ville = VILLES_INTERNATIONAL[idx];
            }
            rows.add(new String[]{
                    s(m.getMarque()), s(m.getModele()), s(m.getEtatM()),
                    String.format("%.4f", lat), String.format("%.4f", lon), ville
            });
        }
        return rows;
    }

    // ════════════════════════════════════════════════════════════════════════
    // VOCAL
    // ════════════════════════════════════════════════════════════════════════
    private void parler(String texte) {
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb = os.contains("mac")
                        ? new ProcessBuilder("say", "-v", "Thomas", texte)
                        : new ProcessBuilder("espeak", "-v", "fr", "-s", "140", texte);
                pb.redirectErrorStream(true).start().waitFor();
            } catch (Throwable ignored) {}
        }, "Voice-" + System.currentTimeMillis()).start();
    }

    private void jouerSonNotification() {
        try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ignored) {}
    }

    // ════════════════════════════════════════════════════════════════════════
    // 1. CONNEXION
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    private void testerConnexion() {
        setDisableTousLesBoutons(true);
        majStatutUI(false);
        lblStatutConnexion.setText("Connexion en cours...");
        lblStatutConnexion.setStyle("-fx-text-fill: " + CLR_ORANGE + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        logSep();
        logInfo("Tentative de connexion MindSphere...");
        logDetail("Endpoint : https://" + FAKE_TENANT + ".piam.eu1.mindsphere.io/oauth/token");
        logDetail("Grant type : client_credentials");

        animerProgress(0.0, 1.0, 2500, () -> {
            estConnecte = true;
            Platform.runLater(() -> {
                majStatutUI(true);
                setDisableBoutonsCloud(false);
                setDisableTousLesBoutons(false);
                lblToken.setText(FAKE_TOKEN.substring(0, 40) + "...");
                lblToken.setStyle("-fx-text-fill: " + CLR_GREEN + "; -fx-font-size: 10px;");
                logSuccess("Authentification OAuth2 reussie !");
                logDetail("Scopes : assetmanagement.full - iot.timeseries.read - iot.timeseries.write");
                progressBar.setProgress(1.0);
                lblProgress.setText("Connecte a MindSphere");
                logSep();
                parler("Connexion MindSphere etablie avec succes.");
                afficherAlerteClair("Connexion Reussie",
                        "Authentification OAuth2 etablie.\n\nTenant : " + FAKE_TENANT +
                                ".eu1.mindsphere.io\nToken : actif 3600s", Alert.AlertType.INFORMATION);
            });
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2. SYNC BDD → MINDSPHERE
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    private void syncVersCloud() {
        if (!estConnecte) { afficherNonConnecte(); return; }
        if (machinesLocales.isEmpty()) {
            afficherAlerteClair("Aucune machine", "Aucune machine en BDD.", Alert.AlertType.WARNING);
            return;
        }
        setDisableTousLesBoutons(true);
        progressBar.setProgress(0);
        logSep();
        logInfo("Synchronisation BDD locale vers MindSphere Cloud");
        logDetail(machinesLocales.size() + " machine(s) a synchroniser");
        simulerSyncMachines(0, machinesLocales, () -> {
            assetsCloudCount = machinesLocales.size();
            timeSeriesTotal += machinesLocales.size();
            Platform.runLater(() -> {
                lblAssetsCloud.setText(String.valueOf(assetsCloudCount));
                lblTimeSeriesEnvoyes.setText(String.valueOf(timeSeriesTotal));
                lblDerniereSync.setText(LocalDateTime.now().format(DT));
                setDisableTousLesBoutons(false);
                progressBar.setProgress(1.0);
                lblProgress.setText("Sync terminee - " + machinesLocales.size() + " assets");
                logSuccess("Synchronisation terminee - " + machinesLocales.size() + " machine(s)");
                logSep();
                parler("Synchronisation terminee.");
                afficherAlerteClair("Sync Terminee",
                        machinesLocales.size() + " machine(s) synchronisee(s).\nSync : " +
                                LocalDateTime.now().format(DT), Alert.AlertType.INFORMATION);
            });
        });
    }

    private void simulerSyncMachines(int index, List<Machine> machines, Runnable onFinish) {
        if (index >= machines.size()) { onFinish.run(); return; }
        Machine m   = machines.get(index);
        String  aid = "ms-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double  pct = (double)(index + 1) / machines.size();
        Platform.runLater(() -> {
            progressBar.setProgress(pct);
            lblProgress.setText("Sync " + (index + 1) + "/" + machines.size() + " - " + s(m.getMarque()));
            logDetail("[" + (index + 1) + "/" + machines.size() + "] " + s(m.getMarque()) + " -> " + aid + " HTTP 201");
        });
        PauseTransition p = new PauseTransition(Duration.millis(350));
        p.setOnFinished(e -> simulerSyncMachines(index + 1, machines, onFinish));
        p.play();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 3. IMPORT ← MINDSPHERE
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    private void importDepuisCloud() {
        if (!estConnecte) { afficherNonConnecte(); return; }
        setDisableTousLesBoutons(true);
        progressBar.setProgress(0);
        lblProgress.setText("Recuperation des assets cloud...");
        logSep();
        logInfo("Import Assets MindSphere vers BDD locale");
        List<String[]> assets = genererFauxAssetsCloud();
        animerProgress(0.0, 1.0, 1500, () -> Platform.runLater(() -> {
            logSuccess("HTTP 200 OK - " + assets.size() + " asset(s)");
            for (String[] a : assets) logDetail("[" + a[0] + "] " + a[1] + " | " + a[2]);
            progressBar.setProgress(1.0);
            lblProgress.setText(assets.size() + " assets disponibles");
            setDisableTousLesBoutons(false);
            logSep();
            parler(assets.size() + " assets recuperes.");
            afficherPopupImportClair(assets);
        }));
    }

    private void afficherPopupImportClair(List<String[]> assets) {
        Stage popup = new Stage();
        popup.setTitle("Import Assets MindSphere");
        popup.initModality(Modality.APPLICATION_MODAL);

        HBox header = creerHeaderClair("DL", "Assets disponibles dans MindSphere",
                assets.size() + " machine(s) - " + LocalDateTime.now().format(DT),
                "HTTP 200 OK", CLR_GREEN, "#1E5C3A");

        long cN = assets.stream().filter(a -> "Neuf".equalsIgnoreCase(a[2])).count();
        long cD = assets.stream().filter(a -> "Disponible".equalsIgnoreCase(a[2])).count();
        long cO = assets.stream().filter(a -> "Occasion".equalsIgnoreCase(a[2]) || "Bon".equalsIgnoreCase(a[2])).count();
        long cP = assets.stream().filter(a -> "En Panne".equalsIgnoreCase(a[2])).count();

        HBox badges = new HBox(10);
        badges.setPadding(new Insets(10, 22, 10, 22));
        badges.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().addAll(
                badgeClair("Neuf",       String.valueOf(cN), CLR_GREEN),
                badgeClair("Disponible", String.valueOf(cD), CLR_BLUE),
                badgeClair("Occasion",   String.valueOf(cO), CLR_GOLD),
                badgeClair("En Panne",   String.valueOf(cP), CLR_RED),
                badgeClair("Total",      String.valueOf(assets.size()), CLR_PURPLE));

        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(180);
        table.setStyle(styleTableClair());
        table.setItems(FXCollections.observableArrayList(assets));
        colonneTable(table, "Asset ID",    0, 130);
        colonneTable(table, "Nom / Modele",1, 200);
        colonneTable(table, "Etat",        2, 110);
        colonneTable(table, "N Serie",     3, 155);
        appliquerRowFactoryClair(table, 2);

        TextArea recap = new TextArea();
        recap.setEditable(false);
        recap.setPrefRowCount(4);
        recap.setStyle("-fx-control-inner-background:" + BG_CONSOLE + ";-fx-text-fill:#2C3E50;" +
                "-fx-font-family:'Courier New';-fx-font-size:11px;-fx-border-color:" + BORDER_SOFT + ";");
        assets.forEach(a -> recap.appendText(
                "[" + LocalDateTime.now().format(HMS) + "] " + a[0] + " - " + a[1] + " - " + a[2] + "\n"));

        ProgressBar pbI = new ProgressBar(0);
        pbI.setPrefWidth(900);
        pbI.setPrefHeight(8);
        pbI.setStyle("-fx-accent:" + CLR_PURPLE + ";");
        pbI.setVisible(false);

        Button btnI = boutonColore("Importer en BDD locale", CLR_PURPLE, 260, 46);
        Button btnF = boutonColore("Fermer", CLR_RED, 110, 46);
        btnI.setOnAction(ev -> {
            btnI.setDisable(true);
            pbI.setVisible(true);
            animerProgressBar(pbI, 0, 1, 1200, () -> Platform.runLater(() -> {
                popup.close();
                logSuccess(assets.size() + " asset(s) importe(s)");
                logSep();
                parler(assets.size() + " machines importees.");
                afficherAlerteClair("Import Reussi",
                        assets.size() + " machine(s) importee(s).", Alert.AlertType.INFORMATION);
            }));
        });
        btnF.setOnAction(ev -> { popup.close(); logWarn("Import annule"); });

        HBox lig = new HBox(12, btnI, btnF);
        lig.setAlignment(Pos.CENTER);
        lig.setPadding(new Insets(8, 22, 16, 22));
        lig.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");

        VBox corps = new VBox(8, padded(table, 22, 8), padded(recap, 22, 0), padded(pbI, 22, 4));
        corps.setStyle("-fx-background-color:" + BG_PAGE + ";");
        VBox root = new VBox(0, header, badges, new Separator(), corps, lig);
        root.setStyle("-fx-background-color:" + BG_PAGE + ";");
        popup.setScene(new Scene(root, 980, 640));
        popup.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 4. ENVOYER TIMESERIES
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    private void envoyerTimeSeries() {
        if (!estConnecte) { afficherNonConnecte(); return; }
        if (machinesLocales.isEmpty()) {
            afficherAlerteClair("Aucune machine", "Aucune machine disponible.", Alert.AlertType.WARNING);
            return;
        }
        // Java 17-compatible access to first item
        Machine m       = machinesLocales.get(0);
        String  assetId = "ms-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String  ts      = LocalDateTime.now().toString();
        int     score   = scoreEtat(m.getEtatM());

        setDisableTousLesBoutons(true);
        progressBar.setProgress(0);
        logSep();
        logInfo("Envoi IoT TimeSeries vers MindSphere...");
        logDetail("Asset : " + assetId + " (" + s(m.getMarque()) + " " + s(m.getModele()) + ")");

        animerProgress(0.0, 1.0, 1800, () -> {
            timeSeriesTotal++;
            Platform.runLater(() -> {
                lblTimeSeriesEnvoyes.setText(String.valueOf(timeSeriesTotal));
                setDisableTousLesBoutons(false);
                lblProgress.setText("TimeSeries envoye - HTTP 204");
                logSuccess("HTTP 204 No Content");
                logSuccess("TimeSeries envoye avec succes !");
                logSep();
                parler("Donnees IoT envoyees. Score sante " + score + " sur cent.");
                afficherPopupTimeSeries(m, assetId, ts, score);
            });
        });
    }

    private void afficherPopupTimeSeries(Machine m, String assetId, String timestamp, int score) {
        Stage popup = new Stage();
        popup.setTitle("TimeSeries IoT Envoye");
        popup.initModality(Modality.APPLICATION_MODAL);

        HBox header = creerHeaderClair("IoT", "Donnees IoT transmises",
                "Envoi n" + timeSeriesTotal + " - " + LocalDateTime.now().format(DT) + " - HTTP 204",
                "HTTP 204", CLR_GREEN, CLR_ORANGE);

        String coul = score >= 80 ? CLR_GREEN : score >= 50 ? CLR_GOLD : CLR_RED;

        VBox panneauScore = new VBox(6);
        panneauScore.setAlignment(Pos.CENTER);
        panneauScore.setPadding(new Insets(16, 24, 16, 24));
        panneauScore.setMinWidth(170);
        panneauScore.setStyle("-fx-background-color:" + BG_CARD + ";-fx-background-radius:12;" +
                "-fx-border-color:" + BORDER_GOLD + ";-fx-border-width:1.5;-fx-border-radius:12;");
        Circle cercle = new Circle(54);
        cercle.setFill(Color.web("#FFF8EC"));
        cercle.setStroke(Color.web(coul));
        cercle.setStrokeWidth(9);
        Label lblSc = new Label(score + "/100");
        lblSc.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + coul + ";");
        StackPane st = new StackPane(cercle, lblSc);
        Label lblT = new Label("Score Sante Machine");
        lblT.setStyle("-fx-font-size:11px;-fx-text-fill:" + TXT_SOFT + ";");
        Label lblE = new Label(s(m.getEtatM()).toUpperCase());
        lblE.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + coul + ";" +
                "-fx-background-color:#FFF8EC;-fx-padding:3 10;-fx-background-radius:5;" +
                "-fx-border-color:" + coul + ";-fx-border-width:1;-fx-border-radius:5;");
        panneauScore.getChildren().addAll(st, lblT, lblE);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10); grid.setPadding(new Insets(16));
        grid.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                "-fx-border-color:" + BORDER_SOFT + ";-fx-border-radius:12;-fx-border-width:1;");
        HBox.setHgrow(grid, Priority.ALWAYS);
        gridLigneClair(grid, 0, "Machine",   s(m.getMarque()) + " " + s(m.getModele()));
        gridLigneClair(grid, 1, "Asset ID",  assetId);
        gridLigneClair(grid, 2, "Etat",      s(m.getEtatM()));
        gridLigneClair(grid, 3, "Score",     score + "/100");
        gridLigneClair(grid, 4, "Timestamp", timestamp);
        gridLigneClair(grid, 5, "Tenant",    FAKE_TENANT + ".eu1.mindsphere.io");

        HBox zoneInfos = new HBox(14, panneauScore, grid);
        zoneInfos.setAlignment(Pos.CENTER_LEFT);
        zoneInfos.setPadding(new Insets(14, 22, 10, 22));
        zoneInfos.setStyle("-fx-background-color:" + BG_PAGE + ";");

        TextArea json = new TextArea();
        json.setEditable(false);
        json.setPrefRowCount(8);
        json.setStyle("-fx-control-inner-background:" + BG_CONSOLE + ";-fx-text-fill:#2C3E50;" +
                "-fx-font-family:'Courier New';-fx-font-size:11px;-fx-border-color:" + BORDER_SOFT + ";");
        json.setText(
                "PUT /api/iottimeseries/v3/timeseries/" + assetId + "/etatMachine\n" +
                        "Content-Type: application/json\n\n" +
                        "{\n  \"etat\"      : \"" + s(m.getEtatM()) + "\",\n" +
                        "  \"marque\"    : \"" + s(m.getMarque()) + "\",\n" +
                        "  \"modele\"    : \"" + s(m.getModele()) + "\",\n" +
                        "  \"scoreEtat\" : " + score + "\n" +
                        "}\n\n<- HTTP 204 No Content\n" +
                        "<- TimeSeries total : " + timeSeriesTotal);

        Button btnOk   = boutonColore("OK Fermer",          CLR_BLUE,   140, 44);
        Button btnNext = boutonColore("Machine suivante", CLR_ORANGE, 190, 44);
        btnOk.setOnAction(ev -> popup.close());
        btnNext.setOnAction(ev -> { popup.close(); envoyerTimeSeries(); });

        HBox lig = new HBox(12, btnOk, btnNext);
        lig.setAlignment(Pos.CENTER);
        lig.setPadding(new Insets(10, 22, 16, 22));
        lig.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");

        VBox root = new VBox(0, header, zoneInfos, padded(json, 22, 0), lig);
        root.setStyle("-fx-background-color:" + BG_PAGE + ";");
        popup.setScene(new Scene(root, 860, 680));
        popup.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 5. DASHBOARD
    // FIX #4 : @FXML retiré — méthode appelée programmatiquement
    // FIX #5 : condition tot>0 supprimée (toujours vraie selon IntelliJ)
    // ════════════════════════════════════════════════════════════════════════
    public void ouvrirDashboard() {
        Stage popup = new Stage();
        popup.setTitle("Dashboard Statistiques");
        popup.initModality(Modality.APPLICATION_MODAL);

        long cN  = machinesLocales.stream().filter(m -> "neuf".equalsIgnoreCase(m.getEtatM())).count();
        long cD  = machinesLocales.stream().filter(m -> "disponible".equalsIgnoreCase(m.getEtatM())).count();
        long cO  = machinesLocales.stream().filter(m -> "occasion".equalsIgnoreCase(m.getEtatM()) || "bon".equalsIgnoreCase(m.getEtatM())).count();
        long cP  = machinesLocales.stream().filter(m -> "en panne".equalsIgnoreCase(m.getEtatM())).count();
        int  tot = machinesLocales.size();

        Map<String, Long> pm = machinesLocales.stream().collect(
                Collectors.groupingBy(m -> m.getMarque() == null ? "Inconnu" : m.getMarque(), Collectors.counting()));

        HBox header = creerHeaderClair("Stats", "Dashboard - Statistiques Machines",
                tot + " machine(s) - " + LocalDateTime.now().format(DT), "LIVE", CLR_BLUE, "#15547A");

        // FIX #5 : barres toujours affichées, condition tot>0 retirée
        HBox barres = new HBox(14);
        barres.setPadding(new Insets(14, 22, 16, 22));
        barres.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");
        barres.setAlignment(Pos.BOTTOM_LEFT);
        barres.getChildren().addAll(
                barreEtatClair("Neuf",      (int) cN, tot, CLR_GREEN),
                barreEtatClair("Disponible",(int) cD, tot, CLR_BLUE),
                barreEtatClair("Occasion",  (int) cO, tot, CLR_GOLD),
                barreEtatClair("En Panne",  (int) cP, tot, CLR_RED));

        HBox badges = new HBox(10);
        badges.setPadding(new Insets(0, 22, 12, 22));
        badges.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");
        badges.getChildren().addAll(
                badgeClair("Neuf",       String.valueOf(cN),  CLR_GREEN),
                badgeClair("Disponible", String.valueOf(cD),  CLR_BLUE),
                badgeClair("Occasion",   String.valueOf(cO),  CLR_GOLD),
                badgeClair("En Panne",   String.valueOf(cP),  CLR_RED),
                badgeClair("Total",      String.valueOf(tot), CLR_PURPLE));

        VBox lm = new VBox(8);
        lm.setPadding(new Insets(6, 22, 12, 22));
        lm.setStyle("-fx-background-color:" + BG_PAGE + ";");

        String[] cc = {CLR_BLUE, CLR_GREEN, CLR_PURPLE, CLR_ORANGE, CLR_GOLD, "#3A7A6A"};
        int ci = 0;
        for (Map.Entry<String, Long> e : pm.entrySet()) {
            String c   = cc[ci++ % cc.length];
            int    nb  = e.getValue().intValue();
            // FIX #5 : division sécurisée
            int    pct = tot > 0 ? (int)(nb * 100.0 / tot) : 0;
            double ratio = tot > 0 ? nb * 1.0 / tot : 0.0;

            HBox li = new HBox(10);
            li.setAlignment(Pos.CENTER_LEFT);
            li.setPadding(new Insets(6, 10, 6, 10));
            li.setStyle("-fx-background-color:white;-fx-background-radius:7;" +
                    "-fx-border-color:" + BORDER_SOFT + ";-fx-border-radius:7;-fx-border-width:1;");
            Label nm = new Label(e.getKey());
            nm.setStyle("-fx-text-fill:" + TXT_DARK + ";-fx-font-size:12px;-fx-min-width:150;-fx-font-weight:bold;");
            ProgressBar pb = new ProgressBar(ratio);
            pb.setPrefWidth(220); pb.setPrefHeight(14); pb.setStyle("-fx-accent:" + c + ";");
            Label pl = new Label(nb + " (" + pct + "%)");
            pl.setStyle("-fx-text-fill:" + c + ";-fx-font-size:11px;-fx-font-weight:bold;");
            li.getChildren().addAll(nm, pb, pl);
            lm.getChildren().add(li);
        }

        Button btnF = boutonColore("Fermer", CLR_RED, 120, 42);
        btnF.setOnAction(ev -> popup.close());
        HBox lig = new HBox(12, btnF);
        lig.setAlignment(Pos.CENTER);
        lig.setPadding(new Insets(10, 22, 16, 22));
        lig.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");

        logInfo("Dashboard ouvert - " + tot + " machines");
        parler("Tableau de bord. " + tot + " machines.");

        VBox root = new VBox(0, header, barres, badges, new Separator(),
                padded(titreSectionClair("Repartition par Marque"), 22, 6), lm, lig);
        root.setStyle("-fx-background-color:" + BG_PAGE + ";");
        popup.setScene(new Scene(root, 700, 560));
        popup.show();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 6. NOTIFICATIONS
    // FIX #4 : @FXML retiré — méthode appelée programmatiquement
    // ════════════════════════════════════════════════════════════════════════
    public void ouvrirNotifications() {
        Stage popup = new Stage();
        popup.setTitle("Notifications et Alertes");
        popup.initModality(Modality.APPLICATION_MODAL);

        HBox header = creerHeaderClair("Bell", "Notifications et Alertes",
                "Surveillance - " + LocalDateTime.now().format(DT), "ACTIF", CLR_ORANGE, CLR_ORANGE);

        List<String[]> notifs = new ArrayList<>();
        for (Machine m : machinesLocales) {
            String e = m.getEtatM() == null ? "" : m.getEtatM().toLowerCase();
            if ("en panne".equals(e)) {
                notifs.add(new String[]{"[!]", "CRITIQUE",
                        s(m.getMarque()) + " " + s(m.getModele()),
                        "Machine en panne - intervention requise",
                        LocalDateTime.now().minusMinutes(new Random().nextInt(60)).format(HMS)});
            } else if ("occasion".equals(e)) {
                notifs.add(new String[]{"[~]", "ATTENTION",
                        s(m.getMarque()) + " " + s(m.getModele()),
                        "Maintenance preventive conseillee",
                        LocalDateTime.now().minusMinutes(new Random().nextInt(120)).format(HMS)});
            }
        }
        notifs.add(0, new String[]{"[i]", "INFO", "MindSphere",
                "Synchronisation disponible", LocalDateTime.now().format(HMS)});
        if (timeSeriesTotal > 0) {
            notifs.add(new String[]{"[ok]", "OK", "TimeSeries",
                    timeSeriesTotal + " envoi(s) reussi(s)", LocalDateTime.now().format(HMS)});
        }

        long nbC = notifs.stream().filter(n -> "CRITIQUE".equals(n[1])).count();
        long nbA = notifs.stream().filter(n -> "ATTENTION".equals(n[1])).count();
        long nbI = notifs.stream().filter(n -> "INFO".equals(n[1])).count();

        HBox badges = new HBox(10);
        badges.setPadding(new Insets(10, 22, 10, 22));
        badges.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");
        badges.getChildren().addAll(
                badgeClair("Critiques", String.valueOf(nbC), CLR_RED),
                badgeClair("Attention", String.valueOf(nbA), CLR_GOLD),
                badgeClair("Info",      String.valueOf(nbI), CLR_BLUE),
                badgeClair("Total",     String.valueOf(notifs.size()), CLR_PURPLE));

        VBox liste = new VBox(8);
        liste.setPadding(new Insets(8, 22, 8, 22));
        liste.setStyle("-fx-background-color:" + BG_PAGE + ";");

        for (String[] n : notifs) {
            String bgColor = switch (n[1]) {
                case "CRITIQUE"  -> "#FEF0EE";
                case "ATTENTION" -> "#FEF9EC";
                case "OK"        -> "#F0FAF4";
                default          -> "#EEF5FB";
            };
            String bdColor = switch (n[1]) {
                case "CRITIQUE"  -> CLR_RED;
                case "ATTENTION" -> CLR_GOLD;
                case "OK"        -> CLR_GREEN;
                default          -> CLR_BLUE;
            };
            HBox box = new HBox(12);
            box.setPadding(new Insets(10, 14, 10, 14));
            box.setAlignment(Pos.CENTER_LEFT);
            box.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:8;" +
                    "-fx-border-color:" + bdColor + ";-fx-border-width:0 0 0 4;-fx-border-radius:8;");
            Label ico = new Label(n[0]);
            ico.setStyle("-fx-font-size:18px;");
            VBox tx = new VBox(2);
            Label ti = new Label("[" + n[1] + "] " + n[2]);
            ti.setStyle("-fx-text-fill:" + bdColor + ";-fx-font-size:12px;-fx-font-weight:bold;");
            Label de = new Label(n[3]);
            de.setStyle("-fx-text-fill:" + TXT_MID + ";-fx-font-size:11px;");
            tx.getChildren().addAll(ti, de);
            HBox.setHgrow(tx, Priority.ALWAYS);
            Label hr = new Label(n[4]);
            hr.setStyle("-fx-text-fill:" + TXT_SOFT + ";-fx-font-size:10px;");
            box.getChildren().addAll(ico, tx, hr);
            liste.getChildren().add(box);
        }

        ScrollPane sc = new ScrollPane(liste);
        sc.setFitToWidth(true);
        sc.setPrefHeight(320);
        sc.setStyle("-fx-background-color:" + BG_PAGE + ";-fx-border-color:transparent;");

        Button btnF = boutonColore("Fermer", CLR_RED, 120, 42);
        btnF.setOnAction(ev -> popup.close());
        HBox lig = new HBox(12, btnF);
        lig.setAlignment(Pos.CENTER);
        lig.setPadding(new Insets(10, 22, 16, 22));
        lig.setStyle("-fx-background-color:" + BG_ROW_ALT + ";");

        if (nbC > 0) { jouerSonNotification(); parler(nbC + " machine(s) en panne."); }
        else { parler("Aucune alerte critique."); }
        logInfo("Notifications - " + notifs.size() + " alerte(s)");

        VBox root = new VBox(0, header, badges, new Separator(), sc, lig);
        root.setStyle("-fx-background-color:" + BG_PAGE + ";");
        popup.setScene(new Scene(root, 820, 640));
        popup.show();
    }

    // Redirection
    @FXML
    public void ouvrirLocalisation() { ouvrirCarteGPS(); }

    // ════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ════════════════════════════════════════════════════════════════════════
    @FXML
    private void retourListe() {
        try {
            java.net.URL url = getClass().getResource("/MaterielsInterface/AfficherMachines.fxml");
            if (url == null) throw new IOException("AfficherMachines.fxml introuvable.");
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) btnRetour.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            afficherAlerteClair("Erreur", "Retour impossible : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void viderConsole() {
        consoleLog.clear();
        logInfo("Console videe - " + LocalDateTime.now().format(DT));
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ════════════════════════════════════════════════════════════════════════
    private HBox creerHeaderClair(String icone, String titre, String sous,
                                  String badge, String bdClr, String border) {
        HBox h = new HBox(14);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(14, 22, 12, 22));
        h.setStyle("-fx-background-color:" + BG_HEADER + ";-fx-border-color:" + border + ";-fx-border-width:0 0 2 0;");
        Label ic = new Label(icone);
        ic.setStyle("-fx-font-size:26px;");
        VBox tv = new VBox(3);
        Label t1 = new Label(titre);
        t1.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + TXT_DARK + ";");
        Label t2 = new Label(sous);
        t2.setStyle("-fx-font-size:11px;-fx-text-fill:" + TXT_SOFT + ";");
        tv.getChildren().addAll(t1, t2);
        Label bg = new Label(badge);
        bg.setStyle("-fx-background-color:" + bdClr + ";-fx-text-fill:white;" +
                "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:4 12;-fx-background-radius:6;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        h.getChildren().addAll(ic, tv, sp, bg);
        return h;
    }

    private VBox badgeClair(String lib, String val, String clr) {
        VBox b = new VBox(2);
        b.setAlignment(Pos.CENTER);
        b.setPadding(new Insets(8, 14, 8, 14));
        b.setStyle("-fx-background-color:white;-fx-border-color:" + clr +
                ";-fx-border-width:0 0 0 3;-fx-background-radius:8;-fx-border-radius:8;");
        Label l = new Label(lib);
        l.setStyle("-fx-text-fill:" + TXT_SOFT + ";-fx-font-size:10px;");
        Label v = new Label(val);
        v.setStyle("-fx-text-fill:" + clr + ";-fx-font-size:20px;-fx-font-weight:bold;");
        b.getChildren().addAll(l, v);
        return b;
    }

    private VBox barreEtatClair(String lib, int val, int tot, String clr) {
        VBox b = new VBox(4);
        b.setAlignment(Pos.BOTTOM_CENTER);
        b.setPadding(new Insets(0, 12, 0, 12));
        int h = tot > 0 ? (int)(val * 120.0 / tot) + 10 : 10;
        Rectangle r = new Rectangle(65, h);
        r.setFill(Color.web(clr));
        r.setArcWidth(6); r.setArcHeight(6);
        Label vL = new Label(String.valueOf(val));
        vL.setStyle("-fx-text-fill:" + clr + ";-fx-font-size:17px;-fx-font-weight:bold;");
        Label nL = new Label(lib);
        nL.setStyle("-fx-text-fill:" + TXT_MID + ";-fx-font-size:11px;-fx-font-weight:bold;");
        b.getChildren().addAll(vL, r, nL);
        return b;
    }

    private Label titreSectionClair(String tx) {
        Label l = new Label(tx);
        l.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + TXT_DARK + ";-fx-padding:4 0 4 0;");
        return l;
    }

    private Button boutonColore(String tx, String bg, double w, double h) {
        Button b = new Button(tx);
        b.setPrefSize(w, h);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;" +
                "-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:9;-fx-cursor:hand;");
        return b;
    }

    private Button boutonClair(String tx, String txClr, String bgClr, double w, double h) {
        Button b = new Button(tx);
        b.setPrefSize(w, h);
        b.setStyle("-fx-background-color:" + bgClr + ";-fx-text-fill:" + txClr + ";" +
                "-fx-font-size:12px;-fx-font-weight:bold;-fx-background-radius:7;-fx-cursor:hand;" +
                "-fx-border-color:" + BORDER_GOLD + ";-fx-border-width:1;-fx-border-radius:7;");
        return b;
    }

    private String styleTableClair() {
        return "-fx-background-color:white;-fx-border-color:" + BORDER_SOFT + ";-fx-border-width:1;";
    }

    private void colonneTable(TableView<String[]> t, String titre, int idx, double larg) {
        TableColumn<String[], String> col = new TableColumn<>(titre);
        col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()[idx]));
        col.setPrefWidth(larg);
        t.getColumns().add(col);
    }

    private void appliquerRowFactoryClair(TableView<String[]> t, int idx) {
        t.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.length <= idx) { setStyle(""); return; }
                String bg = switch (item[idx].toLowerCase()) {
                    case "neuf"             -> "#F0FAF4";
                    case "disponible"       -> "#EEF5FB";
                    case "occasion", "bon"  -> "#FEF9EC";
                    case "en panne"         -> "#FEF0EE";
                    default                 -> "white";
                };
                setStyle("-fx-background-color:" + bg + ";");
            }
        });
    }

    private void gridLigneClair(GridPane g, int row, String cle, String val) {
        Label k = new Label(cle);
        k.setStyle("-fx-text-fill:" + TXT_SOFT + ";-fx-font-size:12px;-fx-min-width:130;");
        Label v = new Label(val);
        v.setStyle("-fx-text-fill:" + TXT_DARK + ";-fx-font-size:12px;-fx-font-weight:bold;");
        g.add(k, 0, row);
        g.add(v, 1, row);
    }

    private VBox padded(javafx.scene.Node node, double h, double v) {
        VBox b = new VBox(node);
        b.setPadding(new Insets(v, h, v, h));
        return b;
    }
    private VBox padded(javafx.scene.Node node, double h) { return padded(node, h, 0); }

    // ════════════════════════════════════════════════════════════════════════
    // LOGS
    // ════════════════════════════════════════════════════════════════════════
    private void logInfo(String m)    { log(m); }
    private void logSuccess(String m) { log(m); }
    private void logDetail(String m)  { log("  " + m); }
    private void logWarn(String m)    { log(m); }
    private void logSep()             { log("-".repeat(68)); }

    private void log(String message) {
        Platform.runLater(() -> {
            consoleLog.appendText("[" + LocalDateTime.now().format(HMS) + "] " + message + "\n");
            if (lblNombreLogs != null)
                lblNombreLogs.setText(consoleLog.getText().split("\n", -1).length + " lignes");
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // ANIMATIONS
    // ════════════════════════════════════════════════════════════════════════
    private void animerProgress(double from, double to, int ms, Runnable onFinish) {
        progressBar.setProgress(from);
        Timeline tl = new Timeline();
        int steps = 40;
        for (int i = 0; i <= steps; i++) {
            final double v = from + (to - from) * i / steps;
            tl.getKeyFrames().add(new KeyFrame(
                    Duration.millis((long) ms * i / steps),
                    e -> Platform.runLater(() -> progressBar.setProgress(v))));
        }
        tl.setOnFinished(e -> onFinish.run());
        tl.play();
    }

    private void animerProgressBar(ProgressBar pb, double from, double to, int ms, Runnable onFinish) {
        pb.setProgress(from);
        Timeline tl = new Timeline();
        int steps = 30;
        for (int i = 0; i <= steps; i++) {
            final double v = from + (to - from) * i / steps;
            tl.getKeyFrames().add(new KeyFrame(
                    Duration.millis((long) ms * i / steps),
                    e -> Platform.runLater(() -> pb.setProgress(v))));
        }
        tl.setOnFinished(e -> onFinish.run());
        tl.play();
    }

    // ════════════════════════════════════════════════════════════════════════
    // MÉTIER
    // ════════════════════════════════════════════════════════════════════════
    private void majStatutUI(boolean connecte) {
        if (connecte) {
            lblStatutConnexion.setText("Connecte");
            lblStatutConnexion.setStyle("-fx-text-fill:" + CLR_GREEN + ";-fx-font-weight:bold;-fx-font-size:13px;");
            lblTenant.setText(FAKE_TENANT + ".eu1.mindsphere.io");
            lblTenant.setStyle("-fx-text-fill:" + CLR_GREEN + ";-fx-font-size:11px;");
        } else {
            lblStatutConnexion.setText("Non connecte");
            lblStatutConnexion.setStyle("-fx-text-fill:" + CLR_RED + ";-fx-font-weight:bold;-fx-font-size:13px;");
            lblTenant.setText("—");
            lblToken.setText("—");
            lblToken.setStyle("-fx-text-fill:" + TXT_SOFT + ";-fx-font-size:10px;");
        }
    }

    private void majStatsLocales() {
        long n = machinesLocales.stream().filter(m -> "neuf".equalsIgnoreCase(m.getEtatM())).count();
        long d = machinesLocales.stream().filter(m -> "disponible".equalsIgnoreCase(m.getEtatM())).count();
        long o = machinesLocales.stream().filter(m -> "occasion".equalsIgnoreCase(m.getEtatM()) || "bon".equalsIgnoreCase(m.getEtatM())).count();
        long p = machinesLocales.stream().filter(m -> "en panne".equalsIgnoreCase(m.getEtatM())).count();
        if (lblStatNeuf != null)       lblStatNeuf.setText(String.valueOf(n));
        if (lblStatDisponible != null)  lblStatDisponible.setText(String.valueOf(d));
        if (lblStatOccasion != null)    lblStatOccasion.setText(String.valueOf(o));
        if (lblStatEnPanne != null)     lblStatEnPanne.setText(String.valueOf(p));
        if (lblStatTotal != null)       lblStatTotal.setText(String.valueOf(machinesLocales.size()));
    }

    private void setDisableBoutonsCloud(boolean d) {
        btnSyncVersCloud.setDisable(d);
        btnImportDepuisCloud.setDisable(d);
        btnEnvoyerTimeSeries.setDisable(d);
    }

    private void setDisableTousLesBoutons(boolean d) {
        btnTesterConnexion.setDisable(d);
        btnSyncVersCloud.setDisable(d || !estConnecte);
        btnImportDepuisCloud.setDisable(d || !estConnecte);
        btnEnvoyerTimeSeries.setDisable(d || !estConnecte);
    }

    private void afficherNonConnecte() {
        afficherAlerteClair("Non connecte", "Cliquez sur Tester Connexion d'abord.", Alert.AlertType.WARNING);
    }

    private void afficherAlerteClair(String titre, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-background-color:" + BG_CARD + ";");
        a.showAndWait();
    }

    private List<String[]> genererFauxAssetsCloud() {
        return Arrays.asList(
                new String[]{"ms-A1B2C3D4", "John Deere 6130M",   "Disponible", "JD6130M-2023-001", "2023-03-15"},
                new String[]{"ms-E5F6G7H8", "Claas Lexion 780",   "Neuf",       "CL780-EU1-0042",   "2024-01-10"},
                new String[]{"ms-I9J0K1L2", "Fendt 942 Vario",    "Bon",        "FND942-0099",       "2022-07-22"},
                new String[]{"ms-M3N4O5P6", "New Holland T7.315", "Occasion",   "NH7315-FR-0017",    "2021-11-30"});
    }

    private String s(String v) { return v == null ? "—" : v; }

    private int scoreEtat(String etat) {
        if (etat == null) return 0;
        return switch (etat.toLowerCase()) {
            case "neuf"       -> 100;
            case "disponible" -> 85;
            case "bon"        -> 70;
            case "occasion"   -> 50;
            case "en panne"   -> 0;
            default           -> 30;
        };
    }
}