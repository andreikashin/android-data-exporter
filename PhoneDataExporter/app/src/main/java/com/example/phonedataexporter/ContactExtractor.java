package com.example.phonedataexporter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

interface ExportContactsCallback {
    void onExportSuccess(String contactsJson);
    void onPermissionDenied();
}
public class ContactExtractor {

    private static final int REQUEST_CODE_READ_CONTACTS = 1001;



    private Context context;
    private ExportContactsCallback callback;

    public ContactExtractor(
            Context context,
            ExportContactsCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void exportContacts() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(
                    (MainActivity) context,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_READ_CONTACTS
            );
        } else {
            // Permission already granted, proceed with exporting
            String contactsJson = fetchContactsAsJson();
            callback.onExportSuccess(contactsJson);
        }
    }

    public void handlePermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with exporting
                String contactsJson = fetchContactsAsJson();
                callback.onExportSuccess(contactsJson);
            } else {
                // Permission denied
                callback.onPermissionDenied();
            }
        }
    }

    @SuppressLint("Range")
    private String fetchContactsAsJson() {
        try {
            JSONArray contactsJsonArray = new JSONArray(); // This will hold all contacts
            ContentResolver contentResolver = context.getContentResolver();
            Cursor contactsCursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            if (contactsCursor != null && contactsCursor.getCount() > 0) {
                while (contactsCursor.moveToNext()) {
                    JSONObject contactJson = new JSONObject();

                    // Get contact ID and Name
                    @SuppressLint("Range")
                    String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));

                    @SuppressLint("Range")
                    String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    contactJson.put("id", contactId);
                    contactJson.put("name", contactName);

                    // Fetch phone numbers if available
                    if (contactsCursor.getInt(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        JSONArray phoneNumbersJsonArray = new JSONArray();

                        Cursor phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contactId},
                                null);

                        if (phoneCursor != null) {
                            while (phoneCursor.moveToNext()) {
                                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumbersJsonArray.put(phoneNumber);
                            }
                            phoneCursor.close();
                        }
                        contactJson.put("phone_numbers", phoneNumbersJsonArray);
                    }

                    // Fetch email addresses if available
                    Cursor emailCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    JSONArray emailsJsonArray = new JSONArray();
                    if (emailCursor != null) {
                        while (emailCursor.moveToNext()) {
                            String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            emailsJsonArray.put(email);
                        }
                        emailCursor.close();
                    }
                    contactJson.put("emails", emailsJsonArray);

                    // Add contact to the main array
                    contactsJsonArray.put(contactJson);
                }
                contactsCursor.close();
            }

            // Convert to string and return the JSON object of contacts.
            return contactsJsonArray.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}"; // Return empty JSON object in case of error
    }
}
