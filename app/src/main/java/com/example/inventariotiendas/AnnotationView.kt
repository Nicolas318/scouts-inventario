package com.example.inventariotiendas.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// Un simple View que guarda puntos donde el usuario toca
class AnnotationView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    private val marks = mutableListOf<Pair<Float,Float>>()
    private val paint = Paint().apply {
        color = 0xFFFF0000.toInt()   // rojo por defecto
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            marks.add(event.x to event.y)
            invalidate()
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        marks.forEach { (x,y) ->
            // dibuja una X
            val size = 30f
            canvas.drawLine(x - size, y - size, x + size, y + size, paint)
            canvas.drawLine(x - size, y + size, x + size, y - size, paint)
        }
    }

    fun clearMarks() {
        marks.clear()
        invalidate()
    }

    /** Elimina la última marca y repinta */
    fun undoLastMark() {
        if (marks.isNotEmpty()) {
            marks.removeAt(marks.lastIndex)
            invalidate()
        }
    }

    /** Devuelve todas las marcas como pares (x/width, y/height) */
    fun getMarksRelative(): List<Pair<Float, Float>> =
        marks.map { (x, y) -> x / width to y / height }

    /** Carga marcas relativas [0..1] escalándolas a píxeles */
    fun setMarksRelative(rel: List<Pair<Float, Float>>) {
        marks.clear()
        rel.forEach { (xr, yr) ->
            marks.add(xr * width to yr * height)
        }
        invalidate()
    }
}
