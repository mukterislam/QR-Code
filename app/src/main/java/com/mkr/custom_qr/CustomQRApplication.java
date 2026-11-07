package com.mkr.custom_qr;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.mkr.custom_qr.database.QRRepository;

/**
 * Custom QR Application class.
 * Initializes database repository and default application preferences offline.
 */
public class CustomQRApplication extends Application {

    private static CustomQRApplication instance;
    private QRRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        repository = new QRRepository(this);

        // Apply saved theme (Default is Dark Theme for security aesthetics)
        boolean isDarkMode = repository.isDarkModeEnabled();
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static CustomQRApplication getInstance() {
        return instance;
    }

    public QRRepository getRepository() {
        return repository;
    }
}
