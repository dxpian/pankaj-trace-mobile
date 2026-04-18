package myandroid.app.hhobzic.inventorymobile.gps

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String?,
        @Query("key") apiKey: String,
        @Query("mode") mode: String = "driving",
        @Query("avoid") avoid: String = "ferries",
        @Query("traffic_model") trafficModel: String = "pessimistic",
        @Query("departure_time") departureTime: String = "" // Add departure_time parameter
    ): DirectionsResponse
}

// Retrofit instance
object RetrofitInstance {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val directionsApi: DirectionsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
    }
}
