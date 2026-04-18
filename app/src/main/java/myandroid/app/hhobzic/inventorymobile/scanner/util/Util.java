package myandroid.app.hhobzic.inventorymobile.scanner.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import myandroid.app.hhobzic.inventorymobile.home.AppMacro;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Beacon;

import kotlin.UByte;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class Util {
    public static final boolean GPSisOPen(Context context) {
        //LocationManager locationManager = (LocationManager) context.getSystemService("location");
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network");
    }

    public static final boolean BleIsOPen(Context context) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null && defaultAdapter.isEnabled()) {
            return true;
        }
        return false;
    }

    public static final void openGPS(Activity activity) {
        activity.startActivityForResult(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"), 0);
    }

    public static final void openBle(Activity activity) {
        activity.startActivityForResult(new Intent("android.settings.BLUETOOTH_SETTINGS"), 0);
    }

    public static Beacon getDevice(ScanResult scanResult, Beacon beacon) {
        Beacon beacon2 = beacon == null ? new Beacon() : beacon;
        beacon2.setAddress(scanResult.getDevice().getAddress());
        beacon2.setRssi(scanResult.getRssi());
        byte[] bytes = scanResult.getScanRecord().getBytes();
        beacon2.setRaw(StringUtils.ByteArrToHex(bytes).replaceAll(" ", ""));
        byte[] adv_report_parse = ParseLeAdvData.adv_report_parse((short)255, bytes);
        byte[] adv_report_parse2 = ParseLeAdvData.adv_report_parse((short)9, bytes);
        if (adv_report_parse == null || adv_report_parse.length < 25 || adv_report_parse[2] != 2) {
            printMessage("aaMAC=" + scanResult.getDevice().getAddress());
            return null;
        }
        beacon2.setCompanyId(StringUtils.ByteArrToHex(new byte[]{adv_report_parse[0], adv_report_parse[1]}).replaceAll(" ", ""));
        byte[] bArr = new byte[16];
        int i = 0;
        for (int i2 = 4; i2 <= 19; i2++) {
            bArr[i] = adv_report_parse[i2];
            i++;
        }
        beacon2.setUuid(StringUtils.ByteArrToHex(bArr).replace(" ", ""));
        beacon2.setMajor(Integer.parseInt(StringUtils.ByteArrToHex(new byte[]{adv_report_parse[20], adv_report_parse[21]}).replace(" ", ""), 16));
        beacon2.setMinor(Integer.parseInt(StringUtils.ByteArrToHex(new byte[]{adv_report_parse[22], adv_report_parse[23]}).replace(" ", ""), 16));
        beacon2.setRssiInOne(adv_report_parse[24]);
        printMessage("bbMAC=" + scanResult.getDevice().getAddress());
        if (adv_report_parse.length == 26) {
            beacon2.setBt(adv_report_parse[25]);
        }
        if (adv_report_parse2 == null || adv_report_parse2.length <= 0) {
            beacon2.setDeviceName("N/A");
        } else {
            beacon2.setDeviceName(new String(adv_report_parse2));
        }
        beacon2.setOnlyBeacon(false);
        byte[] adv_report_parse3 = ParseLeAdvData.adv_report_parse((short)10, bytes);
        if (adv_report_parse3 != null) {
            beacon2.setTxPower("" + adv_report_parse3[0]);
        }
        byte[] adv_report_parse4 = ParseLeAdvData.adv_report_parse((short)22, bytes);
        if (adv_report_parse4 != null && adv_report_parse4.length >= 3 && adv_report_parse4[1] == 115) {
            beacon2.setBt(255 & adv_report_parse4[0]);
            beacon2.setInterval(adv_report_parse4[2] * 100);
            printMessage("广播间隔=" + beacon2.getInterval());
            if (adv_report_parse4.length < 11) {
                return beacon2;
            }
            byte[] bArr2 = new byte[(adv_report_parse4.length - 7)];
            int i3 = 0;
            for (int i4 = 3; i4 < adv_report_parse4.length - 4; i4++) {
                bArr2[i3] = adv_report_parse4[i4];
                i3++;
            }
            beacon2.setServiceData(StringUtils.ByteArrToHex(bArr2));
            char[] charArray = StringUtils.binary2(adv_report_parse4[9]).toCharArray();
            printMessage(beacon2.getAddress() + "d'd'd'd'd'd'd'd'd'd'd'd'd'd'd=" + StringUtils.binary2(adv_report_parse4[9]));
            if (charArray[6] == '1' && adv_report_parse4.length == 13) {
                beacon2.isSupportTempShidu(true);
                beacon2.setTemp("" + adv_report_parse4[11]);
                beacon2.setHumidity("" + adv_report_parse4[12]);
                if (charArray[7] == '1') {
                    beacon2.setOpenTemp(true);
                    printMessage("log11===" + beacon2.getAddress() + beacon2.getOpenTemp());
                } else {
                    beacon2.setOpenTemp(false);
                    printMessage("log222===" + beacon2.getAddress() + beacon2.getOpenTemp());
                }
            } else {
                beacon2.isSupportTempShidu(false);
                beacon2.setOpenTemp(false);
                printMessage("log33===" + beacon2.getAddress() + beacon2.getOpenTemp());
            }
            printMessage(beacon2.getAddress() + "  温度=" + beacon2.getTemp() + "湿度=" + beacon2.getHumidity() + "移动=" + adv_report_parse4[10]);
            if (charArray[4] == '1' && adv_report_parse4.length == 13) {
                beacon2.setIsMove(adv_report_parse4[10]);
                beacon2.isSupportAcc(true);
                if (charArray[5] == '1') {
                    beacon2.isOpenAcc(true);
                } else {
                    beacon2.isOpenAcc(false);
                }
            } else {
                beacon2.isSupportAcc(false);
                beacon2.isOpenAcc(false);
            }
        } else if (adv_report_parse4 != null && adv_report_parse4.length == 17 && adv_report_parse4[1] == 75 && adv_report_parse4[0] == 76) {
            int i5 = ((adv_report_parse4[2] & UByte.MAX_VALUE) * UByte.MIN_VALUE) + (adv_report_parse4[3] & UByte.MAX_VALUE);
            printMessage("用户数据" + ((255 & adv_report_parse4[2]) * UByte.MIN_VALUE));
            beacon2.setBt(i5);
            beacon2.setInterval(adv_report_parse4[4] * 100);
            beacon2.setTxPower("" + adv_report_parse4[5]);
            byte[] bArr3 = new byte[(adv_report_parse4.length - 11)];
            int i6 = 6;
            int i7 = 0;
            for (int i8 = 5; i6 < adv_report_parse4.length - i8; i8 = 5) {
                bArr3[i7] = adv_report_parse4[i6];
                i7++;
                i6++;
            }
            beacon2.setServiceData(StringUtils.ByteArrToHex(bArr3));
            char[] charArray2 = StringUtils.binary2(adv_report_parse4[12]).toCharArray();
            printMessage(beacon2.getAddress() + "=" + StringUtils.binary2(adv_report_parse4[9]));
            if (charArray2[6] == '1') {
                beacon2.isSupportTempShidu(true);
                if (adv_report_parse4[15] < 10) {
                    beacon2.setTemp("" + adv_report_parse4[14] + ".0" + adv_report_parse4[15]);
                } else {
                    beacon2.setTemp("" + adv_report_parse4[14] + "." + adv_report_parse4[15]);
                }
                beacon2.setHumidity("" + adv_report_parse4[16]);
                if (charArray2[7] == '1') {
                    beacon2.setOpenTemp(true);
                    printMessage("log11===" + beacon2.getAddress() + beacon2.getOpenTemp());
                } else {
                    beacon2.setOpenTemp(false);
                    printMessage("log222===" + beacon2.getAddress() + beacon2.getOpenTemp());
                }
            } else {
                beacon2.isSupportTempShidu(false);
                beacon2.setOpenTemp(false);
                printMessage("log33===" + beacon2.getAddress() + beacon2.getOpenTemp());
            }
            if (charArray2[4] == '1') {
                beacon2.setIsMove(adv_report_parse4[13]);
                beacon2.isSupportAcc(true);
                if (charArray2[5] == '1') {
                    beacon2.isOpenAcc(true);
                } else {
                    beacon2.isOpenAcc(false);
                }
            } else {
                beacon2.isSupportAcc(false);
                beacon2.isOpenAcc(false);
            }
        } else {
            beacon2.setIsConnect(true);
            beacon2.setIsMove(2);
        }
        printMessage("log444===" + beacon2.getAddress() + beacon2.getOpenTemp() + "            " + beacon2.isOpenAcc());
        return beacon2;
    }

    private static void printMessage(String message){
        if(AppMacro.ENABLE_SCAN_VERBOSE)
            System.out.println(message);
    }
}
