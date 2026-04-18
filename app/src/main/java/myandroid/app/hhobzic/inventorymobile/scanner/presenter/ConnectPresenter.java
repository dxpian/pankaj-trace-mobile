package myandroid.app.hhobzic.inventorymobile.scanner.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import myandroid.app.hhobzic.inventorymobile.scanner.presenter.IBeaconDetailView;
import myandroid.app.hhobzic.inventorymobile.R;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.MLog;
import myandroid.app.hhobzic.inventorymobile.scanner.bean.Type;
import myandroid.app.hhobzic.inventorymobile.scanner.model.ConnectDataCallBack;
import myandroid.app.hhobzic.inventorymobile.scanner.model.ConnectModel;
import myandroid.app.hhobzic.inventorymobile.scanner.util.StringUtils;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import no.nordicsemi.android.ble.DisconnectRequest;

/* compiled from: ConnectPresenter.kt */
public final class ConnectPresenter implements IConnectPresenter, ConnectDataCallBack {
    private String address;
    private IBeaconDetailView beaconDetailView;
    private ConnectModel connectModel;
    private Context context;

    public void setCompanyId(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "byte");
    }

    public void setDeviceName(String str) {
        Intrinsics.checkNotNullParameter(str, "name");
    }

    public void setInterval(int i) {
    }

    public void setMajor(int i) {
    }

    public void setMinor(int i) {
    }

    public void setPassword(String str) {
        Intrinsics.checkNotNullParameter(str, "psw");
    }

    public void setRssiInOne(int i) {
    }

    public void setServiceData(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "data");
    }

    public void setTxPower(byte b) {
    }

    public void setUUID(String str) {
        Intrinsics.checkNotNullParameter(str, "uuid");
    }

    public final String getAddress() {
        return this.address;
    }

    public final void setAddress(String str) {
        this.address = str;
    }

    public final Context getContext() {
        return this.context;
    }

    public final void setContext(Context context2) {
        this.context = context2;
    }

    public final IBeaconDetailView getBeaconDetailView() {
        return this.beaconDetailView;
    }

    public final void setBeaconDetailView(IBeaconDetailView iBeaconDetailView) {
        this.beaconDetailView = iBeaconDetailView;
    }

    public final ConnectModel getConnectModel() {
        return this.connectModel;
    }

    public final void setConnectModel(ConnectModel connectModel2) {
        this.connectModel = connectModel2;
    }

    public ConnectPresenter(Context context2, IBeaconDetailView iBeaconDetailView) {
        Intrinsics.checkNotNullParameter(context2, "context");
        Intrinsics.checkNotNullParameter(iBeaconDetailView, "beaconDetailView");
        this.context = context2;
        this.beaconDetailView = iBeaconDetailView;
        this.connectModel = new ConnectModel(context2, this);
    }

    public void connect(String str) {
        Intrinsics.checkNotNullParameter(str, "address");
        this.address = str;
        Log.e(MLog.ConnectE, "Presener连接=" + str);
        ConnectModel connectModel2 = this.connectModel;
        if (connectModel2 != null) {
            connectModel2.connectDevice(str);
        }
    }

    public final void reConnect() {
        ConnectModel connectModel2;
        Log.e(MLog.ConnectE, "Presener连接=" + this.address);
        String str = this.address;
        if (str != null && (connectModel2 = this.connectModel) != null) {
            connectModel2.connectDevice(str);
        }
    }

    public void disconnect() {
        DisconnectRequest disconnect;
        ConnectModel connectModel2 = this.connectModel;
        if (connectModel2 != null && (disconnect = connectModel2.disconnect()) != null) {
            disconnect.enqueue();
        }
    }

    public void sendPassword(String str) {
        Intrinsics.checkNotNullParameter(str, "psw");
        byte[] bytes = str.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "this as java.lang.String).getBytes(charset)");
        byte[] bArr = new byte[(bytes.length + 2)];
        bArr[0] = 36;
        bArr[1] = 0;
        System.arraycopy(bytes, 0, bArr, 2, bytes.length);
        System.out.println("密码1=" + StringUtils.ByteArrToHex(bArr));

        if (this.connectModel != null) {
            this.connectModel.sendPasswrod(bArr);
        }
    }

    public void sendChangePassword(String str) {
        Intrinsics.checkNotNullParameter(str, "psw");
        byte[] bytes = str.getBytes(Charsets.UTF_8);
        Intrinsics.checkNotNullExpressionValue(bytes, "this as java.lang.String).getBytes(charset)");
        byte[] bArr = new byte[(bytes.length + 2)];
        bArr[0] = 36;
        bArr[1] = 1;
        System.arraycopy(bytes, 0, bArr, 2, bytes.length);
        System.out.println("密码2=" + StringUtils.ByteArrToHex(bArr));
        ConnectModel connectModel2 = this.connectModel;
        if (connectModel2 != null) {
            connectModel2.sendPasswrod(bArr);
        }
    }

    public final void sendValue(byte[] bArr, Type type) {
        Intrinsics.checkNotNullParameter(bArr, "value");
        Intrinsics.checkNotNullParameter(type, "type");
        ConnectModel connectModel2 = this.connectModel;
        if (connectModel2 != null) {
            connectModel2.sendValue(bArr, type);
        }
    }

    public void close() {
        ConnectModel connectModel2 = this.connectModel;
        if (connectModel2 != null) {
            connectModel2.close();
        }
    }

    public void onData1(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "data");
        IBeaconDetailView iBeaconDetailView = this.beaconDetailView;
        if (iBeaconDetailView != null) {
            iBeaconDetailView.showData(bArr);
        }
    }

    public void onData(String str) {
        Resources resources;
        Resources resources2;
        Resources resources3;
        Resources resources4;
        Resources resources5;
        Resources resources6;
        Resources resources7;
        Resources resources8;
        IBeaconDetailView iBeaconDetailView;
        Resources resources9;
        Resources resources10;
        Resources resources11;
        Resources resources12;
        Resources resources13;
        Resources resources14;
        Resources resources15;
        Resources resources16;
        Resources resources17;
        Resources resources18;
        Resources resources19;
        Intrinsics.checkNotNullParameter(str, "data");
        String str2 = null;
        switch (str.hashCode()) {
            case -1361211138:
                if (str.equals("$#0C3\r\n")) {
                    Context context2 = this.context;
                    if (!(context2 == null || (resources = context2.getResources()) == null)) {
                        str2 = resources.getString(R.string.connect_passwordError);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    IBeaconDetailView iBeaconDetailView2 = this.beaconDetailView;
                    if (iBeaconDetailView2 != null) {
                        iBeaconDetailView2.connectDis();
                        return;
                    }
                    return;
                }
                return;
            case -1275385189:
                if (str.equals("$&0A1\r\n")) {
                    Context context3 = this.context;
                    if (!(context3 == null || (resources2 = context3.getResources()) == null)) {
                        str2 = resources2.getString(R.string.change_uuid_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275384228:
                if (str.equals("$&0A2\r\n")) {
                    Context context4 = this.context;
                    if (!(context4 == null || (resources3 = context4.getResources()) == null)) {
                        str2 = resources3.getString(R.string.change_major_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275383267:
                if (str.equals("$&0A3\r\n")) {
                    Context context5 = this.context;
                    if (!(context5 == null || (resources4 = context5.getResources()) == null)) {
                        str2 = resources4.getString(R.string.change_minor_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275382306:
                if (str.equals("$&0A4\r\n")) {
                    Context context6 = this.context;
                    if (!(context6 == null || (resources5 = context6.getResources()) == null)) {
                        str2 = resources5.getString(R.string.change_customer_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275380384:
                if (str.equals("$&0A6\r\n")) {
                    Context context7 = this.context;
                    if (!(context7 == null || (resources6 = context7.getResources()) == null)) {
                        str2 = resources6.getString(R.string.change_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275379423:
                if (str.equals("$&0A7\r\n")) {
                    Context context8 = this.context;
                    if (!(context8 == null || (resources7 = context8.getResources()) == null)) {
                        str2 = resources7.getString(R.string.change_txpower_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275324646:
                if (str.equals("$&0C2\r\n")) {
                    Context context9 = this.context;
                    if (!(context9 == null || (resources8 = context9.getResources()) == null)) {
                        str2 = resources8.getString(R.string.change_name_fail);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -1275323685:
                if (str.equals("$&0C3\r\n") && (iBeaconDetailView = this.beaconDetailView) != null) {
                    iBeaconDetailView.connectFail();
                    return;
                }
                return;
            case -531027263:
                if (str.equals("$@0A1\r\n")) {
                    Context context10 = this.context;
                    if (!(context10 == null || (resources9 = context10.getResources()) == null)) {
                        str2 = resources9.getString(R.string.change_uuid_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -531026302:
                if (str.equals("$@0A2\r\n")) {
                    Context context11 = this.context;
                    if (!(context11 == null || (resources10 = context11.getResources()) == null)) {
                        str2 = resources10.getString(R.string.change_major_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -531025341:
                if (str.equals("$@0A3\r\n")) {
                    Context context12 = this.context;
                    if (!(context12 == null || (resources11 = context12.getResources()) == null)) {
                        str2 = resources11.getString(R.string.change_minor_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -531024380:
                if (str.equals("$@0A4\r\n")) {
                    Context context13 = this.context;
                    if (!(context13 == null || (resources12 = context13.getResources()) == null)) {
                        str2 = resources12.getString(R.string.change_customer_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -531022458:
                if (str.equals("$@0A6\r\n")) {
                    Context context14 = this.context;
                    if (!(context14 == null || (resources13 = context14.getResources()) == null)) {
                        str2 = resources13.getString(R.string.change_adv);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -531021497:
                if (str.equals("$@0A7\r\n")) {
                    Context context15 = this.context;
                    if (!(context15 == null || (resources14 = context15.getResources()) == null)) {
                        str2 = resources14.getString(R.string.change_txpower_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -531020536:
                if (str.equals("$@0A8\r\n")) {
                    Context context16 = this.context;
                    if (!(context16 == null || (resources15 = context16.getResources()) == null)) {
                        str2 = resources15.getString(R.string.change_rssi);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -530966720:
                if (str.equals("$@0C2\r\n")) {
                    Context context17 = this.context;
                    if (!(context17 == null || (resources16 = context17.getResources()) == null)) {
                        str2 = resources16.getString(R.string.change_name_OK);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -530965759:
                if (str.equals("$@0C3\r\n")) {
                    Context context18 = this.context;
                    if (!(context18 == null || (resources17 = context18.getResources()) == null)) {
                        str2 = resources17.getString(R.string.connect_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    IBeaconDetailView iBeaconDetailView3 = this.beaconDetailView;
                    if (iBeaconDetailView3 != null) {
                        iBeaconDetailView3.connectOk();
                    }
                    ConnectModel connectModel2 = this.connectModel;
                    if (connectModel2 != null) {
                        connectModel2.read();
                        return;
                    }
                    return;
                }
                return;
            case -530963837:
                if (str.equals("$@0C5\r\n")) {
                    Context context19 = this.context;
                    if (!(context19 == null || (resources18 = context19.getResources()) == null)) {
                        str2 = resources18.getString(R.string.change_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            case -530962876:
                if (str.equals("$@0C6\r\n")) {
                    Context context20 = this.context;
                    if (!(context20 == null || (resources19 = context20.getResources()) == null)) {
                        str2 = resources19.getString(R.string.change_ok);
                    }
                    Toast.makeText(this.context, str2, Toast.LENGTH_SHORT).show();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onDataAcc(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "data");
        System.out.println("111真实数据=" + StringUtils.ByteArrToHex(bArr));
        IBeaconDetailView iBeaconDetailView = this.beaconDetailView;
        if (iBeaconDetailView != null) {
            iBeaconDetailView.setAcc(bArr);
        }
    }

    public void onDataTemp(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "data");
        System.out.println("222真实数据=" + StringUtils.ByteArrToHex(bArr));
        IBeaconDetailView iBeaconDetailView = this.beaconDetailView;
        if (iBeaconDetailView != null) {
            iBeaconDetailView.setTemp(bArr);
        }
    }

    public void onDataVersion(byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "data");
        System.out.println("版本号=" + StringUtils.ByteArrToHex(bArr));
        IBeaconDetailView iBeaconDetailView = this.beaconDetailView;
        if (iBeaconDetailView != null) {
            iBeaconDetailView.showVersion(new String(bArr, Charsets.UTF_8));
        }
    }

    public final void setMode() {
        sendValue(new byte[]{36, 49, 48}, Type.setMode_beacon);
    }

    public void onDisConnected(String str) {
        Intrinsics.checkNotNullParameter(str, "address");
        IBeaconDetailView iBeaconDetailView = this.beaconDetailView;
        if (iBeaconDetailView != null) {
            iBeaconDetailView.connectDis();
        }
    }

    public void onConnected(String str) {
        Intrinsics.checkNotNullParameter(str, "address");
        IBeaconDetailView iBeaconDetailView = this.beaconDetailView;
        if (iBeaconDetailView != null) {
            iBeaconDetailView.showPasswordDialog();
        }
    }
}
