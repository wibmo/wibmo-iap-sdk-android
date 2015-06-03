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
package com.enstage.wibmo.sdk.inapp;

import android.app.Activity;
import android.util.Log;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.DeviceInfo;
import com.enstage.wibmo.util.PhoneInfo;

/**
 * Created by akshath on 24/10/14.
 */
public class InAppUtil {
    private static final String TAG = "wibmo.sdk.InAppUtil";

    public static DeviceInfo makeDeviceInfo(Activity activity) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAppInstalled(isWibmoInstalled(activity));

        deviceInfo.setDeviceID(PhoneInfo.getInstance(activity).getDeviceID());
        deviceInfo.setDeviceIDType(3);//mobile
        deviceInfo.setDeviceType(3);//mobile

        deviceInfo.setDeviceMake(PhoneInfo.getInstance(activity).getPhoneMaker());
        deviceInfo.setDeviceModel(PhoneInfo.getInstance(activity).getPhoneModel());
        deviceInfo.setOsType("Android");
        deviceInfo.setOsVersion(PhoneInfo.getInstance(activity).getAndroidVersion());
        deviceInfo.setWibmoSdkVersion("??");//todo
        deviceInfo.setWibmoAppVersion("??");//todo

        return deviceInfo;
    }

    public static boolean isWibmoInstalled(Activity activity) {
        boolean flag = WibmoSDK.isWibmoIAPIntentAppAvailable(activity, WibmoSDK.getWibmoIntentActionPackage());
        if (flag==false) {
            Log.d(TAG, "Wibmo IAP supported app not installed!");
            //Exception e = new Exception("I am here");
            //Log.e(TAG, "I am here",e);
            return false;
        } else {
            Log.d(TAG, "Wibmo IAP App is installed!");
            return true;
        }
    }
}
