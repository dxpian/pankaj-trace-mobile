package myandroid.app.hhobzic.inventorymobile.scanner.model;

import static java.sql.DriverManager.println;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.MLog;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Type;
import myandroid.app.hhobzic.inventorymobile.scanner.util.StringUtils;
import com.huawei.agconnect.exception.AGCServerException;
import java.util.UUID;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.RequestQueue;
import no.nordicsemi.android.ble.WriteRequest;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;


/* compiled from: ConnectModel.kt */
public final class ConnectModel extends BleManager {
    private BluetoothGattService BluetoothGattBeacon_service;
    private BluetoothGattService BluetoothGattBeacon_service_version;
    private BluetoothGattService BluetoothGattOther_service;
    private BluetoothGattCharacteristic Characteristic_BeaconUUID;
    private BluetoothGattCharacteristic Characteristic_CompanyId;
    private BluetoothGattCharacteristic Characteristic_DeviceName;
    private BluetoothGattCharacteristic Characteristic_Interval;
    private BluetoothGattCharacteristic Characteristic_RssiInOne;
    private BluetoothGattCharacteristic Characteristic_ServiceData;
    private BluetoothGattCharacteristic Characteristic_TxPower;
    private BluetoothGattCharacteristic Characteristic_get;
    private BluetoothGattCharacteristic Characteristic_major;
    private BluetoothGattCharacteristic Characteristic_minor;
    private BluetoothGattCharacteristic Characteristic_notify;
    private BluetoothGattCharacteristic Characteristic_notify_version;
    private BluetoothGattCharacteristic Characteristic_psw;
    private BluetoothGattCharacteristic Characteristic_read;
    private BluetoothGattCharacteristic Characteristic_setMode;
    private final UUID Service_Beacon_UUID;
    private final UUID Service_Other_UUID;
    private String address;
    private final UUID char_ACC;
    private final UUID char_BeaconUUID_UUID;
    private final UUID char_CompanyId_UUID;
    private final UUID char_DeviceName_UUID;
    private final UUID char_Interval_UUID;
    private final UUID char_Psw_UUID;
    private final UUID char_RssiInOne_UUID;
    private final UUID char_ServiceData_UUID;
    private final UUID char_TxPower_UUID;
    private final UUID char_WenduShidu;
    private final UUID char_get;
    private final UUID char_major_UUID;
    private final UUID char_minor_UUID;
    private final UUID char_notify_UUID;
    private final UUID char_read;
    private final UUID char_setMode_UUID;
    private ConnectDataCallBack connectDataCallBack;



    private DataReceivedCallback receivedCallback = new DataReceivedCallback() {
        @Override
        public void onDataReceived(BluetoothDevice bluetoothDevice, Data data) {
            ConnectModel.receivedCallbacklambda10(ConnectModel.this,  bluetoothDevice, data);
        }
    };
    private DataReceivedCallback receivedCallback1 = new DataReceivedCallback() {
        @Override
        public void onDataReceived(BluetoothDevice bluetoothDevice, Data data) {
            ConnectModel.receivedCallback1lambda12(ConnectModel.this, bluetoothDevice, data);
        }
    };
    private DataReceivedCallback receivedCallback_Acc = new DataReceivedCallback() {
        public void onDataReceived(BluetoothDevice bluetoothDevice, Data data) {
            ConnectModel.receivedCallback_Acclambda14(ConnectModel.this, bluetoothDevice, data);
        }
    };
    private DataReceivedCallback receivedCallback_Temp = new DataReceivedCallback() {

        public void onDataReceived(BluetoothDevice bluetoothDevice, Data data) {
            ConnectModel.receivedCallback_Templambda19(ConnectModel.this, bluetoothDevice, data);
        }
    };
    private DataReceivedCallback receivedCallback_version = new DataReceivedCallback() {
        public void onDataReceived(BluetoothDevice bluetoothDevice, Data data) {
            ConnectModel.receivedCallback_versionlambda17(ConnectModel.this, bluetoothDevice, data);
        }
    };
    private final UUID version_char;
    private final UUID version_service_UUID;

    @Metadata(k = 3, mv = {1, 7, 1}, xi = 48)
    /* compiled from: ConnectModel.kt */
    public static/* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[Type.values().length];
            iArr[Type.deviceName.ordinal()] = 1;
            iArr[Type.uuid.ordinal()] = 2;
            iArr[Type.serviceData.ordinal()] = 3;
            iArr[Type.major.ordinal()] = 4;
            iArr[Type.minor.ordinal()] = 5;
            iArr[Type.txpower.ordinal()] = 6;
            iArr[Type.rssiatone.ordinal()] = 7;
            iArr[Type.interval.ordinal()] = 8;
            iArr[Type.setMode_beacon.ordinal()] = 9;
            iArr[Type.wendu_Open.ordinal()] = 10;
            iArr[Type.ACC_Open.ordinal()] = 11;
            $EnumSwitchMapping$0 = iArr;
        }
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String str) {
        this.address = str;
    }

    public UUID getService_Beacon_UUID() {
        return this.Service_Beacon_UUID;
    }

    public UUID getChar_minor_UUID() {
        return this.char_minor_UUID;
    }

    public UUID getChar_major_UUID() {
        return this.char_major_UUID;
    }

    public final UUID getChar_TxPower_UUID() {
        return this.char_TxPower_UUID;
    }

    public final UUID getChar_ServiceData_UUID() {
        return this.char_ServiceData_UUID;
    }

    public final UUID getChar_BeaconUUID_UUID() {
        return this.char_BeaconUUID_UUID;
    }

    public final UUID getChar_CompanyId_UUID() {
        return this.char_CompanyId_UUID;
    }

    public final UUID getChar_RssiInOne_UUID() {
        return this.char_RssiInOne_UUID;
    }

    public final UUID getChar_Interval_UUID() {
        return this.char_Interval_UUID;
    }

    public final UUID getVersion_service_UUID() {
        return this.version_service_UUID;
    }

    public final UUID getVersion_char() {
        return this.version_char;
    }

    public final UUID getService_Other_UUID() {
        return this.Service_Other_UUID;
    }

    public final UUID getChar_Psw_UUID() {
        return this.char_Psw_UUID;
    }

    public final UUID getChar_DeviceName_UUID() {
        return this.char_DeviceName_UUID;
    }

    public final UUID getChar_notify_UUID() {
        return this.char_notify_UUID;
    }

    public final UUID getChar_setMode_UUID() {
        return this.char_setMode_UUID;
    }

    public final UUID getChar_ACC() {
        return this.char_ACC;
    }

    public final UUID getChar_WenduShidu() {
        return this.char_WenduShidu;
    }

    public final UUID getChar_read() {
        return this.char_read;
    }

    public final UUID getChar_get() {
        return this.char_get;
    }

    public final BluetoothGattService getBluetoothGattBeacon_service() {
        return this.BluetoothGattBeacon_service;
    }

    public final void setBluetoothGattBeacon_service(BluetoothGattService bluetoothGattService) {
        this.BluetoothGattBeacon_service = bluetoothGattService;
    }

    public final BluetoothGattCharacteristic getCharacteristic_minor() {
        return this.Characteristic_minor;
    }

    public final void setCharacteristic_minor(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_minor = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_major() {
        return this.Characteristic_major;
    }

    public final void setCharacteristic_major(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_major = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_TxPower() {
        return this.Characteristic_TxPower;
    }

    public final void setCharacteristic_TxPower(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_TxPower = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_ServiceData() {
        return this.Characteristic_ServiceData;
    }

    public final void setCharacteristic_ServiceData(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_ServiceData = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_BeaconUUID() {
        return this.Characteristic_BeaconUUID;
    }

    public final void setCharacteristic_BeaconUUID(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_BeaconUUID = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_CompanyId() {
        return this.Characteristic_CompanyId;
    }

    public final void setCharacteristic_CompanyId(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_CompanyId = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_RssiInOne() {
        return this.Characteristic_RssiInOne;
    }

    public final void setCharacteristic_RssiInOne(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_RssiInOne = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_Interval() {
        return this.Characteristic_Interval;
    }

    public final void setCharacteristic_Interval(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_Interval = bluetoothGattCharacteristic;
    }

    public final BluetoothGattService getBluetoothGattBeacon_service_version() {
        return this.BluetoothGattBeacon_service_version;
    }

    public final void setBluetoothGattBeacon_service_version(BluetoothGattService bluetoothGattService) {
        this.BluetoothGattBeacon_service_version = bluetoothGattService;
    }

    public final BluetoothGattService getBluetoothGattOther_service() {
        return this.BluetoothGattOther_service;
    }

    public final void setBluetoothGattOther_service(BluetoothGattService bluetoothGattService) {
        this.BluetoothGattOther_service = bluetoothGattService;
    }

    public final BluetoothGattCharacteristic getCharacteristic_psw() {
        return this.Characteristic_psw;
    }

    public final void setCharacteristic_psw(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_psw = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_notify() {
        return this.Characteristic_notify;
    }

    public final void setCharacteristic_notify(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_notify = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_DeviceName() {
        return this.Characteristic_DeviceName;
    }

    public final void setCharacteristic_DeviceName(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_DeviceName = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_setMode() {
        return this.Characteristic_setMode;
    }

    public final void setCharacteristic_setMode(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_setMode = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_read() {
        return this.Characteristic_read;
    }

    public final void setCharacteristic_read(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_read = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_get() {
        return this.Characteristic_get;
    }

    public final void setCharacteristic_get(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_get = bluetoothGattCharacteristic;
    }

    public final BluetoothGattCharacteristic getCharacteristic_notify_version() {
        return this.Characteristic_notify_version;
    }

    public final void setCharacteristic_notify_version(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.Characteristic_notify_version = bluetoothGattCharacteristic;
    }

    public final ConnectDataCallBack getConnectDataCallBack() {
        return this.connectDataCallBack;
    }

    public final void setConnectDataCallBack(ConnectDataCallBack connectDataCallBack2) {
        this.connectDataCallBack = connectDataCallBack2;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ConnectModel(Context context, ConnectDataCallBack connectDataCallBack2) {
        super(context);
        Intrinsics.checkNotNullParameter(connectDataCallBack2, "connectDataCallBack");
        Intrinsics.checkNotNull(context);
        UUID fromString = UUID.fromString("0000FFA0-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString, "fromString(\"0000FFA0-0000-1000-8000-00805F9B34FB\")");
        this.Service_Beacon_UUID = fromString;
        UUID fromString2 = UUID.fromString("0000FFA3-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString2, "fromString(\"0000FFA3-0000-1000-8000-00805F9B34FB\")");
        this.char_minor_UUID = fromString2;
        UUID fromString3 = UUID.fromString("0000FFA2-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString3, "fromString(\"0000FFA2-0000-1000-8000-00805F9B34FB\")");
        this.char_major_UUID = fromString3;
        UUID fromString4 = UUID.fromString("0000FFA7-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString4, "fromString(\"0000FFA7-0000-1000-8000-00805F9B34FB\")");
        this.char_TxPower_UUID = fromString4;
        UUID fromString5 = UUID.fromString("0000FFA4-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString5, "fromString(\"0000FFA4-0000-1000-8000-00805F9B34FB\")");
        this.char_ServiceData_UUID = fromString5;
        UUID fromString6 = UUID.fromString("0000FFA1-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString6, "fromString(\"0000FFA1-0000-1000-8000-00805F9B34FB\")");
        this.char_BeaconUUID_UUID = fromString6;
        UUID fromString7 = UUID.fromString("0000FFA5-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString7, "fromString(\"0000FFA5-0000-1000-8000-00805F9B34FB\")");
        this.char_CompanyId_UUID = fromString7;
        UUID fromString8 = UUID.fromString("0000FFA8-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString8, "fromString(\"0000FFA8-0000-1000-8000-00805F9B34FB\")");
        this.char_RssiInOne_UUID = fromString8;
        UUID fromString9 = UUID.fromString("0000FFA6-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString9, "fromString(\"0000FFA6-0000-1000-8000-00805F9B34FB\")");
        this.char_Interval_UUID = fromString9;
        UUID fromString10 = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString10, "fromString(\"0000180A-0000-1000-8000-00805F9B34FB\")");
        this.version_service_UUID = fromString10;
        UUID fromString11 = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString11, "fromString(\"00002A26-0000-1000-8000-00805F9B34FB\")");
        this.version_char = fromString11;
        UUID fromString12 = UUID.fromString("0000FFC0-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString12, "fromString(\"0000FFC0-0000-1000-8000-00805F9B34FB\")");
        this.Service_Other_UUID = fromString12;
        UUID fromString13 = UUID.fromString("0000FFC3-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString13, "fromString(\"0000FFC3-0000-1000-8000-00805F9B34FB\")");
        this.char_Psw_UUID = fromString13;
        UUID fromString14 = UUID.fromString("0000FFC2-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString14, "fromString(\"0000FFC2-0000-1000-8000-00805F9B34FB\")");
        this.char_DeviceName_UUID = fromString14;
        UUID fromString15 = UUID.fromString("0000FFC4-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString15, "fromString(\"0000FFC4-0000-1000-8000-00805F9B34FB\")");
        this.char_notify_UUID = fromString15;
        UUID fromString16 = UUID.fromString("0000FFC1-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString16, "fromString(\"0000FFC1-0000-1000-8000-00805F9B34FB\")");
        this.char_setMode_UUID = fromString16;
        UUID fromString17 = UUID.fromString("0000FFC5-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString17, "fromString(\"0000FFC5-0000-1000-8000-00805F9B34FB\")");
        this.char_ACC = fromString17;
        UUID fromString18 = UUID.fromString("0000FFC6-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString18, "fromString(\"0000FFC6-0000-1000-8000-00805F9B34FB\")");
        this.char_WenduShidu = fromString18;
        UUID fromString19 = UUID.fromString("0000FFC7-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString19, "fromString(\"0000FFC7-0000-1000-8000-00805F9B34FB\")");
        this.char_read = fromString19;
        UUID fromString20 = UUID.fromString("0000FFC8-0000-1000-8000-00805F9B34FB");
        Intrinsics.checkNotNullExpressionValue(fromString20, "fromString(\"0000FFC8-0000-1000-8000-00805F9B34FB\")");
        this.char_get = fromString20;
        this.connectDataCallBack = connectDataCallBack2;
    }

    public final void connectDevice(String str) {
        Intrinsics.checkNotNullParameter(str, "address");
        Object systemService = getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.bluetooth.BluetoothManager");
        BluetoothAdapter adapter = ((BluetoothManager) systemService).getAdapter();
        Log.e(MLog.ConnectE, "进入连接=" + str);
        this.address = str;
        connect(adapter.getRemoteDevice(str)).timeout(10000).retry(20, AGCServerException.UNKNOW_EXCEPTION).done((SuccessCallback) new SuccessCallback() {
            public final String address = str;
            public final ConnectModel model = ConnectModel.this;
            @Override
            public final void onRequestCompleted(BluetoothDevice bluetoothDevice) {
                ConnectModel.m13connectDevicelambda1(address, model, bluetoothDevice);
            }
        }).fail((FailCallback) new FailCallback() {
            public final String address = str;
            public final ConnectModel model = ConnectModel.this;
            @Override
            public final void onRequestFailed(BluetoothDevice bluetoothDevice, int i) {
                ConnectModel.connectDevicelambda4(ConnectModel.this, address, bluetoothDevice, i);
            }
        }).enqueue();
    }

    /* access modifiers changed from: private */
    /* renamed from: connectDevice$lambda-1  reason: not valid java name */
    public static final void m13connectDevicelambda1(String str, ConnectModel connectModel, BluetoothDevice bluetoothDevice) {
        Intrinsics.checkNotNullParameter(str, "$address");
        Intrinsics.checkNotNullParameter(connectModel, "this$0");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "it");
        ConnectDataCallBack connectDataCallBack2 = connectModel.connectDataCallBack;
        if (connectDataCallBack2 != null) {
            connectDataCallBack2.onConnected(str);
        }
        Log.e(MLog.ConnectE, "开始连接咯");
    }

    /* access modifiers changed from: private */
    /* renamed from: connectDevice$lambda-4  reason: not valid java name */
    public static final void connectDevicelambda4(ConnectModel connectModel, String str, BluetoothDevice bluetoothDevice, int i) {
        Intrinsics.checkNotNullParameter(connectModel, "this$0");
        Intrinsics.checkNotNullParameter(str, "$address");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        ConnectDataCallBack connectDataCallBack2 = connectModel.connectDataCallBack;
        if (connectDataCallBack2 != null) {
            connectDataCallBack2.onDisConnected(str);
        }
        Log.e(MLog.ConnectE, "连接超时" + str);
    }

    /* access modifiers changed from: protected */
    public BleManager.BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }
    /* compiled from: ConnectModel.kt */
    private final class MyManagerGattCallback extends BleManager.BleManagerGattCallback {
        public MyManagerGattCallback() {
        }

        @Override
        public boolean isRequiredServiceSupported(android.bluetooth.BluetoothGatt gatt) {
            /*
            // Check if the BluetoothGattService objects are not null
            if (BluetoothGattBeacon_service == null || BluetoothGattBeacon_service_version == null || BluetoothGattOther_service == null) {
                Log.e("ConnectModel", "One or more required services are null");
                return false;
            }
            */
            // Set BluetoothGattService objects
            setBluetoothGattBeacon_service(gatt.getService(getService_Beacon_UUID()));
            setBluetoothGattOther_service(gatt.getService(getService_Other_UUID()));
            setBluetoothGattBeacon_service_version(gatt.getService(getVersion_service_UUID()));

            // Check if BluetoothGattService objects are not null
            if (BluetoothGattBeacon_service != null) {
                BluetoothGattCharacteristic char_minor = BluetoothGattBeacon_service.getCharacteristic(getChar_minor_UUID());
                setCharacteristic_minor(char_minor);

                BluetoothGattCharacteristic char_major = BluetoothGattBeacon_service.getCharacteristic(getChar_major_UUID());
                setCharacteristic_major(char_major);

                BluetoothGattCharacteristic char_TxPower = BluetoothGattBeacon_service.getCharacteristic(getChar_TxPower_UUID());
                setCharacteristic_TxPower(char_TxPower);

                BluetoothGattCharacteristic char_ServiceData = BluetoothGattBeacon_service.getCharacteristic(getChar_ServiceData_UUID());
                setCharacteristic_ServiceData(char_ServiceData);

                BluetoothGattCharacteristic char_CompanyId = BluetoothGattBeacon_service.getCharacteristic(getChar_CompanyId_UUID());
                setCharacteristic_CompanyId(char_CompanyId);

                BluetoothGattCharacteristic char_RssiInOne = BluetoothGattBeacon_service.getCharacteristic(getChar_RssiInOne_UUID());
                setCharacteristic_RssiInOne(char_RssiInOne);

                BluetoothGattCharacteristic char_Interval = BluetoothGattBeacon_service.getCharacteristic(getChar_Interval_UUID());
                setCharacteristic_Interval(char_Interval);

                BluetoothGattCharacteristic char_BeaconUUID = BluetoothGattBeacon_service.getCharacteristic(getChar_BeaconUUID_UUID());
                setCharacteristic_BeaconUUID(char_BeaconUUID);
            }

            if (BluetoothGattBeacon_service_version != null) {
                BluetoothGattCharacteristic version_char = BluetoothGattBeacon_service_version.getCharacteristic(getVersion_char());
                setCharacteristic_notify_version(version_char);
            }

            if (BluetoothGattOther_service != null) {
                BluetoothGattCharacteristic char_get = BluetoothGattOther_service.getCharacteristic(getChar_get());
                setCharacteristic_get(char_get);

                BluetoothGattCharacteristic char_DeviceName = BluetoothGattOther_service.getCharacteristic(getChar_DeviceName_UUID());
                setCharacteristic_DeviceName(char_DeviceName);

                BluetoothGattCharacteristic char_psw = BluetoothGattOther_service.getCharacteristic(getChar_Psw_UUID());
                setCharacteristic_psw(char_psw);

                BluetoothGattCharacteristic char_notify = BluetoothGattOther_service.getCharacteristic(getChar_notify_UUID());
                setCharacteristic_notify(char_notify);

                BluetoothGattCharacteristic char_setMode = BluetoothGattOther_service.getCharacteristic(getChar_setMode_UUID());
                setCharacteristic_setMode(char_setMode);

                BluetoothGattCharacteristic char_read = BluetoothGattOther_service.getCharacteristic(getChar_read());
                setCharacteristic_read(char_read);
            }

            // Logging for verification
            Log.e("连接Log", "UUID=" + getChar_get());
            if (BluetoothGattOther_service != null && BluetoothGattOther_service.getCharacteristics() != null) {
                int size = BluetoothGattOther_service.getCharacteristics().size();
                for (int i = 1; i <= size; i++) {
                    BluetoothGattCharacteristic characteristic = BluetoothGattOther_service.getCharacteristics().get(i - 1);
                    Log.e("UUID=", String.valueOf(characteristic.getUuid()));
                    Log.e("UUID=", String.valueOf(characteristic));
                }
            }

            // Return true if all services are supported
            return true;
        }


        /* access modifiers changed from: protected */
        public boolean isOptionalServiceSupported(BluetoothGatt bluetoothGatt) {
            Intrinsics.checkNotNullParameter(bluetoothGatt, "gatt");
            Log.e(MLog.ConnectE, "isOptionalServiceSupported=" + getBluetoothDevice().getAddress());
            return super.isOptionalServiceSupported(bluetoothGatt);
        }

        /* access modifiers changed from: protected */
        public void initialize() {
            Log.e(MLog.ConnectE, "initialize初始化=" + getBluetoothDevice().getAddress());
            Log.e(MLog.ConnectE, "initialize初始化=" + ConnectModel.this.getCharacteristic_get());
            Log.e(MLog.ConnectE, "initialize初始化=" + ConnectModel.this.getCharacteristic_notify());
            Log.e(MLog.ConnectE, "initialize初始化=" + ConnectModel.this.getCharacteristic_notify_version());
            RequestQueue access$beginAtomicRequestQueue = ConnectModel.this.beginAtomicRequestQueue();
            ConnectModel connectModel = ConnectModel.this;
            RequestQueue add = access$beginAtomicRequestQueue.add(connectModel.enableNotifications(connectModel.getCharacteristic_notify()));
            ConnectModel connectModel2 = ConnectModel.this;
            add.add(connectModel2.enableNotifications(connectModel2.getCharacteristic_get())).done((SuccessCallback) new SuccessCallback() {
                public final ConnectModel model = ConnectModel.this;
                @Override
                public final void onRequestCompleted(BluetoothDevice bluetoothDevice) {
                    ConnectModel.initializelambda2(ConnectModel.MyManagerGattCallback.this, model, bluetoothDevice);
                }
            }).fail((FailCallback) new FailCallback() {
                public final ConnectModel model = ConnectModel.this;

                @Override
                public final void onRequestFailed(BluetoothDevice bluetoothDevice, int i) {
                    ConnectModel.initializelambda4(ConnectModel.MyManagerGattCallback.this, model, bluetoothDevice, i);
                }
            }).enqueue();
            ConnectModel connectModel3 = ConnectModel.this;
            connectModel3.setNotificationCallback(connectModel3.getCharacteristic_notify()).with(ConnectModel.this.getReceivedCallback());
            ConnectModel connectModel4 = ConnectModel.this;
            connectModel4.setNotificationCallback(connectModel4.getCharacteristic_get()).with(ConnectModel.this.getReceivedCallback1());
        }



        /* access modifiers changed from: protected */
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Intrinsics.checkNotNullParameter(bluetoothGatt, "gatt");
            Intrinsics.checkNotNullParameter(bluetoothGattCharacteristic, "characteristic");
            super.onCharacteristicRead(bluetoothGatt, bluetoothGattCharacteristic);
            System.out.println("数据=" + bluetoothGattCharacteristic.getUuid() + "   " + StringUtils.ByteArrToHex(bluetoothGattCharacteristic.getValue()));
        }

        /* access modifiers changed from: protected */
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Intrinsics.checkNotNullParameter(bluetoothGatt, "gatt");
            Intrinsics.checkNotNullParameter(bluetoothGattCharacteristic, "characteristic");
            super.onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic);
            System.out.println("11数据=" + bluetoothGattCharacteristic.getUuid() + "   " + StringUtils.ByteArrToHex(bluetoothGattCharacteristic.getValue()));
        }

        /* access modifiers changed from: protected */
        public void onDeviceDisconnected() {
            ConnectDataCallBack connectDataCallBack;
            ConnectModel.this.setBluetoothGattBeacon_service((BluetoothGattService) null);
            ConnectModel.this.setCharacteristic_minor((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_major((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_TxPower((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_ServiceData((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_BeaconUUID((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_CompanyId((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_RssiInOne((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_Interval((BluetoothGattCharacteristic) null);
            ConnectModel.this.setBluetoothGattOther_service((BluetoothGattService) null);
            ConnectModel.this.setCharacteristic_psw((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_notify((BluetoothGattCharacteristic) null);
            ConnectModel.this.setCharacteristic_DeviceName((BluetoothGattCharacteristic) null);
            Log.e(MLog.ConnectE, "onDeviceDisconnected断开连接=");
            String address = ConnectModel.this.getAddress();
            if (address != null && (connectDataCallBack = ConnectModel.this.getConnectDataCallBack()) != null) {
                connectDataCallBack.onDisConnected(address);
            }
        }

        @Override
        protected void onServicesInvalidated() {
            //Log.d(MLog.ConnectE, "onServicesInvalidated");
        }
    }


    /* access modifiers changed from: private */
    /* renamed from: initialize$lambda-2  reason: not valid java name */
    public static final void initializelambda2(MyManagerGattCallback myManagerGattCallback, ConnectModel connectModel, BluetoothDevice bluetoothDevice) {
        Log.e(MLog.ConnectE, "使能通知成功" + myManagerGattCallback.getBluetoothDevice().getAddress());
        connectModel.sleep(500).enqueue();
        connectModel.writeCharacteristic(connectModel.getCharacteristic_read(), StringUtils.HexToByteArr("2405")).done((SuccessCallback) new SuccessCallback() {
            public final void onRequestCompleted(BluetoothDevice bluetoothDevice) {
                ConnectModel.initializelambda2lambda0(myManagerGattCallback, bluetoothDevice);
            }
        }).enqueue();
        connectModel.sleep(500).enqueue();
        connectModel.writeCharacteristic(connectModel.getCharacteristic_read(), StringUtils.HexToByteArr("2404")).done((SuccessCallback) new SuccessCallback() {
            public final void onRequestCompleted(BluetoothDevice bluetoothDevice) {
                ConnectModel.initializelambda2lambda1(myManagerGattCallback, bluetoothDevice);
            }
        }).enqueue();
        connectModel.sleep(500).enqueue();
        Log.e(MLog.ConnectE, "进行读取版本号" + myManagerGattCallback.getBluetoothDevice().getAddress());
        connectModel.readCharacteristic(connectModel.getCharacteristic_notify_version()).with(connectModel.getReceivedCallback_version()).enqueue();
    }

    /* access modifiers changed from: private */
    /* renamed from: initialize$lambda-2$lambda-0  reason: not valid java name */
    public static final void initializelambda2lambda0(MyManagerGattCallback myManagerGattCallback, BluetoothDevice bluetoothDevice) {
        //Intrinsics.checkNotNullParameter(myManagerGattCallback, "this$0");
        //Intrinsics.checkNotNullParameter(bluetoothDevice, "it");
        Log.e(MLog.ConnectE, "写设备名称发送成功" + myManagerGattCallback.getBluetoothDevice().getAddress());
    }

    /* access modifiers changed from: private */
    /* renamed from: initialize$lambda-2$lambda-1  reason: not valid java name */
    public static final void initializelambda2lambda1(MyManagerGattCallback myManagerGattCallback, BluetoothDevice bluetoothDevice) {
        Log.e(MLog.ConnectE, "写设备名称发送成功" + myManagerGattCallback.getBluetoothDevice().getAddress());
    }

    /* access modifiers changed from: private */
    /* renamed from: initialize$lambda-4  reason: not valid java name */
    public static final void initializelambda4(MyManagerGattCallback myManagerGattCallback, ConnectModel connectModel, BluetoothDevice bluetoothDevice, int i) {
        Intrinsics.checkNotNullParameter(myManagerGattCallback, "this$0");
        Intrinsics.checkNotNullParameter(connectModel, "this$1");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        Log.e(MLog.ConnectE, "使能通知失败了" + myManagerGattCallback.getBluetoothDevice().getAddress());
        Log.e(MLog.ConnectE, "使能通知失败了" + connectModel.getCharacteristic_notify_version());
        Log.e(MLog.ConnectE, "使能通知失败了" + connectModel.getReceivedCallback_version());
        connectModel.readCharacteristic(connectModel.getCharacteristic_notify_version()).with(connectModel.getReceivedCallback_version()).enqueue();
    }

    public final void sendPasswrod(byte[] bArr) {
        println("Send password: " + bArr.toString());

        writeCharacteristic(this.Characteristic_psw, bArr).done((SuccessCallback) new SuccessCallback() {
            public final byte[] array = bArr;

            @Override
            public final void onRequestCompleted(BluetoothDevice bluetoothDevice) {
                ConnectModel.sendPasswrodlambda5(array, bluetoothDevice);
            }
        }).enqueue();
    }

    /* access modifiers changed from: private */
    /* renamed from: sendPasswrod$lambda-5  reason: not valid java name */
    public static final void sendPasswrodlambda5(byte[] bArr, BluetoothDevice bluetoothDevice) {
        Intrinsics.checkNotNullParameter(bArr, "$data");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "it");
        Log.e(MLog.ConnectE, "发送密码" + StringUtils.ByteArrToHex(bArr));
    }

    public final void read() {
        BluetoothGattService bluetoothGattService = this.BluetoothGattOther_service;
        BluetoothGattCharacteristic bluetoothGattCharacteristic = null;
        BluetoothGattCharacteristic characteristic = bluetoothGattService != null ? bluetoothGattService.getCharacteristic(this.char_WenduShidu) : null;
        BluetoothGattService bluetoothGattService2 = this.BluetoothGattOther_service;
        if (bluetoothGattService2 != null) {
            bluetoothGattCharacteristic = bluetoothGattService2.getCharacteristic(this.char_ACC);
        }
        if (characteristic != null) {
            readCharacteristic(characteristic).with(this.receivedCallback_Temp).enqueue();
            readCharacteristic(bluetoothGattCharacteristic).with(this.receivedCallback_Acc).enqueue();
            return;
        }
        System.out.println("666666666666666666");
    }

    public final void sendValue(byte[] data, Type type) {
        Intrinsics.checkNotNullParameter(data, "data");
        Intrinsics.checkNotNullParameter(type, "type");
        BluetoothGattCharacteristic characteristic = null;
        switch (type.ordinal()) {
            case 1:
                characteristic = Characteristic_DeviceName;
                break;
            case 2:
                characteristic = Characteristic_BeaconUUID;
                Intrinsics.checkNotNull(characteristic);
                break;
            case 3:
                characteristic = Characteristic_ServiceData;
                break;
            case 4:
                characteristic = Characteristic_major;
                break;
            case 5:
                characteristic = Characteristic_minor;
                break;
            case 6:
                characteristic = Characteristic_TxPower;
                break;
            case 7:
                characteristic = Characteristic_RssiInOne;
                break;
            case 8:
                characteristic = Characteristic_Interval;
                break;
            case 9:
                characteristic = Characteristic_setMode;
                break;
            case 10:
                characteristic = BluetoothGattOther_service != null ? BluetoothGattOther_service.getCharacteristic(char_WenduShidu) : null;
                break;
            case 11:
                characteristic = BluetoothGattOther_service != null ? BluetoothGattOther_service.getCharacteristic(char_ACC) : null;
                break;
        }

        System.out.println("开始发送" + type + "   消息=" + StringUtils.ByteArrToHex(data));
        WriteRequest request = writeCharacteristic(characteristic, data);
        // request.done(new -$$Lambda$ConnectModel$QoxzDY88MBNEX5e3O7rXNufjfrg(data));
        request.enqueue();
    }

    /* access modifiers changed from: private */
    /* renamed from: sendValue$lambda-6  reason: not valid java name */
    public static final void m23sendValue$lambda6(Type type, byte[] bArr, BluetoothDevice bluetoothDevice) {
        Intrinsics.checkNotNullParameter(type, "$type");
        Intrinsics.checkNotNullParameter(bArr, "$data");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "it");
        Log.e(MLog.ConnectE, "成功发送 Type= " + type + StringUtils.ByteArrToHex(bArr));
    }

    public final DataReceivedCallback getReceivedCallback() {
        return this.receivedCallback;
    }

    public final void setReceivedCallback(DataReceivedCallback dataReceivedCallback) {
        Intrinsics.checkNotNullParameter(dataReceivedCallback, "<set-?>");
        this.receivedCallback = dataReceivedCallback;
    }

    /* access modifiers changed from: private */
    /* renamed from: receivedCallback$lambda-10  reason: not valid java name */
    public static void receivedCallbacklambda10(ConnectModel connect_model, BluetoothDevice bluetoothDevice, Data data) {

        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        Intrinsics.checkNotNullParameter(data, "data");
        StringBuilder append = new StringBuilder().append("通知数据=");
        byte[] value = data.getValue();
        Log.e(MLog.ConnectE, append.append(value != null ? new String(value, Charsets.UTF_8) : null).toString());
        byte[] value2 = data.getValue();
        if (value2 != null) {
            String str = new String(value2, Charsets.UTF_8);
            ConnectDataCallBack connectDataCallBack2 = connect_model.connectDataCallBack;
            if (connectDataCallBack2 != null) {
                connectDataCallBack2.onData(str);
            }
        }
    }

    public final DataReceivedCallback getReceivedCallback1() {
        return this.receivedCallback1;
    }

    public final void setReceivedCallback1(DataReceivedCallback dataReceivedCallback) {
        Intrinsics.checkNotNullParameter(dataReceivedCallback, "<set-?>");
        this.receivedCallback1 = dataReceivedCallback;
    }

    /* access modifiers changed from: private */
    /* renamed from: receivedCallback1$lambda-12  reason: not valid java name */
    public static final void receivedCallback1lambda12(ConnectModel connectModel, BluetoothDevice bluetoothDevice, Data data) {
        ConnectDataCallBack connectDataCallBack2;
        //Intrinsics.checkNotNullParameter(connectModel, "this$0");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        Intrinsics.checkNotNullParameter(data, "data");
        byte[] value = data.getValue();
        if (value != null && (connectDataCallBack2 = connectModel.connectDataCallBack) != null) {
            connectDataCallBack2.onData1(value);
        }
    }

    public final DataReceivedCallback getReceivedCallback_Acc() {
        return this.receivedCallback_Acc;
    }

    public final void setReceivedCallback_Acc(DataReceivedCallback dataReceivedCallback) {
        Intrinsics.checkNotNullParameter(dataReceivedCallback, "<set-?>");
        this.receivedCallback_Acc = dataReceivedCallback;
    }

    /* access modifiers changed from: private */
    /* renamed from: receivedCallback_Acc$lambda-14  reason: not valid java name */
    public static final void receivedCallback_Acclambda14(ConnectModel connectModel, BluetoothDevice bluetoothDevice, Data data) {
        ConnectDataCallBack connectDataCallBack2;
        // Intrinsics.checkNotNullParameter(connectModel, "this$0");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        Intrinsics.checkNotNullParameter(data, "data");
        byte[] value = data.getValue();
        if (value != null && (connectDataCallBack2 = connectModel.connectDataCallBack) != null) {
            connectDataCallBack2.onDataAcc(value);
        }
    }

    public final DataReceivedCallback getReceivedCallback_version() {
        return this.receivedCallback_version;
    }

    public final void setReceivedCallback_version(DataReceivedCallback dataReceivedCallback) {
        Intrinsics.checkNotNullParameter(dataReceivedCallback, "<set-?>");
        this.receivedCallback_version = dataReceivedCallback;
    }

    /* access modifiers changed from: private */
    /* renamed from: receivedCallback_version$lambda-17  reason: not valid java name */
    public static final void receivedCallback_versionlambda17(ConnectModel connectModel, BluetoothDevice bluetoothDevice, Data data) {
        ConnectDataCallBack connectDataCallBack2;
        //Intrinsics.checkNotNullParameter(connectModel, "this$0");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        Intrinsics.checkNotNullParameter(data, "data");
        StringBuilder append = new StringBuilder().append("通知数据66=");
        byte[] value = data.getValue();
        Log.e(MLog.ConnectE, append.append(value != null ? new String(value, Charsets.UTF_8) : null).toString());
        byte[] value2 = data.getValue();
        if (value2 != null && (connectDataCallBack2 = connectModel.connectDataCallBack) != null) {
            connectDataCallBack2.onDataVersion(value2);
        }
    }

    public final DataReceivedCallback getReceivedCallback_Temp() {
        return this.receivedCallback_Temp;
    }

    public final void setReceivedCallback_Temp(DataReceivedCallback dataReceivedCallback) {
        Intrinsics.checkNotNullParameter(dataReceivedCallback, "<set-?>");
        this.receivedCallback_Temp = dataReceivedCallback;
    }

    /* access modifiers changed from: private */
    /* renamed from: receivedCallback_Temp$lambda-19  reason: not valid java name */
    public static final void receivedCallback_Templambda19(ConnectModel connectModel, BluetoothDevice bluetoothDevice, Data data) {
        ConnectDataCallBack connectDataCallBack2;
        Intrinsics.checkNotNullParameter(connectModel, "this$0");
        Intrinsics.checkNotNullParameter(bluetoothDevice, "<anonymous parameter 0>");
        Intrinsics.checkNotNullParameter(data, "data");
        byte[] value = data.getValue();
        if (value != null && (connectDataCallBack2 = connectModel.connectDataCallBack) != null) {
            connectDataCallBack2.onDataTemp(value);
        }
    }
}
