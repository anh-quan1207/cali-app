# 02 — Công thức tính toán

Tất cả logic dưới đây nằm trong một lớp thuần Kotlin (`NutritionCalculator`), không phụ thuộc Android, có unit test cho từng công thức và ví dụ ở mục 10.

## 1. Đầu vào

| Trường | Kiểu | Ghi chú |
|---|---|---|
| `sex` | MALE / FEMALE | |
| `birthYear` | Int | Tuổi = năm hiện tại − năm sinh |
| `heightCm` | Double | 120–220 |
| `weightKg` | Double | Lấy **bản ghi cân nặng mới nhất** |
| `activityLevel` | Enum (5 mức) | Xem mục 3 |
| `goalType` | LOSE / MAINTAIN / GAIN | |
| `targetWeightKg` | Double? | Null nếu MAINTAIN |
| `weeklyRateKg` | Double | 0 nếu MAINTAIN |

## 2. BMR (Mifflin-St Jeor)

```
Nam: BMR = 10 × weightKg + 6.25 × heightCm − 5 × age + 5
Nữ:  BMR = 10 × weightKg + 6.25 × heightCm − 5 × age − 161
```

## 3. TDEE

`TDEE = BMR × hệ số vận động`

| Enum | Hiển thị | Mô tả | Hệ số |
|---|---|---|---|
| SEDENTARY | Ít vận động | Ngồi nhiều, gần như không tập | 1.2 |
| LIGHT | Vận động nhẹ | Đi lại nhiều hoặc tập 1–3 buổi/tuần | 1.375 |
| MODERATE | Vận động vừa | Tập 3–5 buổi/tuần | 1.55 |
| ACTIVE | Vận động nhiều | Tập 6–7 buổi/tuần | 1.725 |
| VERY_ACTIVE | Rất nhiều | Lao động chân tay nặng / tập 2 lần/ngày | 1.9 |

## 4. Calo mục tiêu

Hằng số: **1 kg mỡ ≈ 7700 kcal**.

```
dailyDelta = weeklyRateKg × 7700 / 7
LOSE:     rawTarget = TDEE − dailyDelta
MAINTAIN: rawTarget = TDEE
GAIN:     rawTarget = TDEE + dailyDelta
```

Tùy chọn tốc độ:

| Mục tiêu | Tốc độ (kg/tuần) | Chênh lệch (kcal/ngày) | Nhãn |
|---|---|---|---|
| Giảm | 0.25 | 275 | Nhẹ nhàng |
| Giảm | 0.5 | 550 | Khuyên dùng (mặc định) |
| Giảm | 0.75 | 825 | Nhanh |
| Giảm | 1.0 | 1100 | Rất nhanh |
| Tăng | 0.25 | 275 | Khuyên dùng (mặc định) |
| Tăng | 0.5 | 550 | Nhanh |

## 5. Giới hạn an toàn

Áp dụng theo thứ tự, ghi lại cờ `clampReason` nếu bị kẹp:

1. **Thâm hụt tối đa:** khi giảm cân, `TDEE − target ≤ 25% × TDEE`.
2. **Sàn calo:** `target ≥ max(sàn theo giới tính, BMR)`, với sàn = 1200 (nữ), 1500 (nam).
3. **Dư thừa tối đa:** khi tăng cân, `target − TDEE ≤ 500`.
4. Làm tròn `target` đến bội số của 10 gần nhất.

Nếu bị kẹp: tốc độ thực tế = `|TDEE − target| × 7 / 7700`. Dùng tốc độ này để tính ngày dự kiến và hiện cảnh báo:
> "Tốc độ bạn chọn cần ăn dưới mức an toàn. App đã điều chỉnh về X kcal/ngày (~Y kg/tuần)."

## 6. Macro

**Cân nặng tham chiếu cho đạm:** dùng cân nặng hiện tại; nếu BMI ≥ 25 thì dùng cân nặng mục tiêu (tránh đạm quá cao ở người thừa cân).

| Mục tiêu | Đạm | Béo | Tinh bột |
|---|---|---|---|
| Giảm | 1.8 g/kg | 27% tổng calo | Phần còn lại |
| Giữ | 1.4 g/kg | 28% tổng calo | Phần còn lại |
| Tăng | 1.6 g/kg | 25% tổng calo | Phần còn lại |

```
proteinKcal = proteinG × 4
fatKcal     = target × fatPct     → fatG  = fatKcal / 9
carbKcal    = target − proteinKcal − fatKcal → carbG = carbKcal / 4
```

Làm tròn gram đến số nguyên. Nếu `carbKcal < 20% target` → giảm đạm xuống 1.6 g/kg rồi tính lại. Người dùng có thể ghi đè tỉ lệ % trong Cài đặt (tổng phải = 100%).

## 7. BMI (ngưỡng cho người châu Á)

`BMI = weightKg / (heightM)²`, làm tròn 1 chữ số.

| BMI | Phân loại |
|---|---|
| < 18.5 | Thiếu cân |
| 18.5 – 22.9 | Bình thường |
| 23 – 24.9 | Thừa cân |
| 25 – 29.9 | Béo phì độ I |
| ≥ 30 | Béo phì độ II |

Cảnh báo khi người dùng chọn **cân nặng mục tiêu** có BMI < 18.5 hoặc chọn "Giảm" khi đang BMI < 18.5.

## 8. Nước

```
waterMl = weightKg × 35
+ 300 nếu activityLevel = ACTIVE
+ 500 nếu activityLevel = VERY_ACTIVE
```
Làm tròn đến bội số của 100. Kẹp trong khoảng 1500 – 4000 ml.

## 9. Ngày dự kiến đạt mục tiêu

```
weeks = |weightKg − targetWeightKg| / actualWeeklyRateKg
etaDate = today + ceil(weeks × 7) ngày
```
Hiển thị dạng "khoảng tháng 3/2027 (~14 tuần)". Ghi chú: thực tế thường chậm dần do cân nặng giảm làm TDEE giảm, nên app tính lại mỗi lần cập nhật cân.

## 10. Ví dụ kiểm chứng (dùng làm unit test)

Nam, 30 tuổi, 170 cm, 75 kg, vận động nhẹ, giảm cân 0.5 kg/tuần, mục tiêu 68 kg.

| Chỉ số | Tính | Kết quả |
|---|---|---|
| BMR | 750 + 1062.5 − 150 + 5 | **1667.5** |
| TDEE | 1667.5 × 1.375 | **2292.8** |
| Raw target | 2292.8 − 550 | 1742.8 |
| Kiểm tra thâm hụt | 550 ≤ 573.2 | OK |
| Kiểm tra sàn | 1742.8 ≥ max(1500, 1667.5) | OK |
| Calo mục tiêu | làm tròn 10 | **1740 kcal** |
| BMI | 75 / 1.7² | **26.0** → Béo phì độ I |
| Đạm | BMI ≥ 25 → 1.8 × 68 | **122 g** (488 kcal) |
| Béo | 1740 × 27% = 469.8 kcal | **52 g** |
| Tinh bột | (1740 − 488 − 469.8) / 4 | **196 g** |
| Nước | 75 × 35 = 2625 | **2600 ml** |
| Dự kiến | 7 kg / 0.5 | **14 tuần** |

## 11. Khi nào tính lại

- Sửa hồ sơ / mục tiêu trong Cài đặt → luôn tính lại.
- Ghi cân nặng mới → hỏi người dùng (hoặc tự động nếu đã bật).
- Sang năm mới (tuổi thay đổi) → tính lại lần mở app đầu tiên.
- Mỗi lần tính lại tạo một **bản ghi mục tiêu mới** có hiệu lực từ hôm nay (`GoalSnapshot`, xem 04), để lịch sử các ngày cũ vẫn so với mục tiêu tại thời điểm đó.
- Nếu đang ghi đè thủ công: chỉ tính lại các giá trị không bị ghi đè.
