package myandroid.app.hhobzic.inventorymobile.home.profile

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentEditTruckBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class EditTruckFragment: Fragment() {

    private val TAG = "EditProfileFragment"
    private var _binding: FragmentEditTruckBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var viewPager: ViewPager2
    private var         userinfo: Driver? = null
    private var         timeStart = "00:00"
    private var         timeStop = "00:00"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTruckBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        setupUI(binding.root)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.user.observe(viewLifecycleOwner, Observer { userinfo ->
            this.userinfo = userinfo
            //Log.d(TAG, "Success to load user information in  truck profile page: $userinfo")
            binding.truckVin.setText(userinfo.vin)
            this.userinfo?.let { u ->
                this.timeStart = u.time_taskstart
                this.timeStop = u.time_taskstop
            }
            val autoStart = userinfo.auto_start > 0
            binding.chkAutostart.isChecked = autoStart
            binding.layoutDeliverFrom.isVisible = autoStart
            binding.layoutDeliverTo.isVisible = autoStart
            binding.btnPickupFrom.text = this.convertToAmPmFormat(this.timeStart)
            binding.btnPickupTo.text = this.convertToAmPmFormat(this.timeStop)
        })

        binding.back.setOnClickListener {
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openProfileHome()
        }

        binding.save.setOnClickListener() {
            val newVin = binding.truckVin.text.toString()
            if (newVin.isEmpty()) {
                showToast(ApiNotification.USER_INVALID_VIN)
                return@setOnClickListener
            }
            this.userinfo?.let { u->
                u.vin = newVin
                u.auto_start = if(binding.chkAutostart.isChecked) 1 else 0
                if(u.auto_start > 0){
                    u.time_taskstart = this.timeStart
                    u.time_taskstop = this.timeStop
                }
                viewModel.updateTruckInformation(u,
                    onSuccess = { message ->
                        showToast(message)
                        gotoPreviousPage()
                    },
                    onFailed = { message -> showToast(message) })
            }
        }

        // deliver start time
        binding.btnPickupFrom.setOnClickListener { this.showTimePicker(true) }

        // deliver stop time
        binding.btnPickupTo.setOnClickListener { this.showTimePicker(false) }

        // check autostart
        binding.chkAutostart.setOnCheckedChangeListener { _, isChecked ->
            this.userinfo?.auto_start = if(isChecked) 1 else 0
            binding.layoutDeliverFrom.isVisible = isChecked
            binding.layoutDeliverTo.isVisible = isChecked
        }
    }

    private fun showTimePicker(from: Boolean){
        var initialTime = "00:00"
        this.userinfo?.let { u->
            initialTime = if(from) u.time_taskstart else u.time_taskstop

        }
        val parts = initialTime.split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts[1].toIntOrNull() ?: 0

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                if(from) {
                    binding.btnPickupFrom.text = this.convertToAmPmFormat(formattedTime)
                    this.timeStart = formattedTime
                }
                else {
                    binding.btnPickupTo.text = formattedTime
                    this.timeStop = this.convertToAmPmFormat(formattedTime)
                }
            },
            hour,
            minute,
            true // true for 24-hour format, false for 12-hour format
        )
        // set initial value.

        timePickerDialog.show()
    }

    private fun convertToAmPmFormat(time24: String): String {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val date = inputFormat.parse(time24)
        return if (date != null) outputFormat.format(date) else ""
    }


    private fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                view.clearFocus()
                false
            }
        }
        // If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun onBackPressed() {
        val adapter = viewPager.adapter as ViewPagerAdapter
        adapter.openProfileHome()
    }

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }

    private fun gotoPreviousPage(){
        val adaptor = viewPager.adapter as ViewPagerAdapter
        adaptor.openProfileHome()
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