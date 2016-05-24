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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.enstage.wibmo.sdk.R;
import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayResponse;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;

public class InAppShellHepler {
    private static final String TAG = "InAppShellHepler";

    protected static final String charSet = "utf-8";

    private WeakReference<Activity> activityWR;

    private int dialogStyle = -1;
    private int toastBackgroundColor = -1;

    private String responseUrl;
    private WeakReference<WebView> webViewWR;

    private InAppShellJavaScriptInterface jsInterface;

    public void injectIAP() {
        jsInterface = new InAppShellJavaScriptInterface(this);

        WebView webView = webViewWR.get();
        if(webView==null) {
            Log.e(TAG, "webView was null");
            return;
        }
        webView.addJavascriptInterface(jsInterface, "WibmoIAP");
    }

    public void initSDK() {
        initSDK(null, null, null);
    }

    public void initSDK(final String wibmoIntentActionPackage, final String wibmoDomain) {
        initSDK(wibmoIntentActionPackage, null, wibmoDomain);
    }

    public void initSDK(final String wibmoIntentActionPackage, final String wibmoAppPackage, final String wibmoDomain) {
        Activity activity = activityWR.get();
        if(activity==null) {
            Log.e(TAG, "activity was null");
            return;
        }

        final Context context = activity.getApplicationContext();
        Thread t = new Thread() {
            public void run() {
                Log.i(TAG, "initSDK in inappshell helper");
                if(wibmoIntentActionPackage!=null) {
                    WibmoSDK.setWibmoIntentActionPackage(wibmoIntentActionPackage);
                }
                if(wibmoAppPackage!=null) {
                    WibmoSDK.setWibmoAppPackage(wibmoAppPackage);
                }
                if(wibmoDomain!=null) {
                    WibmoSDKConfig.setWibmoDomain(wibmoDomain);
                }

                WibmoSDK.init(context);
                WibmoSDK.setInAppTxnIdCallback(null);//re-set
            }
        };
        t.start();
    }

    public static String getPostBodyForMerchant(Intent data) throws Exception {
        if (data == null) {
            return null;
        }

        String resCode = data.getStringExtra(InAppUtil.EXTRA_KEY_RES_CODE);
        String resDesc = data.getStringExtra(InAppUtil.EXTRA_KEY_RES_DESC);

        String wibmoTxnId = data.getStringExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID);
        String merTxnId = data.getStringExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID);
        String merAppData = data.getStringExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA);

        WPayResponse wPayResponse = WibmoSDK.processInAppResponseWPay(data);

        return getPostBodyForMerchant(resCode, resDesc, wibmoTxnId, merTxnId, merAppData, wPayResponse);
    }

    public static String getPostBodyForMerchant(String resCode, String resDesc,
            String wibmoTxnId, String merTxnId, String merAppData, WPayResponse wPayResponse) throws Exception {


        StringBuilder resPostData = new StringBuilder();
        resPostData.append("resCode=").append(URLEncoder.encode(resCode, charSet)).append('&');
        if(resDesc!=null) {
            resPostData.append("resDesc=").append(URLEncoder.encode(resDesc, charSet)).append('&');
        }

        if(merTxnId!=null) {
            resPostData.append("merTxnId=").append(URLEncoder.encode(merTxnId, charSet)).append('&');
        }

        if(merAppData!=null) {
            resPostData.append("merAppData=").append(URLEncoder.encode(merAppData, charSet)).append('&');
        }

        if (wPayResponse != null) {
            if(wPayResponse.getWibmoTxnId()!=null) {
                resPostData.append("wibmoTxnId=").append(URLEncoder.encode(wPayResponse.getWibmoTxnId(), charSet)).append('&');
            }
            if(wPayResponse.getMsgHash()!=null) {
                resPostData.append("msgHash=").append(URLEncoder.encode(wPayResponse.getMsgHash(), charSet)).append('&');
            }
            if(wPayResponse.getDataPickUpCode()!=null) {
                resPostData.append("dataPickUpCode=").append(URLEncoder.encode(wPayResponse.getDataPickUpCode(), charSet)).append('&');
            }
        } else {
            if(merTxnId!=null) {
                resPostData.append("wibmoTxnId=").append(URLEncoder.encode(wibmoTxnId, charSet)).append('&');
            }
        }

        return resPostData.toString();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String resCode = null;
        String resDesc = null;
        WPayResponse wPayResponse = null;

        WebView webView = webViewWR.get();
        if(webView==null) {
            Log.e(TAG, "webView was null");
            return;
        }

        if (requestCode == WibmoSDK.REQUEST_CODE_IAP_PAY) {
            if(data==null) {
                Log.e(TAG, "DATA was null!");

                resCode = "--";
                resDesc = "data was null";
            } else {
                resCode = data.getStringExtra("ResCode");
                resDesc = data.getStringExtra("ResDesc");
            }

            Log.i(TAG, "resCode: " + resCode+"; ResDesc: "+resDesc);

            try {
                if (resultCode == Activity.RESULT_OK) {
                    wPayResponse = WibmoSDK.processInAppResponseWPay(data);

                    //success;
                    String wPayTxnId = wPayResponse.getWibmoTxnId();
                    Log.i(TAG, "wPayTxnId: " + wPayTxnId);

                    String dataPickUpCode = wPayResponse.getDataPickUpCode();
                    Log.i(TAG, "dataPickUpCode: " + dataPickUpCode);
                } else {
                    Log.i(TAG, "requestCode: not ok");
                }//result not ok


                String resPostData = getPostBodyForMerchant(data);

                if(resPostData!=null) {
                    webView.postUrl(getResponseUrl(), resPostData.getBytes());
                    Log.i(TAG, "posting to responseUrl " + getResponseUrl());
                } else {
                    Log.i(TAG, "resPostData was null! will finish");

                    Activity activity = activityWR.get();
                    if(activity==null) {
                        Log.e(TAG, "activity was null");
                        return;
                    }
                    activity.finish();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in onActivityResult: "+e, e);
                showMsg("We had an error! " + e.getMessage());
            }
        }// requestCode
    }//onActivityResult


    protected void processIAP(String jsonWPayInitRequest) {
        Log.d(TAG, "processIAP: " + jsonWPayInitRequest);

        Activity activity = activityWR.get();
        if(activity==null) {
            Log.e(TAG, "activity was null");
            return;
        }

        try {
            WPayInitRequest wPayInitRequest = InAppUtil.makeGson().fromJson(jsonWPayInitRequest, WPayInitRequest.class);

            InAppShellTxnIdCallback inAppShellTxnIdCallback = new InAppShellTxnIdCallback(jsInterface);
            WibmoSDK.setInAppTxnIdCallback(inAppShellTxnIdCallback);

            WibmoSDK.startForInApp(activity, wPayInitRequest);
        } catch (Exception e) {
            Log.e(TAG, "Error: "+e,e);
            showMsg("We had an error in request!");
        }
    }

    protected void openUrl(String url) {
        Log.d(TAG, "url: "+url);

        Activity activity = activityWR.get();
        if(activity==null) {
            Log.e(TAG, "activity was null");
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error: "+e,e);
            showMsg("We had an error in request! "+e.getMessage());
        }
    }

    @SuppressLint("NewApi")
    public void showMsg(String msg) {
        Log.d(TAG, "showMsg: "+msg);

        Activity activity = activityWR.get();
        if(activity==null) {
            Log.e(TAG, "activity was null");
            return;
        }

        AlertDialog.Builder builder = null;

        if(getDialogStyle() !=-1) {
            try {
                builder = new AlertDialog.Builder(
                        new ContextThemeWrapper(activity, getDialogStyle()));
            } catch (Throwable e) {
                Log.e(TAG, "does not support theme " + e);
                builder = new AlertDialog.Builder(activity);
            }
        } else {
            builder = new AlertDialog.Builder(activity);
        }

        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setNegativeButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        AlertDialog alert = builder.create();
        alert.setIcon(android.R.drawable.ic_dialog_alert);

        try {
            alert.show();
        } catch(Throwable e) {
            Log.e(TAG, "error: " + e, e);
            showToast(msg);
        }
    }

    private Handler handler = new Handler();
    protected void showToast(final String msg) {
        Log.i(TAG, "Show Toast: " + msg);

        final Activity activity = activityWR.get();
        if(activity==null) {
            Log.e(TAG, "activity was null");
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
                View view = toast.getView();
                if(getToastBackgroundColor() !=-1) {
                    view.setBackgroundColor(getToastBackgroundColor());
                }
                try {
                    toast.show();
                } catch(Throwable e) {
                    Log.e(TAG, "error: " + e, e);
                }
            }
        });
    }

    public int getDialogStyle() {
        return dialogStyle;
    }

    public void setDialogStyle(int dialogStyle) {
        this.dialogStyle = dialogStyle;
    }

    public int getToastBackgroundColor() {
        return toastBackgroundColor;
    }

    public void setToastBackgroundColor(int toastBackgroundColor) {
        this.toastBackgroundColor = toastBackgroundColor;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }

    public WebView getWebView() {
        WebView webView = webViewWR.get();
        if(webView==null) {
            Log.e(TAG, "webView was null");
        }
        return webView;
    }

    public void setWebView(WebView webView) {
        webViewWR = new WeakReference<WebView>(webView);
    }

    public Activity getActivity() {
        return activityWR.get();
    }

    public void setActivity(Activity activity) {
        activityWR = new WeakReference<Activity>(activity);
    }

}
