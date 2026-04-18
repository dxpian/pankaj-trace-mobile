package myandroid.app.hhobzic.inventorymobile.scanner.presenter;


import android.util.Log;

import myandroid.app.hhobzic.inventorymobile.scanner.bean.Beacon;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.MLog;
import myandroid.app.hhobzic.inventorymobile.scanner.model.Scanner;
import myandroid.app.hhobzic.inventorymobile.scanner.model.ScannerResult;
import java.util.ArrayList;

/*
interface to read the scanning result
 */


/* BLE scan manager */
public final class ScanManager implements IMainPresenter, ScannerResult {

    private Scanner scanner = new Scanner(this);
    private ScannerResult _scanResCallback;

    public ScanManager(ScannerResult callback){ _scanResCallback = callback; }


    public void startScan(int i) {

    }

    public void startScan() {
        //Log.d(MLog.ConnectE, "start scanning");
        this.scanner.startScan();
    }

    public void stopScan() {
        //Log.d(MLog.ConnectE, "Stop scanning");
        this.scanner.stopScan();
    }

    public void findBeacon(ArrayList<Beacon> arrayList) {
        //Log.d(MLog.ConnectE, "Successfully Found beacond: " + arrayList.size());
        if(_scanResCallback != null){
            _scanResCallback.findBeacon(arrayList);
        }

    }
}
