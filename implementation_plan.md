# Sửa lỗi Đa ngôn ngữ (i18n) cho TomaFlow App

## Tổng quan

Sau khi quét toàn bộ mã nguồn (XML layout + Java code + strings.xml), phát hiện **3 nhóm vấn đề** cần xử lý:

| Nhóm | Mô tả | Số lượng |
|------|--------|----------|
| **A** | Hardcode text trong Java code | 5 chỗ |
| **B** | Hardcode text trong XML layout | 1 chỗ |
| **C** | Thiếu bản dịch tiếng Việt (keys có trong EN nhưng không có trong VI) | 22 keys |

---

## A. Hardcode text trong Java code

### A1. [TasksFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TasksFragment.java#L101-L104) — `" ITEMS"` (Tiếng Anh)

```diff
 private void updateCounts() {
-    tvActiveCountLabel.setText(activeTasks.size() + " ITEMS");
-    tvDoneCountLabel.setText(doneTasks.size() + " ITEMS");
+    tvActiveCountLabel.setText(getResources().getQuantityString(R.plurals.task_count, activeTasks.size(), activeTasks.size()));
+    tvDoneCountLabel.setText(getResources().getQuantityString(R.plurals.task_count, doneTasks.size(), doneTasks.size()));
 }
```

> [!IMPORTANT]
> Cách tốt nhất là dùng **plurals resource**. Tuy nhiên nếu team muốn đơn giản hơn, có thể dùng format string:
> ```java
> tvActiveCountLabel.setText(getString(R.string.task_items_count, activeTasks.size()));
> ```
> Với string resource: `<string name="task_items_count">%1$d ITEMS</string>` (EN) / `<string name="task_items_count">%1$d MỤC</string>` (VI)

---

### A2. [TaskPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TaskPickerActivity.java#L63) — `"Chưa chọn công việc"` (Tiếng Việt hardcode)

```diff
 findViewById(R.id.btn_clear_task).setOnClickListener(v -> {
-    tvSelectedTaskName.setText("Chưa chọn công việc");
+    tvSelectedTaskName.setText(getString(R.string.task_picker_none_selected));
     Intent result = new Intent();
```

> [!WARNING]
> Đây là lỗi nghiêm trọng: khi người dùng chọn ngôn ngữ Tiếng Anh, nút "Clear task" vẫn hiển thị tiếng Việt.

---

### A3. [MusicPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/music/MusicPickerActivity.java#L140) — `"Từ thiết bị"` (Tiếng Việt hardcode)

```diff
     tvIcon.setText("🎧");
     tvName.setText(track.title);
-    tvCategory.setText("Từ thiết bị");
+    tvCategory.setText(getString(R.string.music_from_device));
```

---

### A4. [TaskAdapter.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TaskAdapter.java#L51) — `"m"` (phút viết tắt)

```diff
 if (task.estimatedMinutes > 0) {
-    holder.tvPomos.setText(task.estimatedMinutes + "m");
+    holder.tvPomos.setText(task.estimatedMinutes + holder.itemView.getContext().getString(R.string.task_duration_unit));
 }
```

---

### A5. [StatsFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/stats/StatsFragment.java#L194-L198) — Chart labels `"Focus"` / `"Break"` (Tiếng Anh)

```diff
-    BarDataSet focusSet = new BarDataSet(focusEntries, "Focus");
+    BarDataSet focusSet = new BarDataSet(focusEntries, getString(R.string.stats_focus_label));
     ...
-    BarDataSet breakSet = new BarDataSet(breakEntries, "Break");
+    BarDataSet breakSet = new BarDataSet(breakEntries, getString(R.string.stats_breaks_label));
```

---

## B. Hardcode text trong XML Layout

### B1. [activity_task_picker.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/activity_task_picker.xml#L22) — `app:title="Chọn công việc"` (Tiếng Việt hardcode)

```diff
     app:navigationIcon="@drawable/ic_back"
     app:navigationIconTint="@color/toma_primary"
-    app:title="Chọn công việc"
+    app:title="@string/task_picker_sheet_title"
     app:titleTextColor="@color/toma_text"
```

---

## C. Thiếu bản dịch Tiếng Việt (`values-vi/strings.xml`)

Các key dưới đây **có trong `values/strings.xml` (EN)** nhưng **không có trong `values-vi/strings.xml`**. Khi người dùng chọn Tiếng Việt, chúng sẽ tự động fallback về Tiếng Anh — gây trải nghiệm thiếu nhất quán.

| # | Key | Giá trị EN | Giá trị VI (đề xuất) |
|---|-----|-----------|---------------------|
| 1 | `auth_google` | Sign in with Google | Đăng nhập bằng Google |
| 2 | `reg_confirm_password` | Confirm password | Xác nhận mật khẩu |
| 3 | `stats_breakdown` | Session Breakdown | Phân tích phiên |
| 4 | `stats_no_data` | No sessions yet | Chưa có phiên nào |
| 5 | `stats_completed` | Completed | Hoàn thành |
| 6 | `stats_failed` | Failed | Thất bại |
| 7 | `settings_dark_mode` | Dark Mode | Chế độ tối |
| 8 | `settings_dark_mode_desc` | Switch between light and dark themes. | Chuyển đổi giữa giao diện sáng và tối. |
| 9 | `nav_rewards` | REWARDS | THÀNH TÍCH |
| 10 | `notification_channel_timer_name` | Pomodoro Timer | Bộ đếm Pomodoro |
| 11 | `notification_channel_timer_desc` | Shows the current timer and controls | Hiển thị bộ đếm và nút điều khiển |
| 12 | `notification_channel_sound_name` | Timer Alerts | Chuông hẹn giờ |
| 13 | `notification_channel_sound_desc` | Plays sound when a phase is complete | Phát âm thanh khi kết thúc một giai đoạn |
| 14 | `notification_timer_title` | TomaFlow Timer | Bộ đếm TomaFlow |
| 15 | `notification_action_pause` | Pause | Tạm dừng |
| 16 | `notification_action_resume` | Resume | Tiếp tục |
| 17 | `notification_action_skip` | Skip | Bỏ qua |
| 18 | `notification_focus_complete_title` | Focus Complete! | Xong phiên tập trung! |
| 19 | `notification_break_complete_title` | Break Complete! | Hết giờ nghỉ! |
| 20 | `notification_focus_complete_msg` | Focus session %1$d complete. Time for a break! | Phiên tập trung #%1$d hoàn thành. Đến lúc nghỉ ngơi! |
| 21 | `notification_break_complete_msg` | Break complete. Ready to focus? | Hết giờ nghỉ. Sẵn sàng tập trung? |
| 22 | `placeholder_task_title` | No Active Task | Chưa có công việc |
| 23 | `placeholder_task_subtitle` | Select a task to start focusing | Chọn công việc để bắt đầu tập trung |

> [!NOTE]
> Ngoài ra cần thêm 1 key mới cho cả EN và VI:
> `task_items_count` → `"%1$d ITEMS"` (EN) / `"%1$d MỤC"` (VI) — dùng để thay thế hardcode ở mục A1.

---

## Các chỗ chấp nhận được (KHÔNG CẦN SỬA)

| Vị trí | Nội dung | Lý do |
|--------|----------|-------|
| `StatsFragment.java:183` | `setText("—")` | Ký tự em dash, không phải ngôn ngữ |
| `StatsFragment.java:156` | `description.setText("")` | Chuỗi rỗng |
| `MusicPickerActivity.java:138` | `setText("🎧")` | Emoji, không phải ngôn ngữ |
| `LoginActivity.java:105` | `setText("EN"/"VI")` | Mã ngôn ngữ quốc tế, hiển thị cố định |
| `fragment_settings.xml:383,391` | `text="EN"`, `text="VI"` | Mã ngôn ngữ cố định |
| `activity_login.xml:38` | `text="VI | EN"` | Nút toggle ngôn ngữ |
| `StatsAggregator.java:17` | `DAY_LABELS = {"Sun","Mon"...}` | Nhãn biểu đồ dùng viết tắt quốc tế |
| `BuiltInTrackCatalog.java` | Tên nhạc `"Lo-Fi Chill"`, `"Tiếng Mưa"` | Tên riêng của bài nhạc, không dịch |

---

## Proposed Changes

### String Resources

#### [MODIFY] [strings.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/values/strings.xml)
- Thêm key `task_items_count` = `%1$d ITEMS`

#### [MODIFY] [strings.xml (vi)](file:///d:/SE114/TomaFlow-App/app/src/main/res/values-vi/strings.xml)
- Bổ sung 23 keys thiếu (22 keys từ bảng C + 1 key mới `task_items_count`)

---

### XML Layout

#### [MODIFY] [activity_task_picker.xml](file:///d:/SE114/TomaFlow-App/app/src/main/res/layout/activity_task_picker.xml)
- Dòng 22: Đổi `app:title="Chọn công việc"` → `app:title="@string/task_picker_sheet_title"`

---

### Java Code

#### [MODIFY] [TasksFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TasksFragment.java)
- Dòng 102-103: Dùng `getString(R.string.task_items_count, size)` thay vì nối chuỗi

#### [MODIFY] [TaskPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TaskPickerActivity.java)
- Dòng 63: Dùng `getString(R.string.task_picker_none_selected)` thay vì hardcode

#### [MODIFY] [MusicPickerActivity.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/music/MusicPickerActivity.java)
- Dòng 140: Dùng `getString(R.string.music_from_device)` thay vì hardcode

#### [MODIFY] [TaskAdapter.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/tasks/TaskAdapter.java)
- Dòng 51: Dùng `getString(R.string.task_duration_unit)` thay vì `"m"`

#### [MODIFY] [StatsFragment.java](file:///d:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/stats/StatsFragment.java)
- Dòng 194, 198: Dùng `getString()` cho chart labels "Focus"/"Break"

---

## Verification Plan

### Manual Verification
- Chuyển đổi ngôn ngữ EN ↔ VI trong Settings, kiểm tra từng màn hình:
  - Màn hình Tasks: label "X ITEMS" / "X MỤC"
  - Màn hình Task Picker: tiêu đề toolbar + nút clear task
  - Màn hình Music Picker: nhãn "Từ thiết bị" / "From device"
  - Màn hình Stats: chart labels
  - Thanh thông báo: tất cả notification text
