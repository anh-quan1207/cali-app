---
version: 1
slug: "docs-design-s30-history-html"
primary_target: "docs/design/s30-history.html"
related_targets: []
---

# S30 Lịch sử

Scope: tab Lịch sử (Operate), mockup HTML tại docs/design/s30-history.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 S30 (lịch tháng có trạng thái từng ngày, tóm tắt tuần hiện tại, lối vào Tổng kết tuần / Cân nặng / Thống kê / Món của tôi, chạm nhãn tuần → S35, chạm ngày → S33), 01 F11, 08 §2 (định nghĩa Đạt / Vượt / Thiếu / chưa ghi / nghi ghi thiếu).
Người dùng: một người, lướt lại tháng để thấy nhịp ăn, mở lại một ngày cũ để sửa, hoặc mở tổng kết của một tuần.

## Direction contract

THESIS: lịch tháng là năm hàng S35 thu nhỏ: mỗi tuần là một thẻ có nhãn tuần + điểm (chạm → S35) và bảy ô ngày, mỗi ô là một cột calo mini trên vạch mục tiêu chung của hàng, cùng ngữ pháp Goal-Line Split. Màu ngày lấy từ 08 §2, không phải chấm trang trí.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, Three States, Amber Check, Goal-Line Split, --under cho Thiếu).
STORY: mở tab → thấy tháng này ngày nào đạt / vượt / thiếu / chưa ghi trong một cái liếc → chạm một ngày để xem và sửa (S33), hoặc chạm nhãn tuần để đọc tổng kết (S35) → cuộn xuống có tóm tắt tuần này và bốn lối vào.
FIRST VIEWPORT: thanh trên (‹ "Lịch sử" + "Tháng 11, 2026 ▾" ›); hàng thứ T2…CN; năm thẻ tuần (nhãn "dd/mm – dd/mm" + điểm, hoặc "Tuần này · tạm tính"), mỗi thẻ bảy ô 48dp có số ngày và cột mini; hôm nay viền tròn primary, cột kiểu đoạn chờ; ngày tương lai chỉ có số mờ; navigation bar với Lịch sử được chọn. Trạng thái: tháng đang diễn ra, tháng đã qua (có ngày chưa ghi), người mới bắt đầu (ngày trước khi dùng app không phải "chưa ghi"), giao diện tối.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: ô lịch là cột calo mini dùng chung vạch mục tiêu của cả hàng tuần, nên một tuần đọc như biểu đồ 7 cột của S35. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
