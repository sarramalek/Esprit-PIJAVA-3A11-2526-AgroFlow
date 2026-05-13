package services.Events;

import models.Events.CategorieEvenement;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategorieEvenementServiceTest {

    private static CategorieEvenementService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Setting up CategorieEvenement tests...");
        service = new CategorieEvenementService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter categorie test...");

        CategorieEvenement c = new CategorieEvenement(
                "Formation Agricole",
                "Catégorie dédiée aux formations et ateliers agricoles"
        );

        service.ajouter(c);
        System.out.println(service.recuperer());
        assertFalse(service.recuperer().isEmpty());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier categorie test...");

        CategorieEvenement c = service.recuperer()
                .get(service.recuperer().size() - 1);

        c.setNom_categorie("Formation & Sensibilisation");
        service.modifier(c);

        assertTrue(service.recuperer().get(service.recuperer().size() - 1).getNom_categorie().equals("Formation & Sensibilisation"));
    }

    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer categorie test...");

        CategorieEvenement c = service.recuperer()
                .get(service.recuperer().size() - 1);

        service.supprimer(c.getId_categorie());

        assertFalse(
                service.recuperer()
                        .stream()
                        .anyMatch(cat -> cat.getId_categorie() == c.getId_categorie())
        );
    }
}