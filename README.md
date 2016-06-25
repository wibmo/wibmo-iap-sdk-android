# wibmo-iap-sdk-android
Wibmo IAP SDK for Android
=====

Wibmo IAP Android SDK is library to enable native apps do in-app payment with cards and wibmo wallets. This SDK will also support one step payments.

Download
--------
You can download a jar from GitHub's [releases page][1].

Or use Gradle:

```gradle
repositories {
  jcenter()
}

dependencies {
  compile 'com.wibmo.iap.sdk:wibmo-iap-sdk:1.4.+'
}
```

Or Maven:

```xml
<dependency>
  <groupId>com.wibmo.iap.sdk</groupId>
  <artifactId>wibmo-iap-sdk</artifactId>
  <version>1.4.2</version>
</dependency>
```

Proguard
--------
Depending on your proguard config and usage, you may need to include the following lines in your proguard.cfg:

```pro
#------ WIBMO -----
-keep class com.enstage.wibmo.sdk.** { *; }
-keepclassmembers class com.enstage.wibmo.sdk.inapp.pojo.** { *; }
-keep class com.enstage.wibmo.sdk.inapp.InAppBrowserActivity$* {
  *;
}
-keep class com.enstage.wibmo.sdk.inapp.InAppShellJavaScriptInterface {
  *;
}
-keep class com.enstage.wibmo.util.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn okio.**
#------ WIBMO -----
```
