package com.cali.feature.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.notify.ReminderStateStore
import com.cali.core.notify.WaterReminderActions
import com.cali.core.notify.WaterReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Chỉ bản debug: thử nhắc nước nhanh (09 §6 mục 10) mà không phải đợi 45–120 phút.
 * - `adb shell am broadcast -n com.cali/.feature.debug.DebugWaterReceiver -a com.cali.debug.WATER_INTERVAL --ei min 2`
 *   đặt chu kỳ tạm (ngoài 4 lựa chọn của S43) rồi đặt lại lịch; đặt lại 90 khi thử xong.
 * - `-a com.cali.debug.WATER_STATE`: ghi ra logcat (tag CaliWater) lần nhắc đã hẹn.
 */
@AndroidEntryPoint
class DebugWaterReceiver : BroadcastReceiver() {
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var scheduler: WaterReminderScheduler
    @Inject lateinit var actions: WaterReminderActions
    @Inject lateinit var state: ReminderStateStore

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    "com.cali.debug.WATER_INTERVAL" -> {
                        val min = intent.getIntExtra("min", 90)
                        settings.update { it.copy(waterIntervalMin = min) }
                        state.lastReminderAt = null
                        scheduler.reschedule()
                    }
                    "com.cali.debug.WATER_FIRE" -> actions.onAlarm()
                }
                Log.i(TAG, "interval=${settings.current().waterIntervalMin} scheduledAt=${state.scheduledAt} lastReminder=${state.lastReminderAt} snooze=${state.snoozeUntil}")
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "CaliWater"
    }
}
