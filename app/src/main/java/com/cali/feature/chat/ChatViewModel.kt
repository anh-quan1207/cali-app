package com.cali.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiLogService
import com.cali.core.ai.CompressedImage
import com.cali.core.ai.ImageCompressor
import com.cali.core.ai.ThumbnailStore
import com.cali.core.calc.MealTypeResolver
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ChatRepository
import com.cali.core.data.repo.ChatSendResult
import com.cali.core.data.repo.DayData
import com.cali.core.data.repo.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Chữ điền sẵn khi mở Chat từ S13 ("Hỏi AI" với chữ đang tìm). */
@Singleton
class ChatPrefill @Inject constructor() {
    val text = MutableStateFlow<String?>(null)
}

data class ChatUi(
    val date: LocalDate,
    val today: LocalDate,
    val messages: List<ChatMessageEntity>,
    val day: DayData?,
    val sending: Boolean,
    /** Lỗi của tin người dùng chưa có trả lời (theo id). */
    val errors: Map<Long, AiErrorType>,
    val input: String,
    val attachment: CompressedImage?,
    val zone: ZoneId,
)

sealed interface ChatEvent {
    data class Moved(val date: LocalDate) : ChatEvent
    data class Discarded(val id: Long) : ChatEvent
    data object OpenAiSettings : ChatEvent
}

/** S20 Chat AI: luồng theo ngày, thẻ ghi món đổi trạng thái tại chỗ (00-direction, quyết định S20). */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chat: ChatRepository,
    private val diary: DiaryRepository,
    private val ai: AiLogService,
    private val compressor: ImageCompressor,
    private val thumbs: ThumbnailStore,
    private val settings: SettingsRepository,
    private val prefill: ChatPrefill,
    private val clock: Clock,
) : ViewModel() {
    private val date = MutableStateFlow(LocalDate.now(clock))
    private val sending = MutableStateFlow(false)
    private val errors = MutableStateFlow<Map<Long, AiErrorType>>(emptyMap())
    private val input = MutableStateFlow("")
    private val attachment = MutableStateFlow<CompressedImage?>(null)
    /** Ảnh của tin chưa gửi được, giữ trong bộ nhớ để Thử lại. */
    private val pendingImages = mutableMapOf<Long, ByteArray>()
    private val events = Channel<ChatEvent>(capacity = 4)
    val eventFlow = events.receiveAsFlow()

    private val thread = date.flatMapLatest { d -> combine(chat.observe(d), diary.observeDay(d)) { m, day -> m to day } }

    val ui: StateFlow<ChatUi?> = combine(
        date, thread, combine(sending, errors) { s, e -> s to e }, input, attachment,
    ) { d, (msgs, day), (snd, err), inp, att ->
        ChatUi(d, LocalDate.now(clock), msgs, day, snd, err, inp, att, clock.zone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            settings.settings.collect { s ->
                val r = MealTypeResolver(s.mealTimes)
                ai.resolver = r
                diary.resolver = r
            }
        }
        viewModelScope.launch {
            prefill.text.collect { t ->
                if (t != null) {
                    input.value = t
                    date.value = LocalDate.now(clock)
                    prefill.text.value = null
                }
            }
        }
    }

    fun setDate(d: LocalDate) { date.value = minOf(d, LocalDate.now(clock)) }
    fun shiftDay(days: Long) = setDate(date.value.plusDays(days))
    fun setInput(t: String) { input.value = t.take(INPUT_MAX) }

    fun attach(bytes: ByteArray) = viewModelScope.launch {
        attachment.value = try {
            compressor.compress(bytes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    fun clearAttachment() { attachment.value = null }

    fun send() = viewModelScope.launch {
        val text = input.value.trim()
        val img = attachment.value
        if ((text.isEmpty() && img == null) || sending.value) return@launch
        input.value = ""
        attachment.value = null
        sending.value = true
        val thumbPath = img?.takeIf { settings.current().saveThumbnails }?.let { thumbs.save(it.thumbnailJpeg).getOrNull() }
        handle(chat.send(date.value, text.ifEmpty { null }, img?.jpeg, thumbPath), img?.jpeg)
        sending.value = false
    }

    fun retry(userId: Long) = viewModelScope.launch {
        if (sending.value) return@launch
        sending.value = true
        errors.update { it - userId }
        handle(chat.retry(userId, pendingImages[userId]), pendingImages[userId])
        sending.value = false
    }

    private suspend fun handle(r: ChatSendResult, jpeg: ByteArray?) {
        when (r) {
            is ChatSendResult.Sent -> {
                r.movedTo?.let { events.send(ChatEvent.Moved(it)) }
            }
            is ChatSendResult.Failed -> {
                errors.update { it + (r.userMessageId to r.type) }
                if (jpeg != null) pendingImages[r.userMessageId] = jpeg
                if (r.type == AiErrorType.NO_KEY) events.send(ChatEvent.OpenAiSettings)
            }
        }
    }

    fun discard(id: Long) = viewModelScope.launch {
        chat.discard(id).onSuccess { events.send(ChatEvent.Discarded(id)) }
    }

    fun undoDiscard(id: Long) = viewModelScope.launch { chat.undoDiscard(id) }

    fun localTime(millis: Long): LocalDateTime = diary.toLocal(millis)

    companion object {
        const val INPUT_MAX = 1000
    }
}
