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

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by akshath on 21/10/14.
 */
public class PhoneInfo extends PhoneInfoBase {
    private static final String TAG = "wibmo.PhoneInfo";

    private static PhoneInfo instance = null;

    public static PhoneInfo updateLocation(Context context, Location location) {
        if(location==null) return null;
        getInstance(context);

        instance.setGpsAccuracy(location.getAccuracy());
        instance.setGpsLatitude(location.getLatitude());
        instance.setGpsLongitude(location.getLongitude());

        instance.setGpsTime(location.getTime());

        return instance;
    }

    @SuppressLint("NewApi") public static PhoneInfo getInstance(Context context) {
        if(instance!=null) return instance;

        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);

        instance = new PhoneInfo();

        //mask device id
        if(telephonyManager.getDeviceId()!=null) {
            instance.setDeviceID("tdid:"+mask(telephonyManager.getDeviceId()));
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                //Log.d(TAG, "h/w serial: "+android.os.Build.SERIAL);
                instance.setDeviceID("srnm:"+mask(android.os.Build.SERIAL));
            }
        }

        instance.setNetCountryIsoCode(telephonyManager.getNetworkCountryIso());

        instance.setNetOperator(telephonyManager.getNetworkOperator()+
                "/"+telephonyManager.getNetworkOperatorName());

        instance.setNetSubId(telephonyManager.getSubscriberId());

        instance.setNetRoaming(telephonyManager.isNetworkRoaming());

        instance.setSimSrNum(telephonyManager.getSimSerialNumber());

        instance.setAndroidVersion(android.os.Build.VERSION.RELEASE);

        instance.setPhoneModel(android.os.Build.MODEL);

        instance.setPhoneMaker(android.os.Build.MANUFACTURER);


        return instance;
    }

}
