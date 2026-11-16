package com.cali.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.cali.R

/*
 * Sinh từ frontmatter `typography` + mục Hierarchy của DESIGN.md.
 * Roboto Flex dạng variable: mỗi tổ hợp (wght, opsz, wdth) là một Font riêng.
 * CSS để opsz tự động theo cỡ chữ; Android không tự làm, nên opsz mặc định = cỡ chữ.
 */

private const val TNUM = "tnum"

@OptIn(ExperimentalTextApi::class)
private fun flex(weight: Int, opsz: Float, wdth: Float = 100f): FontFamily = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.Setting("opsz", opsz),
            FontVariation.width(wdth),
        ),
    ),
)

private fun style(
    sizeSp: Int,
    lineSp: Int,
    weight: Int,
    tracking: TextUnit = 0.em,
    opsz: Float = sizeSp.toFloat(),
    wdth: Float = 100f,
) = TextStyle(
    fontFamily = flex(weight, opsz, wdth),
    fontWeight = FontWeight(weight),
    fontSize = sizeSp.sp,
    lineHeight = lineSp.sp,
    letterSpacing = tracking,
    fontFeatureSettings = TNUM,
)

val CaliTypography = Typography(
    displayLarge = style(57, 60, 650, (-0.03).em, opsz = 72f, wdth = 92f),
    displayMedium = style(45, 52, 600, (-0.01).em, opsz = 48f),
    displaySmall = style(36, 40, 650, (-0.02).em, opsz = 48f, wdth = 92f),
    headlineMedium = style(28, 35, 600, (-0.01).em, opsz = 32f),
    titleLarge = style(22, 28, 600, opsz = 22f),
    titleMedium = style(16, 24, 600),
    bodyLarge = style(16, 24, 400),
    bodyMedium = style(14, 20, 500),
    bodySmall = style(12, 16, 400),
    labelLarge = style(14, 20, 600),
    labelMedium = style(12, 16, 500),
    labelSmall = style(11, 16, 500),
    // DESIGN.md không dùng ba vai trò dưới; giữ cỡ M3 nhưng cùng họ chữ để không lọt font hệ thống.
    headlineLarge = style(32, 40, 600),
    headlineSmall = style(24, 32, 600),
    titleSmall = style(14, 20, 600),
)

/** Biến thể giờ ăn / giờ uống: 600, wdth 90 (DESIGN.md Label Medium, Body Medium S15). */
val TimeLabelStyle: TextStyle = style(12, 16, 600, wdth = 90f)
