package com.tomaflow.app.utils;

/**
 * Trình trợ giúp thuần logic (pure-logic helper) để tạo định danh kết nối bạn bè (friend-connection id) xác định cho một cặp người dùng.
 * Việc sắp xếp từ điển (lexicographic ordering) đảm bảo cả hai phía đều cùng một id kết nối,
 * nên một yêu cầu từ A tới B và một yêu cầu từ B tới A sẽ ánh xạ tới cùng một bản ghi.
 *
 * Được trích xuất từ {@code FriendRepository.sendFriendRequest} để có thể kiểm thử đơn vị (unit-test)
 * mà không cần khởi tạo Firebase Firestore.
 */
public final class ConnectionIds {

    private ConnectionIds() {}

    /**
     * Trả về connection id xác định cho cặp {@code uidA} và {@code uidB}.
     * UID nhỏ hơn theo thứ tự từ điển luôn được đặt trước, ngăn cách bằng dấu gạch dưới.
     *
     * @throws IllegalArgumentException nếu một trong hai uid là null
     */
    public static String idFor(String uidA, String uidB) {
        if (uidA == null || uidB == null) {
            throw new IllegalArgumentException("uid must not be null");
        }
        if (uidA.compareTo(uidB) < 0) {
            return uidA + "_" + uidB;
        }
        return uidB + "_" + uidA;
    }
}
