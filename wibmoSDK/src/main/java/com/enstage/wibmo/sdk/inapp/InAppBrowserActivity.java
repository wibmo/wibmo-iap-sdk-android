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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.enstage.wibmo.sdk.R;
import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitResponse;

/**
 * Created by akshath on 20/10/14.
 */
public class InAppBrowserActivity extends Activity {
    private static final String TAG = "wibmo.sdk.InAppBrowser";

    public static final int REQUEST_CODE = 0x0000c0c0; // Only use bottom 16 bits

    private View view = null;

    private Context context = null;

    private String qrMsg;

    private View mainView;
    private WebView webView;

    private W2faInitRequest w2faInitRequest;
    private W2faInitResponse w2faInitResponse;

    private WPayInitRequest wPayInitRequest;
    private WPayInitResponse wPayInitResponse;

    private boolean resultSet;


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
                sendAbort();
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
                Log.e(TAG, "onReceivedError: " + description+"; "+failingUrl);
                Toast.makeText(activity, "Oh no! " + description,
                        Toast.LENGTH_SHORT).show();
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
                    webViewProgressBar.setVisibility(View.INVISIBLE);
                } else if(webViewProgressBar.getVisibility()==View.INVISIBLE) {
                    webViewProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        class MyJavaScriptInterface {
            @android.webkit.JavascriptInterface
            @SuppressWarnings("unused")
            public void notifyAbort() {
                sendAbort();
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



    private void sendAbort() {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra("ResCode", "204");
        resultData.putExtra("ResDesc", "user abort");

        if(w2faInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
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
        resultData.putExtra("ResCode", resCode);
        resultData.putExtra("ResDesc", resDesc);

        if(w2faInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
        } else if(wPayInitResponse!=null) {
            resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
        }

        setResult(Activity.RESULT_CANCELED, resultData);
        finish();
    }

    private void _recordSuccess(String resCode, String resDesc,
            String dataPickUpCode, String wTxnId, String msgHash) {
        //webView.destroy();

        Intent resultData = new Intent();
        resultData.putExtra("ResCode", resCode);
        resultData.putExtra("ResDesc", resDesc);

        resultData.putExtra("DataPickUpCode", dataPickUpCode);
        resultData.putExtra("WibmoTxnId", wTxnId);
        resultData.putExtra("MsgHash", msgHash);

        if(w2faInitResponse!=null) {
            //resultData.putExtra("WibmoTxnId", w2faInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", w2faInitResponse.getTransactionInfo().getMerTxnId());
        } else if(wPayInitResponse!=null) {
            //resultData.putExtra("WibmoTxnId", wPayInitResponse.getWibmoTxnId());
            resultData.putExtra("MerTxnId", wPayInitResponse.getTransactionInfo().getMerTxnId());
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

    @Override
    public void onBackPressed() {
        if(resultSet) {
            Log.v(TAG, "resultSet was true");
            _notifyCompletion();
        } else {
            Log.v(TAG, "resultSet was false");
            sendAbort();
        }
        super.onBackPressed();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void showWait(boolean flag) {

    }

    public void startWait() {

    }

    public void stopWait() {

    }

    public Activity getActivity() {
        return this;
    }
}
