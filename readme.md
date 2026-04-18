## android tutorial

[see document](https://developer.android.com/topic/architecture/ui-layer)

## How to create google api key
### Create Map ID  
- go to `google cloud/map management` and create `Map ID`.   
```shell
# Map name: 
Map.IceBattery.Inventory.Map.release_1.0
# Map type
Android
```
- Copy map ID after save the above result:  
Generated Map ID: `fcf8e3c4f0d36ca5`

### Create key store credentials:
```shell
# alias name
ibtrace.inventory.release_1.0

#password:
RFV104F1D10fKTN

# certificate first and last name: 
inventory
```
### check the JVM version firmst  
open terminal in android studio and run   
`./gradlew -version`
this command will show the jvm version which android studio is using now 

### find JVM home and add to system path  
`C:\Program Files\Java\jdk-[JVM_version]\bin`

### create fingerpint
- for debugging
`keytool -list -v -keystore C:\Users\Milind\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android`
- Fingerprint result: 
`Certificate fingerprints:
  SHA1: C6:FA:56:F8:05:F3:3E:75:93:E2:9A:CE:D2:CD:16:16:23:59:38:BF
  SHA256: 21:C8:85:2C:47:E6:22:5B:35:51:39:60:FE:2F:67:C7:F5:12:75:A6:CB:2B:02:19:A8:47:FF:7A:E8:31:9B:6A`
- for releasing  
  - command format:   
  `keytool -list -v -keystore <path_to_your_release_keystore> -alias <your_alias_name> -storepass <your_store_password>`
  - in my case:  
  `keytool -list -v -keystore doc/honey.inventory_release_1.0.jks -alias ibtrace.inventory.release_1.0 -storepass RFV104F1D10fKTN`
  fingerprint result of release mode: 
  ```
  SHA1: 89:AD:98:71:AD:B3:88:ED:BF:0E:5A:2D:52:CB:7D:26:9D:18:CB:E7
  SHA256: 04:EC:6E:2D:8A:EB:A9:E9:FB:5D:94:5F:D2:D5:B9:4C:12:77:65:E8:9D:6D:03:E4:60:80:5F:49:12:61:26:B5
  ```
### add SHA1 fingerprint with package name to google api key 

## Custom map style  
[download map style file](https://mapstyle.withgoogle.com/) 



C:\Users\Milind\AppData\Local\Android\Sdk\build-tools\34.0.0\apksigner sign --ks doc/honey.inventory.jks --ks-key-alias key0 app/release/app-release.apk

## How to log from mobile phone

###  Enable USB Debugging on Your Phone  
https://www.youtube.com/watch?v=Ucs34BkfPB0
- Go to Settings → About Phone.
- Tap Build Number 7 times to enable Developer Options.
- Go to Developer Options and enable USB Debugging.
- Connect your phone to your PC via USB.

### check if device is connected
- download adb sdk for windows from [here](https://developer.android.com/studio/releases/platform-tools): 

- open terminal at a root directory of  `adb.exe` file, run   
  `adb devices`
  output shoud be if device connected successfully
```shell
platform-tools-latest-windows\platform-tools>adb devices
    List of devices attached
    LUGUT19B12009182        device
```
### get log when app crahsed
- connect phone and run the following command in terminal
- get all log
`adb logcat | findstr myandroid.app.hhobzic.inventorymobile > log.txt`
- get log only when crashed
`adb logcat -b crash > log_crash.txt`



## Goolge Play Stroe Account  
```shell
#site url 
https://play.google.com/console/signup

# account
dxicebattery@gmail.com
IcebatteryZNS1!

https://copperdigital.com/blog/a-step-by-step-guide-to-upload-app-to-google-play-store/

```


## Sentry IO account
```shell
$downloadUrl = "https://github.com/getsentry/sentry-wizard/releases/download/v4.0.1/sentry-wizard-win-x64.exe"
Invoke-WebRequest $downloadUrl -OutFile sentry-wizard.exe
./sentry-wizard.exe -i android --saas --org sam-lt --project android
```