# 🍅 TomaFlow

> Ứng dụng quản lý thời gian theo phương pháp Pomodoro dành cho Android — tập trung hơn, hiệu quả hơn.

---

## 📖 Giới thiệu

**TomaFlow** là ứng dụng Android giúp người dùng quản lý thời gian làm việc theo kỹ thuật **Pomodoro** — chia nhỏ công việc thành các phiên tập trung 25 phút xen kẽ với những khoảng nghỉ ngắn. Ứng dụng kết hợp bộ đếm giờ thông minh, danh sách công việc và thống kê trực quan giúp bạn theo dõi năng suất mỗi ngày.

## ✨ Tính năng chính

### 🕐 Đồng hồ Pomodoro
- Đếm ngược theo chu kỳ: **Làm việc (25p) → Nghỉ ngắn (5p) → Nghỉ dài (15p)**
- Điều khiển cơ bản: Start, Pause, Skip, Reset
- **Chạy ngầm (ForegroundService)** ngay cả khi thoát app hoặc tắt màn hình
- Chuông và rung báo hiệu kết thúc mỗi phiên

### ✅ Quản lý công việc
- Thêm, sửa, xóa công việc (To-do list)
- Gắn nhãn (Tag) và ước tính số Pomodoro cần thiết
- Đánh dấu hoàn thành từng task

### ⚙️ Tuỳ chỉnh cài đặt
- Tùy chỉnh thời lượng phiên Làm việc, Nghỉ ngắn, Nghỉ dài
- Cấu hình số chu kỳ trước khi vào Nghỉ dài

### 📊 Thống kê
- Biểu đồ số Pomodoro hoàn thành theo ngày
- Lọc theo 7 ngày / 30 ngày gần nhất

### 🌙 Giao diện
- Hỗ trợ **Dark Mode** (DayNight theme)
- Animation chuyển pha mượt mà (Work ↔ Break)
- Tương thích màn hình 5" – 6.7", Android API 26+

---

## 🛠️ Công nghệ sử dụng

| Thành phần       | Công nghệ                                           |
|------------------|-----------------------------------------------------|
| Ngôn ngữ         | Java (Android)                                      |
| Kiến trúc        | MVVM (Model-View-ViewModel)                         |
| Cơ sở dữ liệu    | SQLite thông qua Room Database                      |
| UI               | XML Layout, Material Design 3, Navigation Component |
| Biểu đồ          | MPAndroidChart                                      |
| Background       | ForegroundService + WakeLock                        |
| Thông báo        | NotificationChannel API                             |
| Test             | JUnit 4, Mockito                                    |
| Quản lý mã nguồn | Git + GitHub                                        |

---

## 🚀 Hướng dẫn cài đặt & build

### Yêu cầu
- Android Studio **Hedgehog** (2023.1.1) trở lên
- JDK 17+
- Android SDK API 26+ (minSdk 26, targetSdk 34)

### Các bước

```bash
# 1. Clone repository
git clone 
cd TomaFlow

# 2. Mở bằng Android Studio
#    File → Open → chọn thư mục TomaFlow

# 3. Để Android Studio tự sync Gradle
#    (lần đầu sẽ tải dependencies, cần kết nối internet)

# 4. Chạy trên emulator hoặc thiết bị thật
#    Run → Run 'app' (Shift + F10)
```

### Build APK
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (cần keystore)
./gradlew assembleRelease
```

APK output tại: `app/build/outputs/apk/`

---

## 📁 Cấu trúc dự án

```
TomaFlow/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tomaflow/
│   │   │   │   ├── ui/           # Fragment, Activity, Adapter
│   │   │   │   ├── timer/        # PomodoroTimer, ForegroundService
│   │   │   │   ├── data/         # Room DB, DAO, Entity, Repository
│   │   │   │   ├── notification/ # NotificationHelper, SoundManager
│   │   │   │   ├── utils/        # TimerUtils, DateUtils
│   │   │   │   └── constants/    # AppConstants
│   │   │   └── res/              # Layout XML, drawable, values
│   │   ├── test/                 # JUnit unit tests
│   │   └── androidTest/          # Instrumented tests (Room)
│   └── build.gradle
├── docs/                         # SRS, ERD, UML diagrams
└── README.md
```

---

## 👥 Nhóm phát triển

| Thành viên | GitHub                                   | Vai trò                                            |
|------------|------------------------------------------|----------------------------------------------------|
| **Hoàng**  |                                          | Leader · Timer Engine · ForegroundService · GitHub |
| **Hải**    | [@Haibrosh](https://github.com/Haibrosh) | UI/UX · XML Layout · Navigation · Animation        |
| **Hồng**   | [@hongtc08](https://github.com/hongtc08) | Database · Room · ViewModel · Repository           |
| **Long**   | [@hlongit](https://github.com/hlongit)   | QA · Notification · MPAndroidChart · Báo cáo       |

---

## 📋 Trạng thái phát triển

- Kiến trúc MVVM & Room DB schema
- Timer Engine (CountDownTimer + LiveData)
- ForegroundService + WakeLock
- Task Management (CRUD)
- Settings tuỳ chỉnh thời gian
- Notification + âm thanh + rung
- Thống kê SQLite + MPAndroidChart
- Dark Mode
- Firebase Auth (tuỳ chọn mở rộng)
- Cloud Sync với Firestore (tuỳ chọn mở rộng)

---

## 📄 Giấy phép  
© 2026 Nhóm TomaFlow. All rights reserved.
