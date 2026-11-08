package de.pressflow.view;

import de.pressflow.model.Comment;
import de.pressflow.model.PressRelease;
import de.pressflow.model.Role;
import de.pressflow.model.StatusChange;
import de.pressflow.model.User;
import de.pressflow.service.CommentService;
import de.pressflow.service.PressReleaseService;
import de.pressflow.service.UserService;
import de.pressflow.service.WordExportService;
import de.pressflow.service.WorkflowException;
import de.pressflow.service.WorkflowService;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * View-scoped Bean für die Detailseite einer Pressemitteilung.
 * Hier wird eine Pressemitteilung angelegt, bearbeitet, kommentiert,
 * durch den Freigabeprozess geführt und als Word-Datei exportiert.
 */
@Named
@ViewScoped
public class ReleaseDetailBean extends MessageSupport implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String WORD_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @Inject
    private PressReleaseService pressReleaseService;

    @Inject
    private CommentService commentService;

    @Inject
    private UserService userService;

    @Inject
    private WorkflowService workflowService;

    @Inject
    private WordExportService wordExportService;

    @Inject
    private LoginBean loginBean;

    // ID aus der URL (release.xhtml?id=...); leer beim Anlegen einer neuen Pressemitteilung
    private Long releaseId;

    // Aktuell angezeigte Pressemitteilung
    private PressRelease release;

    // Ausgewählte Fachprüfer:in beim Einreichen
    private Long reviewerId;

    // Anmerkung zu einem Prozessschritt (Pflicht bei "Überarbeitung anfordern")
    private String workflowNote;

    // Text eines neuen Kommentars
    private String newComment;

    // Lädt die Pressemitteilung beim ersten Aufruf der Seite
    public void load() {
        if (releaseId == null) {
            release = new PressRelease();
            return;
        }
        release = pressReleaseService.find(releaseId);
        if (release == null) {
            error("Die Pressemitteilung wurde nicht gefunden.");
            return;
        }
        if (release.getReviewer() != null) {
            reviewerId = release.getReviewer().getId();
        }
    }

    // Speichert eine neue oder geänderte Pressemitteilung
    public String save() {
        if (!isCanEdit()) {
            error("Sie dürfen diese Pressemitteilung nicht bearbeiten.");
            return null;
        }
        if (isNewRelease()) {
            pressReleaseService.create(release, currentUser());
            info("Die Pressemitteilung wurde als Entwurf angelegt.");
            keepMessagesAfterRedirect();
            return "/release.xhtml?faces-redirect=true&id=" + release.getId();
        }
        pressReleaseService.updateContent(release);
        reload();
        info("Die Änderungen wurden gespeichert.");
        return null;
    }

    // Redaktion: zur Fachprüfung einreichen
    public void submitForReview() {
        runWorkflowStep(() -> workflowService.submitForReview(release, reviewerId, currentUser(), workflowNote),
                "Die Pressemitteilung wurde zur Fachprüfung eingereicht.");
    }

    // Fachprüfung: sachliche Richtigkeit bestätigen
    public void confirmReview() {
        runWorkflowStep(() -> workflowService.confirmReview(release, currentUser(), workflowNote),
                "Die Prüfung wurde bestätigt und an die Pressestelle weitergegeben.");
    }

    // Fachprüfung oder Pressestelle: zurück an die Redaktion
    public void requestRevision() {
        runWorkflowStep(() -> workflowService.requestRevision(release, currentUser(), workflowNote),
                "Die Pressemitteilung wurde zur Überarbeitung zurückgegeben.");
    }

    // Pressestelle: finale Freigabe
    public void approve() {
        runWorkflowStep(() -> workflowService.approve(release, currentUser(), workflowNote),
                "Die Pressemitteilung wurde freigegeben.");
    }

    // Speichert einen neuen Kommentar
    public void addComment() {
        if (newComment == null || newComment.isBlank()) {
            error("Bitte geben Sie einen Kommentar ein.");
            return;
        }
        commentService.add(release.getId(), currentUser(), newComment);
        newComment = null;
        info("Der Kommentar wurde gespeichert.");
    }

    // Erzeugt die Word-Datei und sendet sie als Download an den Browser
    public void exportWord() throws IOException {
        if (!isCanExport()) {
            error("Nur freigegebene Pressemitteilungen können exportiert werden.");
            return;
        }
        byte[] document = wordExportService.createDocument(release);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.responseReset();
        externalContext.setResponseContentType(WORD_CONTENT_TYPE);
        externalContext.setResponseContentLength(document.length);
        externalContext.setResponseHeader("Content-Disposition",
                "attachment; filename=\"" + wordExportService.createFileName(release) + "\"");

        try (OutputStream output = externalContext.getResponseOutputStream()) {
            output.write(document);
        }
        // JSF soll nach dem Download keine Seite mehr rendern
        facesContext.responseComplete();
    }

    // Führt einen Prozessschritt aus und zeigt Erfolg oder Fehler an
    private void runWorkflowStep(Runnable step, String successMessage) {
        try {
            // Vorher neu laden, damit die Berechtigung am aktuellen Status geprüft wird
            reload();
            step.run();
            workflowNote = null;
            reload();
            info(successMessage);
        } catch (WorkflowException e) {
            error(e.getMessage());
        }
    }

    // Lädt die Pressemitteilung neu, damit Status und Zuständigkeiten aktuell sind
    private void reload() {
        release = pressReleaseService.find(release.getId());
    }

    private User currentUser() {
        return loginBean.getCurrentUser();
    }

    // --- Daten für die Anzeige ---

    // Wird gerade eine neue Pressemitteilung angelegt?
    public boolean isNewRelease() {
        return release != null && release.getId() == null;
    }

    // Wurde eine Pressemitteilung geladen bzw. wird eine angelegt?
    public boolean isLoaded() {
        return release != null;
    }

    public List<Comment> getComments() {
        return isNewRelease() || release == null ? Collections.emptyList() : commentService.findByRelease(release.getId());
    }

    public List<StatusChange> getHistory() {
        return isNewRelease() || release == null ? Collections.emptyList() : pressReleaseService.findHistory(release.getId());
    }

    // Alle Fachprüfer:innen für die Auswahl beim Einreichen
    public List<User> getReviewers() {
        return userService.findByRole(Role.FACHPRUEFER);
    }

    // --- Berechtigungen für die Anzeige von Formularen und Schaltflächen ---

    public boolean isCanEdit() {
        if (release == null) {
            return false;
        }
        return isNewRelease() ? workflowService.canCreate(currentUser()) : workflowService.canEdit(release, currentUser());
    }

    public boolean isCanSubmit() {
        return !isNewRelease() && release != null && workflowService.canSubmit(release, currentUser());
    }

    public boolean isCanReview() {
        return !isNewRelease() && release != null && workflowService.canReview(release, currentUser());
    }

    public boolean isCanApprove() {
        return !isNewRelease() && release != null && workflowService.canApprove(release, currentUser());
    }

    public boolean isCanRequestRevision() {
        return !isNewRelease() && release != null && workflowService.canRequestRevision(release, currentUser());
    }

    public boolean isCanExport() {
        return !isNewRelease() && release != null && workflowService.canExport(release);
    }

    // Steht für die angemeldete Person gerade ein Prozessschritt an?
    public boolean isWorkflowActionAvailable() {
        return isCanSubmit() || isCanReview() || isCanApprove();
    }

    // --- Getter und Setter ---

    public Long getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    public PressRelease getRelease() {
        return release;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getWorkflowNote() {
        return workflowNote;
    }

    public void setWorkflowNote(String workflowNote) {
        this.workflowNote = workflowNote;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }
}
