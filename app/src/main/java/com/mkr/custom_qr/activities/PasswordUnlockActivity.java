package com.mkr.custom_qr.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.encryption.EncryptionManager;
import com.mkr.custom_qr.utils.SecurityUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordUnlockActivity extends AppCompatActivity {

    private String rawEncryptedPayload;
    private EditText etPassword;
    private TextView tvError, tvDecrypted;
    private LinearLayout layoutResult;
    private String decryptedText = "";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_unlock);

        Toolbar toolbar = findViewById(R.id.toolbar_unlock);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rawEncryptedPayload = getIntent().getStringExtra("raw_encrypted_payload");
        etPassword = findViewById(R.id.et_unlock_password);
        tvError = findViewById(R.id.tv_unlock_error);
        tvDecrypted = findViewById(R.id.tv_decrypted_content);
        layoutResult = findViewById(R.id.layout_decrypted_result);

        findViewById(R.id.btn_submit_unlock).setOnClickListener(v -> attemptDecryption());

        findViewById(R.id.btn_copy_decrypted).setOnClickListener(v -> {
            if (!decryptedText.isEmpty()) {
                boolean autoWipe = CustomQRApplication.getInstance().getRepository().isClipboardPrivacyEnabled();
                SecurityUtils.copyWithAutoWipe(this, "Decrypted Content", decryptedText, autoWipe);
            }
        });
    }

    private void attemptDecryption() {
        String password = etPassword.getText().toString();
        if (password.isEmpty()) {
            tvError.setText("Please enter the password");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                // Cryptographic AES-256-GCM authentication & decryption
                decryptedText = EncryptionManager.decryptWithPassword(rawEncryptedPayload, password);

                runOnUiThread(() -> {
                    tvDecrypted.setText(decryptedText);
                    layoutResult.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Private QR successfully decrypted!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    layoutResult.setVisibility(View.GONE);
                    tvError.setText("Authentication failed: Incorrect password or corrupted payload.");
                    tvError.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
