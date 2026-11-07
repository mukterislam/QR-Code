package com.mkr.custom_qr.models;

import java.io.Serializable;

/**
 * Domain model representing a scanned QR code entry.
 */
public class ScanHistoryModel implements Serializable {
    private long id;
    private String title;
    private String rawContent;
    private String detectedType;
    private boolean isPrivate;
    private long scannedAt;
    private boolean isFavorite;

    public ScanHistoryModel() {
        this.scannedAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public String getDetectedType() { return detectedType; }
    public void setDetectedType(String detectedType) { this.detectedType = detectedType; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public long getScannedAt() { return scannedAt; }
    public void setScannedAt(long scannedAt) { this.scannedAt = scannedAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
