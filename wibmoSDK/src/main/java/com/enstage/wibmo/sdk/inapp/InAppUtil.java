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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.inapp.pojo.DeviceInfo;
import com.enstage.wibmo.util.PhoneInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by akshath on 24/10/14.
 */
public class InAppUtil {
    private static final String TAG = "wibmo.sdk.InAppUtil";

    public static final String EXTRA_KEY_RES_CODE = "ResCode";
    public static final String EXTRA_KEY_RES_DESC = "ResDesc";
    public static final String EXTRA_KEY_WIBMO_TXN_ID = "WibmoTxnId";
    public static final String EXTRA_KEY_DATA_PICKUP_CODE = "DataPickUpCode";//no more used in v2
    public static final String EXTRA_KEY_MSG_HASH = "MsgHash";
    public static final String EXTRA_KEY_MER_TXN_ID = "MerTxnId";
    public static final String EXTRA_KEY_MER_APP_DATA = "MerAppData";

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();//new Gson();

    public static Gson makeGson() {
        return gson;
    }

    public static DeviceInfo makeDeviceInfo(Activity activity, String sdkVersion) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAppInstalled(isWibmoInstalled(activity));

        if (WibmoSDKPermissionUtil.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            deviceInfo.setDeviceID(PhoneInfo.getInstance(activity).getDeviceID());

            deviceInfo.setDeviceMake(PhoneInfo.getInstance(activity).getPhoneMaker());
            deviceInfo.setDeviceModel(PhoneInfo.getInstance(activity).getPhoneModel());
            deviceInfo.setOsVersion(PhoneInfo.getInstance(activity).getAndroidVersion());
        }
        deviceInfo.setDeviceIDType(3);//mobile
        deviceInfo.setDeviceType(3);//mobile

        deviceInfo.setOsType("Android");
        deviceInfo.setWibmoSdkVersion(sdkVersion);
        deviceInfo.setWibmoAppVersion("??");

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

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        NetworkInfo activeNetworkInfo = getActiveNetworkInfo(context);
        return activeNetworkInfo != null;
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if(activeNetworkInfo!=null) {
            Log.i(TAG, "N/W Type: " + activeNetworkInfo.getTypeName());
        }
        return activeNetworkInfo;
    }

    public static void manageWebViewOnError(final Activity activity, final WebView webView,
                                            final int errorCode, final String description, final String failingUrl, final Runnable abortAction) {
        if(description!=null) {
            Toast.makeText(activity, "Oh no! " + description,
                    Toast.LENGTH_SHORT).show();
        }

        if(isNetworkAvailable(activity)==false) {
            String msg = activity.getString(com.enstage.wibmo.sdk.R.string.msg_internet_issue);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(
                            activity.getString(com.enstage.wibmo.sdk.R.string.label_try_again),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    manageWebViewOnError(activity, webView,
                                            errorCode, null, failingUrl, abortAction);
                                }
                            })
                    .setNegativeButton(
                            activity.getString(com.enstage.wibmo.sdk.R.string.label_cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (dialog != null) {
                                        try {
                                            dialog.dismiss();
                                        } catch (IllegalArgumentException e) {
                                            Log.e(TAG, "Error: " + e);
                                        }
                                    }
                                    if (abortAction != null) {
                                        abortAction.run();
                                    }
                                }
                            });

            Dialog dialog = builder.create();
            try {
                dialog.show();
            } catch (Throwable e) {
                Log.e(TAG, "Error: " + e, e);
            }

            return;
        }

        String msg = activity.getString(com.enstage.wibmo.sdk.R.string.msg_servers_issue);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(
                        activity.getString(com.enstage.wibmo.sdk.R.string.label_try_again),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                manageReAttempt(activity, webView,
                                        errorCode, description, failingUrl, abortAction);
                            }
                        })
                .setNegativeButton(
                        activity.getString(com.enstage.wibmo.sdk.R.string.label_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (dialog != null) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "Error: " + e);
                                    }
                                }
                                if (abortAction != null) {
                                    abortAction.run();
                                }
                            }
                        });

        Dialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e, e);
        }
    }

    private static void manageReAttempt(Activity activity, WebView webView,
                                        int errorCode, String description, String failingUrl, final Runnable abortAction) {
        if(canRetryWebView(activity, errorCode, description, failingUrl)) {
            Toast.makeText(activity, activity.getString(com.enstage.wibmo.sdk.R.string.label_pl_wait_reloading),
                    Toast.LENGTH_SHORT).show();

            /*if(webView.canGoBack()) {
                Toast.makeText(activity, activity.getString(com.enstage.wibmo.sdk.R.string.label_pl_wait_goingback),
                        Toast.LENGTH_SHORT).show();

                webView.goBack();
            } else*/ {
                Toast.makeText(activity, activity.getString(com.enstage.wibmo.sdk.R.string.label_pl_wait_reloading),
                        Toast.LENGTH_SHORT).show();

                webView.reload();
            }
        } else {
            if (abortAction != null) {
                abortAction.run();
            }
        }
    }

    private static boolean canRetryWebView(Context context,
                                           int errorCode, String description, String failingUrl) {
        if(errorCode == WebViewClient.ERROR_CONNECT || errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE
                || errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_TIMEOUT) {
            return true;
        } else {
            return false;
        }
    }
}
