package myandroid.app.hhobzic.inventorymobile.gps

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger
import myandroid.app.hhobzic.inventorymobile.utility.haversineDistanceMeter
import java.util.Date
import kotlin.math.max
import java.text.SimpleDateFormat
import java.util.*

// class to monitoring the location of driver.
// this class is used to trigger the start/stop event of truck

interface LocationEventInterface{
    // truck reached to any location
    fun onReachedEntity(entity: GpsEntity)

    // truck leave from location
    fun onLeaveEntity(entity: GpsEntity)

    // truck parked on any location more than limited time
    suspend fun onParkEntity(entity: GpsEntity?, time: Date)

}

class LocMonitor(private val locEventCallback: LocationEventInterface) {
    private val TAG = "LocationMonitor"
    private var deliverState: DeliveryState? = null
    private var initialized = false                      // setting is valid
    private var taskInitialized = false
    private var duty: Boolean = false                       // truck is in task status?

    private var timeLimit = 120                             // limit time which can be considered as task is completed
    private var stopLoc: LatLng? = null                     // location where the truck stops
    private var stopTime: Date? = null                   // time where he truck stops
    private var cntLeaveLastNode = 0
    // check if truck is in duty
    fun isOnDuty(): Boolean{
        return this.duty
    }

    fun setTimeLimit(timeLimit: Int = 60 * 2){
        this.timeLimit = timeLimit
        AppLogger.d(TAG, "Update the track setting into $timeLimit")
    }

    fun updateTaskStatus(deliStatus: DeliveryState?){
        if(deliStatus == null){
            Log.d(TAG, "Invalid deliver status")
            return
        }
        this.initialized = true
        this.deliverState = deliStatus.copy()
        this.duty = deliStatus.status != TRACK_STATUS.COMPLETED.code
        if(this.duty){ // start deliver task
            this.stopLoc = null
            this.stopTime = null
            this.taskInitialized = true
        }
        else{
            this.cntLeaveLastNode = 0
        }
        return
    }

    fun getPreviousCompletedTime(): Date?{

        val strEndDate = this.deliverState?.endtime
        if(strEndDate.isNullOrEmpty())
        {
            return null
        }
        AppLogger.d(TAG, "End date of previous task is $strEndDate")

        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date: Date? = try {
            format.parse(strEndDate)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return date
    }

    fun getLeaveLastNodeCount(): Int{
        if(this.taskInitialized){ // At least first task is completed
            return this.cntLeaveLastNode
        }else{
            return max(this.cntLeaveLastNode, 1) // return non-zero if truck leaved node at the first time
        }

    }

    fun leaveFromLastNode(){
        this.cntLeaveLastNode ++
    }

    fun getTaskStartTime(): String{
        return this.deliverState?.startime ?:""
    }


    suspend fun updateLocation(loc: LatLng){
        if(!this.initialized){
            AppLogger.d(TAG, "Location manager isn't initialized")
            return
        }
        if(!this.duty){ // check if truck leaves from DC or LC
            AppLogger.d(TAG, "Check if truck leave the candidate entity")
            return
        }
        else{ // check if truck stops in one location for a long time
            AppLogger.d(TAG, "Check if truck completes the task in the current location")
            if(this.stopLoc == null){
                this.stopLoc  = loc
                this.stopTime = Date()
                return
            }
            // compare the distance from the last location
            val distance = haversineDistanceMeter(this.stopLoc!!, loc)
            if(distance > 2000) { // moved 2 km
                this.stopLoc = loc
                this.stopTime = Date()
                return
            }
            val elapsedSec = getElapsedTimeSec(this.stopTime!!, Date())
            var limitTime = this.timeLimit
            if(AppMacro.SIMULATE_GPS)
                limitTime = 2
            if(elapsedSec / 60 < limitTime ){ // time isn't expired yet for stopping
                return
            }
            this.locEventCallback.onParkEntity(null, this.stopTime!!)
        }
    }
}