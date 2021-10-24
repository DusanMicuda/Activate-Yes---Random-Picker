package com.micudasoftware.activateyes_randompicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

public class SplashScreen extends Activity {

    private boolean shouldStart = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        Handler handler = new Handler();
        handler.postDelayed(runnable, 3000);
    }

    private final Runnable runnable = () -> {
        if (shouldStart) {
            Intent intent = new Intent(this, MainActivity.class);
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
