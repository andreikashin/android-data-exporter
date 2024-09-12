package com.example.phonedataexporter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_FOLDER = 1001;
    private Button exportSmsButton;
    private Button exportContactsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        exportSmsButton = findViewById(R.id.export_sms_button);
        exportSmsButton.setOnClickListener(v -> {
            // Code to export SMS messages
            // Trigger folder picker
            openFolderPicker();

            Toast
                    .makeText(
                            MainActivity.this,
                            "Button clicked!",
                            Toast.LENGTH_SHORT)
                    .show();
        });


        exportContactsButton = findViewById(R.id.export_contacts_button);
        exportContactsButton.setOnClickListener(v -> {
            // Code to export SMS messages
            // Trigger folder picker
            openFolderPicker();

            Toast
                    .makeText(
                            MainActivity.this,
                            "Button clicked!",
                            Toast.LENGTH_SHORT)
                    .show();
        });
//        TextView textView = findViewById(R.id.txtMessage);
//        textView.setText("<PLACEHOLDER>");
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FOLDER && resultCode == RESULT_OK) {
            if (data != null) {
                Uri treeUri = data.getData();
                // Persist access permission
                getContentResolver().takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // get SMS array as JSON
                SmsExtractor extractor = new SmsExtractor(this);
                try {
                    String json = extractor.extractSMSToJSON();
                    saveImmageIntoExternalStrogaeaboveQ(json);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

                // Save a file to the selected folder
//                saveFileToSelectedFolder(treeUri);

            }
        }
    }

    private void saveFileToSelectedFolder(Uri treeUri) {
        // Your logic to save file bytes to the selected folder using treeUri
//        Context context = v.getContext();
        SmsExtractor extractor = new SmsExtractor(this);
        try {
            extractor.extractSMSToJSON();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveImmageIntoExternalStrogaeaboveQ(String json) throws UnsupportedEncodingException {
        OutputStream outputStream = null;
        byte[] fileBytes = json.getBytes("UTF-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver contentResolver = getContentResolver();

            ContentValues mValue = new ContentValues();

            mValue.put(MediaStore.MediaColumns.DISPLAY_NAME, "sms" + ".json");
            //mValue.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + "json");
            Uri uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, mValue);
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            try {
                outputStream = contentResolver.openOutputStream(uri);
                Toast.makeText(this, "Image Saved SuccessFull", Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Something went wrong " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }finally {
                if (outputStream != null) {
                    try {
                        outputStream.write(fileBytes);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e("FileUtil", "Error closing output stream", e);
                    }
                }
            }

        } else {
            //old method to write
        }
    }
}