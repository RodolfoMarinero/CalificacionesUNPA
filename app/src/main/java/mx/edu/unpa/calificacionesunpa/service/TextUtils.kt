package mx.edu.unpa.calificacionesunpa.service

import android.content.Context
import android.graphics.Paint
import android.util.DisplayMetrics

object TextUtils {


    /** Mide en px el ancho que ocupa el texto con un tamaño en sp dado. */
    fun measureTextWidthPx(context: Context, text: String, textSizeSp: Float): Float {
        val dm: DisplayMetrics = context.resources.displayMetrics
        val textSizePx = textSizeSp * dm.scaledDensity
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSizePx
        }
        return paint.measureText(text)
    }

    /** Devuelve el porcentaje (0–100) del ancho de pantalla que ocupa el texto. */
    fun textWidthPercentage(context: Context, text: String, textSizeSp: Float): Float {
        val dm: DisplayMetrics = context.resources.displayMetrics
        val screenWidth = dm.widthPixels.toFloat()
        val textWidth = measureTextWidthPx(context, text, textSizeSp)
        return (textWidth / screenWidth) * 100f
    }

    /**
     * Si el texto ocupa más de [maxPercent]% de la pantalla, lo parte en dos líneas
     * intentando partir en el espacio más cercano al medio.
     */
    fun splitTextIfTooLong(
        context: Context,
        text: String,
        textSizeSp: Float,
        maxPercent: Float = 60f
    ): String {
        val pct = textWidthPercentage(context, text, textSizeSp)
        if (pct <= maxPercent) return text

        val mid = text.length / 2
        val before = text.lastIndexOf(' ', startIndex = mid).takeIf { it > 0 }
        val after = text.indexOf(' ', startIndex = mid).takeIf { it > 0 }

        val splitPos = when {
            before != null && after != null -> if (mid - before <= after - mid) before else after
            before != null                    -> before
            after != null                     -> after
            else                               -> mid
        }

        val first = text.substring(0, splitPos).trimEnd()
        val second = text.substring(splitPos).trimStart()
        return "$first\n$second"
    }
}
