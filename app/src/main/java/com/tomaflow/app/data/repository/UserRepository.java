package com.tomaflow.app.data.repository;

/**
 * Repository quản lý thông tin user hiện tại.
 *
 * Hiện tại app chưa có cloud auth nên dùng user giả lập.
 * Sau này nếu chuyển sang Firebase/Auth/API thì chỉ sửa bên trong class này,
 * các ViewModel và UI bên ngoài không cần đổi nhiều.
 */
public class UserRepository {

    private static volatile UserRepository INSTANCE;

    // User tạm thời cho giai đoạn local/offline.
    // Sau này có Firebase thì thay bằng FirebaseAuth.getInstance().getCurrentUser().getUid()
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