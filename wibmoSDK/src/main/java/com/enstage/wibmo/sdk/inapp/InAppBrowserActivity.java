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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.enstage.wibmo.sdk.WibmoSDK;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitResponse;
import com.enstage.wibmo.sdk.R;

/**
 * Created by akshath on 20/10/14.
 */
public class InAppBrowserActivity extends Activity {
    private static final String TAG = "wibmo.sdk.InAppBrowser";

    public static final int REQUEST_CODE = 0x0000c0c0; // Only use bottom 16 bits

    private View view = null;

    private Context context = null;

    private String qrMsg;

    private FrameLayout mainView;
    private WebView webView;

    private W2faInitRequest w2faInitRequest;
    private W2faInitResponse w2faInitResponse;

    private WPayInitRequest wPayInitRequest;
    private WPayInitResponse wPayInitResponse;

    private boolean resultSet;
    private boolean isViewSmall = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = (Context) this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            w2faInitRequest = (W2faInitRequest) extras
                    .getSerializable("W2faInitRequest");
            w2faInitResponse = (W2faInitResponse) extras
                    .getSerializable("W2faInitResponse");

            wPayInitRequest = (WPayInitRequest) extras
                    .getSerializable("WPayInitRequest");
            wPayInitResponse = (WPayInitResponse) extras
                    .getSerializable("WPayInitResponse");

            if (w2faInitRequest != null && w2faInitResponse!=null) {
                qrMsg = "Wibmo InApp payment";
            } else if (wPayInitRequest != null && wPayInitResponse!=null) {
                qrMsg = "Wibmo InApp payment";
            } else {
                sendAbort(WibmoSDK.RES_CODE_FAILURE_SYSTEM_ABORT, "SDK Browser - InitReq was null");
                return;
            }
        }

        resultSet = false;

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);

        LayoutInflater inflator = getLayoutInflater();
        view = inflator.inflate(R.layout.activity_inapp_browser, null, false);
        view.startAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left));
        setContentView(view);

        webView = (WebView) findViewById(R.id.webView);
        mainView = (FrameLayout) findViewById(R.id.layout_root);


        final Activity activity = this;

        webView.setWebViewClient(new WebViewClient() {
            boolean stopCalled = false;

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if(WibmoSDKConfig.isTestMode()) {
                    Log.w(TAG, "We have bad certificate.. but this is test mode so okay");
                    Toast.makeText(activity, "Test Mode!! We have bad certificate.. but this is test mode so okay",
                            Toast.LENGTH_SHORT).show();
                    handler.proceed(); // Ignore SSL certificate errors
                } else {
                    Log.w(TAG, "We have bad certificate.. but this is not test!! will abort");
                    Toast.makeText(activity, "We have bad certificate.. will abort!!", Toast.LENGTH_LONG);
                    handler.cancel();
                }
            }

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Log.e(TAG, "errorCode: "+errorCode+"; description: " + description + "; " + failingUrl);

                InAppUtil.manageWebViewOnError(activity, webView, errorCode, description, failingUrl, null);
            }

            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished: ->" + url+"<-" + stopCalled);
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "Url " + url,
                            Toast.LENGTH_SHORT).show();
                    */
                }

                view.zoomOut();
            }
        });

        //---
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

        //webSettings.setLoadWithOverviewMode(true);
        //webSettings.setUseWideViewPort(true);

        //webView.setPadding(0, 0, 0, 0);
        //webView.setInitialScale(1);

        Log.d(TAG, "Webview: " + webView);
        //--



        final ProgressBar webViewProgressBar = (ProgressBar) findViewById(R.id.web_view_progress_bar);

        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different
                // scales.
                // The progress meter will automatically disappear when we
                // reach 100%
                Log.i(TAG, "progress: " + progress);
                webViewProgressBar.setProgress(progress);
                if(progress==100) {
                    webViewProgressBar.setVisibility(View.GONE);
                } else if(webViewProgressBar.getVisibility()!=View.VISIBLE) {
                    webViewProgressBar.setVisibility(View.VISIBLE);
                }

                String url = view.getUrl();

                if(isViewSmall && url.indexOf(WibmoSDKConfig.getWibmoNwDomainOnly())==-1) {
                    changeSizeToFull();
                } else if(isViewSmall==false && url.indexOf(WibmoSDKConfig.getWibmoNwDomainOnly())!=-1) {
                    changeSizeToSmall();
                }
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                Log.i(TAG, "onJsConfirm");
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setMessage(message)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                result.cancel();
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .setPositiveButton(activity.getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .create();
                dialog.show();
                return true;
            }
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                Log.i(TAG, "onJsAlert");
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setMessage(message)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                result.cancel();
                            }
                        })
                        .setPositiveButton(activity.getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .create();
                dialog.show();
                return true;
            }
        });

        class MyJavaScriptInterface {
            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyAbort() {
                sendAbort(WibmoSDK.RES_CODE_FAILURE_USER_ABORT, "SDK Browser - JS");
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyFailure(String resCode, String resDesc) {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifyFailure called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                sendFailure(resCode, resDesc);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifySuccess(String resCode, String resDesc,
                    String dataPickUpCode, String wTxnId, String msgHash) {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifySuccess called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                _recordSuccess(resCode, resDesc,
                        dataPickUpCode, wTxnId, msgHash);
                _notifyCompletion();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void recordSuccess(String resCode, String resDesc,
                                      String dataPickUpCode, String wTxnId, String msgHash) {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifySuccess called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                _recordSuccess(resCode, resDesc,
                        dataPickUpCode, wTxnId, msgHash);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyCompletion() {
                if(WibmoSDKConfig.isTestMode()) {
                    /*
                    Toast.makeText(activity, "notifySuccess called",
                            Toast.LENGTH_SHORT).show();
                            */
                }
                _notifyCompletion();
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void toast(String msg) {
                Log.d(TAG, "alert: " + msg);
                showToast(msg);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void alert(String msg) {
                Log.d(TAG, "alert: "+msg);
                showMsg(msg);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void log(String msg) {
                Log.v(TAG, "log: " + msg);
            }

            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void userCancel() {
                Log.d(TAG, "userCancel");
                onBackPressed();
            }
        }
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "WibmoSDK");
        //webView.clearCache(true);

        if(w2faInitResponse!=null) {
            webView.postUrl(w2faInitResponse.getWebUrl(), "a=b".getBytes());
            Log.i(TAG, "web posting to " + w2faInitResponse.getWebUrl());
        }

        if(wPayInitResponse!=null) {
            webView.postUrl(wPayInitResponse.getWebUrl(), "a=b".getBytes());
            Log.i(TAG, "web posting to " + wPayInitResponse.getWebUrl());
        }

        //webView.requestFocus();
    }

    private void askRetryOnError(final  WebView webView) {
        String msg = getString(com.enstage.wibmo.sdk.R.string.msg_internet_issue);

        final Activity activity = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(
                        activity.getString(com.enstage.wibmo.sdk.R.string.label_try_again),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.v(TAG, "Calling reload");
                                Toast.makeText(activity, activity.getString(R.string.label_pl_wait_reloading),
                                        Toast.LENGTH_SHORT).show();
                                webView.reload();
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
                            }
                        });

        Dialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e, e);
        }
    }

    private void changeSizeToFull() {
        Log.i(TAG, "changeSizeToFull "+webView.getUrl());

        isViewSmall = false;
        mainView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        /*
        //webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setInitialScale(1);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        */
    }

    private void changeSizeToSmall() {
        Log.i(TAG, "changeSizeToSmall "+webView.getUrl());

        isViewSmall = true;
        mainView.setLayoutParams(new FrameLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.activity_inapp_browser_width), ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        /*
        //webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.getSettings().setUseWideViewPort(false);
        webView.setInitialScale(0);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        */
    }


    private void sendAbort() {
        sendAbort(WibmoSDK.RES_CODE_FAILURE_USER_ABORT, "sdk browser - user abort");
    }

    private void sendAbort(String resCode, String resDesc) {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, resCode);
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, resDesc);

        if(w2faInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, w2faInitResponse.getWibmoTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, w2faInitResponse.getTransactionInfo().getMerTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, w2faInitResponse.getTransactionInfo().getMerAppData());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, wPayInitResponse.getWibmoTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, wPayInitResponse.getTransactionInfo().getMerTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, wPayInitResponse.getTransactionInfo().getMerAppData());
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void sendFailure(String resCode, String resDesc) {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, resCode);
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, "sdk browser - "+resDesc);

        if(w2faInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, w2faInitResponse.getWibmoTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, w2faInitResponse.getTransactionInfo().getMerTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, w2faInitResponse.getTransactionInfo().getMerAppData());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, wPayInitResponse.getWibmoTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, wPayInitResponse.getTransactionInfo().getMerTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, wPayInitResponse.getTransactionInfo().getMerAppData());
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void _recordSuccess(String resCode, String resDesc,
            String dataPickUpCode, String wTxnId, String msgHash) {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_CODE, resCode);
        resultData.putExtra(InAppUtil.EXTRA_KEY_RES_DESC, resDesc);

        resultData.putExtra(InAppUtil.EXTRA_KEY_DATA_PICKUP_CODE, dataPickUpCode);
        resultData.putExtra(InAppUtil.EXTRA_KEY_WIBMO_TXN_ID, wTxnId);
        resultData.putExtra(InAppUtil.EXTRA_KEY_MSG_HASH, msgHash);

        if(w2faInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, w2faInitResponse.getTransactionInfo().getMerTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, w2faInitResponse.getTransactionInfo().getMerAppData());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_TXN_ID, wPayInitResponse.getTransactionInfo().getMerTxnId());
            resultData.putExtra(InAppUtil.EXTRA_KEY_MER_APP_DATA, wPayInitResponse.getTransactionInfo().getMerAppData());
        }

        setResult(Activity.RESULT_OK, resultData);

        resultSet = true;
    }

    private void _notifyCompletion() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(resultCode, data);
        finish();
    }


    public void processBackAction() {
        if(resultSet) {
            Log.v(TAG, "resultSet was true");
            _notifyCompletion();
        } else {
            Log.v(TAG, "resultSet was false");
            sendAbort();
        }
    }

    @Override
    public void onBackPressed() {
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(
                activity.getString(R.string.confirm_iap_cancel))
                .setPositiveButton(
                        activity.getString(R.string.label_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                processBackAction();
                            }
                        })
                .setNegativeButton(
                        activity.getString(R.string.label_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                if (dialog != null) {
                                    try {
                                        dialog.dismiss();
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, "Error: " + e);
                                    }
                                }
                            }
                        });

        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        try {
            dialog.show();
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e, e);

            processBackAction();
        }
    }

    @SuppressLint("NewApi")
    protected void showMsg(String msg) {
        Log.d(TAG, msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        AlertDialog alert = builder.create();
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setCancelable(false);
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

        final Activity activity = this;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
                View view = toast.getView();

                try {
                    toast.show();
                } catch(Throwable e) {
                    Log.e(TAG, "error: " + e, e);
                }
            }
        });
    }
}
