package myandroid.app.hhobzic.inventorymobile.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import myandroid.app.hhobzic.inventorymobile.R

class AppForegroundService : Service() {
    private val TAG = "AppForegroundService"
    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    val channelId = "inventory_tracker_channel_1.0.0"
    val channelName = "Inventory tracker"
    val channelDescription = "Description of your channel"
    val notifyTitle = "Inventory tracker"
    val notifyContent  = "Inventory tracker is running now"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create a notification for the foreground service
        startForegroundServiceWithNotification()

        // Keep the app active
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d(TAG, "start command of foreground service")
        return START_STICKY
    }


    // Method to start location updates with a callback passed from the Activity
    fun startLocationUpdates(activityLocationCallback: LocationCallback, intervalMilisec: Long) {
        this.locationCallback = activityLocationCallback

        Log.d(TAG, "start location updating server")
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMilisec).apply {
            setMinUpdateIntervalMillis(intervalMilisec) // 5 seconds
        }.build()

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "No permission to request location service")
            return
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)

        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                Log.d(TAG, "Location settings are satisfied.")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Location settings are NOT satisfied: $exception")
            }
        fusedLocationClient.requestLocationUpdates(locationRequest, this.locationCallback!!, Looper.getMainLooper())
        Log.d(TAG, "Succuess to update the gps request interval time into $intervalMilisec miliseconds")
    }

     fun restartLocationUpdates(intervalMiliseconds: Long){
        fusedLocationClient.removeLocationUpdates(this.locationCallback!!).addOnCompleteListener {
            // Once removed, start requesting updates with the new interval
            startLocationUpdates(this.locationCallback!!, intervalMiliseconds)
        }
    }

    // Method to stop location updates
    private fun stopLocationUpdates() {
        if(locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()  // Stop location updates when the service is destroyed
    }

    // Function to create notification channel
    private fun createNotificationChannel() {

        // Notification channels are only available in Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            // Register the channel with the system
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        // Create the notification channel if running on Android O or higher
        createNotificationChannel()
        // Create the notification
        val notification = NotificationCompat.Builder(this, this.channelId)
            .setContentTitle(notifyTitle)
            .setContentText(notifyContent)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Start the foreground service
        startForeground(1, notification)
    }

    // Class used for the client Binder
    inner class LocalBinder : Binder() {
        fun getService(): AppForegroundService = this@AppForegroundService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }


}

