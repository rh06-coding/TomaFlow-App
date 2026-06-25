package com.tomaflow.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsHelper {

    @SuppressLint("Range")
    public static List<String> getPhoneNumbers(Context context) {
        Set<String> phoneNumbers = new HashSet<>();

        try (Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (phone != null) {
                        // Normalize phone number (remove spaces, dashes, etc.)
                        phone = phone.replaceAll("[^0-9+]", "");
                        if (!phone.isEmpty()) {
                            phoneNumbers.add(phone);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(phoneNumbers);
    }
}
