---
version: 1
slug: "docs-design-s15-water-html"
primary_target: "docs/design/s15-water.html"
related_targets: []
---

# S15 Chi tiết nước trong ngày

Scope: màn con Nước trong ngày (Operate, ghi và sửa nhanh), mockup HTML tại docs/design/s15-water.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 (S10 thẻ nước, S15, S43, §3 thông báo nhắc), 01 F10 (mục tiêu tự tính / chỉnh tay, nhắc trong khung giờ, dừng khi đủ, nút +250 ml trên thông báo, xoá / sửa lần uống), 02 §8 + §10 (75 kg × 35 = 2.625 → 2.600 ml), 04 `water_entry` (amount_ml, source APP / NOTIFICATION / WIDGET, created_at), 06 §4 (nhắc kế tiếp = max(lần uống gần nhất, lần nhắc trước) + 90 phút, kẹp 07:00–22:00; đủ mục tiêu → đầu khung ngày mai), 08 §2 / §3.3 (ngày đạt nước = đã uống ≥ mục tiêu).
Người dùng: một người, chạm ô Nước của S10 (hoặc "Sửa" trên ô Nước của S33); muốn thấy đã uống bao nhiêu, còn bao nhiêu, bao giờ nhắc tiếp, và sửa / xoá một lần ghi nhầm, bằng một tay.

## Direction contract

THESIS: một ngày nước là thanh làn của S12 duỗi theo mục tiêu nước: mỗi lần uống là một đoạn primary đầu vuông cách nhau 3dp, toàn chiều dài làn = mục tiêu; phần trên mục tiêu chạy làn riêng 6dp bên dưới nhưng vẫn primary, vì uống quá mục tiêu không phải trạng thái xấu. Từng lần uống đọc nhỏ và nhạt như từng lần cân của S31; số lớn là tổng của ngày.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, nước = primary, không màu dữ liệu, Goal-Line Split, vuốt xoá + snackbar Hoàn tác, sheet phụ + OutlinedTextField, ô vuông chú giải, biểu đồ cột nước của S35).
STORY: chạm ô Nước trên S10 → thấy 1.250 / 2.600 ml (48%), còn 1.350 ml, nhắc tiếp 14:05 → bấm +250 ở chân ghim trong vùng ngón cái → hoặc chạm một lần uống để sửa giờ / lượng, vuốt trái để xoá (Hoàn tác) → liếc 7 ngày của tuần, chạm cột để xem ngày khác.
FIRST VIEWPORT: thanh trên (quay lại; "Nước · Hôm nay" + "Thứ Năm, 13/11 ▾"; ‹ ›, › tắt ở hôm nay); thẻ tổng ngày (tổng ml Display Large là số lớn nhất màn, phần trăm mục tiêu, làn từng lần uống, còn lại, dòng mục tiêu tự tính, dòng nhắc tiếp); đầu danh sách các lần uống; chân ghim +250 ml (filled) / +500 ml (tonal) / lượng khác (outlined 48dp). Trạng thái: hôm nay đang uống, tối, đạt và trên mục tiêu, sheet thêm lượng khác, sheet sửa lần uống, vừa xoá + snackbar, ngày đã qua, nhắc tắt vì thiếu quyền thông báo.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: làn nước chia đoạn theo từng lần uống, tách tại vạch mục tiêu bằng làn trên-mục-tiêu riêng 6dp cùng màu primary, chữ trung tính "Đạt mục tiêu · +250 ml". seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
