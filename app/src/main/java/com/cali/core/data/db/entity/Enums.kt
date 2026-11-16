package com.cali.core.data.db.entity

/** Room lưu enum theo tên (String), khớp cột kiểu String ở 04 §2. */
enum class FoodSource { AI_PHOTO, AI_CHAT, MY_FOOD, MANUAL }

enum class WaterSource { APP, NOTIFICATION, WIDGET }

enum class ChatRole { USER, ASSISTANT }

enum class FoodLogStatus { PENDING, SAVED, DISCARDED }

/** 04 §2 chỉ ghi PHOTO_LOG / CHAT; thêm WEEKLY_REVIEW theo 03 §3.3 (ghi ở 09 §8). */
enum class AiMode { PHOTO_LOG, CHAT, WEEKLY_REVIEW }
