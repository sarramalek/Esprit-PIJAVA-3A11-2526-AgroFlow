import entities.examens;
import org.junit.jupiter.api.*;
import services.ServiceExamen;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class examensServiceTest {
    private static ServiceExamen service;

    @BeforeAll
    static void setUp() {
        System.out.println("Initialisation du service Examen...");
        service = new ServiceExamen();
    }

    @Test
    @Order(1)
    void afficher() throws SQLException {
        System.out.println("Running afficher examen test...");
        List<examens> liste = service.afficher();

        assertNotNull(liste, "La liste ne doit pas être null.");
        // Style Mr : on vérifie si la liste contient bien des objets examens
        if (!liste.isEmpty()) {
            assertTrue(liste.get(0) instanceof examens);
        }
    }

    @Test
    @Order(2)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter examen test...");

        // CORRECTION DE L'ERREUR ROUGE :
        // L'ordre doit être : (id, date_examen, type_examen, diagnostic, traitement, id_animal)
        // D'après ton message d'erreur : int, Date, String, String, String, int
        examens examen = new examens(0, new java.util.Date(), "Radio", "Suspicion fracture", "Repos", 1);

        service.ajouter(examen);

        List<examens> liste = service.afficher();
        assertFalse(liste.isEmpty());

        boolean trouve = liste.stream()
                .anyMatch(e -> e.getType_examen().equalsIgnoreCase("Radio"));

        assertTrue(trouve, "L'examen 'Radio' n'a pas été ajouté !");
    }

    @Test
    @Order(3)
    void modifier() throws SQLException {
        System.out.println("Running modifier examen test...");
        List<examens> liste = service.afficher();
        examens examen = liste.get(liste.size() - 1);

        examen.setType_examen("Scanner");
        examen.setDiagnostic("OK");

        service.modifier(examen);

        boolean trouve = service.afficher().stream()
                .anyMatch(e -> e.getType_examen().equals("Scanner") && e.getDiagnostic().equals("OK"));

        assertTrue(trouve);
    }

    /*@Test
    @Order(4)
    void supprimer() throws SQLException {
        System.out.println("Running supprimer examen test...");
        List<examens> liste = service.afficher();
        int idASupprimer = liste.get(liste.size() - 1).getId();

        service.supprimer(idASupprimer);

        boolean existe = service.afficher().stream()
                .anyMatch(e -> e.getId() == idASupprimer);

        assertFalse(existe);
    }*/
}