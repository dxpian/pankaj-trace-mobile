package myandroid.app.hhobzic.inventorymobile.api.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserViewModel (application: Application) : AndroidViewModel(application) {
    private val TAG =  "CompanyViewModel"
    private val _isuploadEnabled = MutableLiveData(true)
    val isUploadEnabled: LiveData<Boolean> get() = _isuploadEnabled
    private val userRepo: UserRepository = UserRepository(application.applicationContext)
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun uploadDriverData(
        namePart: RequestBody,
        companyPart: RequestBody,
        phonePart: RequestBody,
        licensePart: RequestBody,
        vinNumberPart: RequestBody,
        passwordPart: RequestBody,
        emailPart: RequestBody,
        filePart: MultipartBody.Part,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _isuploadEnabled.postValue(false)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userRepo.uploadDriverData(
                namePart, companyPart, phonePart, licensePart, vinNumberPart, passwordPart, emailPart, filePart))
            {
                is ApiResult.Code -> {
                    withContext(Dispatchers.Main) { onSuccess() }
                    _isuploadEnabled.postValue(true)
                }
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main) { onError(result.message) }
                    _isuploadEnabled.postValue(true)
                    Log.e(TAG, result.message)
                }
                else -> {
                    withContext(Dispatchers.Main) { onError("Unexpected response") }
                    _isuploadEnabled.postValue(true)
                }

            }
        }
    }

    fun login(company: String, name: String, password: String, versionName: String, versionCode: Int,
              onSuccess:(Driver)->Unit,
              onError: (String)->Unit){
        _isuploadEnabled.postValue(false)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = userRepo.userLogin(company, name, password, versionName, versionCode)) {
                is ApiResult.Success -> {
                    withContext(Dispatchers.Main) { onSuccess(result.data) }
                    _isuploadEnabled.postValue(true)
                }
                is ApiResult.Error -> {
                    withContext(Dispatchers.Main) { onError(result.message) }
                    _isuploadEnabled.postValue(true)
                    Log.e(TAG, "Failed to login: ${result.message}")
                }
                else -> {
                    withContext(Dispatchers.Main) { onError("Unexpected response") }
                    _isuploadEnabled.postValue(true)
                }
            }
        }
    }
}
