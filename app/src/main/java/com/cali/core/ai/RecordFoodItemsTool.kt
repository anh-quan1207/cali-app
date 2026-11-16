package com.cali.core.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Tool `record_food_items` (03 §5).
 * Khác bản đặc tả: thêm `additionalProperties: false` (bắt buộc khi `strict: true`) và bỏ `minimum/maximum`
 * của `confidence` (đưa vào description; FoodLogValidator tự kẹp 0–1). Ghi ở 09 §8.
 */
object RecordFoodItemsTool {
    const val NAME = "record_food_items"

    const val DESCRIPTION =
        "Ghi nhận danh sách món ăn/đồ uống người dùng đã ăn kèm ước lượng dinh dưỡng. " +
            "Gọi khi người dùng mô tả hoặc chụp ảnh thứ họ ăn/uống."

    val inputSchema: JsonObject = Json.parseToJsonElement(
        """
        {
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
                  "confidence":   { "type": "number", "description": "Độ tin cậy từ 0 đến 1" },
                  "assumptions":  { "type": "string", "description": "Giả định đã dùng, vd 'xào với ~1 thìa dầu'" }
                },
                "required": ["name", "portion_desc", "grams", "kcal", "protein_g", "carb_g", "fat_g", "confidence"],
                "additionalProperties": false
              }
            },
            "meal_type": { "type": "string", "enum": ["BREAKFAST", "LUNCH", "DINNER", "SNACK"], "description": "Chỉ điền nếu người dùng nói rõ bữa" },
            "eaten_time": { "type": "string", "description": "Giờ ăn dạng HH:mm, chỉ điền nếu người dùng nói rõ giờ hoặc buổi (vd '7h sáng' → 07:00). Không đoán." },
            "eaten_date": { "type": "string", "description": "Ngày ăn yyyy-MM-dd, chỉ điền nếu người dùng nói ngày khác hôm nay (vd 'hôm qua')." },
            "clarifying_question": { "type": "string", "description": "Một câu hỏi ngắn nếu thông tin quan trọng còn mơ hồ" },
            "comment": { "type": "string", "description": "Một câu nhận xét ngắn (tùy chọn), vd so với calo còn lại" }
          },
          "required": ["items"],
          "additionalProperties": false
        }
        """.trimIndent(),
    ).jsonObject
}
