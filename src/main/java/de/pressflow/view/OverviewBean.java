package de.pressflow.view;

import de.pressflow.model.PressRelease;
import de.pressflow.model.ReleaseStatus;
import de.pressflow.service.PressReleaseService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

/**
 * Bean für die Übersichtsseite (Dashboard).
 * Liefert Kennzahlen je Status und die offenen Aufgaben der angemeldeten Person.
 * Die Seite enthält keine Formulare, daher genügt RequestScoped.
 */
@Named
@RequestScoped
public class OverviewBean {

    @Inject
    private PressReleaseService pressReleaseService;

    @Inject
    private LoginBean loginBean;

    // Alle Statuswerte für die Kennzahlen-Kacheln
    public ReleaseStatus[] getStatuses() {
        return ReleaseStatus.values();
    }

    // Anzahl der Pressemitteilungen mit dem angegebenen Status
    public long countByStatus(ReleaseStatus status) {
        return pressReleaseService.countByStatus(status);
    }

    // Offene Aufgaben der angemeldeten Person
    public List<PressRelease> getMyTasks() {
        return pressReleaseService.findOpenTasksFor(loginBean.getCurrentUser());
    }

    // Erklärt, welche Aufgaben für die eigene Rolle angezeigt werden
    public String getTaskHint() {
        switch (loginBean.getCurrentUser().getRole()) {
            case REDAKTEUR:
                return "Ihre Entwürfe und Pressemitteilungen, die überarbeitet werden sollen.";
            case FACHPRUEFER:
                return "Pressemitteilungen, die Ihnen zur fachlichen Prüfung zugewiesen wurden.";
            case PRESSESPRECHER:
                return "Fachlich geprüfte Pressemitteilungen, die auf Ihre Freigabe warten.";
            default:
                return "Alle Pressemitteilungen, die noch nicht freigegeben sind.";
        }
    }
}
