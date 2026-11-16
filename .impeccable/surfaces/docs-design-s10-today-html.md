---
version: 1
slug: "docs-design-s10-today-html"
primary_target: "docs/design/s10-today.html"
related_targets: []
---

# S10 Hôm nay

Scope: màn Hôm nay của app Android Cali (Operate). Mockup HTML tại docs/design/s10-today.html, sau đó chuyển sang Compose.
Người dùng ghi món ngay lúc ăn, một tay; nhìn đầu tiên là số calo còn lại. Chuẩn so sánh: Google Fit / Pixel apps và MacroFactor.

## Direction contract

THESIS: Material 3 chuẩn của thể loại đếm calo, làm ở độ tinh xảo của app Pixel; từ chối kiểu "app mẫu Google" bằng độ chính xác số liệu kiểu MacroFactor, không bằng trang trí.
OWN-WORLD: Material You tonal scheme từ seed chàm (#4355B9), surface container tonal thay cho bóng đổ, góc 28/16/12dp, Roboto Flex cho mọi chữ với số tabular, Material Symbols Rounded. Ba màu dữ liệu cố định cho đạm / tinh bột / béo; trạng thái: primary = trong mục tiêu, hổ phách = vượt nhẹ, error = vượt nhiều.
STORY: mở app, thấy ngay còn bao nhiêu kcal, biết đã ăn gì lúc mấy giờ, bấm Chụp món bằng ngón cái.
FIRST VIEWPORT: status bar + top app bar sticky (‹ Hôm nay / Thứ Năm, 13/11 ▾ ›, chạm tiêu đề mở lịch; khi cuộn chuyển nền surface-container); vòng calo 220dp ở giữa, số còn lại displayLarge 57sp; hàng Đã ăn / Mục tiêu; 3 macro có "còn / vượt X g"; hàng nút [Chụp món filled][Chat AI tonal][+ outlined] theo spec S10 mục 4; khi hàng nút cuộn khuất, FAB mở rộng "Chụp món" hiện góc phải dưới trên navigation bar 4 tab. Danh sách bữa Sáng/Trưa/Tối/Phụ, món xếp theo giờ ăn.
FORM: canon (chuẩn của thể loại), được chọn thay cho hướng bốc thăm; signature move: vòng calo chia đoạn theo bữa theo thứ tự giờ ăn (các món liên tiếp cùng bữa gộp một đoạn), mỗi bữa một tông chàm khớp ô màu ở đầu thẻ bữa, đầu đoạn vuông, cung tối thiểu 4dp; vượt mục tiêu thì phần dư chạy vòng trong màu hổ phách (≤10%) hoặc error. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
