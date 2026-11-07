package com.mkr.custom_qr.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Security and Privacy utilities.
 */
public class SecurityUtils {

    /**
     * Copy text to clipboard and schedule auto-wipe after 30 seconds to protect private decrypted data.
     */
    public static void copyWithAutoWipe(Context context, String label, String text, boolean autoWipe) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return;

        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard" + (autoWipe ? " (Auto-clears in 30s)" : ""), Toast.LENGTH_SHORT).show();

        if (autoWipe) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    ClipData emptyClip = ClipData.newPlainText("", "");
                    clipboard.setPrimaryClip(emptyClip);
                } catch (Exception ignored) {}
            }, 30000);
        }
    }
}
