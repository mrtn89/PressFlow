package de.pressflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.pressflow.model.PressRelease;
import de.pressflow.model.User;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

class WordExportServiceTest {

    private final WordExportService exportService = new WordExportService();

    @Test
    void documentContainsTitleLeadAndAllParagraphs() throws IOException {
        String text = readText(exportService.createDocument(sampleRelease()));

        assertTrue(text.contains("PRESSEMITTEILUNG – 04.11.2026"));
        assertTrue(text.contains("Neues Werk in Olpe eröffnet"));
        assertTrue(text.contains("Kurzfassung der Kernaussage"));
        assertTrue(text.contains("Erster Absatz."));
        assertTrue(text.contains("Zweiter Absatz."));
        assertTrue(text.contains("Lena Schulte (Unternehmenskommunikation)"));
    }

    @Test
    void fileNameContainsId() {
        assertEquals("Pressemitteilung_7.docx", exportService.createFileName(sampleRelease()));
    }

    private PressRelease sampleRelease() {
        User author = new User();
        author.setFullName("Lena Schulte");
        author.setDepartment("Unternehmenskommunikation");

        PressRelease release = new PressRelease();
        release.setId(7L);
        release.setTitle("Neues Werk in Olpe eröffnet");
        release.setLead("Kurzfassung der Kernaussage");
        release.setBody("Erster Absatz.\n\nZweiter Absatz.");
        release.setAuthor(author);
        release.setApprovedAt(LocalDateTime.of(2026, 11, 4, 10, 0));
        return release;
    }

    // Liest den reinen Text aus dem erzeugten Word-Dokument
    private String readText(byte[] document) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(document));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
