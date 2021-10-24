package com.micudasoftware.activateyes_randompicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;
    ListView listView;
    ViewGroup container;
    int state;
    ArrayList<String> cells;
    ArrayList<String> randomized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        state = 0;
        layoutInflater = getLayoutInflater();
        listView = findViewById(R.id.list_view);
        listView.setAdapter(null);
        TextView textView = findViewById(R.id.textView);
        textView.setVisibility(View.VISIBLE);
        container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        layoutInflater.inflate(R.layout.button, container);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                startActivityForResult(intent, 2);
            } else
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
        });
    }

    @Override
    public void onBackPressed() {
        switch (state) {
            case 0:
                super.onBackPressed();
                break;
            case 1:
                init();
                break;
            case 2:
                setView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                startActivityForResult(intent, 2);
            } else {
                Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && data != null) {
            try {
                Uri uri = data.getData();
                readExcelData(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readExcelData(Uri excelFile) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(excelFile);
        cells = new ArrayList<>();
        Log.v("debug", excelFile.toString());
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                cells.add(cell.getStringCellValue());
            }
        }
        setView();
    }

    private void setView() {
        state = 1;
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cells));
        container.removeAllViews();
        layoutInflater.inflate(R.layout.randomize, container);
        TextView textView = findViewById(R.id.textView);
        textView.setVisibility(View.INVISIBLE);
        EditText editText = findViewById(R.id.count);
        editText.setHint("Count: (1 - " + cells.size() + ")");
        Button randomizeButton = findViewById(R.id.randomize);
        randomizeButton.setOnClickListener(v -> {
            int count = Integer.parseInt(editText.getText().toString());
            if (count < 1 || count > cells.size())
                Toast.makeText(this, "Count isn't in range", Toast.LENGTH_SHORT).show();
            else
                randomize(count);
        });
    }

    private void randomize(int count) {
        ArrayList<String> cells = new ArrayList<>(this.cells);
        randomized = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            Random rand = new Random();
            int index = rand.nextInt(cells.size());
            randomized.add(cells.get(index));
            cells.remove(index);
        }

        state = 2;
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, randomized));
        container.removeAllViews();
        layoutInflater.inflate(R.layout.button, container);
        Button button = findViewById(R.id.button);
        button.setText("Export to PDF");
        button.setOnClickListener(v -> {
            exportToPDF(randomized);
        });
    }

    private void exportToPDF(ArrayList<String> randomized) {

        PdfDocument pdfDocument = new PdfDocument();

//        Paint paint = new Paint();
        TextPaint textPaint = new TextPaint(Color.BLACK);
        textPaint.setTextSize(14);

        int pageHeight = 842;
        int pageWidth = 595;
        int pageNumber = 1;

        PdfDocument.Page myPage = pdfDocument.startPage(
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());

        Canvas canvas = myPage.getCanvas();

//        canvas.drawBitmap(scaledbmp, 56, 40, paint);
        int height = 50;
        canvas.translate(50,50);
        for (String text : randomized) {
            StaticLayout staticLayout = new StaticLayout(text, textPaint, pageWidth - 100,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (height + staticLayout.getHeight() + 50 < pageHeight) {
                staticLayout.draw(canvas);
                canvas.translate(0, staticLayout.getHeight() + 50);
                height += staticLayout.getHeight() + 50;
            } else {
                pdfDocument.finishPage(myPage);
                pageNumber += 1;
                myPage = pdfDocument.startPage(
                        new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create());
                canvas = myPage.getCanvas();
                canvas.translate(50,50);
                height = 50;

                staticLayout.draw(canvas);
                canvas.translate(0, staticLayout.getHeight() + 50);
                height += staticLayout.getHeight() + 50;
            }
        }

        pdfDocument.finishPage(myPage);

        String displayName = "ActiveYes.pdf";
        String mimeType = "application/pdf";
        String relativeLocation = Environment.DIRECTORY_DOCUMENTS;

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);
        final ContentResolver resolver = getContentResolver();
        Uri uri;

        try {
            uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");

            pdfDocument.writeTo(new FileOutputStream(pfd.getFileDescriptor()));

            pfd.close();
            Toast.makeText(MainActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {

            e.printStackTrace();
        }

        pdfDocument.close();
    }
}