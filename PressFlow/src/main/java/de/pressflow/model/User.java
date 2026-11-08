package de.pressflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

// Benutzer:in der Anwendung mit Rolle und Zugangsdaten
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    // Primärschlüssel
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Anmeldename, muss eindeutig sein
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Vollständiger Name für die Anzeige
    @Column(nullable = false, length = 100)
    private String fullName;

    // Abteilung, z. B. Technik oder Finanzen (wichtig für die Auswahl der Fachprüfung)
    @Column(length = 100)
    private String department;

    // Rolle im Freigabeprozess
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.REDAKTEUR;

    // Gehashtes Passwort, niemals im Klartext speichern
    @Column(nullable = false, length = 200)
    private String passwordHash;

    // Name mit Abteilung, z. B. für Auswahllisten
    public String getNameWithDepartment() {
        if (department == null || department.isBlank()) {
            return fullName;
        }
        return fullName + " (" + department + ")";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
