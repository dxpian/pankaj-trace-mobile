package myandroid.app.hhobzic.inventorymobile


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import myandroid.app.hhobzic.inventorymobile.api.company.CompanyViewModel
import android.provider.OpenableColumns
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts

import myandroid.app.hhobzic.inventorymobile.databinding.ActivityRegisterBinding
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.user.UserViewModel
import myandroid.app.hhobzic.inventorymobile.home.AppMacro
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding // Declare binding variable
    private val companyViewModel: CompanyViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val REQUEST_PERMISSION_CODE = 1001
    private val REQUEST_IMAGE_PICK_CODE = 1002
    private var selectedImageUri: Uri? = null


    private val TAG =  "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding using ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.navigationBarColor = Color.BLACK

        // show initial state at companyname list
        val initialList = listOf("Loading...")
        val initialAdapter = ArrayAdapter(this, R.layout.dropdown_autocomplete, initialList)
        binding.companyName.setAdapter(initialAdapter)

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri;
            selectedImageUri?.let {
                // Update the ImageView to show the selected image
                val selectedPhotoThumbnail = findViewById<ImageView>(R.id.selectedPhotoThumbnail)
                selectedPhotoThumbnail.setImageURI(it)
                selectedPhotoThumbnail.visibility = View.VISIBLE
            }
        }


        binding.uploadDriverPhoto.setOnClickListener(){
            // requestPermissions()
            pickImageLauncher.launch("image/*")
        }
        binding.submit.setOnClickListener(){

            ////Log.d(this.TAG, "checking input parameters")

            val name = binding.driverName.text.toString()
            val phone = binding.driverContact.text.toString()
            val license = binding.driverLicenceNumber.text.toString()
            val vin_number = binding.truckIdentificationNumber.text.toString()
            val password = binding.password.text.toString()
            val password_confirm = binding.confirmPassword.text.toString()

            if (name.isEmpty() || phone.isEmpty() || license.isEmpty() || vin_number.isEmpty() || selectedImageUri == null
                || password.isEmpty() || password_confirm.isEmpty()) {
                showToast("Please fill in all fields and select a photo")
                return@setOnClickListener
            }
            if(password != password_confirm){
                showToast("Password is unmatched")
                return@setOnClickListener
            }
            if(AppMacro.USE_STRON_PASSWORD && !isPasswordValid(password)){
                showToast(ApiNotification.USER_PWD_HINT)
                return@setOnClickListener
            }

            val companyName = binding.companyName.text.toString()
            if (companyName.isEmpty() || companyName.isBlank()) {
                Toast.makeText(this, ApiNotification.REQUIRE_FILL_FIELD, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = binding.email.text.toString()
            if(email.isNotEmpty() &&  ! isValidEmail(email)){
                Toast.makeText(this, ApiNotification.USER_INVALID_EMAIL, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val filePart = getFilePart(inputStream, getFileName(selectedImageUri!!))
            // Call your ViewModel method to upload the image
            userViewModel.uploadDriverData(
                namePart = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                companyPart =  companyName.toRequestBody("text/plain".toMediaTypeOrNull()),
                phonePart = phone.toRequestBody("text/plain".toMediaTypeOrNull()),
                licensePart = license.toRequestBody("text/plain".toMediaTypeOrNull()),
                vinNumberPart = vin_number.toRequestBody("text/plain".toMediaTypeOrNull()),
                passwordPart = password.toRequestBody("text/plain".toMediaTypeOrNull()),
                emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull()),
                filePart = filePart,
                onSuccess = {
                    showToast("Operation success")
                    startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                    finish()
                },
                onError = { errorMsg -> showToast("Failed to upload driver data: $errorMsg") }
            )
        }

        // observe company names
        companyViewModel.company.observe(this, Observer { companies ->
            //Log.d(this.TAG, "Success to get all companies: $companies")

            val companyNames = if (companies.isEmpty()) {
                listOf("No companies available")
            } else {
                companies.map { it.name }
            }

            // Create adapter
            val adapter = ArrayAdapter<String>(this, R.layout.dropdown_autocomplete, companyNames)
            binding.companyName.setAdapter(adapter)


        })

        // observe company service error
        companyViewModel.error.observe(this, Observer{error->
            if (error != null) {
                //Log.d(this.TAG, "company error occurd: $error")
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                binding.submit.isEnabled = false
            } else {
                binding.submit.isEnabled = true
            }
        })

        userViewModel.isUploadEnabled.observe(this, Observer{enabled->
            binding.submit.isEnabled = enabled
        })
        // fetch all company names
        companyViewModel.fetchCompany()

    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        } else {
            pickImageFromGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data

            selectedImageUri?.let {
                // Update the ImageView to show the selected image
                val selectedPhotoThumbnail = findViewById<ImageView>(R.id.selectedPhotoThumbnail)
                selectedPhotoThumbnail.setImageURI(it)
                selectedPhotoThumbnail.visibility = View.VISIBLE
            }
        }
    }

    private fun getFilePart(inputStream: InputStream?, fileName: String): MultipartBody.Part {
        val tempFile = File(cacheDir, fileName)
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
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    fileName = cursor.getString(index)
                }
            }
        }
        return fileName
    }
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

    private fun isValidEmail(email: String?): Boolean {
        if(email.isNullOrEmpty())
            return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showToast(message: String, durationInMillis: Long = 5000) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, durationInMillis)

    }
}


