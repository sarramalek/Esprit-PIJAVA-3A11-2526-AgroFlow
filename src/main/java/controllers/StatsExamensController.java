package controllers;

import entities.examens;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import services.ServiceExamen;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StatsExamensController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    private ServiceExamen service = new ServiceExamen();

    @FXML
    public void initialize() {
        try {
            List<examens> liste = service.afficher();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM"); // Pour avoir le nom du mois

            // On groupe par mois et on compte
            Map<String, Long> stats = liste.stream()
                    .collect(Collectors.groupingBy(
                            e -> sdf.format(e.getDate_examen()),
                            TreeMap::new, // TreeMap pour garder l'ordre alphabétique/chronologique
                            Collectors.counting()
                    ));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Examens 2026");

            stats.forEach((mois, nombre) -> {
                series.getData().add(new XYChart.Data<>(mois, nombre));
            });

            barChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}