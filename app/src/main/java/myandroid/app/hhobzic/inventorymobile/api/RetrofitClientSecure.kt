package myandroid.app.hhobzic.inventorymobile.api
import android.content.Context
import myandroid.app.hhobzic.inventorymobile.api.model.AuthInterceptor
import myandroid.app.hhobzic.inventorymobile.home.AppMacro

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class RetrofitClientSecure(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getInstance(context: Context): Retrofit {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildRetrofit(context).also { INSTANCE = it }
            }
        }

        private fun buildRetrofit(context: Context): Retrofit {
            // Initialize AuthManager if needed

            // Create a TrustManager that trusts all certificates
            val trustAllCertificates = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            })

            // Create an OkHttpClient with the custom TrustManager and AuthInterceptor
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(createTrustAllSSLFactory(trustAllCertificates), trustAllCertificates[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // Allow all hostnames, not recommended for production
                .addInterceptor(AuthInterceptor(context)) // Add AuthInterceptor here
                .connectTimeout(6000, TimeUnit.MILLISECONDS)  // Set connection timeout
                .readTimeout(6, TimeUnit.SECONDS)     // Set read timeout for large files
                .writeTimeout(5, TimeUnit.SECONDS)    // Set write timeout
                .build()

            return Retrofit.Builder()
                .baseUrl(AppMacro.SERVER_URL_CLOUD)
                .client(okHttpClient) // Set the client with the interceptor
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private fun createTrustAllSSLFactory(trustAllCertificates: Array<TrustManager>): SSLSocketFactory {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCertificates, null)
            return sslContext.socketFactory
        }
    }
}
