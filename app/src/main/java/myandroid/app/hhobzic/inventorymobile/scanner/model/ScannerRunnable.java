package myandroid.app.hhobzic.inventorymobile.scanner.model;


/* compiled from: Scanner.kt */
public final class ScannerRunnable implements Runnable {
    final  Scanner scanner;

    ScannerRunnable(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        this.scanner.stopScan();
    }
}
