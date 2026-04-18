package myandroid.app.hhobzic.inventorymobile.api.model

object AuthManager {
    var accessToken: String? = null
    var expired: Boolean = false
    fun cleanSession(){
        accessToken = null
        expired = true
    }
    fun isExpired():Boolean{
        return expired
    }
}