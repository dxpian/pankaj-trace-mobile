package myandroid.app.hhobzic.inventorymobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val points = mutableListOf<Pair<Float, Float>>()

    init {
        pointPaint.color = context.getColor(R.color.lightbrown)
        pointPaint.style = Paint.Style.FILL
        linePaint.color = context.getColor(R.color.appColor)
        linePaint.strokeWidth = 5f
    }

    fun setPoints(newPoints: List<Pair<Float, Float>>) {
        points.clear()
        points.addAll(newPoints)
        invalidate()
    }

    fun updatePoint(index: Int, yValue: Float) {
        if (index in points.indices) {
            points[index] = Pair(points[index].first, yValue)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.size < 2) return

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()

        val xInterval = width / (points.size - 1)

        // Draw lines
        for (i in 0 until points.size - 1) {
            val startX = i * xInterval
            val startY = height * (1 - points[i].second / 100f)
            val endX = (i + 1) * xInterval
            val endY = height * (1 - points[i + 1].second / 100f)
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }

        // Draw points
        points.forEach { (x, y) ->
            val pointX = x * width / 100f
            val pointY = height * (1 - y / 100f)
            canvas.drawCircle(pointX, pointY, 10f, pointPaint)
        }
    }
}
