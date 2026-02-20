package controllers;

import entities.Sexe;
import entities.animaux;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import services.ServiceAnimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsAnimauxController {

    @FXML private PieChart pieChart;
    private ServiceAnimal service = new ServiceAnimal();

    @FXML
    public void initialize() {
        try {
            List<animaux> liste = service.afficher();

            // Logique avancée : on groupe par sexe et on compte
            Map<Sexe, Long> stats = liste.stream()
                    .collect(Collectors.groupingBy(animaux::getSexe, Collectors.counting()));

            // On remplit le graphique
            stats.forEach((sexe, nombre) -> {
                pieChart.getData().add(new PieChart.Data(sexe.toString() + " (" + nombre + ")", nombre));
            });

            pieChart.setTitle("Total : " + liste.size() + " animaux");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}