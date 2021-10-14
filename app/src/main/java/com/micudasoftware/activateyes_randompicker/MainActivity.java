package com.micudasoftware.activateyes_randompicker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(R.layout.button, (ViewGroup) findViewById(R.id.container));


    }
}