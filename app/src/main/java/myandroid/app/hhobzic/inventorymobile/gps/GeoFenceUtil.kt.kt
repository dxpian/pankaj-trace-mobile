package myandroid.app.hhobzic.inventorymobile.gps

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonLineString
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.android.gms.maps.GoogleMap

object GeoFenceUtil {

    /**
     * Parse GeoJSON and return polygon coordinates.
     * Works for Polygon and LineString (closed to polygon).
     */
    fun parsePolygonFromGeoJson(geoJsonString: String): List<LatLng> {
        val geoJsonObject = JSONObject(geoJsonString)
        val features = geoJsonObject.getJSONArray("features")

        if (features.length() == 0) return emptyList()

        val feature = features.getJSONObject(0) // take first feature (or loop if multiple)
        val geometry = feature.getJSONObject("geometry")
        val type = geometry.getString("type")

        return when (type) {
            "Polygon" -> {
                val coordinates = geometry.getJSONArray("coordinates").getJSONArray(0)
                parseCoordinates(coordinates)
            }
            "LineString" -> {
                val coordinates = geometry.getJSONArray("coordinates")
                val coords = parseCoordinates(coordinates).toMutableList()

                // Close polygon by repeating first point at the end
                if (coords.isNotEmpty() && coords.first() != coords.last()) {
                    coords.add(coords.first())
                }
                coords
            }
            else -> emptyList()
        }
    }

    private fun parseCoordinates(array: JSONArray): List<LatLng> {
        val list = mutableListOf<LatLng>()
        for (i in 0 until array.length()) {
            val coord = array.getJSONArray(i)
            val lng = coord.getDouble(0)
            val lat = coord.getDouble(1)
            list.add(LatLng(lat, lng))
        }
        return list
    }


    /**
     * Point-in-polygon check using ray casting
     */
    fun isPointInPolygon(point: LatLng, polygon: List<LatLng>?): Boolean {
        if(polygon.isNullOrEmpty() || polygon.size<= 2){
            return false
        }
        var intersectCount = 0
        for (j in 0 until polygon.size - 1) {
            if (rayCastIntersect(point, polygon[j], polygon[j + 1])) {
                intersectCount++
            }
        }
        return (intersectCount % 2 == 1)
    }

    private fun rayCastIntersect(point: LatLng, vertA: LatLng, vertB: LatLng): Boolean {
        val aY = vertA.latitude
        val bY = vertB.latitude
        val aX = vertA.longitude
        val bX = vertB.longitude
        val pY = point.latitude
        val pX = point.longitude

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY) || (aX < pX && bX < pX)) {
            return false
        }
        val m = (bY - aY) / (bX - aX)
        val bee = -aX * m + aY
        val x = (pY - bee) / m
        return x > pX
    }
}
