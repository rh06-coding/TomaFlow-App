package com.tomaflow.app.data.repository;

/**
 * Repository user tạm thời khi app chưa có đăng nhập.
 * Sau này có thể đổi sang Firebase/Auth/API mà ít ảnh hưởng UI.
 */
public class UserRepository {

    private static volatile UserRepository INSTANCE;

    // User id giả lập cho giai đoạn app chạy local.
    private static final String DEFAULT_USER_ID = "local_user";

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository();
                }
            }
        }

        return INSTANCE;
    }

    public String getCurrentUserId() {
        return DEFAULT_USER_ID;
    }

    public boolean isLoggedIn() {
        return true;
    }

    public void signOut() {
        // Tạm thời chưa làm gì.
        // Sau này nếu dùng Firebase thì gọi FirebaseAuth.getInstance().signOut()
    }
}