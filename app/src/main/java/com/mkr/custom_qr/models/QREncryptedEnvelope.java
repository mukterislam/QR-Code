package com.mkr.custom_qr.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * Versioned Cryptographic Payload Envelope for Private QR codes.
 * Prefix: CQR1:
 * Contains:
 * - app: "Custom_QR"
 * - v: 1 (Protocol Version)
 * - type: "password" or "paired"
 * - alg: "AES-256-GCM"
 * - kdf: "PBKDF2-HMAC-SHA256" or "ECDH-HKDF-SHA256"
 * - salt: Base64
 * - iv: Base64
 * - tag: Base64
 * - ct: Base64
 * - recipientFingerprint (optional, paired device mode)
 * - ephemeralPublicKey (optional, paired device mode)
 */
public class QREncryptedEnvelope implements Serializable {
    public static final String PREFIX = "CQR1:";

    private String app = "Custom_QR";
    private int v = 1;
    private String type = "password";
    private String alg = "AES-256-GCM";
    private String kdf = "PBKDF2-HMAC-SHA256";
    private String salt = "";
    private String iv = "";
    private String tag = "";
    private String ct = "";
    private String recipientFingerprint;
    private String ephemeralPublicKey;

    public QREncryptedEnvelope() {}

    public String toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("app", app);
            obj.put("v", v);
            obj.put("type", type);
            obj.put("alg", alg);
            obj.put("kdf", kdf);
            obj.put("salt", salt);
            obj.put("iv", iv);
            obj.put("tag", tag);
            obj.put("ct", ct);
            if (recipientFingerprint != null) {
                obj.put("recipientFingerprint", recipientFingerprint);
            }
            if (ephemeralPublicKey != null) {
                obj.put("ephemeralPublicKey", ephemeralPublicKey);
            }
            return obj.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static QREncryptedEnvelope fromJson(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        QREncryptedEnvelope envelope = new QREncryptedEnvelope();
        envelope.app = obj.optString("app", "Custom_QR");
        envelope.v = obj.optInt("v", 1);
        envelope.type = obj.optString("type", "password");
        envelope.alg = obj.optString("alg", "AES-256-GCM");
        envelope.kdf = obj.optString("kdf", "PBKDF2-HMAC-SHA256");
        envelope.salt = obj.optString("salt", "");
        envelope.iv = obj.optString("iv", "");
        envelope.tag = obj.optString("tag", "");
        envelope.ct = obj.optString("ct", "");
        if (obj.has("recipientFingerprint")) {
            envelope.recipientFingerprint = obj.getString("recipientFingerprint");
        }
        if (obj.has("ephemeralPublicKey")) {
            envelope.ephemeralPublicKey = obj.getString("ephemeralPublicKey");
        }
        return envelope;
    }

    public static boolean isEncryptedPayload(String payload) {
        return payload != null && payload.startsWith(PREFIX);
    }

    public String toPayloadString() {
        return PREFIX + toJson();
    }

    // Getters and Setters
    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    public int getV() { return v; }
    public void setV(int v) { this.v = v; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAlg() { return alg; }
    public void setAlg(String alg) { this.alg = alg; }

    public String getKdf() { return kdf; }
    public void setKdf(String kdf) { this.kdf = kdf; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getCt() { return ct; }
    public void setCt(String ct) { this.ct = ct; }

    public String getRecipientFingerprint() { return recipientFingerprint; }
    public void setRecipientFingerprint(String recipientFingerprint) { this.recipientFingerprint = recipientFingerprint; }

    public String getEphemeralPublicKey() { return ephemeralPublicKey; }
    public void setEphemeralPublicKey(String ephemeralPublicKey) { this.ephemeralPublicKey = ephemeralPublicKey; }
}
