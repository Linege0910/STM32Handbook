package com.linege.stm32.data

import android.content.Context

/**
 * 本地设置（SharedPreferences）
 */
object Settings {

    private const val PREF = "stm32_settings"

    /** 主题模式 */
    private const val KEY_THEME = "theme_mode"

    /** 代码字号 */
    private const val KEY_FONT = "code_font_size"

    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    fun getThemeMode(ctx: Context): Int =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, THEME_SYSTEM)

    fun setThemeMode(ctx: Context, mode: Int) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, mode).apply()
    }

    /** 代码字号（sp），默认 12.5 */
    fun getCodeFontSize(ctx: Context): Float =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getFloat(KEY_FONT, 12.5f)

    fun setCodeFontSize(ctx: Context, size: Float) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_FONT, size).apply()
    }
}
