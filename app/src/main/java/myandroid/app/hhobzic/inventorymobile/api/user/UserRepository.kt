package myandroid.app.hhobzic.inventorymobile.api.user

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import myandroid.app.hhobzic.inventorymobile.BuildConfig
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResponse
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.AuthManager
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.RetrofitCleintLocal
import myandroid.app.hhobzic.inventorymobile.api.RetrofitClientSecure
import myandroid.app.hhobzic.inventorymobile.api.model.LoginRequest
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository(private val context: Context) {
    private val TAG = "UserRepository"
    private val apiService: UserService
    init {
        apiService = if (isLocalEnvironment()) {
            RetrofitCleintLocal.getInstance(context).create(UserService::class.java)
        } else {
            RetrofitClientSecure.getInstance(context).create(UserService::class.java)
        }
    }

    private fun isLocalEnvironment(): Boolean {
        return BuildConfig.SERVER == "local"
    }

    suspend fun uploadDriverData(
        namePart: RequestBody,
        companyPart: RequestBody,
        phonePart: RequestBody,
        licensePart: RequestBody,
        vinNumberPart: RequestBody,
        passwordPart: RequestBody,
        emailPart: RequestBody,
        filePart: MultipartBody.Part
    ): ApiResult<Nothing> {
        return try {

            val response = apiService.uploadDriverData(namePart, companyPart, phonePart, licensePart, vinNumberPart, passwordPart, emailPart, filePart)
            if (response.error_code == 0) {
                ApiResult.Code(0)
            } else {
                ApiResult.Error(response.meta)
            }
        } catch (e: Exception) {
            ApiResult.Error("${ApiNotification.CONNECTION_ERROR} ${e.message}")
        }
    }

    suspend fun userLogin(
        company: String,
        name: String,
        password: String,
        versionName: String,
        versionCode: Int): ApiResult<Driver> {
        return try
        {
            Log.d(TAG, "Trying to login. company id = $company, $name, $password, $versionName, $versionCode")
            val response = apiService.login(LoginRequest(company, name, password, versionName, versionCode)).execute()
            if(response.isSuccessful){
                val apiResponse: ApiResponse<Driver> = response.body()!!
                if(apiResponse.error_code == 0){
                    AuthManager.accessToken = apiResponse.accesstoken
                    AuthManager.expired = false
                    Log.d(TAG, "Success to get accesstoken: ${AuthManager.accessToken} ")
                    // write token into sharedPreference space

                    val sharedPreferences = context.getSharedPreferences(AppMacro.SHAREDPREF_NAME, MODE_PRIVATE)
                    sharedPreferences.edit().putString(AppMacro.KEY_TOKEN, apiResponse.accesstoken).apply()
                    sharedPreferences.edit().apply()


                    val driverInfo = apiResponse.data
                    driverInfo.token = apiResponse.accesstoken
                    driverInfo.password = password
                    ApiResult.Success(driverInfo)
                }
                else{
                    ApiResult.Error(apiResponse.meta)
                }
            }
            else{
                ApiResult.Error(ApiNotification.INVALID_RESPONSE)
            }
        }
        catch(e: Exception){
            ApiResult.Error("${ApiNotification.CONNECTION_ERROR}: $e")
        }
    }

}