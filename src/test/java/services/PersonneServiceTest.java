package services;

import models.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PersonneServiceTest {

    private PersonneService service;
    private Utilisateur testUtilisateur;
    private Employe testEmploye;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        service = new PersonneService();

        // Utilisateur (role = 1)
        testUtilisateur = new Utilisateur();
        testUtilisateur.setCin(11111111);
        testUtilisateur.setNom("Ben Ali");
        testUtilisateur.setPrenom("Mohamed");
        testUtilisateur.setTel("22222222");
        testUtilisateur.setDate_naiss("2000-01-15");
        testUtilisateur.setEmail("mohamed.benali@test.com");
        testUtilisateur.setMdp("pass1");  // ← court
        testUtilisateur.setAdresse("Tunis");
        testUtilisateur.setVille("Tunis");
        testUtilisateur.setDate_creationcpt("2024-01-01");
        testUtilisateur.setDate_dernierchg("2024-01-01");

        // Employé (role = 2)
        testEmploye = new Employe();
        testEmploye.setCin(22222222);
        testEmploye.setNom("Trabelsi");
        testEmploye.setPrenom("Sarra");
        testEmploye.setTel("33333333");
        testEmploye.setDate_naiss("1995-05-20");
        testEmploye.setEmail("sarra@test.com");
        testEmploye.setMdp("pass2");  // ← court
        testEmploye.setAdresse("Sfax");
        testEmploye.setVille("Sfax");
        testEmploye.setDate_creationcpt("2024-01-01");
        testEmploye.setDate_dernierchg("2024-01-01");

        // Admin (role = 3)
        testAdmin = new Admin();
        testAdmin.setCin(33333333);
        testAdmin.setNom("Hamdi");
        testAdmin.setPrenom("Ali");
        testAdmin.setTel("44444444");
        testAdmin.setDate_naiss("1990-03-10");
        testAdmin.setEmail("ali@test.com");
        testAdmin.setMdp("pass3");  // ← court
        testAdmin.setAdresse("Sousse");
        testAdmin.setVille("Sousse");
        testAdmin.setDate_creationcpt("2024-01-01");
        testAdmin.setDate_dernierchg("2024-01-01");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Nettoyer les données de test après chaque test
        service.supprimer(11111111);
        service.supprimer(22222222);
        service.supprimer(33333333);
        testUtilisateur = null;
        testEmploye = null;
        testAdmin = null;
    }

    @Test
    void ajouter() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);

            Personne trouve = service.rechercherParId(11111111);
            assertNotNull(trouve, "La personne ajoutée doit être trouvable");
            assertEquals("Ben Ali", trouve.getNom());
            assertEquals("Mohamed", trouve.getPrenom());
            assertEquals("Tunis", trouve.getVille());
            assertEquals(1, trouve.getRole());
            assertInstanceOf(Utilisateur.class, trouve,
                    "Doit être une instance de Utilisateur");
        });
    }

    @Test
    void modifier() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);

            // Modifier les données
            testUtilisateur.setNom("Ben Ali Modifié");
            testUtilisateur.setVille("Bizerte");
            testUtilisateur.setTel("99999999");
            testUtilisateur.setDate_dernierchg("2024-06-01");
            service.modifier(testUtilisateur);

            // Vérifier la modification
            Personne modifie = service.rechercherParId(11111111);
            assertNotNull(modifie);
            assertEquals("Ben Ali Modifié", modifie.getNom());
            assertEquals("Bizerte", modifie.getVille());
            assertEquals("99999999", modifie.getTel());
            assertEquals("2024-06-01", modifie.getDate_dernierchg());
        });
    }

    @Test
    void supprimer() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);

            // Vérifier qu'il existe avant suppression
            assertNotNull(service.rechercherParId(11111111));

            // Supprimer et vérifier
            service.supprimer(11111111);
            assertNull(service.rechercherParId(11111111),
                    "La personne supprimée ne doit plus exister");
        });
    }

    @Test
    void recuperer() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);
            service.ajouter(testEmploye);
            service.ajouter(testAdmin);

            List<Personne> liste = service.recuperer();
            assertNotNull(liste, "La liste ne doit pas être null");
            assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");

            // Vérifier que chaque personne a des données valides
            for (Personne p : liste) {
                assertTrue(p.getCin() > 0, "Le CIN doit être positif");
                assertNotNull(p.getNom(), "Le nom ne doit pas être null");
                assertNotNull(p.getEmail(), "L'email ne doit pas être null");
                assertTrue(p.getRole() >= 1 && p.getRole() <= 3,
                        "Le rôle doit être entre 1 et 3");
                // Vérifier que toString() fonctionne
                assertNotNull(p.toString());
                assertTrue(p.toString().contains(p.getNom()));
            }
        });
    }

    @Test
    void rechercherParId() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);

            // Rechercher avec un CIN existant
            Personne trouve = service.rechercherParId(11111111);
            assertNotNull(trouve, "La personne doit être trouvée");
            assertEquals(11111111, trouve.getCin());
            assertEquals("Ben Ali", trouve.getNom());
            assertEquals("Mohamed", trouve.getPrenom());
            assertEquals("mohamed.benali@test.com", trouve.getEmail());
            assertEquals("Utilisateur", trouve.getRoleNom());

            // Rechercher avec un CIN inexistant
            assertNull(service.rechercherParId(-1),
                    "Un CIN inexistant doit retourner null");
        });
    }

    @Test
    void getUtilisateurs() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);
            service.ajouter(testEmploye);
            service.ajouter(testAdmin);

            List<Utilisateur> utilisateurs = service.getUtilisateurs();
            assertNotNull(utilisateurs, "La liste ne doit pas être null");
            assertFalse(utilisateurs.isEmpty(), "La liste ne doit pas être vide");

            for (Utilisateur u : utilisateurs) {
                assertEquals(1, u.getRole(), "Rôle doit être 1");
                assertEquals("Utilisateur", u.getRoleNom());
                assertInstanceOf(Utilisateur.class, u);
            }

            assertTrue(utilisateurs.stream().anyMatch(u -> u.getCin() == 11111111),
                    "L'utilisateur test doit être dans la liste");

            // Vérifier que les employés et admins ne sont pas dans la liste
            assertFalse(utilisateurs.stream().anyMatch(u -> u.getCin() == 22222222),
                    "Un employé ne doit pas être dans la liste des utilisateurs");
            assertFalse(utilisateurs.stream().anyMatch(u -> u.getCin() == 33333333),
                    "Un admin ne doit pas être dans la liste des utilisateurs");
        });
    }

    @Test
    void getEmployes() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);
            service.ajouter(testEmploye);
            service.ajouter(testAdmin);

            List<Employe> employes = service.getEmployes();
            assertNotNull(employes, "La liste ne doit pas être null");
            assertFalse(employes.isEmpty(), "La liste ne doit pas être vide");

            for (Employe e : employes) {
                assertEquals(2, e.getRole(), "Rôle doit être 2");
                assertEquals("Employé", e.getRoleNom());
                assertInstanceOf(Employe.class, e);
            }

            assertTrue(employes.stream().anyMatch(e -> e.getCin() == 22222222),
                    "L'employé test doit être dans la liste");

            // Vérifier que les utilisateurs et admins ne sont pas dans la liste
            assertFalse(employes.stream().anyMatch(e -> e.getCin() == 11111111),
                    "Un utilisateur ne doit pas être dans la liste des employés");
            assertFalse(employes.stream().anyMatch(e -> e.getCin() == 33333333),
                    "Un admin ne doit pas être dans la liste des employés");
        });
    }

    @Test
    void getAdmins() {
        assertDoesNotThrow(() -> {
            service.ajouter(testUtilisateur);
            service.ajouter(testEmploye);
            service.ajouter(testAdmin);

            List<Admin> admins = service.getAdmins();
            assertNotNull(admins, "La liste ne doit pas être null");
            assertFalse(admins.isEmpty(), "La liste ne doit pas être vide");

            for (Admin a : admins) {
                assertEquals(3, a.getRole(), "Rôle doit être 3");
                assertEquals("Administrateur", a.getRoleNom());
                assertInstanceOf(Admin.class, a);
            }

            assertTrue(admins.stream().anyMatch(a -> a.getCin() == 33333333),
                    "L'admin test doit être dans la liste");

            // Vérifier que les utilisateurs et employés ne sont pas dans la liste
            assertFalse(admins.stream().anyMatch(a -> a.getCin() == 11111111),
                    "Un utilisateur ne doit pas être dans la liste des admins");
            assertFalse(admins.stream().anyMatch(a -> a.getCin() == 22222222),
                    "Un employé ne doit pas être dans la liste des admins");
        });
    }




}