# 06 — Kiến trúc kỹ thuật

## 1. Tech stack

| Hạng mục | Lựa chọn |
|---|---|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose + Material 3, Navigation Compose |
| Kiến trúc | MVVM + Unidirectional Data Flow (UiState / UiEvent), Clean-ish layering |
| DI | Hilt |
| Bất đồng bộ | Coroutines + Flow |
| CSDL | Room (+ KSP) |
| Cấu hình | DataStore Preferences |
| Mạng | OkHttp + Retrofit (hoặc Ktor client) + kotlinx.serialization |
| Camera | CameraX; chọn ảnh bằng Photo Picker |
| Ảnh | Coil (hiển thị), xử lý nén bằng Bitmap/ImageDecoder |
| Biểu đồ | Vico |
| Thông báo / lịch | AlarmManager + BroadcastReceiver; WorkManager cho việc nền khác |
| Widget | Jetpack Glance (giai đoạn 2) |
| Bảo mật key | Android Keystore (AES-GCM), lưu ciphertext trong DataStore |
| Test | JUnit5/JUnit4, Turbine, MockK, Room in-memory, Compose UI test |
| Build | Gradle Kotlin DSL + Version Catalog |
| minSdk / targetSdk | 26 / mới nhất |

## 2. Cấu trúc module & package

Một module `app` là đủ cho app cá nhân, chia package theo tầng và tính năng:

```
com.cali
 ├─ core
 │   ├─ calc          NutritionCalculator, các enum (thuần Kotlin, không Android)
 │   ├─ data
 │   │   ├─ db        AppDatabase, entity, dao, converter
 │   │   ├─ prefs     SettingsRepository (DataStore), SecureKeyStore
 │   │   └─ repo      ProfileRepo, DiaryRepo, MyFoodRepo, WaterRepo, WeightRepo, ChatRepo, BackupRepo
 │   ├─ ai
 │   │   ├─ AiProvider, AiRequest, AiResult
 │   │   ├─ neuralwatt NeuralwattProvider (+ DTO, Config)
 │   │   ├─ PromptBuilder   ghép system prompt + user_context
 │   │   ├─ FoodLogValidator
 │   │   └─ ImageCompressor
 │   ├─ notify        WaterReminderScheduler, receivers, NotificationHelper
 │   └─ ui            theme, component dùng chung (CalorieRing, MacroBar, ...)
 ├─ feature
 │   ├─ onboarding
 │   ├─ today
 │   ├─ capture       camera + review sheet
 │   ├─ chat
 │   ├─ history       lịch, chi tiết ngày, thống kê, cân nặng
 │   ├─ myfoods
 │   └─ settings
 └─ MainActivity, CaliApp, AppNavHost
```

Quy tắc phụ thuộc: `feature → core`; `core.calc` không phụ thuộc gì; UI không gọi DAO trực tiếp, chỉ qua repository / use case.

## 3. Luồng dữ liệu tiêu biểu: chụp ảnh ghi món

```
CaptureScreen ──(ảnh, ghi chú, bữa)──> CaptureViewModel
   → ImageCompressor.compress()
   → PromptBuilder.build(PHOTO_LOG, context từ DiaryRepo + ProfileRepo + MyFoodRepo)
   → AiProvider.send()  ── ghi AiCallLog
   → FoodLogValidator.validate()
   → UiState.Review(items)           → ReviewSheet (sửa hệ số, tên, xóa…)
   → DiaryRepo.saveBatch(items, meal, date)   (+ MyFoodRepo.upsert nếu tick)
   → TodayScreen tự cập nhật qua Flow
```

## 4. Nhắc uống nước

- `WaterReminderScheduler` tính lần nhắc kế tiếp:
  `next = max(lần uống gần nhất, lần nhắc trước) + interval`, kẹp trong khung giờ; ngoài khung → đầu khung ngày mai; đã đủ mục tiêu → đầu khung ngày mai.
- Đặt **một** alarm tại một thời điểm bằng `AlarmManager.setAndAllowWhileIdle` (không cần exact alarm → không cần quyền `SCHEDULE_EXACT_ALARM`; lệch vài phút chấp nhận được).
- `WaterAlarmReceiver`: kiểm tra lại điều kiện → hiện thông báo → đặt alarm kế tiếp.
- `WaterActionReceiver`: xử lý nút "+250 ml" (ghi `water_entry`, cập nhật thông báo, đặt lại lịch) và "Để sau" (+30 phút).
- `BootReceiver` (`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`): đặt lại lịch.
- Mỗi lần ghi nước trong app cũng gọi `reschedule()`.
- Quyền: `POST_NOTIFICATIONS` (Android 13+), `RECEIVE_BOOT_COMPLETED`.

### Thông báo tổng kết tuần
- `WeeklySummaryScheduler` đặt alarm Thứ Hai kế tiếp vào `weekly_notify_time`; `WeeklySummaryReceiver` tính điểm tuần trước (trên máy, không gọi AI) → hiện thông báo → đặt alarm tuần sau. Kênh thông báo riêng "Tổng kết tuần". Được đặt lại trong `BootReceiver`.
- Logic tổng hợp nằm trong `core.calc.WeeklyStatsCalculator` (thuần Kotlin, có unit test với ví dụ ở 08 §5.4).

## 5. Bảo mật API key

- Người dùng nhập key ở S03 / S42.
- `SecureKeyStore` tạo khóa AES-256-GCM trong **Android Keystore** (không xuất được), mã hóa key → lưu ciphertext + IV (Base64) trong DataStore riêng `secure_prefs`.
- Chỉ giải mã ngay trước khi gọi API, không giữ trong biến toàn cục lâu dài.
- Không ghi log key; OkHttp logging interceptor chỉ bật ở debug và **che header** `x-api-key` / `Authorization`.
- Loại trừ `secure_prefs` khỏi Auto Backup (`data_extraction_rules.xml`) — khóa Keystore không đi theo bản backup nên ciphertext cũng vô dụng ở máy khác.

## 6. Gọi AI (Neuralwatt, chốt 16/11/2026, 09 §8 G4-14…18)

- `POST /v1/chat/completions` (OpenAI-compatible), header `Authorization: Bearer <key>`.
- Một system message: phần tĩnh (prompt) đứng đầu để prefix cache tự động, nối sau là `<user_context>`.
- Ảnh gửi dạng phần `image_url` với `data:image/jpeg;base64,…` (cần xác nhận ở lần test tay đầu).
- `tools = [record_food_items]` (`type: function`); PHOTO_LOG ép gọi theo tên; CHAT: `auto`. `reasoning_effort: "low"`.
- `arguments` của tool call là chuỗi JSON → parse rồi qua `FoodLogValidator`.
- 429 / 5xx retry 2 s → 5 s trừ khi body có `retryable: false`; 402 = hết số dư (`QUOTA`).
- Kiểm tra kết nối: `GET /v1/models`, chỉ đạt khi `scope` khác `public`.
- Đọc `usage` (`prompt_tokens`, `completion_tokens`, `prompt_tokens_details.cached_tokens`) và thời gian để ghi `AiCallLog`.

## 7. Xử lý lỗi & trạng thái UI

- Repository trả `Result`/sealed type, không ném exception ra UI.
- Mỗi màn có `UiState` gồm `Loading / Content / Error`; lỗi mạng & AI hiển thị theo bảng ở 03 §8.

## 8. Kiểm thử

| Tầng | Nội dung |
|---|---|
| `core.calc` | 100% công thức, ví dụ 02 §10, các trường hợp bị kẹp, BMI biên, làm tròn |
| `FoodLogValidator` | JSON hỏng, số âm, lệch macro, món rỗng |
| `PromptBuilder` | ngữ cảnh đúng định dạng, cắt lịch sử 10 lượt, thay ảnh cũ bằng mô tả |
| `WaterReminderScheduler` | tính thời điểm nhắc kế tiếp với nhiều tình huống giờ |
| Room | DAO + migration test |
| Backup | xuất → nhập lại → dữ liệu giống hệt |
| UI | smoke test onboarding & luồng ghi món với `FakeAiProvider` |

`FakeAiProvider` trả về payload mẫu, dùng cho test và chế độ "demo" khi chưa có API key.

## 9. Build & cài đặt

- Build `release` ký bằng keystore cá nhân (lưu ngoài repo, đường dẫn trong `local.properties`).
- Cài qua `adb install` hoặc copy APK vào máy.
- R8 bật cho release; giữ rule cho kotlinx.serialization.
- `.gitignore`: `local.properties`, `*.jks`, `/build`.
