package com.mkr.custom_qr.pairing;

import android.os.Build;
import android.util.Base64;
import com.mkr.custom_qr.encryption.KeyDerivationManager;
import com.mkr.custom_qr.models.PairedDeviceModel;
import com.mkr.custom_qr.models.PairingEnvelope;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

/**
 * Secure Device Pairing Engine using ECDH (NIST P-256) and HKDF-SHA256.
 * Zero-cloud offline peer-to-peer device pairing via optical QR exchange.
 */
public class DevicePairingManager {

    private static final String EC_ALGORITHM = "EC";
    private static final String ECDH_ALGORITHM = "ECDH";
    private static final String EC_CURVE = "secp256r1";

    public static class EphemeralSession {
        public String sessionId;
        public KeyPair keyPair;
        public String fingerprint;
        public long createdAt;
    }

    /**
     * Generate an ephemeral NIST P-256 ECDH KeyPair.
     */
    public static KeyPair generateECDHKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(EC_ALGORITHM);
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(EC_CURVE);
        kpg.initialize(ecSpec);
        return kpg.generateKeyPair();
    }

    /**
     * Start an Initiator Pairing Session.
     */
    public static EphemeralSession createInitiatorSession() throws Exception {
        EphemeralSession session = new EphemeralSession();
        session.sessionId = UUID.randomUUID().toString().substring(0, 8);
        session.keyPair = generateECDHKeyPair();
        session.createdAt = System.currentTimeMillis();

        byte[] pubBytes = session.keyPair.getPublic().getEncoded();
        session.fingerprint = KeyDerivationManager.computeFingerprint(pubBytes);
        return session;
    }

    /**
     * Build Pairing Request Envelope for Device A.
     */
    public static PairingEnvelope buildPairingRequest(EphemeralSession session) {
        PairingEnvelope env = new PairingEnvelope();
        env.setType("pairing_request");
        env.setSessionId(session.sessionId);
        env.setDeviceName(Build.MANUFACTURER + " " + Build.MODEL);
        env.setPublicKeyBase64(Base64.encodeToString(session.keyPair.getPublic().getEncoded(), Base64.NO_WRAP));
        env.setFingerprint(session.fingerprint);
        return env;
    }

    /**
     * Process Pairing Request on Device B and build Pairing Response Envelope.
     */
    public static PairingEnvelope handlePairingRequestOnDeviceB(PairingEnvelope requestEnv, KeyPair deviceBKeyPair)
            throws Exception {
        if (!"pairing_request".equals(requestEnv.getType())) {
            throw new IllegalArgumentException("Expected pairing_request type.");
        }

        // Derive Shared Secret on Device B
        PublicKey deviceAPublicKey = decodePublicKey(requestEnv.getPublicKeyBase64());
        KeyAgreement ka = KeyAgreement.getInstance(ECDH_ALGORITHM);
        ka.init(deviceBKeyPair.getPrivate());
        ka.doPhase(deviceAPublicKey, true);
        byte[] sharedSecret = ka.generateSecret();

        byte[] pubBytes = deviceBKeyPair.getPublic().getEncoded();
        String fingerprint = KeyDerivationManager.computeFingerprint(pubBytes);

        PairingEnvelope responseEnv = new PairingEnvelope();
        responseEnv.setType("pairing_response");
        responseEnv.setSessionId(requestEnv.getSessionId());
        responseEnv.setDeviceName(Build.MANUFACTURER + " " + Build.MODEL);
        responseEnv.setPublicKeyBase64(Base64.encodeToString(pubBytes, Base64.NO_WRAP));
        responseEnv.setFingerprint(fingerprint);
        return responseEnv;
    }

    /**
     * Finalize pairing on Device A after scanning Device B's response.
     */
    public static PairedDeviceModel finalizePairingOnDeviceA(EphemeralSession sessionA, PairingEnvelope responseEnv)
            throws Exception {
        if (!"pairing_response".equals(responseEnv.getType())) {
            throw new IllegalArgumentException("Expected pairing_response type.");
        }
        if (!sessionA.sessionId.equals(responseEnv.getSessionId())) {
            throw new IllegalStateException("Session ID mismatch. Pairing request expired or tampered.");
        }

        PublicKey deviceBPublicKey = decodePublicKey(responseEnv.getPublicKeyBase64());

        // Derive ECDH Shared Secret
        KeyAgreement ka = KeyAgreement.getInstance(ECDH_ALGORITHM);
        ka.init(sessionA.keyPair.getPrivate());
        ka.doPhase(deviceBPublicKey, true);
        byte[] sharedSecret = ka.generateSecret();

        // Derive 256-bit Session Key using HKDF
        SecretKey aesKey = KeyDerivationManager.deriveHKDFKey(
                sharedSecret,
                null,
                "CustomQR_Pairing_v1".getBytes(StandardCharsets.UTF_8),
                32
        );

        PairedDeviceModel device = new PairedDeviceModel();
        device.setDeviceName(responseEnv.getDeviceName());
        device.setFingerprint(responseEnv.getFingerprint());
        device.setPublicKeyBase64(responseEnv.getPublicKeyBase64());
        device.setSharedSecretHash(Base64.encodeToString(aesKey.getEncoded(), Base64.NO_WRAP));
        device.setPairedAt(System.currentTimeMillis());
        device.setLastUsedAt(System.currentTimeMillis());
        device.setStatus("active");

        return device;
    }

    public static PublicKey decodePublicKey(String base64) throws Exception {
        byte[] keyBytes = Base64.decode(base64, Base64.NO_WRAP);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(EC_ALGORITHM);
        return kf.generatePublic(spec);
    }
}
