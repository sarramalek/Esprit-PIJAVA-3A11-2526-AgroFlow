package services.User;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import models.User.Personne;
import utils.MyDatabase;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour générer des rapports PDF de l'historique des logs
 */
public class LogReportService {

    private final Connection connection;

    public LogReportService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Classe pour représenter une entrée de log
     */
    public static class LogEntry {
        private final int id;
        private final String loginTime;
        private final String ipAddress;
        private final boolean success;
        private final boolean twoFactorUsed;
        private final String failureReason;

        public LogEntry(int id, String loginTime, String ipAddress, boolean success,
                        boolean twoFactorUsed, String failureReason) {
            this.id = id;
            this.loginTime = loginTime;
            this.ipAddress = ipAddress;
            this.success = success;
            this.twoFactorUsed = twoFactorUsed;
            this.failureReason = failureReason;
        }

        // Getters
        public int getId() { return id; }
        public String getLoginTime() { return loginTime; }
        public String getIpAddress() { return ipAddress; }
        public boolean isSuccess() { return success; }
        public boolean isTwoFactorUsed() { return twoFactorUsed; }
        public String getFailureReason() { return failureReason; }
    }

    /**
     * Récupère l'historique des logs pour un utilisateur
     */
    public List<LogEntry> getLoginHistoryForUser(int userCin) throws SQLException {
        List<LogEntry> logs = new ArrayList<>();

        String query = "SELECT id, login_time, ip_address, success, two_factor_used, failure_reason " +
                "FROM login_history WHERE user_cin = ? ORDER BY login_time DESC LIMIT 100";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userCin);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LogEntry log = new LogEntry(
                            rs.getInt("id"),
                            rs.getString("login_time"),
                            rs.getString("ip_address"),
                            rs.getBoolean("success"),
                            rs.getBoolean("two_factor_used"),
                            rs.getString("failure_reason")
                    );
                    logs.add(log);
                }
            }
        }

        return logs;
    }

    /**
     * Génère un PDF de l'historique des logs
     * @param personne La personne sélectionnée
     * @param outputPath Chemin du fichier PDF à créer
     * @return true si la génération a réussi
     */
    public boolean generateLogHistoryPDF(Personne personne, String outputPath) {
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // ═══════════════════════════════════════════════════════════════
            // EN-TÊTE DU DOCUMENT
            // ═══════════════════════════════════════════════════════════════

            // Logo et titre
            addHeader(document, personne);

            // Ligne de séparation
            addSeparator(document);

            // ═══════════════════════════════════════════════════════════════
            // INFORMATIONS UTILISATEUR
            // ═══════════════════════════════════════════════════════════════

            addUserInfo(document, personne);

            document.add(new Paragraph("\n"));

            // ═══════════════════════════════════════════════════════════════
            // STATISTIQUES
            // ═══════════════════════════════════════════════════════════════

            addStatistics(document, personne.getCin());

            document.add(new Paragraph("\n"));

            // ═══════════════════════════════════════════════════════════════
            // TABLEAU DE L'HISTORIQUE
            // ═══════════════════════════════════════════════════════════════

            addLogHistoryTable(document, personne.getCin());

            // ═══════════════════════════════════════════════════════════════
            // PIED DE PAGE
            // ═══════════════════════════════════════════════════════════════

            addFooter(document);

            System.out.println("✅ PDF généré avec succès : " + outputPath);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF");
            e.printStackTrace();
            return false;

        } finally {
            document.close();
        }
    }

    /**
     * Ajoute l'en-tête avec logo et titre
     */
    private void addHeader(Document document, Personne personne) throws DocumentException {
        // Titre principal
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(46, 125, 50));
        Paragraph title = new Paragraph("🌱 AGROFLOW", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Sous-titre
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.NORMAL, new BaseColor(85, 139, 47));
        Paragraph subtitle = new Paragraph("Historique des Connexions", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        // Date de génération
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        Paragraph datePara = new Paragraph("Rapport généré le " + currentDate, dateFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        datePara.setSpacingAfter(20);
        document.add(datePara);
    }

    /**
     * Ajoute une ligne de séparation
     */
    private void addSeparator(Document document) throws DocumentException {
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(new BaseColor(76, 175, 80));
        separator.setLineWidth(2);
        document.add(new Chunk(separator));
        document.add(new Paragraph("\n"));
    }

    /**
     * Ajoute les informations de l'utilisateur
     */
    private void addUserInfo(Document document, Personne personne) throws DocumentException {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.BLACK);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);

        // Titre de la section
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(46, 125, 50));
        Paragraph sectionTitle = new Paragraph("📋 Informations de l'utilisateur", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        // Tableau d'informations
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);
        infoTable.setWidths(new float[]{1.5f, 3f});

        // Style des cellules
        PdfPCell labelCell, valueCell;

        // CIN
        labelCell = new PdfPCell(new Phrase("CIN:", labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);
        infoTable.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(String.valueOf(personne.getCin()), valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        infoTable.addCell(valueCell);

        // Nom complet
        labelCell = new PdfPCell(new Phrase("Nom complet:", labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);
        infoTable.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(personne.getPrenom() + " " + personne.getNom(), valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        infoTable.addCell(valueCell);

        // Email
        labelCell = new PdfPCell(new Phrase("Email:", labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);
        infoTable.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(personne.getEmail(), valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        infoTable.addCell(valueCell);

        // Rôle
        labelCell = new PdfPCell(new Phrase("Rôle:", labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(8);
        infoTable.addCell(labelCell);

        String roleName = switch(personne.getRole()) {
            case 1 -> "Utilisateur (Agricole)";
            case 2 -> "Employé";
            case 3 -> "Administrateur";
            default -> "Inconnu";
        };
        valueCell = new PdfPCell(new Phrase(roleName, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        infoTable.addCell(valueCell);

        document.add(infoTable);
    }

    /**
     * Ajoute les statistiques de connexion
     */
    private void addStatistics(Document document, int userCin) throws DocumentException, SQLException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(46, 125, 50));
        Paragraph sectionTitle = new Paragraph("📊 Statistiques", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        // Récupérer les statistiques
        String query = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN success = TRUE THEN 1 ELSE 0 END) as reussies, " +
                "SUM(CASE WHEN success = FALSE THEN 1 ELSE 0 END) as echouees, " +
                "SUM(CASE WHEN two_factor_used = TRUE THEN 1 ELSE 0 END) as avec_2fa, " +
                "MAX(login_time) as derniere_connexion " +
                "FROM login_history WHERE user_cin = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userCin);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int reussies = rs.getInt("reussies");
                    int echouees = rs.getInt("echouees");
                    int avec2FA = rs.getInt("avec_2fa");
                    String derniereConnexion = rs.getString("derniere_connexion");

                    // Créer un tableau de statistiques
                    PdfPTable statsTable = new PdfPTable(2);
                    statsTable.setWidthPercentage(100);
                    statsTable.setSpacingAfter(15);

                    Font statLabelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
                    Font statValueFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(46, 125, 50));

                    // Cellules avec fond coloré
                    BaseColor bgColor = new BaseColor(232, 245, 233);

                    addStatCell(statsTable, "Connexions totales", String.valueOf(total), statLabelFont, statValueFont, bgColor);
                    addStatCell(statsTable, "Connexions réussies", String.valueOf(reussies), statLabelFont, statValueFont, bgColor);
                    addStatCell(statsTable, "Connexions échouées", String.valueOf(echouees), statLabelFont, statValueFont, bgColor);
                    addStatCell(statsTable, "Avec 2FA", String.valueOf(avec2FA), statLabelFont, statValueFont, bgColor);

                    document.add(statsTable);

                    // Dernière connexion
                    if (derniereConnexion != null) {
                        Font infoFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
                        Paragraph lastLogin = new Paragraph("Dernière connexion : " + derniereConnexion, infoFont);
                        lastLogin.setSpacingAfter(10);
                        document.add(lastLogin);
                    }
                }
            }
        }
    }

    /**
     * Ajoute une cellule de statistique
     */
    private void addStatCell(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(10);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new BaseColor(76, 175, 80));
        cell.setBorderWidth(1);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);

        table.addCell(cell);
    }

    /**
     * Ajoute le tableau de l'historique des connexions
     */
    private void addLogHistoryTable(Document document, int userCin) throws DocumentException, SQLException {
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(46, 125, 50));
        Paragraph sectionTitle = new Paragraph("📝 Historique détaillé des connexions", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);

        // Récupérer les logs
        List<LogEntry> logs = getLoginHistoryForUser(userCin);

        if (logs.isEmpty()) {
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, BaseColor.GRAY);
            Paragraph noLogs = new Paragraph("Aucun historique de connexion disponible.", infoFont);
            document.add(noLogs);
            return;
        }

        // Créer le tableau
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{1.5f, 2.5f, 2f, 1.5f, 2.5f});

        // Fonts
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);

        // En-têtes du tableau
        BaseColor headerColor = new BaseColor(76, 175, 80);
        addTableHeader(table, "ID", headerFont, headerColor);
        addTableHeader(table, "Date et Heure", headerFont, headerColor);
        addTableHeader(table, "Adresse IP", headerFont, headerColor);
        addTableHeader(table, "2FA", headerFont, headerColor);
        addTableHeader(table, "Statut", headerFont, headerColor);

        // Données
        int rowNum = 0;
        for (LogEntry log : logs) {
            BaseColor rowColor = (rowNum % 2 == 0) ? BaseColor.WHITE : new BaseColor(245, 245, 245);

            addTableCell(table, String.valueOf(log.getId()), cellFont, rowColor);
            addTableCell(table, log.getLoginTime(), cellFont, rowColor);
            addTableCell(table, log.getIpAddress() != null ? log.getIpAddress() : "N/A", cellFont, rowColor);
            addTableCell(table, log.isTwoFactorUsed() ? "✓" : "✗", cellFont, rowColor);

            // Statut avec couleur
            String status;
            BaseColor statusColor;
            if (log.isSuccess()) {
                status = "✓ Réussie";
                statusColor = new BaseColor(76, 175, 80);
            } else {
                status = "✗ Échouée";
                statusColor = new BaseColor(244, 67, 54);
            }

            PdfPCell statusCell = new PdfPCell(new Phrase(status,
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, statusColor)));
            statusCell.setBackgroundColor(rowColor);
            statusCell.setPadding(5);
            statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(statusCell);

            rowNum++;
        }

        document.add(table);

        // Note
        Font noteFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
        Paragraph note = new Paragraph("\nNote: Seules les 100 dernières connexions sont affichées.", noteFont);
        document.add(note);
    }

    /**
     * Ajoute une cellule d'en-tête au tableau
     */
    private void addTableHeader(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    /**
     * Ajoute une cellule de données au tableau
     */
    private void addTableCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    /**
     * Ajoute le pied de page
     */
    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        LineSeparator separator = new LineSeparator();
        separator.setLineColor(new BaseColor(76, 175, 80));
        separator.setLineWidth(1);
        document.add(new Chunk(separator));

        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
        Paragraph footer = new Paragraph("\nAgroFlow - Système de Gestion Agricole\n" +
                "© 2025 Tous droits réservés\n" +
                "Document confidentiel - Usage interne uniquement", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}