package myandroid.app.hhobzic.inventorymobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class TripTimeCustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progressPaint: Paint = Paint()
    private var backgroundPaint: Paint = Paint()
    private var strokeWidth = 20f // Stroke width of the progress bar
    private var progressAngle = 0f

    init {
        progressPaint.color = ContextCompat.getColor(context, R.color.appColor)
        // Color for progress
        progressPaint.strokeWidth = strokeWidth
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeCap = Paint.Cap.SQUARE

        backgroundPaint.color = ContextCompat.getColor(context, R.color.lightappColor)// Background color
        backgroundPaint.strokeWidth = strokeWidth
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeCap = Paint.Cap.SQUARE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height/2f // Align the center of the circle at the bottom of the view
        val radius = (width - strokeWidth) / 2f

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw progress arc
        val startAngle = 180f // Start angle for progress (left side)
        val sweepAngle = 120f// Angle of visible part (180 degrees for half circle)
        canvas.drawArc(
            strokeWidth / 2,
            centerY - radius,
            width - strokeWidth / 2,
            centerY + radius,
            startAngle,
            sweepAngle,
            false,
            progressPaint
        )
    }

    fun setProgress(progressAngle: Float) {
        this.progressAngle = progressAngle
        invalidate()
    }
}
