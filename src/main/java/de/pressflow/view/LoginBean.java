package de.pressflow.view;

import de.pressflow.model.Role;
import de.pressflow.model.User;
import de.pressflow.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Session-Bean für die Anmeldung.
 * Ersetzt die Rollenauswahl aus Require4Testing durch einen einfachen Login
 * und hält die angemeldete Person für die Dauer der Sitzung.
 */
@Named
@SessionScoped
public class LoginBean extends MessageSupport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UserService userService;

    // Eingaben aus dem Login-Formular
    private String username;
    private String password;

    // Angemeldete Person; null, solange niemand angemeldet ist
    private User currentUser;

    // Prüft die Zugangsdaten und leitet bei Erfolg zur Übersicht weiter
    public String login() {
        userService.createDemoUsersIfEmpty();
        User user = userService.authenticate(username, password);
        password = null;

        if (user == null) {
            error("Benutzername oder Passwort ist falsch.");
            return null;
        }

        // Neue Session-ID nach der Anmeldung schützt vor Session-Fixation
        HttpServletRequest request = (HttpServletRequest) externalContext().getRequest();
        request.changeSessionId();

        currentUser = user;
        return "/overview.xhtml?faces-redirect=true";
    }

    // Meldet die Person ab und beendet die Sitzung
    public String logout() {
        externalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    // Ist aktuell jemand angemeldet?
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Hat die angemeldete Person die angegebene Rolle? (in XHTML z. B. hasRole('REDAKTEUR'))
    public boolean hasRole(Role role) {
        return isLoggedIn() && currentUser.getRole() == role;
    }

    // Kurzform für die Administrationsrolle
    public boolean isAdmin() {
        return hasRole(Role.ADMINISTRATOR);
    }

    private ExternalContext externalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
