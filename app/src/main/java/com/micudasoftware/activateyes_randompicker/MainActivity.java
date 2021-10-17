package com.micudasoftware.activateyes_randompicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;
    ListView listView;
    ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutInflater = getLayoutInflater();
        listView = findViewById(R.id.list_view);
        container = (ViewGroup) findViewById(R.id.container);
        layoutInflater.inflate(R.layout.button, container);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(
                        this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            startActivityForResult(intent, 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && data != null) {
            try {
                readExcelData(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readExcelData(Uri excelFile) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(excelFile);
        Log.v("debug", inputStream.toString());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        ArrayList<String> cells = new ArrayList<>();
        for (Row row : sheet) {
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                cells.add(cell.getStringCellValue());
            }
        }

        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cells));
        container.removeAllViews();
        layoutInflater.inflate(R.layout.randomize, container);
        EditText editText = findViewById(R.id.count);
        editText.setHint("Count: (1 - " + cells.size() + ")");
        Button randomizeButton = findViewById(R.id.randomize);
        randomizeButton.setOnClickListener(v -> {
            int count = Integer.parseInt(editText.getText().toString());
            if (count < 1 || count > cells.size())
                Toast.makeText(this, "Count isn't in range", Toast.LENGTH_SHORT).show();
            else
                randomize(cells, count);
        });
    }


}