# Phân tích việc tích hợp gói Normal và VIP

Dựa vào kiến trúc hiện tại của TomaFlow (đã có Firebase Auth và Firestore cho người dùng), ứng dụng **hoàn toàn có thể mở rộng** để chia gói người dùng Normal (Miễn phí) và VIP (Cao cấp). Dưới đây là phân tích chi tiết và đề xuất kế hoạch thực hiện.

## 1. Quản lý trạng thái VIP (Data Model)
Hiện tại app lưu dữ liệu người dùng qua `FirebaseUser` và đồng bộ (Tasks, Sessions, Rewards) trên Firestore trong collection `users`.
- **Đề xuất**: Thêm một trường `subscription_tier` (String: "normal" hoặc "vip") vào document của mỗi user trên Firestore.
- Khi đăng nhập (`UserRepository` / `LoginActivity`), hệ thống sẽ tải thông tin này về và lưu cục bộ qua `SharedPreferences` (ví dụ: `is_vip = true/false`) để app truy cập nhanh chóng không cần mạng.

## 2. Phân loại tính năng (Tính năng nào vào gói VIP?)
Dựa trên các chức năng hiện có của app, dưới đây là đề xuất giới hạn tính năng cho người dùng Normal nhằm tạo động lực nâng cấp VIP:

### Âm thanh & Nhạc nền (Music Picker)
- **Normal**: Chỉ được sử dụng một bản nhạc mặc định (ví dụ: `Lofi Chill`). Không được chọn nhạc từ thiết bị.
- **VIP**: Mở khóa toàn bộ nhạc nền có sẵn (`Piano Focus`, `Rain Ambience`) và đặc biệt là tính năng **Chọn nhạc từ thiết bị (Local Music)**.

### Tùy chỉnh đồng hồ (Settings)
- **Normal**: Sử dụng thời gian mặc định của Pomodoro (Focus 25 phút, Nghỉ ngắn 5 phút, Nghỉ dài 15 phút). Vô hiệu hoá các thanh trượt thời gian.
- **VIP**: Toàn quyền tùy chỉnh độ dài phiên tập trung và nghỉ ngơi (qua thanh trượt trong Settings).

### Tính năng độc quyền: Strict Mode
- **VIP**: Được sử dụng **Strict Mode** (Chế độ nghiêm ngặt - khóa nút tạm dừng khi đang chạy đếm ngược), giúp đạt trạng thái làm việc sâu (Deep Work). 
*(Tính năng này hiện tại chỉ có UI, chúng ta có thể kết hợp implement logic Strict Mode dưới dạng tính năng Premium).*

### Quản lý công việc (Tasks)
- **Normal**: Giới hạn số lượng công việc (Task) chưa hoàn thành (Active) tối đa là 5 task.
- **VIP**: Không giới hạn số lượng công việc. Thỏa sức lên kế hoạch.

## 3. Các thay đổi về UI/UX cần thêm

#### [NEW] Màn hình nâng cấp VIP (Premium Activity/Dialog)
- Hiển thị bảng so sánh lợi ích giữa Normal và VIP. 
- Nút "Nâng cấp ngay" (Tạm thời có thể làm nút giả lập thanh toán - Mock payment).

#### [MODIFY] `ProfileFragment`
- Thêm biểu tượng Vương miện (Crown) hoặc nhãn "VIP" bên cạnh tên người dùng nếu họ đã nâng cấp.
- Thêm nút "Upgrade to Premium" nếu người dùng đang ở gói Normal.

#### [MODIFY] Màn hình bị giới hạn (`SettingsFragment`, `MusicPickerActivity`, `TaskPickerActivity`)
- Khi người dùng Normal chạm vào tính năng VIP (ví dụ gạt Strict Mode, chọn bài nhạc khóa, hoặc vượt quá 5 tasks), hiển thị một Popup hoặc Toast chuyển hướng họ tới màn hình Nâng cấp.

---

## User Review Required

> [!IMPORTANT]
> - Bạn có đồng ý với danh sách phân loại tính năng Normal/VIP như đề xuất ở trên không? Bạn có muốn thay đổi tính năng nào ở mục 2 không?
> - Trong phạm vi hiện tại, chức năng thanh toán sẽ chỉ được **giả lập** (Bấm nút "Mua" -> Cập nhật Firestore thành VIP -> Mở khoá tính năng). Bạn có đồng ý hướng này không hay muốn tích hợp cổng thanh toán thật (ví dụ Google Play Billing)?

Xin vui lòng xem xét bản phân tích trên. Nếu bạn đồng ý, tôi sẽ tạo Task list để bắt đầu lập trình.
