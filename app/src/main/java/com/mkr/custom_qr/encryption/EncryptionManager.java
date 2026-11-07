package com.mkr.custom_qr.encryption;

import android.util.Base64;
import com.mkr.custom_qr.models.QREncryptedEnvelope;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Enterprise Cryptographic Engine for Private QR Codes.
 * Implements AES-256-GCM authenticated encryption with PBKDF2-HMAC-SHA256.
 * Zero-leak policy: Any authentication tag mismatch or wrong password cleanly fails
 * without leaking partial plaintexts.
 */
public class EncryptionManager {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128; // 16 bytes auth tag

    /**
     * Encrypt plaintext into a versioned Private QR Payload Envelope using password.
     */
    public static String encryptWithPassword(String plainText, String password) throws Exception {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Encryption password cannot be empty.");
        }
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Payload content cannot be empty.");
        }

        // 1. Generate Secure Random Salt and IV
        byte[] salt = KeyDerivationManager.generateRandomSalt();
        byte[] iv = KeyDerivationManager.generateRandomIV();

        // 2. Derive AES-256 Key
        char[] passwordChars = password.toCharArray();
        SecretKey secretKey;
        try {
            secretKey = KeyDerivationManager.deriveKeyPBKDF2(passwordChars, salt);
        } finally {
            // Clean sensitive password from memory
            Arrays.fill(passwordChars, '\0');
        }

        // 3. Encrypt with AES-256-GCM
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherTextWithTag = cipher.doFinal(plainBytes);

        // Separate Ciphertext and Auth Tag (Last 16 bytes in Java GCM)
        int tagLen = GCM_TAG_LENGTH_BITS / 8;
        int ctLen = cipherTextWithTag.length - tagLen;
        byte[] ctBytes = new byte[ctLen];
        byte[] tagBytes = new byte[tagLen];

        System.arraycopy(cipherTextWithTag, 0, ctBytes, 0, ctLen);
        System.arraycopy(cipherTextWithTag, ctLen, tagBytes, 0, tagLen);

        // 4. Wrap into QREncryptedEnvelope
        QREncryptedEnvelope envelope = new QREncryptedEnvelope();
        envelope.setApp("Custom_QR");
        envelope.setV(1);
        envelope.setType("password");
        envelope.setAlg("AES-256-GCM");
        envelope.setKdf("PBKDF2-HMAC-SHA256");
        envelope.setSalt(Base64.encodeToString(salt, Base64.NO_WRAP));
        envelope.setIv(Base64.encodeToString(iv, Base64.NO_WRAP));
        envelope.setTag(Base64.encodeToString(tagBytes, Base64.NO_WRAP));
        envelope.setCt(Base64.encodeToString(ctBytes, Base64.NO_WRAP));

        return envelope.toPayloadString();
    }

    /**
     * Decrypt private payload string using password.
     */
    public static String decryptWithPassword(String payloadString, String password) throws Exception {
        if (!QREncryptedEnvelope.isEncryptedPayload(payloadString)) {
            throw new IllegalArgumentException("Invalid Private QR code format.");
        }

        String jsonString = payloadString.substring(QREncryptedEnvelope.PREFIX.length());
        QREncryptedEnvelope envelope = QREncryptedEnvelope.fromJson(jsonString);

        if (!"AES-256-GCM".equals(envelope.getAlg())) {
            throw new GeneralSecurityException("Unsupported encryption algorithm: " + envelope.getAlg());
        }

        byte[] salt = Base64.decode(envelope.getSalt(), Base64.NO_WRAP);
        byte[] iv = Base64.decode(envelope.getIv(), Base64.NO_WRAP);
        byte[] ctBytes = Base64.decode(envelope.getCt(), Base64.NO_WRAP);
        byte[] tagBytes = Base64.decode(envelope.getTag(), Base64.NO_WRAP);

        // Combine ciphertext and authentication tag for GCM decryption
        byte[] combined = new byte[ctBytes.length + tagBytes.length];
        System.arraycopy(ctBytes, 0, combined, 0, ctBytes.length);
        System.arraycopy(tagBytes, 0, combined, ctBytes.length, tagBytes.length);

        char[] passwordChars = password.toCharArray();
        SecretKey secretKey;
        try {
            secretKey = KeyDerivationManager.deriveKeyPBKDF2(passwordChars, salt);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] decrypted = cipher.doFinal(combined);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            // Authentication Failure (wrong password or altered ciphertext/tag)
            throw new GeneralSecurityException("Authentication failure: Incorrect password or corrupted payload.", e);
        }
    }

    /**
     * Encrypt data with a pre-shared or ECDH derived SecretKey for a paired device.
     */
    public static String encryptWithSecretKey(String plainText, SecretKey secretKey, String recipientFingerprint, String ephemeralPubKeyBase64)
            throws Exception {
        byte[] iv = KeyDerivationManager.generateRandomIV();

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherTextWithTag = cipher.doFinal(plainBytes);

        int tagLen = GCM_TAG_LENGTH_BITS / 8;
        int ctLen = cipherTextWithTag.length - tagLen;
        byte[] ctBytes = new byte[ctLen];
        byte[] tagBytes = new byte[tagLen];

        System.arraycopy(cipherTextWithTag, 0, ctBytes, 0, ctLen);
        System.arraycopy(cipherTextWithTag, ctLen, tagBytes, 0, tagLen);

        QREncryptedEnvelope envelope = new QREncryptedEnvelope();
        envelope.setApp("Custom_QR");
        envelope.setV(1);
        envelope.setType("paired");
        envelope.setAlg("AES-256-GCM");
        envelope.setKdf("ECDH-HKDF-SHA256");
        envelope.setSalt("");
        envelope.setIv(Base64.encodeToString(iv, Base64.NO_WRAP));
        envelope.setTag(Base64.encodeToString(tagBytes, Base64.NO_WRAP));
        envelope.setCt(Base64.encodeToString(ctBytes, Base64.NO_WRAP));
        envelope.setRecipientFingerprint(recipientFingerprint);
        envelope.setEphemeralPublicKey(ephemeralPubKeyBase64);

        return envelope.toPayloadString();
    }

    /**
     * Decrypt data with SecretKey for a paired device.
     */
    public static String decryptWithSecretKey(QREncryptedEnvelope envelope, SecretKey secretKey)
            throws Exception {
        byte[] iv = Base64.decode(envelope.getIv(), Base64.NO_WRAP);
        byte[] ctBytes = Base64.decode(envelope.getCt(), Base64.NO_WRAP);
        byte[] tagBytes = Base64.decode(envelope.getTag(), Base64.NO_WRAP);

        byte[] combined = new byte[ctBytes.length + tagBytes.length];
        System.arraycopy(ctBytes, 0, combined, 0, ctBytes.length);
        System.arraycopy(tagBytes, 0, combined, ctBytes.length, tagBytes.length);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] decrypted = cipher.doFinal(combined);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
