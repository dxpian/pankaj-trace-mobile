package myandroid.app.hhobzic.inventorymobile.utility

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.Language
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LED_STATUS{
    companion object Constant{
        const val INACTIVE = 0
        const val ACTIVE = 1
        const val COMPLETED = 2
    }
}

data class MapRouteComponents(
    val reached: MutableList<LatLng> = mutableListOf(),
    val unreached: MutableList<LatLng> = mutableListOf(),
    val returns: MutableList<LatLng> = mutableListOf(),
    val bound: LatLngBounds? = null
)

/*
get the label matched with status
params:
    state: Delivering state
return (string, LED_STATUS)
 */
fun getDeliverStatusLabel(stateLabels: Array<String>,  state: DeliveryState?): Pair<String, Int>{
    if(state == null){
        return Pair("Idle", LED_STATUS.INACTIVE)
    }
    if( stateLabels.size < TRACK_STATUS.STATE_CNT.code
        || state.status < 0
        || state.status >= TRACK_STATUS.STATE_CNT.code )
        return Pair(stateLabels[TRACK_STATUS.UNKNOWN.code], LED_STATUS.INACTIVE )

    //Log.d("AppUtils", "check status of deliver task: ${state.status}")

    return when(state.status){
        TRACK_STATUS.INIT.code->
            Pair(stateLabels[state.status],  LED_STATUS.ACTIVE) // idle
        TRACK_STATUS.PROGRESS.code->
            Pair(stateLabels[state.status], LED_STATUS.ACTIVE)// onDuty
        TRACK_STATUS.LOADING.code->
            Pair(stateLabels[state.status], LED_STATUS.ACTIVE) // loading
        TRACK_STATUS.DELIVERING.code->
            Pair(stateLabels[state.status], LED_STATUS.ACTIVE)// delivering
        TRACK_STATUS.RETURNING.code->
            Pair(stateLabels[state.status], LED_STATUS.ACTIVE) // returning
        TRACK_STATUS.COMPLETED.code->
            Pair(stateLabels[state.status], LED_STATUS.COMPLETED) // completed
        else->
            Pair(stateLabels[TRACK_STATUS.UNKNOWN.code], LED_STATUS.INACTIVE)
    }
}

fun getGreetingBasedOnTime(greetings: Array<String>): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    if(greetings.isEmpty() || greetings.size < 2)
        return ""
    return when {
        hour in 5..11 -> greetings[0]
        hour in 12..17 -> greetings[1]
        else -> greetings[2]
    }
}

//  convert time string into miliseconds
fun convertTimestrToMillis(timeString: String): Long {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    val date: Date? = dateFormat.parse(timeString)
    return date?.time ?: 0L  // Convert Date to milliseconds
}


fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    return sdf.format(Date()) // Get the current time
}


fun saveLanguagePreference(context: Context, languageCode: String) {
    val sharedPreferences = context.getSharedPreferences(AppMacro.SHAREDPREF_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(AppMacro.KEY_LANGUAGE, languageCode)
    editor.apply()
}

fun loadLanguagePreference(context: Context): String {
    val sharedPreferences = context.getSharedPreferences(AppMacro.SHAREDPREF_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getString(AppMacro.KEY_LANGUAGE, Language.JPN) ?: Language.ENG  // Default to English if none set
}