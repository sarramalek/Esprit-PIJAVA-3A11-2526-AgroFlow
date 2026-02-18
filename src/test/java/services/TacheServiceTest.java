package services;

import models.Employe;
import models.Tache;
import org.junit.jupiter.api.*;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TacheServiceTest {

    private TacheService service;
    private PersonneService personneService;
    private Tache testTache;
    private Connection connection;

    // Two real users we'll insert via PersonneService
    private static final int TEST_CIN_1 = 99999991;
    private static final int TEST_CIN_2 = 99999992;

    @BeforeEach
    void setUp() throws SQLException {
        connection = MyDatabase.getInstance().getConnection();
        personneService = new PersonneService();

        // ✅ Insert user 1 — used as initial assignee
        Employe u1 = new Employe();
        u1.setCin(TEST_CIN_1);
        u1.setNom("Test");
        u1.setPrenom("UserOne");
        u1.setTel("11111111");
        u1.setDate_naiss("1990-01-01");
        u1.setEmail("testuser1@tachetest.com");
        u1.setMdp("pass1");
        u1.setAdresse("Tunis");
        u1.setVille("Tunis");
        u1.setDate_creationcpt("2024-01-01");
        u1.setDate_dernierchg("2024-01-01");
        personneService.supprimer(TEST_CIN_1); // clean up if leftover
        personneService.ajouter(u1);

        // ✅ Insert user 2 — used in modifier test as new assignee
        Employe u2 = new Employe();
        u2.setCin(TEST_CIN_2);
        u2.setNom("Test");
        u2.setPrenom("UserTwo");
        u2.setTel("22222222");
        u2.setDate_naiss("1991-01-01");
        u2.setEmail("testuser2@tachetest.com");
        u2.setMdp("pass2");
        u2.setAdresse("Sfax");
        u2.setVille("Sfax");
        u2.setDate_creationcpt("2024-01-01");
        u2.setDate_dernierchg("2024-01-01");
        personneService.supprimer(TEST_CIN_2); // clean up if leftover
        personneService.ajouter(u2);

        // ✅ Init service and test tache using real cin as assignee
        service = new TacheService();

        testTache = new Tache();
        testTache.setNom_tache("Tache Test");
        testTache.setDescription("Description tache test");
        testTache.setAssignee(TEST_CIN_1); // ✅ real int cin that exists in users
        testTache.setEtat("en_attente");
        testTache.setPriorite("moyenne");
        testTache.setDate_echeancee("2025-12-31");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // ✅ Delete taches first (child), then users (parent)
        connection.createStatement().executeUpdate(
                "DELETE FROM taches WHERE assignee IN (" + TEST_CIN_1 + ", " + TEST_CIN_2 + ")"
        );
        // Also sweep by name in case assignee was changed
        List<Tache> liste = service.recuperer();
        for (Tache t : liste) {
            if (t.getNom_tache().equals("Tache Test") ||
                    t.getNom_tache().equals("Tache Modifiee")) {
                service.supprimer(t.getId_tache());
            }
        }
        personneService.supprimer(TEST_CIN_1);
        personneService.supprimer(TEST_CIN_2);
        testTache = null;
    }

    @Test
    void ajouter() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            assertNotNull(liste);
            assertFalse(liste.isEmpty());

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

            ajoutee.setNom_tache("Tache Modifiee");
            ajoutee.setDescription("Description modifiee");
            ajoutee.setAssignee(TEST_CIN_2); // ✅ valid second user cin
            ajoutee.setEtat("en_cours");
            ajoutee.setPriorite("haute");
            ajoutee.setDate_echeancee("2026-06-30");
            service.modifier(ajoutee);

            Tache modifiee = service.rechercherParId(ajoutee.getId_tache());
            assertNotNull(modifiee);
            assertEquals("Tache Modifiee",      modifiee.getNom_tache());
            assertEquals("Description modifiee", modifiee.getDescription());
            assertEquals(TEST_CIN_2,             modifiee.getAssignee()); // ✅ int
            assertEquals("en_cours",             modifiee.getEtat());
            assertEquals("haute",                modifiee.getPriorite());
            assertEquals("2026-06-30",           modifiee.getDate_echeancee());
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
            assertNotNull(ajoutee);
            int id = ajoutee.getId_tache();

            service.supprimer(id);

            assertNull(service.rechercherParId(id),
                    "La tâche supprimée ne doit plus exister");
        });
    }

    @Test
    void recuperer() {
        assertDoesNotThrow(() -> {
            service.ajouter(testTache);

            List<Tache> liste = service.recuperer();
            assertNotNull(liste);
            assertFalse(liste.isEmpty());

            for (Tache t : liste) {
                assertTrue(t.getId_tache() > 0);
                assertNotNull(t.getNom_tache());
                assertNotNull(t.getDescription());
                assertNotNull(t.getEtat());
                assertNotNull(t.getPriorite());
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

            Tache trouve = service.rechercherParId(id);
            assertNotNull(trouve);
            assertEquals(id,                       trouve.getId_tache());
            assertEquals("Tache Test",             trouve.getNom_tache());
            assertEquals("Description tache test", trouve.getDescription());
            assertEquals("en_attente",             trouve.getEtat());
            assertEquals("moyenne",                trouve.getPriorite());
            assertEquals("2025-12-31",             trouve.getDate_echeancee());

            assertNull(service.rechercherParId(-1),
                    "Un id inexistant doit retourner null");
        });
    }
}