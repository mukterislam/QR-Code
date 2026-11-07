package com.mkr.custom_qr.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.chip.ChipGroup;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.encryption.EncryptionManager;
import com.mkr.custom_qr.models.QRCodeModel;
import com.mkr.custom_qr.qr.QRGenerator;
import com.mkr.custom_qr.utils.FileUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRGeneratorActivity extends AppCompatActivity {

    private ChipGroup chipGroup;
    private EditText etTitle, etContent, etPassword, etConfirmPassword;
    private LinearLayout layoutPrivate, layoutPreview;
    private CheckBox cbPairedDevice;
    private ImageView ivPreview;
    private Bitmap generatedBitmap;
    private String currentPayload = "";
    private boolean isPrivateMode = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        Toolbar toolbar = findViewById(R.id.toolbar_generator);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        chipGroup = findViewById(R.id.chip_group_qr_type);
        etTitle = findViewById(R.id.et_qr_title);
        etContent = findViewById(R.id.et_content);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        layoutPrivate = findViewById(R.id.layout_private_encryption);
        layoutPreview = findViewById(R.id.layout_qr_preview_card);
        cbPairedDevice = findViewById(R.id.cb_paired_device_mode);
        ivPreview = findViewById(R.id.iv_qr_result);

        // Pre-select private mode if passed from intent
        if (getIntent().getBooleanExtra("is_private", false)) {
            chipGroup.check(R.id.chip_private);
            layoutPrivate.setVisibility(View.VISIBLE);
            isPrivateMode = true;
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_private)) {
                layoutPrivate.setVisibility(View.VISIBLE);
                isPrivateMode = true;
            } else {
                layoutPrivate.setVisibility(View.GONE);
                isPrivateMode = false;
            }
        });

        findViewById(R.id.btn_generate_qr).setOnClickListener(v -> generateQRCode());

        findViewById(R.id.btn_save_png).setOnClickListener(v -> {
            if (generatedBitmap != null) {
                try {
                    FileUtils.saveBitmapToGallery(this, generatedBitmap, etTitle.getText().toString());
                    Toast.makeText(this, "Saved to Gallery (Pictures/CustomQR)", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_share_qr).setOnClickListener(v -> {
            if (generatedBitmap != null) {
                FileUtils.shareBitmap(this, generatedBitmap, etTitle.getText().toString());
            }
        });

        findViewById(R.id.btn_save_to_my_qr).setOnClickListener(v -> saveToDatabase());
    }

    private void generateQRCode() {
        String content = etContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter content", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPrivateMode) {
            String pass = etPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();
            if (pass.isEmpty() || pass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                try {
                    // Authenticated AES-256-GCM + PBKDF2 encryption
                    currentPayload = EncryptionManager.encryptWithPassword(content, pass);
                    renderQRBitmap(currentPayload);
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Encryption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            currentPayload = content;
            executor.execute(() -> renderQRBitmap(currentPayload));
        }
    }

    private void renderQRBitmap(String payload) {
        try {
            int fgColor = 0xFF000000;
            int bgColor = 0xFFFFFFFF;
            Bitmap bitmap = QRGenerator.generateQRCode(payload, 512, 512, fgColor, bgColor, "M", 2);
            generatedBitmap = bitmap;

            runOnUiThread(() -> {
                ivPreview.setImageBitmap(bitmap);
                layoutPreview.setVisibility(View.VISIBLE);
                Toast.makeText(this, "QR Code Generated!", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Generation error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveToDatabase() {
        if (currentPayload.isEmpty()) return;

        QRCodeModel model = new QRCodeModel();
        model.setTitle(etTitle.getText().toString().trim().isEmpty() ? "QR Code" : etTitle.getText().toString().trim());
        model.setRawPayload(currentPayload);
        model.setPrivate(isPrivateMode);
        model.setSecurityMode(isPrivateMode ? "password" : "none");
        model.setQrType(isPrivateMode ? "private_password" : "text");

        CustomQRApplication.getInstance().getRepository().insertQRCode(model);
        Toast.makeText(this, "Saved to My QR Codes!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
