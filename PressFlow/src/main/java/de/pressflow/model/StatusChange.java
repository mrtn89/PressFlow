package de.pressflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Eintrag in der Statushistorie einer Pressemitteilung.
 * Macht nachvollziehbar, wer wann welchen Schritt ausgeführt hat.
 */
@Entity
@Table(name = "status_changes")
public class StatusChange implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primärschlüssel
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Betroffene Pressemitteilung
    @ManyToOne(optional = false)
    @JoinColumn(name = "press_release_id")
    private PressRelease pressRelease;

    // Vorheriger Status (leer beim Anlegen)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReleaseStatus oldStatus;

    // Neuer Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReleaseStatus newStatus;

    // Person, die den Statuswechsel ausgelöst hat
    @ManyToOne(optional = false)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    // Optionale Anmerkung, z. B. Begründung für eine Überarbeitung
    @Column(length = 2000)
    private String note;

    // Zeitpunkt des Statuswechsels
    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PressRelease getPressRelease() {
        return pressRelease;
    }

    public void setPressRelease(PressRelease pressRelease) {
        this.pressRelease = pressRelease;
    }

    public ReleaseStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(ReleaseStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public ReleaseStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ReleaseStatus newStatus) {
        this.newStatus = newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
