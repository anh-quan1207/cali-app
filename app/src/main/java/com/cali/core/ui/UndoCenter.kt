package com.cali.core.ui

import androidx.annotation.StringRes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Một thông báo snackbar, có thể kèm Hoàn tác (05 §5: xoá dùng Hoàn tác thay cho hộp thoại). */
data class UiMessage(
    @StringRes val text: Int,
    val args: List<Any> = emptyList(),
    val undo: (suspend () -> Unit)? = null,
)

/**
 * Kênh snackbar dùng chung giữa các màn: xoá ở S14 rồi quay về S10 vẫn thấy Hoàn tác trên S10.
 * Màn đang hiện (S10, S34, S45) thu và hiển thị.
 */
@Singleton
class UndoCenter @Inject constructor() {
    // Channel: thông báo đăng khi chưa màn nào thu (S14 xoá rồi quay về S10) vẫn được giữ tới khi S10 thu.
    private val channel = Channel<UiMessage>(capacity = 8)
    val messages: Flow<UiMessage> = channel.receiveAsFlow()

    fun post(message: UiMessage) {
        channel.trySend(message)
    }
}
