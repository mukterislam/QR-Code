package com.mkr.custom_qr.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.models.PairingEnvelope;
import com.mkr.custom_qr.models.QREncryptedEnvelope;
import com.mkr.custom_qr.models.ScanHistoryModel;
import com.mkr.custom_qr.qr.QRPayloadManager;
import java.io.InputStream;

public class QRScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private DecoratedBarcodeView barcodeView;
    private boolean isFlashOn = false;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleGalleryImageUri
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner_view);
        findViewById(R.id.btn_scanner_back).setOnClickListener(v -> finish());

        ImageView btnFlash = findViewById(R.id.btn_scanner_flash);
        btnFlash.setOnClickListener(v -> {
            if (isFlashOn) {
                barcodeView.setTorchOff();
                isFlashOn = false;
            } else {
                barcodeView.setTorchOn();
                isFlashOn = true;
            }
        });

        findViewById(R.id.btn_scanner_gallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        checkCameraPermissionAndStart();
    }

    private void checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startScanning() {
        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    handleScanResult(result.getText());
                }
            }
        });
        barcodeView.resume();
    }

    private void handleScanResult(String raw) {
        // Save to scan history
        ScanHistoryModel history = new ScanHistoryModel();
        history.setRawContent(raw);
        history.setDetectedType(QRPayloadManager.detectPayloadType(raw));
        history.setPrivate(QREncryptedEnvelope.isEncryptedPayload(raw));
        history.setTitle(history.isPrivate() ? "Private Encrypted QR" : "Scanned QR");
        CustomQRApplication.getInstance().getRepository().insertScanHistory(history);

        // Check if Private Encrypted QR
        if (QREncryptedEnvelope.isEncryptedPayload(raw)) {
            Intent intent = new Intent(this, PasswordUnlockActivity.class);
            intent.putExtra("raw_encrypted_payload", raw);
            startActivity(intent);
            finish();
            return;
        }

        // Check if Device Pairing QR
        if (PairingEnvelope.isPairingPayload(raw)) {
            Intent intent = new Intent(this, PairingActivity.class);
            intent.putExtra("scanned_pairing_payload", raw);
            startActivity(intent);
            finish();
            return;
        }

        // Normal QR Result
        Intent intent = new Intent(this, QRDetailsActivity.class);
        intent.putExtra("scanned_raw_content", raw);
        startActivity(intent);
        finish();
    }

    private void handleGalleryImageUri(Uri uri) {
        if (uri == null) return;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                Result result = reader.decode(binaryBitmap);
                handleScanResult(result.getText());
            }
        } catch (Exception e) {
            Toast.makeText(this, "No QR Code found in image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
