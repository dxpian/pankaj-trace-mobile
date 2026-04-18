package myandroid.app.hhobzic.inventorymobile.api.truck

import myandroid.app.hhobzic.inventorymobile.api.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CartService {
    @POST("mobile/distcenter/getNames")
    fun getDistCenterNames(): Call<ApiResponse<List<DistCenter>>>

    @POST("mobile/cart/getCartDist/{dist_id}")
    fun getCartsOfDistCenter(@Path("dist_id") dist_id: String): Call<ApiResponse<List<Cart>>>

    // getAreaStores
    @POST("mobile/store/getAllStores")
    fun getStoresOfArea(): Call<ApiResponse<List<Store>>>

    // download image of retail store
    @POST("mobile/store/downloadImage/{store_id}")
    fun downloadStoreImage(
        @Path("store_id") store_id: String
    ): Call<ResponseBody>


    // download image of retail store
    @POST("mobile/distcenter/downloadImage/{dist_id}")
    fun downloadDistCenterImage(
        @Path("dist_id") dist_id: String
    ): Call<ResponseBody>

    /////////////////////////////////// company related api ////////////////////////////////
    @POST("mobile/company/downloadImage/{comp_id}")
    fun downloadCompanyImage(
        @Path("comp_id") dist_id: String
    ): Call<ResponseBody>



    /////////////////////////////////// shipment related api ////////////////////////////////
    @FormUrlEncoded
    @POST("mobile/ship/queryStatus")
    fun queryDeliverStatus(
        @Field("id_driver") id_drvier: String,
        @Field("id_truck") id_truck: String,
        @Field("force") force: Boolean
    ): Call<ApiResponse<DeliveryState>>

    @FormUrlEncoded
    @POST("mobile/ship/arriveDc/{deliver_id}")
    fun arriveDistCenter(
        @Path("deliver_id") deliver_id: String,
        @Field("id_dc") dc_id: String,
    ): Call<ApiResponse<DeliveryState>>

    @FormUrlEncoded
    @POST("mobile/ship/returnDc/{deliver_id}")
    fun arriveDistCenterForReturning(
        @Path("deliver_id") deliver_id: String,
        @Field("id_dc") dc_id: String,
    ): Call<ApiResponse<NodeDeliver>>


    @FormUrlEncoded
    @POST("mobile/ship/leaveDistCenter/{deliver_id}")
    fun leaveDistCenter(
        @Path("deliver_id") deliver_id: String,
        @Field("id_dc") dc_id: String,
    ): Call<ApiResponse<DeliveryState>>

    @FormUrlEncoded
    @POST("mobile/ship/initStatus")
    fun initDeliverStatus(
        @Field("id_driver") id_drvier: String,
        @Field("id_truck") id_truck: String,
        @Field("id_force") force: Boolean
    ): Call<ApiResponse<DeliveryState>>

    @FormUrlEncoded
    @POST("mobile/ship/startDeliver")
    fun startDeliverTask(
        @Field("id_driver") id_drvier: String,
        @Field("id_truck") id_truck: String,
        @Field("id_company") id_comapny: String,
        @Field("id_force") force: Boolean
    ): Call<ApiResponse<DeliveryState>>


    @FormUrlEncoded
    @POST("mobile/ship/startDeliverAudo")
    fun startDeliverTaskAuto(
        @Field("id_driver") id_drvier: String,
        @Field("id_truck") id_truck: String,
        @Field("id_company") id_comapny: String,
        @Field("id_dc") id_dc: String,
        @Field("id_force") force: Boolean
    ): Call<ApiResponse<DeliveryState>>


    @FormUrlEncoded
    @POST("mobile/ship/arriveCompany/{deliver_id}")
    fun arriveCompany(
        @Path("deliver_id") id_drvier: String,
        @Field("id") company_id: String,
    ): Call<ApiResponse<DeliveryState>>

    @FormUrlEncoded
    @POST("mobile/ship/arriveCompanyWithoutCompleting/{deliver_id}")
    fun arriveCompanyWithoutCompleting(
        @Path("deliver_id") id_drvier: String,
        @Field("id") company_id: String,
    ): Call<ApiResponse<NodeDeliver>>


    @FormUrlEncoded
    @POST("mobile/ship/leaveCompany/{deliver_id}")
    fun leaveCompany(
        @Path("deliver_id") id_drvier: String,
        @Field("id") company_id: String,
    ): Call<ApiResponse<NodeDeliver>>

    // stop deliver task forcely
    @FormUrlEncoded
    @POST("mobile/ship/timoutInNeighberDc/{deliver_id}")
    fun stopTaskForcely(
        @Path("deliver_id") id_drvier: String,
        @Field("id") dc_id: String,
    ): Call<ApiResponse<DeliveryState>>

    @POST("mobile/driver/downloadProfile/{driver_id}")
    fun downloadPhoto(
        @Path("driver_id") driver_id: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("mobile/ship/complete/{deliver_id}")
    fun stopDeliver(
        @Path("deliver_id") deliver_id: String,
        @Field("node_type") node_type: Int,
        @Field("node_id") node_id: String,
        @Field("reason") reason: Int,
        @Field("description") description: String,
        @Field("force") force: Boolean
    ): Call<ApiResponse<DeliveryState>>


    @FormUrlEncoded
    @POST("mobile/ship/completeAuto/{deliver_id}")
    fun stopDeliverAuto(
        @Path("deliver_id") deliver_id: String,
        @Field("node_type") node_type: Int,
        @Field("node_id") node_id: String?,
        @Field("reason") reason: Int,
        @Field("description") description: String,
        @Field("time") time: String,
    ): Call<ApiResponse<DeliveryState>>


    @FormUrlEncoded
    @POST("mobile/ship/arrivedStore/{deliver_id}")
    fun arriveStore(
        @Path("deliver_id") deliver_id: String,
        @Field("node_id") node_id: String
    ): Call<ApiResponse<NodeDeliver>>

    @FormUrlEncoded
    @POST("mobile/ship/leavestore/{deliver_id}")
    fun leaveStore(
        @Path("deliver_id") deliver_id: String,
        @Field("node_id") node_id: String
    ): Call<ApiResponse<DeliveryState>>


    // deselect shop for trip
    @FormUrlEncoded
    @POST("mobile/ship/selectshop/{deliver_id}")
    fun selectStore(
        @Path("deliver_id") id_deliver: String,
        @Field("id_retail") id_store: String,

        ): Call<ApiResponse<NodeDeliver>>


    // deselect shop for trip
    @FormUrlEncoded
    @POST("mobile/ship/deselectshop/{deliver_id}")
    fun deselectStore(
        @Path("deliver_id") id_deliver: String,
        @Field("id_retail") id_store: String,

    ): Call<ApiResponse<Unit>>


    @FormUrlEncoded
    @POST("mobile/ship/loadcart/{deliver_id}")
    fun loadCart(
        @Path("deliver_id") id_deliver: String,
        @Field("node_type") node_type: Int,
        @Field("node_id") node_id: String,
        @Field("tag") tag: String,
        ): Call<ApiResponse<Cart>>

    @FormUrlEncoded
    @POST("mobile/ship/loadcartByTag/{deliver_id}")
    fun loadCartsByTag(
        @Path("deliver_id") id_deliver: String,
        @Field("node_type") node_type: Int,
        @Field("node_id") node_id: String,
        @Field("tag") tags: String,
    ): Call<ApiResponse<DistCenterDeliverResult>>


    @FormUrlEncoded
    @POST("mobile/ship/unloadcart/{deliver_id}")
    fun unloadCart(
        @Path("deliver_id") id_deliver: String,
        @Field("node_type") node_type: Int,
        @Field("node_id") node_id: String,
        @Field("tag") cart_id: String,
    ): Call<ApiResponse<NodeDeliver>>

    @FormUrlEncoded
    @POST("mobile/ship/returnCart/{deliver_id}")
    fun returnCart(
        @Path("deliver_id") id_deliver: String,
        @Field("dc_id") node_type: String,
        @Field("cart_id") cart_id: String,
    ): Call<ApiResponse<NodeDeliver>>

    @FormUrlEncoded
    @POST("mobile/ship/returnCartByTag/{deliver_id}")
    fun returnCartByTag(
        @Path("deliver_id") id_deliver: String,
        @Field("node_id") node_id: String,
        @Field("tag") tag: String,
    ): Call<ApiResponse<DistCenterDeliverResult>>



    @FormUrlEncoded
    @POST("mobile/ship/deliverCart/{deliver_id}")
    fun deliverCart(
        @Path("deliver_id") id_deliver: String,
        @Field("receiver_type") receiver_type: Int, // NODE_TYPE.STORE
        @Field("node_id") node_id: String,  // store id
        @Field("id_cart") id_cart: String,
    ): Call<ApiResponse<NodeDeliver>>

    @FormUrlEncoded
    @POST("mobile/ship/deliverCartByTag/{deliver_id}")
    fun deliverCartByTag(
        @Path("deliver_id") id_deliver: String,
        @Field("receiver_type") receiver_type: Int, // NODE_TYPE.STORE
        @Field("node_id") node_id: String,  // store id
        @Field("tag") tags: String,
    ): Call<ApiResponse<DeliveredStoreResult>>



    @FormUrlEncoded
    @POST("mobile/ship/pickupCart/{deliver_id}")
    fun pickEmptyCartFromStore(
        @Path("deliver_id") id_deliver: String,
        @Field("node_type") node_type: Int, // NODE_TYPE.STORE
        @Field("node_id") node_id: String,  // store id
        @Field("iot_tag") iot_tag: String,  // iot tag
    ): Call<ApiResponse<Cart>>

    @FormUrlEncoded
    @POST("mobile/ship/pickupCartByTag/{deliver_id}")
    fun pickEmptyCartFromStoreByTag(
        @Path("deliver_id") id_deliver: String,
        @Field("node_type") node_type: Int, // NODE_TYPE.STORE
        @Field("node_id") node_id: String,  // store id
        @Field("iot_tag") tag: String,  // iot tag
    ): Call<ApiResponse<DeliveredStoreResult>>



    @FormUrlEncoded
    @POST("mobile/ship/pushMobileGpsLocation/{deliver_id}")
    fun pushGpsLocation(
        @Path("deliver_id") id_deliver: String,
        @Field("route") route: String?, // route ID
        @Field("latitude") latitude: Number, // NODE_TYPE.STORE
        @Field("longitude") longitude: Number,  // store id
        @Field("speed") speed: Number,
        @Field("elapsedSec") elapsedSec: Int,
        @Field("fuel") fuel: String,  // iot tag
        @Field("co2") co2: String
    ): Call<ApiResponse<Unit>>

    // read the point list of deliver task
    @POST("mobile/ship/queryTimeInformation/{deliver_id}")
    fun readCourseInformation(
        @Path("deliver_id") id_deliver: String,
    ): Call<ApiResponse<List<ServerPoint>>>

    // read the speed information within one week
    @FormUrlEncoded
    @POST("mobile/ship/queryWeekSpeed/{truck_id}")
    fun readWeekSpeedInformation(
        @Path("truck_id") id_truck: String,
        @Field("days") days: Number
    ): Call<ApiResponse<List<ServerPoint>>>


    @Multipart
    @POST("mobile/driver/updateProfile/{driver_id}")
    fun updateUserProfile(
        @Path("driver_id") driverId: String,
        @Part("company") company: RequestBody,
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("email") email: RequestBody,
        @Part("contact") contract: RequestBody,
        @Part("password") password: RequestBody,
        @Part("license") license: RequestBody,
        @Part file: MultipartBody.Part?
    ): Call<ApiResponse<Driver>>


    @FormUrlEncoded
    @POST("mobile/driver/updateTruck/{driver_id}")
    fun updateTruck(
        @Path("driver_id") driverId: String,
        @Field("vin") vinNumber: String, // NODE_TYPE.STORE
        @Field("auto_start") autostart: Int, // NODE_TYPE.STORE
        @Field("time_taskstart") from: String, // NODE_TYPE.STORE
        @Field("time_taskstop") to: String, // NODE_TYPE.STORE
    ): Call<ApiResponse<Driver>>

    @POST("mobile/driver/delete/{driver_id}")
    fun deleteDriver(
        @Path("driver_id") driverId: String
    ): Call<ApiResponse<Unit>>

    @POST("mobile/notify/readNotify/{deliver_id}")
    fun readNotify(
        @Path("deliver_id") deliverId: String
    ): Call<ApiResponse<List<Notification>>>

    @POST("mobile/notify/markReadNotify/{notify_id}")
    fun markAsReadNotify(
        @Path("notify_id") notifyId: String
    ): Call<ApiResponse<Notification>>



    @DELETE("mobile/notify/deleteNotify/{notify_id}")
    fun deleteNotify(
        @Path("notify_id") notifyId: String
    ): Call<ApiResponse<Unit>>


    @DELETE("mobile/notify/deleteAllNotify")
    fun deleteAllNotify(): Call<ApiResponse<Unit>>

    // get track setting

    @POST("mobile/driver/getTrackSetting/{driver_id}")
    fun getTrackSetting(
        @Path("driver_id") driverId: String
    ): Call<ApiResponse<ServerTrackSetting>>


    // update track setting
    @FormUrlEncoded
    @POST("mobile/driver/updateTrackSetting/{driver_id}")
    fun updateTrackSetting(
        @Path("driver_id") driverId: String,
        @Field("ble_scan_sec") scaSecond: Int, // NODE_TYPE.STORE
        @Field("distance_meter") distance: Int,
        @Field("gps_fetch_sec") gpsFetchSec: Int,
        @Field("timezone") timezone: String,
    ): Call<ApiResponse<Unit>>

    // check last APK version
    @POST("mobile/apk/checkLastApk")
    fun checkLastApkVersion(
    ): Call<ApiResponse<ApkVersion>>

    // Download geo of node
    @FormUrlEncoded
    @POST("mobile/geo/download")
    fun downloaddGeodata(
        @Field("type") nodetype: Int, // NODE_TYPE.STORE
        @Field("id") nodeID: String
    ): Call<ApiResponse<String>>



}