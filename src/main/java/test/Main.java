/*package test;

import models.*;
import services.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static PersonneService personneService;
    private static OffresServicees offreService;
    private static AbonnementService abonnementService;
    private static TacheService tacheService;
    //private static affectService affectationService;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   SYSTÈME DE GESTION AGROFLOW - UTILISATEURS  ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Initialiser tous les services
        try {
            personneService = new PersonneService();
            offreService = new OffresServicees();
            abonnementService = new AbonnementService();
            tacheService = new TacheService();
            affectationService = new affectService();
            System.out.println("✓ Tous les services ont été initialisés avec succès!\n");
        } catch (Exception e) {
            System.err.println("✗ Erreur d'initialisation: " + e.getMessage());
            return;
        }

        boolean continuer = true;
        while (continuer) {
            afficherMenuPrincipal();
            int choix = lireChoix();

            try {
                switch (choix) {
                    case 1:
                        menuPersonnes();
                        break;
                    case 2:
                        menuOffres();
                        break;
                    case 3:
                        menuAbonnements();
                        break;
                    case 4:
                        menuTaches();
                        break;
                    case 5:
                        menuAffectations();
                        break;
                    case 0:
                        continuer = false;
                        System.out.println("\n👋 Merci d'avoir utilisé le système. Au revoir !");
                        break;
                    default:
                        System.out.println("❌ Choix invalide. Veuillez réessayer.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur SQL: " + e.getMessage());
            }

            if (continuer) {
                System.out.println("\nAppuyez sur Entrée pour continuer...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void afficherMenuPrincipal() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         MENU PRINCIPAL                 ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 1. Gestion des Personnes              ║");
        System.out.println("║ 2. Gestion des Offres                 ║");
        System.out.println("║ 3. Gestion des Abonnements            ║");
        System.out.println("║ 4. Gestion des Tâches                 ║");
        System.out.println("║ 5. Gestion des Affectations           ║");
        System.out.println("║ 0. Quitter                             ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.print("Votre choix : ");
    }

    // ========== MENU PERSONNES ==========
    private static void menuPersonnes() throws SQLException {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║       GESTION DES PERSONNES            ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. Ajouter une personne               ║");
            System.out.println("║ 2. Afficher toutes les personnes      ║");
            System.out.println("║ 3. Afficher par rôle                  ║");
            System.out.println("║ 4. Rechercher une personne            ║");
            System.out.println("║ 5. Modifier une personne              ║");
            System.out.println("║ 6. Supprimer une personne             ║");
            System.out.println("║ 0. Retour au menu principal           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();
            switch (choix) {
                case 1:
                    ajouterPersonne();
                    break;
                case 2:
                    afficherToutesPersonnes();
                    break;
                case 3:
                    afficherParRole();
                    break;
                case 4:
                    rechercherPersonne();
                    break;
                case 5:
                    modifierPersonne();
                    break;
                case 6:
                    supprimerPersonne();
                    break;
                case 0:
                    retour = true;
                    break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    // ========== MENU OFFRES ==========
    private static void menuOffres() throws SQLException {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        GESTION DES OFFRES              ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. Ajouter une offre                  ║");
            System.out.println("║ 2. Afficher toutes les offres         ║");
            System.out.println("║ 3. Rechercher une offre               ║");
            System.out.println("║ 4. Modifier une offre                 ║");
            System.out.println("║ 5. Supprimer une offre                ║");
            System.out.println("║ 0. Retour au menu principal           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();
            switch (choix) {
                case 1:
                    ajouterOffre();
                    break;
                case 2:
                    afficherToutesOffres();
                    break;
                case 3:
                    rechercherOffre();
                    break;
                case 4:
                    modifierOffre();
                    break;
                case 5:
                    supprimerOffre();
                    break;
                case 0:
                    retour = true;
                    break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    // ========== MENU ABONNEMENTS ==========
    private static void menuAbonnements() throws SQLException {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║      GESTION DES ABONNEMENTS           ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. Ajouter un abonnement              ║");
            System.out.println("║ 2. Afficher tous les abonnements      ║");
            System.out.println("║ 3. Rechercher un abonnement           ║");
            System.out.println("║ 4. Abonnements par utilisateur        ║");
            System.out.println("║ 5. Modifier un abonnement             ║");
            System.out.println("║ 6. Supprimer un abonnement            ║");
            System.out.println("║ 0. Retour au menu principal           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();
            switch (choix) {
                case 1:
                    ajouterAbonnement();
                    break;
                case 2:
                    afficherTousAbonnements();
                    break;
                case 3:
                    rechercherAbonnement();
                    break;
                case 4:
                    afficherAbonnementsParUser();
                    break;
                case 5:
                    modifierAbonnement();
                    break;
                case 6:
                    supprimerAbonnement();
                    break;
                case 0:
                    retour = true;
                    break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    // ========== MENU TACHES ==========
    private static void menuTaches() throws SQLException {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        GESTION DES TÂCHES              ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. Ajouter une tâche                  ║");
            System.out.println("║ 2. Afficher toutes les tâches         ║");
            System.out.println("║ 3. Rechercher une tâche               ║");
            System.out.println("║ 4. Modifier une tâche                 ║");
            System.out.println("║ 5. Supprimer une tâche                ║");
            System.out.println("║ 0. Retour au menu principal           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();
            switch (choix) {
                case 1:
                    ajouterTache();
                    break;
                case 2:
                    afficherToutestaches();
                    break;
                case 3:
                    rechercherTache();
                    break;
                case 4:
                    modifierTache();
                    break;
                case 5:
                    supprimerTache();
                    break;
                case 0:
                    retour = true;
                    break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    // ========== MENU AFFECTATIONS ==========
    private static void menuAffectations() throws SQLException {
        boolean retour = false;
        while (!retour) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║      GESTION DES AFFECTATIONS          ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. Ajouter une affectation            ║");
            System.out.println("║ 2. Afficher toutes les affectations   ║");
            System.out.println("║ 3. Rechercher une affectation         ║");
            System.out.println("║ 4. Affectations par utilisateur       ║");
            System.out.println("║ 5. Affectations par tâche             ║");
            System.out.println("║ 6. Modifier une affectation           ║");
            System.out.println("║ 7. Supprimer une affectation          ║");
            System.out.println("║ 0. Retour au menu principal           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();
            switch (choix) {
                case 1:
                    ajouterAffectation();
                    break;
                case 2:
                    afficherToutesAffectations();
                    break;
                case 3:
                    rechercherAffectation();
                    break;
                case 4:
                    afficherAffectationsParUser();
                    break;
                case 5:
                    afficherAffectationsParTache();
                    break;
                case 6:
                    modifierAffectation();
                    break;
                case 7:
                    supprimerAffectation();
                    break;
                case 0:
                    retour = true;
                    break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    // ========== FONCTIONS PERSONNES ==========
    private static void ajouterPersonne() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║   AJOUTER UNE NOUVELLE PERSONNE   ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.println("Choisissez le type de personne :");
        System.out.println("1. Utilisateur");
        System.out.println("2. Employé");
        System.out.println("3. Administrateur");
        System.out.print("Votre choix : ");
        int typePersonne = lireEntier();

        Personne p;
        switch (typePersonne) {
            case 1:
                p = new Utilisateur();
                break;
            case 2:
                p = new Employe();
                break;
            case 3:
                p = new Admin();
                break;
            default:
                System.out.println("❌ Choix invalide. Utilisateur créé par défaut.");
                p = new Utilisateur();
        }

        System.out.print("CIN (8 chiffres) : ");
        p.setCin(lireEntier());

        System.out.print("Nom : ");
        p.setNom(scanner.nextLine());

        System.out.print("Prénom : ");
        p.setPrenom(scanner.nextLine());

        System.out.print("Téléphone : ");
        p.setTel(scanner.nextLine());

        p.setDate_naiss(lireDate("Date de naissance (DD/MM/YYYY ou YYYY-MM-DD) : "));

        System.out.print("Email : ");
        p.setEmail(scanner.nextLine());

        System.out.print("Mot de passe : ");
        p.setMdp(scanner.nextLine());

        System.out.print("Adresse : ");
        p.setAdresse(scanner.nextLine());

        System.out.print("Ville : ");
        p.setVille(scanner.nextLine());

        String dateActuelle = LocalDate.now().toString();
        p.setDate_creationcpt(dateActuelle);
        p.setDate_dernierchg(dateActuelle);

        personneService.ajouter(p);
        System.out.println("\n✓ " + p.getRoleNom() + " ajouté(e) avec succès !");
    }

    private static void afficherToutesPersonnes() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║    LISTE DE TOUTES LES PERSONNES  ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        List<Personne> personnes = personneService.recuperer();

        if (personnes.isEmpty()) {
            System.out.println("📭 Aucune personne trouvée.");
        } else {
            System.out.println(String.format("%-10s %-15s %-15s %-25s %-12s %-15s %-15s",
                    "CIN", "Nom", "Prénom", "Email", "Téléphone", "Ville", "Rôle"));
            System.out.println("─".repeat(110));

            for (Personne p : personnes) {
                System.out.println(String.format("%-10d %-15s %-15s %-25s %-12s %-15s %-15s",
                        p.getCin(), p.getNom(), p.getPrenom(),
                        p.getEmail(), p.getTel(), p.getVille(), p.getRoleNom()));
            }
            System.out.println("\n📊 Total : " + personnes.size() + " personne(s)");
        }
    }

    private static void afficherParRole() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║      AFFICHER PAR RÔLE             ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.println("Choisissez le rôle :");
        System.out.println("1. Utilisateurs");
        System.out.println("2. Employés");
        System.out.println("3. Administrateurs");
        System.out.print("Votre choix : ");
        int choix = lireEntier();

        List<? extends Personne> personnes;
        String roleNom;

        switch (choix) {
            case 1:
                personnes = personneService.getUtilisateurs();
                roleNom = "Utilisateurs";
                break;
            case 2:
                personnes = personneService.getEmployes();
                roleNom = "Employés";
                break;
            case 3:
                personnes = personneService.getAdmins();
                roleNom = "Administrateurs";
                break;
            default:
                System.out.println("❌ Choix invalide.");
                return;
        }

        System.out.println("\n═══ Liste des " + roleNom + " ═══\n");

        if (personnes.isEmpty()) {
            System.out.println("📭 Aucun(e) " + roleNom.toLowerCase() + " trouvé(e).");
        } else {
            System.out.println(String.format("%-10s %-15s %-15s %-25s %-12s %-15s",
                    "CIN", "Nom", "Prénom", "Email", "Téléphone", "Ville"));
            System.out.println("─".repeat(95));

            for (Personne p : personnes) {
                System.out.println(String.format("%-10d %-15s %-15s %-25s %-12s %-15s",
                        p.getCin(), p.getNom(), p.getPrenom(),
                        p.getEmail(), p.getTel(), p.getVille()));
            }
            System.out.println("\n📊 Total : " + personnes.size() + " " + roleNom.toLowerCase());
        }
    }

    private static void rechercherPersonne() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║      RECHERCHER UNE PERSONNE      ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez le CIN : ");
        int cin = lireEntier();

        Personne trouve = personneService.rechercherParId(cin);

        if (trouve != null) {
            System.out.println("\n✓ Personne trouvée :");
            afficherDetailsPersonne(trouve);
        } else {
            System.out.println("\n✗ Aucune personne trouvée avec le CIN " + cin);
        }
    }

    private static void modifierPersonne() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║       MODIFIER UNE PERSONNE       ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez le CIN de la personne à modifier : ");
        int cin = lireEntier();

        Personne existante = personneService.rechercherParId(cin);

        if (existante == null) {
            System.out.println("\n✗ Aucune personne trouvée avec le CIN " + cin);
            return;
        }

        System.out.println("\nInformations actuelles :");
        afficherDetailsPersonne(existante);

        System.out.println("\n📝 Voulez-vous changer le rôle ? (o/n) : ");
        String changerRole = scanner.nextLine().toLowerCase();

        Personne p;
        if (changerRole.equals("o") || changerRole.equals("oui")) {
            System.out.println("Nouveau rôle :");
            System.out.println("1. Utilisateur");
            System.out.println("2. Employé");
            System.out.println("3. Administrateur");
            System.out.print("Votre choix : ");
            int typePersonne = lireEntier();

            switch (typePersonne) {
                case 1:
                    p = new Utilisateur();
                    break;
                case 2:
                    p = new Employe();
                    break;
                case 3:
                    p = new Admin();
                    break;
                default:
                    p = existante;
            }
        } else {
            if (existante instanceof Utilisateur) {
                p = new Utilisateur();
            } else if (existante instanceof Employe) {
                p = new Employe();
            } else {
                p = new Admin();
            }
        }

        p.setCin(cin);

        System.out.println("\nEntrez les nouvelles informations (Entrée pour conserver) :");

        System.out.print("Nouveau nom [" + existante.getNom() + "] : ");
        String input = scanner.nextLine();
        p.setNom(input.isEmpty() ? existante.getNom() : input);

        System.out.print("Nouveau prénom [" + existante.getPrenom() + "] : ");
        input = scanner.nextLine();
        p.setPrenom(input.isEmpty() ? existante.getPrenom() : input);

        System.out.print("Nouveau téléphone [" + existante.getTel() + "] : ");
        input = scanner.nextLine();
        p.setTel(input.isEmpty() ? existante.getTel() : input);

        System.out.print("Nouvelle date de naissance [" + existante.getDate_naiss() + "] (Entrée pour conserver) : ");
        input = scanner.nextLine();
        if (input.isEmpty()) {
            p.setDate_naiss(existante.getDate_naiss());
        } else {
            p.setDate_naiss(convertirDate(input, existante.getDate_naiss()));
        }

        System.out.print("Nouvel email [" + existante.getEmail() + "] : ");
        input = scanner.nextLine();
        p.setEmail(input.isEmpty() ? existante.getEmail() : input);

        System.out.print("Nouveau mot de passe (Entrée pour ne pas changer) : ");
        input = scanner.nextLine();
        p.setMdp(input.isEmpty() ? existante.getMdp() : input);

        System.out.print("Nouvelle adresse [" + existante.getAdresse() + "] : ");
        input = scanner.nextLine();
        p.setAdresse(input.isEmpty() ? existante.getAdresse() : input);

        System.out.print("Nouvelle ville [" + existante.getVille() + "] : ");
        input = scanner.nextLine();
        p.setVille(input.isEmpty() ? existante.getVille() : input);

        p.setDate_creationcpt(existante.getDate_creationcpt());
        p.setDate_dernierchg(LocalDate.now().toString());

        personneService.modifier(p);
        System.out.println("\n✓ Personne modifiée avec succès !");
    }

    private static void supprimerPersonne() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║      SUPPRIMER UNE PERSONNE       ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez le CIN de la personne à supprimer : ");
        int cin = lireEntier();

        System.out.print("⚠️  Êtes-vous sûr de vouloir supprimer cette personne ? (o/n) : ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("o") || confirmation.equals("oui")) {
            personneService.supprimer(cin);
            System.out.println("\n✓ Personne supprimée avec succès !");
        } else {
            System.out.println("\n❌ Suppression annulée.");
        }
    }

    private static void afficherDetailsPersonne(Personne p) {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│ CIN            : " + p.getCin());
        System.out.println("│ Nom            : " + p.getNom());
        System.out.println("│ Prénom         : " + p.getPrenom());
        System.out.println("│ Email          : " + p.getEmail());
        System.out.println("│ Téléphone      : " + p.getTel());
        System.out.println("│ Date naissance : " + p.getDate_naiss());
        System.out.println("│ Adresse        : " + p.getAdresse());
        System.out.println("│ Ville          : " + p.getVille());
        System.out.println("│ Rôle           : " + p.getRoleNom());
        System.out.println("│ Créé le        : " + p.getDate_creationcpt());
        System.out.println("│ Modifié le     : " + p.getDate_dernierchg());
        System.out.println("└─────────────────────────────────────┘");
    }

    // ========== FONCTIONS OFFRES ==========
    private static void ajouterOffre() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     AJOUTER UNE NOUVELLE OFFRE    ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        offres offre = new offres();

        System.out.print("Nom de l'offre : ");
        offre.setNom_offre(scanner.nextLine());

        System.out.print("Description : ");
        offre.setDescription(scanner.nextLine());

        System.out.print("Prix (en DT) : ");
        offre.setPrix(lireFloat());

        System.out.print("Durée de l'offre (en jours) : ");
        offre.setDuree_offre(lireEntier());

        offreService.ajouter(offre);
        System.out.println("\n✓ Offre ajoutée avec succès !");
    }

    private static void afficherToutesOffres() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     LISTE DE TOUTES LES OFFRES    ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        List<offres> offres = offreService.recuperer();

        if (offres.isEmpty()) {
            System.out.println("📭 Aucune offre trouvée.");
        } else {
            System.out.println(String.format("%-5s %-25s %-40s %-10s %-10s",
                    "ID", "Nom", "Description", "Prix (DT)", "Durée"));
            System.out.println("─".repeat(95));

            for (offres o : offres) {
                String desc = o.getDescription().length() > 37
                        ? o.getDescription().substring(0, 37) + "..."
                        : o.getDescription();
                System.out.println(String.format("%-5d %-25s %-40s %-10.2f %-10d",
                        o.getId_offres(), o.getNom_offre(), desc,
                        o.getPrix(), o.getDuree_offre()));
            }
            System.out.println("\n📊 Total : " + offres.size() + " offre(s)");
        }
    }

    private static void rechercherOffre() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║       RECHERCHER UNE OFFRE        ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'offre : ");
        int id = lireEntier();

        offres offre = offreService.rechercherParId(id);

        if (offre != null) {
            System.out.println("\n✓ Offre trouvée :");
            //afficherDetailsOffre(offre);
        } else {
            System.out.println("\n✗ Aucune offre trouvée avec l'ID " + id);
        }
    }

    private static void modifierOffre() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║        MODIFIER UNE OFFRE         ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'offre à modifier : ");
        int id = lireEntier();

        offres existante = offreService.rechercherParId(id);

        if (existante == null) {
            System.out.println("\n✗ Aucune offre trouvée avec l'ID " + id);
            return;
        }

        System.out.println("\nInformations actuelles :");
       // afficherDetailsOffre(existante);

        offres offre = new offres();
        offre.setId_offres(id);

        System.out.println("\n📝 Entrez les nouvelles informations (Entrée pour conserver) :");

        System.out.print("Nouveau nom [" + existante.getNom_offre() + "] : ");
        String input = scanner.nextLine();
        offre.setNom_offre(input.isEmpty() ? existante.getNom_offre() : input);

        System.out.print("Nouvelle description [" + existante.getDescription() + "] : ");
        input = scanner.nextLine();
        offre.setDescription(input.isEmpty() ? existante.getDescription() : input);

        System.out.print("Nouveau prix [" + existante.getPrix() + "] : ");
        input = scanner.nextLine();
        offre.setPrix(input.isEmpty() ? existante.getPrix() : Float.parseFloat(input));

        System.out.print("Nouvelle durée [" + existante.getDuree_offre() + "] : ");
        input = scanner.nextLine();
        offre.setDuree_offre(input.isEmpty() ? existante.getDuree_offre() : Integer.parseInt(input));

        offreService.modifier(offre);
        System.out.println("\n✓ Offre modifiée avec succès !");
    }

    private static void supprimerOffre() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║       SUPPRIMER UNE OFFRE         ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'offre à supprimer : ");
        int id = lireEntier();

        System.out.print("⚠️  Êtes-vous sûr de vouloir supprimer cette offre ? (o/n) : ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("o") || confirmation.equals("oui")) {
            offreService.supprimer(id);
            System.out.println("\n✓ Offre supprimée avec succès !");
        } else {
            System.out.println("\n❌ Suppression annulée.");
        }
    }

    private static void afficherDetailsOffre(offres o) {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│ ID             : " + o.getId_offres());
        System.out.println("│ Nom            : " + o.getNom_offre());
        System.out.println("│ Description    : " + o.getDescription());
        System.out.println("│ Prix           : " + o.getPrix() + " DT");
        System.out.println("│ Durée          : " + o.getDuree_offre() + " jours");
        System.out.println("└─────────────────────────────────────┘");
    }

    // ========== FONCTIONS ABONNEMENTS ==========
    private static void ajouterAbonnement() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║   AJOUTER UN NOUVEL ABONNEMENT    ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        Abonnements abonnement = new Abonnements();

        System.out.print("CIN de l'utilisateur : ");
        abonnement.setCin(lireEntier());

        // Vérifier si l'utilisateur existe
        Personne personne = personneService.rechercherParId(abonnement.getCin());
        if (personne == null) {
            System.out.println("❌ Aucun utilisateur trouvé avec ce CIN.");
            return;
        }

        // Afficher les offres2
        // disponibles
        System.out.println("\n📋 Offres disponibles :");
        afficherToutesOffres();

        System.out.print("\nID de l'offre choisie : ");
        abonnement.setId_offre(lireEntier());

        // Vérifier si l'offre existe
        offres offre = offreService.rechercherParId(abonnement.getId_offre());
        if (offre == null) {
            System.out.println("❌ Aucune offre trouvée avec cet ID.");
            return;
        }

        // Dates automatiques
        LocalDate dateInscription = LocalDate.now();
        LocalDate dateExpiration = dateInscription.plusDays(offre.getDuree_offre());

        abonnement.setDate_inscription(dateInscription.toString());
        abonnement.setDate_expiration(dateExpiration.toString());

        System.out.println("\nChoisissez la situation :");
        System.out.println("1. Actif");
        System.out.println("2. Expiré");
        System.out.println("3. Suspendu");
        System.out.print("Votre choix : ");
        int choixSituation = lireEntier();

        String situation;
        switch (choixSituation) {
            case 1:
                situation = "Actif";
                break;
            case 2:
                situation = "Expiré";
                break;
            case 3:
                situation = "Suspendu";
                break;
            default:
                situation = "Actif";
        }
        abonnement.setSituation(situation);

        abonnementService.ajouter(abonnement);
        System.out.println("\n✓ Abonnement ajouté avec succès !");
        System.out.println("📅 Date d'inscription : " + dateInscription);
        System.out.println("📅 Date d'expiration  : " + dateExpiration);
    }

    private static void afficherTousAbonnements() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║  LISTE DE TOUS LES ABONNEMENTS    ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        List<Abonnements> abonnements = abonnementService.recuperer();

        if (abonnements.isEmpty()) {
            System.out.println("📭 Aucun abonnement trouvé.");
        } else {
            System.out.println(String.format("%-5s %-10s %-10s %-15s %-15s %-12s",
                    "ID", "CIN", "ID Offre", "Inscription", "Expiration", "Situation"));
            System.out.println("─".repeat(75));

            for (Abonnements a : abonnements) {
                System.out.println(String.format("%-5d %-10d %-10d %-15s %-15s %-12s",
                        a.getId_abonn(), a.getCin(), a.getId_offre(),
                        a.getDate_inscription(), a.getDate_expiration(), a.getSituation()));
            }
            System.out.println("\n📊 Total : " + abonnements.size() + " abonnement(s)");
        }
    }

    private static void rechercherAbonnement() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     RECHERCHER UN ABONNEMENT      ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'abonnement : ");
        int id = lireEntier();

        Abonnements abonnement = abonnementService.rechercherParId(id);

        if (abonnement != null) {
            System.out.println("\n✓ Abonnement trouvé :");
            //afficherDetailsAbonnement(abonnement);
        } else {
            System.out.println("\n✗ Aucun abonnement trouvé avec l'ID " + id);
        }
    }

    private static void afficherAbonnementsParUser() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║  ABONNEMENTS PAR UTILISATEUR      ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez le CIN de l'utilisateur : ");
        int cin = lireEntier();

        List<Abonnements> abonnements = abonnementService.getAbonnementsByUser(cin);

        if (abonnements.isEmpty()) {
            System.out.println("📭 Aucun abonnement trouvé pour cet utilisateur.");
        } else {
            System.out.println(String.format("%-5s %-10s %-15s %-15s %-12s",
                    "ID", "ID Offre", "Inscription", "Expiration", "Situation"));
            System.out.println("─".repeat(65));

            for (Abonnements a : abonnements) {
                System.out.println(String.format("%-5d %-10d %-15s %-15s %-12s",
                        a.getId_abonn(), a.getId_offre(),
                        a.getDate_inscription(), a.getDate_expiration(), a.getSituation()));
            }
            System.out.println("\n📊 Total : " + abonnements.size() + " abonnement(s)");
        }
    }

    private static void modifierAbonnement() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║      MODIFIER UN ABONNEMENT       ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'abonnement à modifier : ");
        int id = lireEntier();

        Abonnements existant = abonnementService.rechercherParId(id);

        if (existant == null) {
            System.out.println("\n✗ Aucun abonnement trouvé avec l'ID " + id);
            return;
        }

        System.out.println("\nInformations actuelles :");
        afficherDetailsAbonnement(existant);

        Abonnements abonnement = new Abonnements();
        abonnement.setId_abonn(id);

        System.out.println("\n📝 Entrez les nouvelles informations (Entrée pour conserver) :");

        System.out.print("Nouveau CIN [" + existant.getCin() + "] : ");
        String input = scanner.nextLine();
        abonnement.setCin(input.isEmpty() ? existant.getCin() : Integer.parseInt(input));

        System.out.print("Nouvel ID offre [" + existant.getId_offre() + "] : ");
        input = scanner.nextLine();
        abonnement.setId_offre(input.isEmpty() ? existant.getId_offre() : Integer.parseInt(input));

        System.out.print("Nouvelle date d'inscription [" + existant.getDate_inscription() + "] : ");
        input = scanner.nextLine();
        if (input.isEmpty()) {
            abonnement.setDate_inscription(existant.getDate_inscription());
        } else {
            abonnement.setDate_inscription(convertirDate(input, existant.getDate_inscription()));
        }

        System.out.print("Nouvelle date d'expiration [" + existant.getDate_expiration() + "] : ");
        input = scanner.nextLine();
        if (input.isEmpty()) {
            abonnement.setDate_expiration(existant.getDate_expiration());
        } else {
            abonnement.setDate_expiration(convertirDate(input, existant.getDate_expiration()));
        }

        System.out.println("\nNouvelle situation [" + existant.getSituation() + "] :");
        System.out.println("1. Actif");
        System.out.println("2. Expiré");
        System.out.println("3. Suspendu");
        System.out.print("Votre choix (Entrée pour conserver) : ");
        input = scanner.nextLine();

        if (input.isEmpty()) {
            abonnement.setSituation(existant.getSituation());
        } else {
            int choixSituation = Integer.parseInt(input);
            String situation;
            switch (choixSituation) {
                case 1:
                    situation = "Actif";
                    break;
                case 2:
                    situation = "Expiré";
                    break;
                case 3:
                    situation = "Suspendu";
                    break;
                default:
                    situation = existant.getSituation();
            }
            abonnement.setSituation(situation);
        }

        abonnementService.modifier(abonnement);
        System.out.println("\n✓ Abonnement modifié avec succès !");
    }

    private static void supprimerAbonnement() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     SUPPRIMER UN ABONNEMENT       ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'abonnement à supprimer : ");
        int id = lireEntier();

        System.out.print("⚠️  Êtes-vous sûr de vouloir supprimer cet abonnement ? (o/n) : ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("o") || confirmation.equals("oui")) {
            abonnementService.supprimer(id);
            System.out.println("\n✓ Abonnement supprimé avec succès !");
        } else {
            System.out.println("\n❌ Suppression annulée.");
        }
    }

    private static void afficherDetailsAbonnement(Abonnements a) {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│ ID              : " + a.getId_abonn());
        System.out.println("│ CIN             : " + a.getCin());
        System.out.println("│ ID Offre        : " + a.getId_offre());
        System.out.println("│ Date inscription: " + a.getDate_inscription());
        System.out.println("│ Date expiration : " + a.getDate_expiration());
        System.out.println("│ Situation       : " + a.getSituation());
        System.out.println("└─────────────────────────────────────┘");
    }

    // ========== FONCTIONS TACHES ==========
    private static void ajouterTache() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║    AJOUTER UNE NOUVELLE TÂCHE     ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        Tache tache = new Tache();

        System.out.print("Nom de la tâche : ");
        tache.setNom_tache(scanner.nextLine());

        System.out.print("Description : ");
        tache.setDescription(scanner.nextLine());

        tacheService.ajouter(tache);
        System.out.println("\n✓ Tâche ajoutée avec succès !");
    }

    private static void afficherToutestaches() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║    LISTE DE TOUTES LES TÂCHES     ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        List<Tache> taches = tacheService.recuperer();

        if (taches.isEmpty()) {
            System.out.println("📭 Aucune tâche trouvée.");
        } else {
            System.out.println(String.format("%-5s %-30s %-50s",
                    "ID", "Nom", "Description"));
            System.out.println("─".repeat(90));

            for (Tache t : taches) {
                System.out.println(String.format("%-5d %-30s %-50s",  // ✅ %s au lieu de %d
                        t.getId_tache(), t.getNom_tache(), t.getDescription()));
            }
            System.out.println("\n📊 Total : " + taches.size() + " tâche(s)");
        }
    }

    private static void rechercherTache() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║       RECHERCHER UNE TÂCHE        ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de la tâche : ");
        int id = lireEntier();

        Tache tache = tacheService.rechercherParId(id);

        if (tache != null) {
            System.out.println("\n✓ Tâche trouvée :");
            afficherDetailsTache(tache);
        } else {
            System.out.println("\n✗ Aucune tâche trouvée avec l'ID " + id);
        }
    }

    private static void modifierTache() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║        MODIFIER UNE TÂCHE         ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de la tâche à modifier : ");
        int id = lireEntier();

        Tache existante = tacheService.rechercherParId(id);

        if (existante == null) {
            System.out.println("\n✗ Aucune tâche trouvée avec l'ID " + id);
            return;
        }

        System.out.println("\nInformations actuelles :");
        afficherDetailsTache(existante);

        Tache tache = new Tache();
        tache.setId_tache(id);

        System.out.println("\n📝 Entrez les nouvelles informations (Entrée pour conserver) :");

        System.out.print("Nouveau nom [" + existante.getNom_tache() + "] : ");
        String input = scanner.nextLine();
        tache.setNom_tache(input.isEmpty() ? existante.getNom_tache() : input );

        System.out.print("Nouvelle description [" + existante.getDescription() + "] : ");
        input = scanner.nextLine();
        tache.setDescription(input.isEmpty() ? existante.getDescription() : input );

        tacheService.modifier(tache);
        System.out.println("\n✓ Tâche modifiée avec succès !");
    }

    private static void supprimerTache() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║       SUPPRIMER UNE TÂCHE         ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de la tâche à supprimer : ");
        int id = lireEntier();

        System.out.print("⚠️  Êtes-vous sûr de vouloir supprimer cette tâche ? (o/n) : ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("o") || confirmation.equals("oui")) {
            tacheService.supprimer(id);
            System.out.println("\n✓ Tâche supprimée avec succès !");
        } else {
            System.out.println("\n❌ Suppression annulée.");
        }
    }

    private static void afficherDetailsTache(Tache t) {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│ ID             : " + t.getId_tache());
        System.out.println("│ Nom            : " + t.getNom_tache());
        System.out.println("│ Description    : " + t.getDescription());
        System.out.println("└─────────────────────────────────────┘");
    }

    // ========== FONCTIONS AFFECTATIONS ==========
    private static void ajouterAffectation() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║  AJOUTER UNE NOUVELLE AFFECTATION ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        Affectation affectation = new Affectation();

        System.out.print("CIN de la personne : ");
        affectation.setCin(lireEntier());

        // Vérifier si la personne existe
        Personne personne = personneService.rechercherParId(affectation.getCin());
        if (personne == null) {
            System.out.println("❌ Aucune personne trouvée avec ce CIN.");
            return;
        }

        // Afficher les tâches disponibles
        System.out.println("\n📋 Tâches disponibles :");
        afficherToutestaches();

        System.out.print("\nID de la tâche : ");
        affectation.setId_tache(lireEntier());

        // Vérifier si la tâche existe
        Tache tache = tacheService.rechercherParId(affectation.getId_tache());
        if (tache == null) {
            System.out.println("❌ Aucune tâche trouvée avec cet ID.");
            return;
        }

        affectationService.ajouter(affectation);
        System.out.println("\n✓ Affectation ajoutée avec succès !");
    }

    private static void afficherToutesAffectations() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║ LISTE DE TOUTES LES AFFECTATIONS  ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        List<Affectation> affectations = affectationService.recuperer();

        if (affectations.isEmpty()) {
            System.out.println("📭 Aucune affectation trouvée.");
        } else {
            System.out.println(String.format("%-5s %-10s %-10s",
                    "ID", "CIN", "ID Tâche"));
            System.out.println("─".repeat(30));

            for (Affectation a : affectations) {
                System.out.println(String.format("%-5d %-10d %-10d",
                        a.getId_affect(), a.getCin(), a.getId_tache()));
            }
            System.out.println("\n📊 Total : " + affectations.size() + " affectation(s)");
        }
    }

    private static void rechercherAffectation() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║    RECHERCHER UNE AFFECTATION     ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'affectation : ");
        int id = lireEntier();

        Affectation affectation = affectationService.rechercherParId(id);

        if (affectation != null) {
            System.out.println("\n✓ Affectation trouvée :");
            afficherDetailsAffectation(affectation);
        } else {
            System.out.println("\n✗ Aucune affectation trouvée avec l'ID " + id);
        }
    }

    private static void afficherAffectationsParUser() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║  AFFECTATIONS PAR UTILISATEUR     ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez le CIN de l'utilisateur : ");
        int cin = lireEntier();

        List<Affectation> affectations = affectationService.getAffectationsByUser(cin);

        if (affectations.isEmpty()) {
            System.out.println("📭 Aucune affectation trouvée pour cet utilisateur.");
        } else {
            System.out.println(String.format("%-5s %-10s",
                    "ID", "ID Tâche"));
            System.out.println("─".repeat(20));

            for (Affectation a : affectations) {
                System.out.println(String.format("%-5d %-10d",
                        a.getId_affect(), a.getId_tache()));
            }
            System.out.println("\n📊 Total : " + affectations.size() + " affectation(s)");
        }
    }

    private static void afficherAffectationsParTache() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     AFFECTATIONS PAR TÂCHE        ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de la tâche : ");
        int idTache = lireEntier();

        List<Affectation> affectations = affectationService.getAffectationsByTache(idTache);

        if (affectations.isEmpty()) {
            System.out.println("📭 Aucune affectation trouvée pour cette tâche.");
        } else {
            System.out.println(String.format("%-5s %-10s",
                    "ID", "CIN"));
            System.out.println("─".repeat(20));

            for (Affectation a : affectations) {
                System.out.println(String.format("%-5d %-10d",
                        a.getId_affect(), a.getCin()));
            }
            System.out.println("\n📊 Total : " + affectations.size() + " affectation(s)");
        }
    }

    private static void modifierAffectation() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║     MODIFIER UNE AFFECTATION      ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'affectation à modifier : ");
        int id = lireEntier();

        Affectation existante = affectationService.rechercherParId(id);

        if (existante == null) {
            System.out.println("\n✗ Aucune affectation trouvée avec l'ID " + id);
            return;
        }

        System.out.println("\nInformations actuelles :");
        afficherDetailsAffectation(existante);

        Affectation affectation = new Affectation();
        affectation.setId_affect(id);

        System.out.println("\n📝 Entrez les nouvelles informations (Entrée pour conserver) :");

        System.out.print("Nouveau CIN [" + existante.getCin() + "] : ");
        String input = scanner.nextLine();
        affectation.setCin(input.isEmpty() ? existante.getCin() : Integer.parseInt(input));

        System.out.print("Nouvel ID tâche [" + existante.getId_tache() + "] : ");
        input = scanner.nextLine();
        affectation.setId_tache(input.isEmpty() ? existante.getId_tache() : Integer.parseInt(input));

        affectationService.modifier(affectation);
        System.out.println("\n✓ Affectation modifiée avec succès !");
    }

    private static void supprimerAffectation() throws SQLException {
        System.out.println("\n╔═══════════════════════════════════╗");
        System.out.println("║    SUPPRIMER UNE AFFECTATION      ║");
        System.out.println("╚═══════════════════════════════════╝\n");

        System.out.print("Entrez l'ID de l'affectation à supprimer : ");
        int id = lireEntier();

        System.out.print("⚠️  Êtes-vous sûr de vouloir supprimer cette affectation ? (o/n) : ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("o") || confirmation.equals("oui")) {
            affectationService.supprimer(id);
            System.out.println("\n✓ Affectation supprimée avec succès !");
        } else {
            System.out.println("\n❌ Suppression annulée.");
        }
    }

    private static void afficherDetailsAffectation(Affectation a) {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│ ID             : " + a.getId_affect());
        System.out.println("│ CIN            : " + a.getCin());
        System.out.println("│ ID Tâche       : " + a.getId_tache());
        System.out.println("└─────────────────────────────────────┘");
    }

    // ========== FONCTIONS UTILITAIRES ==========
    private static int lireChoix() {
        try {
            String input = scanner.nextLine().trim();
            return input.isEmpty() ? -1 : Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int lireEntier() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("❌ Veuillez entrer un nombre valide : ");
            }
        }
    }

    private static float lireFloat() {
        while (true) {
            try {
                return Float.parseFloat(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("❌ Veuillez entrer un nombre décimal valide : ");
            }
        }
    }

    private static String lireDate(String message) {
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };

        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDate date = LocalDate.parse(input, formatter);
                    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (DateTimeParseException e) {
                    // Essayer le prochain format
                }
            }

            System.out.println("❌ Format de date invalide. Utilisez : DD/MM/YYYY ou YYYY-MM-DD");
        }
    }

    private static String convertirDate(String input, String dateParDefaut) {
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(input, formatter);
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                // Essayer le prochain format
            }
        }

        System.out.println("⚠️ Format invalide, date conservée.");
        return dateParDefaut;
    }
}*/