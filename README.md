Wibmo IAP SDK for Android
=====

Wibmo IAP Android SDK is library to enable native apps do in-app payment with cards and wibmo wallets. This SDK will also support one step payments.

Download
--------
You can use Gradle:

```gradle
repositories {
  jcenter()
}

dependencies {
  compile 'com.wibmo.iap.sdk:wibmo-iap-sdk:+'
}
```

Or Maven:

```xml
<dependency>
  <groupId>com.wibmo.iap.sdk</groupId>
  <artifactId>wibmo-iap-sdk</artifactId>
  <version>2.0.5</version>
</dependency>
```

Proguard
--------
Depending on your proguard config and usage, you may need to include the following lines in your proguard.cfg:

```pro
#------ WIBMO -----
-keep class com.enstage.wibmo.sdk.inapp.WibmoSDK { *; }
-keep class com.enstage.wibmo.sdk.inapp.pojo.** { *; }
-keepclassmembers class com.enstage.wibmo.sdk.inapp.pojo.** { *; }
-keep class com.enstage.wibmo.sdk.inapp.InAppBrowserActivity$* { *; }
-keep class com.enstage.wibmo.sdk.inapp.InAppShellJavaScriptInterface { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
#------ WIBMO -----
```

Support
-------
Wibmo is a tech company. All our engineers handle support too. So can drop us an email on merchant@wibmo.com and expect a response from the dev responsible for the android SDK.

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
