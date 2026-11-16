package com.cali.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Sinh từ frontmatter `colors` của DESIGN.md. Đây là nơi DUY NHẤT được gõ mã màu.

// ---- Sáng ----
internal val Primary = Color(0xFF4355B9)
internal val OnPrimary = Color(0xFFFFFFFF)
internal val PrimaryContainer = Color(0xFFDEE0FF)
internal val OnPrimaryContainer = Color(0xFF00105C)
internal val SecondaryContainer = Color(0xFFE0E1F9)
internal val OnSecondaryContainer = Color(0xFF181A2C)
internal val Surface = Color(0xFFFBF8FF)
internal val SurfaceLow = Color(0xFFF5F2FA)
internal val SurfaceCont = Color(0xFFEFEDF4)
internal val SurfaceHighest = Color(0xFFE4E1E9)
internal val OnSurface = Color(0xFF1B1B21)
internal val OnSurfaceVar = Color(0xFF46464F)
internal val Outline = Color(0xFF767680)
internal val OutlineVar = Color(0xFFC6C5D0)
internal val Error = Color(0xFFBA1A1A)
internal val OnError = Color(0xFFFFFFFF)
internal val ErrorContainer = Color(0xFFFFDAD6)
internal val OnErrorContainer = Color(0xFF410002)
internal val Warn = Color(0xFF8A5100)
internal val WarnContainer = Color(0xFFFFDCBE)
internal val OnWarnContainer = Color(0xFF2C1600)
internal val InverseSurface = Color(0xFF303036)
internal val InverseOnSurface = Color(0xFFF2EFF7)
internal val InversePrimary = Color(0xFFBAC3FF)
internal val Scrim = Color(0x52000000) // rgba(0,0,0,0.32)
internal val Protein = Color(0xFF006780)
internal val Carb = Color(0xFF4C6700)
internal val Fat = Color(0xFF8E4585)
internal val MealSang = Color(0xFF4355B9)
internal val MealTrua = Color(0xFF5868C9)
internal val MealToi = Color(0xFF1E2A78)
internal val MealPhu = Color(0xFF6C79D6)

// ---- Tối ----
internal val PrimaryDark = Color(0xFFBAC3FF)
internal val OnPrimaryDark = Color(0xFF08218A)
internal val PrimaryContainerDark = Color(0xFF293CA0)
internal val OnPrimaryContainerDark = Color(0xFFDEE0FF)
internal val SecondaryContainerDark = Color(0xFF43465A)
internal val OnSecondaryContainerDark = Color(0xFFE0E1F9)
internal val SurfaceDark = Color(0xFF121318)
internal val SurfaceLowDark = Color(0xFF1B1B21)
internal val SurfaceContDark = Color(0xFF1F1F25)
internal val SurfaceHighestDark = Color(0xFF35343A)
internal val OnSurfaceDark = Color(0xFFE4E1E9)
internal val OnSurfaceVarDark = Color(0xFFC6C5D0)
internal val OutlineDark = Color(0xFF90909A)
internal val OutlineVarDark = Color(0xFF46464F)
internal val ErrorDark = Color(0xFFFFB4AB)
internal val OnErrorDark = Color(0xFF690005)
internal val ErrorContainerDark = Color(0xFF93000A)
internal val OnErrorContainerDark = Color(0xFFFFDAD6)
internal val WarnDark = Color(0xFFFFB86E)
internal val WarnContainerDark = Color(0xFF693C00)
internal val OnWarnContainerDark = Color(0xFFFFDCBE)
internal val InverseSurfaceDark = Color(0xFFE4E1E9)
internal val InverseOnSurfaceDark = Color(0xFF303036)
internal val InversePrimaryDark = Color(0xFF4355B9)
internal val ScrimDark = Color(0x80000000) // rgba(0,0,0,0.5)
internal val ProteinDark = Color(0xFF5FD4F6)
internal val CarbDark = Color(0xFFB1D46A)
internal val FatDark = Color(0xFFFFAEE9)
internal val MealSangDark = Color(0xFFBAC3FF)
internal val MealTruaDark = Color(0xFF8E9BF5)
internal val MealToiDark = Color(0xFFE4E6FF)
internal val MealPhuDark = Color(0xFF6B78C9)

/*
 * DESIGN.md không có token cho secondary/tertiary (vai trò chữ), surfaceContainerLowest/High,
 * surfaceDim/Bright. Gán theo token gần nhất để không lọt màu tím mặc định của M3 (ghi ở 09 §8).
 */
internal val LightScheme: ColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Primary,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Primary,
    onTertiary = OnPrimary,
    tertiaryContainer = PrimaryContainer,
    onTertiaryContainer = OnPrimaryContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = OnSurfaceVar,
    surfaceTint = Primary,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVar,
    scrim = Scrim,
    surfaceBright = Surface,
    surfaceDim = SurfaceHighest,
    surfaceContainerLowest = Surface,
    surfaceContainerLow = SurfaceLow,
    surfaceContainer = SurfaceCont,
    surfaceContainerHigh = SurfaceCont,
    surfaceContainerHighest = SurfaceHighest,
)

internal val DarkScheme: ColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    inversePrimary = InversePrimaryDark,
    secondary = PrimaryDark,
    onSecondary = OnPrimaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = PrimaryDark,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = PrimaryContainerDark,
    onTertiaryContainer = OnPrimaryContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceHighestDark,
    onSurfaceVariant = OnSurfaceVarDark,
    surfaceTint = PrimaryDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = OutlineDark,
    outlineVariant = OutlineVarDark,
    scrim = ScrimDark,
    surfaceBright = SurfaceHighestDark,
    surfaceDim = SurfaceDark,
    surfaceContainerLowest = SurfaceDark,
    surfaceContainerLow = SurfaceLowDark,
    surfaceContainer = SurfaceContDark,
    surfaceContainerHigh = SurfaceContDark,
    surfaceContainerHighest = SurfaceHighestDark,
)

/** Màu ngoài ColorScheme của M3: trạng thái hổ phách, ba màu dữ liệu, bốn tông bữa, rãnh vòng. */
@Immutable
data class CaliColors(
    val warn: Color,
    val warnContainer: Color,
    val onWarnContainer: Color,
    val protein: Color,
    val carb: Color,
    val fat: Color,
    val mealSang: Color,
    val mealTrua: Color,
    val mealToi: Color,
    val mealPhu: Color,
    /** Rãnh vòng calo / thanh làn: surface-cont (sáng), surface-highest (tối). */
    val ringTrack: Color,
)

internal val LightCaliColors = CaliColors(
    warn = Warn,
    warnContainer = WarnContainer,
    onWarnContainer = OnWarnContainer,
    protein = Protein,
    carb = Carb,
    fat = Fat,
    mealSang = MealSang,
    mealTrua = MealTrua,
    mealToi = MealToi,
    mealPhu = MealPhu,
    ringTrack = SurfaceCont,
)

internal val DarkCaliColors = CaliColors(
    warn = WarnDark,
    warnContainer = WarnContainerDark,
    onWarnContainer = OnWarnContainerDark,
    protein = ProteinDark,
    carb = CarbDark,
    fat = FatDark,
    mealSang = MealSangDark,
    mealTrua = MealTruaDark,
    mealToi = MealToiDark,
    mealPhu = MealPhuDark,
    ringTrack = SurfaceHighestDark,
)
