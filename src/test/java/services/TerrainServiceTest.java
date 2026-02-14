package services;

import entities.terrain;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TerrainServiceTest {

    private static TerrainService service;

    @BeforeAll
    static void setUp() {
        System.out.println("Initialisation du service Terrain pour les tests...");
        service = new TerrainService();
    }

    @Test
    @Order(1)
    void ajouter() {
        System.out.println("Running ajouter terrain test...");
        // On crée un terrain de test
        terrain t = new terrain(0, "Zone Test JUnit", 20.5f, "Argileux", "Ariana Test", 6.8f);

        int tailleAvant = service.afficherTous().size();
        service.ajouter(t);

        List<terrain> liste = service.afficherTous();
        assertFalse(liste.isEmpty(), "La liste ne doit pas être vide.");
        assertEquals(tailleAvant + 1, liste.size(), "La taille doit avoir augmenté de 1.");

        // On vérifie le nom du dernier terrain ajouté
        assertEquals("Zone Test JUnit", liste.get(liste.size() - 1).getNom_terrain());
    }

    @Test
    @Order(2)
    void modifier() {
        System.out.println("Running modifier terrain test...");
        List<terrain> liste = service.afficherTous();
        terrain dernier = liste.get(liste.size() - 1);

        dernier.setNom_terrain("Zone Modifiée");
        dernier.setP_h(7.2f);

        service.modifier(dernier);

        // On récupère la version en base pour comparer
        terrain tModifie = service.afficherTous().get(service.afficherTous().size() - 1);
        assertEquals("Zone Modifiée", tModifie.getNom_terrain());
        assertEquals(7.2f, tModifie.getP_h(), 0.01);
    }

    @Test
    @Order(3)
    void afficherTous() {
        System.out.println("Running afficherTous test...");
        List<terrain> liste = service.afficherTous();
        assertNotNull(liste);
        assertTrue(liste.size() > 0, "La liste devrait contenir au moins le terrain de test.");
    }

    @Test
    @Order(4)
    void supprimer() {
        System.out.println("Running supprimer terrain test...");
        List<terrain> listeAvant = service.afficherTous();
        int idASupprimer = listeAvant.get(listeAvant.size() - 1).getId_terrain();

        service.supprimer(idASupprimer);

        List<terrain> listeApres = service.afficherTous();
        // On vérifie que l'ID n'est plus présent dans le flux de données
        boolean existeEncore = listeApres.stream().anyMatch(t -> t.getId_terrain() == idASupprimer);
        assertFalse(existeEncore, "Le terrain devrait être supprimé de la base.");
    }

}