---
version: 1
slug: "docs-design-s40-settings-html"
primary_target: "docs/design/s40-settings.html"
related_targets: []
---

# S40 Cài đặt và các màn con S41–S45

Scope: tab Cài đặt (S40, cấp cao, có navigation bar) và năm màn con: S41 Hồ sơ & mục tiêu, S42 AI, S43 Nước & nhắc nhở, S44 Bữa ăn, S45 Dữ liệu (Operate, chỉnh cấu hình), mockup HTML tại docs/design/s40-settings.html, sẽ chuyển sang Compose. Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 §2 (bảng S40), §4.2 (khung giờ bữa); 01 F09, F12, F13; 02 §5 (giới hạn an toàn), §6 (macro), §8 (nước), §11 (khi nào tính lại, ghi đè); 03 §2, §6, §8 (model, system prompt, lỗi); 04 §3 (khóa DataStore và mặc định), §5 (sao lưu JSON); 06 §4–§5 (nhắc nước, thông báo tuần, Keystore); 08 §1, §5.1 (thông báo Thứ Hai 08:00, "Tự tạo nhận xét khi mở"). Số liệu: 02 §10 và S31 (mục tiêu đang dùng 1.740 kcal tính theo 75,0 kg ngày 21/10; lần cân mới nhất 74,1 kg ngày 08/11).
Người dùng: một người, vào Cài đặt hiếm khi; muốn thấy ngay giá trị đang dùng ở mỗi mục, sửa một thứ, và biết chắc thay đổi đó làm gì với mục tiêu calo trước khi áp dụng.

## Direction contract

THESIS: cài đặt là bảng thông số của chính đồng hồ đo: mỗi hàng nói giá trị đang dùng bằng số tabular canh phải, và mọi thay đổi chạm tới mục tiêu calo đi qua cùng một sheet Đang dùng / Mới của S31, nên sửa hồ sơ, đổi tốc độ hay ghi đè calo đều đọc như một phép đo được tính lại, không như một form.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, tonal surface, ngữ pháp chọn duy nhất, OutlinedTextField M3 và biến thể API key, Switch M3 52×32, sheet phụ S12 trên màn che, TimeInput, Amber Adjust, Three States, The Tabular Rule).
STORY: mở tab Cài đặt → mỗi mục đã nói giá trị hiện tại (1.740 kcal/ngày, key …3f9A, 2,6 L, giờ các bữa, sao lưu 09/11) → vào Hồ sơ & mục tiêu, đổi tốc độ sang 0,75 kg/tuần → sheet nói mới là 1.710 kcal vì app đã kẹp về mức an toàn (~0,52 kg/tuần) → áp dụng từ hôm nay; ở AI kiểm tra kết nối; ở Bữa ăn thấy cả ngày 24 giờ như một làn; ở Dữ liệu, xoá toàn bộ phải qua hai bước.
FIRST VIEWPORT: S40: thanh trên "Cài đặt", năm hàng 64dp có ô biểu tượng 40dp (như "Xem thêm" của S30) với dòng phụ là giá trị đang dùng, mục Giới thiệu, navigation bar với Cài đặt được chọn. Trạng thái: đủ cấu hình (sáng), "Để sau" ở S03 (tối: chưa có key, nhắc tắt, chưa sao lưu), thiếu quyền thông báo, kiểm tra kết nối thành công / thất bại 401, ghi đè calo dưới sàn an toàn, khung giờ bữa trùng nhau, xoá toàn bộ bước 1 / bước 2.
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10); signature move: hàng cài đặt là "nhãn ← giá trị tabular canh phải", và khung giờ bốn bữa vẽ thành một làn 24 giờ cùng ngữ pháp đoạn của vòng calo (tông meal-*, đầu vuông, khe 3dp), Phụ hiện hai lần như vòng calo. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
