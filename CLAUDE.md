# Cali — hướng dẫn cho Claude

App Android cá nhân tính và kiểm soát calo: ghi món bằng AI (chụp ảnh / chat), nhập tay, Món của tôi, nhắc uống nước, cân nặng, tổng kết tuần. Một người dùng, cài APK trực tiếp, không backend, không phát hành store.

## Quy tắc giao tiếp
- Luôn trả lời bằng **tiếng Việt**. Giao diện app cũng tiếng Việt. Đơn vị: kg, cm, ml, kcal, gram; số kiểu Việt (1.740 · 74,1).
- Tên app là **Cali** (package `com.cali`). Không dùng tên cũ "CalCal".

## Bắt đầu mỗi phiên
1. Đọc [docs/09-plan.md](docs/09-plan.md) §9 (bảng tiến độ) để biết đang ở giai đoạn nào, rồi đọc mục của giai đoạn đó ở §4.
2. Chỉ đọc phần đặc tả liên quan tới việc đang làm, không đọc lại toàn bộ.
3. Xong việc: cập nhật §9 (trạng thái, ngày, ghi chú) và ghi mâu thuẫn phát hiện được vào §8.

## Bản đồ tài liệu
| File | Dùng khi |
|---|---|
| `docs/01-product-requirements.md` | Phạm vi, tính năng F01–F16, tiêu chí chấp nhận |
| `docs/02-calculations.md` | BMR, TDEE, mục tiêu, kẹp an toàn §5, macro, BMI, nước, ETA. §10 là ví dụ làm unit test |
| `docs/03-ai-integration.md` | Luồng AI, context, tool `record_food_items`, validate, system prompt, lỗi & retry |
| `docs/04-data-model.md` | Bảng Room, khóa DataStore, truy vấn chính, sao lưu JSON |
| `docs/05-screens-ux.md` | Danh sách màn S01–S45, điều hướng, quy tắc giờ ăn / bữa (§4) |
| `docs/06-architecture.md` | Tech stack, package, nhắc nước, bảo mật key, test |
| `docs/07-roadmap.md` | Giai đoạn tổng quát, câu hỏi mở |
| `docs/08-weekly-review.md` | Đạt/Vượt/Thiếu, chỉ số tuần, điểm tuần, nhận xét AI, S35 |
| `docs/09-plan.md` | **Kế hoạch thực hiện**: việc, thứ tự, tiêu chí xong, kiểm tra tay, tiến độ |
| `DESIGN.md` (+ `.impeccable/design.json`) | Token màu, chữ, bo góc, khoảng cách, chuyển động, component và các quy tắc đặt tên |
| `PRODUCT.md` | Người dùng, giọng văn, nguyên tắc sản phẩm |
| `docs/design/00-direction.md` | Hướng thiết kế đã chốt, quyết định riêng từng màn |
| `docs/design/s*.html` | Mockup từng màn; `?f=N` hiện một khung |
| `.impeccable/review/<màn>/` | Ảnh chụp mockup để so khi dựng Compose |

## Quyết định đã chốt
- Android native: Kotlin + Jetpack Compose + Material 3. Không iOS (D1). Một module `app`.
- MVVM + UDF (UiState/UiEvent), Hilt, Coroutines/Flow, Room (KSP), DataStore, kotlinx.serialization, OkHttp, CameraX, Coil. Biểu đồ tự vẽ bằng Canvas (D4). minSdk 26.
- AI: Neuralwatt (`/v1/chat/completions`, OpenAI-compatible), gọi thẳng API bằng key người dùng; model theo ID cụ thể, không dùng bí danh `nw-*`: `kimi-k3.1-pro` ảnh, `deepseek-v4.1-flash` chat, dự phòng `qwen3.6-35b` (09 §8 G4-14). Mọi kết quả AI phải qua bước người dùng xác nhận trước khi lưu.
- Dữ liệu chỉ ở máy. Sao lưu/khôi phục JSON làm ở GĐ3 (D2).
- Tuần bắt đầu Thứ Hai. Điểm tuần Calo 40 / Đạm 20 / Nước 20 / Ghi chép 20.

## Cấu trúc package (06 §2)
```
com.cali
 ├─ core/calc     thuần Kotlin, KHÔNG phụ thuộc Android (NutritionCalculator, WeeklyStatsCalculator, MealTypeResolver…)
 ├─ core/data     db (entity, dao), prefs (SettingsRepository, SecureKeyStore), repo
 ├─ core/ai       AiProvider, anthropic/, PromptBuilder, FoodLogValidator, ImageCompressor
 ├─ core/notify   nhắc nước, tổng kết tuần, receivers
 ├─ core/ui       theme + component dùng chung (CalorieRing, MacroBar, MealCard, FoodRow…)
 └─ feature/      onboarding, today, capture, chat, history, myfoods, settings
```
Phụ thuộc: `feature → core`; UI không gọi DAO trực tiếp; repository trả `Result`/sealed type, không ném exception ra UI.

## Quy tắc code
- **Màu, chữ, bo góc chỉ lấy từ theme** (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt` sinh từ DESIGN.md). Không gõ mã hex hay sp trực tiếp trong màn.
- **Mockup là hợp đồng giao diện, đặc tả là hợp đồng về số.** Khi lệch nhau ghi vào 09 §8, không tự chọn im lặng.
- Logic: viết test từ ví dụ trong đặc tả **trước**, rồi mới viết hàm. Không sửa test cho khớp code sai.
- Món trong nhật ký lưu **giá trị tuyệt đối**, không tham chiếu Món của tôi. Ngày của món = ngày của `eaten_at`.
- Kẹp mục tiêu: trần thâm hụt 25% trước, rồi mới sàn (02 §5).
- Chuỗi giao diện tiếng Việt để trong `strings.xml`.
- Không thêm tính năng ngoài giai đoạn đang làm; ghi vào 09 §8.

## Quy tắc thiết kế cần nhớ (chi tiết ở DESIGN.md)
- Seed chàm `#4355B9`, tonal surface, không đổ bóng (trừ FAB). Roboto Flex, số tabular. Material Symbols Rounded. Chạm tối thiểu 48dp.
- Trong mục tiêu = primary · vượt ≤10% = hổ phách (`warn`) · vượt nhiều = error. Luôn kèm chữ, không chỉ dựa vào màu.
- Mỗi màn chỉ **một** số Display Large (Remaining-First).
- Thay đổi cân nặng và mũi tên so sánh luôn trung tính (No-Judgement Scale). Nước vượt mục tiêu vẫn primary.
- Ngày "Thiếu" là chàm nhạt, không bao giờ hổ phách; hổ phách chỉ cho cờ nghi ghi thiếu / Amber Check.
- Hộp thoại xác nhận chỉ có ở "Xoá toàn bộ dữ liệu"; xoá khác dùng vuốt + Hoàn tác.
- Camera luôn tối. Hỗ trợ sáng và tối cho mọi màn khác.
- Mockup dùng lịch giả định 13/11/2026 = Thứ Năm; code theo lịch thật.

## Dữ liệu mẫu (dùng cho preview, test và so ảnh)
Mục tiêu 1.740 kcal, TDEE 2.293 (75,0 kg); macro 122/196/52 g; nước 2.600 ml. Hôm nay: sáng 820 + trưa 490 = 1.310, còn 430; nước 1.250 ml. Cân 76,0 → 74,1 kg, mục tiêu 68 kg. Ở 74,1 kg: BMR 1.658,5, TDEE 2.280; 0,5 kg/tuần → 1.730; 0,75 → kẹp 1.710; ghi đè 1.400 → 1.710; macro 30/40/30 → 128/171/57 g. Tuần 03–09/11 điểm 71.

## Lệnh
```bash
./gradlew test            # unit test
./gradlew installDebug    # cài lên máy / emulator
./gradlew lint
adb exec-out screencap -p > screen.png   # chụp màn hình để so mockup
```
Trên Windows dùng `gradlew.bat` nếu không chạy qua Git Bash.

## Kiểm chứng trước khi báo "xong"
- Chạy test và build; báo kết quả thật, kể cả khi đỏ.
- Màn giao diện: chụp sáng và tối, so với ảnh trong `.impeccable/review/<màn>/`.
- Cuối giai đoạn: chạy bộ kiểm tra tay 09 §6 (phần Claude làm được) và nói rõ phần nào cần người dùng tự làm.

## Bảo mật
- Không bao giờ ghi API key vào file, log hay commit. Key chỉ nhập trong app, mã hoá bằng Android Keystore (`SecureKeyStore`).
- Log OkHttp chỉ ở debug và che header `Authorization` / `x-api-key`.
- `.gitignore`: `local.properties`, `*.jks`, `/build`, `.gradle/`.
