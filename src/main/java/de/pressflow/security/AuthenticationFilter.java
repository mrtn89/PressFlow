package de.pressflow.security;

import de.pressflow.view.LoginBean;
import jakarta.faces.application.ResourceHandler;
import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Schützt alle Seiten der Anwendung.
 * Nicht angemeldete Zugriffe werden auf die Login-Seite umgeleitet,
 * die Benutzerverwaltung ist nur für Administrator:innen erreichbar.
 */
@WebFilter(urlPatterns = "*.xhtml")
public class AuthenticationFilter extends HttpFilter {

    private static final long serialVersionUID = 1L;

    private static final String LOGIN_PAGE = "/login.xhtml";
    private static final String OVERVIEW_PAGE = "/overview.xhtml";
    private static final String ADMIN_PAGE = "/users.xhtml";

    // Session-Bean mit der angemeldeten Person
    @Inject
    private LoginBean loginBean;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String page = request.getServletPath();

        // Login-Seite sowie CSS/JavaScript-Ressourcen sind ohne Anmeldung erreichbar
        if (isPublic(request, page)) {
            chain.doFilter(request, response);
            return;
        }

        if (!loginBean.isLoggedIn()) {
            response.sendRedirect(request.getContextPath() + LOGIN_PAGE);
            return;
        }

        if (ADMIN_PAGE.equals(page) && !loginBean.isAdmin()) {
            response.sendRedirect(request.getContextPath() + OVERVIEW_PAGE);
            return;
        }

        chain.doFilter(request, response);
    }

    // Prüft, ob die angefragte Seite ohne Anmeldung aufgerufen werden darf
    private boolean isPublic(HttpServletRequest request, String page) {
        return LOGIN_PAGE.equals(page)
                || request.getRequestURI().contains(ResourceHandler.RESOURCE_IDENTIFIER);
    }
}
