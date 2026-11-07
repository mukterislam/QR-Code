package com.mkr.custom_qr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        SwitchMaterial switchDark = findViewById(R.id.switch_dark_mode);
        boolean isDark = CustomQRApplication.getInstance().getRepository().isDarkModeEnabled();
        switchDark.setChecked(isDark);
        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CustomQRApplication.getInstance().getRepository().setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        SwitchMaterial switchClipboard = findViewById(R.id.switch_clipboard_privacy);
        switchClipboard.setChecked(CustomQRApplication.getInstance().getRepository().isClipboardPrivacyEnabled());
        switchClipboard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            CustomQRApplication.getInstance().getRepository().setClipboardPrivacyEnabled(isChecked);
            Toast.makeText(this, "Clipboard privacy " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_manage_paired_devices).setOnClickListener(v -> {
            startActivity(new Intent(this, PairedDevicesActivity.class));
        });

        findViewById(R.id.btn_clear_cache).setOnClickListener(v -> {
            CustomQRApplication.getInstance().getRepository().clearHistory();
            Toast.makeText(this, "Scan history and cache cleared", Toast.LENGTH_SHORT).show();
        });
    }
}
