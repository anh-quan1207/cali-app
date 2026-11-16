package com.cali.feature.onboarding

import androidx.compose.material.icons.rounded.Restore
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cali.R
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.Sex
import com.cali.core.ui.component.AmberBox
import com.cali.core.ui.component.BmiScale
import com.cali.core.ui.component.IconChoiceRow
import com.cali.core.ui.component.IconTile
import com.cali.core.ui.component.NumberField
import com.cali.core.ui.component.PrimaryButton
import com.cali.core.ui.component.RateRow
import com.cali.core.ui.component.SelectCard
import com.cali.core.ui.component.StepHeader
import com.cali.core.ui.component.YearWheel
import com.cali.core.ui.component.description
import com.cali.core.ui.component.icon
import com.cali.core.ui.component.label
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import com.cali.core.calc.InputLimits

/** S01 (5 bước) + S02 (kết quả). */
@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    onRestored: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
    data: com.cali.feature.settings.DataViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snack = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
    com.cali.core.ui.component.CollectMessages(data.messages, snack)
    val restore = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { data.import(it, onRestored) }
    }
    LaunchedEffect(Unit) { vm.done.collect { onFinished() } }
    BackHandler(enabled = s.step > 1) { vm.back() }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        if (s.step == 6) {
            ResultScreen(s, vm.preview(s), onBack = { vm.back() }, onToggleHow = vm::toggleHow, onStart = vm::start)
        } else {
            OnboardingSteps(s, vm, onRestore = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) })
        }
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
            androidx.compose.material3.SnackbarHost(snack)
        }
    }
}

@Composable
private fun OnboardingSteps(s: OnboardingUiState, vm: OnboardingViewModel, onRestore: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
        StepHeader(step = s.step, total = s.totalSteps, onBack = if (s.step > 1) ({ vm.back() }) else null)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.gutter),
        ) {
            Spacer(Modifier.height(Spacing.lg))
            when (s.step) {
                1 -> Step1(s, vm, onRestore)
                2 -> Step2(s, vm)
                3 -> Step3(s, vm)
                4 -> Step4(s, vm)
                5 -> Step5(s, vm)
            }
            Spacer(Modifier.height(Spacing.lg))
        }
        PrimaryButton(
            text = stringResource(if (s.step == 5) R.string.s01_see_result else R.string.action_next),
            onClick = vm::next,
            enabled = s.canContinue,
            modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.lg),
        )
    }
}

@Composable
private fun StepTitle(title: String, lead: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
    if (lead != null) {
        Spacer(Modifier.height(Spacing.xs))
        Text(lead, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(Spacing.gutter))
}

@Composable
private fun Step1(s: OnboardingUiState, vm: OnboardingViewModel, onRestore: () -> Unit) {
    StepTitle(stringResource(R.string.s01_step1_title), stringResource(R.string.s01_step1_lead))
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Sex.entries.forEach { sex ->
            SelectCard(selected = s.sex == sex, onClick = { vm.setSex(sex) }, modifier = Modifier.weight(1f).height(110.dp)) {
                Column(Modifier.padding(Spacing.lg)) {
                    IconTile(sex.icon(), s.sex == sex, size = 44)
                    Spacer(Modifier.height(Spacing.md))
                    Text(stringResource(sex.label()), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
    Spacer(Modifier.height(Spacing.xl))
    Text(stringResource(R.string.s01_birth_year), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(Spacing.sm))
    val ctx = LocalResources.current
    YearWheel(
        years = InputLimits.birthYears(s.currentYear),
        selected = s.birthYear,
        onSelected = vm::setBirthYear,
        label = { y -> ctx.getString(R.string.s01_year_age, y, s.currentYear - y) },
    )
    Spacer(Modifier.height(Spacing.lg))
    androidx.compose.material3.TextButton(onClick = onRestore, modifier = Modifier.height(48.dp)) {
        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Restore, null, Modifier.padding(end = Spacing.sm))
        Text(stringResource(R.string.s01_restore), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Step2(s: OnboardingUiState, vm: OnboardingViewModel) {
    StepTitle(stringResource(R.string.s01_step2_title))
    NumberField(
        value = s.heightText, onValueChange = vm::setHeight,
        label = stringResource(R.string.field_height), unit = stringResource(R.string.unit_cm),
        error = if (s.heightError) stringResource(R.string.error_height) else null,
    )
    Spacer(Modifier.height(Spacing.md))
    NumberField(
        value = s.weightText, onValueChange = vm::setWeight,
        label = stringResource(R.string.field_weight_now), unit = stringResource(R.string.unit_kg),
        error = if (s.weightError) stringResource(R.string.error_weight) else null,
        imeAction = ImeAction.Done, onDone = vm::next,
    )
    Spacer(Modifier.height(Spacing.lg))
    val bmi = s.bmi
    if (bmi == null) {
        Text(stringResource(R.string.s01_bmi_pending), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        BmiCard(bmi, stringResource(s.bmiClass!!.label()))
    }
}

@Composable
private fun BmiCard(bmi: Double, cls: String) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(cs.surfaceContainerLow)
            .padding(Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.bmi_value, VnFormat.decimal1(bmi)), style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.bmi_asia, cls), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
        Spacer(Modifier.height(Spacing.md))
        BmiScale(bmi)
    }
}

@Composable
private fun Step3(s: OnboardingUiState, vm: OnboardingViewModel) {
    StepTitle(stringResource(R.string.s01_step3_title), stringResource(R.string.s01_step3_lead))
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        ActivityLevel.entries.forEach { a ->
            IconChoiceRow(
                icon = a.icon(), title = stringResource(a.label()), subtitle = stringResource(a.description()),
                selected = s.activity == a, onClick = { vm.setActivity(a) },
                trailing = "×" + VnFormat.factor(a.factor),
            )
        }
    }
}

@Composable
private fun Step4(s: OnboardingUiState, vm: OnboardingViewModel) {
    StepTitle(stringResource(R.string.s01_step4_title))
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        GoalType.entries.forEach { g ->
            IconChoiceRow(
                icon = g.icon(), title = stringResource(g.label()), subtitle = stringResource(g.description()),
                selected = s.goal == g, onClick = { vm.setGoal(g) },
            )
        }
    }
}

@Composable
private fun Step5(s: OnboardingUiState, vm: OnboardingViewModel) {
    StepTitle(stringResource(R.string.s01_step5_title))
    val weight = s.weight ?: 0.0
    val directionError = InputLimits.weightValid(s.target) && !s.targetDirectionOk
    NumberField(
        value = s.targetText, onValueChange = vm::setTarget,
        label = stringResource(R.string.field_target_weight), unit = stringResource(R.string.unit_kg),
        error = when {
            s.targetError -> stringResource(R.string.error_weight)
            directionError && s.goal == GoalType.LOSE -> stringResource(R.string.error_target_lose, VnFormat.kg(weight))
            directionError -> stringResource(R.string.error_target_gain, VnFormat.kg(weight))
            else -> null
        },
        supporting = s.targetBmi?.let { stringResource(R.string.s01_target_bmi, VnFormat.decimal1(it), stringResource(s.targetBmiClass!!.label())) },
        imeAction = ImeAction.Done,
    )
    if (s.lowTargetBmi || s.losingWhileUnderweight) {
        Spacer(Modifier.height(Spacing.md))
        AmberBox(stringResource(if (s.losingWhileUnderweight) R.string.s01_low_bmi_now else R.string.s01_low_bmi_warning))
    }
    val options = vm.rateOptions(s)
    if (options.isNotEmpty() && s.targetDirectionOk) {
        Spacer(Modifier.height(Spacing.xl))
        Text(stringResource(R.string.s01_rate_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            options.forEach { o -> RateRow(o, selected = s.rate == o.rateKg, onClick = { vm.setRate(o.rateKg) }) }
        }
    }
}
