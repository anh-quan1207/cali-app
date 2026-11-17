# 05 — Màn hình & UX

Tài liệu này mô tả **nội dung và hành vi** của từng màn hình, chưa phải thiết kế giao diện. Bộ thiết kế (wireframe → UI) sẽ dựa trên tài liệu này.

## 1. Sơ đồ điều hướng

```
Mở app
 ├─ chưa onboarding → [S01 Onboarding] → [S02 Kết quả] → [S03 Thiết lập nhanh] ─┐
 └─ đã onboarding ───────────────────────────────────────────────────────────────┤
                                                                                   ▼
Bottom navigation (4 tab):
 [S10 Hôm nay]   [S20 Chat AI]   [S30 Lịch sử]   [S40 Cài đặt]

Từ S10:  📷 → [S11 Camera] → [S12 Thẻ xác nhận]
         💬 → S20
         +  → [S13 Thêm nhanh] → (Món của tôi / Nhập tay) → S12
         chạm món → [S14 Sửa món]
         chạm nước → [S15 Chi tiết nước trong ngày]
         chạm cân nặng → [S31 Cân nặng]
Từ S30:  [S31 Cân nặng] [S32 Thống kê] [S33 Chi tiết ngày] [S34 Món của tôi] [S35 Tổng kết tuần]
Thông báo sáng Thứ Hai / thẻ trên S10 (T2–T3) → S35
Từ S40:  [S41 Hồ sơ & mục tiêu] [S42 AI] [S43 Nước & nhắc nhở] [S44 Bữa ăn] [S45 Dữ liệu]
```

## 2. Chi tiết màn hình

### S01 — Onboarding (6 bước)
Thanh tiến độ ở trên, nút **Tiếp** to ở dưới (vô hiệu khi chưa hợp lệ), nút Quay lại.

| Bước | Nội dung | Điều khiển |
|---|---|---|
| 1 | Chào mừng + "Bạn là" | 2 thẻ chọn Nam / Nữ, chọn năm sinh (wheel picker) |
| 2 | Chiều cao, cân nặng | Ô số có đơn vị, bàn phím số; hiện BMI tức thời bên dưới |
| 3 | Mức vận động | 5 thẻ có biểu tượng + mô tả ngắn (02 §3) |
| 4 | Mục tiêu | 3 thẻ: Giảm cân / Giữ cân / Tăng cân |
| 5 | Cân mong muốn + tốc độ | Ô số cân mục tiêu (hiện BMI mục tiêu); chọn tốc độ dạng segmented (02 §4), mỗi tốc độ hiện kcal/ngày và ngày dự kiến tương ứng |
| — | Nếu "Giữ cân" thì bỏ qua bước 5 | |

Màn có link "Khôi phục từ bản sao lưu" ở bước 1.

### S02 — Kết quả
- Số lớn: **1740 kcal / ngày**.
- Lưới chỉ số: BMI (kèm phân loại màu), BMR, TDEE, nước/ngày.
- 3 thanh macro: Đạm 122 g · Tinh bột 196 g · Béo 52 g.
- Dòng dự kiến: "Đạt 68 kg vào khoảng 19/02/2027 (~14 tuần)".
- Cảnh báo (nếu có) trong hộp màu vàng.
- Mục "Cách tính" có thể mở rộng: giải thích ngắn BMR, TDEE.
- Dòng nhỏ: "Số liệu là ước lượng, không thay thế tư vấn y tế."
- Nút **Bắt đầu**.

### S03 — Thiết lập nhanh (có thể "Để sau")
- Nhập API key (ô ẩn ký tự, nút dán) + nút "Kiểm tra".
- Bật nhắc uống nước → xin quyền thông báo (Android 13+).

### S10 — Hôm nay
Thứ tự từ trên xuống:
1. **Thanh ngày**: "Hôm nay, 13/11" + mũi tên ‹ ›, chạm để mở lịch chọn ngày.
2. **Vòng calo**: giữa vòng là **còn lại** (số lớn), dưới là "Đã ăn 820 / 1740". Vượt mục tiêu → vòng đổi màu cảnh báo, chữ "Vượt 120 kcal".
3. **3 thanh macro** ngang.
4. **Hai nút hành động chính**: 📷 Chụp món · 💬 Chat AI. Nút phụ: + Thêm nhanh.
5. **Danh sách bữa**: Sáng / Trưa / Tối / Phụ — mỗi bữa là thẻ có tổng kcal, các món sắp theo giờ ăn (giờ ăn, tên, khẩu phần, kcal, icon nguồn AI/tay), nút + nhỏ để thêm thẳng vào bữa đó.
6. **Thẻ nước**: thanh tiến độ "1250 / 2600 ml", nút +250, +500.
7. **Thẻ cân nặng**: cân gần nhất, thay đổi so với tuần trước, nút "Ghi cân".

Thao tác: vuốt trái 1 món → xóa (có Hoàn tác); nhấn giữ → Sửa / Chuyển bữa / Lưu vào Món của tôi.

Trạng thái trống: "Chưa ghi món nào hôm nay. Chụp bữa ăn đầu tiên nhé!"

### S11 — Camera
- Xem trước camera toàn màn hình, nút chụp, nút chọn từ thư viện, bật/tắt đèn.
- Sau khi chụp: xem lại ảnh + ô ghi chú tùy chọn + chọn bữa → **Phân tích**.
- Đang phân tích: ảnh mờ + hiệu ứng quét + chữ "Đang nhận diện món ăn…", nút Hủy.

### S12 — Thẻ xác nhận (bottom sheet toàn màn hình)
- Ảnh thumbnail (nếu có), tổng kcal + macro của cả lần ghi, chip **giờ ăn · bữa** (tự gán theo §4, chạm để sửa).
- Nhận xét của AI (nếu có), câu hỏi làm rõ + ô trả lời (nếu có).
- Danh sách món, mỗi món:
  - Tên (sửa được), mô tả khẩu phần, kcal, P/C/F.
  - Chip hệ số: ½ · 1 · 1½ · 2 · ✎ (nhập gram).
  - Chấm độ tin cậy (xanh/vàng/đỏ), chạm để xem giả định của AI.
  - Checkbox "Lưu vào Món của tôi"; nút xóa.
- "+ Thêm món" (tay hoặc từ Món của tôi).
- Nút **Lưu vào nhật ký** (chính), Hủy.

### S13 — Thêm nhanh
- Ô tìm kiếm (không dấu) trên Món của tôi.
- Tab: Gần đây · Hay dùng · Yêu thích.
- Nút "Nhập tay" → form tên, kcal, gram, P/C/F.
- Chọn món → chọn khẩu phần → vào S12 (không gọi AI).

### S14 — Sửa món
Giống một dòng của S12 nhưng cho món đã lưu: sửa tên, khẩu phần, kcal, macro, bữa; Xóa.

### S15 — Nước trong ngày
Danh sách các lần uống (giờ, ml), xóa / sửa, thêm với dung tích tùy ý. Chân màn: một nút +250 ml, nút Xoá lần vừa thêm (có Hoàn tác), nút + lượng khác (09 §8 G5-8).

### S20 — Chat AI
- Luồng chat theo ngày (đổi ngày ở thanh trên cùng).
- Ô nhập ở dưới + nút đính ảnh (camera / thư viện) + gửi.
- Gợi ý nhanh khi luồng trống: "Trưa nay mình ăn…", "Tối nay còn bao nhiêu calo, nên ăn gì?", "Hôm nay đủ đạm chưa?".
- Tin nhắn AI ghi món hiển thị dạng **thẻ món ăn thu gọn** với nút "Xem & lưu" → mở S12. Sau khi lưu, thẻ đổi trạng thái "✓ Đã lưu vào Trưa". Bỏ qua → "Đã bỏ qua".
- Đang chờ: bong bóng "đang gõ…"; lỗi: bong bóng lỗi + "Thử lại".

### S30 — Lịch sử
- Lịch tháng: chấm màu mỗi ngày (xanh = đạt mục tiêu, cam = vượt, xám = chưa ghi đủ).
- Dưới lịch: tóm tắt tuần hiện tại (kcal TB, số ngày đạt).
- Lối vào: **Tổng kết tuần** · Cân nặng · Thống kê · Món của tôi. Chạm nhãn tuần ở cạnh lịch → S35 của tuần đó.
- Chạm ngày → S33 (giống S10 cho ngày đó).

### S31 — Cân nặng
Biểu đồ đường (7N/30N/90N/Tất cả) + đường mục tiêu; danh sách bản ghi; nút + Ghi cân (chọn ngày, nhập kg). Sau khi ghi: hộp thoại tính lại mục tiêu (F09).

### S32 — Thống kê
Chọn Tuần / Tháng: biểu đồ cột kcal theo ngày có vạch mục tiêu; macro trung bình (thanh chồng); nước trung bình; số ngày đạt mục tiêu; thay đổi cân nặng trong kỳ.

### S35 — Tổng kết tuần
Chi tiết ở [08-weekly-review.md §6](08-weekly-review.md#6-màn-hình-s35--tổng-kết-tuần): chọn tuần, thẻ điểm, calo vượt/thiếu + biểu đồ 7 ngày, nước, macro, cân nặng, top món, nhận xét AI.

### S34 — Món của tôi
Danh sách + tìm kiếm + sắp xếp; sửa / xóa / yêu thích; thêm món mới bằng tay.

### S40 — Cài đặt
| Mục | Nội dung |
|---|---|
| S41 Hồ sơ & mục tiêu | Sửa các trường onboarding; ghi đè kcal & macro %; công tắc "Tự tính lại khi ghi cân" |
| S42 AI | Hãng, API key (ẩn, chỉ hiện 4 ký tự cuối), model ảnh, model chat, Kiểm tra kết nối; **System prompt** (trình soạn thảo toàn màn hình, Khôi phục mặc định); **Ghi chú cá nhân**; Thống kê sử dụng AI (F16) |
| S43 Nước & nhắc nhở | Mục tiêu (tự động / tùy chỉnh), dung tích 2 nút nhanh, bật nhắc, khung giờ, chu kỳ, kiểm tra quyền thông báo; thông báo tổng kết tuần (bật/tắt, giờ); tự tạo nhận xét AI tuần |
| S44 Bữa ăn | Khung giờ từng bữa để tự gợi ý |
| S45 Dữ liệu | Xuất / Nhập JSON, lưu thumbnail bật/tắt, Xóa toàn bộ dữ liệu |
| Giới thiệu | Phiên bản, tuyên bố miễn trừ y tế |

## 3. Thông báo nhắc uống nước

- Tiêu đề: "Uống nước thôi 💧"; nội dung: "Bạn đã uống 1250/2600 ml hôm nay."
- Nút hành động: **+250 ml** (dung tích nhỏ đã cài) · **Để sau** (nhắc lại sau 30 phút).
- Bấm vào thông báo → mở S10.
- Kênh thông báo riêng "Nhắc uống nước" để người dùng tắt độc lập.

## 4. Thời điểm ăn & tự gán bữa

### 4.1 Ghi thời điểm
Mỗi món lưu **thời điểm ăn** (`eaten_at`). Giá trị mặc định lấy theo thứ tự:

| Cách ghi | Thời điểm mặc định |
|---|---|
| Chụp ảnh bằng camera trong app | Lúc bấm chụp |
| Chọn ảnh từ thư viện | Giờ chụp trong EXIF (`DateTimeOriginal`, đọc **trước** khi xóa EXIF); không có EXIF → lúc chọn ảnh |
| Chat | Lúc gửi tin nhắn |
| Món của tôi / nhập tay | Lúc bấm thêm |
| Ghi cho ngày khác hôm nay | Giờ giữa khung của bữa được chọn (vd Trưa → 12:00) |

**Người dùng nói rõ giờ trong chat / ghi chú** ("sáng nay 7h ăn…", "hôm qua tối ăn…") → AI trả `eaten_time` / `eaten_date` / `meal_type`, app dùng các giá trị này thay cho mặc định.

### 4.2 Tự gán bữa theo giờ ăn (mặc định, sửa được ở S44)

| Bữa | Khung giờ |
|---|---|
| Sáng | 05:00 – 09:59 |
| Trưa | 10:00 – 13:59 |
| Phụ | 14:00 – 17:29 |
| Tối | 17:30 – 21:59 |
| Phụ | 22:00 – 04:59 |

### 4.3 Thứ tự ưu tiên khi quyết định bữa
1. Người dùng **tự chọn bữa** trên Thẻ xác nhận / Sửa món → giữ nguyên, không tự đổi nữa.
2. AI trả `meal_type` (người dùng nói rõ "bữa trưa", "ăn sáng") → dùng giá trị đó.
3. Còn lại → suy ra từ giờ của `eaten_at` theo bảng 4.2.

### 4.4 Hiển thị & chỉnh sửa
- Thẻ xác nhận (S12) có chip **🕐 12:35 · Trưa**; chạm để đổi giờ (time picker) hoặc bữa. Đổi giờ → bữa tự cập nhật theo bảng, **trừ khi** người dùng đã chọn bữa thủ công.
- Màn Hôm nay (S10): mỗi món hiện giờ ăn nhỏ bên cạnh tên; các món trong một bữa sắp xếp theo giờ ăn.
- Ngày của món = ngày của `eaten_at` (món ăn lúc 23:30 thuộc ngày đó, bữa Phụ).
- Ngữ cảnh gửi AI liệt kê món kèm giờ ăn, để AI biết được nhịp ăn (vd "ăn tối muộn 22:30").

## 5. Nguyên tắc UX chung

- Hành động phổ biến nhất (chụp món) luôn cách tối đa **1 chạm** từ màn Hôm nay.
- Mọi thao tác xóa có **Hoàn tác** (snackbar) thay vì hộp thoại xác nhận, trừ xóa toàn bộ dữ liệu.
- Số liệu kcal hiển thị số nguyên; macro số nguyên + "g".
- Màu ngữ nghĩa: xanh = trong mục tiêu, cam = vượt nhẹ (≤ 10%), đỏ = vượt nhiều.
- Hỗ trợ cỡ chữ hệ thống lớn, vùng chạm ≥ 48 dp, giao diện tối.
