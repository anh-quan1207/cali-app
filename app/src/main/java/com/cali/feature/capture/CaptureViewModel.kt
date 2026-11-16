package com.cali.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.core.ai.AiDraft
import com.cali.core.ai.AiDraftHolder
import com.cali.core.ai.AiErrorType
import com.cali.core.ai.AiLogService
import com.cali.core.ai.AiOutcome
import com.cali.core.ai.CompressedImage
import com.cali.core.ai.ImageCompressor
import com.cali.core.ai.ResolvedTiming
import com.cali.core.calc.MealTypeResolver
import com.cali.core.data.db.entity.FoodSource
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.ui.component.MealTime
import com.cali.core.ui.component.TimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CaptureStage { CAMERA, REVIEW, ANALYZING, ERROR }

data class CaptureUi(
    val stage: CaptureStage = CaptureStage.CAMERA,
    val mealTime: MealTime,
    val note: String = "",
    val image: CompressedImage? = null,
    val startedAtMs: Long = 0,
    val error: AiErrorType? = null,
    val badImage: Boolean = false,
    val today: LocalDate,
)

/** Kết quả điều hướng một lần của S11. */
sealed interface CaptureNav {
    data object ToConfirm : CaptureNav
    data object ToAiSettings : CaptureNav
}

/**
 * S11 (05 S11, 03 §3.1, §7, §8): chụp / chọn ảnh → xem lại + ghi chú + giờ·bữa → Phân tích → S12.
 * Lỗi giữ nguyên ảnh + ghi chú để Thử lại. Huỷ = huỷ coroutine (OkHttp huỷ theo), không lưu gì.
 */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val compressor: ImageCompressor,
    private val ai: AiLogService,
    private val drafts: AiDraftHolder,
    private val clock: Clock,
    settings: SettingsRepository,
) : ViewModel() {

    var resolver = MealTypeResolver()
        private set

    private val _ui = MutableStateFlow(CaptureUi(mealTime = nowTime(), today = LocalDate.now(clock)))
    val ui: StateFlow<CaptureUi> = _ui.asStateFlow()
    val nav = MutableStateFlow<CaptureNav?>(null)
    private var job: Job? = null

    init {
        viewModelScope.launch {
            settings.settings.collect { s ->
                resolver = MealTypeResolver(s.mealTimes)
                ai.resolver = resolver
                if (_ui.value.mealTime.source == TimeSource.NOW) _ui.update { it.copy(mealTime = nowTime()) }
            }
        }
    }

    private fun nowTime(): MealTime {
        val now = LocalDateTime.now(clock)
        val t = now.toLocalTime().withSecond(0).withNano(0)
        return MealTime(now.toLocalDate(), t, resolver.fromTime(t), false, TimeSource.NOW)
    }

    fun setMealTime(m: MealTime) = _ui.update { it.copy(mealTime = m) }
    fun setNote(n: String) = _ui.update { it.copy(note = n.take(NOTE_MAX)) }
    fun consumeNav() { nav.value = null }

    /** Ảnh từ camera: giờ ăn mặc định = lúc bấm chụp (05 §4.1), trừ khi người dùng đã chọn giờ. */
    fun onCaptured(bytes: ByteArray) = load(bytes, fromGallery = false)

    /** Ảnh thư viện: giờ ăn mặc định = EXIF DateTimeOriginal (đọc trước khi xoá EXIF), không có thì lúc chọn. */
    fun onPicked(bytes: ByteArray) = load(bytes, fromGallery = true)

    private fun load(bytes: ByteArray, fromGallery: Boolean) = viewModelScope.launch {
        val img = try {
            compressor.compress(bytes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _ui.update { it.copy(badImage = true) }
            return@launch
        }
        _ui.update { s ->
            val mt = when {
                s.mealTime.source == TimeSource.USER -> s.mealTime
                fromGallery && img.takenAt != null -> {
                    val t = img.takenAt.withSecond(0).withNano(0)
                    MealTime(t.toLocalDate(), t.toLocalTime(), resolver.fromTime(t.toLocalTime()), false, TimeSource.EXIF)
                }
                else -> nowTime().copy(source = TimeSource.CAPTURE)
            }
            s.copy(stage = CaptureStage.REVIEW, image = img, mealTime = mt, badImage = false)
        }
    }

    fun dismissBadImage() = _ui.update { it.copy(badImage = false) }

    fun retake() {
        job?.cancel()
        _ui.update { it.copy(stage = CaptureStage.CAMERA, image = null, error = null, mealTime = if (it.mealTime.source == TimeSource.USER) it.mealTime else nowTime()) }
    }

    fun analyze() {
        val s = _ui.value
        val img = s.image ?: return
        job?.cancel()
        _ui.update { it.copy(stage = CaptureStage.ANALYZING, startedAtMs = clock.millis(), error = null) }
        job = viewModelScope.launch {
            val mt = _ui.value.mealTime
            val at = LocalDateTime.of(mt.date, mt.time)
            when (val r = ai.analyzePhoto(img.jpeg, s.note.trim().ifEmpty { null }, at)) {
                is AiOutcome.FoodLog -> {
                    // Người dùng tự chọn bữa trên chip (ưu tiên 1, 05 §4.3) → giữ nguyên giờ và bữa đó.
                    val manual = mt.mealManual
                    val timing = if (manual) ResolvedTiming(at, mt.meal) else r.timing
                    drafts.put(
                        AiDraft(
                            log = r.log, timing = timing, mealManual = manual, source = FoodSource.AI_PHOTO, callId = r.callId,
                            thumbnailJpeg = img.thumbnailJpeg, hadImage = true, defaultAt = at,
                        ),
                    )
                    nav.value = CaptureNav.ToConfirm
                    // Quay lại từ S12 thì về màn xem lại.
                    _ui.update { it.copy(stage = CaptureStage.REVIEW) }
                }
                is AiOutcome.Text -> _ui.update { it.copy(stage = CaptureStage.ERROR, error = AiErrorType.INVALID_RESPONSE) }
                is AiOutcome.Failed -> {
                    _ui.update { it.copy(stage = CaptureStage.ERROR, error = r.type) }
                    if (r.type == AiErrorType.NO_KEY) nav.value = CaptureNav.ToAiSettings
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        _ui.update { it.copy(stage = CaptureStage.REVIEW) }
    }

    fun elapsedSeconds(): Long = (clock.millis() - _ui.value.startedAtMs) / 1000

    companion object {
        const val NOTE_MAX = 200
    }
}
