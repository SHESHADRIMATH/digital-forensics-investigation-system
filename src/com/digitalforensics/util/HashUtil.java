package com.digitalforensics.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * SHA-256 helpers.
 * SHA-256 turns any input into a fixed 64-character hex "fingerprint".
 * Changing even one byte of the input gives a completely different hash,
 * which is how we detect tampered evidence.
 */
public final class HashUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private HashUtil() {
    }

    /** Hashes a file in 8 KB chunks, so large files never have to fit in memory. */
    public static String sha256(Path file) throws IOException {
        MessageDigest digest = newDigest();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public static String sha256(String text) {
        return HexFormat.of().formatHex(newDigest().digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    /** Password hash = SHA-256(salt + password). The salt makes equal passwords hash differently. */
    public static String hashPassword(String salt, String password) {
        return sha256(salt + password);
    }

    public static String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** Compares in constant time so the check does not leak how many characters matched. */
    public static boolean matches(String expectedHex, String actualHex) {
        return MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.US_ASCII),
                actualHex.getBytes(StandardCharsets.US_ASCII));
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available in the JDK", e);
        }
    }
}
