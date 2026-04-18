package myandroid.app.hhobzic.inventorymobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.company.CompanyViewModel
import myandroid.app.hhobzic.inventorymobile.api.user.UserViewModel
import myandroid.app.hhobzic.inventorymobile.databinding.ActivityLoginBinding
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.HomeActivity
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding // Declare binding variable
    private val companyViewModel: CompanyViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val TAG =  "RegisterActivity"
    private lateinit var viewModel: AppStatusSharedViewModel
    private var _companies: List<Company> = listOf()
    private val gson = Gson()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(AppStatusSharedViewModel::class.java)
        window.navigationBarColor = Color.BLACK

        binding.submit.setOnClickListener(){
            if(AppMacro.REQUIRE_LOGIN){
                var driverName = binding.driverName.text.toString()
                var company = binding.companyName.text.toString()
                var password = binding.password.text.toString()
                if(driverName.isEmpty() || company.isEmpty() || password.isEmpty()){
                    showToast(ApiNotification.REQUIRE_FILL_FIELD)
                    return@setOnClickListener
                }
                val versionName = BuildConfig.VERSION_NAME
                val versionCode = BuildConfig.VERSION_CODE

                Log.d(TAG, "Login activity. loaded driver name and password: $driverName, $password")
                if(company.isEmpty() || driverName.isEmpty() || password.isEmpty()){
                    Log.w(TAG, "Failed to relaod user credentials")
                    return@setOnClickListener
                }
                userViewModel.login(company, driverName, password, versionName, versionCode,
                    onSuccess = { userInfo->
                        showToast("Login successfully")
                        //save user login information
                        val userInfoJson = gson.toJson(userInfo)
                        val companiesJson = gson.toJson(_companies)

                        // val companiesJson = gson.toJson(_companies)
                        val sharedPreferences = getSharedPreferences(AppMacro.SHAREDPREF_NAME, MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString(AppMacro.KEY_USERINFO, userInfoJson).apply()
                        editor.putString(AppMacro.KEY_COMPANYINFO, companiesJson).apply()
                        editor.apply()

                        // update user information
                        viewModel.updateUser(userInfo, _companies)

                        // appStatusSaharedViewModel.updateUser(userInfo, area, _companies)
                        Log.d(TAG, "Success to login, set user information: $userInfo")
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }, onError = {errorMessage->
                        showToast(errorMessage)
                    }
                )
            }
            else{ // navigate into main page without login
                startActivity(Intent(this@LoginActivity,HomeActivity::class.java))
                finish()
            }
        }

        userViewModel.isUploadEnabled.observe(this, Observer{enabled->
            binding.submit.isEnabled = enabled
        })


        companyViewModel.company.observe(this, Observer { data->
            _companies = data

            val companyNames = _companies.map { it.name }
            Log.d(TAG, "Success to load all company list")
            _companies.forEach{it->
                Log.d(TAG, "$it")
            }
            val adapter = ArrayAdapter(this, R.layout.dropdown_autocomplete, companyNames)
            binding.companyName.setAdapter(adapter)
            ////Log.d(TAG, "All companies: ${companyNames.size}")
        })


        // observe company names
       companyViewModel.error.observe(this, Observer{error->
            if (error != null) {
                ////Log.d(this.TAG, "company error occurd: $error")
                showToast(error)
                binding.submit.isEnabled = false
            } else {
                binding.submit.isEnabled = true
            }
        })
        companyViewModel.fetchCompany()

    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showToast(message: String, durationInMillis: Long = 5000) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, durationInMillis)
    }
}
