package myandroid.app.hhobzic.inventorymobile.utility

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class CustomScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Get the screen height
        val screenHeight = resources.displayMetrics.heightPixels

        // Calculate the height of the content
        val contentHeight = getChildAt(0).measuredHeight

        // Set the height of the ScrollView based on the content height
        if (contentHeight < screenHeight) {
            setMeasuredDimension(measuredWidth, screenHeight)
        } else {
            setMeasuredDimension(measuredWidth, contentHeight)
        }
    }
}
