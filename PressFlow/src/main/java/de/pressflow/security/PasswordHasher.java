package de.pressflow.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Erzeugt und prüft Passwort-Hashes mit PBKDF2 (im JDK enthalten, keine Zusatzbibliothek nötig).
 * Format des gespeicherten Werts: iterationen:salt:hash (Salt und Hash Base64-kodiert)
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int HASH_LENGTH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Reine Hilfsklasse, keine Instanzen
    private PasswordHasher() {
    }

    // Erzeugt einen Hash mit zufälligem Salt für das übergebene Passwort
    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS);
        return ITERATIONS + ":" + encode(salt) + ":" + encode(hash);
    }

    // Prüft, ob das eingegebene Passwort zum gespeicherten Hash passt
    public static boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
        byte[] actualHash = pbkdf2(password, salt, iterations);
        // Zeitkonstanter Vergleich, damit die Laufzeit nichts über den Hash verrät
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    // Berechnet den eigentlichen PBKDF2-Hash
    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, HASH_LENGTH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Passwort-Hash konnte nicht berechnet werden.", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
