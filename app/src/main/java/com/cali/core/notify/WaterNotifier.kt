package com.cali.core.notify

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cali.MainActivity
import com.cali.R
import com.cali.core.ui.format.VnFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thông báo nhắc uống nước (05 §3): kênh riêng, nút "+{nút nhỏ} ml" và "Để sau", chạm mở S10. */
@Singleton
class WaterNotifier @Inject constructor(@ApplicationContext private val context: Context) {

    private val nm = NotificationManagerCompat.from(context)

    fun ensureChannel() {
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.water_channel_name))
                .setDescription(context.getString(R.string.water_channel_desc))
                .build(),
        )
    }

    @SuppressLint("MissingPermission") // đã kiểm bằng NotificationPermission
    fun showReminder(totalMl: Int, goalMl: Int, cupMl: Int) {
        if (!NotificationPermission.canPostWater(context)) return
        ensureChannel()
        val n = base()
            .setContentTitle(context.getString(R.string.water_notif_title))
            .setContentText(context.getString(R.string.water_notif_text, VnFormat.int(totalMl), VnFormat.int(goalMl)))
            .addAction(0, context.getString(R.string.water_notif_add, VnFormat.int(cupMl)), action(WaterActionReceiver.ACTION_ADD, 1))
            .addAction(0, context.getString(R.string.water_notif_snooze), action(WaterActionReceiver.ACTION_SNOOZE, 2))
            .build()
        nm.notify(NOTIFICATION_ID, n)
    }

    /** Sau khi bấm +ml trên thông báo: thay bằng dòng xác nhận, tự tắt sau vài giây. */
    @SuppressLint("MissingPermission")
    fun showLogged(addedMl: Int, totalMl: Int, goalMl: Int) {
        if (!NotificationPermission.canPostWater(context)) return
        val n = base()
            .setContentTitle(context.getString(R.string.water_notif_logged, VnFormat.int(addedMl), VnFormat.int(totalMl), VnFormat.int(goalMl)))
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setTimeoutAfter(LOGGED_TIMEOUT_MS)
            .build()
        nm.notify(NOTIFICATION_ID, n)
    }

    fun cancel() = nm.cancel(NOTIFICATION_ID)

    private fun base() = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_water)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )

    private fun action(action: String, code: Int) = PendingIntent.getBroadcast(
        context, code,
        Intent(context, WaterActionReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val CHANNEL_ID = "water_reminder"
        const val NOTIFICATION_ID = 1001
        private const val LOGGED_TIMEOUT_MS = 4_000L
    }
}
