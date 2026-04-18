package myandroid.app.hhobzic.inventorymobile.api.company

import myandroid.app.hhobzic.inventorymobile.api.model.ApiResponse
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import retrofit2.Call
import retrofit2.http.POST

interface CompanyService {
    @POST("mobile/company/getAllCompany")
    fun getAllCompany(): Call<ApiResponse<List<Company>>>
}