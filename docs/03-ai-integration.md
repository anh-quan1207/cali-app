# 03 — Tích hợp AI

## 1. Nguyên tắc

1. **AI chỉ ước lượng, người dùng quyết định.** Không có kết quả AI nào được lưu vào nhật ký mà không qua Thẻ xác nhận.
2. **Đầu ra có cấu trúc.** Khi ghi món, AI phải trả về dữ liệu qua **tool call** với JSON schema cố định, không phải văn bản tự do.
3. **Không phụ thuộc hãng.** Code gọi AI qua interface `AiProvider`; đổi hãng chỉ cần thêm một implementation.
4. **Gọi trực tiếp, không backend.** API key do người dùng nhập, mã hóa trên máy (xem 06).

## 2. Hãng & model (đã chốt 16/11/2026: Neuralwatt, 09 §8 G4-14)

Gọi `POST https://api.neuralwatt.com/v1/chat/completions` (OpenAI-compatible) bằng key người dùng, header `Authorization: Bearer`.
Model ghi **ID cụ thể**, không dùng bí danh `nw-large` / `nw-flash` / `nw-small` (có thể bị trỏ sang model khác).

| Mục đích | Model mặc định | Lý do |
|---|---|---|
| Phân tích ảnh | `kimi-k3.1-pro` | Nhận diện món & ước lượng khẩu phần từ ảnh cần model mạnh; gửi `reasoning_effort: "low"` để nhanh (G4-16) |
| Chat văn bản | `deepseek-v4.1-flash` | Nhanh, rẻ, đủ cho ghi món bằng chữ và tư vấn |
| Dự phòng ảnh | `qwen3.6-35b` | Nếu Kimi ước sai hoặc chậm |

Model là chuỗi cấu hình trong Cài đặt, đổi được không cần build lại.

```kotlin
interface AiProvider {
    suspend fun send(request: AiRequest): AiResult
    suspend fun testConnection(): Result<Unit>
}

data class AiRequest(
    val mode: AiMode,                 // PHOTO_LOG, CHAT, WEEKLY_REVIEW (xem 08)
    val systemPrompt: String,         // prompt gốc (có thể đã sửa trong Cài đặt)
    val context: UserContext,         // xem mục 4
    val history: List<ChatTurn>,      // tối đa 10 lượt gần nhất trong ngày
    val userText: String?,
    val imageJpeg: ByteArray?,
)

sealed interface AiResult {
    data class FoodLog(val payload: FoodLogPayload, val usage: TokenUsage) : AiResult
    data class Text(val text: String, val usage: TokenUsage) : AiResult
    data class Error(val type: AiErrorType, val message: String) : AiResult
}
```

## 3. Luồng xử lý

### 3.1 Chụp ảnh (PHOTO_LOG)
```
Chụp / chọn ảnh → (ghi chú tùy chọn) → nén ảnh → gửi kèm context
→ bắt buộc gọi tool `record_food_items` (`tool_choice = {type: function, function: {name: record_food_items}}`)
→ validate → Thẻ xác nhận → người dùng sửa → Lưu
```

### 3.2 Chat (CHAT)
```
Người dùng gửi chữ (± ảnh) → gửi kèm context + lịch sử chat trong ngày
→ tool_choice = auto
   ├─ AI gọi `record_food_items` → hiện Thẻ món ăn trong chat → xác nhận → Lưu
   └─ AI trả lời chữ → hiện tin nhắn (tư vấn, giải thích)
```

### 3.3 Đánh giá tuần (WEEKLY_REVIEW)
Gửi số liệu tuần đã tổng hợp → bắt buộc gọi tool `submit_weekly_review` → lưu vào `weekly_review`. Dùng model chat. Chi tiết schema & prompt ở [08-weekly-review.md](08-weekly-review.md#5-đánh-giá-của-ai).

### 3.4 Câu hỏi làm rõ
Nếu payload có `clarifying_question` (khi độ tin cậy thấp), Thẻ xác nhận hiện câu hỏi kèm ô trả lời. Gửi câu trả lời = một lượt chat mới có kèm kết quả trước đó → AI trả về payload cập nhật. Người dùng vẫn có thể bỏ qua câu hỏi và lưu luôn.

**Nhờ AI sửa bằng lời** (thêm 17/11/2026, 09 §8 G6-18): khi AI không hỏi, Thẻ xác nhận vẫn có dòng "Kết quả chưa đúng? Nhờ AI sửa". Gửi lời sửa = một lượt CHAT (model chat) kèm các món AI vừa trả, có khẩu phần và kcal, **không gửi lại ảnh**; AI trả payload mới thay các món AI cũ, món người dùng tự thêm giữ nguyên. Lỗi thì giữ kết quả cũ.

## 4. Ngữ cảnh gửi kèm mỗi lần gọi

Được chèn vào **cuối** system prompt (phần động, sau phần tĩnh để tận dụng prompt caching):

```
<user_context>
Ngày: 2026-11-13 (Thứ Sáu), giờ hiện tại: 12:35
Hồ sơ: Nam, 30 tuổi, 170 cm, 74.2 kg, mục tiêu giảm còn 68 kg
Mục tiêu ngày: 1740 kcal | Đạm 122 g | Tinh bột 196 g | Béo 52 g
Đã ăn hôm nay: 820 kcal | Đạm 45 g | Tinh bột 98 g | Béo 30 g
Còn lại: 920 kcal
Các bữa đã ghi: Sáng: 07:10 Bánh mì trứng (380 kcal), 07:15 Cà phê sữa đá (140 kcal); Phụ: ...
Thời điểm của lần ghi này: 12:35 → bữa Trưa (tự gán theo giờ)
Ghi chú cá nhân: <nội dung người dùng nhập trong Cài đặt>
Món hay ăn: Cơm nhà (1 bát 180 g); Cà phê sữa đá ít đường (110 kcal); ...
</user_context>
```

"Món hay ăn" = tối đa 20 món trong Món của tôi có `useCount` cao nhất.

## 5. Tool schema: `record_food_items`

```json
{
  "name": "record_food_items",
  "description": "Ghi nhận danh sách món ăn/đồ uống người dùng đã ăn kèm ước lượng dinh dưỡng. Gọi khi người dùng mô tả hoặc chụp ảnh thứ họ ăn/uống.",
  "input_schema": {
    "type": "object",
    "properties": {
      "items": {
        "type": "array",
        "items": {
          "type": "object",
          "properties": {
            "name":         { "type": "string", "description": "Tên món tiếng Việt, ngắn gọn" },
            "portion_desc": { "type": "string", "description": "Mô tả khẩu phần dễ hiểu, vd '1 tô vừa', '2 lát'" },
            "grams":        { "type": "number", "description": "Khối lượng ăn được ước tính (g), với đồ uống tính ml" },
            "kcal":         { "type": "number" },
            "protein_g":    { "type": "number" },
            "carb_g":       { "type": "number" },
            "fat_g":        { "type": "number" },
            "confidence":   { "type": "number", "minimum": 0, "maximum": 1 },
            "assumptions":  { "type": "string", "description": "Giả định đã dùng, vd 'xào với ~1 thìa dầu'" }
          },
          "required": ["name", "portion_desc", "grams", "kcal", "protein_g", "carb_g", "fat_g", "confidence"]
        }
      },
      "meal_type": { "type": "string", "enum": ["BREAKFAST", "LUNCH", "DINNER", "SNACK"], "description": "Chỉ điền nếu người dùng nói rõ bữa" },
      "eaten_time": { "type": "string", "description": "Giờ ăn dạng HH:mm, chỉ điền nếu người dùng nói rõ giờ hoặc buổi (vd '7h sáng' → 07:00). Không đoán." },
      "eaten_date": { "type": "string", "description": "Ngày ăn yyyy-MM-dd, chỉ điền nếu người dùng nói ngày khác hôm nay (vd 'hôm qua')." },
      "clarifying_question": { "type": "string", "description": "Một câu hỏi ngắn nếu thông tin quan trọng còn mơ hồ" },
      "comment": { "type": "string", "description": "Một câu nhận xét ngắn (tùy chọn), vd so với calo còn lại" }
    },
    "required": ["items"]
  }
}
```

### Validate phía app sau khi nhận

| Kiểm tra | Xử lý |
|---|---|
| JSON sai schema / thiếu trường bắt buộc | Gọi lại 1 lần; vẫn lỗi → báo lỗi, cho nhập tay |
| `items` rỗng | Hiện "Không nhận ra món ăn nào" + câu hỏi/nhận xét của AI nếu có |
| Số âm hoặc NaN | Đặt về 0, đánh dấu cảnh báo trên món |
| `kcal` lệch > 20% so với `4P + 4C + 9F` | Giữ `kcal` của AI, hiện biểu tượng ⚠ nhỏ (không chặn) |
| Một món > 2500 kcal | Hiện cảnh báo "Số liệu có vẻ cao, kiểm tra lại" |
| Làm tròn | kcal → bội số 5; gram & macro → 1 chữ số thập phân |

## 6. System prompt mặc định

Lưu trong `assets/prompts/system_prompt_vi.md`, người dùng có thể sửa trong Cài đặt (lưu bản sửa trong DataStore, có nút "Khôi phục mặc định"). Có số phiên bản để app biết khi bản mặc định được cập nhật.

```markdown
Bạn là trợ lý dinh dưỡng trong app Cali, giúp một người Việt Nam theo dõi lượng calo nạp vào.
Bạn có hai nhiệm vụ:
1. GHI MÓN: khi người dùng mô tả hoặc chụp ảnh thứ họ đã/đang ăn uống, hãy gọi tool
   `record_food_items` với ước lượng dinh dưỡng cho từng món.
2. TƯ VẤN: khi người dùng hỏi (nên ăn gì, đã đủ chất chưa, món này bao nhiêu calo
   nhưng chưa ăn...), trả lời ngắn gọn bằng tiếng Việt, dựa vào <user_context>.
   Không gọi tool trong trường hợp này.

## Quy tắc ước lượng
- Tách món ghép thành các thành phần có ý nghĩa khi giúp người dùng chỉnh sửa
  (vd "cơm tấm sườn trứng" → cơm tấm, sườn nướng, trứng ốp la, mỡ hành + nước mắm).
  Món một tô/một suất quen thuộc (phở, bún bò, bánh mì) thì giữ nguyên là một món.
- Luôn tính dầu mỡ dùng khi chiên, xào, rán, và nước sốt, nước chấm, nước béo.
  Ghi giả định vào `assumptions`.
- Với ảnh: ước lượng khẩu phần dựa trên vật tham chiếu (bát, đĩa, đũa, thìa, tay, hộp).
  Chỉ ghi phần thực tế được ăn; nếu người dùng nói "ăn một nửa" thì chia tương ứng.
- Đồ uống: hỏi/giả định mức đường. Nếu không rõ, dùng mức đường phổ biến ở quán Việt Nam.
- Ưu tiên thông tin trong "Ghi chú cá nhân" và "Món hay ăn" khi khớp với món người dùng ăn.
- `confidence`: 0.8–1 khi món và khẩu phần rõ ràng; 0.5–0.8 khi phải đoán khẩu phần;
  < 0.5 khi không chắc món gì. Khi có món < 0.5, đặt một `clarifying_question` ngắn,
  nhưng VẪN trả về ước lượng tốt nhất.
- Không bịa: nếu ảnh không có đồ ăn/uống, trả về `items` rỗng và giải thích trong `comment`.
- Tên món tiếng Việt, ngắn gọn. Số liệu là giá trị cho phần ăn thực tế, không phải trên 100 g.

## Khẩu phần tham khảo phổ biến ở Việt Nam
| Món | Khẩu phần | Ước tính |
|---|---|---|
| Cơm trắng | 1 bát vừa ~150 g | ~195 kcal |
| Phở bò | 1 tô vừa | 430–500 kcal |
| Bún bò Huế | 1 tô vừa | 500–600 kcal |
| Bún chả | 1 suất | 550–650 kcal |
| Cơm tấm sườn | 1 đĩa | 600–700 kcal |
| Bánh mì thịt | 1 ổ | 400–480 kcal |
| Xôi mặn | 1 gói | 450–550 kcal |
| Bánh cuốn | 1 đĩa | 350–400 kcal |
| Trứng gà | 1 quả luộc | ~75 kcal |
| Dầu ăn | 1 thìa canh ~13 g | ~120 kcal |
| Canh rau | 1 bát | 30–50 kcal |
| Cà phê sữa đá | 1 ly | 120–180 kcal |
| Trà sữa trân châu | 1 ly size M, 100% đường | 350–450 kcal |

## Phong cách trả lời tư vấn
- Ngắn gọn (tối đa ~120 từ), thân thiện, xưng "mình" – "bạn".
- Dựa vào calo/macro còn lại để gợi ý cụ thể, có con số.
- Không đưa lời khuyên y khoa; nếu người dùng nhắc bệnh lý, khuyên hỏi bác sĩ.
```

## 7. Xử lý ảnh trước khi gửi

- Thu nhỏ để cạnh dài tối đa **1024 px**, JPEG chất lượng **80**.
- Đọc `DateTimeOriginal` trong EXIF (dùng làm giờ ăn khi chọn ảnh từ thư viện, xem 05 §4.1), xoay đúng chiều theo EXIF, rồi **xóa toàn bộ EXIF** (vị trí GPS...).
- Không lưu ảnh gốc. Tùy chọn lưu thumbnail 256 px trong bộ nhớ riêng của app để hiển thị trong nhật ký.

## 8. Lỗi & retry

| Tình huống | Hành vi |
|---|---|
| Không có mạng | Báo ngay "Không có kết nối", gợi ý thêm từ Món của tôi / nhập tay. Giữ lại ảnh + ghi chú để gửi lại. |
| Chưa có API key | Mở thẳng màn Cài đặt AI. |
| 401 / 403 | "API key không hợp lệ", mở Cài đặt AI. |
| 429 / quá tải (5xx, 529) | Tự retry tối đa 2 lần, backoff 2 s → 5 s; vẫn lỗi thì cho nút "Thử lại". |
| Timeout | Kết nối 15 s, đọc 60 s. |
| Người dùng hủy | Hủy coroutine, không lưu gì. |
| JSON không hợp lệ | Retry 1 lần (xem mục 5). |

## 9. Chi phí & giới hạn

- Đặt **hạn mức chi tiêu tháng** trên trang quản lý của hãng AI (việc làm thủ công, ngoài app).
- Phần tĩnh của system prompt đặt **đầu** system message để Neuralwatt tự dùng prefix cache (không có `cache_control` như API Anthropic).
- Lịch sử chat gửi kèm tối đa 10 lượt gần nhất của ngày hiện tại; ảnh trong các lượt cũ được thay bằng mô tả chữ "[ảnh: <tên các món đã ghi>]" để không gửi lại ảnh.
- Mỗi lần gọi ghi một bản ghi `AiCallLog` (model, token vào/ra, thời gian, thành công/lỗi) để xem thống kê sử dụng (F16).
- Món đã lưu vào Món của tôi thì thêm lại không tốn lượt gọi AI.

## 10. Quyền riêng tư

- Dữ liệu gửi đi: ảnh đã nén, chữ người dùng nhập, ngữ cảnh ở mục 4. Không gửi tên, email hay định danh nào.
- Không log nội dung request/response trong bản release.
