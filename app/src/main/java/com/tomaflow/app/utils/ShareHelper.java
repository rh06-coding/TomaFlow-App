package com.tomaflow.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.View;

import androidx.core.content.FileProvider;

import com.tomaflow.app.R;

import java.io.File;
import java.io.FileOutputStream;

public class ShareHelper {

    public static void shareStats(Context context, View chartView, String totalPomos, String totalTime) {
        Bitmap bitmap = createShareBitmap(context, totalPomos, totalTime);
        if (bitmap != null) {
            shareBitmap(context, bitmap);
        }
    }

    private static Bitmap createShareBitmap(Context context, String totalPomos, String totalTime) {
        int width = 1080;
        int height = 1080;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#F5F5F7")); // toma_background roughly
        canvas.drawRect(0, 0, width, height, paint);

        // Draw card background
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(20, 0, 10, Color.parseColor("#20000000"));
        canvas.drawRoundRect(100, 100, width - 100, height - 100, 40, 40, paint);
        paint.clearShadowLayer();

        // Text paints
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1C1C1E")); // toma_text
        textPaint.setTextSize(80);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setColor(Color.parseColor("#8E8E93")); // toma_text_muted
        subTextPaint.setTextSize(50);
        subTextPaint.setTextAlign(Paint.Align.CENTER);

        // TomaFlow brand
        Paint brandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        brandPaint.setColor(Color.parseColor("#FF453A")); // toma_primary
        brandPaint.setTextSize(60);
        brandPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        brandPaint.setTextAlign(Paint.Align.CENTER);

        int centerX = width / 2;

        canvas.drawText("TomaFlow", centerX, 250, brandPaint);
        
        canvas.drawText("My Focus Stats", centerX, 400, textPaint);
        
        // Draw stats
        textPaint.setTextSize(120);
        textPaint.setColor(Color.parseColor("#FF453A"));
        canvas.drawText(totalPomos, centerX, 600, textPaint);
        
        subTextPaint.setTextSize(40);
        canvas.drawText("POMODOROS COMPLETED", centerX, 680, subTextPaint);
        
        textPaint.setTextSize(80);
        textPaint.setColor(Color.parseColor("#1C1C1E"));
        canvas.drawText(totalTime, centerX, 820, textPaint);
        
        canvas.drawText("TOTAL FOCUS TIME", centerX, 880, subTextPaint);

        return bitmap;
    }

    private static void shareBitmap(Context context, Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "tomaflow_stats.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, "several"));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_achievement_title)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
