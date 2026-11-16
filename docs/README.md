# Cali — Bộ tài liệu đặc tả

App Android cá nhân để **tính và kiểm soát calo**, ghi món ăn bằng **AI (chụp ảnh / chat)**, kèm **nhắc uống nước**. Không có phần tập luyện. Chỉ dùng trên thiết bị của chủ app, không phát hành Play Store, không có backend.

## Mục lục

| # | Tài liệu | Nội dung |
|---|---|---|
| 01 | [Yêu cầu sản phẩm](01-product-requirements.md) | Mục tiêu, phạm vi, danh sách tính năng, user story, tiêu chí chấp nhận |
| 02 | [Công thức tính toán](02-calculations.md) | BMR, TDEE, calo mục tiêu, macro, BMI, nước, giới hạn an toàn, ví dụ |
| 03 | [Tích hợp AI](03-ai-integration.md) | Luồng gọi AI, system prompt, JSON schema, xử lý lỗi, chi phí |
| 04 | [Mô hình dữ liệu](04-data-model.md) | Bảng Room, DataStore, quan hệ, quy tắc lưu |
| 05 | [Màn hình & UX](05-screens-ux.md) | Danh sách màn hình, nội dung, trạng thái, điều hướng |
| 06 | [Kiến trúc kỹ thuật](06-architecture.md) | Tech stack, cấu trúc package, thông báo, bảo mật API key |
| 07 | [Lộ trình & câu hỏi mở](07-roadmap.md) | Các giai đoạn, thứ tự làm, quyết định còn chờ chốt |
| 08 | [Tổng kết & đánh giá tuần](08-weekly-review.md) | Chỉ số tuần, điểm tuần, nhận xét AI, màn S35 |
| 09 | [Kế hoạch thực hiện](09-plan.md) | Việc cụ thể từng giai đoạn, tiêu chí xong, bộ kiểm tra tay, bảng tiến độ |

## Các quyết định đã chốt

- **Nền tảng:** Android native — Kotlin + Jetpack Compose.
- **Người dùng:** 1 người (chủ app), cài APK trực tiếp.
- **Dữ liệu:** lưu hoàn toàn trên máy (Room + DataStore), offline-first. Sao lưu bằng xuất/nhập file JSON.
- **AI:** app gọi **thẳng** AI API, API key nhập trong Cài đặt và được mã hóa trên máy. System prompt có sẵn và sửa được trong app.
- **AI chỉ ước lượng:** mọi kết quả AI đều qua bước **người dùng xác nhận / chỉnh sửa** trước khi lưu.
- **Ngôn ngữ giao diện:** tiếng Việt. Đơn vị: kg, cm, ml, kcal, gram.

## Câu hỏi còn mở

Xem [07-roadmap.md](07-roadmap.md#câu-hỏi-mở). Quan trọng nhất: **chọn hãng AI** (tài liệu đang giả định Claude, nhưng thiết kế cho phép đổi hãng).
