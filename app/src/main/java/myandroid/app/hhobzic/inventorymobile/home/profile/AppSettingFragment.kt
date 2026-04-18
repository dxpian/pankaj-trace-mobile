package myandroid.app.hhobzic.inventorymobile.home.profile

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.res.loadImageBitmap
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.huawei.hms.api.Api
import myandroid.app.hhobzic.inventorymobile.R
import myandroid.app.hhobzic.inventorymobile.api.model.ApiNotification
import myandroid.app.hhobzic.inventorymobile.api.model.Driver
import myandroid.app.hhobzic.inventorymobile.databinding.FragmentAppSettingBinding
import myandroid.app.hhobzic.inventorymobile.home.Language
import myandroid.app.hhobzic.inventorymobile.models.AppStatusSharedViewModel
import myandroid.app.hhobzic.inventorymobile.home.ViewPagerAdapter
import myandroid.app.hhobzic.inventorymobile.utility.loadLanguagePreference
import myandroid.app.hhobzic.inventorymobile.utility.saveLanguagePreference
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class AppSettingFragment: Fragment() {

    private val TAG = "AppSettingFragment"
    private var _binding: FragmentAppSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppStatusSharedViewModel
    private lateinit var viewPager: ViewPager2
    private var userinfo: Driver? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppSettingBinding.inflate(inflater, container, false)

        setupUI(binding.root)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // load and set language
        val languages = listOf("English", "日本語")
        val adaptor = ArrayAdapter(requireContext(), R.layout.dropdown_autocomplete, languages)
        val viewLanguage = binding.autocmpLan
        viewLanguage.setAdapter(adaptor)
        var curLan = loadLanguagePreference(requireContext())
        var selectedLan = Language.JPN
        if(curLan == Language.ENG){
            viewLanguage.setText(languages[0], false)
            selectedLan = Language.ENG
        }
        else{
            viewLanguage.setText(languages[1], false)
            selectedLan = Language.JPN
        }
        viewLanguage.setOnItemClickListener { parent, view, position, id ->
            // val selectedName = parent.getItemAtPosition(position) as String
            selectedLan = if(position == 0) Language.ENG else Language.JPN
            Log.d(TAG, "Language changed into ${selectedLan}")
        }
        viewModel = ViewModelProvider(requireActivity()).get(AppStatusSharedViewModel::class.java)

        binding.applyLanguage.setOnClickListener(){
            if(selectedLan == curLan)
                return@setOnClickListener
            curLan = selectedLan
            saveLanguagePreference(requireContext(), selectedLan)
            showToast(requireContext().getString(R.string.require_apply_language_change))

        }
        binding.back.setOnClickListener {
            val adaptor = viewPager.adapter as ViewPagerAdapter
            adaptor.openProfileHome()
        }

        // event handler for apk update
        binding.btnCheckApkUpdate.setOnClickListener{
            checkApkVersion()
        }

        // check the apk version
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionCode = packageInfo.longVersionCode  // versionCode from build.gradle
        val versionName = packageInfo.versionName      // versionName from build.gradle
        binding.labelAppVersion.text = "V$versionName($versionCode)"
    }

    private fun checkApkVersion(){
        // check the self APK version
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val currentVersionCode = packageInfo.longVersionCode  // versionCode from build.gradle

        viewModel.checkLastApkVersion(
            onSuccess = {apkVersion ->
                Log.d(TAG, "Success to find lastest apk version: $apkVersion")
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
        Log.d(TAG, "folder path to save the apk: ${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}")

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
        Log.d(TAG, "New apk download url: $apkUrl. ")

        this.downloadApkFile(requireContext(), apkUrl, fileName, onComplete = {f->
            if(f!= null)
                this.installApk(f)
            else{
                this.showToast(ApiNotification.APK_DOWNLOAD_FAILED)
            }
        })
    }



    private fun installApk(file: File) {
        Log.d(TAG, "Installing APK file")
        if(!file.exists() ||  file.length() <=0){
            this.showToast(ApiNotification.APK_DOWNLOAD_FAILED)
            return
        }
        Log.d(TAG, "Success to download APK: ${file.absolutePath}. size = ${file.length()}")
        val apkUri: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        Log.d(TAG, "apk uri: $apkUri")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        requireContext().startActivity(intent)
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