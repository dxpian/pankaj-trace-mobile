package myandroid.app.hhobzic.inventorymobile.api.model

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import myandroid.app.hhobzic.inventorymobile.SplashActivity
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.Language
import myandroid.app.hhobzic.inventorymobile.home.LanguageCode
import myandroid.app.hhobzic.inventorymobile.home.TokenKey
import myandroid.app.hhobzic.inventorymobile.utility.loadLanguagePreference
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    private var TAG = "AuthInterceptor"
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = AuthManager.accessToken
        // load language token
        val lan = loadLanguagePreference(context)
        val lanCode = if(lan == Language.ENG) LanguageCode.ENG else LanguageCode.JPN
        // If accessToken is not null, add it to the headers
        val requestBuilder = originalRequest.newBuilder()
            .apply {
                accessToken?.let {
                    header(TokenKey.accesstoken, "$it")
                }
                header(TokenKey.language, lanCode.toString())
        }
        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // Check if the token is expired
        if (response.code == 401) { // Or any other specific status code or response body check
            //Log.d(TAG, "Token expired in authinterceptor")
            showTokenExpiredMessage()

            // Redirect to login page
            if(!AuthManager.expired) {
                redirectToLogin()

            }
        }
        return response
    }

    private fun showTokenExpiredMessage() {
        // You can use a Toast, Snackbar, Dialog, or any other method to show a message
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun redirectToLogin() {
        //Log.d(TAG, "redirecto to login page. token is expired")
        AuthManager.cleanSession()
        // remove token from sharedPreference
        val sharedPreferences = context.getSharedPreferences(AppMacro.SHAREDPREF_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        if (sharedPreferences.contains(AppMacro.KEY_TOKEN)) {
            editor.remove(AppMacro.KEY_TOKEN)
            editor.apply() // or editor.commit() if you want to do it synchronously
        }

        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}