---
version: 1
slug: "docs-design-s12-confirm-html"
primary_target: "docs/design/s12-confirm.html"
related_targets: []
---

# S12 Thẻ xác nhận

Scope: bottom sheet toàn màn hình hiện sau khi AI phân tích ảnh/chat (Operate), mockup HTML tại docs/design/s12-confirm.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token.
Người dùng: phần lớn "liếc rồi lưu"; độ tin cậy chỉ hiện khi thấp. Thứ tự ưu tiên bữa: chọn tay > meal_type của AI > suy từ giờ (05 §4).

## Direction contract

THESIS: thẻ xác nhận là một hoá đơn nhanh để ký, không phải form để điền; mặc định đúng, chỉnh sửa nằm sẵn nhưng im lặng.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, tông bữa meal-*, màu dữ liệu đạm/tinh bột/béo, hổ phách = vượt nhẹ / cần kiểm tra).
STORY: thấy tổng kcal, giờ ăn · bữa, tác động lên ngày; nếu ổn thì bấm Lưu bằng ngón cái; nếu AI không chắc thì thấy ngay câu hỏi và món cần kiểm tra ở đầu danh sách.
FIRST VIEWPORT: thanh trên (X huỷ, "Xác nhận món", ⋮); thumbnail 64dp + tổng kcal 36sp + macro inline; chip "🕐 giờ · ô tông bữa · Bữa ▾" (mở sheet chọn giờ/bữa); thẻ "Hôm nay sau khi lưu" (thanh làn chia đoạn theo tông bữa, phần mới nhạt có viền, vạch mục tiêu, làn vượt riêng); nhận xét AI một dòng hoặc thẻ câu hỏi làm rõ màu hổ phách; danh sách món (món cần kiểm tra lên đầu, nhãn + giả định dạng dòng chữ; hàng hệ số ½ ×1 1½ 2 ✎ 48dp chỉ tô ô đang chọn; nút lưu vào Món của tôi; xoá bằng vuốt + Hoàn tác); nút Lưu 56dp toàn chiều rộng ghim đáy "Lưu vào bữa X · N kcal".
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: thanh làn "Hôm nay sau khi lưu" là vòng calo S10 duỗi thẳng: cùng tông bữa, đầu đoạn vuông, khe 3dp, phần sắp thêm nhạt có viền cùng tông (đổi từ vạch chéo sau khi bộ dò đánh dấu sọc là trang trí), phần vượt chạy trên làn riêng màu trạng thái như vòng trong của S10. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
