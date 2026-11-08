package de.pressflow.service;

/**
 * Wird ausgelöst, wenn ein Schritt im Freigabeprozess nicht erlaubt ist.
 * Die Meldung ist für die Anzeige in der Oberfläche gedacht.
 */
public class WorkflowException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WorkflowException(String message) {
        super(message);
    }
}
