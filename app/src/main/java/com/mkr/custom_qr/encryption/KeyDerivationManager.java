package com.mkr.custom_qr.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Key Derivation Manager for Custom QR.
 * Provides PBKDF2-HMAC-SHA256 password key derivation and HKDF-SHA256 extract/expand.
 */
public class KeyDerivationManager {

    public static final int PBKDF2_ITERATIONS = 100000;
    public static final int KEY_LENGTH_BITS = 256;
    public static final int SALT_LENGTH_BYTES = 16;
    public static final int IV_LENGTH_BYTES = 12;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static byte[] generateRandomSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] generateRandomIV() {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    /**
     * Derive 256-bit AES key from password and salt using PBKDF2-HMAC-SHA256.
     */
    public static SecretKey deriveKeyPBKDF2(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            // Fallback for older Android engines
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        }
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * HKDF-SHA256 Key Derivation from shared secret.
     */
    public static SecretKey deriveHKDFKey(byte[] ikm, byte[] salt, byte[] info, int lengthBytes)
            throws Exception {
        if (salt == null || salt.length == 0) {
            salt = new byte[32]; // zero salt
        }

        // 1. HKDF-Extract: PRK = HMAC-Hash(salt, IKM)
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(ikm);

        // 2. HKDF-Expand: OKM = HMAC-Hash(PRK, info || 0x01)
        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        if (info != null) {
            mac.update(info);
        }
        mac.update((byte) 0x01);
        byte[] okmFull = mac.doFinal();

        byte[] resultKey = new byte[lengthBytes];
        System.arraycopy(okmFull, 0, resultKey, 0, lengthBytes);
        return new SecretKeySpec(resultKey, "AES");
    }

    /**
     * Compute SHA-256 Key Fingerprint in format XX:XX:XX:XX:...
     */
    public static String computeFingerprint(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(hash.length, 12);
            for (int i = 0; i < limit; i++) {
                sb.append(String.format("%02X", hash[i]));
                if (i < limit - 1) {
                    sb.append(":");
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "UNKNOWN";
        }
    }
}
