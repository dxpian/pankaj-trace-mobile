package myandroid.app.hhobzic.inventorymobile.home

class AppMacro {
    companion object Constant {
        const val USE_STRON_PASSWORD = false                // if true, require strong password

        const val REQUIRE_LOGIN = true                      // do login if true
        const val SIMULATE_GPS = false                       // use the virtual GPS signal for debugging purpose
        const val SIMULATE_NEIGHBOR_GPS = false              // true when simulating the neighbor DC. valid only if SIMULATE_GPS = true

        const val AUTO_START_TRIP = true                    // start trip automatically if task is completed
        const val SHOW_PLAN_COURSE = false                  // show the planned course on map

        var LOCATION_UPDATE_INTERVAL = if (SIMULATE_GPS) 2000L else 20000L // Simplified// 10 second. Time interval which get the gps location
        const val LOCATION_REPORT_INTERVAL_HIGH = 2           // 3 times of LOCATION_UPDATE_INTERVAL. High time interval which report the GPS location to cloud server
        const val LOCATION_REPORT_INTERVAL_LOW = LOCATION_REPORT_INTERVAL_HIGH * 4     // 10 minutes. Low time interval which report the GPS location to cloud server.

        const val LOCATION_REACHED_DIST_THRESHOLD_METER = 500           // the distance threshold which could be consider for truck is already reached at one location entity
        const val DRIVER_PASSED_SEC_WITHOUT_STOP = 30                   // average seconds to pass an entity(LC, DC) without staying

        const val WIFI_SCAN_UPDATE_ONE_TIME_SEC = 2 * 60          // max time seconds to find one BLE tag at least
        const val WIFI_SCAN_FIND_ALL_TIME_SEC = 15 * 60L           // max time seconds to find all tag in truck


        const val SERVER_URL_LOCAL="http://192.168.101.62:9002/api/v2/"
        const val SERVER_URL_CLOUD="https://ibtrace.com/inventoryserver/api/v2/"
        const val SERVER_WS_URL_LOCAL = "ws://192.168.101.62:9002/"
        const val SERVER_WS_URL_CLOUD = "wss://ibtrace.com:9002/"

        const val NOTIFY_EVENT_NAME = "notification"
        const val SHAREDPREF_NAME = "AppPrefs"
        const val KEY_USERINFO = "UserInfo"
        const val KEY_COMPANYINFO = "CompanyInfo"
        const val KEY_LANGUAGE = "Language"
        const val KEY_TOKEN = "token"
        const val MAP_INIT_SCALE = 10.0f

        const val ENABLE_SCAN_VERBOSE = false

        const val SELECTED_DC = "SelectedDC"
        const val SELECTED_RS = "SelectedRS"
        const val NODE_CLICK = false
    }
}

class Language{
    companion object{
        const val ENG =  "en"
        const val JPN = "ja"
    }
}

class LanguageCode{
    companion object{
        const val ENG =  0
        const val JPN = 1
    }
}

class TokenKey{
    companion object {
        const val accesstoken = "accesstoken"
        const val language = "language"
    }
}

