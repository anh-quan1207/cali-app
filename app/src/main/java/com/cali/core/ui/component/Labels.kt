package com.cali.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AirlineSeatReclineNormal
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Man
import androidx.compose.material.icons.rounded.Woman
import androidx.compose.ui.graphics.vector.ImageVector
import com.cali.R
import com.cali.core.calc.RateLabel
import com.cali.core.calc.model.ActivityLevel
import com.cali.core.calc.model.BmiClass
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.Sex

// Nhãn + biểu tượng cho enum của core.calc. Chuỗi ở strings.xml.

@StringRes fun Sex.label() = if (this == Sex.MALE) R.string.sex_male else R.string.sex_female
fun Sex.icon(): ImageVector = if (this == Sex.MALE) Icons.Rounded.Man else Icons.Rounded.Woman

@StringRes fun ActivityLevel.label() = when (this) {
    ActivityLevel.SEDENTARY -> R.string.activity_sedentary
    ActivityLevel.LIGHT -> R.string.activity_light
    ActivityLevel.MODERATE -> R.string.activity_moderate
    ActivityLevel.ACTIVE -> R.string.activity_active
    ActivityLevel.VERY_ACTIVE -> R.string.activity_very_active
}

@StringRes fun ActivityLevel.description() = when (this) {
    ActivityLevel.SEDENTARY -> R.string.activity_sedentary_desc
    ActivityLevel.LIGHT -> R.string.activity_light_desc
    ActivityLevel.MODERATE -> R.string.activity_moderate_desc
    ActivityLevel.ACTIVE -> R.string.activity_active_desc
    ActivityLevel.VERY_ACTIVE -> R.string.activity_very_active_desc
}

fun ActivityLevel.icon(): ImageVector = when (this) {
    ActivityLevel.SEDENTARY -> Icons.Rounded.AirlineSeatReclineNormal
    ActivityLevel.LIGHT -> Icons.AutoMirrored.Rounded.DirectionsWalk
    ActivityLevel.MODERATE -> Icons.AutoMirrored.Rounded.DirectionsRun
    ActivityLevel.ACTIVE -> Icons.Rounded.FitnessCenter
    ActivityLevel.VERY_ACTIVE -> Icons.Rounded.Handyman
}

@StringRes fun GoalType.label() = when (this) {
    GoalType.LOSE -> R.string.goal_lose
    GoalType.MAINTAIN -> R.string.goal_maintain
    GoalType.GAIN -> R.string.goal_gain
}

@StringRes fun GoalType.description() = when (this) {
    GoalType.LOSE -> R.string.goal_lose_desc
    GoalType.MAINTAIN -> R.string.goal_maintain_desc
    GoalType.GAIN -> R.string.goal_gain_desc
}

fun GoalType.icon(): ImageVector = when (this) {
    GoalType.LOSE -> Icons.AutoMirrored.Rounded.TrendingDown
    GoalType.MAINTAIN -> Icons.AutoMirrored.Rounded.TrendingFlat
    GoalType.GAIN -> Icons.AutoMirrored.Rounded.TrendingUp
}

@StringRes fun RateLabel.label() = when (this) {
    RateLabel.GENTLE -> R.string.rate_gentle
    RateLabel.RECOMMENDED -> R.string.rate_recommended
    RateLabel.FAST -> R.string.rate_fast
    RateLabel.VERY_FAST -> R.string.rate_very_fast
}

@StringRes fun BmiClass.label() = when (this) {
    BmiClass.UNDERWEIGHT -> R.string.bmi_underweight
    BmiClass.NORMAL -> R.string.bmi_normal
    BmiClass.OVERWEIGHT -> R.string.bmi_overweight
    BmiClass.OBESE_1 -> R.string.bmi_obese_1
    BmiClass.OBESE_2 -> R.string.bmi_obese_2
}
