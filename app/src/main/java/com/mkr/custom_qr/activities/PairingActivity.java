package com.mkr.custom_qr.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.models.PairedDeviceModel;
import com.mkr.custom_qr.models.PairingEnvelope;
import com.mkr.custom_qr.pairing.DevicePairingManager;
import com.mkr.custom_qr.qr.QRGenerator;

public class PairingActivity extends AppCompatActivity {

    private DevicePairingManager.EphemeralSession initiatorSession;
    private ImageView ivPairingQr;
    private TextView tvFingerprint;
    private LinearLayout layoutInitiator, layoutReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        Toolbar toolbar = findViewById(R.id.toolbar_pairing);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivPairingQr = findViewById(R.id.iv_pairing_qr);
        tvFingerprint = findViewById(R.id.tv_initiator_fingerprint);
        layoutInitiator = findViewById(R.id.layout_initiator_view);
        layoutReceiver = findViewById(R.id.layout_receiver_view);

        TabLayout tabLayout = findViewById(R.id.tab_layout_pairing);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutInitiator.setVisibility(View.VISIBLE);
                    layoutReceiver.setVisibility(View.GONE);
                } else {
                    layoutInitiator.setVisibility(View.GONE);
                    layoutReceiver.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        startInitiatorSession();

        findViewById(R.id.btn_scan_device_b_response).setOnClickListener(v -> {
            Toast.makeText(this, "Scan Device B's response QR code to finalize pairing", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_scan_device_a).setOnClickListener(v -> {
            Toast.makeText(this, "Scan Device A's pairing QR code with camera", Toast.LENGTH_SHORT).show();
        });
    }

    private void startInitiatorSession() {
        try {
            initiatorSession = DevicePairingManager.createInitiatorSession();
            tvFingerprint.setText(initiatorSession.fingerprint);

            PairingEnvelope env = DevicePairingManager.buildPairingRequest(initiatorSession);
            String payload = env.toPayloadString();

            Bitmap qr = QRGenerator.generateQRCode(payload, 450, 450, 0xFF000000, 0xFFFFFFFF, "M", 2);
            ivPairingQr.setImageBitmap(qr);
        } catch (Exception e) {
            Toast.makeText(this, "Pairing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
