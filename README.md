# 🍅 TomaFlow

> Ứng dụng quản lý thời gian theo phương pháp Pomodoro dành cho Android — tập trung hơn, hiệu quả hơn.

**Nhóm 6 · Môn Nhập môn Ứng dụng di động · SE114.Q21**  
Giảng viên hướng dẫn: Nguyễn Tấn Toàn  
TP. Hồ Chí Minh, 6/2026

---

## 📖 Giới thiệu

**TomaFlow** là ứng dụng Android giúp người dùng quản lý thời gian làm việc theo kỹ thuật **Pomodoro** — chia nhỏ công việc thành các phiên tập trung xen kẽ với những khoảng nghỉ ngắn. Ứng dụng kết hợp bộ đếm giờ thông minh chạy nền, danh sách công việc, nhạc nền, thống kê trực quan, mạng xã hội bạn bè và bảng xếp hạng — tất cả được đồng bộ lên Firebase Firestore.

---

## ✨ Tính năng chính

### 🕐 Đồng hồ Pomodoro
- Đếm ngược theo chu kỳ: **Làm việc → Nghỉ ngắn → Nghỉ dài** (tuỳ chỉnh thời lượng)
- Điều khiển: Start, Pause, Resume, Skip, Reset, Jump-to-phase
- **ForegroundService + WakeLock** — chạy ngầm khi thoát app hoặc tắt màn hình
- Chuông, rung và thông báo khi kết thúc mỗi phiên
- Tomato Growth View: quả cà chua "lớn lên" theo tiến trình phiên
- Strict Mode (VIP): chặn pause/skip khi đang chạy

### ✅ Quản lý công việc
- CRUD công việc với tiêu đề, ghi chú, số Pomodoro ước tính, thời lượng (phút)
- Gắn task vào phiên Focus (TaskPicker) — tự động giảm số Pomodoro khi hoàn thành
- Phân loại Active / Completed

### 🎵 Nhạc nền
- Thư viện nhạc built-in + import nhạc từ máy
- AppMusicPlayer (Singleton) phát nền, pause/resume/stop
- Đồng bộ với timer: tự dừng khi timer dừng (tuỳ chọn)

###  Thống kê & Journal
- Biểu đồ tuần/tháng (MPAndroidChart): số Pomodoro, phút tập trung, streak
- Journal: ghi chú kèm mood (Happy / Focused / Tired / Stressed), strip màu theo mood
- Lọc 7 ngày / 30 ngày / tháng cụ thể

### 👥 Bạn bè & Chat
- Tìm bạn qua username hoặc danh bạ điện thoại
- Gửi/chấp nhận/từ chối lời mời kết bạn
- Nhắn tin thời gian thực (Firestore snapshot listener)
- Badge tin nhắn chưa đọc trên tab Profile

###  Bảng xếp hạng & Phần thưởng
- Leaderboard: so sánh Pomodoro, streak, level với bạn bè
- Hệ thống huy hiệu (Early Bird, Night Owl, Marathon…)
- Hành trình (Journey): level up theo tổng phút tập trung

### 👤 Hồ sơ & Cài đặt
- Đăng ký/đăng nhập (email + Google Sign-In)
- Chỉnh sửa hồ sơ: avatar (ảnh/base64), tên, username, SĐT, ngày sinh
- Dark Mode, ngôn ngữ (Tiếng Việt / English)
- Tuỳ chỉnh thời lượng các pha, Strict Mode, Auto-start Break
- **Premium/VIP**: giới hạn task, bỏ Strict Mode, tuỳ chỉnh slider thời lượng

### ☁️ Đồng bộ đám mây
- Firebase Authentication (email/password + Google)
- Cloud Firestore: đồng bộ Tasks, Sessions, Notes, Rewards, Profile, Friends, Chat
- Room Database (local) + Firestore (cloud) — offline-first

---

## 🛠️ Công nghệ sử dụng

| Thành phần           | Công nghệ                                                    |
|----------------------|--------------------------------------------------------------|
| Ngôn ngữ             | Java 17                                                      |
| Kiến trúc            | MVVM + Repository + LiveData                                 |
| Cơ sở dữ liệu local  | Room Database (SQLite)                                       |
| Cơ sở dữ liệu cloud  | Firebase Firestore                                           |
| Xác thực             | Firebase Authentication (email + Google)                     |
| UI                   | XML Layout, Material Design 3, Navigation Component          |
| Biểu đồ              | MPAndroidChart                                               |
| Background           | ForegroundService + WakeLock                                 |
| Thông báo            | NotificationChannel API                                      |
| Âm thanh             | MediaPlayer + MediaSession                                   |
| Ảnh                  | Glide                                                        |
| ViewBinding          | Bật, đang áp dụng dần (5 màn hình nặng nhất)                 |
| Test                 | JUnit 4, Robolectric, Espresso                               |
| CI                   | GitHub Actions (unit tests)                                  |
| Quản lý mã nguồn     | Git + GitHub                                                 |

---

## 🚀 Hướng dẫn cài đặt & build

### Yêu cầu
- Android Studio **Hedgehog** (2023.1.1) trở lên
- JDK 17+
- Android SDK API 26+ (minSdk 26, targetSdk 34)
- File `google-services.json` (Firebase config — đã được gitignore, cần tự thêm để build có Firebase)

### Các bước

```bash
# 1. Clone repository
git clone https://github.com/hlongit/TomaFlow-App.git
cd TomaFlow-App

# 2. Thêm google-services.json vào thư mục app/
#    (tải từ Firebase Console → Project Settings → Your apps)

# 3. Mở bằng Android Studio
#    File → Open → chọn thư mục TomaFlow-App

# 4. Để Android Studio tự sync Gradle
#    (lần đầu sẽ tải dependencies, cần kết nối internet)

# 5. Chạy trên emulator hoặc thiết bị thật
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

> **Lưu ý CI:** Plugin `com.google.gms.google-services` chỉ được áp dụng khi `google-services.json` tồn tại, nên CI có thể build và chạy unit test mà không cần file này.

---

## 📁 Cấu trúc dự án

```
TomaFlow-App/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tomaflow/app/
│   │   │   │   ├── constants/         # AppConstants (commands, prefs, notif IDs)
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/            # Room: TomaFlowDatabase, DAO, Entity
│   │   │   │   │   ├── model/         # POJO: UserProfile, ChatMessage, ...
│   │   │   │   │   ├── remote/        # Firestore remote data sources
│   │   │   │   │   └── repository/    # Repository layer + SyncManager
│   │   │   │   ├── timer/             # PomodoroTimer, TimerEngineService,
│   │   │   │   │                      # TimerStateManager, SettingsManager
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/          # LoginActivity, RegisterActivity
│   │   │   │   │   ├── chat/          # ChatActivity, ChatAdapter
│   │   │   │   │   ├── focus/         # FocusFragment, Dialogs
│   │   │   │   │   ├── friends/       # FriendsActivity, Fragments, Adapters
│   │   │   │   │   ├── leaderboard/   # LeaderboardActivity
│   │   │   │   │   ├── music/         # MusicPickerActivity, AppMusicPlayer,
│   │   │   │   │   │                  # MusicService, LocalMusicManager
│   │   │   │   │   ├── premium/       # PremiumActivity, PremiumGateDialog
│   │   │   │   │   ├── profile/       # ProfileFragment, EditProfileActivity
│   │   │   │   │   ├── rewards/       # RewardsFragment, FarmAdapter
│   │   │   │   │   ├── settings/      # SettingsFragment
│   │   │   │   │   ├── stats/         # StatsFragment, JournalActivity,
│   │   │   │   │   │                  # NoteAdapter, StatsAggregator
│   │   │   │   │   ├── tasks/         # TasksFragment, TaskPickerActivity,
│   │   │   │   │   │                  # TaskAdapter, TaskViewModel
│   │   │   │   │   └── timer/         # TimerView, TomatoGrowthView,
│   │   │   │   │                      # TimerViewModel
│   │   │   │   ├── utils/             # AvatarHelper, FirestoreLiveData,
│   │   │   │   │                      # HeaderUIHelper, LanguageManager,
│   │   │   │   │                      # NotificationHelper, TomaToast, ...
│   │   │   │   ├── MainActivity.java
│   │   │   │   └── TomaFlowApp.java
│   │   │   ── res/                   # Layout XML, drawable, values,
│   │   │                              # values-night, values-vi, navigation
│   │   ├── test/                      # JUnit unit tests (Robolectric)
│   │   └── androidTest/               # Instrumented tests (Room)
│   ├── build.gradle
│   └── proguard-rules.pro
├── .github/workflows/                 # CI: unit-tests.yml
├── docs/                              # FinalReport.pdf, diagrams, QA reports
└── README.md
```

---

## 👥 Nhóm phát triển

| Thành viên            | MSSV       | GitHub                                   | Vai trò                                            |
|-----------------------|------------|------------------------------------------|----------------------------------------------------|
| **Lê Hoàng**          | 24520538   | [@rh06-coding](https://github.com/rh06-coding) | Leader · Timer Engine · ForegroundService · GitHub |
| **Tăng Chân Hồng**    | 24520579   | [@hongtc08](https://github.com/hongtc08)       | Database · Room · ViewModel · Repository           |
| **Nguyễn Hoàn Hải**   | 24520437   | [@Haibrosh](https://github.com/Haibrosh)       | UI/UX · XML Layout · Navigation · Animation        |
| **Trương Võ Hoàng Long** | 24521022 | [@hlongit](https://github.com/hlongit)         | QA · Notification · MPAndroidChart · Báo cáo       |

---

##  Lịch sử phát triển

- [x] Kiến trúc MVVM + Room DB schema
- [x] Timer Engine (state machine + LiveData)
- [x] ForegroundService + WakeLock + Notification
- [x] Task Management (CRUD + TaskPicker + decrement)
- [x] Settings tuỳ chỉnh thời gian, Strict Mode, Auto-start Break
- [x] Nhạc nền (built-in + import local, AppMusicPlayer singleton)
- [x] Thống kê SQLite + MPAndroidChart (tuần/tháng)
- [x] Dark Mode (DayNight theme) + đa ngôn ngữ (VI/EN)
- [x] Firebase Authentication (email/password + Google Sign-In)
- [x] Cloud Firestore sync (Tasks, Sessions, Notes, Rewards, Profile)
- [x] Friends & Chat (real-time, unread badge)
- [x] Leaderboard (so sánh với bạn bè)
- [x] Rewards & Journey (huy hiệu, level up)
- [x] Notes / Journal (mood strip, edit/delete)
- [x] Profile (avatar base64, edit, VIP status)
- [x] Premium/VIP gate (giới hạn task, slider, Strict Mode)
- [x] FirestoreLiveData (auto-release snapshot listeners)
- [x] UnreadBadgeManager (session-safe, observer-safe)
- [x] Focus session dedupe (Service sole persistor, accurate timing)
- [x] SyncManager (tách logic đồng bộ khỏi LoginActivity)
- [x] ViewBinding pilot (5 màn hình nặng nhất)
- [x] CI workflow (GitHub Actions unit tests)

---

## 📄 Giấy phép

© 2026 Nhóm 6 — TomaFlow. All rights reserved.
