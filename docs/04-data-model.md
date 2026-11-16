# 04 — Mô hình dữ liệu

Dữ liệu có cấu trúc lưu trong **Room** (SQLite). Cấu hình và giá trị đơn lẻ lưu trong **DataStore**. API key lưu riêng, mã hóa (xem 06).

## 1. Quy ước chung

- Ngày lưu dạng `LocalDate` → chuỗi ISO `yyyy-MM-dd` (TypeConverter). Thời điểm lưu dạng `Instant` → epoch millis.
- "Ngày" của một bản ghi theo **múi giờ của máy** tại lúc ghi.
- Mỗi món ăn trong nhật ký lưu **giá trị tuyệt đối** (kcal, macro của phần đã ăn), không tham chiếu động tới Món của tôi. Sửa Món của tôi không làm thay đổi lịch sử.
- Xóa: xóa cứng (app cá nhân, đã có sao lưu).

## 2. Các bảng Room

### `user_profile` (chỉ 1 dòng, `id = 1`)
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Int PK | luôn = 1 |
| sex | String | MALE / FEMALE |
| birth_year | Int | |
| height_cm | Double | |
| activity_level | String | enum ở 02 §3 |
| goal_type | String | LOSE / MAINTAIN / GAIN |
| target_weight_kg | Double? | |
| weekly_rate_kg | Double | |
| auto_recalc_on_weigh_in | Boolean | mặc định false |
| created_at / updated_at | Long | |

Cân nặng hiện tại **không** lưu ở đây, lấy từ `weight_entry` mới nhất.

### `goal_snapshot`
Mục tiêu có hiệu lực từ một ngày. Mục tiêu của ngày D = snapshot có `effective_from` lớn nhất ≤ D.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| effective_from | LocalDate | unique; tính lại trong cùng ngày thì ghi đè |
| weight_kg_used | Double | cân nặng dùng để tính |
| bmr / tdee | Double | |
| target_kcal | Int | |
| protein_g / carb_g / fat_g | Int | |
| water_ml | Int | |
| is_kcal_overridden | Boolean | |
| is_macro_overridden | Boolean | |
| clamp_reason | String? | nếu bị giới hạn an toàn |
| created_at | Long | |

### `weight_entry`
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| date | LocalDate | unique |
| weight_kg | Double | |
| note | String? | |
| created_at | Long | |

### `food_entry` — món đã ăn
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| date | LocalDate | index |
| eaten_at | Long | thời điểm ăn (epoch millis), index; `date` = ngày của `eaten_at` theo múi giờ máy. Cách lấy giá trị: 05 §4.1 |
| meal_type | String | BREAKFAST / LUNCH / DINNER / SNACK, tự suy từ `eaten_at` (05 §4.2) nếu không được chỉ định |
| meal_type_manual | Boolean | true nếu người dùng tự chọn bữa → đổi giờ không tự đổi bữa |
| name | String | |
| portion_desc | String? | |
| grams | Double? | |
| kcal | Double | |
| protein_g / carb_g / fat_g | Double | mặc định 0 |
| source | String | AI_PHOTO / AI_CHAT / MY_FOOD / MANUAL |
| ai_confidence | Double? | |
| ai_call_id | Long? | FK → `ai_call_log` (SET NULL) |
| my_food_id | Long? | FK → `my_food` (SET NULL) |
| batch_id | String? | UUID, gom các món được lưu cùng 1 lần xác nhận |
| thumbnail_path | String? | ảnh 256 px trong bộ nhớ app |
| sort_order | Int | |
| created_at / updated_at | Long | |

### `my_food` — Món của tôi
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| name | String | index, tìm kiếm không dấu (lưu thêm `name_normalized`) |
| name_normalized | String | bỏ dấu, chữ thường |
| portion_desc | String? | khẩu phần chuẩn |
| grams | Double? | |
| kcal | Double | cho khẩu phần chuẩn |
| protein_g / carb_g / fat_g | Double | |
| is_favorite | Boolean | |
| use_count | Int | +1 mỗi lần thêm vào nhật ký |
| last_used_at | Long? | |
| created_at / updated_at | Long | |

### `water_entry`
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| date | LocalDate | index |
| amount_ml | Int | |
| source | String | APP / NOTIFICATION / WIDGET |
| created_at | Long | thời điểm uống |

### `chat_message`
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| date | LocalDate | index, mỗi ngày là một luồng chat |
| role | String | USER / ASSISTANT |
| text | String? | |
| thumbnail_path | String? | ảnh người dùng gửi (thumbnail) |
| food_log_json | String? | payload `record_food_items` nếu AI ghi món |
| food_log_status | String? | PENDING / SAVED / DISCARDED |
| ai_call_id | Long? | |
| created_at | Long | |

### `ai_call_log`
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| mode | String | PHOTO_LOG / CHAT |
| provider / model | String | |
| input_tokens / output_tokens | Int? | |
| cache_read_tokens | Int? | |
| duration_ms | Long | |
| success | Boolean | |
| error_type | String? | |
| created_at | Long | |

### `weekly_review` — nhận xét AI theo tuần
| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | Long PK | |
| week_start | LocalDate | Thứ Hai của tuần, unique |
| score | Int | điểm tuần tại thời điểm tạo |
| stats_hash | String | hash của số liệu tuần đã gửi cho AI; khác hash hiện tại → "Dữ liệu đã thay đổi" |
| review_json | String | payload `submit_weekly_review` |
| is_partial_week | Boolean | tạo khi tuần chưa kết thúc |
| ai_call_id | Long? | |
| created_at | Long | |

Số liệu tuần (mục 3 của 08) **không lưu**, luôn tính lại từ `food_entry`, `water_entry`, `weight_entry`, `goal_snapshot`.

## 3. DataStore (Preferences)

| Khóa | Mặc định | Ghi chú |
|---|---|---|
| `onboarding_done` | false | |
| `ai_provider` | "anthropic" | |
| `ai_model_vision` | "claude-sonnet-5-5" | |
| `ai_model_chat` | "claude-haiku-5-5" | |
| `system_prompt_override` | null | null = dùng bản mặc định trong assets |
| `system_prompt_default_version` | 1 | |
| `personal_notes` | "" | ghi chú cá nhân cho AI |
| `macro_pct_override` | null | JSON {p, c, f} |
| `kcal_override` | null | |
| `water_enabled` | true | |
| `water_start` / `water_end` | 07:00 / 22:00 | |
| `water_interval_min` | 90 | |
| `water_cup_small_ml` / `water_cup_large_ml` | 250 / 500 | |
| `meal_times` | xem 05 §4 | |
| `save_thumbnails` | true | |
| `weekly_notify_enabled` | true | thông báo tổng kết sáng Thứ Hai |
| `weekly_notify_time` | 08:00 | |
| `weekly_ai_auto` | true | tự tạo nhận xét AI khi mở tổng kết tuần đã kết thúc |

API key **không** nằm trong bảng này (xem 06 §5).

## 4. Truy vấn chính

- `observeDaySummary(date)`: tổng kcal/macro theo bữa + tổng ngày, tổng nước, goal snapshot áp dụng → Flow cho màn Hôm nay.
- `observeMonthStatus(yearMonth)`: mỗi ngày → tổng kcal & mục tiêu → tô màu lịch.
- `weeklyStats(weekStart)`: toàn bộ chỉ số ở 08 §3 (định nghĩa Đạt/Vượt/Thiếu theo 08 §2), dùng chung cho S32 và S35.
- `topMyFoods(limit)`: theo `use_count` giảm dần, dùng cho ngữ cảnh AI.
- `searchMyFoods(query)`: LIKE trên `name_normalized`.

## 5. Sao lưu JSON

```json
{
  "app": "Cali",
  "schema_version": 1,
  "exported_at": "2026-11-13T12:00:00+07:00",
  "profile": { ... },
  "goal_snapshots": [ ... ],
  "weights": [ ... ],
  "food_entries": [ ... ],
  "my_foods": [ ... ],
  "water_entries": [ ... ],
  "chat_messages": [ ... ],
  "settings": { ... }   // DataStore, KHÔNG gồm API key
}
```

- Không xuất thumbnail và `ai_call_log`.
- Khi nhập: `schema_version` cao hơn app hiện tại → từ chối; thấp hơn → chạy chuyển đổi. Nhập là thao tác **thay thế toàn bộ** trong một transaction.

## 6. Migration

- Room `exportSchema = true`, lưu schema JSON trong repo.
- Mỗi thay đổi schema có Migration viết tay + test migration.
