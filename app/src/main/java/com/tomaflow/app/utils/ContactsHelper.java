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

    /**
     * Chuẩn hoá số điện thoại: loại bỏ mọi ký tự không phải chữ số hoặc dấu "+",
     * trả về chuỗi rỗng nếu kết quả không còn ký tự nào.
     *
     * Được trích xuất thành hàm tĩnh (static) riêng để có thể kiểm thử đơn vị (unit-test)
     * mà không cần {@link Context} hay con trỏ (Cursor) danh bạ.
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        String normalized = phone.replaceAll("[^0-9+]", "");
        return normalized;
    }

    @SuppressLint("Range")
    public static List<String> getPhoneNumbers(Context context) {
        Set<String> phoneNumbers = new HashSet<>();

        try (Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null)) {

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String normalized = normalizePhone(phone);
                    if (!normalized.isEmpty()) {
                        phoneNumbers.add(normalized);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>(phoneNumbers);
    }
}
