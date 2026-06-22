# Chức năng Chuyển đổi Ngôn ngữ Anh – Việt

## Tình trạng hiện tại

Dự án đã có sẵn cơ sở hạ tầng tốt:
- ✅ [LanguageManager.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/utils/LanguageManager.java) — lưu/đọc locale từ SharedPreferences, wrap Context
- ✅ `attachBaseContext` đã được override ở **4 Activity**: `MainActivity`, `LoginActivity`, `TaskPickerActivity`, `MusicPickerActivity`
- ✅ Toggle group EN/VI trên [fragment_settings.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_settings.xml) hoạt động, gọi `recreate()`
- ✅ File [values-vi/strings.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/values-vi/strings.xml) đã có 130 dòng dịch

**Tuy nhiên**, sau khi quét toàn bộ dự án, phát hiện **nhiều vấn đề** cần sửa:

---

## Vấn đề cần giải quyết

### 🔴 Vấn đề 1: Hardcoded strings trong Java code (nhiều nhất)

| File | Dòng | Nội dung hardcoded |
|------|------|--------------------|
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L82) | 82, 83, 129, 130, 176, 177 | `"Chọn công việc"`, `"Nhấn để chọn"` |
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L122) | 122 | `"Đang tập trung"` |
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L210) | 210 | `"Vui lòng chọn nhạc trước"` (Toast) |
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L292) | 292–293 | `"Hoàn thành!"`, `"Tuyệt vời! Bạn đã hoàn thành công việc này."` (AlertDialog) |
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L382) | 382 | `"🍅 Pomodoro hoàn thành!"` |
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L390-L394) | 390–394 | Tomato growth labels: `"Hạt giống vừa được gieo..."`, `"Mầm xanh đang nhú lên!"`, v.v. |
| [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L401) | 401 | `"Cây đã chết... 😢 Hãy cố lần sau!"` |
| [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java#L54) | 54 | `"Google sign in failed: "` (Toast) |
| [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java#L109) | 109 | `"Authentication Failed."` (Toast) |
| [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java#L118-L119) | 118, 119 | `"Bắt buộc"` (setError) |
| [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java#L126) | 126 | `"Đăng nhập thất bại: "` (Toast) |
| [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java#L134) | 134 | `"Nhập email để reset mật khẩu"` (setError) |
| [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java#L139) | 139 | `"Email reset đã gửi"` (Toast) |
| [RegisterActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/RegisterActivity.java#L56-L59) | 56–59 | `"Bắt buộc"`, `"Tối thiểu 8 ký tự"`, `"Mật khẩu không khớp"` (setError) |
| [RegisterActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/RegisterActivity.java#L72) | 72 | `"Đăng ký thành công! Hãy đăng nhập."` (Toast) |
| [RegisterActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/RegisterActivity.java#L78) | 78 | `"Đăng ký thất bại: "` (Toast) |
| [TasksFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TasksFragment.java#L102-L103) | 102–103 | `activeTasks.size() + " ITEMS"` |
| [TasksFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TasksFragment.java#L139) | 139 | `"Vui lòng nhập tên công việc"` (Toast) |
| [TaskPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TaskPickerActivity.java#L63) | 63 | `"Chưa chọn công việc"` |
| [MusicPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/music/MusicPickerActivity.java#L138-L140) | 138, 140 | `"🎧"`, `"Từ thiết bị"` |
| [MainActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/MainActivity.java#L46) | 46 | `"Nhấn back lần nữa để thoát"` (Toast) |

### 🔴 Vấn đề 2: Hardcoded strings trong Layout XML

| File | Dòng | Nội dung |
|------|------|----------|
| [fragment_settings.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_settings.xml#L80) | 80, 102, 107, 142, 160, 161, 195, 213, 214 | `"25m"`, `"5 MIN"`, `"90 MIN"`, `"5m"`, `"1 MIN"`, v.v. |
| [fragment_settings.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_settings.xml#L382-L390) | 382, 390 | `"EN"`, `"VI"` |
| [fragment_stats.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_stats.xml) | 39, 81, 89, 119, 152, 182, 214, 219 | `"Stats"`, `"WEEK"`, `"MONTH"`, `"32h 14m"`, v.v. |
| [fragment_tasks.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_tasks.xml) | 70, 104, 130, 165, 197 | `"04"`, `"12"`, `"85%"`, `"3 ITEMS"` |
| [fragment_rewards.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_rewards.xml) | nhiều dòng | `"RANKING"`, `"HOURS"`, `"STREAK"`, v.v. |
| [fragment_profile.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/fragment_profile.xml) | nhiều dòng | `"Level 42"`, `"RANKING"`, v.v. |
| [layout_task_picker_sheet.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/layout_task_picker_sheet.xml#L24) | 24 | `"Chọn công việc"` |
| [item_task_picker.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/item_task_picker.xml) | 37, 47, 61 | `"Tên task"`, `"Mô tả"`, `"4"` |
| [item_task.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/item_task.xml) | 40, 48, 61 | `"Finalize Design Tokens"`, v.v. |
| [item_badge.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/item_badge.xml) | 48, 59 | `"Early Bird"`, `"10 Sessions..."` |
| [item_builtin_track.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/item_builtin_track.xml) | 24, 38, 46 | `"🎵"`, `"Lo-Fi Chill"` |
| [bottom_sheet_add_task.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/bottom_sheet_add_task.xml) | 23, 37 | `"Task Title"` (hint), `"Note (Optional)"` (hint) |

### 🔴 Vấn đề 3: Phase enum hardcoded English

[PomodoroTimer.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/timer/PomodoroTimer.java#L23-L31) dùng `Phase.getDisplayName()` trả về `"Focus"`, `"Short Break"`, `"Long Break"` cứng. Chuỗi này được hiển thị trên Notification và FocusFragment. Cần chuyển sang `getString(R.string.xxx)`.

### 🟡 Vấn đề 4: Thiếu translations trong `values-vi/strings.xml`

Các key sau tồn tại trong `values/strings.xml` nhưng **thiếu** trong `values-vi/strings.xml`:
- `reg_confirm_password`
- `stats_breakdown`, `stats_no_data`, `stats_completed`, `stats_failed`
- `settings_dark_mode`, `settings_dark_mode_desc`
- `nav_rewards`
- `auth_google`
- Toàn bộ nhóm `notification_*` (12 key)
- `placeholder_task_title`, `placeholder_task_subtitle`

### 🟡 Vấn đề 5: `RegisterActivity` thiếu `attachBaseContext`

[RegisterActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/RegisterActivity.java) **không** override `attachBaseContext` → khi chuyển locale, màn hình đăng ký sẽ hiển thị tiếng Anh (mặc định).

---

## Proposed Changes

### Phase 1: Thêm string resources mới vào `values/strings.xml`

#### [MODIFY] [strings.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/values/strings.xml)

Thêm ~40 string mới cho tất cả nội dung hardcoded:

```xml
<!-- Common -->
<string name="common_required">Required</string>
<string name="common_ok">OK</string>
<string name="common_items_count">%1$d ITEMS</string>

<!-- Auth errors & messages -->
<string name="auth_error_google">Google sign in failed: %1$s</string>
<string name="auth_error_failed">Authentication Failed.</string>
<string name="auth_error_login_failed">Login failed: %1$s</string>
<string name="auth_error_email_for_reset">Enter email to reset password</string>
<string name="auth_success_reset_sent">Password reset email sent</string>
<string name="auth_error_min_password">Minimum 8 characters</string>
<string name="auth_error_password_mismatch">Passwords do not match</string>
<string name="auth_success_register">Registration successful! Please sign in.</string>
<string name="auth_error_register_failed">Registration failed: %1$s</string>

<!-- Focus - Task selection -->
<string name="focus_select_task">Select a task</string>
<string name="focus_tap_to_select">Tap to select</string>
<string name="focus_currently_focusing">Focusing</string>
<string name="focus_no_task_selected">No task selected</string>
<string name="focus_select_music_first">Please select music first</string>

<!-- Focus - Task completion -->
<string name="focus_task_complete_title">Completed!</string>
<string name="focus_task_complete_msg">Excellent! You have completed this task.</string>

<!-- Focus - Tomato growth -->
<string name="tomato_seed">Seed has just been planted…</string>
<string name="tomato_sprout">A green sprout is emerging!</string>
<string name="tomato_growing">The plant is growing strong…</string>
<string name="tomato_flowering">Flowers blooming, fruit coming soon!</string>
<string name="tomato_almost_ripe">Tomato is almost ripe! 🍅</string>
<string name="tomato_complete">🍅 Pomodoro complete!</string>
<string name="tomato_dead">Plant has died… 😢 Try again next time!</string>

<!-- Timer Phase labels -->
<string name="phase_focus">Focus</string>
<string name="phase_short_break">Short Break</string>
<string name="phase_long_break">Long Break</string>

<!-- Tasks -->
<string name="task_enter_name">Please enter a task name</string>
<string name="task_add_title_hint">Task Title</string>
<string name="task_add_note_hint">Note (Optional)</string>
<string name="task_picker_title">Choose a task</string>

<!-- Music -->
<string name="music_local_category">From device</string>

<!-- Stats -->
<string name="stats_range_week">WEEK</string>
<string name="stats_range_month">MONTH</string>

<!-- Misc -->
<string name="press_back_again_to_exit">Press back again to exit</string>
```

---

### Phase 2: Cập nhật `values-vi/strings.xml` — bổ sung tất cả key thiếu

#### [MODIFY] [strings.xml (vi)](file:///d:/SE114/TomaFlow-App/app/src/main/res/values-vi/strings.xml)

Thêm bản dịch tiếng Việt cho **tất cả key mới** (Phase 1) + các key đã thiếu:

```xml
<!-- Common -->
<string name="common_required">Bắt buộc</string>
<string name="common_ok">OK</string>
<string name="common_items_count">%1$d MỤC</string>

<!-- Auth errors -->
<string name="auth_error_google">Đăng nhập Google thất bại: %1$s</string>
<string name="auth_error_failed">Xác thực thất bại.</string>
<string name="auth_error_login_failed">Đăng nhập thất bại: %1$s</string>
<string name="auth_error_email_for_reset">Nhập email để đặt lại mật khẩu</string>
<string name="auth_success_reset_sent">Email đặt lại mật khẩu đã gửi</string>
<string name="auth_error_min_password">Tối thiểu 8 ký tự</string>
<string name="auth_error_password_mismatch">Mật khẩu không khớp</string>
<string name="auth_success_register">Đăng ký thành công! Hãy đăng nhập.</string>
<string name="auth_error_register_failed">Đăng ký thất bại: %1$s</string>

<!-- Focus - Task -->
<string name="focus_select_task">Chọn công việc</string>
<string name="focus_tap_to_select">Nhấn để chọn</string>
<string name="focus_currently_focusing">Đang tập trung</string>
<string name="focus_no_task_selected">Chưa chọn công việc</string>
<string name="focus_select_music_first">Vui lòng chọn nhạc trước</string>

<!-- Focus - Task completion -->
<string name="focus_task_complete_title">Hoàn thành!</string>
<string name="focus_task_complete_msg">Tuyệt vời! Bạn đã hoàn thành công việc này.</string>

<!-- Tomato growth -->
<string name="tomato_seed">Hạt giống vừa được gieo...</string>
<string name="tomato_sprout">Mầm xanh đang nhú lên!</string>
<string name="tomato_growing">Cây đang lớn mạnh...</string>
<string name="tomato_flowering">Hoa nở rồi, sắp có quả!</string>
<string name="tomato_almost_ripe">Cà chua sắp chín rồi! 🍅</string>
<string name="tomato_complete">🍅 Pomodoro hoàn thành!</string>
<string name="tomato_dead">Cây đã chết... 😢 Hãy cố lần sau!</string>

<!-- Timer Phase -->
<string name="phase_focus">Tập trung</string>
<string name="phase_short_break">Nghỉ ngắn</string>
<string name="phase_long_break">Nghỉ dài</string>

<!-- Tasks -->
<string name="task_enter_name">Vui lòng nhập tên công việc</string>
<string name="task_add_title_hint">Tên công việc</string>
<string name="task_add_note_hint">Ghi chú (Tuỳ chọn)</string>
<string name="task_picker_title">Chọn công việc</string>

<!-- Music -->
<string name="music_local_category">Từ thiết bị</string>

<!-- Stats -->
<string name="stats_range_week">TUẦN</string>
<string name="stats_range_month">THÁNG</string>

<!-- Missing keys from base -->
<string name="reg_confirm_password">Xác nhận mật khẩu</string>
<string name="stats_breakdown">Phân tích phiên</string>
<string name="stats_no_data">Chưa có phiên nào</string>
<string name="stats_completed">Hoàn thành</string>
<string name="stats_failed">Thất bại</string>
<string name="settings_dark_mode">Chế độ tối</string>
<string name="settings_dark_mode_desc">Chuyển đổi giữa giao diện sáng và tối.</string>
<string name="nav_rewards">PHẦN THƯỞNG</string>
<string name="auth_google">Đăng nhập bằng Google</string>
<string name="placeholder_task_title">Chưa có công việc</string>
<string name="placeholder_task_subtitle">Chọn một công việc để bắt đầu</string>

<!-- Notifications -->
<string name="notification_channel_timer_name">Bộ đếm Pomodoro</string>
<string name="notification_channel_timer_desc">Hiển thị bộ đếm và điều khiển</string>
<string name="notification_channel_sound_name">Cảnh báo bộ đếm</string>
<string name="notification_channel_sound_desc">Phát âm thanh khi hoàn thành giai đoạn</string>
<string name="notification_timer_title">Bộ đếm TomaFlow</string>
<string name="notification_action_pause">Tạm dừng</string>
<string name="notification_action_resume">Tiếp tục</string>
<string name="notification_action_skip">Bỏ qua</string>
<string name="notification_focus_complete_title">Hoàn thành tập trung!</string>
<string name="notification_break_complete_title">Hết giờ nghỉ!</string>
<string name="notification_focus_complete_msg">Phiên tập trung %1$d hoàn thành. Đến giờ nghỉ!</string>
<string name="notification_break_complete_msg">Hết giờ nghỉ. Sẵn sàng tập trung?</string>

<!-- Misc -->
<string name="press_back_again_to_exit">Nhấn back lần nữa để thoát</string>
```

---

### Phase 3: Sửa Java code — thay hardcoded strings bằng `getString()`

#### [MODIFY] [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java)

Thay tất cả chuỗi hardcoded:
- `"Chọn công việc"` → `getString(R.string.focus_select_task)`
- `"Nhấn để chọn"` → `getString(R.string.focus_tap_to_select)`
- `"Đang tập trung"` → `getString(R.string.focus_currently_focusing)`
- `"Vui lòng chọn nhạc trước"` → `getString(R.string.focus_select_music_first)`
- `"Hoàn thành!"` → `getString(R.string.focus_task_complete_title)`
- Tomato labels → `getString(R.string.tomato_seed)`, v.v.
- Phase display name → `getString(R.string.phase_focus)` (xem Phase 4)

#### [MODIFY] [LoginActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java)

Thay tất cả Toast/setError bằng `getString(R.string.xxx)` hoặc `getString(R.string.xxx, param)`.

#### [MODIFY] [RegisterActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/RegisterActivity.java)

- Thêm `attachBaseContext` override
- Thay tất cả setError/Toast

#### [MODIFY] [TasksFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TasksFragment.java)

- Line 102–103: `" ITEMS"` → `getString(R.string.common_items_count, count)`
- Line 139: Toast → `getString(R.string.task_enter_name)`

#### [MODIFY] [TaskPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TaskPickerActivity.java)

- Line 63: → `getString(R.string.focus_no_task_selected)`

#### [MODIFY] [MusicPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/music/MusicPickerActivity.java)

- Line 140: `"Từ thiết bị"` → `getString(R.string.music_local_category)`

#### [MODIFY] [MainActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/MainActivity.java)

- Line 46: → `getString(R.string.press_back_again_to_exit)`

---

### Phase 4: Sửa Phase enum — hỗ trợ locale

#### [MODIFY] [TimerUtils.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/utils/TimerUtils.java)

Sửa `getPhaseLabelLocalized()` để tra cứu từ string resources thay vì dùng `phase.getDisplayName()`:

```java
public static String getPhaseLabelLocalized(Context context, Phase phase) {
    if (context == null || phase == null) return "Unknown";
    switch (phase) {
        case FOCUS:       return context.getString(R.string.phase_focus);
        case SHORT_BREAK: return context.getString(R.string.phase_short_break);
        case LONG_BREAK:  return context.getString(R.string.phase_long_break);
        default:          return phase.getDisplayName();
    }
}
```

#### [MODIFY] [FocusFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/focus/FocusFragment.java#L445)

Line 445 (`mTvSessionLabel.setText(currentPhase.getDisplayName())`) → dùng `TimerUtils.getPhaseLabelLocalized(getContext(), currentPhase)`.

#### [MODIFY] [NotificationHelper.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/utils/NotificationHelper.java#L75)

Line 75 (`TimerUtils.getPhaseLabel()`) → `TimerUtils.getPhaseLabelLocalized(mContext, ...)`.

---

### Phase 5: Sửa Layout XML — chuyển hardcoded text sang `tools:text` hoặc `@string/`

#### Layout text là **mock data** (sẽ được code set lại) → chuyển sang `tools:text`
- `fragment_tasks.xml`: `"04"`, `"12"`, `"85%"`, `"3 ITEMS"`, `"1 ITEM"`
- `fragment_stats.xml`: `"32h 14m"`, `"74"`, `"Wed 8.2h"`
- `fragment_rewards.xml`: `"Elena Rodriguez"`, `"ER"`, `"Level 42"`, `"1,240"`, `"98%"`, v.v.
- `fragment_profile.xml`: tương tự
- `fragment_settings.xml`: `"25m"`, `"5m"`, `"15m"` (giá trị slider được set bởi code)
- `item_task.xml`, `item_task_picker.xml`, `item_badge.xml`, `item_builtin_track.xml`: mock data

#### Layout text là **label cố định** → thêm key `@string/xxx`
- `fragment_stats.xml`: `"Stats"` → `@string/stats_title`; `"WEEK"` → `@string/stats_range_week`; `"MONTH"` → `@string/stats_range_month`; `"FOCUS"`, `"• BREAKS"`
- `fragment_settings.xml`: `"5 MIN"`, `"90 MIN"`, v.v. → `@string/settings_slider_min_5`, v.v. (hoặc giữ nguyên vì đây là nhãn kỹ thuật không cần dịch)
- `fragment_rewards.xml`: `"RANKING"`, `"HOURS"`, `"STREAK"` → `@string/rewards_ranking`, v.v.
- `fragment_profile.xml`: `"RANKING"`, `"HOURS"`, v.v.
- `bottom_sheet_add_task.xml`: hint `"Task Title"` → `@string/task_add_title_hint`; `"Note (Optional)"` → `@string/task_add_note_hint`
- `layout_task_picker_sheet.xml`: `"Chọn công việc"` → `@string/task_picker_title`

> [!NOTE]
> Các label slider range (`"5 MIN"`, `"90 MIN"`, `"1 MIN"`, `"15 MIN"`, `"10 MIN"`, `"45 MIN"`) và toggle button (`"EN"`, `"VI"`) là các nhãn kỹ thuật ngắn, sẽ giữ nguyên không dịch vì chúng là đơn vị phổ quát (MIN) hoặc mã ngôn ngữ cố định (EN/VI).

---

## Verification Plan

### Automated Tests
```bash
./gradlew lint 2>&1 | grep -i "HardcodedText"
```
→ Kiểm tra không còn cảnh báo `HardcodedText` trên các file đã sửa.

### Manual Verification
1. Build và chạy app
2. Ở Settings, bấm **EN** → kiểm tra tất cả màn hình hiển thị tiếng Anh
3. Bấm **VI** → kiểm tra tất cả màn hình hiển thị tiếng Việt
4. Kiểm tra:
   - Auth (Login, Register) — labels, errors, toasts
   - Focus — tomato labels, task selection, music toast, phase label, notification
   - Tasks — add task dialog hints, item count label
   - Stats — tab labels (WEEK/MONTH), chart labels
   - Settings — tất cả labels
   - Rewards/Profile — ranking, hours, streak labels
5. Tắt/mở app → locale vẫn giữ nguyên
