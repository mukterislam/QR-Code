package com.mkr.custom_qr.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.mkr.custom_qr.models.PairedDeviceModel;
import com.mkr.custom_qr.models.QRCodeModel;
import com.mkr.custom_qr.models.ScanHistoryModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Repository for local SQLite operations and Preferences.
 */
public class QRRepository {

    private final QRDatabaseHelper dbHelper;
    private final SharedPreferences prefs;

    private static final String PREF_DARK_MODE = "pref_dark_mode";
    private static final String PREF_CLIPBOARD_PRIVACY = "pref_clipboard_privacy";

    public QRRepository(Context context) {
        this.dbHelper = new QRDatabaseHelper(context);
        this.prefs = context.getSharedPreferences("cqr_prefs", Context.MODE_PRIVATE);
    }

    // --- QR CODES ---

    public long insertQRCode(QRCodeModel model) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QRDatabaseHelper.COL_QR_TITLE, model.getTitle());
        cv.put(QRDatabaseHelper.COL_QR_DESCRIPTION, model.getDescription());
        cv.put(QRDatabaseHelper.COL_QR_TYPE, model.getQrType());
        cv.put(QRDatabaseHelper.COL_QR_RAW_PAYLOAD, model.getRawPayload());
        cv.put(QRDatabaseHelper.COL_QR_IS_PRIVATE, model.isPrivate() ? 1 : 0);
        cv.put(QRDatabaseHelper.COL_QR_SECURITY_MODE, model.getSecurityMode());
        cv.put(QRDatabaseHelper.COL_QR_PAIRED_DEVICE_ID, model.getPairedDeviceId());
        cv.put(QRDatabaseHelper.COL_QR_FG_COLOR, model.getFgColor());
        cv.put(QRDatabaseHelper.COL_QR_BG_COLOR, model.getBgColor());
        cv.put(QRDatabaseHelper.COL_QR_SIZE, model.getSize());
        cv.put(QRDatabaseHelper.COL_QR_ECC, model.getErrorCorrectionLevel());
        cv.put(QRDatabaseHelper.COL_QR_CREATED_AT, model.getCreatedAt());
        cv.put(QRDatabaseHelper.COL_QR_IS_FAVORITE, model.isFavorite() ? 1 : 0);

        long id = db.insert(QRDatabaseHelper.TABLE_QR_CODES, null, cv);
        model.setId(id);
        return id;
    }

    public List<QRCodeModel> getAllQRCodes(String filter) {
        List<QRCodeModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if ("normal".equalsIgnoreCase(filter)) {
            selection = QRDatabaseHelper.COL_QR_IS_PRIVATE + " = 0";
        } else if ("private".equalsIgnoreCase(filter)) {
            selection = QRDatabaseHelper.COL_QR_IS_PRIVATE + " = 1";
        } else if ("favorites".equalsIgnoreCase(filter)) {
            selection = QRDatabaseHelper.COL_QR_IS_FAVORITE + " = 1";
        }

        Cursor c = db.query(
                QRDatabaseHelper.TABLE_QR_CODES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                QRDatabaseHelper.COL_QR_CREATED_AT + " DESC"
        );

        if (c != null) {
            while (c.moveToNext()) {
                QRCodeModel model = cursorToQR(c);
                list.add(model);
            }
            c.close();
        }
        return list;
    }

    public void deleteQRCode(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(QRDatabaseHelper.TABLE_QR_CODES, QRDatabaseHelper.COL_QR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void toggleFavoriteQR(long id, boolean currentFavorite) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QRDatabaseHelper.COL_QR_IS_FAVORITE, currentFavorite ? 0 : 1);
        db.update(QRDatabaseHelper.TABLE_QR_CODES, cv, QRDatabaseHelper.COL_QR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private QRCodeModel cursorToQR(Cursor c) {
        QRCodeModel model = new QRCodeModel();
        model.setId(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_ID)));
        model.setTitle(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_TITLE)));
        model.setDescription(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_DESCRIPTION)));
        model.setQrType(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_TYPE)));
        model.setRawPayload(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_RAW_PAYLOAD)));
        model.setPrivate(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_IS_PRIVATE)) == 1);
        model.setSecurityMode(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_SECURITY_MODE)));
        model.setPairedDeviceId(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_PAIRED_DEVICE_ID)));
        model.setFgColor(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_FG_COLOR)));
        model.setBgColor(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_BG_COLOR)));
        model.setSize(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_SIZE)));
        model.setErrorCorrectionLevel(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_ECC)));
        model.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_CREATED_AT)));
        model.setFavorite(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_QR_IS_FAVORITE)) == 1);
        return model;
    }

    // --- SCAN HISTORY ---

    public long insertScanHistory(ScanHistoryModel history) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QRDatabaseHelper.COL_HIST_TITLE, history.getTitle());
        cv.put(QRDatabaseHelper.COL_HIST_RAW_CONTENT, history.getRawContent());
        cv.put(QRDatabaseHelper.COL_HIST_DETECTED_TYPE, history.getDetectedType());
        cv.put(QRDatabaseHelper.COL_HIST_IS_PRIVATE, history.isPrivate() ? 1 : 0);
        cv.put(QRDatabaseHelper.COL_HIST_SCANNED_AT, history.getScannedAt());
        cv.put(QRDatabaseHelper.COL_HIST_IS_FAVORITE, history.isFavorite() ? 1 : 0);

        long id = db.insert(QRDatabaseHelper.TABLE_SCAN_HISTORY, null, cv);
        history.setId(id);
        return id;
    }

    public List<ScanHistoryModel> getRecentScanHistory(int limit) {
        List<ScanHistoryModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                QRDatabaseHelper.TABLE_SCAN_HISTORY,
                null,
                null,
                null,
                null,
                null,
                QRDatabaseHelper.COL_HIST_SCANNED_AT + " DESC",
                limit > 0 ? String.valueOf(limit) : null
        );

        if (c != null) {
            while (c.moveToNext()) {
                ScanHistoryModel model = new ScanHistoryModel();
                model.setId(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_ID)));
                model.setTitle(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_TITLE)));
                model.setRawContent(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_RAW_CONTENT)));
                model.setDetectedType(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_DETECTED_TYPE)));
                model.setPrivate(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_IS_PRIVATE)) == 1);
                model.setScannedAt(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_SCANNED_AT)));
                model.setFavorite(c.getInt(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_HIST_IS_FAVORITE)) == 1);
                list.add(model);
            }
            c.close();
        }
        return list;
    }

    public void clearHistory() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(QRDatabaseHelper.TABLE_SCAN_HISTORY, null, null);
    }

    // --- PAIRED DEVICES ---

    public long insertPairedDevice(PairedDeviceModel device) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QRDatabaseHelper.COL_DEV_NAME, device.getDeviceName());
        cv.put(QRDatabaseHelper.COL_DEV_FINGERPRINT, device.getFingerprint());
        cv.put(QRDatabaseHelper.COL_DEV_PUBLIC_KEY, device.getPublicKeyBase64());
        cv.put(QRDatabaseHelper.COL_DEV_SECRET_HASH, device.getSharedSecretHash());
        cv.put(QRDatabaseHelper.COL_DEV_PAIRED_AT, device.getPairedAt());
        cv.put(QRDatabaseHelper.COL_DEV_LAST_USED, device.getLastUsedAt());
        cv.put(QRDatabaseHelper.COL_DEV_STATUS, device.getStatus());

        long id = db.insertWithOnConflict(
                QRDatabaseHelper.TABLE_PAIRED_DEVICES,
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        device.setId(id);
        return id;
    }

    public List<PairedDeviceModel> getAllPairedDevices() {
        List<PairedDeviceModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                QRDatabaseHelper.TABLE_PAIRED_DEVICES,
                null,
                null,
                null,
                null,
                null,
                QRDatabaseHelper.COL_DEV_PAIRED_AT + " DESC"
        );

        if (c != null) {
            while (c.moveToNext()) {
                PairedDeviceModel model = new PairedDeviceModel();
                model.setId(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_ID)));
                model.setDeviceName(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_NAME)));
                model.setFingerprint(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_FINGERPRINT)));
                model.setPublicKeyBase64(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_PUBLIC_KEY)));
                model.setSharedSecretHash(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_SECRET_HASH)));
                model.setPairedAt(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_PAIRED_AT)));
                model.setLastUsedAt(c.getLong(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_LAST_USED)));
                model.setStatus(c.getString(c.getColumnIndexOrThrow(QRDatabaseHelper.COL_DEV_STATUS)));
                list.add(model);
            }
            c.close();
        }
        return list;
    }

    public void unpairDevice(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(QRDatabaseHelper.TABLE_PAIRED_DEVICES, QRDatabaseHelper.COL_DEV_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- PREFERENCES ---

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(PREF_DARK_MODE, true); // default true for security theme
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_DARK_MODE, enabled).apply();
    }

    public boolean isClipboardPrivacyEnabled() {
        return prefs.getBoolean(PREF_CLIPBOARD_PRIVACY, true);
    }

    public void setClipboardPrivacyEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_CLIPBOARD_PRIVACY, enabled).apply();
    }
}
