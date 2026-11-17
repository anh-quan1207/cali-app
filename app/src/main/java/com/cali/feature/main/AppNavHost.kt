package com.cali.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cali.R
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import com.cali.feature.onboarding.OnboardingRoute
import com.cali.feature.onboarding.QuickSetupRoute
import com.cali.feature.settings.ProfileRoute
import com.cali.feature.settings.SettingsRoute
import com.cali.feature.today.TodayRoute
import com.cali.feature.food.EditFoodRoute
import com.cali.feature.food.LogFoodRoute
import com.cali.feature.food.MyFoodEditRoute
import com.cali.feature.food.MyFoodsRoute
import com.cali.feature.settings.DataRoute
import com.cali.feature.settings.WaterSettingsRoute
import com.cali.feature.water.WaterRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable object OnboardingDest
@Serializable object QuickSetupDest
@Serializable object MainDest
@Serializable object ProfileDest
/** [ai] = kết quả AI chờ trong AiDraftHolder; [chatId] = mở thẻ món của chat; [start] = "manual" | "myfoods". */
@Serializable data class LogFoodDest(val date: String, val meal: String = "", val ai: Boolean = false, val chatId: Long = -1, val start: String = "")
@Serializable object CaptureDest
@Serializable data class EditFoodDest(val id: Long)
@Serializable object MyFoodsDest
@Serializable data class MyFoodEditDest(val id: Long = -1)
@Serializable object DataDest
// GĐ5
@Serializable data class WaterDest(val date: String)
@Serializable object WaterSettingsDest
@Serializable object AiSettingsDest
@Serializable object SystemPromptDest
@Serializable data class WeeklyDest(val week: String = "")
@Serializable data class DayDest(val date: String)
@Serializable data class StatsDest(val month: Boolean = false)
@Serializable object WeightDest
@Serializable object MealTimesDest

@HiltViewModel
class AppViewModel @Inject constructor(
    settings: SettingsRepository,
    val chatPrefill: com.cali.feature.chat.ChatPrefill,
    val deepLinks: DeepLinks,
) : ViewModel() {
    /** null = đang đọc DataStore. */
    val onboardingDone = settings.settings.map { it.onboardingDone }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

/** 05 §1: chưa onboarding → S01 → S02 → S03 → vỏ chính; đã onboarding → vỏ chính. */
@Composable
fun AppNavHost(vm: AppViewModel = hiltViewModel()) {
    val done by vm.onboardingDone.collectAsStateWithLifecycle()
    // Chốt màn đầu một lần: "Bắt đầu" ở S02 bật onboarding_done nhưng vẫn phải đi tiếp S03.
    var start by remember { mutableStateOf<Any?>(null) }
    if (start == null) {
        start = when (done) {
            null -> return
            true -> MainDest
            false -> OnboardingDest
        }
    }
    val startDest = start ?: return
    val nav = rememberNavController()
    // Thông báo tổng kết tuần → S35 (chỉ khi đã qua onboarding).
    val weeklyLink by vm.deepLinks.weekly.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(weeklyLink) {
        val w = weeklyLink ?: return@LaunchedEffect
        if (done == true) nav.navigate(WeeklyDest(w))
        vm.deepLinks.weekly.value = null
    }
    NavHost(nav, startDestination = startDest) {
        composable<OnboardingDest> {
            OnboardingRoute(
                onFinished = { nav.navigate(QuickSetupDest) { popUpTo(OnboardingDest) { inclusive = true } } },
                onRestored = { nav.toMain() },
            )
        }
        composable<QuickSetupDest> {
            QuickSetupRoute(onFinished = { nav.toMain() }, onBack = { nav.toMain() })
        }
        composable<MainDest> {
            MainShell(
                onOpenProfile = { nav.navigate(ProfileDest) },
                onOpenData = { nav.navigate(DataDest) },
                onOpenMyFoods = { nav.navigate(MyFoodsDest) },
                onQuickAdd = { d, m -> nav.navigate(LogFoodDest(d.toString(), m?.name.orEmpty())) },
                onEditFood = { nav.navigate(EditFoodDest(it)) },
                onOpenWater = { nav.navigate(WaterDest(it.toString())) },
                onOpenWaterSettings = { nav.navigate(WaterSettingsDest) },
                onOpenAi = { nav.navigate(AiSettingsDest) },
                onCapture = { nav.navigate(CaptureDest) },
                onOpenChatCard = { nav.navigate(LogFoodDest(java.time.LocalDate.now().toString(), chatId = it)) },
                prefill = vm.chatPrefill,
                onOpenDay = { nav.navigate(DayDest(it.toString())) },
                onOpenWeekly = { nav.navigate(WeeklyDest(it?.toString().orEmpty())) },
                onOpenWeight = { nav.navigate(WeightDest) },
                onOpenStats = { nav.navigate(StatsDest()) },
                onOpenMeals = { nav.navigate(MealTimesDest) },
            )
        }
        composable<WaterDest> { WaterRoute(onBack = { nav.popBackStack() }, onOpenSettings = { nav.navigate(WaterSettingsDest) }) }
        composable<WaterSettingsDest> { WaterSettingsRoute(onBack = { nav.popBackStack() }) }
        composable<ProfileDest> { ProfileRoute(onBack = { nav.popBackStack() }) }
        composable<LogFoodDest> {
            LogFoodRoute(
                onClose = { nav.popBackStack() },
                onRetake = { nav.navigate(CaptureDest) { popUpTo<LogFoodDest> { inclusive = true } } },
                onAskAi = { text ->
                    vm.chatPrefill.text.value = text
                    nav.popBackStack(MainDest, inclusive = false)
                },
            )
        }
        composable<CaptureDest> {
            val today = java.time.LocalDate.now().toString()
            com.cali.feature.capture.CaptureRoute(
                onClose = { nav.popBackStack() },
                onConfirm = { nav.navigate(LogFoodDest(today, ai = true)) { popUpTo<CaptureDest> { inclusive = true } } },
                onOpenAiSettings = { nav.navigate(AiSettingsDest) },
                onManual = { nav.navigate(LogFoodDest(today, start = "manual")) { popUpTo<CaptureDest> { inclusive = true } } },
                onMyFoods = { nav.navigate(LogFoodDest(today, start = "myfoods")) { popUpTo<CaptureDest> { inclusive = true } } },
            )
        }
        composable<EditFoodDest> { EditFoodRoute(onClose = { nav.popBackStack() }) }
        composable<MyFoodsDest> { MyFoodsRoute(onBack = { nav.popBackStack() }, onOpen = { nav.navigate(MyFoodEditDest(it ?: -1)) }) }
        composable<MyFoodEditDest> { MyFoodEditRoute(onClose = { nav.popBackStack() }) }
        composable<AiSettingsDest> { com.cali.feature.settings.AiSettingsRoute(onBack = { nav.popBackStack() }, onOpenPrompt = { nav.navigate(SystemPromptDest) }) }
        composable<WeeklyDest> {
            com.cali.feature.stats.WeeklyRoute(
                onBack = { nav.popBackStack() },
                onOpenDay = { nav.navigate(DayDest(it.toString())) },
                onOpenAiSettings = { nav.navigate(AiSettingsDest) },
            )
        }
        composable<DayDest> { entry ->
            val date = java.time.LocalDate.parse(entry.toRoute<DayDest>().date)
            TodayRoute(
                onQuickAdd = { d, m -> nav.navigate(LogFoodDest(d.toString(), m?.name.orEmpty())) },
                onEditFood = { nav.navigate(EditFoodDest(it)) },
                onOpenWater = { nav.navigate(WaterDest(it.toString())) },
                onCapture = { nav.navigate(CaptureDest) },
                onOpenChat = { nav.popBackStack(MainDest, inclusive = false) },
                onOpenWeekly = { nav.navigate(WeeklyDest(it.toString())) },
                fixedDate = date,
                onBack = { nav.popBackStack() },
            )
        }
        composable<StatsDest> {
            com.cali.feature.stats.StatsRoute(
                onBack = { nav.popBackStack() },
                onOpenDay = { nav.navigate(DayDest(it.toString())) },
                onOpenWeekly = { nav.navigate(WeeklyDest(it.toString())) },
                onOpenWeight = { nav.navigate(WeightDest) },
            )
        }
        composable<WeightDest> { com.cali.feature.weight.WeightRoute(onBack = { nav.popBackStack() }) }
        composable<MealTimesDest> { com.cali.feature.settings.MealTimesRoute(onBack = { nav.popBackStack() }) }
        composable<SystemPromptDest> { com.cali.feature.settings.SystemPromptRoute(onClose = { nav.popBackStack() }) }
        composable<DataDest> {
            DataRoute(
                onBack = { nav.popBackStack() },
                onRestored = { nav.toMain() },
                onWiped = { nav.navigate(OnboardingDest) { popUpTo(0) { inclusive = true } } },
            )
        }
    }
}

private fun NavHostController.toMain() = navigate(MainDest) { popUpTo(0) { inclusive = true } }

private enum class Tab(val label: Int, val icon: ImageVector, val selectedIcon: ImageVector) {
    TODAY(R.string.tab_today, Icons.Outlined.Today, Icons.Rounded.Today),
    CHAT(R.string.tab_chat, Icons.Outlined.Forum, Icons.Rounded.Forum),
    HISTORY(R.string.tab_history, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
    SETTINGS(R.string.tab_settings, Icons.Outlined.Settings, Icons.Rounded.Settings),
}

/** Vỏ 4 tab (05 §1). GĐ2: Hôm nay / Chat / Lịch sử là chỗ giữ chỗ. */
@Composable
private fun MainShell(
    onOpenProfile: () -> Unit,
    onOpenData: () -> Unit,
    onOpenMyFoods: () -> Unit,
    onQuickAdd: (java.time.LocalDate, com.cali.core.calc.model.MealType?) -> Unit,
    onEditFood: (Long) -> Unit,
    onOpenWater: (java.time.LocalDate) -> Unit,
    onOpenWaterSettings: () -> Unit,
    onOpenAi: () -> Unit,
    onCapture: () -> Unit,
    onOpenChatCard: (Long) -> Unit,
    prefill: com.cali.feature.chat.ChatPrefill,
    onOpenDay: (java.time.LocalDate) -> Unit,
    onOpenWeekly: (java.time.LocalDate?) -> Unit,
    onOpenWeight: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenMeals: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(Tab.TODAY.ordinal) }
    // "Hỏi AI" từ S13 → sang tab Chat với chữ điền sẵn.
    val pre by prefill.text.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(pre) { if (pre != null) tab = Tab.CHAT.ordinal }
    val cs = MaterialTheme.colorScheme
    Scaffold(
        containerColor = cs.surface,
        bottomBar = {
            NavigationBar(containerColor = cs.surfaceContainer) {
                Tab.entries.forEach { t ->
                    val sel = tab == t.ordinal
                    NavigationBarItem(
                        selected = sel,
                        onClick = { tab = t.ordinal },
                        icon = { Icon(if (sel) t.selectedIcon else t.icon, null) },
                        label = { Text(stringResource(t.label), style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = cs.secondaryContainer),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (Tab.entries[tab]) {
                Tab.TODAY -> TodayRoute(onQuickAdd = onQuickAdd, onEditFood = onEditFood, onOpenWater = onOpenWater, onCapture = onCapture, onOpenChat = { tab = Tab.CHAT.ordinal }, onOpenWeekly = onOpenWeekly)
                Tab.CHAT -> com.cali.feature.chat.ChatRoute(onOpenCard = onOpenChatCard, onOpenAiSettings = onOpenAi, onOpenToday = { tab = Tab.TODAY.ordinal })
                Tab.HISTORY -> com.cali.feature.history.HistoryRoute(
                    onOpenDay = onOpenDay, onOpenWeekly = onOpenWeekly, onOpenWeight = onOpenWeight,
                    onOpenStats = onOpenStats, onOpenMyFoods = onOpenMyFoods,
                )
                Tab.SETTINGS -> SettingsRoute(onOpenProfile = onOpenProfile, onOpenData = onOpenData, onOpenMyFoods = onOpenMyFoods, onOpenWater = onOpenWaterSettings, onOpenAi = onOpenAi, onOpenMeals = onOpenMeals)
            }
        }
    }
}
