package myandroid.app.hhobzic.inventorymobile.api.company


import android.content.Context
import android.util.Log
import com.squareup.moshi.Json

import myandroid.app.hhobzic.inventorymobile.api.RetrofitCleintLocal
import myandroid.app.hhobzic.inventorymobile.api.RetrofitClientSecure
import myandroid.app.hhobzic.inventorymobile.BuildConfig
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResponse
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.Company

class CompanyRepository(private val context: Context) {
    private val apiService: CompanyService
    private val TAG = "CompanyRepository"
    init {
        apiService = if (isLocalEnvironment()) {
            Log.d(TAG, "working in location environment")
            RetrofitCleintLocal.getInstance(context).create(CompanyService::class.java)
        } else {
            Log.d(TAG, "working in cloud environment")
            RetrofitClientSecure.getInstance(context).create(CompanyService::class.java)
        }
    }
    private fun isLocalEnvironment(): Boolean {
        Log.d(TAG, "Current working environment ${BuildConfig.SERVER}")
        return BuildConfig.SERVER == "local"
    }

    suspend fun getCompanies(): ApiResult<List<Company>> {
        Log.d(TAG, "Calling api to get company list")
        return try {
            val response = apiService.getAllCompany().execute()
            if (response.isSuccessful) {
                val apiResponse: ApiResponse<List<Company>> = response.body()!!
                Log.d(TAG, "getcompany response:  $apiResponse" )
                if (apiResponse.error_code == 0) {
                    ApiResult.Success(apiResponse.data)
                } else {
                    ApiResult.Error(apiResponse.meta)
                }
            } else {
                Log.e(TAG, "Failed to connect server:${response.errorBody()}")
                ApiResult.Error(ApiNotification.INVALID_RESPONSE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect server: $e")
            ApiResult.Error("${ApiNotification.CONNECTION_ERROR}: $e")
        }
    }
}