package Test;

import entities.Machine;
import entities.Maintenance;
import entities.Achat;
import services.MachineService;
import services.MaintenanceService;
import services.AchatService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        MachineService machineService = new MachineService();
        MaintenanceService maintenanceService = new MaintenanceService();
        AchatService achatService = new AchatService();

        int idMachine = 0;
        int cinClient = 12345678; // Exemple CIN client

        try {
            // =====================================================
            // 1️⃣ AJOUTER UNE MACHINE
            // =====================================================
            Machine machineAdd = new Machine(
                    0,
                    "John Deere",
                    "X100",
                    "Neuf",
                    "SN-100",
                    LocalDate.of(2026, 2, 1),
                    "Tracteur"
            );
            machineService.ajouter(machineAdd);
            System.out.println("✅ Machine ajoutée");

            // =====================================================
            // 2️⃣ AFFICHER MACHINES
            // =====================================================
            List<Machine> machines = machineService.recuperer();
            if (machines.isEmpty()) {
                System.out.println("❌ Aucune machine trouvée");
                return;
            }

            System.out.println("\n📋 Liste des machines :");
            for (Machine m : machines) {
                System.out.println(
                        "ID: " + m.getIdM() +
                                ", Marque: " + m.getMarque() +
                                ", Modèle: " + m.getModele() +
                                ", Nom: " + m.getNom()
                );
            }

            // =====================================================
            // 3️⃣ MACHINE UTILISÉE
            // =====================================================
            Machine machineBase = machines.get(0);
            idMachine = machineBase.getIdM();
            System.out.println("\n✅ Machine utilisée (ID) : " + idMachine);

            // =====================================================
            // 4️⃣ MODIFIER MACHINE
            // =====================================================
            Machine machineModif = new Machine(
                    idMachine,
                    "Kubota",
                    "K200",
                    "Occasion",
                    machineBase.getNumeroSerie(),
                    machineBase.getDateAchat(),
                    machineBase.getNom()
            );
            machineService.modifier(machineModif);
            System.out.println("✏️ Machine modifiée");

            // =====================================================
            // 5️⃣ AJOUTER 3 MAINTENANCES
            // =====================================================
            Maintenance m1 = new Maintenance(
                    0, "Moteur", 300,
                    LocalDate.of(2026, 2, 8),
                    "Vidange moteur + filtre",
                    idMachine
            );
            maintenanceService.ajouter(m1);

            Maintenance m2 = new Maintenance(
                    0, "Hydraulique", 450,
                    LocalDate.of(2026, 3, 1),
                    "Remplacement flexible",
                    idMachine
            );
            maintenanceService.ajouter(m2);

            Maintenance m3 = new Maintenance(
                    0, "Électricité", 180,
                    LocalDate.of(2026, 4, 10),
                    "Réparation faisceau",
                    idMachine
            );
            maintenanceService.ajouter(m3);

            System.out.println("✅ 3 maintenances ajoutées");

            // =====================================================
            // 6️⃣ AFFICHER MAINTENANCES
            // =====================================================
            System.out.println("\n📋 Liste des maintenances :");
            afficherMaintenances(maintenanceService);

            // =====================================================
            // 7️⃣ AJOUTER UN ACHAT
            // =====================================================
            int quantiteAchat = 2; // Exemple : 2 machines achetées
            Achat achat1 = new Achat(
                    0,
                    LocalDate.of(2026, 2, 10),
                    idMachine,
                    cinClient,
                    quantiteAchat
            );
            achatService.ajouter(achat1);
            System.out.println("✅ Achat ajouté : " + achat1);

            // =====================================================
            // 8️⃣ AFFICHER TOUS LES ACHATS
            // =====================================================
            List<Achat> achats = achatService.recuperer();
            System.out.println("\n📋 Liste des achats :");
            for (Achat a : achats) {
                System.out.println(
                        "ID Achat: " + a.getIdAchat() +
                                ", Date: " + a.getDateAchat() +
                                ", Machine ID: " + a.getIdM() +
                                ", CIN Client: " + a.getCin() +
                                ", Quantité: " + a.getQuantite()
                );
            }

            // =====================================================
            // 9️⃣ MODIFIER UN ACHAT
            // =====================================================
            Achat achatModif = new Achat(
                    achats.get(0).getIdAchat(),
                    LocalDate.of(2026, 2, 15),
                    idMachine,
                    cinClient,
                    3 // nouvelle quantité
            );
            achatService.modifier(achatModif);
            System.out.println("✏️ Achat modifié : " + achatModif);

            // =====================================================
            // 🔟 SUPPRIMER UN ACHAT (COMMENTÉ)
            // =====================================================
            // int idASupprimer = achats.get(0).getIdAchat();
            // achatService.supprimer(idASupprimer);
            // System.out.println("🗑️ Achat supprimé (ID: " + idASupprimer + ")");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
        }
    }

    // ================= AFFICHER MAINTENANCES =================
    private static void afficherMaintenances(MaintenanceService ms) {
        try {
            List<Maintenance> list = ms.recuperer();
            if (list.isEmpty()) {
                System.out.println("⚠️ Aucune maintenance trouvée");
            }
            for (Maintenance m : list) {
                System.out.println(
                        "ID: " + m.getIdMain() +
                                ", Type: " + m.getTypePanne() +
                                ", Coût: " + m.getCout() +
                                ", Date: " + m.getDateMain() +
                                ", Machine ID: " + m.getIdM()
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération maintenances : " + e.getMessage());
        }
    }
}
