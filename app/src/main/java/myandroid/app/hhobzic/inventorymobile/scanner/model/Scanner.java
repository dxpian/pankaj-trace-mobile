package myandroid.app.hhobzic.inventorymobile.scanner.model;


import android.os.Handler;
import android.util.Log;

import myandroid.app.hhobzic.inventorymobile.home.AppMacro;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Beacon;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.MLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;





/* compiled from: Scanner.kt */
public final class Scanner {
    private final byte fillter = 115;
    private HashMap<String, Beacon> map_beacon = new HashMap<>();
    private ArrayList<Beacon> raw_beacon_list = new ArrayList<>();
    private final ScanCallback scanCallback;
    private BluetoothLeScannerCompat scanner;
    private ScannerResult scannerResult;
    private final ScanSettings settings;

    public Scanner(ScannerResult scannerResult2) {
        Intrinsics.checkNotNullParameter(scannerResult2, "scannerResult");
        BluetoothLeScannerCompat scanner2 = BluetoothLeScannerCompat.getScanner();
        Intrinsics.checkNotNullExpressionValue(scanner2, "getScanner()");
        this.scanner = scanner2;
        ScanSettings build = new ScanSettings.Builder().setScanMode(1).setUseHardwareFilteringIfSupported(false).build();
        Intrinsics.checkNotNullExpressionValue(build, "Builder()\n              …\n                .build()");
        this.settings = build;
        this.scannerResult = scannerResult2;
        this.scanCallback = new ScannerCallbackExt(this, scannerResult2);
    }

    public final ArrayList<Beacon> getRaw_beacon_list() {
        return this.raw_beacon_list;
    }

    public final void setRaw_beacon_list(ArrayList<Beacon> arrayList) {
        Intrinsics.checkNotNullParameter(arrayList, "<set-?>");
        this.raw_beacon_list = arrayList;
    }

    public final HashMap<String, Beacon> getMap_beacon() {
        return this.map_beacon;
    }

    public final void setMap_beacon(HashMap<String, Beacon> hashMap) {
        Intrinsics.checkNotNullParameter(hashMap, "<set-?>");
        this.map_beacon = hashMap;
    }

    public final byte getFillter() {
        return this.fillter;
    }

    public final ScannerResult getScannerResult() {
        return this.scannerResult;
    }

    public final void setScannerResult(ScannerResult scannerResult2) {
        this.scannerResult = scannerResult2;
    }

    public final BluetoothLeScannerCompat getScanner() {
        return this.scanner;
    }

    public final void setScanner(BluetoothLeScannerCompat bluetoothLeScannerCompat) {
        Intrinsics.checkNotNullParameter(bluetoothLeScannerCompat, "<set-?>");
        this.scanner = bluetoothLeScannerCompat;
    }

    public final ScanSettings getSettings() {
        return this.settings;
    }

    public final void startScan() {
        this.scanner.startScan((List<ScanFilter>) null, this.settings, this.scanCallback);
        this.raw_beacon_list.clear();
        this.map_beacon.clear();
        printMessage("开始扫描");
    }

    public final void startScan(long j) {
        startScan();
        new Handler().postDelayed(new ScannerRunnable(this), j);
        printMessage("定时停止扫描");
    }

    public final void stopScan() {
        this.scanner.stopScan(this.scanCallback);
    }

    private void  printMessage(String message){
        if(AppMacro.ENABLE_SCAN_VERBOSE)
            Log.d(MLog.ScannerE, message);
    }
}
