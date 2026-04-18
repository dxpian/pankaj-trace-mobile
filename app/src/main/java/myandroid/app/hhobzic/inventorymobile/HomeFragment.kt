package myandroid.app.hhobzic.inventorymobile

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build.*
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.*
import kotlinx.coroutines.Runnable
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.Cart
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.api.model.ErrorCode
import myandroid.app.hhobzic.inventorymobile.api.model.NODE_STATUS
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentHomeBinding
import myandroid.app.hhobzic.inventorymobile.gps.VirtualGpsSensor
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.home.ManagePermissions
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import myandroid.app.hhobzic.inventorymobile.home.cart.AddPickupActivity
import myandroid.app.hhobzic.inventorymobile.home.cart.CartViewerAdaptor
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverCartActivity
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverEntity
import myandroid.app.hhobzic.inventorymobile.home.cart.DeliverEntityAdaptor
import myandroid.app.hhobzic.inventorymobile.home.cart.EntityEventType
import myandroid.app.hhobzic.inventorymobile.home.map.MarDcWindowAdaptor
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.service.AppForegroundService
import myandroid.app.hhobzic.inventorymobile.utility.AppLogger
import myandroid.app.hhobzic.inventorymobile.utility.AppState
import myandroid.app.hhobzic.inventorymobile.utility.LED_STATUS
import myandroid.app.hhobzic.inventorymobile.utility.MapRouteComponents
import myandroid.app.hhobzic.inventorymobile.utility.getDeliverStatusLabel
import myandroid.app.hhobzic.inventorymobile.utility.getGreetingBasedOnTime
import myandroid.app.hhobzic.inventorymobile.utility.loadLanguagePreference
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.LinkedList
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private lateinit var addPickupLauncher: ActivityResultLauncher<Intent>
    private val binding get() = _binding!!
    private var isActive = false
    private lateinit var    managePermissions: ManagePermissions
    private lateinit var    locationCallback: LocationCallback
    private lateinit var    nodeAdaptor: DeliverEntityAdaptor
    private lateinit var    cartAdaptor: CartViewerAdaptor
    private var             deliverState: DeliveryState? = null
    private var             appState: AppState? = null
    private lateinit var    companies: List<Company>
    private var             driver: Driver? = null;
    private var             curLocation: Location? = null
    private var             currentLocationMarker: Marker? = null
    private var             curMarkerOption: MarkerOptions? = null
    private var             googleMap: GoogleMap?=null
    private var             isMapReady = false
    private var             entityUpdated = false        //
    private lateinit var    viewModel: AppStatusSharedViewModel
    private lateinit var    viewPager: ViewPager2
    private var             virtGpsSensor: VirtualGpsSensor? = null
    private var             mapScaleInited = false

    private var             forceInitTrip = true
    private var             mapExpanded = false
    private var             tagExpanded = false         // tag is expanded?
    private lateinit  var   geoConext : GeoApiContext
    private var             polyline: Polyline? = null
    private val             markerList = mutableListOf<Marker>()            // marker list
    private var             tripDirection: Int = 0          // trip directions
    private var             _versonChecked = false
    private var             appForegroundService: AppForegroundService? = null
    private var             isLocationServiceBound = false
    private var             locationInterval = AppMacro.LOCATION_UPDATE_INTERVAL
    private var             locationManager :LocationManager? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            AppLogger.d(TAG, "Success to create foreground service for location callback")
            val binder = service as AppForegroundService.LocalBinder
            appForegroundService = binder.getService()
            isLocationServiceBound = true

            // Start location updates and pass the LocationCallback
            appForegroundService?.startLocationUpdates(locationCallback,  AppMacro.LOCATION_UPDATE_INTERVAL)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isLocationServiceBound = false
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateGreetingRunnable = object : Runnable {
        override fun run() {
            updateGreetingText()
            handler.postDelayed(this, 30 * 60 * 1000) // Update every 30 minutes
        }
    }

    private val checkLocPermissionRunnable = object : Runnable {
        override fun run() {
            if(isActive){
                checkLocationService()
            }
            handler.postDelayed(this, 5 * 60 * 1000) // Update every 1 minutes
        }
    }


    private val updateLocationRunnable = object :  Runnable {
        override  fun run(){
            AppLogger.d(TAG, "Update the location of truck manually")
            val tempLocation = Location("manualProvider") // Set the provider name manually
            val virtLocation: LatLng?
            if(AppMacro.SIMULATE_NEIGHBOR_GPS)
                virtLocation = virtGpsSensor!!.getSimulationNeighborGpsLocation()
            else
                virtLocation = virtGpsSensor!!.getGpsLocation()
            tempLocation.latitude = virtLocation.latitude
            tempLocation.longitude = virtLocation.longitude
            curLocation = tempLocation
            updateLocationOnMap()
            callbackSendLocation(curLocation!!)

            handler.postDelayed(this, 1 * 2 * 1000) // Update every 1 minutes
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // load language setting

        val language = loadLanguagePreference(requireContext())
        AppLogger.d(TAG, "load language setting in home frag: ${language}")
        this.setDefaultLocale(language)

        geoConext = createGeoApiContext()
        // Register the ActivityResultLauncher
        addPickupLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onReturnFromAddPickup()
            }
        }
        binding.startTrip.visibility = View.GONE
        binding.addPickup.visibility = View.GONE
        binding.addDelivery.visibility = View.GONE

        // initialize entity viewer
        nodeAdaptor = DeliverEntityAdaptor(mutableListOf(), ::callbackNodeItemClicked)
        val nodeViewer = binding.nodeviewer
        nodeViewer.layoutManager =  LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        nodeViewer.adapter = nodeAdaptor

        // initialize cart viewer
        cartAdaptor = CartViewerAdaptor(mutableListOf(), ::callbackCartItemClicked)
        val cartViewer = binding.tagViewer
        cartViewer.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        cartViewer.adapter = cartAdaptor

        this.loadMap()

        if(AppMacro.SIMULATE_GPS || AppMacro.SIMULATE_NEIGHBOR_GPS){ // init virtual GPS sensor
            virtGpsSensor = VirtualGpsSensor(requireContext())
            virtGpsSensor?.loadJsondata()
        }

        return binding.root
    }

    @Suppress("DEPRECATION")
    private fun setDefaultLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

            AppLogger.d(TAG, "Native LocationManager got location: ${location.latitude}, ${location.longitude}")
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.viewPager.isUserInputEnabled = false
        this.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val status = deliverState?.status ?: TRACK_STATUS.INIT.code
                AppLogger.d(TAG, "Update gps location in home fragment")
                for (location in locationResult.locations) {
                    if(location != null){
                        curLocation = location
                        if(AppMacro.SIMULATE_GPS  && status > TRACK_STATUS.INIT.code ){ // set the current location with the simulation value
                            val tempLocation = Location("manualProvider") // Set the provider name manually
                            var virtLocation: LatLng?
                            if(AppMacro.SIMULATE_NEIGHBOR_GPS)
                                virtLocation = virtGpsSensor!!.getSimulationNeighborGpsLocation()
                            else
                                virtLocation = virtGpsSensor!!.getGpsLocation()
                            tempLocation.latitude = virtLocation.latitude
                            tempLocation.longitude = virtLocation.longitude
                            curLocation = tempLocation
                        }
                        updateLocationOnMap()
                        callbackSendLocation(curLocation!!)
                    }
                }
            }
        }
        isActive = true

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadMap()
        }

        binding.addPickup.setOnClickListener {
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openViewcheckList()
        }

        binding.addDelivery.setOnClickListener(){
            // startActivity(Intent(requireContext(), DeliverCartActivity::class.java))
            val adaptor = viewPager.adapter as ViewPagerAdapter
            // adaptor.openViewcheckList()
            adaptor.openDeliverList()
        }

        binding.startTrip.setOnClickListener(){ // start trip
            val dialog = openStartDeliverDialog()
            dialog.show()
        }

        // show/hide map
        binding.iconControlMap.setOnClickListener(){
            controlMap(!mapExpanded)
        }

        // show/hide tag viewer
        binding.iconControlCartmap.setOnClickListener(){
            controlTagVeiwer(!tagExpanded)
        }

        // hide map initially
        binding.layoutMap.visibility = View.GONE
        binding.iconControlMap.setImageResource(R.mipmap.map_expand)
        mapExpanded = false

        // hide tag viewer
        binding.tagViewer.visibility = View.GONE
        binding.tagSeparator.visibility = View.GONE
        binding.iconAutoStart.visibility = View.GONE
        tagExpanded = false

        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)
        val localBinding = binding

        // user information is updated
        viewModel.user.observe(viewLifecycleOwner, Observer { userinfo ->
            this.driver = userinfo
            localBinding.userName.text = userinfo.name
            this.checkAutomaticStartStatus()
        })

        // user photo is downloaded
        viewModel.userPhoto.observe(viewLifecycleOwner, Observer { bitmap->
            localBinding.profileImage.setImageBitmap(bitmap)
        })

        // error received
        viewModel.error.observe(viewLifecycleOwner, Observer{error->
            showToast(error)
        })

        // app state is updated
        viewModel.loadingDeliverAppState.observe(viewLifecycleOwner, Observer { state->
            appState = state
            onAppStateUpdated()
        })
        // the state of app is updated
        viewModel.entityUpdated.observe(viewLifecycleOwner, Observer { queue->
            val events = LinkedList(queue) // copy events from this emission only
            while (events.isNotEmpty()) {
                val entity = events.poll() // Process each event
                if(entity?.entity_type == EntityEventType.INIT_TRIP){ // init a new trip
                    AppLogger.d(TAG, "init trip since there is no task")
                    binding.startTrip.performClick()
                }
                else if(entity?.entity_type == EntityEventType.INIT_TRIP_NOTIFY){
                    AppLogger.d(TAG, "remove all entities when init trip")
                    nodeAdaptor.clear()
                    removeAllMarkers()
                    removeAllPolylines()
                }
                else{ // update trip
                    if(entity != null)
                    {
                        AppLogger.d(TAG, "update the entity: $entity")
                        nodeAdaptor.updateEntity(entity)
                        entityUpdated = true
                    }
                    else{
                        AppLogger.w(TAG, "Invalid entity is received")
                    }
                }
                // update map
                updateMapCourse()
            }
        })

        // cart item is updated
        viewModel.cartsLoaded.observe(viewLifecycleOwner, Observer { carts->
            cartAdaptor.setItems(carts)
            localBinding.itemLoaded.text = getString(R.string.cart_count, carts.size)
            if(carts.size > 0) {
                binding.iconControlCartmap.visibility = View.VISIBLE
            } else {
                binding.iconControlCartmap.visibility = View.GONE
            }
        })

        // trip direction is chaged
        viewModel.tripDirectionUpdated.observe(viewLifecycleOwner, Observer { direction->
            tripDirection = direction
        })

        // initialization
        viewModel.fetchInitialInformation(onSuccess = {result->
            when (result) {
                is ApiResult.Success -> {
                    showToast( ApiNotification.DELIVER_LOAD_SUCCESS)
                }
                is ApiResult.Code -> {
                    when (result.code) {
                        ErrorCode.TZ_SUCCESS -> {
                            ////AppLogger.d(TAG, "Success to load previous delivering task")
                            showToast(ApiNotification.DELIVER_LOAD_SUCCESS)
                        }
                        ErrorCode.TZ_NO_DELIVER_TASK -> {
                            // //AppLogger.d(TAG, "No deliver task now, start new deliver task automatically")
                            showToast(ApiNotification.DELIVER_NO_FOUND)
                        }
                        else -> {
                            AppLogger.e(TAG, "Get invalid error code : ${result.code}")
                        }
                    }
                }
                is ApiResult.Error -> {
                    showToast(result.message)
                }
            }
        })

        viewModel.loadingDeliverState.observe(viewLifecycleOwner, Observer { state->
            deliverState = state
            this.onDeliverStateUpdated()
            this.checkAutomaticStartStatus()
        })

        viewModel.deliverInitCoursePoint.observe(viewLifecycleOwner, Observer { points->
            removeAllPolylines()
            if(points != null){
                val coordinates = points.map { LatLng(it.latitude.toDouble(), it.longitude.toDouble()) }
                if(googleMap!= null){
                    polyline  = googleMap!!.addPolyline(
                        PolylineOptions()
                            .addAll(coordinates)        // Add the initial point
                            .color(ContextCompat.getColor(requireContext(), R.color.route_reached))         // Set the polyline color
                            .width(10f)                 // Set the polyline width
                            .zIndex(3f)                // Set the z-index for layering
                            .geodesic(true)            // Optional: Follow the curvature of the earth
                    )
                }else{
                    AppLogger.w(TAG, "google map isn't ready")
                }
            }
        })

        viewModel.trackSetting.observe(viewLifecycleOwner, Observer { setting->
            run {
                if(!AppMacro.SIMULATE_GPS){
                    this.locationInterval = setting.locationUpdateSec * 1000L
                }
                if(isLocationServiceBound) {
                    if(this.isGooglePlayServicesAvailable(requireContext())){ // google play location service
                        appForegroundService?.restartLocationUpdates(this.locationInterval)
                    }
                    else{ // normal android location service
                        this.locationManager?.let { manager->{
                            manager.removeUpdates(locationListener)
                            manager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                this.locationInterval,
                                10f,
                                locationListener
                            )
                        } }
                    }

                }
            }
        })

        this.checkApkVersion()

        this.companies = viewModel.getCompany()

        handler.post(updateGreetingRunnable)
        handler.post(checkLocPermissionRunnable)
        if(AppMacro.SIMULATE_GPS || AppMacro.SIMULATE_NEIGHBOR_GPS)
            handler.post(updateLocationRunnable)
        requestLocationPermission(true)


    }

    private fun checkAutomaticStartStatus(){
        val visible =
            (deliverState?.status == TRACK_STATUS.COMPLETED.code) && (driver?.auto_start ?: 0) > 0
        binding.iconAutoStart.visibility = if (visible) View.VISIBLE else View.GONE

    }

    private fun checkApkVersion(){
        // check the self APK version
        if(this._versonChecked){
            AppLogger.d(TAG, "Version is already checked")
            return
        }
        this._versonChecked = true
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val currentVersionCode = packageInfo.longVersionCode  // versionCode from build.gradle

        viewModel.checkLastApkVersion(
            onSuccess = {apkVersion ->
                AppLogger.d(TAG, "Success to find lastest apk version: current version = $currentVersionCode, new code: ${apkVersion.code}")
                if(apkVersion.code <= currentVersionCode){ // no new version is available
                    this.showToast(ApiNotification.APK_UPDATE_UNAVAILABLE)
                }else{
                    this.showUpdateDialog(apkVersion.id)
                }
            },
            onFailed = {msg ->
                this.showToast(ApiNotification.APK_FAILED_CHECK)
            })
    }

    private fun showUpdateDialog(apkUrl: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(ApiNotification.APK_UPDATE)
            .setMessage(ApiNotification.APK_UPDATE_AVAILBABLE)
            .setPositiveButton("Ok") { _, _ ->
                downloadAndInstallApk(apkUrl)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadApkFile(context: Context, url: String, fileName: String, onComplete: (File?) -> Unit) {
//        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//        val apkFile = File(downloadDir, fileName)
        val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        AppLogger.d(TAG, "folder path to save the apk: ${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}")

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    onComplete(null)
                    return@Thread
                }
                val input = connection.inputStream
                val output = FileOutputStream(apkFile)

                val buffer = ByteArray(4096)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }

                output.flush()
                output.close()
                input.close()

                onComplete(apkFile)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }.start()
    }


    private fun downloadAndInstallApk(apkId: String) {
        val fileName = "update.apk"
        val serverUrl = viewModel.getServerUrl()
        val apkUrl = "${serverUrl}/mobile/apk/downloadApk/$apkId"
        AppLogger.d(TAG, "New apk download url: $apkUrl. ")

        this.downloadApkFile(requireContext(), apkUrl, fileName, onComplete = {f->
            if(f!= null)
                this.installApk(f)
            else{
                this.showToast(ApiNotification.APK_DOWNLOAD_FAILED)
            }
        })
    }

    private fun installApk(file: File) {
        AppLogger.d(TAG, "Installing APK file")
        if(!file.exists() ||  file.length() <=0){
            this.showToast(ApiNotification.APK_DOWNLOAD_FAILED)
            return
        }
        AppLogger.d(TAG, "Success to download APK: ${file.absolutePath}. size = ${file.length()}")
        val apkUri: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        AppLogger.d(TAG, "apk uri: $apkUri")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        requireContext().startActivity(intent)
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return result == ConnectionResult.SUCCESS
    }

    private fun openStartDeliverDialog(): AlertDialog {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_start_trip, null)

        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
        // select company adaptor
        val companyNames = this.companies.map { it.name }
        val arrayAdaptor = ArrayAdapter(requireContext(),R.layout.dropdown_autocomplete, companyNames)
        val dropdwonCompany = dialogView.findViewById<AutoCompleteTextView>(R.id.company)
        dropdwonCompany.setAdapter(arrayAdaptor)
        val imgCompany = dialogView.findViewById<ImageView>(R.id.image)
        dialogView.findViewById<TextView>(R.id.code).visibility = View.GONE

        val  compId = viewModel.getUserCompany()
        var compIndex = this.companies.indexOfFirst { it.id == compId }

        if(compIndex >= 0){
            val compSelected = this.companies[compIndex]
            dropdwonCompany.setText(compSelected.name, false)
            dialogView.findViewById<TextView>(R.id.code).setText(compSelected.code)
            dialogView.findViewById<TextView>(R.id.address).setText(compSelected.address)
            viewModel.downloadCompanyImage(this.companies[compIndex].id, onSuccess = { bitmap->
                imgCompany.setImageBitmap(bitmap)
                imgCompany.visibility = View.VISIBLE
            }, onFailed = {msg->
                // imgCompany.setImageBitmap(null)
                imgCompany.visibility = View.GONE
            })
        }
        dropdwonCompany.setOnItemClickListener { parent, view, position, id ->
            val name = dropdwonCompany.text.toString()?:""
            compIndex= this.companies.indexOfFirst {it.name == name}
            if(compIndex < 0){
                return@setOnItemClickListener
            }
            val comp = this.companies[compIndex]
            dialogView.findViewById<TextView>(R.id.code).setText(comp.code)
            dialogView.findViewById<TextView>(R.id.address).setText(comp.address)
            // download retail store photo
            viewModel.downloadCompanyImage(comp.id, onSuccess = { bitmap->
                imgCompany.setImageBitmap(bitmap)
            }, onFailed = {msg->
                imgCompany.setImageBitmap(null)
            })
        }

        dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.confirm).setOnClickListener(){
            if(compIndex < 0){
                showToast(ApiNotification.COMPANY_SELECT_NAME)
                return@setOnClickListener
            }
            // check if the current location is around company
            val comp = this.companies[compIndex]
            if(!AppMacro.SIMULATE_GPS){ // check the current location
                var curLocation: LatLng? = null
                if(this.curLocation != null)
                    curLocation = LatLng(this.curLocation!!.latitude.toDouble(), this.curLocation!!.longitude.toDouble())
                else
                    this.requestLocationPermission(true)
                val locCheckRes = viewModel.checkIfAroundCompany(comp, curLocation)
                if(locCheckRes !is ApiResult.Success){
                    if(locCheckRes is ApiResult.Error){
                        this.showToast(locCheckRes.message)
                    }
                    return@setOnClickListener
                }
            }
            nodeAdaptor.clear()
            removeAllMarkers()
            removeAllPolylines()

            viewModel.startDeliverTaskFromCompany(comp,
                onSuccess = {msg->
                    showToast(msg)
                    if(AppMacro.SIMULATE_GPS){
                        this.virtGpsSensor!!.resetSimulator()
                    }
                },
                onFailed = {msg->showToast(msg)}
            )
            alertDialog.dismiss()
        }
        dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.cancel).setOnClickListener(){
            alertDialog.dismiss()
        }
        return alertDialog
    }

    private fun onAppStateUpdated(){
        ////AppLogger.d(TAG, "App state is changed")

        if(appState != null){
            if(nodeAdaptor.getItemCount() <= 0){ // node view items isn't updated yet
                entityUpdated = true
                updateMapCourse()
            }
        }
    }

    private fun controlMap(expand: Boolean){
        if(!expand){
            ////AppLogger.d(TAG, "collapse map")
            binding.layoutMap.visibility = View.GONE
            binding.iconControlMap.setImageResource(R.mipmap.maximize)
            mapExpanded = false
            this.viewPager.isUserInputEnabled = true

        }
        else{
            ////AppLogger.d(TAG, "Expand map")
            binding.layoutMap.visibility = View.VISIBLE
            binding.iconControlMap.setImageResource(R.mipmap.minimize)
            mapExpanded = true
            this.viewPager.isUserInputEnabled = false
            // this.updateMapCourse()
        }
    }

    private fun controlTagVeiwer(expand:Boolean){
        if(!expand){ // collapse
            binding.tagViewer.visibility = View.GONE
            binding.tagSeparator.visibility = View.GONE
            binding.iconControlCartmap.setImageResource(R.mipmap.maximize)
            tagExpanded = false
        }
        else{ // expand tag viewer, collapse map
            if(cartAdaptor.itemCount == 0){
                showToast( ApiNotification.CART_NO_LOADED)
                binding.iconControlCartmap.setImageResource(R.mipmap.minimize)
                return
            }
            binding.tagViewer.visibility = View.VISIBLE
            binding.tagSeparator.visibility = View.VISIBLE
            tagExpanded = true

            controlMap(false)
        }
        binding.iconControlCartmap.invalidate()
    }

    private fun onDeliverStateUpdated(){
        val stateLabels = requireContext().resources.getStringArray(R.array.app_status_list)
        val statusLabel = getDeliverStatusLabel(stateLabels, deliverState)
        binding.labelStatus.setText(statusLabel.first)
        when (statusLabel.second) {
            LED_STATUS.ACTIVE -> binding.ledStatus.setImageResource(R.drawable.menuselect)
            LED_STATUS.INACTIVE -> binding.ledStatus.setImageResource(R.drawable.menuselect_disable)
            LED_STATUS.COMPLETED -> binding.ledStatus.setImageResource(R.mipmap.menu_completed)
        }

        if(deliverState==null){
            AppLogger.e(TAG, "Invalid delivering state")
            // hide all operation
            binding.startTrip.visibility = View.VISIBLE
            binding.addPickup.visibility = View.GONE
            binding.addDelivery.visibility = View.GONE
        }
        else{
            when(deliverState!!.status  ){ // change status
                /*
                TRACK_STATUS.INIT.code->{ //
                    binding.startTrip.visibility = View.GONE
                    binding.addPickup.visibility = View.VISIBLE
                    binding.addDelivery.visibility = View.GONE
                    binding.addPickup.setText(resources.getString(R.string.load_option_name))
                }
                TRACK_STATUS.LOADING.code->{
                    binding.startTrip.visibility = View.GONE
                    binding.addPickup.visibility = View.VISIBLE
                    binding.addDelivery.visibility = View.GONE
                    binding.addPickup.setText(resources.getString(R.string.load_option_name))
                }
                TRACK_STATUS.PROGRESS.code->{
                    binding.startTrip.visibility = View.GONE
                    binding.addPickup.visibility = View.VISIBLE
                    binding.addDelivery.visibility = View.VISIBLE
                    binding.addPickup.setText(resources.getString(R.string.return_option_name))
                }
                TRACK_STATUS.DELIVERING.code->{
                    binding.startTrip.visibility = View.GONE
                    binding.addPickup.visibility = View.GONE
                    binding.addDelivery.visibility = View.VISIBLE
                    binding.addPickup.setText(resources.getString(R.string.return_option_name))
                }
                TRACK_STATUS.RETURNING.code->{
                    binding.startTrip.visibility = View.GONE
                    binding.addPickup.visibility = View.VISIBLE
                    binding.addDelivery.visibility = View.GONE
                    binding.addPickup.setText(resources.getString(R.string.return_option_name))
                }
                */
                TRACK_STATUS.COMPLETED.code->{
                    ////AppLogger.d(TAG, "Deliver task is completed")
                    binding.startTrip.visibility = View.VISIBLE
                    binding.addPickup.visibility = View.GONE
                    binding.addDelivery.visibility = View.GONE
                }
                else->{
                    binding.startTrip.visibility = View.GONE
                    binding.addPickup.visibility = View.GONE
                    binding.addDelivery.visibility = View.GONE
                }
            }
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startAndBindLocationService(){
        // Bind to the foreground service
        if(this.isGooglePlayServicesAvailable(requireContext())){
            AppLogger.d(TAG, "start location service now")
            if(isLocationServiceBound){
                AppLogger.d(TAG, "location service is already running")
                return
            }
            val context = requireContext()
            val serviceIntent = Intent(context, AppForegroundService::class.java)

            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        else{
            if(this.isLocationServiceBound){
                AppLogger.d(TAG, "Location service is already running")
                return
            }
            this.isLocationServiceBound = true
            if(this.locationManager == null) {
                this.locationManager =
                    requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            }
            this.locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                10f,
                locationListener
            )
        }
    }

    private fun stopLocationService(){
        if(!isLocationServiceBound){ // already location service isn't runing
            return
        }
        AppLogger.w(TAG, "Stop location service")
        requireContext().unbindService(serviceConnection)
        isLocationServiceBound = false
    }

    private fun checkLocationService(){
        AppLogger.d(TAG, "Launch location service")
        requestLocationPermission(true)
    }

    private fun requestLocationPermission(startService: Boolean) {
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
        else if(startService){
            AppLogger.d(TAG, "directly launch locations service because it has already permission")
            startAndBindLocationService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )== PackageManager.PERMISSION_GRANTED)) {
            this.startAndBindLocationService()
        }
    }

    private fun loadMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*
        val mapId = BuildConfig.GOOGLE_MAP_ID

        val mapOptions = GoogleMapOptions().mapId(mapId)

        // Replace the existing SupportMapFragment with one that uses GoogleMapOptions
        val mapFragment = SupportMapFragment.newInstance(mapOptions)

        // Add the map fragment to the FragmentManager
        childFragmentManager.beginTransaction()
            .replace(R.id.mapView, mapFragment)
            .commit()

        // Get the map asynchronously
        mapFragment.getMapAsync(this)
         */

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.isMapReady = true
        initMapProperties()
        AppLogger.d(TAG, "Map is prepared")
    }

    private fun initMapProperties() {
        try {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // apply custom map style
                googleMap!!.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(), R.raw.map_style_custom_1
                    )
                )
                // set snap window adaptor
                googleMap!!.setInfoWindowAdapter(MarDcWindowAdaptor(requireContext()))
                googleMap!!.setOnMarkerClickListener { marker ->
                    // Handle the marker click event
                    marker.showInfoWindow() // Show the info window when the marker is clicked
                    true // Return true if the click event is consumed
                }

                googleMap!!.isMyLocationEnabled = false
                googleMap!!.uiSettings.isMyLocationButtonEnabled = true
                googleMap!!.uiSettings.isZoomControlsEnabled = true

            } else {
                googleMap!!.isMyLocationEnabled = false
                googleMap!!.uiSettings.isMyLocationButtonEnabled = false
                // requestLocationPermission()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun createGeoApiContext(): GeoApiContext {
        val apiKey = BuildConfig.GOOGLE_MAP_API
        // val apiKey = requireContext().getString(R.string.google_maps_key)
        return GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()
    }
    private fun getTruckIconResource(): Int{
        val angles = intArrayOf(20, 40, 50, 75, 105, 127, 142, 165, 195, 217, 232, 255, 275, 307, 322,345 )
        val ids = intArrayOf(
            R.mipmap.truck_series_0,
            R.mipmap.truck_series_30,
            R.mipmap.truck_series_45,
            R.mipmap.truck_series_60,
            R.mipmap.truck_series_90,
            R.mipmap.truck_series_120,
            R.mipmap.truck_series_135,
            R.mipmap.truck_series_150,
            R.mipmap.truck_series_180,
            R.mipmap.truck_series_210,
            R.mipmap.truck_series_225,
            R.mipmap.truck_series_240,
            R.mipmap.truck_series_270,
            R.mipmap.truck_series_300,
            R.mipmap.truck_series_315,
            R.mipmap.truck_series_330,
        )
        ////AppLogger.d(TAG, "checking the resource of trip angle marker. Angule: $tripDirection")
        for(i in 0..14){
            if(tripDirection > angles[i] && tripDirection <= angles[i + 1]) {
                return ids[i + 1]
            }
        }
        return ids[0]
    }

    private fun updateLocationOnMap() {
        AppLogger.d(TAG, "Update the current location of map")
        try {

            if (this.curLocation != null) {
                if (!isAdded || context == null) {
                    //AppLogger.w(TAG, "fragment isn't attached to a context yet")
                    return
                }
                val currentLatLng = LatLng(this.curLocation!!.latitude, this.curLocation!!.longitude)
                val directionBitmap = BitmapFactory.decodeResource(requireContext().resources, getTruckIconResource())
                if(currentLocationMarker == null){
                    AppLogger.d(TAG, "Create new current location mark")
                    curMarkerOption = MarkerOptions()
                        .position(currentLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(directionBitmap))
                    currentLocationMarker = googleMap!!.addMarker(curMarkerOption!!)
                }
                else{
                    AppLogger.d(TAG, "update the position of current location mark")
                    currentLocationMarker?.position = currentLatLng
                    currentLocationMarker?.setIcon(BitmapDescriptorFactory.fromBitmap(directionBitmap))
                }
                // Get current location and add it to the polyline path
                if(!viewModel.trackingIsCompleted()){ // show polyline only when task is in progress
                    if (polyline == null) {
                        polyline = googleMap!!.addPolyline(
                            PolylineOptions()
                                .add(currentLatLng)        // Add the initial point
                                .color(ContextCompat.getColor(requireContext(), R.color.route_reached))         // Set the polyline color
                                .width(10f)                 // Set the polyline width
                                .zIndex(3f)                // Set the z-index for layering
                                .geodesic(true)            // Optional: Follow the curvature of the earth
                        )
                    } else {
                        polyline?.points = polyline?.points?.apply {add(currentLatLng)}!!
                    }
                }

                // Move the camera to the current location
                if(!mapScaleInited)
                {
                    ////AppLogger.d(TAG, "Set the initial map scale")
                    googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, AppMacro.MAP_INIT_SCALE))
                    mapScaleInited = true
                }
                else{
                    ////AppLogger.d(TAG, "change the map view position after initializing")
                    googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                }
            }
            else{
                AppLogger.d(TAG, "Invalid current location")
            }
        } catch (e: SecurityException) {
            ////AppLogger.d(TAG, "draw current position-5")
            e.printStackTrace()
        }
    }

    private fun createMapMarkBitmap(resourdID: Int, bearing: Float = 0.0f): Bitmap {
        // Create a bitmap from the drawable resource
        val drawable = ContextCompat.getDrawable(requireContext(), resourdID)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        // Rotate the bitmap according to the bearing
        val matrix = Matrix()
        matrix.postRotate(bearing)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun isFragmentActive(): Boolean {
        return isActive
    }

    private fun shouldDrawMap(): Boolean {
        if (!isMapReady             // map isn't ready yet
            || !isFragmentActive()  // fragment is invisible now
            || !mapExpanded         // map is collapsed now
        ) {
            return false
        }
        return true
    }

    private fun updateMapCourse(){
        ////AppLogger.d(TAG, "update the map course")
        //if(!shouldDrawMap()||!entityUpdated) { // no need to draw map
        if(!entityUpdated) { // no need to draw map
            return
        }
        // draw route curve
        drawEntityOfRoute( geoConext)
        entityUpdated = false

    }
    // draw the entities on map(no current location)
    private fun drawEntityOfRoute(geoApiContext: GeoApiContext) {
        if(this.googleMap == null){
            AppLogger.w(TAG, "Can't update map. Google map isn't initialized.")
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val items: List<DeliverEntity> = nodeAdaptor.getItems()
            removeAllMarkers()
            if (items.isEmpty()) {
                return@launch
            }
            //var mapComponents: MapRouteComponents? = null
            if(AppMacro.SHOW_PLAN_COURSE) {
                // find the expected course from items
                var mapComponents = withContext(Dispatchers.Default){
                    determineMapRoutins(items.toMutableList())
                }
                // draw reached components
                if (mapComponents.reached.isNotEmpty()) {
                    drawDrivingRoute(
                        mapComponents.reached, geoApiContext,
                        ContextCompat.getColor(requireContext(), R.color.route_reached), 3.0f
                    )
                }
                if (mapComponents.unreached.isNotEmpty()) {
                    drawDrivingRoute(
                        mapComponents.unreached, geoApiContext,
                        ContextCompat.getColor(requireContext(), R.color.route_unreached), 2.0f
                    )
                }
                if (mapComponents.returns.isNotEmpty()) {
                    drawDrivingRoute(
                        mapComponents.returns, geoApiContext,
                        ContextCompat.getColor(requireContext(), R.color.route_return), 1.0f
                    )
                }
                ////AppLogger.d(TAG,"route course information: ${mapComponents.reached}, ${mapComponents.unreached}, ${mapComponents.returns}")
            }

            // Add markers for each location
            val bitmapReached = createMapMarkBitmap(R.drawable.gps_marker_reached)
            val bitmapUnreached = createMapMarkBitmap(R.drawable.gps_marker_unreached)

            items.forEachIndexed { index, item ->
                val latLng = getLatLng(item)
                val markerOptions = MarkerOptions().position(latLng)

                when(index) {
                    0 -> {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(ApiNotification.START)
                    }
                    else -> {
                        if(item is DeliverEntity.distCenter){ // last DC item of course
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapReached))
                        }
                        else if(item is DeliverEntity.company){ // last DC item of course
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapReached))
                        }
                        else if(item is DeliverEntity.store){ // it doesn't include the last DC item
                            val status = item.store.status
                            if(status == NODE_STATUS.NO_DELIVERED.code){
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapUnreached))
                            }else {
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapReached))
                            }
                        }
                    }
                }
                val mk = googleMap!!.addMarker(markerOptions)
                if(mk!=null){
                    mk.tag = item
                    mk.let { markerList.add(it) }

                }
            }
            // set the scale initialy
            if(!mapScaleInited && markerList.size > 0){
                val markerPosition = markerList[markerList.size - 1].position
                googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, AppMacro.MAP_INIT_SCALE))
                ////AppLogger.d(TAG, "init the zoom of map: $markerPosition, ${AppMacro.MAP_INIT_SCALE}")
                mapScaleInited = true
            }
            // if(mapComponents != null)
            //     googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mapComponents.bound!!, 100))
        }
    }



    private fun removeAllMarkers(){
        ////AppLogger.d(TAG, "Remove all markers on map: ${markerList.size}")
        markerList.forEach { marker ->
            marker.remove()
        }
        // Clear the marker list
        markerList.clear()
        // remove the polyline of map
    }

    private fun removeAllPolylines(){
        polyline?.remove()
        polyline = null
    }

    private fun determineMapRoutins(items: MutableList<DeliverEntity>): MapRouteComponents {
        if (items.size <= 0) { // no elements
            return MapRouteComponents()
        }
        val reachedLatLngs = mutableListOf<LatLng>()
        val unreachedLatLngs = mutableListOf<LatLng>()
        val returnLatLngs = mutableListOf<LatLng>()

        var lastItem: DeliverEntity? = null
        items.forEachIndexed{index, item->
            if(index != 0 && item is DeliverEntity.distCenter){
                lastItem = item.copy()
                return@forEachIndexed
            }
        }
        if(lastItem != null){
            items.remove(lastItem)
        }
        else
            lastItem = items[0]


        items.forEach {
            when (it) {
                is DeliverEntity.company ->{
                    val latLng = LatLng(it.cmp.latitude.toDouble(), it.cmp.longitude.toDouble())
                    if (it.cmp.status == NODE_STATUS.REACHED.code || it.cmp.status == NODE_STATUS.LEAVE.code) {
                        reachedLatLngs.add(latLng)
                    } else {
                        unreachedLatLngs.add(latLng)
                    }
                }
                is DeliverEntity.distCenter -> {
                    val latLng = LatLng(it.dc.latitude.toDouble(), it.dc.longitude.toDouble())
                    if (it.dc.status == NODE_STATUS.REACHED.code || it.dc.status == NODE_STATUS.LEAVE.code) {
                        reachedLatLngs.add(latLng)
                    } else {
                        unreachedLatLngs.add(latLng)
                    }
                }

                is DeliverEntity.store -> {
                    val latLng = LatLng(it.store.latitude.toDouble(), it.store.longitude.toDouble())
                    if(it.store.status == NODE_STATUS.NO_DELIVERED.code){
                        unreachedLatLngs.add(latLng)
                    }
                    else {
                        reachedLatLngs.add(latLng)
                    }
                }
            }
        }
        if (reachedLatLngs.size >= 1) {// add last node of reached items, and perform optimization
            unreachedLatLngs.add(0, reachedLatLngs.last())
        }
        val optimizedUnreachedLatLngs = optimizeCourse(unreachedLatLngs)
        if (optimizedUnreachedLatLngs.size > 0) // there is unreached items
            returnLatLngs.add(optimizedUnreachedLatLngs.last())
        else // no unreached items
            returnLatLngs.add(reachedLatLngs.last())
        returnLatLngs.add(getLatLng(lastItem!!))

        val boundsBuilder = LatLngBounds.Builder()
        reachedLatLngs.forEach { boundsBuilder.include(it) }
        unreachedLatLngs.forEach { boundsBuilder.include(it) }
        // curLocation?.let { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        return MapRouteComponents(reachedLatLngs, optimizedUnreachedLatLngs, returnLatLngs, bounds)
    }

    private fun drawDrivingRoute(locations: List<LatLng>, geoApiContext: GeoApiContext, color: Int, zIndex: Float) {
        for (i in 0 until locations.size - 1) {
            val origin = locations[i]
            val destination = locations[i + 1]

            val directionsResult: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .await()

            val path = mutableListOf<LatLng>()
            directionsResult.routes[0].legs.forEach { leg ->
                leg.steps.forEach { step ->
                    val decodedPath = PolyUtil.decode(step.polyline.encodedPath)
                    path.addAll(decodedPath)
                }
            }

            val polylineOptions = PolylineOptions().addAll(path)
                .color(color)
                .width(10f)
                .zIndex(1.0f)

            googleMap!!.addPolyline(polylineOptions)
        }
    }

    private fun optimizeCourse(unreachedLatLngs: List<LatLng>): MutableList<LatLng> {
        if (unreachedLatLngs.isEmpty())
            return unreachedLatLngs.toMutableList()

        val optimizedLatLngs = mutableListOf<LatLng>()
        val remainingLatLngs = unreachedLatLngs.toMutableList()

        // Start with the first location
        optimizedLatLngs.add(remainingLatLngs.removeAt(0))

        while (remainingLatLngs.isNotEmpty()) {
            val lastLatLng = optimizedLatLngs.last()
            val nearestLatLng = remainingLatLngs.minByOrNull { SphericalUtil.computeDistanceBetween(lastLatLng, it) }
            optimizedLatLngs.add(nearestLatLng!!)
            remainingLatLngs.remove(nearestLatLng)
        }
        return optimizedLatLngs.toMutableList()
    }

    // Helper function to draw a driving route on the map using Google Directions API
    private fun drawDrivingRoute(map: GoogleMap, geoApiContext: GeoApiContext, startPoint: LatLng, endPoint: LatLng, color: Int) {
        val directions = DirectionsApi.newRequest(geoApiContext)
            .mode(TravelMode.DRIVING)
            .origin(com.google.maps.model.LatLng(startPoint.latitude, startPoint.longitude))
            .destination(com.google.maps.model.LatLng(endPoint.latitude, endPoint.longitude))
            .await()

        val route = directions.routes.firstOrNull()
        route?.let {
            val decodedPath = PolylineOptions()
                .color(color)
                .width(10f)

            it.legs.forEach { leg ->
                leg.steps.forEach { step ->
                    decodedPath.addAll(PolyUtil.decode(step.polyline.encodedPath))
                }
            }
            map.addPolyline(decodedPath)
        }
    }

    // Helper function to draw markers on the map
    private fun drawMarker(map: GoogleMap, point: LatLng, isReachedOrLeft: Boolean) {
        val markerColor = if (isReachedOrLeft) BitmapDescriptorFactory.HUE_BLUE else BitmapDescriptorFactory.HUE_RED
        map.addMarker(MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(markerColor)))
    }

    // Helper function to adjust camera to show all points
    private fun adjustCameraToBounds(map: GoogleMap, points: List<LatLng>) {
        val boundsBuilder = LatLngBounds.Builder()
        for (point in points) {
            boundsBuilder.include(point)
        }
        val bounds = boundsBuilder.build()
        val padding = 100 // Padding around the edges of the map
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.animateCamera(cu)
    }


    // Helper function to get LatLng from DeliverEntity
    fun getLatLng(entity: DeliverEntity): LatLng {
        return when (entity) {
            is DeliverEntity.distCenter -> LatLng(entity.dc.latitude.toDouble(), entity.dc.longitude.toDouble())
            is DeliverEntity.store -> LatLng(entity.store.latitude.toDouble(), entity.store.longitude.toDouble())
            is DeliverEntity.company -> LatLng(entity.cmp.latitude.toDouble(), entity.cmp.longitude.toDouble())
        }
    }

    // Helper function to draw a polyline on the map
    private fun drawPolyline(map: GoogleMap, points: List<LatLng>, color: Int) {
        val polylineOptions = PolylineOptions().addAll(points).color(color).width(10f)
        map.addPolyline(polylineOptions)
    }

    // Helper function to find the shortest path between entities (if needed)
    private fun findShortestPath(entities: List<DeliverEntity>, startPoint: LatLng): List<DeliverEntity> {
        if (entities.isEmpty()) return emptyList()

        // A basic implementation for finding the shortest path between entities
        // You can replace this with a more sophisticated algorithm like the Traveling Salesman Problem solver if needed
        val remainingEntities = entities.toMutableList()
        val orderedEntities = mutableListOf<DeliverEntity>()

        var currentPoint = startPoint
        while (remainingEntities.isNotEmpty()) {
            val nearestEntity = remainingEntities.minByOrNull { entity -> calculateDistance(currentPoint, getLatLng(entity)) }!!
            orderedEntities.add(nearestEntity)
            remainingEntities.remove(nearestEntity)
            currentPoint = getLatLng(nearestEntity)
        }

        return orderedEntities
    }

    // Helper function to calculate the distance between two LatLng points
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, results)
        return results[0].toDouble()
    }

    // draw course on google map
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // send GPS location into server
    private fun callbackSendLocation(location: Location) {

        val latitude = location.latitude
        val longitude = location.longitude

        if(latitude.isNaN() || longitude.isNaN()){
            AppLogger.w(TAG, "GPS location is invalid")
            return
        }
        viewModel.pushGpsLocation(latitude, longitude,
            onSuccess = {_-> },
            onFailed = {message->
                AppLogger.w(TAG, "Failed to push gps location $message")
            })

    }

    private fun updateGreetingText() {
        val greetings: Array<String> = requireContext().resources.getStringArray(R.array.home_greetings)
        binding.labelGreet.text = getGreetingBasedOnTime(greetings)
    }

    private fun callbackNodeItemClicked(node: DeliverEntity):Unit{
        if(AppMacro.NODE_CLICK) {
            when (node) {
                is DeliverEntity.distCenter -> {
                    if (node.dc.status != NODE_STATUS.LEAVE.code) { // clicked the last distribution center
                        val intent = Intent(requireContext(), AddPickupActivity::class.java)
                        intent.putExtra(AppMacro.SELECTED_DC, node.dc.id)
                        addPickupLauncher.launch(intent)
                    } else { // collapse map and tag viewer
                        controlMap(false)
                        controlTagVeiwer(false)
                    }
                }

                is DeliverEntity.company -> {
                    if (node.cmp.status != NODE_STATUS.LEAVE.code) { // clicked the last distribution center
                        val intent = Intent(requireContext(), AddPickupActivity::class.java)
                        intent.putExtra(AppMacro.SELECTED_DC, node.cmp.id)
                        addPickupLauncher.launch(intent)
                    } else { // collapse map and tag viewer
                        controlMap(false)
                        controlTagVeiwer(false)
                    }
                }

                is DeliverEntity.store -> {
                    if (deliverState?.status == TRACK_STATUS.PROGRESS.code || deliverState?.status == TRACK_STATUS.DELIVERING.code) {
                        if (node.store.status != NODE_STATUS.LEAVE.code) { // clicked the last retail store
                            val intent = Intent(requireContext(), DeliverCartActivity::class.java)
                            intent.putExtra(AppMacro.SELECTED_RS, node.store.id)
                            startActivity(intent)
                        }
                    } else {
                        controlMap(false)
                        controlTagVeiwer(false)
                    }
                }
            }
        }
        else{
            AppLogger.d(TAG, "Disable node item click")
            return
        }

    }

    private fun callbackCartItemClicked(cart: Cart): Unit{
        AppLogger.d(TAG, "Cart Item clicked: $cart")
    }

    private fun onReturnFromAddPickup() {
        AppLogger.d(TAG, "Pickup window is completed")
    }

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager

    }

    override fun onResume() {
        super.onResume()
        if (!isLocationServiceBound && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            AppLogger.d(TAG, "Start location service when resum activity")
            startAndBindLocationService()
        }
        
        if (googleMap == null) {
            loadMap()
        }
        else{
            initMapProperties()
        }
        handler.post(updateGreetingRunnable)
        isActive = true
        mapScaleInited = false
        //mapExpanded = false
        // binding.layoutMap.visibility = View.GONE
        this.viewPager.isUserInputEnabled = !mapExpanded
        tagExpanded = false
        binding.tagViewer.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateGreetingRunnable)
        isActive = true
        this.viewPager.isUserInputEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateGreetingRunnable)
        handler.removeCallbacks(checkLocPermissionRunnable)
        // Unbind the service when the fragment is destroyed
        if (isLocationServiceBound) {
            requireContext().unbindService(serviceConnection)
            isLocationServiceBound = false
        }
        _binding = null
        deliverState = null;
        appState = null;
        googleMap = null;
        curLocation = null;
        currentLocationMarker = null;
        curMarkerOption = null;
        isMapReady = false;
        mapScaleInited =false;
    }

    private fun showToast( message: String, durationInMillis: Long = 5000) {
        if (!isAdded) return

        Handler(Looper.getMainLooper()).post {
            val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({
                toast.cancel()
            }, durationInMillis)
        }

    }

}
