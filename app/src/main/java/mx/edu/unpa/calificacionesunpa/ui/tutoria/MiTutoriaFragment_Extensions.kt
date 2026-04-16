// ══════════════════════════════════════════════════════
// ARCHIVO: MiTutoriaFragment_Extensions.kt
// Helpers y extensiones para el Fragment unificado
// ══════════════════════════════════════════════════════

package mx.edu.unpa.calificacionesunpa.ui.tutoria

import android.graphics.PorterDuff
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import mx.edu.unpa.calificacionesunpa.R
import mx.edu.unpa.calificacionesunpa.data.model.DocenteDisponible

// ── Extensiones de LinearLayout para children ─────────
fun LinearLayout.children(): List<View> =
    (0 until childCount).map { getChildAt(it) }

// ── Colores dinámicos para barra de slots ─────────────
fun ProgressBar.setSlotColor(actual: Long, max: Long) {
    val pct = actual.toFloat() / max.toFloat()
    val colorRes = when {
        pct >= 1f  -> R.color.slot_red
        pct >= 0.8f-> R.color.slot_amber
        else       -> R.color.slot_green
    }
    val color = ContextCompat.getColor(context, colorRes)
    progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
}

// ── Texto y color de slots ─────────────────────────────
fun TextView.setSlotText(actual: Long, max: Long) {
    text = when {
        actual >= max -> "$actual / $max · Sin espacio"
        else          -> "$actual / $max tutorados"
    }
    val colorRes = when {
        actual >= max  -> R.color.slot_red
        actual >= max * 0.8 -> R.color.slot_amber
        else           -> R.color.text_secondary
    }
    setTextColor(ContextCompat.getColor(context, colorRes))
}

// ── Iniciales desde nombre completo ───────────────────
fun iniciales(nombre: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.size >= 2 -> "${partes[0].first()}${partes[1].first()}"
        partes.size == 1 -> partes[0].take(2)
        else             -> "??"
    }.uppercase()
}

