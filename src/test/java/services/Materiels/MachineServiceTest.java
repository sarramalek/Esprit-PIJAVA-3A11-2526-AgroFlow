package services.Materiels;

import entities.Machine;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MachineServiceTest {

    private static MachineService service;

    @BeforeAll
    static void setUp() {
        service = new MachineService();
        System.out.println("=== Initialisation de MachineService pour les tests ===");
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("=== Test: Ajout d'une machine ===");

        // Création d'une nouvelle machine
        Machine machine = new Machine(
                0, // id sera généré automatiquement
                "John Deere",
                "X120",
                "Disponible",
                "SN12345",
                LocalDate.now(),
                "Machine Tracteur "
        );

        // Ajout dans la base
        service.ajouter(machine);

        // Vérification que l'ID a bien été généré
        assertTrue(machine.getIdM() > 0, "L'ID de la machine doit être supérieur à 0 après ajout.");

        // Affichage pour vérification manuelle
        System.out.println("Machine ajoutée avec succès : " + machine);
        System.out.println("Liste actuelle des machines : " + service.recuperer());
    }

    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("=== Test: Modification d'une machine ===");

        // Récupération de la dernière machine ajoutée
        Machine machine = service.recuperer()
                .get(service.recuperer().size() - 1);

        System.out.println("Avant modification : " + machine);

        // Modification des données
        machine.setMarque("Case");
        machine.setModele("MX100");

        int result = service.modifier(machine);

        // Vérification que la modification a affecté 1 ligne
        assertEquals(1, result, "Une seule machine doit être modifiée.");

        System.out.println("Après modification : " + machine);
    }

    /*
    @Test
    @Order(3)
    void supprimer() throws SQLException {
        System.out.println("=== Test: Suppression d'une machine ===");

        // Récupération de la dernière machine
        Machine machine = service.recuperer()
                .get(service.recuperer().size() - 1);

        // Suppression
        int result = service.supprimer(machine);

        // Vérification
        assertEquals(1, result, "Une seule machine doit être supprimée.");

        System.out.println("Machine supprimée : " + machine);
    }
    */

    @Test
    @Order(4)
    void recuperer() throws SQLException {
        System.out.println("=== Test: Récupération de toutes les machines ===");

        List<Machine> machines = service.recuperer();

        assertNotNull(machines, "La liste des machines ne doit pas être nulle.");
        System.out.println("Machines récupérées : " + machines);
    }
}
