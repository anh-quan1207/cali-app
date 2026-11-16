package com.cali.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.core.calc.GoalPlanner
import com.cali.core.calc.InputLimits
import com.cali.core.calc.RateOption
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MacroPct
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WeightEntryEntity
import com.cali.core.data.prefs.Settings
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.GoalPreview
import com.cali.core.data.repo.ProfileInput
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.data.repo.toInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Sheet sửa một mục của S41. */
enum class ProfileSheet { SEX, BIRTH_YEAR, HEIGHT, ACTIVITY, GOAL, RATE }

/** Thay đổi đang chờ "Áp dụng" (sheet Đang dùng / Mới). */
data class PendingApply(
    val input: ProfileInput,
    val kcalOverride: Int?,
    val macroOverride: MacroPct?,
    val preview: GoalPreview,
    /** Mô tả ngắn thay đổi, ví dụ "Tốc độ 0,5 → 0,75 kg/tuần". */
    val reason: ApplyReason,
)

sealed interface ApplyReason {
    data class Rate(val from: Double, val to: Double) : ApplyReason
    data object Profile : ApplyReason
    data class Override(val kcal: Int?, val macro: MacroPct?) : ApplyReason
}

data class ProfileData(
    val profile: UserProfileEntity,
    val weight: WeightEntryEntity,
    val goal: GoalSnapshotEntity,
    val settings: Settings,
    val today: LocalDate,
)

/** Ô nhập ghi đè (chưa áp dụng). */
data class OverrideDraft(
    val kcalOn: Boolean = false,
    val kcalText: String = "",
    val macroOn: Boolean = false,
    val p: String = "",
    val c: String = "",
    val f: String = "",
) {
    val kcal: Int? get() = kcalText.filter { it.isDigit() }.toIntOrNull()
    val kcalError: Boolean get() = kcalOn && kcalText.isNotBlank() && !InputLimits.kcalOverrideValid(kcal)
    val pct: Triple<Int?, Int?, Int?> get() = Triple(p.toIntOrNull(), c.toIntOrNull(), f.toIntOrNull())
    val macroValid: Boolean get() = InputLimits.macroPctValid(pct.first, pct.second, pct.third)
    val macroSum: Int get() = (pct.first ?: 0) + (pct.second ?: 0) + (pct.third ?: 0)
    fun macro(): MacroPct? = if (macroOn && macroValid) MacroPct(pct.first!!, pct.second!!, pct.third!!) else null
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    val data: StateFlow<ProfileData?> = combine(
        repo.profile, repo.latestWeight, repo.observeGoal(), settingsRepo.settings,
    ) { p, w, g, s ->
        if (p == null || w == null || g == null) null else ProfileData(p, w, g, s, repo.today())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sheet = MutableStateFlow<ProfileSheet?>(null)
    val pending = MutableStateFlow<PendingApply?>(null)
    val draft = MutableStateFlow(OverrideDraft())
    val saveFailed = MutableStateFlow(false)
    private var draftLoaded = false

    init {
        viewModelScope.launch {
            data.collect { d ->
                if (d != null && !draftLoaded) {
                    draftLoaded = true
                    resetDraft(d)
                }
            }
        }
    }

    private fun resetDraft(d: ProfileData) {
        val m = d.settings.macroPctOverride
        draft.value = OverrideDraft(
            kcalOn = d.settings.kcalOverride != null,
            kcalText = d.settings.kcalOverride?.toString() ?: d.goal.targetKcal.toString(),
            macroOn = m != null,
            p = (m?.protein ?: 30).toString(),
            c = (m?.carb ?: 40).toString(),
            f = (m?.fat ?: 30).toString(),
        )
    }

    fun open(s: ProfileSheet) { sheet.value = s }
    fun closeSheet() { sheet.value = null }

    fun rateOptions(input: ProfileInput, weightKg: Double): List<RateOption> =
        GoalPlanner.rateOptions(repo.toCalc(input, weightKg))

    fun preview(input: ProfileInput, weightKg: Double, kcal: Int?, macro: MacroPct?): GoalPreview =
        repo.preview(input, weightKg, kcal, macro)

    /** Xong ở sheet sửa hồ sơ → mở sheet Đang dùng / Mới (giữ ghi đè hiện có). */
    fun proposeProfile(newInput: ProfileInput) {
        val d = data.value ?: return
        sheet.value = null
        if (newInput == d.profile.toInput()) return
        val reason = if (newInput.copy(weeklyRateKg = d.profile.weeklyRateKg) == d.profile.toInput()) {
            ApplyReason.Rate(d.profile.weeklyRateKg, newInput.weeklyRateKg)
        } else {
            ApplyReason.Profile
        }
        val kcal = d.settings.kcalOverride
        val macro = d.settings.macroPctOverride
        pending.value = PendingApply(newInput, kcal, macro, repo.preview(newInput, d.weight.weightKg, kcal, macro), reason)
    }

    // ---- Ghi đè ----

    fun setKcalOn(on: Boolean) {
        draft.update { it.copy(kcalOn = on) }
        if (!on) proposeOverride() else Unit
    }
    fun setKcalText(v: String) = draft.update { it.copy(kcalText = v.filter(Char::isDigit).take(4)) }
    fun setMacroOn(on: Boolean) {
        draft.update { it.copy(macroOn = on) }
        if (!on) proposeOverride()
    }
    fun setP(v: String) = draft.update { it.copy(p = v.filter(Char::isDigit).take(3)) }
    fun setC(v: String) = draft.update { it.copy(c = v.filter(Char::isDigit).take(3)) }
    fun setF(v: String) = draft.update { it.copy(f = v.filter(Char::isDigit).take(3)) }

    /** Rời ô ghi đè (hoặc tắt công tắc) → sheet Đang dùng / Mới nếu số hợp lệ và có thay đổi. */
    fun proposeOverride() {
        val d = data.value ?: return
        val dr = draft.value
        if (dr.kcalOn && !InputLimits.kcalOverrideValid(dr.kcal)) return
        if (dr.macroOn && !dr.macroValid) return
        val kcal = if (dr.kcalOn) dr.kcal else null
        val macro = dr.macro()
        if (kcal == d.settings.kcalOverride && macro == d.settings.macroPctOverride) return
        val input = d.profile.toInput()
        pending.value = PendingApply(input, kcal, macro, repo.preview(input, d.weight.weightKg, kcal, macro), ApplyReason.Override(kcal, macro))
    }

    /** Ghi đè hiện tại tính thử (hộp hổ phách dưới ô kcal). */
    fun overridePreview(): GoalPreview? {
        val d = data.value ?: return null
        val dr = draft.value
        if (!dr.kcalOn || !InputLimits.kcalOverrideValid(dr.kcal)) return null
        return repo.preview(d.profile.toInput(), d.weight.weightKg, dr.kcal, dr.macro())
    }

    fun cancelPending() {
        pending.value = null
        data.value?.let(::resetDraft)
    }

    fun applyPending() {
        val p = pending.value ?: return
        val d = data.value ?: return
        viewModelScope.launch {
            val r = repo.apply(p.input, d.profile.autoRecalcOnWeighIn, p.kcalOverride, p.macroOverride)
            saveFailed.value = r.isFailure
            if (r.isSuccess) {
                pending.value = null
                draftLoaded = false
            }
        }
    }

    fun setAutoRecalc(on: Boolean) {
        viewModelScope.launch { saveFailed.value = repo.setAutoRecalc(on).isFailure }
    }

    fun goalTypeNeedsTarget(g: GoalType) = g != GoalType.MAINTAIN
}
