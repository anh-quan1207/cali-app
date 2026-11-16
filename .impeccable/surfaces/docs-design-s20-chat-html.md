---
version: 1
slug: "docs-design-s20-chat-html"
primary_target: "docs/design/s20-chat.html"
related_targets: []
---

# S20 Chat AI

Scope: màn Chat AI (Operate), mockup HTML tại docs/design/s20-chat.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 S20, 03 §3.2 (chat tool_choice auto: ghi món hoặc tư vấn), 03 §4 (ngữ cảnh), 05 §4 (giờ ăn từ thời điểm gửi tin / AI đọc từ tin nhắn).
Người dùng: ghi món ngay lúc ăn một tay; hỏi "còn ăn gì được".

## Direction contract

THESIS: chat là một luồng tin nhắn, không phải một form; mọi kết quả AI đều là tin nhắn, và thẻ ghi món là một thẻ thu gọn chỉ đủ để quyết định (giờ · bữa, kcal, còn lại) rồi chuyển sang S12.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, tông bữa meal-*, hổ phách = cần kiểm tra, error-container cho lỗi).
STORY: gõ hoặc gửi ảnh, thấy thẻ món của AI ngay trong luồng, bấm "Xem & lưu" nếu đúng; hỏi tư vấn thì nhận câu trả lời có số theo calo còn lại.
FIRST VIEWPORT: thanh trên (‹ Chat AI ›, dòng phụ "Thứ Năm, 13/11 ▾ · còn N kcal" luôn thấy số còn lại); luồng tin nhắn (bong bóng của bạn tông secondary-container căn phải, bong bóng AI tông surface-low căn trái kèm giờ); thẻ món thu gọn (chip "Bữa · giờ" có ô tông bữa, tổng kcal, danh sách món, macro, thanh "còn lại", nút "Xem & lưu" tonal 48dp + "Bỏ qua" text); ô nhập có nút đính ảnh và gửi 48dp; thanh điều hướng 4 tab với Chat AI được chọn. Trạng thái: trống (3 gợi ý), đang chờ (bong bóng đang gõ), lỗi (error-container + Thử lại), thẻ đã lưu / đã bỏ qua, đính ảnh (camera / thư viện, ghi rõ giờ lấy từ đâu), ghi cho hôm qua.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: thẻ món thu gọn mang theo thanh "còn lại" cùng ngữ pháp với làn "Hôm nay sau khi lưu" của S12, để một cái liếc là biết bữa này làm ngày còn bao nhiêu. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
