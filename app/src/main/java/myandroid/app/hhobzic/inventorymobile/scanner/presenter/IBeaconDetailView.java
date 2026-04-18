package myandroid.app.hhobzic.inventorymobile.scanner.presenter;

/* compiled from: IBeaconDetailView.kt */
public interface IBeaconDetailView {
    void connectDis();

    void connectFail();

    void connectOk();

    void setAcc(byte[] bArr);

    void setTemp(byte[] bArr);

    void show(String str);

    void showData(byte[] bArr);

    void showPasswordDialog();

    void showVersion(String str);
}