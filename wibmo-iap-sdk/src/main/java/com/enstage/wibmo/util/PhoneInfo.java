/*
 * Copyright (C) 2014 enStage Inc. Cupertino, California USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enstage.wibmo.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by akshath on 21/10/14.
 */
public class PhoneInfo extends PhoneInfoBase {
    private static final String TAG = "wibmo.PhoneInfo";

    private static PhoneInfo instance = null;

    public static PhoneInfo updateLocation(Context context, Location location) {
        if (location == null) return null;
        getInstance(context);

        instance.setGpsAccuracy(location.getAccuracy());
        instance.setGpsLatitude(location.getLatitude());
        instance.setGpsLongitude(location.getLongitude());

        instance.setGpsTime(location.getTime());

        return instance;
    }

    @SuppressLint("NewApi")
    public static PhoneInfo getInstance(Context context) {
        if (instance != null) return instance;

        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);

        instance = new PhoneInfo();

        //mask device id
        String deviceID = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            instance.setDeviceID("anid:" + mask(android_id));
            deviceID = android_id;
            Log.d(TAG, "h/w anid: " + android_id);
        } else if (telephonyManager.getDeviceId() != null && (telephonyManager.getDeviceId().isEmpty() == false) &&
                !telephonyManager.getDeviceId().equalsIgnoreCase("unknown")) {
            instance.setDeviceID("tdid:" + mask(telephonyManager.getDeviceId()));
            deviceID = telephonyManager.getDeviceId();
        } else {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)) {
                Log.d(TAG, "h/w serial: " + android.os.Build.getSerial());
                instance.setDeviceID("srnm:" + mask(android.os.Build.getSerial()));
                deviceID = android.os.Build.getSerial();
            } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)) {
                Log.d(TAG, "h/w serial P>: " + android.os.Build.SERIAL);
                instance.setDeviceID("srnm:" + mask(android.os.Build.SERIAL));
                deviceID = android.os.Build.SERIAL;
            } else {
                instance.setDeviceID("anid:" + mask(Settings.Secure.ANDROID_ID));
                deviceID = Settings.Secure.ANDROID_ID;
            }
        }

        if(deviceID == null) {
            instance.setDeviceID("anid:" + mask(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)));
        }

        instance.setNetCountryIsoCode(telephonyManager.getNetworkCountryIso());

        instance.setNetOperator(telephonyManager.getNetworkOperator()+
                "/"+telephonyManager.getNetworkOperatorName());

        instance.setNetRoaming(telephonyManager.isNetworkRoaming());

        instance.setAndroidVersion(android.os.Build.VERSION.RELEASE);

        instance.setPhoneModel(android.os.Build.MODEL);

        instance.setPhoneMaker(android.os.Build.MANUFACTURER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            instance.setNetSubId(instance.getDeviceID());
            Log.d(TAG, "SimSerialNumber: "+instance.getDeviceID());
            instance.setSimSrNum(mask(instance.getDeviceID()));
        } else {
            String simSerial = null;
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)) {
                Log.d(TAG, "h/w P serial: " + android.os.Build.getSerial());
                simSerial = android.os.Build.getSerial();
            } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)) {
                simSerial = android.os.Build.SERIAL;
                Log.d(TAG, "h/w P> serial: " + android.os.Build.SERIAL);
            }

            if (simSerial != null && !simSerial.isEmpty() && simSerial.equalsIgnoreCase("unknown")) {
                instance.setSimSrNum("srnm:" + mask(simSerial));
            }else{
                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                instance.setSimSrNum("anid:" + mask(android_id));
            }
            instance.setNetSubId(telephonyManager.getSubscriberId());
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            instance.setNetSubId(instance.getDeviceID());
//            instance.setSimSrNum(instance.getDeviceID());
//        }else{
//            if (telephonyManager.getSimSerialNumber() != null) {
//                instance.setSimSrNum(mask(telephonyManager.getSimSerialNumber()));
//            }
//            else if (android.os.Build.getSerial()!=null&&!android.os.Build.getSerial().equals("")&&
//                    !android.os.Build.getSerial().equalsIgnoreCase("unknown")){
//                instance.setSimSrNum("anid:" + mask(android.os.Build.getSerial()));
//            }
//            else {
//                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//                instance.setSimSrNum("anid:" + mask(android_id));
//            }
//            instance.setNetSubId(telephonyManager.getSubscriberId());
//            // instance.setSimSrNum(telephonyManager.getSimSerialNumber());
//        }


        return instance;
    }

}
