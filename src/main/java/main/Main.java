package main;

import models.Events.Evenement;
import services.Events.EvenementService;

import java.sql.Date;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        EvenementService ps = new EvenementService();

        try {
            ps.ajouter(new Evenement(
                    "Atelier de formation sur l’irrigation intelligente",
                    "Atelier pratique destiné aux agriculteurs pour présenter les techniques modernes " +
                            "d’irrigation intelligente et l’optimisation de la consommation d’eau.",
                    "FORMATION",
                    Date.valueOf("2026-03-15"),
                    Date.valueOf("2026-03-15"),
                    "Centre de formation agricole de Sfax",
                    "PLANIFIE",
                    2
            ));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
