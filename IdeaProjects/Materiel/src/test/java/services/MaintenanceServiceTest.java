package services;

import entities.Maintenance;
import entities.Machine;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MaintenanceServiceTest {

    private static MaintenanceService maintenanceService;
    private static MachineService machineService;
    private static Machine testMachine;

    @BeforeAll
    static void setUp() throws SQLException {
        maintenanceService = new MaintenanceService();
        machineService = new MachineService();

        // Créer une machine temporaire pour les tests de maintenance
        testMachine = new Machine(0, "TestBrand", "TestModel", "Disponible", "SN999", LocalDate.now(), "Eya");
        machineService.ajouter(testMachine);
        System.out.println("Machine de test créée : " + testMachine);
    }

    @Test
    @Order(1)
    void testAjouterMaintenance() throws SQLException {
        System.out.println("=== Test: Ajout d'une maintenance ===");

        Maintenance maintenance = new Maintenance(
                0,
                "Panne moteur",
                250.0,
                LocalDate.now(),
                "Changement du moteur principal",
                testMachine.getIdM()
        );

        maintenanceService.ajouter(maintenance);

        assertTrue(maintenance.getIdMain() > 0, "L'ID de la maintenance doit être généré.");
        System.out.println("Maintenance ajoutée avec succès : " + maintenance);
    }

    @Test
    @Order(2)
    void testModifierMaintenance() throws SQLException {
        System.out.println("=== Test: Modification d'une maintenance ===");

        List<Maintenance> maintenances = maintenanceService.recupererParMachine(testMachine.getIdM());
        Maintenance maintenance = maintenances.get(maintenances.size() - 1);

        System.out.println("Avant modification : " + maintenance);

        maintenance.setTypePanne("Panne hydraulique");
        maintenance.setCout(300.0);

        int result = maintenanceService.modifier(maintenance);
        assertEquals(1, result, "Une seule maintenance doit être modifiée.");

        System.out.println("Après modification : " + maintenance);
    }

    @Test
    @Order(3)
    void testRecupererMaintenance() throws SQLException {
        System.out.println("=== Test: Récupération des maintenances ===");

        List<Maintenance> maintenances = maintenanceService.recupererParMachine(testMachine.getIdM());
        assertNotNull(maintenances, "La liste des maintenances ne doit pas être nulle.");
        assertFalse(maintenances.isEmpty(), "Il doit y avoir au moins une maintenance pour la machine.");

        maintenances.forEach(System.out::println);
    }

    @Test
    @Order(4)
    void testSupprimerMaintenance() throws SQLException {
        System.out.println("=== Test: Suppression d'une maintenance ===");

        List<Maintenance> maintenances = maintenanceService.recupererParMachine(testMachine.getIdM());
        Maintenance maintenance = maintenances.get(maintenances.size() - 1);

        int result = maintenanceService.supprimer(maintenance.getIdMain());
        assertEquals(1, result, "Une seule maintenance doit être supprimée.");

        System.out.println("Maintenance supprimée : " + maintenance);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Supprimer la machine de test après tous les tests
        machineService.supprimer(testMachine);
        System.out.println("Machine de test supprimée : " + testMachine);
    }
}
