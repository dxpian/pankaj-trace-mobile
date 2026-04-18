package myandroid.app.hhobzic.inventorymobile.gps

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class WebRouteData(
    val points: List<RoutePoint>
)

data class RoutePoint(
    val pos: List<Double>,
    val time: String
)

/**
 * Wrapper class to get the virtual GPS location to simulate the truck movement
 */
class VirtualGpsSensor(private val context: Context) {
    private val TAG = "VirtualGpsSensor"
    private var _cntCourse = 0
    private var _countloop = 0
    private val courseDeliver: List<LatLng> = listOf(
        LatLng(39.64200700,141.1198527),
        LatLng(39.64200700,141.1198527),
    )

    private val course2Deliver: List<LatLng> = listOf(
        LatLng(38.25029	,140.95854),
        LatLng(38.249744	,140.9854 ),

    )

    private val courseNgbDeliver: List<LatLng> = listOf(
        LatLng(39.65861, 141.1136),  // Rest at エイトウイング盛岡
        LatLng(39.65861, 141.1136),  // Rest at エイトウイング盛岡
    )


    private var courseJson: MutableList<LatLng> = mutableListOf()

    fun loadJsondata(){
        Log.d(TAG, "parse JSON file")
        parseRouteFromJson("route.json")
        Log.d(TAG, "Success to parse JSON file")
    }


    //get gps location for general purpose
    fun getGpsLocation(): LatLng{
        if(_countloop < 1) {
            _cntCourse += 1
            if(_cntCourse >= courseJson.size) {
                _cntCourse = courseJson.size - 1
            }
            val pt = courseJson[_cntCourse]
            Log.d(TAG, "current GPS simulation counter: $_cntCourse/${courseJson.size}")
            return pt
        }
        return courseJson[courseJson.size - 1]
    }
    //get gps location to simulation neighbored DC and company
    fun getSimulationNeighborGpsLocation(): LatLng{
        Log.d(TAG, "Current GPS virtual sensor state: $_countloop:$_cntCourse")

        if(_countloop < 1) // iterate the same loop 2 times
        {
            _cntCourse ++
            if(_cntCourse >= courseNgbDeliver.size) {
                _cntCourse = 0
                _countloop += 1
            }
            return courseNgbDeliver[_cntCourse]
        }
        return courseNgbDeliver[0]

    }

    fun resetSimulator(){
        Log.d(TAG, "Reset simulator index")
        _cntCourse = 0
        _countloop = 0
    }

    private fun parseRouteFromJson(fileName: String) {
        try {
            val jsonStr = context.assets.open(fileName).bufferedReader().use { it.readText() }

            // Parse JSON list
            val listType = object : TypeToken<List<WebRouteData>>() {}.type
            val routeDataList: List<WebRouteData> = Gson().fromJson(jsonStr, listType)

            // Clear previous data
            courseJson.clear()

            // Extract all coordinates from all routes
            for (route in routeDataList) {
                for (point in route.points) {
                    if (point.pos.size == 2) {
                        val lat = point.pos[0]
                        val lng = point.pos[1]
                        courseJson.add(LatLng(lat, lng))
                    }
                }
            }

            Log.d(TAG, "Success: Parsed ${courseJson.size} total route points.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse route JSON: ${e.message}", e)
        }
    }

}