package myandroid.app.hhobzic.inventorymobile.api

import android.content.Context
import myandroid.app.hhobzic.inventorymobile.api.model.AuthInterceptor
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RetrofitCleintLocal {
    companion object {
        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getInstance(context: Context): Retrofit {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRetrofit(context).also { INSTANCE = it }
            }
        }
        private fun buildRetrofit(context: Context): Retrofit {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context)) // Pass context to interceptor
                .connectTimeout(30, TimeUnit.SECONDS) // Adjust connection timeout
                .readTimeout(30, TimeUnit.SECONDS)    // Adjust read timeout
                .writeTimeout(30, TimeUnit.SECONDS)   // Adjust write timeout
                .build()

            return Retrofit.Builder()
                .baseUrl(AppMacro.SERVER_URL_LOCAL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        }

    }
}