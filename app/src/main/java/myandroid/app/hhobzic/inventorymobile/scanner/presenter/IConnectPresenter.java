package myandroid.app.hhobzic.inventorymobile.scanner.presenter;

import kotlin.Metadata;

/* compiled from: IConnectPresenter.kt */
public interface IConnectPresenter {
    void close();

    void connect(String str);

    void disconnect();

    void sendChangePassword(String str);

    void sendPassword(String str);

    void setCompanyId(byte[] bArr);

    void setDeviceName(String str);

    void setInterval(int i);

    void setMajor(int i);

    void setMinor(int i);

    void setPassword(String str);

    void setRssiInOne(int i);

    void setServiceData(byte[] bArr);

    void setTxPower(byte b);

    void setUUID(String str);
}
