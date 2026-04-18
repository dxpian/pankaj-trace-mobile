package myandroid.app.hhobzic.inventorymobile.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ManagePermissions(val activity: Activity,val list: List<String>,val code:Int) {
    val TAG = "ManagePermissions"
    // Check permissions at runtime
    fun checkPermissions() {
        if (countGrantedPermission() != PackageManager.PERMISSION_GRANTED) {
            // showAlert()
            requestPermissions()
        } else {
            Toast.makeText(activity, "Permissions already granted.", Toast.LENGTH_SHORT).show()
        }
    }

    fun isAllPermissionGranted():Boolean{
        Log.d(TAG, "permissions granted: ${countGrantedPermission()}: ${PackageManager.PERMISSION_GRANTED}")
        return (countGrantedPermission() == PackageManager.PERMISSION_GRANTED)
    }

    // Check permissions status
    private fun countGrantedPermission(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0;
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }

    // Find the first denied permission
    private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }

    // Show alert dialog to request permissions
    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Need permission(s)")
        builder.setMessage("Some permissions are required to do the task.")
        builder.setPositiveButton("OK", { dialog, which -> requestPermissions() })
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }


    // Request the permissions at run time
    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Show an explanation asynchronously
            Toast.makeText(activity, "Should show an explanation.", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }
    }


    // Process permissions result
    fun processPermissionsResult(requestCode: Int, permissions: Array<String>,
                                 grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) {
                result += item
            }
        }
        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }
}