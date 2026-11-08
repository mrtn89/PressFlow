package de.pressflow.service;

import de.pressflow.model.PressRelease;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Erzeugt aus einer freigegebenen Pressemitteilung ein Word-Dokument (.docx) mit Apache POI.
 * Das Dokument ist bewusst schlicht gehalten, damit es in Word individuell
 * (z. B. im Corporate Design) weiterformatiert werden kann.
 */
@ApplicationScoped
public class WordExportService implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String FONT = "Arial";
    private static final String GRAY = "666666";

    // Erstellt das Word-Dokument und liefert es als Byte-Array (z. B. für den Download)
    public byte[] createDocument(PressRelease release) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            addLabelAndDate(document, release);
            addTitle(document, release.getTitle());
            addLead(document, release.getLead());
            addBody(document, release.getBody());
            addContact(document, release);

            document.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Word-Dokument konnte nicht erstellt werden.", e);
        }
    }

    // Liefert einen eindeutigen, für alle Systeme gültigen Dateinamen
    public String createFileName(PressRelease release) {
        return "Pressemitteilung_" + release.getId() + ".docx";
    }

    // Kennzeichnung "PRESSEMITTEILUNG" und Datum der Freigabe
    private void addLabelAndDate(XWPFDocument document, PressRelease release) {
        LocalDateTime date = release.getApprovedAt() != null ? release.getApprovedAt() : LocalDateTime.now();
        XWPFRun run = newRun(newParagraph(document, 240));
        run.setBold(true);
        run.setColor(GRAY);
        run.setText("PRESSEMITTEILUNG – " + DATE_FORMAT.format(date));
    }

    // Überschrift
    private void addTitle(XWPFDocument document, String title) {
        XWPFRun run = newRun(newParagraph(document, 240));
        run.setBold(true);
        run.setFontSize(18);
        run.setText(title);
    }

    // Lead in fetter Schrift, entfällt, wenn kein Lead erfasst wurde
    private void addLead(XWPFDocument document, String lead) {
        if (lead == null || lead.isBlank()) {
            return;
        }
        XWPFRun run = newRun(newParagraph(document, 240));
        run.setBold(true);
        run.setText(lead.trim());
    }

    // Haupttext: Leerzeilen trennen Absätze, einfache Zeilenumbrüche bleiben erhalten
    private void addBody(XWPFDocument document, String body) {
        for (String paragraphText : body.trim().split("\\R\\s*\\R")) {
            XWPFRun run = newRun(newParagraph(document, 160));
            String[] lines = paragraphText.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    run.addBreak();
                }
                run.setText(lines[i].trim());
            }
        }
    }

    // Pressekontakt am Ende des Dokuments
    private void addContact(XWPFDocument document, PressRelease release) {
        XWPFRun heading = newRun(newParagraph(document, 0));
        heading.addBreak();
        heading.setBold(true);
        heading.setText("Pressekontakt");

        XWPFRun contact = newRun(newParagraph(document, 0));
        contact.setText(release.getAuthor().getNameWithDepartment());
    }

    // Neuer Absatz mit Abstand nach unten (in Twips, 20 Twips = 1 Punkt)
    private XWPFParagraph newParagraph(XWPFDocument document, int spacingAfter) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }

    // Neuer Textabschnitt mit einheitlicher Schrift
    private XWPFRun newRun(XWPFParagraph paragraph) {
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(FONT);
        run.setFontSize(11);
        return run;
    }
}
