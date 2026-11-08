package de.pressflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

// Kommentar bzw. Änderungswunsch zu einer Pressemitteilung
@Entity
@Table(name = "comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primärschlüssel
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Pressemitteilung, zu der der Kommentar gehört
    @ManyToOne(optional = false)
    @JoinColumn(name = "press_release_id")
    private PressRelease pressRelease;

    // Verfasser:in des Kommentars
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    // Kommentartext
    @Column(name = "comment_text", nullable = false, length = 2000)
    private String text;

    // Erstellungszeitpunkt
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
