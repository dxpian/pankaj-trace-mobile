package myandroid.app.hhobzic.inventorymobile.api.model

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String
)

data class ApiResponse<T>(
    val error_code: Int,
    val meta: String = "",
    val data:T,
    val accesstoken: String = "",
    val refreshtoken: String = ""
)


data class Driver(
    val id: String?,
    val company: String,    // company name
    val name: String,   // driver name
    val phone: String?,
    val email: String?,
    val contact:String, // contact information
    val license:String, // license string
    val truck: String, // truck ID or vin number
    var vin: String,
    val safety: Int = 0,
    var token: String?,
    var password: String,
    var time_taskstart: String,
    var time_taskstop: String,
    var auto_start:Int, // true if task is autostarted
)

data class Parking(
    val id: String,
    val company: String,
    val latitude: String,
    val longitude: String,
    val description: String?
)


data class Company(
    val id: String,
    val name: String,
    val description: String,
    val latitude: String,       // company position
    val longitude: String,
    val latitudePark: String?,
    val longitudePark: String?,
    val code: String,
    val address: String,
    val parkings: String?,  // json string of parking list

    // property for delivered item
    var node_id: String?,       // id in DistCenterDeliver table
    var itemLoaded: Int?,
    var itemreturned:Int?,
    var timereached:String?,
    var timeleave:String?,
    var status: Int?,//DC_DELIVER_STATUS
    // property for view
    var expanded:Boolean = false

)

data class DistCenter(
    val id: String,
    val name: String,
    val address: String,
    val code: String,
    val latitude: Number,
    val longitude: Number,
    val neighbor_company: String,           // company ID which is neighbored with this company
    val neighbor_timeout: Int,           // timeout minutes to stop the task in neighber DC

    // property for delivered item
    var node_id: String?,       // id in DistCenterDeliver table
    var itemLoaded: Int?,
    var itemreturned:Int?,
    var timereached:String?,
    var timeleave:String?,
    var status: Int?,//DC_DELIVER_STATUS

    // property for view
    var expanded:Boolean = false,
)

data class Store(
    val id: String,         // Store ID
    val name: String,       // Store name
    val address: String,
    val code: String,
    // val area_name: String,  // area name
    val latitude: String,   // latitude of gps location
    val longitude: String,  //  longitude of gps location
    val contract: String,   // control phone number
    val nearest: Int,       // distance to nearest item
    val passedSec: Int,

//    val pickup: Int?,       // total number of pickup
    // val empty_return: Int?,   // empty return
    val time_received:String?,  // timestamp of last received
    val time_pickup:String?,    // timestamp of last pickup
    val time_empty:String?,     // timestmp of last empty

    // properties of delivered properties
    var node_id: String?,       // id in StoreDeliver table
    var status: Int?,           // STORE_DELIVER_STATUS
    var itemdelivered: Int?,    // how many item delivered to store
    var itemloaded:Int?,      // how many item loaded from store
    var timereached: String?,
    var timeleave:String?,
    // var sequence: Number?,

    // property for view
    var expanded:Boolean = false,
)



// data structure for cart input
data class Cart(
    val empty: Boolean = false,
    var status: Int = CART_STATUS.DC.code,    //
    var id: String = "",
    var iot: String,       // tag information of tag
    var location: String?,  // location information of Cart
    var product: String?    // product string in cart
){
    companion object {
        fun fromCartDeliver(cartDeliver: CartDeliver): Cart {
            return Cart(
                empty =  cartDeliver.product?.isEmpty()==true,
                status = cartDeliver.status,
                id = cartDeliver.cart_id,
                iot = cartDeliver.iot,
                location = cartDeliver.location?:"",
                product =  cartDeliver.product?:"",
            )
        }
    }
}

data class TrackSetting(
    var scanTimeSec: Int,
    var locationUpdateSec: Int,
    var distanceMeter: Int,
    var timezone: String,                   // timezone of data
    var passedSec: Int,
    var task_start: String,
    var task_stop: String,
    var minutes_expire_task: Int,
    var auto_start: Int,
)

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    data class Code(val code: Number): ApiResult<Nothing>()
}


sealed class DeliverStartItem {
    data class Company(val comp: myandroid.app.hhobzic.inventorymobile.api.model.Company) : DeliverStartItem()
    data class DistCenter(val dc: myandroid.app.hhobzic.inventorymobile.api.model.DistCenter) : DeliverStartItem()
}