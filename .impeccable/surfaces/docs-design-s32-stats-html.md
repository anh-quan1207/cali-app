---
version: 1
slug: "docs-design-s32-stats-html"
primary_target: "docs/design/s32-stats.html"
related_targets: []
---

# S32 Thống kê

Scope: màn Thống kê (Operate, đọc số liệu theo kỳ), mockup HTML tại docs/design/s32-stats.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 §S32, 01 F11, 08 §2 (loại ngày), §3 (chỉ số), §7 (tháng của tổng kết là giai đoạn 2, còn số liệu tháng của S32 là MVP); 02 §10 (mục tiêu 1.740 kcal, đạm 122 / tinh bột 196 / béo 52 g, nước 2,6 L). Dữ liệu ngày lấy nguyên bảng K của S30.
Người dùng: một người, mở từ Lịch sử › Xem thêm › Thống kê khi muốn biết một tuần hoặc một tháng của mình trung bình ra sao, không cần điểm hay lời nhận xét; hay xem tháng và so kỳ này với kỳ trước.

Khác S35: S35 là báo cáo có điểm và AI cho một tuần; S32 là số trung tính cho một kỳ (tuần hoặc tháng). S32 không có điểm, không có xếp loại, không có chữ AI, không có top món; ở chế độ Tuần nó chỉ đưa một hàng "Xem tổng kết tuần" (điểm nằm ở dòng phụ, không lớn) mở S35. Giá trị riêng của S32 là chế độ Tháng (biểu đồ 28–31 cột, các tuần trong tháng, cân theo tuần của tháng) và thẻ so sánh với kỳ trước.

## Direction contract

THESIS: một kỳ là các ngày của vòng calo xếp cạnh nhau, đọc bằng trung bình: số lớn duy nhất là kcal TB mỗi ngày, mọi cột, thanh macro và làn nước dùng lại ngữ pháp đoạn của S35 / S15 (đầu vuông, khe 3dp, tách tại vạch), và mọi so sánh với kỳ trước là chữ trung tính, không màu đánh giá.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, ba màu dữ liệu, Three States, Amber Check, No-Judgement, Goal-Line Split, Remaining-First).
STORY: mở từ Lịch sử → thấy TB tuần này 1.533 kcal so với 1.740 và bảy cột → đổi sang Tháng, thấy tháng 11 đến nay 1.698 kcal, đạt 7 ngày → chạm/kéo trên biểu đồ tháng để xem một ngày → đọc macro, nước, cân trong kỳ → so với cùng kỳ tháng trước; muốn điểm và lời khuyên thì chạm "Xem tổng kết tuần".
FIRST VIEWPORT: thanh trên (quay lại, "Thống kê"); bộ chọn Tuần / Tháng 48dp; hàng chọn kỳ ‹ Tuần này · 10/11 – 16/11 › với "Tạm tính · còn N ngày" hoặc "Đã kết thúc · ghi n/N ngày"; thẻ Calo (kcal TB mỗi ngày là Display Large duy nhất của màn, chênh so với mục tiêu, biểu đồ cột có vạch mục tiêu và dải ±10%, chú giải kiêm bảng đếm). Trạng thái: tuần đang diễn ra, tuần đã kết thúc, tháng đang diễn ra (ngày tới để trống), tháng đã kết thúc có ngày chưa ghi, đã chọn một ngày trên biểu đồ tháng, kỳ trước không đủ dữ liệu, người mới chưa đủ 3 ngày ghi.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: biểu đồ tháng 28–31 cột hẹp cùng ngữ pháp Đạt / Thiếu / Vượt tách tại vạch của S35, là một vùng chạm liền: chạm hoặc kéo để bắt dính vào một ngày, rồi hàng đọc số 48dp dưới biểu đồ mở ngày đó. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
