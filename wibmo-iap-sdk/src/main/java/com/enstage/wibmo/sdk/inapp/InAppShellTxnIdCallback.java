package com.enstage.wibmo.sdk.inapp;

import android.content.Context;

/**
 * Created by Akshathkumar Shetty on 25/04/16.
 */
public class InAppShellTxnIdCallback implements InAppTxnIdCallback {
    private InAppShellJavaScriptInterface jsInterface;

    public InAppShellTxnIdCallback(InAppShellJavaScriptInterface jsInterface) {
        this.jsInterface = jsInterface;
    }

    @Override
    public boolean recordInit(Context context, String wibmoTxnId, String merTxnId) {
        jsInterface.sendWibmoTxnId(wibmoTxnId, merTxnId);
        return true;
    }
}
