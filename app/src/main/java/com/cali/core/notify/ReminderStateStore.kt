package com.cali.core.notify

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trạng thái chạy của lịch nhắc nước: lần nhắc gần nhất và "Để sau" tới lúc nào.
 * Không phải cài đặt người dùng → không nằm trong DataStore cài đặt, không vào sao lưu (09 §8 G5-2).
 * Dùng SharedPreferences vì receiver đọc/ghi đồng bộ, rất ít khoá.
 */
@Singleton
class ReminderStateStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var lastReminderAt: Instant?
        get() = prefs.getLong(K_LAST, 0L).takeIf { it > 0 }?.let(Instant::ofEpochMilli)
        set(v) = prefs.edit { if (v == null) remove(K_LAST) else putLong(K_LAST, v.toEpochMilli()) }

    var snoozeUntil: Instant?
        get() = prefs.getLong(K_SNOOZE, 0L).takeIf { it > 0 }?.let(Instant::ofEpochMilli)
        set(v) = prefs.edit { if (v == null) remove(K_SNOOZE) else putLong(K_SNOOZE, v.toEpochMilli()) }

    /** Lần nhắc đã hẹn gần nhất (cho S15 / S43 "Nhắc tiếp lúc …"). */
    var scheduledAt: Instant?
        get() = prefs.getLong(K_NEXT, 0L).takeIf { it > 0 }?.let(Instant::ofEpochMilli)
        set(v) = prefs.edit { if (v == null) remove(K_NEXT) else putLong(K_NEXT, v.toEpochMilli()) }

    fun clear() = prefs.edit { clear() }

    private companion object {
        const val FILE = "water_reminder_state"
        const val K_LAST = "last_reminder_at"
        const val K_SNOOZE = "snooze_until"
        const val K_NEXT = "scheduled_at"
    }
}
