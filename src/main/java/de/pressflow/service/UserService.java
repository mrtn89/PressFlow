package de.pressflow.service;

import de.pressflow.model.Role;
import de.pressflow.model.User;
import de.pressflow.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

// Verwaltung und Anmeldung von Benutzer:innen
@ApplicationScoped
public class UserService extends AbstractService {

    private static final long serialVersionUID = 1L;

    // Startpasswort der Demo-Benutzer:innen (siehe README)
    private static final String DEMO_PASSWORD = "pressflow";

    // Legt eine neue Person an und speichert das Passwort als Hash
    public void create(User user, String plainPassword) {
        user.setPasswordHash(PasswordHasher.hash(plainPassword));
        inTransaction(() -> entityManager.persist(user));
    }

    // Sucht eine Person anhand der ID
    public User find(Long id) {
        return entityManager.find(User.class, id);
    }

    // Liefert alle Personen alphabetisch nach Namen
    public List<User> findAll() {
        return entityManager.createQuery("select u from User u order by u.fullName", User.class)
                .getResultList();
    }

    // Liefert alle Personen mit einer bestimmten Rolle, z. B. alle Fachprüfer:innen
    public List<User> findByRole(Role role) {
        return entityManager.createQuery(
                        "select u from User u where u.role = :role order by u.fullName", User.class)
                .setParameter("role", role)
                .getResultList();
    }

    // Sucht eine Person anhand des Anmeldenamens; liefert null, wenn es keine gibt
    public User findByUsername(String username) {
        List<User> result = entityManager.createQuery(
                        "select u from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    // Prüft die Zugangsdaten; liefert die Person bei Erfolg, sonst null
    public User authenticate(String username, String plainPassword) {
        User user = findByUsername(username);
        if (user != null && PasswordHasher.matches(plainPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    // Zählt alle vorhandenen Personen
    public long countAll() {
        return entityManager.createQuery("select count(u) from User u", Long.class).getSingleResult();
    }

    /**
     * Legt beim ersten Start Demo-Benutzer:innen für jede Rolle an,
     * damit die Anwendung sofort ausprobiert werden kann.
     */
    public void createDemoUsersIfEmpty() {
        if (countAll() > 0) {
            return;
        }
        create(demoUser("admin", "Administrator", "IT", Role.ADMINISTRATOR), DEMO_PASSWORD);
        create(demoUser("redakteur", "Lena Schulte", "Unternehmenskommunikation", Role.REDAKTEUR), DEMO_PASSWORD);
        create(demoUser("fachpruefer", "Thomas Becker", "Technik", Role.FACHPRUEFER), DEMO_PASSWORD);
        create(demoUser("fachpruefer2", "Jonas Hoffmann", "Finanzen", Role.FACHPRUEFER), DEMO_PASSWORD);
        create(demoUser("pressesprecher", "Sabine Krämer", "Pressestelle", Role.PRESSESPRECHER), DEMO_PASSWORD);
    }

    private User demoUser(String username, String fullName, String department, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setDepartment(department);
        user.setRole(role);
        return user;
    }
}
