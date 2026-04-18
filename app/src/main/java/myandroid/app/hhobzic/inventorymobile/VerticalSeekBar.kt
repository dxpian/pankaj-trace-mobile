package myandroid.app.hhobzic.inventorymobile

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar

class VerticalSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    init {
        rotation = -90f // Rotate the seek bar 90 degrees counter-clockwise to make it vertical
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth) // Swap width and height to make it vertical
    }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(90f) // Rotate the canvas 90 degrees clockwise to compensate for the rotation of the seek bar
        canvas.translate(0f, -width.toFloat()) // Translate the canvas vertically to make it align properly
        super.onDraw(canvas)
    }
}
