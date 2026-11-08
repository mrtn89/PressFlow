package de.pressflow.model;

/**
 * Statuswerte einer Pressemitteilung im Freigabeprozess.
 * Ablauf: Entwurf → In Prüfung → (Überarbeitung →) Zur Freigabe → Freigegeben
 */
public enum ReleaseStatus {
    ENTWURF("Entwurf"),
    IN_PRUEFUNG("In Prüfung"),
    UEBERARBEITUNG("Überarbeitung"),
    ZUR_FREIGABE("Zur Freigabe"),
    FREIGEGEBEN("Freigegeben");

    // Anzeigetext des Status
    private final String label;

    ReleaseStatus(String label) {
        this.label = label;
    }

    // Liefert den Anzeigetext
    public String getLabel() {
        return label;
    }

    // Inhalte dürfen nur im Entwurf oder während der Überarbeitung geändert werden
    public boolean isEditable() {
        return this == ENTWURF || this == UEBERARBEITUNG;
    }
}
