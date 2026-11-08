package de.pressflow.service;

import de.pressflow.model.Comment;
import de.pressflow.model.PressRelease;
import de.pressflow.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

// Kommentare und Änderungswünsche zu Pressemitteilungen
@ApplicationScoped
public class CommentService extends AbstractService {

    private static final long serialVersionUID = 1L;

    // Speichert einen neuen Kommentar zu einer Pressemitteilung
    public void add(Long releaseId, User author, String text) {
        inTransaction(() -> {
            Comment comment = new Comment();
            comment.setPressRelease(entityManager.find(PressRelease.class, releaseId));
            comment.setAuthor(author);
            comment.setText(text.trim());
            comment.setCreatedAt(LocalDateTime.now());
            entityManager.persist(comment);
        });
    }

    // Liefert alle Kommentare einer Pressemitteilung, die ältesten zuerst
    public List<Comment> findByRelease(Long releaseId) {
        return entityManager.createQuery("select c from Comment c where c.pressRelease.id = :id"
                        + " order by c.createdAt, c.id", Comment.class)
                .setParameter("id", releaseId)
                .getResultList();
    }
}
