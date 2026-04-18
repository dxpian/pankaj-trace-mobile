package myandroid.app.hhobzic.inventorymobile.home.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.api.model.Notification
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentNotificationBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import java.util.LinkedList


class NotificationFragment: Fragment(), NotifyEventCallback {

    private val TAG = "NotificationFragment"
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var notifyAdpator: NotifyEventAdaptor
    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        notifyAdpator = NotifyEventAdaptor(requireContext(), mutableListOf(), this)

        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        viewModel.notifyUpdated.observe(viewLifecycleOwner, Observer { queue->
            synchronized(queue) {
                val events = LinkedList(queue) // Copy current events for processing
                queue.clear() // Clear the original queue to avoid re-processing

                while (events.isNotEmpty()) {
                    val notifyEvent = events.poll() // Process each event
                    if(notifyEvent!= null)
                        notifyAdpator.updateNode(notifyEvent)
                }
            }
        })

        val btnRefresh = binding.refreshNotify
        val btnClear = binding.clearNotify
        btnRefresh.isEnabled = false
        btnClear.isEnabled = false
        // fetch initial notificatin message
        viewModel.fetchPastNotifications(
            onSuccess = {message->
                btnRefresh.isEnabled = true
                btnClear.isEnabled = true
                showToast(message)
            },
            onFailed = { message->
                btnRefresh.isEnabled = true
                btnClear.isEnabled = true
                showToast(message)
            })
        // refresh notification
        binding.refreshNotify.setOnClickListener(){
            btnRefresh.isEnabled = true
            btnClear.isEnabled = true
            notifyAdpator.clear()

            viewModel.fetchPastNotifications(
                onSuccess = {message->
                    btnRefresh.isEnabled = true
                    btnClear.isEnabled = true
                    showToast(message)
                },
                onFailed = { message->
                    btnRefresh.isEnabled = true
                    btnClear.isEnabled = true
                    showToast(message)
            })
        }

        binding.clearNotify.setOnClickListener(){
            btnRefresh.isEnabled =false
            viewModel.removeNotification(null,
                onSuccess = {message->
                    //Log.d(TAG, message)
                    btnRefresh.isEnabled},
                onFailed = {message->
                    btnRefresh.isEnabled
                    showToast(message)}
            )
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.back.setOnClickListener {
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openProfileHome()
        }

        val notifyViewer = binding.notifyViewer
        notifyViewer.layoutManager =  LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        notifyViewer.adapter = notifyAdpator
    }

    override fun onNotifyRemove(notification: Notification){
        viewModel.removeNotification(notification,
            onSuccess = {message-> Log.d(TAG, message)},
            onFailed = {message->showToast(message)}
        )
    }
    override fun onNotifyRead(notification: Notification){
        viewModel.markAsReadNotification(notification,
            onSuccess = {message->Log.d(TAG, message)},
            onFailed = {message->Log.e(TAG, message)})
    }
    override fun onNotifyView(notification: Notification){

    }
    override fun onNavigateOtherPage(page: NAVIGATION_TYPE){
        val adapter = viewPager.adapter as ViewPagerAdapter
        when(page){
            NAVIGATION_TYPE.HOME->{
                //Log.d(TAG, "Navigation to home page")
                adapter.openProfileHome()
                adapter.openHomepage()
            }
            NAVIGATION_TYPE.LOCATION->{
                //Log.d(TAG, "Navigation to location page")
                adapter.openProfileHome()
                adapter.openLocationPage()
            }
            NAVIGATION_TYPE.PROFILE->{
                //Log.d(TAG, "Navigation to profile page")
                adapter.openProfileHome()
            }
        }
    }

    override fun onNotifyItemChanged(itemCount: Int){
        if(itemCount <= 0){
            binding.labelHint.visibility = View.VISIBLE
        }
        else{
            binding.labelHint.visibility = View.GONE
        }
    }


    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
        // init notification viewer
    }

    fun onBackPressed() {
        val adapter = viewPager.adapter as ViewPagerAdapter
        adapter.openProfileHome()
    }



    private fun showToast( message: String, durationInMillis: Long = 5000) {
        if(isAdded){
            val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({
                toast.cancel()
            }, durationInMillis)
        }

    }

}