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
import com.cali.R
import com.cali.core.data.prefs.SettingsRepository
import com.cali.core.data.repo.ProfileRepository
import com.cali.core.ui.format.VnFormat
import com.cali.core.ui.theme.Spacing
import com.cali.feature.onboarding.OnboardingRoute
import com.cali.feature.onboarding.QuickSetupRoute
import com.cali.feature.settings.ProfileRoute
import com.cali.feature.settings.SettingsRoute
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

@HiltViewModel
class AppViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
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
    NavHost(nav, startDestination = startDest) {
        composable<OnboardingDest> {
            OnboardingRoute(onFinished = { nav.navigate(QuickSetupDest) { popUpTo(OnboardingDest) { inclusive = true } } })
        }
        composable<QuickSetupDest> {
            QuickSetupRoute(onFinished = { nav.toMain() }, onBack = { nav.toMain() })
        }
        composable<MainDest> { MainShell(onOpenProfile = { nav.navigate(ProfileDest) }) }
        composable<ProfileDest> { ProfileRoute(onBack = { nav.popBackStack() }) }
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
private fun MainShell(onOpenProfile: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(Tab.TODAY.ordinal) }
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
                Tab.TODAY -> TodayPlaceholder()
                Tab.CHAT -> Placeholder(stringResource(R.string.placeholder_chat))
                Tab.HISTORY -> Placeholder(stringResource(R.string.placeholder_history))
                Tab.SETTINGS -> SettingsRoute(onOpenProfile = onOpenProfile)
            }
        }
    }
}

@HiltViewModel
class TodayPlaceholderViewModel @Inject constructor(repo: ProfileRepository) : ViewModel() {
    val target = repo.observeGoal().map { it?.targetKcal }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
private fun TodayPlaceholder(vm: TodayPlaceholderViewModel = hiltViewModel()) {
    val t by vm.target.collectAsStateWithLifecycle()
    Placeholder(stringResource(R.string.placeholder_today, t?.let(VnFormat::int) ?: "—"))
}

@Composable
private fun Placeholder(text: String) {
    Column(Modifier.fillMaxSize().padding(Spacing.xl), verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
