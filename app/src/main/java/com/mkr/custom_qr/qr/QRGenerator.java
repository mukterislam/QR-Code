package com.mkr.custom_qr.qr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.EnumMap;
import java.util.Map;

/**
 * High-performance offline QR Code Bitmap generator using ZXing.
 * Supports custom resolutions, foreground/background colors, and error correction.
 */
public class QRGenerator {

    public static Bitmap generateQRCode(String content, int width, int height, int fgColor, int bgColor, String eccLevel, int margin)
            throws Exception {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        ErrorCorrectionLevel errorCorrection = ErrorCorrectionLevel.M;
        if ("L".equalsIgnoreCase(eccLevel)) errorCorrection = ErrorCorrectionLevel.L;
        else if ("Q".equalsIgnoreCase(eccLevel)) errorCorrection = ErrorCorrectionLevel.Q;
        else if ("H".equalsIgnoreCase(eccLevel)) errorCorrection = ErrorCorrectionLevel.H;

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        hints.put(EncodeHintType.MARGIN, margin > 0 ? margin : 2);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        int matrixWidth = bitMatrix.getWidth();
        int matrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[matrixWidth * matrixHeight];

        for (int y = 0; y < matrixHeight; y++) {
            int offset = y * matrixWidth;
            for (int x = 0; x < matrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? fgColor : bgColor;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight);
        return bitmap;
    }

    /**
     * Embed custom logo in the center of QR code.
     */
    public static Bitmap embedLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        if (logoBitmap == null) return qrBitmap;

        int qrWidth = qrBitmap.getWidth();
        int qrHeight = qrBitmap.getHeight();

        Bitmap combined = Bitmap.createBitmap(qrWidth, qrHeight, qrBitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        canvas.drawBitmap(qrBitmap, 0, 0, null);

        // Logo scale: 20% of QR dimensions to ensure scan reliability with Level H / Q
        int logoWidth = qrWidth / 5;
        int logoHeight = qrHeight / 5;
        int logoX = (qrWidth - logoWidth) / 2;
        int logoY = (qrHeight - logoHeight) / 2;

        Bitmap scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoWidth, logoHeight, true);
        canvas.drawBitmap(scaledLogo, logoX, logoY, new Paint(Paint.FILTER_BITMAP_FLAG));

        return combined;
    }
}
