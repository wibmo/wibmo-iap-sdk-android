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

public class InAppShellJavaScriptInterface {
    private static final String TAG = "InAppShellJSInterface";

    private InAppShellHepler inAppShellHepler;
    private String callbackMethodName;

    public InAppShellJavaScriptInterface(InAppShellHepler inAppShellHepler) {
        this.inAppShellHepler = inAppShellHepler;
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void toast(String msg) {
        Log.d(TAG, "alert: " + msg);
        inAppShellHepler.showToast(msg);
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void alert(String msg) {
        Log.d(TAG, "alert: "+msg);
        inAppShellHepler.showMsg(msg);
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void openUrl(String url) {
        Log.d(TAG, "url: "+url);
        inAppShellHepler.openUrl(url);
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void log(String msg) {
        Log.v(TAG, "log: " + msg);
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void close() {
        Log.d(TAG, "close");
        inAppShellHepler.getActivity().finish();
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void setCallbackForTxnId(String methodName) {
        Log.d(TAG, "setCallbackForTxnId : "+methodName);

        this.callbackMethodName = methodName;
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void doIAPWPay(String wPayInitRequest, String returnUrl) {
        inAppShellHepler.setResponseUrl(returnUrl);
        Log.d(TAG, "IAPWPay : "+wPayInitRequest);
        Log.d(TAG, "responseUrl : "+ inAppShellHepler.getResponseUrl());

        inAppShellHepler.processIAP(wPayInitRequest);
    }

    @android.webkit.JavascriptInterface
    @SuppressWarnings("unused")
    public void scrollToTop() {
        inAppShellHepler.getWebView().scrollTo(0, 0);
    }

    public void sendWibmoTxnId(String wibmoTxnId, String merTxnId) {
        if(callbackMethodName!=null) {
            callJavaScript(callbackMethodName, wibmoTxnId, merTxnId);
        }
    }

    private void callJavaScript(String methodName, Object...params){
        Log.i(TAG, "callJavaScript: " + methodName + "; p: "+params);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if(param instanceof String){
                stringBuilder.append("'");
                stringBuilder.append(param);
                stringBuilder.append("'");
            }
            if(i < params.length - 1){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(")}catch(error){alert('error: '+error.message);}");

        inAppShellHepler.getWebView().post(new Runnable() {
            @Override
            public void run() {
                inAppShellHepler.getWebView().loadUrl(stringBuilder.toString());
            }
        });
    }
}
