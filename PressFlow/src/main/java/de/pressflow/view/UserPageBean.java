package de.pressflow.view;

import de.pressflow.model.Role;
import de.pressflow.model.User;
import de.pressflow.service.UserService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * View-scoped Bean für die Benutzerverwaltung (nur Administration).
 * Ermöglicht das Anlegen neuer Benutzer:innen.
 */
@Named
@ViewScoped
public class UserPageBean extends MessageSupport implements Serializable {

    private static final long serialVersionUID = 1L;

    // Mindestlänge für neue Passwörter
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Inject
    private UserService userService;

    // Formularobjekt für eine neue Person
    private User user = new User();

    // Startpasswort; wird nur gehasht gespeichert
    private String password;

    // Speichert die neue Person, sofern Anmeldename und Passwort gültig sind
    public void save() {
        if (userService.findByUsername(user.getUsername()) != null) {
            error("Der Benutzername ist bereits vergeben.");
            return;
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            error("Das Passwort muss mindestens " + MIN_PASSWORD_LENGTH + " Zeichen lang sein.");
            return;
        }
        userService.create(user, password);
        info("Benutzer:in " + user.getFullName() + " wurde angelegt.");
        user = new User();
        password = null;
    }

    // Alle Personen für die Übersichtstabelle
    public List<User> getUsers() {
        return userService.findAll();
    }

    // Alle Rollen für die Auswahlliste
    public Role[] getRoles() {
        return Role.values();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
