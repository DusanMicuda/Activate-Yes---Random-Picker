package com.micudasoftware.activateyes_randompicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

public class PDFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        PDFView pdfView = findViewById(R.id.PDFView);
        pdfView.fromAsset("LicenseAgreement.pdf").load();

        Button acceptButton = findViewById(R.id.accept_button);
        acceptButton.setOnClickListener((view) -> {
            SharedPreferences.Editor editor = getSharedPreferences("RandomPicker", MODE_PRIVATE).edit();
            editor.putBoolean("isFirstStart", false);
            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
