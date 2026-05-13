package services.Terrains;

import models.Terrains.rotation;
import models.Terrains.plante;
import models.Terrains.terrain;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import java.util.Calendar;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RotationServiceTest {

    private static RotationService rotationService;
    private static PlanteService planteService;
    private static TerrainService terrainService;

    private static int testIdPlante;
    private static int testIdTerrain;

    @BeforeAll
    static void setUp() {
        System.out.println("Initialisation des services pour le test de Rotation...");
        rotationService = new RotationService();
        planteService = new PlanteService();
        terrainService = new TerrainService();

        // On récupère ou on crée une plante et un terrain pour les clés étrangères
        List<plante> plantes = planteService.afficherToutes();
        List<terrain> terrains = terrainService.afficherTous();

        if (plantes.isEmpty()) {
            planteService.ajouter(new plante(0, "PlanteTest", "VarieteTest", 0.5f, 30));
            plantes = planteService.afficherToutes();
        }
        if (terrains.isEmpty()) {
            terrainService.ajouter(new terrain(0, "TerrainTest", 1.0f, "SolTest", "LocTest", 7.0f));
            terrains = terrainService.afficherTous();
        }

        testIdPlante = plantes.get(0).getId_plante();
        testIdTerrain = terrains.get(0).getId_terrain();
    }

    @Test
    @Order(1)
    void ajouter() {
        System.out.println("Running ajouter rotation test...");

        Date debut = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 2);
        Date fin = cal.getTime();

        rotation r = new rotation(0, testIdTerrain, testIdPlante, debut, fin, 0);
        int tailleAvant = rotationService.afficherToutes().size();

        rotationService.ajouter(r);

        List<rotation> liste = rotationService.afficherToutes();
        assertEquals(tailleAvant + 1, liste.size(), "La rotation devrait être ajoutée.");
    }

    @Test
    @Order(2)
    void modifier() {
        System.out.println("Running modifier rotation test...");
        List<rotation> liste = rotationService.afficherToutes();
        rotation derniere = liste.get(liste.size() - 1);

        derniere.setStatus(1); // On change le statut à "Terminé"
        rotationService.modifier(derniere);

        rotation modifiee = rotationService.afficherToutes().get(rotationService.afficherToutes().size() - 1);
        assertEquals(1, modifiee.getStatus(), "Le statut devrait être mis à jour à 1.");
    }

    @Test
    @Order(3)
    void afficherToutes() {
        System.out.println("Running afficherToutes rotation test...");
        List<rotation> liste = rotationService.afficherToutes();
        assertNotNull(liste);
        assertFalse(liste.isEmpty());
    }

    @Test
    @Order(4)
    void supprimer() {
        System.out.println("Running supprimer rotation test...");
        List<rotation> liste = rotationService.afficherToutes();
        int idASupprimer = liste.get(liste.size() - 1).getId_rotation();

        rotationService.supprimer(idASupprimer);

        List<rotation> listeApres = rotationService.afficherToutes();
        boolean encorePresent = listeApres.stream().anyMatch(r -> r.getId_rotation() == idASupprimer);
        assertFalse(encorePresent, "La rotation devrait avoir disparu de la base.");
    }
}