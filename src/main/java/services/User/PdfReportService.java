package services.User;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import models.User.Abonnements;
import models.User.Personne;
import models.User.offres;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de génération de rapports PDF pour AgroFlow.
 * Méthode 1 : generateAbonnementPdf()   → fiche détaillée d'un abonnement
 * Méthode 2 : generateStatistiquesPdf() → statistiques globales par service
 */
public class PdfReportService {

    // ── Palette ───────────────────────────────────────────────────────
    private static final DeviceRgb C_PRIMARY    = new DeviceRgb(22,  90,  22);   // vert foncé
    private static final DeviceRgb C_SECONDARY  = new DeviceRgb(14,  60,  14);   // vert très foncé
    private static final DeviceRgb C_ACCENT     = new DeviceRgb(255, 165,  0);   // orange
    private static final DeviceRgb C_LIGHT_BG   = new DeviceRgb(240, 248, 240);  // vert très clair
    private static final DeviceRgb C_ROW_ALT    = new DeviceRgb(248, 252, 248);  // ligne alt.
    private static final DeviceRgb C_WHITE       = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb C_TEXT_DARK  = new DeviceRgb( 30,  30,  30);
    private static final DeviceRgb C_TEXT_MUTED = new DeviceRgb(100, 100, 100);

    private final Connection       connection;
    private final AbonnementService abonnementService;
    private final OffresServicees   offresService;
    private final PersonneService   personneService;

    public PdfReportService() {
        this.connection        = MyDatabase.getInstance().getConnection();
        this.abonnementService = new AbonnementService();
        this.offresService     = new OffresServicees();
        this.personneService   = new PersonneService();
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODE 1 — Fiche abonnement
    // ═══════════════════════════════════════════════════════════════════

    public void generateAbonnementPdf(int idAbonnement, String outputPath) throws Exception {

        Abonnements abonnement = abonnementService.rechercherParId(idAbonnement);
        if (abonnement == null)
            throw new IllegalArgumentException("Abonnement introuvable : " + idAbonnement);

        offres   offre = offresService.rechercherParId(abonnement.getId_offre());
        Personne user  = personneService.rechercherParId(abonnement.getCin());

        PdfWriter   writer   = new PdfWriter(outputPath);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        Document    document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(40, 50, 40, 50);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        addHeader(document, bold, "FICHE D'ABONNEMENT");

        // Badge statut
        String statut = abonnement.getSituation() != null
                ? abonnement.getSituation().toUpperCase() : "INCONNU";
        DeviceRgb badgeColor = statut.equals("ACTIF") ? C_PRIMARY : C_ACCENT;
        addStatusBadge(document, bold, statut, badgeColor);
        document.add(new Paragraph(" "));

        // Informations abonné
        addSectionTitle(document, bold, "Informations de l'abonné");
        Table infoTable = twoColTable();
        if (user != null) {
            row(infoTable, bold, regular, "Nom complet",  user.getPrenom() + " " + user.getNom(), true);
            row(infoTable, bold, regular, "CIN",          String.valueOf(user.getCin()), false);
            row(infoTable, bold, regular, "Email",        user.getEmail(), true);
            row(infoTable, bold, regular, "Téléphone",    nvl(user.getTel()), false);
            row(infoTable, bold, regular, "Adresse",      nvl(user.getAdresse()) + ", " + nvl(user.getVille()), true);
        } else {
            row(infoTable, bold, regular, "CIN", String.valueOf(abonnement.getCin()), true);
        }
        document.add(infoTable);
        document.add(new Paragraph(" "));

        // Offre souscrite
        addSectionTitle(document, bold, "Offre souscrite");
        Table offreTable = twoColTable();
        if (offre != null) {
            row(offreTable, bold, regular, "Nom de l'offre", offre.getNom_offre(), true);
            row(offreTable, bold, regular, "Description",    nvl(offre.getDescription()), false);
            row(offreTable, bold, regular, "Prix",           String.format("%.2f TND / mois", offre.getPrix()), true);
            row(offreTable, bold, regular, "Durée",          offre.getDuree_offre() + " mois", false);
        } else {
            row(offreTable, bold, regular, "ID Offre", String.valueOf(abonnement.getId_offre()), true);
        }
        document.add(offreTable);
        document.add(new Paragraph(" "));

        // Détails abonnement
        addSectionTitle(document, bold, "Détails de l'abonnement");
        Table abonnTable = twoColTable();
        row(abonnTable, bold, regular, "ID Abonnement",     "#" + abonnement.getId_abonn(), true);
        row(abonnTable, bold, regular, "Date souscription", fmtDate(abonnement.getDate_inscription()), false);
        row(abonnTable, bold, regular, "Date expiration",   fmtDate(abonnement.getDate_expiration()), true);
        row(abonnTable, bold, regular, "Statut",            nvl(abonnement.getSituation()), false);
        if (offre != null)
            row(abonnTable, bold, regular, "Montant total",
                    String.format("%.2f TND", offre.getPrix() * offre.getDuree_offre()), true);
        document.add(abonnTable);
        document.add(new Paragraph(" "));

        addFooter(document, regular,
                "Document généré le "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                        + "  |  AgroFlow – Tous droits réservés");

        document.close();
        System.out.println("PDF abonnement généré : " + outputPath);
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODE 2 — Statistiques
    // ═══════════════════════════════════════════════════════════════════

    public void generateStatistiquesPdf(String outputPath) throws Exception {

        Map<String, Object> stats = collectStats();

        PdfWriter   writer   = new PdfWriter(outputPath);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        Document    document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(40, 50, 40, 50);

        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        addHeader(document, bold, "RAPPORT STATISTIQUE");

        document.add(new Paragraph(
                "Période : " + LocalDate.now().withDayOfMonth(1)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " – " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(regular).setFontSize(10)
                .setFontColor(C_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));

        // ── KPI ──────────────────────────────────────────────────────
        addSectionTitle(document, bold, "Indicateurs clés");
        Table kpi = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(12);
        kpiCard(kpi, bold, regular, "Utilisateurs",       String.valueOf(stats.get("totalUsers")),   C_PRIMARY);
        kpiCard(kpi, bold, regular, "Abonnements actifs", String.valueOf(stats.get("abActifs")),      C_SECONDARY);
        kpiCard(kpi, bold, regular, "Offres",             String.valueOf(stats.get("totalOffres")),   C_ACCENT);
        kpiCard(kpi, bold, regular, "Tâches en cours",    String.valueOf(stats.get("tEnCours")),
                new DeviceRgb(70, 130, 180));
        document.add(kpi);
        document.add(new Paragraph(" "));

        // ── Abonnements ───────────────────────────────────────────────
        addSectionTitle(document, bold, "Service Abonnements");
        Table ta = twoColTable();
        row(ta, bold, regular, "Total abonnements",    String.valueOf(stats.get("totalAbonn")), true);
        row(ta, bold, regular, "Actifs",               String.valueOf(stats.get("abActifs")),   false);
        row(ta, bold, regular, "Expirés",              String.valueOf(stats.get("abExpires")),  true);
        row(ta, bold, regular, "En attente",           String.valueOf(stats.get("abAttente")),  false);
        row(ta, bold, regular, "Revenu mensuel (TND)", String.format("%.2f", (Double) stats.get("revenu")), true);
        document.add(ta);
        document.add(new Paragraph(" "));

        // ── Offres ────────────────────────────────────────────────────
        addSectionTitle(document, bold, "Service Offres");
        Table to = new Table(UnitValue.createPercentArray(new float[]{35, 20, 20, 25}))
                .setWidth(UnitValue.createPercentValue(100));
        String[] hdr = {"Offre", "Prix (TND)", "Durée (mois)", "Abonnés"};
        for (String h : hdr)
            to.addHeaderCell(headerCell(h, bold));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> offresData = (List<Map<String, Object>>) stats.get("offresDetail");
        boolean alt = false;
        for (Map<String, Object> r : offresData) {
            DeviceRgb bg = alt ? C_ROW_ALT : C_WHITE;
            to.addCell(styledCell(String.valueOf(r.get("nom")),      regular, 9, bg));
            to.addCell(styledCell(String.format("%.2f", r.get("prix")), regular, 9, bg));
            to.addCell(styledCell(String.valueOf(r.get("duree")),    regular, 9, bg));
            to.addCell(styledCell(String.valueOf(r.get("nbAbonnes")), bold,   9, bg));
            alt = !alt;
        }
        document.add(to);
        document.add(new Paragraph(" "));

        // ── Utilisateurs ──────────────────────────────────────────────
        addSectionTitle(document, bold, "Service Utilisateurs");
        Table tu = twoColTable();
        row(tu, bold, regular, "Total utilisateurs",     String.valueOf(stats.get("totalUsers")),   true);
        row(tu, bold, regular, "Admins",                 String.valueOf(stats.get("totalAdmins")),  false);
        row(tu, bold, regular, "Employés",               String.valueOf(stats.get("totalEmployes")), true);
        row(tu, bold, regular, "Clients",                String.valueOf(stats.get("totalClients")),  false);
        row(tu, bold, regular, "Utilisateurs avec 2FA",  String.valueOf(stats.get("users2FA")),      true);
        document.add(tu);
        document.add(new Paragraph(" "));

        // ── Tâches ────────────────────────────────────────────────────
        addSectionTitle(document, bold, "Service Tâches");
        Table tt = twoColTable();
        row(tt, bold, regular, "Total tâches",    String.valueOf(stats.get("totalTaches")),  true);
        row(tt, bold, regular, "En cours",        String.valueOf(stats.get("tEnCours")),     false);
        row(tt, bold, regular, "Terminées",       String.valueOf(stats.get("tTerminees")),   true);
        row(tt, bold, regular, "En retard",       String.valueOf(stats.get("tRetard")),      false);
        row(tt, bold, regular, "Priorité haute",  String.valueOf(stats.get("tHaute")),       true);
        document.add(tt);
        document.add(new Paragraph(" "));

        addFooter(document, regular,
                "Rapport généré le "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))
                        + "  |  AgroFlow – Données confidentielles");

        document.close();
        System.out.println("PDF statistiques généré : " + outputPath);
    }

    // ═══════════════════════════════════════════════════════════════════
    // COLLECTE DES STATISTIQUES
    // ═══════════════════════════════════════════════════════════════════

    private Map<String, Object> collectStats() throws SQLException {
        Map<String, Object> s = new HashMap<>();
        try (Statement st = connection.createStatement()) {

            s.put("totalUsers",    queryCount(st, "SELECT COUNT(*) FROM users"));
            s.put("totalAdmins",   queryCount(st, "SELECT COUNT(*) FROM users WHERE role=3"));
            s.put("totalEmployes", queryCount(st, "SELECT COUNT(*) FROM users WHERE role=2"));
            s.put("totalClients",  queryCount(st, "SELECT COUNT(*) FROM users WHERE role=1"));
            s.put("users2FA",      queryCount(st, "SELECT COUNT(*) FROM users WHERE two_factor_enabled=1"));

            s.put("totalAbonn", queryCount(st, "SELECT COUNT(*) FROM abonnements"));
            s.put("abActifs",   queryCount(st, "SELECT COUNT(*) FROM abonnements WHERE LOWER(situation)='actif'"));
            s.put("abExpires",  queryCount(st, "SELECT COUNT(*) FROM abonnements WHERE LOWER(situation)='expire'"));
            s.put("abAttente",  queryCount(st, "SELECT COUNT(*) FROM abonnements WHERE LOWER(situation)='attente'"));

            ResultSet rs = st.executeQuery(
                    "SELECT COALESCE(SUM(o.prix),0) FROM abonnements a " +
                            "JOIN offres o ON a.id_offre=o.id_offres WHERE LOWER(a.situation)='actif'");
            s.put("revenu", rs.next() ? rs.getDouble(1) : 0.0);

            List<Map<String, Object>> offresDetail = new ArrayList<>();
            rs = st.executeQuery(
                    "SELECT o.nom_offre, o.prix, o.duree_offre, COUNT(a.id_abonn) nb " +
                            "FROM offres o LEFT JOIN abonnements a ON o.id_offres=a.id_offre " +
                            "GROUP BY o.id_offres ORDER BY nb DESC");
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("nom",      rs.getString("nom_offre"));
                row.put("prix",     rs.getDouble("prix"));
                row.put("duree",    rs.getInt("duree_offre"));
                row.put("nbAbonnes", rs.getInt("nb"));
                offresDetail.add(row);
            }
            s.put("offresDetail", offresDetail);
            s.put("totalOffres", offresDetail.size());

            s.put("totalTaches", queryCount(st, "SELECT COUNT(*) FROM taches"));
            s.put("tEnCours",    queryCount(st, "SELECT COUNT(*) FROM taches WHERE LOWER(etat)='en cours'"));
            s.put("tTerminees",  queryCount(st, "SELECT COUNT(*) FROM taches WHERE LOWER(etat)='termine'"));
            s.put("tRetard",     queryCount(st, "SELECT COUNT(*) FROM taches WHERE date_echeancee < NOW() AND LOWER(etat)!='termine'"));
            s.put("tHaute",      queryCount(st, "SELECT COUNT(*) FROM taches WHERE LOWER(priorite)='haute'"));
        }
        return s;
    }

    private int queryCount(Statement st, String sql) throws SQLException {
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS VISUELS
    // ═══════════════════════════════════════════════════════════════════

    private void addHeader(Document doc, PdfFont bold, String title) {
        Table banner = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(C_SECONDARY);
        banner.addCell(new Cell()
                .add(new Paragraph("AgroFlow")
                        .setFont(bold).setFontSize(17).setFontColor(C_WHITE))
                .setPadding(14).setBorder(Border.NO_BORDER));
        banner.addCell(new Cell()
                .add(new Paragraph(title)
                        .setFont(bold).setFontSize(20).setFontColor(C_WHITE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setPadding(14).setBorder(Border.NO_BORDER));
        doc.add(banner);
        doc.add(new Paragraph(" "));
    }

    private void addStatusBadge(Document doc, PdfFont bold, String statut, DeviceRgb color) {
        Table badge = new Table(UnitValue.createPercentArray(new float[]{100}))
                .setWidth(UnitValue.createPercentValue(28));
        badge.addCell(new Cell()
                .add(new Paragraph(statut).setFont(bold).setFontSize(11)
                        .setFontColor(C_WHITE).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(color).setPadding(6).setBorder(Border.NO_BORDER));
        doc.add(badge);
    }

    private void addSectionTitle(Document doc, PdfFont bold, String title) {
        doc.add(new Paragraph(title)
                .setFont(bold).setFontSize(12).setFontColor(C_SECONDARY)
                .setBorderBottom(new SolidBorder(C_PRIMARY, 2))
                .setPaddingBottom(4).setMarginBottom(6));
    }

    private Table twoColTable() {
        return new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));
    }

    /** Ligne clé/valeur avec fond alterné */
    private void row(Table table, PdfFont bold, PdfFont regular,
                     String label, String value, boolean shaded) {
        DeviceRgb bg = shaded ? C_LIGHT_BG : C_WHITE;
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(10).setFontColor(C_TEXT_DARK))
                .setBackgroundColor(bg).setPadding(7).setBorder(Border.NO_BORDER));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(regular).setFontSize(10).setFontColor(C_TEXT_DARK))
                .setBackgroundColor(bg).setPadding(7).setBorder(Border.NO_BORDER));
    }

    /** Cellule d'en-tête de tableau */
    private Cell headerCell(String text, PdfFont bold) {
        return new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(C_WHITE))
                .setBackgroundColor(C_SECONDARY).setPadding(6).setBorder(Border.NO_BORDER);
    }

    /** Cellule de données */
    private Cell styledCell(String text, PdfFont font, float size, DeviceRgb bg) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(size).setFontColor(C_TEXT_DARK))
                .setBackgroundColor(bg).setPadding(6).setBorder(Border.NO_BORDER);
    }

    /** Carte KPI (stats) */
    private void kpiCard(Table table, PdfFont bold, PdfFont regular,
                         String label, String value, DeviceRgb color) {
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(bold).setFontSize(22)
                        .setFontColor(color).setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(label).setFont(regular).setFontSize(8)
                        .setFontColor(C_TEXT_MUTED).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(C_ROW_ALT).setPadding(12).setMargin(4)
                .setBorder(new SolidBorder(color, 2)));
    }

    private void addFooter(Document doc, PdfFont regular, String note) {
        doc.add(new Paragraph(note)
                .setFont(regular).setFontSize(8).setFontColor(C_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderTop(new SolidBorder(C_TEXT_MUTED, 0.5f))
                .setPaddingTop(8));
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════

    private String nvl(String s) { return s != null ? s : "—"; }

    private String fmtDate(String d) {
        if (d == null || d.isEmpty()) return "—";
        try {
            return LocalDate.parse(d.substring(0, 10))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) { return d; }
    }
}