package myandroid.app.hhobzic.inventorymobile.api.model

class ErrorCode {
    companion object Constant {
        const val TZ_SUCCESS = 0             // success
        const val TZ_INVALID_USER_NAME = 1    // invalid user name
        const val TZ_INVALID_PWD = 2          // invalid password
        const val TZ_VERIFICATION_ERROR = 3     // verification error
        // areas
        const val TZ_DUPLICATED_AREA = 10      // duplicated area name
        const val TZ_INVALID_ARER = 11         // invalid area

        // distribution center
        const val TZ_DUPLICATED_DISTCENTER = 30   // distribution center name is duplicated
        const val TZ_INVALID_DISTCENTER = 31      // invalid distribution center

        const val TZ_INVALID_DRIVER = 32
        const val TZ_NO_DELIVER_TASK = 33
        const val TZ_PREV_DELIVER_TASK = 34

        const val TZ_INVALID_STORE = 35             // invalid store
        const val TZ_STORE_ALREADY_DELIVERED = 36   // this store is already delivered
        const val TZ_STORE_NOT_IN_TRIP = 37         // store isn't contained in trip course
        const val TZ_STORE_ALREADY_REACHED = 38     // already reached to store

        const val TZ_GEO_NO_FOUND =  50            // no geo data found


        // file operation
        const val TZ_IMG_FORMAT_ERROR = 100        // image format error
        const val TZ_IMG_NOT_FOUNT = 101           // image not found error,
        const val TZ_IMG_UPLOAD_FAILED = 102

        const val TZ_DB_INTERNAL_ERROR = 1000
        const val TZ_INVALID_API  = 1001
        const val TZ_SERVER_INTERNAL_ERROR = 1002  // server internal error

    }
}