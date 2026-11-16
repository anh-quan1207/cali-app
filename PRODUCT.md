# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack
Kotlin + Jetpack Compose + Material 3 (đã chốt trong `docs/06-architecture.md`). Mockup thiết kế làm bằng HTML trước khi chuyển sang Compose.

## Users
Một người duy nhất: chủ app, người Việt, tự cài APK. Thường ghi món **ngay lúc ăn** (ở quán, văn phòng), cầm máy một tay, chụp nhanh rồi cất máy. Mở app nhiều lần mỗi ngày, mỗi lần vài giây.

## Product Purpose
Tính và kiểm soát calo hằng ngày. Vòng lặp: mục tiêu calo/ngày → ghi món (nhanh, bằng AI) → biết còn lại bao nhiêu → theo dõi cân nặng. Thành công là ghi một bữa trong dưới 30 giây và nhìn một lần là biết còn bao nhiêu calo.

## Positioning
Không có cơ sở dữ liệu món ăn hay tính năng tập luyện, cộng đồng, trả phí. AI (chụp ảnh / chat tiếng Việt) thay cho việc nhập tay; mọi kết quả AI đều qua bước người dùng xác nhận / sửa. Toàn bộ dữ liệu nằm trên máy.

## Operating Context
- Màn Hôm nay (S10): thứ mắt nhìn đầu tiên là **số calo còn lại**; mọi thứ khác là phụ.
- Ghi món: chụp → thẻ xác nhận (sửa khẩu phần, giờ ăn, bữa) → lưu. Chat AI theo ngày.
- Mỗi món có giờ ăn (`eaten_at`); bữa tự gán theo giờ (05 §4).
- Nhắc uống nước qua thông báo; tổng kết tuần có điểm 0–100 và nhận xét AI.

## Capabilities and Constraints
- Giao diện tiếng Việt (cần font hiển thị dấu tốt). Đơn vị kg, cm, ml, kcal, g.
- Sáng / tối theo hệ thống; vùng chạm ≥ 48dp; hỗ trợ cỡ chữ lớn.
- Màu ngữ nghĩa: trong mục tiêu / vượt nhẹ (≤10%) / vượt nhiều.
- Offline-first; chỉ AI cần mạng. Số kcal là ước lượng, không phải tư vấn y tế.
- Còn mở: hãng AI (giả định Claude), tên và icon chính thức (tạm "Cali").

## Brand Commitments
Tên tạm "Cali". Người dùng muốn giao diện **đẹp, gọn, hơi công nghệ một chút**. Đã loại 3 hướng: xanh lá Material tươi mát, ấm áp kem/serif, số liệu nền tối mono — cả ba "hơi xấu". Material 3 là nền cấu trúc (đặc tả), màu xanh lá không còn là ràng buộc.

Đã chốt (13/11/2026): đi theo **chuẩn của thể loại — Material 3**, làm ở độ tinh xảo ngang **Google Fit / app Pixel** và **MacroFactor**. Đây là tầm chất lượng phải đạt; không thêm trang trí lạ.

## Evidence on Hand
Đặc tả đầy đủ ở `docs/01`–`08`. Chưa có logo, ảnh món ăn thật hay dữ liệu thật; mọi số liệu trong mockup là dữ liệu mẫu.

## Product Principles
- Một cái nhìn là đủ: số còn lại luôn nổi nhất.
- Ghi món cách tối đa một chạm từ màn Hôm nay.
- AI ước lượng, người dùng quyết định.
- Không phán xét: vượt calo được báo rõ nhưng không trách.

## Accessibility & Inclusion
Cỡ chữ hệ thống lớn, vùng chạm ≥ 48dp, màu trạng thái không chỉ dựa vào sắc độ (kèm chữ/biểu tượng), giao diện tối đầy đủ.
