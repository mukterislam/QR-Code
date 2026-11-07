package com.mkr.custom_qr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite Database Helper for Custom QR.
 * Manages tables for saved QR codes, scan history, paired devices, and settings.
 * Sensitive plaintext keys and passwords are NEVER persisted.
 */
public class QRDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "custom_qr.db";
    public static final int DATABASE_VERSION = 1;

    // Tables
    public static final String TABLE_QR_CODES = "qr_codes";
    public static final String TABLE_SCAN_HISTORY = "scan_history";
    public static final String TABLE_PAIRED_DEVICES = "paired_devices";
    public static final String TABLE_APP_SETTINGS = "app_settings";

    // Columns: qr_codes
    public static final String COL_QR_ID = "id";
    public static final String COL_QR_TITLE = "title";
    public static final String COL_QR_DESCRIPTION = "description";
    public static final String COL_QR_TYPE = "qr_type";
    public static final String COL_QR_RAW_PAYLOAD = "raw_payload";
    public static final String COL_QR_IS_PRIVATE = "is_private";
    public static final String COL_QR_SECURITY_MODE = "security_mode";
    public static final String COL_QR_PAIRED_DEVICE_ID = "paired_device_id";
    public static final String COL_QR_FG_COLOR = "fg_color";
    public static final String COL_QR_BG_COLOR = "bg_color";
    public static final String COL_QR_SIZE = "size";
    public static final String COL_QR_ECC = "error_correction";
    public static final String COL_QR_CREATED_AT = "created_at";
    public static final String COL_QR_IS_FAVORITE = "is_favorite";

    // Columns: scan_history
    public static final String COL_HIST_ID = "id";
    public static final String COL_HIST_TITLE = "title";
    public static final String COL_HIST_RAW_CONTENT = "raw_content";
    public static final String COL_HIST_DETECTED_TYPE = "detected_type";
    public static final String COL_HIST_IS_PRIVATE = "is_private";
    public static final String COL_HIST_SCANNED_AT = "scanned_at";
    public static final String COL_HIST_IS_FAVORITE = "is_favorite";

    // Columns: paired_devices
    public static final String COL_DEV_ID = "id";
    public static final String COL_DEV_NAME = "device_name";
    public static final String COL_DEV_FINGERPRINT = "fingerprint";
    public static final String COL_DEV_PUBLIC_KEY = "public_key";
    public static final String COL_DEV_SECRET_HASH = "shared_secret_hash";
    public static final String COL_DEV_PAIRED_AT = "paired_at";
    public static final String COL_DEV_LAST_USED = "last_used_at";
    public static final String COL_DEV_STATUS = "status";

    public QRDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create qr_codes table
        String createQRCodes = "CREATE TABLE " + TABLE_QR_CODES + " ("
                + COL_QR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_QR_TITLE + " TEXT, "
                + COL_QR_DESCRIPTION + " TEXT, "
                + COL_QR_TYPE + " TEXT, "
                + COL_QR_RAW_PAYLOAD + " TEXT, "
                + COL_QR_IS_PRIVATE + " INTEGER, "
                + COL_QR_SECURITY_MODE + " TEXT, "
                + COL_QR_PAIRED_DEVICE_ID + " TEXT, "
                + COL_QR_FG_COLOR + " INTEGER, "
                + COL_QR_BG_COLOR + " INTEGER, "
                + COL_QR_SIZE + " INTEGER, "
                + COL_QR_ECC + " TEXT, "
                + COL_QR_CREATED_AT + " INTEGER, "
                + COL_QR_IS_FAVORITE + " INTEGER)";
        db.execSQL(createQRCodes);

        // Create scan_history table
        String createScanHistory = "CREATE TABLE " + TABLE_SCAN_HISTORY + " ("
                + COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_HIST_TITLE + " TEXT, "
                + COL_HIST_RAW_CONTENT + " TEXT, "
                + COL_HIST_DETECTED_TYPE + " TEXT, "
                + COL_HIST_IS_PRIVATE + " INTEGER, "
                + COL_HIST_SCANNED_AT + " INTEGER, "
                + COL_HIST_IS_FAVORITE + " INTEGER)";
        db.execSQL(createScanHistory);

        // Create paired_devices table
        String createPairedDevices = "CREATE TABLE " + TABLE_PAIRED_DEVICES + " ("
                + COL_DEV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_DEV_NAME + " TEXT, "
                + COL_DEV_FINGERPRINT + " TEXT UNIQUE, "
                + COL_DEV_PUBLIC_KEY + " TEXT, "
                + COL_DEV_SECRET_HASH + " TEXT, "
                + COL_DEV_PAIRED_AT + " INTEGER, "
                + COL_DEV_LAST_USED + " INTEGER, "
                + COL_DEV_STATUS + " TEXT)";
        db.execSQL(createPairedDevices);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QR_CODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCAN_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAIRED_DEVICES);
        onCreate(db);
    }
}
