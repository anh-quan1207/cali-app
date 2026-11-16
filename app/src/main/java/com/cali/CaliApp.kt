package com.cali

import android.app.Application
import com.cali.core.notify.WaterNotifier
import com.cali.core.notify.WaterReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CaliApp : Application() {
    // GĐ5: kênh "Nhắc uống nước" + đặt lại lịch mỗi lần app mở (phòng alarm bị hệ thống xoá).
    @Inject lateinit var waterNotifier: WaterNotifier
    @Inject lateinit var waterScheduler: WaterReminderScheduler

    override fun onCreate() {
        super.onCreate()
        waterNotifier.ensureChannel()
        // Lỗi đặt lịch lúc mở app không được làm sập app (và không rò sang test Robolectric).
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { runCatching { waterScheduler.reschedule() } }
    }
}
