package de.pressflow.service;

import de.pressflow.model.PressRelease;
import de.pressflow.model.ReleaseStatus;
import de.pressflow.model.Role;
import de.pressflow.model.StatusChange;
import de.pressflow.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datenzugriff für Pressemitteilungen und deren Statushistorie.
 * Welche Person einen Schritt ausführen darf, entscheidet der WorkflowService.
 */
@ApplicationScoped
public class PressReleaseService extends AbstractService {

    private static final long serialVersionUID = 1L;

    // Speichert eine neue Pressemitteilung als Entwurf und protokolliert das Anlegen
    public void create(PressRelease release, User author) {
        inTransaction(() -> {
            release.setAuthor(author);
            release.setStatus(ReleaseStatus.ENTWURF);
            release.setCreatedAt(LocalDateTime.now());
            release.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(release);
            entityManager.persist(statusChange(release, null, ReleaseStatus.ENTWURF, author,
                    "Pressemitteilung angelegt"));
        });
    }

    // Übernimmt geänderte Inhalte (Titel, Lead, Text) in die gespeicherte Pressemitteilung
    public void updateContent(PressRelease changedRelease) {
        inTransaction(() -> {
            PressRelease managed = entityManager.find(PressRelease.class, changedRelease.getId());
            managed.setTitle(changedRelease.getTitle());
            managed.setLead(changedRelease.getLead());
            managed.setBody(changedRelease.getBody());
            managed.setUpdatedAt(LocalDateTime.now());
        });
    }

    // Legt fest, welche Fachprüfer:in die Pressemitteilung prüft
    public void assignReviewer(Long releaseId, User reviewer) {
        inTransaction(() -> {
            PressRelease managed = entityManager.find(PressRelease.class, releaseId);
            managed.setReviewer(reviewer);
        });
    }

    // Setzt einen neuen Status und schreibt einen Eintrag in die Statushistorie
    public void changeStatus(Long releaseId, ReleaseStatus newStatus, User changedBy, String note) {
        inTransaction(() -> {
            PressRelease managed = entityManager.find(PressRelease.class, releaseId);
            ReleaseStatus oldStatus = managed.getStatus();
            managed.setStatus(newStatus);
            managed.setUpdatedAt(LocalDateTime.now());
            if (newStatus == ReleaseStatus.FREIGEGEBEN) {
                managed.setApprovedAt(LocalDateTime.now());
            }
            entityManager.persist(statusChange(managed, oldStatus, newStatus, changedBy, note));
        });
    }

    // Sucht eine Pressemitteilung anhand der ID
    public PressRelease find(Long id) {
        return entityManager.find(PressRelease.class, id);
    }

    // Liefert alle Pressemitteilungen, die zuletzt bearbeiteten zuerst
    public List<PressRelease> findAll() {
        return entityManager.createQuery(
                        "select p from PressRelease p order by p.updatedAt desc", PressRelease.class)
                .getResultList();
    }

    // Liefert alle Pressemitteilungen mit einem bestimmten Status
    public List<PressRelease> findByStatus(ReleaseStatus status) {
        return entityManager.createQuery(
                        "select p from PressRelease p where p.status = :status order by p.updatedAt desc",
                        PressRelease.class)
                .setParameter("status", status)
                .getResultList();
    }

    /**
     * Liefert die offenen Aufgaben einer Person, abhängig von ihrer Rolle:
     * Redaktion: eigene Entwürfe und Überarbeitungen,
     * Fachprüfung: zugewiesene Prüfungen,
     * Pressestelle: alles, was auf die Freigabe wartet,
     * Administration: alle noch nicht freigegebenen Pressemitteilungen.
     */
    public List<PressRelease> findOpenTasksFor(User user) {
        TypedQuery<PressRelease> query;
        Role role = user.getRole();

        if (role == Role.REDAKTEUR) {
            query = entityManager.createQuery("select p from PressRelease p where p.author.id = :userId"
                    + " and p.status in :statuses order by p.updatedAt desc", PressRelease.class);
            query.setParameter("userId", user.getId());
            query.setParameter("statuses", List.of(ReleaseStatus.ENTWURF, ReleaseStatus.UEBERARBEITUNG));
        } else if (role == Role.FACHPRUEFER) {
            query = entityManager.createQuery("select p from PressRelease p where p.reviewer.id = :userId"
                    + " and p.status = :status order by p.updatedAt desc", PressRelease.class);
            query.setParameter("userId", user.getId());
            query.setParameter("status", ReleaseStatus.IN_PRUEFUNG);
        } else if (role == Role.PRESSESPRECHER) {
            return findByStatus(ReleaseStatus.ZUR_FREIGABE);
        } else {
            query = entityManager.createQuery("select p from PressRelease p where p.status <> :status"
                    + " order by p.updatedAt desc", PressRelease.class);
            query.setParameter("status", ReleaseStatus.FREIGEGEBEN);
        }
        return query.getResultList();
    }

    // Liefert die Statushistorie einer Pressemitteilung in zeitlicher Reihenfolge
    public List<StatusChange> findHistory(Long releaseId) {
        return entityManager.createQuery("select s from StatusChange s where s.pressRelease.id = :id"
                        + " order by s.changedAt, s.id", StatusChange.class)
                .setParameter("id", releaseId)
                .getResultList();
    }

    // Zählt die Pressemitteilungen mit einem bestimmten Status
    public long countByStatus(ReleaseStatus status) {
        return entityManager.createQuery(
                        "select count(p) from PressRelease p where p.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    // Baut einen Eintrag für die Statushistorie zusammen
    private StatusChange statusChange(PressRelease release, ReleaseStatus oldStatus,
                                      ReleaseStatus newStatus, User changedBy, String note) {
        StatusChange change = new StatusChange();
        change.setPressRelease(release);
        change.setOldStatus(oldStatus);
        change.setNewStatus(newStatus);
        change.setChangedBy(changedBy);
        change.setNote(note);
        change.setChangedAt(LocalDateTime.now());
        return change;
    }
}
