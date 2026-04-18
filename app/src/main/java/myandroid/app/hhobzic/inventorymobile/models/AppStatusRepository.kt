package myandroid.app.hhobzic.inventorymobile.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import myandroid.app.hhobzic.inventorymobile.BuildConfig
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResponse
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.RetrofitCleintLocal
import myandroid.app.hhobzic.inventorymobile.api.RetrofitClientSecure
import myandroid.app.hhobzic.inventorymobile.api.model.Store
import myandroid.app.hhobzic.inventorymobile.api.truck.CartService
import myandroid.app.hhobzic.inventorymobile.api.model.Cart
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DateDeserializer
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.ErrorCode
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_TYPE
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.utility.AppState
import myandroid.app.hhobzic.inventorymobile.utility.extendStoreProperties
import java.io.InputStream
import java.util.Date
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.squareup.moshi.Json
import myandroid.app.hhobzic.inventorymobile.api.model.ApkVersion
import myandroid.app.hhobzic.inventorymobile.api.model.DELIVER_STOP_REASON
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveredStoreResult
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenterDeliverResult
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_STATUS
import myandroid.app.hhobzic.inventorymobile.api.model.NodeDeliver
import myandroid.app.hhobzic.inventorymobile.api.model.Notification
import myandroid.app.hhobzic.inventorymobile.api.model.ServerPoint
import myandroid.app.hhobzic.inventorymobile.api.model.ServerTrackSetting
import myandroid.app.hhobzic.inventorymobile.api.model.TrackSetting
import myandroid.app.hhobzic.inventorymobile.api.model.getArrivedMessage
import myandroid.app.hhobzic.inventorymobile.api.model.getLeavedMessage
import myandroid.app.hhobzic.inventorymobile.gps.BeaconExport
import myandroid.app.hhobzic.inventorymobile.gps.GeoFenceUtil
import myandroid.app.hhobzic.inventorymobile.gps.GpsEntity
import myandroid.app.hhobzic.inventorymobile.gps.LocationChangedInterface

import myandroid.app.hhobzic.inventorymobile.gps.LocationManager
import myandroid.app.hhobzic.inventorymobile.gps.TagsOverview
import myandroid.app.hhobzic.inventorymobile.gps.getElapsedTimeSec
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.cart.EntityEvent
import myandroid.app.hhobzic.inventorymobile.home.cart.EntityEventType
import myandroid.app.hhobzic.inventorymobile.home.profile.NotifyEvent
import myandroid.app.hhobzic.inventorymobile.home.profile.NotifyEventType
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger
import myandroid.app.hhobzic.inventorymobile.utility.decodeAndRotateBitmap
import myandroid.app.hhobzic.inventorymobile.utility.extendCompanyProperties
import myandroid.app.hhobzic.inventorymobile.utility.extendDistCenterProperties
import myandroid.app.hhobzic.inventorymobile.utility.haversineDistanceMeter

import myandroid.app.hhobzic.inventorymobile.websocket.SocketIOClient
import myandroid.app.hhobzic.inventorymobile.websocket.WebSocketCallback
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Queue
import java.util.TimeZone
import java.util.concurrent.ConcurrentLinkedQueue

//
class AppStatusRepository private constructor(private val context: Context): WebSocketCallback, LocationChangedInterface
{
    private val TAG = "AppStatusRepository"
    private val gson = Gson()

    private var _cartRecentAdded: Cart?=null              // cart added lastly
    private var _cartRecentRemoved: Cart? = null             // cart removed lastly

    private val _user = MutableLiveData<Driver>()
    private val _userPhoto = MutableLiveData<Bitmap>()

    private var _companies: List<Company> = listOf()
    val user: LiveData<Driver> get() = _user
    private var _timeAddLastSpd: Date = Date()

    // private var _carts_dc = MutableLiveData<MutableList<Cart>>()       // all carts of distribution center selected
    private var _cartsLoaded = MutableLiveData<MutableList<Cart>>()   // cart list which is uploaded already
    private var _deliverAppState = MutableLiveData<AppState?>()
    private var _deliverState = MutableLiveData<DeliveryState?>()
    private var _entityUpdated = MutableLiveData<Queue<EntityEvent>>(ConcurrentLinkedQueue())             // the status of DC is updated
    private var _notifyUpdated = MutableLiveData<Queue<NotifyEvent>>(ConcurrentLinkedQueue())              // notifiy message received from server
    private var _notifyLocUpdate = MutableLiveData<Queue<String>>(ConcurrentLinkedQueue())                  // notify message received from location manager

    private var _lastDeliverState: DeliveryState? = null            // last deliver state. Used when the state isn't updated by event
    private var _lastAppState: AppState? = null

    private var _dcs = MutableLiveData<List<DistCenter>>()                  // A list of available DCs
    private var _rss = MutableLiveData<List<Store>>()                  // A list of available RSs

    private var _cartsCount = MediatorLiveData<Int>()                        // number of cart (empty + loaded)
    private var _cartsCountNonEmpty = MediatorLiveData<Int>()                        // number of cart loaded
    private var _cartsCountEmpty = MediatorLiveData<Int>()                        // number of cart empty
    private var _tripDirection = MutableLiveData<Int>()                     // direction of the trip course. right direction is 0 and clockwise direction
    private var _userProfileImageBaseUrl = ""
    private var _serverUrl = ""
    private var _wsClient: SocketIOClient
    private var _locManager: LocationManager = LocationManager(context, this)

    private var _deliverCoursePoints = MutableLiveData<List<ServerPoint>?>() // list of points of course
    private var _deliverWeekSpeed = MutableLiveData<List<ServerPoint>?>()   // list of speed within recent one week
    private var _deliverOnePoint = MutableLiveData<Queue<ServerPoint>>(ConcurrentLinkedQueue())
    private var _trackSetting = MutableLiveData<TrackSetting>()
    private var _trackCompleted = true;                // tracking is completed after task completed and truck leaves from LC
    private var _versionChecked = false;


    companion object {
        @Volatile
        private var INSTANCE: AppStatusRepository? = null

        fun getInstance(context: Context): AppStatusRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppStatusRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val apiService: CartService
    init {
        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .create()

        if(isLocalEnvironment()){
            apiService = RetrofitCleintLocal.getInstance(context).create(CartService::class.java)
            _serverUrl = AppMacro.SERVER_URL_LOCAL
            _wsClient = SocketIOClient(AppMacro.SERVER_WS_URL_LOCAL, this)

        }
        else{
            apiService = RetrofitClientSecure.getInstance(context).create(CartService::class.java)
            _serverUrl = AppMacro.SERVER_URL_CLOUD
            _wsClient = SocketIOClient(AppMacro.SERVER_WS_URL_CLOUD, this)
        }
        _userProfileImageBaseUrl = "${_serverUrl}mobile/driver/downloadProfile"

        _cartsCount.addSource(_cartsLoaded) { carts ->
            _cartsCount.postValue(carts?.count() ?: 0)
        }
        _cartsCountEmpty.addSource(_cartsLoaded){ carts->
            val emptyCount = carts?.count { it.product!=null && it.product!!.isEmpty() } ?: 0
            _cartsCountEmpty.postValue(emptyCount)
        }

        _cartsCountNonEmpty.addSource(_cartsLoaded){ carts->
            val emptyCount = carts?.count { it.product!=null && it.product!!.isNotEmpty() } ?: 0
            _cartsCountNonEmpty.postValue(emptyCount)
        }
        // init track setting with default value
        this.postTrackSetting(
            TrackSetting(
            AppMacro.WIFI_SCAN_FIND_ALL_TIME_SEC.toInt(),
            (AppMacro.LOCATION_UPDATE_INTERVAL/ 1000).toInt(),
            AppMacro.LOCATION_REACHED_DIST_THRESHOLD_METER,
            "+9",
            AppMacro.DRIVER_PASSED_SEC_WITHOUT_STOP,
            "00:00",
            "23:59",
                120,
                1),
        )

        // init value with invalid values
        updateAppState(AppState())
        updateSystemDistCenters(listOf())
        updateSystemRetailStores(listOf())
        _cartsLoaded.postValue(mutableListOf())
    }
    private fun isLocalEnvironment(): Boolean {
        return BuildConfig.SERVER == "local"
    }

    fun updateUser(userInfo: Driver, companies: List<Company>) {
        _user.postValue(userInfo)
        _companies = companies
        if(_wsClient.isConnected())
            _wsClient.disconnect()
        _wsClient.connect(userInfo.id!!)
        _locManager.updateCompanies(_companies)
    }

    suspend fun fetchInitialInformation(): ApiResult<String> {
        AppLogger.d(TAG, "Downloading distirbution center information")
        val apiDCs =  apiLoadDistributionCenters()
        if(apiDCs !is ApiResult.Success){
            AppLogger.e(TAG, "Failed to load distribution information")
            return ApiResult.Error(ApiNotification.DC_NO_FOUND)
        }
        AppLogger.d(TAG, "Download Store information")
        val apiStores = apiLoadStoresOfArea()
        if(apiStores !is ApiResult.Success){
            AppLogger.e(TAG, "Failed to load store information of areas")
            return ApiResult.Error(ApiNotification.STORE_NO_FOUND)
        }

        AppLogger.d(TAG, "Download user photo")
        val apiUserPhoto =  apiDownloadUserPhoto()
        if(apiUserPhoto !is ApiResult.Success){
            AppLogger.e(TAG, "Failed to download user photo")
        }
        AppLogger.d(TAG, "Download task information")
        val query_res =  apiQueryDelivertaskState(apiDCs.data, apiStores.data)

        AppLogger.d(TAG, "Fetch deliver course information")
        fetchCoursePoints()
        AppLogger.d(TAG, "Fetch week speed information")
        fetchWeekSpeedInformation()
        AppLogger.d(TAG, "Fetch tracking setting")
        apiReadTrackSetting()
        return query_res
    }

    suspend fun fetchDelivertaskState(): ApiResult<String> {
        // return apiQueryDelivertaskState()
        return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
    }

    suspend fun refreshJourneyState(): ApiResult<String> {
        return try{
            updateAppState(_deliverAppState.value)
            ApiResult.Success("Success to refresh journey information")
        }
        catch (e: Exception){
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    fun getUserCompany(): String{
        val user = _user.value
        if(user == null){
            AppLogger.d(TAG, "Invalid user information")
            return ""
        }
        val company = _companies.findLast { it.name == user.company}
        if(company == null){
            AppLogger.d(TAG, "Invalid company information")
            return ""
        }
        return company.id
    }

    suspend fun startDeliverTask(): ApiResult<String> {
        val appState = _lastAppState?.copy()
        if(appState?.dc_selected == null){
            return ApiResult.Error(ApiNotification.DC_REQUIRE_CONFIRM)
        }
        return apiLeaveDistCenter(appState!!.dc_selected!!.id)
    }

    suspend fun stopDeliverTask(node_type: Int, node_id: String, reason: Int, description: String?,  force: Boolean = false): ApiResult<String> {
        return apiStopTrip(node_type, node_id, reason, description, force)

    }

    // fetch all notifications
    suspend fun fetchPastNotification(): ApiResult<String> {
        //AppLogger.d(TAG, "Download notifications")
        val apiNotifies = apiReadNotifies()
        if(apiNotifies !is ApiResult.Success){
            if(apiNotifies is ApiResult.Error)
                AppLogger.d(TAG, "Failed to download notification: ${apiNotifies.message}")
            return ApiResult.Error(ApiNotification.NOTIFY_FETCH_FAILED)
        }
        val notifies = apiNotifies.data
        notifies.forEach{it-> postNotifyUpdated(it, NotifyEventType.UPDATE_NOTIFY)}
        return ApiResult.Success(ApiNotification.NOTIFY_FETCH_SUCCESS)
    }

    /*
    remove notification
    param: notifcation: remove only one notify if parameter isn't null.
        remove all notification if parameter is null
     */
    suspend fun removeNotification(notification: Notification?): ApiResult<String> {

        val apiRes = apiDeleteNotify(notification)
        if(apiRes !is ApiResult.Success){
            return ApiResult.Error(ApiNotification.NOTIFY_DELETE_FAILED)
        }
        if(notification != null){
            postNotifyUpdated(notification, NotifyEventType.REMOVE_NOTIFY)
        }else{
            postNotifyUpdated(null, NotifyEventType.REMOVE_ALL_NOTIFY)
        }
        return ApiResult.Success(ApiNotification.NOTIFY_DELETE_SUCCESS)
    }

    /*
    remove notification
    param: notifcation: remove only one notify if parameter isn't null.
        remove all notification if parameter is null
     */
    suspend fun markAsReadNotification(notification: Notification): ApiResult<String> {

        val apiRes = apiMarkAsReadNotify(notification)
        if(apiRes !is ApiResult.Success){
            return ApiResult.Error(ApiNotification.NOTIFY_MARKREAD_FAILED)
        }
        return ApiResult.Success(ApiNotification.NOTIFY_MARKREAD_FAILED)

    }

    suspend fun arriveCompanyWithoutCompleting(company: Company): ApiResult<String>{
        val apiRes = apiArrivedCompanyWithoutCompleting(company)
        if(apiRes !is  ApiResult.Success){
            return ApiResult.Error("Failed to arive LC without completing")
        }
        val nodeCompany = apiRes.data
        val appStatus = _lastAppState?.copy()
        val comps = appStatus?.comps!!
        val compExtend = extendCompanyProperties(company, nodeCompany)
        comps.add(compExtend)
        AppLogger.d(TAG, "arrived new company. Its extended state: $compExtend")
        appStatus.comp_arrived = compExtend
        appStatus.comps = comps
        val deliStatus = _lastDeliverState?.copy(status = TRACK_STATUS.DELIVERING.code)
        updateAppState(appStatus)
        updateDeliverState(deliStatus)
        postEntityUpdated(compExtend, EntityEventType.UPDATE_CMP)
        postLocationNotify(getArrivedMessage(company.name))
        return ApiResult.Success(ApiNotification.STORE_ARRIVED)
    }

    /**
     * @description: arrive DC to load cart
     * params:
     * @dc: distcenter to arrived
     * @startScan: stopScan manually
     */
    suspend fun arriveDistCenter(dc: DistCenter): ApiResult<String> {
        try{
            val state: AppState = _lastAppState?:AppState()

            // truck reached to new DC
            val apiResponse = apiArriveDistCenter(dc)
            if(apiResponse !is ApiResult.Success){
                //AppLogger.d(TAG, "Failed to update distribution center")
                state.dc_selected = null
                updateAppState(state)
                if(apiResponse is ApiResult.Error)
                    return ApiResult.Error(apiResponse.message)
                return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
            }
            // update DC state
            val newDeliverState: DeliveryState = apiResponse.data
            convertDeliverStateToAppState(newDeliverState, this._dcs.value!!, this._rss.value!!)
            return ApiResult.Success(getArrivedMessage(dc.name))
        }
        catch (e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    suspend fun leaveDistCenter(dc: DistCenter): ApiResult<String> {
        return apiLeaveDistCenter(dc.id)
    }

    /**
     * @description: arive DC to return the cart
     * param:
     *  @stopScan: stop scanning manually if true
     */
    suspend fun arriveDistCenterToReturnCart(dc: DistCenter): ApiResult<String> {

        val state = _lastAppState?.copy()
        if(state?.dc_selected == null){
            return ApiResult.Error(ApiNotification.DC_REQUIRE_CONFIRM)
        }

        val apiResponse = apiArriveDistCenterForReturing(state.dc_selected!!)
        if(apiResponse !is ApiResult.Success){
            //AppLogger.d(TAG, "Failed to update distribution center")
            if(apiResponse is ApiResult.Error)
                return ApiResult.Error(apiResponse.message)
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
        val dcResult: NodeDeliver = apiResponse.data
        val dcDeliver = extendDistCenterProperties(state.dc_selected!!, dcResult)
        // update app state
        state.dc_selected = dcDeliver
        state.dcs.add(dcDeliver)
        postEntityUpdated(dcDeliver, EntityEventType.UPDATE_DC)
        updateAppState(state)
        val deliverState = _lastDeliverState?.copy(status=TRACK_STATUS.RETURNING.code)
        updateDeliverState(deliverState)
        return ApiResult.Success(getArrivedMessage(dcDeliver.name))
    }


    suspend  fun loadCart(iotTag: String): ApiResult<String> {
        return try{

            var carts: MutableList<Cart> = _cartsLoaded.value?: mutableListOf()
            AppLogger.d(TAG, "trying to add cart $iotTag, Current loaded tag: $carts")
            if( carts.any{it.iot == iotTag}){
                return ApiResult.Error(ApiNotification.CART_DUPLICATED_IOT)
            }
            val state = _lastAppState!!.copy()
            if(state.dc_selected == null){
                return ApiResult.Error(ApiNotification.DC_REQUIRE_CONFIRM)
            }
            when(val apiRes: ApiResult<Cart> = apiLoadCartFromDc(state.dc_selected!!,  iotTag)){ // success to call API
                is ApiResult.Success->{
                    _cartRecentAdded = apiRes.data
                    carts.add(apiRes.data)
                    _cartsLoaded.postValue(carts)

                    var dcSelected = state.dc_selected!!
                    dcSelected.status = TRACK_STATUS.LOADING.code
                    dcSelected.itemLoaded = (dcSelected.itemLoaded?:0) + 1

                    state.dc_selected = dcSelected.copy()
                    postEntityUpdated(dcSelected, EntityEventType.UPDATE_DC)
                    ApiResult.Success(ApiNotification.CART_LOAD_SUCCESS)
                }
                is ApiResult.Error->{
                    return ApiResult.Error(apiRes.message)
                }
                else ->{
                    return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
                }
            }
        }
        catch (e: Exception){
            return ApiResult.Error(ApiNotification.CART_LOAD_FAILED)
        }
    }

    // remove cart from loaded cart
    suspend fun unloadCart(cart: Cart): ApiResult<String> {
        return try{
            // var deliverState = _lastDeliverState!!
            //AppLogger.d(TAG, "trying to remove cart $cart")
            val carts= _cartsLoaded.value ?: mutableListOf()
            // val cartsDc = _carts_dc.value?: mutableListOf()
            val itemIndex =  carts.indexOfFirst { it.iot == cart.iot }
            if(itemIndex < 0)
                ApiResult.Error(ApiNotification.CART_INVALID_TAG)

            val state = _lastAppState?.copy()
            if(state?.dc_selected == null){
                return ApiResult.Error(ApiNotification.DC_REQUIRE_CONFIRM)
            }
            when(val apiRes = apiUnloadCart(state.dc_selected!!,cart) ){
                is ApiResult.Success ->{// success to remove cart
                    carts.removeAt(itemIndex)
                    _cartRecentRemoved = cart
                    // cartsDc.add(cart)
                    _cartsLoaded.postValue(carts)
                    // _carts_dc.postValue(cartsDc)
                    val dcUpdated = extendDistCenterProperties(state.dc_selected!!, apiRes.data)
                    postEntityUpdated(dcUpdated, EntityEventType.UPDATE_DC)
                    ApiResult.Success(ApiNotification.CART_UNLOAD_SUCCESS)
                }
                is ApiResult.Error->{ // failed to remove cart from db
                    ApiResult.Error(apiRes.message)
                }
                else ->{
                    ApiResult.Error(ApiNotification.INVALID_RESPONSE)
                }
            }
        }
        catch (e: Exception){
            return ApiResult.Error(ApiNotification.CART_UNLOAD_FAILED)
        }
    }

    // rturn empty cart to distribution center
    suspend fun returEmptyCart(cart: Cart): ApiResult<String> {
        return try{
            val deliverState = _lastDeliverState?.copy()?:return ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)

            val carts= _cartsLoaded.value ?: mutableListOf()


            val state = _lastAppState?.copy()
            if(state?.dc_selected == null){
                return ApiResult.Error(ApiNotification.DC_REQUIRE_CONFIRM)
            }

            if(deliverState.status == TRACK_STATUS.PROGRESS.code  ){ // return cart to DC after completing task
                val arriveResult = arriveDistCenterToReturnCart(state.dc_selected!!)
                if(arriveResult !is ApiResult.Success)
                    return arriveResult
            }

            when(val apiRes = apiReturnCart(state.dc_selected!!.node_id!!,cart) ){
                is ApiResult.Success ->{// success to remove cart
                    // carts.removeAt(itemIndex)
                    _cartRecentRemoved = cart
                    // cartsDc.add(cart)
                    _cartsLoaded.postValue(carts)
                    // _carts_dc.postValue(cartsDc)
                    //AppLogger.d(TAG, "Success to return cart. DC status: ${apiRes.data}")
                    val dcUpdated = extendDistCenterProperties(state.dc_selected!!, apiRes.data)
                    postEntityUpdated(dcUpdated, EntityEventType.UPDATE_DC)
                    ApiResult.Success(ApiNotification.CART_RETURN_SUCCESS)
                }
                is ApiResult.Error->{ // failed to remove cart from db
                    ApiResult.Error(apiRes.message)
                }
                else ->{
                    ApiResult.Error(ApiNotification.INVALID_RESPONSE)
                }
            }

        }
        catch (e: Exception){
            return ApiResult.Error(ApiNotification.CART_RETURN_FAILED)
        }
    }


    // add store to route course
    suspend fun selectStoreForTrip(store: Store): ApiResult<String> {
        try{
            val state = _lastAppState!!.copy()
            val rss = _rss.value
            var itemIndex = rss?.indexOfFirst { it.id == store.id } ?:-1
            if(itemIndex< 0){
                return ApiResult.Error(ApiNotification.STORE_NO_FOUND)
            }
            itemIndex = state.rss.indexOfFirst{ it.id == store.id }
            if(itemIndex >= 0){
                //AppLogger.d(TAG, "duplicated store index: $itemIndex")
                return ApiResult.Error(ApiNotification.STORE_DUP_SELECT)
            }
            val apiRes = apiStoreSelectForTrip(store.id)
            if(apiRes !is ApiResult.Success){
                return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
            }
            state.rss.add(store)
            val storeExtend = extendStoreProperties(store, apiRes.data)

            postEntityUpdated(storeExtend, EntityEventType.UPDATE_RS)
            updateAppState(state)
            return ApiResult.Success(ApiNotification.STORE_SELECT_SUCCESS)
        }
        catch(e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    // remove store from root course
    suspend fun deseclectStoreFromTrip(store: Store): ApiResult<String> {
        try{
            val stateApp = _lastAppState!!.copy()
            //val state = _deliverState.value
            var isFind = _rss.value!!.any{it.id == store.id}
            if(!isFind){
                return ApiResult.Error(ApiNotification.STORE_NO_FOUND)
            }
//            isFind = stateApp.rss.any() { it.id == store.id }
//            if(isFind){
//                return ApiResult.Error(ApiNotification.STORE_DUP_SELECT)
//            }

            val apiRes = apiStoreDeselectForTrip(store.id)
            if(apiRes !is ApiResult.Success){
                return apiRes
            }

            var bRemoved = stateApp.rss.remove(store)

            postEntityUpdated(store, EntityEventType.REMOVE_RS)
            updateAppState(stateApp)
            return if(bRemoved)
                ApiResult.Success(ApiNotification.STORE_DESELECT_SUCCESS)
            else
                ApiResult.Error(ApiNotification.STORE_DESELECT_FAILED)
        }
        catch(e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    /**
     * @description: driver reached this store on his route
     * @params:
     *  stopScan: stop scanning if true
     */

    suspend fun arrivedStore(store: Store): ApiResult<String> {
        return try{
            // check if this store exists in course
            val rss = _rss.value!!
            val state = _lastAppState?.copy()
            val storeSelected = state!!.rs_arrived
            val index = rss.indexOfFirst{it.id == store.id}
            if(index < 0){
                AppLogger.d(TAG, "arrived at invalid store")
                ApiResult.Error(ApiNotification.STORE_NO_FOUND)
            }
            if(store.id == storeSelected?.id){ // select the same store
                AppLogger.d(TAG, "Arrived at the same store. deliver store status: ${state.rss[index]}")
                if(storeSelected.status == NODE_STATUS.REACHED.code){
                    ApiResult.Success(ApiNotification.STORE_ARRIVED)
                }
            }

            val apiRes: ApiResult<NodeDeliver> = apiArriveStore(store)
            if(apiRes !is ApiResult.Success){
                AppLogger.w(TAG, "Failed to rrive store after api")
                if(apiRes is ApiResult.Error){
                    AppLogger.w(TAG, "get error in store arrival api. ${apiRes.message}")
                    return ApiResult.Error(apiRes.message)
                }
                return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
            }
            val appStatus = _lastAppState?.copy()

            val storeDeliver: NodeDeliver = apiRes.data
            val stores = appStatus?.rss!!

            val storeExtend = extendStoreProperties(store, storeDeliver)
            stores.add(storeExtend)
            AppLogger.d(TAG, "arrived new store. Its extended state: $storeExtend")
            appStatus.rs_arrived = storeExtend
            appStatus.rss = stores
            var deliStatus = _lastDeliverState?.copy(status = TRACK_STATUS.DELIVERING.code)
            updateAppState(appStatus)
            updateDeliverState(deliStatus)
            postEntityUpdated(storeExtend, EntityEventType.UPDATE_RS)
            ApiResult.Success(ApiNotification.STORE_ARRIVED)
        }
        catch (e: Exception){
            AppLogger.e(TAG, "exception in process arrivalStore: ${e.message}")
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    // deliver cart from truck to retail store
    suspend fun deliverCart(cart: Cart): ApiResult<String> {
        return try{
            // check if cart exists in the current loaded cart
            val carts = _cartsLoaded.value ?: return ApiResult.Error(ApiNotification.NOTIFY_NO_CART)
            val index = carts.indexOf(cart)
            // run api to remove cart
            if(index < 0){
                return ApiResult.Error(ApiNotification.CART_INVALID_TAG)
            }

            val appStatus = _lastAppState?.copy()
            if(appStatus?.rs_arrived == null){
                return ApiResult.Error(ApiNotification.STORE_CONFIRM_ARRIVAL)
            }

            val apiRes = apiDeliverCart(cart)
            when(apiRes){
                is ApiResult.Success->{
                    // remove cart
                    carts.remove(cart)
                    _cartsLoaded.postValue(carts)

                    val store = extendStoreProperties(appStatus!!.rs_arrived!!, apiRes.data)
                    //_entityStoreUpdated.postValue(Event(store))
                    postEntityUpdated(store, EntityEventType.UPDATE_RS)
                    return ApiResult.Success(ApiNotification.CART_DELIVER_SUCCESS)
                }
                is ApiResult.Error ->{
                    return ApiResult.Error(apiRes.message)
                }
                else->{
                    return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
                }
            }
        }
        catch(e:Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    // pickup cart from retail store
    suspend fun pickupCartFromStore(iot: String): ApiResult<String> {
        return try{
            val status = _lastAppState!!.copy()
            val store = status.rs_arrived
            //AppLogger.d(TAG, "pickup cart from store $iot: ${store}")
            val carts: MutableList<Cart> = _cartsLoaded.value?: mutableListOf()
            if( carts.any{it.iot == iot}){
                AppLogger.e(TAG, "Duplicated IOT tag: $iot")
                return ApiResult.Error(ApiNotification.CART_ALREADY_DELIVERED)
            }
            if(store == null){
                return ApiResult.Error(ApiNotification.STORE_CONFIRM_ARRIVAL)
            }
            when(val apiRes: ApiResult<Cart> = apiPickupCart(iot, store!!)){ // success to call API
                is ApiResult.Success->{
                    carts.add(apiRes.data)
                    _cartsLoaded.postValue(carts)
                    ApiResult.Success(ApiNotification.CART_PICKUP_SUCCESS)
                }
                is ApiResult.Error->{
                    ApiResult.Error(apiRes.message)
                }
                else ->{
                    ApiResult.Error(ApiNotification.INVALID_RESPONSE)
                }
            }
        }
        catch (e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    /**
     * @description: truck leaves the store
     * @param:
     *  startScan: Start scanning if true
     */
    suspend fun leaveStore(): ApiResult<String> {
        return try{
            // change the last state.
            val appStatus = _lastAppState!!.copy()
            if(appStatus.rs_arrived == null) {
                AppLogger.w(TAG, "Cannot leave store. confirm the arrived retail store")
                return ApiResult.Error(ApiNotification.STORE_CONFIRM_ARRIVAL)
            }
            when(val apiRes = apiLeaveStore()){
                is ApiResult.Success->{

                    val store: Store = appStatus.rs_arrived!!
                    val storeDeliver = apiRes.data.nodes?.find { it.nodeid == store.id && it.nodetype == NODE_TYPE.STORE.code }
                    if(storeDeliver!=null){
                        val storeExt = extendStoreProperties(store, storeDeliver)
                       // _entityStoreUpdated.postValue(Event(storeExt))
                        postEntityUpdated(storeExt, EntityEventType.UPDATE_RS)
                    }
                    appStatus.rs_arrived = null
                    updateAppState(appStatus)
                    convertDeliverStateToAppState(apiRes.data, _dcs.value!!, _rss.value!!)

                    return ApiResult.Success(ApiNotification.STORE_LEAVE)
                }
                is ApiResult.Error->{
                    return ApiResult.Error(apiRes.message)
                }
                else ->{
                    return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
                }
            }
        }
        catch( e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    // push gps location into server
    suspend fun pushGpsLocation(latitude: Double, longitude: Double, fuel: String = "", co2: String=""): ApiResult<String> {
         return try
         {
            AppLogger.d(TAG, "trying to update the status of location manager")
            val locInformation: Pair<ServerPoint?, Int> = _locManager.addPoint(LatLng(latitude, longitude))
            AppLogger.d(TAG, "Success to update the status of location manager")
            // update the current direction
            _tripDirection.postValue(locInformation.second)

            if(this._trackCompleted){ // track already completed
                AppLogger.d(TAG, "task is completed")
                return ApiResult.Success("Track is completed. No need to push location to server")
            }

            // calling API only when task is on progress
            val point = locInformation.first ?: return ApiResult.Success(ApiNotification.GPS_PUSH_TEMP_VALUE)
            if(getElapsedTimeSec(_timeAddLastSpd, Date()) >= 60 * 10){ // add speed point in every 10 minutes
                postCoursePoint(point)
                _timeAddLastSpd = Date()
            }
            return when(val apiRes = apiPushGpsLocation(point, fuel, co2)){
                is ApiResult.Success->{
                    ApiResult.Success("Successfully push GPS location")
                }
                is ApiResult.Error->{
                    AppLogger.e(this.TAG, "Error in gps push API: ${apiRes.message}")
                    ApiResult.Error(apiRes.message)
                }
                else ->{
                    ApiResult.Error(ApiNotification.INVALID_RESPONSE)
                }
            }
        }
        catch( e: Exception){
            AppLogger.e(this.TAG, "Exception in push gps coordinate: ${e.message}")
            return ApiResult.Error("Exception in push GPS coordinate:${e.message}")
        }
    }

    private fun fetchCoursePoints(){
        val apiCourseRes =  apiFetchCoursePoints()
        if(apiCourseRes is ApiResult.Success){
            AppLogger.d(TAG, "Success to load deliver course points")
            _deliverCoursePoints.postValue(apiCourseRes.data)
        }
        else{
            AppLogger.w(TAG, "Failed to load deliver course points")
            if(apiCourseRes is ApiResult.Error)
                AppLogger.w(TAG, "${apiCourseRes.message}")
            _deliverCoursePoints.postValue(null)
        }
    }

    fun fetchWeekSpeedInformation(){
        val apiWeekRes = apiFetchWeekSpeedInformation()
        if(apiWeekRes is ApiResult.Success){
            //AppLogger.d(TAG, "Success to load week speed information")
            _deliverWeekSpeed.postValue(apiWeekRes.data)
        }else{
            AppLogger.w(TAG, "Failed to load week speed information")
            _deliverWeekSpeed.postValue(null)
        }

    }

    suspend fun updateUserProfile(
        company: RequestBody,
        name: RequestBody,
        phone: RequestBody,
        email: RequestBody,
        address: RequestBody,
        password: RequestBody,
        license: RequestBody,
        photo: MultipartBody.Part?
    ): ApiResult<String> {
        return try{
            AppLogger.d(TAG, "company in repository: ${company.toString()}")
            when(val apiRes = apiUpdateUserProfile( company = company, name = name, phone = phone,
                email = email, address =  address, password = password, license =license, photo = photo)){
                is ApiResult.Success->{
                    _user.postValue(apiRes.data)
                    apiDownloadUserPhoto()
                    saveUserInfomration(apiRes.data)
                    ApiResult.Success(ApiNotification.USER_SUCCESS_UPDATE)
                }
                is ApiResult.Error->{

                    ApiResult.Error(apiRes.message)
                }
                else ->{
                    ApiResult.Error(ApiNotification.USER_FAILED_UPDATE)
                }
            }
        }
        catch( e: Exception){
            AppLogger.e(TAG, "exception in update user profile: ${e.message}")
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }


    suspend fun updateTruckInformation(
        user: Driver
    ): ApiResult<String> {
        return try{
            when(val apiRes = apiUpdateTruckVin(user)){
                is ApiResult.Success->{
                    _user.postValue(apiRes.data)
                    _locManager.updateDriverSetting(apiRes.data)
                    saveUserInfomration(apiRes.data)
                    ApiResult.Success(ApiNotification.USER_SUCCESS_UPDATE)
                }
                is ApiResult.Error->{
                    ApiResult.Error(apiRes.message)
                }
                else ->{
                    ApiResult.Error(ApiNotification.USER_FAILED_UPDATE)
                }
            }
        }
        catch( e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    suspend fun deleteDriverAccount(): ApiResult<String> {
        return try{
            apiDeleteDriverAccount()
        }
        catch( e: Exception){
            return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
        }
    }

    /*
    function: check if current location is vicinity of the selected company
    parameters:
        @company: company object to start the deliver task
        @curLocation: Current location of driver
     */
    fun checkIfCompanyAround(company: Company, curLocation: LatLng?):ApiResult<String>{
        if(curLocation == null){
            return ApiResult.Error(ApiNotification.DELIVER_START_AFTER_GPS_LOCATION)
        }
        var locComp =  LatLng(company.latitude.toDouble(), company.longitude.toDouble())
        var distance = haversineDistanceMeter(locComp, curLocation)
        val trackSetting = this._trackSetting.value
        val distanceThreshold = trackSetting?.distanceMeter ?: 500

        if(distance <= distanceThreshold){
            return ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
        }
        // check distance from parking
        if(company.latitudePark.isNullOrBlank() || company.longitudePark.isNullOrBlank())
            return ApiResult.Error(ApiNotification.DELIVER_WRONG_START_LOCATION)

        locComp =  LatLng(company.latitudePark.toDouble(), company.longitudePark.toDouble())
        distance = haversineDistanceMeter(locComp, curLocation)
        if(distance <= distanceThreshold){
            return ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
        }
        return ApiResult.Error(ApiNotification.DELIVER_WRONG_START_LOCATION)
    }

    // start deliver task from logistic company
    suspend fun apiStartDeliverTask(company: Company ): ApiResult<String>{
        return try {
            val user = _user.value ?: return ApiResult.Error(ApiNotification.USER_INVALID_INFO)
            val dcs = _dcs.value?: emptyList()
            val stores = _rss.value?: emptyList()

            val res = apiService.startDeliverTask(user.id!!, user.truck, company.id, true).execute()
            if(res.isSuccessful){
                val apiResponse: ApiResponse<DeliveryState> = res.body()!!
                if (apiResponse.error_code == ErrorCode.TZ_SUCCESS) {   // success to start deliver task
                    val deliverState: DeliveryState = apiResponse.data
                    AppLogger.d(TAG, "Success to start deliver task: ${deliverState}")
                    if(deliverState.nodes.isNotEmpty()){
                        val lastNode = deliverState.nodes.last()
                        AppLogger.d(TAG, "Success to start deliver task. current node size: last node : $lastNode")
                        _locManager.setLastNodeItem(lastNode)
                    }
                    postEntityUpdated(Unit, EntityEventType.INIT_TRIP_NOTIFY)
                    updateDeliverState(deliverState)
                    convertDeliverStateToAppState(deliverState, dcs, stores)
                    ApiResult.Success(ApiNotification.DELIVER_START_SUCCESS)
                }
                else{
                    updateDeliverState(null)
                    updateAppState(null)
                    AppLogger.e(TAG, "Invalid response: ${apiResponse.meta}")
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                AppLogger.e(TAG, "api request is failed: ${res}")
                updateDeliverState(null)
                updateAppState(null)
                ApiResult.Error(ApiNotification.DELIVER_START_FAILED)
            }
        }
        catch (e: Exception){
            AppLogger.e(TAG, "Exception in connection  in starting delivery task : ${e.message}")
            updateDeliverState(null)
            updateAppState(null)
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }

    }


    // start deliver task either from DC or LC automatically
    private suspend fun apiStartDeliverTaskAutomatically(entity: GpsEntity ): ApiResult<String>{
        return try {
            val user = _user.value ?: return ApiResult.Error(ApiNotification.USER_INVALID_INFO)
            val dcs = _dcs.value?: emptyList()
            val stores = _rss.value?: emptyList()

            var idComp = ""
            var idDc = ""
            if(entity is GpsEntity.cmp){
                idComp = entity.cmp.id
            }
            else if(entity is GpsEntity.dc){
                idDc = entity.dc.id
            }
            AppLogger.d(TAG, "Start deliver task automatically from ${idComp}, ${idDc}")
            val res = apiService.startDeliverTaskAuto(user.id!!, user.truck, idComp, idDc, true).execute()
            if(res.isSuccessful){
                val apiResponse: ApiResponse<DeliveryState> = res.body()!!
                if (apiResponse.error_code == ErrorCode.TZ_SUCCESS) {   // success to start deliver task
                    val deliverState: DeliveryState = apiResponse.data
                    AppLogger.d(TAG, "Success to start deliver task: ${deliverState}")
                    if(deliverState.nodes.isNotEmpty()){
                        val lastNode = deliverState.nodes.last()
                        AppLogger.d(TAG, "Success to start deliver task. current node size: last node : $lastNode")
                        _locManager.setLastNodeItem(lastNode)
                    }
                    postEntityUpdated(Unit, EntityEventType.INIT_TRIP_NOTIFY)
                    updateDeliverState(deliverState)
                    convertDeliverStateToAppState(deliverState, dcs, stores)
                    ApiResult.Success(ApiNotification.DELIVER_START_SUCCESS)
                }
                else{
                    AppLogger.e(TAG, "Failed to start deliver task automatically: ${apiResponse.meta}")
                    updateDeliverState(null)
                    updateAppState(null)
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                AppLogger.e(TAG, "api request is failed: ${res}")
                updateDeliverState(null)
                updateAppState(null)
                ApiResult.Error(ApiNotification.DELIVER_START_FAILED)
            }
        }
        catch (e: Exception){
            AppLogger.e(TAG, "Exception in connection  in starting delivery task : ${e.message}")
            updateDeliverState(null)
            updateAppState(null)
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }



    private fun apiQueryDelivertaskState(distCenters: List<DistCenter>, stores: List<Store>): ApiResult<String> {
        AppLogger.d(TAG, "Fetch a deliver state of truck")
        return try {
            val user = _user.value!!
            val res = apiService.queryDeliverStatus(user.id!!, user.truck, false).execute()
            if(res.isSuccessful){
                val apiResponse: ApiResponse<DeliveryState> = res.body()!!
                if (apiResponse.error_code == 0) {
                    AppLogger.d(TAG, "Success to load deliver state")
                    val deliverState: DeliveryState = apiResponse.data
                    updateDeliverState(deliverState)
                    convertDeliverStateToAppState(deliverState, distCenters, stores)
                    /*if(deliverState.nodes.isNotEmpty()){
                        val node = deliverState.nodes.last()
                        this._locManager.setLastNodeItem(node)
                    }*/
                    ApiResult.Code(ErrorCode.TZ_SUCCESS)
                }
                else{
                    updateDeliverState(null)
                    updateAppState(null)
                    if(apiResponse.error_code == ErrorCode.TZ_NO_DELIVER_TASK){
                        AppLogger.d(TAG, "No deliver task. Init task automatically")
                        if(AppMacro.AUTO_START_TRIP) {
                            AppLogger.d(TAG, "No deliver task now")
                            postEntityUpdated(Unit, EntityEventType.INIT_TRIP)
                        }
                        return ApiResult.Code(ErrorCode.TZ_NO_DELIVER_TASK)
                    }
                    AppLogger.e(TAG, "Failed to load deliver task. Invalid response: ${apiResponse.meta}")
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                AppLogger.e(TAG, "api request is failed: ${res}")
                updateDeliverState(null)
                updateAppState(null)
                ApiResult.Error(ApiNotification.DELIVER_LOAD_FAILED)
            }
        }
        catch(e:Exception){
            AppLogger.e(TAG, "Exception in connection  in apiQueryDelivertaskState : ${e.message}")
            updateDeliverState(null)
            updateAppState(null)
            ApiResult.Error(ApiNotification.DELIVER_LOAD_FAILED)
        }
    }

    // load distribution center of selected area
    private fun apiLoadDistributionCenters(): ApiResult<List<DistCenter>> {
        //AppLogger.d(TAG, "Load all DCs from database")
        return try {
            val apiResDist = apiService.getDistCenterNames().execute()
            if(apiResDist.isSuccessful){
                val apiResponse: ApiResponse<List<DistCenter>> = apiResDist.body()!!
                if (apiResponse.error_code == 0) {
                    updateSystemDistCenters(apiResponse.data)
                    //AppLogger.d(TAG, "Success to download distribution center: ${apiResponse.data}")
                    ApiResult.Success(apiResponse.data)
                } else {
                    //AppLogger.d(TAG, "Failed to get distribution center: ${apiResponse.error_code}: ${apiResponse.meta}")
                    updateSystemDistCenters(listOf())
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e: Exception){
            AppLogger.e(TAG, "Excepton in loading DC: ${e.message}")
            updateSystemDistCenters(listOf())
            return ApiResult.Error( ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiArriveDistCenter(dc: DistCenter): ApiResult<DeliveryState> {
        return try {
            val state = _lastDeliverState?.copy()
            AppLogger.d(TAG, "Api: reached to new dist center: ${state!!.id}")
            val apiResDist = apiService.arriveDistCenter(state!!.id, dc.id).execute()
            if(apiResDist.isSuccessful){
                val apiResponse: ApiResponse<DeliveryState> = apiResDist.body()!!
                AppLogger.d(TAG, "Success to arrive dist center: $apiResponse")
                when (apiResponse.error_code) {
                    ErrorCode.TZ_SUCCESS -> {
                        ApiResult.Success(apiResponse.data)
                    }
                    ErrorCode.TZ_DUPLICATED_DISTCENTER -> {
                        AppLogger.d(TAG, "This distribution center is already reached")
                        return ApiResult.Code(ErrorCode.TZ_DUPLICATED_DISTCENTER)
                    }
                    else -> {
                        AppLogger.d(TAG, "Failed to update distribution center: ${apiResponse.error_code}: ${apiResponse.meta}")
                        ApiResult.Error(apiResponse.meta)
                    }
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e: Exception){
            AppLogger.d(TAG, "Failed to notice for distribution center arrival: ${e.message}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiArriveDistCenterForReturing(dc: DistCenter): ApiResult<NodeDeliver> {
        return try {
            AppLogger.d(TAG, "Api: reached to new dist center")
            val state = _lastDeliverState?.copy()
            val apiResDist = apiService.arriveDistCenterForReturning(state!!.id, dc.id).execute()
            if(apiResDist.isSuccessful){
                val apiResponse: ApiResponse<NodeDeliver> = apiResDist.body()!!
                when (apiResponse.error_code) {
                    ErrorCode.TZ_SUCCESS -> {
                        ApiResult.Success(apiResponse.data)
                    }
                    ErrorCode.TZ_DUPLICATED_DISTCENTER -> {
                        //AppLogger.d(TAG, "This distribution center is already reached")
                        return ApiResult.Code(ErrorCode.TZ_DUPLICATED_DISTCENTER)
                    }
                    else -> {
                        //AppLogger.d(TAG, "Failed to update distribution center: ${apiResponse.error_code}: ${apiResponse.meta}")
                        ApiResult.Error(apiResponse.meta)
                    }
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e: Exception){
            //AppLogger.d(TAG, "Failed to notice for distribution center arrival: ${e.message}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiLeaveDistCenter(dc_id: String): ApiResult<String> {
        //AppLogger.d(TAG, "Leave distribution center: $dc_id")
        return try {
            val deliverState =_lastDeliverState?.copy()
            //AppLogger.d(TAG, "Leave distribution centoer: $deliverState")
            val dcs = _dcs.value
            val apiResDist = apiService.leaveDistCenter(deliverState!!.id, dc_id).execute()
            if(apiResDist.isSuccessful){
                val apiResponse = apiResDist.body()!!
                if (apiResponse.error_code == ErrorCode.TZ_SUCCESS) {
                    val deliverStateNew = apiResponse.data
                    convertDeliverStateToAppState(deliverStateNew, dcs!!, _rss.value!!)

                    //AppLogger.d(TAG, "new distribution centers: ${deliverState.dcs}")
                    if(!deliverStateNew.nodes.isNullOrEmpty()){// update the distcenter of leaved
                        val dcDeliverLeave = deliverStateNew.nodes.findLast { it.nodetype == NODE_TYPE.DC.code && it.nodeid == dc_id}
                        val dc = dcs.findLast { it.id == dc_id }
                        if(dcDeliverLeave!=null && dc != null) {
                            val dcLeave: DistCenter = extendDistCenterProperties(dc, dcDeliverLeave)
                            //AppLogger.d(TAG, "Success to update the leaved distribution center: $dcLeave")
                            postEntityUpdated(dcLeave, EntityEventType.UPDATE_DC)

                        }else{
                            AppLogger.e(TAG, "Failed to find the matched dc in downloaded list")
                        }
                    }else{
                        AppLogger.e(TAG, "No distribution center found")
                    }
                    ApiResult.Success(ApiNotification.DELIVER_START_TRIP)
                } else {
                    val msg = "Failed to leave distribution center: ${apiResponse.meta}"
                    AppLogger.e(TAG, msg)
                    ApiResult.Error(msg)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e: Exception){
            // val msg = "Failed to leave distribution center: ${ApiNotification.INVALID_RESPONSE}"
            AppLogger.e(TAG, e.message?:"")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // push one gps points into server
    private fun apiPushGpsLocation(point: ServerPoint, fuel: String = "", co2: String = ""): ApiResult<String> {
        AppLogger.d(this.TAG, "Try to push GPS location to server")
        return try {
            val state = _lastDeliverState?.copy()
            if(state == null){
                AppLogger.w(TAG, "Deliver state is invalid")
                return ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            AppLogger.d(TAG, "push gps location")
            val apiResDist = apiService.pushGpsLocation(state.id, state.route, point.latitude, point.longitude, point.speed, point.elapsedSec.toInt(), fuel, co2).execute()
            if(apiResDist.isSuccessful){
                val apiResponse: ApiResponse<Unit> = apiResDist.body()!!
                if (apiResponse.error_code == ErrorCode.TZ_SUCCESS) {
                    AppLogger.d(TAG, "Success to push GPS location into sever")
                    ApiResult.Success(ApiNotification.LOCATION_TRACK_SUCCESS)
                } else {
                    AppLogger.e(TAG, "Failed to push GPS location:${apiResponse.meta}: ${apiResponse.error_code}")
                    ApiResult.Error("${ApiNotification.LOCATION_TRACK_FAILED}: ${apiResponse.meta}")
                }
            }
            else{
                AppLogger.e(this.TAG, "Invalid response for apiPushGpsLocation")
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e: Exception){
            AppLogger.e(this.TAG, "Exception in apiPushGpsLocation: ${e.message}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // read all points information of server
    private fun apiFetchCoursePoints(): ApiResult<List<ServerPoint>> {
        //AppLogger.d(TAG, "fetch course points information")
        return try{
            val state = _lastDeliverState?.copy()
            if(state == null){
                AppLogger.w(TAG, "Deliver state is invalid")
                return ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            AppLogger.d(TAG, "fetching course point. Deliver state id: ${state.id}")
            val apiResult = apiService.readCourseInformation(state.id).execute()
            if(apiResult.isSuccessful){
                val apiResponse: ApiResponse<List<ServerPoint>> = apiResult.body()!!
                if (apiResponse.error_code == ErrorCode.TZ_SUCCESS) {
                    AppLogger.d(TAG, "Success to read course points：")
                    ApiResult.Success(apiResponse.data)
                } else {
                    AppLogger.e(TAG, "Failed to read course location information:${apiResponse.meta}")
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.w(TAG, "Failed to read course points information: ${e.message}")
            return ApiResult.Error(ApiNotification.DELIVER_COURSE_READ_FAILED)
        }
    }

    // read average speed information of one week
    private fun apiFetchWeekSpeedInformation(): ApiResult<List<ServerPoint>> {
        //AppLogger.d(TAG, "fetch course points information")
        return try{
            val userInfo = _user.value ?: return ApiResult.Error(ApiNotification.USER_INVALID_INFO )

            val apiResult = apiService.readWeekSpeedInformation(userInfo.truck, 7).execute()
            if(apiResult.isSuccessful){
                val apiResponse: ApiResponse<List<ServerPoint>> = apiResult.body()!!
                if (apiResponse.error_code == ErrorCode.TZ_SUCCESS) {
                    //AppLogger.d(TAG, "Success to read week speed information")
                    ApiResult.Success(apiResponse.data)
                } else {
                    AppLogger.e(TAG, "Failed to read week speed information:${apiResponse.meta}")
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.w(TAG, "Failed to read week speed information: ${e.message}")
            return ApiResult.Error(ApiNotification.DELIVER_COURSE_READ_FAILED)
        }
    }


    private fun apiFetchCartFromDistCenter(dcId: String, postEvent: Boolean = true): List<Cart>{
        return try {
            val apiResDist = apiService.getCartsOfDistCenter(dcId).execute()
            if(apiResDist.isSuccessful){
                val apiResponse: ApiResponse<List<Cart>> = apiResDist.body()!!
                if (apiResponse.error_code == 0) {
                    val data: List<Cart> = apiResponse.data
                    return data

                } else {
                    //AppLogger.d(TAG, "Failed to load carts of distribution center: ${apiResponse.error_code}: ${apiResponse.meta}")
                    emptyList()
                }
            }
            else{
                //AppLogger.d(TAG, "Failed to load carts. request is failed")
                emptyList()
            }
        }
        catch (e: Exception){
            //AppLogger.d(TAG, "Failed to load carts from distribution center: ${e.message}")
            return emptyList()
        }
    }

    private fun apiLoadStoresOfArea(): ApiResult<List<Store>> {
        //AppLogger.d(TAG, "Load all RSs from database")
        return try {
            val apiResStores = apiService.getStoresOfArea().execute()
            if (apiResStores.isSuccessful) {
                val apiResponse: ApiResponse<List<Store>> = apiResStores.body()!!
                if (apiResponse.error_code == 0) {
                    //AppLogger.d(TAG, "Success to load RSS: ${apiResponse.data.size}")
                    updateSystemRetailStores(apiResponse.data)
                    ApiResult.Success(apiResponse.data)
                } else {
                    //AppLogger.d(TAG, "Failed to get store information of area: ${apiResponse.error_code}: ${apiResponse.meta}")
                    updateSystemRetailStores(listOf())
                    ApiResult.Error(apiResponse.meta)
                }
            } else {
                //AppLogger.d(TAG, "Failed ot get store")
                updateSystemRetailStores(listOf())
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        } catch (e: Exception) {
            //AppLogger.d(TAG, "Failed to get retail stores: ${e.message}")
            updateSystemRetailStores(listOf())
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    function: Load cart from DC to truck
     */
    private fun apiLoadCartFromDc(dc: DistCenter, iotTag: String): ApiResult<Cart> {
        return try{
            val deliverState = _lastDeliverState?.copy()
            val response = apiService.loadCart(deliverState?.id!!, NODE_TYPE.DC.code, dc.id, iotTag).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    //AppLogger.d(TAG, "Failed to load cart: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Exception in loading cart: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
   function: Load cart from DC to truck
    */
    private fun apiLoadCartByTagFromDc(nodeid: String, beacons: List<BeaconExport>): ApiResult<DistCenterDeliverResult> {
        return try{
            //AppLogger.d(TAG, "Load ${beacons.size} carts found by scanner")
            if(beacons.isEmpty()){
                AppLogger.w(TAG, "No beacons to load")
                return ApiResult.Error(ApiNotification.NOTIFY_NO_CART)
            }
            val deliverState = _lastDeliverState?.copy()

            val ids = beacons.map { it->it.tag }
            val tags = ids.joinToString ("," )
            //AppLogger.d(TAG, "tag lists for loadcart from dc by tag: ${tags}")
            val response = apiService.loadCartsByTag(deliverState?.id!!, NODE_TYPE.DC.code, nodeid, tags).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    AppLogger.w(TAG, "Failed to load cart: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Exception in loading cart: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    function: remove cart from truck to DC
     */
    private fun apiUnloadCart(dc: DistCenter?, cart: Cart): ApiResult<NodeDeliver> {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            val response = apiService.unloadCart(deliStatus?.id!!, NODE_TYPE.DC.code, dc!!.id, cart.id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            //AppLogger.d(TAG, "FAiled to unload cart to distribution center: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiReturnCart(node_id: String, cart: Cart): ApiResult<NodeDeliver> {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            val response = apiService.returnCart(deliStatus?.id!!,  node_id, cart.id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error("${ApiNotification.CART_RETURN_FAILED}: ${apiRes.meta}")
                }
            }
            else{
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            //AppLogger.d(TAG, "FAiled to unload cart to distribution center: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    function: remove cart from truck to DC
     */
    private fun apiReturnCartByTag(nodeid: String, beacons: List<BeaconExport>): ApiResult<DistCenterDeliverResult> {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            val tag = beacons.map { it.tag }.joinToString(",")
            val response = apiService.returnCartByTag(deliStatus?.id!!,  nodeid, tag).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    return ApiResult.Success(apiRes.data)
                }
                else{
                    return ApiResult.Error("${ApiNotification.CART_UNLOAD_FAILED}: ${apiRes.meta}")
                }
            }
            else{
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            //AppLogger.d(TAG, "FAiled to unload cart to distribution center: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    fun apiInitTrip(force_init: Boolean): ApiResult<Int> {
        //AppLogger.d(TAG, "Load all RSs from database")
        return try {
            val user = _user.value
            val dcs = _dcs.value ?: listOf()
            val rss = _rss.value ?: listOf()
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to init trip: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // check the last apk version
    suspend fun apiApkCheckLastVersion():ApiResult<ApkVersion>{
        return try{
//            if(this._versionChecked){
//                return ApiResult.Error("Apk version is already checked")
//            }
//          this._versionChecked = true
            val res = apiService.checkLastApkVersion().execute()
            if(!res.isSuccessful){
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
            val apiRes = res.body()!!
            if(apiRes.error_code != ErrorCode.TZ_SUCCESS){
                return ApiResult.Error("Failed to check last version: ${apiRes.meta}")
            }
            return ApiResult.Success(apiRes.data)
        }
        catch (e: Exception){
            AppLogger.e(TAG, "Failed to query last apk verison: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    // download user profile image
    private suspend fun  apiDownloadUserPhoto(): ApiResult<String> {
        return try {
            val user = _user.value!!
            val response = apiService.downloadPhoto(user.id!!).execute()
            if (!response.isSuccessful) {
                AppLogger.e(TAG, "failed to download: $response")
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
            // Get the image stream from the response
            val imageStream: InputStream = response.body()?.byteStream()
                ?: return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            // Decode the stream into a Bitmap
            // val bitmap = BitmapFactory.decodeStream(imageStream)
            val bitmap = decodeAndRotateBitmap(imageStream)
            if(bitmap != null) {
                _userPhoto.postValue(bitmap!!)
                AppLogger.d(TAG, "Success to download user photo")
            }
            else
            {
                AppLogger.w(TAG, "Failed to convert user photo")
            }
            return ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
        }
        catch(e: Exception){
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // download profile image of retail store
    suspend fun  apiDownloadStorePhoto(storeId: String): ApiResult<Bitmap> {
        return try {

            val response = apiService.downloadStoreImage(storeId).execute()
            if (!response.isSuccessful) {
                AppLogger.e(TAG, "Failed to download: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }

            // Get the image stream from the response
            val imageStream: InputStream = response.body()?.byteStream()
                ?: return ApiResult.Error(ApiNotification.CONNECTION_ERROR)

            // Decode the stream into a Bitmap
            // val bitmap = decodeAndRotateBitmap(imageStream)
        AppLogger.d(TAG, "Success to download store profile image in model")
            val bitmap = BitmapFactory.decodeStream(imageStream)
            if(bitmap != null) {
                return ApiResult.Success(bitmap)
            }
            else
            {
                AppLogger.w(TAG, "Failed to convert store image")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    // download profile image of retail store
    suspend fun  apiDownloadCompanyPhoto(compId: String): ApiResult<Bitmap> {
        return try {
            AppLogger.d(TAG, "Download company image of $compId")
            val response = apiService.downloadCompanyImage(compId).execute()
            if (!response.isSuccessful) {
                AppLogger.e(TAG, "Failed to download: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }

            // Get the image stream from the response
            val imageStream: InputStream = response.body()?.byteStream()
                ?: return ApiResult.Error(ApiNotification.CONNECTION_ERROR)

            // Decode the stream into a Bitmap
            // val bitmap = decodeAndRotateBitmap(imageStream)
            AppLogger.d(TAG, "Success to download company profile image in model")
            val bitmap = BitmapFactory.decodeStream(imageStream)
            if(bitmap != null) {
                return ApiResult.Success(bitmap)
            }
            else
            {
                AppLogger.w(TAG, "Failed to convert store image")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    // download profile image of distribution center
    suspend fun  apiDownloadDistPhoto(dcId: String): ApiResult<Bitmap> {
        return try {

            val response = apiService.downloadDistCenterImage(dcId).execute()
            if (!response.isSuccessful) {
                AppLogger.e(TAG, "Failed to download: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }

            // Get the image stream from the response
            val imageStream: InputStream = response.body()?.byteStream()
                ?: return ApiResult.Error(ApiNotification.CONNECTION_ERROR)

            // Decode the stream into a Bitmap
            // val bitmap = decodeAndRotateBitmap(imageStream)
            AppLogger.d(TAG, "Success to download store profile image in model")
            val bitmap = BitmapFactory.decodeStream(imageStream)
            if(bitmap != null) {
                return ApiResult.Success(bitmap)
            }
            else
            {
                AppLogger.w(TAG, "Failed to convert store image")
                return ApiResult.Error(ApiNotification.INVALID_RESPONSE)
            }
        }
        catch(e: Exception){
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    private fun apiStoreSelectForTrip(store_id: String): ApiResult<NodeDeliver> {
        return try{
            val stateDeliver = _lastDeliverState?.copy()?: return ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            val response = apiService.selectStore(stateDeliver.id, store_id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            return ApiResult.Error(e.message?: ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiStoreDeselectForTrip(store_id: String): ApiResult<String> {
        return try{
            val stateDeliver = _lastDeliverState?.copy()
            val response = apiService.deselectStore(stateDeliver?.id!!, store_id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to remove store from deliver task: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // deliver cart into store
    private fun apiDeliverCart(cart: Cart): ApiResult<NodeDeliver> {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            val appStatus = _lastAppState!!.copy()
            if(appStatus.rs_arrived==null){
                return ApiResult.Error(ApiNotification.STORE_CONFIRM_ARRIVAL)
            }
            val response = apiService.deliverCart(deliStatus?.id!!, NODE_TYPE.STORE.code, appStatus.rs_arrived!!.node_id!!, cart.id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to deliver cart: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // deliver cart into store
    private fun apiDeliverCartByTag(store: Store?, beacons: List<BeaconExport>): ApiResult<DeliveredStoreResult> {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            // val appStatus = _lastAppState!!
            if(store ==null){
                return ApiResult.Error(ApiNotification.STORE_CONFIRM_ARRIVAL)
            }
            val tags = beacons.map { it.tag }.joinToString(",")

            val response = apiService.deliverCartByTag(
                deliStatus?.id!!,
                NODE_TYPE.STORE.code,
                store.node_id!!,
                tags).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to deliver cart: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // pickup cart from retial store to truck
    private fun apiPickupCart(tag: String, store: Store): ApiResult<Cart> {
        return try{
            val deliverState = _lastDeliverState?.copy()
            val response = apiService.pickEmptyCartFromStore(deliverState?.id!!, NODE_TYPE.STORE.code, store.node_id!!, tag).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    //AppLogger.d(TAG, "Failed to pickup cart: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "exception in pickup cart: ${e.message}" )
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    // pickup cart from retial store to truck
    private fun apiPickupCartByTag(beacons: List<BeaconExport>, store: Store): ApiResult<DeliveredStoreResult> {
        return try{
            val deliverState = _lastDeliverState?.copy()
            val tags = beacons.map { it.tag }.joinToString(",")
            val response = apiService.pickEmptyCartFromStoreByTag(deliverState?.id!!, NODE_TYPE.STORE.code, store.node_id!!, tags).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    //AppLogger.d(TAG, "Failed to pickup cart: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "exception in pickup cart by tag: ${e.message}" )
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }


    private fun apiArriveStore(store: Store): ApiResult<NodeDeliver> {
        return try{
            var deliStatus = _lastDeliverState?.copy()
            val response = apiService.arriveStore(deliStatus?.id!!, store.id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    AppLogger.d(TAG, "Success to receive from arrivalStoreApi")
                    ApiResult.Success(apiRes.data)
                }
                else if(apiRes.error_code == ErrorCode.TZ_INVALID_STORE){
                    ApiResult.Error(ApiNotification.STORE_INVALID)
                }
                else if(apiRes.error_code == ErrorCode.TZ_STORE_ALREADY_DELIVERED){
                    ApiResult.Error(ApiNotification.STORE_DELIVER_ALREADY)
                }
                else if(apiRes.error_code == ErrorCode.TZ_STORE_NOT_IN_TRIP){
                    ApiResult.Error(ApiNotification.STORE_NOT_IN_TRIP)
                }
                else if(apiRes.error_code == ErrorCode.TZ_STORE_ALREADY_REACHED){
                    ApiResult.Error(ApiNotification.STORE_REACHED_ALREADY)
                }
                else{
                    ApiResult.Error("Failed to  signify for store arrival: ${apiRes.meta}")
                }
            }
            else{
                //AppLogger.d(TAG, "Failed in arrivalstore Request: ${response}")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to arrive new store: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    // leave store after delivering
    private fun apiLeaveStore(): ApiResult<DeliveryState> {
        return try{
            val appStatus = _lastAppState?.copy()
            val deliStatus = _lastDeliverState?.copy()
            var response = apiService.leaveStore(deliStatus?.id!!, appStatus?.rs_arrived?.node_id!!).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    AppLogger.w(TAG, "Failed to leave store in api: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.w(TAG, "Failed to leave store in api: Connection error")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to leave store: ${e.message}")
            ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    function: Stop trip
     */
    private fun apiStopTrip(node_type: Int, node_id: String, reason: Int, descrption: String?,
                force:Boolean=false):ApiResult<String>
    {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            if(deliStatus == null || deliStatus.status == TRACK_STATUS.COMPLETED.code){
                ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            //AppLogger.d(TAG, "stop deliver task")
            var response = apiService.stopDeliver(deliStatus?.id!!, node_type, node_id, reason, descrption?:"", force).execute()
            if(response.isSuccessful){
                //AppLogger.d(TAG, "stop deliver task-1")
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    val deliverState: DeliveryState = apiRes.data
                    convertDeliverStateToAppState(deliverState, _dcs.value!!, _rss.value!!)
                    // read week speed information
                    fetchWeekSpeedInformation()
                    ApiResult.Success(ApiNotification.DELIVER_TASK_COMPLETED)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to stop deliver: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to stop deliver task: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    description: complete the deliver task when truck reached to logsitic company
     */
    private suspend fun apiArrivedCompany(company:Company):ApiResult<String>
    {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            if(deliStatus == null || deliStatus.status == TRACK_STATUS.COMPLETED.code){
                ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            AppLogger.d(TAG, "stop deliver task")
            val response = apiService.arriveCompany(deliStatus?.id!!, company.id).execute()
            if(response.isSuccessful){
                AppLogger.d(TAG, "stop deliver task-1")
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    val deliverState: DeliveryState = apiRes.data
                    convertDeliverStateToAppState(deliverState, _dcs.value!!, _rss.value!!)
                    // read week speed information
                    fetchWeekSpeedInformation()
                    ApiResult.Success(ApiNotification.DELIVER_TASK_COMPLETED)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to stop deliver: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to stop deliver task: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    description: Truck arrives to LC without completing the delier task. it will continue to trip
     */
    private suspend fun apiArrivedCompanyWithoutCompleting(company:Company):ApiResult<NodeDeliver>
    {
        return try{
            val deliStatus = _lastDeliverState?.copy()
            if(deliStatus == null || deliStatus.status == TRACK_STATUS.COMPLETED.code){
                ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            //AppLogger.d(TAG, "stop deliver task")
            val response = apiService.arriveCompanyWithoutCompleting(deliStatus?.id!!, company.id).execute()
            if(response.isSuccessful){
                //AppLogger.d(TAG, "stop deliver task-1")
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    return ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to stop deliver: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to stop deliver task: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private suspend fun apiLeaveCompany(company: Company): ApiResult<String>{
        return try{
            val deliStatus = _lastDeliverState?.copy()
            val appStatus = _lastAppState?.copy()
            if(deliStatus == null ){
                ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            AppLogger.d(TAG, "Leave logistic company")
            val response = apiService.leaveCompany(deliStatus?.id!!, company.id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    val nodeCompany: NodeDeliver = apiRes.data
                    val companyNew = extendCompanyProperties(company, nodeCompany)
                    appStatus?.comp = companyNew
                    updateAppState(appStatus)
                    deliStatus.status = TRACK_STATUS.PROGRESS.code
                    updateDeliverState(deliStatus)
                    postEntityUpdated(companyNew, EntityEventType.UPDATE_CMP)
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to leave company: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to leave company: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /**
     * callback function called when the driver stays in the neighboard dc more than 3 hours
     */
    private suspend fun apiStopTaskForcely(dc_id: String): ApiResult<String>{
        return try{
            var deliStatus = _lastDeliverState?.copy()
            // val appStatus = _lastAppState?.copy()
            if(deliStatus == null ){
                ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            AppLogger.d(TAG, "Leave logistic company")
            val response = apiService.stopTaskForcely(deliStatus?.id!!, dc_id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    deliStatus = apiRes.data
                    convertDeliverStateToAppState(deliStatus, _dcs.value!!, this._rss.value!! )
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    AppLogger.w(TAG, "Failed to stop deliver task: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to leave company: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to leave company: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private  fun apiUpdateUserProfile(
        company: RequestBody,
        name: RequestBody,
        phone: RequestBody,
        email: RequestBody,
        address: RequestBody,
        password: RequestBody,
        license: RequestBody,
        photo: MultipartBody.Part?):
            ApiResult<Driver> {
        val user = _user.value!!
        return try {
            AppLogger.d(TAG, "company in repository api: ${company.toString()}")

            val response = apiService.updateUserProfile(
                driverId = user.id!!,
                company = company,
                name = name,
                phone = phone,
                email =email,
                contract = address,
                password = password,
                license = license,
                file = photo).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if (apiRes.error_code == 0) {
                    ApiResult.Success(apiRes.data)
                } else {
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to update api: ${response.errorBody().toString()}")
                ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "${e.message}")
            ApiResult.Error("${ApiNotification.CONNECTION_ERROR}: ${e.message}")
        }
    }

    /*
    update only truck information
     */
    private fun apiUpdateTruckVin(newTruckInfo: Driver): ApiResult<Driver> {
        return try{
            val user = _user.value
            //AppLogger.d(TAG, "Update vin code: $vin")
            val response = apiService.updateTruck(user?.id!!, newTruckInfo.vin,  newTruckInfo.auto_start,
                newTruckInfo.time_taskstart, newTruckInfo.time_taskstop).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(apiRes.data)
                }
                else{
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to stop deliver: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to stop deliver task: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiDeleteDriverAccount(): ApiResult<String>{
        return try{
            val user = _user.value
            //AppLogger.d(TAG, "Update vin code: $vin")
            val response = apiService.deleteDriver(user?.id!!).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    //AppLogger.d(TAG, "Failed to update truck vin: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to stop deliver: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to stop deliver task: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiReadNotifies(): ApiResult<List<Notification>> {
        return try{
            val deliverState = _lastDeliverState?.copy()
            if(deliverState?.id == null){
                ApiResult.Error(ApiNotification.DELIVER_NO_FOUND)
            }
            //AppLogger.d(TAG, "Read notifications")
            val response = apiService.readNotify(deliverState?.id!!).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    //AppLogger.d(TAG,"Success to read notification truck vin")
                    ApiResult.Success(apiRes.data)
                }
                else{
                    //AppLogger.d(TAG, "Failed to read notification: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to read notifications: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to read notification: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    /*
    function: Delete Notify with id:
    param:
        notifyID: if not empty, trying to delete notify with ID
                    if it is empty, delete all notify
     */
    private fun apiDeleteNotify(notify: Notification?): ApiResult<String> {
        return try{
            //AppLogger.d(TAG, "Delete notify")
            var response: Any
            if(notify != null){ // delete only one notify
                response = apiService.deleteNotify(notify.id).execute()
            }
            else{ // delete all notify
                response = apiService.deleteAllNotify().execute()
            }
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    //AppLogger.d(TAG,"Success to delete notify")
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    //AppLogger.d(TAG, "Failed to delete notification: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to delete notifications: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to delete notification: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiMarkAsReadNotify(notify: Notification): ApiResult<String> {
        return try{
            //AppLogger.d(TAG, "mark notification as read")
            val response = apiService.markAsReadNotify(notify.id).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    //AppLogger.d(TAG,"Success to mark notification as read")
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    //AppLogger.d(TAG, "Failed to mark notification as read: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to mark notification as read: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to delete notification: ${e.message?:""}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun apiReadTrackSetting(): ApiResult<String> {

        return try {
            val userInfo = _user.value
            if(userInfo == null){
                AppLogger.w(TAG, "Invalid user information")
                return ApiResult.Error(ApiNotification.USER_INVALID_INFO)
            }
            AppLogger.d(TAG, "Read driver track setting. driver ID: ${userInfo.id}")
            val response = apiService.getTrackSetting(userInfo.id!!).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    val serverSetting: ServerTrackSetting = apiRes.data
                    val trackSetting = TrackSetting(
                        serverSetting.ble_scan_sec,
                        serverSetting.gps_fetch_sec,
                        serverSetting.distance_meter,
                        serverSetting.timezone,
                        serverSetting.passedSec,
                        serverSetting.task_start,
                        serverSetting.task_stop,
                        serverSetting.minutes_expire_task,
                        serverSetting.auto_start)
                    this.postTrackSetting(trackSetting)
                    AppLogger.d(TAG, "Success to read track setting")
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    AppLogger.d(TAG, "Failed to read trackSetting: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to mark notification as read: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e:Exception){
            AppLogger.w(TAG, "Failed to update track setting: ${e.message}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    fun apiUpdateTrackSetting(trackSetting: TrackSetting): ApiResult<String> {
        //AppLogger.d(TAG, "update track setting")
        return try {
            val userInfo = _user.value
            if(userInfo == null){
                AppLogger.w(TAG, "Invalid user information")
                return ApiResult.Error(ApiNotification.USER_INVALID_INFO)
            }
            val response = apiService.updateTrackSetting(userInfo.id!!, trackSetting.scanTimeSec, trackSetting.distanceMeter,
                trackSetting.locationUpdateSec, trackSetting.timezone).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    AppLogger.d(TAG,"Success to update track setting")
                    this.postTrackSetting(trackSetting)
                    ApiResult.Success(ApiNotification.OPERATION_SUCCESS)
                }
                else{
                    AppLogger.d(TAG, "Failed to update track setting: ${apiRes.meta}")
                    ApiResult.Error(apiRes.meta)
                }
            }
            else{
                AppLogger.e(TAG, "Failed to mark notification as read: $response")
                return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
            }
        }
        catch (e:Exception){
            AppLogger.w(TAG, "Failed to update track setting: ${e.message}")
            return ApiResult.Error(ApiNotification.CONNECTION_ERROR)
        }
    }

    private fun convertDeliverStateToAppState(deliverState: DeliveryState, distCenters: List<DistCenter>, stores: List<Store>){
        AppLogger.d(TAG, "Converting deliver state into app type. new deliver state: $deliverState")
        val appState = _lastAppState ?: AppState()

        val nodes = deliverState.nodes
        val cartDelivers = deliverState.carts
        val cartLoaded: MutableList<Cart> = mutableListOf()

        // convert distribution center
        appState.rss = mutableListOf()
        appState.dcs.clear()

        AppLogger.d(TAG, "total deliver nodes: ${nodes.size}. Current company size: ${this._companies.size}")
        for(node in nodes){
            AppLogger.d(TAG, "convert node into deliver node in app: $node")
            if(node.nodetype == NODE_TYPE.COMPANY.code && this._companies.isNotEmpty()){
                val company = this._companies.findLast { it.id == node.nodeid }
                if(company == null){
                    AppLogger.w(TAG, "Invalid company find in deliver task")
                    continue
                }
                val compExt = extendCompanyProperties(company, node)
                appState.comp = compExt
                postEntityUpdated(compExt, EntityEventType.UPDATE_CMP)
                AppLogger.d(TAG, "Success to update company")
            }
            else if(node.nodetype == NODE_TYPE.DC.code && distCenters.isNotEmpty()){// check distribution center
                val dc = distCenters.findLast { it.id == node.nodeid }
                if(dc == null){
                    AppLogger.w(TAG, "Invalid distribution center found in deliver state")
                    continue
                }
                val dcExt = extendDistCenterProperties(dc, node)
                appState.dcs.add(dcExt)
                if(dcExt.status == NODE_STATUS.REACHED.code)
                    appState.dc_selected = dcExt
                postEntityUpdated(dcExt, EntityEventType.UPDATE_DC)
                AppLogger.d(TAG, "Updated dc node")
            }
            else if(node.nodetype == NODE_TYPE.STORE.code ){ // check retail store
                if(stores.isNotEmpty()){
                    val store = stores.findLast { it.id == node.nodeid && node.nodetype == NODE_TYPE.STORE.code }
                    if(store == null) {
                        AppLogger.e(TAG, "Cannot find selected store from course: ${store}")
                        apiStoreDeselectForTrip(node.nodeid)
                        continue
                    }
                    val storeExtend = extendStoreProperties(store, node)
                    appState.rss.add(storeExtend)
                    postEntityUpdated(storeExtend, EntityEventType.UPDATE_RS)
                    AppLogger.d(TAG, "Updated store node")
                }
                else{ // deselect this store from course
                    AppLogger.w(TAG, "No stores are loaded")
                    apiStoreDeselectForTrip(node.nodeid)
                }
            }
            else{
                AppLogger.d(TAG, "Invalid node type: ${node.nodetype}")
            }

        }
        // update last arrived DC
        appState.dc_selected = if(appState.dcs.size > 0) appState.dcs.last() else null
        // convert carts
        //AppLogger.d(TAG,"Convert carts into app state")
        if(!cartDelivers.isNullOrEmpty()){
            for(cartDeliver in cartDelivers){
                val cart = Cart.fromCartDeliver(cartDeliver)
                cartLoaded.add(cart)
            }
            //AppLogger.d(TAG, "final loaded cart: ${cartLoaded.size}")
        }
        // update last store
        appState.rs_arrived = appState.rss.findLast {
            it.status == NODE_STATUS.REACHED.code
        }
        _cartsLoaded.postValue(cartLoaded)
        updateAppState(appState)
        updateDeliverState(deliverState)
    }

    override fun onNotificationReceived(notification: Notification) {
        //AppLogger.d(TAG, "Notification received: $notification")
        postNotifyUpdated(notification, NotifyEventType.UPDATE_NOTIFY)
    }

    // post the updated deliver entity item
    private fun postEntityUpdated(entity: Any, entity_type: Int) {
        AppLogger.d(TAG, "postEntityUpdated: Entity node is updated: $entity, $entity_type")
        val queue = ConcurrentLinkedQueue<EntityEvent>()
        queue.offer(EntityEvent(entity_type, entity))
        _entityUpdated.postValue(queue)
    }


    /**
     * @description: Post the notification message received from server.
     *      This notification would be shows on "notification" page of profile
     */
    private fun postNotifyUpdated(notification:Notification?, type: NotifyEventType){
        synchronized(this){
            val queue = _notifyUpdated.value ?: ConcurrentLinkedQueue()
            queue.offer(NotifyEvent(type, notification))
            _notifyUpdated.postValue(queue)
        }
    }

    /**
     * @description: Post the locatoin update message. This event is occured by location manager which monitors the truck position.
     *      This notification is shown as toast message
     */
    private fun postLocationNotify(notify: String){
        synchronized(this){
            val queue = _notifyLocUpdate.value ?: ConcurrentLinkedQueue()
            queue.offer(notify)
            _notifyLocUpdate.postValue(queue)
        }
    }


    /**
     * @description: Post the truck speed event
     */
    private fun postCoursePoint(point: ServerPoint){
        synchronized(this) {
            val queue = _deliverOnePoint.value ?: ConcurrentLinkedQueue()
            queue.offer(point)
            _deliverOnePoint.postValue(queue)
        }
    }

    private fun postTrackSetting(trackSetting: TrackSetting){
        _trackSetting.postValue(trackSetting)
        _locManager.updateTrackSetting(trackSetting)
    }

    /**
     * @description: callback function when truck leaves the DC
     * @param: id: id of distribution center which
     */
    override suspend fun onTruckLeaveDc(id: String){
        AppLogger.w(TAG, "LocationManager event: Leave DC $id")
        val dcs = _dcs.value
        val dcArrived = dcs?.findLast { it.id == id }
        if(dcArrived == null){
            AppLogger.w(TAG, "No valid distributiuon center: $id")
            return
        }
        val state = _lastDeliverState?.copy()
        if(state == null){
            AppLogger.w(TAG, "Delivering state isn't initialized")
            postLocationNotify(ApiNotification.DELIVER_REQUEST_START)
            return
        }
        if(state.status > TRACK_STATUS.INIT.code && state.status < TRACK_STATUS.COMPLETED.code){
            leaveDistCenter(dcArrived)
            postLocationNotify(getLeavedMessage(dcArrived.name))
        }else{
            AppLogger.w(TAG, "Cannot leave distribution center. Task is already completed or not initialized")
        }
        return;
    }

    /**
     * @description: callback function when truck arrives at DC
     * @param: id: id of distribution center
     */
    override  suspend fun onTruckArriveDc(id: String){
        val dcs = _dcs.value
        val dcArrived = dcs?.findLast { it.id == id }
        if(dcArrived == null){
            AppLogger.w(TAG, "No found valid distribution center: $id")            
            return
        }
        AppLogger.w(TAG, "LocationManager event: Arrive DC ${dcArrived.name}")
        val state = _lastDeliverState
        if(state == null){
            AppLogger.w(TAG, "deliver task isn't initialized")
            postLocationNotify(ApiNotification.DELIVER_REQUEST_START)
            return
        }
        if(state.status > TRACK_STATUS.INIT.code && state.status < TRACK_STATUS.COMPLETED.code){// track status is valid
            AppLogger.d(TAG, "Arrived to new distribution center: $dcArrived")
            arriveDistCenter(dcArrived)
            postLocationNotify(getArrivedMessage(dcArrived.name))
        }else{
            AppLogger.w(TAG, "Cannot arrive distribution center. invalid task status: ${state.status}")
        }
    }

    /**
     * @description: callback function when truck leaves the store
     * @param: id: id of retail store
     */
    override  suspend fun onTruckLeaveStore(id: String){
        AppLogger.w(TAG, "LocationManager event: Leave STORE $id")
        val rss = _rss.value
        if(rss.isNullOrEmpty()){
            AppLogger.w(TAG, "Retial store isn't loaded")
            return
        }
        val rsSelected = rss.findLast { it.id == id }
        if(rsSelected == null){
            AppLogger.w(TAG, "Failed to find the RS arrived")
            return
        }
        val state = _lastDeliverState?.copy()
        if(state == null){
            AppLogger.w(TAG, "deliver task isn't initialized")
            postLocationNotify(ApiNotification.DELIVER_REQUEST_START)
            return
        }

        var storeName = _lastAppState?.rs_arrived?.name?:""
        if(state.status == TRACK_STATUS.DELIVERING.code){ // continue trip
            val apiRes = leaveStore()
            if(apiRes is ApiResult.Success){
                postLocationNotify(getLeavedMessage(storeName))
            }
            else{
                AppLogger.w(TAG, "Failed to leave retail store automatically")
            }
        }
        else{
            AppLogger.w(TAG, "Leave retail store in invalid status: ${state.status}")
        }
    }

    /**
     * @description: callback funtion when the truck arrives the retail retail store
     * @param: id: id of retial store
     */
    override  suspend fun onTruckArriveStore(id: String){
        AppLogger.w(TAG, "LocationManager event: Arrive STORE $id")
        // find retail store
        val rss = _rss.value
        if(rss.isNullOrEmpty()){
            AppLogger.w(TAG, "Retial store isn't loaded")
            return
        }
        val rsSelected = rss.findLast { it.id == id }
        if(rsSelected == null){
            AppLogger.w(TAG, "Failed to find the RS arrived")
            return
        }
        val state = _lastDeliverState?.copy()
        if(state == null){
            AppLogger.w(TAG, "deliver task isn't initialized")
            postLocationNotify(ApiNotification.DELIVER_REQUEST_START)
            return
        }
        if(state.status == TRACK_STATUS.PROGRESS.code){ // arrived at store to deliver the cart
            val apiRes = arrivedStore(rsSelected)
            if(apiRes is ApiResult.Success){
                AppLogger.d(TAG, "Success to arrive new store: ${rsSelected.name}")
                return postLocationNotify(getArrivedMessage(rsSelected.name))
            }
            else{
                AppLogger.w(TAG, "Failed to arrive store automatically")
                return
            }
        }
        else{
            AppLogger.w(TAG, "Arrived at store in invalid state: ${state.status}")
            return
        }
    }

    /**
     * @description: callbck function when truck arrives at logistic company
     * @param: id: id of logistic company
     */
    override suspend fun onTruckArriveCompany(id: String) {
        AppLogger.d(TAG, "LocationManager event: Arrive company: ${id}")
        val state = _lastDeliverState?.copy()
        // check if company exists
        val company = _companies.findLast { it.id == id }
        if(company == null){
            AppLogger.w(TAG, "Truck reached at invalid company")
            return
        }
        if(state == null){ //
            return
        }
        if(state.nodes.isEmpty()){ // error. nodes shouldn't be empty when state isnot null
//            AppLogger.d(TAG, "Start deliver task automatically")
//            apiStartDeliverTask(company)
            AppLogger.d(TAG, "nodes shouldn't be empty when state isnot null")
            return
        }
        if(state.nodes.size == 1){ // Task already started. No need to anything
            AppLogger.d(TAG, "task is already started. No need to do anything")
            return
        }
        if((_lastAppState?.rss?.size ?: 0) > 0){ // contains any valid retail store. can stop deliver task
            apiArrivedCompany(company)
            return
        }
        // no store contained. Just add company as normal node
        val res = arriveCompanyWithoutCompleting(company)
        if(res is ApiResult.Success){
            AppLogger.d(TAG, "Succfully arrive company without completin task")
        }
        else if (res is ApiResult.Error){
            AppLogger.w(TAG, "Failed to arrive company without completin task: ${res.message}")
        }
        return
    }

    /**
     * @description: callbck function when truck leaves at logistic company
     * @param: id: id of logistic company
     */
    override suspend fun onTruckLeaveCompany(id: String) {
        AppLogger.d(TAG, "LocationManager event: Leave company: ${id}")
        val state = _lastDeliverState?.copy()
        // check if company exists
        val company = _companies.findLast { it.id == id }
        if(company == null){
            AppLogger.w(TAG, "Truck reached at invalid company")
            return
        }
        if(state == null){ //
            postLocationNotify(ApiNotification.DELIVER_REQUEST_START)
            return
        }
        val apiRes = apiLeaveCompany(company)
        if(apiRes is ApiResult.Success){
            postLocationNotify(getLeavedMessage(company.name))
        }else{
            AppLogger.w(TAG, "Failed to leave logstic company")
        }
        return
    }

    override suspend fun onTruckCompleteForcely(id: String){
        AppLogger.d(TAG, "LocationManager event: Deliver task is completed forcely in $id")
        if(this._lastAppState?.dc_selected?.id != id){
            AppLogger.d(TAG, "Trying to stop deliver task in invalid distribution center")
            return
        }
        val nodeId =  this._lastAppState?.dc_selected?.node_id
        if(nodeId == null){
            AppLogger.w(TAG, "Invalid node id of last distribution center")
            return
        }
        val apiResult = this.apiStopTaskForcely(nodeId)
        if(apiResult is ApiResult.Success){
            AppLogger.d(TAG, "Success to stop deliver task by neighbor timeout condition")
        }
        else{
            AppLogger.w(TAG, "Failed to stop deliver task forcely")
        }
    }

    override suspend fun onTruckCompleteAutomatically(entity: GpsEntity?, time: Date) {
        return try{
            AppLogger.d(TAG, "Stop deliver task automatically because truck parks in the same location for long time: ${entity} from $time")
            val deliStatus = _lastDeliverState?.copy()
            if(deliStatus == null || deliStatus.status == TRACK_STATUS.COMPLETED.code){
                AppLogger.d(TAG, "Invalid deliver status Or task is completed already")
                return
            }
            //AppLogger.d(TAG, "stop deliver task")
            var nodeType = NODE_TYPE.PARKING.code
            var nodeId = ""
            val reason = DELIVER_STOP_REASON.AUTO.code
            val description = ApiNotification.DELIVER_TASK_COMPLETED_AUTOMATICALLY
            entity?.let { e->
                when(entity){
                    is GpsEntity.cmp -> {
                        nodeType = NODE_TYPE.COMPANY.code
                        nodeId = e.getId()
                    }
                    is GpsEntity.dc->{
                        nodeType = NODE_TYPE.DC.code
                        nodeId = e.getId()
                    }

                    else->{
                        nodeType = NODE_TYPE.PARKING.code
                        nodeId = ""
                    }
                }
            }
            val strTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
                .apply { timeZone = TimeZone.getDefault() }
                .format(time)
            val response = apiService.stopDeliverAuto(deliStatus.id, nodeType, nodeId,
                reason, description, strTime).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                if(apiRes.error_code == ErrorCode.TZ_SUCCESS){
                    AppLogger.d(TAG, "Success to complete deliver task automatically")
                    val deliverState: DeliveryState = apiRes.data
                    convertDeliverStateToAppState(deliverState, _dcs.value!!, _rss.value!!)
                    // read week speed information
                    fetchWeekSpeedInformation()
                    return
                }else{
                    AppLogger.d(TAG, "Failed to complete the deliver task automatically")
                    return
                }
            }
            else{
                AppLogger.e(TAG, "Failed to stop deliver automatically: $response")
                return
            }
        }
        catch(e: Exception){
            AppLogger.e(TAG, "Failed to stop deliver task: ${e.message?:""}")
            return
        }
    }

    override suspend fun onRequireGeofencingData(entity: GpsEntity) {
        AppLogger.d(TAG, "Truck require geodata of entity $entity")
        val nodeId = entity.getId()
        val nodeType = entity.getNodeType().toInt()
        if(nodeId.isEmpty() || nodeType == NODE_TYPE.UNK.code){
            AppLogger.d(TAG, "No need geo data")
            return
        }

        try{// download geo data
            val response = apiService.downloaddGeodata(nodeType, nodeId).execute()
            if(response.isSuccessful){
                val apiRes = response.body()!!
                when (apiRes.error_code) {
                    ErrorCode.TZ_SUCCESS -> {
                        val geojson: String = apiRes.data
                        val geoData = GeoFenceUtil.parsePolygonFromGeoJson(geojson)
                        this._locManager.setGeodata(entity, geoData)
                        AppLogger.d(TAG, "Success to parse geofencing data. final polygon length: ${geoData.size}")
                        return
                    }
                    ErrorCode.TZ_GEO_NO_FOUND -> { // no geo data
                        this._locManager.setGeodata(entity, emptyList<LatLng>())
                    }
                    else -> {
                        AppLogger.d(TAG, "Failed to read geo data: $apiRes")
                        return
                    }
                }
            }
            else{
                AppLogger.e(TAG, "Failed to read geofencing data: $response")
                return
            }
        }
        catch (e: Exception){
            AppLogger.e(TAG, "Exception in read geofencing data: ${e.message?:""}")
            return
        }
    }

    override suspend fun onFoundBecaons(entity: GpsEntity, beacons: List<BeaconExport>) {
        return
        //AppLogger.d(TAG, "Beacon list update: entity: ${entity.getName()},  beacons ${beacons.size}: $beacons")
        /*
        val statusDeli = _lastDeliverState?.copy()
        if(statusDeli == null){
            AppLogger.w(TAG, "Scan is completed in invalid status")
            return
        }
        var status = statusDeli.status
        val statusApp = _deliverAppState.value
        val beaconOverview = findBeaconChanges(beacons)
        //AppLogger.d(TAG, "new loaded tags: ${beaconOverview.loaded.size}, unloaded tags: ${beaconOverview.unloaded.size}")
         // _lastAppState?.dc_selected
        val cartsLoaded = _cartsLoaded.value ?: mutableListOf()
        if(entity is GpsEntity.dc){ // truck leaved from DC
            val dcs = _dcs.value
            val dc = dcs?.findLast { it->it.id == entity.dc.id }
            if(dc == null){
                //AppLogger.d(TAG, "Invalid distribution center is selected")
                return
            }
            //AppLogger.d(TAG, "checking dc beacons: $statusDeli")
            if(status == TRACK_STATUS.PROGRESS.code
                || status == TRACK_STATUS.DELIVERING.code){// load cart from distribution center
                // load all cart into truck
                val loaded = beaconOverview.loaded

                val apiRes = apiLoadCartByTagFromDc(statusApp!!.dc_selected!!.node_id!!, loaded)
                if(apiRes !is ApiResult.Success){
                    AppLogger.w(TAG, "Failed to load batch carts")
                    return
                }
                cartsLoaded.addAll(apiRes.data.carts)
                _cartsLoaded.postValue(cartsLoaded)
                val appState = _lastAppState?.copy()
                val dcExtNew = extendDistCenterProperties(appState?.dc_selected!!, apiRes.data.dc)
                appState.dc_selected = dcExtNew.copy()
                updateAppState(appState)
                postEntityUpdated(dcExtNew, EntityEventType.UPDATE_DC)
            }

            else if(status == TRACK_STATUS.COMPLETED.code
                || status == TRACK_STATUS.RETURNING.code
                ) { // return cart to DC
                var loaded = beaconOverview.loaded
                val unloaded = beaconOverview.unloaded
                // save the current loaded tags
                var cartsLoadedPrev = loaded
                cartsLoadedPrev.addAll(beaconOverview.exits)
                if(unloaded.size > 0){ // there is unloaded carts meaning for truck returns the carts to DC
                    if(status == TRACK_STATUS.RETURNING.code){ // stop task first
                        val r = stopDeliverTask(NODE_TYPE.DC.code,dc.id, 4,
                            ApiNotification.DELIVER_TASK_COMPLETED_AUTOMATICALLY,false)
                        if(r is ApiResult.Success){
                            AppLogger.d(TAG, "Success to stop deliver task")
                        }
                    }
                    //AppLogger.d(TAG, "returning the remaining tags: $unloaded")
                    val apiRes = apiReturnCartByTag(dc.node_id!!, unloaded)// return to distribution center
                    if(apiRes !is ApiResult.Success){
                        AppLogger.w(TAG, "Failed to return batch cart to DC")
                        return
                    }
                    // remove carts
                    //AppLogger.d(TAG, "remove the carts from loaded carts: ${apiRes.data}")
                    apiRes.data.carts.forEach{it->
                        val index = cartsLoaded.indexOfFirst { cart->cart.iot == it.iot }
                        if(index >= 0 ){
                            _cartRecentRemoved = cartsLoaded[index].copy()
                            cartsLoaded.removeAt(index)
                        }
                    }
                    _cartsLoaded.postValue(cartsLoaded)
                    //AppLogger.d(TAG, "final loaded carts: $cartsLoaded")
                    // _carts_dc.postValue(cartsDc)
                    //AppLogger.d(TAG, "Success to return cart. DC status: ${apiRes.data}")
                    _lastAppState!!.dc_selected = extendDistCenterProperties(dc, apiRes.data.dc)
                    updateAppState(_lastAppState)
                    postEntityUpdated(_lastAppState!!.dc_selected!!, EntityEventType.UPDATE_DC)
                    postLocationNotify(ApiNotification.CART_RETURN_SUCCESS)

                }
                if(loaded.size > 0){ // there is new loaded item. which means for new deliver task is completed
                    // status could be updated during the above operation
                    status = _lastDeliverState!!.status
                    //AppLogger.d(TAG, "Start new task with the ramining carts")
                    // complete the previous task first
                    if(status != TRACK_STATUS.COMPLETED.code){ // task isn't completed
                        //AppLogger.d(TAG, "stop task again")
                        val r = stopDeliverTask(NODE_TYPE.DC.code,dc.id, 4, ApiNotification.DELIVER_TASK_COMPLETED_AUTOMATICALLY,false)
                        if(r is ApiResult.Success){
                            //AppLogger.d(TAG,"Success to stop task")
                        }
                    }
                    // notify to complete task
                    postLocationNotify(ApiNotification.DELIVER_TASK_COMPLETED)

                    // init new task
                    postEntityUpdated(Unit, EntityEventType.INIT_TRIP_NOTIFY)
//                    val apiInitres = apiInitTrip(true)
//                    if(apiInitres !is ApiResult.Success){
//                        AppLogger.w(TAG, "Failed to initalize task automatically")
//                        return
//                    }

                    // arrive DC
                    val resArriveDc = arriveDistCenter(dc)
                    if (resArriveDc !is ApiResult.Success){
                        AppLogger.w(TAG, "Failed to notify for arrival DC")
                        return
                    }
                    postLocationNotify(ApiNotification.DELIVER_START_TRIP)

                    // leave DC
                    val resStartTask = startDeliverTask()
                    if(resStartTask !is ApiResult.Success){
                        AppLogger.w(TAG, "FAiled to start task")
                        return
                    }
                    postLocationNotify(getLeavedMessage(dc.name))

                    // add all carts into new taks
                    apiLoadCartByTagFromDc(dc.node_id!!, cartsLoadedPrev)
                    return
                }
            }

            else{
                AppLogger.w(TAG, "Can't use scanning result. Invalid deliver status: ${status}")
            }
        }
        else if(entity is GpsEntity.rs){ // truck leaved from retail store
            // compare store id
            val stores = _rss.value
            var storeArrived = stores?.findLast { it.id == entity.rs.id }
            if(storeArrived == null){
                AppLogger.w(TAG, "Cannot find the retail store: $entity")
                return
            }
            val beaconPicked = beaconOverview.loaded
            val beaconDelivered = beaconOverview.unloaded
            //AppLogger.d(TAG, "checking RS beacons - 1: Delivered: ${beaconDelivered}, picked: ${beaconPicked} ")

            if(beaconDelivered.size > 0){
                //AppLogger.d(TAG, "deliver beacon $beaconDelivered")
                val apiDeliverRes = apiDeliverCartByTag(storeArrived, beaconDelivered)
                if(apiDeliverRes is ApiResult.Success){ // failed to deliver cart
                    //AppLogger.d(TAG, "Success to deliver cart by tag. Delivered cart: ${apiDeliverRes.data.carts.size}")

                    storeArrived = extendStoreProperties(storeArrived, apiDeliverRes.data.store)
                    // remove carts from loaded items
                    val cartsDelivered = apiDeliverRes.data.carts
                    cartsDelivered.forEach{it->
                        val id = cartsLoaded.indexOfFirst { cart->cart.iot == it.iot }
                        if(id > 0){
                            _cartRecentRemoved = cartsLoaded[id].copy()
                            cartsLoaded.removeAt(id)
                        }
                    }
                    postLocationNotify(getLoadCartMessage(cartsDelivered.size, storeArrived.name))
                }
                else if(apiDeliverRes is ApiResult.Error){// update the status
                    //AppLogger.d(TAG, "Failed to deliver cart: ${apiDeliverRes.message}")
                    postLocationNotify(ApiNotification.CART_DELIVER_FAILED)
                }
            }
            if(beaconPicked.size > 0){
                //AppLogger.d(TAG, "picked up beacon: ${beaconPicked}")
                val apiPickRes = apiPickupCartByTag(beaconPicked, storeArrived)
                if(apiPickRes is ApiResult.Success){ // success to pickup cart
                    // change the loaded carts
                    //AppLogger.d(TAG, "Success to pickup cart: ${apiPickRes.data}")
                    if(apiPickRes.data.carts.size > 0) {
                        cartsLoaded.addAll(apiPickRes.data.carts)
                        //AppLogger.d(TAG, "Success to pickup cart : ${apiPickRes.data.carts}")
                    }
                    // change the store status
                    storeArrived = extendStoreProperties(storeArrived, apiPickRes.data.store)
                     //AppLogger.d(TAG, "store status after picking: ${storeArrived}")
                    postLocationNotify(getPickCartMessage(beaconPicked.size, storeArrived.name))
                }
                else if(apiPickRes is ApiResult.Error){
                    //AppLogger.d(TAG, "Failed to pickup carts automatically: ${apiPickRes.message}")
                    postLocationNotify(ApiNotification.CART_PICKUP_FAILED)
                }
            }
            _cartsLoaded.postValue(cartsLoaded)
            postEntityUpdated(storeArrived, EntityEventType.UPDATE_RS)
        }*/
    }

    override  suspend fun  onTruckStartTask(entity: GpsEntity){
        AppLogger.d(TAG, "Start deliver task automatically from $entity")
        if(entity !is GpsEntity.cmp && entity !is GpsEntity.dc){
            AppLogger.w(TAG, "Deliver task could be start either from LC or DC automatically")
            return
        }
        AppLogger.d(TAG, "Start deliver task automatically")
        apiStartDeliverTaskAutomatically(entity)
        return
    }


    private fun findBeaconChanges(beaconCurrent: List<BeaconExport>): TagsOverview {
        return TagsOverview(emptyList<BeaconExport>().toMutableList(),
            emptyList<BeaconExport>().toMutableList(),
            emptyList<BeaconExport>().toMutableList())
        val cartsLoaded = _cartsLoaded.value!!
        // find added newly
        val beaconExists = cartsLoaded.map { cart->BeaconExport(cart.iot) }
        val beaconUnload = beaconExists.subtract(beaconCurrent.toSet())
        val beaconLoaded = beaconCurrent.subtract(beaconExists.toSet())
        return TagsOverview(beaconExists.toMutableList(), beaconLoaded.toMutableList(), beaconUnload.toMutableList())
    }

    /**
     * @description: update a list of distribution center of system
     * @note: Call modifiment of DCs should be performed by this funciton.
     */
    private fun updateSystemDistCenters(dcs: List<DistCenter>){
        _dcs.postValue(dcs)
        _locManager.updateDistCenter(dcs)
    }

    /**
     * @description: update a list of retail store of system
     * @note: Call modifiment of RSs should be performed by this funciton.
     */
    private fun updateSystemRetailStores(rss: List<Store>){
        _rss.postValue(rss)
        _locManager.updateStore(rss)
    }

    private fun updateDeliverState(state: DeliveryState?){
        AppLogger.d(TAG, "Update deliver state to: $state")
        if(state == null){
            AppLogger.w(TAG, "deliver state is invalid")
            _lastDeliverState = null
        }
        else{
            _lastDeliverState = state.copy()
        }
        onTaskStatusChanged()
        _deliverState.postValue(state)
    }

    private fun onTaskStatusChanged(){
        this._lastDeliverState?.let {state->
            this._trackCompleted = (state.status == TRACK_STATUS.COMPLETED.code)
        }
        this._locManager.updateTaskStatus(this._lastDeliverState)
    }


    private fun updateAppState(state: AppState?){
        if(state == null){
            _lastAppState = null
        }
        else{
            _lastAppState = state.copy()
        }
        _deliverAppState.postValue(state)
        //AppLogger.d(TAG, "App state is updated: $state")
    }


    fun getLastAddedCart(): Cart?{
        return _cartRecentAdded
    }
    fun getLastRemovedCart(): Cart?{
        return _cartRecentRemoved
    }

    fun getUserPhoto(): LiveData<Bitmap>{
        return _userPhoto
    }

    fun getDistributionCenter(): List<DistCenter>?{
        return _dcs.value
    }

    fun getCompany(): List<Company>{
        return _companies
    }

    fun getTotalCartsLoaded(): LiveData<MutableList<Cart>>{
        return _cartsLoaded
    }

    fun getTotalCartCount(): LiveData<Int>{
        return _cartsCount
    }
    fun getTotalEmptyCartCount(): LiveData<Int>{
        return _cartsCountEmpty
    }
    fun getTotalLoadCartCount(): LiveData<Int>{
        return _cartsCountNonEmpty
    }

    fun getAppState(): LiveData<AppState?>{
        return _deliverAppState
    }

    fun getAppStateValue(): AppState?{
        return _lastAppState
    }

    fun getDeliverState(): LiveData<DeliveryState?>{
        return _deliverState
    }

    fun getDeliverStateValue(): DeliveryState?{
        return _lastDeliverState
    }

    fun getDistributionCenters(): LiveData<List<DistCenter>>{
        return _dcs
    }

    fun getRetailStores(): LiveData<List<Store>>{
        return _rss
    }

    fun getEntityUpdated(): LiveData<Queue<EntityEvent>>{
        return _entityUpdated
    }

    fun getNotifyUpdated(): LiveData<Queue<NotifyEvent>>{
        return _notifyUpdated
    }

    fun getLocNotifyUpdate(): LiveData<Queue<String>>{
        return _notifyLocUpdate
    }

    fun getTripDirection(): LiveData<Int>{
        return _tripDirection
    }

    fun getTrackSetting(): LiveData<TrackSetting>{
        return _trackSetting
    }

    fun getTrackSettingValue(): TrackSetting {
        return _trackSetting.value!!
    }

    fun getUserInfo(): Driver?{
        return _user.value
    }


    // monitor total course points
    fun getCoursePoints(): LiveData<List<ServerPoint>?>{
        return _deliverCoursePoints
    }

    // monitor week speed information
    fun getWeekSpeed(): LiveData<List<ServerPoint>?>{
        return _deliverWeekSpeed
    }

    // monitory only recent point
    fun getOneCoursePoint(): LiveData<Queue<ServerPoint>>{
        return _deliverOnePoint
    }

    fun getLoadedCart(): LiveData<MutableList<Cart>>{
        return _cartsLoaded
    }

    fun trackingIsCompleted():Boolean{
        return this._trackCompleted
    }

    fun getServerUrl(): String{
        return this._serverUrl
    }

    private fun saveUserInfomration(user: Driver){
        val userInfoJson = gson.toJson(user)
        val sharedPreferences = context.getSharedPreferences(AppMacro.SHAREDPREF_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppMacro.KEY_USERINFO, userInfoJson).apply()
        editor.apply()
    }
}