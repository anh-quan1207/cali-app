# 07 — Lộ trình & câu hỏi mở

## 1. Thứ tự làm việc

```
Đặc tả (xong) → Thiết kế (wireframe → UI kit → màn chính) → Code theo giai đoạn
                     └── giai đoạn 1 (logic tính toán) có thể bắt đầu song song với thiết kế
```

## 2. Giai đoạn

| GĐ | Nội dung | Kết quả dùng được |
|---|---|---|
| 0 | **Thiết kế**: wireframe các luồng chính, bảng màu / typography / component, mockup toàn bộ màn S01–S45 (xong, xem docs/design/00-direction.md) | Bộ thiết kế để code theo |
| 1 | Khung project (Gradle, Hilt, Room, DataStore, theme, navigation) + `NutritionCalculator` + test | Build chạy được, logic tính đã kiểm chứng |
| 2 | Onboarding S01–S03 + Cài đặt hồ sơ S41 | Nhập thông tin → ra calo mục tiêu |
| 3 | Màn Hôm nay S10 + nhập tay + Món của tôi (S13, S14, S34) + cân nặng cơ bản | Dùng được hằng ngày không cần AI |
| 4 | AI: SecureKeyStore, AnthropicProvider, PromptBuilder, Validator; Camera S11 + Thẻ xác nhận S12; Chat S20; Cài đặt AI S42 | Ghi món bằng ảnh / chat |
| 5 | Nước: theo dõi, nhắc nhở, thông báo có nút, S15, S43 | Nhắc uống nước hoạt động |
| 6 | Lịch sử, thống kê, biểu đồ cân nặng (S30–S33), tính lại mục tiêu; **Tổng kết tuần S35** (chỉ số + điểm + nhận xét AI + thông báo Thứ Hai) | Theo dõi tiến độ, đánh giá tuần |
| 7 | Sao lưu / khôi phục, hoàn thiện, xử lý lỗi, giao diện tối | Bản 1.0 |
| 8 | Giai đoạn 2: widget, copy bữa/ngày, thống kê chi phí AI, tổng kết tháng, chia sẻ ảnh tổng kết | |

Sau giai đoạn 3 app đã dùng thật được; sau giai đoạn 4 là có giá trị cốt lõi. Có thể đảo giai đoạn 4 lên trước 3 nếu muốn thử AI sớm.

Kế hoạch chi tiết từng giai đoạn (việc, thứ tự, tiêu chí xong, bộ kiểm tra tay, tiến độ): [09-plan.md](09-plan.md). 09 đề xuất đưa sao lưu JSON lên giai đoạn 3.

## 3. Câu hỏi mở

| # | Câu hỏi | Mặc định đang giả định |
|---|---|---|
| Q1 | Dùng hãng AI nào (Claude / OpenAI / Gemini)? | Claude (`claude-sonnet-5-5` cho ảnh, `claude-haiku-5-5` cho chat) |
| Q2 | Phong cách giao diện mong muốn (tối giản, nhiều màu, tham khảo app nào)? | Material 3, tối giản, màu chủ đạo chàm #4355B9 (đã chốt 13/11/2026; xanh lá chỉ là màu dữ liệu cho tinh bột). Xem docs/design/00-direction.md |
| Q3 | Có cần ghi món cho người khác / nhiều hồ sơ không? | Không |
| Q4 | Có muốn lưu ảnh món ăn đầy đủ (album bữa ăn) không? | Chỉ thumbnail 256 px |
| Q5 | Ngày bắt đầu tuần trong thống kê & tổng kết tuần? | Thứ Hai → Chủ Nhật |
| Q8 | Trọng số điểm tuần (Calo 40 / Đạm 20 / Nước 20 / Ghi chép 20) có phù hợp? | Như đề xuất ở 08 §4 |
| Q6 | Có cần nhắc ghi bữa ăn (ngoài nhắc nước) không? | Không trong MVP |
| Q7 | Tên app & icon chính thức? | Tên "Cali" (đã chốt 16/11/2026); icon chưa chốt |
