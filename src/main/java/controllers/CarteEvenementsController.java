package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Evenement;
import services.EvenementService;

import java.sql.SQLException;
import java.util.*;

public class CarteEvenementsController {

    @FXML private Pane cartePane;
    @FXML private ListView<String> evenementsList;
    @FXML private Label gouvernoratLabel;
    @FXML private Label countLabel;
    @FXML private Label totalLabel;
    @FXML private Button fermerBtn;

    private final EvenementService evenementService = new EvenementService();
    private final Map<String, List<Evenement>> evenementsParGouv = new HashMap<>();

    private static final Map<String, String> GOUVERNORATS_MAP = new LinkedHashMap<>();
    static {
        GOUVERNORATS_MAP.put("tunis",         "Tunis");
        GOUVERNORATS_MAP.put("ariana",        "Ariana");
        GOUVERNORATS_MAP.put("ben arous",     "Ben Arous");
        GOUVERNORATS_MAP.put("manouba",       "Manouba");
        GOUVERNORATS_MAP.put("nabeul",        "Nabeul");
        GOUVERNORATS_MAP.put("hammamet",      "Nabeul");
        GOUVERNORATS_MAP.put("zaghouan",      "Zaghouan");
        GOUVERNORATS_MAP.put("bizerte",       "Bizerte");
        GOUVERNORATS_MAP.put("beja",          "Béja");
        GOUVERNORATS_MAP.put("béja",          "Béja");
        GOUVERNORATS_MAP.put("jendouba",      "Jendouba");
        GOUVERNORATS_MAP.put("le kef",        "Le Kef");
        GOUVERNORATS_MAP.put("kef",           "Le Kef");
        GOUVERNORATS_MAP.put("siliana",       "Siliana");
        GOUVERNORATS_MAP.put("sousse",        "Sousse");
        GOUVERNORATS_MAP.put("monastir",      "Monastir");
        GOUVERNORATS_MAP.put("mahdia",        "Mahdia");
        GOUVERNORATS_MAP.put("sfax",          "Sfax");
        GOUVERNORATS_MAP.put("kairouan",      "Kairouan");
        GOUVERNORATS_MAP.put("kasserine",     "Kasserine");
        GOUVERNORATS_MAP.put("sidi bouzid",   "Sidi Bouzid");
        GOUVERNORATS_MAP.put("gabes",         "Gabès");
        GOUVERNORATS_MAP.put("gabès",         "Gabès");
        GOUVERNORATS_MAP.put("medenine",      "Médenine");
        GOUVERNORATS_MAP.put("médenine",      "Médenine");
        GOUVERNORATS_MAP.put("tataouine",     "Tataouine");
        GOUVERNORATS_MAP.put("gafsa",         "Gafsa");
        GOUVERNORATS_MAP.put("tozeur",        "Tozeur");
        GOUVERNORATS_MAP.put("kebili",        "Kébili");
        GOUVERNORATS_MAP.put("kébili",        "Kébili");
        GOUVERNORATS_MAP.put("stade municipal","Tunis");
    }

    private static final Map<String, double[]> COORDS = new LinkedHashMap<>();
    static {
        COORDS.put("Tunis",       new double[]{195, 85});
        COORDS.put("Ariana",      new double[]{200, 68});
        COORDS.put("Ben Arous",   new double[]{207, 97});
        COORDS.put("Manouba",     new double[]{183, 80});
        COORDS.put("Nabeul",      new double[]{232, 108});
        COORDS.put("Zaghouan",    new double[]{207, 122});
        COORDS.put("Bizerte",     new double[]{183, 52});
        COORDS.put("Béja",        new double[]{152, 78});
        COORDS.put("Jendouba",    new double[]{128, 72});
        COORDS.put("Le Kef",      new double[]{138, 112});
        COORDS.put("Siliana",     new double[]{163, 122});
        COORDS.put("Sousse",      new double[]{222, 148});
        COORDS.put("Monastir",    new double[]{232, 162});
        COORDS.put("Mahdia",      new double[]{237, 182});
        COORDS.put("Sfax",        new double[]{222, 218});
        COORDS.put("Kairouan",    new double[]{192, 157});
        COORDS.put("Kasserine",   new double[]{152, 167});
        COORDS.put("Sidi Bouzid", new double[]{177, 202});
        COORDS.put("Gabès",       new double[]{212, 268});
        COORDS.put("Médenine",    new double[]{232, 308});
        COORDS.put("Tataouine",   new double[]{222, 362});
        COORDS.put("Gafsa",       new double[]{152, 232});
        COORDS.put("Tozeur",      new double[]{137, 272});
        COORDS.put("Kébili",      new double[]{172, 297});
    }

    @FXML
    public void initialize() {
        // État initial
        gouvernoratLabel.setText("Cliquez sur un point pour voir les événements");
        countLabel.setText("");
        totalLabel.setText("");
        evenementsList.setPlaceholder(new Label("Sélectionnez un gouvernorat sur la carte"));

        try {
            List<Evenement> tous = evenementService.recuperer();
            extraireEtGrouper(tous);
            dessinerCarte();
            totalLabel.setText("Total : " + tous.size() + " événement(s) chargé(s)");
        } catch (SQLException e) {
            gouvernoratLabel.setText("Erreur : " + e.getMessage());
        }
    }

    private void extraireEtGrouper(List<Evenement> evenements) {
        for (Evenement ev : evenements) {
            String lieu = ev.getLieu();
            if (lieu == null || lieu.isBlank()) continue;
            String gouv = detecterGouvernorat(lieu);
            evenementsParGouv.computeIfAbsent(gouv, k -> new ArrayList<>()).add(ev);
        }
    }

    private String detecterGouvernorat(String lieu) {
        String lower = lieu.toLowerCase().trim();
        for (Map.Entry<String, String> entry : GOUVERNORATS_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return lieu;
    }

    private void dessinerCarte() {
        cartePane.getChildren().clear();

        // Fond
        Rectangle fond = new Rectangle(0, 0, 390, 490);
        fond.setFill(Color.web("#E8F4FD"));
        fond.setArcWidth(12);
        fond.setArcHeight(12);
        cartePane.getChildren().add(fond);

        // Silhouette Tunisie
        Polygon tunisie = new Polygon(
                185.0,30.0, 220.0,35.0, 245.0,75.0, 255.0,120.0,
                250.0,160.0, 255.0,200.0, 245.0,240.0, 250.0,280.0,
                240.0,320.0, 245.0,360.0, 235.0,410.0, 200.0,430.0,
                160.0,410.0, 130.0,370.0, 110.0,300.0, 100.0,250.0,
                115.0,190.0, 120.0,140.0, 105.0,100.0, 115.0,60.0,
                150.0,40.0,  175.0,30.0
        );
        tunisie.setFill(Color.web("#C8E6C9"));
        tunisie.setStroke(Color.web("#388E3C"));
        tunisie.setStrokeWidth(1.5);
        cartePane.getChildren().add(tunisie);

        // Titre
        Text titre = new Text(8, 20, "Tunisie — Événements par gouvernorat");
        titre.setFont(Font.font("System", FontWeight.BOLD, 11));
        titre.setFill(Color.web("#1A237E"));
        cartePane.getChildren().add(titre);

        int maxCount = evenementsParGouv.values().stream()
                .mapToInt(List::size).max().orElse(1);

        for (Map.Entry<String, double[]> entry : COORDS.entrySet()) {
            String gouv = entry.getKey();
            double[] pos = entry.getValue();
            List<Evenement> evs = evenementsParGouv.getOrDefault(gouv, Collections.emptyList());
            int count = evs.size();

            double radius = count == 0 ? 6 : 8 + (count * 4.0 / maxCount) * 6;
            Color couleur = count == 0 ? Color.web("#B0BEC5") : getCouleur(count, maxCount);

            Circle cercle = new Circle(pos[0], pos[1], radius, couleur);
            cercle.setStroke(Color.WHITE);
            cercle.setStrokeWidth(1.5);

            Tooltip tip = new Tooltip(gouv + " : " + count + " événement(s)");
            tip.setStyle("-fx-font-size: 12px; -fx-background-color: #1A237E; -fx-text-fill: white;");
            Tooltip.install(cercle, tip);

            final String gouvFinal = gouv;
            final List<Evenement> evsFinal = new ArrayList<>(evs);
            cercle.setOnMouseClicked(e -> afficherEvenements(gouvFinal, evsFinal));
            cercle.setOnMouseEntered(e -> { cercle.setScaleX(1.3); cercle.setScaleY(1.3); });
            cercle.setOnMouseExited(e ->  { cercle.setScaleX(1.0); cercle.setScaleY(1.0); });
            cercle.setStyle("-fx-cursor: hand;");
            cartePane.getChildren().add(cercle);

            if (count > 0) {
                Text badge = new Text(pos[0] - (count > 9 ? 5 : 3), pos[1] + 4, String.valueOf(count));
                badge.setFont(Font.font("System", FontWeight.BOLD, 9));
                badge.setFill(Color.WHITE);
                badge.setMouseTransparent(true);
                cartePane.getChildren().add(badge);
            }

            Text lbl = new Text(pos[0] + radius + 3, pos[1] + 4, gouv);
            lbl.setFont(Font.font("System", count > 0 ? FontWeight.BOLD : FontWeight.NORMAL, 8));
            lbl.setFill(count > 0 ? Color.web("#1A237E") : Color.web("#90A4AE"));
            lbl.setMouseTransparent(true);
            cartePane.getChildren().add(lbl);
        }

        ajouterLegende(maxCount);
    }

    private void ajouterLegende(int max) {
        double y = 455;
        Text t = new Text(8, y, "Légende : ");
        t.setFont(Font.font("System", FontWeight.BOLD, 10));
        t.setFill(Color.web("#37474F"));
        cartePane.getChildren().add(t);

        Circle c0 = new Circle(75, y - 5, 6, Color.web("#B0BEC5"));
        c0.setStroke(Color.WHITE); c0.setStrokeWidth(1);
        Text t0 = new Text(84, y, "Aucun");
        t0.setFont(Font.font("System", 9)); t0.setFill(Color.web("#607D8B"));

        Circle c1 = new Circle(135, y - 5, 6, getCouleur(1, max));
        c1.setStroke(Color.WHITE); c1.setStrokeWidth(1);
        Text t1 = new Text(144, y, "Peu");
        t1.setFont(Font.font("System", 9)); t1.setFill(Color.web("#607D8B"));

        Circle c2 = new Circle(185, y - 5, 9, getCouleur(max, max));
        c2.setStroke(Color.WHITE); c2.setStrokeWidth(1);
        Text t2 = new Text(197, y, "Beaucoup");
        t2.setFont(Font.font("System", 9)); t2.setFill(Color.web("#607D8B"));

        cartePane.getChildren().addAll(c0, t0, c1, t1, c2, t2);
    }

    private Color getCouleur(int count, int max) {
        double ratio = max == 0 ? 0 : (double) count / max;
        if (ratio < 0.34) return Color.web("#FFC107");
        if (ratio < 0.67) return Color.web("#FF7043");
        return Color.web("#E53935");
    }

    private void afficherEvenements(String gouvernorat, List<Evenement> evs) {
        gouvernoratLabel.setText("📍 " + gouvernorat);
        countLabel.setText(evs.size() + " événement(s)");
        evenementsList.getItems().clear();

        if (evs.isEmpty()) {
            evenementsList.setPlaceholder(new Label("Aucun événement dans ce gouvernorat"));
        } else {
            for (Evenement ev : evs) {
                String dateStr = ev.getDateDebut() != null ? ev.getDateDebut().toString() : "—";
                String statut  = ev.getStatut()    != null ? ev.getStatut()              : "—";
                evenementsList.getItems().add(
                        "📌 " + ev.getTitre() + "   |   " + dateStr + "   |   " + statut
                );
            }
        }
    }

    @FXML
    private void handleFermer() {
        ((Stage) fermerBtn.getScene().getWindow()).close();
    }
}