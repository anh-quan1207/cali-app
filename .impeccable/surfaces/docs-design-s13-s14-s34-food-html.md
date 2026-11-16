---
version: 1
slug: "docs-design-s13-s14-s34-food-html"
primary_target: "docs/design/s13-s14-s34-food.html"
related_targets: []
---

# S13 Thêm nhanh · S14 Sửa món · S34 Món của tôi

Scope: ba màn quanh kho món tự tích luỹ (Operate, ghi nhanh và sửa), mockup HTML tại docs/design/s13-s14-s34-food.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 (S10 "+" → S13 → S12, S14, S34, §4 eaten_at và tự gán bữa), 01 F07 / F08 (Món của tôi, ghi thủ công: tên + kcal bắt buộc, gram và macro tuỳ chọn), 04 (`food_entry` lưu giá trị tuyệt đối, `my_food` có `is_favorite`, `use_count`, `last_used_at`, tìm không dấu qua `name_normalized`), 03 (món AI mang `portion_desc`, `grams`, `confidence`, `assumptions`).
Người dùng: một người, ăn lặp lại nhiều (cà phê sữa đá, cơm tấm sườn, bún bò Huế); muốn thêm lại một món quen trong hai chạm, sửa khẩu phần một món đã ghi mà thấy ngay ngày đổi thế nào, và giữ kho món gọn.

## Direction contract

THESIS: Món của tôi là một sổ món đã đo sẵn; mỗi hàng đọc như một dòng của thẻ xác nhận S12 (tên, khẩu phần, chấm macro, kcal canh phải), và mọi thay đổi lên ngày vẫn nói bằng làn "Hôm nay sau khi lưu", nên thêm nhanh và sửa món không phải màn mới mà là S12 thu gọn hoặc mở lại.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, ba màu dữ liệu, Data-Series Lock, Amber Check, Ledger Rule, chip giờ ăn · bữa, bộ chọn khẩu phần, bookmark toggle, vuốt xoá + Hoàn tác).
STORY: 12:38, còn 430 kcal → chạm "+" trên S10 → sheet Thêm nhanh biết sẽ ghi vào "12:38 · Trưa" → chạm "Trà sữa trân châu" → S12 với món đó, không gọi AI; tìm "bun cha" không có → Hỏi AI hoặc Nhập tay; sau đó chạm bún bò đã ghi → đổi ×1 thành 1½ → làn cho thấy +245 kcal, còn 185; ở Lịch sử mở Món của tôi → ghim yêu thích, vuốt xoá, sửa kcal một món mà biết lịch sử cũ không đổi.
FIRST VIEWPORT: S13 là sheet toàn màn hình kiểu S12: thanh (đóng / "Thêm nhanh" + "Hôm nay · còn 430 kcal" / trống), ô tìm 56dp, chip "12:38 · Trưa" + "Lúc bấm thêm", bộ chọn Gần đây / Hay dùng / Yêu thích, danh sách món một chạm, chân ghim Hỏi AI / Nhập tay. S14: thẻ món S12 với bộ chọn khẩu phần và bookmark, hàng mở "Sửa tên, kcal, macro", làn sau khi lưu có dòng trước/sau, nút Lưu thay đổi. S34: thanh trên quay lại + "Món của tôi", ô tìm, bộ chọn Hay dùng / Gần đây / A–Z, mục Yêu thích ghim đầu, các hàng 3 dòng với số lần dùng, FAB "Thêm món". Trạng thái: tìm có kết quả (tô chữ khớp không dấu), không khớp, form nhập tay có cờ Số liệu lệch, sửa món của ngày đã qua (chip có ngày, làn "12/11 sau khi lưu", dòng đổi loại ngày), danh sách sáng / tối, đang vuốt xoá, vừa xoá có Hoàn tác, trống, sửa món đã lưu.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10 và S12); signature move: phần chênh của một lần sửa vẽ thành đoạn chờ trên làn ngày (khẩu phần tăng từ ×1 lên 1½ là một đoạn nhạt viền 1.5dp nối sau đoạn cũ), và hàng món đã lưu chính là dòng thẻ xác nhận thu gọn, nên một món đọc giống nhau ở S12, S13, S14 và S34. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
