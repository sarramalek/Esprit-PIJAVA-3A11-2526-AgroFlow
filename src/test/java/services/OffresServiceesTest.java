package services;

import models.offres;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OffresServiceesTest {

    private OffresServicees service;
    private offres testOffre;

    @BeforeEach
    void setUp() {
        service = new OffresServicees();

        testOffre = new offres();
        testOffre.setNom_offre("Offre Test");
        testOffre.setDescription("Description test");
        testOffre.setPrix(9999);
        testOffre.setDuree_offre(30);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Nettoyer les données de test
        List<offres> liste = service.recuperer();
        for (offres o : liste) {
            if (o.getNom_offre().equals("Offre Test") ||
                    o.getNom_offre().equals("Offre Modifiee")) {
                service.supprimer(o.getId_offres());
            }
        }
        testOffre = null;
    }

    @Test
    void ajouter() {
        assertDoesNotThrow(() -> {
            service.ajouter(testOffre);

            // Vérifier que l'offre a bien été ajoutée
            List<offres> liste = service.recuperer();
            assertNotNull(liste, "La liste ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            // Vérifier que l'offre ajoutée est présente
            boolean trouve = liste.stream()
                    .anyMatch(o -> o.getNom_offre().equals("Offre Test")
                            && o.getDescription().equals("Description test")
                            && o.getDuree_offre() == 30);
            assertTrue(trouve, "L'offre ajoutée doit être présente dans la liste");
        });
    }

    @Test
    void modifier() {
        assertDoesNotThrow(() -> {
            // Ajouter d'abord
            service.ajouter(testOffre);

            // Récupérer l'offre ajoutée
            List<offres> liste = service.recuperer();
            offres ajoutee = liste.stream()
                    .filter(o -> o.getNom_offre().equals("Offre Test"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(ajoutee, "L'offre doit exister avant modification");

            // Modifier les données
            ajoutee.setNom_offre("Offre Modifiee");
            ajoutee.setDescription("Description modifiee");
            ajoutee.setPrix(14999);
            ajoutee.setDuree_offre(60);
            service.modifier(ajoutee);

            // Vérifier la modification
            offres modifiee = service.rechercherParId(ajoutee.getId_offres());
            assertNotNull(modifiee, "L'offre modifiée doit exister");
            assertEquals("Offre Modifiee", modifiee.getNom_offre());
            assertEquals("Description modifiee", modifiee.getDescription());
            assertEquals(14999, modifiee.getPrix(), 0.01);
            assertEquals(60, modifiee.getDuree_offre());
        });
    }

    @Test
    void supprimer() {
        assertDoesNotThrow(() -> {
            // Ajouter une offre
            service.ajouter(testOffre);

            // Récupérer son id
            List<offres> liste = service.recuperer();
            offres ajoutee = liste.stream()
                    .filter(o -> o.getNom_offre().equals("Offre Test"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(ajoutee, "L'offre doit exister avant suppression");
            int id = ajoutee.getId_offres();

            // Supprimer
            service.supprimer(id);

            // Vérifier qu'elle n'existe plus
            offres supprimee = service.rechercherParId(id);
            assertNull(supprimee, "L'offre supprimée ne doit plus exister");
        });
    }

    @Test
    void recuperer() {
        assertDoesNotThrow(() -> {
            // Ajouter une offre de test
            service.ajouter(testOffre);

            List<offres> liste = service.recuperer();

            // Vérifications de base
            assertNotNull(liste, "La liste ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            // Vérifier que chaque offre a des données valides
            for (offres o : liste) {
                assertTrue(o.getId_offres() > 0, "L'id doit être positif");
                assertNotNull(o.getNom_offre(), "Le nom ne doit pas être null");
                assertNotNull(o.getDescription(), "La description ne doit pas être null");
                assertTrue(o.getPrix() >= 0, "Le prix doit être positif ou nul");
                assertTrue(o.getDuree_offre() > 0, "La durée doit être positive");
            }
        });
    }

    @Test
    void rechercherParId() {
        assertDoesNotThrow(() -> {
            // Ajouter une offre
            service.ajouter(testOffre);

            // Récupérer son id
            List<offres> liste = service.recuperer();
            offres ajoutee = liste.stream()
                    .filter(o -> o.getNom_offre().equals("Offre Test"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(ajoutee);
            int id = ajoutee.getId_offres();

            // Rechercher par id existant
            offres trouve = service.rechercherParId(id);
            assertNotNull(trouve, "L'offre doit être trouvée");
            assertEquals(id, trouve.getId_offres());
            assertEquals("Offre Test", trouve.getNom_offre());
            assertEquals("Description test", trouve.getDescription());
            assertEquals(30, trouve.getDuree_offre());
            assertEquals(9999, trouve.getPrix(), 0.01);

            // Rechercher avec un id inexistant
            offres inexistant = service.rechercherParId(-1);
            assertNull(inexistant, "Un id inexistant doit retourner null");
        });
    }
}