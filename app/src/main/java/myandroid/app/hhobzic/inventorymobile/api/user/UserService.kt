package myandroid.app.hhobzic.inventorymobile.api.user

import myandroid.app.hhobzic.inventorymobile.api.model.ApiResponse
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.model.LoginRequest
import retrofit2.http.Multipart
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Part



interface UserService {

    @Multipart
    @POST("mobile/driver/signup")
    suspend fun uploadDriverData(
        @Part("name") name: RequestBody,
        @Part("company") company: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("license") license: RequestBody,
        @Part("vin") vin: RequestBody,
        @Part("password") password: RequestBody,
        @Part("email") email: RequestBody,
        @Part file: MultipartBody.Part
    ): ApiResponse<Driver>

    @POST("mobile/driver/login")
    fun login(@Body loginInfo: LoginRequest): Call<ApiResponse<Driver>>
}
