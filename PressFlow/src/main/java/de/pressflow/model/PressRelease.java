package de.pressflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

// Pressemitteilung, die den Freigabeprozess durchläuft
@Entity
@Table(name = "press_releases")
public class PressRelease implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primärschlüssel
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Überschrift der Pressemitteilung
    @Column(nullable = false, length = 200)
    private String title;

    // Kurzer Einleitungstext (Lead), fasst die Kernaussage zusammen.
    // Spaltenname bewusst nicht "lead", da LEAD in MySQL 8 ein reserviertes Wort ist.
    @Column(name = "lead_text", length = 1000)
    private String lead;

    // Haupttext; Absätze werden durch Leerzeilen getrennt
    @Lob
    @Column(nullable = false)
    private String body;

    // Aktueller Status im Freigabeprozess
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReleaseStatus status = ReleaseStatus.ENTWURF;

    // Verfasser:in (Redaktion)
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    // Zuständige Fachprüfer:in, wird beim Einreichen festgelegt
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    // Erstellungszeitpunkt
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Zeitpunkt der letzten Änderung
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Zeitpunkt der finalen Freigabe durch die Pressestelle
    private LocalDateTime approvedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLead() {
        return lead;
    }

    public void setLead(String lead) {
        this.lead = lead;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ReleaseStatus getStatus() {
        return status;
    }

    public void setStatus(ReleaseStatus status) {
        this.status = status;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
