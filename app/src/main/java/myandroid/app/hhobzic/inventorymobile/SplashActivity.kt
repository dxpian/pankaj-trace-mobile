package myandroid.app.hhobzic.inventorymobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import myandroid.app.hhobzic.inventorymobile.api.model.AuthManager
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.user.UserViewModel
import myandroid.app.hhobzic.inventorymobile.databinding.ActivityMainBinding
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.HomeActivity
import myandroid.app.hhobzic.inventorymobile.models.AppStatusRepository
import java.util.Locale
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import myandroid.app.hhobzic.inventorymobile.api.company.CompanyViewModel
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.User
import myandroid.app.hhobzic.inventorymobile.home.Language
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger
import myandroid.app.hhobzic.inventorymobile.utility.saveLanguagePreference


class SplashActivity : AppCompatActivity() {
    private var TAG = "SplashActivity"
    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels()
    private val companyViewModel: CompanyViewModel by viewModels()
    private var _companies: List<Company> = listOf()
    private lateinit var viewModel: AppStatusSharedViewModel

    private val gson = Gson()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDefaultLocale(Language.JPN)
        val sharedPreferences = getSharedPreferences(AppMacro.SHAREDPREF_NAME, MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(AppStatusSharedViewModel::class.java)
        // load language setting
        var language = sharedPreferences.getString(AppMacro.KEY_LANGUAGE, null)
        if(language.isNullOrEmpty()){ // no previous language setting. set default language
            language = Language.JPN
            saveLanguagePreference(this, language)
        }
        setDefaultLocale(language)
        // reload resource if the current language isn't same
        AppLogger.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.navigationBarColor = Color.BLACK

        binding.loginBtn.isEnabled = false
        binding.register.isEnabled = false

        binding.loginBtn.setOnClickListener(){
            startActivity(Intent(this@SplashActivity,LoginActivity::class.java))
            // finish()
        }
        binding.register.setOnClickListener(){
            startActivity(Intent(this@SplashActivity,RegisterActivity::class.java))
            // finish()
        }
        enableLogin(false)

        companyViewModel.company.observe(this, Observer { data->
            _companies = data
            Log.d(TAG, "Success to update company information in splash")
            val companiesJson = gson.toJson(_companies)
            val editor = sharedPreferences.edit()
            editor.putString(AppMacro.KEY_COMPANYINFO, companiesJson).apply()
            editor.apply()
            audoRedirectToHompage()
        })

        // observe company names
        companyViewModel.error.observe(this, Observer{error->
            if (error != null) {
                binding.loginBtn.isEnabled = true
                binding.register.isEnabled = true
                showToast(error)
            }
        })
        companyViewModel.fetchCompany()

        enableLogin(true)
    }

    private fun audoRedirectToHompage(){
        val sharedPreferences = getSharedPreferences(AppMacro.SHAREDPREF_NAME, MODE_PRIVATE)
        try {
            val token = sharedPreferences.getString(AppMacro.KEY_TOKEN, null)
            if(token == null){
                Log.w(TAG, "There is no previous token")
                binding.loginBtn.isEnabled = true
                binding.register.isEnabled = true
                return
            }
            val gson = Gson()
            val userInfoJsonLoaded = sharedPreferences.getString(AppMacro.KEY_USERINFO, null)
            var userInfo = userInfoJsonLoaded?.let {
                try {
                    gson.fromJson(it, Driver::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing userInfo JSON", e)
                }
            }

            val companiesJsonLoaded = sharedPreferences.getString(AppMacro.KEY_COMPANYINFO, null)
            val companiesType = object : TypeToken<List<Company>>() {}.type
            val companyInfo: List<Company> = companiesJsonLoaded?.let {
                try {
                    gson.fromJson(it, companiesType)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing companyInfo JSON", e)
                    listOf()
                }
            } ?: listOf()

            if(userInfo==null){
                Log.d(TAG, "Failed to load user information")
                binding.loginBtn.isEnabled = true
                binding.register.isEnabled = true
                return
            }
            userInfo = userInfo as Driver?

            val versionName = BuildConfig.VERSION_NAME
            val versionCode = BuildConfig.VERSION_CODE

            //Log.d(TAG, "Success to find previous loged information")
            // login with this information
            if(AppMacro.REQUIRE_LOGIN){
                val driverName = userInfo?.name
                val company = userInfo?.company
                val password = userInfo?.password
                Log.d(TAG, "Loaded user information: $userInfo")
                if(driverName.isNullOrEmpty() || company.isNullOrEmpty() || password.isNullOrEmpty()){
                    Log.d(TAG, "Failed to load company")
                    binding.loginBtn.isEnabled = true
                    binding.register.isEnabled = true
                    return
                }

                userViewModel.login(company, driverName, password, versionName, versionCode,
                    onSuccess = { user->
                        showToast(ApiNotification.LOGIN_SUCCESS)
                        viewModel.updateUser(user, companyInfo)
                        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }, onError = {message->// clear user information
                        binding.loginBtn.isEnabled = true
                        binding.register.isEnabled = true
                        showToast(message)
                        cleanUserInformation()
                    }
                )
            }
        }catch (e: Exception) {
            binding.loginBtn.isEnabled = true
            binding.register.isEnabled = true
            Log.e(TAG, "Error loading preferences", e)
        }
    }

    private fun setDefaultLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun resetLanauge(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        // Update the app configuration with the new locale
        val config = resources.configuration
        config.setLocale(locale)

        // Update configuration in context and resources
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart activity to apply changes
        val intent = Intent(this, this::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun enableLogin(enable: Boolean){
        binding.loginBtn.isEnabled = enable
        binding.register.isEnabled = enable
    }

    private fun cleanUserInformation(){
        // remove token
        AuthManager.cleanSession()
        // remove token from sharedPreference
        val sharedPreferences =getSharedPreferences(AppMacro.SHAREDPREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (sharedPreferences.contains(AppMacro.KEY_TOKEN)) {
            editor.remove(AppMacro.KEY_TOKEN)
        }

        if (sharedPreferences.contains(AppMacro.KEY_USERINFO)) {
            editor.remove(AppMacro.KEY_USERINFO)
        }

        if (sharedPreferences.contains(AppMacro.KEY_COMPANYINFO)) {
            editor.remove(AppMacro.KEY_COMPANYINFO)
        }
        editor.apply() // or editor.commit() if you want to do it synchronously
    }

    private fun showToast(message: String, durationInMillis: Long = 5000) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, durationInMillis)
    }

}
