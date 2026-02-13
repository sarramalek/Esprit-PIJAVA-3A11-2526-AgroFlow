package services;

import models.Evenement;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EvenementServiceTest {

    private static EvenementService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up before all tests...");
        service = new EvenementService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter test...");
        Evenement e = new Evenement(
                "Atelier de formation sur l’irrigation intelligente",
                "Atelier pratique destiné aux agriculteurs pour présenter les techniques modernes " +
                        "d’irrigation intelligente et l’optimisation de la consommation d’eau.",
                "FORMATION",
                Date.valueOf("2026-03-15"),
                Date.valueOf("2026-03-15"),
                "Centre de formation agricole de Sfax",
                "PLANIFIE",
                3
        );
        service.ajouter(e);
        System.out.println(service.recuperer());
        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier test...");
        Evenement e = service.recuperer().get(service.recuperer().size() - 1);
        e.setTitre("Atelier de formation sur l’irrigation");
        e.setTypeEvenement("SEMINAIRE");
        service.modifier(e);
        assertTrue(service.recuperer().get(service.recuperer().size() - 1).getTitre().equals("Atelier de formation sur l’irrigation"));
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer test...");
        Evenement e = service.recuperer().get(service.recuperer().size() - 1);
        service.supprimer(e);
        assertFalse(service.recuperer().stream().anyMatch(ev -> ev.getIdEvenement() == e.getIdEvenement()));
    }
}