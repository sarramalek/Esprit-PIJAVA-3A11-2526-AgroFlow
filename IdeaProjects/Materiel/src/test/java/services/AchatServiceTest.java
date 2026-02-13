package services;

import entities.Achat;
import entities.Machine;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AchatServiceTest {

    private static AchatService achatService;
    private static MachineService machineService;
    private static Machine testMachine;

    @BeforeAll
    static void setUp() throws SQLException {
        achatService = new AchatService();
        machineService = new MachineService();

        // Créer une machine temporaire pour lier les achats
        testMachine = new Machine(0, "TestBrand", "TestModel", "Disponible", "SNTEST123", LocalDate.now(), "Eya");
        machineService.ajouter(testMachine);
        System.out.println("Machine de test créée : " + testMachine);
    }

    @Test
    @Order(1)
    void testAjouterAchat() throws SQLException {
        System.out.println("=== Test: Ajout d'un achat ===");

        Achat achat = new Achat(0, LocalDate.now(), testMachine.getIdM(), 12345678, 5);
        achatService.ajouter(achat);

        // Vérifier que l'achat a bien été ajouté
        List<Achat> achats = achatService.recuperer();
        Achat dernier = achats.get(achats.size() - 1);

        assertNotNull(dernier, "L'achat ajouté doit exister dans la base.");
        assertEquals(testMachine.getIdM(), dernier.getIdM(), "L'achat doit être lié à la machine de test.");

        System.out.println("Achat ajouté avec succès : " + dernier);
    }

    @Test
    @Order(2)
    void testModifierAchat() throws SQLException {
        System.out.println("=== Test: Modification d'un achat ===");

        List<Achat> achats = achatService.recuperer();
        Achat achat = achats.get(achats.size() - 1);

        System.out.println("Avant modification : " + achat);

        // ⚠️ Ne pas modifier le CIN pour éviter la violation de clé étrangère
        achat.setQuantite(10); // Modifier uniquement la quantité

        int result = achatService.modifier(achat);
        assertEquals(1, result, "Une seule ligne doit être modifiée.");

        System.out.println("Après modification : " + achat);
    }

    @Test
    @Order(3)
    void testRecupererAchats() throws SQLException {
        System.out.println("=== Test: Récupération des achats ===");

        List<Achat> achats = achatService.recuperer();
        assertNotNull(achats, "La liste des achats ne doit pas être nulle.");
        assertFalse(achats.isEmpty(), "Il doit y avoir au moins un achat.");

        achats.forEach(System.out::println);
    }

    @Test
    @Order(4)
    void testSupprimerAchat() throws SQLException {
        System.out.println("=== Test: Suppression d'un achat ===");

        List<Achat> achats = achatService.recuperer();
        Achat achat = achats.get(achats.size() - 1);

        int result = achatService.supprimer(achat.getIdAchat());
        assertEquals(1, result, "Une seule ligne doit être supprimée.");

        System.out.println("Achat supprimé : " + achat);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Supprimer la machine de test après tous les tests
        machineService.supprimer(testMachine);
        System.out.println("Machine de test supprimée : " + testMachine);
    }
}
