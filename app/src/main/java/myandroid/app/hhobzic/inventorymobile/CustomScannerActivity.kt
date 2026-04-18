package myandroid.app.hhobzic.inventorymobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import com.journeyapps.barcodescanner.CaptureActivity

class CustomScannerActivity : CaptureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }
}
