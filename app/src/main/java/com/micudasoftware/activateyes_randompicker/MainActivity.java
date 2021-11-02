package com.micudasoftware.activateyes_randompicker;

import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.app.NotificationCompat;
        import androidx.core.app.NotificationManagerCompat;
        import androidx.core.content.ContextCompat;
        import android.Manifest;
        import android.app.Notification;
        import android.app.NotificationChannel;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.drawable.ColorDrawable;
        import android.graphics.pdf.PdfDocument;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Environment;
        import android.os.ParcelFileDescriptor;
        import android.provider.MediaStore;
        import android.text.Layout;
        import android.text.StaticLayout;
        import android.text.TextPaint;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;
        import org.apache.poi.hssf.usermodel.HSSFWorkbook;
        import org.apache.poi.ss.usermodel.Cell;
        import org.apache.poi.ss.usermodel.Row;
        import org.apache.poi.ss.usermodel.Sheet;
        import org.apache.poi.ss.usermodel.Workbook;
        import org.apache.poi.xssf.usermodel.XSSFWorkbook;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.Random;

public class MainActivity extends AppCompatActivity {

    LayoutInflater layoutInflater;
    ListView listView;
    ViewGroup container;
    TextView textView;
    int state;
    ArrayList<String> cells;
    ArrayList<String> randomized;
    final String xls = "application/vnd.ms-excel";
    final String xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f10d19")));
        layoutInflater = getLayoutInflater();
        listView = findViewById(R.id.list_view);
        container = (ViewGroup) findViewById(R.id.container);
        textView = findViewById(R.id.textView);
        createNotificationChannel();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_us:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.activateyes.net/about"));
                startActivity(browserIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        state = 0;
        listView.setAdapter(null);
        textView.setVisibility(View.VISIBLE);
        container.removeAllViews();
        layoutInflater.inflate(R.layout.button, container);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openDocument();
            } else
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
        });
    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {xlsx, xls};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, 2);
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

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 1)
                openDocument();
            else if (requestCode == 2)
                exportToPDF(randomized);
        } else
            Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show();
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
        cells = new ArrayList<>();
        InputStream inputStream = getContentResolver().openInputStream(excelFile);
        Workbook workbook = null;
        String mimeType = getContentResolver().getType(excelFile);
        if (mimeType.equals(xls))
            workbook = new HSSFWorkbook(inputStream);
        else if (mimeType.equals(xlsx))
            workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
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
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.list_item, cells));
        container.removeAllViews();
        layoutInflater.inflate(R.layout.randomize, container);
        textView.setVisibility(View.INVISIBLE);
        EditText editText = findViewById(R.id.count);
        editText.setHint("Count: (1 - " + (cells.size()) + ")");
        Button randomizeButton = findViewById(R.id.randomize);
        randomizeButton.setOnClickListener(v -> {
            int count = Integer.parseInt(editText.getText().toString());
            if (count < 1 || count > cells.size())
                Toast.makeText(this, "Count isn't in range", Toast.LENGTH_SHORT).show();
            else {
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                randomize(count);
            }
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
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.list_item, randomized));
        container.removeAllViews();
        layoutInflater.inflate(R.layout.button, container);
        Button button = findViewById(R.id.button);
        button.setText("Export to PDF");
        button.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                exportToPDF(randomized);
            } else
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);
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

        String displayName = "ActivateYes-" + System.currentTimeMillis() + ".pdf";

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
        else
            contentValues.put(MediaStore.MediaColumns.DATA,
                    Environment.getExternalStorageDirectory() + "/" + displayName);
        final ContentResolver resolver = getContentResolver();
        Uri uri;

        try {
            uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            pdfDocument.writeTo(new FileOutputStream(pfd.getFileDescriptor()));
            pfd.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("PDF file generated successfully")
                    .setContentText("Tap to open")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());

        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Activate Yes";
            String description = "Random Picker Created PDF";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}