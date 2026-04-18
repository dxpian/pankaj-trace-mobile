package myandroid.app.hhobzic.inventorymobile.home.cart

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.ChecklistFragment
import myandroid.app.hhobzic.inventorymobile.ChecklistFragment.Companion.CHANNEL_ID
import myandroid.app.hhobzic.inventorymobile.LocationFragment
import myandroid.app.hhobzic.inventorymobile.ProfileFragment
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.databinding.ActivityHome1Binding
import myandroid.app.hhobzic.inventorymobile.home.AppMacro

class DeliverCartActivity : AppCompatActivity() {
        private val deliverCartFragment = DeliverHomeFragment()
        private val locationFragment = LocationFragment()
        private val checklistFragment = ChecklistFragment()
        private val profileFragment = ProfileFragment()
        private lateinit var binding: ActivityHome1Binding
        private lateinit var viewPager: ViewPager2
        private lateinit var additionalIcon: ImageView
        private var selectedRsID:String = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_home1)

            viewPager = binding.viewPager
            additionalIcon = binding.additional

            selectedRsID = intent.getStringExtra(AppMacro.SELECTED_RS).toString()
            deliverCartFragment.setSelectedStoreID(selectedRsID)

            binding.imageView7.setOnClickListener(){
                // Show notification

            }
            binding.imageView2.setOnClickListener {
                toggleSideMenu()
            }
            val fragmentList = listOf(deliverCartFragment, locationFragment, checklistFragment, profileFragment)
            val adapter = FragmentPagerAdapter(this, fragmentList)
            viewPager.adapter = adapter

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
            // Set up navigation item click listener
            binding.navigationView.setNavigationItemSelectedListener { menuItem ->
                // Handle navigation item clicks here
                when (menuItem.itemId) {
                    R.id.logout->{
                        Toast.makeText(this@DeliverCartActivity, "Logout", Toast.LENGTH_SHORT).show()
                    }
                }
                // Close the drawer after handling the click
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            // Request notification permission if needed
            if (ActivityCompat.checkSelfPermission(
                    this@DeliverCartActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@DeliverCartActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    ChecklistFragment.NOTIFICATION_ID
                )
            }
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
            val intentMainLanding = Intent(context, this@DeliverCartActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intentMainLanding, PendingIntent.FLAG_IMMUTABLE)

            // Create a notification builder
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(
                context,
                CHANNEL_ID
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
                    CHANNEL_ID,
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
}

