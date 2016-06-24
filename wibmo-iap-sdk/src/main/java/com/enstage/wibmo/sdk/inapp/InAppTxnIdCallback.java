package com.enstage.wibmo.sdk.inapp;

import android.content.Context;

/**
 * Created by Akshathkumar Shetty on 19/02/16.
 */
public interface InAppTxnIdCallback {
    public boolean recordInit(Context context, String wibmoTxnId, String merTxnId);
}
