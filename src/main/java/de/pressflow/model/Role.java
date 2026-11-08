package de.pressflow.model;

// Rollen der Benutzer:innen im Freigabeprozess
public enum Role {
    REDAKTEUR("Redakteur:in"),
    FACHPRUEFER("Fachprüfer:in"),
    PRESSESPRECHER("Pressesprecher:in"),
    ADMINISTRATOR("Administrator:in");

    // Anzeigetext der Rolle
    private final String label;

    Role(String label) {
        this.label = label;
    }

    // Liefert den Anzeigetext
    public String getLabel() {
        return label;
    }
}
