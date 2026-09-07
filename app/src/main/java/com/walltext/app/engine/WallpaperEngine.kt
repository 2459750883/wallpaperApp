package com.walltext.app.engine

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.walltext.app.HorizontalGravity
import com.walltext.app.TextStyle
import com.walltext.app.VerticalGravity
import kotlin.math.max

/**
 * 渲染文字到壁纸。
 *
 * 工作流程：
 * 1. 取当前系统壁纸作为底图
 * 2. 按样式参数画半透明蒙版（可选）
 * 3. 用 StaticLayout 把文字折行排版
 * 4. 调用 WallpaperManager.setBitmap 设置为系统壁纸
 */
object WallpaperEngine {

    private const val TAG = "WallpaperEngine"
    private const val PADDING_PX = 96     // 96px ≈ 32dp on hdpi

    /**
     * 渲染并设置壁纸。在 IO 线程调用。
     *
     * @return true 表示设置成功
     */
    fun renderAndSet(context: Context, text: String, style: TextStyle): Boolean {
        if (text.isBlank()) return false

        val wm = WallpaperManager.getInstance(context)
        // 取当前系统壁纸的尺寸（一些设备可能没有 wallpaper service）
        val (width, height) = try {
            val intrinsic = wm.drawable?.intrinsicWidth ?: 0
            val intrinsicH = wm.drawable?.intrinsicHeight ?: 0
            if (intrinsic > 0 && intrinsicH > 0) {
                intrinsic to intrinsicH
            } else {
                val dm = context.resources.displayMetrics
                dm.widthPixels to (dm.widthPixels * 16 / 9)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Couldn't read wallpaper size, falling back to display", t)
            val dm = context.resources.displayMetrics
            dm.widthPixels to (dm.widthPixels * 16 / 9)
        }

        val bitmap = render(width, height, text, style) ?: return false
        return try {
            // Android 7.0+ 支持 FLAG_SYSTEM | FLAG_LOCKSCREEN 同时设置
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCKSCREEN
            } else {
                WallpaperManager.FLAG_SYSTEM
            }
            wm.setBitmap(bitmap, null, true, flags)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to set wallpaper", t)
            false
        }
    }

    /**
     * 只渲染出 Bitmap 用于预览，不设置壁纸。
     */
    fun render(width: Int, height: Int, text: String, style: TextStyle): Bitmap? {
        if (text.isBlank() || width <= 0 || height <= 0) return null

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 1. 背景：根据 overlay 决定画纯色蒙版或渐变
        drawBackground(canvas, width, height, style)

        // 2. 文字
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        paint.color = style.textColor
        paint.textSize = spToPx(style.textSizeSp)
        paint.isAntiAlias = true
        paint.typeface = if (style.bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        if (style.shadowColor != null) {
            paint.setShadowLayer(
                style.shadowRadius,
                style.shadowDx,
                style.shadowDy,
                style.shadowColor
            )
        }

        val availableWidth = width - PADDING_PX * 2
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, availableWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, style.lineSpacingMul)
            .setIncludePad(false)
            .build()

        // 3. 按 verticalGravity 计算起始 y
        val totalHeight = layout.height
        val startY = when (style.verticalGravity) {
            VerticalGravity.TOP -> PADDING_PX
            VerticalGravity.CENTER -> max(PADDING_PX, (height - totalHeight) / 2)
            VerticalGravity.BOTTOM -> height - totalHeight - PADDING_PX
        }

        canvas.save()
        canvas.translate(PADDING_PX.toFloat(), startY.toFloat())
        layout.draw(canvas)
        canvas.restore()

        return bmp
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int, style: TextStyle) {
        val overlay = style.overlayColor ?: return
        val isSingleColor = (overlay ushr 24) and 0xFF in 0..254  // 有透明度才需要画
        // 简单策略：所有 overlay 都是单色蒙版（包含 alpha）
        val paint = Paint().apply { color = overlay }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        // 极简灰字特殊处理：顶到底渐变（弱化顶部底图干扰）
        if (style == TextStyle.MINIMAL_GRAY) {
            val gradientPaint = Paint()
            gradientPaint.shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.TRANSPARENT, 0x66000000,
                Shader.TileMode.CLAMP
            )
            // 叠加到底部
            canvas.drawRect(0f, height * 0.5f, width.toFloat(), height.toFloat(), gradientPaint)
        }
    }

    private fun spToPx(sp: Float): Float {
        // 简化为按 1.0 系数；WallpaperEngine 用屏幕 sp->px 默认 1.5 系数偏小
        // 实际渲染由调用方传 context.resources 更好；此处取常见 1.5
        return sp * 1.5f * 1.6f  // 1.5 (sp-to-px baseline) * 1.6 (中屏密度修正)
    }
}