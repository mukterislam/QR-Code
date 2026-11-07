package com.mkr.custom_qr.qr;

import com.mkr.custom_qr.models.PairingEnvelope;
import com.mkr.custom_qr.models.QREncryptedEnvelope;

/**
 * Standard QR Payload generator and parser.
 */
public class QRPayloadManager {

    public static String buildWifiPayload(String ssid, String password, String type, boolean hidden) {
        return "WIFI:S:" + ssid + ";T:" + (type != null ? type : "WPA") + ";P:" + (password != null ? password : "") + ";H:" + (hidden ? "true" : "false") + ";;";
    }

    public static String buildContactPayload(String name, String phone, String email, String org) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD\n");
        sb.append("VERSION:3.0\n");
        if (name != null) sb.append("FN:").append(name).append("\n");
        if (phone != null) sb.append("TEL:").append(phone).append("\n");
        if (email != null) sb.append("EMAIL:").append(email).append("\n");
        if (org != null) sb.append("ORG:").append(org).append("\n");
        sb.append("END:VCARD");
        return sb.toString();
    }

    public static String buildPhonePayload(String phone) {
        return "tel:" + (phone != null ? phone.trim() : "");
    }

    public static String buildEmailPayload(String email, String subject, String body) {
        StringBuilder sb = new StringBuilder("mailto:").append(email != null ? email.trim() : "");
        boolean first = true;
        if (subject != null && !subject.isEmpty()) {
            sb.append(first ? "?" : "&").append("subject=").append(android.net.Uri.encode(subject));
            first = false;
        }
        if (body != null && !body.isEmpty()) {
            sb.append(first ? "?" : "&").append("body=").append(android.net.Uri.encode(body));
        }
        return sb.toString();
    }

    public static String buildSmsPayload(String phone, String message) {
        return "smsto:" + (phone != null ? phone.trim() : "") + ":" + (message != null ? message : "");
    }

    public static String buildWhatsAppPayload(String phone, String message) {
        String cleanPhone = phone != null ? phone.replaceAll("[^0-9]", "") : "";
        String url = "https://wa.me/" + cleanPhone;
        if (message != null && !message.isEmpty()) {
            url += "?text=" + android.net.Uri.encode(message);
        }
        return url;
    }

    public static String buildLocationPayload(double latitude, double longitude) {
        return "geo:" + latitude + "," + longitude;
    }

    /**
     * Detect the type of a scanned QR payload string.
     */
    public static String detectPayloadType(String raw) {
        if (raw == null) return "text";
        if (QREncryptedEnvelope.isEncryptedPayload(raw)) {
            return "private_encrypted";
        }
        if (PairingEnvelope.isPairingPayload(raw)) {
            return "pairing_payload";
        }
        String lower = raw.trim().toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return "url";
        }
        if (raw.startsWith("WIFI:")) {
            return "wifi";
        }
        if (raw.startsWith("BEGIN:VCARD")) {
            return "contact";
        }
        if (lower.startsWith("tel:")) {
            return "phone";
        }
        if (lower.startsWith("mailto:")) {
            return "email";
        }
        if (lower.startsWith("smsto:") || lower.startsWith("sms:")) {
            return "sms";
        }
        if (lower.startsWith("geo:")) {
            return "location";
        }
        return "text";
    }
}
