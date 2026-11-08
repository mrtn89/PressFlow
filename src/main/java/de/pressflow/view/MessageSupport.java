package de.pressflow.view;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Abstrakte Basisklasse für Beans, die Meldungen in der Oberfläche ausgeben.
 * Stellt Hilfsmethoden für Info- und Fehlermeldungen bereit.
 */
public abstract class MessageSupport {

    // Fügt eine Info-Meldung hinzu
    protected void info(String summary) {
        addMessage(FacesMessage.SEVERITY_INFO, summary);
    }

    // Fügt eine Fehlermeldung hinzu
    protected void error(String summary) {
        addMessage(FacesMessage.SEVERITY_ERROR, summary);
    }

    // Sorgt dafür, dass Meldungen auch nach einer Weiterleitung (Redirect) angezeigt werden
    protected void keepMessagesAfterRedirect() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    private void addMessage(FacesMessage.Severity severity, String summary) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, summary));
    }
}
