package services.Materiels;

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
    private static Integer testAchatId;

    @BeforeAll
    static void setUp() throws SQLException {
        achatService = new AchatService();
        machineService = new MachineService();

        // Créer une machine temporaire pour lier les achats
        testMachine = new Machine(0, "TestBrand", "TestModel", "Disponible",
                "SNTEST" + System.currentTimeMillis(),
                LocalDate.now(), "Eya");
        machineService.ajouter(testMachine);

        System.out.println("Machine de test créée avec ID : " + testMachine.getIdM());

        // Vérifier que l'ID a été généré
        assertTrue(testMachine.getIdM() > 0, "L'ID de la machine doit être généré");
    }

    @Test
    @Order(1)
    void testAjouterAchat() throws SQLException {
        System.out.println("\n=== Test: Ajout d'un achat ===");

        // Créer un achat
        Achat achat = new Achat(0, LocalDate.now(), testMachine.getIdM(), 12345678, 5);

        System.out.println("Avant ajout - ID achat : " + achat.getIdAchat());
        System.out.println("ID Machine utilisé : " + testMachine.getIdM());

        // Ajouter l'achat
        achatService.ajouter(achat);

        System.out.println("Après ajout - ID achat : " + achat.getIdAchat());

        // Vérifier que l'ID a été généré
        assertNotNull(achat.getIdAchat(), "L'ID de l'achat ne doit pas être null");
        assertTrue(achat.getIdAchat() > 0, "L'ID de l'achat doit être positif");

        // Stocker l'ID pour les autres tests
        testAchatId = achat.getIdAchat();

        // Vérifier en récupérant depuis la base
        Achat achatRecupere = achatService.recupererParId(testAchatId);
        assertNotNull(achatRecupere, "L'achat doit être présent en base");
        assertEquals(testMachine.getIdM(), achatRecupere.getIdM());
        assertEquals(5, achatRecupere.getQuantite());
        assertEquals(12345678, achatRecupere.getCin());

        System.out.println("✅ Achat ajouté avec succès : " + achatRecupere);
        System.out.println("⚠️  ALLEZ VÉRIFIER DANS PHPMYADMIN - L'achat ne sera PAS supprimé");
    }

    @Test
    @Order(2)
    void testModifierAchat() throws SQLException {
        System.out.println("\n=== Test: Modification d'un achat ===");

        assertNotNull(testAchatId, "L'ID de l'achat de test doit exister");

        // Récupérer l'achat
        Achat achat = achatService.recupererParId(testAchatId);
        assertNotNull(achat, "L'achat doit exister avant modification");

        System.out.println("Avant modification : " + achat);

        // Modifier la quantité
        achat.setQuantite(10);
        int result = achatService.modifier(achat);

        assertEquals(1, result, "Une ligne doit être modifiée");

        // Vérifier la modification en base
        Achat achatModifie = achatService.recupererParId(testAchatId);
        assertEquals(10, achatModifie.getQuantite(), "La quantité doit être 10");

        System.out.println("Après modification : " + achatModifie);
        System.out.println("✅ Modification réussie");
        System.out.println("⚠️  VÉRIFIEZ DANS PHPMYADMIN - La quantité doit être 10");
    }

    @Test
    @Order(3)
    void testRecupererAchats() throws SQLException {
        System.out.println("\n=== Test: Récupération des achats ===");

        List<Achat> achats = achatService.recuperer();

        assertNotNull(achats, "La liste ne doit pas être nulle");
        assertFalse(achats.isEmpty(), "La liste ne doit pas être vide");

        // Vérifier que notre achat de test est présent
        boolean trouve = achats.stream()
                .anyMatch(a -> a.getIdAchat() == testAchatId);

        assertTrue(trouve, "L'achat de test doit être dans la liste");

        System.out.println("Nombre d'achats : " + achats.size());
        System.out.println("Achat de test ID : " + testAchatId);
        System.out.println("✅ Récupération réussie");
    }

    // ========================================================
    // TEST DE SUPPRESSION COMMENTÉ POUR GARDER LES DONNÉES
    // ========================================================
    /*
    @Test
    @Order(4)
    void testSupprimerAchat() throws SQLException {
        System.out.println("\n=== Test: Suppression d'un achat ===");

        assertNotNull(testAchatId, "L'ID de l'achat de test doit exister");

        // Supprimer l'achat
        int result = achatService.supprimer(testAchatId);

        assertEquals(1, result, "Une ligne doit être supprimée");

        // Vérifier que l'achat n'existe plus
        Achat achatSupprime = achatService.recupererParId(testAchatId);
        assertNull(achatSupprime, "L'achat ne doit plus exister");

        System.out.println("✅ Suppression réussie");
    }
    */

    @AfterAll
    static void tearDown() throws SQLException {
        System.out.println("\n=== Nettoyage final ===");

        System.out.println("⚠️  Machine de test NON supprimée (ID: " + testMachine.getIdM() + ")");
        System.out.println("⚠️  Achat de test NON supprimé (ID: " + testAchatId + ")");



    }
}
