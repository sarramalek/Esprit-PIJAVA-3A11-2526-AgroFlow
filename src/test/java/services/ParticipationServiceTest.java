package services;

import models.Participation;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParticipationServiceTest {

    private static ParticipationService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up Participation tests...");
        service = new ParticipationService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter participation test...");

        Participation p = new Participation(
                "INSCRIT",
                LocalDate.now(),
                false,
                1 // id_evenement existant
        );

        service.ajouter(p);
        System.out.println(service.recuperer());
        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier participation test...");

        Participation p = service.recuperer()
                .get(service.recuperer().size() - 1);

        p.setStatut_participation("CONFIRME");
        p.setPresence(true);

        service.modifier(p);

        Participation last = service.recuperer()
                .get(service.recuperer().size() - 1);

        assertTrue(service.recuperer().get(service.recuperer().size() - 1).getStatut_participation().equals("CONFIRME"));

    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer participation test...");

        Participation p = service.recuperer()
                .get(service.recuperer().size() - 1);

        service.supprimer(p);

        assertFalse(
                service.recuperer()
                        .stream()
                        .anyMatch(part -> part.getId_participation() == p.getId_participation())
        );
    }
}