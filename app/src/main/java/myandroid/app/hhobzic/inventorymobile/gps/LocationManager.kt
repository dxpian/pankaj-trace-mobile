package myandroid.app.hhobzic.inventorymobile.gps

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_STATUS
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_TYPE
import myandroid.app.hhobzic.inventorymobile.api.model.NodeDeliver
import myandroid.app.hhobzic.inventorymobile.api.model.Parking
import myandroid.app.hhobzic.inventorymobile.api.model.ServerPoint
import myandroid.app.hhobzic.inventorymobile.api.model.Store
import myandroid.app.hhobzic.inventorymobile.api.model.TrackSetting
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.AppMacro.Constant.LOCATION_REPORT_INTERVAL_HIGH
import myandroid.app.hhobzic.inventorymobile.home.AppMacro.Constant.LOCATION_REPORT_INTERVAL_LOW
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Beacon
import myandroid.app.hhobzic.inventorymobile.scanner.model.ScannerResult
import myandroid.app.hhobzic.inventorymobile.scanner.presenter.ScanManager
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger
import myandroid.app.hhobzic.inventorymobile.utility.getCurrentTimeString
import myandroid.app.hhobzic.inventorymobile.utility.haversineDistanceMeter
import kotlin.math.*


import java.util.Date
import kotlin.collections.ArrayList

// data class used to save the temporary location information in local
val TAG =  "LocationManager"


enum class REPORT_MODE(val mode: Int) {
    HIGH(1),
    LOW (2);
    companion object {
        fun fromCode(code: Int): REPORT_MODE? {
            return entries.find { it.mode == code }
        }
    }
}

// data class to wrap company location
data class CompanyBrief(
    val id: String,
    val name: String,
    val latLng: LatLng,
    var timeReached: Date?,
    var polygon: List<LatLng>?
){
    constructor(company: Company) : this(
        id = company.id,
        name = company.name,
        latLng = LatLng(company.latitude.toDouble(),
            company.longitude.toDouble()),
        timeReached = null,
        polygon = null
    )
}

data class ParkingBrief(
    val id: String,         // id of parking
    val company: String,    // id of company
    val name: String,       // description of company
    val latLng: LatLng,     // location of parking
    var timeReached: Date?,  // reached time at this parking
    var polygon: List<LatLng>?
){
    constructor(parking: Parking) : this(
        id = parking.id,
        name = parking.description?:"unknown park",
        latLng = LatLng(parking.latitude.toDouble(),
            parking.longitude.toDouble()),
        company = parking.company,
        polygon = null,
        timeReached = null
    )
}


// data class to wrap DC location
data class DistCenterBrief(
    val id: String,
    val name: String,
    val latLng: LatLng,
    val ngbCompany: String,     // company ID which is neighboard with this distirbution center
    val ngbTimeOut: Int,
    var polygon: List<LatLng>?

){
    constructor(distCenter: DistCenter) : this(
        id = distCenter.id,
        name = distCenter.name,
        latLng = LatLng(distCenter.latitude.toDouble(), distCenter.longitude.toDouble()),
        ngbCompany = distCenter.neighbor_company,
        ngbTimeOut = distCenter.neighbor_timeout,
        polygon = null
    )
}

// data class to wrap the retail store
data class StoreBrief(
    val id: String,
    val latLng: LatLng,
    val name: String,
    val nearest: Int,       // distance to nearest item
    val passedSec: Int,
    var polygon: List<LatLng>?
){
    constructor(store: Store): this(
        id = store.id,
        name = store.name,
        latLng = LatLng(store.latitude.toDouble(), store.longitude.toDouble()),
        nearest = store.nearest,
        passedSec = store.passedSec,
        polygon = null
    )
}
// expoerted property to match the tag to cart
data class BeaconExport(
    val tag: String
)


// data class to check the overview of cart changes
data class TagsOverview(
    var exits: MutableList<BeaconExport> = mutableListOf(),     // tags already exist
    var loaded: MutableList <BeaconExport> = mutableListOf(),   // tag list loaded newly
    var unloaded: MutableList<BeaconExport> = mutableListOf()    // tag list unloaded newly
){
    // new beacon loaded
    fun addToLoaded(beacon: BeaconExport){
        val item = loaded.findLast { it.tag == beacon.tag }
        if(item != null){ // save tag exists already
            return
        }
        loaded.add(beacon)
    }
    // new beacon removed
    fun removeFromLoaded(beacon: BeaconExport){
        val index = loaded.indexOf(beacon)
        if(index < 0){ // invalid tag
            //AppLogger.d(TAG, "Failed to remove tag from loaded list")
            return
        }
        loaded.removeAt(index)
    }
    // add beacon to unloaded list
    fun addToUnloaded(beacon: BeaconExport){
        val item = unloaded.findLast { it.tag == beacon.tag }
        if(item != null){ // save tag exists already
            return
        }
        unloaded.add(beacon)
    }
    // remove beacon from unloaded list
    fun removeFromUnloaded(beacon: BeaconExport){
        val index = unloaded.indexOf(beacon)
        if(index < 0){ // invalid tag
            //AppLogger.d(TAG, "Failed to remove tag from loaded list")
            return
        }
        unloaded.removeAt(index)
    }
}

enum class DRIVER_STATUS(val code: Int) {
    MOVE(0),              // driver is moving
    REACHED(1),           // driver is reached to any entity
    COMPLETE_FORCE(2),          // complete truck because driver stops in the neighbor distribution for 3 hours
}


class LocationStatusManager(){
    private val _distAroundBig: Int = 1000 * 10     // distance range which the system starts to monitor its location
    private var _reachedItem: GpsEntity? = null
    private var _reacheckItemBack: GpsEntity? = null
    private var _timeReached: Date? = null
    private var _timeLeaved: Date? = null
    private var _hitCount: Int = 0
    private var _hitTime: Date? = null


    private var _status: DRIVER_STATUS = DRIVER_STATUS.MOVE
    private lateinit var   _trackSetting: TrackSetting

    fun isHighMonitorRange(distance: Int): Boolean{
        return distance < _distAroundBig
    }

    // return true if truck is in reached status except for first arrival event
    fun isStayInReachedStatus(): Boolean{
        return _status == DRIVER_STATUS.REACHED && _hitCount == 0
    }

    fun setTrackSetting(trackSetting: TrackSetting){
        _trackSetting = trackSetting
    }

    fun startDeliverTask(company: Company){
        _reachedItem = GpsEntity.cmp(CompanyBrief(company))
        _status = DRIVER_STATUS.REACHED
    }

    fun setLastDeliverNode(node: GpsEntity, status: Number){
        if(status == NODE_STATUS.REACHED.code){
            this._status = DRIVER_STATUS.REACHED
            this._reachedItem = node
            _timeReached = Date()
        }
        else{
            this._status = DRIVER_STATUS.MOVE
            this._reachedItem = null

        }
        this._hitCount = 0
        this.printCurrentStatus()
    }

    private fun printCurrentStatus(){
        val statusLabel = when(this._status){
            DRIVER_STATUS.MOVE->{
                "Move: ${this._reachedItem?.getName()}"
            }
            DRIVER_STATUS.REACHED->{
                "Reached: ${this._reachedItem?.getName()}"
            }
            DRIVER_STATUS.COMPLETE_FORCE->{
                "Completed at ${this._reachedItem?.getName()}"
            }
        }
        AppLogger.d(TAG, "Current Location manager status: ${statusLabel}")
    }


    private fun getHitTimeSeconds(): Int{
        if(this._hitTime == null)
            this._hitTime = Date()
        return getElapsedTimeSec(this._hitTime!!, Date())
    }

    private fun getReachable(entity: GpsEntity, passedSec: Int): Boolean{
        var elapsedSec = getHitTimeSeconds()
        AppLogger.d(TAG, "elased sec from the first arrival event: $elapsedSec/$passedSec")
        if(elapsedSec < passedSec){
            return false
        }
        val timeLeaved = this._timeLeaved
        if(this._reacheckItemBack==null || timeLeaved == null){ // no previous item and no leaved time
            return true
        }
        elapsedSec = getElapsedTimeSec(timeLeaved, Date())
        if(elapsedSec < 60 * 5 &&  this._reacheckItemBack?.getId() == entity.getId()){
            this._timeLeaved = Date()
            AppLogger.d(TAG, "Truck reached to the same item within $elapsedSec. Ignore it")
            return false
        }
        return true

    }

    private fun getStayedTimeSeconds(): Int{
        if(this._timeReached == null){
            this._timeReached = Date()
        }
        return getElapsedTimeSec(this._timeReached!!, Date())
    }

    /**
     * @description: update the current status
     * @return: return the item and its status if truck reached or leaved the entity
     *                  Pair<GpsEntity, DRIVER_STATUS>
     *      return null if there is no any changes
     */
    fun updateStatus(entity: GpsEntity?, reached: Boolean): Pair<GpsEntity,DRIVER_STATUS>? {

        AppLogger.d(TAG, "Current gps track status: $_status. previous item: ${this._reachedItem}")
        if(entity == null){
            AppLogger.d(TAG, "There is no entity vicinity of the current location")
            if(_status == DRIVER_STATUS.REACHED){// already reached to entity, but no found nearest items
                val elapsedSec = getElapsedTimeSec(_timeReached!!, Date())
                if(elapsedSec > _trackSetting.locationUpdateSec * 2){
                    onLeaved()
                    if(this._reachedItem != null)
                        return Pair(_reachedItem!!, _status)
                    else{
                        AppLogger.e(TAG, "Invalid reach item status after leave node")
                        return null
                    }
                }
            }
            return null
        }

        // minimum sec before reaching to new item
        var passedSec: Int = when(entity){
            is GpsEntity.dc->{ _trackSetting.passedSec}
            is GpsEntity.cmp->{ _trackSetting.passedSec}
            is GpsEntity.rs->{entity.rs.passedSec;}
            is GpsEntity.park->{_trackSetting.passedSec}
        }
        // minimum sec before leave from reached item
        var stayMinSec = 30

        if(AppMacro.SIMULATE_GPS){
            passedSec = 0
            stayMinSec = 0
        }

        if (_status == DRIVER_STATUS.MOVE) { // check if truck reached to item
            if(!reached){
                this._hitTime = null
                return null
            }
            if(this.getReachable(entity, passedSec)){
                _hitCount ++;
                onReached(entity)
                return Pair(_reachedItem!!, _status)
            }
            AppLogger.d(TAG, "Cannot reach this item")
            return null
        }
        else if(_status == DRIVER_STATUS.REACHED || _status == DRIVER_STATUS.COMPLETE_FORCE) {
            if(reached){ // still in reached-range
                // reached to neighbor distribution center
                if(entity.getId() != this._reachedItem?.getId()){ // reached to new item without leave the previous entity
                    if(this.getStayedTimeSeconds() >= stayMinSec){ // reached to new item
                        onReached(entity)
                        return Pair(_reachedItem!!, _status)
                    }
                    return null
                }
                /*
                else if(entity is GpsEntity.dc
                    && this._reachedItem is GpsEntity.dc
                    && entity.dc.id == this._reachedItem!!.getId()
                    && entity.dc.ngbCompany.isNotEmpty()
                    ) // stop in the same distribution center
                {
                    val elapsedSec = getElapsedTimeSec(this._timeReached!!, Date())
                    var waitingTimeSec = entity.dc.ngbTimeOut * 60 // 3 hour
                    if(AppMacro.SIMULATE_GPS)
                           waitingTimeSec = 60     // 1 minute

                    if(elapsedSec > waitingTimeSec.toInt()) { // 3 hours. no move 3 hours in the same distribution center
                        this._status = DRIVER_STATUS.COMPLETE_FORCE
                        this._hitCount = 0
                        this._hitTime = null
                        this._timeReached = null
                        return Pair(this._reachedItem!!, this._status)
                    }
                }*/
                else{ // stay in reached state
                    AppLogger.w(TAG, "Reached to the previous same node. skip it")
                    _hitCount = 0
                    return null
                }
            }
            else{ // leaved from previous distance
                if(getStayedTimeSeconds() > stayMinSec){
                    onLeaved()
                    if(this._reachedItem!= null)
                        return Pair(_reachedItem!!, _status)
                    else{
                        AppLogger.e(TAG, "Reached item is null when leave the entity")
                    }

                }
                return null
            }
        }
        else if(this._status == DRIVER_STATUS.COMPLETE_FORCE){ // send the duplicated request if app pushs gps location again
            if(reached){ // still in reached-range
                // reached to neighbor distribution center
                if(entity is GpsEntity.dc
                    && this._reachedItem is GpsEntity.dc
                    && entity.dc.id == this._reachedItem!!.getId()
                    && entity.dc.ngbCompany.isNotEmpty()
                ) // stop in the same distribution center
                {
                    val elapsedSec = getElapsedTimeSec(this._timeReached!!, Date())
                    var waitingTimeSec = 3 * 3600  // 3 hour
                    if(AppMacro.SIMULATE_GPS)
                        waitingTimeSec = 60 * 2     // 1 minute

                    if(elapsedSec > waitingTimeSec) { // 3 hours. no move 3 hours in the same distribution center
                        this._hitCount = 0
                        this._hitTime = null
                        return Pair(this._reachedItem!!, this._status)
                    }
                }
                else{ // this is not neighboard item
                    _hitCount = 0
                    this._hitTime = null
                    return null
                }
            }
        }
        return null
    }

    private fun onReached(entity: GpsEntity){
        AppLogger.d(TAG, "Reached to new entity")
        _hitCount = 0
        _reachedItem = entity
        _status = DRIVER_STATUS.REACHED
        _timeReached = Date()
        _timeLeaved = null
        _hitTime = null
    }
    private fun onLeaved(){
        AppLogger.d(TAG, "Leave new entity")
        _hitCount = 0
        _hitTime = null
        _reacheckItemBack = _reachedItem?.copy()

        _timeReached = null
        _timeLeaved = Date()
        _status = DRIVER_STATUS.MOVE
    }
}

interface LocationChangedInterface{
    suspend fun onTruckLeaveDc(id: String)
    suspend fun onTruckArriveDc(id: String)
    suspend fun onTruckLeaveStore(id: String)
    suspend fun onTruckArriveStore(id: String)
    suspend fun onTruckLeaveCompany(id: String)
    suspend fun onTruckArriveCompany(id: String)
    suspend fun onTruckStartTask(entity: GpsEntity)
    suspend fun onTruckCompleteForcely(id: String)
    suspend fun onTruckCompleteAutomatically(entity: GpsEntity?, time: Date) // Deliver task is completed automatically because truck stops in the same location more than 2 hours
    suspend fun onFoundBecaons(entity: GpsEntity, beacons: List<BeaconExport>)
    suspend fun onRequireGeofencingData(entity: GpsEntity)          // Geofencing data is required because the driver has reached around the entity
}

class LocationManager(private val context: Context, private val eventCallback: LocationChangedInterface) : ScannerResult, LocationEventInterface {

    private val MAX_SPEED = 120.0f             // max speed
    private var _reportMode = REPORT_MODE.HIGH
    private var _locMonitor = LocMonitor(this)
    private var _cntPoint = 0
    private var _reportPeriod = 1
    private val _maxSpeed = 80.0    // max speed of truck. km/h
    private var _timeStart: Date= Date()
    private var _points: MutableList<LatLng> = mutableListOf()    // list of temporal points before send server
    private var _statusManager = LocationStatusManager()
    private var _comps: List<CompanyBrief> = listOf()               // company list
    private var _dcs: List<DistCenterBrief> = listOf()              // distribution centers
    private var _rss: List<StoreBrief> = listOf()                   // retail stores
    private var _parks: MutableList<ParkingBrief> = mutableListOf()
    // scanner related
    private var _isScanning = false                     // now scanning is running?
    private var _scanCompleted = false                  // is scanning now?
    private lateinit  var _timeScanStart: Date        // scanning start time
    private lateinit var  _timeLastUpdate: Date
    private var _scanner = ScanManager(this)
    private var _beacons : ArrayList<Beacon> = arrayListOf()
    private var _lastEntity: GpsEntity? = null
    private var _angle = 0
    private lateinit var _trackSetting: TrackSetting
    private val gson = Gson()



    init {
        initialize()
    }

    // initialize the Location report mode
    fun initialize(){
        _points.clear()
        _cntPoint = 0
        onChangeReportMode(REPORT_MODE.HIGH)
    }

    fun     updateTaskStatus(deliverStatus: DeliveryState?){
        this._locMonitor.updateTaskStatus(deliverStatus)
    }


    fun     updateCompanies(cmps: List<Company>){
        this._comps = cmps.map { CompanyBrief(it)}
        this._parks.clear()
        AppLogger.d(TAG, "Set the company list to location manager:")
        this._comps.forEach{it->AppLogger.d(TAG, "${it}")}
        cmps.forEach{comp->
            if(comp.parkings.isNullOrEmpty())
                return@forEach
            val type = object : TypeToken<List<Parking>>() {}.type // Define the type of the list
            val parkingList: List<Parking> = gson.fromJson(comp.parkings, type) // Parse JSON string to lis
            parkingList.forEach { park ->
                this._parks.add(ParkingBrief(park))
            }
        }
    }

    /**
     * @decription: Update the distcenter information
     */
    fun updateDistCenter(dcs: List<DistCenter>){
        _dcs = dcs.map { DistCenterBrief(it) }
    }

    /**
     * @decription: Update the retial store information
     */
    fun updateStore(rss: List<Store>){
        _rss = rss.map { StoreBrief(it)}
    }

    fun updateTrackSetting(trackSetting: TrackSetting){
        _trackSetting = trackSetting
        _statusManager.setTrackSetting(trackSetting)
        _locMonitor.setTimeLimit(trackSetting.minutes_expire_task)
    }

    fun updateDriverSetting(user: Driver){
        this._trackSetting.task_start = user.time_taskstart
        this._trackSetting.task_stop = user.time_taskstop
        this._trackSetting.auto_start = user.auto_start
    }

    // set as location manager reached to this company
    fun startDeliverTask(company: Company){
        _statusManager.startDeliverTask(company)
    }


    // set last node
    fun setLastNodeItem(node: NodeDeliver){
        AppLogger.d(TAG, "last node of deliver task is : ${node}")
        var lastEntity: GpsEntity? = null
        if(node.nodetype == NODE_TYPE.DC.code){
            val dc = this._dcs.findLast {it->it.id == node.nodeid }?:return
            lastEntity = GpsEntity.dc(dc)
        }
        else if(node.nodetype == NODE_TYPE.STORE.code){
            val store = this._rss.findLast { it->it.id == node.nodeid }?:return
            lastEntity = GpsEntity.rs(store)
        }
        else if(node.nodetype == NODE_TYPE.COMPANY.code) {
            val company = this._comps.findLast { it -> it.id == node.nodeid } ?: return
            lastEntity = GpsEntity.cmp(company)
        }
        else{
            AppLogger.w(TAG, "Node type of last node of deliver is invalid")
            return
        }
        AppLogger.d(TAG, "Success to load the last state of location manager: ${lastEntity}")
        this._statusManager.setLastDeliverNode(lastEntity, node.status)

    }


    /**
     * @description: Add new GPS location into manager.
     * @param:
     *      - Location: current GPS coordinate
     *      - trackCompleted: track is completed?
     * @return: Pair(ServerPoint, angle)
     *          ServerPoint: instance of "ServerPoint" if it has to report new location. null if it doesn't need to report new point
     *          Angle: The rotation value. left direction is 0
     */
    suspend fun addPoint(location: LatLng ): Pair<ServerPoint?, Int>{
        AppLogger.d(TAG, "Get new point. Check the current status of location manager: $location")
        try{
            _points.add(location)
            this._locMonitor.updateLocation(location)

            if(_points.size >= 2){ // calculate the trip angle
                _angle = calculateBearing(_points[_points.size - 2], _points[_points.size - 1])
            }
            if(_points.size < _reportPeriod) { // no need to calculate the distance
                return Pair(null, _angle)
            }
            val elapsedSec =   getElapsedTimeSec(_timeStart, Date())
            val distMeter = getDistanceOfLocationBuffer()
            val speed: Float = Math.min((distMeter.toFloat() * 3.6f) / elapsedSec, this.MAX_SPEED.toFloat())
            if(!AppMacro.SIMULATE_GPS && speed >= this.MAX_SPEED) // invalid speed which means GPS is incorrect
            {
                if(_points.isNotEmpty()) {
                    _points.removeAt(_points.lastIndex)
                }
                AppLogger.d(TAG, "Detect the invalid speed: $speed, return without checking event")
                return Pair(null, _angle)
            }
            val pointServer = ServerPoint(
                latitude = location.latitude.toFloat(),
                longitude = location.longitude.toFloat(),
                speed = speed,
                elapsedSec = elapsedSec,
                time = getCurrentTimeString(),
                distance = 0,
            )
            // retain only last element
            _points = mutableListOf(location)
            _timeStart =  Date()


            updateStatus(location)
            if(_statusManager.isStayInReachedStatus()) // return null points to avoid for reporting the gps location in reached state
                return Pair(null, _angle)

            return Pair(pointServer, _angle)
        }
        catch (e: Exception){
            AppLogger.e(TAG, "Exception in adding point location manager")
            return Pair(null, 0)
        }
    }

    // truck reached to any location
    override fun onReachedEntity(entity: GpsEntity){
        AppLogger.d(TAG, "Truck reached to entity: $entity")
    }

    // truck leave from location
    override  fun onLeaveEntity(entity: GpsEntity){
        AppLogger.d(TAG, "Truck leave from entity: $entity")

    }

    // truck parked on any location more than limited time
    override suspend  fun onParkEntity(entity: GpsEntity?, time: Date){
        AppLogger.d(TAG, "Truck complete the deliver task at $entity")
        this.eventCallback.onTruckCompleteAutomatically(entity, time )
    }

    /**
     * @description: Calculate the angle between 2 points
     */
    private fun calculateBearing(start: LatLng, end: LatLng): Int {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        // atan2 returns result in radians, convert to degrees
        var bearing = Math.toDegrees(atan2(y, x))

        // Normalize the result to 0 - 360 degrees
        bearing = (bearing + 360) % 360
        // //AppLogger.d(TAG, "calcualte the trip angul: ${start}: ${end}: ${bearing}")
        return bearing.toInt()
    }

    /**
     * @description: Change the report interval time when the truck approaches to its target
     */
    private fun onChangeReportMode(mode: REPORT_MODE){
        _reportMode = mode
        if(!AppMacro.SIMULATE_GPS){
            _reportPeriod = when(mode){
                REPORT_MODE.HIGH->{
                    LOCATION_REPORT_INTERVAL_HIGH
                }
                REPORT_MODE.LOW->{
                    LOCATION_REPORT_INTERVAL_LOW
                }
            }
        }
    }

    /**
     * @description: Get the total distance of location buffer
     * @return: the distance meter of a series of location buffer
     */
    private fun getDistanceOfLocationBuffer(): Number{
        var totalDistance = 0.0
        if (_points.size < 2) return totalDistance  // No distance to calculate if less than 2 points
        for (i in 0 until _points.size - 1) {
            totalDistance += haversineDistanceMeter(_points[i], _points[i + 1])
        }
        return totalDistance
    }

    private suspend fun  updateStatus(location: LatLng){
        try{
            // find the dcs less than 100km
            AppLogger.d(TAG, "Update location of the truck: ${location.latitude}, ${location.longitude}")

            // filter by lat/lng first
            val nodeCandidates = findNearentityCandidaters(location)

            // find the nearest one in candidates
            val nearEntity = searchNearestEntity(location, nodeCandidates)

            var reached = false
            if(nearEntity != null){// check if truck reached at this nearest entity
                val distance = haversineDistanceMeter(nearEntity.getLatlng(), location).toInt()
                AppLogger.d(TAG, "Nearest entity event detected: $distance m, $nearEntity, ")
                if(_statusManager.isHighMonitorRange(distance)){
                    onChangeReportMode(REPORT_MODE.HIGH)
                    if(nearEntity.needGeodata()) {
                        AppLogger.d(TAG, "Require geojson data")
                        eventCallback.onRequireGeofencingData(nearEntity)
                    }else{
                        AppLogger.d(TAG, "No need geo data")
                    }
                    // check if truck reached at this nearest entity
                    if(nearEntity.haveGeoData()){ // check by geodata
                        reached = nearEntity.pointInGeoarea(location)
                        if(reached)
                            AppLogger.d(TAG, "Truck location search result: GEO-IN")
                        else
                            AppLogger.d(TAG, "Truck location search result: GEO-OUT")
                    }
                    else{ // check by distance
                        val distThreshold = this.getDistancheThreshold(nearEntity).toInt()
                        reached = distance < distThreshold
                        if(reached)
                            AppLogger.d(TAG, "Truck location search result: DIST-IN")
                        else
                            AppLogger.d(TAG, "Truck location search result: DIST-OUT")
                    }
                }
                else {
                    onChangeReportMode(REPORT_MODE.LOW)
                }
            }

            // val newStatus: Pair<GpsEntity, DRIVER_STATUS> = _statusManager.updateStatus(nearEntity, reached)?:return
            val newStatus: Pair<GpsEntity, DRIVER_STATUS>? = _statusManager.updateStatus(nearEntity, reached)
            if(newStatus==null){
                return
            }
            AppLogger.d(TAG, "New status of location manager: $newStatus")
            val duty = this._locMonitor.isOnDuty()
            val entity = newStatus.first
            if(!duty){ // Truck is in idle status. Check if driver leave from DC or LC
                AppLogger.d(TAG, "Truck is in IDLE status, Check if truck leaves from DC or LC. entity is $entity")
                if(this._trackSetting.auto_start <= 0){
                    AppLogger.d(TAG, "Auto start of deliver task is disabled by driver")
                    return
                }
                if(entity !is GpsEntity.dc &&  entity !is GpsEntity.cmp)
                    return
                if(newStatus.second != DRIVER_STATUS.MOVE) {
                    AppLogger.d(TAG, "No leave event");
                    return
                }

                // check if time is available to start task
                if(!AppMacro.SIMULATE_GPS && !this.isDriverWorkingTime(this._trackSetting.task_start, this._trackSetting.task_stop)){
                    AppLogger.d(TAG, "Now isn't driver working time: ${this._trackSetting.task_start}~${this._trackSetting.task_stop}")
                    return
                }

                // check if this is 2nd task in this working day
                val prevEndTime = this._locMonitor.getPreviousCompletedTime()
                if(prevEndTime!= null && this.isSameWorkingDay(this._trackSetting.task_start, this._trackSetting.task_stop, prevEndTime, Date())){
                    AppLogger.d(TAG, "This is not 1st task of this working day. The 1st task is completed at : $prevEndTime")
                    return;
                }

                this.eventCallback.onTruckStartTask(entity)
            }
            else{
                when (newStatus.second) {
                    DRIVER_STATUS.REACHED -> { // truck reached to entity
                        stopScan()
                        when(entity){
                            is GpsEntity.dc ->{
                                eventCallback.onTruckArriveDc(entity.getId())
                            }

                            is GpsEntity.rs->{
                                eventCallback.onTruckArriveStore(entity.getId())
                            }

                            is GpsEntity.cmp->{
                                eventCallback.onTruckArriveCompany(entity.getId())
                            }

                            is GpsEntity.park->{
                                val parkingId = entity.getParkId()
                                AppLogger.d(TAG, "Driver is reached to park: $parkingId")
                                eventCallback.onTruckArriveCompany(entity.getId())
                            }
                        }
                    }
                    DRIVER_STATUS.MOVE -> { // truck leaved from entity
                        startScan(entity)
                        when(entity){
                            is GpsEntity.dc ->{
                                eventCallback.onTruckLeaveDc(entity.getId())
                            }

                            is GpsEntity.rs->{
                                eventCallback.onTruckLeaveStore(entity.getId())
                            }

                            is GpsEntity.cmp->{
                                eventCallback.onTruckLeaveCompany(entity.getId())
                            }

                            is GpsEntity.park->{
                                eventCallback.onTruckLeaveCompany(entity.getId())
                            }
                        }
                    }
                    DRIVER_STATUS.COMPLETE_FORCE -> { // truck waits in DC neighbored with logistic company
                        val lastEntity:GpsEntity = newStatus.first
                        eventCallback.onTruckCompleteForcely(lastEntity.getId())
                    }
                }
            }
         }
        catch (e: Exception){
            AppLogger.e(TAG, "Exception in update the Location manager: ${e.message}")
        }
    }

    fun setGeodata(entity: GpsEntity, geodata: List<LatLng>){
        AppLogger.d(TAG, "Get geo data of entity: $entity, geodata: $geodata")
        val nodeType = entity.getNodeType()
        when (nodeType) {
            NODE_TYPE.DC.code -> {
                val dc = this._dcs.find { it.id == entity.getId() }
                dc?.polygon = geodata.toList()
                if(dc==null){
                    AppLogger.d(TAG, "No found DC of gps entity: $entity")
                }
            }
            NODE_TYPE.COMPANY.code -> {
                val cmp = this._comps.find { it.id == entity.getId() }
                cmp?.polygon = geodata.toList()
                if(cmp==null){
                    AppLogger.d(TAG, "No found company of gps entity: $entity")
                }
            }
            NODE_TYPE.STORE.code -> {
                val store = this._rss.find { it.id == entity.getId() }
                store?.polygon = geodata.toList()
                if(store==null){
                    AppLogger.d(TAG, "No found store of gps entity: $entity")
                }
            }
        }
    }

    private fun findNearentityCandidaters(location: LatLng): List<GpsEntity>{
        val maxlat = 0.9 / (100/_maxSpeed)   // max latitude range which  truck can move within 1 hour
        val maxlng = 1.2 / (100/_maxSpeed)    // max longitude range which truck can more within 1 hour

        val cmpsFilter: List<GpsEntity> = _comps.filter {
            it.latLng.latitude >= location.latitude - maxlat &&
                    it.latLng.latitude <= location.latitude + maxlat &&
                    it.latLng.longitude >= location.longitude - maxlng &&
                    it.latLng.longitude <= location.longitude + maxlng
        }.map{GpsEntity.cmp(it)}
        val parksFilter: List<GpsEntity> = _parks.filter {
            it.latLng.latitude >= location.latitude - maxlat
                    && it.latLng.latitude <= location.latitude + maxlat
                    && it.latLng.longitude >= location.longitude - maxlng
                    && it.latLng.longitude <= location.longitude + maxlng
        }.map{GpsEntity.park(it)}

        val dcsFilter: List<GpsEntity> = _dcs.filter {
            it.latLng.latitude >= location.latitude - maxlat &&
                    it.latLng.latitude <= location.latitude + maxlat &&
                    it.latLng.longitude >= location.longitude - maxlng &&
                    it.latLng.longitude <= location.longitude + maxlng
        }.map{GpsEntity.dc(it)}

        // find the dcs less than 100km
        val rssFilter: List<GpsEntity> = _rss.filter {
            it.latLng.latitude >= location.latitude - maxlat &&
                    it.latLng.latitude <= location.latitude + maxlat &&
                    it.latLng.longitude >= location.longitude - maxlng &&
                    it.latLng.longitude <= location.longitude + maxlng
        }.map{GpsEntity.rs(it)}
        return  dcsFilter + rssFilter + cmpsFilter + parksFilter
    }

    /**
     * @description: Get the nearest item
     */
    private fun searchNearestEntity(
        location: LatLng,
        entities: List<GpsEntity>
    ): GpsEntity? {
        return entities.minByOrNull { entity ->
            when (entity) {
                is GpsEntity.dc -> {
                    haversineDistanceMeter(location, entity.dc.latLng)
                }
                is GpsEntity.rs -> {
                    haversineDistanceMeter(location, entity.rs.latLng)
                }
                is GpsEntity.cmp -> {
                    haversineDistanceMeter(location, entity.cmp.latLng)
                }
                is GpsEntity.park -> {
                    haversineDistanceMeter(location, entity.park.latLng)
                }
                else -> {
                    10000000.0
                }
            }
        }
    }

    private fun getDistancheThreshold(entity: GpsEntity): Number{
        return when(entity){
            is GpsEntity.dc->{_trackSetting.distanceMeter}
            is GpsEntity.cmp->{ _trackSetting.distanceMeter}
            is GpsEntity.rs->{entity.rs.nearest;}
            is GpsEntity.park->{_trackSetting.distanceMeter}
        }
    }

    private fun isDriverWorkingTime(from: String, to: String): Boolean {
        val now = Calendar.getInstance()

        val formats = listOf(
            SimpleDateFormat("HH:mm", Locale.getDefault()),
            SimpleDateFormat("h:mma", Locale.getDefault())
        )

        fun parseTime(input: String): Calendar? {
            for (format in formats) {
                try {
                    val parsedDate = format.parse(input.uppercase(Locale.getDefault()))
                    val cal = Calendar.getInstance()
                    cal.time = parsedDate!!
                    cal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                    cal.set(Calendar.MONTH, now.get(Calendar.MONTH))
                    cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                    return cal
                } catch (_: Exception) { }
            }
            return null
        }

        val fromCal = parseTime(from)
        val toCal = parseTime(to)

        if (fromCal == null || toCal == null) return false

        // If "to" is before "from", treat as next day
        if (toCal.before(fromCal)) {
            toCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return now.after(fromCal) && now.before(toCal)
    }

    private fun isSameWorkingDay(from: String, to: String, timePrev: Date, now: Date): Boolean {
        val formats = listOf(
            SimpleDateFormat("HH:mm", Locale.getDefault()),
            SimpleDateFormat("h:mma", Locale.getDefault())
        )

        fun parseTime(input: String): Pair<Int, Int>? {
            for (format in formats) {
                try {
                    val date = format.parse(input.uppercase(Locale.getDefault())) ?: continue
                    val cal = Calendar.getInstance()
                    cal.time = date
                    return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                } catch (_: Exception) { }
            }
            return null
        }

        val fromTime = parseTime(from) ?: return false
        val toTime = parseTime(to) ?: return false

        // helper: returns the start of the working-day period that `time` belongs to
        fun getWorkingPeriodStart(time: Date): Calendar {
            val cal = Calendar.getInstance().apply { this.time = time }
            val fromCal = cal.clone() as Calendar
            fromCal.set(Calendar.HOUR_OF_DAY, fromTime.first)
            fromCal.set(Calendar.MINUTE, fromTime.second)
            fromCal.set(Calendar.SECOND, 0)
            fromCal.set(Calendar.MILLISECOND, 0)

            val toCal = fromCal.clone() as Calendar
            toCal.set(Calendar.HOUR_OF_DAY, toTime.first)
            toCal.set(Calendar.MINUTE, toTime.second)
            if (toCal.before(fromCal)) {
                toCal.add(Calendar.DAY_OF_YEAR, 1) // crosses midnight
            }

            // If current time is before today's working window, it belongs to the *previous* one
            if (cal.before(fromCal)) {
                fromCal.add(Calendar.DAY_OF_YEAR, -1)
            }

            return fromCal
        }

        val startPrev = getWorkingPeriodStart(timePrev)
        val startNow = getWorkingPeriodStart(now)

        return startPrev.timeInMillis == startNow.timeInMillis
    }

    /**
     * Start scanning when leave any entity
     */
    private fun startScan(entity: GpsEntity){
        if(_isScanning){
            AppLogger.w(TAG, "Cann't start scanning. Scanner is running now")
            return
        }
        //AppLogger.d(TAG, "start BLE scanning")
        _timeStart = Date()
        _isScanning = true
        _beacons.clear()
        _scanner.startScan()
        _timeLastUpdate = Date()
        _lastEntity = entity
        //AppLogger.d(TAG, "Start scanning to find BLE tags")
    }

    /**
     * Stop scan because time expired or arrived at any entity
     */
    private fun stopScan(){
        if(!_isScanning){
            //AppLogger.d(TAG, "Scanning is already completed")
            return
        }
        //AppLogger.d(TAG, "stop BLE scanning")
        _isScanning = false
        _scanCompleted = true
        _scanner.stopScan()
        //AppLogger.d(TAG, "Stop scanning to find BLE tags")

    }

    override fun findBeacon(arrayList: java.util.ArrayList<Beacon>?) {
        if(_beacons.size != arrayList?.size){ // beacon size is changed
            _timeLastUpdate = Date()
        }
        if(arrayList == null){
            AppLogger.w(TAG, "Empty beacon list")
            return
        }
        _beacons = arrayList
        if(getElapsedTimeSec(_timeStart, Date()) > _trackSetting.scanTimeSec ||
            getElapsedTimeSec(_timeLastUpdate, Date()) > AppMacro.WIFI_SCAN_UPDATE_ONE_TIME_SEC
        ){
            //AppLogger.d(TAG, "Scanner is timeout")
            stopScan()
            return
        }
    }

    /**
     * report the scanning result to repository model
     */
    private suspend fun reportScanResult(){
        _scanCompleted = false
        if(_lastEntity != null) {
            val tags = _beacons.map { it-> BeaconExport(it.address) }
            eventCallback.onFoundBecaons(_lastEntity!!, tags)
        }
    }
}