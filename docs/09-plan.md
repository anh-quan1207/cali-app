# 09 — Kế hoạch thực hiện

Ngày lập: 16/11/2026. Trạng thái đầu vào: đặc tả 01–08 xong, mockup toàn bộ màn S01–S45 xong (xem [design/00-direction.md](design/00-direction.md)), hệ thống token ở [DESIGN.md](../DESIGN.md). Chưa có dòng code nào.

Tài liệu này là **kế hoạch làm việc**, khác với 07 (lộ trình tổng quát). Mỗi giai đoạn có: mục tiêu, việc cụ thể, thứ tự, tiêu chí "xong", và cách kiểm tra. Làm xong một giai đoạn thì tick vào bảng tiến độ ở §9 và cập nhật ghi chú.

## 1. Nguyên tắc chung

1. **Làm theo chiều dọc, không theo chiều ngang.** Mỗi giai đoạn ra một bản app cài được và dùng được một việc (ví dụ GĐ2: nhập hồ sơ → ra calo mục tiêu). Không dựng hết database rồi mới dựng màn.
2. **Logic trước, giao diện sau.** `core.calc` viết trước, có test, không phụ thuộc Android. Giao diện chỉ hiển thị kết quả của logic đã kiểm chứng.
3. **Một nguồn sự thật cho giao diện:** [DESIGN.md](../DESIGN.md) → `Color.kt` / `Type.kt` / `Theme.kt`. Không gõ mã màu trực tiếp trong màn.
4. **Mỗi giai đoạn kết thúc bằng một bản cài thật lên máy** (`adb install`) và dùng thử ít nhất một ngày trước khi sang giai đoạn kế.
5. **Mockup là hợp đồng.** Mỗi màn Compose dựng xong phải so với ảnh ở `.impeccable/review/<màn>/` ở cả giao diện sáng và tối, cùng dữ liệu giả lập của mockup (mục tiêu 1.740, sáng 820 + trưa 490, nước 1.250 ml, cân 74,1 kg…).
6. **Không mở rộng phạm vi.** Tính năng mới ghi vào §8 (danh sách chờ), không chen vào giai đoạn đang làm.

## 2. Quyết định cần chốt trước khi code

| # | Câu hỏi | Mặc định nếu không chốt | Ảnh hưởng |
|---|---|---|---|
| D1 | Có làm iOS trong vòng 1 năm không? | **Không** → Android thuần như 06 | Nếu *Có*: đổi sang Kotlin Multiplatform ngay GĐ1 (SQLDelight hoặc Room KMP, Koin thay Hilt, Compose Multiplatform). Đổi sau GĐ3 rất tốn |
| D2 | Sao lưu JSON làm ở GĐ nào? | **Đưa lên GĐ3** (thay vì GĐ7) | Dữ liệu chỉ nằm trên máy; dùng thật từ GĐ3 nên cần xuất file sớm |
| D3 | Hãng AI | **Claude** như 03 §2 | Chỉ ảnh hưởng `AnthropicProvider`; interface `AiProvider` giữ nguyên |
| D4 | Dùng Vico hay tự vẽ biểu đồ bằng Canvas? | **Tự vẽ bằng Canvas** | Biểu đồ của app đơn giản (cột 7 ngày, đường cân nặng, vòng calo) nhưng có quy tắc riêng (Goal-Line Split, khe 3dp, đoạn vòng theo bữa) mà thư viện ngoài khó làm đúng |

Chốt D1 và D2 rồi mới bắt đầu GĐ1. D3, D4 có thể chốt ở GĐ4 và GĐ6.

## 3. Thứ tự giai đoạn và phụ thuộc

```
GĐ1 Khung + core.calc
 └─ GĐ2 Onboarding + Hồ sơ (S01–S03, S41)
     └─ GĐ3 Hôm nay + nhập tay + Món của tôi + cân cơ bản + SAO LƯU (S10, S12 không AI, S13, S14, S34, S45)
         ├─ GĐ4 AI: camera, chat, cài đặt AI (S11, S12 đầy đủ, S20, S42)
         ├─ GĐ5 Nước + nhắc nhở (S15, S43)
         └─ GĐ6 Lịch sử, thống kê, cân nặng, tổng kết tuần (S30–S33, S35, S44)
             └─ GĐ7 Hoàn thiện 1.0
```

GĐ4, GĐ5, GĐ6 độc lập với nhau, có thể đổi thứ tự. Khuyến nghị **GĐ4 trước** vì là giá trị cốt lõi và là phần rủi ro kỹ thuật cao nhất (API, validate, chi phí); phát hiện vấn đề sớm thì sửa đặc tả còn rẻ.

## 4. Chi tiết từng giai đoạn

### GĐ1 — Khung project và logic tính toán

**Mục tiêu:** build chạy được, theme đúng DESIGN.md, mọi công thức ở 02 và 08 §4 có test xanh.

**Việc, theo thứ tự:**

1. Tạo project: Gradle Kotlin DSL, Version Catalog, một module `app`, package theo 06 §2. minSdk 26.
2. Thêm phụ thuộc: Compose BOM, Material 3, Navigation Compose, Hilt, Room + KSP, DataStore, kotlinx.serialization, Coil, CameraX (chỉ khai báo, chưa dùng), JUnit, MockK, Turbine.
3. `core.ui.theme`: `Color.kt` (light + dark scheme, màu dữ liệu Đạm/Tinh bột/Béo, `warn`, `on-error`), `Type.kt` (Roboto Flex, tabular numbers, Display Large 56sp wdth 92), `Shape.kt` (16 / 12 / 8 / tròn), `Theme.kt` (theo hệ thống, tùy chọn Dynamic Color tắt mặc định).
4. `core.calc`: `NutritionCalculator` (BMR, TDEE, mục tiêu, kẹp 02 §5 đúng thứ tự trần 25% rồi sàn, macro, BMI châu Á, nước, ngày dự kiến 02 §9), `WeeklyStatsCalculator` (08 §3, §4, định nghĩa Đạt/Vượt/Thiếu 08 §2, Same-Days Rule), `MealTypeResolver` (05 §4.2), `AmberCheck` (lệch macro > 20%, công thức 03 §5).
5. Test cho toàn bộ mục 4, dùng ví dụ 02 §10 và 08 §5.4, cộng bộ số của mockup (74,1 kg → BMR 1.658,5, TDEE 2.280, 0,5 kg/tuần → 1.730, 0,75 → kẹp 1.710, ghi đè 1.400 → 1.710, macro 30/40/30 → 128/171/57 g).
6. `AppDatabase` v1 với đủ entity ở 04 §2, DAO tối thiểu, `exportSchema = true`. Test DAO in-memory cho `food_entry` và `my_food`.
7. `SettingsRepository` (DataStore) với đủ khóa 04 §3. `SecureKeyStore` theo 06 §5 (viết luôn vì nhỏ và test được).
8. Một màn "Theme preview" tạm (debug only): hiển thị bảng màu, kiểu chữ, vòng calo tĩnh, sáng và tối. Dùng để so với DESIGN.md.

**Xong khi:** `./gradlew test` xanh; cài được APK hiện màn preview; ảnh chụp preview ở chế độ tối khớp màu với `s10-f3.png`.

### GĐ2 — Onboarding và hồ sơ

**Mục tiêu:** cài app lần đầu → nhập thông tin → thấy calo mục tiêu → vào màn Hôm nay (tạm trống).

**Việc:**

1. `ProfileRepo`, `GoalSnapshot` (tạo snapshot khi lưu hồ sơ; 02 §11).
2. S01 (5 bước) → S02 (kết quả, có "cách tính", trường hợp bị kẹp) → S03 (thiết lập nhanh: API key, "Để sau"). Theo mockup `s01-s02-onboarding.html`, 13 khung.
3. S41 Hồ sơ & mục tiêu: sửa các trường, ghi đè kcal và macro %, công tắc "Tự tính lại khi ghi cân". Khung ghi đè + sheet ở `s40-settings.html`.
4. Validation ô nhập (cân, cao, tuổi) đúng thông báo lỗi trong mockup.
5. Điều hướng: `onboarding_done` quyết định màn đầu. S40 vỏ ngoài (danh sách mục) để vào S41.

**Xong khi:** gỡ app, cài lại, đi hết onboarding với bộ số mockup ra 1.740 kcal; đổi tốc độ ở S41 thấy số đổi và snapshot mới; cả hai giao diện khớp ảnh review.

### GĐ3 — Dùng được hằng ngày, chưa có AI

**Mục tiêu:** ghi món bằng tay và từ Món của tôi, thấy vòng calo chia đoạn theo bữa, cân cơ bản, xuất/nhập sao lưu. Từ đây dùng thật mỗi ngày.

**Việc:**

1. `DiaryRepo.observeDaySummary(date)` (04 §4), `MyFoodRepo` (search không dấu qua `name_normalized`, `topMyFoods`, tăng `use_count`), `WeightRepo` (1 bản ghi/ngày).
2. Component dùng chung: `CalorieRing` (đoạn theo bữa theo `eaten_at`, khe, vòng trong khi vượt, chữ giữa Remaining-First), `MacroBar` (Goal-Line Split), `MealCard`, `FoodRow` (One Food Row), snackbar Hoàn tác. Đây là phần tốn công nhất của GĐ3; làm thành màn preview trước, so với 8 khung S10.
3. S10 Hôm nay: 8 khung của mockup, kể cả trống và cỡ chữ 130%. Thẻ tổng kết tuần trước tạm ẩn (bật ở GĐ6).
4. S13 Thêm nhanh (tab Gần đây / Hay dùng / Yêu thích, tìm không dấu, Nhập tay) → S12 ở chế độ không AI → lưu. S14 Sửa món (hệ số khẩu phần, gõ số trực tiếp, sửa ngày cũ). S34 Món của tôi (danh sách, vuốt xoá, Hoàn tác, trống, tạo, sửa).
5. Ghi cân: sheet "Ghi cân" của S31 (chỉ sheet, chưa có biểu đồ), lỗi nhập "Có phải 74,1 kg?", tự tính lại mục tiêu nếu công tắc bật.
6. `BackupRepo`: xuất JSON theo 04 §5 qua `ACTION_CREATE_DOCUMENT`, nhập thay thế toàn bộ trong một transaction, từ chối `schema_version` cao hơn. S45 phần Sao lưu / Khôi phục (phần Xoá toàn bộ cũng làm luôn vì cùng màn: 2 bước, gõ XOÁ).
7. Test: round-trip sao lưu (xuất → nhập → giống hệt), `observeDaySummary` với món 23:30 thuộc ngày đó, `use_count`.

**Xong khi:** dùng thật 3 ngày liên tiếp, có ít nhất 10 món trong Món của tôi, xuất file sao lưu và nhập lại trên bản cài mới thấy đủ dữ liệu.

### GĐ4 — AI

**Mục tiêu:** chụp ảnh hoặc chat → thẻ xác nhận → lưu. Kể cả "tối qua ăn thêm 1 gói mì tôm" vào đúng hôm qua.

**Việc:**

1. `AiProvider` interface, `AiRequest/AiResult`, `FakeAiProvider` (payload mẫu, dùng khi chưa có key và cho test).
2. `PromptBuilder`: khối tĩnh (system prompt từ assets, `cache_control`) + khối động `<user_context>` (03 §4): mục tiêu, bữa hôm nay, 20 món hay ăn, ghi chú cá nhân; cắt lịch sử chat 10 lượt. Test định dạng.
3. `FoodLogValidator` (03 §5): JSON hỏng, số âm, món rỗng, Amber Check, `eaten_date` / `eaten_time` / `meal_type`. Test.
4. `ImageCompressor` (03 §7) và thumbnail 256 px.
5. `AnthropicProvider`: Messages API, tool `record_food_items`, `tool_choice` theo mode, đọc `usage` → `ai_call_log`. Che key trong log. Xử lý lỗi theo bảng 03 §8 (mất mạng, 401, 429, quá 60 giây, giữ ảnh + ghi chú để gửi lại).
6. S11 Camera (CameraX, forced dark, thiếu sáng, xin quyền, Photo Picker, xem lại + ghi chú, đang phân tích, các lỗi). S12 đầy đủ (Cần kiểm tra, câu hỏi làm rõ, nguồn ghi, sheet giờ/bữa, không nhận ra món).
7. S20 Chat: tin nhắn là luồng theo ngày ăn, thẻ ghi món 304dp đổi trạng thái tại chỗ, đính ảnh, lỗi, ghi cho hôm qua.
8. S42 Cài đặt AI: key (4 ký tự cuối), model, Kiểm tra kết nối (401 tô đỏ ô; mất mạng/429 báo dưới nút), system prompt toàn màn + Khôi phục mặc định, ghi chú cá nhân, thống kê sử dụng.
9. Test tay bắt buộc: 10 ảnh món Việt thật, 5 câu chat trong đó có "tối qua", "sáng nay 7h", "hôm qua trưa"; ghi lại chi phí mỗi lần gọi để đối chiếu 03 §9.

**Xong khi:** 10 ảnh thật cho kết quả qua Validator không lỗi; "tối qua ăn mì tôm" vào đúng ngày hôm qua, bữa Tối; không có key thì app vẫn dùng được với lối Thêm nhanh và báo rõ.

### GĐ5 — Nước và nhắc nhở

**Việc:** `WaterRepo`, `WaterReminderScheduler` (06 §4, test nhiều tình huống giờ), `WaterAlarmReceiver` / `WaterActionReceiver` (nút +250 / Để sau), `BootReceiver`, kênh thông báo, quyền `POST_NOTIFICATIONS`. S15 (11 khung: vượt mục tiêu vẫn primary, nhập lượng khác, sửa, vuốt xoá, ngày cũ, thiếu quyền, cách tính). S43 (mục tiêu, cốc, khung giờ, chu kỳ, kiểm tra quyền; phần thông báo tuần và công tắc AI tuần chỉ hiện giao diện, hoạt động ở GĐ6). Ô nước trên S10 bật.

**Xong khi:** tắt máy bật lại vẫn nhắc; bấm +250 trên thông báo thấy S10 đổi; ngoài khung giờ không nhắc.

### GĐ6 — Lịch sử, thống kê, cân nặng, tổng kết tuần

**Việc:**

1. `weeklyStats(weekStart)` dùng chung S32 và S35, `observeMonthStatus`. Same-Days Rule cho tuần/tháng đang diễn ra.
2. Biểu đồ Canvas dùng chung: cột 7 ngày với vạch mục tiêu và tách phần vượt (khe 3dp, error), đường cân nặng với đường mục tiêu, cột tháng chọn được bằng chạm (slider semantics).
3. S30 (lịch theo tuần, thẻ tuần có điểm, hôm nay chỉ viền, người mới), S33 (S10 cho ngày cũ, chênh lệch có dấu, "Tạo lại" khi tuần đã có nhận xét), S31 (TB tuần là số lớn, ETA từ hôm nay, sheet tính lại mục tiêu, Tất cả), S32 (tuần/tháng, so kỳ trước, chọn ngày).
4. S35: điểm 40/20/20/20, 4 làn, biểu đồ, ngày chưa ghi kéo điểm xuống; nhận xét AI mode WEEKLY_REVIEW (08 §5, dùng lại `AiProvider`), tự tạo khi mở nếu `weekly_ai_auto`, "Tạo lại" khi dữ liệu đổi. `WeeklySummaryScheduler` + thông báo Thứ Hai. Thẻ tuần trước trên S10 bật.
5. S44 Giờ bữa (kể cả trùng giờ).
6. Test `WeeklyStatsCalculator` đã có từ GĐ1; thêm test cho Same-Days Rule và tháng có ngày trước khi dùng app (không tính "chưa ghi").

**Xong khi:** số ở S32 và S35 cho tuần 03–09/11 trùng nhau và trùng mockup; thông báo Thứ Hai đến đúng giờ; S35 tuần đang diễn ra so cùng kỳ (sửa luôn chỗ còn sót của mockup: thẻ điểm tuần đang diễn ra phải ghi "cùng kỳ tuần trước").

### GĐ7 — Hoàn thiện 1.0

Rà lại bảng lỗi 03 §8 trên mọi màn, trạng thái Loading/Content/Error, cỡ chữ 130% và màn nhỏ, giao diện tối toàn bộ, TalkBack trên S10 và S12, R8 + rule serialization, keystore ký release, kiểm tra `data_extraction_rules.xml` loại `secure_prefs`, migration test cho mọi bản DB đã phát hành. Chạy lại bộ kiểm tra tay ở §6 từ đầu. Dán nhãn 1.0.

## 5. Ước lượng

Tính theo buổi làm việc tập trung (~3–4 giờ), có dùng Claude Code. Chỉ là định hướng, không phải cam kết.

| GĐ | Buổi | Rủi ro chính |
|---|---|---|
| 1 | 3–4 | Font Roboto Flex với trục `wdth` trên Compose; chọn đúng Compose BOM |
| 2 | 3–4 | Ít |
| 3 | 8–10 | `CalorieRing` đúng mọi trạng thái; vuốt xoá + Hoàn tác |
| 4 | 8–12 | API thay đổi so với lúc viết đặc tả; chất lượng nhận diện món Việt; chi phí |
| 5 | 3–4 | Alarm bị hệ thống trì hoãn, Doze; quyền thông báo Android 13+ |
| 6 | 8–10 | Biểu đồ Canvas; Same-Days Rule có nhiều trường hợp biên |
| 7 | 3–4 | |
| **Tổng** | **36–48** | |

## 6. Bộ kiểm tra tay (chạy cuối mỗi giai đoạn từ GĐ3)

1. Gỡ app, cài mới, onboarding với bộ số mockup → 1.740 kcal.
2. Ghi sáng 820, trưa 490 bằng Thêm nhanh → vòng 2 đoạn, "còn 430".
3. Thêm 1 món làm vượt nhẹ (≤10%) → hổ phách; thêm nữa cho vượt nhiều → đỏ, vòng trong.
4. Sửa món sang ngày hôm qua → hôm nay giảm, hôm qua tăng.
5. Vuốt xoá → Hoàn tác → món trở lại.
6. Xuất sao lưu → gỡ app → cài → nhập → dữ liệu đủ.
7. Đổi giao diện hệ thống sang tối → mọi màn đã làm khớp ảnh review.
8. Cỡ chữ hệ thống 130% → không vỡ bố cục S10.
9. (GĐ4+) Chụp 1 ảnh thật; chat "tối qua ăn thêm 1 gói mì tôm".
10. (GĐ5+) Đợi 1 lần nhắc nước, bấm +250 trên thông báo.
11. (GĐ6+) Mở S35 tuần trước, so số với S32.

## 7. Cách làm việc với Claude Code

- Mỗi buổi bắt đầu bằng đọc §9 (tiến độ) và việc kế tiếp trong §4; không dựng lại ngữ cảnh từ đầu.
- Mỗi màn: đọc mockup HTML + ảnh review + đoạn 05 tương ứng + DESIGN.md → viết Composable với dữ liệu preview đúng bộ số mockup → chụp preview sáng/tối → so với ảnh → sửa → nối ViewModel.
- Logic: viết test từ ví dụ trong đặc tả trước, rồi mới viết hàm.
- Khi đặc tả và mockup mâu thuẫn: mockup thắng về giao diện, đặc tả thắng về số và quy tắc; ghi lại ở §8.
- Nếu dùng git (khuyến nghị bắt đầu từ GĐ1): mỗi giai đoạn một nhánh, gộp khi đạt "Xong khi".

## 8. Danh sách chờ (không làm trong 1.0)

- Widget Glance, copy bữa/ngày, thống kê chi phí AI chi tiết, tổng kết tháng, chia sẻ ảnh tổng kết (07 GĐ8).
- Nhắc ghi bữa (07 Q6).
- Đồng bộ nhiều máy / iOS (xem D1).
- Review lần hai cho mockup sau các lần sửa (hiện mới xem bằng mắt).

Mâu thuẫn đặc tả / mockup phát hiện khi code:

GĐ1 (16/11/2026):
1. 09 §4 ghi Display Large 56sp; DESIGN.md + mockup là 57sp → dùng **57sp**.
2. 09 §4 ghi shape 16/12/8/tròn; DESIGN.md có thêm 28dp (sheet) và 4dp (ô nhập, snackbar) → theo DESIGN.md (4/8/12/16/28/full).
3. 06 §1 ghi biểu đồ Vico; D4 đã chốt Canvas → Canvas.
4. 06 §1 ghi Retrofit hoặc Ktor; CLAUDE.md chỉ nêu OkHttp → để GĐ4 quyết.
5. 02 không nói kcal ghi đè có bị kẹp không; bộ số mẫu (ghi đè 1.400 → 1.710) ngầm có → **ghi đè cũng qua kẹp**. Trần thâm hụt 25% áp cho mọi giá trị dưới TDEE (02 §5 chỉ nói "khi giảm cân").
6. 03 §5 không nói mẫu số của "lệch > 20%" → chọn `|kcal − (4P+4C+9F)| / kcal của AI`; lệch đúng 20% chưa gắn cờ.
7. 06 §8 cho phép JUnit5 → chọn JUnit4 vì Robolectric (test Room in-memory) chỉ chạy JUnit4.
8. 02 §10 tính tinh bột từ đạm **đã làm tròn** (122 g × 4 = 488) nhưng béo **chưa làm tròn** (469,8) → code làm đúng như vậy để ra 196 g.
9. 08 §4: điểm tổng = làm tròn tổng chưa làm tròn của 4 phần; có thể lệch 1 so với cộng 4 phần đã làm tròn (ví dụ 5.4 thì trùng: 71).
10. 08 §3.5 "bữa có calo trung bình cao nhất": chọn TB tổng kcal của bữa trên các ngày có bữa đó.
11. 05 §4.1 "giờ giữa khung": bữa Phụ có hai khung → lấy khung đầu tính từ mốc Sáng (14:00–17:30 → 15:45).
12. 04 §2 `ai_call_log.mode` chỉ có PHOTO_LOG / CHAT; 03 §3.3 có WEEKLY_REVIEW → enum có cả ba.
13. 08 §5.4 ghi tuần "10/11 – 16/11" theo lịch giả định của mockup; lịch thật Thứ Hai là 09/11 và 16/11/2026. Test dùng lịch thật, giữ nguyên số.
14. DESIGN.md không có token cho secondary/tertiary, surfaceContainerLowest/High, surfaceDim/Bright → gán theo token gần nhất (primary, surface-cont…) để không lọt màu tím mặc định của M3.
15. DESIGN.md chỉ định nghĩa 12 kiểu chữ; headlineLarge/headlineSmall/titleSmall của M3 giữ cỡ mặc định nhưng dùng Roboto Flex.

Môi trường (máy dev, không phải mâu thuẫn đặc tả): JDK không tạo được Unix socket trong `C:\Users\…\Temp` → Gradle không nói chuyện được với daemon/worker ("Unable to establish loopback connection"). Cách chạy: `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=E:\Cali\.gradle\uds` (thư mục đã gitignore). Robolectric 4.18 chưa hỗ trợ SDK 36 → `robolectric.properties` ghim sdk=35.

## 9. Tiến độ

| GĐ | Trạng thái | Bắt đầu | Xong | Ghi chú |
|---|---|---|---|---|
| D1–D2 chốt | ☑ | 16/11/2026 | 16/11/2026 | D1: không iOS, Android thuần. D2: sao lưu JSON ở GĐ3 |
| 1 | ☑ | 16/11/2026 | 16/11/2026 | Nhánh `gd1`. AGP 9.5.1 / Gradle 9.9.1 / Kotlin 2.4.21 / Compose BOM 2026.09.00, compileSdk 37 (Platform 37 cài 16/11), targetSdk 36. 62 unit test xanh (core.calc 48, Room 6, prefs 6, định dạng số 1, bỏ dấu 1) + 1 test thiết bị (Keystore thật, AVD Android 16). Lint 0 lỗi. Theme preview chụp sáng/tối ở `.impeccable/review/gd1-theme-preview/`, màu khớp s10 f0/f3. Còn: người dùng cài lên điện thoại thật; Material Symbols để GĐ3 |
| 2 | ☐ | | | |
| 3 | ☐ | | | |
| 4 | ☐ | | | |
| 5 | ☐ | | | |
| 6 | ☐ | | | |
| 7 | ☐ | | | |
