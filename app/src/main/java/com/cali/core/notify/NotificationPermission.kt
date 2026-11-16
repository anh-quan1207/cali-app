package com.cali.core.notify

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Kiểm quyền thông báo: POST_NOTIFICATIONS (Android 13+), thông báo của app và kênh nhắc nước chưa bị tắt. */
object NotificationPermission {

    fun runtimeGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun canPostWater(context: Context): Boolean {
        val nm = NotificationManagerCompat.from(context)
        if (!runtimeGranted(context) || !nm.areNotificationsEnabled()) return false
        val ch = nm.getNotificationChannelCompat(WaterNotifier.CHANNEL_ID) ?: return true // kênh chưa tạo: sẽ tạo mặc định
        return ch.importance != NotificationManagerCompat.IMPORTANCE_NONE
    }
}
