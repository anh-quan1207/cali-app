package com.cali.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cali.core.calc.GoalPlanner
import com.cali.core.calc.InputLimits
import com.cali.core.calc.NutritionCalculator
import com.cali.core.calc.RateOption
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.BmiClass
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.Sex
import com.cali.core.data.repo.GoalPreview
import com.cali.core.data.repo.ProfileInput
import com.cali.core.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Bước 1–5 của S01; 6 = S02 kết quả. */
data class OnboardingUiState(
    val step: Int = 1,
    val currentYear: Int,
    val sex: Sex? = null,
    val birthYear: Int,
    val heightText: String = "",
    val weightText: String = "",
    val activity: ActivityLevel? = null,
    val goal: GoalType? = null,
    val targetText: String = "",
    val rate: Double? = null,
    val howExpanded: Boolean = false,
    val saving: Boolean = false,
    val saveFailed: Boolean = false,
) {
    val height: Double? get() = InputLimits.parseDecimal(heightText)
    val weight: Double? get() = InputLimits.parseDecimal(weightText)
    val target: Double? get() = InputLimits.parseDecimal(targetText)

    val heightError: Boolean get() = heightText.isNotBlank() && !InputLimits.heightValid(height)
    val weightError: Boolean get() = weightText.isNotBlank() && !InputLimits.weightValid(weight)
    val targetError: Boolean get() = targetText.isNotBlank() && !InputLimits.weightValid(target)

    val bmi: Double? get() =
        if (InputLimits.heightValid(height) && InputLimits.weightValid(weight)) NutritionCalculator.bmi(weight!!, height!!) else null
    val targetBmi: Double? get() =
        if (InputLimits.heightValid(height) && InputLimits.weightValid(target)) NutritionCalculator.bmi(target!!, height!!) else null
    val bmiClass: BmiClass? get() = bmi?.let(NutritionCalculator::bmiClass)
    val targetBmiClass: BmiClass? get() = targetBmi?.let(NutritionCalculator::bmiClass)

    val lowTargetBmi: Boolean get() = targetBmi != null && targetBmi!! < 18.5
    val losingWhileUnderweight: Boolean get() = goal == GoalType.LOSE && bmi != null && bmi!! < 18.5

    /** Đích hợp lệ theo hướng: giảm thì thấp hơn, tăng thì cao hơn cân hiện tại. */
    val targetDirectionOk: Boolean get() = when (goal) {
        GoalType.LOSE -> target != null && weight != null && target!! < weight!!
        GoalType.GAIN -> target != null && weight != null && target!! > weight!!
        else -> true
    }

    val totalSteps = 5

    fun input(): ProfileInput? {
        val s = sex ?: return null
        val a = activity ?: return null
        val g = goal ?: return null
        val h = height?.takeIf(InputLimits::heightValid) ?: return null
        return ProfileInput(
            sex = s, birthYear = birthYear, heightCm = h, activityLevel = a, goalType = g,
            targetWeightKg = if (g == GoalType.MAINTAIN) null else target,
            weeklyRateKg = if (g == GoalType.MAINTAIN) 0.0 else (rate ?: GoalPlanner.defaultRate(g)),
        )
    }

    val canContinue: Boolean get() = when (step) {
        1 -> sex != null
        2 -> InputLimits.heightValid(height) && InputLimits.weightValid(weight)
        3 -> activity != null
        4 -> goal != null
        5 -> InputLimits.weightValid(target) && targetDirectionOk && rate != null
        else -> true
    }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repo: ProfileRepository,
) : ViewModel() {

    private val year = repo.today().year
    private val _state = MutableStateFlow(OnboardingUiState(currentYear = year, birthYear = year - 30))
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    /** Sự kiện một lần: đã lưu xong → sang S03. */
    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    fun setSex(v: Sex) = _state.update { it.copy(sex = v) }
    fun setBirthYear(v: Int) = _state.update { it.copy(birthYear = v) }
    fun setHeight(v: String) = _state.update { it.copy(heightText = v) }
    fun setWeight(v: String) = _state.update { it.copy(weightText = v) }
    fun setActivity(v: ActivityLevel) = _state.update { it.copy(activity = v) }
    fun setGoal(v: GoalType) = _state.update {
        it.copy(goal = v, rate = if (v == it.goal) it.rate else GoalPlanner.defaultRate(v).takeIf { r -> r > 0 })
    }
    fun setTarget(v: String) = _state.update { it.copy(targetText = v) }
    fun setRate(v: Double) = _state.update { it.copy(rate = v) }
    fun toggleHow() = _state.update { it.copy(howExpanded = !it.howExpanded) }

    fun next() = _state.update { s ->
        if (!s.canContinue) return@update s
        val nextStep = if (s.step == 4 && s.goal == GoalType.MAINTAIN) 6 else s.step + 1
        s.copy(step = nextStep.coerceAtMost(6))
    }

    /** false = đang ở bước 1, không lùi được. */
    fun back(): Boolean {
        val s = _state.value
        if (s.step <= 1) return false
        val prev = if (s.step == 6 && s.goal == GoalType.MAINTAIN) 4 else s.step - 1
        _state.update { it.copy(step = prev) }
        return true
    }

    fun rateOptions(s: OnboardingUiState = state.value): List<RateOption> {
        val input = s.input()?.copy(weeklyRateKg = s.rate ?: 0.0) ?: return emptyList()
        val w = s.weight ?: return emptyList()
        if (!InputLimits.weightValid(s.target)) return emptyList()
        return GoalPlanner.rateOptions(repo.toCalc(input, w))
    }

    fun preview(s: OnboardingUiState = state.value): GoalPreview? {
        val input = s.input() ?: return null
        val w = s.weight ?: return null
        return repo.preview(input, w)
    }

    fun start() {
        val s = _state.value
        val input = s.input() ?: return
        val w = s.weight ?: return
        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val r = repo.completeOnboarding(input, w)
            _state.update { it.copy(saving = false, saveFailed = r.isFailure) }
            if (r.isSuccess) _done.send(Unit)
        }
    }
}
