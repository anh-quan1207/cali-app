# 08 — Tổng kết & đánh giá tuần

Tính năng F17. Mỗi tuần (Thứ Hai → Chủ Nhật) app tổng hợp: ăn vượt / thiếu bao nhiêu calo, nước uống, macro, cân nặng; chấm điểm tuần và để AI viết nhận xét + gợi ý cho tuần sau.

## 1. Khi nào xem được

- **Tuần đang diễn ra:** xem bất cứ lúc nào, nhãn "Tạm tính (còn N ngày)". Chỉ tính các ngày đã qua + hôm nay.
- **Tuần đã kết thúc:** tổng kết chính thức.
- **Thông báo sáng Thứ Hai** (mặc định 08:00, tắt được): "Tổng kết tuần 10/11 – 16/11 đã sẵn sàng · Điểm 71/100". Chạm → mở S35.
- Thứ Hai & Thứ Ba: màn Hôm nay hiện thẻ "Xem tổng kết tuần trước".

## 2. Định nghĩa

| Khái niệm | Định nghĩa |
|---|---|
| Mục tiêu ngày | `target_kcal`, macro, nước theo `goal_snapshot` áp dụng cho ngày đó (02 §11) |
| Ngày đã ghi | Có ≥ 1 `food_entry` |
| Ngày nghi ghi thiếu | Đã ghi nhưng kcal < 50% mục tiêu → tính vào "đã ghi" nhưng được đánh dấu ⚠ |
| Chênh lệch ngày | `Δ = kcal ăn − target_kcal` |
| Ngày **Đạt** | \|Δ\| ≤ 10% target |
| Ngày **Vượt** | Δ > +10% target |
| Ngày **Thiếu** | Δ < −10% target |
| Ngày đạt nước | Nước uống ≥ mục tiêu nước |
| Ngày đạt đạm | Đạm ≥ 90% mục tiêu đạm |

Ngày chưa ghi **không** được tính vào tổng / trung bình calo, nhưng được nêu rõ trong báo cáo ("2 ngày chưa ghi").

## 3. Chỉ số tuần

### 3.1 Calo
- Tổng mục tiêu (các ngày đã ghi), tổng đã ăn, **tổng chênh lệch** `ΣΔ`.
  - Hiển thị: "Tuần này bạn ăn **vượt 1 250 kcal** so với mục tiêu" / "**thiếu 800 kcal**".
- Trung bình kcal/ngày so với mục tiêu.
- Số ngày Đạt / Vượt / Thiếu / Chưa ghi.
- Ngày ăn nhiều nhất & ít nhất.
- **Cân bằng năng lượng so với TDEE:** `Σ(kcal ăn − TDEE ngày)` → **thay đổi cân dự kiến** = tổng / 7700 kg.
  - Ví dụ: "Theo lượng ăn, bạn đã thâm hụt ~3 450 kcal ≈ −0.45 kg".

### 3.2 Macro
- Trung bình đạm / tinh bột / béo (g/ngày) và % so với mục tiêu.
- Số ngày đạt đạm.
- Tỉ lệ năng lượng thực tế P/C/F (%) so với tỉ lệ mục tiêu.

### 3.3 Nước
- Tổng đã uống / tổng mục tiêu tuần (ví dụ 15.2 / 18.2 L), trung bình ml/ngày.
- Số ngày đạt nước.
- Thiếu tổng cộng bao nhiêu lít.

### 3.4 Cân nặng
- **Cân trung bình tuần** = trung bình các bản ghi trong tuần (giảm nhiễu dao động hằng ngày).
- Thay đổi so với cân trung bình tuần trước.
- So sánh **thực tế vs dự kiến** (3.1): "Dự kiến −0.45 kg, thực tế −0.5 kg". Không có bản ghi cân → nhắc "Hãy cân ít nhất 2 lần/tuần".
- Tiến độ tới cân mục tiêu: còn X kg, ngày dự kiến cập nhật.

### 3.5 Món ăn
- Top 5 món đóng góp nhiều calo nhất trong tuần (gộp theo tên).
- Bữa có calo trung bình cao nhất.

### 3.6 So với tuần trước
Mũi tên ↑ ↓ cho: kcal trung bình, ngày đạt, nước trung bình, đạm trung bình, điểm tuần.

## 4. Điểm tuần (0–100)

Điểm tính trên máy, theo luật cố định (không phụ thuộc AI):

| Thành phần | Tối đa | Công thức |
|---|---|---|
| Calo | 40 | 40 × (số ngày Đạt / 7) |
| Đạm | 20 | 20 × min(1, đạm TB / mục tiêu đạm) |
| Nước | 20 | 20 × (số ngày đạt nước / 7) |
| Ghi chép đều | 20 | 20 × (số ngày đã ghi và không nghi ghi thiếu / 7) |

- Tuần đang diễn ra: thay 7 bằng số ngày đã qua (kể cả hôm nay).
- Làm tròn số nguyên.

| Điểm | Xếp loại |
|---|---|
| 85–100 | Xuất sắc |
| 70–84 | Tốt |
| 50–69 | Cần cố gắng |
| < 50 | Chưa tốt |

## 5. Đánh giá của AI

### 5.1 Cách hoạt động
- Mode mới `WEEKLY_REVIEW`, dùng **model chat** (văn bản, rẻ).
- Chỉ gửi **số liệu đã tổng hợp** (mục 3–4) + danh sách món theo ngày dạng rút gọn (tên, kcal). Không gửi ảnh.
- Bắt buộc gọi tool `submit_weekly_review` (đầu ra có cấu trúc).
- **Không chạy nền tự động** để tránh tốn tiền khi không xem: tạo khi người dùng mở tổng kết của tuần đã kết thúc (hoặc bấm "Tạo nhận xét" với tuần đang diễn ra). Cài đặt "Tự tạo nhận xét khi mở" mặc định bật.
- Kết quả được **lưu lại** (bảng `weekly_review`). Nếu dữ liệu tuần thay đổi sau đó (sửa món cũ) → hiện "Dữ liệu đã thay đổi · Tạo lại nhận xét".

### 5.2 Tool schema
```json
{
  "name": "submit_weekly_review",
  "input_schema": {
    "type": "object",
    "properties": {
      "headline":    { "type": "string", "description": "1 câu tóm tắt tuần, tối đa 20 từ" },
      "highlights":  { "type": "array", "items": { "type": "string" }, "description": "1–3 điểm làm tốt, có số liệu" },
      "issues":      { "type": "array", "items": { "type": "string" }, "description": "1–3 vấn đề chính, có số liệu" },
      "patterns":    { "type": "array", "items": { "type": "string" }, "description": "0–2 quy luật phát hiện, vd 'cuối tuần ăn vượt', 'bữa tối chiếm 50% calo'" },
      "suggestions": { "type": "array", "items": { "type": "string" }, "description": "đúng 3 gợi ý cụ thể, làm được ngay tuần sau" },
      "tone":        { "type": "string", "enum": ["CELEBRATE", "ENCOURAGE", "WARN"] }
    },
    "required": ["headline", "highlights", "issues", "suggestions", "tone"]
  }
}
```

### 5.3 Prompt bổ sung cho mode này
Thêm vào sau system prompt chính (03 §6):

```markdown
## Đánh giá tuần
Bạn nhận được <weekly_stats> của một tuần. Hãy gọi `submit_weekly_review`.
- Dựa hoàn toàn vào số liệu được cung cấp, luôn dẫn con số cụ thể.
- Đánh giá theo mục tiêu của người dùng (giảm / giữ / tăng cân): khi giảm cân, ăn thiếu
  nhẹ là tốt, nhưng thiếu > 25% hoặc dưới mức sàn là vấn đề.
- So sánh thay đổi cân dự kiến với thực tế; nếu lệch nhiều, nêu khả năng ghi chép
  chưa đủ hoặc dao động nước, không kết luận vội.
- Ngày chưa ghi / nghi ghi thiếu: nhắc nhẹ, không trách móc.
- Gợi ý phải cụ thể, khả thi, gắn với món người dùng thực sự ăn
  (vd "đổi trà sữa 100% đường sang 50% giúp bớt ~100 kcal/ly").
- Giọng thân thiện, xưng "mình" – "bạn", không phán xét. Không đưa lời khuyên y khoa.
```

### 5.4 Dữ liệu gửi (`<weekly_stats>`)
```
Tuần: 10/11/2026 – 16/11/2026 (đã kết thúc)
Mục tiêu: giảm cân, 0.5 kg/tuần, mục tiêu ngày 1740 kcal, đạm 122 g, nước 2600 ml
Điểm tuần: 71/100 (Calo 23/40, Đạm 17/20, Nước 14/20, Ghi chép 17/20)
Calo: đã ghi 7/7 ngày (1 ngày nghi ghi thiếu: T4 690 kcal)
  Đạt 4, Vượt 2 (T7 +620, CN +890), Thiếu 1
  Tổng chênh lệch so với mục tiêu: +420 kcal; so với TDEE: −3 450 kcal (≈ −0.45 kg dự kiến)
Macro TB: đạm 104 g (85%), tinh bột 231 g (118%), béo 55 g (106%)
Nước: 15.2/18.2 L, đạt 5/7 ngày
Cân: TB tuần 74.1 kg (tuần trước 74.6 → −0.5 kg)
Top món theo calo: Trà sữa trân châu ×3 (1 200 kcal), Cơm tấm sườn ×2 (1 300 kcal), ...
Theo ngày: T2 1 710 | T3 1 760 | T4 690⚠ | T5 1 800 | T6 1 650 | T7 2 360 | CN 2 630
So với tuần trước: kcal TB +5%, ngày đạt 4 (trước 3), nước TB −200 ml
```

## 6. Màn hình S35 — Tổng kết tuần

Từ trên xuống:
1. **Chọn tuần** ‹ 10/11 – 16/11 › (mặc định tuần trước nếu hôm nay là T2–T3, ngược lại tuần hiện tại). Nhãn "Tạm tính" nếu tuần đang diễn ra.
2. **Thẻ điểm:** số điểm lớn + xếp loại + 4 thanh thành phần; mũi tên so với tuần trước.
3. **Thẻ calo:**
   - Câu chính: "Vượt 420 kcal so với mục tiêu" (màu theo trạng thái).
   - Biểu đồ cột 7 ngày, vạch ngang mục tiêu; cột tô xanh / cam / đỏ / xám (chưa ghi), ⚠ trên ngày nghi ghi thiếu.
   - Dòng: Đạt 4 · Vượt 2 · Thiếu 1 · Chưa ghi 0.
   - Dòng: "Thâm hụt so với TDEE ≈ 3 450 kcal → dự kiến −0.45 kg".
4. **Thẻ nước:** biểu đồ cột ml/ngày + vạch mục tiêu; "15.2 / 18.2 L · đạt 5/7 ngày · thiếu 3 L".
5. **Thẻ macro:** 3 thanh TB/ngày so với mục tiêu + "đạt đạm 3/7 ngày".
6. **Thẻ cân nặng:** TB tuần, thay đổi, dự kiến vs thực tế, mini chart 4 tuần gần nhất.
7. **Thẻ món ăn:** top 5 món nhiều calo.
8. **Thẻ "Nhận xét của AI":** headline, ✅ điểm tốt, ⚠ cần cải thiện, 🔍 quy luật, 🎯 3 gợi ý tuần sau. Trạng thái: đang tạo / lỗi + thử lại / dữ liệu đã thay đổi + tạo lại / chưa có API key → ẩn thẻ, các phần khác vẫn hiển thị.
9. Nút **Chia sẻ** (xuất ảnh tóm tắt) — giai đoạn 2.

Chạm một cột ngày → mở chi tiết ngày (S33).

Lối vào: S30 Lịch sử (nút "Tổng kết tuần" + chạm số tuần trên lịch), thẻ trên S10 (T2–T3), thông báo sáng Thứ Hai, S32 Thống kê.

## 7. Tổng kết tháng (giai đoạn 2)

Dùng lại cùng logic cho tháng: chỉ số mục 3 tính trên cả tháng, biểu đồ theo tuần, điểm tháng = trung bình điểm các tuần, AI nhận xét xu hướng 4 tuần.
