package com.cali.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalCaliColors = staticCompositionLocalOf { LightCaliColors }

/**
 * Theme của Cali. Theo chế độ sáng/tối của hệ thống.
 * Dynamic Color tắt mặc định: bật thì mất seed chàm và ý nghĩa màu trạng thái.
 */
@Composable
fun CaliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    CompositionLocalProvider(LocalCaliColors provides if (darkTheme) DarkCaliColors else LightCaliColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CaliTypography,
            shapes = CaliShapes,
            content = content,
        )
    }
}

object CaliTheme {
    val colors: CaliColors
        @Composable @ReadOnlyComposable get() = LocalCaliColors.current
}
