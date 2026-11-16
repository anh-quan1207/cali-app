---
version: 1
slug: "docs-design-s33-day-html"
primary_target: "docs/design/s33-day.html"
related_targets: []
---

# S33 Chi tiết ngày

Scope: màn Chi tiết ngày (Operate, xem và sửa một ngày đã qua), mockup HTML tại docs/design/s33-day.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 S30 ("Chạm ngày → S33, giống S10 cho ngày đó"), S10, S14 Sửa món, S15 Nước, 05 §4.1 (ghi cho ngày khác hôm nay: giờ giữa khung của bữa), 01 F11, 08 §2 (Đạt / Vượt / Thiếu / chưa ghi / nghi ghi thiếu dưới 50%) và §5.1 (sửa món cũ sau khi có nhận xét → "Dữ liệu đã thay đổi · Tạo lại nhận xét").
Người dùng: một người, mở từ ô ngày trên Lịch sử (S30) hoặc cột ngày trên Tổng kết tuần (S35); muốn biết ngày đó kết thúc thế nào, đã ăn gì lúc mấy giờ, và sửa hoặc thêm món còn sót.

## Direction contract

THESIS: S33 là S10 của một ngày đã chốt; cùng vòng calo, thẻ bữa, macro và ô dữ liệu, nhưng tâm vòng nói kết quả của ngày (chênh lệch so với mục tiêu theo 08 §2) thay vì số còn lại, vì ngày đã qua không còn gì để "còn".
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, ba màu dữ liệu, Three States, Amber Check, Goal-Line Split, Remaining-First, Ledger Rule, Chàm Thiếu).
STORY: chạm một ngày trên lịch → thấy ngay ngày đó Thiếu / Đạt / Vượt bao nhiêu → đọc món theo giờ → thêm món quên ghi (giờ giữa khung bữa của chính ngày đó) hoặc vuốt xoá món sai → biết tuần chứa ngày này và nhận xét AI của tuần có cần tạo lại không.
FIRST VIEWPORT: thanh trên con (quay lại, "Thứ Tư, 12/11" Title Large + dòng tương đối "Hôm qua ▾" mở lịch, ‹ ›); vòng calo 220dp, tâm là chênh lệch có dấu Display Large với nhãn loại ngày; hàng Đã ăn / Mục tiêu; chip loại ngày (ô chú giải 10dp + chữ, như chú giải S35/S30) hoặc chip trạng thái khi vượt; ba macro; hàng nút [Thêm món filled][Chat AI tonal][ảnh thư viện outlined]; thẻ tuần chứa ngày. Trạng thái: Thiếu, Vượt nhiều (vòng trong, macro tách tại vạch), nghi ghi thiếu (hộp hổ phách, tâm là số đã ghi), chưa ghi (vòng rỗng, mời thêm), vừa xoá trong tuần đã có nhận xét (snackbar Hoàn tác + dải "Tạo lại"), thêm món cho ngày đã qua (sheet xác nhận, chip "07:30 · Sáng · 12/11", làn "12/11 sau khi lưu").
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: tâm vòng calo của ngày đã qua đổi từ "Còn lại" sang kết quả có dấu theo 08 §2 (−320 Thiếu, +620 Vượt), và chip loại ngày dùng đúng ô chú giải của biểu đồ tuần, để vòng, cột S35 và ô lịch S30 nói cùng một ngôn ngữ. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
