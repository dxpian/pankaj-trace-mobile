package myandroid.app.hhobzic.inventorymobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import myandroid.app.hhobzic.inventorymobile.api.model.ApiResult
import myandroid.app.hhobzic.inventorymobile.api.model.AuthManager
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentProfileBinding
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter

class ProfileFragment : Fragment() {
    private var TAG = "ProfileFragment"
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewPager: ViewPager2
    private var userInfo: Driver? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        viewModel.user.observe(viewLifecycleOwner, Observer { userinfo ->
            this.userInfo = userinfo
            binding.userName.text = userinfo.name
            binding.userEmail.text = userinfo.email

        })

        // observer for user photo
        viewModel.userPhoto.observe(viewLifecycleOwner, Observer { bitmap->
            binding.profileImage.setImageBitmap(bitmap)
        })

        editProfileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onReturnFromProfile()
            }
        }
        this.userInfo = viewModel.getUserInfo()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editProfile.setOnClickListener(){
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openEditUserProfile()

        }
        binding.editTruck.setOnClickListener(){
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openEditTruck()

        }
        binding.notification.setOnClickListener(){
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openNotifyPage()
        }

        binding.trackSetting.setOnClickListener(){
            val adpator = viewPager.adapter as ViewPagerAdapter
            adpator.openTrackSettingPage()
        }
        binding.appSetting.setOnClickListener(){
            val adapter = viewPager.adapter as ViewPagerAdapter
            adapter.openAppSettingPage()
        }
        binding.profileDelete.setOnClickListener(){ // delete account
            // create question dialog
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_account, null)
            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true) // Allow the dialog to be canceled

            // Create the AlertDialog
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.cart_dialog_framework)

            dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.confirmDelete).setOnClickListener() {
                viewModel.deleteDriverAccount(onSuccess = {result->
                    val msg = getString(R.string.notify_delete_account)
                    showToast(msg)
                    // navigate to logout
                    cleanUserInformation()
                    // go to login acitivty
                    val intent = Intent(requireContext(), SplashActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }, onFailed = {message->
                    showToast(message)
                })
                alertDialog.dismiss()
            }
            dialogView.findViewById<TextView>(R.id.cancelDelete).setOnClickListener() {
                alertDialog.dismiss()
            }
            // Show the AlertDialog
            alertDialog.show()
        }

        //return binding.root
    }

    private fun cleanUserInformation(){
        // remove token
        AuthManager.cleanSession()
        // remove token from sharedPreference
        val sharedPreferences =requireContext().getSharedPreferences(AppMacro.SHAREDPREF_NAME, Context.MODE_PRIVATE)
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

    private fun showToast( message: String, durationInMillis: Long = 5000) {
        if(isAdded){
            val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            toast.show()
            Handler(Looper.getMainLooper()).postDelayed({
                toast.cancel()
            }, durationInMillis)
        }

    }

    private fun onReturnFromProfile() {
        // //Log.d(TAG, "Profile Window is completed")
    }

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}