package com.mkr.custom_qr.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.models.QRCodeModel;
import com.mkr.custom_qr.qr.QRPayloadManager;
import com.mkr.custom_qr.utils.SecurityUtils;

public class QRDetailsActivity extends AppCompatActivity {

    private String rawContent = "";
    private QRCodeModel qrItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scan_result);

        qrItem = (QRCodeModel) getIntent().getSerializableExtra("qr_item");
        if (qrItem != null) {
            rawContent = qrItem.getRawPayload();
        } else {
            rawContent = getIntent().getStringExtra("scanned_raw_content");
        }
        if (rawContent == null) rawContent = "";

        TextView tvType = findViewById(R.id.tv_result_type);
        TextView tvContent = findViewById(R.id.tv_result_content);
        Button btnAction = findViewById(R.id.btn_result_primary_action);
        Button btnCopy = findViewById(R.id.btn_result_copy);

        String type = QRPayloadManager.detectPayloadType(rawContent);
        tvType.setText("Type: " + type.toUpperCase());
        tvContent.setText(rawContent);

        if ("url".equals(type)) {
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setText("Open Website");
            btnAction.setOnClickListener(v -> {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rawContent));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open link", Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("phone".equals(type)) {
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setText("Call Phone");
            btnAction.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(rawContent));
                startActivity(dialIntent);
            });
        } else {
            btnAction.setVisibility(View.GONE);
        }

        btnCopy.setOnClickListener(v -> {
            SecurityUtils.copyWithAutoWipe(this, "QR Content", rawContent, false);
        });

        findViewById(R.id.btn_result_close).setOnClickListener(v -> finish());
    }
}
