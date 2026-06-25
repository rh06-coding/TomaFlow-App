package com.tomaflow.app.utils;

/**
 * Trình trợ giúp thuần logic (pure-logic helper) để tạo định danh chat (chat id) xác định cho một cặp người dùng.
 * Việc sắp xếp từ điển (lexicographic ordering) đảm bảo hai người dùng có cùng một chat id
 * bất kể ai là người khởi tạo, nên không cần quan tâm đến thứ tự người gọi.
 *
 * Được trích xuất từ {@code ChatRepository.getChatId} và {@code UnreadBadgeManager.getChatId}
 * để có thể kiểm thử đơn vị (unit-test) mà không cần khởi tạo Firebase Firestore.
 */
public final class ChatIds {

    private ChatIds() {}

    /**
     * Trả về chat id xác định cho cặp {@code uid1} và {@code uid2}.
     * UID nhỏ hơn theo thứ tự từ điển luôn được đặt trước, ngăn cách bằng dấu gạch dưới.
     *
     * @throws IllegalArgumentException nếu một trong hai uid là null
     */
    public static String chatIdFor(String uid1, String uid2) {
        if (uid1 == null || uid2 == null) {
            throw new IllegalArgumentException("uid must not be null");
        }
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        }
        return uid2 + "_" + uid1;
    }
}
