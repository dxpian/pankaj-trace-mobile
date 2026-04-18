package myandroid.app.hhobzic.inventorymobile.home.cart

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import myandroid.app.hhobzic.inventorymobile.CustomScannerActivity
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.Store
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentViewChecklistBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.api.model.Cart
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import myandroid.app.hhobzic.inventorymobile.utility.AppState
import java.io.IOException

class VIewChecklistFragment : Fragment() {
    private val TAG = "VIewChecklistFragment"
    private var _binding: FragmentViewChecklistBinding? = null
    private val binding get() = _binding!!
    private var isScanning = false
    private val requestCodeCameraPermission = 1001
    private var scannedValue = ""
    private val REQUEST_CAMERA_PERMISSION = 201
    private var surfaceView: SurfaceView? = null
    private var cameraSource: CameraSource? = null
    private var cartsLoaded: List<Cart> = listOf()        // total cart information
    // private var cartsInDc: List<Cart> = listOf()          // total carts in DC to load

    private var barcodeDetector: BarcodeDetector? = null
    private var isInnerDialogShown = false
    private var gifView: ImageView? = null
    private lateinit var viewModel: AppStatusSharedViewModel
    private var appState:AppState? = null
    private var deliverState: DeliveryState?=null
    private var itemTotal = 0
    private var itemEmpty = 0                                       // count for empty cart
    private var itemNoEmpty = 0                                      // count for loaded cart
    private var _dcs: List<DistCenter> = emptyList()
    private var _rss: MutableList<Store> = mutableListOf()
    private var _rssSelected:MutableList<Store> = mutableListOf()   // stores added to trip

    private var dialogLoadCart: View?=null
    private var dialogUnloadCart: View?=null
    private  var optionColorEnabled: Int = Color.BLACK
    private  var optionColorDisabled: Int = 0
    private var viewPager: ViewPager2? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        optionColorDisabled = ContextCompat.getColor(requireContext(), R.color.disabledText)

        _binding = FragmentViewChecklistBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)
        // listner for total loaded item count
        viewModel.cartsCount.observe(viewLifecycleOwner, Observer { count->
            binding.checklistSize.text = getString(R.string.cart_count, count)
            if(dialogLoadCart!=null){
                val viewItemCount = dialogLoadCart!!.findViewById<TextView>(R.id.label_notification)
                viewItemCount.text = getString(R.string.cart_count, count)
            }
            if(dialogUnloadCart!=null){
                val viewItemCount = dialogUnloadCart!!.findViewById<TextView>(R.id.label_notification)
                viewItemCount.text = getString(R.string.cart_count, count)
            }
        })
        // listener for total empty loaded item count
        viewModel.cartsCountEmpty.observe(viewLifecycleOwner, Observer {
            count-> itemEmpty = count })
        // listener for total non-empty loaded item count
        viewModel.cartsCountLoaded.observe(viewLifecycleOwner, Observer { count->
            itemNoEmpty = count
        })
        viewModel.cartsCount.observe(viewLifecycleOwner, Observer { count->
            itemTotal = count
            if(count > 0){
                binding.imgLoadCarts.setImageResource(R.drawable.checked)
                binding.imgReturnCarts.setImageResource(R.drawable.checked)
            }
            else{
                binding.imgLoadCarts.setImageResource(R.drawable.khalicheckbox)
                binding.imgLoadCarts.setImageResource(R.drawable.khalicheckbox)
            }
            if(canStartJourney())
                binding.imgStartJourney.setImageResource(R.drawable.checked)
            else
                binding.imgStartJourney.setImageResource(R.drawable.khalicheckbox)
        })

        // listener for loaded item changed
        viewModel.carts.observe(viewLifecycleOwner, Observer { carts:MutableList<Cart>->
            cartsLoaded = carts.toList()
            if(dialogUnloadCart!= null){
                val dropdownTag = dialogUnloadCart!!.findViewById<AutoCompleteTextView>(R.id.spinner_tag)
                val tagList = cartsLoaded.map{it.iot}
                val adaptor = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, tagList)
                dropdownTag.setAdapter(adaptor)
                val viewProduct = dialogUnloadCart!!.findViewById<TextView>(R.id.product_desc)
                if(cartsLoaded.size > 0){
                    dropdownTag.setText(cartsLoaded[0].iot, false)
                    viewProduct.setText(cartsLoaded[0].product)
                }
                else{
                    dropdownTag.setText("", false)
                    viewProduct.setText("")
                }
            }
        })
        // listener for DC trucks are changed

        // listener for truck state is changed
        viewModel.loadingDeliverAppState.observe(viewLifecycleOwner, Observer { state->
            appState = state;
            if(state == null){
                Log.e(TAG, "Invalid loading state posting value")
                return@Observer
            }
            onChangeAppState()
        })

        viewModel.loadingDeliverState.observe(viewLifecycleOwner, Observer { state->
            deliverState = state
            onChangeDeliverState()
        })

        // listener for distcenter list are fetched
        viewModel.distcenters.observe(viewLifecycleOwner, Observer { data-> _dcs = data })
        // listener for retail store list are fetched
        viewModel.retailstores.observe(viewLifecycleOwner, Observer { data-> _rss = data.toMutableList() })
        viewModel.refreshJourneyInformation(onSuccess = { msg->
            Log.d(TAG, "Success to load DC and RS information")                                                        },
            onFailed = {msg->
                showToast(msg)
        })
        deliverState = viewModel.getDeliverState()
        appState = viewModel.getAppState()
        // select distribution center
        binding.selectDist.setOnClickListener(){
            //Log.d(TAG, "Select distribution center event handler")
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_dc, null)

            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true) // Allow the dialog to be canceled
            // Create the AlertDialog
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
            var imgDc = dialogView.findViewById<ImageView>(R.id.dcImage)
            val dc_names = _dcs.map { it.name }
            val arrayAdaptor = ArrayAdapter(requireContext(),
                R.layout.dropdown_autocomplete, dc_names)
            val dropdown_dc = dialogView.findViewById<AutoCompleteTextView>(R.id.autocompleteDc)
            dropdown_dc.setAdapter(arrayAdaptor)
            //Log.d(TAG, "current initial distribution center name: ${appState?.dc_selected}")
            var dcSelected = appState?.dc_selected
            if(dcSelected != null){
                // set name
                dropdown_dc.setText(appState?.dc_selected?.name, false)
                dialogView.findViewById<TextView>(R.id.distDesc).setText(dcSelected.address)
                dialogView.findViewById<TextView>(R.id.distCode).setText(dcSelected.code)
                // download image of dc
                viewModel.downloadDistImage(dcSelected.id,
                    onSuccess = {bitmap->
                        imgDc.setImageBitmap(bitmap)
                    },
                    onFailed = {msg->showNotification(msg)}
                )
            }
            dropdown_dc.setOnItemClickListener { parent, view, position, id ->
                val dcname = dropdown_dc.text.toString()?:""
                val index = _dcs.indexOfFirst {it.name == dcname}
                if(index < 0){
                    return@setOnItemClickListener
                }
                dcSelected = _dcs[index]
                dialogView.findViewById<TextView>(R.id.distDesc).setText(dcSelected!!.address)
                dialogView.findViewById<TextView>(R.id.distCode).setText(dcSelected!!.code)

                dropdown_dc.isEnabled = false
                viewModel.downloadDistImage(dcSelected!!.id,
                    onSuccess = {bitmap->
                        imgDc.setImageBitmap(bitmap)
                        dropdown_dc.isEnabled = true
                    },
                    onFailed = {
                        msg->showNotification(msg)
                        dropdown_dc.isEnabled = true
                    }
                )
            }

            // Set onClickListener for the buttons
            val btnConfirm = dialogView.findViewById<Button>(R.id.confirmLoad)
            btnConfirm.setOnClickListener() {
                btnConfirm.isEnabled = false
                val dcname = dropdown_dc.text.toString()?:""
                val index = _dcs.indexOfFirst {it.name == dcname}
                if(index < 0){
                    this.showToast(ApiNotification.REQUIRE_VALID_DC_SELECT)
                    btnConfirm.isEnabled = true
                    return@setOnClickListener
                }

                viewModel.arriveDistributionCenter(
                    _dcs[index],
                    onSuccess = {msg->
                        showToast(msg)
                        alertDialog.dismiss()
                    },
                    onFailed = {msg->
                        showToast(msg)
                        btnConfirm.isEnabled = true}
                )
            }
            dialogView.findViewById<Button>(R.id.confirmCancel).setOnClickListener() {
                alertDialog.dismiss() // Dismiss the dialog after scanning
            }

            // Show the AlertDialog
            alertDialog.show()
        }

        binding.loadCart.setOnClickListener(){
            if(appState?.dc_selected == null){
                showToast(ApiNotification.DC_REQUIRE_CONFIRM)
                return@setOnClickListener
            }
            val dialog = openLoadCartDialog()
            dialog.show()
        }

        binding.addStore.setOnClickListener(){
            if(appState?.dc_selected == null){
                showToast(ApiNotification.DC_REQUIRE_CONFIRM)
                return@setOnClickListener
            }
            val alertDialog = openAddStoreDialog()
            alertDialog.show()
        }
        binding.returnCart.setOnClickListener(){
            if(appState?.dc_selected == null){
                showToast(ApiNotification.DC_REQUIRE_CONFIRM)
                return@setOnClickListener
            }
            if(itemTotal <= 0){
                showToast(ApiNotification.DELIVER_NO_CART_RETURN)
                return@setOnClickListener
            }
            val dialog = openUnloadCartDialog()
            dialog.show()
        }
        binding.startTrip.setOnClickListener(){
            if(!canStartJourney()){ // check if delivering is ready
                if(appState?.dc_selected == null)
                    showToast(ApiNotification.DC_REQUIRE_CONFIRM)
//                if(appState?.rss.isNullOrEmpty())
//                    showToast(ApiNotification.STORE_REQUIRE_CONFIRM)
//                if(cartsLoaded.isEmpty())
//                    showToast(ApiNotification.CART_NO_LOADED)
                return@setOnClickListener
            }

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_start, null)
            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true) // Allow the dialog to be canceled

            // Create the AlertDialog
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
            val dcSelected = appState?.dc_selected
            if(dcSelected!= null){
                dialogView.findViewById<TextView>(R.id.distCode).setText(dcSelected.code)
                dialogView.findViewById<TextView>(R.id.distName).setText(dcSelected.name)
            }

            // Set onClickListener for the buttons
            dialogView.findViewById<ImageView>(R.id.go).setOnClickListener() {
                viewModel.startDeliverTask(onSuccess = {result->
                    if(result is ApiResult.Success)
                        showToast(result.data)
                }, onFailed = {message->
                    showToast(message)
                })
                alertDialog.dismiss()
            }

            dialogView.findViewById<TextView>(R.id.backButton).setOnClickListener() {
                alertDialog.dismiss()
            }

            // Show the AlertDialog
            alertDialog.show()
        }
        binding.back.setOnClickListener(){
            val adapter = viewPager?.adapter as ViewPagerAdapter
            if(adapter != null)
                adapter.openHomepage()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun onChangeAppState(){
        if(deliverState == null){
            //Log.d(TAG, "Invalie delivering state")
            return
        }
        if(deliverState!!.status  <= TRACK_STATUS.LOADING.code){ // loading now
            if(appState!!.dc_selected == null){
                binding.imgSelectDcState.setImageResource(R.drawable.khalicheckbox)
            }
            else{
                binding.imgSelectDcState.setImageResource(R.drawable.checked)
            }
            // check store
            if(appState!!.rss.isEmpty()){
                binding.imgSelectRsStatus.setImageResource(R.drawable.khalicheckbox)
                _rssSelected.clear()
            }
            else{
                binding.imgSelectRsStatus.setImageResource(R.drawable.checked)
                _rssSelected = appState!!.rss.map { store -> store.copy() }.toMutableList()
                //Log.d(TAG, "RSS of store is updated")
            }

            if(!cartsLoaded.isNullOrEmpty()){
                binding.imgLoadCarts.setImageResource(R.drawable.checked)
            }
            else{
                binding.imgLoadCarts.setImageResource(R.drawable.khalicheckbox)
            }
            if(cartsLoaded.isNotEmpty()){
                binding.imgReturnCarts.setImageResource(R.drawable.checked)
            }
            else{
                binding.imgLoadCarts.setImageResource(R.drawable.khalicheckbox)
            }
            if(canStartJourney())
                binding.imgStartJourney.setImageResource(R.drawable.checked)
            else
                binding.imgStartJourney.setImageResource(R.drawable.khalicheckbox)

        }
        else{ // delivering task is already started
            binding.imgSelectDcState.setImageResource(R.drawable.checked)
            binding.imgSelectRsStatus.setImageResource(R.drawable.checked)
            binding.imgLoadCarts.setImageResource(R.drawable.checked)
            binding.imgReturnCarts.setImageResource(R.drawable.checked)
            binding.imgStartJourney.setImageResource(R.drawable.checked)
        }
    }

    private fun onChangeDeliverState(){
        if(deliverState == null){
            //Log.d(TAG, "Invalid delivering state")
            return
        }
        val status = deliverState!!.status
        if(status == TRACK_STATUS.LOADING.code || status == TRACK_STATUS.INIT.code){ // enable to load
            enableSelectDc(true)
            enableSelectStore(true)
            enableLoadCart(true)
            enableReturnCart(true)
            enableStartJourney(true)
        }
        if(status == TRACK_STATUS.DELIVERING.code
            || status == TRACK_STATUS.PROGRESS.code
            || status == TRACK_STATUS.RETURNING.code){
            enableSelectDc(false)
            enableSelectStore(false)
            enableLoadCart(false)
            enableReturnCart(true)
            enableStartJourney(false)
        }
    }

    private fun enableSelectDc(enable: Boolean){
        val backColor = if(enable) optionColorEnabled else optionColorDisabled
        //Log.d(TAG, "Select distribution center: $enable")
        binding.selectDist.isEnabled = enable
        binding.textSelectDc.setTextColor(backColor)
    }
    private fun enableSelectStore(enable: Boolean){
        val backColor = if(enable) optionColorEnabled else optionColorDisabled
        binding.addStore.isEnabled = enable
        binding.textSelectStore.setTextColor(backColor)

    }
    private fun enableLoadCart(enable: Boolean){
        val backColor = if(enable) optionColorEnabled else optionColorDisabled
        binding.loadCart.isEnabled = enable
        binding.textLoadCart.setTextColor(backColor)
    }
    private fun enableReturnCart(enable: Boolean){
        val backColor = if(enable) optionColorEnabled else optionColorDisabled
        binding.returnCart.isEnabled = enable
        binding.textReturnCart.setTextColor(backColor)
    }

    private fun enableStartJourney(enable: Boolean){
        val backColor = if(enable) optionColorEnabled else optionColorDisabled
        binding.startTrip.isEnabled = enable
        binding.textStartTrip.setTextColor(backColor)
    }



    private fun canStartJourney():Boolean{
        var startJoureny = true
        // if(appState == null || appState?.dc_selected == null || appState?.rss.isNullOrEmpty() || cartsLoaded.isEmpty())
        if(appState == null || appState?.dc_selected == null)
            startJoureny = false
        return startJoureny
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(qrCodeText: String) {
        val channelId = "QR_CODE_CHANNEL"
        val channelName = "QR Code Scanner"

        // Create the NotificationChannel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your app's icon
            .setContentTitle("QR Code Detected")
            .setContentText(qrCodeText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.notify(1, builder.build())
    }


    private fun startCameraSource() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraSource!!.start(surfaceView!!.holder)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // startCameraSource()
                openQRScanner()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                showQrScannerResult(result.contents)
                showNotification(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openQRScanner() {
        //Log.d(TAG, "Open custome QR Scanner")
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR code")
        integrator.setCameraId(0) // Use a specific camera of the device
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.captureActivity = CustomScannerActivity::class.java // Specify the custom activity
        integrator.initiateScan()
    }

    private fun showQrScannerResult(qrCodeText: String) {
        //Log.d(TAG, "QR code scanning result: $qrCodeText")
        if(dialogLoadCart!= null){
            val dropdownTag = dialogLoadCart!!.findViewById<TextView>(R.id.iot_tag)
            dropdownTag.text = qrCodeText
            // val cart = cartsInDc.find { it.iot==qrCodeText}
            dialogLoadCart!!.findViewById<TextView>(R.id.product_desc).setText("")
        }
        else if(dialogUnloadCart!= null){
            val dropdownTag = dialogUnloadCart!!.findViewById<AutoCompleteTextView>(R.id.spinner_tag)
            dropdownTag.setText(qrCodeText, false)
            val cart = cartsLoaded.find { it.iot==qrCodeText}
            dialogUnloadCart!!.findViewById<TextView>(R.id.product_desc).setText(cart?.product)
        }
    }

    private fun generateQRCodeBitmap(qrCodeText: String): Bitmap {
        val width = 300
        val height = 300
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrCodeText, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /* open AddCart Dialog
    params:
        CartInputData: Cart data used recently
        listener: callback interface to add item
    */
    private fun openLoadCartDialog(): AlertDialog
    {
        val TAG = "createAddCartDialog"
        dialogLoadCart = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_addcart, null)

        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogLoadCart)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)

        //set tag information
        val dropdownTag = dialogLoadCart!!.findViewById<TextView>(R.id.iot_tag)
        val viewProduct = dialogLoadCart!!.findViewById<TextView>(R.id.product_desc)


//        val iotList: List<String> = cartsInDc.map { it.iot }
//        //Log.d(TAG, "Current carts in dc: ${iotList}")
//        var adaptor = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, iotList)
//        dropdownTag.setAdapter(adaptor)
//        dropdownTag.setOnItemClickListener { parent, view, position, id ->
//            val iot = dropdownTag.text.toString()
//            val cart = cartsInDc.find { it.iot==iot}
//            viewProduct.setText(cart?.product?:"")
//        }

        val imgScan = dialogLoadCart!!.findViewById<ImageView>(R.id.scanQr)
        imgScan.setOnClickListener{
            //Log.d(TAG, "Open QR scanner")
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCameraPermission()
            } else {
                openQRScanner()
            }
        }

        // select DC
        val labelDc = dialogLoadCart!!.findViewById<TextView>(R.id.label_dc)
        labelDc.setText(appState?.dc_selected?.name?:"")

        // select
        val viewItemCount = dialogLoadCart!!.findViewById<TextView>(R.id.label_notification)
        viewItemCount.text = getString(R.string.cart_count, itemNoEmpty + itemEmpty)

        // Set onClickListener for the buttons
        dialogLoadCart!!.findViewById<AppCompatButton>(R.id.addcart).setOnClickListener() {
            val selectedTag =dropdownTag.text.toString()
            if(appState?.dc_selected!!.name.isEmpty()){
                showToast(ApiNotification.DC_REQUIRE_CONFIRM)
                return@setOnClickListener
            }
            if( selectedTag.isEmpty()){
                showToast(ApiNotification.REQUIRE_FILL_FIELD)
                return@setOnClickListener
            }
//            val selectedCart = cartsInDc.find { it.iot== selectedTag}
//            if(selectedCart == null){
//                showToast("Unavailable cart")
//                return@setOnClickListener
//            }

            viewModel.addCart(selectedTag,
                onSuccess = { message ->
                    showToast(message)
                },
                onFailed = {message->
                    Log.e(TAG, "Failed to add cart, error = $message")
                    showToast(message)
                }
            )
            // alertDiaLog.dismiss() // Dismiss the dialog after scanning
        }
        dialogLoadCart!!.findViewById<TextView>(R.id.backButton).setOnClickListener() {
            //Log.d(TAG, "back button clicked")
            alertDialog.dismiss()
        }
        return alertDialog
    }

    private fun openUnloadCartDialog(): AlertDialog
    {
        val TAG = "createRemoveCartDialog"
        val context = requireContext()
        dialogUnloadCart = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_remove_cart, null)

        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogUnloadCart)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)

        val dropdownTags = dialogUnloadCart!!.findViewById<AutoCompleteTextView>(R.id.spinner_tag)
        val showProduct = dialogUnloadCart!!.findViewById<TextView>(R.id.product_desc)
        // init dropdown for tag
        var tagList: List<String> = cartsLoaded.map { it.iot }
        val adapterTag = ArrayAdapter(context, R.layout.dropdown_autocomplete,tagList)
        dropdownTags.setAdapter(adapterTag)
        if(cartsLoaded.size > 0){
            dropdownTags.setText(cartsLoaded[0].iot, false)
            showProduct.setText(cartsLoaded[0].product)
        }
        dropdownTags.setOnItemClickListener { parent, view, position, id ->
            val iot = dropdownTags.text.toString()
            val cart = cartsLoaded.find { it.iot==iot}
            showProduct.setText(cart?.product?:"")
        }
        // init scan button
        val imgScan = dialogUnloadCart!!.findViewById<ImageView>(R.id.scanQr)
        imgScan.setOnClickListener{
            //Log.d(TAG, "Open QR scanner")
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCameraPermission()
            } else {
                openQRScanner()
            }
        }
        // init label
        dialogUnloadCart!!.findViewById<TextView>(R.id.label_dc).setText(appState?.dc_selected?.name)
        val viewItemLoaded = dialogUnloadCart!!.findViewById<TextView>(R.id.label_notification)
        viewItemLoaded.setText(getString(R.string.cart_count, itemNoEmpty + itemEmpty))

        // Set onClickListener for the buttons
        dialogUnloadCart!!.findViewById<Button>(R.id.confirmDelete).setOnClickListener() {
            val selectedTag = dropdownTags.text.toString()
            if(selectedTag.isEmpty()){
                showToast(ApiNotification.SELECT_VALID_CART)
                return@setOnClickListener
            }

            val newCartData = cartsLoaded.find { it.iot == selectedTag }
            if(newCartData== null){
                showToast(ApiNotification.INVALID_CART)
                return@setOnClickListener
            }
            if(deliverState!!.status <= TRACK_STATUS.LOADING.code){ // load cart
                viewModel.unloadCart(
                    newCartData,
                    onSuccess = { message ->showToast(message)},
                    onFailed = {message->{showToast(message)}}
                )
            }
            else{ // return empty cart to distribution center
                viewModel.returnEmptyCart(
                    newCartData,
                    onSuccess = { message ->showToast(message)},
                    onFailed = {message->{showToast(message)}}
                )
            }
            // alertDiaLog.dismiss()
        }
        dialogUnloadCart!!.findViewById<Button>(R.id.cancel).setOnClickListener() {
            alertDialog.dismiss()
        }
        dialogUnloadCart!!.findViewById<TextView>(R.id.backButton).setOnClickListener() {
            alertDialog.dismiss()
        }
        return alertDialog

    }

    // open dialog to add retail store to trip
    private fun openAddStoreDialog(): AlertDialog{
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_store, null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)

        // dropdown of store names
        var selectedStore = -1
        val viewStores = dialogView.findViewById<AutoCompleteTextView>(R.id.autocompleteDc)

        val storeNames = _rss.map { it.name }
        val adaptor = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, storeNames)
        viewStores.setAdapter(adaptor)
        if(_rss.size > 0){
            viewStores.setText(storeNames[0], false)
            selectedStore = 0
            dialogView.findViewById<TextView>(R.id.storeAddress).setText(_rss[0].address)
            dialogView.findViewById<TextView>(R.id.storeCode).setText(_rss[0].code)

        }
        viewStores.setOnItemClickListener { parent, view, position, id ->
            val selectedName = parent.getItemAtPosition(position) as String

            val index = _rss.indexOfFirst {it.name == selectedName}
            if(index < 0 ){
                selectedStore = -1
                return@setOnItemClickListener
            }
            selectedStore = index
            val storeSelected = _rss[index]
            dialogView.findViewById<TextView>(R.id.storeAddress).setText(storeSelected.address)
            dialogView.findViewById<TextView>(R.id.storeCode).setText(storeSelected.code)
        }

        val viewSelectedStore = dialogView.findViewById<ListView>(R.id.listStores)
        val adaptorSelected = StoreItemAdapter(requireContext(), _rssSelected,
            {store->
                viewModel.removeStoreFromTrip(
                    store,
                    onSuccess = {msg->showToast(msg)},
                    onFailed = {msg->showToast(msg)}
                )
            }
        )
        viewSelectedStore.adapter = adaptorSelected

        dialogView.findViewById<ImageView>(R.id.confirmStore).setOnClickListener(){
            viewSelectedStore.isEnabled = false
            if(selectedStore < 0){
                showToast(ApiNotification.STORE_REQUIRE_CONFIRM)
                return@setOnClickListener
            }
            val store = _rss[selectedStore]

            // add store to list
            viewModel.addStoretoTrip(
                _rss[selectedStore],
                onSuccess = {msg->
                    //Log.d(TAG, "Success to add store $msg")
                    adaptorSelected.addStore(_rss[selectedStore])
                    showToast(msg)
                    viewSelectedStore.isEnabled = true
                },
                onFailed = {msg->
                    _rssSelected.remove(store)
                    showToast(msg)
                    viewSelectedStore.isEnabled = true
                }
            )
        }
        dialogView.findViewById<TextView>(R.id.backButton).setOnClickListener() {
            alertDialog.dismiss()
        }
        return alertDialog
    }

    fun onBackPressed() {
        val adapter = viewPager?.adapter as ViewPagerAdapter
        if(adapter != null)
            adapter.openHomepage()
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

