package myandroid.app.hhobzic.inventorymobile.home.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.company.CompanyViewModel
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.Company
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentEditProfileBinding
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditProfileFragment: Fragment() {

    private val TAG = "EditProfileFragment"
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private var userInfo: Driver? = null
    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var viewPager: ViewPager2
    private val gson = Gson()

    private val REQUEST_PERMISSION_CODE = 1001
    private val REQUEST_IMAGE_PICK_CODE = 1002
    private var selectedImageUri: Uri? = null

    private val companyViewModel: CompanyViewModel by viewModels()
    private var _companies: List<Company> = listOf()
    private var _selectedCompay = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)
        setupUI(binding.root)
        this.userInfo = viewModel.getUserInfo()
        companyViewModel.company.observe(viewLifecycleOwner, Observer { data->
            _companies = data
            val companyNames = _companies.map { it.name }
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, companyNames)
            binding.company.setAdapter(adapter)
            val compIndex = this._companies.indexOfFirst { it->it.name == this.userInfo?.company }
            if(compIndex >= 0){
                binding.company.setText(this._companies[compIndex].name, false)
                _selectedCompay = compIndex
            }
        })
        companyViewModel.fetchCompany()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // this.userInfo = viewModel.getUserInfo()
        viewModel.user.observe(viewLifecycleOwner, Observer { userinfo ->
            this.userInfo = userinfo
            binding.userName.setText(userinfo?.name)
            binding.userEmail.setText(userinfo?.email)
            binding.phoneNumber.setText(userinfo?.phone)
            binding.address.setText(userinfo?.contact)
            binding.license.setText(userInfo?.license)
            val compIndex = this._companies.indexOfFirst { it->it.name == this.userInfo?.company }
            Log.d(TAG, "Found the selected company index: ${this._companies.size}: ${this.userInfo?.company}: ${compIndex}")
            if(compIndex >= 0){
                binding.company.setText(this._companies[compIndex].name, false)
            }
        })

        // observer for user photo
        viewModel.userPhoto.observe(viewLifecycleOwner, Observer { bitmap->
            binding.profileImage.setImageBitmap(bitmap)
        })
        val passwordHint = binding.passwordHint
        val checkUpdatePwd: CheckBox = binding.checkboxUpdatePwd
        binding.layoutPassword.visibility = View.GONE
        binding.layoutPasswordConfirm.visibility  =View.GONE


        checkUpdatePwd.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                binding.layoutPassword.visibility = View.VISIBLE
                binding.layoutPasswordConfirm.visibility  =View.VISIBLE
            }
            else{
                binding.layoutPassword.visibility = View.GONE
                binding.layoutPasswordConfirm.visibility  =View.GONE
            }
        }
        binding.password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordHint.text = ApiNotification.USER_PWD_HINT
                passwordHint.visibility = View.VISIBLE
            } else {
                passwordHint.visibility = View.GONE
            }
        }
        binding.passwordConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordHint.text = ApiNotification.USER_PWD_HINT
                passwordHint.visibility = View.VISIBLE
            } else {
                passwordHint.visibility = View.GONE
            }
        }
        binding.uploadPhoto.setOnClickListener(){
            requestPermissions()
        }

        binding.company.setOnItemClickListener { parent, view, position, id ->
            val selectedName = parent.getItemAtPosition(position) as String
            val index = _companies.indexOfFirst {it.name == selectedName}
            Log.d(TAG, "Change company. selected name: $selectedName, selected index: $index")
            if(index < 0 ){
                this._selectedCompay = -1
                return@setOnItemClickListener
            }
            this._selectedCompay = index
        }

        binding.save.setOnClickListener(){
            val userName: String = binding.userName.text.toString()
            val email : String = binding.userEmail.text.toString()
            val phone : String = binding.phoneNumber.text.toString()
            val password:String = binding.password.text.toString()
            val passwordConfirm: String = binding.passwordConfirm.text.toString()
            val address : String = binding.address.text.toString()
            val license: String = binding.license.text.toString()
            // checking parameter validity
            if(userName.isEmpty()){
                showToast(ApiNotification.USER_INVALID_NAME)
                return@setOnClickListener
            }

            if(this._selectedCompay < 0){
                showToast(ApiNotification.REQUIRE_VALID_COMPANY)
                return@setOnClickListener
            }
            val compId = this._companies[this._selectedCompay].id

            if(email.isNotEmpty() && !isEmailValid(email)){
                showToast(ApiNotification.USER_INVALID_EMAIL)
                return@setOnClickListener
            }
//            if(phone.isNotEmpty() && !isPhoneNumberValid(phone)){
//                showToast(ApiNotification.USER_INVALID_PHONE)
//                return@setOnClickListener
//            }
            // if(password.isEmpty()|| !isPasswordValid(password)){
            val checked = checkUpdatePwd.isChecked
            var passwordNew = ""
            if(checked){ // update password
                if(password.isEmpty()){
                    showToast(ApiNotification.USER_INVALID_PWD)
                    return@setOnClickListener
                }
                if(password != passwordConfirm){
                    showToast(ApiNotification.USER_PWD_UNMATCH)
                    return@setOnClickListener
                }
                passwordNew = password
            }

            if(license.isEmpty()){
                showToast(ApiNotification.USER_INVALID_LICENSE)
                return@setOnClickListener
            }
            if(address.isEmpty()){
                showToast(ApiNotification.USER_INVALID_ADDR)
                return@setOnClickListener
            }
            // call api
            var filePart : MultipartBody.Part? = null
            if(selectedImageUri != null){
                val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                filePart = getFilePart(inputStream, getFileName(selectedImageUri!!))
            }

            Log.d(TAG, "Update driver profile: new company id: ${compId}")

            viewModel.updateUserProfile(
                company = compId.toRequestBody("text/plain".toMediaTypeOrNull()),
                name = userName.toRequestBody("text/plain".toMediaTypeOrNull()),
                phone = phone.toRequestBody("text/plain".toMediaTypeOrNull()),
                email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
                address = address.toRequestBody("text/plain".toMediaTypeOrNull()),
                password = passwordNew.toRequestBody("text/plain".toMediaTypeOrNull()),
                license = license.toRequestBody("text/plain".toMediaTypeOrNull()),
                photo = filePart,
                onSuccess = {message->
                    showToast(message)
                    gotoPreviousPage()
                },
                onFailed = {message->showToast(message)}
            )
        }

        binding.back.setOnClickListener{
            gotoPreviousPage()
        }
    }

    private fun gotoPreviousPage(){
        val adaptor = viewPager.adapter as ViewPagerAdapter
        adaptor.openProfileHome()
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

    // Function to hide the keyboard
    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun onBackPressed() {
        val adapter = viewPager.adapter as ViewPagerAdapter
        adapter.openProfileHome()
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        // Regular expression for validating a phone number
        val phoneRegex = "^\\+?[0-9]{10,15}\$".toRegex()
        return phoneNumber.matches(phoneRegex)
    }
    // 2 capital, 2 number, 6 length. no space
    private fun isPasswordValid(password: String): Boolean {
        // Check length
        if (password.length < 6) return false

        // Check for no spaces
        if (password.contains(" ")) return false

        // Check for at least 2 uppercase letters
        val uppercaseCount = password.count { it.isUpperCase() }
        if (uppercaseCount < 2) return false

        // Check for at least 2 numbers
        val numberCount = password.count { it.isDigit() }
        if (numberCount < 2) return false

        return true
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        } else {
            pickImageFromGallery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data

            selectedImageUri?.let {
                // Update the ImageView to show the selected image
                val selectedPhotoThumbnail = binding.profileImage
                selectedPhotoThumbnail.setImageURI(it)
                selectedPhotoThumbnail.visibility = View.VISIBLE
            }
        }
    }

    private fun getFilePart(inputStream: InputStream?, fileName: String): MultipartBody.Part {
        val tempFile = File(requireContext().cacheDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "temp_image"
        val resolver = requireContext().contentResolver // Use requireContext() to get the ContentResolver
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    fileName = cursor.getString(index)
                }
            }
        }
        return fileName
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