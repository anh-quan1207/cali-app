# 00 — Hướng thiết kế đã chốt

Ngày chốt: 13/11/2026. Quy trình: skill design Impeccable (PRODUCT.md → vòng chọn hướng → mockup → review → DESIGN.md).

## Quyết định

- **Hướng:** chuẩn của thể loại — **Material 3**, làm ở độ tinh xảo ngang **Google Fit / app Pixel** và **MacroFactor**. Không thêm trang trí lạ; "hơi công nghệ" đến từ độ chính xác của số liệu (số tabular, nhãn gọn, đường kẻ mảnh), không phải từ hiệu ứng.
- **Các hướng đã loại:** xanh lá tươi mát, ấm áp kem/serif, số liệu nền tối mono (vòng 1); Vé điện tử, Số dư ví và 6 hướng phụ (vòng 2 của skill).
- **Công cụ:** mockup HTML trong `docs/design/` → chuyển sang Compose (`Color.kt`, `Type.kt`, `Theme.kt`) ở giai đoạn 1.

## Nền tảng thị giác

| Hạng mục | Quyết định |
|---|---|
| Màu | Material You tonal scheme, seed chàm `#4355B9`; có Dynamic Color (Android 12+) làm tùy chọn, scheme tĩnh này là mặc định |
| Màu dữ liệu | Đạm `#006780` / tối `#5FD4F6` · Tinh bột `#8A5100` / `#FFB86E` · Béo `#A0404C` / `#FFB2B9` |
| Trạng thái | Trong mục tiêu = primary · Vượt nhẹ (≤10%) = hổ phách (`warn`) · Vượt nhiều = error. Luôn kèm chữ ("Vượt 14%…"), không chỉ dựa vào màu |
| Chữ | Roboto Flex cho mọi vai trò, số tabular; số còn lại 56sp, wdth 92 |
| Icon | Material Symbols Rounded |
| Bo góc | 16dp thẻ bữa, ô nước/cân, FAB · 12dp nút nhỏ · 8dp chip · tròn cho nút chính |
| Chiều sâu | Tonal surface container, không đổ bóng (trừ FAB) |
| Sáng / tối | Cả hai là scheme chính thức, theo hệ thống |

## Nét riêng (signature)

**Vòng calo chia đoạn theo bữa:** mỗi bữa là một đoạn của vòng, xếp theo giờ ăn (`eaten_at`), cách nhau một khe nhỏ. Vượt mục tiêu thì phần dư chạy trên một vòng trong mảnh hơn, màu cảnh báo; giữa vòng đổi thành "Vượt mục tiêu +250 kcal".

## Màn đã có mockup

| Màn | File | Trạng thái |
|---|---|---|
| S10 Hôm nay | [s10-today.html](s10-today.html) | Đã qua review (ship): 8 khung — trong mục tiêu sáng/tối, vượt nhẹ, vượt nhiều, đã cuộn (FAB), trống, thẻ tổng kết tuần trước (Thứ Hai), cỡ chữ 130%. Thanh macro vượt đã tách phần vượt theo S35 |
| S12 Thẻ xác nhận | [s12-confirm.html](s12-confirm.html) | Đã qua review (8/8 điểm, lỗi màu cuối đã sửa): 9 khung — liếc rồi lưu sáng/tối, cần kiểm tra + câu hỏi, sheet giờ/bữa, ghi từ chat, cảnh báo số liệu + vượt nhiều, không nhận ra món, đang vuốt xoá, Hoàn tác |
| S01–S03 Onboarding, Kết quả, Thiết lập nhanh | [s01-s02-onboarding.html](s01-s02-onboarding.html) | Đã qua review (8/8 điểm + 4 điểm nhỏ cuối đã sửa): 13 khung, có nút xem toàn bộ ở giao diện tối |
| S20 Chat AI | [s20-chat.html](s20-chat.html) | Đã qua review (8/8 điểm): 7 khung — trống, chờ xác nhận, đã lưu + tư vấn, ảnh + câu hỏi làm rõ, lỗi, đính ảnh, ghi cho hôm qua |
| S35 Tổng kết tuần | [s35-weekly.html](s35-weekly.html) | Đã qua review (7 điểm đã sửa): 7 khung — tuần đã kết thúc (toàn trang, sáng), màn đầu tối, tuần đang diễn ra (toàn trang), tuần có ngày chưa ghi, AI đang viết, AI lỗi, dữ liệu đã đổi |
| S30 Lịch sử | [s30-history.html](s30-history.html) | Đã qua review (8 điểm đã sửa): 5 khung — tháng này sáng/tối, đã cuộn (tóm tắt tuần + 4 lối vào), tháng trước có ngày chưa ghi, người mới bắt đầu |
| S31 Cân nặng | [s31-weight.html](s31-weight.html) | Đã qua review (8 điểm đã sửa): 10 khung — 30 ngày toàn trang, tối, 7 ngày, Tất cả, sheet Ghi cân, sheet tính lại mục tiêu, sau khi lưu, lỗi nhập, người mới, sau khi xoá |
| S33 Chi tiết ngày | [s33-day.html](s33-day.html) | Đã qua review (8 điểm đã sửa): 8 khung — ngày Thiếu, Vượt (tối), Đạt, nghi ghi thiếu, chưa ghi (tối), sau khi xoá món trong tuần đã có nhận xét AI, thêm món cho ngày cũ, Đạt · vượt nhẹ |
| S11 Camera | [s11-camera.html](s11-camera.html) | Đã qua review (7 điểm đã sửa): 10 khung — sẵn sàng, thiếu sáng (đèn bật / tắt), xin quyền, Mở Cài đặt, xem lại + ghi chú, đang phân tích, mất mạng, AI quá 60 giây, ảnh thư viện |
| S13 Thêm nhanh · S14 Sửa món · S34 Món của tôi | [s13-s14-s34-food.html](s13-s14-s34-food.html) | Đã qua review (8 điểm đã sửa): 15 khung — thêm nhanh từ “+” và từ thẻ bữa, tìm không dấu, không khớp, nhập tay, sửa tăng / vượt / giảm / ngày cũ, danh sách, vuốt xoá, Hoàn tác, trống, tạo món, sửa món đã lưu |
| S15 Nước | [s15-water.html](s15-water.html) | Đã qua review (7 điểm đã sửa): 11 khung — hôm nay, tối, vượt mục tiêu, nhập lượng khác, lỗi khoảng, sửa, đang vuốt, đã xoá, ngày cũ, thiếu quyền thông báo, cách tính mục tiêu |
| S32 Thống kê | [s32-stats.html](s32-stats.html) | Đã qua review (7 điểm đã sửa): 8 khung — tuần này, tuần đã xong (tối), tháng 11, tháng 10, so với kỳ trước, người mới, chọn ngày trên biểu đồ tháng, so sánh tuần đang diễn ra |
| S40–S45 Cài đặt | [s40-settings.html](s40-settings.html) | Đã qua review (7 điểm đã sửa): 16 khung — S40 sáng/tối, hồ sơ & mục tiêu, tốc độ, Đang dùng / Mới, ghi đè + sheet, AI, lỗi key, system prompt, nước & nhắc nhở, giờ bữa, trùng giờ, dữ liệu, xoá toàn bộ 2 bước |

Quyết định chung mới: tuần / tháng đang diễn ra so với đúng số ngày đầu của kỳ trước (S32 và S35); camera luôn tối; hộp thoại xác nhận chỉ có ở Xoá toàn bộ dữ liệu; ghi đè calo kẹp theo 02 §5 (trần thâm hụt 25% trước, rồi sàn).

Quyết định riêng cho S31: số lớn là cân trung bình tuần (tuần này chỉ thành số TB khi có ≥ 2 lần cân); tiến độ, số kg còn lại và ngày dự kiến tính theo lần cân gần nhất, ngày dự kiến đếm từ hôm nay (02 §9); thay đổi cân không tô màu; đường mục tiêu chỉ vẽ ở đúng giá trị của nó; Ghi cân và tính lại mục tiêu là bottom sheet.

Quyết định riêng cho S33: là S10 cho ngày đã qua; giữa vòng hiện chênh lệch có dấu kèm loại ngày theo 08 §2 ("Thiếu mục tiêu −320", "Vượt mục tiêu +620"), ngày chưa ghi hiện mục tiêu; macro không nói "còn" mà nói "thiếu / đạt / vượt"; sửa món trong tuần đã có nhận xét AI thì thẻ tuần hiện hàng "Tạo lại".

Quyết định riêng cho S30: lịch xếp theo tuần, mỗi tuần một thẻ (đầu thẻ = điểm tuần, chạm mở S35); mỗi ô ngày là một cột calo nhỏ trên vạch mục tiêu chung của hàng, cùng ngữ pháp biểu đồ S35; hôm nay chỉ có viền (không tô) để không lẫn với ngày Thiếu ở giao diện tối; tuần tương lai và tuần trước khi dùng app thu thành một hàng số mờ. Mockup dùng lịch giả định 13/11/2026 là Thứ Năm (thực tế là Thứ Sáu) cho khớp các màn khác; code sẽ theo lịch thật.

Quyết định riêng cho S35: màn con (nút quay lại, không có navigation bar); điểm là số lớn nhất màn (Display Large) với làn điểm 40/20/20/20; cột ngày và thanh macro tách phần vượt ở vạch mục tiêu bằng khe 3dp, phần vượt màu error; ngày "Thiếu" là tông chàm nhạt, không bao giờ hổ phách (hổ phách chỉ cho cờ nghi ghi thiếu); điểm chia cho 7 ngày (hoặc số ngày đã qua) đúng 08 §4, nên ngày chưa ghi kéo điểm xuống; AI chỉ viết lời, các thẻ số liệu không chờ AI.

Quyết định riêng cho S20: mọi kết quả AI là tin nhắn; thẻ ghi món thu gọn rộng cố định 304dp để các trạng thái (chờ, đã lưu, bỏ qua) đổi tại chỗ; thẻ chưa lưu không làm đổi số "còn N" trên thanh trên, chỉ lưu mới đổi; tin nhắn xếp vào luồng theo ngày ăn.

Quyết định riêng cho onboarding: nhanh, gọn, không minh hoạ; chọn thẻ không tự sang bước, luôn bấm Tiếp; S02 đặt kcal/ngày trong vòng calo trống của S10; thang BMI châu Á không tô màu phán xét.

Quyết định riêng cho S12: người dùng thường "liếc rồi lưu", độ tin cậy chỉ hiện khi thấp (nhãn "Cần kiểm tra" màu hổ phách). Màu đỏ chỉ dành cho vượt calo > 10% và macro vượt mục tiêu.

Hệ thống token đầy đủ (màu, chữ, bo góc, khoảng cách, hình học vòng calo, chuyển động) nằm ở [DESIGN.md](../../DESIGN.md) ở gốc repo — dùng file đó khi viết `Theme.kt`.

## Tiếp theo

Đã có mockup cho toàn bộ màn trong 05. Bước kế: giai đoạn 1 code — chuyển DESIGN.md sang Color.kt / Type.kt / Theme.kt, NutritionCalculator theo docs/02, rồi dựng S01–S03 và S10 bằng Compose.
