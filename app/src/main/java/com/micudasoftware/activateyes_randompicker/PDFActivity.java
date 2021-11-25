package com.micudasoftware.activateyes_randompicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

public class PDFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        String file = getIntent().getStringExtra("file");

        PDFView pdfView = findViewById(R.id.PDFView);
        pdfView.fromAsset(file).load();

        if (getIntent().hasExtra("isFirstStart")) {
            Button acceptButton = findViewById(R.id.accept_button);
            if (file.equals("UserManual.pdf"))
                acceptButton.setText("Continue");
            acceptButton.setVisibility(View.VISIBLE);
            acceptButton.setOnClickListener((view) -> {
                SharedPreferences.Editor editor = getSharedPreferences("RandomPicker", MODE_PRIVATE).edit();
                editor.putBoolean("isFirstStart", false);
                editor.apply();

                Intent intent = new Intent();
                if (file.equals("LicenseAgreement.pdf")) {
                    intent.setClass(this, PDFActivity.class);
                    intent.putExtra("file", "UserManual.pdf");
                    intent.putExtra("isFirstStart", true);
                } else
                    intent.setClass(this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.about_us:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.activateyes.net/about"));
                break;
            case R.id.user_manual:
                intent = new Intent(this, PDFActivity.class);
                intent.putExtra("file", "UserManual.pdf");
                break;
            case R.id.license_agreement:
                intent = new Intent(this, PDFActivity.class);
                intent.putExtra("file", "LicenseAgreement.pdf");
                break;
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}
