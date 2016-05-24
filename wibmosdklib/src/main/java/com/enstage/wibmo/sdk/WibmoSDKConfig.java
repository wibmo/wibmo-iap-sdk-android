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
package com.enstage.wibmo.sdk;

/**
 * Created by akshath on 21/10/14.
 */
public class WibmoSDKConfig {
    public static final String CHARTSET = "utf-8";

    //will trust dummy certificate
    private static boolean testMode = false;

    private static String wibmoDomain = "https://www.wibmo.com";

    private static String init2FAPostUrl = wibmoDomain + "/v1/w2fa/init";
    private static String initPayPostUrl = wibmoDomain + "/v1/wPay/init";

    private static String statusIAPPostUrl = wibmoDomain + "/v1/wPay/status/sdk/";


    public static void setWibmoDomain(String wibmoDomain) {
        WibmoSDKConfig.wibmoDomain = wibmoDomain;
        if(wibmoDomain==null) {
            throw new IllegalArgumentException("Domain can not be null!");
        }
        if(wibmoDomain.endsWith("/")) {
            throw new IllegalArgumentException("Domain can not end with /");
        }

        if(wibmoDomain.equals("https://www.wibmo.com")==false && wibmoDomain.equals("https://beta.wibmo.com")==false) {
            testMode = true;
        } else {
            testMode = false;
        }

        init2FAPostUrl = wibmoDomain + "/v1/w2fa/init";
        initPayPostUrl = wibmoDomain + "/v1/wPay/init";
        setStatusIAPPostUrl(wibmoDomain + "/v1/wPay/status/sdk/");
    }

    public static boolean isTestMode() {
        return testMode;
    }

    public static void setTestMode(boolean flag) {
        testMode = flag;
    }

    public static String getInit2FAPostUrl() {
        return init2FAPostUrl;
    }

    public static void setInit2FAPostUrl(String init2FAPostUrl) {
        WibmoSDKConfig.init2FAPostUrl = init2FAPostUrl;
    }

    public static String getInitPayPostUrl() {
        return initPayPostUrl;
    }

    public static void setInitPayPostUrl(String initPayPostUrl) {
        WibmoSDKConfig.initPayPostUrl = initPayPostUrl;
    }

    public static String getWibmoDomain() {
        return wibmoDomain;
    }

    public static String getWibmoNwDomainOnly() {
        int i = wibmoDomain.indexOf(".");
        return wibmoDomain.substring(i+1);//wibmo.com
    }

    public static String getStatusIAPPostUrl() {
        return statusIAPPostUrl;
    }

    public static void setStatusIAPPostUrl(String statusIAPPostUrl) {
        WibmoSDKConfig.statusIAPPostUrl = statusIAPPostUrl;
    }
}
