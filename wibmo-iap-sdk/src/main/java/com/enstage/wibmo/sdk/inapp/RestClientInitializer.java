package com.enstage.wibmo.sdk.inapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.wibmo.analytics.pojo.AppInfo;
import com.wibmo.analytics.pojo.CustomerInfo;
import com.wibmo.analytics.pojo.MerchantInfo;
import com.wibmo.analytics.test.RestClientHelper;

import java.util.HashMap;

/**
 * Created by nithyak on 31/01/18.
 */

public class RestClientInitializer {

    private static RestClientInitializer restClientInitialiser;

    public static RestClientInitializer initializeWibmoAnalytics() {
        return initializeWibmoAnalytics(false);
    }

    public static RestClientInitializer initializeWibmoAnalytics(boolean forceReload) {
        if (forceReload) {
            restClientInitialiser = new RestClientInitializer();
            RestClientHelper.setServerDomain(InAppUtil.getPostURLForWA());
            RestClientHelper.setProductName(InAppUtil.getProdNameForWA());
            RestClientHelper.setApiKey(InAppUtil.getApiKeyForWA());
            RestClientHelper.setApiUser(InAppUtil.getApiUserForWA());
        } else {
            if (restClientInitialiser == null) {
                restClientInitialiser = new RestClientInitializer();
            }
        }
        return restClientInitialiser;
    }

    public RestClientInitializer() {
        RestClientHelper.setEnabled(true);
        RestClientHelper.setDebug(false);

        try {
            RestClientHelper.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static CustomerInfo makeCustInfo(W2faInitRequest initRequest) throws Exception{
        CustomerInfo custInfo = new CustomerInfo();
        custInfo.setCustomerName(initRequest.getCustomerInfo().getCustName());
        custInfo.setCustomerMobile(initRequest.getCustomerInfo().getCustMobile());
        custInfo.setCustomerEmail(initRequest.getCustomerInfo().getCustEmail());
        return custInfo;
    }

    public static MerchantInfo makeMerInfo(W2faInitRequest initReq) throws Exception{
        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMcc(initReq.getMerchantInfo().getMerCountryCode());
        merchantInfo.setMerchantName(initReq.getMerchantInfo().getMerName());
        merchantInfo.setMerchantId(initReq.getMerchantInfo().getMerId());
        merchantInfo.setMerchantCountry(initReq.getMerchantInfo().getMerCountryCode());
        merchantInfo.setMerchantBankId(initReq.getMerchantInfo().getMerProgramId());
        return merchantInfo;
    }

    public static AppInfo makeAppInfo(W2faInitRequest initReq) throws Exception{
        AppInfo appInfo = new AppInfo();
        if(WibmoSDK.getWibmoIntentActionPackage() != null) {
            appInfo.setAppId(WibmoSDK.getWibmoIntentActionPackage());
        }
        appInfo.setAppOS(initReq.getDeviceInfo().getOsType());
        appInfo.setDeviceId(initReq.getDeviceInfo().getDeviceID());
        appInfo.setAppVersion(initReq.getDeviceInfo().getWibmoAppVersion());
        appInfo.setAppOSVersion(initReq.getDeviceInfo().getOsVersion());

        return appInfo;
    }

    public static HashMap<String,Object> getNetworkInfo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        HashMap<String, Object> nwData = new HashMap<>(3);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            if(activeNetwork.getSubtypeName() != null && activeNetwork.getSubtypeName().isEmpty() == false) {
                nwData.put("networkType", activeNetwork.getSubtypeName());
            } else {
                nwData.put("networkType", activeNetwork.getTypeName());
            }
        }
        nwData.put("operatorId", telephonyManager.getNetworkOperator());
        nwData.put("operatorName", telephonyManager.getNetworkOperatorName());
        return nwData;
    }
}
