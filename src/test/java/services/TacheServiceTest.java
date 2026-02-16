package services;

import models.Tache;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TacheServiceTest {

    private TacheService service;
    private Tache testTache;

    @BeforeEach
    void setUp() {
        service = new TacheService();

        testTache = new Tache();
        testTache.setNom_tache("Tache Test");
        testTache.setDescription("Description tache test");
        testTache.setAssignee(0);
        testTache.setEtat("en_attente");
        testTache.setPriorite("moyenne");
        testTache.setDate_echeancee("2025-12-31");
    }

    @AfterEach
    void tearDown() throws SQLException {
        List<Tache> liste = service.recuperer();
        for (Tache t : liste) {
            if (t.getNom_tache().equals("Tache Test") ||
                    t.getNom_tache().equals("Tache Modifiee")) {
                service.supprimer(t.getId_tache());
            }
        }
        testTache = null;
    }

    @Test
    void ajouter() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            assertNotNull(liste, "La liste ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            boolean trouve = liste.stream()
                    .anyMatch(t -> t.getNom_tache().equals("Tache Test")
                            && t.getDescription().equals("Description tache test")
                            && t.getEtat().equals("en_attente")
                            && t.getPriorite().equals("moyenne")
                            && t.getDate_echeancee().equals("2025-12-31"));
            assertTrue(trouve, "La tâche ajoutée doit être présente dans la liste");
        });
    }

    @Test
    void modifier() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            Tache ajoutee = liste.stream()
                    .filter(t -> t.getNom_tache().equals("Tache Test"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(ajoutee, "La tâche doit exister avant modification");

            // Modifier tous les champs
            ajoutee.setNom_tache("Tache Modifiee");
            ajoutee.setDescription("Description modifiee");
            ajoutee.setAssignee(1);
            ajoutee.setEtat("en_cours");
            ajoutee.setPriorite("haute");
            ajoutee.setDate_echeancee("2026-06-30");
            service.modifier(ajoutee);

            // Vérifier toutes les modifications
            Tache modifiee = service.rechercherParId(ajoutee.getId_tache());
            assertNotNull(modifiee, "La tâche modifiée doit exister");
            assertEquals("Tache Modifiee", modifiee.getNom_tache());
            assertEquals("Description modifiee", modifiee.getDescription());
            assertEquals(1, modifiee.getAssignee());
            assertEquals("en_cours", modifiee.getEtat());
            assertEquals("haute", modifiee.getPriorite());
            assertEquals("2026-06-30", modifiee.getDate_echeancee());
        });
    }

    @Test
    void supprimer() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            Tache ajoutee = liste.stream()
                    .filter(t -> t.getNom_tache().equals("Tache Test"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(ajoutee, "La tâche doit exister avant suppression");
            int id = ajoutee.getId_tache();

            service.supprimer(id);

            Tache supprimee = service.rechercherParId(id);
            assertNull(supprimee, "La tâche supprimée ne doit plus exister");
        });
    }

    @Test
    void recuperer() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            assertNotNull(liste, "La liste ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            for (Tache t : liste) {
                assertTrue(t.getId_tache() > 0, "L'id doit être positif");
                assertNotNull(t.getNom_tache(), "Le nom ne doit pas être null");
                assertNotNull(t.getDescription(), "La description ne doit pas être null");
                assertNotNull(t.getEtat(), "L'état ne doit pas être null");
                assertNotNull(t.getPriorite(), "La priorité ne doit pas être null");
            }
        });
    }

    @Test
    void rechercherParId() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            Tache ajoutee = liste.stream()
                    .filter(t -> t.getNom_tache().equals("Tache Test"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(ajoutee);
            int id = ajoutee.getId_tache();

            // Rechercher avec id existant
            Tache trouve = service.rechercherParId(id);
            assertNotNull(trouve, "La tâche doit être trouvée");
            assertEquals(id, trouve.getId_tache());
            assertEquals("Tache Test", trouve.getNom_tache());
            assertEquals("Description tache test", trouve.getDescription());
            assertEquals("en_attente", trouve.getEtat());
            assertEquals("moyenne", trouve.getPriorite());
            assertEquals("2025-12-31", trouve.getDate_echeancee());

            // Rechercher avec id inexistant
            Tache inexistant = service.rechercherParId(-1);
            assertNull(inexistant, "Un id inexistant doit retourner null");
        });
    }
}