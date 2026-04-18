package myandroid.app.hhobzic.inventorymobile.scanner.model;

public interface ConnectDataCallBack {
    void onConnected(String str);

    void onData(String str);

    void onData1(byte[] bArr);

    void onDataAcc(byte[] bArr);

    void onDataTemp(byte[] bArr);

    void onDataVersion(byte[] bArr);

    void onDisConnected(String str);
}
