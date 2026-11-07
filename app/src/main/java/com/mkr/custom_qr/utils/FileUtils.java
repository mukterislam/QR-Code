package com.mkr.custom_qr.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * File, MediaStore, and sharing utilities for QR images.
 */
public class FileUtils {

    /**
     * Save generated QR Bitmap to Gallery/Pictures using modern MediaStore (API 29+) or legacy external storage.
     */
    public static Uri saveBitmapToGallery(Context context, Bitmap bitmap, String title) throws Exception {
        String filename = "CustomQR_" + System.currentTimeMillis() + ".png";
        OutputStream fos;
        Uri imageUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CustomQR");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri == null) throw new Exception("Failed to create MediaStore entry.");

            fos = context.getContentResolver().openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (fos != null) fos.close();

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            context.getContentResolver().update(imageUri, values, null, null);
        } else {
            // Legacy Storage for API 22 - 28
            File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File qrDir = new File(imagesDir, "CustomQR");
            if (!qrDir.exists()) qrDir.mkdirs();

            File imageFile = new File(qrDir, filename);
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        return imageUri;
    }

    /**
     * Share QR Bitmap via Android Intent.
     */
    public static void shareBitmap(Context context, Bitmap bitmap, String subject) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File stream = new File(cachePath, "share_qr.png");
            FileOutputStream fos = new FileOutputStream(stream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", stream);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject != null ? subject : "Custom QR Code");
                context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
