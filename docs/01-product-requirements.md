# 01 — Yêu cầu sản phẩm

## 1. Bối cảnh & mục tiêu

Các app như Caloer, Nutree có nhiều tính năng (tập luyện, cộng đồng, gói trả phí) mà chủ app không cần, và nhập món ăn thủ công khá mất thời gian. Cali tập trung vào một vòng lặp duy nhất:

> **Mục tiêu calo/ngày → ghi món ăn (nhanh, bằng AI) → biết còn lại bao nhiêu → theo dõi cân nặng theo thời gian**

**Mục tiêu:**
- G1. Ghi một bữa ăn trong **dưới 30 giây** (chụp ảnh → xác nhận → lưu).
- G2. Luôn biết **còn bao nhiêu calo** trong ngày chỉ bằng một cái nhìn.
- G3. Calo mục tiêu được **tính tự động** từ thông tin cá nhân và mục tiêu cân nặng.
- G4. Uống đủ nước nhờ **nhắc nhở thông minh**.

**Phi mục tiêu (không làm):**
- Tập luyện, đếm bước, calo tiêu hao do vận động.
- Nhiều người dùng, tài khoản, đăng nhập, đồng bộ đám mây.
- Backend riêng, phát hành Play Store, thu phí.
- Cơ sở dữ liệu món ăn đóng gói sẵn (AI thay thế vai trò này; "Món của tôi" là kho món tự tích lũy).

## 2. Danh sách tính năng

| ID | Tính năng | Ưu tiên |
|---|---|---|
| F01 | Onboarding: nhập thông tin cá nhân & mục tiêu | MVP |
| F02 | Tính toán chỉ số tự động (BMR, TDEE, calo, macro, BMI, nước) | MVP |
| F03 | Màn Hôm nay: calo còn lại, macro, bữa ăn, nước | MVP |
| F04 | Ghi món bằng ảnh (AI) | MVP |
| F05 | Ghi món / hỏi đáp bằng chat (AI) | MVP |
| F06 | Thẻ xác nhận & chỉnh sửa kết quả AI | MVP |
| F07 | Món của tôi (lưu & chọn lại nhanh, không tốn lượt AI) | MVP |
| F08 | Ghi món thủ công | MVP |
| F09 | Theo dõi cân nặng + tính lại mục tiêu | MVP |
| F10 | Theo dõi & nhắc uống nước | MVP |
| F11 | Lịch sử & thống kê | MVP |
| F12 | Cài đặt (hồ sơ, AI, system prompt, nhắc nhở) | MVP |
| F13 | Sao lưu: xuất / nhập JSON | MVP |
| F14 | Widget màn hình chính (calo còn lại + nút uống nước) | Giai đoạn 2 |
| F15 | Copy bữa / copy ngày hôm qua | Giai đoạn 2 |
| F16 | Theo dõi chi phí AI (token / ước tính tiền) | Giai đoạn 2 |
| F17 | Tổng kết & đánh giá tuần (vượt/thiếu calo, nước, macro, cân nặng, điểm tuần, nhận xét AI) | MVP |

## 3. User story & tiêu chí chấp nhận

### F01 — Onboarding
**Story:** Lần đầu mở app, tôi nhập thông tin của mình để app biết tôi cần ăn bao nhiêu.

Tiêu chí:
- Hiện khi chưa có hồ sơ; không bỏ qua được (trừ khi nhập dữ liệu từ file sao lưu).
- Các bước: (1) giới tính + năm sinh → (2) chiều cao + cân nặng → (3) mức vận động → (4) mục tiêu giảm / giữ / tăng → (5) cân nặng mong muốn + tốc độ (bỏ qua nếu "Giữ") → (6) kết quả → (7) cài đặt AI & nhắc nước (có thể "Để sau").
- Có nút Quay lại ở mọi bước; dữ liệu đã nhập được giữ lại.
- Kiểm tra hợp lệ: tuổi 18–80, chiều cao 120–220 cm, cân nặng 30–250 kg. Cân mong muốn phải thấp hơn hiện tại nếu "Giảm", cao hơn nếu "Tăng".
- Dưới 18 tuổi: cảnh báo app không phù hợp, vẫn cho tiếp tục.

### F02 — Tính toán chỉ số
- Tính theo [02-calculations.md](02-calculations.md).
- Màn kết quả hiển thị: BMI + phân loại, BMR, TDEE, **calo mục tiêu/ngày**, macro (g), nước (ml), ngày dự kiến đạt mục tiêu.
- Nếu calo bị kẹp bởi giới hạn an toàn: hiện cảnh báo rõ ràng và ngày dự kiến được tính lại theo tốc độ thực tế.
- Người dùng có thể **ghi đè thủ công** calo mục tiêu và tỉ lệ macro (Cài đặt). Khi ghi đè, hiện nhãn "Tùy chỉnh".

### F03 — Màn Hôm nay
- Vòng tròn: đã ăn / mục tiêu, số calo còn lại (âm thì hiện "Vượt X kcal" màu cảnh báo).
- 3 thanh macro: đạm / tinh bột / béo (đã ăn / mục tiêu).
- Danh sách 4 bữa (Sáng, Trưa, Tối, Phụ) với tổng calo mỗi bữa và các món.
- Thẻ nước: đã uống / mục tiêu, nút +250 ml, +500 ml (dung tích chỉnh được).
- Nút lớn: 📷 Chụp, 💬 Chat; nút phụ: + Thêm nhanh (Món của tôi / thủ công).
- Vuốt trái/phải hoặc chọn ngày để xem / ghi cho ngày khác.

### F04 — Ghi món bằng ảnh
- Mở camera (hoặc chọn từ thư viện). Có ô ghi chú tùy chọn ("ăn một nửa", "ít cơm").
- Ảnh được nén trước khi gửi (xem 03).
- Hiện trạng thái đang phân tích; có thể hủy.
- Kết quả mở **Thẻ xác nhận (F06)**.
- Thời gian phản hồi mục tiêu: < 15 giây trên 4G.

### F05 — Chat AI
- Một luồng chat theo ngày. Gửi chữ, ảnh, hoặc cả hai.
- AI tự phân biệt: **ghi món** → trả về thẻ món ăn (F06) ngay trong chat; **tư vấn** → trả lời bằng chữ.
- Mỗi lần gọi đều kèm ngữ cảnh: hồ sơ, mục tiêu, đã ăn hôm nay, còn lại, ghi chú cá nhân.
- Ví dụ phải xử lý được: "trưa ăn 1 tô bún bò với 1 ly trà đá", "tối nay còn 500 kcal ăn gì?", "hôm nay ăn đủ đạm chưa?".

### F06 — Thẻ xác nhận
- Liệt kê từng món: tên, mô tả khẩu phần, gram, kcal, macro, độ tin cậy (ẩn/hiện).
- Mỗi món: đổi hệ số khẩu phần (×0.5, ×1, ×1.5, ×2 hoặc nhập gram) → calo & macro **tính lại trên máy theo tỉ lệ**, không gọi AI.
- Sửa tên, xóa món, thêm món thủ công.
- **Ghi lại thời điểm ăn** (lúc chụp / lúc gửi chat / giờ trong EXIF của ảnh thư viện / giờ người dùng nói) và **tự gán bữa Sáng / Trưa / Tối / Phụ theo giờ đó**. Người dùng sửa được giờ hoặc bữa (chi tiết 05 §4).
- Nếu AI có câu hỏi làm rõ: hiện câu hỏi + ô trả lời → gửi lại để AI cập nhật.
- Tùy chọn "Lưu vào Món của tôi" cho từng món.
- Nút **Lưu** ghi vào nhật ký; **Hủy** bỏ toàn bộ.

### F07 — Món của tôi
- Danh sách món đã lưu, sắp xếp theo: dùng gần đây / dùng nhiều / yêu thích. Có tìm kiếm.
- Chạm 1 lần → chọn khẩu phần → thêm vào bữa.
- Sửa, xóa, đánh dấu yêu thích.

### F08 — Ghi thủ công
- Nhập tên, kcal (bắt buộc), gram và macro (tùy chọn).

### F09 — Cân nặng
- Ghi cân nặng theo ngày (1 bản ghi/ngày, ghi lại thì thay thế).
- Biểu đồ đường 7 ngày / 30 ngày / 90 ngày / tất cả, kèm đường cân mục tiêu.
- Sau khi ghi cân mới: hỏi "Cập nhật mục tiêu calo theo cân nặng mới?" (có tùy chọn "Luôn tự động").
- Khi đạt cân mục tiêu: thông báo chúc mừng + gợi ý chuyển sang "Giữ cân".

### F10 — Nước
- Mục tiêu tự tính (xem 02), chỉnh tay được.
- Nhắc trong khung giờ, theo chu kỳ; dừng khi đủ; lùi lịch khi vừa uống.
- Thông báo có nút **+250 ml** ghi trực tiếp không cần mở app.
- Xóa / sửa lần uống trong ngày.

### F11 — Lịch sử & thống kê
- Lịch tháng: mỗi ngày tô màu theo trạng thái (trong mục tiêu / vượt / chưa ghi).
- Chi tiết ngày: giống màn Hôm nay nhưng cho ngày đã qua (có thể sửa).
- Thống kê tuần / tháng: calo trung bình, ngày đạt mục tiêu, macro trung bình, nước trung bình, xu hướng cân nặng.

### F12 — Cài đặt
- Hồ sơ & mục tiêu (sửa → tính lại).
- Ghi đè calo / macro.
- AI: hãng, API key, model ảnh, model chat, **system prompt** (sửa / khôi phục mặc định), **ghi chú cá nhân** cho AI, nút "Kiểm tra kết nối".
- Nước: mục tiêu, dung tích cốc, khung giờ, chu kỳ, bật/tắt.
- Giờ các bữa (dùng để gợi ý bữa).
- Sao lưu / khôi phục.
- Xóa toàn bộ dữ liệu (xác nhận 2 lần).

### F13 — Sao lưu
- Xuất toàn bộ dữ liệu (trừ API key) ra 1 file JSON qua Storage Access Framework.
- Nhập: kiểm tra phiên bản schema; cảnh báo sẽ **ghi đè** dữ liệu hiện tại.
- Bật Android Auto Backup cho DB (loại trừ API key).

### F17 — Tổng kết & đánh giá tuần
**Story:** Cuối mỗi tuần tôi muốn biết mình đã ăn vượt hay thiếu bao nhiêu calo, uống đủ nước chưa, và nhận lời đánh giá để tuần sau làm tốt hơn.

Tiêu chí (chi tiết ở [08-weekly-review.md](08-weekly-review.md)):
- Tuần tính từ Thứ Hai đến Chủ Nhật; xem được tuần hiện tại (tạm tính) và mọi tuần cũ.
- Hiện tổng chênh lệch calo so với mục tiêu (vượt / thiếu X kcal), số ngày Đạt / Vượt / Thiếu / Chưa ghi, biểu đồ 7 ngày.
- Hiện nước: tổng đã uống / mục tiêu, số ngày đạt, thiếu bao nhiêu lít.
- Hiện macro trung bình, cân nặng trung bình tuần, thay đổi cân dự kiến vs thực tế, so sánh với tuần trước.
- Điểm tuần 0–100 tính trên máy theo luật cố định, kèm xếp loại.
- AI viết nhận xét có cấu trúc: điểm tốt, vấn đề, quy luật, 3 gợi ý cho tuần sau. Nhận xét được lưu lại, không tạo lại mỗi lần mở.
- Không có API key hoặc mất mạng: mọi phần trừ nhận xét AI vẫn hoạt động.
- Thông báo sáng Thứ Hai khi tổng kết tuần trước sẵn sàng (tắt được).

## 4. Yêu cầu phi chức năng

| Mã | Yêu cầu |
|---|---|
| NF1 | Offline-first: mọi tính năng trừ AI đều chạy không cần mạng. |
| NF2 | Mở app đến màn Hôm nay < 1 giây (cold start < 2 giây). |
| NF3 | API key không nằm trong source code, không xuất ra file sao lưu, không ghi log. |
| NF4 | Ảnh chỉ gửi tới AI API đã cấu hình; ảnh gốc không lưu lại, chỉ lưu thumbnail (tùy chọn). |
| NF5 | minSdk 26 (Android 8.0), targetSdk mới nhất. |
| NF6 | Hỗ trợ giao diện sáng / tối theo hệ thống. |
| NF7 | Logic tính toán có unit test đầy đủ. |
| NF8 | App hiển thị rõ ràng con số calo là **ước lượng**, không phải tư vấn y tế. |
