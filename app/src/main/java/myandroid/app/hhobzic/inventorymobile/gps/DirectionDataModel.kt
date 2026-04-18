package myandroid.app.hhobzic.inventorymobile.gps


import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>
)

data class Route(
    @SerializedName("legs") val legs: List<Leg>
)

data class Leg(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration
)

data class Distance(
    @SerializedName("text") val text: String, // e.g., "5.3 km"
    @SerializedName("value") val value: Int   // Distance in meters
)

data class Duration(
    @SerializedName("text") val text: String, // e.g., "15 mins"
    @SerializedName("value") val value: Int   // Duration in seconds
)
