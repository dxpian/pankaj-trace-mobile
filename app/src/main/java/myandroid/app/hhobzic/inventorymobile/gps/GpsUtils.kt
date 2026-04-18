package myandroid.app.hhobzic.inventorymobile.gps


import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.gms.maps.model.LatLng
import retrofit2.HttpException
import java.util.Date
import java.util.concurrent.TimeUnit


/**
 * @description: Get the distance and and expected time from list of (lat, lng)
 * @return:
 *      pair(distance_meter, expected_time_sec)
 */
suspend fun getDistanceAndTravelTime(
    apiKey: String,
    locations: List<LatLng>
): Pair<Int, Int>? {
    if (locations.size < 2) return null

    // Origin: First location
    val origin = Uri.encode("${locations.first().latitude},${locations.first().longitude}")

    // Destination: Last location
    val destination = Uri.encode("${locations.last().latitude},${locations.last().longitude}")

    // Waypoints (intermediate points)
    val waypoints = locations.subList(1, locations.size - 1)
        .takeIf { it.isNotEmpty() }
        ?.joinToString("|") { Uri.encode("${it.latitude},${it.longitude}") }
    val departureTime = (System.currentTimeMillis() / 1000).toString()
    //Log.d(TAG, "departure time: $departureTime, the location: $locations")
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.directionsApi.getRoute(
                origin = origin,
                destination = destination,
                waypoints = null, // Pass null if there are no waypoints
                apiKey = apiKey,
                mode = "driving",
                departureTime = departureTime
            )
            if(response == null){
                //Log.d(TAG, "Invalid response")
                return@withContext null
            }
            //Log.d(TAG, "1: ${response.toString()}")

            // Parse the response
            val route = response.routes.firstOrNull() ?: return@withContext null
            //Log.d(TAG, "2")
            val totalDistance = route.legs.sumOf { it.distance.value }
            //Log.d(TAG, "3")
            val totalDuration = route.legs.sumOf { it.duration.value }
            //Log.d(TAG, "4")
            Pair(totalDistance, totalDuration) // Return distance and duration as Pair
        }
        catch (e: HttpException) {
            // Log the detailed error message
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API Error", "HTTP 400 Error: $errorBody")
            null
        }
        catch (e: Exception) {
            Log.e("API Error", "http exception in google directin api")
            e.printStackTrace()
            null
        }
    }
}


fun getElapsedTimeSec(from: Date, to: Date) : Int{
    val elapsedMillis = to.time - from.time

    // Convert milliseconds to seconds, minutes, and hours
    return (TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)).toInt()
}