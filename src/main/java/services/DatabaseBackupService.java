package services;

import java.io.File;
import java.io.IOException;

/**
 * Service pour la sauvegarde et la restauration de la base de données MySQL.
 * Adapté pour une utilisation avec Laragon sur Windows.
 */
public class DatabaseBackupService {

    private final String dbName = "agro";
    private final String dbUser = "root";
    private final String dbPass = "";
    
    // Chemins vers les exécutables MySQL de Laragon
    private final String mysqlDumpPath = "C:\\laragon\\bin\\mysql\\mysql-8.4.3-winx64\\bin\\mysqldump.exe";
    private final String mysqlPath = "C:\\laragon\\bin\\mysql\\mysql-8.4.3-winx64\\bin\\mysql.exe";

    /**
     * Effectue une sauvegarde de la base de données dans le fichier spécifié.
     * @param filePath Chemin complet du fichier .sql de destination
     * @return true si la sauvegarde a réussi
     */
    public boolean backup(String filePath) {
        try {
            // Commande pour mysqldump
            // On utilise --result-file pour éviter les problèmes de redirection de flux sur Windows
            ProcessBuilder pb;
            
            if (dbPass.isEmpty()) {
                pb = new ProcessBuilder(
                        mysqlDumpPath,
                        "-u" + dbUser,
                        "--databases", dbName,
                        "--result-file=" + filePath
                );
            } else {
                pb = new ProcessBuilder(
                        mysqlDumpPath,
                        "-u" + dbUser,
                        "-p" + dbPass,
                        "--databases", dbName,
                        "--result-file=" + filePath
                );
            }

            Process process = pb.start();
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("✅ Sauvegarde réussie : " + filePath);
                return true;
            } else {
                System.err.println("❌ Échec de la sauvegarde. Code de sortie : " + processComplete);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Erreur lors de la sauvegarde : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restaure la base de données à partir d'un fichier SQL.
     * @param filePath Chemin du fichier .sql à importer
     * @return true si la restauration a réussi
     */
    public boolean restore(String filePath) {
        try {
            ProcessBuilder pb;
            // Commande : mysql -u root -p agro < file.sql
            // En Java, on utilise le flag -e "source path/to/file.sql"
            if (dbPass.isEmpty()) {
                pb = new ProcessBuilder(
                        mysqlPath,
                        "-u" + dbUser,
                        dbName,
                        "-e", "source " + filePath.replace("\\", "/")
                );
            } else {
                pb = new ProcessBuilder(
                        mysqlPath,
                        "-u" + dbUser,
                        "-p" + dbPass,
                        dbName,
                        "-e", "source " + filePath.replace("\\", "/")
                );
            }

            Process process = pb.start();
            int processComplete = process.waitFor();

            if (processComplete == 0) {
                System.out.println("✅ Restauration réussie depuis : " + filePath);
                return true;
            } else {
                System.err.println("❌ Échec de la restauration. Code de sortie : " + processComplete);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Erreur lors de la restauration : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
