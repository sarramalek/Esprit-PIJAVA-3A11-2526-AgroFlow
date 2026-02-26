package controllers.User;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import utils.MyDatabase;

import java.net.URL;
import java.sql.*;
import java.util.*;

/**
 * Controller du dashboard de statistiques AgroFlow
 * Affiche en temps réel les KPIs et graphiques de chaque service.
 */
public class StatsDashboardController implements Initializable {

    // ── KPI Cards ──────────────────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblAbonnementsActifs;
    @FXML private Label lblTotalOffres;
    @FXML private Label lblTachesEnCours;

    @FXML private Label lblTotalUsersSubtitle;
    @FXML private Label lblAbonnementsSubtitle;

    // ── Graphiques ────────────────────────────────────────────────────
    @FXML private PieChart pieAbonnements;
    @FXML private BarChart<String, Number> barOffres;
    @FXML private BarChart<String, Number> barTaches;
    @FXML private PieChart pieUsers;

    // ── Tableau offres ────────────────────────────────────────────────
    @FXML private TableView<OffreRow> tableOffres;
    @FXML private TableColumn<OffreRow, String>  colNomOffre;
    @FXML private TableColumn<OffreRow, String>  colPrix;
    @FXML private TableColumn<OffreRow, Integer> colDuree;
    @FXML private TableColumn<OffreRow, Integer> colNbAbonnes;

    // ── Conteneurs ────────────────────────────────────────────────────
    @FXML private VBox loadingOverlay;
    @FXML private Label lblRevenuMensuel;
    @FXML private Label lblTauxAdoption2FA;
    @FXML private Label lblTachesRetard;
    @FXML private ProgressBar progressActifs;
    @FXML private ProgressBar progress2FA;

    private Connection connection;

    // ── Modèle pour la TableView ───────────────────────────────────────
    public static class OffreRow {
        public final javafx.beans.property.StringProperty  nom      = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty  prix     = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.IntegerProperty duree    = new javafx.beans.property.SimpleIntegerProperty();
        public final javafx.beans.property.IntegerProperty nbAbonnes = new javafx.beans.property.SimpleIntegerProperty();

        public OffreRow(String nom, double prix, int duree, int nbAbonnes) {
            this.nom.set(nom);
            this.prix.set(String.format("%.2f TND", prix));
            this.duree.set(duree);
            this.nbAbonnes.set(nbAbonnes);
        }

        public javafx.beans.property.StringProperty  nomProperty()      { return nom; }
        public javafx.beans.property.StringProperty  prixProperty()     { return prix; }
        public javafx.beans.property.IntegerProperty dureeProperty()    { return duree; }
        public javafx.beans.property.IntegerProperty nbAbonnesProperty(){ return nbAbonnes; }
    }

    // ─────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connection = MyDatabase.getInstance().getConnection();
        setupTableColumns();
        loadStatsAsync();
    }

    // ── Chargement asynchrone (ne bloque pas l'UI) ────────────────────
    private void loadStatsAsync() {
        if (loadingOverlay != null) loadingOverlay.setVisible(true);

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return collectStats();
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Object> stats = task.getValue();
            Platform.runLater(() -> {
                populateDashboard(stats);
                if (loadingOverlay != null) loadingOverlay.setVisible(false);
            });
        });

        task.setOnFailed(e -> Platform.runLater(() -> {
            showError("Impossible de charger les statistiques : " +
                    task.getException().getMessage());
            if (loadingOverlay != null) loadingOverlay.setVisible(false);
        }));

        new Thread(task).start();
    }

    // ── Collecte SQL ──────────────────────────────────────────────────
    private Map<String, Object> collectStats() throws SQLException {
        Map<String, Object> s = new HashMap<>();

        try (Statement st = connection.createStatement()) {

            // Utilisateurs
            rs1(st, "SELECT COUNT(*) FROM users",                       s, "totalUsers");
            rs1(st, "SELECT COUNT(*) FROM users WHERE role=3",          s, "totalAdmins");
            rs1(st, "SELECT COUNT(*) FROM users WHERE role=2",          s, "totalEmployes");
            rs1(st, "SELECT COUNT(*) FROM users WHERE role=1",          s, "totalClients");
            rs1(st, "SELECT COUNT(*) FROM users WHERE two_factor_enabled=1", s, "users2FA");

            // Abonnements
            rs1(st, "SELECT COUNT(*) FROM abonnements",                              s, "totalAbonn");
            rs1(st, "SELECT COUNT(*) FROM abonnements WHERE LOWER(situation)='actif'",   s, "abActifs");
            rs1(st, "SELECT COUNT(*) FROM abonnements WHERE LOWER(situation)='expire'",  s, "abExpires");
            rs1(st, "SELECT COUNT(*) FROM abonnements WHERE LOWER(situation)='attente'", s, "abAttente");

            // Revenu mensuel
            ResultSet rs = st.executeQuery(
                    "SELECT COALESCE(SUM(o.prix),0) FROM abonnements a " +
                            "JOIN offres o ON a.id_offre=o.id_offres WHERE LOWER(a.situation)='actif'");
            if (rs.next()) s.put("revenu", rs.getDouble(1));

            // Offres avec nb abonnés
            List<OffreRow> offresRows = new ArrayList<>();
            rs = st.executeQuery(
                    "SELECT o.nom_offre, o.prix, o.duree_offre, COUNT(a.id_abonn) nb " +
                            "FROM offres o LEFT JOIN abonnements a ON o.id_offres=a.id_offre " +
                            "GROUP BY o.id_offres ORDER BY nb DESC");
            while (rs.next()) {
                offresRows.add(new OffreRow(
                        rs.getString("nom_offre"),
                        rs.getDouble("prix"),
                        rs.getInt("duree_offre"),
                        rs.getInt("nb")
                ));
            }
            s.put("offresRows", offresRows);
            s.put("totalOffres", offresRows.size());

            // Tâches
            rs1(st, "SELECT COUNT(*) FROM taches",                                            s, "totalTaches");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE LOWER(etat)='en cours'",               s, "tEnCours");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE LOWER(etat)='termine'",                s, "tTerminees");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE LOWER(etat)='en attente'",             s, "tAttente");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE date_echeancee < NOW() AND LOWER(etat)!='termine'", s, "tRetard");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE LOWER(priorite)='haute'",              s, "tHaute");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE LOWER(priorite)='moyenne'",            s, "tMoyenne");
            rs1(st, "SELECT COUNT(*) FROM taches WHERE LOWER(priorite)='basse'",              s, "tBasse");
        }
        return s;
    }

    // ── Remplissage du dashboard ──────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void populateDashboard(Map<String, Object> s) {

        int totalUsers    = (int) s.getOrDefault("totalUsers",   0);
        int abActifs      = (int) s.getOrDefault("abActifs",     0);
        int totalAbonn    = (int) s.getOrDefault("totalAbonn",   0);
        int totalOffres   = (int) s.getOrDefault("totalOffres",  0);
        int tEnCours      = (int) s.getOrDefault("tEnCours",     0);
        int users2FA      = (int) s.getOrDefault("users2FA",     0);
        int tRetard       = (int) s.getOrDefault("tRetard",      0);
        double revenu     = (double) s.getOrDefault("revenu",    0.0);

        // ── KPI Cards (avec animation de compteur) ──────────────────
        animateCount(lblTotalUsers,         0, totalUsers,   "");
        animateCount(lblAbonnementsActifs,  0, abActifs,     "");
        animateCount(lblTotalOffres,        0, totalOffres,  "");
        animateCount(lblTachesEnCours,      0, tEnCours,     "");

        safeSet(lblRevenuMensuel,  String.format("%.0f TND", revenu));
        safeSet(lblTachesRetard,   tRetard + " en retard");

        // Taux 2FA
        double taux2FA = totalUsers > 0 ? (double) users2FA / totalUsers : 0;
        safeSet(lblTauxAdoption2FA, String.format("%.0f%%", taux2FA * 100));
        if (progress2FA != null) progress2FA.setProgress(taux2FA);

        // Progress abonnements actifs
        double tauxActifs = totalAbonn > 0 ? (double) abActifs / totalAbonn : 0;
        if (progressActifs != null) progressActifs.setProgress(tauxActifs);

        // ── PieChart : Abonnements par statut ───────────────────────
        if (pieAbonnements != null) {
            pieAbonnements.getData().setAll(
                    new PieChart.Data("Actifs ("   + s.getOrDefault("abActifs",  0) + ")",
                            (int) s.getOrDefault("abActifs",  0)),
                    new PieChart.Data("Expirés ("  + s.getOrDefault("abExpires", 0) + ")",
                            (int) s.getOrDefault("abExpires", 0)),
                    new PieChart.Data("En attente ("+ s.getOrDefault("abAttente",0)+ ")",
                            (int) s.getOrDefault("abAttente", 0))
            );
            pieAbonnements.setTitle("Répartition abonnements");
            applyPieColors(pieAbonnements, "#22a722", "#e05c2a", "#f0a500");
        }

        // ── PieChart : Utilisateurs par rôle ────────────────────────
        if (pieUsers != null) {
            pieUsers.getData().setAll(
                    new PieChart.Data("Clients ("  + s.getOrDefault("totalClients",  0) + ")",
                            (int) s.getOrDefault("totalClients",  0)),
                    new PieChart.Data("Employés (" + s.getOrDefault("totalEmployes", 0) + ")",
                            (int) s.getOrDefault("totalEmployes", 0)),
                    new PieChart.Data("Admins ("   + s.getOrDefault("totalAdmins",   0) + ")",
                            (int) s.getOrDefault("totalAdmins",   0))
            );
            pieUsers.setTitle("Répartition utilisateurs");
            applyPieColors(pieUsers, "#2563eb", "#22a722", "#dc2626");
        }

        // ── BarChart : Offres par nb d'abonnés ──────────────────────
        if (barOffres != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Abonnés par offre");
            List<OffreRow> rows = (List<OffreRow>) s.getOrDefault("offresRows", Collections.emptyList());
            for (OffreRow row : rows) {
                series.getData().add(new XYChart.Data<>(row.nom.get(), row.nbAbonnes.get()));
            }
            barOffres.getData().setAll(series);
            barOffres.setTitle("Abonnés par offre");
        }

        // ── BarChart : Tâches par statut/priorité ───────────────────
        if (barTaches != null) {
            XYChart.Series<String, Number> seriesStatut = new XYChart.Series<>();
            seriesStatut.setName("Par statut");
            seriesStatut.getData().addAll(
                    new XYChart.Data<>("En cours",   (int) s.getOrDefault("tEnCours",   0)),
                    new XYChart.Data<>("Terminées",  (int) s.getOrDefault("tTerminees", 0)),
                    new XYChart.Data<>("En attente", (int) s.getOrDefault("tAttente",   0)),
                    new XYChart.Data<>("En retard",  (int) s.getOrDefault("tRetard",    0))
            );

            XYChart.Series<String, Number> seriesPriorite = new XYChart.Series<>();
            seriesPriorite.setName("Par priorité");
            seriesPriorite.getData().addAll(
                    new XYChart.Data<>("Haute",   (int) s.getOrDefault("tHaute",   0)),
                    new XYChart.Data<>("Moyenne", (int) s.getOrDefault("tMoyenne", 0)),
                    new XYChart.Data<>("Basse",   (int) s.getOrDefault("tBasse",   0))
            );

            barTaches.getData().setAll(seriesStatut, seriesPriorite);
            barTaches.setTitle("Tâches");
        }

        // ── TableView : Offres ────────────────────────────────────────
        if (tableOffres != null) {
            List<OffreRow> rows = (List<OffreRow>) s.getOrDefault("offresRows", Collections.emptyList());
            tableOffres.getItems().setAll(rows);
        }
    }

    // ── Configuration des colonnes TableView ─────────────────────────
    private void setupTableColumns() {
        if (colNomOffre  != null) colNomOffre.setCellValueFactory(c -> c.getValue().nomProperty());
        if (colPrix      != null) colPrix.setCellValueFactory(c -> c.getValue().prixProperty());
        if (colDuree     != null) colDuree.setCellValueFactory(c -> c.getValue().dureeProperty().asObject());
        if (colNbAbonnes != null) colNbAbonnes.setCellValueFactory(c -> c.getValue().nbAbonnesProperty().asObject());
    }

    // ── Bouton Rafraîchir ─────────────────────────────────────────────
    @FXML
    public void handleRefresh() {
        loadStatsAsync();
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    /** Lecture d'un COUNT(*) et stockage dans la map */
    private void rs1(Statement st, String sql, Map<String, Object> map, String key) throws SQLException {
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) map.put(key, rs.getInt(1));
    }

    /** Anime un label de 0 → finalValue sur 800ms */
    private void animateCount(Label lbl, int from, int to, String suffix) {
        if (lbl == null) return;
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(800),
                e -> lbl.setText(to + suffix),
                new KeyValue(new javafx.beans.property.SimpleIntegerProperty(from) {
                    @Override protected void invalidated() { lbl.setText(get() + suffix); }
                }, to)
        ));
        tl.play();
    }

    /** Applique des couleurs CSS aux secteurs d'un PieChart */
    private void applyPieColors(PieChart chart, String... colors) {
        Platform.runLater(() -> {
            int i = 0;
            for (PieChart.Data data : chart.getData()) {
                if (data.getNode() != null && i < colors.length) {
                    data.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
                }
                i++;
            }
        });
    }

    private void safeSet(Label lbl, String value) {
        if (lbl != null) lbl.setText(value);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}