package com.example.inventariotiendas.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// Un View que permite dibujar marcas de distintos tipos con colores
class AnnotationView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    // Cada marca guarda x, y y tipo ("Tienda", "Chupete", "Viento")
    private val marks = mutableListOf<Triple<Float, Float, String>>()
    private val paints = mapOf(
        "Tienda" to Paint().apply {
            color = 0xFF00FF00.toInt() // verde
            strokeWidth = 5f
            style = Paint.Style.STROKE
        },
        "Chupete" to Paint().apply {
            color = 0xFF0000FF.toInt() // azul
            strokeWidth = 5f
            style = Paint.Style.STROKE
        },
        "Viento" to Paint().apply {
            color = 0xFFFF0000.toInt() // rojo
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
    )

    // Tipo seleccionado actualmente; la Activity cambia este valor según checkbox
    var currentType: String = "Tienda"

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // Añade la marca usando el tipo actual
            marks.add(Triple(event.x, event.y, currentType))
            invalidate()
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        marks.forEach { (x, y, type) ->
            val paint = paints[type] ?: paints["Tienda"]!!
            val size = 30f
            canvas.drawLine(x - size, y - size, x + size, y + size, paint)
            canvas.drawLine(x - size, y + size, x + size, y - size, paint)
        }
    }

    fun clearMarks() {
        marks.clear()
        invalidate()
    }

    fun undoLastMark() {
        if (marks.isNotEmpty()) {
            marks.removeAt(marks.lastIndex)
            invalidate()
        }
    }

    /** Devuelve todas las marcas como List<Map<String, Any>> para Firestore */
    fun getMarksRelative(): List<Map<String, Any>> =
        marks.map { (x, y, type) ->
            mapOf(
                "x" to (x / width).toDouble(),
                "y" to (y / height).toDouble(),
                "tipo" to type
            )
        }

    /** Carga marcas desde List<Map<String, Any>> de Firestore */
    fun setMarksRelative(rel: List<Map<String, Any>>) {
        marks.clear()
        rel.forEach { m ->
            val x = (m["x"] as? Double)?.toFloat() ?: return@forEach
            val y = (m["y"] as? Double)?.toFloat() ?: return@forEach
            val type = m["tipo"] as? String ?: "Tienda"
            marks.add(Triple(x * width, y * height, type))
        }
        invalidate()
    }
}
