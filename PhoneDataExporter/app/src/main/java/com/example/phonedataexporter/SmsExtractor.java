package com.example.phonedataexporter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.provider.Telephony;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SmsExtractor {

    private Context context;

    public SmsExtractor(Context context) {
        this.context = context;
    }

    @SuppressLint("Range")
    public String extractSMSToJSON() throws UnsupportedEncodingException {
        if (checkPermission()) {
            JSONArray smsArray = new JSONArray();

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject smsObject = new JSONObject();
                    try {
                        smsObject.put("address", cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)));
                        smsObject.put("body", cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)));
                        smsObject.put("date", cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE)));
                        smsObject.put("type", cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE)));
                        smsArray.put(smsObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }
            System.out.println(smsArray);
//            return writeJSONToFile(smsArray);
            return smsArray.toString();
        }

        return "No permissions";
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_SMS}, 1);
            return false;
        }
        return true;
    }

    private String writeJSONToFile(JSONArray jsonArray) throws UnsupportedEncodingException {
        String filename = "sms_messages.json";
        byte[] jsonBytes = FileUtils.jsonToByteArray(jsonArray);
        FileUtils.saveFileToDownloads3(context, jsonBytes,filename);

        String downloadsPath = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();

        File file = new File(downloadsPath, filename);

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }
}