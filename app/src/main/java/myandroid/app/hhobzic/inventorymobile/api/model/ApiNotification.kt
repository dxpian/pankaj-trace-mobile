package myandroid.app.hhobzic.inventorymobile.api.model

import java.util.Locale

class ApiNotificationEng {
    companion object Constant{
        const val CONNECTION_ERROR = "Failed to connect server"
        const val INVALID_RESPONSE = "Operation failed"
        const val OPERATION_SUCCESS = "Operation success"
        const val REQUIRE_FILL_FIELD = "Please input all fields"
        const val START = "Start"
        const val LOGIN_SUCCESS = "Login successfully"

        // message related with trip
        const val DELIVER_NO_FOUND = "You don't have deliver task now"
        const val DELIVER_START_SUCCESS = "Success to start delivery task"
        const val DELIVER_START_FAILED = "Failed to start delivery task"
        const val DELIVER_LOAD_SUCCESS = "Deliver task is loaded successfully"
        const val DELIVER_LOAD_FAILED = "Failed to load delivering state"
        const val DELIVER_START_TRIP = "You starts your trip"
        const val DELIVERI_CONFIRM_LEAVE_DC = "Please start trip first"
        const val DELIVER_NO_CART_RETURN = "No cart to return"
        const val DELIVER_TASK_COMPLETED = "You completed deliver task"
        const val DELIVER_TASK_COMPLETED_AUTOMATICALLY = "Completed automatically since truck parked for a long time"
        const val DELIVER_REQUEST_START = "You have to start deliver task first"
        const val DELIVER_COURSE_READ_FAILED = "Failed to read trip course information"
        const val DELIVER_WRONG_START_LOCATION = "Please start trip after reached at logistic company"
        const val DELIVER_START_AFTER_GPS_LOCATION = "Please wait a moment until we find out where you are"

        // message related with company
        const val COMP_SELECT_VALID_NAME = "Select a valid company name"

        // message related with DC
        const val DC_NO_FOUND = "No found distribution centers in this area"
        const val DC_REQUIRE_CONFIRM = "Select distribution center to load carts first"
        const val DC_ARRIVED = "You reached to new distribution center"
        const val REQUIRE_VALID_SELECT = "Select a distribution center"
        const val REQUIRE_VALID_COMPANY = "Select a valid logistic company"

        // message related with store
        const val STORE_NO_FOUND = "No found retail stores in this area"
        const val STORE_REQUIRE_CONFIRM = "Select retail stores to deliver"
        const val STORE_SELECT_SUCCESS = "Successfully add store to delivering target"
        const val STORE_DESELECT_SUCCESS = "Successfully remove store from delivering target"
        const val STORE_INVALID = "Invalid retail store selected"

        const val STORE_DESELECT_FAILED = "Failed to remove store from delivering target"
        const val STORE_ARRIVED = "You reached to new retail store"
        const val STORE_LEAVE = "You already leave store"
        const val STORE_REACHED_ALREADY = "You already reached store"
        const val STORE_DELIVER_ALREADY = "You already delivered to this store"
        const val STORE_DUP_SELECT = "This store is already selected for delivery target"
        const val STORE_CONFIRM_ARRIVAL = "Select retail store to deliver"
        const val STORE_NOT_IN_TRIP = "This store isn't contained in trip course"


        // message related with cart
        const val CART_NO_LOADED = "You don't have cart to deliver"
        const val CART_INVALID_TAG = "No found cart matched with tag"
        const val SELECT_VALID_CART = "Select valid cart"
        const val INVALID_CART = "No cart available"
        const val CART_DUPLICATED_IOT = "Duplicated IOT tag"
        const val CONFIRM_REQUIRE_LOAD = "You don't have cart to deliver now"
        const val CART_DELIVER_SUCCESS = "Successfully deliver cart to store"
        const val CART_DELIVER_FAILED = "Failed to deliver cart"
        const val CART_LOAD_SUCCESS = "Success to load cart"
        const val CART_UNLOAD_SUCCESS = "Success to unload cart"
        const val CART_UNLOAD_FAILED = "Failed to unload cart"
        const val CART_ALREADY_DELIVERED = "This cart is already delivered"
        const val CART_PICKUP_SUCCESS = "Successfully pickup cart from store"
        const val CART_PICKUP_FAILED = "Failed to pickup cart from store"
        const val CART_RETURN_SUCCESS = "Success to return cart to distribution center"
        const val CART_RETURN_FAILED = "Failed to return cart to distribution center"
        const val CART_LOAD_FAILED = "Failed to load cart"

    
        // message related with location track
        const val LOCATION_TRACK_SUCCESS = "Your location is saved successfully"
        const val LOCATION_TRACK_FAILED = "Failed to save your location"
        const val LOCATION_ARRIVED = "You arrived at "
        const val LOCATION_LEAVED = "You leaved from "

        const val USER_INVALID_INFO = "Invalid user information"
        const val USER_INVALID_NAME = "User name is invalid"
        const val USER_INVALID_EMAIL = "Email address is invalid"
        const val USER_INVALID_PHONE = "Phone number is invalid"
        const val USER_INVALID_ADDR = "Address is invalid"
        const val USER_INVALID_PWD = "Password is invalid"
        const val USER_PWD_UNMATCH = "Password is unmatched"
        const val USER_INVALID_LICENSE = "License is invalid"
        const val USER_FAILED_UPDATE = "Failed to update user profile"
        const val USER_SUCCESS_UPDATE = "Success to update user profile"
        const val USER_PWD_HINT = "Password should contain 6 characters at least"
        const val USER_INVALID_VIN = "VIN is invalid"

        const val GPS_PUSH_TEMP_VALUE = "Success to save the current location"

        // Notification related message
        const val NOTIFY_FETCH_SUCCESS = "Successfully load all notifications"
        const val NOTIFY_FETCH_FAILED = "Failed to load notifications"
        const val NOTIFY_DELETE_FAILED = "Failed to remove notifications"
        const val NOTIFY_DELETE_SUCCESS = "Failed to remove notifications"
        const val NOTIFY_MARKREAD_SUCCESS = "Success to mark notification as read"
        const val NOTIFY_MARKREAD_FAILED = "Failed to mark notification as read"

        const val NOTIFY_NO_CART = "No cart available"
        const val NOTIFY_INPUT_ADDRESS = "Enter the address of the current location"

        const val APK_UPDATE = "App Update Available"
        const val APK_UPDATE_AVAILBABLE = "A newer version of the app is available. Would you like to download and install it now?"
        const val APK_UPDATE_UNAVAILABLE = "You are already using the latest version."
        const val APK_DOWNLOAD_FAILED = "Unable to download the update. Please try again later."
        const val APK_FAILED_CHECK = "Failed to check for updates. Please check your network connection."

    }
}

class ApiNotificationJpn {
    companion object Constant {
        const val CONNECTION_ERROR = "サーバーに接続できませんでした"
        const val INVALID_RESPONSE = "操作に失敗しました"
        const val OPERATION_SUCCESS = "操作が成功しました"
        const val REQUIRE_FILL_FIELD = "すべての欄に入力してください"
        const val START = "開始"
        const val LOGIN_SUCCESS = "ログイン成功"

        // message related with trip
        const val DELIVER_NO_FOUND = "現在、配達タスクはありません"
        const val DELIVER_START_SUCCESS = "配信タスクが正常に開始されました"
        const val DELIVER_START_FAILED = "配達タスクの開始に失敗しました"
        const val DELIVER_LOAD_SUCCESS = "配達タスクが正常に読み込まれました"
        const val DELIVER_LOAD_FAILED = "配達状態の読み込みに失敗しました"
        const val DELIVER_START_TRIP = "輸送を開始しました"
        const val DELIVERI_CONFIRM_LEAVE_DC = "輸送を開始してください"
        const val DELIVER_NO_CART_RETURN = "返却するカートはありません"
        const val DELIVER_TASK_COMPLETED = "配達タスクを完了しました"
        const val DELIVER_TASK_COMPLETED_AUTOMATICALLY = "トラックが長時間駐車したため、自動的に完了しました"
        const val DELIVER_REQUEST_START = "最初に配達タスクを開始する必要があります"
        const val DELIVER_COURSE_READ_FAILED = "輸送コース情報の読み込みに失敗しました"
        const val DELIVER_WRONG_START_LOCATION = "物流会社に到着後、輸送を開始します"
        const val DELIVER_START_AFTER_GPS_LOCATION = "お客様の所在が判明するまで少々お待ちくださいませ"


        // message related with company
        const val COMP_SELECT_VALID_NAME = "有効な会社名を選択してください"

        // message related with DC
        const val DC_NO_FOUND = "このエリアに配送センターは見つかりません"
        const val DC_REQUIRE_CONFIRM = "カートを最初にロードするために配送センターを選択してください"
        const val DC_ARRIVED = "新しい配送センターに到着しました"
        const val REQUIRE_VALID_SELECT = "配送センターの選択"
        const val REQUIRE_VALID_COMPANY = "有効な物流会社を選択する"

        // message related with store
        const val STORE_NO_FOUND = "このエリアに店舗は見つかりません"
        const val STORE_REQUIRE_CONFIRM = "配送する店舗を選択してください"
        const val STORE_SELECT_SUCCESS = "店舗が配送対象に正常に追加されました"
        const val STORE_DESELECT_SUCCESS = "店舗が配送対象から正常に削除されました"
        const val STORE_INVALID = "無効な店舗が選択されました"

        const val STORE_DESELECT_FAILED = "店舗の配送対象からの削除に失敗しました"
        const val STORE_ARRIVED = "新しい店舗に到着しました"
        const val STORE_LEAVE = "すでに店舗を離れました"
        const val STORE_REACHED_ALREADY = "すでに店舗に到着しました"
        const val STORE_DELIVER_ALREADY = "すでにこの店舗に配送済みです"
        const val STORE_DUP_SELECT = "この店舗はすでに配送対象に選択されています"
        const val STORE_CONFIRM_ARRIVAL = "配送する店舗を選択してください"
        const val STORE_NOT_IN_TRIP = "この店舗は輸送コースに含まれていません"

        // message related with cart
        const val CART_NO_LOADED = "配達するカートはありません"
        const val CART_INVALID_TAG = "タグに一致するカートが見つかりません"
        const val SELECT_VALID_CART = "有効なカートを選択してください。"
        const val INVALID_CART = "カートがありません。"
        const val CART_DUPLICATED_IOT = "重複したIOTタグ"
        const val CONFIRM_REQUIRE_LOAD = "現在配達するカートはありません"
        const val CART_DELIVER_SUCCESS = "カートを店舗に正常に配送しました"
        const val CART_DELIVER_FAILED = "カートの配送に失敗しました"
        const val CART_LOAD_SUCCESS = "カートの積載に成功しました"
        const val CART_UNLOAD_SUCCESS = "カートの荷卸しに成功しました"
        const val CART_UNLOAD_FAILED = "カートの積載に失敗\n"
        const val CART_ALREADY_DELIVERED = "このカートはすでに配送されています"
        const val CART_PICKUP_SUCCESS = "店舗からカートの回収に成功しました"
        const val CART_PICKUP_FAILED = "店舗からカートの回収に失敗しました"
        const val CART_RETURN_SUCCESS = "配送センターにカートを正常に返却しました"
        const val CART_RETURN_FAILED = "カートを配送センターに返せなかった\n"
        const val CART_LOAD_FAILED = "カートの読み込みに失敗しました"
        // message related with location track
        const val LOCATION_TRACK_SUCCESS = "位置情報が正常に保存されました"
        const val LOCATION_TRACK_FAILED = "位置情報の保存に失敗しました"
        const val LOCATION_ARRIVED = "到着しました"
        const val LOCATION_LEAVED = "出発しました"

        const val USER_INVALID_INFO = "無効なユーザー情報です"
        const val USER_INVALID_NAME = "ユーザー名が無効です"
        const val USER_INVALID_EMAIL = "メールアドレスが無効です"
        const val USER_INVALID_PHONE = "電話番号が無効です"
        const val USER_INVALID_ADDR = "住所が無効です"
        const val USER_INVALID_PWD = "パスワードが無効です"
        const val USER_PWD_UNMATCH = "パスワードが一致しません"
        const val USER_INVALID_LICENSE = "ライセンスが無効です"
        const val USER_FAILED_UPDATE = "ユーザープロファイルの更新に失敗しました"
        const val USER_SUCCESS_UPDATE = "ユーザープロファイルの更新に成功しました"
        const val USER_PWD_HINT = "パスワードは6文字以上である必要があります"
        const val USER_INVALID_VIN = "VINが無効です"

        const val GPS_PUSH_TEMP_VALUE = "現在の位置を正常に保存しました"

        // Notification related message
        const val NOTIFY_FETCH_SUCCESS = "すべての通知を正常に読み込みました"
        const val NOTIFY_FETCH_FAILED = "通知の読み込みに失敗しました"
        const val NOTIFY_DELETE_FAILED = "通知の削除に失敗しました"
        const val NOTIFY_DELETE_SUCCESS = "通知を正常に削除しました"
        const val NOTIFY_MARKREAD_SUCCESS = "通知を既読としてマークしました"
        const val NOTIFY_MARKREAD_FAILED = "通知を既読としてマークすることに失敗しました"
        const val NOTIFY_NO_CART = "利用可能なカートがありません"
        const val NOTIFY_INPUT_ADDRESS = "現在の場所のアドレスを入力します"

        const val APK_UPDATE = "アプリのアップデート"
        const val APK_UPDATE_AVAILBABLE = "新しいバージョンが利用可能です。今すぐダウンロードしてインストールしますか？"
        const val APK_UPDATE_UNAVAILABLE = "ご利用中のバージョンは最新です。"
        const val APK_DOWNLOAD_FAILED = "アップデートのダウンロードに失敗しました。後でもう一度お試しください。"
        const val APK_FAILED_CHECK = "アップデートの確認に失敗しました。ネットワーク接続をご確認ください。"


    }
}

class ApiNotification {

    companion object {
        // Define instances for English and Japanese messages
        private val eng = ApiNotificationEng.Constant
        private val jpn = ApiNotificationJpn.Constant

        private fun isJapanese(): Boolean {
            val lgn = Locale.getDefault()
            return Locale.getDefault().language == "ja"
        }

        // Wrapper properties that switch based on locale
        val CONNECTION_ERROR: String
            get() = if (isJapanese()) jpn.CONNECTION_ERROR else eng.CONNECTION_ERROR

        val INVALID_RESPONSE: String
            get() = if (isJapanese()) jpn.INVALID_RESPONSE else eng.INVALID_RESPONSE

        val OPERATION_SUCCESS: String
            get() = if (isJapanese()) jpn.OPERATION_SUCCESS else eng.OPERATION_SUCCESS

        val REQUIRE_FILL_FIELD: String
            get() = if (isJapanese()) jpn.REQUIRE_FILL_FIELD else eng.REQUIRE_FILL_FIELD

        val START: String
            get() = if (isJapanese()) jpn.START else eng.START


        val LOGIN_SUCCESS: String
            get() = if (isJapanese()) jpn.LOGIN_SUCCESS else eng.LOGIN_SUCCESS

        // Trip-related messages
        val DELIVER_NO_FOUND: String
            get() = if (isJapanese()) jpn.DELIVER_NO_FOUND else eng.DELIVER_NO_FOUND

        val DELIVER_START_SUCCESS: String
            get() = if(isJapanese()) jpn.DELIVER_START_SUCCESS else eng.DELIVER_START_SUCCESS

        val DELIVER_START_FAILED: String
            get() = if(isJapanese()) jpn.DELIVER_START_FAILED else eng.DELIVER_START_FAILED

        val DELIVER_LOAD_SUCCESS: String
            get() = if (isJapanese()) jpn.DELIVER_LOAD_SUCCESS else eng.DELIVER_LOAD_SUCCESS

        val DELIVER_LOAD_FAILED: String
            get() = if (isJapanese()) jpn.DELIVER_LOAD_FAILED else eng.DELIVER_LOAD_FAILED

        val DELIVER_START_TRIP: String
            get() = if (isJapanese()) jpn.DELIVER_START_TRIP else eng.DELIVER_START_TRIP

        val DELIVERI_CONFIRM_LEAVE_DC: String
            get() = if (isJapanese()) jpn.DELIVERI_CONFIRM_LEAVE_DC else eng.DELIVERI_CONFIRM_LEAVE_DC

        val DELIVER_WRONG_START_LOCATION: String
            get() = if (isJapanese()) jpn.DELIVER_WRONG_START_LOCATION else eng.DELIVER_WRONG_START_LOCATION

        val DELIVER_START_AFTER_GPS_LOCATION: String
            get() = if (isJapanese()) jpn.DELIVER_START_AFTER_GPS_LOCATION else eng.DELIVER_START_AFTER_GPS_LOCATION


        val DELIVER_NO_CART_RETURN: String
            get() = if (isJapanese()) jpn.DELIVER_NO_CART_RETURN else eng.DELIVER_NO_CART_RETURN

        val DELIVER_TASK_COMPLETED: String
            get() = if (isJapanese()) jpn.DELIVER_TASK_COMPLETED else eng.DELIVER_TASK_COMPLETED

        val DELIVER_TASK_COMPLETED_AUTOMATICALLY: String
            get() = if (isJapanese()) jpn.DELIVER_TASK_COMPLETED_AUTOMATICALLY else eng.DELIVER_TASK_COMPLETED_AUTOMATICALLY

        val DELIVER_REQUEST_START: String
            get() = if (isJapanese()) jpn.DELIVER_REQUEST_START else eng.DELIVER_REQUEST_START

        val DELIVER_COURSE_READ_FAILED: String
            get() = if (isJapanese()) jpn.DELIVER_COURSE_READ_FAILED else eng.DELIVER_COURSE_READ_FAILED

        // DC-related messages
        val DC_NO_FOUND: String
            get() = if (isJapanese()) jpn.DC_NO_FOUND else eng.DC_NO_FOUND

        val DC_REQUIRE_CONFIRM: String
            get() = if (isJapanese()) jpn.DC_REQUIRE_CONFIRM else eng.DC_REQUIRE_CONFIRM

        val DC_ARRIVED: String
            get() = if (isJapanese()) jpn.DC_ARRIVED else eng.DC_ARRIVED

        val REQUIRE_VALID_DC_SELECT: String
            get() = if (isJapanese()) jpn.REQUIRE_VALID_SELECT else eng.REQUIRE_VALID_SELECT

        val REQUIRE_VALID_COMPANY: String
            get() = if (isJapanese()) jpn.REQUIRE_VALID_COMPANY else eng.REQUIRE_VALID_COMPANY

        // Store-related messages
        val STORE_NO_FOUND: String
            get() = if (isJapanese()) jpn.STORE_NO_FOUND else eng.STORE_NO_FOUND

        val STORE_REQUIRE_CONFIRM: String
            get() = if (isJapanese()) jpn.STORE_REQUIRE_CONFIRM else eng.STORE_REQUIRE_CONFIRM

        val STORE_SELECT_SUCCESS: String
            get() = if (isJapanese()) jpn.STORE_SELECT_SUCCESS else eng.STORE_SELECT_SUCCESS

        val STORE_DESELECT_SUCCESS: String
            get() = if (isJapanese()) jpn.STORE_DESELECT_SUCCESS else eng.STORE_DESELECT_SUCCESS

        val STORE_INVALID: String
            get() = if (isJapanese()) jpn.STORE_INVALID else eng.STORE_INVALID

        val STORE_DESELECT_FAILED: String
            get() = if (isJapanese()) jpn.STORE_DESELECT_FAILED else eng.STORE_DESELECT_FAILED

        val STORE_ARRIVED: String
            get() = if (isJapanese()) jpn.STORE_ARRIVED else eng.STORE_ARRIVED

        val STORE_LEAVE: String
            get() = if (isJapanese()) jpn.STORE_LEAVE else eng.STORE_LEAVE

        val STORE_REACHED_ALREADY: String
            get() = if (isJapanese()) jpn.STORE_REACHED_ALREADY else eng.STORE_REACHED_ALREADY

        val STORE_DELIVER_ALREADY: String
            get() = if (isJapanese()) jpn.STORE_DELIVER_ALREADY else eng.STORE_DELIVER_ALREADY

        val STORE_DUP_SELECT: String
            get() = if (isJapanese()) jpn.STORE_DUP_SELECT else eng.STORE_DUP_SELECT

        val STORE_CONFIRM_ARRIVAL: String
            get() = if (isJapanese()) jpn.STORE_CONFIRM_ARRIVAL else eng.STORE_CONFIRM_ARRIVAL

        val STORE_NOT_IN_TRIP: String
            get() = if (isJapanese()) jpn.STORE_NOT_IN_TRIP else eng.STORE_NOT_IN_TRIP

        // Cart-related messages
        val CART_NO_LOADED: String
            get() = if (isJapanese()) jpn.CART_NO_LOADED else eng.CART_NO_LOADED

        val CART_INVALID_TAG: String
            get() = if (isJapanese()) jpn.CART_INVALID_TAG else eng.CART_INVALID_TAG

        val CART_DUPLICATED_IOT: String
            get() = if (isJapanese()) jpn.CART_DUPLICATED_IOT else eng.CART_DUPLICATED_IOT

        val CONFIRM_REQUIRE_LOAD: String
            get() = if (isJapanese()) jpn.CONFIRM_REQUIRE_LOAD else eng.CONFIRM_REQUIRE_LOAD

        val CART_DELIVER_SUCCESS: String
            get() = if (isJapanese()) jpn.CART_DELIVER_SUCCESS else eng.CART_DELIVER_SUCCESS

        val SELECT_VALID_CART: String
            get() = if (isJapanese()) jpn.SELECT_VALID_CART else eng.SELECT_VALID_CART

        val CART_DELIVER_FAILED: String
            get() = if (isJapanese()) jpn.CART_DELIVER_FAILED else eng.CART_DELIVER_FAILED

        val INVALID_CART: String
            get() = if (isJapanese()) jpn.INVALID_CART else eng.INVALID_CART


        val CART_LOAD_SUCCESS: String
            get() = if (isJapanese()) jpn.CART_LOAD_SUCCESS else eng.CART_LOAD_SUCCESS

        val CART_LOAD_FAILED: String
            get() = if (isJapanese()) jpn.CART_LOAD_FAILED else eng.CART_LOAD_FAILED

        val CART_UNLOAD_SUCCESS: String
            get() = if (isJapanese()) jpn.CART_UNLOAD_SUCCESS else eng.CART_UNLOAD_SUCCESS

        val CART_UNLOAD_FAILED: String
            get() = if (isJapanese()) jpn.CART_UNLOAD_FAILED else eng.CART_UNLOAD_FAILED

        val CART_ALREADY_DELIVERED: String
            get() = if (isJapanese()) jpn.CART_ALREADY_DELIVERED else eng.CART_ALREADY_DELIVERED

        val CART_PICKUP_SUCCESS: String
            get() = if (isJapanese()) jpn.CART_PICKUP_SUCCESS else eng.CART_PICKUP_SUCCESS

        val CART_PICKUP_FAILED: String
            get() = if (isJapanese()) jpn.CART_PICKUP_FAILED else eng.CART_PICKUP_FAILED

        val CART_RETURN_SUCCESS: String
            get() = if (isJapanese()) jpn.CART_RETURN_SUCCESS else eng.CART_RETURN_SUCCESS

        val CART_RETURN_FAILED: String
            get() = if (isJapanese()) jpn.CART_RETURN_FAILED else eng.CART_RETURN_FAILED


        // Location-related messages
        val LOCATION_TRACK_SUCCESS: String
            get() = if (isJapanese()) jpn.LOCATION_TRACK_SUCCESS else eng.LOCATION_TRACK_SUCCESS

        val LOCATION_TRACK_FAILED: String
            get() = if (isJapanese()) jpn.LOCATION_TRACK_FAILED else eng.LOCATION_TRACK_FAILED

        val LOCATION_ARRIVED: String
            get() = if (isJapanese()) jpn.LOCATION_ARRIVED else eng.LOCATION_ARRIVED

        val LOCATION_LEAVED: String
            get() = if (isJapanese()) jpn.LOCATION_LEAVED else eng.LOCATION_LEAVED

        // User-related messages
        val USER_INVALID_INFO: String
            get() = if (isJapanese()) jpn.USER_INVALID_INFO else eng.USER_INVALID_INFO

        val USER_INVALID_NAME: String
            get() = if (isJapanese()) jpn.USER_INVALID_NAME else eng.USER_INVALID_NAME

        val USER_INVALID_EMAIL: String
            get() = if (isJapanese()) jpn.USER_INVALID_EMAIL else eng.USER_INVALID_EMAIL

        val USER_INVALID_PHONE: String
            get() = if (isJapanese()) jpn.USER_INVALID_PHONE else eng.USER_INVALID_PHONE

        val USER_INVALID_ADDR: String
            get() = if (isJapanese()) jpn.USER_INVALID_ADDR else eng.USER_INVALID_ADDR

        val USER_INVALID_PWD: String
            get() = if (isJapanese()) jpn.USER_INVALID_PWD else eng.USER_INVALID_PWD

        val USER_PWD_UNMATCH: String
            get() = if (isJapanese()) jpn.USER_PWD_UNMATCH else eng.USER_PWD_UNMATCH

        val USER_INVALID_LICENSE: String
            get() = if (isJapanese()) jpn.USER_INVALID_LICENSE else eng.USER_INVALID_LICENSE

        val USER_FAILED_UPDATE: String
            get() = if (isJapanese()) jpn.USER_FAILED_UPDATE else eng.USER_FAILED_UPDATE

        val USER_SUCCESS_UPDATE: String
            get() = if (isJapanese()) jpn.USER_SUCCESS_UPDATE else eng.USER_SUCCESS_UPDATE

        val USER_PWD_HINT: String
            get() = if (isJapanese()) jpn.USER_PWD_HINT else eng.USER_PWD_HINT

        val USER_INVALID_VIN: String
            get() = if (isJapanese()) jpn.USER_INVALID_VIN else eng.USER_INVALID_VIN

        val GPS_PUSH_TEMP_VALUE: String
            get() = if (isJapanese()) jpn.GPS_PUSH_TEMP_VALUE else eng.GPS_PUSH_TEMP_VALUE

        // Notification-related messages
        val NOTIFY_FETCH_SUCCESS: String
            get() = if (isJapanese()) jpn.NOTIFY_FETCH_SUCCESS else eng.NOTIFY_FETCH_SUCCESS

        val NOTIFY_FETCH_FAILED: String
            get() = if (isJapanese()) jpn.NOTIFY_FETCH_FAILED else eng.NOTIFY_FETCH_FAILED

        val NOTIFY_DELETE_FAILED: String
            get() = if (isJapanese()) jpn.NOTIFY_DELETE_FAILED else eng.NOTIFY_DELETE_FAILED

        val NOTIFY_DELETE_SUCCESS: String
            get() = if (isJapanese()) jpn.NOTIFY_DELETE_SUCCESS else eng.NOTIFY_DELETE_SUCCESS

        val NOTIFY_MARKREAD_SUCCESS: String
            get() = if (isJapanese()) jpn.NOTIFY_MARKREAD_SUCCESS else eng.NOTIFY_MARKREAD_SUCCESS

        val NOTIFY_MARKREAD_FAILED: String
            get() = if (isJapanese()) jpn.NOTIFY_MARKREAD_FAILED else eng.NOTIFY_MARKREAD_FAILED

        val NOTIFY_NO_CART: String
            get() = if (isJapanese()) jpn.NOTIFY_NO_CART else eng.NOTIFY_NO_CART

        val COMPANY_SELECT_NAME: String
            get()= if(isJapanese()) jpn.COMP_SELECT_VALID_NAME else eng.COMP_SELECT_VALID_NAME

        val NOTIFY_INPUT_ADDRESS: String
            get() = if(isJapanese()) jpn.NOTIFY_INPUT_ADDRESS else eng.NOTIFY_INPUT_ADDRESS


        // apk related
        val APK_UPDATE : String
            get() = if(isJapanese()) jpn.APK_UPDATE  else eng.APK_UPDATE

        val APK_UPDATE_AVAILBABLE  : String
            get() = if(isJapanese()) jpn.APK_UPDATE_AVAILBABLE   else eng.APK_UPDATE_AVAILBABLE

        val APK_UPDATE_UNAVAILABLE   : String
            get() = if(isJapanese()) jpn.APK_UPDATE_UNAVAILABLE    else eng.APK_UPDATE_UNAVAILABLE

        val APK_DOWNLOAD_FAILED    : String
            get() = if(isJapanese()) jpn.APK_DOWNLOAD_FAILED     else eng.APK_DOWNLOAD_FAILED

        val APK_FAILED_CHECK     : String
            get() = if(isJapanese()) jpn.APK_FAILED_CHECK      else eng.APK_FAILED_CHECK


    }
}


fun getArrivedMessage(name: String): String{
    return "${ApiNotification.LOCATION_ARRIVED} $name"
}
fun getLeavedMessage(name: String): String{
   return "${ApiNotification.LOCATION_LEAVED} $name"
}

fun getLoadCartMessage(cartCount: Int, storeName: String): String{
    return "You loaded $cartCount cart(s) from retail store ${storeName}"
}

fun getPickCartMessage(cartCount: Int, storeName: String): String{
    return "You picked $cartCount cart(s) from retail store ${storeName}"
}
