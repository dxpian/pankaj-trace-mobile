package myandroid.app.hhobzic.inventorymobile.scanner.bean;


/* compiled from: Beacon.kt */
public final class Beacon {
    private int AccChangeAdv;
    private boolean IsConnect;
    private boolean OnlyBeacon;
    private boolean OpenTemp;
    private String address = "";
    private int bt = 1998;
    private String companyId;
    private String deviceName = "";
    private boolean flag;
    private String humidity = "N/A";
    private int interval;
    private int isMove = 5;
    private boolean isOpenAcc;
    private boolean isSupportAcc;
    private boolean isSupportTempShidu;
    private int major;
    private int minor;
    private String password = "";
    private String raw;
    private int rssi;
    private int rssiInOne;
    private String serviceData;
    private String temp = "N/A";
    private int temp_shidu_Time;
    private String txPower = "N/A";
    private String uuid;

    public final String getDeviceName() {
        return this.deviceName;
    }

    public final void setDeviceName(String str) {
        this.deviceName = str;
    }

    public final String getAddress() {
        return this.address;
    }

    public final void setAddress(String str) {
        this.address = str;
    }

    public final int getRssi() {
        return this.rssi;
    }

    public final void setRssi(int i) {
        this.rssi = i;
    }

    public final String getUuid() {
        return this.uuid;
    }

    public final void setUuid(String str) {
        this.uuid = str;
    }

    public final int getMajor() {
        return this.major;
    }

    public final void setMajor(int i) {
        this.major = i;
    }

    public final int getMinor() {
        return this.minor;
    }

    public final void setMinor(int i) {
        this.minor = i;
    }

    public final String getTxPower() {
        return this.txPower;
    }

    public final void setTxPower(String str) {
        this.txPower = str;
    }

    public final String getCompanyId() {
        return this.companyId;
    }

    public final void setCompanyId(String str) {
        this.companyId = str;
    }

    public final int getRssiInOne() {
        return this.rssiInOne;
    }

    public final void setRssiInOne(int i) {
        this.rssiInOne = i;
    }

    public final String getServiceData() {
        return this.serviceData;
    }

    public final void setServiceData(String str) {
        this.serviceData = str;
    }

    public final int getBt() {
        return this.bt;
    }

    public final void setBt(int i) {
        this.bt = i;
    }

    public final int getInterval() {
        return this.interval;
    }

    public final void setInterval(int i) {
        this.interval = i;
    }

    public final String getRaw() {
        return this.raw;
    }

    public final void setRaw(String str) {
        this.raw = str;
    }

    public final boolean getIsConnect() {
        return this.IsConnect;
    }

    public final void setIsConnect(boolean z) {
        this.IsConnect = z;
    }

    public final String getTemp() {
        return this.temp;
    }

    public final void setTemp(String str) {
        this.temp = str;
    }

    public final String getHumidity() {
        return this.humidity;
    }

    public final void setHumidity(String str) {
        this.humidity = str;
    }

    public final int isMove() {
        return this.isMove;
    }

    public final void setMove(int i) {
        this.isMove = i;
    }

    public final boolean isSupportTempShidu() {
        return this.isSupportTempShidu;
    }

    public final void setSupportTempShidu(boolean z) {
        this.isSupportTempShidu = z;
    }

    public final boolean getOpenTemp() {
        return this.OpenTemp;
    }

    public final void setOpenTemp(boolean z) {
        this.OpenTemp = z;
    }

    public final int getTemp_shidu_Time() {
        return this.temp_shidu_Time;
    }

    public final void setTemp_shidu_Time(int i) {
        this.temp_shidu_Time = i;
    }

    public final boolean isSupportAcc() {
        return this.isSupportAcc;
    }

    public final void setSupportAcc(boolean z) {
        this.isSupportAcc = z;
    }

    public final boolean isOpenAcc() {
        return this.isOpenAcc;
    }

    public final void setOpenAcc(boolean z) {
        this.isOpenAcc = z;
    }

    public final int getAccChangeAdv() {
        return this.AccChangeAdv;
    }

    public final void setAccChangeAdv(int i) {
        this.AccChangeAdv = i;
    }

    public final String getPassword() {
        return this.password;
    }

    public final void setPassword(String str) {
        this.password = str;
    }

    public final boolean getFlag() {
        return this.flag;
    }

    public final void setFlag(boolean z) {
        this.flag = z;
    }

    public final boolean getOnlyBeacon() {
        return this.OnlyBeacon;
    }

    public final void setOnlyBeacon(boolean z) {
        this.OnlyBeacon = z;
    }

    public final void setIsMove(int i) {
        this.isMove = i;
    }

    public final void isSupportTempShidu(boolean z) {
        this.isSupportTempShidu = z;
    }

    public final void isOpenAcc(boolean z) {
        this.isOpenAcc = z;
    }

    public final void setAccAdv(int i) {
        this.AccChangeAdv = i;
    }

    public final void isSupportAcc(boolean z) {
        this.isSupportAcc = z;
    }
}
