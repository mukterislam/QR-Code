package com.mkr.custom_qr.models;

import java.io.Serializable;

/**
 * Domain model representing an authorized paired Android device.
 */
public class PairedDeviceModel implements Serializable {
    private long id;
    private String deviceName;
    private String fingerprint; // SHA-256 Hex fingerprint of device's public key
    private String publicKeyBase64; // X.509 Encoded SubjectPublicKeyInfo
    private String sharedSecretHash;
    private long pairedAt;
    private long lastUsedAt;
    private String status; // "active", "revoked"

    public PairedDeviceModel() {
        this.pairedAt = System.currentTimeMillis();
        this.lastUsedAt = System.currentTimeMillis();
        this.status = "active";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public String getPublicKeyBase64() { return publicKeyBase64; }
    public void setPublicKeyBase64(String publicKeyBase64) { this.publicKeyBase64 = publicKeyBase64; }

    public String getSharedSecretHash() { return sharedSecretHash; }
    public void setSharedSecretHash(String sharedSecretHash) { this.sharedSecretHash = sharedSecretHash; }

    public long getPairedAt() { return pairedAt; }
    public void setPairedAt(long pairedAt) { this.pairedAt = pairedAt; }

    public long getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(long lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
