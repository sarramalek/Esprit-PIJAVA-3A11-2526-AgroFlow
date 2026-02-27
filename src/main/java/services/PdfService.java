package services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import models.Animaux.animaux;
import models.Animaux.examens;
import java.io.FileOutputStream;
import java.util.List;

public class PdfService {

    public void genererCarnetSante(animaux animal, List<examens> historique) {
        Document document = new Document(PageSize.A4);
        try {
            String fileName = "Carnet_Sante_" + animal.getNom() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // 1. En-tête (Design AgroFlow)
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph titre = new Paragraph("AGROFLOW - CARNET DE SANTÉ", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            document.add(titre);
            document.add(new Paragraph(" ")); // Espace

            // 2. Informations de l'Animal
            document.add(new Paragraph("Nom de l'animal : " + animal.getNom()));
            document.add(new Paragraph("Espèce : " + animal.getEspece()));
            document.add(new Paragraph("Sexe : " + animal.getSexe()));
            document.add(new Paragraph("------------------------------------------------------------------"));
            document.add(new Paragraph(" "));

            // 3. Tableau de l'historique médical
            PdfPTable table = new PdfPTable(4); // 4 colonnes
            table.setWidthPercentage(100);

            // En-têtes du tableau
            table.addCell("Date");
            table.addCell("Type d'Examen");
            table.addCell("Diagnostic");
            table.addCell("Traitement");

            // Remplissage avec les données de la base
            for (examens ex : historique) {
                table.addCell(ex.getDate_examen().toString());
                table.addCell(ex.getType_examen());
                table.addCell(ex.getDiagnostic());
                table.addCell(ex.getTraitement());
            }
            document.add(table);

            // 4. Pied de page
            document.add(new Paragraph(" "));
            Paragraph signature = new Paragraph("Signature du Vétérinaire : _______________________");
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
            System.out.println("PDF généré avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}