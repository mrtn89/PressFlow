package de.pressflow.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void correctPasswordMatchesHash() {
        String hash = PasswordHasher.hash("geheim123");
        assertTrue(PasswordHasher.matches("geheim123", hash));
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String hash = PasswordHasher.hash("geheim123");
        assertFalse(PasswordHasher.matches("falsch", hash));
    }

    @Test
    void hashDoesNotContainPlainPassword() {
        assertFalse(PasswordHasher.hash("geheim123").contains("geheim123"));
    }

    @Test
    void samePasswordGetsDifferentHashesBecauseOfSalt() {
        assertNotEquals(PasswordHasher.hash("geheim123"), PasswordHasher.hash("geheim123"));
    }

    @Test
    void invalidStoredValueIsRejected() {
        assertFalse(PasswordHasher.matches("geheim123", "kein-gueltiger-hash"));
        assertFalse(PasswordHasher.matches("geheim123", null));
    }
}
