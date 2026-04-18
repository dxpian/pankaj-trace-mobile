package myandroid.app.hhobzic.inventorymobile.scanner.model;

import android.util.Log;

import myandroid.app.hhobzic.inventorymobile.home.AppMacro;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Beacon;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.MLog;
import myandroid.app.hhobzic.inventorymobile.scanner.util.ParseLeAdvData;
import myandroid.app.hhobzic.inventorymobile.scanner.util.StringUtils;
import myandroid.app.hhobzic.inventorymobile.scanner.util.Util;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/* compiled from: Scanner.kt */
public final class ScannerCallbackExt extends ScanCallback {
    final  ScannerResult scannerResult;
    final  Scanner scanner;
    private final String TAG = "ScannerCallbackExt";

    ScannerCallbackExt(Scanner scanner, ScannerResult scannerResult) {
        this.scanner = scanner;
        this.scannerResult = scannerResult;
    }


    @Override
    public void onScanResult(int i, ScanResult scanResult) {
        ScanRecord scanRecord = scanResult.getScanRecord();
        Intrinsics.checkNotNull(scanRecord);
        byte[] bytes = scanRecord.getBytes();
        ParseLeAdvData.adv_report_parse((short)22, bytes);
        ParseLeAdvData.adv_report_parse((short)255, bytes);
        byte[] adv_report_parse = ParseLeAdvData.adv_report_parse((short)1, bytes);

        if (adv_report_parse != null) {
            printMessage("步骤111");
            if (this.scanner.getMap_beacon().get(scanResult.getDevice().getAddress()) != null) {
                printMessage("步骤222");
                Beacon beacon = this.scanner.getMap_beacon().get(scanResult.getDevice().getAddress());
                printMessage("found pre-exist beacon: " + beacon.getAddress());
                Util.getDevice(scanResult, beacon);
                if (adv_report_parse[0] == 6) {
                    if (beacon != null) {
                        beacon.setIsConnect(true);
                    }
                } else if (beacon != null) {
                    beacon.setIsConnect(false);
                }
            } else {
                printMessage("步骤888");
                Beacon device = Util.getDevice(scanResult, (Beacon) null);
                if (adv_report_parse[0] == 6) {
                    if (device != null) {
                        device.setIsConnect(true);
                    }
                } else if (device != null) {
                    device.setIsConnect(false);
                }
                if (device != null) {
                    this.scanner.getRaw_beacon_list().add(device);
                    this.scanner.getMap_beacon().put(device.getAddress(), device);
                    printMessage("add beacon to beacon: " + device.getAddress() + "current beacon list: " + this.scanner.getRaw_beacon_list().size());
                } else {
                    printMessage("device is invalid, ccc=MAC=" + scanResult.getDevice().getAddress());
                }
            }
            printMessage("current beacon list: " + this.scanner.getRaw_beacon_list().size());
            this.scannerResult.findBeacon(this.scanner.getRaw_beacon_list());
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> list) {
        Intrinsics.checkNotNullParameter(list, "results");
        printMessage("多次结果=" + list.get(0).getDevice().getAddress());
    }

    @Override
    public void onScanFailed(int errorCode) {
        printMessage("Failed to scan: " + errorCode);

    }

    private  void printMessage(String message){
        if(AppMacro.ENABLE_SCAN_VERBOSE)
            Log.v(TAG, message);
    }

}

