package services.Events;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.Evenement;
import services.Events.EvenementService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service pour afficher un calendrier visuel avec les événements
 * Sans modification de la base de données - lecture seule
 */
public class CalendarViewService {

    private final EvenementService evenementService;
    private YearMonth currentMonth;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private Map<LocalDate, VBox> dateCells;

    public CalendarViewService() {
        this.evenementService = new EvenementService();
        this.currentMonth = YearMonth.now();
        this.dateCells = new HashMap<>();
    }

    /**
     * Ouvre une fenêtre modale affichant le calendrier des événements
     */
    public void afficherCalendrier(Stage parentStage) {
        Stage calendarStage = new Stage();
        calendarStage.initModality(Modality.APPLICATION_MODAL);
        calendarStage.initOwner(parentStage);
        calendarStage.setTitle("📅 Calendrier des Événements - AgroFlow");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fcf8e6;");

        // En-tête avec navigation
        HBox header = creerEntete();
        root.setTop(header);

        // Grille du calendrier
        calendarGrid = new GridPane();
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setPadding(new Insets(20));

        construireCalendrier();

        ScrollPane scrollPane = new ScrollPane(calendarGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #fcf8e6; -fx-background-color: #fcf8e6;");
        root.setCenter(scrollPane);

        // Légende
        VBox legende = creerLegende();
        root.setBottom(legende);

        Scene scene = new Scene(root, 1100, 700);
        calendarStage.setScene(scene);
        calendarStage.show();
    }

    /**
     * Crée l'en-tête avec navigation mois précédent/suivant
     */
    private HBox creerEntete() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Button prevButton = new Button("◀ Mois précédent");
        prevButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        prevButton.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            construireCalendrier();
        });

        monthYearLabel = new Label();
        monthYearLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        monthYearLabel.setStyle("-fx-text-fill: #2C3E50;");

        Button nextButton = new Button("Mois suivant ▶");
        nextButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        nextButton.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            construireCalendrier();
        });

        Button todayButton = new Button("📍 Aujourd'hui");
        todayButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        todayButton.setOnAction(e -> {
            currentMonth = YearMonth.now();
            construireCalendrier();
        });

        header.getChildren().addAll(prevButton, monthYearLabel, nextButton, todayButton);
        return header;
    }

    /**
     * Construit la grille du calendrier avec les événements
     */
    private void construireCalendrier() {
        calendarGrid.getChildren().clear();
        dateCells.clear();

        // Mettre à jour le label mois/année
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(monthName.toUpperCase() + " " + currentMonth.getYear());

        // En-têtes des jours de la semaine
        String[] joursS = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label jourLabel = new Label(joursS[i]);
            jourLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            jourLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-padding: 10;");
            jourLabel.setMaxWidth(Double.MAX_VALUE);
            jourLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(jourLabel, i, 0);
        }

        // Obtenir le premier jour du mois
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 = Lundi

        // Charger les événements du mois
        Map<LocalDate, java.util.List<Evenement>> evenementsByDate = chargerEvenementsDuMois();

        // Remplir le calendrier
        int dayCounter = 1;
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int row = 1; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                if ((row == 1 && col < dayOfWeek) || dayCounter > daysInMonth) {
                    // Cellule vide
                    Label empty = new Label();
                    calendarGrid.add(empty, col, row);
                } else {
                    LocalDate date = currentMonth.atDay(dayCounter);
                    VBox cellule = creerCelluleJour(date, evenementsByDate.get(date));
                    dateCells.put(date, cellule);
                    calendarGrid.add(cellule, col, row);
                    dayCounter++;
                }
            }
            if (dayCounter > daysInMonth) break;
        }
    }

    /**
     * Crée une cellule pour un jour avec ses événements
     */
    private VBox creerCelluleJour(LocalDate date, List<Evenement> evenements) {
        VBox cellule = new VBox(5);
        cellule.setPrefSize(140, 120);
        cellule.setPadding(new Insets(8));
        cellule.setAlignment(Pos.TOP_LEFT);

        // Style de base
        String style = "-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;";

        // Mettre en évidence aujourd'hui
        if (date.equals(LocalDate.now())) {
            style = "-fx-background-color: #E8F5E9; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-background-radius: 8; -fx-border-radius: 8;";
        }

        cellule.setStyle(style);

        // Numéro du jour
        Label jourLabel = new Label(String.valueOf(date.getDayOfMonth()));
        jourLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        jourLabel.setStyle("-fx-text-fill: #2C3E50;");

        cellule.getChildren().add(jourLabel);

        // Ajouter les événements
        if (evenements != null && !evenements.isEmpty()) {
            for (Evenement evt : evenements) {
                Label eventLabel = new Label("• " + evt.getTitre());
                eventLabel.setFont(Font.font("System", 10));
                eventLabel.setWrapText(true);
                eventLabel.setMaxWidth(120);

                // Couleur selon le statut
                String couleur = "#3498DB";
                switch (evt.getStatut().toLowerCase()) {
                    case "planifié":
                        couleur = "#4CAF50";
                        break;
                    case "terminé":
                        couleur = "#3498DB";
                        break;
                    case "annulé":
                        couleur = "#E74C3C";
                        break;
                }

                eventLabel.setStyle("-fx-text-fill: " + couleur + "; -fx-font-weight: bold;");

                // Tooltip avec détails
                Tooltip tooltip = new Tooltip(
                        "📋 " + evt.getTitre() + "\n" +
                                "📍 " + evt.getLieu() + "\n" +
                                "⏰ " + evt.getDateDebut() + " → " + evt.getDateFin() + "\n" +
                                "📊 " + evt.getStatut()
                );
                Tooltip.install(eventLabel, tooltip);

                cellule.getChildren().add(eventLabel);
            }

            // Badge avec le nombre d'événements
            if (evenements.size() > 3) {
                Label moreLabel = new Label("+" + (evenements.size() - 3) + " autres...");
                moreLabel.setFont(Font.font("System", 9));
                moreLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");
                cellule.getChildren().add(moreLabel);
            }
        }

        return cellule;
    }

    /**
     * Charge tous les événements du mois en cours
     */
    private Map<LocalDate, java.util.List<Evenement>> chargerEvenementsDuMois() {
        Map<LocalDate, java.util.List<Evenement>> eventsByDate = new HashMap<>();

        try {
            List<Evenement> tousLesEvenements = evenementService.recuperer();

            for (Evenement evt : tousLesEvenements) {
                LocalDate dateDebut = evt.getDateDebut().toLocalDate();
                LocalDate dateFin = evt.getDateFin().toLocalDate();

                // Ajouter l'événement à toutes les dates de sa durée
                LocalDate currentDate = dateDebut;
                while (!currentDate.isAfter(dateFin)) {
                    // Vérifier si la date est dans le mois actuel
                    if (currentDate.getYear() == currentMonth.getYear() &&
                            currentDate.getMonth() == currentMonth.getMonth()) {

                        eventsByDate.putIfAbsent(currentDate, new java.util.ArrayList<>());
                        eventsByDate.get(currentDate).add(evt);
                    }
                    currentDate = currentDate.plusDays(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return eventsByDate;
    }

    /**
     * Crée la légende des couleurs
     */
    private VBox creerLegende() {
        VBox legende = new VBox(10);
        legende.setPadding(new Insets(15));
        legende.setAlignment(Pos.CENTER);
        legende.setStyle("-fx-background-color: white;");

        Label legendeTitle = new Label("LÉGENDE :");
        legendeTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        legendeTitle.setStyle("-fx-text-fill: #7F8C8D;");

        HBox legendeItems = new HBox(30);
        legendeItems.setAlignment(Pos.CENTER);

        String[][] items = {
                {"🟢", "Planifié", "#4CAF50"},
                {"🔵", "Terminé", "#3498DB"},
                {"🔴", "Annulé", "#E74C3C"}
        };

        for (String[] item : items) {
            HBox legendeItem = new HBox(5);
            legendeItem.setAlignment(Pos.CENTER);

            Label icon = new Label(item[0]);
            Label text = new Label(item[1]);
            text.setStyle("-fx-text-fill: " + item[2] + "; -fx-font-weight: bold;");

            legendeItem.getChildren().addAll(icon, text);
            legendeItems.getChildren().add(legendeItem);
        }

        legende.getChildren().addAll(legendeTitle, legendeItems);
        return legende;
    }
}