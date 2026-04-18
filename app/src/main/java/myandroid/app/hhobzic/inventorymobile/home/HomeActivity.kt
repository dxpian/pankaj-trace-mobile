package myandroid.app.hhobzic.inventorymobile.home

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.AttributeSet
import android.provider.Settings
import android.util.Log

import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import myandroid.app.hhobzic.inventorymobile.ChecklistFragment
import myandroid.app.hhobzic.inventorymobile.HomeFragment
import myandroid.app.hhobzic.inventorymobile.LocationFragment
import myandroid.app.hhobzic.inventorymobile.ProfileFragment
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.SplashActivity
import myandroid.app.hhobzic.inventorymobile.api.model.AuthManager
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.databinding.ActivityHomeBinding
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverHomeFragment
import myandroid.app.hhobzic.inventorymobile.home.cart.VIewChecklistFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.AppSettingFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.EditProfileFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.EditTruckFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.NotificationFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.TrackSettingFragment
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.service.AppForegroundService
import java.util.LinkedList


class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"
    private val homeFragment = HomeFragment()
    private val locationFragment = LocationFragment()
    private val checklistFragment = ChecklistFragment()
    private val profileFragment = ProfileFragment()
    private val gson = Gson()
    private lateinit var viewModel: AppStatusSharedViewModel

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var additionalIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acquire Wake Lock
        requestIgnoreBatteryOptimizations()
        // startFgdService()


        window.navigationBarColor = Color.BLACK
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProvider(this).get(AppStatusSharedViewModel::class.java)
        // reloadUserInformation()
        viewPager = binding.viewPager
        additionalIcon = binding.additional


        binding.imageMore.setOnClickListener {
            toggleSideMenu()
        }

        val adapter = ViewPagerAdapter(this, viewPager)
        viewPager.adapter = adapter
        binding.imageNotify.setOnClickListener(){
            adapter.openNotifyPage()
        }

        binding.bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            val position = when (menuItem.itemId) {
                R.id.menu_home -> 0
                R.id.menu_location -> 1
                R.id.menu_checklist -> 2
                R.id.menu_profile -> 3
                else -> 0
            }
            viewPager.setCurrentItem(position, true)
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavView.menu.getItem(position).isChecked = true

                val newMarginStart = calculateAdditionalIconMargin(binding.bottomNavView.menu.getItem(position).itemId)
                updateAdditionalIconMargin(newMarginStart)
            }
        })

        viewPager.setCurrentItem(0, false)

        ViewCompat.setOnApplyWindowInsetsListener(viewPager) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBarsInsets.bottom)
            insets
        }

        // Set up navigation item click listener
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                /*
                R.id.menu_scan -> {
                    Toast.makeText(this@HomeActivity, "Scan", Toast.LENGTH_SHORT).show()

                }*/
                R.id.logout -> {
                    // Show a toast message
                    Toast.makeText(this@HomeActivity, "Logout", Toast.LENGTH_SHORT).show()
                    cleanUserInformation()
                    // Start LogoutActivity
                    val intent = Intent(this@HomeActivity, SplashActivity::class.java)
                    startActivity(intent)

                    // Optional: finish the current activity if you don't want the user to return to it
                    finish()
                }
                /*
                R.id.menu_empty_load -> {
                    Toast.makeText(this@HomeActivity, "Emplty/Load", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_loading_cart -> {
                    Toast.makeText(this@HomeActivity, "Cart", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_start_trip -> {
                    Toast.makeText(this@HomeActivity, "Go", Toast.LENGTH_SHORT).show()
                }*/
            }
            // Close the drawer after handling the click
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        // Request notification permission if needed
        if (ActivityCompat.checkSelfPermission(
                this@HomeActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@HomeActivity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                ChecklistFragment.NOTIFICATION_ID
            )
        }
        viewModel.locNotifyUpdated.observe(this, Observer { queue->
            synchronized(queue) {
                val events = LinkedList(queue) // Copy current events for processing
                queue.clear() // Clear the original queue to avoid re-processing

                while (events.isNotEmpty()) {
                    val message = events.poll() // Process each event
                    showToast(message)
                }
            }
        })

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }

    private fun requestIgnoreBatteryOptimizations() {

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::AppWakeLock")
        wakeLock.acquire()  // Acquire wake lock when needed

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName

        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }


    private fun startFgdService(){
        val serviceIntent = Intent(this, AppForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    // save user information
    private fun saveUserInformation(){
        //Log.d(TAG, "Success to save the user information")
    }

    @Suppress("DEPRECATION")
    private fun enableFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun reloadUserInformation(){

        val sharedPreferences = getSharedPreferences(AppMacro.SHAREDPREF_NAME, MODE_PRIVATE)
        // read accesstoken
        val token = sharedPreferences.getString(AppMacro.KEY_TOKEN, null)
        if(!token.isNullOrEmpty()){
            AuthManager.accessToken = token
            AuthManager.expired = false
        }

        val userInfoJsonLoaded = sharedPreferences.getString(AppMacro.KEY_USERINFO, null)
        val userInfo = userInfoJsonLoaded?.let {
            gson.fromJson(it, Driver::class.java)
        }

       val companiesJsonLoaded = sharedPreferences.getString(AppMacro.KEY_COMPANYINFO, null)
        val companiesType = object : TypeToken<List<Company>>() {}.type
        val companyInfo: List<Company> = companiesJsonLoaded?.let {
            gson.fromJson(it, companiesType)
        } ?: listOf()

        viewModel.updateUser(userInfo!!, companyInfo)
        //Log.d(TAG, "User information loaded: $userInfo")
        //Log.d(TAG, "Company information loaded: $companyInfo")
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

    private fun toggleSideMenu() {
        val drawerLayout: DrawerLayout? = binding.drawerLayout
        drawerLayout?.let {
            if (it.isDrawerOpen(GravityCompat.START)) {
                it.closeDrawer(GravityCompat.START)
            } else {
                it.openDrawer(GravityCompat.START)
            }
        }
    }
    private fun showNotification(context: Context, medicine: String) {
        // Create an Intent to launch the main landing activity
        val intentMainLanding = Intent(context, this@HomeActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intentMainLanding, PendingIntent.FLAG_IMMUTABLE)

        // Create a notification builder
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            ChecklistFragment.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.carticon)
            .setContentTitle("Reminder")
            .setContentText(medicine)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the PendingIntent
            .setAutoCancel(true) // Automatically remove the notification when tapped

        // Create a notification manager
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (always create regardless of API level)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ChecklistFragment.CHANNEL_ID,
                "Reminder Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        notificationManager.notify(1, builder.build())
    }

    private fun calculateAdditionalIconMargin(selectedItemId: Int): Int {
        return when (selectedItemId) {
            R.id.menu_home -> resources.getDimensionPixelSize(R.dimen.initial_additional_icon_margin_start)
            R.id.menu_location -> resources.getDimensionPixelSize(R.dimen.additional_icon_margin_location)
            R.id.menu_checklist -> resources.getDimensionPixelSize(R.dimen.additional_icon_margin_checklist)
            R.id.menu_profile -> resources.getDimensionPixelSize(R.dimen.additional_icon_margin_profile)
            else -> resources.getDimensionPixelSize(R.dimen.initial_additional_icon_margin_start)
        }
    }

    private fun updateAdditionalIconMargin(newMarginStart: Int) {
        val layoutParams = additionalIcon.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = newMarginStart
        additionalIcon.layoutParams = layoutParams
    }

    inner class FragmentPagerAdapter(activity: AppCompatActivity, private val fragments: List<Fragment>) :
        FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save your state here, e.g.:
        outState.putString("someStateKey", "Some value to save")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //Log.d(TAG, "onRestoreInstanceState function is called")
        // Use the restored state
    }
    override fun onBackPressed() {

        val adapter = viewPager.adapter as ViewPagerAdapter
        val currentItemPosition = viewPager.currentItem
        val frag = adapter.getFragment(currentItemPosition)

        // Do something with the current fragment
        when (frag) {
            is VIewChecklistFragment-> frag.onBackPressed()
            is DeliverHomeFragment -> frag.onBackPressed()
            is EditProfileFragment->frag.onBackPressed()
            is EditTruckFragment -> frag.onBackPressed()
            is NotificationFragment->frag.onBackPressed()
            is TrackSettingFragment->frag.onBackPressed()
            is AppSettingFragment->frag.onBackPressed()
            else->{
                saveUserInformation()
                super.onBackPressed()
                finish() // This will finish the activity and save the state
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveUserInformation()
    }

    private fun showToast( message: String, durationInMillis: Long = 5000) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, durationInMillis)
    }


}
