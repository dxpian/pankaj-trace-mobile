package myandroid.app.hhobzic.inventorymobile.home.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.TrackSetting
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentTrackSettingBinding
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import kotlin.math.max
import kotlin.math.min


class TrackSettingConstant{
    companion object Constant{
        // constant for distant threshold value
        val DISTANCE_MIN = 100      // unit is sec
        val DISTANCE_MAX = 1000
        val DISTANCE_UNIT = 100

        // constant for scan time value
        val SCANTIME_MIN = 1        // 1 munutes
        val SCANTIME_MAX = 120      // 2 hours
        val SCANTIME_LIST: List<Int> = listOf(1, 5, 10, 15, 20, 30, 40, 50, 60,70, 80, 90, 100, 110, 120)

        // constant for gps location value
        val LOC_UPDATE_MIN = 1        // 1 minutes
        val LOC_UPDATE_MAX = 5        // 2 minutes
    }
}


class TrackSettingFragment: Fragment() {
    private val TAG = "TrackSettingFragment"
    private var _binding: FragmentTrackSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var trackSetting: TrackSetting
    private lateinit var adaptorTimezone: ArrayAdapter<String>
    // List with "UTC" prefix
    val tzLabelList = listOf("UTC-12:00","UTC-11:00","UTC-10:00","UTC-09:30","UTC-09:00","UTC-08:00","UTC-07:00","UTC-06:00","UTC-05:00","UTC-04:00","UTC-03:30","UTC-03:00","UTC-02:00","UTC-01:00","UTC+00:00","UTC+01:00",
        "UTC+02:00","UTC+03:00","UTC+03:30","UTC+04:00","UTC+04:30","UTC+05:00","UTC+05:30","UTC+05:45","UTC+06:00","UTC+06:30","UTC+07:00","UTC+08:00",
        "UTC+08:45","UTC+09:00","UTC+09:30","UTC+10:00","UTC+10:30","UTC+11:00","UTC+12:00","UTC+12:45","UTC+13:00","UTC+14:00"
    )

    // List of time zone offsets as float values
    val tzValueList = listOf(-12.0f,-11.0f,-10.0f,-9.5f,-9.0f,-8.0f,-7.0f,-6.0f,-5.0f,-4.0f,-3.5f,-3.0f,-2.0f,-1.0f,0.0f,1.0f,2.0f,
        3.0f,3.5f,4.0f,4.5f,5.0f,5.5f,5.75f,6.0f,6.5f,7.0f,8.0f,8.75f,9.0f,9.5f,10.0f,10.5f,11.0f,12.0f,12.75f,13.0f,14.0f)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackSettingBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        var textScanTime = binding.scantimeValue
        var textLocation = binding.locUpdateValue
        var textDistance = binding.distacneValue

        if(tzValueList.size != tzLabelList.size){
            Log.e(TAG, "timezone constant isn't matched")
        }

        // add adaptor to timezone
        adaptorTimezone = ArrayAdapter(requireContext(),  R.layout.dropdown_autocomplete, tzLabelList)
        binding.trackTimezone.setAdapter(adaptorTimezone)
        binding.trackTimezone.setText(tzLabelList[0], false)
        binding.trackTimezone.setOnItemClickListener{ parent, view, position, id ->
            val tzValue = tzValueList[position].toString()
            trackSetting.timezone = tzValue
        }

        // set event handler for seek bar
        binding.scanSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scanTime = TrackSettingConstant.SCANTIME_LIST[progress]
                textScanTime.text = requireContext().getString(R.string.tracksetting_distance_value, scanTime)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.distanceSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textDistance.text = requireContext().getString(R.string.tracksetting_distance_value, (progress + 1) * 100)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.locationUpdateSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textLocation.text = requireContext().getString(R.string.tracksetting_locupdate_value, (progress + 1))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // set click listener for help icon
        binding.helpIconDistance.setOnClickListener(){// toggle visiblity of help text
            binding.helpDistance.visibility = if (binding.helpDistance.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        binding.helpIconLocation.setOnClickListener(){
            binding.helpLocation.visibility = if (binding.helpLocation.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        binding.helpIconScantime.setOnClickListener(){
            binding.helpScanTime.visibility = if (binding.helpScanTime.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        binding.back.setOnClickListener(){
            this.onBackPressed()
        }

        binding.save.setOnClickListener(){
            //Log.d(TAG, "Saving the track setting parameter")

            trackSetting.distanceMeter = (binding.distanceSeek.progress + 1) * 100
            val progScantime = binding.scanSeek.progress
            trackSetting.scanTimeSec = TrackSettingConstant.SCANTIME_LIST[progScantime] * 60 // convert to sec
            val progLocupdate = binding.locationUpdateSeek.progress
            trackSetting.locationUpdateSec = (progLocupdate + 1) * 60     // convert to sec
            //Log.d(TAG, "Save new track setting: $trackSetting")
            binding.back.isEnabled = false
            viewModel.updateTrackSetting(trackSetting,
                onSuccess = { msg->
                    showToast(ApiNotification.OPERATION_SUCCESS)
                    onBackPressed()
                },
                onFailed = {msg->
                    binding.back.isEnabled = true
                    showToast(msg)
                })
        }

        viewModel.trackSetting.observe(viewLifecycleOwner, Observer { setting->
            this.trackSetting = setting;
            this.onLoadSetting()
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // hide all help string
        binding.helpScanTime.visibility = View.GONE
        binding.helpDistance.visibility = View.GONE
        binding.helpLocation.visibility = View.GONE
        // clear all values
        binding.scantimeValue.text = ""
        binding.distacneValue.text = ""
        binding.locUpdateValue.text = ""


    }

    override fun onResume() {
        super.onResume()
        trackSetting = viewModel.getTrackSetting()
    }

    /*
        New setting is loaded from truck
         */
    private fun onLoadSetting(){
        // update the scan time. input unit is seconds
        //Log.d(TAG, "Track setting is updated: $trackSetting")
        val scantimeMinutes = min((max((this.trackSetting.scanTimeSec / 60).toInt(), TrackSettingConstant.SCANTIME_MIN)), TrackSettingConstant.SCANTIME_MAX)
        val scanTimeLists = TrackSettingConstant.SCANTIME_LIST
        var scanTimeDisCreate = 0
        var scanTimeRaw = 0
        for((index, value) in scanTimeLists.withIndex()){
            if(scantimeMinutes <= value){
                scanTimeDisCreate = index
                scanTimeRaw = value
                break
            }
        }
        binding.scantimeValue.setText(requireContext().getString(R.string.tracksetting_scantime_value, scanTimeRaw))
        binding.scanSeek.progress = scanTimeDisCreate

        // update the distance value
        //Log.d(TAG, "Track setting is updated: ${this.trackSetting}")
        var distanceRaw: Int = this.trackSetting.distanceMeter
        val distanceDiscreted: Int = min(max(distanceRaw, TrackSettingConstant.DISTANCE_MIN), TrackSettingConstant.DISTANCE_MAX) / TrackSettingConstant.DISTANCE_UNIT - 1
        distanceRaw = (distanceDiscreted + 1) * 100
        binding.distacneValue.setText(requireContext().getString(R.string.tracksetting_distance_value, distanceRaw))
        binding.distanceSeek.progress = distanceDiscreted

        // update gps location time
        val gpsIntervalDiscrete: Int = min(max(this.trackSetting.locationUpdateSec / 60, TrackSettingConstant.LOC_UPDATE_MIN), TrackSettingConstant.LOC_UPDATE_MAX) - 1
        val gpsIntervalRaw = gpsIntervalDiscrete + 1
        binding.locUpdateValue.setText(requireContext().getString(R.string.tracksetting_locupdate_value, gpsIntervalRaw))
        binding.locationUpdateSeek.progress = gpsIntervalDiscrete

        // update timezone
        var timezone:Float = 5.0f
        try {
            timezone = trackSetting.timezone.toFloat()
        }
        catch (e: NumberFormatException){
            Log.w(TAG, "Failed to convert timezone into float value")
        }
        var label = tzLabelList[0]
        for((index, value) in tzValueList.withIndex()){
            if(value < timezone)
                continue
            label = tzLabelList[index]
            break
        }
        binding.trackTimezone.setAdapter(adaptorTimezone)
        binding.trackTimezone.setText(label, false)
    }

    fun onBackPressed() {
        val adapter = viewPager.adapter as ViewPagerAdapter
        adapter.openProfileHome()
    }

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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