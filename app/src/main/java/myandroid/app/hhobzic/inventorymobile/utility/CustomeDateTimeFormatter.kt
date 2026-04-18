package myandroid.app.hhobzic.inventorymobile.utility

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class TimeValueFormatter(private val referenceTimestamp: Long) : ValueFormatter() {

    //private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Customize the format
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // Customize the format
    // convert tick value  to date-time. One tick is 1 minutes
    override fun getFormattedValue(value_tick: Float): String {
        // Convert the value to a timestamp
        val timestamp = value_tick.toLong() * 1000  + referenceTimestamp
        return dateFormat.format(Date(timestamp))
    }
}

class DateValueFormatter(private val referenceTimestamp: Long) : ValueFormatter() {

    //private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Customize the format
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Customize the format
    // convert tick value  to date-time. One tick is 1  day
    override fun getFormattedValue(value_tick: Float): String {
        // Convert the value to a timestamp
        val timestamp = value_tick.toLong() * 24 * 60 * 60 * 1000 + referenceTimestamp
        return dateFormat.format(Date(timestamp))
    }
}


class SpeedValueFormatter(private val unit: String) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return when (unit) {
            "km/h" -> String.format("%d km/h", value.toInt())
            "m/s"  -> String.format("%.1f m/s", value / 3.6f) // 1 km/h = 0.277... m/s
            else   -> value.toString()
        }
    }
}

