package myandroid.app.hhobzic.inventorymobile.models

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.ApkVersion

import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.model.Store
import myandroid.app.hhobzic.inventorymobile.api.model.Cart
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.ErrorCode
import myandroid.app.hhobzic.inventorymobile.api.model.Notification
import myandroid.app.hhobzic.inventorymobile.api.model.ServerPoint
import myandroid.app.hhobzic.inventorymobile.api.model.TrackSetting
import myandroid.app.hhobzic.inventorymobile.home.cart.EntityEvent
import myandroid.app.hhobzic.inventorymobile.home.profile.NotifyEvent
import myandroid.app.hhobzic.inventorymobile.utility.AppState
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.Queue


class AppStatusSharedViewModel(application: Application) : AndroidViewModel(application) {
    // private val repository = AppStatusRepository()
    private val TAG = "AppStatusSharedViewModel"
    private var retryJob: Job? = null
    private var _error = MutableLiveData<String>()
    // user information is updated
    val user: LiveData<Driver> get() = repository.user
    // user photo is updated
    val userPhoto: LiveData<Bitmap> get() = repository.getUserPhoto()
    // the total loaded cart  is updated
    val cartsCount: LiveData<Int> get() = repository.getTotalCartCount()
    // the empty loaded cart is update
    val cartsCountEmpty: LiveData<Int> get() = repository.getTotalEmptyCartCount()
    // the non-empty cart is updated
    val cartsCountLoaded: LiveData<Int> get() = repository.getTotalLoadCartCount()
    // the cart is updated
    val carts: LiveData<MutableList<Cart>> get() = repository.getTotalCartsLoaded()

    // status of any deliver node is updated
    val entityUpdated: LiveData<Queue<EntityEvent>> get() = repository.getEntityUpdated()
    val notifyUpdated: LiveData<Queue<NotifyEvent>> get() = repository.getNotifyUpdated()
    val locNotifyUpdated: LiveData<Queue<String>> get() = repository.getLocNotifyUpdate()
    val tripDirectionUpdated: LiveData<Int> get() = repository.getTripDirection()
    val cartsLoaded: LiveData<MutableList<Cart>> get() = repository.getLoadedCart()
    // deliver course points. Used to draw graph
    val deliverInitCoursePoint: LiveData<List<ServerPoint>?> get() = repository.getCoursePoints()
    // monitor recent course point
    val deliverRecentCoursePoint: LiveData<Queue<ServerPoint>> get() = repository.getOneCoursePoint()
    val deliverWeekSpeed: LiveData<List<ServerPoint>?> get() = repository.getWeekSpeed()

    val trackSetting: LiveData<TrackSetting> get() = repository.getTrackSetting()


    private var repository = AppStatusRepository.getInstance(application.applicationContext)

    // error event
    val error: LiveData<String> get() = _error

    val loadingDeliverAppState: LiveData<AppState?> get() = repository.getAppState()      // loading state of truck
    val loadingDeliverState : LiveData<DeliveryState?> get() = repository.getDeliverState()
    val distcenters: LiveData<List<DistCenter>> get() = repository.getDistributionCenters()
    val retailstores: LiveData<List<Store>> get() = repository.getRetailStores()



    fun getUserInfo(): Driver?{
        return repository.getUserInfo()
    }

    fun getAppState(): AppState?{
        return repository.getAppStateValue()
    }

    fun getDeliverState(): DeliveryState?{
        return repository.getDeliverStateValue()
    }

    fun getTrackSetting(): TrackSetting {
        return repository.getTrackSettingValue()
    }

    fun updateUser(userInfo: Driver, companies: List<Company>) {
        repository.updateUser(userInfo, companies)
    }

    fun trackingIsCompleted():Boolean{
        return repository.trackingIsCompleted()
    }



    // fetch system context
    fun fetchInitialInformation(tryCnt: Int = 5, onSuccess: (ApiResult<String>) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 0 until tryCnt){
                when (val result = repository.fetchInitialInformation()) {
                    is ApiResult.Success -> {
                        withContext(Dispatchers.Main){
                            onSuccess(result)
                        }
                        //Log.d(TAG, "Success to fetch distcenter information: ${result.data}")
                        break;
                    }
                    is ApiResult.Error -> {
                        _error.postValue(result.message)
                        Log.e(TAG, "Failed to query initial delivery state: ${result.message}")
                    }
                    is ApiResult.Code ->{
                        val code = result.code
                        if(code == ErrorCode.TZ_SUCCESS || code == ErrorCode.TZ_NO_DELIVER_TASK){
                            withContext(Dispatchers.Main){
                                onSuccess(result)
                            }
                            //Log.d(TAG, "Success to load previous devlier task")
                            break
                        }
                        else{
                            Log.d(TAG, "Invalid deliver task return code: $code")                            }
                    }
                }
                delay(5000) // Retry after 5 seconds
            }
        }
    }

    fun getUserCompany(): String{
        return repository.getUserCompany()
    }

    // init Deliver task
    fun checkIfAroundCompany(company: Company, curLocation: LatLng?):ApiResult<String>{
        return repository.checkIfCompanyAround(company, curLocation)
    }

    // stop deliver task
    fun stopDeliverTask(
        node_type: Int,         // NODE_TYPE where trip is completed
        node_id: String,        // node id where trip is completed
        reason: Int,            // enumeration value of why trip is completed
        description: String?,   // commends for reason
        force: Boolean = false,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.stopDeliverTask(node_type, node_id, reason, description, force)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        //Log.d(TAG, "Failed to stop deliver task : ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }



    fun startDeliverTask(onSuccess: (ApiResult<String>) -> Unit,
                         onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.startDeliverTask()){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Failed to start deliver: ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    fun startDeliverTaskFromCompany(company: Company,  onSuccess: (String) -> Unit,
                         onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.apiStartDeliverTask(company)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Failed to start deliver: ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }





    fun addCart(
        iot_tag: String,
        onSuccess:(String)->Unit,
        onFailed: (String)->Unit){
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.loadCart(iot_tag)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        //Log.d(TAG, "Failed to add cart : ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // remove cart while loading
    fun unloadCart(
        cart: Cart,
        onSuccess: (String)->Unit,
        onFailed: (String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            when(val apiRes: ApiResult<String> = repository.unloadCart(cart)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // return empty cart to DC after deliver is completed
    fun returnEmptyCart(
        cart: Cart,
        onSuccess: (String)->Unit,
        onFailed: (String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            when(val apiRes: ApiResult<String> = repository.returEmptyCart(cart)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // deliver cart from TRUCK to Store
    fun deliverCart(
        cart: Cart,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            when(val apiRes: ApiResult<String> = repository.deliverCart(cart)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // pickup cart from store to TRUCK
    fun pickupCartFromStore(
        iot: String,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            when(val apiRes: ApiResult<String> = repository.pickupCartFromStore(iot)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // pickup cart from store to TRUCK
    fun continueTrip(
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            when(val apiRes: ApiResult<String> = repository.leaveStore()){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // push GPS location into server
    fun pushGpsLocation(
        latitude: Double,
        longitude: Double,
        fuel: String = "",
        co2: String = "",
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            when(val apiRes: ApiResult<String> = repository.pushGpsLocation(
                latitude, longitude, fuel, co2)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // read DCs and RSs information
    fun refreshJourneyInformation(onSuccess: (String) -> Unit,
                                  onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<String> = repository.refreshJourneyState()){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }

                }
            }
        }

    }

    // update the distribution center
    fun arriveDistributionCenter(dc: DistCenter,
                                 onSuccess: (String) -> Unit,
                                 onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<String> = repository.arriveDistCenter(dc)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }

                }
            }
        }
    }

    fun addStoretoTrip(store: Store,
                       onSuccess: (String) -> Unit,
                       onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<String> = repository.selectStoreForTrip(store)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }

                }
            }
        }
    }




    fun removeStoreFromTrip(store: Store, onSuccess: (String) -> Unit, onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<String> = repository.deseclectStoreFromTrip(store)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }

                }
            }
        }
    }

    fun onArrivedStore(store: Store, onSuccess: (String) -> Unit, onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<String> = repository.arrivedStore(store)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }

                }
            }
        }
    }

    // download profile image of retail store
    fun downloadStoreImage(store_id: String, onSuccess: (Bitmap) -> Unit, onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<Bitmap> = repository.apiDownloadStorePhoto(store_id)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // download profile image of retail store
    fun downloadCompanyImage(company_id: String, onSuccess: (Bitmap) -> Unit, onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<Bitmap> = repository.apiDownloadCompanyPhoto(company_id)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }


    // download profile image of distribution center
    fun downloadDistImage(dcId: String, onSuccess: (Bitmap) -> Unit, onFailed: (String) -> Unit){
        viewModelScope.launch (Dispatchers.IO){
            when (val apiRes: ApiResult<Bitmap> = repository.apiDownloadDistPhoto(dcId)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }



    fun updateUserProfile(
        company: RequestBody,
        name: RequestBody,
        phone: RequestBody,
        email: RequestBody,
        address: RequestBody,
        password: RequestBody,
        license: RequestBody,
        photo: MultipartBody.Part?,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            Log.d(TAG, "company in viewmode: ${company.toString()}")
            when(val apiRes: ApiResult<String> = repository.updateUserProfile( company=company,
                name = name,phone =  phone, email = email,address =  address, password = password,license = license,photo= photo)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        //Log.d(TAG, "Failed to update driver profile : ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }


    fun updateTruckInformation(
        user: Driver,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.updateTruckInformation(user)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        //Log.d(TAG, "Failed to update driver profile : ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    fun deleteDriverAccount(
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.deleteDriverAccount()){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        //Log.d(TAG, "Failed to update driver profile : ${apiRes.message}")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }





    // load past notifications
    fun fetchPastNotifications(onSuccess: (String) -> Unit,
                               onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.fetchPastNotification()){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // load past notifications
    fun removeNotification(
        notify: Notification?,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.removeNotification(notify)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    // mark as read notification
    fun markAsReadNotification(
        notify: Notification,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.markAsReadNotification(notify)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }


    fun updateTrackSetting(trackSetting: TrackSetting,
                           onSuccess: (String) -> Unit,
                           onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<String> = repository.apiUpdateTrackSetting(trackSetting)){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        //Log.d(TAG, "$apiRes.message")
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }

    fun checkLastApkVersion(onSuccess: (ApkVersion) -> Unit,
                           onFailed: (String) -> Unit)
    {
        viewModelScope.launch(Dispatchers.IO){
            when(val apiRes: ApiResult<ApkVersion> = repository.apiApkCheckLastVersion()){
                is ApiResult.Success->{
                    withContext(Dispatchers.Main) {
                        onSuccess(apiRes.data)
                    }
                }
                is ApiResult.Error->{
                    withContext(Dispatchers.Main) {
                        onFailed(apiRes.message)
                    }
                }
                else->{
                    withContext(Dispatchers.Main) {
                        onFailed(ApiNotification.INVALID_RESPONSE)
                    }
                }
            }
        }
    }



    fun getDistributionCenter(): List<DistCenter>?{
        return repository.getDistributionCenter()
    }

    fun getCompany(): List<Company>{
        return repository.getCompany()
    }

    fun getRetailStores(): List<Store>{
        val rss = repository.getRetailStores().value
        return if(!rss.isNullOrEmpty()) rss else  listOf()
    }
    fun getCartLastAdded(): Cart?{
        return repository.getLastAddedCart()
    }
    fun getCartLastRemoved(): Cart?{
        return repository.getLastRemovedCart()
    }

    fun getServerUrl(): String{
        return repository.getServerUrl()
    }

}
