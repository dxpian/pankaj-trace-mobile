package myandroid.app.hhobzic.inventorymobile.home.cart

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.EditText
import android.widget.ImageView
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
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import myandroid.app.hhobzic.inventorymobile.CustomScannerActivity
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.DistCenter
import myandroid.app.hhobzic.inventorymobile.api.model.Store
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentViewDeliverBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.api.model.Cart
import myandroid.app.hhobzic.inventorymobile.api.model.DeliveryState
import myandroid.app.hhobzic.inventorymobile.api.model.TRACK_STATUS
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import myandroid.app.hhobzic.inventorymobile.utility.AppState
import myandroid.app.hhobzic.inventorymobile.utility.hideKeyboardCart
import java.io.IOException

enum class DLG_TYPE(val code: Int) {
    UNK (0),
    DELIVER(1),
    PICKUP(2);

    companion object {
        fun fromCode(code: Int): DLG_TYPE? {
            return entries.find { it.code == code }
        }
    }
}

class DeliverHomeFragment : Fragment() {
    private val TAG = "DeliverHomeFragment"
    private var _binding: FragmentViewDeliverBinding? = null
    private val binding get() = _binding!!
    private var isScanning = false
    private val requestCodeCameraPermission = 1001
    private var scannedValue = ""
    private val REQUEST_CAMERA_PERMISSION = 201
    private var surfaceView: SurfaceView? = null
    private var cameraSource: CameraSource? = null
    private var cartsUpdated: MutableList<Cart> = mutableListOf()        // total cart information
    private var itemLoaded = 0                                      // count for loaded cart
    private var itemEmpty = 0                                       // count for empty cart
    private var barcodeDetector: BarcodeDetector? = null
    private var isInnerDialogShown = false
    private var gifView: ImageView? = null
    private lateinit var viewModel: AppStatusSharedViewModel
    private var appState = AppState()
    private var deliverState: DeliveryState? = null
    private lateinit var rss: List<Store>
    private var _dcs: List<DistCenter> = emptyList()
    private var _rss: MutableList<Store> = mutableListOf()
    private var _rssSelected:MutableList<Store> = mutableListOf()   // stores added to trip
    private var iotTagEditText: EditText? = null
    private var dlgDeliver: View?=null
    private var selectedRsID: String = ""                       // store id which is selected in home fragment
    private var _dlgtype: DLG_TYPE = DLG_TYPE.UNK
    private lateinit var _viewTag: AutoCompleteTextView         // view to show tag
    private lateinit var _editTag: EditText
    private lateinit var _viewProduct: TextView                  // view to show product


    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewDeliverBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        viewModel.cartsCount.observe(viewLifecycleOwner, Observer { count->
            binding.checklistSize.text = getString(R.string.cart_count, count)
            // binding.itemLoadedLabel.text = getString(R.string.cart_count, count)
        })

        viewModel.cartsCountEmpty.observe(viewLifecycleOwner, Observer { count-> itemEmpty = count })
        viewModel.cartsCountLoaded.observe(viewLifecycleOwner, Observer { count->itemLoaded = count })
        viewModel.carts.observe(viewLifecycleOwner, Observer { carts:MutableList<Cart>->
            cartsUpdated = carts
        })
        // fecth distribution centers and retail stores
        viewModel.loadingDeliverAppState.observe(viewLifecycleOwner, Observer { state->
            if(state != null){
                appState = state;
                // _rssSelected = loadingState.rss
                _rssSelected = state.rss.map { store -> store.copy() }.toMutableList()
                //Log.d(TAG, "RSS of store is updated")
            }
            onAppStateChanged()
        })
        viewModel.loadingDeliverState.observe(viewLifecycleOwner, Observer { state->
            deliverState = state
            val isEnabled =  if(state!=null) true else false
            binding.selectShop.isEnabled = isEnabled
            binding.deliverCart.isEnabled = isEnabled
            binding.continueTrip.isEnabled = isEnabled
        })


        viewModel.distcenters.observe(viewLifecycleOwner, Observer { data-> _dcs = data })
        viewModel.retailstores.observe(viewLifecycleOwner, Observer { data-> _rss = data.toMutableList() })
        viewModel.refreshJourneyInformation(onSuccess = { msg->
            Log.d(TAG, "Success to load DC and RS information")
            },
            onFailed = {msg->
                showToast(msg)
        })
        rss = viewModel.getRetailStores()

        // select retail store arrived
        binding.selectShop.setOnClickListener(){
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_shop, null)

            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true) // Allow the dialog to be canceled
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
            val imgStore = dialogView.findViewById<ImageView>(R.id.storeImage)
            val rsNames = rss.map { it.name }

            val arrayAdaptor = ArrayAdapter(requireContext(),R.layout.dropdown_autocomplete, rsNames)
            val dropdownStore = dialogView.findViewById<AutoCompleteTextView>(R.id.autocompleteDc)
            dropdownStore.setAdapter(arrayAdaptor)
            var storeIndex = rss.indexOfFirst { it.id == appState.rs_arrived?.id }
            if(storeIndex < 0)
                storeIndex = rss.indexOfFirst { it.id == selectedRsID }
            if(storeIndex >= 0){
                val storeSelected = rss[storeIndex]
                dropdownStore.setText(storeSelected.name, false)
                dialogView.findViewById<TextView>(R.id.storeCode).setText(storeSelected.code)
                dialogView.findViewById<TextView>(R.id.storeDesc).setText(storeSelected.address)
                viewModel.downloadStoreImage(rss[storeIndex].id, onSuccess = { bitmap->
                    imgStore.setImageBitmap(bitmap)
                }, onFailed = {msg->
                    imgStore.setImageBitmap(null)
                })
            }
            dropdownStore.setOnItemClickListener { parent, view, position, id ->
                val storeName = dropdownStore.text.toString()?:""
                val index = rss.indexOfFirst {it.name == storeName}
                if(index < 0){
                    return@setOnItemClickListener
                }
                binding.selectShop.isEnabled = false
                dialogView.findViewById<TextView>(R.id.storeDesc).setText(rss[index].address)
                dialogView.findViewById<TextView>(R.id.storeCode).setText(rss[index].code)
                // download retail store photo
                viewModel.downloadStoreImage(rss[index].id, onSuccess = { bitmap->
                    imgStore.setImageBitmap(bitmap)
                    binding.selectShop.isEnabled = true
                }, onFailed = {msg->
                    imgStore.setImageBitmap(null)
                    binding.selectShop.isEnabled = true
                    Log.d(TAG, "Failed to download retail store image: ${msg}")
                })
            }

            // Set onClickListener for the buttons
            val btnConfirm = dialogView.findViewById<Button>(R.id.confirmLoad)
            btnConfirm.setOnClickListener() {
                btnConfirm.isEnabled = false
                val storename = dropdownStore.text.toString()?:""
                val index = rss.indexOfFirst {it.name == storename}
                if(index < 0){
                    this.showToast(ApiNotification.STORE_CONFIRM_ARRIVAL)
                    btnConfirm.isEnabled = true
                    return@setOnClickListener
                }

                val store = rss[index]
                selectedRsID = store.id
                viewModel.onArrivedStore(store,
                    onSuccess = {msg->
                        showToast(msg)
                        alertDialog.dismiss()
                    },
                    onFailed = {msg->
                        btnConfirm.isEnabled = true
                        showToast(msg)})
            }
            dialogView.findViewById<Button>(R.id.confirmCancel).setOnClickListener() {
                alertDialog.dismiss()
            }

            // Show the AlertDialog
            alertDialog.show()
        }

        // deliver cart and pickup empty cart from store
        binding.deliverCart.setOnClickListener(){
            // check if driver selects retail store already
            if(deliverState?.status != TRACK_STATUS.DELIVERING.code){
                showToast(ApiNotification.STORE_CONFIRM_ARRIVAL)
                return@setOnClickListener
            }
            // check if selected store is valid again
            val store_index = appState.rss.indexOfFirst { it.id == appState.rs_arrived?.id }
            if(store_index < 0){
                showToast(ApiNotification.STORE_CONFIRM_ARRIVAL)
                return@setOnClickListener
            }

            dlgDeliver = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_process_cart, null)
            val builder = AlertDialog.Builder(requireContext())
                .setView(dlgDeliver)
                .setCancelable(true) // Allow the dialog to be canceled

            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)

            //set store name
            val storeName = appState.rs_arrived?.name!!
            dlgDeliver!!.findViewById<TextView>(R.id.labelStore).setText(storeName)

            val itemLoadedView = dlgDeliver!!.findViewById<TextView>(R.id.cartLoaded)
            val itemEmptyView = dlgDeliver!!.findViewById<TextView>(R.id.cartEmpty)
            viewModel.cartsCountLoaded.observe(viewLifecycleOwner, Observer { count->itemLoadedView.text = "$count" })
            viewModel.cartsCountEmpty.observe(viewLifecycleOwner, Observer { count->itemEmptyView.text = "$count" })


            // event handler for add cart
            dlgDeliver!!.findViewById<Button>(R.id.cart_deliver).setOnClickListener() {
                val dialog = openDeliverCartDialog(cartsUpdated)
                dialog.show()
            }

            dlgDeliver!!.findViewById<Button>(R.id.cart_pickup).setOnClickListener() {
                val dialog = openPickupCartDialog()
                dialog.show()
            }
            dlgDeliver!!.findViewById<TextView>(R.id.backButton).setOnClickListener() {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        // continue trip
        binding.continueTrip.setOnClickListener(){
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_contiue_trip, null)
            val builder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true) // Allow the dialog to be canceled

            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
            val btnGo = dialogView.findViewById<ImageView>(R.id.go)
            var storeArrived = appState.rs_arrived
            if(storeArrived!= null){
                dialogView.findViewById<TextView>(R.id.storeCode).setText(storeArrived.code)
                dialogView.findViewById<TextView>(R.id.storeDesc).setText(storeArrived.name)
            }

            // Set onClickListener for the buttons
            dialogView.findViewById<ImageView>(R.id.go).setOnClickListener() {
                btnGo.isEnabled = false
                val storeLast = appState.rs_arrived
                if(storeLast == null){
                    showToast(ApiNotification.STORE_CONFIRM_ARRIVAL)
                    btnGo.isEnabled = true
                    return@setOnClickListener
                }
                viewModel.continueTrip(
                    onSuccess = {
                        msg->showToast(msg)
                        btnGo.isEnabled = true
                                },
                    onFailed = {
                        msg->showToast(msg)
                        btnGo.isEnabled = true
                    }
                )
                alertDialog.dismiss()
            }
            dialogView.findViewById<TextView>(R.id.backButton).setOnClickListener() {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }

        binding.back.setOnClickListener(){
            val adapter = viewPager.adapter as ViewPagerAdapter
            adapter.openHomepage()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = requireView().findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)

    }

    private fun onAppStateChanged(){
        val store = appState.rs_arrived
        Log.d(TAG, "Store arrived: ${store}: current status: ${deliverState?.status}")
        if(appState.rs_arrived != null){ // store is selected
            binding.imgSelectStore.setImageResource(R.drawable.checked)

            if(store?.itemdelivered.let { it!! > 0 } || store?.itemloaded.let { it!! > 0 }){ // item is already delivered
                binding.imageItemDelivered.setImageResource(R.drawable.checked)
            }
            else{
                binding.imageItemDelivered.setImageResource(R.drawable.khalicheckbox)
            }
        }
        else{
            binding.imgSelectStore.setImageResource(R.drawable.khalicheckbox)
            binding.imageItemDelivered.setImageResource(R.drawable.khalicheckbox)
        }
        if(deliverState != null){
            if(deliverState!!.status == TRACK_STATUS.PROGRESS.code){ // already leaved the store
                binding.imgContinueTrip.setImageResource(R.drawable.checked)
                binding.continueTrip.isEnabled = false
            }
            else{
                binding.imgContinueTrip.setImageResource(R.drawable.khalicheckbox)
            }
        }else{
            binding.imgContinueTrip.setImageResource(R.drawable.khalicheckbox)
            binding.continueTrip.isEnabled = false
        }
    }

    /*
    function: Open dialog to deliver cart from truck to retial store
     */
    private fun openDeliverCartDialog(
        cartData:MutableList<Cart> = mutableListOf()):AlertDialog
    {
        _dlgtype = DLG_TYPE.DELIVER
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deliver_cart, null)

        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)

        // add item into status
        _viewTag = dialogView.findViewById<AutoCompleteTextView>(R.id.iot_tag)
        val taglist = cartData.map { it.iot }.toMutableList()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, taglist)
        _viewTag.setAdapter(adapter)
        fun removeCartFromAdapter(itemToRemove: String) {
            taglist.remove(itemToRemove)
            adapter.notifyDataSetChanged() // Notify the adapter of the data change
        }

        _viewProduct = dialogView.findViewById<TextView>(R.id.product)
        _viewTag.setOnItemClickListener { parent, view, position, id ->
            val tag = _viewTag.text.toString()
            // find cart of tag
            val cart: Cart? = cartData.find { it.iot== tag }
            if(cart==null){
                _viewProduct.setText("")
                showToast(ApiNotification.CART_INVALID_TAG)
                return@setOnItemClickListener
            }
            if(cart.product!= null)
                _viewProduct.setText(cart.product)
            else
                _viewProduct.setText("empty cart")
        }

        // show store name first
        val viewStore = dialogView.findViewById<TextView>(R.id.store)
        val storeLast = appState.rs_arrived
        //Log.d(TAG, "last store arrived: $storeLast")
        if(storeLast!= null){
            viewStore.setText("${storeLast.name}(${storeLast.address})")
        }

        dialogView.findViewById<ImageView>(R.id.scanQr).setOnClickListener{
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCameraPermission()
            } else {
                openQRScanner()
            }
        }
        // Set onClickListener for the buttons
        var btnConfirm = dialogView.findViewById<AppCompatButton>(R.id.cart_deliver)
        btnConfirm.setOnClickListener() {
            if(storeLast==null){
                showToast(ApiNotification.STORE_CONFIRM_ARRIVAL)
                return@setOnClickListener
            }
            btnConfirm.isEnabled = false
            _viewTag.isEnabled = false
            val tag = _viewTag.text.toString()
            val cart: Cart? = cartData.find { it.iot== tag }
            if(cart==null){
                _viewProduct.setText("")
                showToast(ApiNotification.CART_INVALID_TAG)
                btnConfirm.isEnabled = true
                _viewTag.isEnabled = true
                return@setOnClickListener
            }
            viewModel.deliverCart(cart,
                onSuccess = {msg->
                    removeCartFromAdapter(tag)
                    showToast(msg)

                    if(taglist.size > 0){
                        _viewTag.setText(taglist[0], false)
                    }
                    else{
                        _viewTag.setText("", false)
                    }
                    btnConfirm.isEnabled = true
                    _viewTag.isEnabled = true
                },
                onFailed = {msg->
                    showToast(msg)
                    btnConfirm.isEnabled = true
                    _viewTag.isEnabled = true
                })
        }
        dialogView.findViewById<TextView>(R.id.backButton).setOnClickListener() {
            alertDialog.dismiss()
        }
        return alertDialog
    }

    /*
    function: Open dialog to pickup cart to retial store from truck
     */
    private fun openPickupCartDialog(): AlertDialog{
        _dlgtype = DLG_TYPE.PICKUP
        val TAG = "openPickupCartDialog"
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pickup_cart, null)

        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)
        dialogView.setOnTouchListener { v, event ->
            hideKeyboardCart(v)
            v.performClick() // To satisfy the OnTouchListener interface
            false
        }

        _editTag = dialogView.findViewById<EditText>(R.id.textview_tag)

        // show store name first
        val viewStore = dialogView.findViewById<TextView>(R.id.textview_store)
        val storeLast = appState.rs_arrived
        //Log.d(TAG, "last store arrived: $storeLast")
        if(storeLast!= null){
            viewStore.setText("${storeLast.name}(${storeLast.address})")
        }
        dialogView.findViewById<ImageView>(R.id.scanQr).setOnClickListener{
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                askForCameraPermission()
            } else {
                openQRScanner()
            }
        }

        // Set onClickListener for the buttons
        var btnConfirm = dialogView.findViewById<AppCompatButton>(R.id.confirmPickup)
        btnConfirm.setOnClickListener() {

            btnConfirm.isEnabled = false
            _editTag.isEnabled = false
            val tag = _editTag.text.toString()
            if(tag.isNullOrEmpty()){
                showToast(ApiNotification.CART_INVALID_TAG)
                return@setOnClickListener
            }
            viewModel.pickupCartFromStore(tag,
                onSuccess = {msg->
                    showToast(msg)
                    _editTag.setText("")
                    btnConfirm.isEnabled = true
                    _editTag.isEnabled = true
                },
                onFailed = { msg->
                    showToast(msg)
                    btnConfirm.isEnabled = true
                    _editTag.isEnabled = true
                })
        }
        dialogView.findViewById<TextView>(R.id.backButton).setOnClickListener() {
            alertDialog.dismiss()
        }
        return alertDialog
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
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraSource()
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
        if(_dlgtype== DLG_TYPE.DELIVER){
            val tag = qrCodeText
            // find cart of tag
            val cart: Cart? = cartsUpdated.find { it.iot== tag }
            if(cart==null){
                _viewProduct.setText(ApiNotification.CART_INVALID_TAG)
                showToast(ApiNotification.CART_INVALID_TAG)
                return
            }
            _viewTag.setText(tag, false)
            //Log.d(TAG, "find cart information: $cart")
            if(cart.product!=null) {
                _viewProduct.setText(cart.product)
            }
            else {
                _viewProduct.setText("empty cart")
            }
        }
        else if(_dlgtype == DLG_TYPE.PICKUP){
            _editTag.setText(qrCodeText)
        }
    }
    fun setSelectedStoreID(storeID:String){
        this.selectedRsID = storeID
    }
    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }

    fun onBackPressed() {
        val adapter = viewPager.adapter as ViewPagerAdapter
        adapter.openHomepage()
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

