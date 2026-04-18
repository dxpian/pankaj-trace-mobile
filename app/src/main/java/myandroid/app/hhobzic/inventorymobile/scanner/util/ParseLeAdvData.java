package myandroid.app.hhobzic.inventorymobile.scanner.util;

import android.util.Log;

public final class ParseLeAdvData {
    public static final short BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE = 7;
    public static final short BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE = 6;
    public static final short BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_COMPLETE = 3;
    public static final short BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE = 2;
    public static final short BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_COMPLETE = 5;
    public static final short BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_MORE_AVAILABLE = 4;
    public static final short BLE_GAP_AD_TYPE_APPEARANCE = 25;
    public static final short BLE_GAP_AD_TYPE_CLASS_OF_DEVICE = 13;
    public static final short BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME = 9;
    public static final short BLE_GAP_AD_TYPE_FLAGS = 1;
    public static final short BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA = 255;
    public static final short BLE_GAP_AD_TYPE_PUBLIC_TARGET_ADDRESS = 23;
    public static final short BLE_GAP_AD_TYPE_RANDOM_TARGET_ADDRESS = 24;
    public static final short BLE_GAP_AD_TYPE_SECURITY_MANAGER_OOB_FLAGS = 17;
    public static final short BLE_GAP_AD_TYPE_SECURITY_MANAGER_TK_VALUE = 16;
    public static final short BLE_GAP_AD_TYPE_SERVICE_DATA = 22;
    public static final short BLE_GAP_AD_TYPE_SHORT_LOCAL_NAME = 8;
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C = 14;
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R = 15;
    public static final short BLE_GAP_AD_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE = 18;
    public static final short BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_128BIT = 21;
    public static final short BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_16BIT = 20;
    public static final short BLE_GAP_AD_TYPE_TX_POWER_LEVEL = 10;
    private static final String TAG = "ParseLeAdvData";

    public ParseLeAdvData() {
        //Log.d(TAG, "ParseLeAdvData init....");
    }

    public static byte[] adv_report_parse(short s, byte[] bArr) {
        int length = bArr.length;
        int i = 0;
        while (i < length) {
            try {
                byte b = bArr[i];
                if (bArr[i + 1] == ((byte) s)) {
                    int i2 = b - 1;
                    byte[] bArr2 = new byte[i2];
                    for (byte b2 = 0; b2 < i2; b2 = (byte) (b2 + 1)) {
                        bArr2[b2] = bArr[i + 2 + b2];
                    }
                    return bArr2;
                }
                i += b + 1;
                if (i >= 60) {
                    return null;
                }
            } catch (Exception unused) {
                //Log.d(TAG, "There is a exception here.");
            }
        }
        return null;
    }
}
