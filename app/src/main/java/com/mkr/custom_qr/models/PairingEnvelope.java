package com.mkr.custom_qr.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

/**
 * Versioned Cryptographic Envelope for Secure Device Pairing.
 * Prefix: CQR_PAIR:
 */
public class PairingEnvelope implements Serializable {
    public static final String PREFIX = "CQR_PAIR:";

    private String app = "Custom_QR_Pair";
    private int v = 1;
    private String type = "pairing_request"; // "pairing_request" or "pairing_response"
    private String sessionId;
    private String deviceName;
    private String publicKeyBase64;
    private String fingerprint;
    private long timestamp;

    public PairingEnvelope() {
        this.timestamp = System.currentTimeMillis();
    }

    public String toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("app", app);
            obj.put("v", v);
            obj.put("type", type);
            obj.put("sessionId", sessionId);
            obj.put("deviceName", deviceName);
            obj.put("publicKey", publicKeyBase64);
            obj.put("fingerprint", fingerprint);
            obj.put("timestamp", timestamp);
            return obj.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static PairingEnvelope fromJson(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        PairingEnvelope envelope = new PairingEnvelope();
        envelope.app = obj.optString("app", "Custom_QR_Pair");
        envelope.v = obj.optInt("v", 1);
        envelope.type = obj.optString("type", "pairing_request");
        envelope.sessionId = obj.optString("sessionId", "");
        envelope.deviceName = obj.optString("deviceName", "Android Device");
        envelope.publicKeyBase64 = obj.optString("publicKey", "");
        envelope.fingerprint = obj.optString("fingerprint", "");
        envelope.timestamp = obj.optLong("timestamp", System.currentTimeMillis());
        return envelope;
    }

    public static boolean isPairingPayload(String payload) {
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

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getPublicKeyBase64() { return publicKeyBase64; }
    public void setPublicKeyBase64(String publicKeyBase64) { this.publicKeyBase64 = publicKeyBase64; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
