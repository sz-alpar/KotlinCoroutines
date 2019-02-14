package net.gini.android.kotlincoroutines

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Size
import android.view.View

/**
 * Created by Alpar Szotyori on 12.02.2019.
 *
 * Copyright (c) 2019 Gini GmbH.
 */

class FieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var fieldSize: Size = Size(0, 0)
    var ants: List<Ant> = listOf()

    private val antPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#009edc")
        strokeWidth = 5F
    }

    private val borderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#e30b5d")
        strokeWidth = 2F
    }
    private val borderRect: Rect
        get() {
            return Rect(0, 0, fieldSize.width, fieldSize.height)
        }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas.run {
            canvas?.drawRect(borderRect, borderPaint)

            for (ant in ants) {
                canvas?.drawRect(ant.boundsRect, antPaint)
            }
        }
    }
}