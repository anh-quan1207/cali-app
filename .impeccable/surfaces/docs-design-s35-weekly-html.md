---
version: 1
slug: "docs-design-s35-weekly-html"
primary_target: "docs/design/s35-weekly.html"
related_targets: []
---

# S35 Tổng kết tuần

Scope: màn Tổng kết tuần (Operate, đọc báo cáo), mockup HTML tại docs/design/s35-weekly.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 08 (định nghĩa §2, chỉ số §3, điểm §4, AI §5, màn §6), 02 §10 (mục tiêu 1.740 kcal, TDEE 2.293, đạm 122 / tinh bột 196 / béo 52 g, nước 2,6 L).
Người dùng: một người, mở sáng Thứ Hai từ thông báo hoặc từ Lịch sử; muốn biết trong một cái liếc tuần qua thế nào, rồi đọc kỹ nếu muốn.

## Direction contract

THESIS: tuần là bảy ngày của vòng calo xếp cạnh nhau; điểm, cột ngày và các thanh đều dùng lại ngữ pháp làn / đoạn của vòng (đầu vuông, khe 3dp, phần vượt tách khỏi phần trong mục tiêu), để báo cáo đọc như một bảng đo chính xác, không như infographic.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, ba màu dữ liệu, Three States, Amber Check, No-Judgement).
STORY: mở từ thông báo → thấy điểm 71 "Tốt" và bốn phần của điểm → biết tuần vượt 420 kcal, vì sao (T7, CN) → đọc nước, macro, cân → đọc nhận xét AI có ba gợi ý cho tuần sau.
FIRST VIEWPORT: thanh trên (quay lại, "Tổng kết tuần"); hàng chọn tuần ‹ 03/11 – 09/11 › với trạng thái "Đã kết thúc" hoặc "Tạm tính · còn N ngày"; thẻ điểm (số điểm Display Large là số lớn nhất màn, /100, xếp loại, so với tuần trước, làn điểm 4 đoạn 40/20/20/20); đầu thẻ calo (câu chính theo trạng thái + biểu đồ 7 cột có vạch mục tiêu). Trạng thái: tuần đã kết thúc, tuần đang diễn ra (cột hôm nay chưa xong, ngày tới trống), không có bản ghi cân, AI đang tạo / lỗi / dữ liệu đã đổi / chưa tạo, không có API key (ẩn thẻ AI).
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: làn điểm 0–100 chia bốn đoạn theo trọng số và cột ngày tách phần vượt lên trên vạch mục tiêu bằng khe 3dp, cùng cách vòng calo tách vòng vượt. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
