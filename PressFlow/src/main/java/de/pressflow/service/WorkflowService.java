package de.pressflow.service;

import de.pressflow.model.PressRelease;
import de.pressflow.model.ReleaseStatus;
import de.pressflow.model.Role;
import de.pressflow.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

/**
 * Bildet die Regeln des Freigabeprozesses ab:
 * Wer darf in welchem Status welchen Schritt ausführen?
 *
 * Ablauf: Entwurf → In Prüfung → Zur Freigabe → Freigegeben
 * Fach- oder Pressestelle können jederzeit eine Überarbeitung anfordern.
 * Administrator:innen dürfen alle Schritte ausführen (z. B. für Tests oder Vertretungen).
 */
@ApplicationScoped
public class WorkflowService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PressReleaseService pressReleaseService;

    @Inject
    private UserService userService;

    // ---------------------------------------------------------------
    // Berechtigungen (ohne Datenbankzugriff, dadurch einfach testbar)
    // ---------------------------------------------------------------

    // Neue Pressemitteilungen legt die Redaktion an
    public boolean canCreate(User user) {
        return hasRole(user, Role.REDAKTEUR) || isAdmin(user);
    }

    // Bearbeiten darf die Verfasser:in, solange der Status es erlaubt
    public boolean canEdit(PressRelease release, User user) {
        return release.getStatus().isEditable()
                && (isSameUser(release.getAuthor(), user) || isAdmin(user));
    }

    // Einreichen zur Fachprüfung ist möglich, wenn bearbeitet werden darf
    public boolean canSubmit(PressRelease release, User user) {
        return canEdit(release, user);
    }

    // Prüfen darf nur die zugewiesene Fachprüfer:in
    public boolean canReview(PressRelease release, User user) {
        return release.getStatus() == ReleaseStatus.IN_PRUEFUNG
                && (isSameUser(release.getReviewer(), user) || isAdmin(user));
    }

    // Die finale Freigabe erteilt die Pressestelle
    public boolean canApprove(PressRelease release, User user) {
        return release.getStatus() == ReleaseStatus.ZUR_FREIGABE
                && (hasRole(user, Role.PRESSESPRECHER) || isAdmin(user));
    }

    // Eine Überarbeitung kann während der Prüfung oder vor der Freigabe angefordert werden
    public boolean canRequestRevision(PressRelease release, User user) {
        return canReview(release, user) || canApprove(release, user);
    }

    // Exportiert werden nur freigegebene Pressemitteilungen
    public boolean canExport(PressRelease release) {
        return release.getStatus() == ReleaseStatus.FREIGEGEBEN;
    }

    // ---------------------------------------------------------------
    // Prozessschritte
    // ---------------------------------------------------------------

    // Redaktion reicht die Pressemitteilung bei einer Fachprüfer:in ein
    public void submitForReview(PressRelease release, Long reviewerId, User user, String note) {
        if (!canSubmit(release, user)) {
            throw new WorkflowException("Sie dürfen diese Pressemitteilung nicht einreichen.");
        }
        User reviewer = reviewerId == null ? null : userService.find(reviewerId);
        if (!hasRole(reviewer, Role.FACHPRUEFER)) {
            throw new WorkflowException("Bitte wählen Sie eine Fachprüfer:in aus.");
        }
        pressReleaseService.assignReviewer(release.getId(), reviewer);
        pressReleaseService.changeStatus(release.getId(), ReleaseStatus.IN_PRUEFUNG, user, note);
    }

    // Fachprüfung bestätigt die sachliche Richtigkeit und gibt an die Pressestelle weiter
    public void confirmReview(PressRelease release, User user, String note) {
        if (!canReview(release, user)) {
            throw new WorkflowException("Sie dürfen diese Prüfung nicht abschließen.");
        }
        pressReleaseService.changeStatus(release.getId(), ReleaseStatus.ZUR_FREIGABE, user, note);
    }

    // Fachprüfung oder Pressestelle schicken die Pressemitteilung zurück an die Redaktion
    public void requestRevision(PressRelease release, User user, String note) {
        if (!canRequestRevision(release, user)) {
            throw new WorkflowException("Sie dürfen für diese Pressemitteilung keine Überarbeitung anfordern.");
        }
        if (note == null || note.isBlank()) {
            throw new WorkflowException("Bitte begründen Sie, was überarbeitet werden soll.");
        }
        pressReleaseService.changeStatus(release.getId(), ReleaseStatus.UEBERARBEITUNG, user, note);
    }

    // Pressestelle erteilt die finale Freigabe
    public void approve(PressRelease release, User user, String note) {
        if (!canApprove(release, user)) {
            throw new WorkflowException("Sie dürfen diese Pressemitteilung nicht freigeben.");
        }
        pressReleaseService.changeStatus(release.getId(), ReleaseStatus.FREIGEGEBEN, user, note);
    }

    // ---------------------------------------------------------------
    // Hilfsmethoden
    // ---------------------------------------------------------------

    private boolean hasRole(User user, Role role) {
        return user != null && user.getRole() == role;
    }

    private boolean isAdmin(User user) {
        return hasRole(user, Role.ADMINISTRATOR);
    }

    // Vergleicht zwei Personen anhand ihrer ID
    private boolean isSameUser(User first, User second) {
        return first != null && second != null && Objects.equals(first.getId(), second.getId());
    }
}
