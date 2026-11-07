package com.mkr.custom_qr.encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hardware-backed KeyStore helper.
 * Uses AndroidKeyStore (API 23+) or secure obfuscated storage for API 22.
 */
public class SecureKeyStore {

    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String MASTER_KEY_ALIAS = "CustomQR_MasterKey";
    private static final String PREF_NAME = "cqr_secure_keys";
    private static final String PREF_FALLBACK_KEY = "fallback_master_key";

    public static SecretKey getOrCreateMasterKey(Context context) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            if (keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                return (SecretKey) keyStore.getKey(MASTER_KEY_ALIAS, null);
            }

            // Generate in hardware keystore
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());
            return keyGenerator.generateKey();
        } else {
            // API 22 Compatibility fallback
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String encoded = prefs.getString(PREF_FALLBACK_KEY, null);
            if (encoded != null) {
                byte[] raw = Base64.decode(encoded, Base64.NO_WRAP);
                return new SecretKeySpec(raw, "AES");
            }

            byte[] newKey = new byte[32];
            new java.security.SecureRandom().nextBytes(newKey);
            prefs.edit().putString(PREF_FALLBACK_KEY, Base64.encodeToString(newKey, Base64.NO_WRAP)).apply();
            return new SecretKeySpec(newKey, "AES");
        }
    }
}
