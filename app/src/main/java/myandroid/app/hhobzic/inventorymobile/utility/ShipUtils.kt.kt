package myandroid.app.hhobzic.inventorymobile.utility

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @description: Get the distance between 2 gps location
 */
 fun haversineDistanceMeter(loc1: LatLng, loc2: LatLng): Double {
    val R = 6371.0 // Radius of Earth in kilometers
    val latDistance = Math.toRadians(loc2.latitude - loc1.latitude)
    val lonDistance = Math.toRadians(loc2.longitude - loc1.longitude)
    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc1.latitude)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c * 1000 // Distance in kilometers
}