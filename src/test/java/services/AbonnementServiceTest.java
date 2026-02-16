package services;

import models.Abonnements;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AbonnementServiceTest {

    private AbonnementService service;
    private Abonnements testAbonnement;

    @BeforeEach
    void setUp() {
        service = new AbonnementService();
        testAbonnement = new Abonnements();
        testAbonnement.setCin(12345678);
        testAbonnement.setId_offre(1);
        testAbonnement.setDate_inscription("2024-01-01");
        testAbonnement.setDate_expiration("2025-01-01");
        testAbonnement.setSituation("actif");
    }

    @AfterEach
    void tearDown() {
        testAbonnement = null;
    }

    @Test
    void ajouter() {
        assertDoesNotThrow(() -> {
            service.ajouter(testAbonnement);

            // Vérifier que l'abonnement a bien été ajouté
            List<Abonnements> liste = service.recuperer();
            assertNotNull(liste);
            assertFalse(liste.isEmpty());

            // Vérifier que le dernier ajout correspond
            boolean trouve = liste.stream()
                    .anyMatch(a -> a.getCin() == 12345678
                            && a.getId_offre() == 1
                            && a.getSituation().equals("actif"));
            assertTrue(trouve, "L'abonnement ajouté doit être présent dans la liste");
        });
    }

    @Test
    void modifier() {
        assertDoesNotThrow(() -> {
            // Ajouter d'abord un abonnement
            service.ajouter(testAbonnement);

            // Récupérer l'abonnement ajouté
            List<Abonnements> liste = service.recuperer();
            assertFalse(liste.isEmpty());
            Abonnements ajoute = liste.get(liste.size() - 1);

            // Modifier ses données
            ajoute.setSituation("expiré");
            ajoute.setDate_expiration("2024-06-01");
            service.modifier(ajoute);

            // Vérifier la modification
            Abonnements modifie = service.rechercherParId(ajoute.getId_abonn());
            assertNotNull(modifie);
            assertEquals("expiré", modifie.getSituation());
            assertEquals("2024-06-01", modifie.getDate_expiration());
        });
    }

    @Test
    void supprimer() {
        assertDoesNotThrow(() -> {
            // Ajouter un abonnement à supprimer
            service.ajouter(testAbonnement);

            // Récupérer l'abonnement ajouté
            List<Abonnements> liste = service.recuperer();
            assertFalse(liste.isEmpty());
            Abonnements ajoute = liste.get(liste.size() - 1);
            int id = ajoute.getId_abonn();

            // Supprimer l'abonnement
            service.supprimer(id);

            // Vérifier qu'il n'existe plus
            Abonnements supprime = service.rechercherParId(id);
            assertNull(supprime, "L'abonnement supprimé ne doit plus exister");
        });
    }

    @Test
    void recuperer() {
        assertDoesNotThrow(() -> {
            List<Abonnements> liste = service.recuperer();

            // Vérifier que la liste n'est pas null
            assertNotNull(liste, "La liste ne doit pas être null");

            // Vérifier que chaque abonnement a des données valides
            for (Abonnements a : liste) {
                assertNotNull(a.getSituation());
                assertTrue(a.getCin() > 0, "Le CIN doit être positif");
                assertTrue(a.getId_offre() > 0, "L'id_offre doit être positif");
                assertNotNull(a.getDate_inscription());
                assertNotNull(a.getDate_expiration());
            }
        });
    }

    @Test
    void rechercherParId() {
        assertDoesNotThrow(() -> {
            // Ajouter un abonnement
            service.ajouter(testAbonnement);

            // Récupérer son id
            List<Abonnements> liste = service.recuperer();
            assertFalse(liste.isEmpty());
            int id = liste.get(liste.size() - 1).getId_abonn();

            // Rechercher par id
            Abonnements trouve = service.rechercherParId(id);
            assertNotNull(trouve, "L'abonnement doit être trouvé");
            assertEquals(id, trouve.getId_abonn());
            assertEquals(12345678, trouve.getCin());
            assertEquals("actif", trouve.getSituation());

            // Tester avec un id inexistant
            Abonnements inexistant = service.rechercherParId(-1);
            assertNull(inexistant, "Un id inexistant doit retourner null");
        });
    }

    @Test
    void getAbonnementsByUser() {
        assertDoesNotThrow(() -> {
            // Ajouter deux abonnements pour le même CIN
            service.ajouter(testAbonnement);

            Abonnements deuxieme = new Abonnements();
            deuxieme.setCin(12345678);
            deuxieme.setId_offre(1);
            deuxieme.setDate_inscription("2024-03-01");
            deuxieme.setDate_expiration("2025-03-01");
            deuxieme.setSituation("actif");
            service.ajouter(deuxieme);

            // Récupérer les abonnements du user
            List<Abonnements> liste = service.getAbonnementsByUser(12345678);
            assertNotNull(liste, "La liste ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            // Vérifier que tous les abonnements appartiennent au bon CIN
            for (Abonnements a : liste) {
                assertEquals(12345678, a.getCin(),
                        "Tous les abonnements doivent appartenir au CIN 12345678");
            }

            // Tester avec un CIN inexistant
            List<Abonnements> vide = service.getAbonnementsByUser(-1);
            assertNotNull(vide);
            assertTrue(vide.isEmpty(), "Un CIN inexistant doit retourner une liste vide");
        });
    }
}