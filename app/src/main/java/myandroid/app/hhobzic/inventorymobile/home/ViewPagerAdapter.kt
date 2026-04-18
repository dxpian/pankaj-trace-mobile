package myandroid.app.hhobzic.inventorymobile.home

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.ChecklistFragment
import myandroid.app.hhobzic.inventorymobile.HomeFragment
import myandroid.app.hhobzic.inventorymobile.LocationFragment
import myandroid.app.hhobzic.inventorymobile.ProfileFragment
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverHomeFragment
import myandroid.app.hhobzic.inventorymobile.home.cart.VIewChecklistFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.AppSettingFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.EditProfileFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.EditTruckFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.NotificationFragment
import myandroid.app.hhobzic.inventorymobile.home.profile.TrackSettingFragment

class PageType{
    companion object Constant{
        const val HOME = 0
        const val LOCATION = 1
        const val CHECKLIST = 2
        const val PROFILE = 3
    }
}

class ViewPagerAdapter(activity: AppCompatActivity, private val viewPager: ViewPager2) : FragmentStateAdapter(activity) {
    val TAG = "ViewPagerAdapter"

    var fragHome =  HomeFragment()
    var fragLocation = LocationFragment()
    var fragOverview = ChecklistFragment()
    var fragProfile = ProfileFragment()
    var fragEditUser = EditProfileFragment()
    var fragEditTruck = EditTruckFragment()
    var fragNotify = NotificationFragment()
    var fragTrackSetting = TrackSettingFragment()
    var fragCheckList = VIewChecklistFragment()
    var fragDeliver = DeliverHomeFragment()
    var fragAppSetting = AppSettingFragment()
    private val fragments = mutableListOf(fragHome,fragLocation, fragOverview, fragProfile)
    private val fragmentIds = mutableListOf<Long>(0,0,0,0)

    init {
        fragHome.setViewPager(viewPager)
        fragLocation.setViewPager(viewPager)
        fragOverview.setViewPager(viewPager)
        fragProfile.setViewPager(viewPager)
        fragEditUser.setViewPager(viewPager)
        fragEditTruck.setViewPager(viewPager)
        fragNotify.setViewPager(viewPager)
        fragCheckList.setViewPager(viewPager)
        fragDeliver.setViewPager(viewPager)
        fragTrackSetting.setViewPager(viewPager)
        fragAppSetting.setViewPager(viewPager)
        updateId()
    }


    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long {
        return fragmentIds[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragmentIds.contains(itemId)
    }

    fun openViewcheckList(){
        replaceFragment(PageType.HOME, fragCheckList)
        viewPager.isUserInputEnabled = true
    }

    fun openDeliverList(){
        replaceFragment(PageType.HOME, fragDeliver)
        viewPager.isUserInputEnabled = true
    }

    fun openHomepage(){
        viewPager.isUserInputEnabled = false
        replaceFragment(PageType.HOME, fragHome)

    }

    fun openEditUserProfile(){
        replaceFragment(PageType.PROFILE, fragEditUser)
        viewPager.isUserInputEnabled = true
    }

    fun openEditTruck(){
        replaceFragment(PageType.PROFILE, fragEditTruck)
        viewPager.isUserInputEnabled = true
    }
    fun openNotifyPage(){
        replaceFragment(PageType.PROFILE, fragNotify)
        viewPager.isUserInputEnabled = true
    }

    fun openTrackSettingPage(){
        replaceFragment(PageType.PROFILE, fragTrackSetting)
        viewPager.isUserInputEnabled = true
    }

    fun openAppSettingPage(){
        replaceFragment(PageType.PROFILE, fragAppSetting)
        viewPager.isUserInputEnabled = true
    }


    fun openProfileHome(){
        replaceFragment(PageType.PROFILE, fragProfile)
        viewPager.isUserInputEnabled = true
    }



    fun openLocationPage(){
        viewPager.setCurrentItem(PageType.LOCATION, false)
        notifyItemChanged(PageType.LOCATION) // Trigger update to the ViewPager2
        viewPager.isUserInputEnabled = true
    }

    fun getFragment(position: Int): Fragment? {
        return if (position in fragments.indices) {
            fragments[position]
        } else {
            null
        }
    }

    private fun replaceFragment(index: Int, fragment: Fragment) {
        if (index in fragments.indices) {
            fragments[index] = fragment
            updateId()
            viewPager.setCurrentItem(index, false)
            notifyItemChanged(index) // Trigger update to the ViewPager2

        } else {
            Log.e(TAG, "Invalid page index: $index")
        }
    }



    private fun updateId(){
        fragmentIds[PageType.HOME] = fragments[PageType.HOME].hashCode().toLong()
        fragmentIds[PageType.LOCATION] = fragments[PageType.LOCATION].hashCode().toLong()
        fragmentIds[PageType.CHECKLIST] = fragments[PageType.CHECKLIST].hashCode().toLong()
        fragmentIds[PageType.PROFILE] = fragments[PageType.PROFILE].hashCode().toLong()
    }
}
