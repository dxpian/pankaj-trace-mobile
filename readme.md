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
  SHA1: C6:FA:xxx:
  SHA256: 21:C8:xxx`
- for releasing  
  - command format:   
  `keytool -list -v -keystore <path_to_your_release_keystore> -alias <your_alias_name> -storepass <your_store_password>`
  - in my case:  
  `keytool -list -v -keystore doc/honey.inventory_release_1.0.jks -alias ibtrace.inventory.release_1.0 -storepass `
  fingerprint result of release mode: 
  
### add SHA1 fingerprint with package name to google api key 

## Custom map style  
[download map style file](https://mapstyle.withgoogle.com/) 



