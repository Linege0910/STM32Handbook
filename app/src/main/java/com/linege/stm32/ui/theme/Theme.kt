package com.linege.stm32.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 深色配色（默认）
 * 参考 GitHub Dark + 科技蓝
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF79C0FF),
    onPrimary = Color(0xFF00315C),
    primaryContainer = Color(0xFF1F6FEB),
    onPrimaryContainer = Color(0xFFD6E9FF),

    secondary = Color(0xFFB8C9E0),
    onSecondary = Color(0xFF23303F),
    secondaryContainer = Color(0xFF2C3B4E),
    onSecondaryContainer = Color(0xFFD4E3F7),

    tertiary = Color(0xFFD2A8FF),
    onTertiary = Color(0xFF3B1D5C),
    tertiaryContainer = Color(0xFF522B7D),
    onTertiaryContainer = Color(0xFFEEDBFF),

    error = Color(0xFFFF7B72),
    onError = Color(0xFF4C0A06),
    errorContainer = Color(0xFF7A1A13),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3),

    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF9DA7B3),

    surfaceContainerLowest = Color(0xFF0A0D12),
    surfaceContainerLow = Color(0xFF11161D),
    surfaceContainer = Color(0xFF161B22),
    surfaceContainerHigh = Color(0xFF1C2128),
    surfaceContainerHighest = Color(0xFF22272E),

    outline = Color(0xFF3D444D),
    outlineVariant = Color(0xFF2A3038),

    inverseSurface = Color(0xFFE6EDF3),
    inverseOnSurface = Color(0xFF161B22),
    inversePrimary = Color(0xFF1F6FEB),

    scrim = Color(0xFF000000)
)

/**
 * 浅色配色
 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3D),

    secondary = Color(0xFF545F71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8E3F8),
    onSecondaryContainer = Color(0xFF111C2B),

    tertiary = Color(0xFF6D5677),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6D9FF),
    onTertiaryContainer = Color(0xFF271430),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFF7F9FC),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F4F9),
    surfaceContainer = Color(0xFFECF0F5),
    surfaceContainerHigh = Color(0xFFE6EAEF),
    surfaceContainerHighest = Color(0xFFE0E4EA),

    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF),

    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFFA9C7FF),

    scrim = Color(0xFF000000)
)

/**
 * 代码语法高亮配色
 *
 * 深色/浅色各一套，保证任何主题下都有足够对比度
 */
data class CodePalette(
    /** 普通文本：变量名、运算符、括号 */
    val plain: Color,
    /** 括号（单独上色，便于配对识别） */
    val bracket: Color,
    /** 关键字 if/while/for… */
    val keyword: Color,
    /** 类型 int/uint8_t… */
    val type: Color,
    /** 函数名（含自定义函数） */
    val function: Color,
    /** 字符串 */
    val string: Color,
    /** 注释 */
    val comment: Color,
    /** 数字 */
    val number: Color,
    /** 预处理指令 */
    val preprocessor: Color,
    /** 行号 */
    val lineNo: Color,
    /** 代码区背景 */
    val background: Color
)

/** 深色：适配 #11161D 背景 */
val DarkCodePalette = CodePalette(
    plain = Color(0xFFE6EDF3),
    bracket = Color(0xFF7EE787),
    keyword = Color(0xFFFF8A80),
    type = Color(0xFFFFB86C),
    function = Color(0xFFD2A8FF),
    string = Color(0xFFA5D6FF),
    comment = Color(0xFF9DA7B3),
    number = Color(0xFF79C0FF),
    preprocessor = Color(0xFFFF9B95),
    lineNo = Color(0xFF8B949E),
    background = Color(0xFF11161D)
)

/** 浅色：适配 #F6F8FA 背景 */
val LightCodePalette = CodePalette(
    plain = Color(0xFF1F2328),
    bracket = Color(0xFF116329),
    keyword = Color(0xFFCF222E),
    type = Color(0xFF953800),
    function = Color(0xFF6639BA),
    string = Color(0xFF0A3069),
    comment = Color(0xFF57606A),
    number = Color(0xFF0550AE),
    preprocessor = Color(0xFFB35900),
    lineNo = Color(0xFF656D76),
    background = Color(0xFFF6F8FA)
)

/** 当前主题对应的代码配色 */
@Composable
fun currentCodePalette(): CodePalette =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        DarkCodePalette
    } else {
        LightCodePalette
    }

@Composable
fun STM32Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
