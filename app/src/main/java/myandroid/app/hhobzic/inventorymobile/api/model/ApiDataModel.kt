package myandroid.app.hhobzic.inventorymobile.api.model

enum class TRACK_STATUS(val code: Int) {
    INIT(0),              // truck starts moving
    LOADING(1),           // truck is loading from distribution center
    PROGRESS(2),          // truck is on road
    DELIVERING(3),        // truck is delivering item to retail store
    RETURNING(4),         // truck is returning cart to distribution center
    COMPLETED(5),         // truck is completed
    UNKNOWN(6),
    STATE_CNT(7),
}

enum class NODE_TYPE(val code: Int) {
    UNK(0),           // unknown type node
    DC(1),            // distribution center node
    STORE(2),         // retail store node
    COMPANY(3),
    PARKING(4),        // parking
    HOME(5),
    TOTAL_CNT(6),     // max number of node types
}

enum class NODE_STATUS(val code: Int){
    NO_DELIVERED(0),    // truck isn't reached yet
    REACHED(1),         // truck reached
    LEAVE(2)            // truck leaved
}


enum class CART_STATUS(val code: Int) {
    UNKNOWN(0),
    DC(1),
    DELIVERING (2),
    STORE(3),
    PICKUP(4),
    RETRUN(5);
    companion object {
        fun fromCode(code: Int): CART_STATUS? {
            return entries.find { it.code == code }
        }
    }
}

data class  NodeDeliver( // corresponded to DeliverStores
    val id: String,                 // node id in DeliverNode table
    val nodetype: Int,           // node type
    val nodeid: String,             // id of each item(DC id, RS id, COMPANY id)
    val itemLoaded: Int,
    val itemreturned:Int,
    val timereached:String,
    val timeleave:String?,
    val status: Int,                // node status
)

data class DistCenterDeliverResult(
    val dc: NodeDeliver,
    val carts: List<Cart>
)


data class DeliveredStoreResult(
    val store: NodeDeliver,
    val carts: List<Cart>
)




data class CartDeliver(
    val id:String,
    val cart_id: String,
    val iot:String,
    val product: String?,
    val location:  String?,
    val timeloaded: String?,
    val receivertype: Int?,
    val receiverid: String?,
    val status:Int,
    val timedelivered:String?
)

data class NodeDeliverResult(
    val node: NodeDeliver,
    val carts: List<CartDeliver>
)


data class DeliveryState(
    val id: String,
    val route: String?,         // route ID
    val start_node_type: Int?,
    val startime: String?,
    val end_node_type: Int?,
    val end_id: String?,
    val endtime: String?,
    var status: Int, // TRACK_STATUS
    val latitude: Double?,
    val longitude: Double?,
    val nodes: List<NodeDeliver>,
    val carts: List<CartDeliver>?
)

enum class DELIVER_STOP_REASON(val code: Int) {
    DELIVERING(0),         // unknown state
    COMPLETED(1),   // completed
    DELAYED(2),     // delayed
    CANCELLED(3),   // cancelled
    OTHER(4),       // task is completed by app. on parking
    AUTO(5),        // task is completed automatically. No move more than 2 hours
    EXPIRED(5),     // task is expired. stop when start the next task.   // max number of node types
}


// data class used to save the history. Don't modify it because it is same as RecordPoint of server
data class ServerPoint(
    var latitude: Float,
    var longitude: Float,
    var speed: Float,
    var time: String?,
    var elapsedSec: Int,
    var distance: Int?
)


data class ServerTrackSetting(
    val ble_scan_sec: Int,
    val distance_meter: Int,
    val gps_fetch_sec: Int,
    val timezone: String,
    val passedSec: Int,
    val task_start: String,
    val task_stop: String,
    val minutes_expire_task: Int,
    val auto_start: Int,
)

class NotifyType{
    companion object Constant{
        const val CART = 0      // cart related notification
        const val ROUTE = 1     // route related notification
        const val USER = 2      // user setting related notification
        const val DELIVER = 3
    }
}

data class Notification(
    val id: String,
    val message: String,
    val time: String,   // log time
    val type: Int,       // NotifyType
    var read: Boolean
)

data class LoginRequest(
    val company: String,
    val name: String,
    val password: String,
    val versionName: String?,
    val versionCode: Int?
)

data class ApkVersion(
    val id: String,
    val version: String,        // version string;
    val code:   Int,           // version code
    val description: String?,       // description of apk file
    val uploaded: String?          // apk uploaded time
)




