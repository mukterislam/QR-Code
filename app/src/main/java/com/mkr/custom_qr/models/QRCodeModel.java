package com.mkr.custom_qr.models;

import java.io.Serializable;

/**
 * Domain model representing a generated or saved QR Code.
 */
public class QRCodeModel implements Serializable {
    private long id;
    private String title;
    private String description;
    private String qrType; // text, url, wifi, contact, phone, email, sms, whatsapp, location, private_password, private_paired
    private String rawPayload; // Encrypted envelope string if private, plain text if standard
    private boolean isPrivate;
    private String securityMode; // "none", "password", "paired"
    private String pairedDeviceId;
    private int fgColor;
    private int bgColor;
    private int size;
    private String errorCorrectionLevel; // L, M, Q, H
    private long createdAt;
    private boolean isFavorite;

    public QRCodeModel() {
        this.createdAt = System.currentTimeMillis();
        this.fgColor = 0xFF000000;
        this.bgColor = 0xFFFFFFFF;
        this.size = 512;
        this.errorCorrectionLevel = "M";
        this.securityMode = "none";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQrType() { return qrType; }
    public void setQrType(String qrType) { this.qrType = qrType; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public String getSecurityMode() { return securityMode; }
    public void setSecurityMode(String securityMode) { this.securityMode = securityMode; }

    public String getPairedDeviceId() { return pairedDeviceId; }
    public void setPairedDeviceId(String pairedDeviceId) { this.pairedDeviceId = pairedDeviceId; }

    public int getFgColor() { return fgColor; }
    public void setFgColor(int fgColor) { this.fgColor = fgColor; }

    public int getBgColor() { return bgColor; }
    public void setBgColor(int bgColor) { this.bgColor = bgColor; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getErrorCorrectionLevel() { return errorCorrectionLevel; }
    public void setErrorCorrectionLevel(String errorCorrectionLevel) { this.errorCorrectionLevel = errorCorrectionLevel; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
