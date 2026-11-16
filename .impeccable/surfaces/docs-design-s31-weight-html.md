---
version: 1
slug: "docs-design-s31-weight-html"
primary_target: "docs/design/s31-weight.html"
related_targets: []
---

# S31 Cân nặng

Scope: màn Cân nặng (Operate, đọc xu hướng và ghi số), mockup HTML tại docs/design/s31-weight.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 §S31 (biểu đồ đường 7 ngày / 30 ngày / 90 ngày / Tất cả + đường mục tiêu, danh sách bản ghi, nút Ghi cân chọn ngày + nhập kg, sau khi ghi là hộp tính lại mục tiêu F09), 01 F09 (1 bản ghi/ngày, ghi lại thì thay thế; hỏi "Cập nhật mục tiêu calo theo cân nặng mới?" có tuỳ chọn "Luôn tự động"), 02 §2–§7, §9–§11 (Mifflin-St Jeor, 7700 kcal/kg, giới hạn an toàn, BMI ngưỡng châu Á, ETA, GoalSnapshot), 08 §3.4 (cân TB tuần để giảm nhiễu). Số liệu khớp S10 / S30 / S35: TB tuần 03/11 74,1 kg (3 lần cân), −0,5 kg so với tuần trước, mục tiêu 68 kg ~02/02/2027, 1.740 kcal/ngày tính theo 75,0 kg (02 §10).
Người dùng: một người, nam 30 tuổi, 170 cm; cân buổi sáng 2–3 lần/tuần, mở màn từ ô Cân nặng của S10 hoặc hàng "Cân nặng" của S30; muốn biết mình có đang đi đúng tốc độ không, và ghi số mới trong vài giây.

## Direction contract

THESIS: cân nặng là chuỗi trung bình tuần, không phải từng con số buổi sáng; số lớn, đường đậm và danh sách đều xếp theo tuần (cùng đơn vị với S35 và S30), còn từng lần cân chỉ là ô nhỏ nhạt dưới đường, để dao động nước muối không bao giờ thành tin chính.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, ba màu dữ liệu không dùng cho cân, Three States, Amber Check / Amber Adjust, No-Judgement Scale, Remaining-First).
STORY: sáng Thứ Năm 13/11 mở từ S10 → thấy 74,1 kg TB tuần trước, −0,5 kg, đã giảm 1,9 / còn 6,1 kg tới 68, dự kiến ~07/02/2027 (02 §9, tính từ hôm nay) → đọc đường TB tuần 30 ngày và tốc độ −0,43 kg/tuần so với kế hoạch −0,5 → bấm Ghi cân, nhập 73,8 → sheet tính lại cho thấy 1.740 → 1.730 kcal và vì sao an toàn → chọn Cập nhật hoặc Giữ nguyên.
FIRST VIEWPORT: thanh trên (quay lại, "Cân nặng"); thẻ cân (nhãn tuần + số lần cân, cân TB tuần Display Large là số lớn nhất màn, thay đổi so với tuần trước, làn tiến độ từ tuần đầu tới cân mục tiêu, dòng còn X kg · dự kiến); thẻ Xu hướng (chọn khoảng 7 ngày / 30 ngày / 90 ngày / Tất cả, biểu đồ đường TB tuần trên các ô lần cân, vạch mục tiêu); FAB "Ghi cân". Trạng thái: có số (sáng / tối), Tất cả (có vạch mục tiêu thật và đường dự kiến), sheet Ghi cân (đang nhập, BMI xem trước), lỗi khoảng sau khi rời ô + ngày đã có bản ghi (sẽ thay), sheet tính lại mục tiêu, người mới một lần cân, vừa xoá + Hoàn tác.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10 và S35); signature move: đường TB tuần 2dp chàm với ô vuông 7dp (ngữ pháp sparkline S35) nằm trên các ô lần cân 5dp màu Mực Phụ; vạch mục tiêu chỉ vẽ ở đúng giá trị của nó, khi nằm ngoài khung thì chú giải nói "dưới khung" thay vì kéo vạch về mép sai. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
