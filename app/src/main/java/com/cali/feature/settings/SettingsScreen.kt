package com.cali.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.cali.BuildConfig
import com.cali.R
import com.cali.core.calc.model.GoalType
import com.cali.core.calc.model.MealType
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.prefs.SecureKeyStore
import com.cali.core.data.prefs.Settings
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUi(
    val profile: UserProfileEntity?,
    val goal: GoalSnapshotEntity?,
    val settings: Settings,
    val hasKey: Boolean,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    profileRepo: ProfileRepository,
    settingsRepo: SettingsRepository,
    keyStore: SecureKeyStore,
) : ViewModel() {
    val ui = combine(profileRepo.profile, profileRepo.observeGoal(), settingsRepo.settings, keyStore.hasKey) { p, g, s, k ->
        SettingsUi(p, g, s, k)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** S40 — vỏ danh sách mục. GĐ2 chỉ mở được S41; các mục khác ở giai đoạn sau. */
@Composable
fun SettingsRoute(onOpenProfile: () -> Unit, onOpenData: () -> Unit, onOpenMyFoods: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg)) {
        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.s40_title), style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
        }
        val u = ui ?: return@Column
        val s = u.settings

        GroupTitle(stringResource(R.string.s40_group_you))
        GroupBlock {
            val sub = u.goal?.let { g ->
                val kcal = VnFormat.int(g.targetKcal)
                when (u.profile?.goalType) {
                    GoalType.LOSE -> stringResource(R.string.s40_profile_sub_lose, kcal, VnFormat.kg(u.profile.targetWeightKg ?: 0.0))
                    GoalType.GAIN -> stringResource(R.string.s40_profile_sub_gain, kcal, VnFormat.kg(u.profile.targetWeightKg ?: 0.0))
                    else -> stringResource(R.string.s40_profile_sub_maintain, kcal)
                }
            } ?: ""
            NavRow(Icons.Outlined.Person, stringResource(R.string.s40_profile), sub, onOpenProfile)
        }
        Spacer(Modifier.height(Spacing.xl))

        GroupTitle(stringResource(R.string.s40_group_logging))
        GroupBlock {
            NavRow(Icons.Rounded.AutoAwesome, stringResource(R.string.s40_ai), stringResource(if (u.hasKey) R.string.s40_ai_sub_key else R.string.s40_ai_sub_none), null)
            RowDivider()
            val litres = VnFormat.decimal1((u.goal?.waterMl ?: 0) / 1000.0)
            NavRow(
                Icons.Outlined.WaterDrop, stringResource(R.string.s40_water),
                if (s.waterEnabled) stringResource(R.string.s40_water_sub_on, litres, s.waterIntervalMin, s.waterStart.toString(), s.waterEnd.toString())
                else stringResource(R.string.s40_water_sub_off, litres),
                null,
            )
            RowDivider()
            NavRow(Icons.Rounded.Restaurant, stringResource(R.string.s40_meals), mealSummary(s), null)
        }
        Spacer(Modifier.height(Spacing.xl))

        GroupTitle(stringResource(R.string.s40_group_data))
        GroupBlock {
            NavRow(Icons.Outlined.BookmarkBorder, stringResource(R.string.s34_title), stringResource(R.string.s40_my_foods_sub), onOpenMyFoods)
            RowDivider()
            NavRow(Icons.Outlined.Storage, stringResource(R.string.s40_data), stringResource(R.string.s40_data_sub), onOpenData)
        }
        Spacer(Modifier.height(Spacing.xl))

        GroupTitle(stringResource(R.string.s40_group_about))
        GroupBlock { NavRow(Icons.Outlined.Info, stringResource(R.string.s40_about, BuildConfig.VERSION_NAME), stringResource(R.string.s40_about_sub), null, soon = false) }
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
private fun mealSummary(s: Settings): String {
    val names = mapOf(
        MealType.BREAKFAST to stringResource(R.string.meal_breakfast),
        MealType.LUNCH to stringResource(R.string.meal_lunch),
        MealType.SNACK to stringResource(R.string.meal_snack),
        MealType.DINNER to stringResource(R.string.meal_dinner),
    )
    // Mỗi bữa một lần, theo mốc đầu tiên trong ngày.
    return s.mealTimes.sortedBy { it.start }.distinctBy { it.meal }.joinToString(" · ") { "${names[it.meal]} ${it.start}" }
}

/** Hàng 64dp theo ngữ pháp "Xem thêm": ô biểu tượng 40dp góc 12dp, tiêu đề, dòng phụ là giá trị đang dùng. */
@Composable
private fun NavRow(icon: ImageVector, title: String, sub: String, onClick: (() -> Unit)?, soon: Boolean = onClick == null) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).background(cs.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = cs.onSecondaryContainer, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (onClick != null) cs.onSurface else cs.onSurfaceVariant)
            Text(
                if (soon) "$sub · ${stringResource(R.string.s40_soon)}" else sub,
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
            )
        }
        if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant)
    }
}
