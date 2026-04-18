package myandroid.app.hhobzic.inventorymobile

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_TYPE
import myandroid.app.hhobzic.inventorymobile.api.model.ServerPoint
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentChecklistBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.utility.DateValueFormatter
import myandroid.app.hhobzic.inventorymobile.utility.TimeValueFormatter
import myandroid.app.hhobzic.inventorymobile.utility.LED_STATUS
import myandroid.app.hhobzic.inventorymobile.utility.SpeedValueFormatter
import myandroid.app.hhobzic.inventorymobile.utility.convertTimestrToMillis
import myandroid.app.hhobzic.inventorymobile.utility.getDeliverStatusLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale

class ChecklistFragment : Fragment() {
    private var TAG = "ChecklistFragment"
    private var _binding: FragmentChecklistBinding? = null
    private val binding get() = _binding!!
    private lateinit  var _chatToday: LineChart
    private lateinit var _chatWeek: LineChart
    private lateinit var viewModel: AppStatusSharedViewModel
    private var deliverState: DeliveryState? = null
    private var cartsLoad: Int = 0               // the number of carts loaded on
    private lateinit var viewPager: ViewPager2

    private var referenceTimeToday: Long = 0
    private var referenceTimeWeek: Long = 0

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "my_channel_id"
        private const val CHANNEL_NAME = "My Channel"
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChecklistBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        // observer for deliver state
        viewModel.loadingDeliverState.observe(viewLifecycleOwner, Observer { state->
            // //Log.d(TAG, "Deliver state is updated")
            deliverState = state
            val stateLabels = requireContext().resources.getStringArray(R.array.app_status_list)
            val statusLabel = getDeliverStatusLabel(stateLabels, deliverState)
            binding.labelStatus.setText(statusLabel.first)

            if(statusLabel.second == LED_STATUS.ACTIVE) {
                binding.ledStatus.setImageResource(R.drawable.menuselect)
                binding.stopTrip.visibility = View.VISIBLE
            }
            else if(statusLabel.second == LED_STATUS.INACTIVE){
                binding.ledStatus.setImageResource(R.drawable.menuselect_disable)
                binding.stopTrip.visibility = View.VISIBLE
            }
            else if(statusLabel.second == LED_STATUS.COMPLETED) {
                binding.ledStatus.setImageResource(R.mipmap.menu_completed)
                binding.stopTrip.visibility = View.VISIBLE
            }
            if(deliverState==null|| deliverState?.status == TRACK_STATUS.COMPLETED.code){ // no task to stop
                binding.stopTrip.visibility = View.GONE
            }
        })

        // observer for user information
        viewModel.user.observe(viewLifecycleOwner, Observer { userinfo ->
            // //Log.d(TAG, "Success to read user information in home fragment: $userinfo")
            binding.labelUserName.text = userinfo.name
        })

        viewModel.cartsCount.observe(viewLifecycleOwner, Observer { count->cartsLoad = count })

        viewModel.userPhoto.observe(viewLifecycleOwner, Observer { bitmap->
            binding.profileImage.setImageBitmap(bitmap)
        })

        viewModel.deliverInitCoursePoint.observe(viewLifecycleOwner, Observer { points->
            ////Log.d(TAG, "Received the course points data: ${points?.size}")
            setLineTodayChartData(points)
        })

        // delivery point is updated
        viewModel.deliverRecentCoursePoint.observe(viewLifecycleOwner, Observer { queue->
            synchronized(queue) {
                val events = LinkedList(queue) // Copy current events for processing
                queue.clear() // Clear the original queue to avoid re-processing

                while (events.isNotEmpty()) {
                    val point = events.poll() // Process each event
                    if(point!= null)
                        addOneTodayChartData(point)
                    else
                        Log.w(TAG, "Invalid data point was received")
                }
            }
        })

        viewModel.deliverWeekSpeed.observe(viewLifecycleOwner, Observer { points->
            ////Log.d(TAG, "Success to receive the week speed information")
            setLineWeekChartData(points)
        })

        binding.week.setOnClickListener(){
            binding.week.setBackgroundResource(R.drawable.backgroundbutton)
            binding.today.setBackgroundResource(R.drawable.backgroundbutton2)
            // show week graph
            binding.graphWeek.visibility = View.VISIBLE
            binding.graphToday.visibility = View.GONE
        }
        binding.today.setOnClickListener(){
            binding.week.setBackgroundResource(R.drawable.backgroundbutton2)
            binding.today.setBackgroundResource(R.drawable.backgroundbutton)
            // show today data
            binding.graphWeek.visibility = View.GONE
            binding.graphToday.visibility = View.VISIBLE
        }

        // event for stoptrip button
        binding.stopTrip.setOnClickListener {
            ////Log.d(TAG, "Trying to stop deliver")
            this.onStopTrip()
        }
        return binding.root
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = "Channel for app notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification() {
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.carticon) // Replace with your app's notification icon
            .setContentTitle("Notification Title")
            .setContentText("This is a virtual notification.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission if it's not granted
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_ID
                )
                return
            }
            // notificationId is a unique int for each notification that you must define.
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // this.viewPager.isUserInputEnabled = true
        // Initialize lineChart
        _chatToday = binding.graphToday
        _chatWeek = binding.graphWeek
        initTodayChatProperty()
        initWeekChatProperty()

        // setLineTodayChartData()

        // Set the current date and time
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())
        binding.dateandtime.text = currentDateAndTime

        val progress = 80 // Progress in percentage
        val driverProgress = 30 // Progress in percentage
        val tripProgress = 30 // Progress in percentage
        val breakProgress = 30 // Progress in percentage

        // Calculate the angle for progress
        val angle = (360 * progress / 100).toFloat()
        val driverAngle = (360 * driverProgress / 100).toFloat()
        val tripAngle = (360 * tripProgress / 100).toFloat()
        val breakAngle = (360 * breakProgress / 100).toFloat()

        // Set the progress to the custom progress bar
        binding.customProgressBar.setProgress(angle)
        binding.driveProgressBar.setProgress(driverAngle)
        binding.tripProgressBar.setProgress(tripAngle)
        binding.breakProgressBar.setProgress(breakAngle)

        binding.graphToday.visibility = View.VISIBLE
        binding.graphWeek.visibility = View.GONE
    }
    // set today chat data
    private fun setLineTodayChartData( data: List<ServerPoint>?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())
        binding.dateandtime.text = currentDateAndTime

        // Creating a list of entries (data points)
        val lineValues = ArrayList<Entry>()
        referenceTimeToday = System.currentTimeMillis()
        val xAxis = _chatToday.xAxis

        var unit = "km/h"

        if((data?.size?:0) > 0){ // there is points data
            val refTime = convertTimestrToMillis(data!![0].time!!)
            if(refTime > 0){ // failed to convert the timestring into miliseconds
                referenceTimeToday = refTime

                var point = data[0]
                var maxSpd = point.speed
                lineValues.add(Entry(0f, point.speed))
                if(data.size >= 2){
                    for( s in 1..<data.size){
                        point = data[s]
                        val diffSec = (convertTimestrToMillis(point.time!!) - refTime)/ 1000f
                        lineValues.add(Entry(diffSec, point.speed))
                        if(point.speed > maxSpd){
                            maxSpd = point.speed
                        }
                    }
                }
                if(maxSpd < 10){
                    unit = "m/s"
                }
            }
        }
        xAxis.valueFormatter = TimeValueFormatter(referenceTimeToday)

        val leftAxis = _chatToday.axisLeft
        leftAxis.valueFormatter = SpeedValueFormatter(unit)


        val lineDataSet = LineDataSet(lineValues, "Speed over time")
        lineDataSet.color = Color.BLACK
        lineDataSet.circleRadius = 10f
        lineDataSet.setDrawFilled(true)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val chatdata = LineData(lineDataSet)
        _chatToday.data = chatdata
        _chatToday.invalidate()

    }

    // add one point to today chart data
    private fun addOneTodayChartData(point: ServerPoint){
        val lineData = _chatToday.data
        if (lineData != null && referenceTimeToday > 0) {
            // Get the LineDataSet (the first dataset in this case)
            val lineDataSet = lineData.getDataSetByIndex(0) as LineDataSet
            val diffSec = (convertTimestrToMillis(point.time!!) - referenceTimeToday) / 1000.0f
            // Add new entry to the dataset
            lineDataSet.addEntry(Entry(diffSec, point.speed))

            // Notify the dataset has changed
            lineData.notifyDataChanged()

            // Notify the chart that the data has changed
            _chatToday.notifyDataSetChanged()

            // Refresh the chart
            _chatToday.invalidate()
            ////Log.d(TAG, "Success to add recent point into today graph")
        }
    }

    private fun setLineWeekChartData( data: List<ServerPoint>?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())
        binding.dateandtime.text = currentDateAndTime

        // Creating a list of entries (data points)
        val lineValues = ArrayList<Entry>()
        referenceTimeWeek = System.currentTimeMillis()
        val xAxis = _chatWeek.xAxis
        var unit = "km/h"
        if((data?.size?:0) > 0){ // there is points data
            val refTime = convertTimestrToMillis(data!![0].time!!)
            if(refTime > 0){ // failed to convert the timestring into miliseconds

                referenceTimeWeek = refTime
                var point = data[0]
                var maxSpd = point.speed
                lineValues.add(Entry(0f, point.speed))
                if(data.size >= 2){
                    for( s in 1..<data.size){
                        point = data[s]
                        val diffSec = (convertTimestrToMillis(point.time!!) - refTime)/ 1000f
                        lineValues.add(Entry(diffSec, point.speed))
                        maxSpd = Math.max(maxSpd, point.speed)
                    }
                }
                if(maxSpd < 10)
                    unit = "m/s"
            }
        }

        val leftAxis = _chatWeek.axisLeft
        leftAxis.valueFormatter = SpeedValueFormatter(unit)

        xAxis.valueFormatter = DateValueFormatter(referenceTimeToday)
        val lineDataSet = LineDataSet(lineValues, "Speed over time")
        lineDataSet.color = Color.BLACK
        lineDataSet.circleRadius = 10f
        lineDataSet.setDrawFilled(true)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val chatdata = LineData(lineDataSet)
        _chatWeek.data = chatdata
        _chatWeek.invalidate()
    }


    // set the property of today-chat
    private fun initTodayChatProperty() {
        _chatToday.isDragEnabled = true
        _chatToday.setScaleEnabled(true)
        _chatToday.setPinchZoom(true)
        _chatToday.isScaleYEnabled = false // Horizontal zoom only

        // Configure X-axis (time axis)
        val xAxis = _chatToday.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // one tick unit. One tick is 1 minutes
        xAxis.isGranularityEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 11f
        xAxis.typeface = Typeface.DEFAULT // You can change to custom typeface if needed
        // xAxis.setLabelRotationAngle(45f) // Rotate labels for better visibility
        val referenceTimestamp = System.currentTimeMillis()
        xAxis.valueFormatter = TimeValueFormatter(referenceTimestamp)


        // Y-axis styling (Left)
        val yAxisLeft = _chatToday.axisLeft
        yAxisLeft.textColor = Color.RED
        yAxisLeft.textSize = 12f
        yAxisLeft.typeface = Typeface.SANS_SERIF // You can use a custom typeface here
        yAxisLeft.setDrawGridLines(true)
        // yAxisLeft.valueFormatter = SpeedValueFormatter()


        val yAxisRight = _chatToday.axisRight
        yAxisRight.isEnabled = false // Disable right Y-axis
        // Other configurations
        _chatToday.description.isEnabled = false
        // hide dataset description
        _chatToday.legend.isEnabled = false

        _chatToday.setBackgroundColor(Color.WHITE)
        _chatToday.animateXY(1000, 1000, Easing.EaseInCubic)


    }



    private fun initWeekChatProperty() {
        _chatWeek.isDragEnabled = true
        _chatWeek.setScaleEnabled(true)
        _chatWeek.setPinchZoom(true)
        _chatWeek.isScaleYEnabled = false // Horizontal zoom only

        // Configure X-axis (time axis)
        val xAxis = _chatWeek.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // one tick unit. One tick is 1 minutes
        xAxis.isGranularityEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 11f
        xAxis.typeface = Typeface.DEFAULT // You can change to custom typeface if needed
        // xAxis.setLabelRotationAngle(45f) // Rotate labels for better visibility
        val referenceTimestamp = System.currentTimeMillis()
        xAxis.valueFormatter = TimeValueFormatter(referenceTimestamp)


        // Y-axis styling (Left)
        val yAxisLeft = _chatWeek.axisLeft
        yAxisLeft.textColor = Color.RED
        yAxisLeft.textSize = 12f
        yAxisLeft.typeface = Typeface.SANS_SERIF // You can use a custom typeface here
        yAxisLeft.setDrawGridLines(true)
        // yAxisLeft.valueFormatter = SpeedValueFormatter()


        val yAxisRight = _chatWeek.axisRight
        yAxisRight.isEnabled = false // Disable right Y-axis
        // Other configurations
        _chatWeek.description.isEnabled = false
        // hide dataset description
        _chatWeek.legend.isEnabled = false

        _chatWeek.setBackgroundColor(Color.WHITE)
        _chatWeek.animateXY(1000, 1000, Easing.EaseInCubic)
    }

    private fun onStopTrip(){
        ////Log.d(TAG, "trying to stop trip")
        var dialogStopTrip = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_question_stop, null)

        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogStopTrip)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
        val layoutRemain = dialogStopTrip.findViewById<LinearLayout>(R.id.layoutRemain)
        layoutRemain.visibility = View.GONE

        var dropdownEntity = dialogStopTrip.findViewById<AutoCompleteTextView>(R.id.entityID)
        val nodeNameLayout = dialogStopTrip.findViewById<LinearLayout>(R.id.layoutNode)

        // parking is default option
        val checkPark = dialogStopTrip.findViewById<CheckBox>(R.id.checkParking)
        val checkLc = dialogStopTrip.findViewById<CheckBox>(R.id.checkCompany)
        val checkAddLabel = dialogStopTrip.findViewById<TextView>(R.id.labelStopLocation)
        val addrOrDesc = dialogStopTrip.findViewById<EditText>(R.id.stopDescription)

        checkPark.isChecked = true
        checkLc.isChecked = false
        nodeNameLayout.visibility = View.GONE

        checkPark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nodeNameLayout.visibility = View.GONE
                checkLc.isChecked = false
                checkAddLabel.text =  getString(R.string.address)
                addrOrDesc.hint = getString(R.string.addresshint)

            } else {
                nodeNameLayout.visibility = View.VISIBLE
                checkLc.isChecked = true
                checkAddLabel.text =  getString(R.string.description)
                addrOrDesc.hint = getString(R.string.descriptionhint)
            }
        }

        checkLc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nodeNameLayout.visibility = View.VISIBLE
                checkPark.isChecked = false
                checkAddLabel.text =  getString(R.string.description)
                addrOrDesc.hint = getString(R.string.descriptionhint)
            } else {
                nodeNameLayout.visibility = View.GONE
                checkPark.isChecked = true
                checkAddLabel.text =  getString(R.string.address)
                addrOrDesc.hint = getString(R.string.addresshint)
            }
        }

        val companies: List<Company> = viewModel.getCompany()
        val dcs: List<DistCenter>? = viewModel.getDistributionCenter()
        if(dcs?.isNotEmpty()==true) {
            val dcNames =dcs.map{it.name}
            var adaptorEntity = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, dcNames)
            dropdownEntity.setAdapter(adaptorEntity)
            dropdownEntity.setText(dcNames[0], false)
        }

        // create checkbox for selecting the final entity item
        val names = companies.map{it.name}
        var adaptorEntity = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, names)
        dropdownEntity.setAdapter(adaptorEntity)
        dropdownEntity.setText(names[0], false)


        // create dropdown for terminating reason
        val pairs = requireContext().resources.getStringArray(R.array.trip_stop_reason)
        val reasonPairs: List<Pair<Int, String>> = pairs.map { item ->
            val splitItem = item.split(":")
            val number = splitItem[0].toInt()
            val text = splitItem[1]
            Pair(number, text)
        }
        val reasonLabels = reasonPairs.map { it.second }
        val adaptorReason = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, reasonLabels)
        var dropdownReason = dialogStopTrip.findViewById<AutoCompleteTextView>(R.id.stopReason)
        dropdownReason.setAdapter(adaptorReason)
        if(reasonLabels.isNotEmpty())
            dropdownReason.setText(reasonLabels[0],  false)
        val btnConfirm = dialogStopTrip.findViewById<AppCompatButton>(R.id.confirmStop)

        btnConfirm.setOnClickListener(){
            val description = dialogStopTrip.findViewById<EditText>(R.id.stopDescription).text.toString()
            val reasonLabel = dropdownReason.text.toString()
            val r = reasonPairs.findLast { it.second == reasonLabel }
            if(r == null){
                showToast(ApiNotification.REQUIRE_FILL_FIELD)
                return@setOnClickListener
            }
            val reason = r.first
            var node_id = ""
            var node_type = NODE_TYPE.UNK.code

            if(checkLc.isChecked){ // Stop at logistic company
                var node_name = dropdownEntity.text.toString()
                val company = companies.findLast { it.name == node_name }
                if(company==null){
                    showToast(ApiNotification.REQUIRE_VALID_COMPANY)
                    return@setOnClickListener
                }
                node_type = NODE_TYPE.COMPANY.code
                node_id = company.id
            }
            else{ // stop at parking
                node_type = NODE_TYPE.PARKING.code
                if(description.isEmpty()){
                    showToast(ApiNotification.NOTIFY_INPUT_ADDRESS)
                    return@setOnClickListener
                }
            }
            btnConfirm.isEnabled = false
            //val forceStop: Boolean = dialogStopTrip.findViewById<CheckBox>(R.id.forceStop).isChecked
            val forceStop = true
            alertDialog.dismiss()
            viewModel.stopDeliverTask(
                node_type, node_id, reason, description, forceStop,
                onSuccess = {msg->
                    showToast(msg)
                    alertDialog.dismiss()
                },
                onFailed = {msg->showToast(msg)})
            btnConfirm.isEnabled = true
        }

        dialogStopTrip!!.findViewById<TextView>(R.id.cancelStop).setOnClickListener() {
            ////Log.d(TAG, "back button clicked")
            alertDialog.dismiss()
        }
        alertDialog.show()

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

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
