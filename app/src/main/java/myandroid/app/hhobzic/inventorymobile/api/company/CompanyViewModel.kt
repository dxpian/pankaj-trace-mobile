package myandroid.app.hhobzic.inventorymobile.api.company

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import kotlinx.coroutines.*


class CompanyViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG =  "CompanyViewModel"
    private var retryJob: Job? = null


    private val compoanyRepository: CompanyRepository = CompanyRepository(application.applicationContext)
    private val _company =  MutableLiveData<List<Company>>()
    val company: LiveData<List<Company>> get() = _company
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    fun fetchCompany(){
        viewModelScope.launch(Dispatchers.IO) {
            retryJob?.cancel()
            retryJob = launch {
                while (isActive) {
                    when (val result = compoanyRepository.getCompanies()) {
                        is ApiResult.Success -> {
                            _company.postValue(result.data)
                            _error.postValue(null) // No error
                            //Log.d(TAG, "Success to run get company request")
                            break
                        }
                        is ApiResult.Error -> {
                            _company.postValue(emptyList())
                            _error.postValue(result.message)
                            Log.e(TAG, "Failed to run get company api: ${result.message}")
                        }
                        is ApiResult.Code ->{
                            _company.postValue(emptyList())
                            _error.postValue(null)
                        }
                    }
                    delay(5000) // Retry after 5 seconds
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        retryJob?.cancel()
    }

}