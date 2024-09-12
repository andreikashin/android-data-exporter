package com.example.phonedataexporter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class FileUtils {

    public static boolean saveFileToDownloads3(Context context, byte[] fileBytes, String fileName) {
        OutputStream outputStream = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 and above
                ContentResolver contentResolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
                contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                outputStream = contentResolver.openOutputStream(contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues));
            } else {
                // Android 9 and below
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }

                File file = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                outputStream.write(fileBytes);
                outputStream.flush();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e("FileUtil", "Error saving file to downloads", e);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e("FileUtil", "Error closing output stream", e);
                }
            }
        }
    }

    public static boolean saveFileToSelectedFolder(Context context, Uri folderUri, byte[] fileBytes, String fileName) {
        try {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(context, folderUri);
            DocumentFile newFile = pickedDir.createFile("application/octet-stream", fileName);

            try (OutputStream outputStream = context.getContentResolver().openOutputStream(newFile.getUri())) {
                if (outputStream != null) {
                    outputStream.write(fileBytes);
                    outputStream.flush();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void saveFileToDownloads1(Context context, byte[] fileContent, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE,
                "application/octet-stream"); // Replace with appropriate MIME type
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        try {
            // Insert the file into the Downloads folder
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            }
            if (uri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(fileContent);

                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFileToDownloads2(Context context, byte[] fileContent, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE,
                "application/octet-stream"); // Replace with appropriate MIME type
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        try {
            // Insert the file into the Downloads folder
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            }
            if (uri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(fileContent);

                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFileToInternalStorage(
            Context context,
            byte[] fileContent,
            String fileName) {
        File internalStoragePath = context.getFilesDir();
        File file = new File(internalStoragePath, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileContent);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] jsonToByteArray(JSONArray jsonArray) throws UnsupportedEncodingException {
        String jsonString = jsonArray.toString();
        return jsonString.getBytes("UTF-8");
    }

    public static byte[] stringToByteArray(String jsonString) throws UnsupportedEncodingException {
        return jsonString.getBytes("UTF-8");
    }
}
