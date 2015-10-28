# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in G:\Program Files\AndroidStudio\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-optimizationpasses 5
-optimizations !code/simplification/arithmetic

#When not preverifing in a case-insensitive filing system, such as Windows. Because this tool unpacks your processed jars, you should then use:
-dontusemixedcaseclassnames

#Specifies not to ignore non-public library classes. As of version 4.5, this is the default setting
-dontskipnonpubliclibraryclasses

#Preverification is irrelevant for the dex compiler and the Dalvik VM, so we can switch it off with the -dontpreverify option.
-dontpreverify


#To keep parcelable classes (to serialize - deserialize objects to sent through Intents)
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#To remove debug logs:
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

#Uncomment if using Serializable
-keep class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#http://proguard.sourceforge.net/manual/examples.html#androidapplication
#Deprecated
-keepattributes Exceptions,InnerClasses,SourceFile,Signature,LineNumberTable,*Annotation*


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
-keep class com.fasterxml.jackson.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn okio.**
-dontwarn android.test.**
#------ WIBMO -----