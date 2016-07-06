package com.enstage.wibmo.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.InAppUtil;
import com.enstage.wibmo.sdk.inapp.WibmoSDKPermissionUtil;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Akshathkumar Shetty on 30/05/16.
 */
public class AnalyticalUtil {
    private static final String TAG = "wibmo.sdk.AUtil";

    private static final boolean debug = false;
    private static String mToken = "";

    public static void logTxn(final Context context,
                              final Map<String, Object> extraData,
                              final W2faInitRequest w2faInitRequest, final W2faInitResponse w2faInitResponse,
                              final int requestCode, final int resultCode, final Intent resultData) {
        if(debug) Log.v(TAG, "logTxn: " + requestCode + "; " + resultCode);

        if(mToken==null || mToken.isEmpty()) {
            if(debug) Log.v(TAG, "mToken is not set.. will abort");
            return;
        }

        if(debug) Log.v(TAG, "extraData: " + extraData);

        Thread t = new Thread() {
            public void run() {

                try {
                    if (debug) Log.v(TAG, "thread start");
                    AnalyticalUtil.logTxnInternal(context,
                            extraData,
                            w2faInitRequest, w2faInitResponse,
                            requestCode, resultCode, resultData);
                    if (debug) Log.v(TAG, "thread end");
                } catch (Exception e) {
                    if(debug) Log.e(TAG, "Error: "+e,e);
                }
            }
        };
        t.start();
    }


    public static void logTxnInternal(Context context,
                                      Map<String, Object> extraData,
                                      W2faInitRequest w2faInitRequest, W2faInitResponse w2faInitResponse,
                                      int requestCode, int resultCode, Intent resultData) {
        String eventName = "IAP-Pay";
        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_2FA) {
            eventName = "IAP-W2fa";
        }

        eventName = eventName + " SDK";

        Map<String, Object> data = new HashMap<>(20);
        data.put("WibmoSdkVersion", WibmoSDK.VERSION);
        if(extraData!=null) {
            data.putAll(extraData);
        }

        data.put("Event_Time", (long) (System.currentTimeMillis() / 1000));

        if (resultCode == Activity.RESULT_OK) {
            data.put("Status", "true");
        } else {
            data.put("Status", "false");
        }

        WPayResponse wPayResponse = WibmoSDK.processInAppResponseWPay(resultData);
        if(wPayResponse!=null) {
            data.put("ResCode", wPayResponse.getResCode());
            data.put("ResDesc", wPayResponse.getResDesc());

            String wibmoTxnId = wPayResponse.getWibmoTxnId();
            data.put("wibmoTxnId", wibmoTxnId);

            String merAppData = wPayResponse.getMerAppData();
            data.put("merAppData", merAppData);

            String merTxnId = wPayResponse.getMerTxnId();
            data.put("merTxnId", merTxnId);
        } else {
            if (data != null && resultData!=null) {
                String resCode = resultData.getStringExtra("ResCode");
                String resDesc = resultData.getStringExtra("ResDesc");

                data.put("ResCode", resCode);
                data.put("ResDesc", resDesc);
            }
        }

        if (WibmoSDKPermissionUtil.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            PhoneInfo phoneInfo = PhoneInfo.getInstance(context);

            data.put("$os", "Android");
            data.put("$os_version", phoneInfo.getAndroidVersion());
            data.put("$brand", phoneInfo.getPhoneMaker());
            data.put("$model", phoneInfo.getPhoneModel());

            data.put("$carrier", phoneInfo.getNetOperatorName());
        }

        if(w2faInitRequest!=null) {
            if(w2faInitRequest.getCustomerInfo()!=null) {
                data.put("CustMobile", w2faInitRequest.getCustomerInfo().getCustMobile());
                data.put("CustEmail", w2faInitRequest.getCustomerInfo().getCustEmail());
                data.put("CustName", w2faInitRequest.getCustomerInfo().getCustName());
            }

            if(w2faInitRequest.getTransactionInfo()!=null) {
                data.put("AmountImpl", w2faInitRequest.getTransactionInfo().getTxnAmount());
                data.put("Currency", w2faInitRequest.getTransactionInfo().getTxnCurrency());
            }
        }

        if(w2faInitResponse!=null) {
            if(w2faInitResponse.getTransactionInfo()!=null) {
                data.put("Amount", w2faInitResponse.getTransactionInfo().getTxnFormattedAmount());
                data.put("TxnAmtKnown", w2faInitResponse.getTransactionInfo().isTxnAmtKnown());
                data.put("ChargeLater", w2faInitResponse.getTransactionInfo().isChargeLater());
            }

            if(w2faInitResponse.getMerchantInfo()!=null) {
                data.put("MerName", w2faInitResponse.getMerchantInfo().getMerName());
                data.put("MerProgramId", w2faInitResponse.getMerchantInfo().getMerProgramId());
            }
        }

        try {
            String dataToPost = buildEventToPostFor(context, getmToken(), eventName, data);

            boolean flag = postToServer(context, dataToPost);
            if(debug) Log.i(TAG, "postToServer: "+flag);
        } catch (Exception e) {
            if(debug) Log.e(TAG, "Error: "+e, e);
        }
    }

    private static String buildEventToPostFor(Context context, String mToken,
                                              String eventName, Map<String, Object> data) throws JSONException {
        JsonObject dataObj = new JsonObject();
        dataObj.addProperty("event", eventName);

        long time = System.currentTimeMillis() / 1000;

        JsonObject propertiesObj =  new JsonObject();
        propertiesObj.addProperty("token", mToken);
        propertiesObj.addProperty("time", time);
        propertiesObj.addProperty("mp_lib", "AndroidSDK");

        Iterator<String> iterator = data.keySet().iterator();
        String key = null;
        Object value = null;
        while(iterator.hasNext()) {
            key = iterator.next();
            value = data.get(key);
            if(value instanceof Boolean) {
                propertiesObj.addProperty(key, (Boolean) value);
            } else if(value instanceof Integer) {
                propertiesObj.addProperty(key, (Integer) value);
            } else if(value instanceof Long) {
                propertiesObj.addProperty(key, (Long) value);
            } else {
                propertiesObj.addProperty(key, (String) value);
            }
        }

        dataObj.add("properties", propertiesObj);

        String payload = InAppUtil.makeGson().toJson(dataObj);
        if(debug) Log.v(TAG, "payload: "+payload);

        return payload;
    }

    public static boolean postToServer(Context context, String payload) {
        String endPoint = "https://api.mixpanel.com/track/";

        try {
            StringBuilder sb = new StringBuilder(100);
            sb.append("data=");
            sb.append(new String(Base64.encode(payload.getBytes(WibmoSDKConfig.CHARTSET))));
            sb.append("&verbose=0");

            if(debug) Log.v(TAG, "rawreq: "+sb.toString());

            String rawres = HttpUtil.postData(endPoint,
                    sb.toString().getBytes(WibmoSDKConfig.CHARTSET),
                    false, HttpUtil.WWW_FORM);

            if(debug) Log.v(TAG, "rawres: "+rawres);

            if("1".equals(rawres)) {
                return true;
            } else  {
                return false;
            }
        } catch (IOException e) {
            //TODO re-try option, save to disk and intent service
            if(debug) Log.e(TAG, "Error : "+e,e);
            return false;
        } catch (Exception e) {
            if(debug) Log.e(TAG, "Error : "+e,e);
            return false;
        }
    }

    public static String getmToken() {
        return mToken;
    }

    public static void setmToken(String mToken) {
        AnalyticalUtil.mToken = mToken;
    }
}