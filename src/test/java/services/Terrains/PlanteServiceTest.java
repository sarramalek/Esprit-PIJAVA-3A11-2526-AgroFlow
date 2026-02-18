package services.Terrains;

import models.Terrains.plante;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlanteServiceTest {

    private static PlanteService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Initialisation du service de test...");
        service = new PlanteService();
    }

    @Test
    @Order(1)
    void ajouter() {
        System.out.println("Test de l'ajout...");
        // On crée une plante de test
        plante p = new plante(0, "JUnitPlante", "TestVariete", 0.7f, 100);

        int tailleAvant = service.afficherToutes().size();
        service.ajouter(p);

        List<plante> liste = service.afficherToutes();
        assertFalse(liste.isEmpty(), "La liste ne devrait pas être vide après l'ajout.");
        assertEquals(tailleAvant + 1, liste.size(), "La taille de la liste devrait augmenter de 1.");

        // Vérification que le nom de la dernière plante insérée est correct
        assertEquals("JUnitPlante", liste.get(liste.size() - 1).getNom_p());
    }

    @Test
    @Order(2)
    void modifier() {
        System.out.println("Test de la modification...");
        List<plante> liste = service.afficherToutes();
        plante dernierePlante = liste.get(liste.size() - 1);

        dernierePlante.setNom_p("JUnitModifiee");
        dernierePlante.setBesoin_eau(0.9f);

        service.modifier(dernierePlante);

        // On récupère à nouveau la liste pour vérifier en base
        plante pModifiee = service.afficherToutes().get(service.afficherToutes().size() - 1);
        assertEquals("JUnitModifiee", pModifiee.getNom_p());
        assertEquals(0.9f, pModifiee.getBesoin_eau(), 0.01);
    }

    @Test
    @Order(3)
    void afficherToutes() {
        System.out.println("Test de l'affichage...");
        List<plante> liste = service.afficherToutes();
        assertNotNull(liste);
        assertTrue(liste.size() > 0);
    }

    @Test
    @Order(4)
    void supprimer() {
        System.out.println("Test de la suppression...");
        List<plante> listeAvant = service.afficherToutes();
        int idASupprimer = listeAvant.get(listeAvant.size() - 1).getId_plante();

        service.supprimer(idASupprimer);

        List<plante> listeApres = service.afficherToutes();
        // On vérifie qu'aucune plante dans la liste n'a l'ID supprimé
        boolean existeEncore = listeApres.stream().anyMatch(p -> p.getId_plante() == idASupprimer);
        assertFalse(existeEncore, "La plante devrait être supprimée de la base.");
    }
}