---
version: 1
slug: "docs-design-s11-camera-html"
primary_target: "docs/design/s11-camera.html"
related_targets: []
---

# S11 Camera

Scope: màn Camera (Capture, chụp một ảnh bữa ăn), mockup HTML tại docs/design/s11-camera.html, sẽ chuyển sang Compose (CameraX + Photo Picker). Mở rộng trong thế giới đã có (DESIGN.md), không đổi token. Đặc tả: 05 S11 và §4 (eaten_at: lúc bấm chụp / EXIF DateTimeOriginal, tự gán bữa theo giờ), 03 §3.1 và §7 (nén 1024 px JPEG 80, xóa EXIF sau khi đọc giờ) và §8 (lỗi, retry, hủy), 01 F04. Luồng S10 📷 → S11 → S12.
Người dùng: một người, đang cầm đĩa cơm bằng một tay, muốn một cú chạm là có ảnh rõ cả đĩa; nếu thiếu sáng, thiếu quyền hoặc mất mạng thì biết ngay làm gì tiếp mà không mất ảnh.

## Direction contract

THESIS: camera là nơi duy nhất ứng dụng tối theo bản chất, nên nó dùng đúng scheme tối của DESIGN.md (viewfinder đen, nút là tầng tonal tối, chữ on-surface-dark) và mọi thứ trên màn đều là một việc: căn đĩa, biết giờ ăn sẽ ghi, bấm chụp; sau khi chụp thì ảnh, ghi chú và giờ ăn nằm cùng một chỗ trước khi gửi.
OWN-WORLD: kế thừa DESIGN.md (Material 3 seed chàm, Roboto Flex tabular, scheme tối đầy đủ, chip giờ ăn · bữa của S12, chat-field và attach-option của S20, nút bước 56dp, hộp lỗi, Three States, Amber Check).
STORY: bấm 📷 ở S10 → thấy khung đĩa và chip "12:35 · Trưa" là giờ sẽ ghi → bấm chụp → xem lại ảnh, gõ "không đường, ăn một nửa" nếu muốn → Phân tích → chờ có chữ nói thật (đã thu nhỏ, đang gửi, đã chờ 0:07, có Hủy) → sang S12; nếu hỏng thì ảnh và ghi chú còn nguyên, có Thử lại và Nhập tay.
FIRST VIEWPORT: thanh trên (đóng, đèn flash kèm chữ trạng thái, đổi camera); khung xem trước đen có vòng căn đĩa và một dòng gợi ý chụp từ trên xuống; cạnh dưới trong vùng ngón cái: chip giờ ăn · bữa (chạm để sửa) ngay trên nút chụp, hàng Thư viện 48dp / nút chụp 72dp / Ghi chú 48dp. Trạng thái: sẵn sàng, thiếu sáng + đèn bật, thiếu sáng + đèn tắt (nút "Bật đèn"), chưa có quyền camera, đã chọn "Không hỏi lại" (Mở Cài đặt), xem lại sau khi chụp, đang phân tích, lỗi mất kết nối, AI không trả lời sau 60 giây, ảnh từ thư viện (giờ lấy từ EXIF, có thể sang ngày khác).
FORM: mở rộng trong thế giới đã có (kế thừa hướng canon của S10 và S12); signature move: một vòng căn đĩa mảnh trên nền đen thay cho khung ngắm trang trí, và chip giờ ăn · bữa của S12 đặt ngay trên nút chụp để thấy giờ sẽ ghi trước khi bấm. seed:0f1a18bbc2ca593f
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
