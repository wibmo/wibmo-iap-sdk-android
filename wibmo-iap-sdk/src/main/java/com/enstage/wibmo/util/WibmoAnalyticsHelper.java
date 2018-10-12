package com.enstage.wibmo.util;

import com.enstage.wibmo.sdk.inapp.pojo.CustomerInfo;
import com.enstage.wibmo.sdk.inapp.pojo.MerchantInfo;
import com.wibmo.analytics.entiry.AnalyticsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nithya on 22/02/18.
 */

public class WibmoAnalyticsHelper {
    public static final String IAP_INIT = "IAP-Merchant-Init-[A]";
    public static final String IAP_WEB_CHECKOUT = "IAP-Web-Checkout";
    public static final String IAP_WEB_RESPONSE = "IAP-Web-Response";
    public static final String IAP_APP_RESPONSE = "IAP-SDK-Response-[Z]";

    public static final String IAP_FUNNEL_ID = "Wibmo-IAP-Android";

    public static final int IAP_INIT_EVENT = 5;
    public static final int IAP_WEB_CHECKOUT_EVENT = 10;
    public static final int IAP_WEB_RESPONSE_EVENT = 310;
    public static final int IAP_APP_RESPONSE_EVENT = 320;
    private static List<AnalyticsEvent> analyticsListData = new ArrayList<AnalyticsEvent>();

    public static com.wibmo.analytics.pojo.MerchantInfo makeMerchantInfo(MerchantInfo merchantInfo) {
        com.wibmo.analytics.pojo.MerchantInfo merchantInfoForAnalytics = new com.wibmo.analytics.pojo.MerchantInfo();
        if(merchantInfo != null) {
            merchantInfoForAnalytics.setMerchantId(merchantInfo.getMerId());
            merchantInfoForAnalytics.setMerchantName(merchantInfo.getMerName());
            merchantInfoForAnalytics.setMerchantCountry(merchantInfo.getMerCountryCode());
        }

        return merchantInfoForAnalytics;
    }

    public static com.wibmo.analytics.pojo.CustomerInfo makeCustomerInfo(CustomerInfo customerInfo) {
        com.wibmo.analytics.pojo.CustomerInfo customerInfoForAnalytics = new com.wibmo.analytics.pojo.CustomerInfo();
        if(customerInfo != null) {
            customerInfoForAnalytics.setCustomerEmail(customerInfo.getCustEmail());
            customerInfoForAnalytics.setCustomerMobile(customerInfo.getCustMobile());
            customerInfoForAnalytics.setCustomerName(customerInfo.getCustName());
        }

        return customerInfoForAnalytics;
    }

    public static void setAnalyticsData(AnalyticsEvent analyticsData) {
        analyticsListData.add(analyticsData);
    }

    public static List<AnalyticsEvent> getAnalyticsData() {
        return analyticsListData;
    }
}
