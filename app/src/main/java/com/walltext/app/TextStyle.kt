package com.walltext.app

import android.graphics.Color

/**
 * 6 种预设文字样式。
 *
 * 每种样式包含：
 * - textColor / overlayColor: 文字色 + 半透明底色蒙版（null 表示不画蒙版）
 * - textSizeSp: 文字字号（sp）
 * - shadowColor / shadowDx / shadowDy / shadowRadius: 阴影参数（null 表示无阴影）
 * - bold: 是否加粗
 * - verticalGravity: 文字在壁纸上的垂直位置（CENTER / TOP / BOTTOM）
 * - horizontalGravity: 水平位置（CENTER / START / END）
 * - lineSpacingMul: 行距乘数
 */
enum class TextStyle(
    val key: String,
    val displayName: String,
    val textColor: Int,
    val overlayColor: Int?,         // null = 不画蒙版
    val textSizeSp: Float,
    val shadowColor: Int?,          // null = 无阴影
    val shadowDx: Float,
    val shadowDy: Float,
    val shadowRadius: Float,
    val bold: Boolean,
    val verticalGravity: VerticalGravity,
    val horizontalGravity: HorizontalGravity,
    val lineSpacingMul: Float = 1.4f,
) {
    CLASSIC_WHITE(
        key = "classic_white",
        displayName = "经典白字",
        textColor = Color.WHITE,
        overlayColor = 0x66000000.toInt(),
        textSizeSp = 24f,
        shadowColor = 0x80000000.toInt(),
        shadowDx = 2f, shadowDy = 4f, shadowRadius = 6f,
        bold = false,
        verticalGravity = VerticalGravity.CENTER,
        horizontalGravity = HorizontalGravity.CENTER,
    ),
    ELEGANT_BLACK(
        key = "elegant_black",
        displayName = "优雅黑字",
        textColor = 0xFF1A1A1A.toInt(),
        overlayColor = 0x66F5E6D3.toInt(),         // 米色蒙版
        textSizeSp = 24f,
        shadowColor = 0x60FFFFFF.toInt(),
        shadowDx = 1f, shadowDy = 2f, shadowRadius = 4f,
        bold = false,
        verticalGravity = VerticalGravity.CENTER,
        horizontalGravity = HorizontalGravity.CENTER,
    ),
    MINIMAL_GRAY(
        key = "minimal_gray",
        displayName = "极简灰字",
        textColor = 0xFFCCCCCC.toInt(),
        overlayColor = null,
        textSizeSp = 20f,
        shadowColor = 0x40000000.toInt(),
        shadowDx = 1f, shadowDy = 1f, shadowRadius = 3f,
        bold = false,
        verticalGravity = VerticalGravity.TOP,
        horizontalGravity = HorizontalGravity.START,
    ),
    BOLD_LARGE(
        key = "bold_large",
        displayName = "醒目大字",
        textColor = Color.WHITE,
        overlayColor = 0xAA000000.toInt(),
        textSizeSp = 36f,
        shadowColor = 0xA0000000.toInt(),
        shadowDx = 3f, shadowDy = 6f, shadowRadius = 8f,
        bold = true,
        verticalGravity = VerticalGravity.CENTER,
        horizontalGravity = HorizontalGravity.CENTER,
    ),
    SOFT_WARM(
        key = "soft_warm",
        displayName = "柔和暖色",
        textColor = 0xFFFFE4B5.toInt(),            // 暖米色
        overlayColor = 0x66D2B48C.toInt(),         // 暖米色蒙版
        textSizeSp = 22f,
        shadowColor = 0x50000000.toInt(),
        shadowDx = 1f, shadowDy = 2f, shadowRadius = 3f,
        bold = false,
        verticalGravity = VerticalGravity.CENTER,
        horizontalGravity = HorizontalGravity.CENTER,
    ),
    DARK_MODE(
        key = "dark_mode",
        displayName = "夜间模式",
        textColor = Color.WHITE,
        overlayColor = 0xCC000000.toInt(),
        textSizeSp = 22f,
        shadowColor = null,
        shadowDx = 0f, shadowDy = 0f, shadowRadius = 0f,
        bold = false,
        verticalGravity = VerticalGravity.BOTTOM,
        horizontalGravity = HorizontalGravity.CENTER,
    );

    companion object {
        /** SharedPreferences 保存的默认值 */
        const val PREFS_NAME = "walltext_prefs"
        const val KEY_LAST_STYLE = "last_style_key"

        fun fromKey(key: String?): TextStyle =
            values().firstOrNull { it.key == key } ?: CLASSIC_WHITE

        fun names(): List<String> = values().map { it.displayName }
    }
}

enum class VerticalGravity { TOP, CENTER, BOTTOM }
enum class HorizontalGravity { START, CENTER, END }