---
version: 1
slug: "docs-design-s01-s02-onboarding-html"
primary_target: "docs/design/s01-s02-onboarding.html"
related_targets: []
---

# S01–S03 Onboarding & Kết quả

Scope: luồng lần đầu mở app (Operate), mockup HTML tại docs/design/s01-s02-onboarding.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md).
Người dùng muốn onboarding nhanh, gọn (không minh hoạ, không giải thích dài); S02 lấy số kcal/ngày làm trọng tâm. Đặc tả: 01 F01/F02, 02 (công thức, giới hạn an toàn, BMI châu Á), 05 S01–S03.

## Direction contract

THESIS: onboarding là năm câu hỏi một chạm, kết thúc bằng đúng một con số; không màn chào, không hình minh hoạ, không trang trí.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, màu dữ liệu đạm/tinh bột/béo, hổ phách cho cảnh báo điều chỉnh an toàn).
STORY: trả lời 5 bước (giới tính+năm sinh, chiều cao+cân nặng, vận động, mục tiêu, cân mong muốn+tốc độ), thấy ngay kcal/ngày, bấm Bắt đầu; tuỳ chọn dán API key và bật nhắc nước.
FIRST VIEWPORT: mỗi bước: thanh tiến độ "Bước n/5" + nút quay lại; tiêu đề headline; lựa chọn dạng thẻ chọn (outline → secondary-container + viền primary khi chọn); nút Tiếp 56dp ghim đáy. S02: số kcal/ngày displayLarge trong vòng calo còn trống (trạng thái đầu ngày của S10, có vạch chia mảnh), dòng dự kiến đạt cân, thanh chồng tỉ lệ năng lượng đạm/tinh bột/béo, lưới BMI/BMR/TDEE/Nước, "Cách tính" mở rộng, miễn trừ y tế, nút Bắt đầu.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: S02 đặt mục tiêu vào vòng calo trống của S10, để con số đầu tiên người dùng thấy chính là con số họ sẽ thấy mỗi sáng; BMI hiện trên thang ngưỡng châu Á có vạch số. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
