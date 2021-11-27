package com.micudasoftware.randompicker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

public class SplashScreen extends Activity {

    private boolean shouldStart = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        this.getWindow().setStatusBarColor(Color.parseColor("#FFF5A0"));

        Handler handler = new Handler();
        handler.postDelayed(runnable, 3000);
    }

    private final Runnable runnable = () -> {
        if (shouldStart) {
            SharedPreferences preferences = getSharedPreferences("com.micudasoftware.randompicker", MODE_PRIVATE);
            boolean isFirstStart = preferences.getBoolean("isFirstStart", true);

            Intent intent = new Intent();
            Class<?> cls;
            if (isFirstStart) {
                cls = PDFActivity.class;
                intent.putExtra("isFirstStart", true);
                intent.putExtra("file", "LicenseAgreement.pdf");
            }else
                cls = MainActivity.class;

            intent.setClass(this, cls);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onPause() {
        shouldStart = false;
        super.onPause();
        finish();
    }
}
