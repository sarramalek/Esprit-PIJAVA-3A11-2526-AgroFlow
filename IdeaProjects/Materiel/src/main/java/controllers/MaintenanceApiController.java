package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import entities.Maintenance;
import services.MaintenanceService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  API REST — Coût Total Maintenance  (Java HTTP Server intégré)
 *  BASE URL : http://localhost:8081/api/maintenance
 *
 *  ENDPOINTS DISPONIBLES :
 *  ┌──────────────────────────────────────────────────────────────────────┐
 *  │ GET  /api/maintenance/cout-total           → coût total général      │
 *  │ GET  /api/maintenance/cout-total/{idM}     → coût total par machine  │
 *  │ GET  /api/maintenance/stats                → statistiques complètes  │
 *  │ GET  /api/maintenance/all                  → toutes les maintenances │
 *  │ GET  /api/maintenance/{id}                 → une maintenance par id  │
 *  │ POST /api/maintenance/add                  → ajouter une maintenance │
 *  │ PUT  /api/maintenance/update/{id}          → modifier une maintenance│
 *  │ DELETE /api/maintenance/delete/{id}        → supprimer               │
 *  │ GET  /api/maintenance/cout-par-machine     → coûts groupés           │
 *  │ GET  /api/maintenance/cout-par-type        → coûts par type de panne │
 *  │ GET  /api/maintenance/periode?de=&a=       → filter par période      │
 *  └──────────────────────────────────────────────────────────────────────┘
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class MaintenanceApiController {

    private static final int PORT = 8081;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final MaintenanceService service = new MaintenanceService();
    private HttpServer server;

    // ════════════════════════════════════════════════════════════════════════
    //  DÉMARRAGE / ARRÊT DU SERVEUR
    // ════════════════════════════════════════════════════════════════════════

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // ── Routes ──────────────────────────────────────────────────────────
        server.createContext("/api/maintenance/cout-total",     new CoutTotalHandler());
        server.createContext("/api/maintenance/stats",          new StatsHandler());
        server.createContext("/api/maintenance/all",            new AllHandler());
        server.createContext("/api/maintenance/cout-par-machine", new CoutParMachineHandler());
        server.createContext("/api/maintenance/cout-par-type",  new CoutParTypeHandler());
        server.createContext("/api/maintenance/periode",        new PeriodeHandler());
        server.createContext("/api/maintenance/add",            new AddHandler());
        server.createContext("/api/maintenance/update/",        new UpdateHandler());
        server.createContext("/api/maintenance/delete/",        new DeleteHandler());
        server.createContext("/api/maintenance/",               new ByIdHandler());   // catch-all

        server.setExecutor(null); // thread pool par défaut
        server.start();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║  API Maintenance démarrée → http://localhost:" + PORT + "   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 1 — GET /api/maintenance/cout-total          (ENDPOINT PRINCIPAL)
    //              GET /api/maintenance/cout-total/{idM}    (par machine)
    // ════════════════════════════════════════════════════════════════════════

    class CoutTotalHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"GET".equals(ex.getRequestMethod())) { sendError(ex, 405, "Méthode non autorisée"); return; }

            try {
                String path  = ex.getRequestURI().getPath();
                // /api/maintenance/cout-total  ou  /api/maintenance/cout-total/5
                String[] parts = path.split("/");
                // parts: ["","api","maintenance","cout-total","?idM"]

                if (parts.length >= 5 && !parts[4].isBlank()) {
                    // ── Par machine spécifique ──────────────────────────────
                    int idM = Integer.parseInt(parts[4]);
                    List<Maintenance> liste = service.recupererParMachine(idM);
                    double total  = liste.stream().mapToDouble(Maintenance::getCout).sum();
                    double moyen  = liste.isEmpty() ? 0 : total / liste.size();
                    double max    = liste.stream().mapToDouble(Maintenance::getCout).max().orElse(0);
                    double min    = liste.stream().mapToDouble(Maintenance::getCout).min().orElse(0);

                    String json = "{"
                            + "\"idMachine\":"    + idM          + ","
                            + "\"nbMaintenances\":"+ liste.size() + ","
                            + "\"coutTotal\":"    + round(total) + ","
                            + "\"coutMoyen\":"    + round(moyen) + ","
                            + "\"coutMax\":"      + round(max)   + ","
                            + "\"coutMin\":"      + round(min)   + ","
                            + "\"devise\":\"DT\""
                            + "}";
                    sendJson(ex, 200, json);

                } else {
                    // ── Coût total toutes machines ──────────────────────────
                    List<Maintenance> all  = service.recuperer();
                    double total  = all.stream().mapToDouble(Maintenance::getCout).sum();
                    double moyen  = all.isEmpty() ? 0 : total / all.size();
                    double max    = all.stream().mapToDouble(Maintenance::getCout).max().orElse(0);
                    double min    = all.stream().mapToDouble(Maintenance::getCout).min().orElse(0);

                    // Top 3 maintenances les plus chères
                    List<Map<String,Object>> top3 = all.stream()
                            .sorted(Comparator.comparingDouble(Maintenance::getCout).reversed())
                            .limit(3)
                            .map(m -> {
                                Map<String,Object> map = new LinkedHashMap<>();
                                map.put("idMain",     m.getIdMain());
                                map.put("typePanne",  m.getTypePanne());
                                map.put("cout",       round(m.getCout()));
                                map.put("dateMain",   m.getDateMain() != null ? m.getDateMain().toString() : "");
                                map.put("idMachine",  m.getIdM());
                                return map;
                            })
                            .collect(Collectors.toList());

                    String json = "{"
                            + "\"nbMaintenances\":"  + all.size()      + ","
                            + "\"coutTotal\":"        + round(total)    + ","
                            + "\"coutMoyen\":"        + round(moyen)    + ","
                            + "\"coutMax\":"          + round(max)      + ","
                            + "\"coutMin\":"          + round(min)      + ","
                            + "\"devise\":\"DT\""                       + ","
                            + "\"top3PlusCouteux\":"  + toJsonArray(top3)
                            + "}";
                    sendJson(ex, 200, json);
                }
            } catch (NumberFormatException e) {
                sendError(ex, 400, "ID machine invalide");
            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 2 — GET /api/maintenance/stats
    // ════════════════════════════════════════════════════════════════════════

    class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"GET".equals(ex.getRequestMethod())) { sendError(ex, 405, "Méthode non autorisée"); return; }

            try {
                List<Maintenance> all = service.recuperer();
                if (all.isEmpty()) { sendJson(ex, 200, "{\"message\":\"Aucune maintenance\"}"); return; }

                double total  = all.stream().mapToDouble(Maintenance::getCout).sum();
                double moyen  = total / all.size();
                double max    = all.stream().mapToDouble(Maintenance::getCout).max().orElse(0);
                double min    = all.stream().mapToDouble(Maintenance::getCout).min().orElse(0);

                // Répartition par type de panne
                Map<String,DoubleSummaryStatistics> parType = all.stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getTypePanne() != null ? m.getTypePanne() : "Inconnu",
                                Collectors.summarizingDouble(Maintenance::getCout)));

                // Maintenances coûteuses (> moyenne)
                long nbCouteux = all.stream().filter(m -> m.getCout() > moyen).count();
                double pctCouteux = (nbCouteux * 100.0) / all.size();

                // Construction JSON stats par type
                StringBuilder typesJson = new StringBuilder("[");
                boolean first = true;
                for (Map.Entry<String, DoubleSummaryStatistics> e : parType.entrySet()) {
                    if (!first) typesJson.append(",");
                    DoubleSummaryStatistics s = e.getValue();
                    typesJson.append("{")
                            .append("\"type\":\"").append(escJson(e.getKey())).append("\",")
                            .append("\"nb\":").append(s.getCount()).append(",")
                            .append("\"coutTotal\":").append(round(s.getSum())).append(",")
                            .append("\"coutMoyen\":").append(round(s.getAverage())).append(",")
                            .append("\"pct\":").append(round((s.getSum() / total) * 100))
                            .append("}");
                    first = false;
                }
                typesJson.append("]");

                String json = "{"
                        + "\"nbTotal\":"          + all.size()            + ","
                        + "\"coutTotal\":"         + round(total)          + ","
                        + "\"coutMoyen\":"         + round(moyen)          + ","
                        + "\"coutMax\":"           + round(max)            + ","
                        + "\"coutMin\":"           + round(min)            + ","
                        + "\"nbCouteux\":"         + nbCouteux             + ","
                        + "\"pctCouteux\":"        + round(pctCouteux)     + ","
                        + "\"devise\":\"DT\""                              + ","
                        + "\"parTypePanne\":"      + typesJson
                        + "}";
                sendJson(ex, 200, json);

            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 3 — GET /api/maintenance/all
    // ════════════════════════════════════════════════════════════════════════

    class AllHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"GET".equals(ex.getRequestMethod())) { sendError(ex, 405, "Méthode non autorisée"); return; }

            try {
                List<Maintenance> all = service.recuperer();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < all.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(maintenanceToJson(all.get(i)));
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());
            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 4 — GET /api/maintenance/{id}
    // ════════════════════════════════════════════════════════════════════════

    class ByIdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }

            String path   = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            // parts: ["","api","maintenance","idMain"]
            if (parts.length < 4 || parts[3].isBlank()) { sendError(ex, 400, "ID manquant"); return; }

            try {
                int id = Integer.parseInt(parts[3]);
                List<Maintenance> all = service.recuperer();
                Optional<Maintenance> opt = all.stream().filter(m -> m.getIdMain() == id).findFirst();
                if (opt.isEmpty()) { sendError(ex, 404, "Maintenance introuvable"); return; }
                sendJson(ex, 200, maintenanceToJson(opt.get()));
            } catch (NumberFormatException e) {
                sendError(ex, 400, "ID invalide");
            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 5 — POST /api/maintenance/add
    // ════════════════════════════════════════════════════════════════════════

    class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendError(ex, 405, "POST requis"); return; }

            try {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> params = parseJsonSimple(body);

                Maintenance m = new Maintenance();
                m.setTypePanne(params.getOrDefault("typePanne", ""));
                m.setCout(Double.parseDouble(params.getOrDefault("cout", "0")));
                m.setDateMain(LocalDate.parse(params.getOrDefault("dateMain",
                        LocalDate.now().toString()), FMT));
                m.setDescription(params.getOrDefault("description", ""));
                m.setIdM(Integer.parseInt(params.getOrDefault("idM", "0")));

                service.ajouter(m);
                sendJson(ex, 201,
                        "{\"success\":true,\"message\":\"Maintenance ajoutée\",\"id\":" + m.getIdMain() + "}");

            } catch (Exception e) {
                sendError(ex, 500, "Erreur : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 6 — PUT /api/maintenance/update/{id}
    // ════════════════════════════════════════════════════════════════════════

    class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"PUT".equals(ex.getRequestMethod())) { sendError(ex, 405, "PUT requis"); return; }

            String path   = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            // /api/maintenance/update/5
            if (parts.length < 5) { sendError(ex, 400, "ID manquant"); return; }

            try {
                int id   = Integer.parseInt(parts[4]);
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String,String> params = parseJsonSimple(body);

                Maintenance m = new Maintenance();
                m.setIdMain(id);
                m.setTypePanne(params.getOrDefault("typePanne", ""));
                m.setCout(Double.parseDouble(params.getOrDefault("cout", "0")));
                m.setDateMain(LocalDate.parse(params.getOrDefault("dateMain",
                        LocalDate.now().toString()), FMT));
                m.setDescription(params.getOrDefault("description", ""));
                m.setIdM(Integer.parseInt(params.getOrDefault("idM", "0")));

                int rows = service.modifier(m);
                if (rows == 0) { sendError(ex, 404, "Maintenance introuvable"); return; }
                sendJson(ex, 200, "{\"success\":true,\"message\":\"Maintenance modifiée\",\"id\":" + id + "}");

            } catch (NumberFormatException e) {
                sendError(ex, 400, "ID ou données invalides");
            } catch (Exception e) {
                sendError(ex, 500, "Erreur : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 7 — DELETE /api/maintenance/delete/{id}
    // ════════════════════════════════════════════════════════════════════════

    class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"DELETE".equals(ex.getRequestMethod())) { sendError(ex, 405, "DELETE requis"); return; }

            String[] parts = ex.getRequestURI().getPath().split("/");
            if (parts.length < 5) { sendError(ex, 400, "ID manquant"); return; }

            try {
                int id   = Integer.parseInt(parts[4]);
                int rows = service.supprimer(id);
                if (rows == 0) { sendError(ex, 404, "Maintenance introuvable"); return; }
                sendJson(ex, 200, "{\"success\":true,\"message\":\"Maintenance supprimée\",\"id\":" + id + "}");
            } catch (NumberFormatException e) {
                sendError(ex, 400, "ID invalide");
            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 8 — GET /api/maintenance/cout-par-machine
    // ════════════════════════════════════════════════════════════════════════

    class CoutParMachineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"GET".equals(ex.getRequestMethod())) { sendError(ex, 405, "Méthode non autorisée"); return; }

            try {
                List<Maintenance> all = service.recuperer();
                Map<Integer, DoubleSummaryStatistics> map = all.stream()
                        .collect(Collectors.groupingBy(Maintenance::getIdM,
                                Collectors.summarizingDouble(Maintenance::getCout)));

                double totalGeneral = all.stream().mapToDouble(Maintenance::getCout).sum();

                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Map.Entry<Integer, DoubleSummaryStatistics> e : map.entrySet()) {
                    if (!first) sb.append(",");
                    DoubleSummaryStatistics s = e.getValue();
                    sb.append("{")
                            .append("\"idMachine\":").append(e.getKey()).append(",")
                            .append("\"nbMaintenances\":").append(s.getCount()).append(",")
                            .append("\"coutTotal\":").append(round(s.getSum())).append(",")
                            .append("\"coutMoyen\":").append(round(s.getAverage())).append(",")
                            .append("\"coutMax\":").append(round(s.getMax())).append(",")
                            .append("\"pctDuTotal\":").append(round((s.getSum() / totalGeneral) * 100))
                            .append("}");
                    first = false;
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());

            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 9 — GET /api/maintenance/cout-par-type
    // ════════════════════════════════════════════════════════════════════════

    class CoutParTypeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"GET".equals(ex.getRequestMethod())) { sendError(ex, 405, "Méthode non autorisée"); return; }

            try {
                List<Maintenance> all = service.recuperer();
                double totalGeneral = all.stream().mapToDouble(Maintenance::getCout).sum();

                Map<String, DoubleSummaryStatistics> map = all.stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getTypePanne() != null ? m.getTypePanne() : "Inconnu",
                                Collectors.summarizingDouble(Maintenance::getCout)));

                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Map.Entry<String, DoubleSummaryStatistics> e : map.entrySet()) {
                    if (!first) sb.append(",");
                    DoubleSummaryStatistics s = e.getValue();
                    sb.append("{")
                            .append("\"typePanne\":\"").append(escJson(e.getKey())).append("\",")
                            .append("\"nbMaintenances\":").append(s.getCount()).append(",")
                            .append("\"coutTotal\":").append(round(s.getSum())).append(",")
                            .append("\"coutMoyen\":").append(round(s.getAverage())).append(",")
                            .append("\"pctDuTotal\":").append(round((s.getSum() / totalGeneral) * 100))
                            .append("}");
                    first = false;
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());

            } catch (SQLException e) {
                sendError(ex, 500, "Erreur base de données : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HANDLER 10 — GET /api/maintenance/periode?de=YYYY-MM-DD&a=YYYY-MM-DD
    // ════════════════════════════════════════════════════════════════════════

    class PeriodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"GET".equals(ex.getRequestMethod())) { sendError(ex, 405, "Méthode non autorisée"); return; }

            try {
                Map<String,String> qp = parseQuery(ex.getRequestURI().getQuery());
                LocalDate debut = qp.containsKey("de") ? LocalDate.parse(qp.get("de"), FMT) : LocalDate.MIN;
                LocalDate fin   = qp.containsKey("a")  ? LocalDate.parse(qp.get("a"),  FMT) : LocalDate.MAX;

                List<Maintenance> filtered = service.recuperer().stream()
                        .filter(m -> m.getDateMain() != null
                                && !m.getDateMain().isBefore(debut)
                                && !m.getDateMain().isAfter(fin))
                        .collect(Collectors.toList());

                double total = filtered.stream().mapToDouble(Maintenance::getCout).sum();
                double moyen = filtered.isEmpty() ? 0 : total / filtered.size();

                StringBuilder sb = new StringBuilder("{");
                sb.append("\"periode\":{")
                        .append("\"de\":\"").append(debut == LocalDate.MIN ? "début" : debut.toString()).append("\",")
                        .append("\"a\":\"").append(fin == LocalDate.MAX ? "fin" : fin.toString()).append("\"}")
                        .append(",\"nbMaintenances\":").append(filtered.size())
                        .append(",\"coutTotal\":").append(round(total))
                        .append(",\"coutMoyen\":").append(round(moyen))
                        .append(",\"devise\":\"DT\"")
                        .append(",\"maintenances\":[");

                for (int i = 0; i < filtered.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(maintenanceToJson(filtered.get(i)));
                }
                sb.append("]}");
                sendJson(ex, 200, sb.toString());

            } catch (Exception e) {
                sendError(ex, 400, "Paramètres invalides : " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRES HTTP
    // ════════════════════════════════════════════════════════════════════════

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void sendError(HttpExchange ex, int code, String msg) throws IOException {
        sendJson(ex, code, "{\"success\":false,\"error\":\"" + escJson(msg) + "\",\"code\":" + code + "}");
    }

    private void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITAIRES JSON / PARSING
    // ════════════════════════════════════════════════════════════════════════

    private String maintenanceToJson(Maintenance m) {
        return "{"
                + "\"idMain\":"     + m.getIdMain()    + ","
                + "\"typePanne\":\"" + escJson(m.getTypePanne() != null ? m.getTypePanne() : "") + "\","
                + "\"cout\":"        + round(m.getCout()) + ","
                + "\"dateMain\":\""  + (m.getDateMain() != null ? m.getDateMain().toString() : "") + "\","
                + "\"description\":\"" + escJson(m.getDescription() != null ? m.getDescription() : "") + "\","
                + "\"idMachine\":"   + m.getIdM()
                + "}";
    }

    /** Parser JSON minimaliste pour requêtes simples (pas de bibliothèque externe) */
    private Map<String,String> parseJsonSimple(String json) {
        Map<String,String> map = new LinkedHashMap<>();
        json = json.trim().replaceAll("^\\{|\\}$", "");
        for (String pair : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replaceAll("\"","");
                String v = kv[1].trim().replaceAll("^\"|\"$","");
                map.put(k, v);
            }
        }
        return map;
    }

    private Map<String,String> parseQuery(String query) {
        Map<String,String> map = new LinkedHashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    private String toJsonArray(List<Map<String,Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{");
            boolean f = true;
            for (Map.Entry<String,Object> e : list.get(i).entrySet()) {
                if (!f) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof String) sb.append("\"").append(escJson((String)v)).append("\"");
                else sb.append(v);
                f = false;
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private double round(double v)    { return Math.round(v * 100.0) / 100.0; }
    private String escJson(String s)  { return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\""); }

    // ════════════════════════════════════════════════════════════════════════
    //  MAIN — pour tester l'API indépendamment
    // ════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws IOException {
        MaintenanceApiController api = new MaintenanceApiController();
        api.start();
        System.out.println("Appuyez sur Entrée pour arrêter...");
        new java.util.Scanner(System.in).nextLine();
        api.stop();
    }
}